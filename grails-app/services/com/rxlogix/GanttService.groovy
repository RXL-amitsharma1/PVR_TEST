package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.publisher.*
import com.rxlogix.enums.*
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.ViewHelper
import grails.util.Holders

class GanttService {

    def configurationService
    def CRUDService

    public final static String DF = "yyyy-MM-dd"


    List getScheduled(boolean pvp, Date ganttFrom, Date ganttTo) {
        List outJson = [];

        outJson << [
                "pID"        : 2,
                "pName"      : "Scheduled Reports",
                "pStart"     : ganttFrom.format(DF),
                "pEnd"       : ganttTo.format(DF),
                "pPlanStart" : "",
                "pPlanEnd"   : "",
                "pClass"     : "ggroupblack",
                "pLink"      : "",
                "pMile"      : 0,
                "pRes"       : "",
                "pComp"      : 0,
                "pGroup"     : 1,
                "pParent"    : 0,
                "pOpen"      : 1,
                "pDepend"    : "",
                "pCaption"   : "",
                "description": "",
                "category"   : "",
                "entityname" : ""
        ]
        List<PeriodicReportConfiguration> list = pvp ? PeriodicReportConfiguration.findAllByIsDeletedAndIsEnabledAndNextRunDateGreaterThanAndNextRunDateLessThanAndIsPublisherReport(false, true, ganttFrom, ganttTo, true) :
                PeriodicReportConfiguration.findAllByIsDeletedAndIsEnabledAndNextRunDateGreaterThanAndNextRunDateLessThan(false, true, ganttFrom, ganttTo)
        list.each { cfg ->

            List<Date> runDateList = [new Date(cfg.nextRunDate.getTime())]
            runDateList.addAll(configurationService.getFutureRunDates(cfg, cfg.nextRunDate, ganttTo))
            outJson << [
                    "pID"        : "" + cfg.id + "0",
                    "pName"      : cfg.reportName,
                    "pStart"     : "",
                    "pEnd"       : "",
                    "pPlanStart" : "",
                    "pPlanEnd"   : "",
                    "pClass"     : "gtaskblue",
                    "pLink"      : "",
                    "pMile"      : 0,
                    "pRes"       : "",
                    "pComp"      : 0,
                    "pGroup"     : 2,
                    "pParent"    : 2,
                    "pOpen"      : 1,
                    "pDepend"    : "",
                    "pCaption"   : "",
                    "description": "",
                    "category"   : "",
                    "entityname" : ""
            ]


            runDateList.eachWithIndex { Date runDate, int i ->

                def reportPeriod = cfg.globalDateRangeInformation?.getReportStartAndEndDateForDate(runDate)
                Date reportPeriodEnd = reportPeriod[1] ?: runDate
                Date dueDate = reportPeriodEnd + (cfg.dueInDays ?: 0)
                if (dueDate < runDate) dueDate = runDate + 1;
                outJson << [
                        "pID"        : "" + cfg.id + i,
                        "pName"      : cfg.reportName,
                        "pStart"     : runDate.format(DF),
                        "pEnd"       : dueDate.format(DF),
                        "pPlanStart" : "",
                        "pPlanEnd"   : "",
                        "pClass"     : "gtaskblue",
                        "pLink"      : "",
                        "pMile"      : 0,
                        "pRes"       : "",
                        "pComp"      : 0,
                        "pGroup"     : 0,
                        "pParent"    : "" + cfg.id + "0",
                        "pOpen"      : 1,
                        "pDepend"    : "",
                        "pCaption"   : "",
                        "description": "",
                        "category"   : "",
                        "entityname" : ""
                ]

            }
        }
        return outJson
    }

    List getGanttForExecutedConfigurationList(List<ExecutedPeriodicReportConfiguration> list) {
        def outJson = [];

        list.each { cfg ->
            outJson.addAll(getGanttForExecutedConfiguration(cfg))
        }
        return outJson
    }

