package com.rxlogix

import com.rxlogix.config.Dashboard
import com.rxlogix.config.DataTabulationTemplate
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.NonCaseSQLTemplate
import com.rxlogix.config.ReportWidget
import com.rxlogix.enums.DashboardEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import com.rxlogix.enums.WidgetTypeEnum
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.Transactional

class DashboardService {

    def userService
    def commentService
    GrailsApplication grailsApplication

    @Transactional
    def getDashboard(params, request) {
        DashboardEnum type = DashboardEnum.PVR_MAIN
        if (ViewHelper.isPvqModule(request)) type = DashboardEnum.PVQ_MAIN
        else if (ViewHelper.isPvcModule(request)) type = DashboardEnum.PVC_MAIN
        else if (ViewHelper.isPvPModule(request)) type = DashboardEnum.PVP_MAIN
        Dashboard dashboard = params.id ? Dashboard.get(params.id) : Dashboard.findByDashboardTypeAndOwnerAndIsDeleted(type, userService.currentUser, false)
        if (!dashboard)
            dashboard = initDashboard(type)
        return dashboard
    }

    List<Dashboard> listPvrDashboards() {
        Dashboard.selectByUserForPvr(userService.currentUser).list()
    }

    List<Dashboard> listPvqDashboards() {
        Dashboard.selectByUserForPvq(userService.currentUser).list()
    }

    List<Dashboard> listPvcDashboards() {
        Dashboard.selectByUserForPvc(userService.currentUser).list(sort: 'dateCreated')
    }

    List<Dashboard> listPvcDashboardsForId(Long parentId, Boolean mainDashboard = false) {
        List<Dashboard> list = []
        list.addAll Dashboard.selectByUserForPvcChild(parentId, userService.currentUser).list(sort: 'dateCreated')
        if (mainDashboard) {
            list.addAll Dashboard.selectByUserForPvcShared(parentId, userService.currentUser).list(sort: 'dateCreated')
        }
        return list
    }

    Dashboard mainPvcDashboard() {
        Dashboard dashboard = Dashboard.findByDashboardTypeAndOwnerAndIsDeleted(DashboardEnum.PVC_MAIN, userService.currentUser, false)
        if (!dashboard) dashboard = initDashboard(DashboardEnum.PVC_MAIN)
        return dashboard
    }


    List<Dashboard> mainPvcChildDashboard(){
        List<Dashboard> dashboardChild = Dashboard.findAllByDashboardTypeAndIsDeleted(DashboardEnum.PVC_MAIN, false)
        return dashboardChild
    }

