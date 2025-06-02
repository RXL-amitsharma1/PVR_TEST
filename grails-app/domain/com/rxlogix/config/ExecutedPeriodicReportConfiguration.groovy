package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.config.publisher.Gantt
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import grails.gorm.DetachedCriteria
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
@DirtyCheck
@CollectionSnapshotAudit
class ExecutedPeriodicReportConfiguration extends ExecutedReportConfiguration {

    PeriodicReportTypeEnum periodicReportType
    boolean includePreviousMissingCases = false
    boolean includeOpenCasesInDraft = false
    boolean includeLockedVersion = false //As we don't want to show in case of PeriodicReports
    Integer dueInDays
    String primaryReportingDestination
    User primaryPublisherContributor
    String generatedReportName
    Date dueDate
    Gantt gantt
    String associatedSpotfireFile
    Date finalExecutedEtlDate
    Date finalLastRunDate
    static hasMany = [reportingDestinations: String, reportSubmissions: ReportSubmission, publisherContributors: User ]

    static constraints = {
        dueInDays nullable: true
        dueDate nullable: true
        gantt nullable: true
        primaryReportingDestination(nullable: true, maxSize: 255)
        primaryPublisherContributor nullable: true
        publisherContributors nullable: true
        generatedReportName nullable: true, maxSize: 255
        associatedSpotfireFile(nullable:true)
        finalExecutedEtlDate(nullable: true)
        finalLastRunDate(nullable: true)
    }