    List getGanttForExecutedConfiguration(ExecutedPeriodicReportConfiguration cfg) {
        if (!cfg || !cfg.gantt) return []
        def outJson = [];

        outJson << [
                "pID"        : cfg.id,
                "pName"      : cfg.reportName + " ver. " + cfg.numOfExecutions,
                "pStart"     : "",
                "pEnd"       : "",
                "pPlanStart" : "",
                "pPlanEnd"   : "",
                "pClass"     : "ggroupblack",
                "pLink"      : createLink("report/showFirstSection/" + cfg.id),
                "pMile"      : 0,
                "pRes"       : "",
                "pComp"      : 0,
                "pGroup"     : 1,
                "pParent"    : "",
                "pOpen"      : 1,
                "pDepend"    : "",
                "pCaption"   : "",
                "description": cfg.description,
                "category"   : "report",
                "entityname" : cfg.reportName + " ver. " + cfg.numOfExecutions
        ]

        //----report stage----------
        outJson << getStageJson(cfg, GanttItem.TaskType.REPORT_FLOW_STEP, ViewHelper.getMessage("app.label.gantt.stage.reportStage"))

        cfg.actionItems.each { ActionItem ai ->
            outJson << getActionItemJson(cfg.id, ai, cfg.id + "_" + GanttItem.TaskType.REPORT_FLOW_STEP, GanttItem.TaskType.REPORT_AI, cfg.gantt.defaultAiDuration)
        }

        cfg.gantt.getReportStage()?.each { GanttItem step ->
            outJson << getStepJson(cfg, step)
        }

        //----publisher section stage----------
        outJson << getStageJson(cfg, GanttItem.TaskType.PUB_SEC_FLOW_STEP, ViewHelper.getMessage("app.label.gantt.stage.pubSectStage"))

        cfg.publisherConfigurationSections?.each { PublisherConfigurationSection section ->
            cfg.gantt.getPubSectionStage(section.id)?.each { step ->
                outJson << getStepJson(cfg, step, section)
            }
            section.actionItems.each { ai ->
                outJson << getActionItemJson(cfg.id, ai, cfg.id + "_" + GanttItem.TaskType.PUB_SEC_FLOW_STEP, GanttItem.TaskType.PUB_SEC_AI, cfg.gantt.defaultAiDuration, section)
            }
            if (section.dueDate)
                outJson << getMilestoneJson(section, WorkflowConfigurationTypeEnum.PUBLISHER_SECTION, section.executedConfigurationId + "_" + GanttItem.TaskType.PUB_SEC_FLOW_STEP)
        }

        //----publisher full stage----------
        outJson << getStageJson(cfg, GanttItem.TaskType.PUB_FULL_FLOW_STEP, ViewHelper.getMessage("app.label.gantt.stage.publishingStage"))

        if (cfg.publisherReports) {
            cfg.publisherReports?.each { PublisherReport report ->
                cfg.gantt.getFullPublisherStage(report.id)?.each { step ->
                    outJson << getStepJson(cfg, step, report)
                }
                report.actionItems.each { ai ->
                    outJson << getActionItemJson(cfg.id, ai, cfg.id + "_" + GanttItem.TaskType.PUB_FULL_FLOW_STEP, GanttItem.TaskType.PUB_FULL_AI, cfg.gantt.defaultAiDuration, report)
                }
                if (report.dueDate)
                    outJson << getMilestoneJson(report, WorkflowConfigurationTypeEnum.PUBLISHER_FULL, report.executedReportConfigurationId + "_" + GanttItem.TaskType.PUB_FULL_FLOW_STEP)
            }
        } else {
            cfg.gantt.fullPublisherStage?.each { step ->
                outJson << getStepJson(cfg, step)
            }
        }

        //----submission stage----------
        outJson << getStageJson(cfg, GanttItem.TaskType.SUBMISSION, ViewHelper.getMessage("app.label.gantt.stage.submissionStage"))

        cfg.gantt.submissionStage?.each { step ->
            outJson << getStepJson(cfg, step)
        }


        return outJson
    }

