package com.rxlogix

import com.rxlogix.config.PublisherSectionTask
import com.rxlogix.config.ReportTask
import com.rxlogix.config.Task
import com.rxlogix.config.TaskTemplate
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.TaskTemplateTypeEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

/**
 * Created by Chetan on 3/8/2016.
 */

@Secured(["isAuthenticated()"])
class TaskTemplateController {

    static allowedMethods = [save: "POST", update: ['PUT','POST'], delete: ['DELETE','POST']]

    def taskTemplateService
    def CRUDService
    def userService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {}

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {

        List<TaskTemplate> taskTemplates = TaskTemplate.findAllByIsDeleted(false)?.collect {
           it.toTaskTemplateDto()
        }
        response.status = 200
        render taskTemplates as JSON
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create() {
        boolean aggregateTaskTemplate = false
        def taskTemplateInstance = new TaskTemplate()
        if (params.type==TaskTemplateTypeEnum.AGGREGATE_REPORTS.name())
            taskTemplateInstance.type=TaskTemplateTypeEnum.AGGREGATE_REPORTS
            aggregateTaskTemplate = true
        if (params.type==TaskTemplateTypeEnum.PUBLISHER_SECTION.name())
            taskTemplateInstance.type=TaskTemplateTypeEnum.PUBLISHER_SECTION
        render view: "create", model: [taskTemplateInstance: taskTemplateInstance, newAggregateTaskTemplate: aggregateTaskTemplate]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def show() {
        def taskTemplateId = params.id
        def taskTemplateInstance = TaskTemplate.get(taskTemplateId)
        render view: "show", model: [taskTemplateInstance: taskTemplateInstance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit() {
        def taskTemplateId = params.id
        def taskTemplateInstance = TaskTemplate.get(taskTemplateId)
        render view: "edit", model: [taskTemplateInstance: taskTemplateInstance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save() {
        TaskTemplate.withNewTransaction { status ->
            //Task template instantiation
            def taskTemplateInstance = new TaskTemplate()

            bindData(taskTemplateInstance, params, ['tasks', 'reportTasks'])
            try {
                if (params.type == TaskTemplateTypeEnum.REPORT_REQUEST.name())
                    bindTasks(taskTemplateInstance)
                else
                    bindReportTasks(taskTemplateInstance, params.type)
                CRUDService.save(taskTemplateInstance)
                flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.task.template.appName', default: 'Task Template'), taskTemplateInstance?.name])}"
                redirect(action: "index")
            } catch (ValidationException ve) {
                status.setRollbackOnly()
                render view: "create", model: [taskTemplateInstance: taskTemplateInstance]
            } catch (Exception ex) {
                status.setRollbackOnly()
                flash.error = "${message(code: 'app.label.task.template.save.exception')}"
                ex.printStackTrace()
                render view: "create", model: [taskTemplateInstance: taskTemplateInstance]
            }
        }

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update() {
        def taskTemplateId = params.long('id');
        def taskTemplateInstance

        if (taskTemplateId) {

            taskTemplateInstance = TaskTemplate.get(taskTemplateId)

            if (taskTemplateInstance) {
                TaskTemplate.withNewTransaction { status ->
                    try {
                        bindData(taskTemplateInstance, params, ['tasks', 'reportTasks'])
                        if (params.type == TaskTemplateTypeEnum.REPORT_REQUEST.name()) {
                            bindExistingTasks(taskTemplateInstance)
                            bindTasks(taskTemplateInstance)
                        } else {
                            bindReportTasks(taskTemplateInstance, params.type)
                        }
                        CRUDService.update(taskTemplateInstance)
                        taskTemplateInstance.tasks = taskTemplateInstance.tasks.findAll{ !it.deleted}
                        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.task.template.appName', default: 'Task Template'), taskTemplateInstance.name])}"
                        redirect(action: "index")
                    } catch (ValidationException ve) {
                        status.setRollbackOnly()
                        render view: "edit", model: [taskTemplateInstance: taskTemplateInstance]
                    } catch (Exception ex) {
                        status.setRollbackOnly()
                        flash.error = "${message(code: 'app.label.task.template.save.exception')}"
                        render view: "edit", model: [taskTemplateInstance: taskTemplateInstance]
                    }
                }

            } else {
                render view: "edit", model: [taskTemplateInstance: taskTemplateInstance]
            }
        } else {
            render view: "edit", model: [taskTemplateInstance: taskTemplateInstance]
        }
    }

    /**
     * Action to delete the task template
     * @return
     */

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete() {
        def taskTemplateId = params.long('id');

        if (taskTemplateId) {

            def taskTemplateInstance = TaskTemplate.get(taskTemplateId)

            if (taskTemplateInstance) {

                try {
                    CRUDService.softDelete(taskTemplateInstance,taskTemplateInstance.name,params.deleteJustification)
                    flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.task.template.appName', default: 'Task Template'), taskTemplateInstance.name])}"
                } catch(ValidationException ve) {
                    flash.error = "Unable to delete the task template"
                }
            } else {
                flash.error = "${message(code: 'default.not.found.message', args: [message(code: 'app.label.task.template.appName', default: 'Task Template'), taskTemplateId])}"
            }
        } else {
            flash.error = "${message(code: 'app.label.id.invalid')}"
        }
        redirect(action: "index")
    }

    /**
     * This method binds the values to the existing tasks
     * @param taskTemplateInstance
     * @return
     */
    private bindExistingTasks(taskTemplateInstance) {

        //handle edits to the existing comments
        taskTemplateInstance?.tasks?.eachWithIndex() { task, index ->
            //A check if comment added is not a newObject.
            if ( params["tasks[" + index + "].newObj"]
                    && !Boolean.valueOf(params["tasks[" + index + "].newObj"]) ) {

                //A check if task id is same is the padded task id
                if (params.long("tasks[" + index + "].id") == task.id) {
                    task.baseDate = Task.BaseDate.valueOf(params["tasks[" + index + "].baseDate"])
                    task.taskName = params["tasks[" + index + "].taskName"]
                    task.deleted = Boolean.valueOf(params["tasks[" + index + "].deleted"])
                    task.dueDate = params["tasks[" + index + "].dueDate"] ? Integer.parseInt(params["tasks[" + index + "].sign"] + params.int("tasks[" + index + "].dueDate")) : null
                    task.priority = params["tasks[" + index + "].priority"]
                    if(task.deleted){
                        Task taskInstance = Task.load(task.id)
                        taskInstance.delete()
                    }
                }
            }
        }
    }

    /**
     * This method binds the task to the task template.
     * @param taskTemplateInstance
     * @return
     */
    private bindTasks(taskTemplateInstance) {

        def taskList = []

        //Iterate over the params with action items.
        for (int i = 0; params.containsKey("tasks[" + i + "]"); i++) {

            //A check if comment added is not a newObject.
            Boolean isNewObjectAndNotDeleted = !(Boolean.valueOf(params["tasks[" + i +"].deleted"])) && params["tasks[" + i + "].newObj"] && Boolean.valueOf(params["tasks[" + i + "].newObj"]);
            if (isNewObjectAndNotDeleted) {
                def task = new Task()
                task.baseDate = Task.BaseDate.valueOf(params["tasks[" + i + "].baseDate"])
                task.taskName = params["tasks[" + i + "].taskName"]
                task.dueDate = params.int("tasks[" + i + "].dueDate")
                if (task.dueDate && params["tasks[" + i + "].sign"] == '-') {
                    task.dueDate = -1 * task.dueDate
                }
                task.priority = params["tasks[" + i + "].priority"]
                def user = userService.currentUser
                task.createdBy = user.username
                task.modifiedBy = user.username
                taskList.add(task)
            }
        }
        //Add comments to the report object.
        taskList.each {
            taskTemplateInstance.addToTasks(it)
        }
    }

    private bindReportTasks(TaskTemplate taskTemplateInstance, String type) {
        if (taskTemplateInstance?.reportTasks) {
            taskTemplateInstance.reportTasks*.delete()
            taskTemplateInstance.reportTasks.clear()
        }
        if (taskTemplateInstance?.publisherSectionTasks) {
            taskTemplateInstance.publisherSectionTasks*.delete()
            taskTemplateInstance.publisherSectionTasks.clear()
        }
        if (type == TaskTemplateTypeEnum.AGGREGATE_REPORTS.name()) {
            List<ReportTask> taskList = taskTemplateService.fetchReportTasksFromRequest(params)
            taskList.each {
                taskTemplateInstance.addToReportTasks(it)
            }
        } else {
            List<PublisherSectionTask> taskList = taskTemplateService.fetchPublisherSectionTasksFromRequest(params)
            taskList.each {
                taskTemplateInstance.addToPublisherSectionTasks(it)
            }
        }

    }

    def ajaxGetTasksForConfiguration(){

        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try {
            responseDTO.setSuccessResponse( ReportTask.listTasksForReportConfiguration(params.long("id") ).list().collect{it.toMap()})
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    def ajaxGetReportTasksForTemplate(){

        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try {
            responseDTO.setSuccessResponse( ReportTask.listTasksForReportTemplate(params.long("id") ).list().collect{it.toMap()})
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    def ajaxGetReportTemplates(){
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try {
            responseDTO.setSuccessResponse(TaskTemplate.findAllByTypeAndIsDeleted(TaskTemplateTypeEnum.AGGREGATE_REPORTS, false).collect{[id:it.id,name:it.name]})
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    def ajaxGetPublisherSectionTemplates(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        render([items : TaskTemplate.findAllByNameIlikeAndTypeAndIsDeleted("%" + term + "%", TaskTemplateTypeEnum.PUBLISHER_SECTION, false, [max: max, offset: Math.max(page - 1, 0) * max]).collect {
            [id: it.id, text: it.name]
        }, total_count: TaskTemplate.countByNameIlikeAndTypeAndIsDeleted("%" + term + "%", TaskTemplateTypeEnum.PUBLISHER_SECTION, false)] as JSON)
    }

    def ajaxGetPublisherSectionTemplatesName(Long id) {
        TaskTemplate tt = TaskTemplate.read(id)
        Map result = [
                text: tt?.name
        ]
        render(result as JSON)
    }
}
