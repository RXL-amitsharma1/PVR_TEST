package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ExecutionStatusConfigTypeEnum
import grails.plugin.springsecurity.annotation.Secured

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class ExecutionStatusController {

    def userService
    def notificationService

    def list(boolean isICSRProfile) {
        render(view: "/configuration/executionStatus", model: [related: "executionStatusPage", isAdmin: userService.isCurrentUserAdmin(), isICSRProfile: isICSRProfile?:false])
    }

    def listAllResults() {
        render(view: "/configuration/executionStatus", model: [related: "listAllResultsPage", isAdmin: userService.isCurrentUserAdmin()])
    }

    def reportExecutionError(Long id) {
        ExecutionStatus exStatus = ExecutionStatus.read(id)
        if (!exStatus) {
            notFound()
            return
        }
        render(view: "/configuration/reportExecutionError", model: [exStatus: exStatus])
    }

    def viewNotificationError(Notification notification) {
        if (!notification) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.notification'), params.id])
            redirect(action: 'list')
            return
        }
        notificationService.deleteNotification(notification)
        ExecutionStatus exStatus = ExecutionStatus.findById(notification.executionStatusId)
        render(view: "/configuration/reportExecutionError", model: [exStatus: exStatus])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW', 'ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def viewConfig(Long id) {
        ExecutionStatus executionStatus = ExecutionStatus.read(id)
        if (executionStatus) {
            switch (executionStatus.entityType) {
                case ExecutingEntityTypeEnum.CONFIGURATION:
                    redirect(controller: 'configuration', action: 'view', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
                case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                    redirect(controller: 'configuration', action: 'viewExecutedConfig', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                    redirect(controller: 'periodicReport', action: 'view', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES:
                case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                    redirect(controller: 'periodicReport', action: 'viewExecutedConfig', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.CASESERIES:
                    redirect(controller: 'caseSeries', action: 'show', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
                case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                    redirect(controller: 'executedCaseSeries', action: 'show', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                    //For Icsr Report
                    redirect(controller: 'icsrReport', action: 'view', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.EXECUTED_ICSR_CONFIGURATION:
                    //For Executed Icsr Report
                    redirect(controller: 'icsrReport', action: 'viewExecutedConfig', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                    redirect(controller: 'icsrProfileConfiguration', action: 'view', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.EXECUTED_ICSR_PROFILE_CONFIGURATION:
                    redirect(controller: 'executedIcsrProfile', action: 'view', id: executionStatus.entityId)
                    break
            }
        } else {
            notFound()
        }
    }

    def viewResult(Long id) {
        ExecutionStatus executionStatus = ExecutionStatus.read(id)
        if (executionStatus && executionStatus.executionStatus in ReportExecutionStatusEnum.completedStatusesList) {
            switch (executionStatus.entityType) {
                case ExecutingEntityTypeEnum.CONFIGURATION:
                case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                    redirect(controller: 'report', action: 'showFirstSection', id: executionStatus.executedEntityId)
                    break
                case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
                case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                    redirect(controller: 'report', action: 'showFirstSection', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT:
                case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                    redirect(controller: 'report', action: 'showFirstSection', id: executionStatus.entityId)
                    break
                case ExecutingEntityTypeEnum.CASESERIES:
                    redirect(controller: 'caseList', action: 'index', params: [cid: executionStatus.executedEntityId, reportName: executionStatus.toString()])
                    break
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES:
                    redirect(controller: 'caseList', action: 'index', params: [id: executionStatus.entityId, reportName: "Cases for ${executionStatus.toString()}"])
                    break
                case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
                case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                    redirect(controller: 'caseList', action: 'index', params: [cid: executionStatus.entityId, reportName: executionStatus.toString()])
                    break
            }
        } else {
            notFound()
        }
    }

    def viewScheduledConfig(Long id){
        ReportConfiguration reportConfiguration = ReportConfiguration.get(id)
        if (reportConfiguration) {
            if (reportConfiguration.instanceOf(PeriodicReportConfiguration)) {
                redirect(controller: "periodicReport", action: "view", id: id)
            } else if (reportConfiguration.instanceOf(IcsrReportConfiguration)) {
                redirect(controller: "icsrReport", action: "view", id: id)
            } else {
                redirect(controller: "configuration", action: "view", id: id)
            }
        } else {
            CaseSeries caseSeries = CaseSeries.get(id)
            if (caseSeries) {
                redirect(controller: "caseSeries", action: "show", id: id)
            } else {
                notFound()
            }
        }
    }


    protected void notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.executionStatus'), params.id])
                redirect action: "list", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