    private String getClassForTaskLine(Date d, int complete) {
        if (complete >= 100) return "gtaskgreen"
        Date now = new Date()
        if (d < now.plus(5) && (d >= now)) return "gtaskyellow"
        if (d < now) return "gtaskred"
        return "gtaskgreen"
    }


    int validateTaskState(ExecutedPeriodicReportConfiguration cfg, GanttItem step, def section = null) {
        if (step.complete >= 100) return 100;
        if (step.taskType == GanttItem.TaskType.SUBMISSION) {
            return cfg.status == ReportExecutionStatusEnum.SUBMITTED ? 100 : 0
        }
        Map context = ["cfg": cfg, "section": section]
        try {
            switch (step.completeConditionType.name()) {
                case GanttItem.ConditionType.ADVANCED.name():
                    return eval(context, step.completeCondition) ? 100 : 0
                case GanttItem.ConditionType.REPORT_STATE.name():
                    return cfg.status in step.completeCondition?.split(",")?.collect { ReportExecutionStatusEnum.valueOf(it.trim()) } ? 100 : 0
                case GanttItem.ConditionType.REPORT_WORKFLOW.name():
                    return cfg.workflowStateId in step.completeCondition?.split(",")?.collect { it.trim() as Long } ? 100 : 0
                case GanttItem.ConditionType.PUBLISHER_SECTION_WORKFLOW.name():
                    return section.workflowStateId in step.completeCondition?.split(",")?.collect { it.trim() as Long } ? 100 : 0
                case GanttItem.ConditionType.PUBLISHER_SECTION_STATE.name():
                    return section.state in step.completeCondition?.split(",")?.collect { PublisherExecutedTemplate.Status.valueOf(it.trim()) } ? 100 : 0
                case GanttItem.ConditionType.PUBLISHER_FULL_STATE.name():
                    PublisherExecutedTemplate.Status s = (section?.published ? PublisherExecutedTemplate.Status.FINAL : PublisherExecutedTemplate.Status.DRAFT)
                    return (s in step.completeCondition?.split(",")?.collect { PublisherExecutedTemplate.Status.valueOf(it.trim()) }) ? 100 : 0
                case GanttItem.ConditionType.PUBLISHER_FULL_WORKFLOW.name():
                    return section?.workflowStateId in step.completeCondition?.split(",")?.collect { it.trim() as Long } ? 100 : 0
            }

        } catch (Exception e) {
            e.printStackTrace()
        }
        return 0;
    }

    def eval(Map context, String expression) {
        String preparedExpression = expression
        context.each { String key, Object value ->
            preparedExpression = preparedExpression.replaceAll(/\b((?<!\.)$key)\b/, "x.$key")
        }
        return Eval.x(context, preparedExpression)
    }

    String createLink(String link) {
        return Holders.grailsApplication.config.grails.appBaseURL + "/" + link
    }

    Map getStepJson(ExecutedPeriodicReportConfiguration cfg, GanttItem step, def section = null) {
        String link = step.taskType == GanttItem.TaskType.REPORT_FLOW_STEP ? createLink("report/showFirstSection/" + cfg.id) : createLink("pvp/sections?id=" + cfg.id)
        int complete = validateTaskState(cfg, step, section)
        [
                "pID"              : step.uuid,
                "pName"            : (section?.name ? section.name + ": " : "") + step.name,
                "pStart"           : step.startDate?.format(DF),
                "pEnd"             : step.endDate?.format(DF),
                "pPlanStart"       : "",
                "pPlanEnd"         : "",
                "pClass"           : getClassForTaskLine(step.endDate, complete),
                "pLink"            : link,
                "pMile"            : 0,
                "pRes"             : step.assignedTo ? step.assignedTo.fullName : step.assignedGroupTo?.name,
                "pComp"            : complete ?: 0,
                "pGroup"           : 0,
                "pParent"          : step.parent,
                "pOpen"            : 1,
                "pDepend"          : step.depend,
                "pCaption"         : "",
                "description"      : step.name,
                "category"         : step.taskType.name(),
                "entityname"       : section?.name,
                "entityId"         : step.id,
                "assignedToId"     : step.assignedToId,
                "assignedGroupToId": step.assignedGroupToId,
                "uuid"             : step.uuid,
                "cfgId"            : cfg.id
        ]
    }

