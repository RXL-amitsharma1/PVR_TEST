package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.ReportRequestService
import com.rxlogix.ReportsJsonUtil
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import com.rxlogix.enums.ReportRequestFrequencyEnum
import org.hibernate.sql.JoinType
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type
import java.text.SimpleDateFormat
@CollectionSnapshotAudit
class ReportRequest {
    transient def reportRequestService
    static auditable =  true
    @AuditEntityIdentifier
    String reportName
    ReportRequestPriority priority
    String description
    ReportRequestType reportRequestType
    User assignedTo
    UserGroup assignedGroupTo
    Date dueDate
    Date asOfVersionDate
    Date startDate
    Date endDate
    Date completionDate
    User owner
    WorkflowState workflowState
    User primaryPublisherContributor
    String generatedReportName
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    String requestorsNames

    //Report related info fields.
    DateRangeType dateRangeType
    EvaluateCaseDateEnum evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean includeAllStudyDrugsCases = false
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean suspectProduct = false
    boolean limitPrimaryPath = false
    boolean includeMedicallyConfirmedCases = false
    boolean isDeleted = false
    String productSelection
    String productGroupSelection
    String studySelection
    String eventSelection
    String eventGroupSelection


    static hasMany = [requesters: User, requesterGroups: UserGroup, comments: ReportRequestComment, actionItems: ActionItem, attachments: ReportRequestAttachment, reportingDestinations: String,  publisherContributors: User]


    String psrTypeFile
    String inn
    String drugCode
    Date ibd
    Boolean masterPlanningRequest


    String requestorNotes
    String primaryReportingDestination
    String linkedConfigurations
    String linkedGeneratedReports
    Date reportingPeriodStart
    Date reportingPeriodEnd

    Date dueDateForDistribution
    Date curPrdDueDate

    Date previousPeriodStart
    Date previousPeriodEnd
    String previousPsrTypeFile

    Integer dueInToHa
    ReportRequestFrequencyEnum frequency
    Integer frequencyX
    Integer daysToDlp
    Integer occurrences
    Long parentReportRequest
    String periodCoveredByReport

    String customValues
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false

    static transients = ['requestorUserList', 'allRequestorNames']

    static constraints = {
        reportName nullable: false, maxSize: 255
        actionItems cascade: 'all-delete-orphan'
        actionItems(validator: { val, obj ->
            if (obj.actionItems?.any { !it.description }) {
                return "app.action.item.description.nullable"
            }
        })
        dueDateForDistribution(nullable: true)
        curPrdDueDate(nullable: true)
        periodCoveredByReport(nullable: true)
        psrTypeFile(nullable: true)
        comments(nullable: true)
        attachments(nullable: true)
        assignedTo(nullable: true)
        assignedGroupTo(nullable: true)
        description(length: 8000, nullable: true)
        productSelection(nullable: true)
        productGroupSelection(nullable: true)
        studySelection(nullable: true)
        eventSelection(nullable: true)
        eventGroupSelection(nullable: true)
        workflowState(nullable: true)
        inn(nullable: true)
        ibd(nullable: true)
        customValues(nullable: true)
        primaryPublisherContributor nullable: true
        publisherContributors nullable: true

        parentReportRequest(nullable: true)
        drugCode(nullable: true)
        reportingPeriodStart(nullable: true)
        reportingPeriodEnd(nullable: true)
        dueInToHa(nullable: true)
        frequency(nullable: true)
        frequencyX(nullable: true)
        daysToDlp(nullable: true)
        occurrences(nullable: true)
        requestorsNames(nullable: true)
        requestorNotes(nullable: true, length: 4000)
        primaryReportingDestination(nullable: true,validator: { val, obj ->
            if(obj.reportRequestType?.aggregate && !val) {
                return "com.rxlogix.config.PeriodicReportConfiguration.primaryReportingDestination.nullable"
            }
        })
        reportingDestinations(nullable: true)
        linkedConfigurations(nullable: true, length: 8000)
        linkedGeneratedReports(nullable: true, length: 8000)
        generatedReportName nullable: true, maxSize: 255
        priority(nullable: true)
        masterPlanningRequest(nullable: true)
        previousPeriodEnd(nullable: true)
        previousPeriodStart(nullable: true)
        previousPsrTypeFile(nullable: true)

        startDate(nullable: true, bindable: false, validator: { val, obj ->
            if (!val && obj.endDate) {
                return "app.report.request.startDate.not.selected"
            }
        })
        endDate(nullable: true, bindable: false, validator: { val, obj ->
            if (val && obj.startDate && val < obj.startDate) {
                return "app.report.request.endDate.less.than.startDate"
            }
        })
        dueDate(nullable: false, bindable: false)
        completionDate(nullable: true)
        asOfVersionDate(nullable: true, validator: { val, obj ->
            if (obj.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF && !val) {
                return "version.date.not.null"
            }
        })
        assignedTo(validator: { val, obj ->
            if (!obj.assignedTo && !obj.assignedGroupTo)
                return "com.rxlogix.config.ReportRequest.assignedTo.nullable"
        })
    }

