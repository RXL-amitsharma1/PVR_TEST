package com.rxlogix

import com.rxlogix.config.WorkflowState
import com.rxlogix.config.WorkflowStateReportAction
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class WorkflowStateController {

    static allowedMethods = [save: "POST", update: 'POST']

    def CRUDService

    def index() {}
    @Secured(['ROLE_CONFIGURATION_VIEW','ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def list() {
        def workflowStates = WorkflowState.findAllByIsDeleted(false)?.collect {
            it.toWorkflowStateMap()
        }
        response.status = 200
        render workflowStates as JSON
    }

    def create(WorkflowState workflowState) {
        render view: "create", model: [workflowStateInstance: workflowState]
    }

    def save(WorkflowState workflowState) {
        bindWorkflowActions(workflowState, false)
        try {
            CRUDService.save(workflowState)
        } catch (ValidationException ve) {
            render view: "create", model: [workflowStateInstance: workflowState]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.workflow.appName', default: 'Workflow State'), workflowState.name])}"
        redirect(action: "index")
    }

    def edit(WorkflowState workflowState) {
        if (!workflowState) {
            notFound()
        }
        render view: "edit", model: [workflowStateInstance: workflowState]
    }

    def update(Long id) {
        WorkflowState workflowState = WorkflowState.get(id)
        if (!workflowState) {
            notFound()
        }

        if (workflowState.reportActions) {
            List<String> reportActions = []
            workflowState.reportActions.each {
                String reportActionWithUsers = it.reportAction.getDisplayName() + " [ "
                if (!it.executors && !it.executorGroups) {
                    reportActionWithUsers += "Users: AnyUser"
                }
                if (it.executors) {
                    reportActionWithUsers += "Users: " + it.executors.collect { it.fullName }
                }
                if (it.executorGroups) {
                    reportActionWithUsers += "User Group: " + it.executorGroups.collect { it.name }
                }
                reportActionWithUsers += "]"
                reportActions.add(reportActionWithUsers)
            }
            params.put('oldReportActions', reportActions)
        }

        bindData(workflowState, params)
        bindWorkflowActions(workflowState, true)

        try {
            CRUDService.update(workflowState)
        } catch (ValidationException ve) {
            render view: "edit", model: [workflowStateInstance: workflowState]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.workflow.appName', default: 'Workflow State'), workflowState.name])
        redirect(action: 'index')
    }

    def show(Long id) {
        WorkflowState workflowState = WorkflowState.read(id)
        if (!workflowState) {
            notFound()
        }
        render view: "show", model: [workflowStateInstance: workflowState]
    }

    def delete(Long id) {
        WorkflowState workflowState = WorkflowState.get(id)
        if (!workflowState) {
            notFound()
            return
        }
        if (workflowState.name in [WorkflowState.NEW_NAME, WorkflowState.UNDER_REVIEW_NAME, WorkflowState.REVIEWED_NAME]) {
            flash.error = "Can not delete default state"
        } else {
            try {
                CRUDService.softDelete(workflowState, workflowState.name, params.deleteJustification)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.workflow.appName', default: 'Workflow State'), workflowState.name])}"
            } catch (ValidationException ve) {
                log.warn("Validation Error during workflowstate -> delete")
                flash.error = "Unable to delete the Workflow State"
            }
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.workflow.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    protected void bindWorkflowActions(WorkflowState instance, boolean isUpdate) {
        if (isUpdate) {
            instance.reportActions?.collect { it }?.each { instance.reportActions.remove(it); it.delete(); }
        }
        params.findAll { it.key.toString().startsWith("reportAction_enabled_") }.each { param ->
            ReportActionEnum type = param.key.toString().substring(21) as ReportActionEnum
            WorkflowStateReportAction workflowStateReportAction = new WorkflowStateReportAction(reportAction: type);
            String executors = params["canExecute_" + type.name()]
            if (executors) {
                executors.split(";").each { String shared ->
                    if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                        UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        if (userGroup) {
                            workflowStateReportAction.addToExecutorGroups(userGroup)
                        }
                    } else if (shared.startsWith(Constants.USER_TOKEN)) {
                        User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                        if (user) {
                            workflowStateReportAction.addToExecutors(user)
                        }
                    }
                }
            }
            instance.addToReportActions(workflowStateReportAction)
        }

    }
}