    Map getMilestoneJson(def section, WorkflowConfigurationTypeEnum type, String parent) {
        [
                "pID"              : section.id,
                "pName"            : section.name + " " + ViewHelper.getMessage("app.label.gantt.deadline"),
                "pStart"           : section.dueDate?.format(DF),
                "pEnd"             : section.dueDate?.format(DF),
                "pClass"           : "gmilestone",
                "pLink"            : "",
                "pMile"            : 1,
                "pRes"             : "",
                "pComp"            : section.workflowState in WorkflowState.getFinalStatesForType(type) ? 100 : 0,
                "pGroup"           : 0,
                "pParent"          : parent,
                "pOpen"            : 1,
                "pDepend"          : "",
                "pCaption"         : "",
                "description"      : section.name + " " + ViewHelper.getMessage("app.label.gantt.deadline"),
                "category"         : "",
                "entityname"       : section.name + " " + ViewHelper.getMessage("app.label.gantt.deadline"),
                "entityId"         : section.id,
                "assignedToId"     : "",
                "assignedGroupToId": "",
                "uuid"             : "",
                "cfgId"            : ""
        ]
    }

    Map getActionItemJson(Long cfgId, ActionItem ai, String parent, GanttItem.TaskType type, defaultAiDuration, def section = null) {

        [
                "pID"              : ai.uuid ?: ai.id,
                "pName"            : (section?.name ? (section?.name + ": ") : "") + (ai.description.size() > 30 ? (ai.description.substring(0, 30) + "...") : ai.description),
                "pStart"           : ai.startDate ? ai.startDate.format(DF) : (ai.dueDate.minus(defaultAiDuration).format(DF)),
                "pEnd"             : ai.dueDate.format(DF),
                "pPlanStart"       : "",
                "pPlanEnd"         : "",
                "pClass"           : getClassForTaskLine(ai.dueDate, ai.completionDate ? 100 : 0),
                "pLink"            : createLink("actionItem/index?id=" + ai.id),
                "pMile"            : 0,
                "pRes"             : ai.assignedTo ? ai.assignedTo.fullName : ai.assignedGroupTo.name,
                "pComp"            : ai.completionDate ? 100 : 0,
                "pGroup"           : 0,
                "pParent"          : parent,
                "pOpen"            : 1,
                "pDepend"          : ai.depend,
                "pCaption"         : "",
                "description"      : ai.description,
                "category"         : type.name(),
                "entityname"       : ai.description,
                "entityId"         : ai.id,
                "assignedToId"     : ai.assignedToId,
                "assignedGroupToId": ai.assignedGroupToId,
                "uuid"             : ai.uuid,
                "cfgId"            : cfgId

        ]
    }

    Map getStageJson(ExecutedPeriodicReportConfiguration cfg, GanttItem.TaskType type, String label) {
        String link = type == GanttItem.TaskType.REPORT_FLOW_STEP ? createLink("report/showFirstSection/" + cfg.id) : createLink("pvp/sections?id=" + cfg.id)
        [
                "pID"        : cfg.id + "_" + type,
                "pName"      : label,
                "pStart"     : "",
                "pEnd"       : "",
                "pPlanStart" : "",
                "pPlanEnd"   : "",
                "pClass"     : "ggroupblack",
                "pLink"      : link,
                "pMile"      : 0,
                "pRes"       : "",
                "pComp"      : 0,
                "pGroup"     : 1,
                "pParent"    : cfg.id,
                "pOpen"      : 1,
                "pDepend"    : "",
                "pCaption"   : "",
                "description": "",
                "category"   : "",
                "entityname" : ""
        ]
    }

