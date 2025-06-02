package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class IcsrReportController {
    def configurationService
    def userService
    def CRUDService
    def reportExecutorService
    def icsrReportService
    def dmsService
    def taskTemplateService
    def qualityService
    def dynamicReportService
    def notificationService
    def reportFieldService

    static allowedMethods = [transferCases:'POST',update: ['PUT','POST'],save: 'POST', delete: ['DELETE','POST']]


    @Secured(['ROLE_ICSR_REPORTS_VIEWER'])
    def index() {
    }

    @Secured(['ROLE_ICSR_REPORTS_VIEWER'])
    def reports(Long id) {

    }

    def showResult(Long id) {
        ExecutedIcsrReportConfiguration executedReportConfiguration = ExecutedIcsrReportConfiguration.read(id)
        if (!executedReportConfiguration) {
            notFound()
            return
        }
        forward(action: "show", id: executedReportConfiguration.executedTemplateQueries?.first()?.reportResult?.id)
    }

    def show(ReportResult reportResult) {
        if (!reportResult) {
            notFound()
            return
        }
        boolean isInDraftMode = false
        String reportName = dynamicReportService.getReportName(reportResult, isInDraftMode, params)
        // Reports not view able by a user will not be delivered to the inbox
        User currentUser = userService.getUser()
        if (!reportResult?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [reportName, message(code: "app.label.report")])
            render(view: "index", model: [executedConfigurationInstance: reportResult.executedTemplateQuery.executedConfiguration,
                                          executedTemplateInstance     : reportResult.executedTemplateQuery.executedTemplate])
            return
        }
        ExecutedIcsrReportConfiguration executedReportConfiguration = (ExecutedIcsrReportConfiguration) reportResult.executedTemplateQuery.executedConfiguration
        String userLocale = (userService.currentUser?.preference?.locale ?: executedReportConfiguration.owner.preference.locale).toString()
        Long referenceProfileId = executedReportConfiguration.referenceProfileName ? (ExecutionStatus.findByReportNameAndEntityType(executedReportConfiguration.referenceProfileName, ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION, [sort: 'reportVersion', order: 'desc'])?.entityId) : null
        if (referenceProfileId && (!executedReportConfiguration.receiverId || (IcsrProfileConfiguration.read(referenceProfileId)?.recipientOrganization?.unitRegisteredId != executedReportConfiguration.receiverId))) {
            log.debug("No valid reference profile id is found for ${executedReportConfiguration}")
            referenceProfileId = null
        }
        [reportResult: reportResult, executedConfigurationInstance: executedReportConfiguration, referenceProfileId: referenceProfileId, userLocale: userLocale]
    }

    def targetStatesAndApplications() {
        def intialState = params.initialState;
        def executedReportConfiguration = params.executedReportConfiguration
        render icsrReportService.targetStatesAndApplications(executedReportConfiguration, intialState) as JSON
    }

    def updateReportState(Long executedConfigId) {
        try {
            ExecutedIcsrReportConfiguration executedPeriodicReportConfiguration = ExecutedIcsrReportConfiguration.createCriteria().get {
                eq('id', executedConfigId)
                'workflowState' {
                    eq('name', params.oldState);
                }
            }
            executedPeriodicReportConfiguration.workflowState = WorkflowState.findByName(params.newState)
            CRUDService.saveWithoutAuditLog(executedPeriodicReportConfiguration)
            render([success: true, message: message(code: "app.periodicReportConfiguration.state.update.success")] as JSON)
        } catch (Exception ex) {
            log.error("Couldn't update state due to " + ex.message)
            response.status = 500
            render([message: "Server Error"] as JSON)
        }

    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def create() {
        IcsrReportConfiguration configurationInstance = new IcsrReportConfiguration()
        Map fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.configurationParams) {
            initConfigurationFromMap(configurationInstance, fromSession.configurationParams)
        }
        Map templateQueryIndex = fromSession.templateQueryIndex
        boolean templateBlanks = false
        if (params.selectedTemplate) {
            ReportTemplate template = ReportTemplate.get(params.selectedTemplate)
            if (template) {
                IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(template: template)
                configurationInstance.addToTemplateQueries(templateQuery)
                if (template.hasBlanks) {
                    templateBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.template.notFound', args: [params.selectedTemplate])
            }
        }

        boolean queryBlanks = false
        if (params.selectedQuery) {
            SuperQuery query = SuperQuery.get(params.selectedQuery)
            if (query) {
                IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(query: query)
                configurationInstance.addToTemplateQueries(templateQuery)
                if (query.hasBlanks) {
                    queryBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.query.notFound', args: [params.selectedQuery])
            }
        }
        User user = getCurrentUser()
        render(view: "create", model: [configurationInstance: configurationInstance, queryBlanks: queryBlanks, templateBlanks: templateBlanks, templateQueryIndex: templateQueryIndex, hasConfigTemplateCreatorRole: getCurrentUser().isConfigurationTemplateCreator(), sourceProfiles: SourceProfile.sourceProfilesForUser(user)])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def createTemplate() {
        saveConfigurationToSession()
        redirect(controller: "template", action: 'create', params: [templateType: params.templateType])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def createQuery() {
        saveConfigurationToSession()
        redirect(controller: "query", action: 'create')
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def edit(Long id) {
        IcsrReportConfiguration configurationInstance = id ? IcsrReportConfiguration.read(id) : null
        if (!configurationInstance) {
            notFound()
            return
        }
        Map fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.configurationParams && (id == Long.parseLong(fromSession.configurationParams.id))) {
            initConfigurationFromMap(configurationInstance, fromSession.configurationParams)
        }
        Map templateQueryIndex = fromSession.templateQueryIndex
        User currentUser = getCurrentUser()
        if (configurationInstance.running) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.reportName])
            redirect(action: "index")
        }else
        if (!configurationInstance?.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.reportName])
            redirect(action: "index")
        } else {
            try {
                if (configurationInstance.isEnabled && !configurationInstance?.nextRunDate) {
                    // update to find out if configuration finished executing
                    log.error("Configuration with id ${configurationInstance.id} and WITHOUT nextRunDate has isEnabled property set to true")
                    configurationInstance.isEnabled = false
                    CRUDService.saveWithoutAuditLog(configurationInstance)
                }
            } catch (ValidationException ve) {
                log.warn("Error validation error while updating in edit")
            }
            User user = getCurrentUser()
            configurationInstance.scheduleDateJSON = configurationService.correctSchedulerJSONForCurrentDate(configurationInstance.scheduleDateJSON, configurationInstance.nextRunDate)
            render(view: "edit", model: [configurationInstance : configurationInstance, templateQueryIndex: templateQueryIndex,
                                         configSelectedTimeZone: params?.configSelectedTimeZone, hasConfigTemplateCreatorRole: currentUser.isConfigurationTemplateCreator(), sourceProfiles: SourceProfile.sourceProfilesForUser(user)])
        }
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    public disable() {
        IcsrReportConfiguration periodicReportConfigurationInstance = IcsrReportConfiguration.get(params.long('id'))
        if (!periodicReportConfigurationInstance) {
            notFound()
            return
        }
        periodicReportConfigurationInstance.setIsEnabled(false)
        periodicReportConfigurationInstance.executing = false
//        populateModel(periodicReportConfigurationInstance) Disabled Updating data while unscheduling

        try {
            periodicReportConfigurationInstance = (IcsrReportConfiguration) CRUDService.update(periodicReportConfigurationInstance)
        } catch (ValidationException ve) {
            IcsrReportConfiguration originalConfiguration = IcsrReportConfiguration.read(periodicReportConfigurationInstance.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            render view: "edit", model: [configurationInstance: originalConfiguration, configSelectedTimeZone: originalConfiguration.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'configuration.label'), periodicReportConfigurationInstance.reportName])
                redirect(action: "view", id: periodicReportConfigurationInstance.id)
            }
            '*' { respond periodicReportConfigurationInstance, [status: OK] }
        }

    }

    @Secured(['ROLE_ICSR_REPORTS_VIEWER'])
    def view(Long id) {
        IcsrReportConfiguration icsrReportConfiguration = IcsrReportConfiguration.read(id)
        if (!icsrReportConfiguration) {
            notFound()
            return
        }
        String configurationJson = null
        if (params.viewConfigJSON && SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            configurationJson = configurationService.getConfigurationAsJSON(icsrReportConfiguration)
        }
        render(view: "view", model: [templateQueries: icsrReportConfiguration.templateQueries, configurationInstance: icsrReportConfiguration,
                                     isExecuted     : false, deliveryOption: icsrReportConfiguration.deliveryOption,
                                     viewSql        : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(icsrReportConfiguration) : null, viewGlobalSql: params.getBoolean("viewSql") ? reportExecutorService.debugGlobalQuerySQL(icsrReportConfiguration) : null, configurationJson: configurationJson])
    }

    @Secured(['ROLE_ICSR_REPORTS_VIEWER'])
    def viewExecutedConfig(ExecutedIcsrReportConfiguration executedConfiguration) {
        if (!executedConfiguration) {
            notFound()
            return
        }
        render(view: "view", model: [templateQueries: executedConfiguration.executedTemplateQueries, configurationInstance: executedConfiguration,
                                     isExecuted     : true, deliveryOption: executedConfiguration.executedDeliveryOption])
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def queryDataTemplate() {
        def result = []
        if (params.queryId) {
            Query.get(params.queryId)?.queryExpressionValues?.each {
                result += [value: it.value, operator: it.operator.name(),
                           field: it.reportField.name, key: it.key]
            }
        }
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    @Transactional
    def save() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'create')
            return
        }
        IcsrReportConfiguration configurationInstance = new IcsrReportConfiguration()
        configurationInstance.setIsEnabled(false)
        populateModel(configurationInstance)

        try {
            configurationInstance = (IcsrReportConfiguration) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            render view: "create", model: [configurationInstance : configurationInstance,
                                           configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }


    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    @Transactional
    def update() {
        IcsrReportConfiguration configurationInstance = IcsrReportConfiguration.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: configurationInstance.id)
            return
        }
        populateModel(configurationInstance)
        try {
            CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            render view: "edit", model: [configurationInstance : configurationInstance,
                                         configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
                if(params.reportId) {
                    redirect(controller: "report", action: "showFirstSection", id: params.reportId)
                }else{
                    redirect(action: "view", id: configurationInstance.id)
                }
            }
            '*' { respond configurationInstance, [status: OK] }
        }
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def copy(IcsrReportConfiguration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = getCurrentUser()
        def savedConfig = configurationService.copyConfig(originalConfig, currentUser)
        if (savedConfig.hasErrors()) {
            chain(action: "index", model: [error: savedConfig])
        } else {
            flash.message = message(code: "app.copy.success", args: [savedConfig.reportName])
            redirect(action: "view", id: savedConfig.id)
        }
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def delete(IcsrReportConfiguration configurationInstance) {

        if (!configurationInstance) {
            notFound()
            return
        }

        User currentUser = getCurrentUser()

        if (!configurationInstance.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.configuration.delete.permission", args: [configurationInstance.reportName])
            redirect(view: "index")
            return
        }


        try {
            CRUDService.softDelete(configurationInstance, configurationInstance.reportName, params.deleteJustification)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), configurationInstance.reportName])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.configuration'), configurationInstance.reportName])
                    redirect(action: "view", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }

    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    @Transactional
    def run(Long id) {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        IcsrReportConfiguration icsrReportConfiguration = IcsrReportConfiguration.get(id)
        if (!icsrReportConfiguration) {
            icsrReportConfiguration = new IcsrReportConfiguration()
            if(!validateTenant(params.long('tenantId'))){
                flash.error = message(code: "invalid.tenant")
                redirect(action: 'create')
                return
            }
        } else if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: icsrReportConfiguration.id)
            return
        }
        icsrReportConfiguration.setIsEnabled(true)
        populateModel(icsrReportConfiguration)
        try {
            if (icsrReportConfiguration.id) {
                icsrReportConfiguration = (IcsrReportConfiguration) CRUDService.update(icsrReportConfiguration)
            } else {
                icsrReportConfiguration = (IcsrReportConfiguration) CRUDService.save(icsrReportConfiguration)
            }
        } catch (ValidationException ve) {
            icsrReportConfiguration.errors = ve.errors
            icsrReportConfiguration.setIsEnabled(false)
            if (id) {
                render view: "edit", model: [configurationInstance : icsrReportConfiguration,
                                             configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            } else {
                render view: "create", model: [configurationInstance : icsrReportConfiguration,
                                               configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            }
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'app.Configuration.RunningMessage')
                if(params.reportId) {
                    redirect(controller: "report", action: "showFirstSection", id: params.reportId)
                } else {
                    redirect(controller: "executionStatus", action: "list")
                }
            }
            '*' { respond icsrReportConfiguration, [status: CREATED] }
        }
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def runOnce(IcsrReportConfiguration periodicReportConfigurationInstance) {
        if (!periodicReportConfigurationInstance) {
            notFound()
            return
        }
        if (periodicReportConfigurationInstance.nextRunDate && periodicReportConfigurationInstance.isEnabled) {
            flash.warn = message(code: 'app.configuration.run.exists')
            redirect(action: "index")
            return
        }
        try {
            periodicReportConfigurationInstance.isPriorityReport = params.boolean('isPriorityReport')
            icsrReportService.scheduleToRunOnce(periodicReportConfigurationInstance)
        } catch (ValidationException ve) {
            periodicReportConfigurationInstance.errors = ve.errors
            periodicReportConfigurationInstance.setIsEnabled(false)
            render view: "create", model: [configurationInstance : periodicReportConfigurationInstance,
                                           configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        }
        if(periodicReportConfigurationInstance.isPriorityReport){
            flash.message = message(code: 'app.Configuration.PriorityRunningMessage')
        }else{
            flash.message = message(code: 'app.Configuration.RunningMessage')
        }
        redirect(controller: 'executionStatus', action: "list")
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def bulkUpdate() {
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def importExcel() {
        MultipartFile file = request.getFile('file')
        Workbook workbook = null

        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }

        Map result = icsrReportService.importFromExcel(workbook)
        String addCountMessage = icsrReportService.getDisplayMessage('app.bulkUpdate.error.added', result.added)
        String updateCountMessage = icsrReportService.getDisplayMessage('app.bulkUpdate.error.updated', result.updated)
        flash.message = [addCountMessage, updateCountMessage].join("\n\n")
        if (result.errors.size() > 0)
            flash.error = result.errors.size() + " errors:\n" + result.errors.join("\n")
        redirect(action: "bulkUpdate")
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def exportToExcel() {

        User currentUser = userService.currentUser
        LibraryFilter filter = new LibraryFilter(params, currentUser, IcsrReportConfiguration, [IcsrReportConfiguration.class])
        List<Long> idsForUser = IcsrReportConfiguration.fetchAllIdsForBulkUpdate(filter).list([sort: params.sort, order: params.direction])
        List<IcsrReportConfiguration> configurationList = IcsrReportConfiguration.getAll(idsForUser)

        def data = configurationList.collect { conf ->
            def product = conf.productSelection ? JSON.parse(conf.productSelection as String) : [:]
            [conf.reportName,
             conf.configurationTemplate?.reportName ?: "",
             product["1"]?.collect { it.name }?.join(","),
             product["2"]?.collect { it.name }?.join(","),
             product["3"]?.collect { it.name }?.join(","),
             product["4"]?.collect { it.name }?.join(","),
             conf.periodicReportType.name(),
             conf.globalDateRangeInformation.dateRangeEnum.name(),
             conf.globalDateRangeInformation.relativeDateRangeValue,
             conf.globalDateRangeInformation.dateRangeStartAbsolute?.format(DateUtil.ISO_DATE_TIME_FORMAT),
             conf.globalDateRangeInformation.dateRangeEndAbsolute?.format(DateUtil.ISO_DATE_TIME_FORMAT),
             conf.primaryReportingDestination,
             conf.dueInDays ?: "",
             conf.scheduleDateJSON
            ]
        }
        def metadata = [sheetName: "Configurations",
                        columns  : [
                                [title: ViewHelper.getMessage("app.label.reportName"), width: 25],
                                [title: ViewHelper.getMessage("app.PeriodicReport.configuration.template.label"), width: 25],
                                [title: ViewHelper.getMessage("app.widget.button.quality.product.label") + " " + ViewHelper.getMessage("productDictionary.ingredient"), width: 25],
                                [title: ViewHelper.getMessage("app.widget.button.quality.product.label") + " " + ViewHelper.getMessage("productDictionary.family"), width: 25],
                                [title: ViewHelper.getMessage("app.periodicReport.executed.productName.label"), width: 25],
                                [title: ViewHelper.getMessage("app.trade.name"), width: 25],
                                [title: ViewHelper.getMessage("app.label.periodicReportType"), width: 25],
                                [title: ViewHelper.getMessage("app.label.DateRangeType"), width: 25],
                                [title: " X ", width: 25],
                                [title: ViewHelper.getMessage("app.label.startDate"), width: 25],
                                [title: ViewHelper.getMessage("app.label.endDate"), width: 25],
                                [title: ViewHelper.getMessage("app.periodicReport.executed.reportingDestination.label"), width: 25],
                                [title: ViewHelper.getMessage("app.label.dueInDaysPastDLP"), width: 25],
                                [title: ViewHelper.getMessage("app.label.scheduler"), width: 25]
                        ]]
        byte[] file = qualityService.exportToExcel(data, metadata)
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: System.currentTimeMillis() + ".xlsx")
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def ajaxCopy() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        IcsrReportConfiguration instance = params.id ? IcsrReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else {

            try {
                User currentUser = userService.currentUser
                def savedConfig = configurationService.copyConfig(instance, currentUser)
                responseDTO.data = icsrReportService.toBulkTableMap(savedConfig)
            } catch (ValidationException e) {
                responseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def ajaxRun() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        IcsrReportConfiguration instance = params.id ? IcsrReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else if (instance.nextRunDate && instance.isEnabled) {
            responseDTO.setFailureResponse(message(code: 'app.configuration.run.exists') as String)
        } else {
            try {
                instance.setIsEnabled(true)
                setNextRunDateAndScheduleDateJSON(instance)
                CRUDService.update(instance)
                responseDTO.data = [nextRunDate: DateUtil.getLongDateStringForTimeZone(instance.nextRunDate, userService.currentUser?.preference?.timeZone)]
            } catch (ValidationException e) {
                responseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def ajaxDelete() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        IcsrReportConfiguration instance = params.id ? IcsrReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else if (!instance.isEditableBy(userService.currentUser)) {
            responseDTO.setFailureResponse(message(code: "app.configuration.delete.permission", args: [instance.reportName]) as String)
        } else {
            try {
                CRUDService.softDelete(instance, instance.reportName, params.deleteJustification)
                responseDTO.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), instance.reportName])
            } catch (ValidationException e) {
                responseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def editField() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        IcsrReportConfiguration instance = params.id ? IcsrReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else {
            try {
                if (params.globalDateRangeInformation) {
                    assignParameterValuesToGlobalQuery(instance)
                    CRUDService.update(instance)
                } else if (params.reportName) {
                    instance.setReportName(params.reportName.trim())
                    instance.validate(["reportName"])
                    if (instance.hasErrors())
                        responseDTO.setFailureResponse(g.message(code: 'com.rxlogix.config.configuration.name.unique.per.user') as String)
                    else
                        CRUDService.update(instance)
                } else if (params.scheduleDateJSON) {
                    instance.scheduleDateJSON = params.scheduleDateJSON
                    CRUDService.update(instance)
                    responseDTO.data = [json: instance.scheduleDateJSON, label: icsrReportService.parseScheduler(instance.scheduleDateJSON, userService.currentUser?.preference?.locale)]
                } else {
                    bindData(instance, params)
                    CRUDService.update(instance)
                }
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }


    def getDmsFolders() {
        render dmsService.getFolderList(params.folder) as JSON
    }

    @Secured(['ROLE_ICSR_REPORTS_EDITOR'])
    def createFromTemplate(IcsrReportConfiguration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = userService.getCurrentUser()
        def savedConfig = configurationService.copyConfig(originalConfig, currentUser, null, Tenants.currentId() as Long, true)
        if (savedConfig.hasErrors()) {
            chain(action: "index", model: [theInstance: savedConfig])
        } else {
            flash.message = message(code: "app.copy.success", args: [savedConfig.reportName])
            redirect(action: "edit", id: savedConfig.id, params: [fromTemplate: true])
        }
    }

    def downloadBatchXML(ReportResult reportResult) {
        List<String> caseNumbers = params.getList("caseNumber[]")
        IcsrReportSpecEnum reportSpec = IcsrReportSpecEnum.valueOf(params.reportSpec)
        String reportFileName = "ICSR Batch Report.xml"
        File reportFile = icsrReportService.createBatchXMLReport(reportResult, caseNumbers, reportSpec)
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
        try {
            MultipartFileSender.renderFile(reportFile, reportFileName, ReportFormatEnum.XML.name(), dynamicReportService.getContentType(ReportFormatEnum.XML.name()), request, response, false)
        } catch (Exception ex) {
            flash.error = message(code: "default.server.error.message")
            log.warn("IOException occurred in downloadBatchXML while rendering file ${reportFile.name}. Error: ${ex.getMessage()}")
        }
    }

    def downloadBulkXML(ReportResult reportResult) {
        List<String> caseNumbers = params.getList("caseNumber[]")
        IcsrReportSpecEnum reportSpec = IcsrReportSpecEnum.valueOf(params.reportSpec)
        String reportFileName = "ICSR Bulk Report.zip"
        File reportFile = icsrReportService.createBulkXMLReport(reportResult, caseNumbers, reportSpec)
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
        try {
            MultipartFileSender.renderFile(reportFile, reportFileName, ReportFormatEnum.ZIP.name(), dynamicReportService.getContentType(ReportFormatEnum.ZIP.name()), request, response, false)
        } catch (IOException ex){
            flash.error = message(code: "default.server.error.message")
            log.warn("IOException occurred in downloadBulkXML while rendering file ${reportFile.name} . Error: ${ex.getMessage()}")
        }
    }

    def transferCases(Long id, String caseNumbersWithVersion, Long profile, Integer dueInDays, Boolean isExpedited) {
        ReportResult reportResult = ReportResult.read(id)
        IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profile)
        if ( !profileConfiguration ||!reportResult || !reportResult.resultDataSize()) {
            flash.error = message(code: "icsr.report.case.transfer.no.proper.source.data.error")
            redirect(action: 'show', id: reportResult?.id)
            return
        }
        Long sourceTemplateId = reportResult?.executedTemplateQuery?.usedTemplate?.originalTemplateId
        TemplateQuery templateQuery = profileConfiguration?.templateQueries?.find {
            it.templateId == sourceTemplateId
        }
        if (!templateQuery) {
            flash.error = message(code: "icsr.report.case.transfer.no.proper.target.data.error")
            redirect(action: 'show', id: reportResult?.id)
            return
        }
        Map<String, Integer> caseNumberMap = caseNumbersWithVersion.split(',').collectEntries {
            [it.split(':').first(), it.split(':').last().toInteger()]
        }
        try {
            Map result = icsrReportService.transferCases(reportResult, profileConfiguration, caseNumberMap, dueInDays, isExpedited)
            flash.message = "Transferred cases : ${result.success?.join(',') ?: ''} \n Not found cases data: ${result.notfound?.join(',') ?: ''}"
        } catch (Exception ex) {
            flash.error = message(code: "icsr.report.case.transfer.error")
            log.error("Failed while transferring cases", ex)
        }
        redirect(action: 'show', id: reportResult.id)
    }

    def favorite() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        ReportConfiguration configuration = params.id ? ReportConfiguration.get(params.id) : null
        if (!configuration) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id]) as String)
        } else {
            try {
                configurationService.setFavorite(configuration, params.boolean("state"))
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    private IcsrReportConfiguration setAttributeTags(IcsrReportConfiguration configuration) {

        if (params?.tags) {
            if (configuration?.tags) {
                configuration?.tags?.each {
                    configuration.removeFromTags(it)
                }
            }
            if (params.tags.class == String) {
                params.tags = [params.tags]
            }
            List updatedTags = params.tags
            updatedTags.unique().each {
                Tag tag = Tag.findByName(it)
                if (!tag) {
                    tag = new Tag(name: it).save()
                }
                configuration.addToTags(tag)
            }
        }
        return configuration
    }

    private void setReportingDestinations(IcsrReportConfiguration periodicReportConfiguration) {
        if (params.reportingDestinations) {
            params.reportingDestinations.toString().split(Constants.MULTIPLE_AJAX_SEPARATOR).each {
                if (it != periodicReportConfiguration.primaryReportingDestination) {
                    periodicReportConfiguration.addToReportingDestinations(it)
                }
            }
        }
    }

    private void setNextRunDateAndScheduleDateJSON(IcsrReportConfiguration configurationInstance) {
        configurationInstance.nextRunDate = null
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            if (MiscUtil.validateScheduleDateJSON(configurationInstance.scheduleDateJSON)) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
                return
            }
        }
        configurationInstance.nextRunDate = null
    }

    private clearListFromConfiguration(IcsrReportConfiguration configurationInstance) {
        configurationInstance?.deliveryOption?.emailToUsers?.clear()
        configurationInstance?.deliveryOption?.attachmentFormats?.clear()
        configurationInstance?.tags?.clear()
        configurationInstance?.reportingDestinations?.clear()
        configurationInstance?.poiInputsParameterValues?.clear()
        return configurationInstance
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private populateModel(IcsrReportConfiguration configurationInstance) {
        String executableBy = params.get('executableBy')
        List<String> executableUserList = executableBy ? Arrays.asList(executableBy.split(";")) : []
        //Do not bind in any other way because of the clone contained in the params
        clearListFromConfiguration(configurationInstance)

        /* List<IcsrPartnerProfile> partnerProfiles
         partnerProfiles = IcsrPartnerProfile.findAllByPartnerProfileName(String.valueOf(params.primaryReportingDestination))
         log.info("partnerProfiles " + partnerProfiles)
         log.info("partnerProfiles.get(0).associatedTempltId " + partnerProfiles.get(0).associatedTempltId)*/
//        params.put("templateQueries[0].template",partnerProfiles.get(0).associatedTempltId)
        bindData(configurationInstance, params, [exclude: ["templateQueries", "tags", "executableBy", "reportingDestinations", "isEnabled", "asOfVersionDate", "includeLockedVersion", "globalQueryValueLists", "emailConfiguration", "sharedWith", "dmsConfiguration"]])
        bindAsOfVersionDate(configurationInstance, params.asOfVersionDate)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        setAttributeTags(configurationInstance)
        setReportingDestinations(configurationInstance)
        if (params.globalDateRangeInformation) {
            assignParameterValuesToGlobalQuery(configurationInstance)
        }
        bindExistingTemplateQueryEdits(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        configurationService.removeRemovedTemplateQueries(configurationInstance)
        if (params.emailConfiguration) {
            bindEmailConfiguration(configurationInstance, params.emailConfiguration)
        }
        configurationService.bindSharedWith(configurationInstance, params.list('sharedWith'), executableUserList, configurationInstance.id ? true : false)
        bindDmsConfiguration(configurationInstance, params.dmsConfiguration)
        bindReportTasks(configurationInstance, params)
        configurationService.checkProductCheckboxes(configurationInstance)
        if (configurationInstance.includeWHODrugs) {
            configurationInstance.isMultiIngredient = true
        }
    }

    private bindReportTasks(IcsrReportConfiguration configurationInstance, params) {

        Set<ReportTask> reportTaskList = configurationInstance.reportTasks?.grep()
        reportTaskList.each {
            configurationInstance.removeFromReportTasks(it)
            //TODO:this should be done using CRUDService
            it.delete()
        }
        List<ReportTask> taskList = taskTemplateService.fetchReportTasksFromRequest(params)
        taskList.each {
            configurationInstance.addToReportTasks(it)
        }
    }

    private bindAsOfVersionDate(IcsrReportConfiguration configuration, def asOfDate) {
        if (!(configuration?.evaluateDateAs in [EvaluateCaseDateEnum.LATEST_VERSION, EvaluateCaseDateEnum.ALL_VERSIONS])) {
            configuration.includeLockedVersion = true
        } else {
            configuration.includeLockedVersion = params.includeLockedVersion ?: false
        }
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            configuration.asOfVersionDate = DateUtil.getEndDate(asOfDate, userService.currentUser?.preference?.locale)
        } else {
            configuration.asOfVersionDate = null
        }
    }

    private bindNewTemplateQueries(IcsrReportConfiguration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = configurationInstance.templateQueries.size(); params.containsKey("templateQueries[" + i + "].id"); i++) {
            if (params.get("templateQueries[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                IcsrTemplateQuery templateQueryInstance = new IcsrTemplateQuery(bindingMap)

                templateQueryInstance = (TemplateQuery) userService.setOwnershipAndModifier(templateQueryInstance)
                //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
                DateRangeInformation dateRangeInformationForTemplateQuery = templateQueryInstance.dateRangeInformationForTemplateQuery
                setDateRangeInformation(i, dateRangeInformationForTemplateQuery, configurationInstance)
                dateRangeInformationForTemplateQuery.templateQuery = templateQueryInstance
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = params.int("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")
                assignParameterValuesToTemplateQuery(configurationInstance, templateQueryInstance, i)
                configurationInstance.addToTemplateQueries(templateQueryInstance)
            }

        }
    }

    private bindTemplatePOIInputs(IcsrReportConfiguration configurationInstance) {
        for (int i = 0; params.containsKey("poiInput[" + i + "].key"); i++) {
            String key = params.("poiInput[" + i + "].key")
            String value = params.("poiInput[" + i + "].value")
            if (!configurationInstance.poiInputsParameterValues*.key?.contains(key) && value) {
                configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: key, value: value))
            }
        }
    }

    private bindExistingTemplateQueryEdits(IcsrReportConfiguration configurationInstance) {
        //handle edits to the existing Template Queries
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            templateQuery = (IcsrTemplateQuery) userService.setOwnershipAndModifier(templateQuery)
            //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery
            setDateRangeInformation(i, dateRangeInformationForTemplateQuery, configurationInstance)
            dateRangeInformationForTemplateQuery.templateQuery = templateQuery
            if (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") && params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") =~ "-?\\d+") {
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer
            }
            assignParameterValuesToTemplateQuery(configurationInstance, templateQuery, i)
        }
        configurationInstance
    }

    private void setDateRangeInformation(int i, DateRangeInformation dateRangeInformationForTemplateQuery, IcsrReportConfiguration configurationInstance) {
        def dateRangeEnum = params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")
        if (dateRangeEnum) {
            dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
            if (dateRangeEnum == DateRangeEnum.CUSTOM.name()) {
                dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
                Locale locale = userService.currentUser?.preference?.locale
                dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), locale)
                dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), locale)
            } else {
                dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute = null
                dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute = null
            }
        }
    }

    private void assignParameterValuesToTemplateQuery(IcsrReportConfiguration configurationInstance, TemplateQuery templateQuery, int i) {
        if (templateQuery.queryValueLists) {
            params.put("oldQueryValueList${templateQuery.id}", templateQuery.queryValueLists.toString())
        }
        if (templateQuery.templateValueLists) {
            params.put("oldTemplateValueList${templateQuery.id}", templateQuery.templateValueLists.toString())
        }
        templateQuery.queryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        templateQuery.queryValueLists?.clear()
        templateQuery.templateValueLists?.each {
            it.parameterValues?.each {
                CustomSQLValue.get(it.id)?.delete() // CustomSQLTemplateValue?
            }
            it.parameterValues?.clear()
        }
        templateQuery.templateValueLists?.clear()

        if (params.containsKey("templateQuery" + i + ".qev[0].key")) {

            // for each single query
            int start = 0
            params.("templateQueries[" + i + "].validQueries").split(",").each { queryId -> // if query set
                QueryValueList queryValueList = new QueryValueList(query: queryId)

                int size = SuperQuery.get(queryId).getParameterSize()

                // if query set, iterate each query in query set
                for (int j = start; params.containsKey("templateQuery" + i + ".qev[" + j + "].key") && j < (start + size); j++) {
                    ParameterValue tempValue
                    String key = params.("templateQuery" + i + ".qev[" + j + "].key")
                    String value = params.("templateQuery" + i + ".qev[" + j + "].value")
                    String specialKeyValue = params.("templateQuery" + i + ".qev[" + j + "].specialKeyValue")
                    boolean isFromCopyPaste = false
                    if (params.("templateQuery" + i + ".qev[" + j + "].copyPasteValue")) {
                        value = params.("templateQuery" + i + ".qev[" + j + "].copyPasteValue")
                    }
                    if (params.("templateQuery" + i + ".qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("templateQuery" + i + ".qev[" + j + "].field"), false)
                    if (specialKeyValue) {
                        if (!configurationInstance.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: specialKeyValue, value: value, isFromCopyPaste: isFromCopyPaste))
                        } else if (configurationInstance.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            ParameterValue parameterValue = configurationInstance.poiInputsParameterValues.find {
                                it.key == specialKeyValue
                            }
                            value = parameterValue?.value
                            isFromCopyPaste = parameterValue?.isFromCopyPaste
                        }
                    }

                    if (params.containsKey("templateQuery" + i + ".qev[" + j + "].field")) {
                        tempValue = new QueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("templateQuery" + i + ".qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new CustomSQLValue(key: key, value: value)
                    }

                    queryValueList.addToParameterValues(tempValue)
                }

                start += size
                templateQuery.addToQueryValueLists(queryValueList)
            }
        }

        if (params.containsKey("templateQuery" + i + ".tv[0].key")) {
            TemplateValueList templateValueList = new TemplateValueList(template: params.("templateQueries[" + i + "].template"))

            for (int j = 0; params.containsKey("templateQuery" + i + ".tv[" + j + "].key"); j++) {
                ParameterValue tempValue
                tempValue = new CustomSQLValue(key: params.("templateQuery" + i + ".tv[" + j + "].key"),
                        value: params.("templateQuery" + i + ".tv[" + j + "].value"))
                templateValueList.addToParameterValues(tempValue)
            }
            templateQuery.addToTemplateValueLists(templateValueList)
        }
    }

    private void assignParameterValuesToTemplateQuery(ExecutedTemplateQuery executedTemplateQuery) {
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.executedConfiguration
        executedTemplateQuery.executedQueryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        executedTemplateQuery.executedQueryValueLists = []
        executedTemplateQuery.executedTemplateValueLists?.each {
            it.parameterValues?.each {
                CustomSQLValue.get(it.id)?.delete() // CustomSQLTemplateValue?
            }
            it.parameterValues?.clear()
        }
        executedTemplateQuery.executedTemplateValueLists?.clear()

        if (params.containsKey("qev[0].key")) {

            // for each single query
            int start = 0
            params.("validQueries").split(",").each { queryId -> // if query set
                ExecutedQueryValueList executedQueryValueList = new ExecutedQueryValueList(query: queryId)

                int size = SuperQuery.get(queryId).getParameterSize()

                // if query set, iterate each query in query set
                for (int j = start; params.containsKey("qev[" + j + "].key") && j < (start + size); j++) {
                    ParameterValue tempValue
                    String key = params.("qev[" + j + "].key")
                    String value = params.("qev[" + j + "].value")
                    String specialKeyValue = params.("qev[" + j + "].specialKeyValue")
                    boolean isFromCopyPaste = false
                    if (params.("qev[" + j + "].copyPasteValue")) {
                        value = params.("qev[" + j + "].copyPasteValue")
                    }
                    if (params.("qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("qev[" + j + "].field"), false)
                    if (specialKeyValue) {
                        if (!executedReportConfiguration.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            executedReportConfiguration.addToPoiInputsParameterValues(new ParameterValue(key: specialKeyValue, value: value, isFromCopyPaste: isFromCopyPaste))
                        } else if (executedReportConfiguration.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            ParameterValue parameterValue = executedReportConfiguration.poiInputsParameterValues.find {
                                it.key == specialKeyValue
                            }
                            value = parameterValue?.value
                            isFromCopyPaste = parameterValue?.isFromCopyPaste
                        }
                    }

                    if (params.containsKey("qev[" + j + "].field")) {
                        tempValue = new QueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new CustomSQLValue(key: key, value: value)
                    }

                    executedQueryValueList.addToParameterValues(tempValue)
                }

                start += size
                executedTemplateQuery.addToExecutedQueryValueLists(executedQueryValueList)
            }
        }

        if (params.containsKey("tv[0].key")) {
            ExecutedTemplateValueList executedTemplateValueList = new ExecutedTemplateValueList(template: params.("template"))

            for (int j = 0; params.containsKey("tv[" + j + "].key"); j++) {
                ParameterValue tempValue
                tempValue = new CustomSQLValue(key: params.("tv[" + j + "].key"),
                        value: params.("tv[" + j + "].value"))
                executedTemplateValueList.addToParameterValues(tempValue)
            }
            executedTemplateQuery.addToExecutedTemplateValueLists(executedTemplateValueList)
        }
    }

    private void setExecutedDateRangeInformation(ExecutedTemplateQuery executedTemplateQuery) {
        ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery = executedTemplateQuery.executedDateRangeInformationForTemplateQuery
        DateRangeEnum dateRangeEnum = executedDateRangeInformationForTemplateQuery.dateRangeEnum
        executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate = executedTemplateQuery.executedConfiguration.executedTemplateQueries.first().executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate
        if (dateRangeEnum) {
            executedDateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
            if (dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
                executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute = executedTemplateQuery.executedConfiguration.reportMinMaxDate[1] ?: executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate ?: new Date()
            } else {
                executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute = executedTemplateQuery.executedConfiguration?.executedGlobalDateRangeInformation?.dateRangeStartAbsolute
                executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute = executedTemplateQuery.executedConfiguration?.executedGlobalDateRangeInformation?.dateRangeEndAbsolute
            }
        }
        executedDateRangeInformationForTemplateQuery.executedTemplateQuery = executedTemplateQuery
    }

    private void assignParameterValuesToGlobalQuery(IcsrReportConfiguration icsrReportConfiguration) {
        GlobalDateRangeInformation globalDateRangeInformation = icsrReportConfiguration.globalDateRangeInformation
        if (!globalDateRangeInformation) {
            globalDateRangeInformation = new GlobalDateRangeInformation()
            icsrReportConfiguration.globalDateRangeInformation = globalDateRangeInformation
        }
        bindData(globalDateRangeInformation, params.globalDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        configurationService.fixBindDateRange(globalDateRangeInformation, icsrReportConfiguration, params)
        configurationService.bindParameterValuesToGlobalQuery(icsrReportConfiguration, params)
    }

    private bindEmailConfiguration(IcsrReportConfiguration configurationInstance, Map emailConfiguration) {
        if (emailConfiguration.subject && emailConfiguration.body) {
            EmailConfiguration emailConfigurationInstance
            if (configurationInstance.emailConfiguration) {
                emailConfigurationInstance = configurationInstance.emailConfiguration
                emailConfigurationInstance.isDeleted = false
                bindData(emailConfigurationInstance, emailConfiguration)
            } else {
                emailConfigurationInstance = new EmailConfiguration(emailConfiguration)
                emailConfigurationInstance.save()
                configurationInstance.emailConfiguration = emailConfigurationInstance
            }
        } else {
            configurationInstance.emailConfiguration=null
            if (configurationInstance.emailConfigurationId) {
                CRUDService.delete(configurationInstance.emailConfiguration)
            }
        }
    }

    private bindDmsConfiguration(IcsrReportConfiguration configurationInstance, Map dmsConfiguration) {
        if (dmsConfiguration && dmsConfiguration.format) {
            DmsConfiguration dmsConfigurationInstance
            if (configurationInstance.getId() && !configurationInstance.isAttached()) {
                configurationInstance.attach()
            }
            if (configurationInstance.dmsConfiguration) {
                dmsConfigurationInstance = configurationInstance.dmsConfiguration
                dmsConfigurationInstance.isDeleted = false
                bindData(dmsConfigurationInstance, dmsConfiguration)
                //TODO:this should be done using CRUDService
                dmsConfigurationInstance.save()
            } else {
                dmsConfigurationInstance = new DmsConfiguration(dmsConfiguration)
                CRUDService.save(dmsConfigurationInstance)
                configurationInstance.dmsConfiguration = dmsConfigurationInstance
            }
        } else {

        }
    }


    private getBindingMap(int i) {
        def bindingMap = [
                template                  : params.("templateQueries[" + i + "].template"),
                query                     : params.("templateQueries[" + i + "].query"),
                operator                  : params.("templateQueries[" + i + "].operator"),
                queryLevel                : params.("templateQueries[" + i + "].queryLevel"),
                dynamicFormEntryDeleted   : params.("templateQueries[" + i + "].dynamicFormEntryDeleted") ?: false,
                header                    : params.("templateQueries[" + i + "].header") ?: null,
                footer                    : params.("templateQueries[" + i + "].footer") ?: null,
                title                     : params.("templateQueries[" + i + "].title") ?: null,
                draftOnly                 : params.("templateQueries[" + i + "].draftOnly") ?: false,
                headerProductSelection    : params.("templateQueries[" + i + "].headerProductSelection") ?: false,
                headerDateRange           : params.("templateQueries[" + i + "].headerDateRange") ?: false,
                blindProtected            : params.("templateQueries[" + i + "].blindProtected") ?: false,
                privacyProtected          : params.("templateQueries[" + i + "].privacyProtected") ?: false,
                displayMedDraVersionNumber: params.("templateQueries[" + i + "].displayMedDraVersionNumber") ?: false,
                icsrMsgType    : params.("templateQueries[" + i + "].msgType"),
                granularity: params.("templateQueries[" + i + "].granularity")
        ]
        bindingMap
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
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

    private void saveConfigurationToSession() {
        Map editingConfigurationMap = [configurationParams: (params as JSON).toString(), configurationId: params.id, action: params.id ? "edit" : "create", controller: "icsrReport", templateQueryIndex: params.templateQueryIndex]
        session.setAttribute("editingConfiguration", editingConfigurationMap)
    }

    private void initConfigurationFromMap(IcsrReportConfiguration configurationInstance, Map map) {
        params.putAll(map)
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        populateModel(configurationInstance)
        configurationService.initConfigurationTemplatesFromSession(session, configurationInstance)
        configurationService.initConfigurationQueriesFromSession(session, configurationInstance)
        session.removeAttribute("editingConfiguration")
    }

    private User getCurrentUser() {
        return userService.getUser()
    }

    private boolean checkIfReportExceedsHtmlLimit(Long reportRows = 0) {
        return (reportRows > getReportHtmlLimitFromConfig())
    }

    private Long getReportHtmlLimitFromConfig() {
        return grailsApplication.config.pvreports.show.max.html
    }

    private validateTenant(Long tenantId){
        if(tenantId && (tenantId != (Tenants.currentId() as Long)) && !SpringSecurityUtils.ifAnyGranted("ROLE_DEV")){
            log.error("Request and Session tenant mismatch issue for User ${currentUser?.username} in IcsrReportController")
            return false
        }
        return true
    }

}