    static mapping = {
        table("REPORT_REQUEST")
        asOfVersionDate column: "AS_OF_VERSION_DATE"
        isDeleted column: "IS_DELETED"
        drugCode column: "product_lead"
        requesterGroups column: "REQUESTOR_GROUPS"
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        productGroupSelection column: "PRODUCT_GROUP_SELECTION", sqlType: DbUtil.longStringType
        studySelection column: "STUDY_SELECTION", sqlType: DbUtil.longStringType
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        eventGroupSelection column: "EVENT_GROUP_SELECTION", sqlType: DbUtil.longStringType
        includeAllStudyDrugsCases column: "INCL_ALL_STUD_DRUG_CASES"
        dateRangeType column: "DATE_RANGE_TYPE"
        workflowState column: "WORKFLOW_STATE_ID"
        priority column: "PRIORITY_ID"
        includeMedicallyConfirmedCases column: "INCL_MEDICAL_CONFIRM_CASES"
        attachments cascade: "all-delete-orphan"
        reportingDestinations joinTable: [name: "REPORT_REQUEST_DESTS", column: "REPORT_DESTINATION", key: "REPORT_REQUEST_ID"]
        reportRequestType cascade: 'none'
        publisherContributors joinTable: [name: "REPORT_REQ_P_C_USERS", column: "USER_ID", key: "REPORT_REQ_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        primaryPublisherContributor column: "PRIMARY_P_CONTRIBUTOR"
        generatedReportName column: "GENERATED_RPT_NAME"
        isMultiIngredient column: "IS_MULTI_INGREDIENT"
        includeWHODrugs column: "INCLUDE_WHO_DRUGS"
    }

    static Map toReportRequestDtoFromFilter(def filteredFow, List customFields) {
        Map customValuesValues = filteredFow[25] ? JSON.parse(filteredFow[25])?.collectEntries { k, v -> [(k): v] } : [:]

        Map result = [
                reportRequestId       : filteredFow[0],
                masterPlanningRequest : filteredFow[26],
                requestName           : filteredFow[1],
                description           : filteredFow[2],
                assignedTo            : filteredFow[15] ?: (filteredFow[16] ?: filteredFow[17]),
                requesters            : filteredFow[31],
                parentReportRequest   : filteredFow[27],
                inn                   : filteredFow[5],
                drugCode              : filteredFow[6],
                recipient             : filteredFow[7],
                dueDate               : filteredFow[4] ? (filteredFow[4] as Date).format(DateUtil.DATEPICKER_UTC_FORMAT) : "",
                daysToDlp             : filteredFow[8],
                occurrences           : filteredFow[9],
                linkedGeneratedReports: filteredFow[20],
                priority              : filteredFow[18],
                status                : filteredFow[14],
                createdBy             : filteredFow[10],
                reportRequestType     : filteredFow[13],
                aggregate             : filteredFow[19],
                productSelection      : ReportsJsonUtil.getNameFieldFromSelectionJson(filteredFow[28]),
                eventSelection        : ReportsJsonUtil.getNameFieldFromSelectionJson(filteredFow[29]),
                psrTypeFile           : filteredFow[21],
                reportingPeriodStart  : filteredFow[22] ? (filteredFow[22] as Date)?.format(DateUtil.DATEPICKER_FORMAT) : "",
                dateCreated           : filteredFow[11] ? (filteredFow[11] as Date).format(DateUtil.DATEPICKER_UTC_FORMAT) : "",
                frequencyX            : filteredFow[23],
                frequency             : filteredFow[24] as ReportRequestFrequencyEnum,
                dueInToHa             : filteredFow[30]

        ]

        ReportRequest reportRequest = new ReportRequest(result)
        reportRequest.reportingPeriodStart = (filteredFow[22] as Date)
        reportRequest.reportingPeriodEnd = (filteredFow[31] as Date)
        result.reportingPeriodEnd = ReportRequestService.getReportEndDate(reportRequest)?.format(DateUtil.DATEPICKER_FORMAT)
        result.dueDateToHa = ReportRequestService.getDueDateToHa(reportRequest)
        customFields.each {
            if (it.startsWith("secondary")) {
                String[] v = customValuesValues[it]?.split("~")
                if (v?.size() > 1) {
                    result.put(it, v[1])
                    return
                }
            }
            result.put(it, customValuesValues[it] ?: "")
        }
        return result
    }
    def toReportRequestDto() {
        List customFields=[]
        ReportRequestField.findAllByIsDeletedAndShowInPlan(false,true)?.sort{it.id}?.each{
                    customFields<< it.name
                    if(it.fieldType == ReportRequestField.Type.CASCADE) customFields<< "secondary"+it.name
                }
        Map customValuesValues = customValues ? JSON.parse(customValues)?.collectEntries{ k, v->[(k):v]} : [:]
        Map result = [
                reportRequestId       : id,
                masterPlanningRequest : masterPlanningRequest,
                requestName           : reportName,
                description           : description,
                assignedTo            : assignedToName(),
                requesters            : requestorList,
                parentReportRequest   : parentReportRequest,
                inn                   : inn,
                drugCode              : drugCode,
                recipient             : primaryReportingDestination,
                dueDate               : dueDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                daysToDlp             : daysToDlp,
                occurrences           : occurrences,
                linkedGeneratedReports: linkedGeneratedReports,
                priority              : priority?.name,
                status                : workflowState?.name,
                createdBy             : createdBy,
                reportRequestType     : reportRequestType.name,
                aggregate             : reportRequestType.aggregate,
                productSelection      : ReportsJsonUtil.getNameFieldFromSelectionJson(productSelection),
                eventSelection        : ReportsJsonUtil.getNameFieldFromSelectionJson(eventSelection),
                psrTypeFile           : psrTypeFile,
                reportingPeriodStart:reportingPeriodStart?.format(DateUtil.DATEPICKER_FORMAT),
                reportingPeriodEnd:reportRequestService.getReportEndDate(this)?.format(DateUtil.DATEPICKER_FORMAT),
                dueDateToHa           : reportRequestService.getDueDateToHa(this),
                dateCreated      : dateCreated?.format(DateUtil.DATEPICKER_UTC_FORMAT)

        ]
        customFields.each {
            if (it.startsWith("secondary")) {
                String[] v = customValuesValues[it]?.split("~")
                if (v?.size() > 1) {
                    result.put(it, v[1])
                    return
                }
            }
            result.put(it, customValuesValues[it] ?: "")
        }
        return result
    }

    static namedQueries = {
        fetchByWorkflowState { WorkflowState state, User user ->
            eq('isDeleted', false)
            eq('workflowState', state)
            assignedToOrRequesterOrOwner(user)
        }
        fetchByTerm { User user, String term ->
            eq('isDeleted', false)
            if (term) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(term)}%")
                    if (term.isNumber()) {
                        eq('id', Long.valueOf(term))
                    }
                }
            }
            assignedToOrRequesterOrOwner(user)
        }
        fetchByPriority { User user ->
            projections {
                groupProperty('priority.id')
                groupProperty('priority.name')
                countDistinct('id')
            }
            createAlias('priority', 'priority', org.hibernate.criterion.CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            assignedToOrRequesterOrOwner(user)
        }
        countByWidgetFilter { widgetFilter, User user ->
            projections {
                countDistinct("id")
            }
            fetchByWidgetFilterFilterQuery ( widgetFilter, user)
        }


        fetchByWidgetFilter { widgetFilter, User user ->
            projections {
                distinct('id')
                property("reportName")
                property("description")
                property("dueDate")
            }
            fetchByWidgetFilterFilterQuery ( widgetFilter, user)
            }
        fetchByWidgetFilterFilterQuery { widgetFilter, User user ->
            eq('isDeleted', false)

            if (widgetFilter?.user) {
                List<UserGroup> userGroupList = (widgetFilter.user.contains("assignedGroups") || widgetFilter.user.contains("requestedGroup")) ? UserGroup.fetchAllUserGroupByUser(user) : null
                or {
                    if (widgetFilter.user.contains("owner"))
                        eq('owner', user)
                    if (widgetFilter.user.contains("assigned")) {
                        createAlias('assignedTo', 'assignedTo', CriteriaSpecification.LEFT_JOIN)
                        eq('assignedTo.id', user.id)
                    }
                    if (widgetFilter.user.contains("requested")) {
                        createAlias('requesters', 'requesters', CriteriaSpecification.LEFT_JOIN)
                        'eq'('requesters.id', user.id)
                    }
                    if (widgetFilter.user.contains("assignedGroups")) {
                        if (userGroupList) {
                            createAlias('assignedGroupTo', 'assignedGroupTo', CriteriaSpecification.LEFT_JOIN)
                            'in'('assignedGroupTo.id', userGroupList*.id)
                        }
                    }
                    if (widgetFilter.user.contains("requestedGroup")) {
                        if (userGroupList) {
                            createAlias('requesterGroups', 'requesterGroups', CriteriaSpecification.LEFT_JOIN)
                            'in'('requesterGroups.id', userGroupList*.id)
                        }
                    }
                }
            } else {
                assignedToOrRequesterOrOwner(user)
            }
            if (widgetFilter?.priority) {
                or {
                    widgetFilter.priority.each {
                        if (it.equals("null")) {
                            isNull("priority")
                        } else {
                        eq("priority.id", it as Long)
                        }
                    }
                }
            }
            if (widgetFilter?.status) {
                'in'('workflowState.id', widgetFilter.status.collect { it as Long })
            }
            if (widgetFilter?.due) {
                Date today = ReportRequest.getUserDayInUTC(user)
                List<Long> finalStates = WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST)?.collect { it.id } ?: [0L]
                not {
                    'in'('workflowState.id', finalStates)
                }
                or {
                    if (widgetFilter.due.contains("overdue")) {
                        lt('dueDate', today)
                    }
                    if (widgetFilter.due.contains("today")) {
                        and {
                            eq('dueDate', today)
                        }
                    }
                    if (widgetFilter.due.contains("tomorrow")) {
                        and {
                            eq('dueDate', today + 1)
                        }
                    }
                    if (widgetFilter.due.contains("five")) {
                        and {
                            gte('dueDate', today + 2)
                            lte('dueDate', today + 5)
                        }
                    }

                }
            }
        }
        countByFilter { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            fetchByFilterQuery(filter)
        }
        fetchByFilter { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("reportName")
                property("description")
                property("priority")
                property("dueDate")
                property("inn")
                property("drugCode")
                property("primaryReportingDestination")
                property("daysToDlp")
                property("occurrences")
                property("createdBy")
                property("dateCreated")
                property("lastUpdated")
                "reportRequestType"{
                    property("name" , "reportRequestTypeName")
                }
                "workflowState"{
                    property("name" , "workflowStateName")
                }
                property("assignedTo.fullName", "assignedToFullName")
                property("assignedTo.username", "assignedToUsername")
                property("assignedGroupTo.name", "assignedGroupToName")
            }
            fetchByFilterQuery(filter)
            if (sortBy) {
                if (sortBy == 'assignedTo') {
                    order(OrderByUtil.orOrderIgnoreCase(["assignedToFullName", "assignedToUsername", "assignedGroupToName"], sortDirection))
                } else if (sortBy == 'reportRequestId') {
                    order("id", "${sortDirection}")
                } else if (sortBy == 'requestName') {
                    order("reportName", "${sortDirection}")
                } else if (sortBy == 'status') {
                    order("workflowStateName", "${sortDirection}")
                } else if (sortBy == 'reportRequestType') {
                    order("reportRequestTypeName", "${sortDirection}")
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        fetchPlanByFilter { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("reportName")
                property("description")
                property("priority")
                property("dueDate")
                property("inn")
                property("drugCode")
                property("primaryReportingDestination")
                property("daysToDlp")
                property("occurrences")
                property("createdBy")
                property("dateCreated")
                property("lastUpdated")
                "reportRequestType" {
                    property("name", "reportRequestTypeName")
                }
                "workflowState" {
                    property("name", "workflowStateName")
                }
                property("assignedTo.fullName", "assignedToFullName")
                property("assignedTo.username", "assignedToUsername")
                property("assignedGroupTo.name", "assignedGroupToName")
                "priority" {
                    property("name", "priorityName")
                }
                "reportRequestType" {
                    property("aggregate", "reportRequestTypeAggregate")
                }
                property("linkedGeneratedReports")
                property("psrTypeFile")
                property("reportingPeriodStart")
                property("frequencyX")//23
                property("frequency")
                sqlProjection(
                        "TO_CHAR(custom_values) as customValuesContent", 'customValuesContent', org.hibernate.type.StandardBasicTypes.STRING
                )
                property("masterPlanningRequest")
                property("parentReportRequest")
                sqlProjection(
                        "TO_CHAR(product_Selection) as productSelectionContent", 'productSelectionContent', org.hibernate.type.StandardBasicTypes.STRING
                )
                sqlProjection(
                        "TO_CHAR(event_Selection) as eventSelectionContent", 'eventSelectionContent', org.hibernate.type.StandardBasicTypes.STRING
                )
                property("dueInToHa")
                property("reportingPeriodEnd")
                property("requestorsNames")
            }

            fetchByFilterQuery(filter)
            if (sortBy) {
                if (sortBy == 'assignedTo') {
                    order(OrderByUtil.orOrderIgnoreCase(["assignedToFullName", "assignedToUsername", "assignedGroupToName"], sortDirection))
                } else if (sortBy == 'reportRequestId') {
                    order("id", "${sortDirection}")
                } else if (sortBy == 'requestName') {
                    order("reportName", "${sortDirection}")
                } else if (sortBy == 'status') {
                    order("workflowStateName", "${sortDirection}")
                } else if (sortBy == 'reportRequestType') {
                    order("reportRequestTypeName", "${sortDirection}")
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        fetchByFilterQuery { LibraryFilter filter ->

            if (filter.aggregateOnly) {
                createAlias('reportRequestType', 'reportRequestType', CriteriaSpecification.INNER_JOIN)
                isNotNull('reportRequestType.aggregate')
                eq('reportRequestType.aggregate', true)
            }
            assignedToOrRequesterOrOwner(filter.user)
            if (filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    if (filter.manualAdvancedFilter && filter.manualAdvancedFilter["isRequestsPlan"] == "true") {
                        iLikeWithEscape('productSelection', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                        iLikeWithEscape('inn', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                        iLikeWithEscape('primaryReportingDestination', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                        iLikeWithEscape('psrTypeFile', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    } else {
                        iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('assignedTo.fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('assignedGroupTo.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    if (filter.search.isNumber()) {
                        eq('id', Long.valueOf(filter.search))
                    }
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

        assignedToOrRequesterOrOwner { User user ->
            eq('isDeleted', false)
            createAlias('assignedTo', 'assignedTo', CriteriaSpecification.LEFT_JOIN)
            createAlias('assignedGroupTo', 'assignedGroupTo', CriteriaSpecification.LEFT_JOIN)
            if (!user?.isAdmin()) {
                List<UserGroup> userGroupList = UserGroup.fetchAllUserGroupByUser(user)
                createAlias('requesters', 'requesters', CriteriaSpecification.LEFT_JOIN)
                createAlias('requesterGroups', 'requesterGroups', CriteriaSpecification.LEFT_JOIN)
                or {
                    eq('owner', user)
                    eq('assignedTo.id', user.id)
                    if (userGroupList) {
                        'in'('assignedGroupTo.id', userGroupList*.id)
                        'in'('requesterGroups.id', userGroupList*.id)
                    }
                    'in'('requesters.id', user.id)
                }
            }
        }

        getReportRequestsForUserBetweenDates { User user, Date startDate, Date endDate ->
            assignedToOrRequesterOrOwner(user)
            gte('dueDate', startDate)
            lte('dueDate', endDate)
        }

        getByActionItem { actionItemId ->
            maxResults(1)
            actionItems {
                eq 'id', actionItemId
            }
        }

        getReportRequestForShowReportRequestFilter { List<ReportRequestType> reportTypes, List<WorkflowState> wf ->
            eq("isDeleted", false)
            if (reportTypes) {
                'in'("reportRequestType", reportTypes)
            }
            or {
                isNull("workflowState")
                if (wf) {
                    not {
                        'in'("workflowState", wf)
                    }
                }
            }
        }
    }

    Set<User> getAllPublisherContributors() {
        Set set = []
        if (this.publisherContributors) set.addAll(this.publisherContributors)
        if (this.primaryPublisherContributor) set.add(this.primaryPublisherContributor)
        return set
    }

    String assignedToName() {
        return (assignedTo ? (assignedTo?.fullName ?: assignedTo?.username) : assignedGroupTo?.name)
    }

    List getAllRequestorNames() {
        List requestGroupOrUserList = []
        List requestorNames = requesters.collect {
            if (it.fullName) {
                return it?.fullName
            } else {
                return it.username
            }
        }
        if (requestorNames) {
            requestGroupOrUserList.addAll(requestorNames)
        }
        requestGroupOrUserList.addAll(requesterGroups*.name?.flatten())
        requestGroupOrUserList = requestGroupOrUserList.findAll { it }
        requestGroupOrUserList
    }

    Set<User> getAssignedToUserList() {
        Set<User> users = (assignedTo ? [assignedTo] : assignedGroupTo?.users?.flatten())
        users = users?.findAll { it }
        users
    }

    Set<User> getRequestorUserList() {
        Set<User> users = requesters ?: []
        if (requesterGroups) {
            users.addAll(requesterGroups*.users?.flatten())
        }
        users = users?.findAll { it }
        users
    }

    Set getRequestorList() {
        Set requestors = requesterGroups ? requesterGroups?.name : []
        if (requesters) {
            requestors.addAll(requesters?.fullName)
        }
        requestors.flatten()
    }

    String getAttachmentsString() {
        return attachments?.collect { it.name }?.join(",") ?: ''
    }

    String getLinksString() {
        return getLinkedReports()?.collect { it.to.id + " - " + it.to.reportName }?.join(", ")
    }

    List<ReportRequestLink> getLinkedReports() {
        return this.id ? ReportRequestLink.createCriteria().list {
            eq("isDeleted", false)
            or {
                eq('from', this)
                eq('to', this)
            }
        } : []
    }

    static getSummary(User user) {
        List<Long> finalStates = WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST)?.collect { it.id } ?: [0L]
        List<Long> newAndFinalStates = finalStates + [WorkflowState.getDefaultWorkState().id]
        Date today = getUserDayInUTC(user)

        String availableForUser = " and (rr.owner=:user or (rr.assignedTo=:user or rr.assignedGroupTo in (select userGroup from UserGroupUser where user=:user)) or " +
                "(id in (select distinct (rr.id) from ReportRequest rr left join rr.requesters as rq left join rr.requesterGroups as rqg where rq=:user or rqg in (select ugu.userGroup from UserGroupUser ugu where ugu.user=:user)))))"
        Map qparams = [user: user, today: today, dateDueSoon: today + 30, day1: today + 1, day2: today + 2, day5: today + 5, closed: finalStates, newAndFinalStates: newAndFinalStates]
        if (user.isAdmin()) {
            availableForUser = ""
            qparams.remove("user")
        }

        String sql = "select COALESCE(SUM(1), 0) as total, " +
                "COALESCE(SUM(CASE WHEN (dueDate<:today and (workflowState.id not in (:closed))) THEN 1 ELSE 0 END), 0) as overdue, " +
                "COALESCE(SUM(CASE WHEN (dueDate>:today and dueDate<:dateDueSoon and (workflowState.id not in (:closed))) THEN 1 ELSE 0 END), 0) as duesoon, " +
                "COALESCE(SUM(CASE WHEN (dueDate>=:today and dueDate<:day1 and (workflowState.id not in (:closed))) THEN 1 ELSE 0 END), 0) as duetoday, " +
                "COALESCE(SUM(CASE WHEN (dueDate>=:day1 and dueDate<:day2 and (workflowState.id not in (:closed))) THEN 1 ELSE 0 END), 0) as duetomorrow, " +
                "COALESCE(SUM(CASE WHEN (dueDate>=:day2 and dueDate<=:day5 and (workflowState.id not in (:closed))) THEN 1 ELSE 0 END), 0) as duefive, " +
                "COALESCE(SUM(CASE WHEN (workflowState.id not in (:closed)) THEN 1 ELSE 0 END), 0) as open, " +
                "COALESCE(SUM(CASE WHEN (workflowState.id not in (:newAndFinalStates)) THEN 1 ELSE 0 END), 0) as inprogress " +
                " from ReportRequest rr where rr.isDeleted=false " + availableForUser
        def result = executeQuery(sql, qparams)[0]
        [total: result[0], overdue: result[1], dueSoon: result[2], today: result[3], tomorrow: result[4], five: result[5], open: result[6], inprogress: result[7]]

    }

    private static Date getUserDayInUTC(User user) {
        //we need to get date in user timezone and convert it to beginning of the day in UTC timezone, because Due date field contains date in UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        sdf.setTimeZone(TimeZone.getTimeZone(user.preference.timeZone))
        String dateValue = sdf.format(new Date())
        sdf = new SimpleDateFormat("yyyy-MM-dd")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        return sdf.parse(dateValue)
    }

    boolean isViewableBy(User currentUser) {
        return (owner.id == currentUser?.id || currentUser.isAdmin() || (currentUser.id in getRequestorUserList()?.collect{it.id})
                || (currentUser.id in getAssignedToUserList()?.collect{it.id}) || owner.id in currentUser.getUserTeamIds())
    }

    static getActiveUsersAndUserGroups(User user, String term) {
        def result = [users: [], userGroups: []]

        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableReportsSql = " where rc.id in (select rc1.id from ReportRequest as rc1 left join rc1.assignedGroupTo as swg1 left join rc1.assignedTo as swu1 " +
                "where rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser))"
        String groupsSql = "from UserGroup as ug where " +
                (term ? " lower(ug.name) like :term and " : "") +
                "ug.id in (select swg.id from ReportRequest as rc join rc.assignedGroupTo as swg " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term ? " ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and " : "") +
                " (u.id in (select swu.id from ReportRequest as rc join rc.assignedTo as swu" +
                (user.isAdmin() ? "" : userViewableReportsSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))"
        Map groupParams = user.isAdmin() ? [:] : [userid: user.id, groupIdsForUser: groupIdsForUser]
        if (term) groupParams.put('term', '%' + term.toLowerCase() + '%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        Map userParams = user.isAdmin() ? [groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [userid: user.id, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if (term) userParams.put('term', '%' + term.toLowerCase() + '%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    transient String getValidProductGroupSelection() {
        if (productGroupSelection && productGroupSelection != "[]") {
            return productGroupSelection
        }
        return null
    }

    transient String getValidEventGroupSelection() {
        if (eventGroupSelection && eventGroupSelection != "[]") {
            return eventGroupSelection
        }
        return null
    }

    @Override
    public String toString() {
        return "$reportName - $owner"
    }

    Set<String> getAllReportingDestinations() {
        Set<String> destinations = new HashSet<>([])
        if (reportingDestinations) {
            destinations.addAll(reportingDestinations)
        }
        if (primaryReportingDestination) {
            destinations.add(primaryReportingDestination)
        }
        return destinations
    }
}