    void createFullStage(Gantt gantt, PublisherReport report) {
        GanttItem item = gantt.getFullPublisherStage().min { it.startDate }
        boolean first = false
        if (!item.bingingId) {
            first = true
            gantt.getFullPublisherStage()?.each { GanttItem ganttItem ->
                ganttItem.bingingId = report.id
                ganttItem.save(flush: true)
            }
        }
        def previous = null
        Date startDate = report.executedReportConfiguration.dateCreated.plus(gantt.defaultReportDuration + gantt.defaultSectionDuration)
        Date previousDate = startDate
        gantt?.ganttItems?.findAll { it.template && (it.taskType == GanttItem.TaskType.PUB_FULL_FLOW_STEP) }?.sort { it.sortIndex }?.each {
            if (it.completeConditionType == GanttItem.ConditionType.MANUAL) {
                ActionItem ai = createActionItem(it, gantt.executedConfiguration, startDate, null, gantt.executedConfiguration.owner, report)
                ai.publisherReport = report
                //  previous = ai
                //  previousDate = ai.dueDate
                CRUDService.save(ai)
            } else if (!first) {
                item = createGanttItem(it, gantt.executedConfiguration, previousDate, gantt.executedConfiguration.owner, report)
                item.depend = previous?.uuid
                item.bingingId = report.id
                item.parent = gantt.executedConfiguration.id + "_" + GanttItem.TaskType.PUB_FULL_FLOW_STEP.name()
                previous = item
                previousDate = previous?.endDate
                gantt.addToGanttItems(item)
            }
        }
        CRUDService.save(gantt)
        if (!report.dueDate) {
            report.dueDate = gantt.executedConfiguration.dateCreated.plus(gantt.defaultReportDuration + gantt.defaultSectionDuration + gantt.defaultFullDuration)
        }
        CRUDService.save(report)

    }

    void createSectionStage(Gantt gantt, Gantt newInstance, PublisherConfigurationSection section, ExecutedPeriodicReportConfiguration cfg, Date startDate, def _previous) {
        Date previousDate = startDate
        def previous = _previous
        List items = newInstance ? gantt.getPubSectionStage() : gantt?.ganttItems?.findAll { it.template && (it.taskType == GanttItem.TaskType.PUB_SEC_FLOW_STEP) }?.sort { it.sortIndex }

        items?.each {
            if (it.completeConditionType == GanttItem.ConditionType.MANUAL) {
                ActionItem ai = createActionItem(it, cfg, startDate, null, cfg.owner, section)
                ai.publisherSection = section
                // previous = ai
                // previousDate = ai.dueDate
                CRUDService.save(ai)
            } else {
                GanttItem item = createGanttItem(it, cfg, previousDate, cfg.owner, section)
                item.depend = previous?.uuid
                item.bingingId = section.id
                item.parent = cfg.id + "_" + GanttItem.TaskType.PUB_SEC_FLOW_STEP.name()
                previous = item
                previousDate = previous?.endDate
                if (newInstance)
                    newInstance.addToGanttItems(item)
                else
                    gantt.addToGanttItems(item)
            }
        }
        if (!section.dueDate) {
            section.dueDate = cfg.dateCreated.plus(gantt.defaultReportDuration + gantt.defaultSectionDuration)
        }
    }

