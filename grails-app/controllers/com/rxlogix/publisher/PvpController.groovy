package com.rxlogix.publisher

import com.rxlogix.CRUDService
import com.rxlogix.DataAnalysisController
import com.rxlogix.config.Comment
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherExecutedTemplate
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.enums.ReportTypeEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SecurityUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.Cookie

@Secured(["isAuthenticated()"])
class PvpController {
    static final String SECTION = "queryTemplateId"
    static final String ATTACHMENT = "attachmentId"
    static final Map executedTemplateStatusCss = [(PublisherExecutedTemplate.ExecutionStatus.SUCCESS.name())    : "alert-success",
                                                  (PublisherExecutedTemplate.ExecutionStatus.ERRORS.name())     : "alert-danger",
                                                  (PublisherExecutedTemplate.ExecutionStatus.FATAL_ERROR.name()): "alert-danger",
                                                  (PublisherExecutedTemplate.ExecutionStatus.WARNINGS.name())   : "alert-warning"]

    def userService
    def CRUDService
    def publisherService
    def ganttService
    def oneDriveRestService
    def spotfireService

    def dashboard() {}

    def reports() {}

    def updatePublisherTemplate() {
        def publisherTemplateSectionParameterValue = JSON.parse(params.publisherTemplateSectionParameterValue)
        PublisherConfigurationSection section = PublisherConfigurationSection.get(params.long("id"))
        section.setParameterValues(publisherTemplateSectionParameterValue.parameterValues)
        if (publisherTemplateSectionParameterValue.templateId) {
            section.publisherTemplate = PublisherTemplate.get(publisherTemplateSectionParameterValue.templateId as Long)
            section.filename = null
            section.templateFileData = null
        } else {
            section.publisherTemplate = null
            request.getFiles('publisherTemplateFile').each { MultipartFile file ->
                if (file.size > 0) {
                    section.templateFileData = file.bytes
                    section.filename = file.originalFilename
                }
            }
        }
        CRUDService.save(section)
        redirect(action: "sections", params: [id: params.reportId])
    }

    def updatePublisherTemplateAndGenerate() {
        def publisherTemplateSectionParameterValue = JSON.parse(params.publisherTemplateSectionParameterValue)
        PublisherConfigurationSection section = PublisherConfigurationSection.get(params.long("id"))
        section.setParameterValues(publisherTemplateSectionParameterValue.parameterValues)
        CRUDService.save(section)
        try {
            generateOne(params.long("id"))
        } catch (PublisherPermissionException e) {
            flash.error = message(code: "app.label.PublisherTemplate.error.sectionPermission", args: [e.getMessage()])
        }
        redirect(action: "sections", params: [id: params.reportId])
    }

