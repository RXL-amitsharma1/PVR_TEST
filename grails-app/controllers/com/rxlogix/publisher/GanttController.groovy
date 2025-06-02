package com.rxlogix.publisher

import com.rxlogix.Constants
import com.rxlogix.config.ActionItem
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.publisher.Gantt
import com.rxlogix.config.publisher.GanttItem
import com.rxlogix.config.publisher.PublisherExecutedTemplate
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class GanttController {

    static allowedMethods = [save: "POST", update: 'POST']

    def CRUDService
    def ganttService
    def userService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {}

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {
        def gantts = Gantt.findAllByIsDeletedAndIsTemplate(false, true).collect {
            it.toMap()
        }
        render([aaData: gantts, recordsTotal: gantts.size(), recordsFiltered: gantts.size()] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create(Gantt gantt) {
        render view: "create", model: getModel([gantt: gantt])
    }

    private getModel(Map model) {
        List reportWorkflowList = WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.PERIODIC_REPORT).collect { [name: it.id, display: it.name] }
        List publisherSectionWorkflowList = WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.PUBLISHER_SECTION).collect { [name: it.id, display: it.name] }
        List publisherWorkflowList = WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.PUBLISHER_FULL).collect { [name: it.id, display: it.name] }
        List reportStateList = toSelectList([ReportExecutionStatusEnum.GENERATED_CASES, ReportExecutionStatusEnum.GENERATED_DRAFT, ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED])
        List publisherStateList = toSelectList([PublisherExecutedTemplate.Status.DRAFT, PublisherExecutedTemplate.Status.FINAL]);
        List publisherSectionStateList = toSelectList([PublisherExecutedTemplate.Status.DRAFT, PublisherExecutedTemplate.Status.FINAL]);
        model.putAll([publisherWorkflowList: publisherWorkflowList, publisherSectionWorkflowList: publisherSectionWorkflowList, reportWorkflowList: reportWorkflowList, reportStateList: reportStateList, publisherStateList: publisherStateList, publisherSectionStateList: publisherSectionStateList])
        model
    }

    private toSelectList(Collection enumList) {
        enumList.collect {
            [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
        }
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save(Gantt gantt) {
        bindStages(gantt)
        try {
            CRUDService.save(gantt)
        } catch (ValidationException ve) {
            render view: "create", model: getModel([gantt: gantt])
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.gantt.appName', default: 'Plan Template'), gantt.name])}"
        redirect(action: "index")
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit(Long id) {
        Gantt gantt = Gantt.read(id)
        if (!gantt) {
            notFound()
        }
        render view: "edit", model: getModel([gantt: gantt])
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update(Long id) {
        Gantt gantt = Gantt.get(id)
        if (!gantt) {
            notFound()
        }
        gantt.defaultSubmissionDuration = params.defaultSubmissionDuration as Integer
        gantt.defaultAiDuration = params.defaultAiDuration as Integer
        gantt.defaultReportDuration = params.defaultReportDuration as Integer
        gantt.defaultSectionDuration = params.defaultSectionDuration as Integer
        gantt.defaultFullDuration = params.defaultFullDuration as Integer
        gantt.name = params.name
        bindStages(gantt)
        try {
            CRUDService.update(gantt)
        } catch (ValidationException ve) {
            render view: "edit", model: getModel([gantt: gantt])
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.gantt.appName'), gantt.name])
        redirect(action: 'index')
    }

    private bindStages(Gantt gantt) {
        gantt.ganttItems.collect { it }.each {
            gantt.removeFromGanttItems(it)
            it.delete()
        }

        if (params.itemName instanceof String[]) {
            for (int i = 1; i < params.itemName.size(); i++) {
                GanttItem ganttItem = new GanttItem(
                        sortIndex: i,
                        name: params.itemName[i],
                        duration: params.itemDuration[i] as Integer,
                        complete: 0,
                        completeCondition: params.ganttCondition[i],
                        completeConditionType: GanttItem.ConditionType.valueOf(params.itemConditionType[i]),
                        taskType: GanttItem.TaskType.valueOf(params.ganttTaskType[i])
                )
                String assignedTo = params.assignedTo[i]
                if (assignedTo) {
                    if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                        ganttItem.assignedGroupTo = UserGroup.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                        ganttItem.assignedTo = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                    }
                }
                gantt.addToGanttItems(ganttItem)
            }
        }
    }

    def gantt() {}

    def changeDependence() {
        def ganttItem = params.type.endsWith("AI") ? ActionItem.get(params.long("id")) : GanttItem.get(params.long("id"))
        if (ganttItem) {
            if (params.dependOn) {
                def dependOnItem = params.dependOnType.endsWith("_AI") ? ActionItem.get(params.long("dependOn")) : GanttItem.get(params.long("dependOn"))
                if (dependOnItem) {
                    ganttItem.depend = dependOnItem.uuid?:dependOnItem.id
                    CRUDService.save(ganttItem)
                }
            } else {
                ganttItem.depend = null
                CRUDService.save(ganttItem)
            }
        }
        if (params.backUrl)
            redirect(url: params.backUrl)
        else
           render "ok"
    }

    boolean isEditableByUser(ExecutedPeriodicReportConfiguration cfg, User assignedTo, UserGroup assignedGroupTo) {
        User currentUser = userService.currentUser
        if (currentUser.isAdmin()) return true
        if (currentUser.id == cfg.ownerId) return true
        if (!cfg.isViewableBy(currentUser)) return false
        if (assignedTo?.id == currentUser.id) return true
        if (assignedGroupTo?.getUsers()?.find { it.id == currentUser.id }) return true
        return false
    }

    def updateTask() {
        ExecutedPeriodicReportConfiguration cfg = ExecutedPeriodicReportConfiguration.get(params.long("cfgId"))
        if (params.type in [GanttItem.TaskType.REPORT_FLOW_STEP.name(), GanttItem.TaskType.PUB_SEC_FLOW_STEP.name(), GanttItem.TaskType.PUB_FULL_FLOW_STEP.name()]) {
            GanttItem ganttItem = GanttItem.get(params.long("id"))
            if (ganttItem) {
                if (isEditableByUser(cfg, ganttItem.assignedTo, ganttItem.assignedGroupTo)) {
                    if (params.complete && params.complete.isInteger()) ganttItem.complete = params.int("complete")
                    if (params.startDate) ganttItem.startDate = Date.parse(DateUtil.DATEPICKER_FORMAT, params.startDate)
                    if (params.endDate) ganttItem.endDate = Date.parse(DateUtil.DATEPICKER_FORMAT, params.endDate)
                    if (params.duration) ganttItem.duration = params.duration
                    if (params.name) ganttItem.name = params.name
                    if (params.assignedTo) {
                        if (params.assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                            ganttItem.assignedGroupTo = UserGroup.get(Long.valueOf(params.assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                            ganttItem.assignedTo = null
                        } else if (params.assignedTo.startsWith(Constants.USER_TOKEN)) {
                            ganttItem.assignedTo = User.get(Long.valueOf(params.assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                            ganttItem.assignedGroupTo = null
                        }
                    }
                    CRUDService.save(ganttItem)
                } else {
                    flash.error = g.message(code: 'app.label.gantt.item.task.forbidden')
                }
            }
        } else {
            ActionItem actionItem = ActionItem.get(params.long("id"))
            if (actionItem) {
                if (isEditableByUser(cfg, actionItem.assignedTo, actionItem.assignedGroupTo)) {
                    if (params.complete && params.complete.isInteger()) {
                        if (params.int("complete") >= 100) {
                            actionItem.status = StatusEnum.CLOSED;
                            actionItem.completionDate = new Date()
                        } else {
                            actionItem.status = StatusEnum.OPEN;
                            actionItem.completionDate = null;
                        }
                    }
                    if (params.startDate) actionItem.startDate = Date.parse(DateUtil.DATEPICKER_FORMAT, params.startDate)
                    if (params.endDate) actionItem.dueDate = Date.parse(DateUtil.DATEPICKER_FORMAT, params.endDate)
                    if (params.name) actionItem.description = params.name
                    if (params.assignedTo) {
                        if (params.assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                            actionItem.assignedGroupTo = UserGroup.get(Long.valueOf(params.assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                            actionItem.assignedTo = null
                        } else if (params.assignedTo.startsWith(Constants.USER_TOKEN)) {
                            actionItem.assignedTo = User.get(Long.valueOf(params.assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                            actionItem.assignedGroupTo = null
                        }
                    }
                    CRUDService.save(actionItem)
                } else {
                    flash.error = g.message(code: 'app.label.gantt.item.task.forbidden')
                }
            }
        }
        if (params.backUrl)
            redirect(url: params.backUrl)
        else
            redirect(action: "gantt")
    }

    def ganttAjax() {
        boolean pvp = params.boolean("pvp")
        List<ExecutedPeriodicReportConfiguration> list = ExecutedPeriodicReportConfiguration.reportsWithGantt(ReportSubmissionStatusEnum.PENDING, pvp).list([max: 1000, sort: "id"])
        def outJson = ganttService.getGanttForExecutedConfigurationList(list)
        render outJson as JSON
    }

    def singleGanttAjax() {
        def outJson = []
        ExecutedPeriodicReportConfiguration cfg = ExecutedPeriodicReportConfiguration.get(params.long("id"))
        outJson = ganttService.getGanttForExecutedConfiguration(cfg)
        render outJson as JSON
    }

    def show(Long id) {
        Gantt gantt = Gantt.read(id)
        if (!gantt) {
            notFound()
        }
        render view: "show", model: getModel([gantt: gantt])
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete(Long id) {
        Gantt gantt = Gantt.get(id)
        if (!gantt) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(gantt, gantt.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.gantt.appName'), gantt.name])}"
        } catch (ValidationException ve) {
            flash.error = message(code: "app.label.gantt.delete.error.message")
        }
        redirect(action: "index")
    }


    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.gantt.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