    Gantt createForReport(Gantt template, ExecutedPeriodicReportConfiguration cfg, Date startDate, User owner) {
        if (!template) return null
        Gantt newInstance = new Gantt(
                name: template.name,
                executedConfiguration: cfg,
                isDeleted: false,
                defaultAiDuration: template.defaultAiDuration,
                defaultSubmissionDuration: template.defaultSubmissionDuration,
                defaultReportDuration: template.defaultReportDuration,
                defaultSectionDuration: template.defaultSectionDuration,
                defaultFullDuration: template.defaultFullDuration,
        )
        def previous
        Date previousDate = startDate
        template.getReportStage()?.each {
            if (it.completeConditionType == GanttItem.ConditionType.MANUAL) {
                ActionItem ai = createActionItem(it, cfg, startDate, null, owner)
                //  previous = ai
                //   previousDate = ai.dueDate
                cfg.addToActionItems(ai)
                CRUDService.save(ai)
            } else {
                GanttItem item = createGanttItem(it, cfg, previousDate, owner)
                item.depend = previous?.uuid
                item.parent = cfg.id + "_" + GanttItem.TaskType.REPORT_FLOW_STEP.name()
                previous = item
                previousDate = previous?.endDate
                newInstance.addToGanttItems(item)
            }
        }
        cfg.publisherConfigurationSections.each { PublisherConfigurationSection section ->
            createSectionStage(template, newInstance, section, cfg, cfg.dateCreated.plus(template.defaultReportDuration), null)
        }
        previousDate = cfg.dateCreated.plus(template.defaultReportDuration + template.defaultSectionDuration)
        previous = null
        template.getFullPublisherStage()?.each {
            if (it.completeConditionType != GanttItem.ConditionType.MANUAL) {
                GanttItem item = createGanttItem(it, cfg, previousDate, owner)
                item.depend = previous?.uuid
                item.parent = cfg.id + "_" + GanttItem.TaskType.PUB_FULL_FLOW_STEP.name()
                previous = item
                previousDate = previous?.endDate
                newInstance.addToGanttItems(item)
            }

        }

        newInstance.addToGanttItems(new GanttItem(
                name: ViewHelper.getMessage("app.label.gantt.stage.submissionStage.submit"),
                duration: template.defaultSubmissionDuration,
                startDate: cfg.dateCreated.plus(template.defaultReportDuration + template.defaultSectionDuration + template.defaultReportDuration),
                endDate: cfg.dateCreated.plus(template.defaultReportDuration + template.defaultSectionDuration + template.defaultReportDuration + template.defaultSubmissionDuration),
                assignedTo: owner,
                sortIndex: previous.sortIndex + 1,
                taskType: GanttItem.TaskType.SUBMISSION,
                parent: cfg.id + "_" + GanttItem.TaskType.SUBMISSION.name(),
                depend: null,
                uuid: cfg.id + "_" + template.id

        ))

        template.ganttItems?.each {
            newInstance.addToGanttItems(new GanttItem(
                    name: it.name,
                    duration: it.duration,
                    sortIndex: it.sortIndex,
                    completeConditionType: it.completeConditionType,
                    taskType: it.taskType,
                    assignedTo: it.assignedTo,
                    assignedGroupTo: it.assignedGroupTo,
                    bingingId: it.bingingId,
                    completeCondition: it.completeCondition,
                    parent: it.parent,
                    depend: it.depend,
                    uuid: it.uuid,
                    template: true
            ))
        }
        return newInstance

    }

    ActionItem createActionItem(GanttItem step, ExecutedPeriodicReportConfiguration cfg, Date startDate, String depend, User owner, def section = null) {
        ActionItem ai = new ActionItem(
                actionCategory: ActionItemCategory.findByKey("PERIODIC_REPORT"),
                description: step.name,
                assignedTo: step.assignedTo,
                assignedGroupTo: step.assignedGroupTo,
                priority: PriorityEnum.MEDIUM,
                status: StatusEnum.OPEN,
                appType: AppTypeEnum.PERIODIC_REPORT,
                depend: depend,
                uuid: cfg.id + "_" + (section ? (section.id + "_") : "") + step.id,
                startDate: startDate,
                createdBy: "Application",
                modifiedBy: "Application",
                dateCreated: new Date(),
                lastUpdated: new Date()
        )
        ai.dueDate = startDate.plus(step.duration)
        if (!ai.assignedTo && !ai.assignedGroupTo) ai.assignedTo = owner
        return ai
    }

    GanttItem createGanttItem(GanttItem step, ExecutedPeriodicReportConfiguration cfg, Date startDate, User owner, def section = null) {
        GanttItem item = new GanttItem(step.properties)
        item.id = null
        item.version = null
        item.gantt = null
        item.template = false
        item.uuid = cfg.id + "_" + (section ? (section.id + "_") : "") + step.id
        item.startDate = startDate
        item.endDate = startDate.plus(item.duration)
        if (!item.assignedTo && !item.assignedGroupTo) item.assignedTo = owner
        return item
    }

}