    def updateName() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        section.name = params.value
        CRUDService.update(section)
        render "ok"
    }

    @Transactional
    def restoreDraft() {
        PublisherExecutedTemplate toRestore = PublisherExecutedTemplate.get(params.long("id"))
        if ((toRestore.status != PublisherExecutedTemplate.Status.ARCHIVE) || !toRestore.data ||
                (toRestore.executionStatus in [PublisherExecutedTemplate.ExecutionStatus.WARNINGS, PublisherExecutedTemplate.ExecutionStatus.FATAL_ERROR]))
            throw new Exception("Restoring to empty of failed version forbidden!")
        PublisherConfigurationSection section = toRestore.publisherConfigurationSection
        PublisherExecutedTemplate current = toRestore.publisherConfigurationSection.getDraftPublisherExecutedTemplates()
        current?.status = PublisherExecutedTemplate.Status.ARCHIVE
        current?.save()
        PublisherExecutedTemplate newOne = new PublisherExecutedTemplate(
                name: toRestore.name,
                numOfExecution: toRestore.numOfExecution + 1,
                status: PublisherExecutedTemplate.Status.DRAFT,
                executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS,
                data: toRestore.data,
                createdBy: userService.currentUser.fullName, modifiedBy: userService.currentUser.fullName
        )
        section.addToPublisherExecutedTemplates(newOne)
        Map parameters = WordTemplateExecutor.fetchParameters(new ByteArrayInputStream(newOne.data))
        section.pendingComment = parameters.comment?.size() ?: 0
        section.pendingVariable = parameters.variable?.size() ?: 0
        section.pendingManual = parameters.manual?.size() ?: 0
        CRUDService.save(section)
        pushTheLastSectionChanges(section)
        render "ok"
    }

    def fetchPendingParameters(long id) {
        Map parameters = [:]
        PublisherConfigurationSection section = PublisherConfigurationSection.get(id)
        if (section) {
            pullTheLastSectionChanges(section)
            parameters = publisherService.updatePendingParameters(section)
        }

        CRUDService.save(section)
        render parameters as JSON
    }

    void pullTheLastSectionChanges(section) {
        Map result = publisherService.pullTheLastSectionChanges(section)
        if (result.error) {
            flash.error = (flash.error ? flash.error + "; " : "") + result.message
        }
    }

    void pullTheLastFullDocumentChanges(section) {
        Map result = publisherService.pullTheLastFullDocumentChanges(section)
        if (result.error) {
            flash.error = (flash.error ? flash.error + "; " : "") + result.message
        }
    }

    void pushTheLastSectionChanges(section) {
        Map result = publisherService.pushTheLastSectionChanges(section)
        if (result.error) {
            flash.error = (flash.error ? flash.error + "; " : "") + result.message
        }
    }

    void pushTheLastFullDocumentChanges(document) {
        Map result = publisherService.pushTheLastFullDocumentChanges(document)
        if (result.error) {
            flash.error = (flash.error ? flash.error + "; " : "") + result.message
        }
    }

    void updateAccessRight(section, boolean isSection) {
        try {
            isSection ? publisherService.updateSectionAccessRights(section) : publisherService.updateFullDocumentAccessRights(section)
        } catch (Exception e) {
            log.error("Errore updateAccessRight!",e);
            flash.error = (flash.error ? flash.error + "; " : "") + e.getMessage()
        }
    }

    def pushTheLastChanges() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        (params.sectionId ? pushTheLastSectionChanges(section) : pushTheLastFullDocumentChanges(section))
        redirect(action: "sections", params: [id: params.reportId])
    }

    def saveParamsAndGenerateURL() {

        PublisherConfigurationSection section = PublisherConfigurationSection.get(params.id as Long)
        params.each { k, v ->
            if (k.startsWith("data_") && v)
                section.putParameterValues(k.substring(5), v);
        }
        CRUDService.save(section)
        pullTheLastSectionChanges(section)
        generateOne(params.id as Long, true)
        render "ok"
    }

    def changeSortOrder() {
        PublisherConfigurationSection section1 = PublisherConfigurationSection.get(params.long("id1"))
        PublisherConfigurationSection section2 = PublisherConfigurationSection.get(params.long("id2"))
        int sortNumber = section1.sortNumber
        section1.sortNumber = section2.sortNumber
        section2.sortNumber = sortNumber
        CRUDService.save(section1)
        CRUDService.save(section2)
        render "ok"
    }

    def updatePublisherReport() {
        PublisherReport publisherReport = PublisherReport.get(params.long("id"))
        request.getFiles('file').each { MultipartFile file ->
            if (file.size > 0) {
                if (publisherReport) {
                    publisherReport.data = file.bytes
                } else {
                    ExecutedPeriodicReportConfiguration configuration = ExecutedPeriodicReportConfiguration.get(params.reportId)
                    publisherReport = new PublisherReport(
                            name: params.name,
                            executedReportConfiguration: configuration,
                            comment: params.comment,
                            destination: params.destinations ?: null,
                            owner: userService.currentUser,
                            author: userService.currentUser,
                            workflowState: WorkflowState.getDefaultWorkState(),
                            qcWorkflowState: WorkflowState.getDefaultWorkState(),
                            data: file.bytes,
                            published: false,
                            isDeleted: false
                    )
                    String username = userService.currentUser.username
                    if (params.comment) publisherReport.comments = [(new Comment(textData: params.comment, publisherReport: publisherReport,
                            dateCreated: new Date(), lastUpdated: new Date(), createdBy: username, modifiedBy: username))]
                    if (configuration.gantt) {
                        ganttService.createFullStage(configuration.gantt, publisherReport)
                    } else {
                        publisherReport.dueDate = configuration.dueDate
                    }
                }
                CRUDService.saveOrUpdate(publisherReport)
            }
        }
        pushTheLastFullDocumentChanges(publisherReport)
        redirect(action: "sections", params: [id: params.reportId])
    }

    def removeSection() {
        PublisherConfigurationSection section = PublisherConfigurationSection.get(params.long("id"))
        oneDriveRestService.removeItem(section.lockCode)
        CRUDService.delete(section)
        render "ok"
    }

    def index() {
        forward(controller: "dashboard", action: 'index', params: [pvp: true])
    }

    def updateAssignedTo() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        if (params.long("id") != null) {
            section.assignedToGroup = UserGroup.get(params.long("id"))
        } else {
            section.assignedToGroup = null
        }
        CRUDService.save(section)
        updateAccessRight(section, !!params.sectionId)
        render "ok"
    }

    def updateReviewer() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        if (params.long("id") != null) {
            section.reviewer = User.get(params.long("id"))
        } else {
            section.reviewer = null
        }
        CRUDService.update(section)
        updateAccessRight(section, !!params.sectionId)
        render "ok"
    }

    def updateApprover() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        if (params.long("id") != null) {
            section.approver = User.get(params.long("id"))
        } else {
            section.approver = null
        }
        CRUDService.save(section)
        updateAccessRight(section, !!params.sectionId)
        render "ok"
    }

    def updateAuthor() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        if (params.long("id") != null) {
            section.author = User.get(params.long("id"))
        } else {
            section.author = null
        }
        CRUDService.update(section)
        updateAccessRight(section, !!params.sectionId)
        render "ok"
    }

    def updateDestination() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        section.destination = params.value
        CRUDService.update(section)
        render "ok"
    }

    def updateDue() {
        def section = params.sectionId ? PublisherConfigurationSection.get(params.long("sectionId")) : PublisherReport.get(params.long("publisherId"))
        section.dueDate = Date.parse("dd-MMM-yyyy", params.value)
        CRUDService.update(section)
        render "ok"
    }

    def updateComment() {
        PublisherReport publisherReport = PublisherReport.get(params.long("sectionId"))
        publisherReport.comment = params.value
        CRUDService.update(publisherReport)
        render "ok"
    }

    def distribute() {
        PublisherReport publisherReport = PublisherReport.get(params.long("id"))
        if(!publisherReport.published){
            pullTheLastFullDocumentChanges(publisherReport)
            oneDriveRestService.removeItem(publisherReport.lockCode)
        } else {
            pushTheLastFullDocumentChanges(publisherReport)
        }
        publisherReport.published = !publisherReport.published

        CRUDService.save(publisherReport)
        redirect(action: "sections", params: [id: params.reportId])
    }

    def publish(Long id, String ids) {
        try {
            List<byte[]> sections = []
            ExecutedPeriodicReportConfiguration configuration = ExecutedPeriodicReportConfiguration.get(id)
            ByteArrayInputStream first
            ids.split(",").each { String stringId ->
                Long cid = stringId as Long
                PublisherConfigurationSection section = configuration.publisherConfigurationSections.find { it.id == cid }
                if (section) {
                    byte[] data = section.publisherExecutedTemplates.find { it.status == PublisherExecutedTemplate.Status.FINAL }.data
                    sections << data
                }
            }
            byte[] out = publisherService.mergeDocx(sections)
            PublisherReport publisherReport = new PublisherReport(
                    name: params.name,
                    executedReportConfiguration: configuration,
                    comment: params.comment,
                    destination: params.destinations ?: null,
                    owner: userService.currentUser,
                    author: userService.currentUser,
                    workflowState: WorkflowState.getDefaultWorkState(),
                    qcWorkflowState: WorkflowState.getDefaultWorkState(),
                    data: out,
                    published: false,
                    isDeleted: false)
            String username = userService.currentUser.username
            if (params.comment) publisherReport.comments = [(new Comment(textData: params.comment, publisherReport: publisherReport,
                    dateCreated: new Date(), lastUpdated: new Date(), createdBy: username, modifiedBy: username))]
            if (configuration.gantt) {
                ganttService.createFullStage(configuration.gantt, publisherReport)
            } else {
                publisherReport.dueDate = configuration.dueDate
            }
            CRUDService.save(publisherReport)
            pushTheLastFullDocumentChanges(publisherReport)
        }catch(Exception e){
            log.error("Unexpected error", e)
            response.status = 500
            render(message(code: "default.server.error.message").toString())
        }
        render "ok"
    }

    def removePublisherReport() {
        PublisherReport publisherReport = PublisherReport.get(params.long("id"))
        oneDriveRestService.removeItem(publisherReport.lockCode)
        CRUDService.softDelete(publisherReport, publisherReport.name)
        render "ok"
    }

    def edit() {}

    def setAsFinal() {
        if (params.sectionid) {
            PublisherConfigurationSection section = PublisherConfigurationSection.get(params.long("sectionid"))
            pullTheLastSectionChanges(section)
            publisherService.updatePendingParameters(section)
            oneDriveRestService.removeItem(section.lockCode)
            if (!section.pendingVariable && !section.pendingManual) {
                PublisherExecutedTemplate executedTemplate = section.getDraftPublisherExecutedTemplates()
                executedTemplate.status = PublisherExecutedTemplate.Status.FINAL
                //  executedTemplate.data = WordTemplateExecutor.removeComments(new ByteArrayInputStream(executedTemplate.data))
                section.lastUpdated = new Date();
                CRUDService.save(section)
            }
        }
        redirect(action: "sections", params: [id: params.reportId])
    }

    def removeFinalStatus(Long id) {
        PublisherExecutedTemplate executedTemplate = PublisherExecutedTemplate.get(id)
        executedTemplate.status = PublisherExecutedTemplate.Status.DRAFT
        executedTemplate.publisherConfigurationSection.lastUpdated = new Date();
        CRUDService.save(executedTemplate.publisherConfigurationSection)
        executedTemplate.save(flush: true, failOnError: true)
        pushTheLastSectionChanges(executedTemplate.publisherConfigurationSection)
        redirect(action: "sections", params: [id: params.reportId])
    }

    def sections(Long id) {
        ExecutedPeriodicReportConfiguration executedReportConfiguration = ExecutedPeriodicReportConfiguration.get(id)
        if (!executedReportConfiguration) {
            notFound()
            return
        }
        User currentUser = userService.currentUser
        if (!executedReportConfiguration?.isVisibleForPublisher(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(view: "sections")
        } else {
            def includeAllStudyDrugsCases
            if (executedReportConfiguration.includeAllStudyDrugsCases) {
                includeAllStudyDrugsCases = executedReportConfiguration.studyDrugs
            }
            String defaultSharedWith = "Document Level" + " (" + ViewHelper.getMessage("users.label") +
                    ": " + executedReportConfiguration.executedDeliveryOption?.sharedWith?.collect { it.fullName }?.join(", ") +
                    "; " + ViewHelper.getMessage("app.excelExport.groups") +
                    " " + executedReportConfiguration.executedDeliveryOption?.sharedWithGroup?.collect { it.name }?.join(", ") + ")"
            Map allowedSectiona = getAllowedSections(executedReportConfiguration, defaultSharedWith)

            //--spotfire
            Map spotfireParams = [:]
            if (executedReportConfiguration.associatedSpotfireFile) {
                String username = currentUser.username ?: ""
                String secret = Holders.config.getProperty('spotfire.token_secret')
                String token = SecurityUtil.encrypt(secret, username)
                spotfireService.addAuthToken(token, username, currentUser.fullName, currentUser.email)
                response.addCookie(new Cookie("pvr-spotfire-cookie", System.currentTimeMillis().toString()))
                spotfireParams = [
                        user_name      : spotfireService.getHashedValue(username),
                        fileName       : Holders.config.getProperty('spotfire.libraryRoot') + "/" + executedReportConfiguration.associatedSpotfireFile,
                        libraryRoot    : Holders.config.getProperty('spotfire.libraryRoot'),
                        wp_url         : DataAnalysisController.composeSpotfireUrl(),
                        auth_token     : token,
                        callback_server: Holders.config.getProperty('spotfire.callbackUrl')]
            }
            Map model = [executedConfigurationInstance       : executedReportConfiguration,
                         executedTemplateQueriesForProcessing: executedReportConfiguration.getExecutedTemplateQueriesForProcessing(),
                         comments                            : executedReportConfiguration.comments,
                         reportType                          : ReportTypeEnum.REPORT,
                         includeAllStudyDrugsCases           : includeAllStudyDrugsCases,
                         viewSql                             : null,
                         table                               : allowedSectiona.sections,
                         canPublish                          : allowedSectiona.canPublish,
                         defaultSharedWith                   : defaultSharedWith,
                         canGenerateAll                      : allowedSectiona.canGenerateAll,
                         attachmentesUpdatedErrorMessage     : params.attachmentesUpdatedErrorMessage,
                         attachmentesUpdated                 : params.attachmentesUpdated,
                         publisherReports                    : getAllowedPublisherDocuments(executedReportConfiguration, defaultSharedWith),
                         isContributor                       : isCurrentUserContributor(executedReportConfiguration),
                         isOwner                             : (executedReportConfiguration.ownerId == currentUser.id)
            ]
            model.putAll(spotfireParams)
            render(view: "sections", model: model)

        }
    }

    def listPublisherExecutedLogUrl(Long id) {
        PublisherExecutedTemplate executedTemplates = PublisherExecutedTemplate.get(id)
        render([contentType: "application/json", encoding: "UTF-8", text: executedTemplates.executionLogJson])
    }

    def listPublisherExecutedTemplates(Long id) {
        List out = []
        PublisherConfigurationSection section = PublisherConfigurationSection.get(id)
        out = section.publisherExecutedTemplates.sort { it.id }
        out = out.collect {
            [id                : it.id, name: it.name,
             lastUpdated       : it.dateCreated?.format(DateUtil.DATEPICKER_FORMAT_AM_PM),
             status            : it.status.name(),
             executionStatus   : ViewHelper.getMessage(it.executionStatus?.i18nKey) ?: "-",
             executionStatusCss: executedTemplateStatusCss.get(it.executionStatus?.name()), modifiedBy: it.modifiedBy ?: ""
            ]
        }
        render out as JSON
    }

    def generateAllDraft(Long reportId, String sectionsId) {
        try {
            sectionsId.split(",").each {
                generateOne(Long.parseLong(it))
            }
        } catch (PublisherPermissionException e) {
            flash.error = message(code: "app.label.PublisherTemplate.error.sectionPermission", args: [e.getMessage()])
        }
        redirect(action: "sections", params: [id: params.reportId])
    }

    def generate(Long id) {
        try {
            generateOne(id)
        } catch (PublisherPermissionException e) {
            flash.error = message(code: "app.label.PublisherTemplate.error.sectionPermission", args: [e.getMessage()])
        }
        redirect(action: "sections", params: [id: params.reportId])
    }

    void generateOne(Long id, Boolean cntinue = false) throws PublisherPermissionException {
        PublisherConfigurationSection section = PublisherConfigurationSection.get(id)
        if (!cntinue && !section.publisherTemplate && !section.filename) {
            flash.error = message(code: "app.label.PublisherTemplate.error.noTemplateFound")
        } else {
            publisherService.processSection(section, cntinue)
            pushTheLastSectionChanges(section)
        }
    }

    def downloadPublisherExecutedTemplate(Long id) {
        if (params.boolean("fromOneDrive")) {
            PublisherExecutedTemplate executedTemplate = PublisherExecutedTemplate.get(id)
            pullTheLastSectionChanges(executedTemplate.publisherConfigurationSection)
            render(file: executedTemplate.publisherConfigurationSection.getDraftPublisherExecutedTemplates().data, fileName: executedTemplate.name + ".docx", contentType: "application/octet-stream")
        } else {
            PublisherExecutedTemplate executedTemplate = PublisherExecutedTemplate.get(id)
            render(file: executedTemplate.data, fileName: executedTemplate.name + ".docx", contentType: "application/octet-stream")
        }
    }

    def downloadPublisherReport(Long id) {

        PublisherReport publisherReport = PublisherReport.get(id)
        if (params.boolean("fromOneDrive"))
            pullTheLastFullDocumentChanges(publisherReport)
        render(file: publisherReport.data, fileName: publisherReport.name + ".docx", contentType: "application/octet-stream")
    }

    List getAllowedPublisherDocuments(ExecutedPeriodicReportConfiguration executedReportConfiguration, defaultSharedWith) {
        User currentUser = userService.currentUser
        List table = []
        executedReportConfiguration.publisherReports?.sort { it.id }?.each { PublisherReport report ->
            if (!report.isDeleted && report.isVisible(userService.currentUser)) {
                def aiCssClassAndLabel = getAiCssClassAndLabel(report)
                table << [id          : report.id,
                          name        : report.name,
                          destination : report.destination,
                          assignedTo  : report.assignedToGroup ? (ViewHelper.getMessage("app.excelExport.groups") + report.assignedToGroup.name) : defaultSharedWith,
                          dueDate     : report.dueDate?.format(DateUtil.DATEPICKER_FORMAT),
                          dueDateClass: getDueDateCssClass(report, WorkflowConfigurationTypeEnum.PUBLISHER_FULL),
                          aiClass     : aiCssClassAndLabel.cssClass,
                          aiLabel     : aiCssClassAndLabel.label,
                          comments    : report.comments,
                          lockedBy    : null,//report.lockedBy?.fullName,
                          canUnlock   : true,// ((report.lockedBy == currentUser) || currentUser.isAdmin()),
                          modifiedBy  : report.modifiedBy,
                          lastUpdated : report.lastUpdated,
                          author      : report.author?.fullName,
                          reviewer    : report.reviewer?.fullName,
                          approver    : report.approver?.fullName,
                          workflowSate: report.workflowState?.name ?: ViewHelper.getMessage("app.label.new"),
                          qcWorkflowState: report.qcWorkflowState?.name ?: ViewHelper.getMessage("app.label.new"),
                          published   : report.published
                ]
            }
        }
        table
    }

    def getSectionsInfoList() {
        ExecutedPeriodicReportConfiguration executedReportConfiguration = ExecutedPeriodicReportConfiguration.get(params.id)
        String defaultSharedWith = "Document Level" + " (" + ViewHelper.getMessage("users.label") +
                ": " + executedReportConfiguration.executedDeliveryOption?.sharedWith?.collect { it.fullName }?.join(", ") +
                "; " + ViewHelper.getMessage("app.excelExport.groups") +
                " " + executedReportConfiguration.executedDeliveryOption?.sharedWithGroup?.collect { it.name }?.join(", ") + ")"
        List aaData = getAllowedSections(executedReportConfiguration, defaultSharedWith).sections
        render([aaData: aaData, recordsTotal: aaData.size(), recordsFiltered: aaData.size()] as JSON)
    }

    private boolean isCurrentUserContributor(ExecutedPeriodicReportConfiguration executedConfiguration) {
        Long userId = userService.currentUser.id
        return userService.currentUser.isAdmin() || (executedConfiguration.primaryPublisherContributor?.id == userId) || executedConfiguration.publisherContributors?.find { it.id == userId }
    }

    Map getAllowedSections(ExecutedPeriodicReportConfiguration executedReportConfiguration, defaultSharedWith) {
        User currentUser = userService.currentUser
        List table = []
        Boolean canPublish = true
        Boolean canGenerateAll = true
        List destinationsFilter = []
        if (params && params.destinationFilter)
            destinationsFilter = params.destinationFilter?.split(";")?.collect { it.toString() }
        executedReportConfiguration.publisherConfigurationSections?.sort { it.sortNumber }?.each { PublisherConfigurationSection section ->
            if (section.isVisible(userService.currentUser)) {
                def aiCssClassAndLabel = getAiCssClassAndLabel(section)
                def finalDraft = getPublisherAction(section, executedReportConfiguration)
                if (!finalDraft.finalFileId)
                    canPublish = false
                Set<Long> finalSates = WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.PUBLISHER_SECTION)?.collect { it.id }
                if (!(section.workflowState?.id in finalSates))
                    canPublish = false
                if (destinationsFilter.size() == 0 || !section.destination || (section.destination && !destinationsFilter.disjoint(section.destination.split(";").collect { it.toString() }))) {
                    table << [id                  : section.id,
                              name                : section.name,
                              destination         : section.destination,
                              assignedTo          : section.assignedToGroup ? (ViewHelper.getMessage("app.excelExport.groups") + section.assignedToGroup.name) : defaultSharedWith,
                              dueDate             : section.dueDate?.format(DateUtil.DATEPICKER_FORMAT),
                              dueDateClass        : getDueDateCssClass(section, WorkflowConfigurationTypeEnum.PUBLISHER_SECTION),
                              aiClass             : aiCssClassAndLabel.cssClass,
                              aiLabel             : aiCssClassAndLabel.label,
                              comments            : section.comments,
                              publisherAction     : finalDraft.action,
                              noTemplate          : finalDraft.noTemplate,
                              finalFileId         : finalDraft.finalFileId,
                              draftFileId         : finalDraft.draftFileId,
                              draftExecutionStatus: finalDraft.draftExecutionStatus,
                              executionStatusCss  : executedTemplateStatusCss.get(finalDraft.draftExecutionStatus?.name()),
                              lockedBy            : null,// section.lockedBy?.fullName,
                              canUnlock           : true,//((section.lockedBy == currentUser) || currentUser.isAdmin()),
                              parameterValues     : section.parameterValues,
                              publisherTemplate   : section.publisherTemplate,
                              filename            : section.filename,
                              modifiedBy          : section.modifiedBy,
                              lastUpdated         : section.lastUpdated,
                              author              : section.author?.fullName,
                              reviewer            : section.reviewer?.fullName,
                              approver            : section.approver?.fullName,
                              pendingManual       : section.pendingManual != null ? section.pendingManual : "-",
                              pendingVariable     : section.pendingVariable != null ? section.pendingVariable : "-",
                              pendingComment      : section.pendingComment != null ? section.pendingComment : "-",
                              workflowSate        : section.workflowState?.name ?: ViewHelper.getMessage("app.label.new"),
                    ]
                }
            } else {
                canPublish = false; canGenerateAll = false
            }
        }
        [sections: table, canPublish: canPublish, canGenerateAll: canGenerateAll]
    }

    private static getDueDateCssClass(section, WorkflowConfigurationTypeEnum type) {
        String clazz = "no"
        Date now = new Date();
        Date soon = now + 30;
        boolean isInFinalSate = section.workflowState.isFinalState(type)
        if (type == WorkflowConfigurationTypeEnum.PUBLISHER_FULL) {
            isInFinalSate = isInFinalSate && section?.qcWorkflowState?.isFinalState(type)
        }
        if (section.dueDate) {
            if (section.dueDate > now && section.dueDate < soon && !isInFinalSate) clazz = "label-primary";
            if (section.dueDate < now && !isInFinalSate) clazz = "label-danger text-white";
        }
        return clazz
    }

    private static getPublisherAction(PublisherConfigurationSection section, executedConfigurationInstance) {
        PublisherExecutedTemplate executedTemplate = section.getLastPublisherExecutedTemplates()
        String action = PublisherExecutedTemplate.Status.EMPTY.name()
        Long finalFileId
        Long draftFileId
        PublisherExecutedTemplate.ExecutionStatus draftExecutionStatus
        if (executedTemplate) {
            if (executedTemplate.status == PublisherExecutedTemplate.Status.FINAL) {
                action = PublisherExecutedTemplate.Status.FINAL.name()
                finalFileId = executedTemplate.id
            } else if (executedTemplate.status == PublisherExecutedTemplate.Status.DRAFT) {
                action = PublisherExecutedTemplate.Status.DRAFT.name()
                PublisherExecutedTemplate draft = section.getDraftPublisherExecutedTemplates()
                if (draft) {
                    draftExecutionStatus = draft.executionStatus
                    draftFileId = draft.id
                }
            }
        } else {
            PublisherExecutedTemplate error = section.publisherExecutedTemplates?.max { it.id }
            if (error) {
                draftExecutionStatus = error.executionStatus
                draftFileId = error.id
            }
        }
        Boolean noTemplate = (!section.publisherTemplate && !section.filename)

        return [action: action, finalFileId: finalFileId, noTemplate: noTemplate, draftFileId: draftFileId, draftExecutionStatus: draftExecutionStatus]
    }

    private static getAiCssClassAndLabel(def section) {
        String state = "", label = ""
        if (section.actionItems) {
            boolean waiting = false

            for (ai in section.actionItems) {
                if ((ai.status != StatusEnum.CLOSED) && (ai.dueDate < new Date())) {
                    state = "btn-danger"; label = ViewHelper.getMessage("app.label.PublisherTemplate.ai.overdue")
                }//ActionItemGroupState.OVERDUE
                if (ai.status != StatusEnum.CLOSED && !ai.isDeleted) waiting = true
            }
            state = waiting ? "btn-warning" : "btn-success"
            label = waiting ? ViewHelper.getMessage("app.label.PublisherTemplate.ai.waiting") : ViewHelper.getMessage("app.label.PublisherTemplate.ai.allclosed")
        }
        [cssClass: state, label: label]
    }

    def uploadDocument() {
        PublisherConfigurationSection section = PublisherConfigurationSection.get(params.long("id"))
        def configuration = section.executedConfiguration
        request.getFiles('publisherTemplateFile').each { MultipartFile file ->
            if (file.size > 0) {
                section.getDraftPublisherExecutedTemplates()?.status = PublisherExecutedTemplate.Status.ARCHIVE
                PublisherExecutionLog log = new PublisherExecutionLog()
                PublisherExecutedTemplate executedTemplates = new PublisherExecutedTemplate()
                executedTemplates.name = file.originalFilename
                executedTemplates.numOfExecution = (section.publisherExecutedTemplates?.size() ?: 0) + 1
                executedTemplates.status = PublisherExecutedTemplate.Status.DRAFT
                executedTemplates.createdBy = userService.currentUser.fullName
                executedTemplates.modifiedBy = userService.currentUser.fullName
                executedTemplates.data = file.bytes
                section.addToPublisherExecutedTemplates(executedTemplates)
                Map templateParameters = [:]
                params.parameterName?.eachWithIndex { name, i ->
                    templateParameters.put(name, params.parameterValue[i])
                }
                section.setParameterValues(templateParameters)

                CRUDService.update(section)
                try {
                    generateOne(params.long("id"), true)
                } catch (PublisherPermissionException e) {
                    flash.error = message(code: "app.label.PublisherTemplate.error.sectionPermission", args: [e.getMessage()])
                }
            }
        }

        redirect(action: "sections", params: [id: params.reportId])
    }

    def addSection() {
        ExecutedPeriodicReportConfiguration configuration = ExecutedPeriodicReportConfiguration.get(params.long("reportId"))
        PublisherConfigurationSection section = new PublisherConfigurationSection(name: "New section",
                workflowState: WorkflowState.getDefaultWorkState(),
                sortNumber: configuration.publisherConfigurationSections.size() + 2)
        configuration.addToPublisherConfigurationSections(section)
        CRUDService.save(configuration)
        if (configuration.gantt)
            ganttService.createSectionStage(configuration.gantt, null, section, configuration, configuration.dateCreated.plus(configuration.gantt.defaultReportDuration), null)
        redirect(action: "sections", params: [id: params.reportId])
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.report'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: HttpStatus.NOT_FOUND }
        }
    }

    def pvpSections(Long id, String term) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(id)
        List list = executedReportConfiguration.publisherConfigurationSections.findAll {
            it.state != PublisherExecutedTemplate.Status.EMPTY
        }?.collect {
            [id: it.id, name: it.name, text: it.name + "(" + ViewHelper.getMessage("app.label.country") + ": " + (it.destination ?: "") + "; " +
                    (it.state == PublisherExecutedTemplate.Status.FINAL ? ViewHelper.getMessage("app.publisherExecutionStatus.Status.FINAL") : ViewHelper.getMessage("app.publisherExecutionStatus.Status.DRAFT")) +
                    ")"]
        }
        if (term) {
            String upTerm = term.toUpperCase()
            list = list.findAll { it.name.toUpperCase().contains(upTerm) }
        }
        render list as JSON

    }

    def pvpFullDocuments(Long id, String term) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(id)
        List list = executedReportConfiguration.publisherReports?.findAll { it.data && !it.isDeleted }?.collect {
            [id: it.id, name: it.name, text: it.name + "(" + ViewHelper.getMessage("app.label.country") + ": " + (it.destination ?: "") + "; " +
                    (it.published ? ViewHelper.getMessage("app.publisherExecutionStatus.Status.FINAL") : ViewHelper.getMessage("app.publisherExecutionStatus.Status.DRAFT")) + ")"
            ]
        }

        if (term) {
            String upTerm = term.toUpperCase()
            list = list.findAll { it.name.toUpperCase().contains(upTerm) }
        }
        render list as JSON
    }
}
