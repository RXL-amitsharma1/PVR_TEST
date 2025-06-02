package com.rxlogix

import com.rxlogix.config.AdvancedAssignment
import com.rxlogix.config.AutoAssignment
import com.rxlogix.config.WorkflowRule
import com.rxlogix.config.WorkflowState
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import grails.plugin.springsecurity.SpringSecurityUtils
import com.rxlogix.enums.AssignmentRuleEnum

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class WorkflowRuleController {

    static allowedMethods = [save: "POST", update: ['PUT','POST'], delete: ['DELETE','POST']]

    def CRUDService
    def userService

    def index() {}

    def list() {
        List<WorkflowRule> workflowRule = WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeleted(WorkflowConfigurationTypeEnum.getAllPVReports().collect {it.name()}, false)
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PVC_EDIT, ROLE_ADMIN, ROLE_DEV")) {
            workflowRule += WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeleted(WorkflowConfigurationTypeEnum.getAllPVCentral()?.collect {it.name()}, false)
        }
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PVQ_EDIT, ROLE_ADMIN, ROLE_DEV")) {
            workflowRule += WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeleted(WorkflowConfigurationTypeEnum.getAllPVQuality()?.collect {it.name()}, false)
        }
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PUBLISHER_TEMPLATE_EDITOR, ROLE_PUBLISHER_SECTION_EDITOR, ROLE_ADMIN, ROLE_DEV")) {
            workflowRule += WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeleted(WorkflowConfigurationTypeEnum.getAllPVPublisher()?.collect {it.name()}, false)
        }
        def workflowRules = workflowRule?.collect {
            it.toWorkflowRuleMap()
        }
        response.status = 200
        render workflowRules as JSON
    }

    def create() {
        WorkflowRule workflowRuleInstance = new WorkflowRule()
        render view: "create", model: getWorkflowRuleModal(workflowRuleInstance)
    }

    def save(WorkflowRule workflowRuleInstance) {
        try {
            bindExecutors(workflowRuleInstance, params.list('canExecute'), false)
            bindAssignedTo(workflowRuleInstance, params.list('assignedTo'), false)
            workflowRuleInstance.owner = userService.currentUser
            workflowRuleInstance = (WorkflowRule) CRUDService.save(workflowRuleInstance)
        } catch (ValidationException ve) {
            render view: "create", model: getWorkflowRuleModal(workflowRuleInstance)
            return
        } catch (Exception ex) {
            render view: "create", model: getWorkflowRuleModal(workflowRuleInstance)
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.workflow.rule.appName', default: 'Workflow Rule'), workflowRuleInstance.name])}"
        redirect(action: "index")
    }

    def edit(WorkflowRule workflowRuleInstance) {
        if (!workflowRuleInstance) {
            notFound()
            return
        }

        render view: "edit", model: getWorkflowRuleModal(workflowRuleInstance)
    }

    private def getWorkflowRuleModal(WorkflowRule workflowRuleInstance){
        List<WorkflowState> initialStates = WorkflowState.findAllByIsDeletedAndDisplay(false,true)
        List<WorkflowState> finalStates = WorkflowState.findAllByIsDeletedAndDisplay(false,true)
        if (workflowRuleInstance.initialState && !(initialStates*.id.contains(workflowRuleInstance.initialState.id))) {
            initialStates << (workflowRuleInstance.initialState)
        } else if (workflowRuleInstance.targetState && !(finalStates*.id.contains(workflowRuleInstance.targetState.id))) {
            finalStates << (workflowRuleInstance.targetState)
        }
        List<AdvancedAssignment> advancedAssignmentList = AdvancedAssignment.findAllByIsDeletedAndTenantId(false, Tenants.currentId() as Long)?.collect {
            it.toAdvancedAssignmentMap()
        }
        return [initialStates: initialStates,targetStates:finalStates,workflowRuleInstance:workflowRuleInstance, advancedAssignmentList: advancedAssignmentList]
    }

    def update(Long id) {
        WorkflowRule workflowRuleInstance = WorkflowRule.get(id)
        List<WorkflowState> targetStates = []
        List<WorkflowState> initialStates = []

        if (workflowRuleInstance) {
            bindData(workflowRuleInstance, params)
            bindExecutors(workflowRuleInstance, params.list('canExecute'), true)
            bindAssignedTo(workflowRuleInstance, params.list('assignedTo'), true)
            workflowRuleInstance.owner = userService.currentUser
            try {
                List<WorkflowConfigurationTypeEnum> workflowConfigurationTypeEnumList = []
                if(SpringSecurityUtils.ifAnyGranted("ROLE_PVC_EDIT, ROLE_ADMIN, ROLE_DEV")) {
                    workflowConfigurationTypeEnumList += WorkflowConfigurationTypeEnum.getAllPVCentral()
                }
                if(SpringSecurityUtils.ifAnyGranted("ROLE_PVQ_EDIT, ROLE_ADMIN, ROLE_DEV")) {
                    workflowConfigurationTypeEnumList += WorkflowConfigurationTypeEnum.getAllPVQuality()
                }
                if(workflowConfigurationTypeEnumList && workflowConfigurationTypeEnumList.find { it == workflowRuleInstance.configurationTypeEnum}) {
                    boolean isBasicRule = (workflowRuleInstance?.assignmentRule == AssignmentRuleEnum.BASIC_RULE.name()) && workflowRuleInstance?.assignedToUserGroup && (workflowRuleInstance?.assignToUserGroup || workflowRuleInstance?.autoAssignToUsers)
                    boolean isAdvanceRule = (workflowRuleInstance?.assignmentRule == AssignmentRuleEnum.ADVANCED_RULE.name()) && workflowRuleInstance?.advancedAssignment
                    if (!isBasicRule && !isAdvanceRule) {
                        List<AutoAssignment> autoAssignmentList = AutoAssignment.findAllByWorkflowRule(workflowRuleInstance)
                        autoAssignmentList?.each {
                            CRUDService.delete(it)
                        }
                    }
                }
                CRUDService.update(workflowRuleInstance)
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.workflow.rule.appName', default: 'Workflow Rule'), workflowRuleInstance.name])}"
                redirect(action: "index")
            } catch (ValidationException ve) {
                render view: "edit", model:getWorkflowRuleModal(workflowRuleInstance)
            } catch (Exception ex) {
                render view: "edit", model: getWorkflowRuleModal(workflowRuleInstance)
                return
            }
        } else {
            flash.error = "${message(code: 'default.not.found.message', args: [message(code: 'app.label.workflow.rule.appName', default: 'Workflow Rule'), params.id])}"
            render view: "edit", model:getWorkflowRuleModal(workflowRuleInstance)
        }
    }

    /**
     * Action to show the workflow
     * @return
     */
    def show(Long id) {
        WorkflowRule workflowRuleInstance = WorkflowRule.read(id)
        if (!workflowRuleInstance) {
            notFound()
            return
        }
        render view: "show", model: getWorkflowRuleModal(workflowRuleInstance)
    }

    def delete(Long id) {
        WorkflowRule workflowRuleInstance = WorkflowRule.get(id)
        if (!workflowRuleInstance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(workflowRuleInstance, workflowRuleInstance.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.workflow.rule.appName', default: 'Workflow Rule'), workflowRuleInstance.name])}"
        } catch (ValidationException ve) {
            flash.error = "Unable to delete the Workflow Rule"
        }
        redirect(action: "index")
    }

    private void bindExecutors(WorkflowRule instance, List<String> executors, Boolean isUpdate = false) {
        if (isUpdate) {
            instance.executors?.clear()
            instance.executorGroups?.clear()
        }
        if (executors) {
            executors.each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup) {
                        instance.addToExecutorGroups(userGroup)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    if (user) {
                        instance.addToExecutors(user)
                    }
                }
            }
        }
    }

    private void bindAssignedTo(WorkflowRule instance, List<String> assignedTo, Boolean isUpdate = false) {
        if (isUpdate) {
            instance.assignedToUser?.clear()
            instance.assignedToUserGroup?.clear()
        }
        if (assignedTo) {
            assignedTo.each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup) {
                        instance.addToAssignedToUserGroup(userGroup)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    if (user) {
                        instance.addToAssignedToUser(user)
                    }
                }
            }
        }
    }

    private notFound() {
        request.withFormat {
            form {
                flash.error = "${message(code: 'default.not.found.message', args: [message(code: 'app.label.workflow.rule.appName', default: 'Workflow Rule'), params.id])}"
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
