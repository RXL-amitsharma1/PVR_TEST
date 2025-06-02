package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.DbUtil
import grails.gorm.DetachedCriteria
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions
@DirtyCheck
@CollectionSnapshotAudit
class ExecutedConfiguration extends ExecutedReportConfiguration {
    String eventSelection
    ExecutedCaseSeries usedCaseSeries
    String eventGroupSelection
    static mapping = {
        table name: "EX_RCONFIG"

        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        usedCaseSeries column: "USED_CASE_SERIES_ID"
        eventGroupSelection column: "EVENT_GROUP_SELECTION", sqlType: DbUtil.longStringType
    }

    static constraints = {
        eventSelection(nullable: true)
        usedCaseSeries nullable: true
        eventGroupSelection(nullable: true)
    }

    @Override
    String getUsedEventSelection() {
        return eventSelection
    }

    @Override
    String getUsedEventGroupSelection() {
        return eventGroupSelection
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

        searchByTenant { Long id ->
            eq('tenantId', id)
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
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
            eq('isDeleted', false)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }
        countAllBySearchStringAndStatusInList  { LibraryFilter filter ->
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
                property("numOfExecutions")
                property("reportName")
                property("description")
                property("state.isFavorite", "isFavorite")
                'owner' {
                    property("fullName", "fullName")
                }
                'workflowState'{
                    property("name" , "name")
                }
            }
            fetchAllBySearchStringAndStatusInListQuery(filter)
        }
       fetchAllBySearchStringAndStatusInListQuery{ LibraryFilter filter ->
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
           if (filter.manualAdvancedFilter && filter.manualAdvancedFilter['reportName'] != null) {
               eq('reportName', filter.manualAdvancedFilter['reportName'])
           }
           if (filter.forPvq) {
               isNotNull('pvqType')
           } else {
               isNull('pvqType')
           }
            if (filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'workflowState' {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                }
            }
            ownedByAndSharedWithUser(filter.user, filter.user.isAdmin(), filter.includeArchived)
            sharedWithFilter(filter.sharedWith, filter.user)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }

            if (filter.advancedFilterCriteria) {
                createAlias('exd.emailToUsers', 'emails', CriteriaSpecification.LEFT_JOIN)
                createAlias('emailConfiguration', 'emc', CriteriaSpecification.LEFT_JOIN)
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (filter.favoriteSort) {
                and {
                    order('state.isFavorite', 'asc')
                    order('dateCreated', 'desc')
                }
            }
       }

        countAllBySearchStringAndStatusInList { LibraryFilter filter ->
            projections {
                countDistinct('id')
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if (filter.forPvq) {
                isNotNull('pvqType')
            } else {
                isNull('pvqType')
            }
            if (filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                }
            }
            ownedByAndSharedWithUser(filter.user, filter.user.isAdmin(), filter.includeArchived)
            sharedWithFilter(filter.sharedWith, filter.user)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }

            if (filter.advancedFilterCriteria) {
                createAlias('exd.emailToUsers', 'emails', CriteriaSpecification.LEFT_JOIN)
                createAlias('emailConfiguration', 'emc', CriteriaSpecification.LEFT_JOIN)
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (filter.manualAdvancedFilter && filter.manualAdvancedFilter['reportName'] != null) {
                eq('reportName', filter.manualAdvancedFilter['reportName'])
            }
        }

    }


    static getActiveUsersAndUserGroups(User user, String term) {
        def result = [users: [], userGroups: []]
        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableReportsSql = " and rc.id in (select rc1.id from ExecutedConfiguration as rc1 left join rc1.executedDeliveryOption as dop1 left join dop1.sharedWithGroup as swg1 left join dop1.sharedWith as swu1 " +
                "where rc.isDeleted=false and (rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser)))"
        String groupsSql = "from UserGroup as ug where " +
                (term?" lower(ug.name) like :term and ":"")+
                " ug.id in (select swg.id from ExecutedConfiguration as rc join rc.executedDeliveryOption as dop join dop.sharedWithGroup as swg where rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"") +
                " (u.id in (select swu.id from ExecutedConfiguration as rc join rc.executedDeliveryOption as dop join dop.sharedWith as swu where rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))"
        Map groupParams = user.isAdmin() ? [:] : [userid: user.id, groupIdsForUser: groupIdsForUser]
        if(term) groupParams.put('term','%'+term.toLowerCase()+'%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        Map userParams = user.isAdmin() ? [groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [userid: user.id, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if(term) userParams.put('term','%'+term.toLowerCase()+'%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    static getStates(User user) {
        def isAdmin = user.isAdmin()
        def queryParameters = [ statuses: ReportExecutionStatusEnum.reportsListingStatuses]
        def groupsIdsForUser;
        if (!isAdmin) {
            groupsIdsForUser = UserGroup.fetchAllUserGroupByUser(user).id
            if (groupsIdsForUser)
                queryParameters << [groupIdsForUser: groupsIdsForUser]
        }
        def result = executeQuery("select workflowState.name, count(distinct exConfig.id) \n" +
                "from ExecutedConfiguration exConfig \n" +
                "left join exConfig.owner as owner \n" +
                "left join exConfig.workflowState as workflowState \n" +
                (!isAdmin ? "left join exConfig.executedDeliveryOption.sharedWith shareWith \n" : "") +
                ((!isAdmin && groupsIdsForUser) ? "left join exConfig.executedDeliveryOption.sharedWithGroup sharedWithGroup \n" : "") +
                " left join exConfig.executedReportUserStates as state with (state.user.id=${user.id})\n" +
                "  where \n" +
                " exConfig.isDeleted=false  and exConfig.pvqType is null and exConfig.status in (:statuses) and "+
                "((state.id is null and exConfig.archived=false) or (state.isDeleted=false and state.isArchived=false))  \n" +
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
}
