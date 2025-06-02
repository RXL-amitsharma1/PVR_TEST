package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.ActionItemFilterEnum
import com.rxlogix.enums.ActionItemCategoryEnum
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import groovy.transform.CompileStatic
import org.hibernate.Criteria
import org.springframework.web.context.request.RequestContextHolder

import java.text.SimpleDateFormat

@CollectionSnapshotAudit
class ActionItem {

    transient def userService

    static auditable =  [ignore:['deleted', 'newObj']]
    //Action Item fields
    ActionItemCategory actionCategory
    @AuditEntityIdentifier
    String description
    String comment
    User assignedTo
    UserGroup assignedGroupTo
    Date dueDate
    Date completionDate
    String priority
    StatusEnum status
    AppTypeEnum appType
    ReportConfiguration configuration
    String parentEntityKey
    PublisherConfigurationSection publisherSection
    PublisherReport publisherReport

    //gantt
    String depend
    String uuid
    Date startDate

    //action plan
    Date dateRangeFrom
    Date dateRangeTo

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    //Transient fields
    boolean deleted
    boolean newObj
    boolean isDeleted = false

    static constraints = {
        assignedGroupTo(nullable: true)
        assignedTo(nullable: true)
        dueDate(bindable: false)
        completionDate(nullable: true, bindable: false, validator: { val, obj ->
            if (obj.status == StatusEnum.CLOSED && !val) {
                return "app.action.item.completionDate.not.selected"
            }
        })
        assignedTo(validator: { val, obj ->
            if (!obj.assignedTo && !obj.assignedGroupTo)
                return "com.rxlogix.config.ReportRequest.assignedTo.nullable"
        })
        appType(nullable: true)
        deleted(bindable: true)
        newObj(bindable: true)
        description(maxSize: 8000)
        comment(maxSize: 8000, nullable: true)
        configuration(nullable: true)
        parentEntityKey(nullable: true)
        dateRangeFrom(nullable: true)
        dateRangeTo(nullable: true)
        publisherSection(nullable: true)
        publisherReport(nullable: true)
        depend(nullable: true)
        startDate(nullable: true)
        uuid(nullable: true)
    }

    static mapping = {
        table("ACTION_ITEM")
        isDeleted column: "IS_DELETED"
        dateRangeFrom column: "period_from"
        dateRangeTo column: "period_to"
        configuration column: "CONFIG_ID"
        publisherSection column: "PUBLISHER_SECTION_ID"
        publisherReport column: "PUBLISHER_REPORT_ID"
        startDate column: "START_DATE"
        depend column: "DEPEND"
        uuid column: "UUID"
        comment column: "COMMNT"
        autoTimestamp false // required to maintain ordering of Action Items when creating report request.
    }

    static beforeInsert = {
        dateCreated = dateCreated ?: new Date()
        lastUpdated = new Date()
    }

    static beforeUpdate = {
        lastUpdated = new Date()
    }

    static belongsTo = [ReportRequest, ExecutedPeriodicReportConfiguration, PublisherConfigurationSection, QualityCaseData, QualitySampling, QualitySubmission, DrilldownCLLData]

    static transients = ['deleted', 'newObj']


    def toActionItemMap() {
        [
                actionItemId  : id,
                description   : description,
                actionCategory: actionCategory.key,
                assignedTo    : assignedToName(),
                assignedToId  : assignedTo ? (Constants.USER_TOKEN + assignedTo?.id) : (Constants.USER_GROUP_TOKEN + assignedGroupTo?.id),
                completionDate: completionDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                dueDate       : dueDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                dateCreated   : dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                priority      : priority,
                status        : status?.key,
                createdBy     : createdBy,
                appType       : ViewHelper.getMessage("app.actionItemAppType." + appType),
                version       : version,
                actionItemCategory: ViewHelper.getMessage(actionCategory.getI18nKey()),
                parentEntityKey: parentEntityKey,
                comment: comment
        ]
    }

