package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang3.StringUtils

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class CaseSeriesController {

    def CRUDService
    def reportExecutorService
    def reportService
    def caseSeriesService
    def configurationService
    def userService

    static allowedMethods = [update: ['PUT','POST'],updateAndRun: 'POST', delete: ['DELETE','POST']]

    @Secured(['ROLE_CASE_SERIES_VIEW'])
    def index() {
        render(view: 'index')
    }

    /**
     * This action is responsible got rendering the create action.
     * @return
     */

    @Secured(['ROLE_CASE_SERIES_CRUD'])
    def create() {
        CaseSeries seriesInstance= new CaseSeries()
        render view: "create", model: [seriesInstance: seriesInstance, hasConfigTemplateCreatorRole: userService.getUser().isConfigurationTemplateCreator()]
    }

    /**
     * This action is responsible to show the case series.
     * @return
     */
    @Secured(['ROLE_CASE_SERIES_VIEW'])
    def show(Long id) {
        CaseSeries caseSeriesInstance = CaseSeries.read(id)
        if (!caseSeriesInstance) {
            notFound()
            return
        }

        if (!caseSeriesInstance.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }

        render view: "show", model: [seriesInstance: caseSeriesInstance,
                                     viewGlobalSql : params.getBoolean("viewSql") ? reportExecutorService.debugGlobalQuerySQL(caseSeriesInstance) : null]
    }


    /**
     * This action is responsible to edit the case series.
     * @return
     */
    @Secured(['ROLE_CASE_SERIES_CRUD'])
    def edit(Long id) {
        CaseSeries caseSeriesInstance = CaseSeries.read(id)
        if (!caseSeriesInstance) {
            notFound()
            return
        }
        if (!caseSeriesInstance.isEditable(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        if(caseSeriesInstance.running) {
            flash.warn = message(code: "app.caseSeries.running.fail", args: [caseSeriesInstance.seriesName])
            redirect(action: "index")
            return
        }
        caseSeriesInstance.scheduleDateJSON = configurationService.correctSchedulerJSONForCurrentDate(caseSeriesInstance.scheduleDateJSON, caseSeriesInstance.nextRunDate)
        render view: "edit", model: [seriesInstance: caseSeriesInstance, hasConfigTemplateCreatorRole: userService.getUser().isConfigurationTemplateCreator()]
    }

    @Secured(['ROLE_QUERY_VIEW'])
    def preview(Long selectedQuery) {
        SuperQuery query = SuperQuery.get(selectedQuery)
        if (!query) {
            flash.error = message(code: 'app.configuration.query.notFound', args: [params.selectedQuery])
            redirect(controller: "query", action: "index")
            return
        }
        render view: "preview", model: [seriesInstance: new CaseSeries(globalQuery: query, owner: userService.currentUser), globalQueryBlanks: query.hasBlanks]
    }

    /**
     * This action is responsible to save the case series.
     * @return
     */
    @Secured(['ROLE_CASE_SERIES_CRUD'])
    @Transactional
    def save() {
        if(request.method == 'GET') {
            notSaved()
            return
        }

        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'create')
            return
        }

        CaseSeries caseSeriesInstance = new CaseSeries()
        caseSeriesInstance.deliveryOption=new CaseDeliveryOption()
        caseSeriesInstance.setIsEnabled(false)
        caseSeriesInstance.nextRunDate = null
        populateModel(caseSeriesInstance)
        try {
            CRUDService.save(caseSeriesInstance)
            flash.message = message(code: 'default.created.message', args: [message(code: 'caseSeries.label'), caseSeriesInstance.seriesName])
            redirect(action: "show",id: caseSeriesInstance.id)
            return
        } catch (ValidationException ve) {
            log.warn("Validation Error during caseseries -> save")
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            log.error("Unexpected Error in caseseries -> save", ex)
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        render view: "create", model: [seriesInstance: caseSeriesInstance, qev: params.qev, asOfVersionDate: params.asOfVersionDate]
    }

    /**
     * This action is to update the case series.
     * @return
     */
    @Secured(['ROLE_CASE_SERIES_CRUD'])
    @Transactional
    def update(Long id) {
        CaseSeries caseSeriesInstance = CaseSeries.lock(id)
        if (!caseSeriesInstance) {
            notFound()
            return
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: caseSeriesInstance.id)
            return
        }

        if (!caseSeriesInstance.isEditable(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }

        if (params.version && (caseSeriesInstance.version > params.long('version'))) {
            flash.error = message(code: 'app.configuration.update.lock.permission', args: [caseSeriesInstance.seriesName])
            redirect(action: 'edit', id: caseSeriesInstance.id)
            return;
        }
        //Bind the case series instance.
        caseSeriesInstance.nextRunDate = null
        populateModel(caseSeriesInstance)
        try {
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                caseSeriesInstance.qualityChecked = false
            }
            CRUDService.update(caseSeriesInstance)
            flash.message = message(code: 'default.updated.message', args: [message(code: 'caseSeries.label'), caseSeriesInstance.seriesName])
            redirect(action: "show",id: caseSeriesInstance.id)
            return
        } catch (ValidationException ve) {
            log.warn("Validation Error during caseseries -> update")
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            log.error("Unexpected Error in caseseries -> update", ex)
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        render view: "edit", model: [seriesInstance: caseSeriesInstance, qev: params.qev, asOfVersionDate: params.asOfVersionDate]
    }

    @Secured(['ROLE_CASE_SERIES_CRUD'])
    @Transactional
    def saveAndRun() {
        if(request.method == 'GET') {
            notSaved()
            return
        }

        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'create')
            return
        }

        CaseSeries caseSeriesInstance = new CaseSeries()
        caseSeriesInstance.setIsEnabled(true)
        populateModel(caseSeriesInstance)
        try {
            CRUDService.save(caseSeriesInstance)

            flash.message = "${message(code: 'app.caseSeries.generation.progress', args: [caseSeriesInstance?.seriesName])}"
            redirect(controller: 'caseSeries', action: "index")
            return
        } catch (ValidationException ve) {
            log.warn("Validation Error during caseseries -> saveAndRun",ve)
            caseSeriesInstance.setIsEnabled(false)
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            log.error("Unexpected Error in caseseries -> saveAndRun", ex)
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        render view: "create", model: [seriesInstance: caseSeriesInstance, qev: params.qev, asOfVersionDate: params.asOfVersionDate]
    }

    @Secured(['ROLE_CASE_SERIES_CRUD'])
    @Transactional
    def updateAndRun(Long id) {
        CaseSeries caseSeriesInstance = CaseSeries.get(id)
        if (!caseSeriesInstance) {
            notFound()
            return
        }

        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'create')
            return
        }

        if (!caseSeriesInstance.isEditable(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        if (caseSeriesInstance.executing) {
            flash.warn = message(code: 'app.caseseries.run.exists', args: [caseSeriesInstance.seriesName])
            redirect(action: "index")
            return
        }
        caseSeriesInstance.setIsEnabled(true)
        populateModel(caseSeriesInstance)
        try {
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                caseSeriesInstance.qualityChecked = false
            }
            CRUDService.saveOrUpdate(caseSeriesInstance)
            flash.message = "${message(code: 'app.caseSeries.generation.progress', args: [caseSeriesInstance?.seriesName])}"
            redirect(controller: 'caseSeries', action: "index")
            return
        } catch (ValidationException ve) {
            log.warn("Validation Error during caseseries -> updateAndRun", ve)
            caseSeriesInstance.setIsEnabled(false)
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            log.error("Unexpected Error in caseseries -> updateAndRun", ex)
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        render view: "edit", model: [seriesInstance: caseSeriesInstance, qev: params.qev, asOfVersionDate: params.asOfVersionDate]

    }

    @Secured(['ROLE_CASE_SERIES_VIEW'])
    def runNow(Long id) {
        CaseSeries caseSeriesInstance = CaseSeries.get(id)
        if (!caseSeriesInstance) {
            notFound()
            return
        }
        if (!caseSeriesInstance.isEditable(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        if ((caseSeriesInstance.nextRunDate != null && caseSeriesInstance.isEnabled == true) || caseSeriesInstance.executing) {
            flash.warn = message(code: 'app.caseseries.run.exists', args: [caseSeriesInstance.seriesName])
            redirect(action: "index")
            return
        }
        try {
            caseSeriesInstance.setIsEnabled(true)
            caseSeriesInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
            caseSeriesInstance.setNextRunDate(null)
            caseSeriesInstance.setNextRunDate(configurationService.getNextDate(caseSeriesInstance))
            CRUDService.saveOrUpdate(caseSeriesInstance)
            flash.message = "${message(code: 'app.caseSeries.generation.progress', args: [caseSeriesInstance?.seriesName])}"
            redirect(controller: 'caseSeries', action: "index")
            return
        } catch (ValidationException ve) {
            log.warn("Validation Error during caseseries -> runNow",ve)
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            log.error("Unexpected Error in caseseries -> runNow",ex)
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        render view: "edit", model: [seriesInstance: caseSeriesInstance, qev: params.qev, asOfVersionDate: params.asOfVersionDate]
    }

    @Secured(['ROLE_QUERY_VIEW'])
    def updatePreview() {
        CaseSeries caseSeriesInstance = new CaseSeries(tenantId: Tenants.currentId() as Long)
        populateModel(caseSeriesInstance, true)
        CaseDeliveryOption caseExecutedDeliveryOption = new CaseDeliveryOption(sharedWith: [userService.currentUser])
        caseSeriesInstance.deliveryOption = caseExecutedDeliveryOption
        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.getByOriginalQueryId(caseSeriesInstance.globalQuery?.id, userService.currentUser).get()
        if (!executedCaseSeries) {
            executedCaseSeries = new ExecutedCaseSeries()
        }
        if (executedCaseSeries.executing) {
            flash.warn = message(code: 'app.query.preview.run.exists', args: [caseSeriesInstance.globalQuery?.name])
            redirect(controller: "query", action: "index")
            return
        }
        try {
            caseSeriesService.updateDetailsFrom(caseSeriesInstance, executedCaseSeries)
            caseSeriesService.setOwnerAndNameForPreview(executedCaseSeries)
            CRUDService.saveOrUpdate(executedCaseSeries)
            ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedCaseSeries.id, entityType: ExecutionStatus.getEntityTypeFromClass(executedCaseSeries.class), reportVersion: executedCaseSeries.numExecutions,
                    startTime: System.currentTimeMillis(), owner: executedCaseSeries.owner, reportName: executedCaseSeries.seriesName,
                    attachmentFormats: caseSeriesInstance?.deliveryOption?.attachmentFormats, sharedWith: caseSeriesInstance?.allSharedUsers, tenantId: caseSeriesInstance?.tenantId)
            executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
            executionStatus.frequency = FrequencyEnum.RUN_ONCE
            executionStatus.nextRunDate = new Date()
            CRUDService.instantSaveWithoutAuditLog(executionStatus)
            AuditLogConfigUtil.logChanges(executedCaseSeries,getExecutedCaseSeriesMap(executedCaseSeries),[:],Constants.AUDIT_LOG_INSERT)
            flash.message = "${message(code: 'app.preview.generation', args: [executedCaseSeries?.seriesName])}"
            redirect(controller: "query", action: "index")
            return
        } catch (ValidationException ve) {
            log.warn("Validation Error during caseseries -> updatePreview",ve)
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            ex.printStackTrace()
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        render view: "preview", model: [seriesInstance: caseSeriesInstance, qev: params.qev, asOfVersionDate: params.asOfVersionDate]

    }

    def disable() {
        CaseSeries caseSeries = CaseSeries.get(params.id)
        if (!caseSeries) {
            notFound()
            return
        }

        if (!caseSeries.isEditableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(view: "index")
            return
        }

        if (!caseSeries.executing) {
            caseSeries.setIsEnabled(false)
            caseSeries.setScheduleDateJSON(caseSeriesService.getScheduledDateJsonAfterDisable(caseSeries))

            try {
                caseSeries = (CaseSeries) CRUDService.update(caseSeries)
            } catch (ValidationException ve) {
                CaseSeries originalCaseSeries = CaseSeries.get(caseSeries.id)
                populateModel(originalCaseSeries)
                caseSeries.seriesName = params.seriesName
                originalCaseSeries.errors = ve.errors
                render view: "edit", model: [seriesInstance: originalCaseSeries, hasConfigTemplateCreatorRole: userService.getUser().isConfigurationTemplateCreator()]
                return
            }
        } else {
            flash.error = message(code: 'app.caseSeries.unscheduled.fail', args: [caseSeries.seriesName])
            render view: 'index'
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'caseSeries.label'), caseSeries.seriesName])
                redirect(action: "show", id: caseSeries.id)
            }
            '*' { respond caseSeries, [status: OK] }
        }
    }


    def previewCrosstabCases(ReportResult reportResult) {
        User currentUser = userService.currentUser
        String reportName = reportResult.executedTemplateQuery.executedConfiguration.reportName
        def headerSetting = JSON.parse(reportResult.data.crossTabHeader)
        String caseSeriesName = reportName + " - " + headerSetting.find { it."${params.columnName}" }."${params.columnName}".trim().split('\n\r').first()
        CaseSeries caseSeriesInstance = new CaseSeries(
                seriesName: caseSeriesName,
                owner: currentUser,
                tenantId: Tenants.currentId() as Long
        )
        populateModel(caseSeriesInstance, true)
        CaseDeliveryOption caseExecutedDeliveryOption = new CaseDeliveryOption(sharedWith: [currentUser])
        caseSeriesInstance.deliveryOption = caseExecutedDeliveryOption
        //To solve issue of multiple concurrent user use case we will create temp caseSeries per owner but still issue would be there in multiple tab.
        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.fetchTemporaryCaseSeriesFor(caseSeriesInstance).get()
        boolean refreshCases = true
        if (!executedCaseSeries) {
            executedCaseSeries = new ExecutedCaseSeries()
            refreshCases = false
        }
        String label = null
        try {
            ReportResultData reportResultData = reportResult.data
            Map<String, ?> dataRow = reportService.getOutputJSONROW(reportResultData, params.int("rowId"))
            //IMPT Used Description for label name transfer in case of drilldown.

            caseSeriesInstance.description = getReadableLabelName(dataRow, params.columnName, reportResultData.crossTabHeader)
            List<Tuple2<String, String>> caseIds = reportService.getCrosstabCaseIds(dataRow, params.columnName)
            executedCaseSeries.isTemporary = true
            caseSeriesService.updateDetailsFrom(caseSeriesInstance, executedCaseSeries)
            CRUDService.saveOrUpdate(executedCaseSeries)
            boolean isDrillDownToCaseList = caseSeriesService.isDrillDownToCaseList(reportResult)
            int count = params.getInt("count", -1)
            reportExecutorService.generateCaseSeriesByCaseCommandList(executedCaseSeries, caseIds, count, refreshCases, isDrillDownToCaseList)
        } catch (ValidationException ve) {
            log.error("Validation error while generating Temporary CaseSeries:" + ve.message)
            caseSeriesInstance.errors = ve.errors
        } catch (Exception ex) {
            log.error("Error while generating Temporary CaseSeries", ex)
            flash.error = "${message(code: 'app.label.caseSeries.save.exception')}"
        }
        String filePostFix = "${params.rowId}_${params.columnName?.replaceAll(' ', '-')}"
        chain(controller: "caseList", action: "index", params: [cid: executedCaseSeries.id, detailed: true, filePostfix: filePostFix, parentId: reportResult.id])
    }

    private String getReadableLabelName(Map<String, ?> dataRow, String columnName, String headerJSON) {
        try {
            String rowName = "ROW_1" //always first
            def headerSetting = JSON.parse(headerJSON)
            String label = headerSetting.find { it."${rowName}" }?."${rowName}"?.trim() ?: ''
            label += dataRow.get(rowName) ? " ( ${dataRow.get(rowName)} ) : " : ''
            label += "${headerSetting.find { it."${columnName}" }."${columnName}".trim().split('\n\r').first()}"
            return StringUtils.left(StringEscapeUtils.unescapeHtml(label), grailsApplication.config.getProperty('caseSeries.drilldown.label.length',Integer,180));
        } catch (Exception ex) {
            log.error("Exception while finding temp caseseries lable info from getReadableLabelName ", ex)
        }
        return null
    }

    /**
     * This action is responsible to delete the case series.
     * @return
     */
    @Secured(['ROLE_CASE_SERIES_CRUD'])
    def delete(Long id) {
        CaseSeries caseSeriesInstance = CaseSeries.get(id)
        if (!caseSeriesInstance) {
            notFound()
            return
        }
        if (!caseSeriesInstance.isEditable(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        try {
            CRUDService.softDelete(caseSeriesInstance, caseSeriesInstance.seriesName, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'caseSeries.label', default: 'Case Series'), caseSeriesInstance.seriesName])}"
            redirect(action: "index")
        } catch (ValidationException ve) {
            flash.error = "Unable to delete case series"
            redirect(action: "index")
        }
    }

    @Secured(['ROLE_CASE_SERIES_CRUD'])
    def copy(CaseSeries originalCaseSeries) {
        if (!originalCaseSeries) {
            notFound()
            return
        }

        if (!originalCaseSeries.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }

        CaseSeries savedCaseSeries = caseSeriesService.copyCaseSeries(originalCaseSeries)
        if (savedCaseSeries.hasErrors()) {
            chain(action: "index", model: [theInstance: savedCaseSeries])
        } else {
            flash.message = message(code: "app.copy.success", args: [savedCaseSeries.seriesName])
            redirect(action: "show", id: savedCaseSeries.id)
        }
    }

    def favorite() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        CaseSeries caseSeries = params.id ? CaseSeries.get(params.id) : null
        if (!caseSeries) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.case.series'), params.id]) as String)
        } else {
            try {
                caseSeriesService.setFavorite(caseSeries, params.boolean("state"))
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private populateModel(CaseSeries caseSeriesInstance, boolean preview = false) {
        clearListFromCaseSeries(caseSeriesInstance)
        //Do not bind in any other way because of the clone contained in the params
        bindData(caseSeriesInstance, params, [exclude: ["isEnabled", "asOfVersionDate", "includeLockedVersion", "globalQueryValueLists","tags"]])
        bindAsOfVersionDate(caseSeriesInstance, params.asOfVersionDate)
        setNextRunDateAndScheduleDateJSON(caseSeriesInstance)

        caseSeriesInstance.locale = userService.currentUser.preference.locale
        assignParameterValuesToGlobalQuery(caseSeriesInstance, preview)
        bindEmailConfiguration(caseSeriesInstance, params.emailConfiguration)
        bindSharedWith(caseSeriesInstance, params.list('sharedWith'), caseSeriesInstance.id ? true : false)
        setAttributeTags(caseSeriesInstance)
        if (caseSeriesInstance.includeWHODrugs) {
            caseSeriesInstance.isMultiIngredient = true
        }
    }

    private clearListFromCaseSeries(CaseSeries caseSeries) {
        caseSeries?.deliveryOption?.emailToUsers?.clear()
        caseSeries?.deliveryOption?.attachmentFormats?.clear()
        caseSeries?.tags?.clear()
        return caseSeries
    }


    private void setNextRunDateAndScheduleDateJSON(CaseSeries caseSeriesInstance) {
        caseSeriesInstance.nextRunDate = null
        if (caseSeriesInstance.scheduleDateJSON && caseSeriesInstance.isEnabled) {
            if (MiscUtil.validateScheduleDateJSON(caseSeriesInstance.scheduleDateJSON)) {
                caseSeriesInstance.nextRunDate = configurationService.getNextDate(caseSeriesInstance)
                return
            }
        }
        caseSeriesInstance.nextRunDate = null
    }

    private String getRunOnceScheduledDateJson() {
        User user = userService.currentUser
        def startupTime = (RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(user, DateUtil.JSON_DATE))
        def timeZone = DateUtil.getTimezoneForRunOnce(user)
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }

    private clearProperties(CaseSeries caseSeriesInstance){
        caseSeriesInstance?.deliveryOption?.emailToUsers?.clear()
        caseSeriesInstance?.deliveryOption?.attachmentFormats?.clear()
    }
    private bindEmailConfiguration(CaseSeries caseSeriesInstance, Map emailConfiguration) {

        if (emailConfiguration && emailConfiguration.subject && emailConfiguration.body) {
            EmailConfiguration emailInstance
            if (caseSeriesInstance.emailConfiguration) {
                emailInstance = caseSeriesInstance.emailConfiguration
                emailInstance.isDeleted = false
                bindData(emailInstance, emailConfiguration)
                if (!emailConfiguration.containsKey("cc")) {
                    emailInstance.cc = null
                }
                CRUDService.update(emailInstance)
            } else {
                emailInstance = new EmailConfiguration(emailConfiguration)
                CRUDService.save(emailInstance)
                caseSeriesInstance.emailConfiguration = emailInstance
            }
        } else {
            caseSeriesInstance.emailConfiguration=null
            if (caseSeriesInstance.emailConfigurationId) {
                CRUDService.delete(caseSeriesInstance.emailConfiguration)
            }
        }
    }

    private void bindSharedWith(CaseSeries caseSeriesInstance, List<String> sharedWith, Boolean isUpdate = false){
        List<User> allowedUsers = userService.getAllowedSharedWithUsersForCurrentUser();
        List<UserGroup> allowedGroups = userService.getAllowedSharedWithGroupsForCurrentUser();

        if (isUpdate) {
            if (caseSeriesInstance.getShareWithUsers()) {
                allowedUsers.addAll(caseSeriesInstance.getShareWithUsers())
                allowedUsers.unique { it.id }
            }
            if (caseSeriesInstance.getShareWithGroups()) {
                allowedGroups.addAll(caseSeriesInstance.getShareWithGroups())
                allowedGroups.unique { it.id }
            }
            caseSeriesInstance?.deliveryOption?.sharedWith?.clear()
            caseSeriesInstance?.deliveryOption?.sharedWithGroup?.clear()
        }
        
        if (sharedWith) {
            sharedWith.each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup && allowedGroups.find{it.id==userGroup.id}) {
                        caseSeriesInstance.deliveryOption.addToSharedWithGroup(userGroup)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    if (user && allowedUsers.find{it.id==user.id}) {
                        caseSeriesInstance.deliveryOption.addToSharedWith(user)
                    }
                }
            }
        }
    }

    private bindAsOfVersionDate(CaseSeries caseSeriesInstance, def asOfDate) {
        if (!(caseSeriesInstance?.evaluateDateAs in [EvaluateCaseDateEnum.LATEST_VERSION, EvaluateCaseDateEnum.ALL_VERSIONS])) {
            caseSeriesInstance.includeLockedVersion = true
        } else {
            caseSeriesInstance.includeLockedVersion = params.includeLockedVersion ?: false
        }
        if (caseSeriesInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            Locale locale = userService.currentUser?.preference?.locale
            caseSeriesInstance.asOfVersionDate = DateUtil.getEndDate(asOfDate, locale)
        } else {
            caseSeriesInstance.asOfVersionDate = null
        }
    }

    private setAttributeTags(CaseSeries caseSeries) {
        caseSeries?.tags?.clear()
        if (params?.tags) {
            if (params.tags.class == String) {
                params.tags = [params.tags]
            }
            List updatedTags = params.tags
            updatedTags.unique().each {
                Tag tag = Tag.findByName(it)
                if (!tag) {
                    tag = new Tag(name: it).save()
                }
                caseSeries.addToTags(tag)
            }
        }
    }

    private void assignParameterValuesToGlobalQuery(CaseSeries caseSeries, boolean preview) {
        if (caseSeries.globalQueryValueLists) {
            params.put("oldglobalQueryValueLists${caseSeries.id}", caseSeries.globalQueryValueLists.toString())
        }
        CaseSeriesDateRangeInformation caseSeriesDateRangeInformation = caseSeries.caseSeriesDateRangeInformation
        if (!caseSeriesDateRangeInformation) {
            caseSeriesDateRangeInformation = new CaseSeriesDateRangeInformation()
            caseSeries.caseSeriesDateRangeInformation = caseSeriesDateRangeInformation
        }
        bindData(caseSeriesDateRangeInformation, params.caseSeriesDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        if (!DateRangeEnum.relativeDateOperatorsWithX.contains(caseSeriesDateRangeInformation.dateRangeEnum)) {
            caseSeriesDateRangeInformation.relativeDateRangeValue = 1
        }
        if (caseSeriesDateRangeInformation.dateRangeEnum != DateRangeEnum.CUSTOM && caseSeriesDateRangeInformation.dateRangeEnum != DateRangeEnum.CUMULATIVE) {
            def startAndEndDate = RelativeDateConverter.(caseSeriesDateRangeInformation.dateRangeEnum.value())(new Date(), caseSeriesDateRangeInformation.relativeDateRangeValue ?: 1, 'UTC')
            caseSeriesDateRangeInformation.dateRangeStartAbsolute = startAndEndDate[0]
            caseSeriesDateRangeInformation.dateRangeEndAbsolute = startAndEndDate[1]
        } else if (caseSeriesDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM) {
            caseSeriesDateRangeInformation.dateRangeStartAbsolute = DateUtil.getStartDate(params.caseSeriesDateRangeInformation.dateRangeStartAbsolute, caseSeries.locale)
            caseSeriesDateRangeInformation.dateRangeEndAbsolute = DateUtil.getEndDate(params.caseSeriesDateRangeInformation.dateRangeEndAbsolute, caseSeries.locale)
        } else {
            caseSeriesDateRangeInformation.dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
            caseSeriesDateRangeInformation.dateRangeEndAbsolute = caseSeriesDateRangeInformation?.asOfVersionDate ?: new Date()
        }
        caseSeriesDateRangeInformation.caseSeries = caseSeries
        caseSeries.globalQueryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        Set<ParameterValue> poiParametersValues = []
        caseSeries.globalQueryValueLists = []
        if (params.containsKey("qev[0].key")) {

            // for each single query
            int start = 0
            params.("validQueries").split(",").each { queryId -> // if query set
                QueryValueList queryValueList = new QueryValueList(query: queryId)

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
                        if (!poiParametersValues*.key?.contains(specialKeyValue)) {
                            poiParametersValues.add(new ParameterValue(key: specialKeyValue, value: value, isFromCopyPaste: isFromCopyPaste))
                        } else if (poiParametersValues*.key?.contains(specialKeyValue)) {
                            ParameterValue parameterValue = poiParametersValues.find { it.key == specialKeyValue }
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
                    queryValueList.addToParameterValues(tempValue)
                }
                start += size
                caseSeries.addToGlobalQueryValueLists(queryValueList)
            }
        }
    }

    private validateTenant(Long tenantId){
        if(tenantId && (tenantId != (Tenants.currentId() as Long)) && !SpringSecurityUtils.ifAnyGranted("ROLE_DEV")){
            log.error("Request and Session tenant mismatch issue for User ${currentUser?.username} in CaseSeriesController")
            return false
        }
        return true
    }

    private User getCurrentUser()
    {
        return userService.getUser()
    }


    private notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.case.series'), params.id])
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

    private Map getExecutedCaseSeriesMap(ExecutedCaseSeries executedCaseSeries) {
        Map resultMap = [:]
        resultMap['seriesName'] = executedCaseSeries.seriesName
        resultMap['description'] = executedCaseSeries.description
        resultMap['tags'] = executedCaseSeries.tags
        resultMap['nextRunDate'] = executedCaseSeries.nextRunDate
        resultMap['scheduleDateJSON'] = executedCaseSeries.scheduleDateJSON
        resultMap['isEnabled'] = executedCaseSeries.isEnabled
        resultMap['configSelectedTimeZone'] = executedCaseSeries.configSelectedTimeZone
        resultMap['dateRangeType'] = executedCaseSeries.dateRangeType
        resultMap['asOfVersionDate'] = executedCaseSeries.asOfVersionDate
        resultMap['evaluateDateAs'] = executedCaseSeries.evaluateDateAs
        resultMap['excludeFollowUp'] = executedCaseSeries.excludeFollowUp
        resultMap['includeLockedVersion'] = executedCaseSeries.includeLockedVersion
        resultMap['includeAllStudyDrugsCases'] = executedCaseSeries.includeAllStudyDrugsCases
        resultMap['excludeNonValidCases'] = executedCaseSeries.excludeNonValidCases
        resultMap['excludeDeletedCases'] = executedCaseSeries.excludeDeletedCases
        resultMap['suspectProduct'] = executedCaseSeries.suspectProduct
        resultMap['qualityChecked'] = executedCaseSeries.qualityChecked
        resultMap['productSelection'] = executedCaseSeries.productSelection
        resultMap['productGroupSelection'] = executedCaseSeries.productGroupSelection
        resultMap['studySelection'] = executedCaseSeries.studySelection
        resultMap['eventSelection'] = executedCaseSeries.eventSelection
        resultMap['eventGroupSelection'] = executedCaseSeries.eventGroupSelection
        resultMap['owner'] = executedCaseSeries.owner
        resultMap['numExecutions'] = executedCaseSeries.numExecutions
        resultMap['locale'] = executedCaseSeries.locale
        resultMap['isDeleted'] = executedCaseSeries.isDeleted
        resultMap['isMultiIngredient'] = executedCaseSeries.isMultiIngredient
        resultMap['tenantId'] = executedCaseSeries.tenantId
        resultMap['isTemporary'] = executedCaseSeries.isTemporary
        resultMap['caseSeriesOwner'] = executedCaseSeries.caseSeriesOwner
        resultMap['executedCaseSeriesDateRangeInformation'] = executedCaseSeries.executedCaseSeriesDateRangeInformation
        resultMap['executedGlobalQueryValueLists'] = executedCaseSeries.executedGlobalQueryValueLists
        resultMap['emailConfiguration'] = executedCaseSeries.emailConfiguration
        resultMap['isSpotfireCaseSeries'] = executedCaseSeries.isSpotfireCaseSeries
        resultMap['associatedSpotfireFile'] = executedCaseSeries.associatedSpotfireFile
        resultMap['includeWHODrugs'] = executedCaseSeries.includeWHODrugs
        return resultMap
    }
}
