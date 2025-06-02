package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class PeriodicReportController {

    def configurationService
    def userService
    def CRUDService
    def reportExecutorService
    def periodicReportService
    def dmsService
    def taskTemplateService
    def qualityService
    def publisherSourceService

    static allowedMethods = [delete: ['DELETE','POST', 'GET']]

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def index() {
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def reports() {
    }

    def targetStatesAndApplications() {
        def intialState = params.initialState;
        def executedReportConfiguration = params.executedReportConfiguration
        render periodicReportService.targetStatesAndApplications(executedReportConfiguration, intialState) as JSON
    }

    def updateReportState(Long executedConfigId) {
        try {
            ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = ExecutedPeriodicReportConfiguration.createCriteria().get {
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
            render([message: "Server Error"]) as JSON
            return
        }

    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def create() {
        PeriodicReportConfiguration configurationInstance = new PeriodicReportConfiguration()
        Map fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        Long requestId
        if (fromSession.configurationParams) {
            requestId = session.editingConfiguration?.requestId as Long
            initConfigurationFromMap(configurationInstance, fromSession.configurationParams)
        }
        Map templateQueryIndex = fromSession.templateQueryIndex
        boolean templateBlanks = false
        if (params.selectedTemplate) {
            ReportTemplate template = ReportTemplate.get(params.selectedTemplate)
            if (template) {
                TemplateQuery templateQuery = new TemplateQuery(template: template)
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
                TemplateQuery templateQuery = new TemplateQuery(query: query)
                configurationInstance.addToTemplateQueries(templateQuery)
                if (query.hasBlanks) {
                    queryBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.query.notFound', args: [params.selectedQuery])
            }
        }
        render(view: "create", model: [requestId: requestId, configurationInstance: configurationInstance, queryBlanks: queryBlanks, templateBlanks: templateBlanks, templateQueryIndex: templateQueryIndex, hasConfigTemplateCreatorRole: getCurrentUser().isConfigurationTemplateCreator()])
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

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    @Transactional
    def updatePeriodicAjaxCall() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        PeriodicReportConfiguration configurationInstance = PeriodicReportConfiguration.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        if(!validateTenant(params.long('tenantId'))){
            responseDTO.setFailureResponse(message(code: 'invalid.tenant').toString() as String)
        }else {
            populateModel(configurationInstance)
            try {
                if (!userService.isAnyGranted("ROLE_QUALITY_CHECK")) {
                    configurationInstance.qualityChecked = false
                }
                configurationInstance = (PeriodicReportConfiguration) CRUDService.update(configurationInstance)
            } catch (ValidationException ve) {
                responseDTO.setFailureResponse(ve.errors)
            } catch (Exception ex) {
                responseDTO.setFailureResponse(message(code: "default.server.error.message").toString())
            }
        }
        return render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    @Transactional
    def ajaxPeriodicSaveAsAndRun(){
        if (request.method == 'GET') {
            notSaved()
            return
        }
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        if(!validateTenant(params.long('tenantId'))){
            responseDTO.setFailureResponse(message(code: 'invalid.tenant').toString() as String)
            return render(responseDTO.toAjaxResponse())
        }
        PeriodicReportConfiguration configurationInstance = new PeriodicReportConfiguration()
        configurationInstance.setIsEnabled(false)
        populateModelForEditedConfig(configurationInstance, getCurrentUser())
        if(!configurationInstance.getReportName()) {
            responseDTO.setFailureResponse(message(code: 'com.rxlogix.config.Configuration.reportName.nullable').toString() as String)
        }else if(!configurationService.isUniqueName(configurationInstance.getReportName(),getCurrentUser())) {
            responseDTO.setFailureResponse(message(code: 'com.rxlogix.config.configuration.name.unique.per.user').toString() as String)
        }else {
            configurationInstance.setReportName(configurationInstance.getReportName())
            try {
                configurationInstance = (PeriodicReportConfiguration) CRUDService.save(configurationInstance)
            } catch (ValidationException ve) {
                responseDTO.setFailureResponse(ve.errors)
            }
        }
        return render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def edit(Long id) {
        PeriodicReportConfiguration configurationInstance = id ? PeriodicReportConfiguration.read(id) : null
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
            return
        }
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
            configurationInstance.scheduleDateJSON = configurationService.correctSchedulerJSONForCurrentDate(configurationInstance.scheduleDateJSON, configurationInstance.nextRunDate)
            render(view: "edit", model: [configurationInstance : configurationInstance, templateQueryIndex: templateQueryIndex,
                                         configSelectedTimeZone: params?.configSelectedTimeZone, hasConfigTemplateCreatorRole: currentUser.isConfigurationTemplateCreator()])
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    public disable() {
        PeriodicReportConfiguration periodicReportConfigurationInstance = PeriodicReportConfiguration.get(params.long('id'))
        if (!periodicReportConfigurationInstance) {
            notFound()
            return
        }
        if (!periodicReportConfigurationInstance.isEditableBy(currentUser)) {
            flash.warn = message(code:  "app.warn.noPermission")
            redirect(view: "index")
            return
        }

        if (!periodicReportConfigurationInstance.executing) {
            periodicReportConfigurationInstance.setIsEnabled(false)
//        populateModel(periodicReportConfigurationInstance) Disabled Updating data while unscheduling

            try {
                periodicReportConfigurationInstance = (PeriodicReportConfiguration) CRUDService.update(periodicReportConfigurationInstance)
            } catch (ValidationException ve) {
                PeriodicReportConfiguration originalConfiguration = PeriodicReportConfiguration.read(periodicReportConfigurationInstance.id)
                populateModel(originalConfiguration)
                originalConfiguration.errors = ve.errors
                render view: "edit", model: [configurationInstance: originalConfiguration, configSelectedTimeZone: originalConfiguration.configSelectedTimeZone]
                return
            }
        } else {
            flash.error = message(code:'app.configuration.unscheduled.fail', args: [periodicReportConfigurationInstance.reportName])
            render view: 'index'
            return
        }
        flash.message = message(code: 'default.disabled.message', args: [message(code: 'configuration.label'), periodicReportConfigurationInstance.reportName])
        redirect(action: "view", id: periodicReportConfigurationInstance.id)
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def view(Long id) {
        PeriodicReportConfiguration periodicReportConfiguration = PeriodicReportConfiguration.read(id)
        if (!periodicReportConfiguration) {
            notFound()
            return
        }
        String configurationJson = null
        if (params.viewConfigJSON && SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            configurationJson = configurationService.getConfigurationAsJSON(periodicReportConfiguration)
        }
        render(view: "view", model: [templateQueries: periodicReportConfiguration.templateQueries, configurationInstance: periodicReportConfiguration,
                                     isExecuted     : false, deliveryOption: periodicReportConfiguration.deliveryOption,
                                     viewSql        : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(periodicReportConfiguration) : null, viewGlobalSql: params.getBoolean("viewSql") ? reportExecutorService.debugGlobalQuerySQL(periodicReportConfiguration) : null, configurationJson: configurationJson])
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def viewExecutedConfig(ExecutedPeriodicReportConfiguration executedConfiguration) {
        if (!executedConfiguration || executedConfiguration.isDeleted) {
            notFound()
            return
        }

        ExecutionStatus executionStatus = ExecutionStatus.getExecutionStatusByExectutedEntity(executedConfiguration.id,ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION).list()[0]
        PeriodicReportConfiguration reportConfiguration = PeriodicReportConfiguration.get(executionStatus?.entityId) ?:
                PeriodicReportConfiguration.findByReportNameAndOwner(executedConfiguration.reportName, executedConfiguration.owner)
        render(view: "view", model: [templateQueries: executedConfiguration.executedTemplateQueries, configurationInstance: executedConfiguration,
                                     isExecuted     : true, deliveryOption: executedConfiguration.executedDeliveryOption, periodicReportConfigurationInstanceId: reportConfiguration?.id])
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def queryDataTemplate() {
        def result = []
        if (params.queryId) {
            Query.get(params.queryId)?.queryExpressionValues?.each {
                result += [value: it.value, operator: it.operator.name(),
                           field: it.reportField.name, key: it.key]
            }
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
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
        PeriodicReportConfiguration configurationInstance = new PeriodicReportConfiguration()
        configurationInstance.setIsEnabled(false)
        populateModel(configurationInstance)

        try {
            configurationInstance = (PeriodicReportConfiguration) CRUDService.save(configurationInstance)
            if (params.requestId) {
                ReportRequest reportRequest = ReportRequest.get(params.long("requestId"))
                def link = JSON.parse(reportRequest.linkedConfigurations ?: "[]")
                link.add([id: configurationInstance.id, name: configurationInstance.reportName])
                reportRequest.linkedConfigurations = (link as JSON).toString()
                reportRequest.save(flush: true, failOnError: true)
            }
        } catch (ValidationException ve) {
            boolean showAttachmentWarning = false
            boolean showSectionAttachmentWarning = false
            if (configurationInstance.attachments) {
                configurationInstance.attachments.each {
                    if (!it.id && (it.fileSource == PublisherSource.Source.FILE)) it.path = ""
                }
                showAttachmentWarning = true
            }
            if (configurationInstance.publisherConfigurationSections) {
                configurationInstance.publisherConfigurationSections.each {
                    if (!it.id && it.filename) it.filename = ""
                }
                showSectionAttachmentWarning = true
            }
            configurationInstance.errors = ve.errors
            render view: "create", model: [configurationInstance : configurationInstance, showAttachmentWarning: showAttachmentWarning,
                                           showSectionAttachmentWarning: showSectionAttachmentWarning, configSelectedTimeZone: params?.configSelectedTimeZone]
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
        redirect(action: "view", id: configurationInstance.id)
    }


    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        PeriodicReportConfiguration configurationInstance = PeriodicReportConfiguration.lock(params.id)
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
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                configurationInstance.qualityChecked = false
            }
            CRUDService.update(configurationInstance)
            flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
            if(params.reportId) {
                redirect(controller: "report", action: "showFirstSection", id: params.reportId)
            } else {
                redirect(action: "view", id: configurationInstance.id)
            }
        } catch (ValidationException ve) {
            boolean showAttachmentWarning = false
            boolean showSectionAttachmentWarning = false
            if (configurationInstance.attachments) {
                configurationInstance.attachments.each {
                    if (!it.id && (it.fileSource == PublisherSource.Source.FILE)) {
                        it.path = ""
                        showAttachmentWarning = true
                    }
                    if (it.id && (it.fileSource == PublisherSource.Source.FILE)) {
                        PublisherSource source = PublisherSource.findByIdAndPath(it.id, it.path)
                        if (!source) {
                            it.path = ""
                            showAttachmentWarning = true
                        }
                    }
                }
            }
            if (configurationInstance.publisherConfigurationSections) {
                configurationInstance.publisherConfigurationSections.each {
                    if (!it.id && it.filename ) {
                        it.filename = ""
                        showSectionAttachmentWarning = true
                    }
                    if (it.id && it.filename) {
                        PublisherConfigurationSection section = PublisherConfigurationSection.findByIdAndFilename(it.id, it.filename)
                        if (!section) {
                            it.filename = ""
                            showSectionAttachmentWarning = true
                        }
                    }
                }
            }
            configurationInstance.errors = ve.errors
            render view: "edit", model: [configurationInstance : configurationInstance, showAttachmentWarning: showAttachmentWarning,allPublisherContributors: configurationInstance.allPublisherContributors,
                                         showSectionAttachmentWarning: showSectionAttachmentWarning, configSelectedTimeZone: params.configSelectedTimeZone]
            return
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    @Transactional
    def updatePublisherAttachment() {

        if (params.executedConfigId && params.long("executedConfigId") == 0) {
            def sources = ["attachments": ExecutedPublisherSource.findAllByConfigurationIsNull()]
            try {
                bindAttachments(sources)
                redirect(action: "sources")
            } catch (ValidationException ve) {
                render(view: "sources", model: [attachments                    : sources.attachments,
                                                attachmentesUpdatedErrorMessage: g.renderErrors(bean: ve, as: "list", codec: "HTML")])

            }
        } else {
            ExecutedPeriodicReportConfiguration configurationInstance = ExecutedPeriodicReportConfiguration.lock(params.executedConfigId)
            if (!configurationInstance) {
                notFound()
                return
            }
            bindAttachments(configurationInstance)
            try {
                CRUDService.update(configurationInstance)
                redirect(controller: "pvp", action: "sections", params: [id: configurationInstance.id, attachmentesUpdated: true])
            } catch (ValidationException ve) {
                configurationInstance.errors = ve.errors
                redirect(controller: "pvp", action: "sections", params: [id                             : configurationInstance.id,
                                                                         attachmentesUpdatedErrorMessage: g.renderErrors(bean: ve, as: "list", codec: "HTML")])

            }
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def copy(PeriodicReportConfiguration originalConfig) {
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

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def delete(PeriodicReportConfiguration configurationInstance) {

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
            if (request.getMethod() == "GET") { //from dashboard widget
                redirect controller: "dashboard", action: "index", method: "GET"
            } else {
                request.withFormat {
                    form {
                        flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), configurationInstance.reportName])
                        redirect action: "index", method: "GET"
                    }
                    '*' { render status: NO_CONTENT }
                }
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

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    @Transactional
    def run(Long id) {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        PeriodicReportConfiguration periodicReportConfigurationInstance = PeriodicReportConfiguration.get(id)
        if (!periodicReportConfigurationInstance) {
            periodicReportConfigurationInstance = new PeriodicReportConfiguration()
            if(!validateTenant(params.long('tenantId'))){
                flash.error = message(code: "invalid.tenant")
                redirect(action: 'create')
                return
            }
        } else if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: periodicReportConfigurationInstance.id)
            return
        }
        periodicReportConfigurationInstance.setIsEnabled(true)
        EvaluateCaseDateEnum evaluateDateAs = periodicReportConfigurationInstance.evaluateDateAs ?: EvaluateCaseDateEnum.LATEST_VERSION
        populateModel(periodicReportConfigurationInstance)
        if (params.fromTemplate) {
            periodicReportConfigurationInstance.evaluateDateAs = evaluateDateAs
        }
        try {
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                periodicReportConfigurationInstance.qualityChecked = false
            }
            if (periodicReportConfigurationInstance.id) {
                periodicReportConfigurationInstance = (PeriodicReportConfiguration) CRUDService.update(periodicReportConfigurationInstance)
            } else {
                periodicReportConfigurationInstance = (PeriodicReportConfiguration) CRUDService.save(periodicReportConfigurationInstance)
                if (params.requestId) {
                    ReportRequest reportRequest = ReportRequest.get(params.long("requestId"))
                    def link = JSON.parse(reportRequest.linkedConfigurations ?: "[]")
                    link.add([id: periodicReportConfigurationInstance.id, name: periodicReportConfigurationInstance.reportName])
                    reportRequest.linkedConfigurations = (link as JSON).toString()
                    reportRequest.save()
                }
            }
            flash.message = message(code: 'app.Configuration.RunningMessage')
            if(params.reportId) {
                redirect(controller: "report", action: "showFirstSection", id: params.reportId)
            } else {
                redirect(controller: "executionStatus", action: "list")
            }
        } catch (ValidationException ve) {
            periodicReportConfigurationInstance.errors = ve.errors
            periodicReportConfigurationInstance.setIsEnabled(false)
            if (id) {
                render view: "edit", model: [configurationInstance : periodicReportConfigurationInstance,
                                             configSelectedTimeZone: params?.configSelectedTimeZone, allPublisherContributors: periodicReportConfigurationInstance.allPublisherContributors]
            } else {
                render view: "create", model: [configurationInstance : periodicReportConfigurationInstance,
                                               configSelectedTimeZone: params?.configSelectedTimeZone]
            }
            return
        }
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def runOnce(PeriodicReportConfiguration periodicReportConfigurationInstance) {
        if (!periodicReportConfigurationInstance) {
            notFound()
            return
        }
        if (periodicReportConfigurationInstance.nextRunDate && periodicReportConfigurationInstance.isEnabled) {
            flash.warn = message(code: 'app.configuration.run.exists')
            redirect(action: "index")
            return
        }
        if (periodicReportConfigurationInstance?.isTemplate && periodicReportConfigurationInstance.hasAuthorityToRunAsTemplateReport(currentUser)) {
            flash.warn = message(code: "com.rxlogix.config.ReportConfiguration.create.template.forbidden")
            redirect(action: "index")
            return
        }
        try {
            periodicReportConfigurationInstance.isPriorityReport = params.boolean('isPriorityReport')
            periodicReportService.scheduleToRunOnce(periodicReportConfigurationInstance)
        } catch (ValidationException ve) {
            periodicReportConfigurationInstance.errors = ve.errors
            periodicReportConfigurationInstance.setIsEnabled(false)
            render view: "create", model: [configurationInstance : periodicReportConfigurationInstance,
                                           configSelectedTimeZone: params?.configSelectedTimeZone]
            return
        }

        if(periodicReportConfigurationInstance.isPriorityReport){
            flash.message = message(code: 'app.Configuration.PriorityRunningMessage')
        }else{
            flash.message = message(code: 'app.Configuration.RunningMessage')
        }
        redirect(controller: 'executionStatus', action: "list", model: [periodicReportConfigurationInstance: periodicReportConfigurationInstance, status: CREATED])
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def addSection(Long id) {
        ExecutedPeriodicReportConfiguration executedConfiguration = ExecutedPeriodicReportConfiguration.read(id)
        if (!executedConfiguration) {
            log.error("Requested Entity not found : ${params.id}")
            render "Not Found"
            return
        }
        render template: "includes/addSectionForm", model: [executedConfiguration: executedConfiguration]
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def saveOnDemandSection(ExecutedTemplateQuery executedTemplateQuery) {
        try {
            executedTemplateQuery.manuallyAdded = true
            boolean isExecuteRptFromCount = params.boolean('executeRptFromCount')
            Map model  = [:]
            model.put("rowId", params.int("rowId"))
            model.put("columnName", params.columnName)
            model.put("count", params.int("count"))
            model.put("reportResultId", params.int("reportResultId"))
            executedTemplateQuery.onDemandSectionParams = (model as JSON).toString()

            ExecutedTemplateQuery primaryExecutedTemplateQuery = null
            if(params.boolean('isInDraftMode')) {
                primaryExecutedTemplateQuery = params.("reportResultId") ? ExecutedTemplateQuery.findByDraftReportResult(ReportResult.read(params.long('reportResultId'))) : null
            }else {
                primaryExecutedTemplateQuery = params.("reportResultId") ? ExecutedTemplateQuery.findByFinalReportResult(ReportResult.read(params.long('reportResultId'))) : null
            }
            executedTemplateQuery.executedQuery = primaryExecutedTemplateQuery.executedQuery
            executedTemplateQuery.addToExecutedQueryValueLists(primaryExecutedTemplateQuery.executedQueryValueLists)
            setExecutedDateRangeInformation(executedTemplateQuery)
            SuperQuery query = executedTemplateQuery.executedQuery
            ReportTemplate template = params.('template.id') ? ReportTemplate.read(params.long('template.id')) : null
            reportExecutorService.saveExecutedTemplateQuery(executedTemplateQuery, template, query, isExecuteRptFromCount)
            render(AjaxResponseDTO.success().withSuccessAlert(message(code: 'executedReportConfiguration.report.template.section.success')).toJsonAjaxResponse())
        } catch (ValidationException ve) {
            log.warn("Validation Error during periodic configuration -> saveOnDemandSection")
            response.status = 500
            render([error: true, errors: executedTemplateQuery.errors.allErrors.collect {
                it.field
            }, message   : message(code: "default.system.error.message")] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected Error in periodic configuration -> saveOnDemandSection", ex)
            response.status = 500
            render([message: message(code: "default.server.error.message").toString()] as JSON)
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def bulkUpdate() {
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def importExcel() {
        params.auditLogJustification = session.bulkJustification
        MultipartFile file = request.getFile('file')
        Workbook workbook = null

        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }

        Map result = periodicReportService.importFromExcel(workbook)
        String addCountMessage = periodicReportService.getDisplayMessage('app.bulkUpdate.error.added', result.added)
        String updateCountMessage = periodicReportService.getDisplayMessage('app.bulkUpdate.error.updated', result.updated)
        flash.message = [addCountMessage, updateCountMessage].join("\n\n")
        if (result.errors.size() > 0)
            flash.error = result.errors.size() + " errors:\n" + result.errors.join("\n")
        redirect(action: "bulkUpdate")
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def exportToExcel() {

        User currentUser = userService.currentUser
        LibraryFilter filter = new LibraryFilter(params, currentUser, PeriodicReportConfiguration, [PeriodicReportConfiguration.class])
        List<Long> idsForUser = PeriodicReportConfiguration.fetchAllIdsForBulkUpdate(filter).list([sort: params.sort, order: params.direction])
        def data = []
        List<PeriodicReportConfiguration> configurationList
        def productSelection = PVDictionaryConfig.ProductConfig.views.collectEntries { element ->
            [element.index, ViewHelper.getMessage(element.code)]
        }
        idsForUser.collate(999).each {
            configurationList = PeriodicReportConfiguration.getAll(it)

            configurationList.each { conf ->
                def product = conf.productSelection ? JSON.parse(conf.productSelection as String) : [:]
                List row = [conf.reportName,
                            conf.configurationTemplate?.reportName ?: ""]
                productSelection.eachWithIndex { entry, int i ->
                    row.add(product["${(i + 1)}"]?.collect { it.name }?.join(","),)
                }
                if (Holders.config.getProperty('pv.dictionary.group.enabled', Boolean)) {
                    row.add(ViewHelper.getDictionaryGroupValues(conf.productGroupSelection))
                }
                row.addAll([
                        conf.periodicReportType?.name(),
                        conf.globalDateRangeInformation?.dateRangeEnum?.name(),
                        conf.globalDateRangeInformation?.relativeDateRangeValue,
                        conf.globalDateRangeInformation?.dateRangeStartAbsolute?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                        conf.globalDateRangeInformation?.dateRangeEndAbsolute?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                        conf.primaryReportingDestination,
                        conf.dueInDays ?: "",
                        conf.scheduleDateJSON,
                        conf.tenantId,
                        conf.owner?.username
                ])
                data.add(row)
            }
        }
        List columns = [
                [title: ViewHelper.getMessage("app.label.reportName"), width: 25],
                [title: ViewHelper.getMessage("app.PeriodicReport.configuration.template.label"), width: 25]]
        productSelection.each { k, v ->
            columns << [title: v, width: 25]
        }
        if (Holders.config.getProperty('pv.dictionary.group.enabled', Boolean)) {
            columns << [title: ViewHelper.getMessage("app.label.productGroup"), width: 25]
        }
        columns.addAll([
                [title: ViewHelper.getMessage("app.label.periodicReportType"), width: 25],
                [title: ViewHelper.getMessage("app.label.DateRangeType"), width: 25],
                [title: " X ", width: 25],
                [title: ViewHelper.getMessage("app.label.startDate"), width: 25],
                [title: ViewHelper.getMessage("app.label.endDate"), width: 25],
                [title: ViewHelper.getMessage("app.periodicReport.executed.reportingDestination.label"), width: 25],
                [title: ViewHelper.getMessage("app.label.dueInDaysPastDLP"), width: 25],
                [title: ViewHelper.getMessage("app.label.scheduler"), width: 25],
                [title: ViewHelper.getMessage("app.label.tenantId"), width: 25],
                [title: ViewHelper.getMessage("app.label.username"), width: 25]
        ])
        def metadata = [sheetName: "Configurations",
                        columns  : columns]
        byte[] file = qualityService.exportToExcel(data, metadata)
        String fileName = System.currentTimeMillis() + ".xlsx"
        AuditLogConfigUtil.logChanges(configurationList, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Aggregate Report", ReportFormatEnum.XLSX.displayName))
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def ajaxCopy() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        PeriodicReportConfiguration instance = params.id ? PeriodicReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else{

            try {
                User currentUser = userService.currentUser
                def savedConfig = configurationService.copyConfig(instance, currentUser)
                responseDTO.data = periodicReportService.toBulkTableMap(savedConfig)
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
        PeriodicReportConfiguration instance = params.id ? PeriodicReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else
        if (instance.nextRunDate && instance.isEnabled) {
            responseDTO.setFailureResponse(message(code: 'app.configuration.run.exists') as String)
        }else {
            try {
                instance.setIsEnabled(true)
                setNextRunDateAndScheduleDateJSON(instance)
                CRUDService.update(instance)
                responseDTO.data = [nextRunDate: DateUtil.getLongDateStringForTimeZone(instance.nextRunDate,  userService.currentUser?.preference?.timeZone)]
            } catch (ValidationException e) {
                responseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def ajaxDelete() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        PeriodicReportConfiguration instance = params.id ? PeriodicReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else
        if (!instance.isEditableBy(userService.currentUser)) {
            responseDTO.setFailureResponse(message(code: "app.configuration.delete.permission", args: [instance.reportName]) as String)
        }else {
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

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def editField() {
        params.auditLogJustification = session.bulkJustification
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        PeriodicReportConfiguration instance = params.id ? PeriodicReportConfiguration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.PERIODIC_REPORT'), params.id]) as String)
        } else {
            try {
                if(params.globalDateRangeInformation){
                    assignParameterValuesToGlobalQuery(instance)
                    CRUDService.update(instance)
                } else  if(params.reportName){
                    instance.setReportName(params.reportName.trim())
                    instance.validate(["reportName"])
                    if(instance.hasErrors())
                        responseDTO.setFailureResponse(g.message(code: 'com.rxlogix.config.configuration.name.unique.per.user') as String)
                    else
                        CRUDService.update(instance)
                }  else if(params.scheduleDateJSON){
                    instance.scheduleDateJSON = params.scheduleDateJSON
                    CRUDService.update(instance)
                    responseDTO.data = [json:instance.scheduleDateJSON, label:periodicReportService.parseScheduler(instance.scheduleDateJSON,userService.currentUser?.preference?.locale)]
                }else {
                    bindData(instance, params)
                    CRUDService.update(instance)
                }
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def saveSection(ExecutedTemplateQuery executedTemplateQuery) {
        try {
            if(ExecutionStatus.findAllByEntityIdAndExecutionStatusInList(executedTemplateQuery.executedConfigurationId, (ReportExecutionStatusEnum.getInProgressStatusesList()+ReportExecutionStatusEnum.BACKLOG+ReportExecutionStatusEnum.SCHEDULED))){
                render(AjaxResponseDTO.success().withWarningAlert(message(code: 'executedReportConfiguration.add.section.warn')).toJsonAjaxResponse())
            }else {
                assignParameterValuesToTemplateQuery(executedTemplateQuery)
                executedTemplateQuery.manuallyAdded = true
                setExecutedDateRangeInformation(executedTemplateQuery)
                SuperQuery query = params.('query.id') ? SuperQuery.read(params.long('query.id')) : null
                ReportTemplate template = params.('template.id') ? ReportTemplate.read(params.long('template.id')) : null
                if (!template) {
                    response.status = 500
                    render([message: message(code: "com.rxlogix.config.Configuration.template.nullable").toString()] as JSON)
                }
                reportExecutorService.saveExecutedTemplateQuery(executedTemplateQuery, template, query, false)
//          need to handle job executions
                render(AjaxResponseDTO.success().withSuccessAlert(message(code: 'executedReportConfiguration.add.section.success')).toJsonAjaxResponse())
            }
        } catch (ValidationException ve) {
            log.warn("Validation Error during periodicReport -> saveSection")
            response.status = 500
            render([error: true, errors: executedTemplateQuery.errors.allErrors.collect {
                it.field
            }, message   : message(code: "default.system.error.message")] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected error in periodicReport -> saveSection", ex)
            response.status = 500
            render([message: message(code: "default.server.error.message").toString()] as JSON)
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD', 'ROLE_CONFIGURATION_CRUD'])
    def removeSection(Long id) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(id)
        if (!executedTemplateQuery) {
            response.status = 500;
            render(([error: "Not Found"]) as JSON)
            return
        }
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.executedConfiguration
        try {
            executedReportConfiguration.removeFromExecutedTemplateQueries(executedTemplateQuery)
            CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
            Boolean isInDraftMode = (executedReportConfiguration.class == PeriodicReportConfiguration) ? (executedReportConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
            reportExecutorService.deleteReportsCachedFilesIfAny(executedReportConfiguration, isInDraftMode)
            render(AjaxResponseDTO.success()
                    .withSuccessAlert(message(code: 'executedReportConfiguration.remove.section.success'))
                    .toJsonAjaxResponse())
        } catch (ValidationException ve) {
            log.warn("Validation Error during periodicReport -> removeSection")
            response.status = 500
            render([message: message(code: "executedReportConfiguration.remove.section.error")] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected error in periodicReport -> removeSection", ex)
            response.status = 500
            render([message: message(code: "default.server.error.message").toString()] as JSON)
        }
    }

    @Secured(['ROLE_DMS'])
    def getDmsFolders() {
        def settings = JSON.parse(ApplicationSettings.first().dmsIntegration)
        Object object = null
        if (params.id) {
            object = ExecutedReportConfiguration.read(params.long('id'))
        }
        render dmsService.getFolderList("${settings.rootFolder ?: ''}${params.folder ?: ''}", object) as JSON
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def listTemplates() {
        params.max = params.int('length') ?: params.max
        if(params.max != null) {
            params.max = params.max ?: 10

            params.offset = params.int('start') ?: params.offset
            params.offset = params.offset ?: 0

            def data = ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), PeriodicReportConfiguration.class, params.searchString).list([sort: params.sort, order: params.direction, max: params.max, offset: params.offset]).collect {
                [id: it.id, reportName: it.reportName, description: it.description, dateCreated: it.dateCreated]
            }

            int totalCount = ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), PeriodicReportConfiguration.class).count()
            int filterCount = ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), PeriodicReportConfiguration.class, params.searchString).count()

            render([aaData: data, recordsTotal: totalCount, recordsFiltered: filterCount] as JSON)
        } else {
            render ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), PeriodicReportConfiguration.class).list([sort: 'reportName']).collect {
                [id: it.id, reportName: it.reportName, description: it.description, dateCreated: it.dateCreated]
            } as JSON
        }
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def createFromTemplate(Long id) {
        PeriodicReportConfiguration originalConfig = PeriodicReportConfiguration.read(id)
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = userService.getCurrentUser()
        // No tenant check.
        if (!currentUser.isAdmin() && !originalConfig.isVisible(currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(view: "index")
            return
        }
        def savedConfig = configurationService.copyConfig(originalConfig, currentUser, null, Tenants.currentId() as Long, true)
        if (savedConfig.hasErrors()) {
            chain(action: "index", model: [theInstance: savedConfig])
        } else {
            flash.message = message(code: "app.copy.success", args: [savedConfig.reportName])
            redirect(action: "edit", id: savedConfig.id, params: [fromTemplate: true])
        }
    }

    def sources() {
        render(view: "sources", model: [attachments: ExecutedPublisherSource.findAllByConfigurationIsNull()])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def downloadAttachment() {
        BasicPublisherSource attachment = params.executedAttachment ?
                ExecutedPublisherSource.get(params.long("id")) : PublisherSource.get(params.long("id"))
        if (!attachment) {
            notFound()
            return
        }
        try{
            Map att = publisherSourceService.getDataMap(attachment)
            render(file: att.data, fileName: att.name, contentType: att.contentType)
        } catch (Exception e){
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            render(view: '/configuration/includes/errorAttachment', model: [error: errors.toString()])
        }

    }

    def downloadExecutedAttachment() {
        ExecutedPublisherSource attachment = ExecutedPublisherSource.get(params.long("id"))
        if (!attachment) {
            notFound()
            return
        }
        render(file: attachment.data, fileName: attachment.name, contentType: attachment.ext)
    }

    private bindAttachments(def configurationInstance) {
        boolean isConfigParams = configurationInstance instanceof BaseConfiguration
        Set<Long> newId = params.attachmentSectionId?.findAll { it && (it != "0") }?.collect { it as Long }
        Set<Long> oldId = configurationInstance.attachments?.collect { it.id } ?: []
        Set<Long> idToDelete = oldId - newId
        params << [auditLog: [attachments: []]]
        idToDelete?.each { val ->
            Long id = val as Long
            BasicPublisherSource attachment = configurationInstance.attachments?.find { it.id == id }
            if (isConfigParams) {
                configurationInstance.removeFromAttachments(attachment)
                attachment.delete()
            } else {
                CRUDService.deleteWithAuditLog(attachment, attachment.name)
            }
            params.auditLog.attachments << [old: attachment.toMap(), updated: [:]]
        }
        params.attachmentSectionId?.eachWithIndex { val, i ->
            Long id = val ? (val as Long) : 0L
            if (i > 0) {
                BasicPublisherSource attachment
                def auditLog
                if (id == 0) {
                    attachment = configurationInstance instanceof PeriodicReportConfiguration ? new PublisherSource() : new ExecutedPublisherSource()
                    auditLog = [old: [:], updated: [:]]
                } else {
                    attachment = configurationInstance.attachments.find { it.id == id }
                    auditLog = [old: attachment.toMap(), updated: [:]]
                }
                attachment.sortNumber = i
                if (params.attachmentSectionUserGroup && params.attachmentSectionUserGroup[i])
                    attachment.userGroup = UserGroup.get(params.attachmentSectionUserGroup[i] as Long)
                MultipartFile file = request.getFiles('attachmentSectionFile')[i]
                if (file && file.size > 0) {
                    attachment.data = file.bytes
                    attachment.path = file.originalFilename?.trim()
                    attachment.ext = file.contentType
                } else {
                    attachment.path = params.attachmentPath[i]?.trim()
                    attachment.script = params.attachmentScript[i]
                }
                attachment.fileSource = params.attachmentFileSource[i]
                attachment.fileType = params.attachmentFileType[i]
                attachment.name = params.attachmentName[i]

                if (params.oneDriveUserSettings[i]) {
                    attachment.oneDriveUserSettings = OneDriveUserSettings.get(params.oneDriveUserSettings[i] as Long)
                    attachment.oneDriveFolderId = params.oneDriveFolderId[i]
                    attachment.oneDriveSiteId = params.oneDriveSiteId[i]
                    attachment.oneDriveFolderName = params.oneDriveFolderName[i]
                }

                removeProtocol(attachment)
                if (!attachment.id) {
                    if (isConfigParams)
                        configurationInstance.addToAttachments(attachment)
                    else
                        CRUDService.save(attachment, params)
                } else {
                    if (!isConfigParams) CRUDService.update(attachment, params)
                }
                auditLog.updated = attachment.toMap()
                params.auditLog.attachments << auditLog
            }
        }
    }

    private void removeProtocol(BasicPublisherSource attachment) {
        if ((attachment.fileSource == BasicPublisherSource.Source.HTTPS) && (attachment.path.toLowerCase().startsWith("https://")))
            attachment.path = attachment.path.substring(8);
        else if ((attachment.fileSource == BasicPublisherSource.Source.HTTP) && (attachment.path.toLowerCase().startsWith("http://")))
            attachment.path = attachment.path.substring(7);
        else if ((attachment.fileSource == BasicPublisherSource.Source.FOLDER) && (attachment.path.toLowerCase().startsWith("file://")))
            attachment.path = attachment.path.substring(7);
    }

    private PeriodicReportConfiguration setAttributeTags(PeriodicReportConfiguration configuration) {

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
            List<Tag> orderedTags = updatedTags.unique().collect { it ->
                Tag tag = Tag.findByName(it)
                if (!tag) {
                    tag = new Tag(name: it).save()
                }
                return tag
            }
            configuration.tags=orderedTags
        }
        return configuration
    }

    private void setReportingDestinations(PeriodicReportConfiguration periodicReportConfiguration) {
        String allReportingDestinations = params.reportingDestinations
        Set<String> reportingDestinations = []

        if (allReportingDestinations?.startsWith("[") && allReportingDestinations?.endsWith("]")) {
            String innerContent = allReportingDestinations.substring(1, allReportingDestinations.size() - 1).trim()
            if (innerContent.contains(",")) {
                reportingDestinations = innerContent.split(",").collect { it } as Set
            } else {
                reportingDestinations = [allReportingDestinations] as Set
            }
        } else {
            reportingDestinations = [allReportingDestinations] as Set
        }

        if (reportingDestinations && reportingDestinations.any { it != null && it.trim() }) {
            reportingDestinations.each {
                if (it != periodicReportConfiguration.primaryReportingDestination) {
                    periodicReportConfiguration.addToReportingDestinations(it)
                }
            }
        }
    }

    private void bindPublisherContributors(PeriodicReportConfiguration periodicReportConfiguration) {
        if (params.primaryPublisherContributor) {
            periodicReportConfiguration.primaryPublisherContributor = User.get(params.long("primaryPublisherContributor"))
        } else {
            periodicReportConfiguration.primaryPublisherContributor = userService.currentUser
        }
        if (params.publisherContributors) {
            params.publisherContributors.toString().split(Constants.MULTIPLE_AJAX_SEPARATOR).each {
                if (it != params.primaryPublisherContributor) {
                    periodicReportConfiguration.addToPublisherContributors(User.get(it as Long))
                }
            }
        }
    }

    private void setNextRunDateAndScheduleDateJSON(PeriodicReportConfiguration configurationInstance) {
        configurationInstance.nextRunDate = null
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            if (MiscUtil.validateScheduleDateJSON(configurationInstance.scheduleDateJSON)) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
                return
            }
        }
        configurationInstance.nextRunDate = null
    }

    private clearListFromConfiguration(PeriodicReportConfiguration configurationInstance) {
        configurationInstance?.deliveryOption?.emailToUsers?.clear()
        configurationInstance?.deliveryOption?.attachmentFormats?.clear()
        configurationInstance?.deliveryOption?.oneDriveFormats?.clear()
        configurationInstance?.tags?.clear()
        configurationInstance?.reportingDestinations?.clear()
        configurationInstance?.poiInputsParameterValues?.clear()
        return configurationInstance
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private populateModel(PeriodicReportConfiguration configurationInstance) {
        String executableBy = params.get('executableBy')
        List<String> executableUserList = executableBy ? Arrays.asList(executableBy.split(";")) : []
        //Do not bind in any other way because of the clone contained in the params
        clearListFromConfiguration(configurationInstance)
        bindData(configurationInstance, params, [exclude: ["templateQueries", "tags", "executableBy", "reportingDestinations", "isEnabled", "publisherConfigurationSections", "asOfVersionDate", "includeLockedVersion", "globalQueryValueLists", "emailConfiguration", "sharedWith", "dmsConfiguration", "publisherContributors", "primaryPublisherContributor"]])
        configurationInstance.deliveryOption.oneDriveSiteId = params.deliveryOption.oneDriveSiteId
        bindAsOfVersionDate(configurationInstance, params.asOfVersionDate)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        setAttributeTags(configurationInstance)
        setReportingDestinations(configurationInstance)
        if(params.globalDateRangeInformation) {
            assignParameterValuesToGlobalQuery(configurationInstance)
        }
        bindExistingTemplateQueryEdits(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        configurationService.removeRemovedTemplateQueries(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        if(params.emailConfiguration) {
            bindEmailConfiguration(configurationInstance, params.emailConfiguration)
        }
        configurationService.bindSharedWith(configurationInstance, params.list('sharedWith'), executableUserList, configurationInstance.id ? true : false)
        bindDmsConfiguration(configurationInstance, params.dmsConfiguration)
        bindReportTasks(configurationInstance, params)
        configurationService.checkProductCheckboxes(configurationInstance)
        bindAttachments(configurationInstance)
        bindPublisherSections(configurationInstance)
        bindPublisherContributors(configurationInstance)
        if (configurationInstance.includeWHODrugs) {
            configurationInstance.isMultiIngredient = true
        }
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private populateModelForEditedConfig(PeriodicReportConfiguration configurationInstance, User currUser) {
        //Do not bind in any other way because of the clone contained in the params
        bindData(configurationInstance, params, [exclude: ["id", "templateQueries", "executableBy", "tags", "reportingDestinations", "isEnabled", "asOfVersionDate", "includeLockedVersion", "globalQueryValueLists", "emailConfiguration", "sharedWith", "dmsConfiguration", "publisherContributors", "primaryPublisherContributor"]])
        bindAsOfVersionDate(configurationInstance, params.asOfVersionDate)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        setAttributeTags(configurationInstance)
        setReportingDestinations(configurationInstance)
        if(params.globalDateRangeInformation) {
            assignParameterValuesToGlobalQuery(configurationInstance)
        }
        bindNewTemplateQueriesForEditedConfig(configurationInstance)
        configurationService.removeRemovedTemplateQueries(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        if(params.emailConfiguration) {
            bindEmailConfiguration(configurationInstance, params.emailConfiguration)
        }
        configurationService.bindSharedWith(configurationInstance, params.list('sharedWith'), params.list('executableBy'), configurationInstance.id ? true : false)
        bindDmsConfiguration(configurationInstance, params.dmsConfiguration)
        bindReportTasks(configurationInstance, params)
        bindPublisherContributors(configurationInstance)
        configurationService.checkProductCheckboxes(configurationInstance)
        configurationInstance.setCreatedBy(currUser.username)
        configurationInstance.setModifiedBy(currUser.username)
        configurationInstance.setOwner(currUser)
    }

    private bindNewTemplateQueriesForEditedConfig(PeriodicReportConfiguration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = 0; params.containsKey("templateQueries[" + i + "].id"); i++) {
            if (!params.boolean("templateQueries[" + i + "].dynamicFormEntryDeleted")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                TemplateQuery templateQueryInstance = new TemplateQuery(bindingMap)

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

    private bindReportTasks(PeriodicReportConfiguration configurationInstance, params) {

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

    private bindAsOfVersionDate(PeriodicReportConfiguration configuration, def asOfDate) {
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

    private bindPublisherSections(PeriodicReportConfiguration configurationInstance) {
        Set<Long> newId = params.publisherSectionId?.findAll { it && (it != "0") }?.collect { it as Long }
        Set<Long> oldId = configurationInstance.publisherConfigurationSections?.collect { it.id } ?: []
        Set<Long> idToDelete = oldId - newId
        idToDelete?.each { val ->
            Long id = val as Long
            PublisherConfigurationSection section = configurationInstance.publisherConfigurationSections?.find { it.id == id }
            configurationInstance.removeFromPublisherConfigurationSections(section)
            section.delete()
        }
        configurationInstance
        params.publisherSectionId?.eachWithIndex { val, i ->
            Long id = val? (val as Long) : 0
            if (i > 1 && params.publisherSectionName[i]) {
                PublisherConfigurationSection section
                if (id == 0) {
                    section = new PublisherConfigurationSection()
                } else {
                    section = configurationInstance.publisherConfigurationSections.find { it.id == id }
                }
                section.sortNumber = i
                section.name = params.publisherSectionName[i]
                section.dueInDays = params.pubDueInDays[i]? params.pubDueInDays[i] as Integer:null
                section.taskTemplate = params.publisherSectionTaskTemplate[i]? TaskTemplate.get(params.publisherSectionTaskTemplate[i] as Long):null
                section.destination = params.publisherReportingDestinations[i]
                if (params.publisherSectionUserGroup[i])
                    section.assignedToGroup = UserGroup.get(params.publisherSectionUserGroup[i] as Long)
                if (params.author[i])
                    section.author = User.get(params.author[i] as Long)
                if (params.reviewer[i])
                    section.reviewer = User.get(params.reviewer[i] as Long)
                if (params.approver[i])
                    section.approver = User.get(params.approver[i] as Long)
                if (params.publisherSectionTemplate[i]) {
                    def publisherTemplateDescription = JSON.parse(params.publisherSectionTemplate[i])
                    Long templateId = publisherTemplateDescription.templateId ? publisherTemplateDescription.templateId as Long : null
                    if (templateId)
                        section.publisherTemplate = PublisherTemplate.get(templateId)
                    section.setParameterValues(publisherTemplateDescription.parameterValues)
                }
                MultipartFile file = request.getFiles('publisherTemplateFile')[i]
                if (file && file.size > 0) {
                    section.templateFileData = file.bytes
                    section.filename = file.originalFilename
                }

                if (!section.id) {
                    configurationInstance.addToPublisherConfigurationSections(section)
                }
            }
        }
    }
    private bindNewTemplateQueries(PeriodicReportConfiguration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = configurationInstance.templateQueries.size(); params.containsKey("templateQueries[" + i + "]"); i++) {
            if (params.get("templateQueries[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                TemplateQuery templateQueryInstance = new TemplateQuery(bindingMap)

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

    private bindTemplatePOIInputs(PeriodicReportConfiguration configurationInstance) {
        for (int i = 0; params.containsKey("poiInput[" + i + "].key"); i++) {
            String key = params.("poiInput[" + i + "].key")
            String value = params.("poiInput[" + i + "].value")
            if (!configurationInstance.poiInputsParameterValues*.key?.contains(key) && value) {
                configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: key, value: value))
            }
        }
    }

    private bindExistingTemplateQueryEdits(PeriodicReportConfiguration configurationInstance) {
        //handle edits to the existing Template Queries
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            templateQuery = (TemplateQuery) userService.setOwnershipAndModifier(templateQuery)
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

    private void setDateRangeInformation(int i, DateRangeInformation dateRangeInformationForTemplateQuery, PeriodicReportConfiguration configurationInstance) {
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

    private void assignParameterValuesToTemplateQuery(PeriodicReportConfiguration configurationInstance, TemplateQuery templateQuery, int i) {
        //TODO: This has been done for Audit Log, need to find alternative solution for this, unnecessarily keeping old values in ParameterValue Table.
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
                    if (value && value.startsWith(";")) {
                        value = value.substring(1)
                    }
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

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("qev[" + j + "].field"),false)
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

    private void assignParameterValuesToGlobalQuery(PeriodicReportConfiguration periodicReportConfiguration) {
        GlobalDateRangeInformation globalDateRangeInformation = periodicReportConfiguration.globalDateRangeInformation
        if (!globalDateRangeInformation) {
            globalDateRangeInformation = new GlobalDateRangeInformation()
            periodicReportConfiguration.globalDateRangeInformation = globalDateRangeInformation
        }
        bindData(globalDateRangeInformation, params.globalDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        configurationService.fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
        configurationService.bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
    }

    private bindEmailConfiguration(PeriodicReportConfiguration configurationInstance, Map emailConfiguration) {
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

    private bindDmsConfiguration(PeriodicReportConfiguration configurationInstance, Map dmsConfiguration) {
        if (dmsConfiguration && dmsConfiguration.format) {
            DmsConfiguration dmsConfigurationInstance
            if(configurationInstance.getId() && !configurationInstance.isAttached()){
                configurationInstance.attach()
            }
            if (configurationInstance.dmsConfiguration) {
                dmsConfigurationInstance = configurationInstance.dmsConfiguration
                dmsConfigurationInstance.isDeleted = false
                bindData(dmsConfigurationInstance, dmsConfiguration)
                CRUDService.update(dmsConfigurationInstance)
            } else {
                dmsConfigurationInstance = new DmsConfiguration(dmsConfiguration)
                CRUDService.save(dmsConfigurationInstance)
                configurationInstance.dmsConfiguration = dmsConfigurationInstance
            }
        } else {
            if (configurationInstance.dmsConfigurationId && !configurationInstance.dmsConfiguration.isDeleted) {
                CRUDService.softDelete(configurationInstance.dmsConfiguration, configurationInstance.dmsConfigurationId)
            }
        }
    }

    private getBindingMap(int i) {
        def bindingMap = [
                template               : params.("templateQueries[" + i + "].template"),
                query                  : params.("templateQueries[" + i + "].query"),
                operator               : params.("templateQueries[" + i + "].operator"),
                queryLevel             : params.("templateQueries[" + i + "].queryLevel"),
                dynamicFormEntryDeleted: params.("templateQueries[" + i + "].dynamicFormEntryDeleted") ?: false,
                header                 : params.("templateQueries[" + i + "].header") ?: null,
                loadToElastic          : params.("templateQueries[" + i + "].loadToElastic") ?: null,
                footer                 : params.("templateQueries[" + i + "].footer") ?: null,
                title                  : params.("templateQueries[" + i + "].title") ?: null,
                draftOnly              : params.("templateQueries[" + i + "].draftOnly") ?: false,
                headerProductSelection : params.("templateQueries[" + i + "].headerProductSelection") ?: false,
                headerDateRange        : params.("templateQueries[" + i + "].headerDateRange") ?: false,
                blindProtected         : params.("templateQueries[" + i + "].blindProtected") ?: false,
                privacyProtected       : params.("templateQueries[" + i + "].privacyProtected") ?: false,
                displayMedDraVersionNumber: params.("templateQueries[" + i + "].displayMedDraVersionNumber") ?: false,
                granularity               : params.("templateQueries[" + i + "].granularity"),
                templtReassessDate     : params.("templateQueries[" + i + "].templtReassessDate"),
                reassessListednessDate     : params.("templateQueries[" + i + "].reassessListednessDate"),
                userGroup                      : params.("templateQueries[" + i + "].userGroup"),
                dueInDays                      : params.("templateQueries[" + i + "].dueInDays")?params.("templateQueries[" + i + "].dueInDays") as Integer:null
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
        params.remove("attachmentSectionFile")
        params.remove("publisherTemplateFile")
        Map editingConfigurationMap = [configurationParams: (params as JSON).toString(), configurationId: params.id, action: params.id ? "edit" : "create", controller: "periodicReport", templateQueryIndex: params.templateQueryIndex]
        session.setAttribute("editingConfiguration", editingConfigurationMap)
    }

    private void initConfigurationFromMap(PeriodicReportConfiguration configurationInstance, Map map) {
        params.putAll(map)
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        populateModel(configurationInstance)
        configurationService.initConfigurationTemplatesFromSession(session, configurationInstance)
        configurationService.initConfigurationQueriesFromSession(session, configurationInstance)
        session.removeAttribute("editingConfiguration")
    }

    private User getCurrentUser(){
        return userService.getUser()
    }

    private validateTenant(Long tenantId){
        if(tenantId && (tenantId != (Tenants.currentId() as Long)) && !SpringSecurityUtils.ifAnyGranted("ROLE_DEV")){
            log.error("Request and Session tenant mismatch issue for User ${currentUser?.username} in PeriodicReportController")
            return false
        }
        return true
    }



}