    String getInstanceIdentifierForAuditLog() {
        return (description.length() < 50 ? description : (description.substring(0, 49) + "..."))
    }

    Map getReasonOfDelayData(boolean isInbound=false) {
        Map result = [:]
        DrilldownMetadata metadataRecord
        if(isInbound) {
            metadataRecord = InboundDrilldownMetadata.getByActionItem(this.id).get()
        }else {
            metadataRecord = DrilldownCLLMetadata.getByActionItem(this.id).get()
        }
        if (metadataRecord) {
            Map metadataParams = [:]
            metadataParams['masterCaseId'] = metadataRecord.caseId
            metadataParams['masterEnterpriseId'] = metadataRecord.tenantId
            if (metadataRecord instanceof DrilldownCLLMetadata) {
                metadataParams['vcsProcessedReportId'] = metadataRecord.processedReportId
            }
            else if (metadataRecord instanceof InboundDrilldownMetadata) {
                metadataParams['senderId'] = metadataRecord.senderId
                metadataParams['masterVersionNum'] = metadataRecord.caseVersion
            }
            Long latestReportId
            if (this.configuration) {
                List<ExecutionStatus> list = ExecutionStatus.findAllByEntityIdAndExecutionStatusInList(this.configuration.id, ReportExecutionStatusEnum.getCompletedStatusesList(), [sort: 'id', order: 'desc'])
                if (list.size() > 0) {
                    latestReportId = list[0].executedEntityId
                }
            }
            DrilldownCLLData cllRecord = Holders.getApplicationContext().getBean("actionItemService").getDrilldownRecordForMetadataActionItem(metadataParams, latestReportId, isInbound)
            if (cllRecord) {
                result.drilldownRecordCaseNum = JSON.parse(cllRecord.cllRowData).masterCaseNum
                result.drilldownRecordId = cllRecord.id
                result.drilldownReportId = cllRecord.reportResultId
                result.drilldownReportName = ReportResult.read(cllRecord.reportResultId)?.drillDownSource?.executedConfiguration?.reportName
            }
        }
        return result
    }

