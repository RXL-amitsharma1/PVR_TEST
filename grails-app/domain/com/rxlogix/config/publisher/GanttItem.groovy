package com.rxlogix.config.publisher


import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.ViewHelper

class GanttItem {

    String name
    Integer duration
    Date startDate
    Date endDate
    User assignedTo
    UserGroup assignedGroupTo
    Integer complete
    Integer sortIndex = 0
    Long bingingId
    String completeCondition
    ConditionType completeConditionType
    TaskType taskType
    String parent
    String depend
    String uuid
    Boolean template


    static belongsTo = [gantt: Gantt]

    static mapping = {
        table name: "GANTT_ITEM"
        name column: "NAME"
        duration column: "DURATION"
        startDate column: "START_DATE"
        endDate column: "END_DATE"
        assignedTo column: "ASSIGNED_ID"
        complete column: "COMPLETE"
        completeCondition column: "CONDITION"
        taskType column: "TYPE"
        bingingId column: "BINDING_ID"
        completeConditionType column: "CONDITION_TYPE"
        sortIndex column: "sort_index"
        assignedGroupTo column: "ASSIGNED_GROUP_ID"
        gantt column: "GANTT_ID"
        parent column: "PARENT"
        depend column: "DEPEND"
        uuid column: "UUID"

    }

    static constraints = {

        name nullable: true
        startDate nullable: true
        endDate nullable: true
        assignedTo nullable: true
        complete nullable: true
        completeCondition nullable: true
        taskType nullable: true
        bingingId nullable: true
        completeConditionType nullable: true
        sortIndex nullable: true
        assignedGroupTo nullable: true
        parent nullable: true
        depend nullable: true
        uuid nullable: true
        template nullable: true
    }
    public static Map TASK_TYPES = [
            AI                : "AI",
            REPORT_FLOW_STEP  : "REPORT_FLOW_STEP",
            PUB_SEC_FLOW_STEP : "PUB_SEC_FLOW_STEP",
            PUB_FULL_FLOW_STEP: "PUB_FULL_FLOW_STEP",
            SUBMISSION        : "SUBMISSION"
    ]

    transient String getAdvanced() {
        if (completeConditionType == ConditionType.ADVANCED) return completeCondition
        return ""
    }

    transient String getPublisherSectionWorkflowState() {
        if (completeConditionType == ConditionType.PUBLISHER_SECTION_WORKFLOW) return completeCondition
        return ""
    }

    transient String getReportWorkflowState() {
        if (completeConditionType == ConditionType.REPORT_WORKFLOW) return completeCondition
        return ""
    }

    transient String getReportState() {
        if (completeConditionType == ConditionType.REPORT_STATE) return completeCondition
        return ""
    }

    transient String getPublisherSectionState() {
        if (completeConditionType == ConditionType.PUBLISHER_SECTION_STATE) return completeCondition
        return ""
    }

    transient String getPublisherState() {
        if (completeConditionType == ConditionType.PUBLISHER_FULL_STATE) return completeCondition
        return ""
    }

    transient String getPublisherWorkflowState() {
        if (completeConditionType == ConditionType.PUBLISHER_FULL_WORKFLOW) return completeCondition
        return ""
    }

    static enum TaskType {
        REPORT_AI,
        REPORT_FLOW_STEP,
        PUB_SEC_FLOW_STEP,
        PUB_SEC_AI,
        PUB_FULL_FLOW_STEP,
        PUB_FULL_AI,
        SUBMISSION

        public static Map list() {
            values().collectEntries { v -> [(v.name()): v.name()] }
        }

        public getI18nKey() {
            return "app.GanttItem.TaskType.${this.name()}"
        }

        static getI18List() {
            return values().collect {
                [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
            }
        }
    }

    static enum ConditionType {

        REPORT_WORKFLOW,
        REPORT_STATE,
        PUBLISHER_SECTION_WORKFLOW,
        PUBLISHER_SECTION_STATE,
        PUBLISHER_FULL_WORKFLOW,
        PUBLISHER_FULL_STATE,
        MANUAL,
        ADVANCED

        public static Map list() {
            values().collectEntries { v -> [(v.name()): v.name()] }
        }

        public getI18nKey() {
            return "app.GanttItem.ConditionType.${this.name()}"
        }

        static getI18List() {
            return values().collect {
                [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
            }
        }
    }

}
