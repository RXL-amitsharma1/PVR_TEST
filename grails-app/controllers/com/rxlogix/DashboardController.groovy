package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.json.JsonOutput
import com.rxlogix.localization.ReleaseNotesNotifier
import com.rxlogix.localization.SystemNotification
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SecurityUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import net.sf.json.groovy.JsonSlurper
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.Cookie

@Secured(["isAuthenticated()"])
@Slf4j
class DashboardController {
    private static final int WIDGET_WIDTH = 4
    private static final int REPORTS_TABLE_WIDGET_WIDTH = 6
    private static final int ACTION_TABLE_WIDGET_WIDTH = 6
    private static final int AGGREGATE_REPORTS_SUMMARY_WIDTH = 3
    private static final int SUMMARY_WIDTH = 2
    private static final int REPORT_REQUEST_WIDGET_WIDTH = 6
    private static final int ICSR_TRACKING_TABLE_WIDGET_WIDTH = 12
    private static final int CHART_WIDGET_WIDTH = 6

    private static final int CALENDAR_WIDGET_HEIGHT = 6
    private static final int SUMMARY_HEIGHT = 3
    private static final int WIDGET_HEIGHT = 4
    private static final int REPORT_REQUEST_WIDGET_HEIGHT = 6
    private static final int QUALITY_WIDGET_HEIGHT = 5
    private static final int CHART_WIDGET_HEIGHT = 8


    def userService
    def dashboardService
    def templateService
    def dynamicReportService
    def etlJobService
    def CRUDService
    def commentService
    def spotfireService
    def messageSource
    grails.core.GrailsApplication grailsApplication

    // This is here redirect the / URL, this way the address bar shows the URL instead of /
    def home() {
        redirect(action: "index")
    }

    def latest() {}

