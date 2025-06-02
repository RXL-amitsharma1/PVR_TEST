package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import grails.gorm.DetachedCriteria
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions
@DirtyCheck
@CollectionSnapshotAudit
class ExecutedIcsrReportConfiguration extends ExecutedReportConfiguration {

    PeriodicReportTypeEnum periodicReportType
    boolean includePreviousMissingCases = false
    boolean includeOpenCasesInDraft = false
    boolean includeLockedVersion = false //As we don't want to show in case of PeriodicReports
    Integer dueInDays
    String primaryReportingDestination
    Date dueDate
    TitleEnum recipientTitle
    String recipientFirstName
    String recipientMiddleName
    String recipientLastName
    String recipientDept
    String recipientOrganizationName
    String recipientTypeName
    String recipientCountry
    String receiverId
    String recipientPartnerRegWith
    String recipientType
    String recipientAddress1
    String recipientAddress2
    String recipientState
    String recipientPostcode
    String recipientCity
    String recipientPhone
    String recipientFax
    String recipientEmail
    TitleEnum senderTitle
    String senderFirstName
    String senderMiddleName
    String senderLastName
    String senderOrganizationName
    String senderTypeName
    String senderDept
    String senderCountry
    String senderId
    String senderPartnerRegWith
    String address1
    String address2
    String city
    String state
    String postalCode
    String phone
    String email
    String fax
    String xsltName
    String referenceProfileName
    String allowedAttachments
    String senderCompanyName
    String recipientCompanyName
    String senderUnitOrganizationName
    String recipientUnitOrganizationName
    String preferredTimeZone
    String holderId
    String senderHolderId
    String xmlVersion
    String xmlEncoding
    String xmlDoctype
    String unitAttachmentRegId
    String recipientPrefLanguage
    String senderPrefLanguage

    
    static hasMany = [reportingDestinations: String, reportSubmissions: ReportSubmission]

    static constraints = {
        periodicReportType nullable: true
        dueInDays nullable: true
        dueDate nullable: true
        primaryReportingDestination(nullable: true)
        recipientTitle(nullable: true)
        recipientFirstName(nullable: true)
        recipientMiddleName(nullable: true)
        recipientLastName(nullable: true)
        recipientDept(nullable: true)
        recipientOrganizationName(nullable: true)
        recipientTypeName(nullable: true)
        recipientCountry(nullable: true)
        receiverId(nullable: true)
        recipientPartnerRegWith(nullable: true)
        recipientType(nullable: true)
        recipientAddress1(nullable: true)
        recipientAddress2(nullable: true)
        recipientState(nullable: true)
        recipientPostcode(nullable: true)
        recipientCity(nullable: true)
        recipientPhone(nullable: true)
        recipientFax(nullable: true)
        recipientEmail(nullable: true)
        senderTitle(nullable: true)
        senderFirstName(nullable: true)
        senderMiddleName(nullable: true)
        senderLastName(nullable: true)
        senderOrganizationName(nullable: true)
        senderTypeName(nullable: true)
        senderDept(nullable: true)
        senderCountry(nullable: true)
        senderId(nullable: true)
        senderPartnerRegWith(nullable: true)
        address1(nullable: true)
        address2(nullable: true)
        city(nullable: true)
        state(nullable: true)
        postalCode(nullable: true)
        phone(nullable: true)
        email(nullable: true)
        fax(nullable: true)
        referenceProfileName(nullable: true)
        recipientCompanyName(nullable: true)
        senderCompanyName(nullable: true)
        senderUnitOrganizationName(nullable: true)
        recipientUnitOrganizationName(nullable: true)
        allowedAttachments(nullable: true)
        preferredTimeZone(nullable: true)
        holderId(nullable: true, maxSize: 200)
        senderHolderId(nullable: true, maxSize: 200)
        xmlVersion(nullable: true)
        xmlEncoding(nullable: true)
        xmlDoctype(nullable: true)
        unitAttachmentRegId(nullable: true)
        recipientPrefLanguage(nullable: true)
        senderPrefLanguage(nullable: true)
    }

