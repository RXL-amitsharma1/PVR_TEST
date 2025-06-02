package com.rxlogix

import com.rxlogix.config.DistinctTable
import com.rxlogix.config.ExcludeCase
import com.rxlogix.config.IncludeCase
import com.rxlogix.config.BalanceMinusQuery
import com.rxlogix.config.BmQuerySection
import com.rxlogix.config.SourceProfile
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import com.rxlogix.api.SanitizePaginationAttributes
import grails.converters.JSON
import grails.validation.ValidationException
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import com.rxlogix.Constants
import com.rxlogix.util.DateUtil

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

/* author : anurag kumar gupta
* controller : BalanceMinusQueryController
* definition : This controller is used for Balance and minus query,
 */
@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class BalanceMinusQueryController implements SanitizePaginationAttributes{

    def balanceMinusQueryService
    def userService
    def CRUDService
    def dataSource_pva
    GrailsApplication grailsApplication

    /*method : index
    * definition : This method is used export option, and show the default selected central dataSource
    */
    def index(Long sourceProfileId) {
        render view: "index", model: [ sourceProfiles: SourceProfile.sortedCentralAndAffliateSourceProfiles(), sourceProfileId: sourceProfileId ?: SourceProfile.getCentral().id]
    }

    /*method : fetchBalanceMinusQueryList
    * definition : This method is used fetch the summary for Balance and minus query
    */
    def fetchBalanceMinusQueryList(Long sourceProfileId) {
        SourceProfile sourceProfile = SourceProfile.findById(sourceProfileId)
        if(!sourceProfile) {
            return ([aaData: [], recordsTotal: 0, recordsFiltered: 0] as JSON)
        }
        String tableName = sourceProfile.isCentral ? "DATAVAL_EXEC_STATUS_VW" : "DATAVAL_EXEC_STATUS_VW_AFF"
        sanitize(params)
        Map map = balanceMinusQueryService.fetchBalanceMinusQueryList(params, tableName)
        render (map as JSON)
    }

    /*method : create
    * definition : This method is used to open the form for creating the Balance and minus query
    */
    def create() {
        BalanceMinusQuery balanceMinusQuery = BalanceMinusQuery.first()
        if(balanceMinusQuery){
            redirect(action: "edit", id: balanceMinusQuery.getId())
            return
        }
        List<SourceProfile> sourceProfiles = SourceProfile.sortedCentralAndAffliateSourceProfiles()
        render view: "create", model: [bmQueryInstance: balanceMinusQuery, sourceProfiles: sourceProfiles, centralSource : SourceProfile?.central]
    }

    /*method : fetchDistTables
    * definition : This method is used fetch the distinct table list based on source profile name
    */
    def fetchDistTables(String sourceProfileName) {
        Sql sql = new Sql(dataSource_pva)
        def balanceMinusDistTableList
        try {
            SourceProfile sourceProfile = SourceProfile.findBySourceName(sourceProfileName)
            if(sourceProfile && sourceProfile.isCentral) {
                balanceMinusDistTableList = sql.rows("SELECT * from DATAVAL_TABLE_LIST_VW")
            }else {
                balanceMinusDistTableList = sql.rows("SELECT * from DATAVAL_TABLE_LIST_VW_AFF")
            }
        } catch (Exception ex) {
            log.error("Unknown exception occurred fetching table list getCentralDistTable()" + ex.getMessage())
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        render([items : balanceMinusDistTableList.collect { [id : it.TABLE_LIST, text : it.TABLE_LIST]}] as JSON)
    }

    /*method : edit
    * definition : This method is used open the existing form for balance and minus query
    */
    def edit(Long id) {
        BalanceMinusQuery balanceMinusQueryInstance = id ? BalanceMinusQuery.read(id) : null
        if (!balanceMinusQueryInstance) {
            notFound()
            return
        }
        List<SourceProfile> sourceProfileList = SourceProfile.sortedCentralAndAffliateSourceProfiles()
        List<BmQuerySection> bmQuerySectionsList = []
        sourceProfileList.each { it ->
            BmQuerySection bmQuerySection = BmQuerySection.findBySourceProfile(it)
            if(bmQuerySection) {
                bmQuerySectionsList.add(bmQuerySection)
            }
        }
        balanceMinusQueryInstance.bmQuerySections = bmQuerySectionsList
        render view: "edit", model: [bmQueryInstance: balanceMinusQueryInstance, sourceProfiles: SourceProfile.sortedCentralAndAffliateSourceProfiles()]
    }

    /*method : save
    * definition : This method is used save the newly added data in balance and minus query
    */
    @Transactional
    def save(){
        if (request.method == 'GET') {
            notSaved()
            return
        }
        BalanceMinusQuery bmQueryInstance = new BalanceMinusQuery()
        try{
            populateModel(bmQueryInstance)
            bmQueryInstance = preValidateTemplate(bmQueryInstance, params)
            if(bmQueryInstance.hasErrors()) {
                throw new ValidationException("Balance Minus Query preValidate has added validation issues", bmQueryInstance.errors)
            }
            Date startDate = Date.parse(DateUtil.SCHEDULE_DATE_JSON_FORMAT,bmQueryInstance.startDateTime)
            if(getDate(startDate) < getDate(new Date())){
                flash.error = message(code: "save.start.date.time.balanceMinusQuery")
                render view: "create", model: [bmQueryInstance: bmQueryInstance, sourceProfiles: SourceProfile.sortedCentralAndAffliateSourceProfiles(), centralSource : null]
                return
            }

            for (bmQuerySection in bmQueryInstance?.bmQuerySections) {
                if (bmQuerySection.executeFor == 'ETL_START_DATE') {
                    if (bmQuerySection.executionStartDate > bmQuerySection.executionEndDate) {
                        flash.error = message(code: "com.rxlogix.config.startdate.after.enddate")
                        redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
                        return
                    }
                }
            }

            bmQueryInstance = (BalanceMinusQuery) CRUDService.save(bmQueryInstance)
            balanceMinusQueryService.initializeGttTablesForBMQuery(bmQueryInstance)
            flash.message = message(code: 'app.balance.minus.query.successful.msg')
            redirect(action: "index")
            return
        }catch (ValidationException ve) {
            log.warn("BalanceMinusQueryController : Failed to save due to ${ve.message}")
            bmQueryInstance.errors = ve.errors
            render view: "create", model: [bmQueryInstance: bmQueryInstance, sourceProfiles: SourceProfile.sortedCentralAndAffliateSourceProfiles(), centralSource : null]
            return
        }catch (Exception e) {
            log.warn("BalanceMinusQueryController: Exception occurred while saving data : ${e.message}")
            e.printStackTrace()
            flash.error = message(code: "default.server.error.message")
            redirect action: "create"
            return
        }
    }

    /*method : update
    * definition : This method is used update the existing data in balance and minus query
    */
    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        BalanceMinusQuery bmQueryInstance = BalanceMinusQuery.lock(params.id)
        if (!bmQueryInstance) {
            notFound()
            return
        }
        try {
            populateModel(bmQueryInstance)
            Date startDate = Date.parse(DateUtil.SCHEDULE_DATE_JSON_FORMAT,bmQueryInstance.startDateTime)
            if(getDate(startDate) < getDate(new Date())){
                flash.error = message(code: "update.start.date.time.balanceMinusQuery")
                redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
                return
            }
            for (bmQuerySection in bmQueryInstance?.bmQuerySections) {
                if (bmQuerySection.executeFor == 'ETL_START_DATE') {
                    if (bmQuerySection.executionStartDate > bmQuerySection.executionEndDate) {
                        flash.error = message(code: "com.rxlogix.config.startdate.after.enddate")
                        redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
                        return
                    }
                }
            }

            bmQueryInstance = (BalanceMinusQuery) CRUDService.update(bmQueryInstance)
            balanceMinusQueryService.initializeGttTablesForBMQuery(bmQueryInstance)
            flash.message = message(code: 'app.balance.minus.query.successful.msg')
            redirect(action: "index")
            return
        } catch (ValidationException ve) {
            log.warn("BalanceMinusQueryController : Failed to update due to ${ve.message}")
            bmQueryInstance.errors = ve.errors
            render view: "edit", model: [bmQueryInstance : bmQueryInstance, sourceProfiles: SourceProfile.sortedCentralAndAffliateSourceProfiles()]
            return
        } catch (Exception e) {
            log.error("BalanceMinusQueryController :  Exception occurred while updating data : ${e.message}")
            e.printStackTrace()
            flash.error = message(code: "app.error.500")
            redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
            return
        }
    }

    private populateModel(BalanceMinusQuery bmQueryInstance) {
        //Do not bind in any other way because of the clone contained in the params
        bindData(bmQueryInstance, params, [exclude: ["bmQuerySections"]])
        bindExistingBmQueryEdits(bmQueryInstance)
        bindNewBmQueries(bmQueryInstance)
    }

    private bindExistingBmQueryEdits(BalanceMinusQuery bmQueryInstance) {
        //handle edits to the existing Balance Minus Query Section
        bmQueryInstance?.bmQuerySections?.eachWithIndex() { bmQuerySection, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            bmQuerySection.properties = bindingMap
            bmQuerySection = (BmQuerySection) userService.setOwnershipAndModifier(bmQuerySection)
            bindIncludeCases(bmQuerySection, i, params)
            bindExcludeCases(bmQuerySection, i, params)
            bindDistTableList(bmQuerySection, i, params)
        }
        bmQueryInstance
    }

    private bindNewBmQueries(BalanceMinusQuery bmQueryInstance) {
        //bind new Balance Minus Queries as appropriate

        for (int i = bmQueryInstance.bmQuerySections.size(); params.containsKey("bmQuerySections[" + i + "].id"); i++) {
            if (params.get("bmQuerySections[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                BmQuerySection bmQuerySection = new BmQuerySection(bindingMap)

                bmQuerySection = (BmQuerySection) userService.setOwnershipAndModifier(bmQuerySection)
                bindIncludeCases(bmQuerySection, i, params)
                bindExcludeCases(bmQuerySection, i, params)
                bindDistTableList(bmQuerySection, i, params)
                bmQueryInstance.addToBmQuerySections(bmQuerySection)
            }
        }
    }

    private getBindingMap(int i) {
        def bindingMap = [
                sourceProfile          : params.("bmQuerySections[" + i + "].sourceProfile"),
                executeFor             : params.("bmQuerySections[" + i + "].executeFor"),
                executionStartDate     : params.("bmQuerySections[" + i + "].executionStartDate") ?: null,
                executionEndDate       : params.("bmQuerySections[" + i + "].executionEndDate") ?: null,
                xValue                 : params.("bmQuerySections[" + i + "].xValue"),
                flagCaseExclude        : params.("bmQuerySections[" + i + "].flagCaseExclude") ?: false,
                dynamicFormEntryDeleted: params.("bmQuerySections[" + i + "].dynamicFormEntryDeleted") ?: false
        ]
        return bindingMap
    }

    private BmQuerySection bindIncludeCases(BmQuerySection bmQuerySection, int index, Map params) {
        if (bmQuerySection.includeCases) {
            params.put("oldIncludeCases${bmQuerySection.id}", bmQuerySection.includeCases.toString())
        }
        bmQuerySection?.includeCases?.each {
            IncludeCase.get(it.id)?.delete()
        }
        bmQuerySection?.includeCases?.clear()
        List includeCases = params.get("bmQuerySections[" + index + "].includeCases")?.split(";").collect { it.trim() }.findAll { it }
        includeCases?.each { it ->
            IncludeCase includeCase = new IncludeCase(sourceProfile: bmQuerySection.sourceProfile, caseNumber: it)
            bmQuerySection.addToIncludeCases(includeCase)
        }
        return bmQuerySection
    }

    private BmQuerySection bindExcludeCases(BmQuerySection bmQuerySection, int index, Map params) {
        if (bmQuerySection.excludeCases) {
            params.put("oldExcludeCases${bmQuerySection.id}", bmQuerySection.excludeCases.toString())
        }
        bmQuerySection?.excludeCases?.each {
            ExcludeCase.get(it.id)?.delete()
        }
        bmQuerySection?.excludeCases?.clear()
        List excludeCases = params.get("bmQuerySections[" + index + "].excludeCases")?.split(";").collect { it.trim() }.findAll { it }
        excludeCases?.each { it ->
            ExcludeCase excludeCase = new ExcludeCase(sourceProfile: bmQuerySection.sourceProfile, caseNumber: it)
            bmQuerySection.addToExcludeCases(excludeCase)
        }
        return bmQuerySection
    }

    private BmQuerySection bindDistTableList(BmQuerySection bmQuerySection, int index, Map params) {
        if (bmQuerySection.distinctTables) {
            params.put("oldDistinctTables${bmQuerySection.id}", bmQuerySection.distinctTables.toString())
        }
        bmQuerySection?.distinctTables?.each {
            DistinctTable.get(it.id)?.delete()
        }
        bmQuerySection?.distinctTables?.clear()
        if(params.get("bmQuerySections[" + index + "].distinctTables") instanceof String){
            DistinctTable distinctTable = new DistinctTable(sourceProfile : bmQuerySection.sourceProfile, entity : params.get("bmQuerySections[" + index + "].distinctTables"))
            bmQuerySection.addToDistinctTables(distinctTable)
        }else{
            params.get("bmQuerySections[" + index + "].distinctTables")?.each { it ->
                DistinctTable distinctTable = new DistinctTable(sourceProfile : bmQuerySection.sourceProfile, entity : it)
                bmQuerySection.addToDistinctTables(distinctTable)
            }
        }
        return bmQuerySection
    }

    private BalanceMinusQuery preValidateTemplate(BalanceMinusQuery bmQueryInstance, Map params) {
        if(!params?.sourceProfile) {
            bmQueryInstance.errors.rejectValue('sourceProfile', 'com.rxlogix.config.BmQuerySection.sourceProfile.nullable')
        }
        return bmQueryInstance
    }

    def enable() {
        BalanceMinusQuery bmQueryInstance = BalanceMinusQuery?.first()
        //Change the isDisabled flag to false.
        bmQueryInstance.isDisabled = false;
        bmQueryInstance.bmQuerySections.each { bmQuerySection ->
            if (bmQuerySection.includeCases) {
                params.put("oldIncludeCases${bmQuerySection.id}", bmQuerySection.includeCases.toString())
            }
            if (bmQuerySection.excludeCases) {
                params.put("oldExcludeCases${bmQuerySection.id}", bmQuerySection.excludeCases.toString())
            }
            if (bmQuerySection.distinctTables) {
                params.put("oldDistinctTables${bmQuerySection.id}", bmQuerySection.distinctTables.toString())
            }
        }
        try {
            balanceMinusQueryService.enable()
            bmQueryInstance = (BalanceMinusQuery) CRUDService.update(bmQueryInstance)
        } catch (Exception ex) {
            bmQueryInstance.isDisabled = true;
            log.error("BalanceMinusQueryController :  Exception occurred while enable : ${ex.message}")
            ex.printStackTrace()
            flash.error = message(code: "app.error.500")
            redirect action: "index"
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.enabled.message', args: [message(code: 'app.balanceMinusQuery.label'), ""])
                redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
            }

        }
    }

    def disable() {
        BalanceMinusQuery bmQueryInstance = BalanceMinusQuery?.first()
        bmQueryInstance.isDisabled = true;
        bmQueryInstance.bmQuerySections.each { bmQuerySection ->
            if (bmQuerySection.includeCases) {
                params.put("oldIncludeCases${bmQuerySection.id}", bmQuerySection.includeCases.toString())
            }
            if (bmQuerySection.excludeCases) {
                params.put("oldExcludeCases${bmQuerySection.id}", bmQuerySection.excludeCases.toString())
            }
            if (bmQuerySection.distinctTables) {
                params.put("oldDistinctTables${bmQuerySection.id}", bmQuerySection.distinctTables.toString())
            }
        }
        try {
            Integer bmQueryStatus = balanceMinusQueryService.getBmQueryStatus()
            if(bmQueryStatus == 1){
                bmQueryInstance.isDisabled = false
                flash.error = message(code: "balanceMinusQuery.running.disable.schedule.request.failed")
                redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
                return
            }
            balanceMinusQueryService.disable()
            bmQueryInstance = (BalanceMinusQuery) CRUDService.update(bmQueryInstance)
        } catch (Exception ex) {
            bmQueryInstance.isDisabled = false
            log.error("BalanceMinusQueryController :  Exception occurred while disable : ${ex.message}")
            ex.printStackTrace()
            flash.error = message(code: "app.error.500")
            redirect action: "index"
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'app.balanceMinusQuery.label'), ""])
                redirect action: "edit", method: "GET", params: [id: bmQueryInstance.id]
            }

        }
    }


    /*method : validationSummary
    * definition : This method is used to open the validation summary for Balance and minus query based on source Profile Id
    */
    def validationSummary(Long sourceProfileId) {
        render view: "validationSummary", model: [sourceProfileId: sourceProfileId]
    }

    /*method : validationSummary
    * definition : This method is used to get the data for the validation summary for Balance and minus query based on source Profile Id
    */
    def getValidationSummaryList(Long sourceProfileId) {
        SourceProfile sourceProfile = SourceProfile.findById(sourceProfileId)
        if(!sourceProfile) {
            return ([aaData: [], recordsTotal: 0, recordsFiltered: 0] as JSON)
        }
        String tableName = sourceProfile?.isCentral ? "DATAVAL_EXEC_SUMMARY_VW" : "DATAVAL_EXEC_SUMMARY_VW_AFF"
        sanitize(params)
        Map validationSummaryList = balanceMinusQueryService.getValidationSummaryList(params, tableName)
        render (validationSummaryList as JSON)
    }

    /*method : validationSummary
    * definition : This method is used to open the validation log for Balance and minus query based on source Profile Id
    */
    def validationLog(Long sourceProfileId) {
        render view: "validationLog", model: [sourceProfileId: sourceProfileId]
    }

    /*method : getValidationLogList
    * definition : This method is used to get the data for the validation log for Balance and minus query based on source Profile Id
    */
    def getValidationLogList(Long sourceProfileId) {
        SourceProfile sourceProfile = SourceProfile.findById(sourceProfileId)
        if(!sourceProfile) {
            return ([aaData: [], recordsTotal: 0, recordsFiltered: 0] as JSON)
        }
        String tableName = sourceProfile?.isCentral ? "DATAVAL_EXEC_LOG_VW" : "DATAVAL_EXEC_LOG_VW_AFF"
        sanitize(params)
        Map validationLogList = balanceMinusQueryService.getValidationLogList(params, tableName)
        render (validationLogList as JSON)
    }

    /*method : exportToExcel
    * definition : This method is used to export the excel which have 3 sheets contains, summary, validation summary, validatoin log, based on source Profile Id
    */
    def exportToExcel() {
        SourceProfile sourceProfile = SourceProfile.findById(Long.valueOf(params.sourceProfileId))
        if(!sourceProfile) {
            return null
        }
        Map<String, List> bqMqData = balanceMinusQueryService.getBQMQData(sourceProfile)
        List metadataList = balanceMinusQueryService.createMetadataListForBQMQ(bqMqData)
        byte[] file = balanceMinusQueryService.exportToExcel(metadataList)
        String timeZone = userService.currentUser?.preference?.timeZone ?: Constants.DEFAULT_SELECTED_TIMEZONE
        String currentDate = DateUtil.StringFromDate(new Date(), DateUtil.DATEPICKER_UTC_FORMAT, timeZone).toString().replaceAll('/', '').replaceAll(':', '').replaceAll(" ", "")
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: "BQ-MQ Report_"+sourceProfile.sourceName.trim() + "_" + currentDate +".xlsx")
    }

    private String getDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat.WITHOUT_SEC_TZ)
        simpleDateFormat.timeZone = TimeZone.getTimeZone(Constants.DEFAULT_SELECTED_TIMEZONE)
        return simpleDateFormat.format(date)
    }

    private notSaved() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.saved.message')
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.balanceMinusQuery.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