    static mapping = {
        includePreviousMissingCases column: "INCLUDE_PREV_MISS_CASES"
        includeOpenCasesInDraft column: "INCLUDE_OPEN_CASES_DRAFT"
        dueInDays column: 'DUE_IN_DAYS'
        dueDate column: 'DUE_DATE'
        periodicReportType column: "PR_TYPE"
        reportingDestinations joinTable: [name: "EX_RCONFIG_REPORT_DESTS", column: "REPORT_DESTINATION", key: "EX_RCONFIG_ID"]
        primaryReportingDestination column: "PRIMARY_DESTINATION"
        gantt column: "GANTT_ID"
        publisherContributors joinTable: [name: "EX_RCONFIG_P_C_USERS", column: "USER_ID", key: "RCONFIG_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        primaryPublisherContributor column: "PRIMARY_P_CONTRIBUTOR"
        generatedReportName column: "GENERATED_RPT_NAME"
        associatedSpotfireFile column: "ASSOCIATED_SPOTFIRE_FILE"
        finalExecutedEtlDate column: "FINAL_EX_ETL_DATE"
        finalLastRunDate column: "FINAL_LAST_RUN_DATE"
    }

    @Override
    String getUsedEventSelection() {
        return null
    }

    @Override
    String getUsedEventGroupSelection() {
        return null
    }

    @Override
    List<Date> getReportMinMaxDate() {
        //As in Periodic reports Interval / Cummulative would be dependent on Global criteria only.
        return [executedGlobalDateRangeInformation.dateRangeStartAbsolute, executedGlobalDateRangeInformation.dateRangeEndAbsolute]
    }


    static namedQueries = {

        ownedByAndSharedWithUser { User currentUser, Boolean isAdmin, Boolean includeArchived ->
            createAlias('executedDeliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            createAlias('executedReportUserStates', 'state', CriteriaSpecification.LEFT_JOIN, Restrictions.eq('user', currentUser))
            eq("isDeleted", false)
            or {
                isNull('state.id')
                and {
                    eq('state.isDeleted', false)
                }
            }

            if (!includeArchived) {
                or {
                    and {
                        isNull('state.id')
                        eq("archived", false)
                    }
                    eq('state.isArchived', false)
                }
            }
            if (!isAdmin) {
                or {
                    currentUser.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser?.id)
                    'in'('sw.id', currentUser.id)
                    if (UserGroup.fetchAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }

        fetchDistinctSpotfireFileNames { User user ->
            projections {
                distinct("associatedSpotfireFile")
            }
            isNotNull("associatedSpotfireFile")
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
        }

        ownedByAndSharedWithUserForPublisher { User currentUser, Boolean isAdmin, Boolean includeArchived ->
            createAlias('executedDeliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('publisherContributors', 'pc', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            createAlias('executedReportUserStates', 'state', CriteriaSpecification.LEFT_JOIN, Restrictions.eq('user', currentUser))
            createAlias('publisherConfigurationSections', 'pubSec', CriteriaSpecification.LEFT_JOIN)
            createAlias('publisherReports', 'pubRep', CriteriaSpecification.LEFT_JOIN)
            eq("isDeleted", false)
            or {
                isNull('state.id')
                and {
                    eq('state.isDeleted', false)
                }
            }

            if (!includeArchived) {
                or {
                    and {
                        isNull('state.id')
                        eq("archived", false)
                    }
                    eq('state.isArchived', false)
                }
            }
            if (!isAdmin) {
                or {
                    currentUser.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser?.id)
                    eq('primaryPublisherContributor.id', currentUser?.id)
                    eq('pc.id', currentUser?.id)
                    'in'('sw.id', currentUser.id)
                    eq('pubSec.approver.id', currentUser?.id)
                    eq('pubSec.reviewer.id', currentUser?.id)
                    eq('pubSec.author.id', currentUser?.id)
                    eq('pubRep.reviewer.id', currentUser?.id)
                    eq('pubRep.author.id', currentUser?.id)
                    eq('pubRep.approver.id', currentUser?.id)
                    if (UserGroup.fetchAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                        'in'('pubSec.assignedToGroup.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                        'in'('pubRep.assignedToGroup.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }
        sharedWithFilter { Map sharedWithMap, User user ->
            or {
                if (sharedWithMap?.ownerId) {
                    eq('owner.id', sharedWithMap.ownerId)
                }
                if (sharedWithMap?.usersId) {
                    or {
                        'in'('sw.id', sharedWithMap.usersId)
                        'in'('swg.id', new DetachedCriteria(UserGroupUser).build {
                            projections {
                                distinct('userGroup.id')
                            }
                            'in'('user.id', sharedWithMap.usersId)
                        })
                    }
                }
                if (sharedWithMap?.groupsId) {
                    'in'('swg.id', sharedWithMap.groupsId)
                }
                if (sharedWithMap?.team && user) {
                    or {
                        user.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    }
                }
            }
        }

        fetchAllOwners { User user, search ->
            projections {
                distinct("owner")
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        countAllOwners { User user, search ->
            projections {
                countDistinct("owner")
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }
        fetchByAdvancedPublisherWidgetFilter { widgetFilter, User user ->
            projections {
                distinct('id')
                property("reportName")
                property("description")
                property("dueDate")
                property("primaryReportingDestination")
                property("lastUpdated")
            }
            eq('isDeleted', false)
            eq('isPublisherReport', true)
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
            createAlias('publisherConfigurationSections', 'publisherConfigurationSections', CriteriaSpecification.LEFT_JOIN)
            createAlias('publisherReports', 'publisherReports', CriteriaSpecification.LEFT_JOIN)
            if (widgetFilter?.user) {
                or {
                    if (widgetFilter.user.contains("owner"))
                        eq('owner', user)
                    if (widgetFilter.user.contains("assigned")) {
                        createAlias('publisherConfigurationSections.reviewer', 'reviewer', CriteriaSpecification.LEFT_JOIN)
                        createAlias('publisherConfigurationSections.approver', 'approver', CriteriaSpecification.LEFT_JOIN)
                        createAlias('publisherConfigurationSections.author', 'author', CriteriaSpecification.LEFT_JOIN)
                        createAlias('publisherConfigurationSections.assignedToGroup', 'publisherAssignedToGroup', CriteriaSpecification.LEFT_JOIN)
                        createAlias('gantt', 'gantt', CriteriaSpecification.LEFT_JOIN)
                        createAlias('gantt.ganttItems', 'ganttItems', CriteriaSpecification.LEFT_JOIN)
                        createAlias('ganttItems.assignedTo', 'ganttItemsAssignedTo', CriteriaSpecification.LEFT_JOIN)
                        createAlias('ganttItems.assignedGroupTo', 'ganttItemsAssignedGroupTo', CriteriaSpecification.LEFT_JOIN)
                        List goupid = UserGroup.fetchAllUserGroupByUser(user)
                        or {
                            eq('reviewer.id', user.id)
                            eq('approver.id', user.id)
                            eq('author.id', user.id)
                            eq('ganttItemsAssignedTo.id', user.id)
                            if (goupid) {
                                'in'('ganttItemsAssignedGroupTo.id', goupid.id)
                                'in'('publisherAssignedToGroup.id', goupid.id)
                            }
                        }
                    }
                }
            }
            if (widgetFilter?.stage) {
                or {
                    WorkflowState newState = WorkflowState.getDefaultWorkState()
                    if (widgetFilter.stage.contains("new")) {
                        not {
                            'in'("publisherConfigurationSections.id", new DetachedCriteria(PublisherConfigurationSection).build {
                                projections { property("id") }
                                not {
                                    eq("workflowState", newState)
                                }
                            })
                        }
                    }
                    if (widgetFilter.stage.contains("sections")) {
                        and {
                            'in'("publisherConfigurationSections.id", new DetachedCriteria(PublisherConfigurationSection).build {
                                projections {
                                    property("id")
                                }
                                not {
                                    eq("workflowState", newState)
                                }
                            })
                            isNull("publisherReports.id")
                        }
                    }
                    if (widgetFilter.stage.contains("publishing")) {
                        and {
                            'in'("publisherReports.id", new DetachedCriteria(PublisherReport).build {
                                projections {
                                    property("id")
                                }
                                eq("published", false)
                            })
                            not {
                                'in'("publisherReports.id", new DetachedCriteria(PublisherReport).build {
                                    projections {
                                        property("id")
                                    }
                                    eq("published", true)
                                })
                            }
                        }
                    }
                    if (widgetFilter.stage.contains("published")) {
                        'in'("publisherReports.id", new DetachedCriteria(PublisherReport).build {
                            projections {
                                property("id")
                            }
                            eq("published", true)
                        })
                    }
                }
            }
            if (widgetFilter?.status) {
                'in'('workflowState.id', widgetFilter.status.collect { it as Long })
            }
            if (widgetFilter?.due) {
                Date today = ReportRequest.getUserDayInUTC(user)
                List<Long> finalStates = WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.ADHOC_REPORT)?.collect { it.id } ?: [0L]
                not {
                    'in'('workflowState.id', finalStates)
                }
                or {
                    if (widgetFilter.due.contains("overdue")) {
                        lte('dueDate', today)
                    }
                    if (widgetFilter.due.contains("today")) {
                        and {
                            gte('dueDate', today)
                            lte('dueDate', today + 1)
                        }
                    }
                    if (widgetFilter.due.contains("tomorrow")) {
                        and {
                            gte('dueDate', today + 1)
                            lte('dueDate', today + 2)
                        }
                    }
                    if (widgetFilter.due.contains("five")) {
                        and {
                            gte('dueDate', today)
                            lte('dueDate', today + 5)
                        }
                    }
                }
            }
        }


        fetchByCompliancePublisherWidgetFilter { Object widgetFilter, User user ->
            projections {
                distinct('id')
                property("reportName")
                property("description")
                property("dueDate")
            }
            eq('isDeleted', false)
            eq('isPublisherReport', true)
            ownedByAndSharedWithUser(user, user.isAdmin(), false)

            if (widgetFilter?.periodicReportType) {
                'in'('periodicReportType', widgetFilter.periodicReportType)
            }

            if (widgetFilter?.product) {
                or {
                    widgetFilter.product?.split("@!")?.each {
                        iLikeWithEscape('productSelection', "%${EscapedILikeExpression.escapeString(it.split("_")[1])}%")
                    }
                }
            }
            if (widgetFilter?.dueDateRangeFrom) {
                gt("dueDate", widgetFilter.dueDateRangeFrom)
            }
            if (widgetFilter?.dueDateRangeTo) {
                lt("dueDate", widgetFilter.dueDateRangeTo)
            }
            createAlias('reportSubmissions', 'rs', CriteriaSpecification.LEFT_JOIN)
            or {
                eq('rs.reportSubmissionStatus', ReportSubmissionStatusEnum.SUBMITTED)
                and {
                    isNull('rs.submissionDate')
                    lt("dueDate", new Date())
                }
            }
        }

        countAllBySearchStringAndStatusInList { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            fetchAllBySearchStringAndStatusInListQuery(filter)
        }
        fetchAllBySearchStringAndStatusInList { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("dateCreated")
                property("lastUpdated")
                property("periodicReportType")
                property("numOfExecutions")
                property("reportName")
                property("dueDate")
                property("primaryReportingDestination")
                property("state.isFavorite","isFavorite")
                'owner' {
                    property("fullName", "fullName")
                }
                property("generatedReportName", "versionName")
                'workflowState' {
                    property("name", "name")
                }
                if (sortBy && sortBy == 'primaryReportingDestination') {
                    sqlProjection """({alias}.PRIMARY_DESTINATION || (select listagg((CASE WHEN REP_DESTINATION LIKE '[%' AND REP_DESTINATION LIKE '%]' THEN REPLACE(SUBSTR(REP_DESTINATION, 2, length(REP_DESTINATION)), ({alias}.PRIMARY_DESTINATION || ', '), '') ELSE REPLACE(REP_DESTINATION, ({alias}.PRIMARY_DESTINATION || ', '), '') END), ', ') within group(order by REP_DESTINATION) from (select errd.REPORT_DESTINATION AS REP_DESTINATION from EX_RCONFIG_REPORT_DESTS errd where errd.EX_RCONFIG_ID = {alias}.ID and errd.REPORT_DESTINATION != {alias}.PRIMARY_DESTINATION)
                    )) AS repDestinations""".toString(), 'repDestinations', org.hibernate.type.StandardBasicTypes.STRING
                }
            }
            fetchAllBySearchStringAndStatusInListQuery(filter)
            if (sortBy) {
                if (['periodicReportType', 'reportName', 'generatedReportName'].contains(sortBy)) {
                    order(new Order("${sortBy}", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (sortBy == 'owner.fullName') {
                    order(new Order("fullName", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (sortBy == 'workflowState.name') {
                    order(new Order("name", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (sortBy == 'primaryReportingDestination') {
                    order(OrderByUtil.formulaOrder("UPPER(TRIM(repDestinations))", "${sortDirection}"))
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }
        fetchAllBySearchStringAndStatusInListQuery { LibraryFilter filter ->
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if (filter.forPublisher) {
                if (!filter.allReportsForPublisher) {
                    eq('isPublisherReport', true)
                }
                'in'("status", ReportExecutionStatusEnum.reportsListingStatusesForPublisher)
            } else {
                'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            }
            if(filter.periodicReportType){
                eq('periodicReportType', filter.periodicReportType as PeriodicReportTypeEnum)
            }
             eq('isDeleted', false)
            if (filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('productSelection', "%\"name\":\"${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('primaryReportingDestination', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'workflowState' {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            if(filter.forPublisher) {
                ownedByAndSharedWithUserForPublisher(filter.user, filter.user.isAdmin(), filter.includeArchived)
            } else {
                ownedByAndSharedWithUser(filter.user, filter.user.isAdmin(), filter.includeArchived)
            }
            if(filter.submission){
                submissionStatus(filter.submission)
            }
            sharedWithFilter(filter.sharedWith, filter.user)

            if (filter.advancedFilterCriteria) {
                createAlias('exd.emailToUsers', 'emails', CriteriaSpecification.LEFT_JOIN)
                createAlias('emailConfiguration', 'emc', CriteriaSpecification.LEFT_JOIN)
                filter.advancedFilterCriteria.each{cl->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (filter.favoriteSort) {
                and {
                    order('state.isFavorite', 'asc')
                    order('lastUpdated', 'desc')
                }
            }
        }

        reportsWithGantt { ReportSubmissionStatusEnum submission, Boolean isPublisherReport ->
            eq('isDeleted', false)
            eq('archived', false)
            eq('isPublisherReport', isPublisherReport)
            isNotNull('gantt')
        }

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        submissionStatus { ReportSubmissionStatusEnum submission ->
            createAlias('reportSubmissions', 'rs', CriteriaSpecification.LEFT_JOIN)
            //those reports where submission date is empty and are not marked for non-submission
            if (submission == ReportSubmissionStatusEnum.PENDING) {
                isNull('rs.submissionDate')
                or {
                    isNull('rs.reportSubmissionStatus')
                    ne('rs.reportSubmissionStatus', ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED)
                }
            }
            else
                eq("rs.reportSubmissionStatus", submission)
        }
        // Number of Pending Submissions (reports with no submission date and marked for submission is set to no)
        pendingSubmissionIds { User user, Boolean isAdmin = false ->
            createAlias('reportSubmissions', 'rs', CriteriaSpecification.LEFT_JOIN)
            projections {
                distinct('id')
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            isNull('rs.submissionDate')
            or {
                isNull('rs.reportSubmissionStatus')
                ne('rs.reportSubmissionStatus', ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED)
            }
            ownedByAndSharedWithUser(user, isAdmin, false)
        }

        // Number of Aggregate Reports Due Soon (based on due date, due within next 30 days)
        dueSoonIds { User user, Boolean isAdmin = false ->
            projections {
                countDistinct('id')
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            def now = new Date()
            between("dueDate", now, now + 30)
            ownedByAndSharedWithUser(user, isAdmin, false)
        }

        // Number of Aggregate Reports Submitted Recently (based on submission date, submitted in last 30 days)
        submittedRecentlyIds { User user, Boolean isAdmin = false ->
            createAlias('reportSubmissions', 'rs', CriteriaSpecification.LEFT_JOIN)
            projections {
                countDistinct('id')
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            def now = new Date()
            def beforeNow =  now - 30
            gt('rs.submissionDate', beforeNow)
            ownedByAndSharedWithUser(user, isAdmin, false)
        }

        // Total Overview (Due Date has passed but no submission date yet)
        overdueIds { User user, Boolean isAdmin = false ->
            createAlias('reportSubmissions', 'rs', CriteriaSpecification.LEFT_JOIN)
            projections {
                countDistinct("id")
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            isNull('rs.reportSubmissionStatus')
            isNull('rs.submissionDate')
            def now = new Date()
            lt("dueDate",now)
            ownedByAndSharedWithUser(user, isAdmin, false)
        }

        // Scheduled reports
        scheduledIds {User user, Boolean isAdmin = false ->
            projections {
                countDistinct('id')
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('status', ReportExecutionStatusEnum.SCHEDULED)
            ownedByAndSharedWithUser(user, isAdmin, false)
        }
    }

    Set<String> getAllReportingDestinations() {
        Set<String> destinations = new LinkedHashSet<>([])
        if(reportingDestinations) {
            destinations.addAll(reportingDestinations)
        }
        if(primaryReportingDestination) {
            destinations.add(primaryReportingDestination)
        }
        return destinations
    }

    public boolean finalReportGettingGenerated() {
        // Icluded case when report geneting directly generated to final.
        return ((status == ReportExecutionStatusEnum.GENERATING_FINAL_DRAFT) || (status == ReportExecutionStatusEnum.ERROR && !hasGeneratedCasesData))
    }

    static getActiveUsersAndUserGroups(User user,String term) {
        def result = [users: [], userGroups: []]
        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableReportsSql = " and rc.id in (select rc1.id from ExecutedPeriodicReportConfiguration as rc1 left join rc1.executedDeliveryOption as dop1 left join dop1.sharedWithGroup as swg1 left join dop1.sharedWith as swu1 " +
                "where rc1.isDeleted=false and (rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser)))"
        String groupsSql = "from UserGroup as ug where "  +
                (term?" lower(ug.name) like :term and ":"")+
                " ug.id in (select swg.id from ExecutedPeriodicReportConfiguration as rc join rc.executedDeliveryOption as dop join dop.sharedWithGroup as swg where rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"")+
                " (u.id in (select swu.id from ExecutedPeriodicReportConfiguration as rc join rc.executedDeliveryOption as dop join dop.sharedWith as swu where rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))"
        Map groupParams = user.isAdmin() ? [:] : [userid: user.id, groupIdsForUser: groupIdsForUser]
        if(term) groupParams.put('term','%'+term.toLowerCase()+'%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        def userParams = user.isAdmin() ? [groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [userid: user.id, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if(term) userParams.put('term','%'+term.toLowerCase()+'%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    static getStates(User user) {
        def isAdmin = user.isAdmin()
        def queryParameters = [statuses: ReportExecutionStatusEnum.reportsListingStatuses]
        def groupsIdsForUser;
        if (!isAdmin) {
            groupsIdsForUser = UserGroup.fetchAllUserGroupByUser(user).id
            if (groupsIdsForUser)
                queryParameters << [groupIdsForUser: groupsIdsForUser]
        }

        def result = executeQuery("select workflowState.name, count(distinct exConfig.id) \n" +
                "from ExecutedPeriodicReportConfiguration exConfig \n" +
                "left join exConfig.owner as owner \n" +
                "left join exConfig.workflowState as workflowState \n" +
                (!isAdmin ? "left join exConfig.executedDeliveryOption.sharedWith shareWith \n" : "") +
                ((!isAdmin && groupsIdsForUser) ? "left join exConfig.executedDeliveryOption.sharedWithGroup sharedWithGroup \n" : "") +
                " left join exConfig.executedReportUserStates as state with (state.user.id=${user.id})\n" +
                " where \n" +
                " exConfig.isDeleted=false and "+
                "((state.id is null and exConfig.archived=false) or (state.isDeleted=false and state.isArchived=false))   \n" +
                " and exConfig.status in(:statuses) " +
                (!isAdmin ? "and ( shareWith.id=${user.id} or owner.id=${user.id} " : "") +
                ((!isAdmin && groupsIdsForUser) ? "or sharedWithGroup.id in (:groupIdsForUser)  \n" : "") +
                (!isAdmin ? ")" : "") +
                " group by workflowState.name ", queryParameters)
        return result
    }

    @Override
    public String toString() {
        super.toString()
    }

    public boolean isVisibleForPublisher(User user = null) {
        User currenUser = user ?: Holders.applicationContext.getBean("userService").currentUser
        if(!currenUser)return false
        if (this.isViewableBy(currenUser)) return true
        if ((this.primaryPublisherContributorId == currenUser.id) || (this.publisherContributors?.find { it.id == currenUser.id })) return true
        Set<Long> users = []
        Set<Long> groups = []
        this.publisherConfigurationSections.collect {
            users.add(it.author?.id)
            users.add(it.approver?.id)
            users.add(it.reviewer?.id)
            groups.add(it.assignedToGroup?.id)
        }
        this.publisherReports.collect {
            users.add(it.author?.id)
            users.add(it.approver?.id)
            users.add(it.reviewer?.id)
            groups.add(it.assignedToGroup?.id)
        }
        if (users.find { it == currenUser.id }) return true
        List<Long> userGroupsIds = UserGroup.fetchAllUserGroupByUser(currenUser)?.flatten()?.collect { it.id }
        return groups?.any { it in userGroupsIds }
    }
}
