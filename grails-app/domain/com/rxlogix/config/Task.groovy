package com.rxlogix.config

import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

/**
 * Created by Chetan on 3/8/2016.
 */
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['taskTemplate'])
class Task {
    @AuditEntityIdentifier
    String taskName
    Integer dueDate
    String priority
    BaseDate baseDate

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    //Transients
    boolean deleted, newObj

    static transients = ['deleted', 'newObj','dueDateSign','dueDateVal']

    static belongsTo = [taskTemplate : TaskTemplate]

    static constraints = {
        priority(validator: { val ->
            if(!val){
                return "com.rxlogix.config.Task.priority.nullable"
            }
        })
        taskName(blank: false, maxSize: 4000)
        priority(validator: { val ->
            if(!val){
                return "com.rxlogix.config.Task.priority.nullable"
            }
        })
        baseDate nullable: true
        dueDate max: 365
    }

    static mapping = {
        table("TASK")
    }

    def toTaskDto() {
        [
           taskName : this.taskName,
           dueDate : this.dueDateVal,
           dueDateSign:this.dueDateSign,
           priority: this.priority,
           baseDate: (this.baseDate ?: BaseDate.DUE_DATE).name()
        ]
    }

    String getDueDateSign(){
       return this.dueDate>=0?'+':'-'
    }

    Integer getDueDateVal(){
        return Math.abs(this.dueDate)
    }

    String toString(){
       return taskName
    }

    static enum BaseDate {
        DUE_DATE,
        REPORT_PERIOD_START,
        REPORT_PERIOD_END

        public getI18nKey() {
            return "app.Task.BaseDate.${this.name()}"
        }

        static getI18List() {
            return values().collect {
                [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
            }
        }
    }
}
