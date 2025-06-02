package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['taskTemplate'])
class PublisherSectionTask {

    TaskTemplate taskTemplate

    //Action Item fields
    ActionItemCategory actionCategory
    String description
    User assignedTo
    UserGroup assignedGroupTo
    Integer dueDateShift
    AssignToType assignToType
    String priority
    AppTypeEnum appType
    BaseDate baseDate

    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    boolean isDeleted = false

    static constraints = {
        taskTemplate(nullable: true)
        actionCategory(nullable: true)
        assignedGroupTo(nullable: true)
        assignedTo(nullable: true)
        assignToType(nullable: true)
        priority(nullable: true)
        appType(nullable: true)
        baseDate nullable: true
        description(nullable: false, blank: false, minSize: 1, maxSize: 4000)
    }

    static mapping = {
        table("PUB_SECTION_TASK")
        taskTemplate column: "TASK_ID"
        actionCategory column: "ACTION_CATEGORY_ID"
        description column: "DESCRIPTION"
        assignedTo column: "ASSIGNED_USER_ID"
        assignedGroupTo column: "ASSIGNED_GROUP_ID"
        dueDateShift column: "DUE"
        assignToType column: "ASSIGN_TO_TYPE"
        priority column: "PRIORITY"
        appType column: "APP_TYPE"
        isDeleted column: "IS_DELETED"
    }

    String getAssignToIdAsString() {
        if (assignToType == AssignToType.USER) {
            return assignedTo ? (Constants.USER_TOKEN + assignedTo?.id) : (assignedGroupTo ? Constants.USER_GROUP_TOKEN + assignedGroupTo?.id : null)
        } else {
            return assignToType.name()
        }
    }

    def toMap() {
        String assignedToIdStr = assignedTo ? (Constants.USER_TOKEN + assignedTo?.id) : (assignedGroupTo ? Constants.USER_GROUP_TOKEN + assignedGroupTo?.id : null)
        if (!assignedToIdStr) assignedToIdStr = assignToType?.name()

        [
                actionItemId  : id,
                description   : description,
                actionCategory: actionCategory.key,
                assignedTo    : (assignedTo ? (assignedTo?.fullName ?: assignedTo?.username) : assignedGroupTo?.name),
                assignedToId  : assignedToIdStr,
                dueDateShift  : dueDateShift,
                assignToType  : assignToType?.name(),
                dateCreated   : dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                priority      : priority,
                createdBy     : createdBy,
                appType       : ViewHelper.getMessage("app.actionItemAppType." + appType),
                baseDate      : (this.baseDate ?: BaseDate.DUE_DATE).name()
        ]
    }

    String getInstanceIdentifierForAuditLog() {
        return (description?.length() < 50 ? description : (description?.substring(0, 49) + "..."))
    }


    public String toString() {
        return description
    }

    static enum BaseDate {
        DUE_DATE,
        REPORT_PERIOD_START,
        REPORT_PERIOD_END,
        CREATION_DATE,
        SECTION_DUE_DATE

        public getI18nKey() {
            return "app.Task.BaseDate.${this.name()}"
        }

        static getI18List() {
            return values().collect {
                [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
            }
        }
    }

    static enum AssignToType {
        USER,
        CONTRIBUTOR,
        AUTHOR,
        APPROVER,
        REVIEWER

        static List<String> asStringList() {
            return AssignToType.values().collect { it.name() }
        }
    }

}