    static mapping = {
        includePreviousMissingCases column: "INCLUDE_PREV_MISS_CASES"
        includeOpenCasesInDraft column: "INCLUDE_OPEN_CASES_DRAFT"
        dueInDays column: 'DUE_IN_DAYS'
        dueDate column: 'DUE_DATE'
        periodicReportType column: "PR_TYPE"
        reportingDestinations joinTable: [name: "EX_RCONFIG_REPORT_DESTS", column: "REPORT_DESTINATION", key: "EX_RCONFIG_ID"]
        primaryReportingDestination column: "PRIMARY_DESTINATION"
        recipientTitle column : "RECIPIENT_TITLE"
        recipientFirstName column : "RECIPIENT_FIRST_NAME"
        recipientMiddleName column : "RECIPIENT_MIDDLE_NAME"
        recipientLastName column : "RECIPIENT_LAST_NAME"
        recipientDept column: "RECIPIENT_DEPT"
        recipientOrganizationName column: "RECIPIENT_ORG_NAME"
        recipientTypeName column: "RECIPIENT_TYPE_NAME"
        recipientCountry column: "RECIPIENT_COUNTRY"
        receiverId column: "RECEIVER_ID"
        recipientPartnerRegWith column: "RECIPIENT_PARTNER_REG_WITH"
        recipientType column: "RECIPIENT_TYPE"
        recipientAddress1 column: "RECIPIENT_ADDRESS1"
        recipientAddress2 column: "RECIPIENT_ADDRESS2"
        recipientState column: "RECIPIENT_STATE"
        recipientPostcode column: "RECIPIENT_POST_CODE"
        recipientCity column: "RECIPIENT_CITY"
        recipientPhone column: "RECIPIENT_PHONE"
        recipientFax column: "RECIPIENT_FAX"
        recipientEmail column: "RECIPIENT_EMAIL"
        senderTitle column: "SENDER_TITLE"
        senderFirstName column: "SENDER_FIRST_NAME"
        senderMiddleName column: "SENDER_MIDDLE_NAME"
        senderLastName column: "SENDER_LAST_NAME"
        senderOrganizationName column: "SENDER_ORG_NAME"
        senderTypeName column: "SENDER_TYPE_NAME"
        senderDept column: "SENDER_DEPT"
        senderCountry column: "SENDER_COUNTRY"
        senderId column: "SENDER_ID"
        senderPartnerRegWith column: "SENDER_PARTNER_REG_WITH"
        address1 column: "ADDRESS1"
        address2 column: "ADDRESS2"
        city column: "CITY"
        state column: "STATE"
        postalCode column: "POSTAL_CODE"
        phone column: "PHONE"
        email column: "EMAIL"
        fax column: "FAX"
        xsltName column: "XSLT_NAME"
        referenceProfileName column: 'REFERENCE_PROFILE_NAME'
        allowedAttachments column: "ALLOWED_ATTACHMENTS"
        recipientCompanyName column: "RECEIVER_COMPANY_NAME"
        senderCompanyName column: "SENDER_COMPANY_NAME"
        senderUnitOrganizationName column: "RECEIVER_UNIT_ORG_NAME"
        recipientUnitOrganizationName column: "SENDER_UNIT_ORG_NAME"
        preferredTimeZone column: "PREFERRED_TIME_ZONE"
        holderId column: "HOLDER_ID"
        senderHolderId column: "SENDER_HOLDER_ID"
        xmlVersion column: 'XML_VERSION'
        xmlEncoding column: 'XML_ENCODING'
        xmlDoctype column: 'XML_DOCTYPE'
        unitAttachmentRegId column: 'UNIT_ATTACHMENT_REG_ID'
        recipientPrefLanguage column: 'RECEIVER_PREF_LANGUAGE'
        senderPrefLanguage column: 'SENDER_PREF_LANGUAGE'
    }

    @Override
    String getUsedEventSelection() {
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

        countAllBySearchStringAndStatusInList { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            fetchAllBySearchStringAndStatusInListQuery(filter)
        }
        fetchAllBySearchStringAndStatusInList { LibraryFilter filter ->
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
                fetchAllBySearchStringAndStatusInListQuery(filter)
            }
        }
        fetchAllBySearchStringAndStatusInListQuery { LibraryFilter filter ->

            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            if (filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('productSelection', "%\"name\":\"${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('primaryReportingDestination', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                }
            }
            ownedByAndSharedWithUser(filter.user, filter.user.isAdmin(), filter.includeArchived)
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
                distinct('id')
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
                distinct('id')
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
                distinct('id')
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
                distinct('id')
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('status', ReportExecutionStatusEnum.SCHEDULED)
            ownedByAndSharedWithUser(user, isAdmin, false)
        }
    }

    Set<String> getAllReportingDestinations() {
        Set<String> destinations = new LinkedHashSet<>([])
        if(primaryReportingDestination) {
            destinations.add(primaryReportingDestination)
        }
        if(reportingDestinations) {
            destinations.addAll(reportingDestinations)
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
        String userViewableReportsSql = " and rc.id in (select rc1.id from ExecutedIcsrReportConfiguration as rc1 left join rc1.executedDeliveryOption as dop1 left join dop1.sharedWithGroup as swg1 left join dop1.sharedWith as swu1 " +
                "where rc1.isDeleted=false and (rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser)))"
        String groupsSql = "from UserGroup as ug where "  +
                (term?" lower(ug.name) like :term and ":"")+
                " ug.id in (select swg.id from ExecutedIcsrReportConfiguration as rc join rc.executedDeliveryOption as dop join dop.sharedWithGroup as swg where rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"")+
                " (u.id in (select swu.id from ExecutedIcsrReportConfiguration as rc join rc.executedDeliveryOption as dop join dop.sharedWith as swu where rc.isDeleted=false " +
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

        def result = executeQuery("select count(distinct exConfig.id),workflowState.name \n" +
                "from ExecutedIcsrReportConfiguration exConfig \n" +
                "left join exConfig.owner as owner \n" +
                "left join exConfig.workflowState as workflowState \n" +
                (!isAdmin ? "left join exConfig.executedDeliveryOption.sharedWith shareWith \n" : "") +
                ((!isAdmin && groupsIdsForUser) ? "left join exConfig.executedDeliveryOption.sharedWithGroup sharedWithGroup \n" : "") +
                " left join exConfig.executedReportUserStates as state with (state.user.id=${user.id})\n" +
                " where \n" +
                " exConfig.isDeleted=false and "+
                "(state.id is null or (state.isDeleted=false and state.isArchived=false))   \n" +
                " and exConfig.status in(:statuses) " +
                (!isAdmin ? "and ( shareWith.id=${user.id} or owner.id=${user.id} " : "") +
                ((!isAdmin && groupsIdsForUser) ? "or sharedWithGroup.id in (:groupIdsForUser)  \n" : "") +
                (!isAdmin ? ")" : "") +
                " group by workflowState.name ", queryParameters)
        return result
    }
}
