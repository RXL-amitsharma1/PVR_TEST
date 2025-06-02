package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.ExecutedInboundCompliance
import com.rxlogix.config.ResultInboundCompliance
import com.rxlogix.enums.NotificationApp
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class ExecutedInboundController implements SanitizePaginationAttributes {

    def userService
    def reportExecutorService
    def CRUDService
    def notificationService

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    def index() { }

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    def viewExecutedConfig(Long id) {
        ExecutedInboundCompliance executedInboundCompliance = ExecutedInboundCompliance.read(id) as ExecutedInboundCompliance
        if (!executedInboundCompliance) {
            notFound()
            return
        }

        if (!executedInboundCompliance.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        // delete notification
        notificationService.deleteExecutedReportNotification(userService.currentUser, executedInboundCompliance, NotificationApp.INBOUNDCOMPLIACE)
        Boolean viewBasicSql = params.getBoolean("viewBasicSql")
        Boolean viewAdvanceSql = params.getBoolean("viewAdvanceSql")
        render(view: "/inboundCompliance/view", model: [queriesCompliance: executedInboundCompliance.executedQueriesCompliance, configurationInstance: executedInboundCompliance, reportConfigurationId: executedInboundCompliance?.inboundCompliance?.id,
                                     isExecuted     : true, viewSql: (viewBasicSql || viewAdvanceSql) ? reportExecutorService.debugExecutedInboundSQL(executedInboundCompliance, viewBasicSql ?: viewAdvanceSql) : null])
    }

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    def showReport(Long id){
        render(view: "viewCases", params: [id: id])
    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    def delete(ExecutedInboundCompliance executedInboundCompliance) {

        if (!executedInboundCompliance) {
            notFound()
            return
        }

        User currentUser = userService.currentUser

        if (!executedInboundCompliance.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.configuration.delete.permission", args: [executedInboundCompliance.senderName])
            redirect(view: "index")
            return
        }

        try {
            CRUDService.softDelete(executedInboundCompliance, executedInboundCompliance.senderName, params.deleteJustification)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), executedInboundCompliance.senderName])
            redirect action: "index", method: "GET"
        } catch (ValidationException ve) {
            flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.configuration'), executedInboundCompliance.senderName])
            redirect(action: "viewExecutedConfig", id: params.id)
        }

    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    def executionError(Long id) {
        ExecutedInboundCompliance exInboundCompliance = ExecutedInboundCompliance.get(id)
        if (!exInboundCompliance){
            notFound()
            return
        }
        // delete notification
        notificationService.deleteExecutedReportNotification(userService.currentUser, exInboundCompliance, NotificationApp.INBOUNDCOMPLIACE)
        if (exInboundCompliance.errorDetails){
            render(view: "/configuration/reportExecutionError", model: [exStatus: exInboundCompliance, isInbound : true])
        }
        else {
            ResultInboundCompliance resultInboundCompliance = ResultInboundCompliance.getAllResultByIdAndErrorDetails(id)?.list()[0]
            if(!resultInboundCompliance) {
                log.error("Unexpected error while opening executionPage -> executionError")
                redirect(controller: "inboundComplianceRest", action: "executionStatus")
            }
            render(view: "/configuration/reportExecutionError", model: [exStatus: resultInboundCompliance, isInbound : true])
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect(controller: "inboundCompliance", action: "executionStatus", method: "GET")
            }
            '*' { render status: NOT_FOUND }
        }
    }

}
