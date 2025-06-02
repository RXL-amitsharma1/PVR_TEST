package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.UserService
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
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
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
import com.rxlogix.util.MiscUtil
@DirtyCheck
@CollectionSnapshotAudit
class CaseSeries extends BaseCaseSeries {
    static auditable = [ignore: ["executing", "isEnabled", "nextRunDate", "numExecutions"]]

    transient def utilService

    String generateSpotfire
    SuperQuery globalQuery
    CaseSeriesDateRangeInformation caseSeriesDateRangeInformation
    List<QueryValueList> globalQueryValueLists
    EmailConfiguration emailConfiguration
    static hasOne = [deliveryOption: CaseDeliveryOption]
    static hasMany = [globalQueryValueLists: QueryValueList, caseSeriesUserStates: CaseSeriesUserState]
    static transients = ['queriesIdsAsString', 'configSelectedTimeZone']

    static constraints = {
        generateSpotfire(nullable: true, maxSize: 8000)
        globalQuery nullable: true
        emailConfiguration nullable: true
        seriesName(nullable: false, blank: false, maxSize:555,validator: { val, obj ->
            //Name is unique to user
            if (!obj.id || obj.isDirty("seriesName") || obj.isDirty("owner")) {
                long count = CaseSeries.createCriteria().count{
                    ilike('seriesName', "${val}")
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    if (obj.id){ne('id', obj.id)}
                }
                if (count) {
                    return "com.rxlogix.config.executed.caseSeries.seriesName.unique.per.user"
                }
            }
        })
    }

