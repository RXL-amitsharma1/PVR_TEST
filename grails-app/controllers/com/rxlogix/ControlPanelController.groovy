package com.rxlogix

import com.rxlogix.cmis.AdapterFactory
import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.SensitivityLabelEnum
import com.rxlogix.jobs.InboundComplianceJob
import com.rxlogix.pvdictionary.ProductDictionaryMetadata
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.AIEmailPreference
import com.rxlogix.user.Preference
import com.rxlogix.user.ReportRequestEmailPreference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.commons.io.FileUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import com.rxlogix.localization.Localization
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.utils.HtmlConverter
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import org.xhtmlrenderer.pdf.ITextRenderer

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class ControlPanelController {
    static final String TEMP_FILE_NAME = "test.txt"

    def ldapService
    def CRUDService
    def reportFieldService
    def userService
    def seedDataService
    def quartzScheduler
    def dmsService
    GrailsApplication grailsApplication
    def applicationSettingsService
    def medDraChangeService
    def fileService
    def odataService
    def importJsonService
    def hazelService
    def dataSource_pva
    static int USER_COLUMN_NUMBER = 11

    def index() {

        def applicationSettingsInstance = ApplicationSettings.first()

        if (!applicationSettingsInstance) {
            notFound()
            return
        }

        render view: "index", model: [applicationSettingsInstance: applicationSettingsInstance, selectedLocale: userService.currentUser.preference.locale,
                                      inboundInitialConfiguration: InboundInitialConfiguration.first(), fields : reportFieldService.getAllReportFieldsWithGroupsForTemplates(), defaultDmsSettings: defaultDmsSettings]
    }

    def odataConfig() {
        OdataSettings settings = OdataSettings.get(params.id) ?: new OdataSettings()
        [pvaTables: odataService.getDsTables(settings.dsName), dbSource: settings]
    }

    def getDsTableFields() {
        render text: odataService.getDsTableFields(params.dsName, params.tableName) as JSON, contentType: 'application/json'
    }

    def saveOdataConfig() {
        OdataSettings settings = OdataSettings.get(params.id) ?: new OdataSettings()
        try {
            bindData(settings, params, [exclude: ["dsPassword"]])
            if (params.dsPassword)
                settings.setPasswordEncoded(params.dsPassword)
            CRUDService.saveOrUpdate(settings)
        } catch (ValidationException ve) {
            render view: "odataConfig", model: [pvaTables: odataService.getDsTables(settings.dsName), dbSource: settings]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.odataSource.label'), ""])
        redirect(action: 'odataConfig', id: settings.id)
    }

    def odataSources() {
    }

    def odataSourceList() {
        render text: OdataSettings.findAllByIsDeleted(false) as JSON, contentType: 'application/json'
    }

    def deleteOdataSource() {
        OdataSettings s = OdataSettings.get(params.id)
        CRUDService.softDelete(s, s.dsName, params.deleteJustification)
        flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.odataSource.label'), ""])
        redirect(action: "odataSources")
    }

    def update(ApplicationSettings applicationSettingsInstance) {
        if (applicationSettingsInstance == null) {
            notFound()
            return
        }

        try {
            applicationSettingsInstance = (ApplicationSettings) CRUDService.update(applicationSettingsInstance)
        } catch (ValidationException ve) {
            redirect action: "index"
            return
        }

        reportFieldService.clearCacheReportFields()
        applicationSettingsService.reload()

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'applicationSettings.label'), ""])
                redirect action: "index"
            }
            '*' { respond applicationSettingsInstance, [status: OK] }
        }
    }

    def mirrorLdapValues() {
        ldapService.mirrorLdapValues()
        flash.message = message(code: "app.ldap.mirror.success")
        redirect action: "index"
    }

    def refreshCaches() {
        log.info("RefreshCaches has been called by: ${userService.user?.username}")
        try {
            ProductDictionaryMetadata.withNewSession { session ->
                quartzScheduler.standby()
                seedDataService.seedPVAData()
                resetDictionaryView()
                session.flush()
                session.clear()
                ConfigObject hazelcast = grailsApplication.config.hazelcast
                if (hazelcast.enabled) {
                    String reportFieldCacheChannel = hazelcast.notification.reportFieldCache
                    hazelService.publishToTopic(reportFieldCacheChannel, "Refresh Report Field Cache ")
                }
                else {
                    reportFieldService.reLoadValuesToCacheFile()
                    Localization.resetAll()
                    reportFieldService.clearAllCaches()
                }
                flash.message = message(code: "app.clearAll.caches.success")
                log.info("RefreshCaches has been completed")
            }
        } catch (Exception ex) {
            log.error("Error while refreshing metadata and caches", ex)
            flash.error = message(code: "app.clearAll.caches.error")
        } finally {
            quartzScheduler.start()
        }
        redirect(controller: "dashboard", action: "index")
    }

    def updateMedDra() {
        try {
            render(medDraChangeService.bulkUpdate(JSON.parse(params.data)) as JSON)
        } catch (Exception e) {
            log.error("Error during controlPanel -> updateMedDra", e)
            StringWriter errors = new StringWriter();
            e.print(new PrintWriter(errors));
            String exceptionAsString = errors.toString();
            String[] str = exceptionAsString.split(" ");
            String exception = ""
            for(int i=1;i<str.length;i++){
                exception = exception+str[i]+" ";
            }

            render(status: 500, text: exception)
        }
    }

    def downloadPdfFromHtml(){
        try {
            // Sanitize params to prevent vulnerabilities
            String htmlContent = sanitizeHtml(params.data)
            OutputStream pdfOutputStream = new ByteArrayOutputStream()

            // Generate PDF from HTML content
            HtmlConverter.convertToPdf(htmlContent, pdfOutputStream);

            response.setContentType("application/pdf")
            response.setHeader("Content-Disposition", "attachment; filename=generated.pdf")
            response.outputStream << pdfOutputStream.toByteArray()
            response.outputStream.flush()
            response.outputStream.close()
            response.status = 200
        } catch (Exception ex) {
            log.error("Error generating PDF")
            log.error(ex.getMessage())
            response.status = 500
        }
    }

    def sanitizeHtml(String input) {
        PolicyFactory policy = new HtmlPolicyBuilder().allowElements("p", "h3").toFactory();
        return policy.sanitize(input)
    }

    def medDraAllUsage() {
        try {
            render(medDraChangeService.checkAllUsage(JSON.parse(params.data)) as JSON)
        } catch (Exception e) {
            log.error("Error during controlPanel -> medDraAllUsage", e)
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            render(status: 500, text: errors.toString())
        }
    }

    def medDraUsage() {
        try {
            Integer level = params.level.trim() as Integer
            String from = params.old.trim()
            String to = params.new.trim()
            render(medDraChangeService.checkUsage(level, from) as JSON)
        } catch (Exception e) {
            log.error("Error during controlPanel -> medDraUsage", e)
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            render(status: 500, text: errors.toString())
        }
    }

    def importExcel() {
        Map map = [uploadedValues: "", message: "", success: false]
        MultipartFile file = request.getFile('file')
        List result = []
        Workbook workbook = null

        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }

        Sheet sheet = workbook?.getSheetAt(0);
        Row row;
        String level = null
        String oldValue = null
        String newValue = null
        if (sheet)
            for (int i = 0; i <= sheet?.getLastRowNum(); i++) {
                row = sheet.getRow(i)
                if (row == null) continue
                level = getExcelCell(row, 0)
                oldValue = getExcelCell(row, 1)
                newValue = getExcelCell(row, 2)
                if (!level && !oldValue && !newValue) continue

                if (level && oldValue && newValue) {
                    result << [level: level, old: oldValue, new: newValue]
                } else {
                    map.success = false
                    map.message = "${message(code: 'app.label.required.column.has.no.data.error')}"
                    render(map as JSON)
                    return
                }
            }
        if (result) {
            map.uploadedValues = result
            map.success = true
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render(map as JSON)
    }

    private String getExcelCell(Row row, int i) {
        Cell cell = row?.getCell(i)
        cell?.setCellType(CellType.STRING);
        return cell?.getStringCellValue()?.trim()
    }

    def exportToExcel(){
        Date startTime = new Date()
        log.info("Started generating excel file for export at ${startTime.format(DateUtil.DATEPICKER_UTC_FORMAT)}")
        List entities = []
        boolean qced = params.boolean("qced")
        entities << ReportTemplate.findAllByIsDeletedAndQualityCheckedAndOriginalTemplateId(false, qced, 0L).collect {
            GrailsHibernateUtil.unwrapIfProxy(it) // Issue https://stackoverflow.com/questions/48263366/grails3-hibernate5-proxy-parent-objects-cant-resolve-child-accessors
        }
        entities << SuperQuery.findAllByIsDeletedAndQualityCheckedAndOriginalQueryId(false, qced, 0L).collect {
            GrailsHibernateUtil.unwrapIfProxy(it)
        }
        entities << ReportConfiguration.findAllByIsDeletedAndQualityChecked(false, qced).collect {
            GrailsHibernateUtil.unwrapIfProxy(it)
        }

        //getting service from mainContext needed to use it with scope="request"
        def result = grailsApplication.mainContext.excelExportService.export(entities.flatten())
        ResponseDTO responseDTO = new ResponseDTO()
        responseDTO.data = result
        Date endTime = new Date()
        log.info("Finished generating excel file for export at ${endTime.format(DateUtil.DATEPICKER_UTC_FORMAT)} took ${(endTime.time - startTime.time)/1000} seconds.")
        render(responseDTO as JSON)
    }

    def getExportFile(){
        render ( file: new File(grailsApplication.config.tempDirectory+'/' + params.fileName), fileName: "${grailsApplication.config.excelExportFileName}-${(new Date()).format("yyyy-MM-dd") + (params.part?("-"+params.part):"")}.xlsx", contentType: grailsApplication.config.grails.mime.types.xlsx)
    }

    //Use default as 1 minute
    def deleteReportFiles(Long timeInMilliSeconds) {
        timeInMilliSeconds = timeInMilliSeconds ?: 60000
        log.info("Delete cached report files has been called by: ${userService.user?.username}")
        fileService.deleteOldTempFiles(timeInMilliSeconds)
        log.info("Delete cached report files has been completed")
        flash.message = message(code: 'app.reportfile.delete.success')
        redirect action: "index"
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'applicationSettings.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    def saveDefaultUi() {
        ApplicationSettings applicationSettings = ApplicationSettings.first()
        applicationSettings.defaultUiSettings = "hide"
        CRUDService.update(applicationSettings)
        render "ok"
    }

    def removeDefaultUi() {
        ApplicationSettings applicationSettings = ApplicationSettings.first()
        applicationSettings.defaultUiSettings = ""
        CRUDService.update(applicationSettings)
        render "ok"
    }

    def saveDmsSettings() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        String val=params.dmsSettings?.trim()
        try {
            if (!val) {
                storeDmsConfig("")
                responseDTO.setSuccessResponse(ViewHelper.getMessage("app.label.dms.remove.success"))
            } else {
                def settings = JSON.parse(val)
                if (!settings) {
                    responseDTO.setFailureResponse(message(code: 'app.label.dms.test.json') as String)
                } else {
                    def originalAdapter = dmsService.adapter
                    dmsService.adapter = AdapterFactory.getAdapter(settings);
                    if (sendTestDocument(responseDTO)) {
                        storeDmsConfig(val)
                        responseDTO.setSuccessResponse(ViewHelper.getMessage("app.label.dms.test.success"))
                    } else {
                        dmsService.adapter = originalAdapter
                    }
                }
            }

        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'app.label.dms.save.test.failure') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    private storeDmsConfig(String val) {
        ApplicationSettings applicationSettings = ApplicationSettings.first()
        applicationSettings.dmsIntegration = val
        CRUDService.update(applicationSettings)
        dmsCacheRefresh()
    }

    private boolean sendTestDocument( AjaxResponseDTO responseDTO ){
        File testFile
        try {
            testFile = new File((grailsApplication.config.tempDirectory as String) + "/" + TEMP_FILE_NAME);
            testFile.createNewFile();
            FileUtils.writeStringToFile(testFile, ViewHelper.getMessage("app.label.dms.test.file.content"));
            dmsService.upload(testFile, "", ViewHelper.getMessage("app.label.dms.test.file.name"), ViewHelper.getMessage("app.label.dms.test.file.description"), ViewHelper.getMessage("app.label.dms.test.file.tag"), SensitivityLabelEnum.SENSITIVE.value, userService.currentUser.fullName, null)
            responseDTO.setSuccessResponse(null, ViewHelper.getMessage("app.label.dms.test.success"))
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, (message(code: 'app.label.dms.save.test.failure') as String) +" "+ e.getMessage())
            return false
        }
        finally {
            testFile?.delete()
        }
        return true
    }
    def testDmsSettings() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        sendTestDocument(responseDTO)
        render(responseDTO.toAjaxResponse())
    }


    String defaultDmsSettings = """
    {
    "dmsName":"alfresco",
    "login":"admin",
    "password":"123456",
    "cmisUrl":"http://127.0.0.1:9080/alfresco/api/-default-/public/cmis/versions/1.1/browser",
    "repositoryId":"-default-",
    "rootFolder":"/Published",
    "authorId":"cm:author",
    "documentTypeId":"D:d3:d3",
    "nameId":"cmis:name",
    "descriptionId":"cmis:description",
    "sensitivityId":"d3:priv2",
    "tagId":"d3:tag2"
    }
    """

    private void resetDictionaryView() {
        PVDictionaryConfig.initialize(ProductDictionaryMetadata.findAllByDisplay(true))
    }

    def importQueriesJson() {
        MultipartFile file = request.getFile('file')
        AjaxResponseDTO responseDTO = importJsonService.importJSON(file, "fetchSuperQuery")
        render(responseDTO.toAjaxResponse())
    }

    def importTemplatesJson() {
        MultipartFile file = request.getFile('file')
        AjaxResponseDTO responseDTO = importJsonService.importJSON(file, "fetchReportTemplate")
        render(responseDTO.toAjaxResponse())
    }

    def importConfigurationsJson() {
        MultipartFile file = request.getFile('file')
        AjaxResponseDTO responseDTO = importJsonService.importJSON(file, "fetchReportConfiguration")
        render(responseDTO.toAjaxResponse())
    }

    def importDashboardsJson() {
        MultipartFile file = request.getFile('file')
        AjaxResponseDTO responseDTO = importJsonService.importJSON(file, "fetchDashboards")
        render(responseDTO.toAjaxResponse())
    }

    def encryptAllReportData() {
        if (!Holders.config.getProperty('pvreports.encrypt.data', Boolean)) {
            flash.error = message(code: 'app.encryptAll.config.reportData.error')
            redirect(controller: "controlPanel", action: "index")
            return
        }
        log.info("EncryptAllReportData has been called by: ${userService.user?.username} for ${ReportResultData.countByIsEncrypted(false)} reports")
        try {
            ReportResultData.findAllByIsEncrypted(false).each {
                it.encryptedValue = it.decryptedValue
                it.save()
            }
            flash.message = message(code: "app.encryptAll.reportData.success")
        } catch (Exception e) {
            log.error("Error occurred during EncryptAllReportData ", e)
            flash.error = message(code: "app.encryptAll.reportData.error")
        }
        log.info("EncryptAllReportData has been completed")
        redirect(controller: "dashboard", action: "index")
    }


    def decryptAllReportData() {
        log.info("DecryptAllReportData has been called by: ${userService.user?.username} for ${ReportResultData.countByIsEncrypted(true)} reports")
        try {
            ReportResultData.findAllByIsEncrypted(true).each {
                it.value = it.decryptedValue
                it.isEncrypted = false
                it.save()
            }
            flash.message = message(code: "app.decryptAll.reportData.success")
        } catch (Exception e) {
            log.error("Error occurred during EncryptAllReportData ", e)
            flash.error = message(code: "app.decryptAll.reportData.error")
        }
        log.info("DecryptAllReportData has been completed")
        redirect(controller: "dashboard", action: "index")
    }

    private void dmsCacheRefresh() {
        try {
            ConfigObject hazelcast = grailsApplication.config.hazelcast
            if (hazelcast.enabled) {
                String dmsCacheChannel = hazelcast.notification.dmsCache
                hazelService.publishToTopic(dmsCacheChannel, "RefreshCache")
            } else {
                applicationSettingsService.dmsCacheRefresh()
            }
        } catch (Exception ex) {
            log.error("Error while pushing refresh DMS cache " + ex)
        }
    }

    def resetAutoRODJobIfInProgress(){
        try{
            AutoReasonOfDelay autoReasonOfDelay = AutoReasonOfDelay.first()
            if(autoReasonOfDelay?.executing) {
                autoReasonOfDelay.executing = false
                CRUDService.save(autoReasonOfDelay);
            }
            flash.message = message(code: "controlPanel.reset.autoROD.success")
            redirect(controller: "controlPanel", action: "index")
            return
        }catch(Exception ex) {
            log.error("Error while resetting the Auto Reason of Delay : " +ex)
        }
    }

    def saveInboundIntialConf() {
        //Map model = [:]
        Sql sql = new Sql(dataSource_pva)
        InboundInitialConfiguration inboundInitialConfiguration = InboundInitialConfiguration.first()
        try{
            if(inboundInitialConfiguration) {
                inboundInitialConfiguration.setIsICInitialize(true)
                User user = userService.currentUser
                Locale locale = user?.preference?.locale
                inboundInitialConfiguration.startDate = params.startDate ? DateUtil.getStartDate(params.startDate,locale) : null
            } else {
                inboundInitialConfiguration = new InboundInitialConfiguration()
                inboundInitialConfiguration.setIsICInitialize(false)
            }
            inboundInitialConfiguration.reportField = ReportField.get(params.long('reportFieldId'))
            inboundInitialConfiguration.caseDateLogicValue = true
            int caseDateLogicValue =  1
            if(params.caseDateLogicValue == 'false'){
                inboundInitialConfiguration.setCaseDateLogicValue(false)
                caseDateLogicValue = 0
            }
            CRUDService.saveOrUpdate(inboundInitialConfiguration)
            sql.call("{call PKG_INBOUND_COMPLIANCE.P_POP_SENDER_FIELD(?,?)}", [inboundInitialConfiguration.reportField?.name,caseDateLogicValue])
            if(inboundInitialConfiguration && inboundInitialConfiguration.isICInitialize) {
                flash.message = message(code: "app.sender.initialize.triggered.msg")
                InboundComplianceJob.triggerNow()
            }else {
                flash.message = message(code: "app.sender.initialize.successful.msg")
            }
        } catch (ValidationException ve) {
            inboundInitialConfiguration.errors = ve.errors
            def applicationSettingsInstance = ApplicationSettings.first()
            if (!applicationSettingsInstance) {
                notFound()
                return
            }
            render view: "index", model: [applicationSettingsInstance: applicationSettingsInstance, selectedLocale: userService.currentUser.preference.locale,
                                          inboundInitialConfiguration: inboundInitialConfiguration, fields : reportFieldService.getAllReportFieldsWithGroupsForTemplates(), defaultDmsSettings: defaultDmsSettings]
            return
        } catch(Exception ex) {
            log.error("Error while setting Inbound Initial Configuration : " +ex)
        }finally {
            sql?.close()
        }
        redirect(controller: "controlPanel", action: "index")
    }

    def reloadLocalization(){
        Localization.reload(true)
        render "ok"
    }
    private List parseFile(Workbook workbook, int columnNumber) {
        Sheet sheet = workbook?.getSheetAt(0)
        Row row
        List finalData = []
        if (sheet) {
            List Headers = []
            row = sheet.getRow(0)
            for (int i = 0; i < columnNumber; i++) {
                Headers.add(getExcelCell(row, i))
            }



            for (int i = 1; i <= sheet?.getLastRowNum(); i++) {
                if ((row = sheet.getRow(i)) != null) {
                    Map<String, ArrayList> map = [:]
                    for (int j = 0; j < columnNumber; j++) {
                        map.put(Headers[j], getExcelCell(row, j))
                    }
                    finalData.add(map)
                }
            }
        }
        return finalData
    }

    private static Workbook getExcelWorkbook(request) {
        MultipartFile file = request.getFile('excelFile')
        Workbook workbook
        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream)
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream)
        }
        return workbook
    }

    def addUsers() {
        List model = parseFile(getExcelWorkbook(request), USER_COLUMN_NUMBER)
        if (model.isEmpty()) {
            flash.error = message(code: 'app.import.file.empty')
            redirect(action: 'index')
        } else {
            Integer userCount = 0
            model.each { it ->
                String fullName = it.FULL_NAME ? it.FULL_NAME.trim() : null
                String userName = it.USERNAME.toString().trim()
                if (userName.length() > 0 && !User.findByUsernameIlike(userName) && it.USERNAME!=null) {
                    List allGroupsAdded = userService.getAllGroupsForUser(it.GROUP_NAMES)
                    List allRolesAdded = userService.getAllRolesForUser(it.ROLES)
                    List allTimeZones = ViewHelper.getTimezoneValues()
                    Map timeZoneMap = (it.TIMEZONE) ? (allTimeZones.find { time -> (it.TIMEZONE.toString().trim() == time.display.toString().trim())?.collect{it as String} }) : (['name': 'UTC'])
                    String locale = (it.LANGUAGE) ? ((it.LANGUAGE.toString().toLowerCase().trim() == 'japanese' || it.LANGUAGE.toString().toLowerCase().trim() == 'ja') ? 'ja' : 'en') : 'en'
                    Preference preference = new Preference(timeZone: timeZoneMap.name, locale: locale)
                    preference = userService.setOwnershipAndModifier(preference)
                    User userInstance = userService.addUsers(it as Map, preference, fullName)
                    try {
                        userInstance = (User) CRUDService.save(userInstance)
                        userCount++
                        userService.addAllGroupsAndRolesForUser(userInstance , allGroupsAdded , allRolesAdded)
                    }
                    catch (ValidationException ve) {
                        log.warn("Validation Error Occured on User '${it.USERNAME}'" , ve.getMessage())
                        flash.error += ("Error Occured on User ${it.USERNAME} ->" + ve.getMessage())
                    }
                    catch(Exception ex){
                        log.error("Exception Occured due to" , ex)
                        return
                    }
                }

            }
            log.info("Users Import Completed SuccessFully")
            if(userCount == 0){
                flash.warn = "No Users were added"
            }
            else {
                flash.message = message(code: 'controlPanel.add.users', args: [userCount])
            }
            redirect(controller: "controlPanel",action: "index")
        }
    }

    def downloadUserTemplate(){
        render(file: this.class.classLoader.getResourceAsStream("export/Users.xlsx"), fileName: "userTemplate.xlsx", contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
    }

    void AddTargetCell(Workbook workbook , Sheet sheet){
        String lastCellText = "TENANT"
        Row headerRow = sheet.getRow(0)
        if(headerRow){
            Cell copyCell = headerRow.getCell(0)
            Cell lastCell = headerRow.createCell(headerRow.getLastCellNum())
            if(copyCell.cellStyle){
                CellStyle targetCellStyle = copyCell.getCellStyle()
                lastCell.setCellStyle(targetCellStyle)
                lastCell.setCellValue(lastCellText)
            }
        }
    }

    def downloadAllUsers() {
        try {
            log.info("Started Uploading Users to Excel")
            def fileInputStream = this.class.classLoader.getResourceAsStream("export/Users.xlsx")
            Workbook workbook = new XSSFWorkbook(fileInputStream)
            Sheet sheet = workbook.getSheetAt(0)
            AddTargetCell(workbook, sheet)
            userService.writeUsersToExcel(sheet)
            if (workbook) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                response.setHeader("Content-Disposition", "attachment; filename=Users.xlsx")
                workbook.write(response.outputStream)
                workbook.close()
                response.status = 200
            }
            log.info("Excel Uploading Successfully Done")
        }
        catch (Exception ex){
            log.error("Error generating User Excel")
            log.error(ex.getMessage())
            response.status = 500
        }
    }

}
