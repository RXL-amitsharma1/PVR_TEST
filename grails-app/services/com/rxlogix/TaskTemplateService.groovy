package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.enums.TaskTemplateTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import org.apache.commons.lang3.time.DateUtils

@Transactional
class TaskTemplateService {

    def CRUDService
    def userService
    def actionItemService

    /**
     * Service method to find the tasks.
     * @param taskTemplateId
     * @return
     */
    def findTasks(taskTemplateId) {

        def tasks = {}
        if (taskTemplateId) {
            def taskTemplate = TaskTemplate.get(taskTemplateId)
            if (taskTemplate) {
                tasks = Task.findAllByTaskTemplate(taskTemplate)?.collect {
                    it.toTaskDto()
                }
            }
        }
        tasks
    }

    @NotTransactional
    List<PublisherSectionTask> fetchPublisherSectionTasksFromRequest(params) {
        List<ReportTask> taskList = []
        //(i starts with 1 because 0 has empty values from hidden block).
        for (int i = 1; i < params.list('aiDescription')?.size(); i++) {
            PublisherSectionTask task = new PublisherSectionTask()
            task.description = params.list("aiDescription").get(i)
            task.actionCategory = ActionItemCategory.findByKey('PERIODIC_REPORT')
            task.baseDate = PublisherSectionTask.BaseDate.valueOf(params.list("baseDate")?.get(i) ?: PublisherSectionTask.BaseDate.CREATION_DATE.name())
            String assignedTo = params.list("aiAssignedTo").get(i)
            if (assignedTo) {
                if (assignedTo in PublisherSectionTask.AssignToType.asStringList()) {
                    task.assignToType = PublisherSectionTask.AssignToType.valueOf(assignedTo)
                } else {
                    task.assignToType = PublisherSectionTask.AssignToType.USER
                    if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                        task.assignedGroupTo = UserGroup.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                        task.assignedTo = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                    }
                }
            }
            def shift = params.list("aiDueDateShift").get(i)
            task.dueDateShift = (shift?.isInteger() ? shift as Integer : 0)
            if (params.list("sign").get(i) == "-") task.dueDateShift = -task.dueDateShift
            task.priority = params.list("aiPriority").get(i)
            task.appType = AppTypeEnum.PERIODIC_REPORT
            User user = userService.currentUser
            task.createdBy = user.username
            task.modifiedBy = user.username
            taskList.add(task)
        }
        return taskList
    }


    @NotTransactional
    List<ReportTask> fetchReportTasksFromRequest(params) {
        List<ReportTask> taskList = []
        //(i starts with 1 because 0 has empty values from hidden block).
        for (int i = 1; i < params.list('aiDescription')?.size(); i++) {
            ReportTask task = new ReportTask()
            task.description = params.list("aiDescription").get(i)
            task.baseDate = ReportTask.BaseDate.valueOf(params.list("baseDate")?.get(i) ?: ReportTask.BaseDate.CREATION_DATE.name())
            task.actionCategory = ActionItemCategory.findByKey('PERIODIC_REPORT')
            String assignedTo = params.list("aiAssignedTo").get(i)
            if (assignedTo) {
                if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                    task.assignedGroupTo = UserGroup.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                    task.assignedTo = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                }
            }
            def shift = params.list("aiDueDateShift").get(i)
            task.dueDateShift = (shift?.isInteger() ? shift as Integer : 0)
            if (params.list("sign").get(i) == "-") task.dueDateShift = -1 * task.dueDateShift
            if (params.list("aiBeforeAfter").get(i) == "BEFORE") {
                shift = params.list("aiCreateDateShift").get(i)
                task.createDateShift = shift?.isInteger() ? shift as Integer : 0
            } else {
                task.createDateShift = 0
            }

            task.priority = params.list("aiPriority").get(i)
            task.appType = AppTypeEnum.PERIODIC_REPORT
            User user = userService.currentUser
            task.createdBy = user.username
            task.modifiedBy = user.username
            taskList.add(task)
        }
        return taskList
    }

    def createActionItems(ReportConfiguration configuration, ExecutedReportConfiguration executedConfiguration) {
        ReportTask.listTasksForReportConfiguration(configuration.id).list().each { ReportTask template ->
            if (template.createDateShift == 0) {
                ActionItem actionItem = createActionItem(template, null, executedConfiguration)
                executedConfiguration.addToActionItems(actionItem)
                CRUDService.save(actionItem)

                def emailSubject = ViewHelper.getMessage('app.notification.actionItem.email.created')
                if (actionItem.status == StatusEnum.CLOSED) {
                    emailSubject = ViewHelper.getMessage('app.notification.actionItem.email.closed')
                }
                actionItemService.sendActionItemNotification(actionItem, 'create', null, emailSubject)
            }
        }
        ActionItem.findAllByIsDeletedAndConfigurationAndAppTypeInList(false, configuration, [AppTypeEnum.PERIODIC_REPORT, AppTypeEnum.ADHOC_REPORT]).each { ai ->
            executedConfiguration.addToActionItems(ai)
            ai.configuration = null
        }
    }

    ActionItem createActionItem(def template, ReportConfiguration configuration, ExecutedReportConfiguration executedConfiguration, Date sectionDueDate = null) {
        boolean isDueDate = ((template instanceof ReportTask) && (configuration instanceof PeriodicReportConfiguration)) ? template.baseDate == ReportTask.BaseDate.DUE_DATE : template.baseDate == PublisherSectionTask.BaseDate.DUE_DATE
        boolean isCreationDate = template instanceof ReportTask ? template.baseDate == ReportTask.BaseDate.CREATION_DATE : template.baseDate == PublisherSectionTask.BaseDate.CREATION_DATE
        boolean isReportingPeriodStart = template instanceof ReportTask ? template.baseDate == ReportTask.BaseDate.REPORT_PERIOD_START : template.baseDate == PublisherSectionTask.BaseDate.REPORT_PERIOD_START
        boolean isReportingPeriodEnd = template instanceof ReportTask ? template.baseDate == ReportTask.BaseDate.REPORT_PERIOD_END : template.baseDate == PublisherSectionTask.BaseDate.REPORT_PERIOD_END
        boolean isSectionDueDate = (template instanceof PublisherSectionTask) && template.baseDate == PublisherSectionTask.BaseDate.SECTION_DUE_DATE
        boolean isAsOfDate = (template instanceof ReportTask) && template.baseDate == ReportTask.BaseDate.AS_OF_DATE
        User owner = configuration ? configuration.owner : executedConfiguration.owner
        AppTypeEnum appType = AppTypeEnum.ADHOC_REPORT
        ActionItemCategory actionCategory = ActionItemCategory.findByKey('ADHOC_REPORT')
        if(executedConfiguration instanceof ExecutedPeriodicReportConfiguration || configuration instanceof PeriodicReportConfiguration){
            appType=AppTypeEnum.PERIODIC_REPORT
            actionCategory = ActionItemCategory.findByKey('PERIODIC_REPORT')
        }
        ActionItem ai = new ActionItem(
                actionCategory: actionCategory,
                description: template.description,
                assignedTo: (!template.assignedTo && !template.assignedGroupTo) ? owner : template.assignedTo,
                assignedGroupTo: template.assignedGroupTo,
                priority: template.priority,
                status: StatusEnum.OPEN,
                appType: appType,
                createdBy: owner.username,
                modifiedBy: owner.username,
        )

        Date now = new Date()
        Date baseDate = now
        if (configuration) {
            if (isDueDate) {
                if (configuration.gantt)
                    baseDate = now + configuration.gantt.dueDays
                else
                    baseDate = configuration.globalDateRangeInformation.getReportStartAndEndDate()[1] + (configuration.dueInDays ?: 0)
            } else if (isReportingPeriodStart) {
                baseDate = configuration.globalDateRangeInformation.getReportStartAndEndDate()[0]
            } else if (isReportingPeriodEnd) {
                baseDate = configuration.globalDateRangeInformation.getReportStartAndEndDate()[1]
            } else if (isAsOfDate) {
                if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                    baseDate = configuration.asOfVersionDate
                } else if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD) {
                    baseDate = configuration.globalDateRangeInformation.getReportStartAndEndDate()[1]
                } else {
                    baseDate = configuration.nextRunDate
                }

            }
        } else {
            if (isDueDate) {
                baseDate = executedConfiguration.dueDate
            } else if (isReportingPeriodStart) {
                baseDate = executedConfiguration.executedGlobalDateRangeInformation.getReportStartAndEndDate()[0]
            } else if (isReportingPeriodEnd) {
                baseDate = executedConfiguration.executedGlobalDateRangeInformation.getReportStartAndEndDate()[1]
            } else if (isSectionDueDate && sectionDueDate) {
                baseDate = sectionDueDate
            } else if (isAsOfDate) {
                if (executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                    baseDate = executedConfiguration.asOfVersionDate
                } else if (executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
                    baseDate = executedConfiguration.lastRunDate
                } else if( executedConfiguration.evaluateDateAs in [EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD, EvaluateCaseDateEnum.ALL_VERSIONS]) {
                    baseDate = executedConfiguration.getExecutedAsOfVersionDate()
                }
            }
        }
        Date dueDate = DateUtils.addDays(baseDate, template.dueDateShift)
        ai.dueDate = dueDate ? DateUtil.getDateWithDayEndTime(dueDate) : null
        return ai
    }

    def createPublisherActionItems(ExecutedReportConfiguration executedConfiguration) {
        User owner = executedConfiguration.owner
        executedConfiguration.publisherConfigurationSections?.each { PublisherConfigurationSection section ->
            section.taskTemplate?.publisherSectionTasks?.each { PublisherSectionTask task ->
                ActionItem actionItem = createActionItem(task, null, executedConfiguration, section.dueDate)

                if (task.assignToType == PublisherSectionTask.AssignToType.CONTRIBUTOR) actionItem.assignedTo = executedConfiguration.primaryPublisherContributor ?: owner
                if (task.assignToType == PublisherSectionTask.AssignToType.APPROVER) actionItem.assignedTo = section.approver ?: owner
                if (task.assignToType == PublisherSectionTask.AssignToType.REVIEWER) actionItem.assignedTo = section.reviewer ?: owner
                if (task.assignToType == PublisherSectionTask.AssignToType.AUTHOR) actionItem.assignedTo = section.author ?: owner
                if (!actionItem.assignedTo && !actionItem.assignedGroupTo) actionItem.assignedTo = owner
                section.addToActionItems(actionItem)
                CRUDService.save(actionItem)
            }
        }
    }

    def createActionItemForScheduledTasks() {
        long t = ((new Date()).getTime() / 1000 as Long) * 1000
        Date now = new Date(t)
        now.set(minute: 0, second: 0)

        ReportConfiguration.fetchSheduledConfigurations.list().each { ReportConfiguration reportConfiguration ->

            Date runDate = reportConfiguration.nextRunDate

            if ((runDate > now) && (reportConfiguration.reportTasks?.size() > 0)) {
                use(TimeCategory) {
                    def delta = (runDate - now)
                    reportConfiguration.reportTasks.each { task ->
                        if ((task.createDateShift > 0) && (delta.hours == 0) && (delta.minutes >= 0) && (delta.seconds >= 0) && (delta.millis >= 0)) {
                            if (task.createDateShift == delta.days) {
                                ActionItem actionItem = createActionItem(task, reportConfiguration, null)
                                actionItem.configuration = reportConfiguration
                                CRUDService.save(actionItem)
                                def emailSubject = ViewHelper.getMessage('app.notification.actionItem.email.created')
                                if (actionItem.status == StatusEnum.CLOSED) {
                                    emailSubject = ViewHelper.getMessage('app.notification.actionItem.email.closed')
                                }
                                actionItemService.sendActionItemNotification(actionItem, 'create', null, emailSubject)
                            }
                        }
                    }
                }
            }
        }
    }

}
