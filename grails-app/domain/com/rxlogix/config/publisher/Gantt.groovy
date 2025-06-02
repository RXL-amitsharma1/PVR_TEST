package com.rxlogix.config.publisher


import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.util.DateUtil

class Gantt {
    String name

    boolean isDeleted = false
    boolean isTemplate = false
    Integer defaultAiDuration = 2
    Integer defaultSubmissionDuration = 2
    Integer defaultSectionDuration = 3
    Integer defaultFullDuration = 3
    Integer defaultReportDuration = 3

    ExecutedPeriodicReportConfiguration executedConfiguration

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "Application"
    String modifiedBy = "Application"

    static hasMany = [ganttItems: GanttItem, configurations: PeriodicReportConfiguration]

    static belongsTo = [ExecutedPeriodicReportConfiguration]

    static mapping = {
        table name: "GANTT"
        name column: "NAME"
        isDeleted column: "IS_DELETED"
        isTemplate column: "IS_TEMPLATE"
        defaultAiDuration column: "AI_DURATION"
        defaultSubmissionDuration column: "SUBMISSION_DURATION"
    }

    static constraints = {

        defaultReportDuration nullable: true
        defaultFullDuration nullable: true
        defaultSectionDuration nullable: true
        executedConfiguration nullable: true
        configurations nullable: true
        ganttItems nullable: true
        isTemplate(validator: { val, obj ->
            if (obj.isTemplate && obj.executedConfiguration) {
                return "com.rxlogix.config.publisher.Gantt.templateExecutedConfiguration"
            }
            if (!obj.isTemplate && obj.configurations?.size() > 0) {
                return "com.rxlogix.config.publisher.Gantt.notTemplateConfiguration"
            }
        })
    }

    Map toMap() {
        [
                id         : id,
                name       : name,
                lastUpdated: lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy : modifiedBy
        ]
    }


    transient Integer getDueDays() {
        return defaultSubmissionDuration + defaultReportDuration + defaultFullDuration + defaultSectionDuration
    }

    transient List<GanttItem> getReportStage() {
        getStage(GanttItem.TaskType.REPORT_FLOW_STEP)
    }

    transient List<GanttItem> getPubSectionStage(bindingId = null) {
        getStage(GanttItem.TaskType.PUB_SEC_FLOW_STEP, bindingId)
    }

    transient List<GanttItem> getFullPublisherStage(bindingId = null) {
        getStage(GanttItem.TaskType.PUB_FULL_FLOW_STEP, bindingId)
    }

    transient List<GanttItem> getSubmissionStage() {
        getStage(GanttItem.TaskType.SUBMISSION)
    }

    transient List<GanttItem> getStage(GanttItem.TaskType type, bindingId = null) {
        return ganttItems?.findAll { !it.template && (it.taskType == type) && (!bindingId || bindingId == it.bingingId) }?.sort { it.sortIndex }
    }
}
