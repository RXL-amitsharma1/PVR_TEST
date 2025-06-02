package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.TaskTemplateTypeEnum
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

/**
 * Created by Chetan on 3/8/2016.
 */
@CollectionSnapshotAudit
class TaskTemplate {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    List<Task> tasks
    Set<ReportTask> reportTasks
    Set<PublisherSectionTask> publisherSectionTasks
    TaskTemplateTypeEnum type

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    boolean isDeleted = false

    static hasMany = [tasks: Task, reportTasks: ReportTask, publisherSectionTasks: PublisherSectionTask]

    static constraints = {

        name maxSize: 4000, blank: false, validator: { val, obj ->
            if (val) {
                long count = TaskTemplate.createCriteria().count {
                    ilike('name', "${val}")
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "unique"
                }
            }
        }

        tasks(nullable: true, validator: { val, obj ->
                if(obj.tasks && obj.tasks.size()>0){
                    for(int i=0;i<obj.tasks.size();i++){
                        if(!(obj.tasks[i].taskName?.size()>0))
                            return "com.rxlogix.config.TaskTemplate.tasks.description.empty"
                    }
                }
        })
        reportTasks(nullable: true, validator: { val, obj ->
                if(obj.reportTasks && obj.reportTasks.size()>0){
                    for(int i=0;i<obj.reportTasks.size();i++){
                        if(!(obj.reportTasks[i].description?.size()>0))
                            return "com.rxlogix.config.TaskTemplate.reportTasks.description.empty"
                        else if (obj.reportTasks[i].description?.size() > 4000) {
                            return "com.rxlogix.config.TaskTemplate.reportTasks.description.maxSize.exceeded"
                        }
                    }
                }
        })
        publisherSectionTasks(nullable: true, validator: { val, obj ->
            if (obj.publisherSectionTasks && obj.publisherSectionTasks.size() > 0) {
                for (int i = 0; i < obj.publisherSectionTasks.size(); i++) {
                    if (!(obj.publisherSectionTasks[i].description?.size() > 0))
                        return "com.rxlogix.config.TaskTemplate.publisherSectionTasks.description.empty"
                    else if (obj.publisherSectionTasks[i].description?.size() > 4000) {
                        return "com.rxlogix.config.TaskTemplate.publisherSectionTasks.description.maxSize.exceeded"
                    }
                }
            }
        })
    }

    static mapping = {
        table("TASK_TEMPLATE")
        isDeleted column: "IS_DELETED"
        type column: "TYPE"
    }

    def toTaskTemplateDto() {
        [
                id  : this.id,
                name: this.name,
                type: ViewHelper.getMessage(this.type?.getI18nKey())
        ]
    }

    def getReportTasksAsJson() {
        reportTasks?.sort{it.dateCreated}.collect { it.toMap() } as JSON
    }
    def getPublisherSectionTasksAsJson() {
        publisherSectionTasks?.sort{it.dateCreated}.collect { it.toMap() } as JSON
    }

    public String toString() {
        return name
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            if(newValues?.get("tasks"))
                newValues.put("tasks", createTaskView(tasks))
            if(newValues?.get("reportTasks"))
                newValues.put("reportTasks", createReportTaskView(reportTasks))
            if(newValues?.get("publisherSectionTasks"))
                newValues.put("publisherSectionTasks", createPublisherSectionTaskView(publisherSectionTasks))
        }

        if (newValues && oldValues) {
            def id=this.id
            withNewSession {
                if ((newValues?.get("tasks") || (oldValues?.get("tasks")))) {
                    newValues.put("tasks", createTaskView(tasks))
                    TaskTemplate taskTemplate = TaskTemplate.get(id)
                    oldValues.put("tasks", createTaskView(taskTemplate.tasks))
                }

                if ((newValues?.get("reportTasks") || (oldValues?.get("reportTasks")))) {
                    newValues.put("reportTasks", createReportTaskView(reportTasks))
                    TaskTemplate taskTemplate = TaskTemplate.get(id)
                    oldValues.put("reportTasks", createReportTaskView(taskTemplate.reportTasks))
                }

                if ((newValues?.get("publisherSectionTasks") || (oldValues?.get("publisherSectionTasks")))) {
                    newValues.put("publisherSectionTasks", createPublisherSectionTaskView(publisherSectionTasks))
                    TaskTemplate taskTemplate = TaskTemplate.get(id)
                    oldValues.put("publisherSectionTasks", createPublisherSectionTaskView(taskTemplate.publisherSectionTasks))
                }
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }

    String createTaskView(Collection<Task> tasks){
        StringBuilder sb = new StringBuilder()
        tasks.sort {it.dateCreated}.each {
            sb.append("Task Name : ${it.taskName} \n")
            sb.append("Priority: ${it.priority} \n")
            sb.append("Due Date : ${it.dueDate} \n")
            sb.append("Base Date : ${ViewHelper.getMessage(it.baseDate.getI18nKey())} \n\n")
        }
        return sb.toString()
    }

    String createReportTaskView(Collection<ReportTask> reportTasks){
        StringBuilder sb = new StringBuilder()
        reportTasks.sort {it.dateCreated}.each {
            sb.append("Type: ${ViewHelper.getMessage(it.appType?.getI18nKey())?:""}\n")
            sb.append("Description : ${it.description} \n")
            sb.append("Asigned to: ${(it.assignedTo?.fullNameAndUserName?:it.assignedGroupTo?.name)?:"Owner"}\n " )
            sb.append("Priority: ${it.priority} \n")
            sb.append("Create in : ${it.createDateShift} days \n")
            sb.append("Due Date in : ${it.dueDateShift} days \n")
            sb.append("Base Date : ${ViewHelper.getMessage(it.baseDate.getI18nKey())} \n\n")
        }
        return sb.toString()
    }

    String createPublisherSectionTaskView(Collection<PublisherSectionTask> publisherSectionTasks){
        StringBuilder sb = new StringBuilder()
        publisherSectionTasks.sort {it.dateCreated}.each {
            sb.append("Type: ${ViewHelper.getMessage(it.appType?.getI18nKey())?:""} \n")
            sb.append("Description : ${it.description} \n")
            sb.append("Asigned to: ${(it.assignedTo?.fullNameAndUserName?:it.assignedGroupTo?.name)?:"Owner"}\n " )
            sb.append("Priority: ${it.priority} \n")
            sb.append("Due Date in : ${it.dueDateShift} days \n")
            sb.append("Base Date : ${ViewHelper.getMessage(it.baseDate.getI18nKey())} \n\n")
        }
        return sb.toString()
    }
}
