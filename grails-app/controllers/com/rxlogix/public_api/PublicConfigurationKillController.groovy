package com.rxlogix.public_api

import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionKillRequest
import com.rxlogix.enums.KillStatusEnum
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class PublicConfigurationKillController {

    static allowedMethods = [killExecution: ['POST']]

    def CRUDService

    def killExecution(Long id, String username) {
        checkRightsAndExecute(id, username) { executingObject, executionStatus ->
            ReportExecutionKillRequest reportExecutionKillRequest = new ReportExecutionKillRequest(executionStatusId: executionStatus.id,killStatus: KillStatusEnum.NEW)
            CRUDService.save(reportExecutionKillRequest)
        }

    }

    def checkRightsAndExecute(Long id, String username, closure) {

        try {
            if (id) {
                ExecutionStatus executionStatus = ExecutionStatus.read(id)
                Object executingObject = executionStatus ? executionStatus?.getEntityClass()?.read(executionStatus.entityId) : null
                if (executionStatus && executingObject) {
                    User user = User.findByUsernameIlike(username)
                    boolean isAdmin = user ? user.getAuthorities()?.any { it.authority == "ROLE_ADMIN" } : false
                    if (user && (isAdmin || executingObject.owner?.id == user.id)) {
                        closure.call(executingObject, executionStatus)
                    } else {
                        response.status = 401
                        Map responseMap = [
                                message: message(code: "app.configuration.edit.permission", args: [executingObject.hasProperty('reportName') ? executingObject.reportName : '']),
                                status: 401
                        ]
                        render(contentType: "application/json", responseMap as JSON)
                        return
                    }
                } else {
                    response.status = 404
                    Map responseMap = [
                            message:  message(code: 'default.not.found.message', args: [message(code: 'app.label.executionStatus'), id]),
                            status: 404
                    ]
                    render(contentType: "application/json", responseMap as JSON)
                    return
                }
            }
            render([success: true] as JSON)
        } catch (Exception ex) {
            log.error("UnKnown Error occurred while checkRightsAndExecute configuration for: ${id} ",ex)
            response.status = 500
            Map responseMap = [
                    message:  message(code: "default.server.error.message"),
                    status: 500
            ]
            render(contentType: "application/json", responseMap as JSON)
        }
    }
}