    static namedQueries = {
        fetchActionItemsForPublisherExecutedReport { String search, Long executedReportId, List<Long> sectionsId, List<Long> publisherIds, User user = null ->
            eq('isDeleted', false)
            createAlias('assignedTo', 'assignedTo', Criteria.LEFT_JOIN)
            createAlias('assignedGroupTo', 'assignedGroupTo', Criteria.LEFT_JOIN)
            if (search) {
                or {
                    iLikeWithEscape("description", "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('assignedTo.fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('assignedGroupTo.name', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            if (user) {
                or {
                    eq('assignedTo.id', user.id)
                    assignedToUserGroup(user)
                }
            }
            or {
                if (publisherIds)
                    'in'("publisherReport.id", publisherIds)
                if (sectionsId)
                    'in'("publisherSection.id", sectionsId)
                'in'('id', new DetachedCriteria(ExecutedReportConfiguration).build {
                    createAlias("actionItems", "actionItemsAlias")
                    projections {
                        property("actionItemsAlias.id")
                    }
                    idEq(executedReportId)
                })
            }
        }

//Fetch the action items list based on the current logged in user and filterType..
        getActionItemList { String filterType, User user, Long executedReportId, Long sectionId, Long publisherId ->
            if (user) {
                eq('isDeleted', false)
                if (filterType == ActionItemFilterEnum.MY_OPEN.value) {
                    //We need to show all the action items assigned to passed user and whose status is not closed.
                    or {
                        eq('assignedTo.id', user.id)
                        assignedToUserGroup(user)
                    }
                    ne('status', StatusEnum.CLOSED)
                } else if (filterType == ActionItemFilterEnum.MY_ALL.value) {
                    //We need to show all the action items assigned to passed user.
                    or {
                        eq('assignedTo.id', user.id)
                        assignedToUserGroup(user)
                    }
                } else if (sectionId) {
                    eq("publisherSection.id", sectionId)
                } else if (publisherId) {
                    eq("publisherReport.id", publisherId)
                } else if (filterType == ActionItemFilterEnum.EXECUTED_REPORT_ALL.value) {
                    'in'('id', new DetachedCriteria(ExecutedReportConfiguration).build {
                        createAlias("actionItems", "actionItemsAlias")
                        projections {
                            property("actionItemsAlias.id")
                        }
                        idEq(executedReportId)
                    })
                }
            }
        }

        fetchActionItemsBySearchString { LibraryFilter filter, String filterType, User user, Long executedReportId, Long sectionId, Long publisherId, Boolean pvq = false, String sortBy = null, String sortDirection = "asc" ->

            getActionItemList(filterType, user, executedReportId, sectionId, publisherId)
            if (pvq) {
                'in'('actionCategory.id', ActionItemCategory.findAllWhere(forPvq: true)*.id)
            } else {
                'in'('actionCategory.id', ActionItemCategory.findAll()*.id)
            }
            fetchByFilter(filter)
            createAlias('assignedTo', 'assignedTo', Criteria.LEFT_JOIN)
            createAlias('assignedGroupTo', 'assignedGroupTo', Criteria.LEFT_JOIN)
            if (sortBy) {
                if (sortBy == 'assignedTo') {
                    order(OrderByUtil.orOrderIgnoreCase(["assignedToFullName", "assignedToUsername", "assignedGroupToName"], sortDirection))
                } else if (sortBy == 'actionCategory') {
                    order(OrderByUtil.mapOrderIgnoreCase("actionCategoryKey", [
                            (ActionItemCategoryEnum.REPORT_REQUEST.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.REPORT_REQUEST.i18nKey)),
                            (ActionItemCategoryEnum.REQUEST_MISSING_INFORMATION.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.REQUEST_MISSING_INFORMATION.i18nKey)),
                            (ActionItemCategoryEnum.PROCESS_CASE.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.PROCESS_CASE.i18nKey)),
                            (ActionItemCategoryEnum.PERIODIC_REPORT.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.PERIODIC_REPORT.i18nKey)),
                            (ActionItemCategoryEnum.CONFIGURE_REPORT.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.CONFIGURE_REPORT.i18nKey)),
                            (ActionItemCategoryEnum.ADHOC_REPORT.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.ADHOC_REPORT.i18nKey)),
                            (ActionItemCategoryEnum.REVIEW_REPORT.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.REVIEW_REPORT.i18nKey)),
                            (ActionItemCategoryEnum.QUALITY_MODULE.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.QUALITY_MODULE.i18nKey)),
                            (ActionItemCategoryEnum.QUALITY_MODULE_PREVENTIVE.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.QUALITY_MODULE_PREVENTIVE.i18nKey)),
                            (ActionItemCategoryEnum.QUALITY_MODULE_CORRECTIVE.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.QUALITY_MODULE_CORRECTIVE.i18nKey)),
                            (ActionItemCategoryEnum.DRILLDOWN_RECORD.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.DRILLDOWN_RECORD.i18nKey)),
                            (ActionItemCategoryEnum.IN_DRILLDOWN_RECORD.name()) : (ViewHelper.getMessage(ActionItemCategoryEnum.IN_DRILLDOWN_RECORD.i18nKey))
                    ], sortDirection))
                } else if (sortBy == 'actionItemId') {
                    order("id", "${sortDirection}")
                } else if (sortBy == 'status') {
                    order(OrderByUtil.mapOrderIgnoreCase(sortBy, [
                            (StatusEnum.OPEN.name()) : (ViewHelper.getMessage(StatusEnum.OPEN.i18nKey)),
                            (StatusEnum.IN_PROGRESS.name()) : (ViewHelper.getMessage(StatusEnum.IN_PROGRESS.i18nKey)),
                            (StatusEnum.NEED_CLARIFICATION.name()) : (ViewHelper.getMessage(StatusEnum.NEED_CLARIFICATION.i18nKey)),
                            (StatusEnum.CLOSED.name()) : (ViewHelper.getMessage(StatusEnum.CLOSED.i18nKey))
                    ], sortDirection))
                } else if (sortBy == 'appType') {
                    order(OrderByUtil.mapOrderIgnoreCase(sortBy, [
                            (AppTypeEnum.REPORT_REQUEST.name()) : (ViewHelper.getMessage(AppTypeEnum.REPORT_REQUEST.i18nKey)),
                            (AppTypeEnum.PERIODIC_REPORT.name()) : (ViewHelper.getMessage(AppTypeEnum.PERIODIC_REPORT.i18nKey)),
                            (AppTypeEnum.ADHOC_REPORT.name()) : (ViewHelper.getMessage(AppTypeEnum.ADHOC_REPORT.i18nKey)),
                            (AppTypeEnum.QUALITY_MODULE.name()) : (ViewHelper.getMessage(AppTypeEnum.QUALITY_MODULE.i18nKey)),
                            (AppTypeEnum.QUALITY_MODULE_CAPA.name()) : (ViewHelper.getMessage(AppTypeEnum.QUALITY_MODULE_CAPA.i18nKey)),
                            (AppTypeEnum.DRILLDOWN_RECORD.name()) : (ViewHelper.getMessage(AppTypeEnum.DRILLDOWN_RECORD.i18nKey)),
                            (AppTypeEnum.IN_DRILLDOWN_RECORD.name()) : (ViewHelper.getMessage(AppTypeEnum.IN_DRILLDOWN_RECORD.i18nKey)),
                            (AppTypeEnum.PV_CENTRAL_CAPA.name()) : (ViewHelper.getMessage(AppTypeEnum.PV_CENTRAL_CAPA.i18nKey)),
                            (AppTypeEnum.ACTION_PLAN.name()) : (ViewHelper.getMessage(AppTypeEnum.ACTION_PLAN.i18nKey))
                    ], sortDirection))
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        fetchByFilter { LibraryFilter filter ->
            projections {
                distinct('id')
                property("actionCategory")
                property("description")
                property("priority")
                property("dueDate")
                property("completionDate")
                property("createdBy")
                property("dateCreated")
                property("lastUpdated")
                property("status")
                property("appType")
                'actionCategory' {
                    property("key", "actionCategoryKey")
                }
                property("assignedTo.fullName", "assignedToFullName")
                property("assignedTo.username", "assignedToUsername")
                property("assignedGroupTo.name", "assignedGroupToName")
            }
            fetchByFilterQuery(filter)
        }



        fetchByFilterQuery { LibraryFilter filter ->
            eq('isDeleted', false)
            if (filter.search) {
                or {
                    iLikeWithEscape("description", "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('assignedTo.fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('assignedGroupTo.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                }
            }
            Map assignedTo = filter.sharedWith
            if (assignedTo) {
                if (assignedTo.groupsId) {
                    eq('assignedGroupTo.id', assignedTo.groupsId[0])
                }
                if (assignedTo.usersId) {
                    or {
                        'eq'('assignedTo.id', assignedTo.usersId[0])
                        'in'('assignedGroupTo.id', new DetachedCriteria(UserGroupUser).build {
                            projections {
                                distinct('userGroup.id')
                            }
                            'eq'('user.id', assignedTo.usersId[0])
                        })
                    }
                }
                if (assignedTo?.ownerId) {
                    eq('owner.id', assignedTo.ownerId)
                }
                if (assignedTo?.team && filter.user) {
                    or {
                        filter.user.getUserTeamIds()?.collate(999)?.each { 'in'('assignedTo.id', it) }
                    }
                }
            }

            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
        }

        getActionItemsForUserBetweenDates { User user, Date startDate, Date endDate ->
            eq('isDeleted', false)
            gte('dueDate', startDate)
            lte('dueDate', endDate)
            if (!user.isAdmin()) {
                or {
                    eq('assignedTo.id', user.id)
                    eq('createdBy', user.username) // TODO need to add OWNER field as well
                    assignedToUserGroup(user)
                }
            }
        }

        assignedToUserGroup { User user ->
            'in'('assignedGroupTo.id', new DetachedCriteria(UserGroupUser).build {
                projections {
                    distinct('userGroup.id')
                }
                eq('user.id', user.id as Long)
            })
        }

    }

    String assignedToName() {
        return (assignedTo ? (assignedTo?.fullName ?: assignedTo?.username) : assignedGroupTo?.name)
    }

    Set<User> getAssignedToUserList() {
        Set<User> users = (assignedTo ? [assignedTo] : (assignedGroupTo && !assignedGroupTo.isDeleted ? assignedGroupTo.users.flatten() : []))
        users = users?.findAll { it && it.enabled }
        users
    }

    static getSummary(User user) {
        String sql = "select SUM(1) as total, " +
                "SUM(CASE WHEN (dueDate<:date and status<>:closed) THEN 1 ELSE 0 END) as overdue, " +
                "SUM(CASE WHEN (dueDate>:date and dueDate<:dateDueSoon and status<>:closed) THEN 1 ELSE 0 END) as duesoon, " +
                "SUM(CASE WHEN status=:inprogress THEN 1 ELSE 0 END) as inprogress, " +
                "SUM(CASE WHEN status=:open THEN 1 ELSE 0 END) as opened  " +
                " from ActionItem where isDeleted=false and (assignedTo=:user or assignedGroupTo in (select userGroup from UserGroupUser where user=:user)) "
        Date now = new Date()
        def result = ActionItem.executeQuery(sql, [user: user, date: now, dateDueSoon: now + 30, inprogress: StatusEnum.IN_PROGRESS, open: StatusEnum.OPEN, closed:StatusEnum.CLOSED ])[0]
        [total: result[0], overDue: result[1], dueSoon: result[2], inProgress: result[3], opened: result[4]]
    }

    @Override
    public String toString() {
        return description
    }


    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        def params = RequestContextHolder?.requestAttributes?.params ?: [:]
        if (newValues && (oldValues == null)) {
            processReport(params, newValues)
            processQualityData(params, newValues)
            processCapa(params, newValues)
            processMasterCase(params, newValues)
        }
        return [newValues: newValues, oldValues: oldValues]
    }

    private void processQualityData(params, newValues) {
        if (params?.qualityId && params?.dataType) {
            withNewSession {
                def qualityObj
                switch (params.dataType) {
                    case PvqTypeEnum.CASE_QUALITY.toString():
                        qualityObj = QualityCaseData.get(params.long("qualityId"))
                        break
                    case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                        qualityObj = QualitySubmission.get(params.long("qualityId"))
                        break
                    default:
                        qualityObj = QualitySampling.get(params.long("qualityId"))
                        break
                }
                newValues.put("PVQ", "PVQ Observation:" + params.dataType + ", Case: " + qualityObj.caseNumber)
            }
        }
    }

    private void processReport(params, newValues) {
        if (params?.executedReportId) {
            withNewSession {
                newValues.put("executedReport", ExecutedReportConfiguration.get(params.executedReportId as Long)?.reportName)
            }
        }
    }

    private void processCapa(params, newValues) {
        if (params?.capaId) {
            withNewSession {
                newValues.put("issue", Capa8D.get(params.long("capaId"))?.issueNumber)
            }
        }
    }

    private void processMasterCase(params, newValues) {
        if (params?.masterCaseId) {
            newValues.put("pvc", "Case ID: " + params?.masterCaseId
                    + ((params.processedReportId && (params.processedReportId != "-1")) ? (", Report ID: " + params.processedReportId) : "")
                    + ((params.senderId && (params.senderId != "-1")) ? (", Sender ID: " + params.senderId + ", Version: " + params.masterVersionNum) : "")
                    + (params.tenantId ? (", Tenant ID: " + params.tenantId) : ""))
        }
    }
}
