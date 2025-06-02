package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import  grails.util.Holders
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONElement
import org.springframework.web.multipart.MultipartFile

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class ConfigurationController {

    final Map CHART_WIDGET_ACTIONS=[RUN:"RUN", REFRESH:"REFRESH", SAVE:"SAVE"]

    def configurationService
    def userService
    def SpringSecurityService
    def CRUDService
    def reportExecutorService
    def importService
    def qualityService
    def periodicReportService
    def taskTemplateService

    static allowedMethods = [delete: ['DELETE','POST', "GET"]]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def view(Long id) {
        ReportConfiguration configuration = ReportConfiguration.read(id) as Configuration
        if (!configuration) {
            notFound()
            return
        }
        if(configuration instanceof PeriodicReportConfiguration) {
            redirect(controller: 'periodicReport', action: 'view', id: id)
            return
        }
        String configurationJson = null
        if (params.viewConfigJSON && SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            configurationJson = configurationService.getConfigurationAsJSON(configuration)
        }
        render(view: "view", model: [templateQueries: configuration.templateQueries, configurationInstance: configuration, rcaMap:configuration.pvqType? qualityService.getAllRcaDataMap():[:],
                                     isExecuted     : false, deliveryOption: configuration.deliveryOption,
                                     viewSql        : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(configuration) : null, configurationJson: configurationJson])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def viewExecutedConfig(Long id) {
        ExecutionStatus executionStatus
        ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.read(id)
        if (executedConfiguration instanceof ExecutedIcsrProfileConfiguration) {
            redirect(controller: 'icsrProfileConfiguration', action: 'viewExecutedConfig', id: id)
            return
        }
        if (executedConfiguration instanceof ExecutedIcsrReportConfiguration) {
            redirect(controller: 'icsrReport', action: 'viewExecutedConfig', id: id)
            return
        }
        if (!executedConfiguration) {
            notFound()
            return
        }
        if(!executedConfiguration.isViewableBy(userService.currentUser)){
            flash.warn = message(code:  "app.warn.noPermission")
            redirect(action: "index")
            return
        }

        if(executedConfiguration instanceof ExecutedConfiguration) {
            executionStatus = ExecutionStatus.getExecutionStatusByExectutedEntity(executedConfiguration.id,ExecutingEntityTypeEnum.CONFIGURATION).list()[0]
        }

        if(executedConfiguration instanceof ExecutedPeriodicReportConfiguration) {
            executionStatus = ExecutionStatus.getExecutionStatusByExectutedEntity(executedConfiguration.id,ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION).list()[0]
        }

        ReportConfiguration reportConfiguration = ReportConfiguration.get(executionStatus?.entityId) ?:
                ReportConfiguration.findByReportNameAndOwner(executedConfiguration.reportName, executedConfiguration.owner)
        render(view: "view", model: [templateQueries: executedConfiguration.executedTemplateQueries, configurationInstance: executedConfiguration, rcaMap:executedConfiguration.pvqType? qualityService.getAllRcaDataMap():[:],
                                     isExecuted     : true, deliveryOption: executedConfiguration.executedDeliveryOption, reportConfigurationId: reportConfiguration?.id])
    }


    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def copy(Configuration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = getCurrentUser()
        def savedConfig = configurationService.copyConfig(originalConfig, currentUser)
        if (savedConfig.hasErrors()) {
            chain(action: "index", model: [theInstance: savedConfig])
        } else {
            flash.message = message(code: "app.copy.success", args: [savedConfig.reportName])
            redirect(action: "view", id: savedConfig.id)
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Configuration configurationInstance) {

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
                        if (configurationInstance.pvqType) {
                            redirect(action: "listPvqCfg")
                        } else {
                            redirect action: "index", method: "GET"
                        }
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

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id) {
        Configuration configurationInstance = id ? Configuration.read(id) : null
        if (!configurationInstance) {
            notFound()
            return
        }
        def fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
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
        if (configurationInstance.pvqType && !userService.isAnyGranted("ROLE_SYSTEM_CONFIGURATION,ROLE_PVQ_EDIT")) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.reportName])
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
            render(view: "edit", model: [configurationInstance : configurationInstance,
                                         configSelectedTimeZone: params.configSelectedTimeZone, templateQueryIndex: templateQueryIndex, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser),hasConfigTemplateCreatorRole: currentUser.isConfigurationTemplateCreator()])
        }
    }

    def getAllEmailsUnique(Configuration configurationInstance) {
        return userService.getAllEmails(configurationInstance)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        Configuration configurationInstance = new Configuration()
        def fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.configurationParams) {
            initConfigurationFromMap(configurationInstance, fromSession.configurationParams)
        }
        Map templateQueryIndex = fromSession.templateQueryIndex
        boolean templateBlanks = false
        if (params.selectedTemplate) {
            ReportTemplate template = ReportTemplate.get(params.selectedTemplate)
            if (template) {
                TemplateQuery templateQuery = new TemplateQuery(template: template)
                if (template.instanceOf(DataTabulationTemplate) && template.isGranularity()){
                    templateQuery.granularity = GranularityEnum.MONTHLY
                }
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

        if (params.selectedCaseSeries) {
            ExecutedCaseSeries exCaseSeries = ExecutedCaseSeries.get(params.selectedCaseSeries)
            if (exCaseSeries) {
                configurationInstance.useCaseSeries = exCaseSeries
            }else{
                flash.error = message(code: 'app.configuration.case.series.notFound', args: [params.selectedCaseSeries])
            }
        }
        User user = getCurrentUser()
        if (params.pvqType) {
            if (!userService.isAnyGranted("ROLE_SYSTEM_CONFIGURATION,ROLE_PVQ_EDIT")) {
                flash.warn = message(code: "app.configuration.autorca.permission")
                redirect(action: "index")
                return
            }
            configurationInstance.pvqType = params.pvqType
            configurationInstance.reportName = ViewHelper.getMessage("app.configuration.autorca.state.reportName")
            configurationInstance.globalDateRangeInformation = new GlobalDateRangeInformation()
        }
        render(view: "create", model: [configurationInstance: configurationInstance, queryBlanks: queryBlanks,
                                       templateBlanks       : templateBlanks, configSelectedTimeZone: params?.configSelectedTimeZone, selectedQuery: params.selectedQuery, selectedTemplate: params.selectedTemplate,
                                       templateQueryIndex   : templateQueryIndex, sourceProfiles: SourceProfile.sourceProfilesForUser(user), hasConfigTemplateCreatorRole: user.isConfigurationTemplateCreator()])

    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
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

        Configuration configurationInstance = new Configuration()
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        populateModel(configurationInstance)
        configurationInstance.reportName = params.reportName
        try {
            configurationInstance = (Configuration) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            render view: "create", model: [configurationInstance : configurationInstance,
                                           configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in configuration -> save", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'create')
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
                redirect(action: (configurationInstance.pvqType?"listPvqCfg":"view"), id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def createTemplate() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        saveConfigurationMapToSession()
        redirect(controller: "template", action: 'create', params: [templateType: params.templateType])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def createQuery() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        saveConfigurationMapToSession()
        redirect(controller: "query", action: 'create')
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        Configuration configurationInstance = Configuration.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if (params.version && (configurationInstance.version > params.long('version'))) {
            flash.error = message(code: 'app.configuration.update.lock.permission', args: [configurationInstance.reportName])
            redirect(action: 'edit', id: configurationInstance.id)
            return;
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: configurationInstance.id)
            return
        }
        configurationInstance.nextRunDate = null
        populateModel(configurationInstance)
        configurationInstance.reportName = params.reportName
        try {
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                configurationInstance.qualityChecked = false
            }
            configurationInstance = (Configuration) CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
//            Configuration originalConfiguration = Configuration.get(params.id)
//            populateModel(originalConfiguration)
            configurationInstance.errors = ve.errors
            render view: "edit", model: [configurationInstance : configurationInstance,
                                         configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in configuration -> update", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'edit', id: configurationInstance?.id)
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])

                if(params.reportId) {
                    redirect(controller: "report", action: "showFirstSection", id: params.reportId)
                } else {
                    redirect(action: (configurationInstance.pvqType?"listPvqCfg":"view"), id: configurationInstance.id)
                }
            }
            '*' { respond configurationInstance, [status: OK] }
        }
    }

    private void setNextRunDateAndScheduleDateJSON(ReportConfiguration configurationInstance) {
        configurationInstance.nextRunDate = null
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            if (MiscUtil.validateScheduleDateJSON(configurationInstance.scheduleDateJSON)) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
                return
            }
        }
        configurationInstance.nextRunDate = null
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    @Transactional
    def run() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        Configuration configurationInstance
        if (params.id) {
            configurationInstance = Configuration.get(params.id)
            if (!configurationInstance) {
                notFound()
                return
            }
            if (params.version && (configurationInstance.version > params.long('version'))) {
                flash.error = message(code: 'app.configuration.update.lock.permission', args: [configurationInstance.reportName])
                redirect(action: 'edit', id: configurationInstance.id)
                return;
            }
            if(!validateTenant(params.long('tenantId'))){
                flash.error = message(code: "invalid.tenant")
                redirect(action: 'edit', id: configurationInstance.id)
                return
            }
        } else {
            configurationInstance = new Configuration()
            if(!validateTenant(params.long('tenantId'))){
                flash.error = message(code: "invalid.tenant")
                redirect(action: 'create')
                return
            }
        }
        configurationInstance.setIsEnabled(true)
        EvaluateCaseDateEnum evaluateDateAs = configurationInstance.evaluateDateAs ?: EvaluateCaseDateEnum.LATEST_VERSION
        populateModel(configurationInstance)
        if (params.fromTemplate) {
            configurationInstance.evaluateDateAs = evaluateDateAs
        }
        configurationInstance.reportName = params.reportName
        try {
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                configurationInstance.qualityChecked = false
            }
            if (configurationInstance.id) {
                configurationInstance = (Configuration) CRUDService.update(configurationInstance)
            } else {
                configurationInstance = (Configuration) CRUDService.save(configurationInstance)
            }
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            configurationInstance.setIsEnabled(false)
            if (params.id) {
                render view: "edit", model: [configurationInstance : configurationInstance,
                                             configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            } else {
                render view: "create", model: [configurationInstance : configurationInstance,
                                               configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            }
            return
        } catch (Exception ex) {
            log.error("Unexpected error in configuration -> run", ex)
            flash.error = message(code: "app.error.500")
            if (params.id) {
                redirect(action: 'edit', id: params.id)
            } else {
                redirect(action: 'create')
            }
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'app.Configuration.RunningMessage')
                if(params.reportId) {
                    redirect(controller: "report", action: "showFirstSection", id: params.reportId)
                } else {
                    if (configurationInstance.pvqType) {
                        redirect(action: "listPvqCfg")
                    } else {
                        redirect(controller: 'executionStatus', action: "list")
                    }
                }
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def runOnce() {
        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if (configurationInstance.nextRunDate != null && configurationInstance.isEnabled == true) {
            flash.warn = message(code: 'app.configuration.run.exists')
            redirect(action: "index")

        } else {
            if (configurationInstance?.isTemplate && configurationInstance.hasAuthorityToRunAsTemplateReport(currentUser)) {
                flash.warn = message(code: "com.rxlogix.config.ReportConfiguration.create.template.forbidden")
                redirect(action: "index")
                return
            }
            configurationInstance.setIsEnabled(true)
            configurationInstance.isPriorityReport = params.boolean('isPriorityReport')
            if(configurationInstance.isPriorityReport) {
                setNextRunDateAndScheduleDateJSON(configurationInstance)
            }else {
                configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
                configurationInstance.setNextRunDate(null)
                configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
            }
            try {
                if (configurationInstance.id) {
                    configurationInstance = (Configuration) CRUDService.update(configurationInstance)
                } else {
                    configurationInstance = (Configuration) CRUDService.save(configurationInstance)
                }
            } catch (ValidationException ve) {
                configurationInstance.errors = ve.errors
                configurationInstance.setIsEnabled(false)
                render view: "create", model: [configurationInstance : configurationInstance,
                                               configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
                return
            } catch (Exception ex) {
                log.error("Unexpected error in configuration -> runOnce", ex)
                flash.error = message(code: "app.error.500")
                redirect(action: 'create')
                return
            }

            if(configurationInstance.isPriorityReport){
                flash.message = message(code: 'app.Configuration.PriorityRunningMessage')
            }else{
                flash.message = message(code: 'app.Configuration.RunningMessage')
            }
            redirect(controller: 'executionStatus', action: "list", model: [configurationInstance: configurationInstance, status: CREATED])
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def addSection(Long id) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.read(id)
        if (!executedConfiguration) {
            log.error("Requested Entity not found : ${id}")
            render "Not Found"
            return
        }

        if(!executedConfiguration.isEditableBy(currentUser)){
            log.error("Requested Entity no permission : ${id}")
            render "No permission issue"
            return
        }

        render template: "includes/addSectionForm", model: [executedConfiguration: executedConfiguration]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def saveSection(ExecutedTemplateQuery executedTemplateQuery) {
        try {
            if(ExecutionStatus.findAllByEntityIdAndExecutionStatusInList(executedTemplateQuery.executedConfigurationId, (ReportExecutionStatusEnum.getInProgressStatusesList()+ReportExecutionStatusEnum.BACKLOG+ReportExecutionStatusEnum.SCHEDULED))){
                render(AjaxResponseDTO.success()
                        .withWarningAlert(message(code: 'executedReportConfiguration.add.section.warn'))
                        .toJsonAjaxResponse())
            }
            else {
                assignParameterValuesToTemplateQuery(executedTemplateQuery)
                executedTemplateQuery.manuallyAdded = true
                executedTemplateQuery.templtReassessDate = DateUtil.getEndDate(params.templtReassessDate, userService.currentUser?.preference?.locale)
                executedTemplateQuery.reassessListednessDate = DateUtil.getEndDate(params.reassessListednessDate, userService.currentUser?.preference?.locale)
                setExecutedDateRangeInformation(executedTemplateQuery)
                SuperQuery query = params.('query.id') ? SuperQuery.read(params.long('query.id')) : null
                ReportTemplate template = params.('template.id') ? ReportTemplate.read(params.long('template.id')) : null
                if (!template) {
                    response.status = 500
                    render([message: message(code: "com.rxlogix.config.Configuration.template.nullable").toString()] as JSON)
                }
                reportExecutorService.saveExecutedTemplateQuery(executedTemplateQuery, template, query, false)
                render(AjaxResponseDTO.success()
                        .withSuccessAlert(message(code: 'executedReportConfiguration.add.section.success'))
                        .toJsonAjaxResponse())
            }
        } catch (ValidationException ve) {
            log.warn("Validation Error during configuration -> saveSection")
            response.status = 500
            render([error: true, errors: executedTemplateQuery.errors.allErrors.collect {
                it.field
            }, message   : message(code: "default.system.error.message")] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected Error in configuration -> saveSection", ex)
            response.status = 500
            render([message: message(code: "default.server.error.message").toString()] as JSON)
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def saveOnDemandSection(ExecutedTemplateQuery executedTemplateQuery) {
        try {
            executedTemplateQuery.manuallyAdded = true
            boolean isExecuteRptFromCount = params.boolean('executeRptFromCount')
            Map model  = [:]
            model.put("rowId", params.int("rowId"))
            model.put("columnName", params.columnName)
            model.put("count", params.int("count"))
            model.put("reportResultId", params.int("reportResultId"))
            model.put("executedDateRangeInformationForTemplateQuery.relativeDateRangeValue", params.int("executedDateRangeInformationForTemplateQuery.relativeDateRangeValue"))
            model.put("executedConfiguration.relativeDateRangeValue", params.int("executedConfiguration.relativeDateRangeValue"))
            executedTemplateQuery.onDemandSectionParams = (model as JSON).toString()
            ExecutedTemplateQuery primaryExecutedTemplateQuery = null
            if(params.boolean('isInDraftMode')) {
                primaryExecutedTemplateQuery = params.("reportResultId") ? ExecutedTemplateQuery.findByDraftReportResult(ReportResult.read(params.long('reportResultId'))) : null
            }else{
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
            log.warn("Validation Error during configuration -> saveOnDemandSection")
            response.status = 500
            render([error: true, errors: executedTemplateQuery.errors.allErrors.collect {
                it.field
            }, message   : message(code: "default.system.error.message")] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected Error in configuration -> saveOnDemandSection", ex)
            response.status = 500
            render([message: message(code: "default.server.error.message").toString()] as JSON)
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def bulkUpdateConfig() {
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def importBulkExcel() {
        MultipartFile file = request.getFile('file')
        Workbook workbook = null

        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }

        Map result = configurationService.importFromExcel(workbook)
        String addCountMessage = periodicReportService.getDisplayMessage('app.bulkUpdate.error.added', result.added)
        String updateCountMessage = periodicReportService.getDisplayMessage('app.bulkUpdate.error.updated', result.updated)
        flash.message = [addCountMessage, updateCountMessage].join("\n\n")
        if (result.errors.size() > 0)
            flash.error = result.errors.size() + " errors:\n" + result.errors.join("\n")
        redirect(action: "bulkUpdateConfig")
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def exportToExcel() {
        User currentUser = userService.currentUser
        LibraryFilter filter = new LibraryFilter(params, currentUser, Configuration, [Configuration.class])
        List<Long> sharedWithIds = Configuration.ownedByUser(filter.user).list()
        List<Long> idsForUser = Configuration.fetchAllIdsForBulkUpdate(filter, sharedWithIds).list([sort: params.sort, order: params.direction])
        def data = []
        List<Configuration> configurationList
        def productSelection = PVDictionaryConfig.ProductConfig.views.collectEntries { element ->
            [element.index, ViewHelper.getMessage(element.code)]
        }
        idsForUser.collate(999).each {
            configurationList = Configuration.getAll(it)
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
                        conf.scheduleDateJSON,
                        conf.deliveryOption.sharedWith.username.join(", "),
                        conf.deliveryOption.sharedWithGroup.name.join(", "),
                        conf.deliveryOption.emailToUsers.join(", "),
                        conf.deliveryOption.attachmentFormats.join(", "),
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
        if (Holders.config.pv.dictionary.group.enabled) {
            columns << [title: ViewHelper.getMessage("app.label.productGroup"), width: 25]
        }
        columns.addAll([[title: ViewHelper.getMessage("app.label.scheduler"), width: 25],
                        [title: ViewHelper.getMessage("app.label.sharedWith"), width: 25],
                        [title: ViewHelper.getMessage("app.label.dashboardDictionary.sharedWithGroups"), width: 25],
                        [title: ViewHelper.getMessage("app.label.emailTo"), width: 25],
                        [title: ViewHelper.getMessage("app.label.additionalAttachments.allSections"), width: 25],
                        [title: ViewHelper.getMessage("app.label.tenantId"), width: 25],
                        [title: ViewHelper.getMessage("app.label.username"), width: 25]])
        def metadata = [sheetName: "Configurations",
                        columns  : columns]
        byte[] file = qualityService.exportToExcel(data, metadata)
        String fileName = System.currentTimeMillis() + ".xlsx"
        AuditLogConfigUtil.logChanges(configurationList, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Adhoc Report", ReportFormatEnum.XLSX.displayName))
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def editField() {
        AjaxResponseDTO ajaxResponseDTO = new AjaxResponseDTO()
        Configuration configuration = params.id ? Configuration.get(params.id) : null
        if (!configuration) {
            ajaxResponseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.ADHOC_REPORT'), params.id]) as String)
        } else {
            try {
                if (params.reportName) {
                    configuration.setReportName(params.reportName.trim())
                    configuration.validate(["reportName"])
                    if(configuration.hasErrors()){
                        ajaxResponseDTO.setFailureResponse(g.message(code: 'com.rxlogix.config.configuration.name.unique.per.user') as String)
                    } else{
                        CRUDService.update(configuration)
                    }
                } else if (params.emailToUsers) {
                    if(params.emailToUsers == 'currentUser') {
                        params.emailToUsers = ''
                        configuration?.deliveryOption?.attachmentFormats?.clear()
                        configuration?.deliveryOption?.emailToUsers?.clear()
                    }
                    else {
                        String emailToUsers = params.emailToUsers
                        configuration?.deliveryOption?.emailToUsers?.clear()
                        emailToUsers.split(",").each { String emailId ->
                            configuration.deliveryOption.emailToUsers.add(emailId)
                        }
                    }
                    CRUDService.update(configuration)
                    ajaxResponseDTO.data = [emailToUsers: configuration.deliveryOption.emailToUsers]
                } else if (params.scheduleDateJSON) {
                    configuration.scheduleDateJSON = params.scheduleDateJSON
                    CRUDService.update(configuration)
                    ajaxResponseDTO.data = [json: configuration.scheduleDateJSON, label: periodicReportService.parseScheduler(configuration.scheduleDateJSON, userService.currentUser?.preference?.locale)]
                } else if (params.sharedUsers) {
                    User currentUser = userService.currentUser
                    if(params.sharedUsers=='currentUser'){
                        params.sharedUsers= Constants.USER_TOKEN + currentUser.id.toString()
                    }
                    configurationService.bindSharedWith(configuration, params.list('sharedUsers'), null,true)
                    CRUDService.update(configuration)
                    ajaxResponseDTO.data = [sharedUsers: configuration.deliveryOption.sharedWith, sharedGroups: configuration.deliveryOption.sharedWithGroup]
                } else {
                    bindData(configuration, params)
                    CRUDService.update(configuration)
                }
            } catch (Exception e){
                ajaxResponseDTO.setFailureResponse(e)
            }
        }
        render(ajaxResponseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def ajaxDelete() {
        AjaxResponseDTO ajaxResponseDTO = new AjaxResponseDTO()
        Configuration configuration = params.id ? Configuration.get(params.id) : null
        if(!configuration) {
            ajaxResponseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.ADHOC_REPORT'), params.id]) as String)
        } else if (!configuration.isEditableBy(userService.currentUser)){
            ajaxResponseDTO.setFailureResponse(message(code: "app.configuration.delete.permission", args: [configuration.reportName]) as String)
        } else{
            try {
                CRUDService.softDelete(configuration, configuration.reportName, params.deleteJustification)
                ajaxResponseDTO.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), configuration.reportName])
            } catch (ValidationException e) {
                ajaxResponseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                ajaxResponseDTO.setFailureResponse(e)
            }
        }
        render(ajaxResponseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def ajaxCopy() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        Configuration instance = params.id ? Configuration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.ADHOC_REPORT'), params.id]) as String)
        } else{

            try {
                User currentUser = userService.currentUser
                def savedConfig = configurationService.copyConfig(instance, currentUser)
                responseDTO.data = configurationService.toBulkTableMap(savedConfig)
            } catch (ValidationException e) {
                responseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def ajaxRun() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        Configuration instance = params.id ? Configuration.get(params.id) : null
        if (!instance) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.ADHOC_REPORT'), params.id]) as String)
        } else if (instance.nextRunDate && instance.isEnabled) {
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

    private String getRunOnceScheduledDateJson() {
        User user = getCurrentUser()
        def startupTime = (RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(user, DateUtil.JSON_DATE))
        def timeZone = DateUtil.getTimezoneForRunOnce(user)
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }

    private getDateRangeTypeI18n() {
        return DateRangeType.findAllByIsDeleted(false, [sort: 'sortOrder', order: 'asc']).collect {
            [name: it.id, display: (message(code: (it?.getI18nKey())))]
        }
    }

    private getDateRange() {
        return ((QueryOperatorEnum.getDateOperators() - QueryOperatorEnum.getNumericOperators() + DateRangeValueEnum.values() - DateRangeValueEnum.RELATIVE).collect {
            [name: it, display: (message(code: (it?.getI18nKey())))]
        })
    }

    @Secured(["isAuthenticated()"])
    def executionStatus() {
        User currentUser = getCurrentUser()
        render(view: "executionStatus", model: [isAdmin: currentUser?.isAdmin()])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def viewConfig() {
        if (params.id) {
            if (params.from == "result") {
                def configurationInstance = ExecutedConfiguration.get(params.id)
                redirect(controller: "configuration", action: "viewExecutedConfig", id: configurationInstance.id)
            } else {
                redirect(controller: "configuration", action: "view", id: params.id)
            }
        } else {
            notFound()
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def listTemplates() {
        params.max = params.int('length') ?: params.max
        if(params.max != null) {
            params.max = params.max ?: 10

            params.offset = params.int('start') ?: params.offset
            params.offset = params.offset ?: 0

            def data = ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), Configuration.class, params.searchString).list([sort: params.sort, order: params.direction, max: params.max, offset: params.offset]).collect {
                [id: it.id, reportName: it.reportName, description: it.description, dateCreated: it.dateCreated]
            }

            int totalCount = ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), Configuration.class).count()
            int filterCount = ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), Configuration.class, params.searchString).count()
            render([aaData: data, recordsTotal: totalCount, recordsFiltered: filterCount] as JSON)
        } else {
            render ReportConfiguration.fetchAllTemplatesForUser(userService.getCurrentUser(), Configuration.class).list([sort: 'reportName']).collect {
                [id: it.id, reportName: it.reportName, description: it.description, dateCreated: it.dateCreated]
            } as JSON
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def createFromTemplate(Long id) {
        Configuration originalConfig = Configuration.read(id)
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = userService.getCurrentUser()
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

    def validateValue() {
        Map map = [uploadedValues: "", message: "", success: false]
        String selectedField = params.selectedField
        String qevId = params.qevId
        List<String> list = params.values.split(";").collect { it.trim() }.findAll { it }
        if (list) {
            Map<String, List> validationResult = importService.getValidInvalidValues(list, selectedField, userService.user?.preference?.locale?.toString(), false)
            String template = g.render(template: '/query/importValueModal', model: [qevId: qevId, validValues: validationResult.validValues, invalidValues: validationResult.invalidValues, duplicateValues: importService.getDuplicates(list)])
            map.uploadedValues = template
            map.success = true
        }
        render map as JSON
    }

    def importExcel() {
        Map map = [uploadedValues: "", message: "", success: false]
        String selectedField = params.selectedField
        String qevId = params.qevId
        MultipartFile file = request.getFile('file')

        List list = importService.readFromExcel(file)
        if (list) {
            map.uploadedValues = list.join(';')
            map.success = true
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render map as JSON
    }

    private Configuration setAttributeTags(Configuration configuration) {

        if (params?.tags) {
            if (configuration?.tags) {
                configuration?.tags.each {
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
            configuration.tags = orderedTags
        }
        return configuration
    }

    private clearListFromConfiguration(Configuration configurationInstance) {
        configurationInstance?.deliveryOption?.emailToUsers?.clear()
        configurationInstance?.deliveryOption?.attachmentFormats?.clear()
        configurationInstance?.tags?.clear()
        configurationInstance?.poiInputsParameterValues?.clear()
        return configurationInstance
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback
    private void populateModel(Configuration configurationInstance) {
        String executableBy = params.get('executableBy')
        List<String> executableUserList = executableBy ? Arrays.asList(executableBy.split(";")) : []
        //Do not bind in any other way because of the clone contained in the params
        clearListFromConfiguration(configurationInstance)
        bindData(configurationInstance, params, [exclude: ["templateQueries","pvqType", "tags", "executableBy", "isEnabled", "asOfVersionDate", "includeLockedVersion", "emailConfiguration", "sharedWith", "dmsConfiguration", "reportName", 'globalDateRangeInformation']])
        bindAsOfVersionDate(configurationInstance, params.asOfVersionDate)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        setAttributeTags(configurationInstance)
        assignParameterValuesToGlobalQuery(configurationInstance)
        bindExistingTemplateQueryEdits(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        configurationService.removeRemovedTemplateQueries(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        bindEmailConfiguration(configurationInstance, params.emailConfiguration)
        configurationService.bindSharedWith(configurationInstance, params.list('sharedWith'), executableUserList, configurationInstance.id ? true : false)
        bindDmsConfiguration(configurationInstance, params.dmsConfiguration)
        bindReportTasks(configurationInstance, params)
        configurationService.checkProductCheckboxes(configurationInstance)
        bindPvqType(configurationInstance)
        if (configurationInstance.includeWHODrugs) {
            configurationInstance.isMultiIngredient = true
        }
    }

    private bindPvqType(Configuration configurationInstance) {
        if (params.pvqType) {
            if (params.pvqType instanceof String[])
                configurationInstance.pvqType = params.pvqType.join(";") + ";"
            else
                configurationInstance.pvqType = params.pvqType + ";"
        } else {
            configurationInstance.pvqType = null
        }
    }

    private void assignParameterValuesToGlobalQuery(Configuration configuration) {
        GlobalDateRangeInformation globalDateRangeInformation = configuration.globalDateRangeInformation
        if (!globalDateRangeInformation) {
            globalDateRangeInformation = new GlobalDateRangeInformation()
            configuration.globalDateRangeInformation = globalDateRangeInformation
            if (configuration.id) {
                globalDateRangeInformation.reportConfiguration = configuration
                globalDateRangeInformation.save()
            }
        }
        bindData(globalDateRangeInformation, params.globalDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        configurationService.fixBindDateRange(globalDateRangeInformation, configuration, params)
        configurationService.bindParameterValuesToGlobalQuery(configuration, params)
    }

    private void bindAsOfVersionDate(Configuration configuration, def asOfDate) {
        if (!(configuration?.evaluateDateAs in [EvaluateCaseDateEnum.LATEST_VERSION, EvaluateCaseDateEnum.ALL_VERSIONS])) {
            configuration.includeLockedVersion = true
        } else {
            configuration.includeLockedVersion = params?.includeLockedVersion ?: false
        }
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            configuration.asOfVersionDate = DateUtil.getEndDate(asOfDate, userService.currentUser?.preference?.locale)
        } else {
            configuration.asOfVersionDate = null
        }
    }

    private void bindNewTemplateQueries(Configuration configurationInstance) {
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

    private bindExistingTemplateQueryEdits(Configuration configurationInstance) {
        //handle edits to the existing Template Queries
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            templateQuery = (TemplateQuery) userService.setOwnershipAndModifier(templateQuery)
            //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery
            //For Audit Log of TemplateQuery even when only date range is changed
            String originalDateRange = dateRangeInformationForTemplateQuery.toString()

            setDateRangeInformation(i, dateRangeInformationForTemplateQuery, configurationInstance)
            dateRangeInformationForTemplateQuery.templateQuery = templateQuery
            if (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") && params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") =~ "-?\\d+") {
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer
            }
            //For Audit Log of TemplateQuery even when only date range is changed
            String newDateRange = dateRangeInformationForTemplateQuery.toString()
            if (originalDateRange != newDateRange)
                templateQuery.lastUpdated = new Date()
            assignParameterValuesToTemplateQuery(configurationInstance, templateQuery, i)
        }
        configurationInstance
    }

    private bindTemplatePOIInputs(Configuration configurationInstance) {
        for (int i = 0; params.containsKey("poiInput[" + i + "].key"); i++) {
            String key = params.("poiInput[" + i + "].key")
            String value = params.("poiInput[" + i + "].value")
            if (!configurationInstance.poiInputsParameterValues*.key?.contains(key) && value) {
                configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: key, value: value))
            }
        }
    }

    private void bindEmailConfiguration(Configuration configurationInstance, Map emailConfiguration) {
        if (emailConfiguration && emailConfiguration.subject && emailConfiguration.body) {
            EmailConfiguration emailConfigurationInstance
            if (configurationInstance.emailConfiguration) {
                emailConfigurationInstance = configurationInstance.emailConfiguration
                emailConfigurationInstance.isDeleted = false
                bindData(emailConfigurationInstance, emailConfiguration)
                if (!emailConfiguration.cc) emailConfigurationInstance.cc = null;
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

    private void setDateRangeInformation(int i, DateRangeInformation dateRangeInformationForTemplateQuery, ReportConfiguration configurationInstance) {
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

    private void assignParameterValuesToTemplateQuery(ReportConfiguration configurationInstance, TemplateQuery templateQuery, int i) {
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
//                    String value = 'AMERICAN SAMOA;JAPAN'
                    boolean isFromCopyPaste = false
                    if (params.("qev[" + j + "].copyPasteValue")) {
                        value = params.("qev[" + j + "].copyPasteValue")
                    }
                    if (params.("qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByName(params.("qev[" + j + "].field"))
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
                        tempValue = new ExecutedQueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new ExecutedCustomSQLValue(key: key, value: value)
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
                tempValue = new ExecutedCustomSQLValue(key: params.("tv[" + j + "].key"),
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
        int relativeDateRangeValue = params.int("executedDateRangeInformationForTemplateQuery.relativeDateRangeValue") ?: 1
        executedDateRangeInformationForTemplateQuery.relativeDateRangeValue = relativeDateRangeValue

        if (dateRangeEnum) {
            executedDateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
            if (dateRangeEnum == DateRangeEnum.CUSTOM) {
                Locale locale = userService.currentUser?.preference?.locale
                executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), locale) ?: executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute
                executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), locale) ?: executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute
            } else if (dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
                executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute = executedTemplateQuery.executedConfiguration.reportMinMaxDate[1] ?: executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate ?: new Date()
            } else {
                DateRangeEnum type = dateRangeEnum
                if (dateRangeEnum == DateRangeEnum.PR_DATE_RANGE){
                    type = executedTemplateQuery.executedConfiguration.executedGlobalDateRangeInformation.dateRangeEnum
                    relativeDateRangeValue = params.int("executedConfiguration.relativeDateRangeValue") ?: executedTemplateQuery.executedConfiguration.executedGlobalDateRangeInformation.relativeDateRangeValue?: 1
                }
                if (type == DateRangeEnum.CUMULATIVE) {
                    executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
                    executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute = executedTemplateQuery.executedConfiguration.reportMinMaxDate[1] ?: executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate ?: new Date()
               } else if (type == DateRangeEnum.CUSTOM) {
                   executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute = executedTemplateQuery.executedConfiguration.executedGlobalDateRangeInformation.dateRangeStartAbsolute
                   executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute = executedTemplateQuery.executedConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute
               } else {
                    List l = RelativeDateConverter.(type.value())(new java.util.Date(ExecutedConfiguration.get(params.long("executedConfiguration.id")).nextRunDate?.getTime()), relativeDateRangeValue, 'UTC')
                    executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute = l[0] ?: new Date()
                    executedDateRangeInformationForTemplateQuery?.dateRangeEndAbsolute = l[1] ?: new Date()
                }
            }
        }
        executedDateRangeInformationForTemplateQuery.executedTemplateQuery = executedTemplateQuery
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    public enable() {

        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }

        if (!configurationInstance.isEditableBy(currentUser)) {
            flash.warn = message(code:  "app.warn.noPermission")
            redirect(view: "index")
            return
        }

        // configurationService.setBlankValues(configurationInstance, Query.get(params.query), params.JSONExpressionValues)
        configurationInstance.setIsEnabled(true)
        populateModel(configurationInstance)
        configurationInstance.reportName = params.reportName

        try {
            configurationInstance = (Configuration) CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            configurationInstance.reportName = params.reportName
            originalConfiguration.errors = ve.errors
            render view: "edit", model: [configurationInstance : originalConfiguration,
                                         configSelectedTimeZone: originalConfiguration.configSelectedTimeZoneV, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.enabled.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }

    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    public disable() {

        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }

        if (!configurationInstance.isEditableBy(currentUser)) {
            flash.warn = message(code:  "app.warn.noPermission")
            redirect(view: "index")
            return
        }

        if (!configurationInstance.executing) {
        // configurationService.setBlankValues(configurationInstance, Query.get(params.query), params.JSONExpressionValues)
        configurationInstance.setIsEnabled(false)
        configurationInstance.setScheduleDateJSON(configurationService.getScheduledDateJsonAfterDisable(configurationInstance))
//        populateModel(configurationInstance) Disabled Updating data while unscheduling

            try {
                configurationInstance = (Configuration) CRUDService.update(configurationInstance)
            } catch (ValidationException ve) {
//            TODO need to check later what's the need of having originalConfiguration with errors
                Configuration originalConfiguration = Configuration.read(configurationInstance.id)
                populateModel(originalConfiguration)
                configurationInstance.reportName = params.reportName
                originalConfiguration.errors = ve.errors
                render view: "edit", model: [configurationInstance : originalConfiguration,
                                             configSelectedTimeZone: originalConfiguration.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
                return
            }
        } else {
            flash.error = message(code:'app.configuration.unscheduled.fail', args: [configurationInstance.reportName])
            render view: 'index'
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'configuration.label'), configurationInstance.reportName])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def load() {
        render(view: 'load')
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveJSONConfigurations() {
        if (params?.JSONConfigurations && !(params?.JSONConfigurations as String).trim().isEmpty()) {
            def listOfJSONConfigurations = "[${params?.JSONConfigurations}]"

            JSONElement listOfConfigurations
            List<ReportConfiguration> reportConfigurationList
            try {
                listOfConfigurations = JSON.parse(listOfJSONConfigurations)
                reportConfigurationList = importService.importConfigurations(listOfConfigurations)
            } catch (ConverterException | GroovyCastException | MissingPropertyException e) {
                flash.error = message(code: "app.load.import.json.parse.fail")
                redirect(action: "load")
                return
            }

            List success = []
            List failed = []

            reportConfigurationList.each {
                String type = "Adhoc"
                if (it instanceof PeriodicReportConfiguration) {
                    type = "Aggregate"
                } else if (it instanceof IcsrReportConfiguration) {
                    type = "ICSR Report"
                } else if (it instanceof IcsrProfileConfiguration) {
                    type = "ICSR Profile"
                } else if (it instanceof Map) {
                    type = "Error"
                }
                if (!it.id) {
                    log.error("Failed to import ${it.reportName} ${it.errors?.allErrors}")
                    failed.add("${it.reportName} (${type})")
                } else {
                    log.info("Succefully imported configuration ${it.reportName} ${it.id} ")
                    success.add("${it.reportName} (${type})")
                }
            }
            if (success.size() > 0) {
                flash.message = message(code: "app.load.import.success", args: [success])
            }
            if (failed.size() > 0) {
                flash.error = message(code: "app.load.import.fail", args: [failed])
            }
            redirect(action: "index")
        } else {
            flash.warn = message(code: "app.load.import.noData")
            redirect(action: "load")
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
                footer                 : params.("templateQueries[" + i + "].footer") ?: null,
                title                  : params.("templateQueries[" + i + "].title") ?: null,
                headerProductSelection : params.("templateQueries[" + i + "].headerProductSelection") ?: false,
                headerDateRange        : params.("templateQueries[" + i + "].headerDateRange") ?: false,
                blindProtected         : params.("templateQueries[" + i + "].blindProtected") ?: false,
                privacyProtected       : params.("templateQueries[" + i + "].privacyProtected") ?: false,
                displayMedDraVersionNumber: params.("templateQueries[" + i + "].displayMedDraVersionNumber") ?: false,
                granularity               : params.("templateQueries[" + i + "].granularity"),
                issueType               : params.("templateQueries[" + i + "].issueType"),
                rootCause               : params.("templateQueries[" + i + "].rootCause"),
                responsibleParty        : params.("templateQueries[" + i + "].responsibleParty"),
                assignedToUser          : params.("templateQueries[" + i + "].assignedToUser")?params.("templateQueries[" + i + "].assignedToUser").split("_")[1]:null,
                assignedToGroup         : params.("templateQueries[" + i + "].assignedToGroup")?params.("templateQueries[" + i + "].assignedToGroup").split("_")[1]:null,
                priority               : params.("templateQueries[" + i + "].priority"),
                actions               : params.("templateQueries[" + i + "].actions"),
                summary               : params.("templateQueries[" + i + "].summary"),
                investigation               : params.("templateQueries[" + i + "].investigation"),
                investigationSql               : params.("templateQueries[" + i + "].investigationSql"),
                actionsSql               : params.("templateQueries[" + i + "].actionsSql"),
                summarySql               : params.("templateQueries[" + i + "].summarySql"),
                templtReassessDate     : params.("templateQueries[" + i + "].templtReassessDate"),
                reassessListednessDate : params.("templateQueries[" + i + "].reassessListednessDate")
        ]
        bindingMap
    }

    private void bindDmsConfiguration(Configuration configurationInstance, Map dmsConfiguration) {
        if (dmsConfiguration && dmsConfiguration.format) {
            DmsConfiguration dmsConfigurationInstance
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

    private void saveConfigurationMapToSession() {
        Map editingConfigurationMap = [configurationParams: (params as JSON).toString(), configurationId: params.id, action: params.id ? "edit" : "create", controller: "configuration", templateQueryIndex: params.templateQueryIndex]
        session.setAttribute("editingConfiguration", editingConfigurationMap)
    }

    private void initConfigurationFromMap(Configuration configurationInstance, Map map) {
        params.putAll(map)
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        populateModel(configurationInstance)
        configurationInstance.reportName = params.reportName
        configurationService.initConfigurationTemplatesFromSession(session, configurationInstance)
        configurationService.initConfigurationQueriesFromSession(session, configurationInstance)
        session.removeAttribute("editingConfiguration")
    }

    private User getCurrentUser()
    {
       return userService.getUser()
    }

    private validateTenant(Long tenantId){
        if(tenantId && (tenantId != (Tenants.currentId() as Long)) && !SpringSecurityUtils.ifAnyGranted("ROLE_DEV")){
            log.error("Request and Session tenant mismatch issue for User ${currentUser?.username} in ConfigurationController")
            return false
        }
        return true
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def updateAdhocAjaxCall() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        Configuration configurationInstance = Configuration.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        if (params.version && (configurationInstance.version > params.long('version'))) {
            responseDTO.setFailureResponse(message(code: 'app.configuration.update.lock.permission', args: [configurationInstance.reportName]).toString() as String)
        }else if(!validateTenant(params.long('tenantId'))){
            responseDTO.setFailureResponse(message(code: 'invalid.tenant').toString() as String)
        }else if (!params.reportName || !params.reportName.trim()) {
            responseDTO.setFailureResponse(message(code: 'com.rxlogix.config.Configuration.reportName.nullable').toString() as String)
        }else {
            configurationInstance.nextRunDate = null
            populateModel(configurationInstance)
            configurationInstance.reportName = params.reportName

            try {
                if (!userService.isAnyGranted("ROLE_QUALITY_CHECK")) {
                    configurationInstance.qualityChecked = false
                }
                configurationInstance = (Configuration) CRUDService.update(configurationInstance)
                responseDTO.additionalData = configurationInstance.version
            } catch (ValidationException ve) {
                responseDTO.additionalData = configurationInstance.version
                responseDTO.setFailureResponse(ve.errors)
            } catch (Exception ex) {
                responseDTO.additionalData = configurationInstance.version
                responseDTO.setFailureResponse(message(code: "default.server.error.message").toString())
            }
        }
        return render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def ajaxSaveAsAndRun() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        if(!validateTenant(params.long('tenantId'))){
            responseDTO.setFailureResponse(message(code: 'invalid.tenant').toString() as String)
            return render(responseDTO.toAjaxResponse())
        }
        Configuration configurationInstance = new Configuration()
        configurationInstance.setIsEnabled(true)
        populateModelForEditedConfig(configurationInstance, getCurrentUser())
        if(!configurationInstance.getReportName()) {
            responseDTO.setFailureResponse(message(code: 'com.rxlogix.config.Configuration.reportName.nullable').toString() as String)
        }else if(!configurationService.isUniqueName(configurationInstance.getReportName(),getCurrentUser())) {
            responseDTO.setFailureResponse(message(code: 'com.rxlogix.config.configuration.name.unique.per.user').toString() as String)
        }else {
            configurationInstance.setReportName(configurationInstance.getReportName())
            try {
                configurationInstance = (Configuration) CRUDService.save(configurationInstance)
            } catch (ValidationException ve) {
                responseDTO.setFailureResponse(ve.errors)
            } catch (Exception ex) {
                responseDTO.setFailureResponse(message(code: "default.server.error.message").toString())
            }
        }
        return render(responseDTO.toAjaxResponse())
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback
    private populateModelForEditedConfig(Configuration configurationInstance,User currUser) {
        //clearListFromConfiguration(configurationInstance)
        bindData(configurationInstance, params, [exclude: ["id", "templateQueries", "tags", "executableBy", "isEnabled", "asOfVersionDate", "includeLockedVersion", "emailConfiguration", "sharedWith", "dmsConfiguration"]])
        bindAsOfVersionDate(configurationInstance, params.asOfVersionDate)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        setAttributeTags(configurationInstance)
        assignParameterValuesToGlobalQuery(configurationInstance)
        bindNewTemplateQueriesForEditedConfig(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        configurationService.removeRemovedTemplateQueries(configurationInstance)
        bindEmailConfiguration(configurationInstance, params.emailConfiguration)
        configurationService.bindSharedWith(configurationInstance, params.list('sharedWith'), params.list('executableBy'), configurationInstance.id ? true : false)
        bindDmsConfiguration(configurationInstance, params.dmsConfiguration)
        bindReportTasks(configurationInstance, params)
        configurationService.checkProductCheckboxes(configurationInstance)
        configurationInstance.setCreatedBy(currUser.username)
        configurationInstance.setModifiedBy(currUser.username)
        configurationInstance.setOwner(currUser)
    }

    private bindNewTemplateQueriesForEditedConfig(Configuration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = 0; params.containsKey("templateQueries[" + i + "].id"); i++) {
            if(!params.boolean("templateQueries[" + i + "].dynamicFormEntryDeleted")) {
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

    def updateSectionAndRunAjax() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        ReportConfiguration configuration = ReportConfiguration.get(params.long("id"))
        if (!configuration) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.configurationType.ADHOC_REPORT'), params.id]) as String)
        } else if (configuration.nextRunDate && configuration.isEnabled) {
            responseDTO.setFailureResponse(message(code: 'app.configuration.run.exists') as String)
        } else {
            try {
                if (params.act in [CHART_WIDGET_ACTIONS.SAVE, CHART_WIDGET_ACTIONS.RUN]) {
                    params.each { k, v ->
                        if (k.indexOf(".qev[") > 0 && k.endsWith("].value")) {
                            if (v instanceof String[]) {
                                params[k] = v.join(";")
                            }
                        }

                    }
                    TemplateQuery templateQuery = configuration.templateQueries.find { it.id == params.long("sectionId") }
                    String queryId = params.("templateQueries[0].query")
                    templateQuery.query = queryId ? SuperQuery.get(queryId) : null
                    templateQuery.granularity = params.("templateQueries[0].granularity")
                    templateQuery.templtReassessDate = params.("templateQueries[0].templtReassessDate")
                    templateQuery.reassessListednessDate = params.("templateQueries[0].reassessListednessDate")
                    DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery
                    setDateRangeInformation(0, dateRangeInformationForTemplateQuery, configuration)
                    dateRangeInformationForTemplateQuery.templateQuery = templateQuery
                    if (params.("templateQueries[0].dateRangeInformationForTemplateQuery.relativeDateRangeValue") && params.("templateQueries[0].dateRangeInformationForTemplateQuery.relativeDateRangeValue") =~ "-?\\d+") {
                        dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[0].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer
                    }
                    assignParameterValuesToTemplateQuery(configuration, templateQuery, 0)
                }
                if (params.act in [CHART_WIDGET_ACTIONS.REFRESH, CHART_WIDGET_ACTIONS.RUN]) {
                    configuration.setIsEnabled(true)
                    configuration.setScheduleDateJSON(getRunOnceScheduledDateJson())
                    configuration.setNextRunDate(configurationService.getNextDate(configuration))
                }
                CRUDService.update(configuration)
            } catch (ValidationException e) {
                responseDTO.setFailureResponse(e.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    private void bindReportTasks(Configuration configurationInstance, params) {

        Set<ReportTask> reportTaskList = configurationInstance.reportTasks?.grep()
        reportTaskList.each {
            configurationInstance.removeFromReportTasks(it)
            it.delete()
        }
        List<ReportTask> taskList = taskTemplateService.fetchReportTasksFromRequest(params)
        taskList.each {
            configurationInstance.addToReportTasks(it)
        }
    }


    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_PVQ_EDIT'])
    def listPvqCfg() {
        List observations = []
        Map qualityTypes = qualityService.listTypes().collectEntries { [(it.value): it.label] }
        String defaultPvqType = qualityTypes.keySet()[0]
        Configuration.findAllByIsDeletedAndPvqTypeIsNotNull(false).each { Configuration cfg ->

            Map observation = [id             : cfg.id,
                               reportName     : cfg.reportName,
                               numOfExecutions: cfg.numOfExecutions,
                               pvqType        : cfg.pvqType?.split(";")?.collect { qualityTypes[it] ?: it }?.findAll { it }?.join(";"),
                               nextRunDate    : cfg.nextRunDate?.format(DateUtil.DATEPICKER_FORMAT_AM_PM) ?: "-",
                               state          : cfg.isEnabled ? ViewHelper.getMessage("app.configuration.autorca.state.run") : ViewHelper.getMessage("app.configuration.autorca.state.stop")
            ]
            ExecutionStatus executionStatus = ExecutionStatus.findByEntityId(cfg.id, [sort: "id", order: "desc"])
            if (executionStatus) {
                observation.laststate = executionStatus?.executionStatus?.value() ?: "-"
            } else {
                observation.laststate = "-"
            }
            observations << observation
        }
        [observations: observations, defaultPvqType: defaultPvqType]
    }

}