    static mapping = {

        autoTimestamp false
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        table name: "CASE_SERIES"
        generateSpotfire column: "GENERATE_SPOTFIRE"
        deliveryOption cascade: 'all'
        emailConfiguration cascade: 'all'
        tags joinTable: [name: "CASE_SERIES_TAGS", column: "TAG_ID", key: "CASE_SERIES_ID"], indexColumn: [name: "TAG_IDX"], cascade: 'all'
        caseSeriesDateRangeInformation column: "CASE_SERIES_DATE_RANGE_INFO_ID"
        globalQueryValueLists joinTable: [name: "CASE_SERIES_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "CASE_SERIES_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        caseSeriesUserStates joinTable: [name: "CASE_SERIES_USER_STATE", column: "ID", key: "CASE_SERIES_ID"]
    }

    static namedQueries = {

        fetchAllOwners { User user, search ->
            projections {
                distinct("owner")
            }
            ownedByAndSharedWithUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        countAllOwners { User user, search ->
            projections {
                countDistinct("owner")
            }
            ownedByAndSharedWithUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        countCaseSeriesBySearchString { LibraryFilter filter->
            projections {
                countDistinct("id")
            }
            fetchCaseSeriesBySearchStringQuery(filter)
        }
        fetchCaseSeriesBySearchString { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("dateCreated")
                property("qualityChecked")
                property("lastUpdated")
                property("seriesName")
                property("numExecutions")
                property("description")
                'owner' {
                    property("fullName", "fullName")
                }
                property("state.isFavorite","isFavorite")
            }
            fetchCaseSeriesBySearchStringQuery(filter)
            if (sortBy) {
                if (sortBy == 'qualityChecked') {
                    order(OrderByUtil.booleanOrder(sortBy, sortDirection))
                } else if (sortBy == 'owner.fullName') {
                    order(new Order("fullName", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (['reportName', 'seriesName', 'description'].contains(sortBy)) {
                    order(new Order("${sortBy}", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }
        fetchCaseSeriesBySearchStringQuery { LibraryFilter filter->

            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if (filter.search) {

                or {
                    iLikeWithEscape('seriesName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    if (filter.search == "qced") {
                        eq('qualityChecked', true)
                    }
                }
            }
            ownedByAndSharedWithUser(filter.user)
            if (!filter.user?.isAdmin() || filter.sharedWith?.ownerId || filter.sharedWith?.usersId || filter.sharedWith?.groupsId || filter.sharedWith?.team) {
                or {
                    if (filter.sharedWith?.ownerId) {
                        eq('owner.id', filter.sharedWith.ownerId)
                    }
                    if (filter.sharedWith?.usersId) {
                        or {
                            'in'('sw.id', filter.sharedWith.usersId)
                            'in'('swg.id', new DetachedCriteria(UserGroupUser).build {
                                projections {
                                    distinct('userGroup.id')
                                }
                                'in'('user.id', filter.sharedWith.usersId)
                            })
                        }
                    }
                    if (filter.sharedWith?.groupsId) {
                        'in'('swg.id', filter.sharedWith.groupsId)
                    }
                    if (filter.sharedWith?.team && filter.user) {
                        or {
                            filter.user.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                        }
                    }
                }
            }

            if (filter.advancedFilterCriteria) {
                createAlias('exd.emailToUsers', 'emails', CriteriaSpecification.LEFT_JOIN)
                createAlias('emailConfiguration', 'emc', CriteriaSpecification.LEFT_JOIN)
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            eq('isDeleted', false)
            createAlias('caseSeriesUserStates', 'state', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('user', filter.user))
            if (filter.favoriteSort) {
                and {
                    order('state.isFavorite', 'asc')
                    order('lastUpdated', 'desc')
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        countScheduledCaseSeriesBySearchString { String search, List<Long> alreadyRunningConfigurationIds, User user, Map sharedWith ->
            projections {
                countDistinct("id")
            }
            fetchScheduledCaseSeriesBySearchStringQuery(search, alreadyRunningConfigurationIds, user, sharedWith)
        }
        fetchScheduledCaseSeriesBySearchString { String search, List<Long> alreadyRunningConfigurationIds, User user, Map sharedWith ->
            projections {
                distinct('id')
                property('nextRunDate')
                property('numExecutions')
                property('seriesName')
                'owner' {
                    property("fullName", "fullName")
                }
            }
            fetchScheduledCaseSeriesBySearchStringQuery(search, alreadyRunningConfigurationIds, user, sharedWith)
        }
        fetchScheduledCaseSeriesBySearchStringQuery { String search, List<Long> alreadyRunningConfigurationIds, User user, Map sharedWith ->

            isNotNull('nextRunDate')
            eq('executing', false)
            eq('isEnabled', true)
            fetchMode("owner", FetchMode.JOIN)
            if(search) {
                or {
                    iLikeWithEscape('seriesName', "%${EscapedILikeExpression.escapeString(search)}%")
                    "owner" {
                        iLikeWithEscape("fullName", "%${EscapedILikeExpression.escapeString(search)}%")
                    }
                }
            }

            if (alreadyRunningConfigurationIds?.size() > 0) {
                and {
                    alreadyRunningConfigurationIds.collate(999).each { list ->
                        not {
                            inList("id", list)
                        }
                    }
                }
            }

            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            ownedByAndSharedWithUser(user)
        }

        ownedByAndSharedWithUser { User currentUser ->
            createAlias('deliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            if (!currentUser.isAdmin()) {
                or {
                    currentUser.getUserTeamIds()?.collate(999)?.each{  'in'('owner.id', it) }
                    eq('owner.id', currentUser?.id)
                    'in'('sw.id', currentUser.id)
                    if (UserGroup.countAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        searchBySearchString { User user, String searchString ->
            projections {
                distinct("id")
                property("seriesName")
            }
            searchBySearchStringQuery(user, searchString)
        }
        countBySearchString { User user, String searchString ->
            projections {
                countDistinct("id")
            }
            searchBySearchStringQuery(user, searchString)
        }
        searchBySearchStringQuery { User user, String searchString ->
            ownedByAndSharedWithUser(user)
            if (searchString) {
                iLikeWithEscape('seriesName', "%${EscapedILikeExpression.escapeString(searchString)}%")
            }
        }

        nextCaseSeriesToExecute { List ids ->
            and {
                lte 'nextRunDate', new Date()
                eq 'isEnabled', true
                eq 'isDeleted', false
                if (ids) {
                    not {
                        'in'('id', ids)
                    }
                }
            }
            order 'nextRunDate', 'asc'
        }

        getAllScheduledCaseSeriesForUserAndStartDateAndEndDate { User user, Date startDate, Date endDate ->
            ownedByAndSharedWithUser(user)
            isNotNull('nextRunDate')
            eq('isEnabled', true)
            gte('nextRunDate', startDate)
            lte('nextRunDate', endDate)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }
    }


    String getQueriesIdsAsString() {
        SuperQuery superQuery = globalQuery
        if (superQuery) {
            if (superQuery.queryType == QueryTypeEnum.SET_BUILDER) {
                superQuery = (QuerySet) GrailsHibernateUtil.unwrapIfProxy(superQuery)
                String ids = superQuery.queries.id.toString()
                return ids.substring(1, ids.length() - 1) // remove "[" and "]" from the list
            } else {
                return superQuery.id
            }
        }
        return null
    }

    String getConfigSelectedTimeZone() {
        Constants.DEFAULT_SELECTED_TIMEZONE
    }

    String getInstanceIdentifierForAuditLog() {
        return seriesName
    }

    boolean isFavorite(User user) {
        return caseSeriesUserStates.find { item -> item.user == user }?.isFavorite
    }

    boolean isEditable(User user) {
        return (user.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == user?.id || user.isAdmin() || owner.id in user.getUserTeamIds())))
    }

    public Set<User> getShareWithUsers() {
        Set<User> users = []
        if (deliveryOption?.sharedWith) {
            users.addAll(deliveryOption.sharedWith)
        }
        return users
    }

    public Set<UserGroup> getShareWithGroups() {
        Set<UserGroup> userGroups = []
        if (deliveryOption?.sharedWithGroup) {
            userGroups.addAll(deliveryOption.sharedWithGroup)
        }
        return userGroups
    }

    public Set<User> getAllSharedUsers() {
        Set<User> users = []
        users.addAll(deliveryOption.sharedWith)
        deliveryOption.sharedWithGroup.each { UserGroup userGroup ->
            users.addAll(userGroup.users)
        }
        users
    }

    transient boolean isRunning() {
        return this.executing || (getId() && (ExecutionStatus.countByEntityIdAndEntityTypeAndExecutionStatusInList(getId(), ExecutingEntityTypeEnum.CASESERIES, [ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.BACKLOG]) > 0))
    }

    static getActiveUsersAndUserGroups(User user, String term) {
        def result = [users: [], userGroups: []]

        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableReportsSql = " where rc.id in (select rc1.id from CaseSeries as rc1 left join rc1.deliveryOption as dop1 left join dop1.sharedWithGroup as swg1 left join dop1.sharedWith as swu1 " +
                "where rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser))"
        String groupsSql = "from UserGroup as ug where " +
                (term?" lower(ug.name) like :term and ":"")+
                "ug.id in (select swg.id from CaseSeries as rc join rc.deliveryOption as dop join dop.sharedWithGroup as swg " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"") +
                " (u.id in (select swu.id from CaseSeries as rc join rc.deliveryOption as dop join dop.sharedWith as swu" +
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

    boolean isViewableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || isVisible(currentUser) || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    public boolean isVisible(User currentUser) {
        return deliveryOption.isSharedWith(currentUser)
    }

    static List<Long> getAlreadyRunningConfigurationIds() {
        return CaseSeries.executeQuery("select series.id from CaseSeries series, ExecutionStatus exStatus where series.id = exStatus.entityId and exStatus.reportVersion=series.numExecutions+1 and series.isDeleted = true and exStatus.entityType in (:entityTypes)", [entityTypes: [ExecutingEntityTypeEnum.CASESERIES]])
    }

    @Override
    public String toString() {
        super.toString()
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && oldValues?.keySet()?.contains("caseSeriesDateRangeInformation")) {
            withNewSession {
                CaseSeries caseSeries = CaseSeries.read(id);
                if (oldValues?.keySet()?.contains("caseSeriesDateRangeInformation"))
                    oldValues.put("caseSeriesDateRangeInformation", GrailsHibernateUtil.unwrapIfProxy(caseSeries.caseSeriesDateRangeInformation))
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }

    def beforeInsert() {
        dateCreated = new Date()
        lastUpdated = new Date()
    }

    def beforeUpdate() {
        List dirtyProperties = this.dirtyPropertyNames
        List executionProperties = ["nextRunDate", "isEnabled", "executing", "numExecutions"]
        if (utilService.containsOnlyValues(dirtyProperties, executionProperties)) {
            return
        } else {
            lastUpdated = new Date()
        }
    }

}
