package com.rxlogix.quality

import com.rxlogix.Constants
import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.ActionItem
import com.rxlogix.config.Capa8D
import com.rxlogix.config.Capa8DAttachment
import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.InboundDrilldownMetadata
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.QualitySampling
import com.rxlogix.config.QualitySubmission
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ReportResult
import com.rxlogix.config.WorkflowState
import com.rxlogix.enums.IcsrReportSpecEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import grails.web.mapping.LinkGenerator
import groovy.json.JsonSlurper
import org.grails.web.json.JSONArray
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.governors.MaxPagesGovernorException
import net.sf.jasperreports.governors.TimeoutGovernorException
import org.apache.commons.io.FileUtils
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import com.rxlogix.enums.ReasonOfDelayAppEnum
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class IssueController implements SanitizePaginationAttributes {
    def userService
    def CRUDService
    def emailService
    def dynamicReportService
    def groovyPageRenderer
    LinkGenerator grailsLinkGenerator
    def qualityService
    GrailsApplication grailsApplication

    @Secured(['ROLE_PVQ_EDIT'])
    def create() {
        params.type = params.type?(ReasonOfDelayAppEnum.PVC.name()):(ReasonOfDelayAppEnum.PVQ.name())
        String issueNumber = getAlphaNumericString(10)
        File attachFile = new File(grailsApplication.config.tempDirectory + Constants.ATTACH_FOLDER)
        if(attachFile.exists()) {
            qualityService.deleteAlltheDirectoriesForAttachment(attachFile)
        }
        Capa8D capa = Capa8D.findByIssueNumberAndOwnerType(issueNumber, params.type)
        if(capa){
            if(params.type == ReasonOfDelayAppEnum.PVC.name())
                redirect(controller: "pvcIssue", action: 'create')
            else
                redirect(action: 'create')

        }
        capa = new Capa8D(issueNumber : issueNumber)
        render view: '/issue/create', model: [users: userService.getActiveUsers(), capaInstance: capa, type: params.type]
    }

    @Secured(['ROLE_PVQ_VIEW'])
    def index() {
        render view: "/issue/index", model:[type: ReasonOfDelayAppEnum.PVQ]
    }

    @Secured(['ROLE_PVQ_VIEW'])
    def view() {
        params.type = params.type?(ReasonOfDelayAppEnum.PVC):(ReasonOfDelayAppEnum.PVQ)
        Long issueId = params.long("id")
        String associatedCaseNumber = qualityService.getCaseNoByIssueId(issueId, Tenants.currentId() as Long)
        render view: '/issue/view', model: [users: userService.getActiveUsers(), capaInstance: Capa8D.get(params.long("id")),caseNumber: associatedCaseNumber, type: params.type]
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def delete(Capa8D capa) {
        params.type = params.type?(ReasonOfDelayAppEnum.PVC):(ReasonOfDelayAppEnum.PVQ)
        Capa8D.withTransaction { status ->
            try {
                List<ActionItem> toDelete = [] + capa.preventiveActions + capa.correctiveActions
                CRUDService.softDelete(capa, capa.issueNumber, params.deleteJustification)

                getMetadataIds(capa, false).each { id ->
                    DrilldownCLLMetadata metadataRecord = DrilldownCLLMetadata.findById(id)
                    clearIssueFromMetadata(metadataRecord, capa.issueNumber)
                }

                getMetadataIds(capa, true).each { id ->
                    InboundDrilldownMetadata metadataRecord = InboundDrilldownMetadata.findById(id)
                    clearIssueFromMetadata(metadataRecord, capa.issueNumber)
                }
                toDelete.each {
                    CRUDService.softDelete(it, it.description, params.deleteJustification)
                }
                request.withFormat {
                    form {
                        flash.message = message(code: 'default.deleted.message', args: [message(code: 'quality.capa.capaNumber.label'), capa.issueNumber])
                        if(params.type == ReasonOfDelayAppEnum.PVC)
                            redirect(controller: 'pvcIssue', action: "index", method: "GET")
                        else
                            redirect(action: "index", method: "GET")
                    }
                    '*' { render status: NO_CONTENT }
                }
            } catch (ValidationException ve) {
                status.setRollbackOnly()
                request.withFormat {
                    form {
                        flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'quality.capa.capaNumber.label'), capa.issueNumber])
                        if(params.type == ReasonOfDelayAppEnum.PVC)
                            redirect(controller: 'pvcIssue', action: "view", id: params.id)
                        else
                            redirect(action: "view", id: params.id)
                    }
                    '*' { render status: FORBIDDEN }
                }
            }
            catch(Exception e){
                log.error("Unexpected error in issue -> deletion", e)
                status.setRollbackOnly()
                flash.error = message(code: "app.error.500")
                if(params.type == ReasonOfDelayAppEnum.PVC)
                    redirect(controller: 'pvcIssue', action: 'index')
                else
                    redirect(action: 'index')
            }
        }

    }

     void clearIssueFromMetadata(def metadataRecord, String issueNumber) {
        if (metadataRecord) {
            String allIssueInMeta = metadataRecord.lastUpdatedIssue
            if (allIssueInMeta.contains(issueNumber)) {
                String regex = '(^|,)' + issueNumber + '(,|$)'
                allIssueInMeta = allIssueInMeta.replaceAll(regex, ",")
                allIssueInMeta = allIssueInMeta.replaceAll('^,|,$', "")
                metadataRecord.lastUpdatedIssue = allIssueInMeta
                CRUDService.update(metadataRecord)
            }
        }
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def edit() {
        params.type = params.type?(ReasonOfDelayAppEnum.PVC):(ReasonOfDelayAppEnum.PVQ)
        File attachFile = new File(grailsApplication.config.tempDirectory + Constants.ATTACH_FOLDER)
        if(attachFile.exists()) {
            qualityService.deleteAlltheDirectoriesForAttachment(attachFile)
        }
        render view: '/issue/edit', model: [users: userService.getActiveUsers(), capaInstance: Capa8D.get(params.long("id")), type: params.type]
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def save() {
        if (request.method == 'GET') {
            notSaved()
            return
        }

        Capa8D capa = new Capa8D()
        capa.lastStatusChanged = new Date()
        bindData(capa, params)

        try {
            MiscUtil.validateStringFieldSize(capa, 'description', 'description', Capa8D.DESCRIPTION_MAX_BYTES)
            JSONArray jsonArray = new JSONArray("[${params.AttachJson}]")
            for(int i=0;i<jsonArray.length();i++){
                if(jsonArray.filename[i] != null){
                    bindFile(capa, jsonArray.filename[i],jsonArray.counter[i])
                }
            }
            if(capa.attachments != null)
                capa.attachmentChecked = true
            capa = (Capa8D) CRUDService.save(capa)
        } catch (ValidationException ve) {
            capa.errors = ve.errors
            render view: "/issue/create", model: [capaInstance: capa, users: userService.getActiveUsers(), type: params.ownerType]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in issue -> save", ex)
            flash.error = message(code: "app.error.500")
            if(params.ownerType == ReasonOfDelayAppEnum.PVC.name())
                redirect(controller: 'pvcIssue', action: 'create')
            else
                redirect(action: 'create')
            return
        }
        if(params.ownerType == ReasonOfDelayAppEnum.PVC.name()) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'quality.capa.capaNumber.label'), capa.issueNumber.toString()])
            redirect(controller: "pvcIssue", action: 'index', params: [id: capa.id])
            return
        } else {
            flash.message = message(code: 'default.created.message', args: [message(code: 'quality.capa.capaNumber.label'), capa.issueNumber.toString()])
            redirect(action: 'index', params: [id: capa.id])
            return
        }
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def validateAndCreate(Capa8D capa) {
        bindData(capa, params)
        capa.lastStatusChanged = new Date()
        try {
            JSONArray jsonArray = new JSONArray("[${params.AttachJson}]")
            for(int i=0;i<jsonArray.length();i++){
                if(jsonArray.filename[i] != null){
                    bindFile(capa, jsonArray.filename[i],jsonArray.counter[i])
                }
            }
            if(capa.attachments != null)
                capa.attachmentChecked = true
            capa = (Capa8D) CRUDService.save(capa)
        } catch (ValidationException ve) {
            capa.errors = ve.errors
            render view: "/issue/create", model: [capaInstance: capa, users: userService.getActiveUsers(), type: params.ownerType]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in issue -> validateAndCreate", ex)
            flash.error = message(code: "app.error.500")
            if(params.ownerType == ReasonOfDelayAppEnum.PVC.name())
                redirect(controller: 'pvcIssue', action: 'create')
            else
                redirect(action: 'create')
            return
        }
        if(params.ownerType == ReasonOfDelayAppEnum.PVC.name())
            redirect(controller: "pvcIssue", action: 'edit', params: [id: capa.id])
        else
            redirect(action: 'edit', params: [id: capa.id])
    }

    private void bindFile(capa8d, filename, counter) {
            Capa8DAttachment attachment = new Capa8DAttachment()
            if(filename) {
                attachment.filename = filename
            }
            String fileLocation = grailsApplication.config.tempDirectory + File.separator + Constants.ATTACH_FOLDER+ File.separator + counter + File.separator + filename
            InputStream inputStream = new FileInputStream(fileLocation)
            attachment.data =  inputStream.getBytes()
            attachment.createdBy = userService.currentUser.fullName
            attachment.modifiedBy = userService.currentUser.fullName
            attachment.ownerType = capa8d.ownerType
            File deleteFile = new File(fileLocation)
            if(deleteFile.exists()){
                deleteFile.delete()
            }
            capa8d.addToAttachments(attachment)
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def update(Capa8D capa) {
        Boolean flag = false
        List attachments =[]
        if(params.id ) {
            capa = Capa8D.get(params.long("id"))
            bindData(capa, params)
            try {
                MiscUtil.validateStringFieldSize(capa, 'description', 'description', Capa8D.DESCRIPTION_MAX_BYTES)
                capa?.attachments?.each{
                    if(it.isDeleted == false){
                        attachments.add(it)
                    }
                }
                params.put('oldAttachments',attachments.sort().join(", "))
                JSONArray jsonArray = new JSONArray("[${params.AttachJson}]")
                for(int i=0;i<jsonArray.length();i++){
                    if(jsonArray.filename[i] != null){
                        bindFile(capa, jsonArray.filename[i],jsonArray.counter[i])
                        capa.attachmentChecked = true
                    }
                }
                List<Long> inboundMetadataIds = getMetadataIds(capa, true)
                List<Long> drilldownMetadataIds = getMetadataIds(capa, false)

                if (!inboundMetadataIds.isEmpty()) {
                    updateMetadata(inboundMetadataIds, params, true)
                }
                if (!drilldownMetadataIds.isEmpty()) {
                    updateMetadata(drilldownMetadataIds, params, false)
                }
                if(!capa.teamMembers)
                    capa.teamMembers = []
                if(!capa.correctiveActions)
                    capa.correctiveActions = []
                if(!capa.preventiveActions)
                    capa.preventiveActions = []
                capa = (Capa8D) CRUDService.update(capa)
            } catch (ValidationException ve) {
                capa.errors = ve.errors
                render view: "/issue/edit", model: [capaInstance: capa, users: userService.getActiveUsers(), type: params.ownerType == ReasonOfDelayAppEnum.PVC.name() ? ReasonOfDelayAppEnum.PVC : ReasonOfDelayAppEnum.PVQ]
                return
            } catch (Exception ex) {
                log.error("Unexpected error in issue -> update", ex)
                flash.error = message(code: "app.error.500")
                if(params.ownerType == ReasonOfDelayAppEnum.PVC.name())
                    redirect(controller: 'pvcIssue', action: 'edit')
                else
                    redirect(action: 'edit')
                return
            }
            if(capa.ownerType == ReasonOfDelayAppEnum.PVC.name()) {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'quality.capa.capaNumber.label'), capa.issueNumber.toString()])
                redirect(controller: "pvcIssue", action: 'view', params: [id: capa.id])
                return
            } else {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'quality.capa.capaNumber.label'), capa.issueNumber.toString()])
                redirect(action: 'view', params: [id: capa.id])
                return
            }
        }
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def createCapaForQuality() {
        if (params?.teamMembers) {
            params.teamMembers = params?.teamMembers?.split(",")
        }
        List attachments =[]
        Capa8D capa = Capa8D.findByIssueNumberAndOwnerType(params.issueNumber, ReasonOfDelayAppEnum.PVQ)
        if(capa){
            bindData(capa, params, ['qualityDataType', 'qualityCaseNum', 'qualityErrorType','qualityVersionNum', 'selectedCases'])
            capa.attachments.each{
                if(it.isDeleted == false){
                    attachments.add(it)
                }
            }
            params.put('oldAttachments',attachments.sort().join(","))
            capa.attachments.clear()
            (Capa8D) CRUDService.update(capa)
        }else {
            capa = params.id ? Capa8D.get(params.long("id")) : new Capa8D(lastStatusChanged: new Date())
            capa.ownerType = ReasonOfDelayAppEnum.PVQ
            bindData(capa, params, ['qualityDataType', 'qualityCaseNum', 'qualityErrorType', 'qualityVersionNum', 'selectedCases'])
            CRUDService.save(capa)
        }

        List idList = []
        if (params.selectedIds) {
            idList = JSON.parse(params.selectedIds)
        }
        if (!idList || idList.size() == 0) {
            idList = [params.get("rowId")]
        }

        try {
            //link the issue to cases
            idList.each {
                def relatedQualityObj = qualityService.initializeQualityObjByIdAndTenantId(it, params.qualityDataType, Tenants.currentId() as Long)
                relatedQualityObj.issues.add(capa)
                CRUDService.update(relatedQualityObj)
            }

            render "Ok"
        } catch (Exception ex) {
            log.error("Error in creating issue", ex)
            render(status: 500, text: "Capa creation failed")
        }

    }

    @Secured(['ROLE_PVQ_VIEW', 'ROLE_PVC_VIEW'])
    def ajaxList() {
        sanitize(params)
        params.sort == "dateCreated" ? params.sort = "dateCreated" : params.sort
        List<Closure> advancedFilterCriteria = FilterUtil.buildCriteria(FilterUtil.convertToJsonFilter(params.tableFilter), Capa8D, userService.getUser().preference)
        List<Capa8D> capa8DList = Capa8D.capasBySearchString(params.ownerType, params.searchString, advancedFilterCriteria).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])

        List<Long> allCapaIds = Capa8D.capasBySearchString(params.ownerType, params.searchString, advancedFilterCriteria).list().collect { it.id } as List

        render([aaData: capa8DList*.toMap(), recordsTotal: Capa8D.capasBySearchString(params.ownerType, null,null).count(), recordsFiltered: Capa8D.capasBySearchString(params.ownerType, params.searchString, advancedFilterCriteria).count(), allCapaIds : allCapaIds] as JSON)

    }

    def ajaxActionItems() {
        Capa8D capa = Capa8D.get(params.long("id"))
        List corrective = capa.correctiveActions?.findAll { !it.isDeleted }?.collect { it.toActionItemMap() } ?: []
        List preventive = capa.preventiveActions?.findAll { !it.isDeleted }?.collect { it.toActionItemMap() } ?: []
        render([corrective: corrective, preventive: preventive] as JSON)

    }

    private notSaved() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.saved.message')
                redirect action: "create", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def share() {
        if (params.id) {
            Capa8D capa = Capa8D.get(params.id)
            if (capa) {
                def allowedUsers = capa.sharedWith
                def allowedGroups = capa.sharedWithGroup
                Set<User> newUsers=[]
                Set<UserGroup> newGroups=[]
                if (params.sharedWith) {
                    params.sharedWith.split(";").each { String shared ->
                        if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                            UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                            if (userGroup && !allowedGroups.find { it.id == userGroup.id }) {
                                capa.addToSharedWithGroup(userGroup)
                                newGroups << userGroup
                            }
                        } else if (shared.startsWith(Constants.USER_TOKEN)) {
                            User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                            if (user && !allowedUsers.find { it.id == user.id }) {
                                capa.addToSharedWith(user)
                                newUsers << user
                            }
                        }
                    }
                    CRUDService.update(capa)
                    sendShareNotification(newUsers, newGroups, capa)
                }
            }
        }
        flash.message = message(code: 'quality.capa.shared.successful')

        if(params.type == ReasonOfDelayAppEnum.PVC.name()){
            redirect(controller: "pvcIssue", action: 'index')
        }
        else {
            redirect(action: 'index')

        }
    }

    def getSharedWithUsers() {
        if (params.id) {
            Capa8D capa = Capa8D.get(params.id)
            if (capa) {
                def result = [users: capa.sharedWith, groups: capa.sharedWithGroup]
                render result as JSON
            }
        }
    }

    def addEmailConfiguration() {
        Capa8D capa = Capa8D.get(params.id)
        if (!capa) {
            log.error("Requested Entity not found : ${params.id}")
            render "Not Found"
            return
        }
        render contentType: "application/json", encoding: "UTF-8", text: (capa.emailConfiguration ?: []) as JSON
    }

    private void sendShareNotification(Set<User> newUsers, Set<UserGroup> newGroups, Capa8D capaInstance) {
        Set<String> recipients = newUsers*.email as Set
        newGroups.each {
            recipients.addAll(it.getUsers()*.email)
        }
        if(recipients) {
            String emailSubject = ViewHelper.getMessage('app.email.subject.issueNumber', capaInstance?.issueNumber?.toString())
            String timeZone = userService.currentUser?.preference?.timeZone
            def url = ""
            if (grailsApplication.config.grails.appBaseURL)
                url += grailsLinkGenerator.link(controller: "issue", action: "view", id: capaInstance.id, base: grailsApplication.config.grails.appBaseURL, absolute: true)
            else
                url += ViewHelper.getMessage("app.emailService.result.nourl.message.label")
            def content = groovyPageRenderer.render(template: '/mail/issue/capa8d', model: ['capaInstance': capaInstance, 'userTimeZone': timeZone, 'url': url])
            emailService.sendNotificationEmail(recipients, content, true, emailSubject);
        }
    }

    @Secured(["ROLE_CONFIGURATION_VIEW"])
    def email() {
        if (params.id) {
            Capa8D capa = Capa8D.get(params.id)
            if (capa) {

                List<ReportFormatEnum> formats = []
                if (params.attachmentFormats instanceof String) {
                    formats.add(ReportFormatEnum.valueOf(params.attachmentFormats))
                } else {
                    formats = params.attachmentFormats.collect { ReportFormatEnum.valueOf(it) }
                }

                List<String> emailList = []
                if (params.emailToUsers instanceof String) {
                    emailList.add(params.emailToUsers)
                } else {
                    emailList = params.emailToUsers
                }

                bindEmailConfiguration(capa, params.emailConfiguration)
                try {
                    emailService.emailReportTo(capa, emailList, formats)
                }
                catch (Exception e){
                    Boolean isOtherException = true

                    Map mailException = [
                            'GrailsMailException': message(code: 'app.report.mail.configuration.exception'),
                            'FileNotFoundException': message(code: 'app.report.file.not.found'),
                            'MaxPagesGovernorException': message(code: 'app.report.maxJasperDataBytes.share'),
                            'TimeoutGovernorException': message(code : 'app.report.mail.timeout.exception'),
                            'JRException': message(code: 'app.report.mail.jre.exception'),
                            'AuthenticationFailedException': message(code: 'app.report.mail.auth.exception')
                    ]

                    for (val in mailException) {
                        if (e.getClass().toString().endsWith(val.getKey())){
                            flash.error = val.getValue()
                            isOtherException = false
                        }
                    }

                    if (isOtherException){
                        flash.error = message(code: 'app.report.mail.default')
                    }

                    log.info("${e.message}. So we are not able to mail this file.")
                }

            } else {
                // no such result
            }
        } else {
            // no valid id
        }
        redirect(url: request.getHeader('referer'))   //redirect to the page from where the request comes: to dashboard, to report/index page or periodicReport/reports.
    }

    private bindEmailConfiguration(Capa8D capa, Map emailConfiguration) {
        if (emailConfiguration.subject && emailConfiguration.body) {
            EmailConfiguration emailConfigurationInstance
            if (capa.emailConfiguration) {
                emailConfigurationInstance = capa.emailConfiguration
                emailConfigurationInstance.isDeleted = false
                bindData(emailConfigurationInstance, emailConfiguration)
                CRUDService.update(emailConfigurationInstance)
            } else {
                emailConfigurationInstance = new EmailConfiguration(emailConfiguration)
                CRUDService.save(emailConfigurationInstance)
                capa.emailConfiguration = emailConfigurationInstance
            }
        } else {
            if (capa.emailConfigurationId) {
                CRUDService.softDelete(capa.emailConfiguration, capa.emailConfigurationId)
            }
        }
    }

    def getIssueNumber(){
        String issueNumber = getAlphaNumericString(10)
        render issueNumber
    }

    def getAlphaNumericString(int n) {

        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(n)

        for (int i = 0; i < n; i++) {
            int index = (int)(alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index))
        }
        return sb.toString()
    }

    def exportCapa8d(){
        def paramData = new JsonSlurper().parseText(params.data.toString())
        List<Capa8D> capaList = []
        List<Long> ids = paramData.selectedIds
        if(ids && ids.size()>0) {
            ids?.collate(999)?.each {
                capaList += Capa8D.findAllByIdInListAndOwnerType(it, paramData.ownerType)
            }
        }
        if(capaList){
            params.outputFormat = paramData.outputFormat
            params.reportLocale = (userService.currentUser?.preference?.locale ?: capaList[0].owner.preference.locale).toString()
            params.detailed = paramData.detailed
            File reportFile = dynamicReportService.createMultiCapa8dReport(capaList, params)
            log.debug("reportFile " + reportFile.toPath())
            renderReportOutputType(reportFile, capaList, params)
        }
    }

    protected renderReportOutputType(File reportFile, List<Capa8D> capaList, Map params) {
        if (!reportFile) {
            flash.message = message(code: "app.report.file.not.found")
            redirect(controller: "report", action: "index")
            return
        }
        String reportFileName = dynamicReportService.getReportName(capaList, params)
        try {
            GrailsWebRequest webRequest =
                    (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
            webRequest.setRenderView(false)
            MultipartFileSender.renderFile(reportFile, reportFileName, params.outputFormat as String, dynamicReportService.getContentType(params.outputFormat), request, response, false)
            String entityValue
            if(params.detailed)
                entityValue = ViewHelper.getMessage("auditLog.entityValue.bulk.export","Issue Detailed", ReportFormatEnum.(params.outputFormat).displayName)
            else
                entityValue = ViewHelper.getMessage("auditLog.entityValue.bulk.export","Issue Summarized", ReportFormatEnum.(params.outputFormat).displayName)
            reportFileName = reportFileName + "." + params.outputFormat
            AuditLogConfigUtil.logChanges(capaList, [outputFormat: params.outputFormat, fileName: reportFileName, exportedDate: new Date()],
                    [:], Constants.AUDIT_LOG_EXPORT, entityValue)
        } catch (Exception ex) {
            flash.error = message(code: "default.server.error.message")
            ex.printStackTrace()
        }
    }

    @Secured(['ROLE_PVC_EDIT'])
    def createCapaForReasonOfDelay() {
        List attachments =[]
        List selectedIds = params.selectedIds ? JSON.parse(params.selectedIds) as List : []
        Capa8D capa = Capa8D.findByIssueNumberAndOwnerTypeAndIsDeleted(params.issueNumber, ReasonOfDelayAppEnum.PVC,false)
        Capa8D.withTransaction { status ->
            try {
                if (capa) {
                    if (params?.teamMembers) {
                        params.teamMembers = params?.teamMembers?.split(",")
                    }
                    bindData(capa, params, ['cllRecordId'])
                    capa.attachments.each {
                        if (it.isDeleted == false) {
                            attachments.add(it)
                        }
                    }
                    params.put('oldAttachments', attachments.sort().join(","))
                    capa.attachments.clear()
                    (Capa8D) CRUDService.update(capa)
                } else {
                    capa = params.id ? Capa8D.get(params.long("id")) : new Capa8D(lastStatusChanged: new Date())
                    if (params?.teamMembers) {
                        params.teamMembers = params?.teamMembers?.split(",")
                    }
                    capa.ownerType = ReasonOfDelayAppEnum.PVC
                    bindData(capa, params, ['cllRecordId'])
                    CRUDService.save(capa)
                }
                boolean isInbound = params.senderId && (Long.valueOf(params.senderId) > -1)
                List<Long> metadataIds = getMetadataIds(capa, isInbound)
                if (selectedIds.size() == 0) {
                    def metadataRecord = getMetadataRecord(capa)
                    if(metadataRecord.lastUpdatedIssue==null){
                        metadataRecord.lastUpdatedIssue = params.issueNumber
                        metadataIds.remove(metadataRecord.id)
                    }
                    else {
                        if(!metadataRecord.lastUpdatedIssue.contains(params.issueNumber)){
                            metadataRecord.lastUpdatedIssue = params.issueNumber + ',' + metadataRecord.lastUpdatedIssue
                            metadataIds.remove(metadataRecord.id)
                        }
                    }
                    CRUDService.saveOrUpdate(metadataRecord)
                } else {
                    for (int i = 0; i < selectedIds.size(); i++) {
                        params.masterCaseId = selectedIds[i].caseId
                        params.processedReportId = selectedIds[i].processedReportId
                        params.tenantId = selectedIds[i].tenantId
                        params.senderId = selectedIds[i].senderId
                        params.masterVersionNum = selectedIds[i].versionNum
                        def metadataRecord = getMetadataRecord(capa)
                        if(metadataRecord.lastUpdatedIssue==null){
                            metadataRecord.lastUpdatedIssue = params.issueNumber
                            metadataIds.remove(metadataRecord.id)
                        }
                        else {
                            if(!metadataRecord.lastUpdatedIssue.contains(params.issueNumber)){
                                metadataRecord.lastUpdatedIssue = params.issueNumber + ',' + metadataRecord.lastUpdatedIssue
                                metadataIds.remove(metadataRecord.id)
                            }
                        }
                        CRUDService.saveOrUpdate(metadataRecord)
                    }
                }
                updateMetadata(metadataIds, params, isInbound)
                render "Ok"
            }
            catch (Exception ex) {
                log.error("Error in creating issue for masterCaseId = ${params.masterCaseId} ", ex)
                status.setRollbackOnly()
                render(status: 500, text: "Capa creation failed for masterCaseId = ${params.masterCaseId} ")
            }
        }
    }

    private def getMetadataRecord(Capa8D capa) {
        def metadataRecord
        if (params.senderId!=null && (Long.valueOf(params.senderId) >-1)) {
            metadataRecord = InboundDrilldownMetadata.getMetadataRecord(params).get()
        }
        else {
            metadataRecord = DrilldownCLLMetadata.getMetadataRecord(params).get()
        }
        if (metadataRecord == null) {
            if (params.senderId != null && (Long.valueOf(params.senderId) > -1)) {
                metadataRecord = new InboundDrilldownMetadata()
                metadataRecord.caseVersion = params.long('masterVersionNum')
                metadataRecord.senderId = params.long('senderId')
            }
            else {
                metadataRecord = new DrilldownCLLMetadata()
                metadataRecord.processedReportId = params.long('processedReportId')
            }
            metadataRecord.caseId = params.long('masterCaseId')
            metadataRecord.tenantId = params.long('tenantId')
            metadataRecord.workflowState = WorkflowState.defaultWorkState
        }
        metadataRecord.issues.add(capa)
        return metadataRecord
    }

    String getRODCapaDescription() {
        List selectedIds = params.selectedIds ? JSON.parse(params.selectedIds) as List : []
        Map descMap = [:]
        String description = ''
        selectedIds.each {
            String dueDate = it.issueDueDate ? DateUtil.SimpleDateReformat(it.issueDueDate, DateUtil.ISO_DATE_TIME_FORMAT, DateUtil.DATEPICKER_FORMAT) : ViewHelper.getEmptyLabel()
            String submissionDate = it.issueSubmissionDate != '' ? DateUtil.SimpleDateReformat(it.issueSubmissionDate, DateUtil.ISO_DATE_TIME_FORMAT, DateUtil.DATEPICKER_FORMAT) : ViewHelper.getEmptyLabel()
            String responsibleParty = it.issueResponsibleParty != '' ? it.issueResponsibleParty : ViewHelper.getEmptyLabel()
            String rootCause = it.issueRootCause != '' ? it.issueRootCause : ViewHelper.getEmptyLabel()
            String reportingDest = it?.issueReportingDest?.toString()
            if (reportingDest && reportingDest.trim()!=null) {
                if (!descMap.containsKey(reportingDest)) {
                    descMap.put(reportingDest, ['caseNumberList' : [it.caseNum], 'dueDateList' : [dueDate], 'submissionDateList' : [submissionDate],
                                             'respPartyList' : [responsibleParty], 'rootCauseList' : [rootCause]])
                }
                else {
                    descMap[reportingDest].caseNumberList.add(it.caseNum)
                    descMap[reportingDest].dueDateList.add(dueDate.toString())
                    descMap[reportingDest].submissionDateList.add(submissionDate.toString())
                    descMap[reportingDest].respPartyList.add(responsibleParty)
                    descMap[reportingDest].rootCauseList.add(rootCause)
                }
            }
        }
        descMap.each { k, v ->
            description += message(code: "rod.capa.reporting.destination") + ' : ' + k + ' ; ' + message(code: "rod.capa.case.Numbers") + ' : ' + v.caseNumberList + ' ; ' +
                    message(code: "rod.capa.due.date") + ' : ' +  v.dueDateList + ' ; ' + message(code: "rod.capa.submission.date") + ' : ' + v.submissionDateList + ' ; ' +
                    message(code: "rod.capa.root.cause") + ' : ' + v.rootCauseList + ' ; ' + message(code: "rod.capa.responsible.party") + ' : ' + v.respPartyList + '\n'
        }
        Integer defaultDescriptionMaxSize = Capa8D.constrainedProperties.description.maxSize
        if (description && description.length() > defaultDescriptionMaxSize){
            description= description.substring(0,defaultDescriptionMaxSize)
        }
        render description.trim();
    }

    def fetchIssueNumber() {
        try {
            def metadataRecord
            if (params.senderId != null && (Long.valueOf(params?.senderId) > -1)) {
                metadataRecord = InboundDrilldownMetadata.getMetadataRecord(params).get()
            }
            else {
                metadataRecord = DrilldownCLLMetadata.getMetadataRecord(params).get()
            }
            List issueData = metadataRecord.issues.collect {
                if (!it.isDeleted) {
                    it.issueNumber
                }
            }
            render issueData as JSON
        } catch (Exception ex) {
            log.error("Error in fetching Issue Number Data", ex)

        }
    }

    def fetchIssueNumberCase() {
        try {
            List issueData;
            def metaDataRecord
            if (params.get('qualityDataType') == "CASE_QUALITY") {
                metaDataRecord = QualityCaseData.get(params.long("rowId"))

            } else if (params.get('qualityDataType') == "SUBMISSION_QUALITY") {
                metaDataRecord = QualitySubmission.get(params.long("rowId"))
            } else {
                metaDataRecord = QualitySampling.get(params.long("rowId"))
            }
            issueData = metaDataRecord.issues.findAll { !it.isDeleted }.collect { it.issueNumber }
            render issueData as JSON
        } catch (Exception ex) {
            log.error("Error in fetching Issue Number for Case Data", ex)

        }
    }

    def fetchDataIssue() {
        try {
            Capa8D capa = Capa8D.findByIssueNumber(params.issueNumber)
            render([capaData: capa.toMap()] as JSON)
        }
        catch (Exception ex) {
            log.error("Error in fetching Data", ex)
        }
    }

    private List<Long> getMetadataIds(Capa8D capa, boolean isInbound) {
        def metadataClass = isInbound ? InboundDrilldownMetadata : DrilldownCLLMetadata
        String hql = "SELECT DISTINCT m.id FROM " +
                metadataClass.name +
                " m JOIN m.issues i WHERE i.id = :capaId"
        return metadataClass.executeQuery(hql, [capaId: capa.id])
    }

    void updateMetadata(List<Long> metadataIds, def params, boolean isInbound) {
        def metadataClass = isInbound ? InboundDrilldownMetadata : DrilldownCLLMetadata
        metadataIds.each { id ->
            def metadataRecord = metadataClass.findById(id)
            String allIssue=metadataRecord.lastUpdatedIssue ?: Constants.BLANK_STRING
            List<String> filteredIssues = allIssue ? allIssue.split(",") : []
            String issueToRemove = params.oldIssueNumber ? params.oldIssueNumber: params.issueNumber
            filteredIssues.remove(issueToRemove)
            String issueToPrioritize=params.issueNumber
            filteredIssues.add(0,issueToPrioritize )
            String updatedIssues = filteredIssues.join(",")
            metadataRecord.lastUpdatedIssue=updatedIssues
            CRUDService.update(metadataRecord)
        }
    }

    @Secured(['ROLE_PVC_EDIT'])
    def updateCapaForReasonOfDelay() {
        List attachments =[]
        Capa8D capa = Capa8D.findByIssueNumber(params.issueNumber)
        if (params.issueNumber) {
            if (params?.teamMembers) {
                params.teamMembers = params?.teamMembers?.split(",")
            }
            bindData(capa, params)
            try {
                capa.attachments.each{
                    if(it.isDeleted == false){
                        attachments.add(it)
                    }
                }
                params.put('oldAttachments',attachments.sort().join(","))
                capa.attachments.clear()
                capa = (Capa8D) CRUDService.update(capa)
                def metadataRecord
                if (params.senderId != null && (Long.valueOf(params?.senderId) > -1)) {
                    metadataRecord = InboundDrilldownMetadata.getMetadataRecord(params).get()
                }
                else {
                    metadataRecord = DrilldownCLLMetadata.getMetadataRecord(params).get()
                }
                metadataRecord.issues.add(capa)
                metadataIds.remove(metadataRecord.id)
                boolean isInbound = params.senderId && (Long.valueOf(params.senderId) > -1)
                List<Long> metadataIds = getMetadataIds(capa, isInbound)
                updateMetadata(metadataIds, params, isInbound)
                CRUDService.saveOrUpdate(metadataRecord)
                render "Ok"
            } catch (Exception ex) {
                log.error("Error in updation issue", ex)
                render(status: 500, text: "Capa updation failed")
            }
        }
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updateCapaForQuality() {
        List attachments =[]
        def metaDataRecordCaseQuality
        Capa8D capa = Capa8D.findByIssueNumber(params.issueNumber)
        if (params.issueNumber) {
            if (params?.teamMembers) {
                params.teamMembers = params?.teamMembers?.split(",")
            }
            bindData(capa, params)
            try {
                capa.attachments.each{
                    if(it.isDeleted == false){
                        attachments.add(it)
                    }
                }
                params.put('oldAttachments',attachments.sort().join(","))
                capa.attachments.clear()
                capa = (Capa8D) CRUDService.update(capa)
                if (params.qualityDataType == PvqTypeEnum.CASE_QUALITY.name()) {
                    metaDataRecordCaseQuality = QualityCaseData.get(params.long("rowId"))
                } else if (params.qualityDataType == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                    metaDataRecordCaseQuality = QualitySubmission.get(params.long("rowId"))
                } else {
                    metaDataRecordCaseQuality = QualitySampling.get(params.long("rowId"))
                }
                metaDataRecordCaseQuality.issues.add(capa)
                CRUDService.saveOrUpdate(metaDataRecordCaseQuality)
                render "Ok"
            } catch (Exception ex) {
                log.error("Error in updation issue", ex)
                render(status: 500, text: "Capa updation failed")
            }
        }
    }

    def fetchUsers(){
        List userMap = userService.getActiveUsers().collect{
            [userId: it.id, fullName: it.fullName]
        }
        render userMap as JSON
    }

    def downloadAttachment(Long id) {
        Capa8DAttachment attachment = Capa8DAttachment.get(id)
        render(file: attachment.data, fileName: attachment.filename, contentType: "application/octet-stream")
    }

    def removeAttachments(Long id , Long capaId) {
        Capa8D capa = Capa8D.get(capaId)
        flash.message = message(code: 'quality.attachment.removed')
        Capa8DAttachment capa8dAttachment = Capa8DAttachment.get(id)
        capa.attachmentChecked = false
        CRUDService.softDelete(capa8dAttachment, capa8dAttachment.filename, params.deletejustification)
        if(Capa8DAttachment.findByIsDeletedAndIssues(false,capa)){
            capa.attachmentChecked = true
            CRUDService.update(capa)
        }
        if(capa.ownerType == ReasonOfDelayAppEnum.PVC.name())
            redirect(controller: "pvcIssue", action: 'edit', params: [id: capaId])
        else
            redirect(action: 'edit', params: [id: capaId])
    }

    def downloadAllAttachment() {
        Capa8D capa = Capa8D.get(params.capaInstanceId)
        String filePath = grailsApplication.config.tempDirectory as String
        File tempDirectory = new File(filePath)
        tempDirectory.mkdirs()
        File directoryToArchive = new File(tempDirectory, "${MiscUtil.generateRandomName()}")
        if (directoryToArchive.exists()) {
            FileUtils.deleteDirectory(directoryToArchive, directoryToArchive)
        }
        directoryToArchive.mkdir()
        if(params.boolean("selectAll")) {
            def selectedIds = Capa8DAttachment.findAllByIssues(capa).collect {it.id}
            downloadSelectedIds(directoryToArchive, capa, selectedIds)
        }else if(params.selectedIds) {
            def selectedIds = params.selectedIds.split(",").toList()
            downloadSelectedIds(directoryToArchive, capa, selectedIds)
        }
    }

    def downloadSelectedIds(File directoryToArchive, Capa8D capa, def selectedIds) {
        if(selectedIds.size()==1){
            Capa8DAttachment attachment = Capa8DAttachment.get(selectedIds)
            render(file: attachment.data, fileName: attachment.filename, contentType: "application/octet-stream")
        }else {
            selectedIds.each {
                Capa8DAttachment capa8DAttachment = Capa8DAttachment.get(it)
                if(capa8DAttachment.isDeleted == false) {
                    File entryFile = new File(directoryToArchive, capa8DAttachment.filename)
                    entryFile.append(capa8DAttachment.data)
                }
            }
            String directory = "${grailsApplication.config.tempDirectory as String}${capa.issueNumber}"
            File tempFile = new File("${directory}.zip")

            String zipFolderName = tempFile.getName()
            zipIt(tempFile.toString(), directoryToArchive)
            FileUtils.cleanDirectory(directoryToArchive)
            directoryToArchive.delete()

            try {
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "attachment; filename=\"" + zipFolderName +"\"")
                OutputStream out = response.getOutputStream()
                FileInputStream input = new FileInputStream(tempFile)
                byte[]  b = new byte[4096]
                int count
                while((count = input.read(b)) >=0 ) {
                    out.write(b, 0, count)
                }
                out.flush()
                out.close()
                input.close()
            }catch (IOException ex ) {
                ex.printStackTrace()
            }
        }
    }

    public void zipIt(String zipFile, File directoryToArchive) {

        String[] srcFiles = directoryToArchive.listFiles()

        try {

            // create byte buffer
            byte[] buffer = new byte[1024]

            FileOutputStream fos = new FileOutputStream(zipFile)

            ZipOutputStream zos = new ZipOutputStream(fos)

            for (int i=0; i < srcFiles.length; i++) {

                File srcFile = new File(srcFiles[i])

                FileInputStream fis = new FileInputStream(srcFile)

                // begin writing a new ZIP entry, positions the stream to the start of the entry data
                zos.putNextEntry(new ZipEntry(srcFile.getName()))

                int length

                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length)
                }

                zos.closeEntry()

                // close the InputStream
                fis.close()

            }

            // close the ZipOutputStream
            zos.close()

        }
        catch (IOException ioe) {
            System.out.println("Error creating zip file: " + ioe)
        }

    }

    def removeAllAttachment(Long capaInstanceId) {
        def selectedIds = []
        Capa8D capa = Capa8D.get(capaInstanceId)

        if(params.boolean("selectAll")) {
            selectedIds = Capa8DAttachment.findAllByIssues(capa).collect {it.id}
        }else if(params.selectedIds) {
            selectedIds = params.selectedIds.split(",").toList()
        }
        capa.attachmentChecked = false
        selectedIds.each {
            Capa8DAttachment capa8DAttachment = Capa8DAttachment.get(it)
            if(capa8DAttachment.isDeleted == false)
                CRUDService.softDelete(capa8DAttachment, capa8DAttachment.filename, params.deletejustification)
        }
        if(Capa8DAttachment.findByIsDeletedAndIssues(false,capa)){
            capa.attachmentChecked = true
            CRUDService.update(capa)
        }
        flash.message = message(code: 'quality.attachment.removed')

        if(capa.ownerType == ReasonOfDelayAppEnum.PVC.name())
            redirect(controller: "pvcIssue", action: 'edit', params: [id: capaInstanceId])
        else
            redirect(action: 'edit', params: [id: capaInstanceId])
    }

}