    private Dashboard initDashboard(DashboardEnum type) {
        Dashboard dashboard
        if (type==DashboardEnum.PVQ_MAIN) {
            dashboard = new Dashboard(label: ViewHelper.getMessage("app.label.dashboard.main"),
                    dashboardType: DashboardEnum.PVQ_MAIN,
                    owner: userService.currentUser,
                    createdBy: "Application", modifiedBy: "Application")
            ReportWidget reportWidget = null
            int xPosition = 0
            reportWidget = new ReportWidget(
                    widgetType: WidgetTypeEnum.QUALITY_CASE_COUNT,
                    reportConfiguration: null,
                    x: xPosition,
                    y: 0,
                    width: 5,
                    height: 6,
                    autoPosition: true
            )
            dashboard.addToWidgets(reportWidget)
            dashboard.save(flush: true, failOnError: true)
        } else if (type!=DashboardEnum.PVC_MAIN) {
            dashboard = new Dashboard(label: ViewHelper.getMessage("app.label.dashboard.main"),
                    dashboardType: type,
                    owner: userService.currentUser,
                    createdBy: "Application",
                    modifiedBy: "Application")
            ReportWidget reportWidget = null
            reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.ADHOC_REPORTS_SUMMARY,reportConfiguration: null,x: 0,y: 0,width: 3,height: 2,autoPosition: true)
            dashboard.addToWidgets(reportWidget)
            reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.ACTION_ITEMS_SUMMARY,reportConfiguration: null,x: 2,y: 0,width: 3,height: 2,autoPosition: true)
            dashboard.addToWidgets(reportWidget)
            reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.REPORT_REQUEST_SUMMARY,reportConfiguration: null,x: 4,y: 0,width: 3,height: 2,autoPosition: true)
            dashboard.addToWidgets(reportWidget)
            reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY,reportConfiguration: null,x: 4,y: 0,width: 3,height: 2,autoPosition: true)
            dashboard.addToWidgets(reportWidget)

            int xPosition = 3
            reportWidget = new ReportWidget(
                    widgetType: WidgetTypeEnum.LAST_REPORTS,
                    reportConfiguration: null,
                    x: xPosition,
                    y: 3,
                    width: 6,
                    height: 6,
                    autoPosition: true
            )
            dashboard.addToWidgets(reportWidget)
            xPosition = xPosition + 7

            reportWidget = new ReportWidget(
                    widgetType: WidgetTypeEnum.ACTION_ITEMS,
                    reportConfiguration: null,
                    x: xPosition,
                    y: 3,
                    width: 6,
                    height: 6,
                    autoPosition: true
            )
            dashboard.addToWidgets(reportWidget)
            reportWidget = new ReportWidget(
                    widgetType: WidgetTypeEnum.CONFIGURATIONS,
                    reportConfiguration: null,
                    x: 0,
                    y: 10,
                    width: 6,
                    height: 6,
                    autoPosition: true
            )
            dashboard.addToWidgets(reportWidget)
            dashboard.save(flush: true, failOnError: true)
        } else {
            dashboard = new Dashboard(label: ViewHelper.getMessage("app.label.dashboard.main"),
                    dashboardType: DashboardEnum.PVC_MAIN,
                    owner: userService.currentUser,
                    dateCreated: new Date(0),
                    createdBy: "Application",
                    modifiedBy: "Application")
            ReportWidget reportWidget = null
            dashboard.save(flush: true, failOnError: true)
        }
        return dashboard
    }

    Map getReportWidgetDate(Long widgetId) {
        ReportWidget reportWidget = ReportWidget.get(widgetId)
        def data = [reportWidgetId: widgetId]
        if (reportWidget) {
            def reportConfiguration = reportWidget.reportConfiguration
            ExecutedReportConfiguration executedConfiguration =
                    ExecutedReportConfiguration.findByOwnerAndReportNameAndStatusAndIsDeleted(
                            reportConfiguration.owner,
                            reportConfiguration.reportName,
                            ReportExecutionStatusEnum.COMPLETED,
                            false,
                            [sort: 'id', order: 'desc'])

            if (executedConfiguration) {
                data.title = executedConfiguration.reportName
                data.runDate = DateUtil.getLongDateStringForTimeZone(executedConfiguration.lastRunDate, userService.currentUser?.preference?.timeZone, true)
                ExecutedTemplateQuery executedTemplateQuery = getExecutedTemplateQuery(reportWidget, executedConfiguration)

                data.displayLength = 5
                if (reportWidget.getSettings()) {
                    def cfg = JSON.parse(reportWidget.getSettings())
                    data.hideTable = cfg.hideTable
                    data.displayLength = cfg.displayLength
                    data.showIndicators = cfg.showIndicators
                }
                if (executedTemplateQuery) {
                    data.reportResultId = executedTemplateQuery.reportResult.id
                    data.latestComment = commentService.getReportResultChartAnnotation(executedTemplateQuery.reportResult.id)
                    String sectionTitle = executedTemplateQuery.title ? executedTemplateQuery.title : executedTemplateQuery.usedTemplate.name
                    data.sectionName = reportConfiguration.reportName + " (" + sectionTitle + ")"
                }
            }
        }
        return data
    }

    ExecutedTemplateQuery getExecutedTemplateQuery(ReportWidget reportWidget, ExecutedReportConfiguration executedConfiguration) {
        ExecutedTemplateQuery executedTemplateQuery
        if ((reportWidget.sectionNumber != null) && executedConfiguration.executedTemplateQueries.size() > reportWidget.sectionNumber) {
            executedTemplateQuery = executedConfiguration.executedTemplateQueries[reportWidget.sectionNumber]
        }
        if (!executedTemplateQuery) {
            executedTemplateQuery = executedConfiguration.executedTemplateQueries.find {
                (it.executedTemplate instanceof DataTabulationTemplate || it.executedTemplate instanceof NonCaseSQLTemplate) && it.executedTemplate.showChartSheet
            }
        }
        return executedTemplateQuery
    }
}