    def index() {
        String title = ViewHelper.getMessage("app.dashboard.title")
        if (session.module == "pvp") params.pvp = true
        if (session.module == "pvq") params.pvq = true
        Dashboard dashboard = dashboardService.getDashboard(params, request)
        if (isAccessRestricted(dashboard)) {
            redirect(controller: 'errors',action: 'forbidden')
            return
        }
        if(dashboard.label==messageSource.getMessage("app.label.dashboard.main", null, Locale.ENGLISH) || dashboard.label==messageSource.getMessage("app.label.dashboard.main", null, Locale.JAPANESE)){
            dashboard.label= ViewHelper.getMessage("app.label.dashboard.main")
        }
        Set<WidgetTypeEnum> accessOnWidgetTypes = []
        if (ViewHelper.isPvqModule(request) || (session.module == "pvq")) {
            title = ViewHelper.getMessage("app.quality.dashboard.title")
            if (SpringSecurityUtils.ifAnyGranted("ROLE_PERIODIC_CONFIGURATION_VIEW,ROLE_CONFIGURATION_VIEW")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.CHART)
            }
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_ACTION_ITEMS)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_ACTION_ITEMS_SUMMARY)
            accessOnWidgetTypes.add(WidgetTypeEnum.CASE_COUNT_BY_ERROR)
            accessOnWidgetTypes.add(WidgetTypeEnum.SUBMISSION_COUNT_BY_ERROR)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_CASE_COUNT)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_PRODUCT_COUNT)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_ENTRYSITE_COUNT)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_ERROR_COUNT)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_LATEST_ISSUES)
            accessOnWidgetTypes.add(WidgetTypeEnum.QUALITY_CASE_REPORT_TYPE)
            accessOnWidgetTypes.add(WidgetTypeEnum.ACTION_PLAN_PVQ)
        } else {

            if (SpringSecurityUtils.ifAnyGranted("ROLE_PERIODIC_CONFIGURATION_VIEW,ROLE_CONFIGURATION_VIEW")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.CHART)
                accessOnWidgetTypes.add(WidgetTypeEnum.LAST_REPORTS)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_VIEW")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.ADHOC_REPORTS_SUMMARY)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_PERIODIC_CONFIGURATION_VIEW")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_ACTION_ITEM")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.ACTION_ITEMS)
                accessOnWidgetTypes.add(WidgetTypeEnum.ACTION_ITEMS_SUMMARY)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_REPORT_REQUEST_VIEW")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.REPORT_REQUEST_SUMMARY)
                accessOnWidgetTypes.add(WidgetTypeEnum.ADVANCED_REPORT_REQUEST)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_CALENDAR")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.CALENDAR)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_PERIODIC_CONFIGURATION_CRUD")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.ADVANCED_PUBLISHER)
                accessOnWidgetTypes.add(WidgetTypeEnum.COMPLIANCE_PUBLISHER)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_PERIODIC_CONFIGURATION_CRUD, ROLE_CONFIGURATION_CRUD")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.CONFIGURATIONS)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_DATA_ANALYSIS")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.SPOTFIRE)
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_ICSR_PROFILE_EDITOR,ROLE_ICSR_PROFILE_VIEWER") && grailsApplication.config.getProperty('show.xml.option', Boolean) && grailsApplication.config.getProperty('icsr.profiles.execution', Boolean)) {
                accessOnWidgetTypes.add(WidgetTypeEnum.ICSR_TRACKING)
            }
            if (!SourceProfile.findByIsCentral(true)?.sourceName?.equals("PVCM")) {
                accessOnWidgetTypes.add(WidgetTypeEnum.ETL)
            }
        }
        if (ViewHelper.isPvcModule(request)) {
            title = ViewHelper.getMessage("app.central.dashboard.title")
            accessOnWidgetTypes.add(WidgetTypeEnum.ACTION_PLAN_PVC)
        }
        def widgets = dashboard.widgets.findAll {
            it.widgetType in accessOnWidgetTypes
        }

        User user = userService.currentUser

        widgets = widgets.collect { widget ->
            getWidgetData(widget, user)
        }

        if(session.module) session.removeAttribute("module")
        if(params.pvp )title = ViewHelper.getMessage("app.label.pv.publisher")
        Map model = [title: title, related: "home", dashboard: dashboard, widgets: widgets, workFlowStates: WorkflowState.allWorkFlowStatesForAdhoc]
        User currentUser = userService.currentUser
        if (!session.remindLater) {
            ReleaseNotesNotifier r = ReleaseNotesNotifier.findByUser(currentUser)
            if (!r) model.showVersionModal = true
            model.systemNotificationList = SystemNotification.fetchNew(currentUser)?.sort{it.id}
        }

        if (dashboard.widgets.find { it.widgetType == WidgetTypeEnum.SPOTFIRE }) {
            String username = currentUser.username ?: ""
            String secret = Holders.config.getProperty('spotfire.token_secret')
            String token = SecurityUtil.encrypt(secret, username)
            spotfireService.addAuthToken(token, username, currentUser.fullName, currentUser.email)
            response.addCookie(new Cookie("pvr-spotfire-cookie", System.currentTimeMillis().toString()))
            model.putAll([
                    user_name      : spotfireService.getHashedValue(username),
                    libraryRoot    : Holders.config.getProperty('spotfire.libraryRoot'),
                    wp_url         : DataAnalysisController.composeSpotfireUrl(),
                    auth_token     : token,
                    callback_server: Holders.config.getProperty('spotfire.callbackUrl')])
        }
        render(view: "index", model:model )
    }

    def getDashboardAjax() {
        Dashboard dashboard = Dashboard.get(params.id as Long)
        if (isAccessRestricted(dashboard)) {
            render message(code: "error.403.message")
        }
        else {
            render dashboard.toMap() as JSON
        }
    }

    def newDashboard() {
        String controllerName = params.controller
        if(ViewHelper.isPvqModule(request)){
            controllerName = "quality"
        }else if(ViewHelper.isPvcModule(request)){
            controllerName = "central"
        }
        Dashboard dashboard = new Dashboard(
                owner: userService.currentUser,
                label: g.message(code: 'app.label.dashboard.new'),
                dashboardType: (ViewHelper.checkPVModule(request)),
                parentId: params.id
        )
        dashboard = CRUDService.save(dashboard)
        redirect action: 'index', controller: controllerName, params: [id: dashboard.id]
    }

    def removeDashboard() {
        String controllerName = params.controller
        if(ViewHelper.isPvqModule(request)){
            controllerName = "quality"
        }else if(ViewHelper.isPvcModule(request)){
            controllerName = "central"
        }
        Dashboard dashboard = Dashboard.get(params.id)
        CRUDService.softDelete(dashboard, dashboard.label, params.deleteJustification)
        redirect action: 'index', controller: controllerName, params: []
    }

    def getAdhocSummary() {
        def result = [:]
        def currentUser = userService.getCurrentUser()
        Notification.findAllByUser(currentUser).each {
            if (it.appName == NotificationApp.ADHOC_REPORT) {
                if (it.message == "app.notification.completed") result["new"] ? result["new"]++ : result << ["new": 1]
                if (it.message == "app.notification.failed") result["error"] ? result["error"]++ : result << ["error": 1]
            }
        }
        List states = ExecutedConfiguration.getStates(currentUser)
        result << [states: states]
        result << [total: ExecutedConfiguration.countAllBySearchStringAndStatusInList(new LibraryFilter(user:currentUser, forPvq: false, includeArchived: false)).get()]

        render([result: result] as JSON)
    }

    def getAggregateSummary() {
        def result = [:]
        User currentUser = userService.getUser()
        Notification.findAllByUser(currentUser).each {
            if (it.appName == NotificationApp.AGGREGATE_REPORT) {
                if (it.message == "app.notification.completed") result["new"] ? result["new"]++ : result << ["new": 1]
                if (it.message == "app.notification.failed") result["error"] ? result["error"]++ : result << ["error": 1]
            }
        }
        StringBuilder selectQuery = getQueryForAggregateSummary(currentUser)
        Map queryParams = [
                scheduledStatus: ReportExecutionStatusEnum.SCHEDULED,
                submissionNotRequiredStatus: ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED,
                now: new Date(),
                dueSoonThreshold: new Date() + 30,
                submittedRecentlyThreshold: new Date() - 30,
                statuses: ReportExecutionStatusEnum.reportsListingStatuses,
                currentUser: currentUser
        ]
        if (!currentUser.isAdmin()) {
            queryParams.userId = currentUser.id
            queryParams.userGroupIds = UserGroup.fetchAllUserGroupByUser(currentUser)*.id
        }
        def queryResult = ExecutedPeriodicReportConfiguration.executeQuery(selectQuery, queryParams)
        result << [
                total            : queryResult[0][0] as Long,
                pending          : queryResult[0][1] as Long,
                scheduled        : queryResult[0][2] as Long,
                overDue          : queryResult[0][3] as Long,
                dueSoon          : queryResult[0][4] as Long,
                submittedRecently: queryResult[0][5] as Long
        ]
        List states = ExecutedPeriodicReportConfiguration.getStates(currentUser)
        result << [states: states]
        render([result: result] as JSON)
    }

    def getActionItemSummary() {
        def result = [:]
        def currentUser = userService.getUser()
        Notification.findAllByUser(currentUser).each {
            if (it.message == "app.notification.actionItem.assigned") result["new"] ? result["new"]++ : result << ["new": 1]
        }
        result << ActionItem.getSummary(currentUser)
        render([result: result] as JSON)
    }

    def getReportRequestSummary() {
        def result = [:]
        def currentUser = userService.getUser()
        Notification.findAllByUser(currentUser).each {
            if (it.message == "app.notification.reportRequest.assigned") result["new"] ? result["new"]++ : result << ["new": 1]
        }
        result << ReportRequest.getSummary(currentUser)
        render([result: result] as JSON)
    }

    def getAdvancedPublisher() {
        def result = [:]
        def statuses = []
        List<WorkflowState> states = WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.PERIODIC_REPORT)
        states.each {
            statuses << [title: it.name, id: it.id]
        }
        result << [status: statuses]
        render([result: result] as JSON)
    }

    def getCompliancePublisher() {
        def result = [:]
        def statuses = []
        List<WorkflowState> states = WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.PERIODIC_REPORT)
        states.each {
            statuses << [title: it.name, id: it.id]
        }
        result << [status: statuses]
        render([result: result] as JSON)
    }

    def getAdvancedReportRequest() {
        def result = [:]
        def currentUser = userService.getUser()
        Notification.findAllByUser(currentUser).each {
            if (it.message == "app.notification.reportRequest.assigned") result["new"] ? result["new"]++ : result << ["new": 1]
        }
        def statuses=[]
        List<WorkflowState> states = WorkflowState.findAllByIsDeleted(false).sort { it.name }
        states.each{
            Integer count = ReportRequest.fetchByWorkflowState(it,currentUser).count()
            if(count>0){
                statuses<<[title:it.name, id:it.id, count:count]
            }
        }
        result<<[status:statuses]
        def user=[:]
        List<UserGroup> currentUserGroups = UserGroup.fetchAllUserGroupByUser(currentUser)
        user.owner=ReportRequest.countByOwnerAndIsDeleted(currentUser,false)
        user.assigned=ReportRequest.countByAssignedToAndIsDeleted(currentUser,false)
        user.assignedGroup = currentUserGroups ? ReportRequest.countByAssignedGroupToInListAndIsDeleted(currentUserGroups, false) : 0
        user.requested=ReportRequest.createCriteria().list {
                projections {
                    distinct('id')
                }
                eq('isDeleted',false)
                requesters {
                    eq('id',currentUser.id)
                }
        }.size()
        user.requestedGroup=ReportRequest.createCriteria().list {
                projections {
                    distinct('id')
                }
                eq('isDeleted',false)
                requesterGroups {
                    'in'('id',currentUserGroups?.id?:[0L])
                }
        }.size()
        result<<[user:user]

        List priority = []
        ReportRequest.fetchByPriority(currentUser).list().each {
            priority << [title: it[1], id: it[0], count: it[2]]
        }
        result<<[priority:priority]

        result << [due:ReportRequest.getSummary(currentUser)]
        render([result: result] as JSON)
    }

    def addReportWidget() {
        Dashboard dashboard = dashboardService.getDashboard(params, request)
        if (isAccessRestricted(dashboard)) {
            render message(code: "error.403.message")
            return
        }
        WidgetTypeEnum widgetType = WidgetTypeEnum.valueOf(params.widgetType)
        ReportConfiguration reportConfiguration = params.chartId ? ReportConfiguration.get(params.chartId) : null
        def widgetWidth = {
            switch (it) {
                case WidgetTypeEnum.LAST_REPORTS:
                case WidgetTypeEnum.CONFIGURATIONS:
                    return REPORTS_TABLE_WIDGET_WIDTH
                case WidgetTypeEnum.ACTION_ITEMS:
                    return ACTION_TABLE_WIDGET_WIDTH
                case WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY:
                    return AGGREGATE_REPORTS_SUMMARY_WIDTH
                case WidgetTypeEnum.ADHOC_REPORTS_SUMMARY:
                    return SUMMARY_WIDTH
                case WidgetTypeEnum.REPORT_REQUEST_SUMMARY:
                    return SUMMARY_WIDTH
                case WidgetTypeEnum.COMPLIANCE_PUBLISHER:
                case WidgetTypeEnum.ADVANCED_REPORT_REQUEST:
                case WidgetTypeEnum.ADVANCED_PUBLISHER:
                case WidgetTypeEnum.SPOTFIRE:
                    return REPORT_REQUEST_WIDGET_WIDTH
                case WidgetTypeEnum.ACTION_ITEMS_SUMMARY:
                    return SUMMARY_WIDTH
                case WidgetTypeEnum.ICSR_TRACKING:
                    return ICSR_TRACKING_TABLE_WIDGET_WIDTH
                case WidgetTypeEnum.CHART:
                    return CHART_WIDGET_WIDTH
            }
            return WIDGET_WIDTH
        }.call(widgetType)
        def widgetHeight = {
            switch (it) {
                case WidgetTypeEnum.CALENDAR:
                    return CALENDAR_WIDGET_HEIGHT
                case WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY:
                    return SUMMARY_HEIGHT
                case WidgetTypeEnum.ADHOC_REPORTS_SUMMARY:
                    return SUMMARY_HEIGHT
                case WidgetTypeEnum.REPORT_REQUEST_SUMMARY:
                    return SUMMARY_HEIGHT
                case WidgetTypeEnum.COMPLIANCE_PUBLISHER:
                case WidgetTypeEnum.ADVANCED_REPORT_REQUEST:
                case WidgetTypeEnum.ADVANCED_PUBLISHER:
                case WidgetTypeEnum.SPOTFIRE:
                case WidgetTypeEnum.ICSR_TRACKING:
                    return REPORT_REQUEST_WIDGET_HEIGHT
                case WidgetTypeEnum.ACTION_ITEMS_SUMMARY:
                    return SUMMARY_HEIGHT
                case WidgetTypeEnum.QUALITY_CASE_COUNT:
                case WidgetTypeEnum.QUALITY_ACTION_ITEMS:
                case WidgetTypeEnum.QUALITY_ERROR_COUNT:
                case WidgetTypeEnum.QUALITY_PRODUCT_COUNT:
                case WidgetTypeEnum.QUALITY_CASE_REPORT_TYPE:
                case WidgetTypeEnum.QUALITY_ENTRYSITE_COUNT:
                case WidgetTypeEnum.QUALITY_LATEST_ISSUES:
                    return QUALITY_WIDGET_HEIGHT
                case WidgetTypeEnum.CHART:
                    return CHART_WIDGET_HEIGHT
            }
            return WIDGET_HEIGHT
        }.call(widgetType)
        ReportWidget reportWidget = new ReportWidget(
                widgetType: widgetType,
                reportConfiguration: reportConfiguration,
                x: 0,
                y: 0,
                sectionNumber: params.sectionNumber,
                width: widgetWidth,
                height: widgetHeight,
                autoPosition: true
        )
        if(widgetType==WidgetTypeEnum.SPOTFIRE){
            reportWidget.settings=params.file
        }
        if(((reportWidget.widgetType == WidgetTypeEnum.ACTION_PLAN_PVQ)||(reportWidget.widgetType == WidgetTypeEnum.ACTION_PLAN_PVC)) &&
                (dashboard.widgets.find{((it.widgetType == WidgetTypeEnum.ACTION_PLAN_PVQ)||(it.widgetType == WidgetTypeEnum.ACTION_PLAN_PVC))})){
            flash.warn = message(code: "app.actionPlan.oneWidget")
        } else {
            dashboard.addToWidgets(reportWidget)
            CRUDService.save(dashboard)
        }
        if (ViewHelper.isPvqModule(request)) {
            redirect(controller: "quality", view: "index", params: [id: dashboard.id])
        } else if(dashboard.dashboardType in [DashboardEnum.PVC_USER, DashboardEnum.PVC_PUBLIC, DashboardEnum.PVC_MAIN]){
            redirect(controller: "central", view: "index", params: [id: dashboard.id])
        }
        else
            redirect(view: "index", params: [id: dashboard.id])
    }

    def removeReportWidgetAjax() {
        Dashboard dashboard = dashboardService.getDashboard(params, request)
        if (isAccessRestricted(dashboard)) {
            render message(code: "error.403.message")
            return
        }

        ReportWidget reportWidget = ReportWidget.get(params.widgetId)
        dashboard.widgets.remove(reportWidget)
        CRUDService.save(dashboard)
        render([result: "OK"] as JSON)
    }

    boolean isAccessRestricted(Dashboard dashboard) {
        User currentUser=userService.currentUser
        return dashboard.dashboardType in DashboardEnum.restrictedTypes &&
                dashboard.owner != currentUser && !currentUser.isDev()
    }

    def updateReportWidgetsAjax() {
        def itemsJSON = params?.items
        if (itemsJSON) {
            try {
                def items = JSON.parse(itemsJSON)
                Dashboard dashboard = dashboardService.getDashboard(params, request)
                items.each { item ->
                    def itemId = item.id as Integer
                    def widget = dashboard.widgets.find { it.id == itemId }
                    if (widget) {
                        widget.x = item.x
                        widget.y = item.y
                        widget.width = item.width
                        widget.height = item.height
                        widget.autoPosition = false
                        CRUDService.save(dashboard)
                    }
                }
            } catch (ConverterException ce) {
                log.error("Widget ajax update error",ce)
                return
            }
        }
        render([result: "OK"] as JSON)
    }

    def updateWidgetSettings() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        ReportWidget reportWidget =  ReportWidget.get(params.long("id"))
        if (!reportWidget) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.dashBoard'), params.id]) as String)
        } else {
            try {
                reportWidget.setSettings(params.data)
                CRUDService.update(reportWidget)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    def updateLabel() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        Dashboard dashboard = params.id ? Dashboard.get(params.id) : null
        if (!dashboard) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.dashBoard'), params.id]) as String)
        } else {
            try {
                dashboard.setLabel(params.label)
                CRUDService.update(dashboard)
            } catch (ValidationException ve) {
                responseDTO.setFailureResponse(ve.errors)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    def getChartWidgetDataAjax() {
        def data = dashboardService.getReportWidgetDate(params.long("widgetId"))
        render(text: JsonOutput.toJson(data), contentType: "application/json", latestComment: data.latestComment)
    }

    def etl() {
        def result = [:]
        def etlSchedule = etlJobService.getSchedule()
        def etlStatus = etlJobService.getEtlStatus()
        boolean preEtlApplicable = etlJobService.checkPreMartEtlStatusApplicable()
        boolean affEtlApplicable = etlJobService.checkAffEtlStatusApplicable()
        def preEtlStatus = (preEtlApplicable) ? etlJobService.getPreMartEtlStatus() : null
        def affEtlStatus = (affEtlApplicable) ? etlJobService.getAffiliateEtlStatus() : null
        result.status = etlStatus.status?.name()
        result.enabled = (etlSchedule.isDisabled ? ViewHelper.getMessage("default.button.no.label") : ViewHelper.getMessage("default.button.yes.label"))
        result.lastRun = DateUtil.getLongDateStringForTimeZone(etlStatus.lastRunDateTime, userService.currentUser?.preference?.timeZone)
        result.repeat = []
        etlSchedule?.repeatInterval?.split(';')?.each {
            def set = it.split("=")
            result.repeat << [label: g.message(code: "scheduler." + set[0].toLowerCase(), default: set[0].toLowerCase()), value: g.message(code: "scheduler." + set[1].toLowerCase(), default: set[1].toLowerCase())]
        }
        result.preStatus = preEtlStatus?.status?.name()
        result.preLastRun = DateUtil.getLongDateStringForTimeZone(preEtlStatus?.lastRunDateTime, userService.currentUser?.preference?.timeZone)
        result.preEtlAvailable = preEtlApplicable.toString()
        result.affStatus = affEtlStatus?.status?.name()
        result.affLastRun = DateUtil.getLongDateStringForTimeZone(affEtlStatus?.lastRunDateTime, userService.currentUser?.preference?.timeZone)
        result.affEtlAvailable = affEtlApplicable.toString()
        render([result: result] as JSON)
    }

    private def getWidgetData(ReportWidget reportWidget, User user) {
        WidgetTypeEnum widgetType = reportWidget.widgetType
        switch (widgetType) {
            case WidgetTypeEnum.CHART:
                return getChartWidgetData(reportWidget, user)
            default:
                return [reportWidget: reportWidget, type: widgetType]
        }
    }

    private def getChartWidgetData(ReportWidget reportWidget, User user) {
        ReportConfiguration reportConfiguration = GrailsHibernateUtil.unwrapIfProxy(reportWidget.reportConfiguration)
        def widget = [reportWidget: reportWidget, type: reportWidget.widgetType, title: reportConfiguration.reportName]
        ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.executeQuery('''
                        SELECT erc FROM ExecutedReportConfiguration erc
                        WHERE erc.owner = :owner
                            AND erc.reportName = :reportName
                            AND erc.status = :status
                            AND erc.isDeleted = false
                            order by id desc
                        ''', [
                owner: reportConfiguration.owner,
                reportName: reportConfiguration.reportName,
                status: ReportExecutionStatusEnum.COMPLETED
        ])?.find()
        if (!executedConfiguration) {
            return widget
        }
        ExecutedTemplateQuery executedTemplateQuery = dashboardService.getExecutedTemplateQuery(reportWidget, executedConfiguration)
        widget.executedConfiguration = executedConfiguration
        widget.isEditable = reportConfiguration.isEditableBy(user)
        widget.isViewable = reportConfiguration.isViewableBy(user)
        widget.title = ViewHelper.getReportTitle(executedConfiguration, executedTemplateQuery)
        widget.running = reportConfiguration.executing
        return widget
    }

    def exportWidget(){
        def paramData = new JsonSlurper().parseText(params.data.toString())
        List<ReportWidget> reportWidgetList = []
        List<Long> ids = paramData.selectedWidgets
        if(ids && ids.size()>0) {
            reportWidgetList = ReportWidget.findAllByIdInList(ids)
        }
        if(reportWidgetList){
            params.outputFormat = paramData.outputFormat
            params.reportLocale = (userService.currentUser?.preference?.locale ?: reportConfigurationList[0].owner.preference.locale).toString()

            File reportFile = dynamicReportService.createMultiReportForWidgetExport(reportWidgetList, params)

            renderReportOutputType(reportFile, reportWidgetList, params)
            return
        }
        render "No widget data to render"
    }

    private void renderReportOutputType(File reportFile, List<ReportWidget> reportWidgetList, Map params) {
        if (!reportFile) {
            flash.message = message(code: "app.report.file.not.found")
            redirect(controller: "dashboard", action: "index")
            return
        }
        String reportFileName = dynamicReportService.getReportNameForWidget(reportWidgetList, params)
        try {
            GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
            webRequest.setRenderView(false)
            MultipartFileSender.renderFile(reportFile, reportFileName, params.outputFormat as String, dynamicReportService.getContentType(params.outputFormat), request, response, false)
        } catch (Exception ex) {
            flash.error = message(code: "default.server.error.message")
            log.error("Exception occurred while rendering dashboard response ", ex)
        }
    }
    StringBuilder getQueryForAggregateSummary(User user) {
        StringBuilder selectQuery = new StringBuilder('''
            SELECT 
                COUNT(DISTINCT e.id) AS total,
                COUNT(DISTINCT CASE WHEN rs.submissionDate IS NULL AND (rs.reportSubmissionStatus IS NULL OR rs.reportSubmissionStatus != :submissionNotRequiredStatus) THEN e.id END) AS pending,
                COUNT(DISTINCT CASE WHEN e.status = :scheduledStatus THEN e.id END) AS scheduled,
                COUNT(DISTINCT CASE WHEN e.dueDate < :now AND rs.submissionDate IS NULL AND rs.reportSubmissionStatus IS NULL THEN e.id END) AS overDue,
                COUNT(DISTINCT CASE WHEN e.dueDate BETWEEN :now AND :dueSoonThreshold THEN e.id END) AS dueSoon,
                COUNT(DISTINCT CASE WHEN rs.submissionDate > :submittedRecentlyThreshold THEN e.id END) AS submittedRecently
            FROM ExecutedPeriodicReportConfiguration e
            LEFT JOIN e.reportSubmissions rs
            LEFT JOIN e.executedReportUserStates state WITH state.user = :currentUser
            WHERE e.isDeleted = false
            AND e.status IN (:statuses)
            AND ((state.id IS NULL AND e.archived = false)
            OR (state.isArchived = false and state.isDeleted = false))
        ''')

        if (!user.isAdmin()) {
            selectQuery.append('''
                AND (e.owner.id = :userId OR e.id IN (
                    SELECT exd.executedConfiguration.id 
                    FROM ExecutedDeliveryOption exd 
                    LEFT JOIN exd.sharedWith sw 
                    LEFT JOIN exd.sharedWithGroup swg
                    WHERE sw.id = :userId OR swg.id IN (:userGroupIds)
                ))
            ''')
        }
        return selectQuery
    }

    def reportConfiguration() {
        ReportWidget reportWidget = ReportWidget.get(params.long("widgetId"))
        ReportConfiguration reportConfiguration = reportWidget.reportConfiguration
        TemplateQuery templateQuery = reportConfiguration.templateQueries[reportWidget.sectionNumber]
        [templateQueryInstance: templateQuery , reportConfiguration: true]
    }
}
