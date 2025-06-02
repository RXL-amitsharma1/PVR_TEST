package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.UserQuery
import com.rxlogix.enums.QueryTarget
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import grails.gorm.DetachedCriteria
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.AuditEntityIdentifier
import org.apache.commons.lang.builder.HashCodeBuilder
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
@DirtyCheck
class SuperQuery implements Comparable<SuperQuery>, Serializable {
    transient def queryService
    transient def userService
    @AuditEntityIdentifier
    String name
    String description
    User owner
    boolean isDeleted = false
    boolean hasBlanks = false
    List tags
    String JSONQuery // stay consistent and rename to queryJSON
    QueryTypeEnum queryType
    QueryTarget queryTarget = QueryTarget.REPORTS

    Long originalQueryId = 0         //If > 0, it is an Executed Query

    boolean factoryDefault = false
    boolean nonValidCases = false
    boolean deletedCases = false
    boolean qualityChecked = false
    boolean icsrPadderAgencyCases = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    Date lastExecuted

    Boolean autoTimeStampOff = false

    static transients = ['autoTimeStampOff']
    static hasMany = [tags: Tag, userQueries: UserQuery, userGroupQueries: UserGroupQuery, queryUserStates: QueryUserState]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false
        owner column: "PVUSER_ID", fetch: "join", cascade: 'none'
        table name: "SUPER_QUERY"
        tags joinTable: [name: "SUPER_QRS_TAGS", column: "TAG_ID", key: "SUPER_QUERY_ID"], indexColumn: [name: "TAG_IDX"]
        userQueries cascade: "all-delete-orphan", fetch: 'join', lazy: false
        userGroupQueries cascade: "all-delete-orphan", fetch: 'join', lazy: false

        'JSONQuery' column: "QUERY", sqlType: DbUtil.longStringType
        name column: "NAME"
        description column: "DESCRIPTION"
        isDeleted column: "IS_DELETED"
        hasBlanks column: "HAS_BLANKS"
        nonValidCases column: "NON_VALID_CASES"
        deletedCases column: "DELETED_CASES"
        icsrPadderAgencyCases column: "ICSR_PADDER_AGENCY_CASES"
        factoryDefault column: "FACTORY_DEFAULT"
        queryType column: "QUERY_TYPE"
        queryTarget column: "QUERY_TARGET"
        originalQueryId column: "ORIG_QUERY_ID"
        qualityChecked column: "QUALITY_CHECKED"
        lastExecuted column: "LAST_EXECUTED"
        queryUserStates joinTable: [name: "QUERY_USER_STATE", column: "ID", key: "QUERY_ID"]
    }

    static constraints = {
        name(blank: false, maxSize: 1000, validator: { val, obj ->
            // Name is unique to user
            if ((!obj.id || obj.isDirty("name") || obj.isDirty("owner")) && obj.originalQueryId == 0L) {
                long count = SuperQuery.createCriteria().count {
                    ilike('name', "${val}")
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    eq('originalQueryId', 0L)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.query.name.unique.per.user"
                }
            }
            if (val) {
                String regEx = ".*[\'\"].*";
                if (val.matches(regEx)) {
                    return "com.rxlogix.config.SuperQuery.name.validation";
                }
            }
        })
        description(nullable: true, maxSize: 1000)
        tags(nullable: true)
        JSONQuery(nullable: true, maxSize: 8388608, validator: { val, obj ->
            if (obj.JSONQuery) {
                def queryCriteria = MiscUtil.parseJsonTextForQuery(obj?.JSONQuery)
                if (!queryCriteria?.all?.containerGroups?.expressions || queryCriteria?.all?.containerGroups?.expressions[0]?.size() == 0) {
                    return "com.rxlogix.config.SuperQuery.JSONQuery.required"
                }
            }
        })
        hasBlanks(validator: { val, obj ->
            if (obj && obj.isDirty("JSONQuery") && !obj.userService.isCurrentUserAdmin()) { // for update only
                boolean oldValue = obj.getPersistentValue("hasBlanks")
                if ((oldValue || obj?.hasBlanks) && obj.queryService && !obj.queryService.isQueryUpdateable(obj)) {
                    return "app.query.update.fail.blanks"
                }
            }
        })
        queryType(nullable: false)
        queryTarget(nullable: false)
        nonValidCases(nullable: false, validator: { val, obj ->
            if (val && !obj.originalQueryId) {
                if (obj.id ? SuperQuery.countById(obj.id) : false || obj.isAttached()) {
                    if (obj.isDirty("nonValidCases") && SuperQuery.countByNonValidCasesAndOriginalQueryId(true, 0L) >= 1) {
                        return "app.query.nonValid.count"
                    }
                } else {
                    if (SuperQuery.countByNonValidCasesAndOriginalQueryId(true, 0L) >= 1) {
                        return "app.query.nonValid.count"
                    }
                }
            }
        })

        icsrPadderAgencyCases(nullable: false, validator: { val, obj ->
            if (val && !obj.originalQueryId) {
                if (obj.id ? SuperQuery.countById(obj.id) : false || obj.isAttached()) {
                    if (obj.isDirty("icsrPadderAgencyCases") && SuperQuery.countByIcsrPadderAgencyCasesAndOriginalQueryId(true, 0L) >= 1) {
                        return "app.query.icsrPadderAgencyCases.count"
                    }
                } else {
                    if (SuperQuery.countByIcsrPadderAgencyCasesAndOriginalQueryId(true, 0L) >= 1) {
                        return "app.query.icsrPadderAgencyCases.count"
                    }
                }
            }
        })

        deletedCases(nullable: false, validator: { val, obj ->
            if (val && !obj.originalQueryId) {
                if (obj.id ? SuperQuery.countById(obj.id) : false || obj.isAttached()) {
                    if (obj.isDirty("deletedCases") && SuperQuery.countByDeletedCasesAndOriginalQueryId(true, 0L) >= 1) {
                        return "app.query.deleted.count"
                    }
                } else {
                    if (SuperQuery.countByDeletedCasesAndOriginalQueryId(true, 0L) >= 1) {
                        return "app.query.deleted.count"
                    }
                }
            }
        })

        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
        lastExecuted(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }

    def beforeInsert() {
        dateCreated = new Date()
        lastUpdated = new Date()
    }

    def beforeUpdate() {
        if (!autoTimeStampOff) {
            lastUpdated = new Date()
        }
    }

    static namedQueries = {

        fetchAllOwners { User user, search ->
            projections {
                distinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        countAllOwners { User user, search ->
            projections {
                countDistinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        getAllRecordsBySearchString { LibraryFilter filter ->
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
//            Only Non Executed one
            eq("originalQueryId", 0L)
            if (filter.search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    QueryTypeEnum.searchBy(filter.search)?.each {
                        eq('queryType', it)
                    }

                    if (filter.search == "qced") {
                        eq('qualityChecked', true)
                    }
                }
            }
            ownedByUser(filter.user)
            or {
                if (filter.sharedWith?.ownerId) {
                    eq('owner.id', filter.sharedWith.ownerId)
                }
                if (filter.sharedWith?.usersId) {
                    or {
                        'in'('uq.user.id', filter.sharedWith.usersId)
                        'in'('ugq.userGroup.id', new DetachedCriteria(UserGroupUser).build {
                            projections {
                                distinct('userGroup.id')
                            }
                            'in'('user.id', filter.sharedWith.usersId)
                        })
                    }
                }
                if (filter.sharedWith?.groupsId) {
                    'in'('ugq.userGroup.id', filter.sharedWith.groupsId)
                }
                if (filter.sharedWith?.team && filter.user) {
                    or {
                        filter.user.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                    }
                }
            }
            createAlias('queryUserStates', 'state', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('user', filter.user))
            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each { cl ->
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

        countRecordsBySearchString { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            getAllRecordsBySearchString(filter)
        }

        fetchAllIdsBySearchString { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("dateCreated")
                property("lastUpdated")
                property("lastExecuted")
                property("name")
                property("description")
                property("queryType")
                property("qualityChecked")
                'owner' {
                    property("fullName", "fullName")
                }
                property("state.isFavorite", "isFavorite")
            }
            getAllRecordsBySearchString(filter)
            if (sortBy) {
                if (sortBy == 'qualityChecked') {
                    order(OrderByUtil.booleanOrder(sortBy, sortDirection))
                } else if (sortBy == 'owner.fullName') {
                    order(new Order("fullName", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (['queryType', 'name', 'queryName', 'description'].contains(sortBy)) {
                    order(new Order("${sortBy}", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }


        ownedByUser { User user ->
            createAlias('userQueries', 'uq', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupQueries', 'ugq', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            //            Only Non Executed one
            eq("originalQueryId", 0L)
            if (!user?.isAdmin()) {
                or {
                    user?.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                    eq('owner.id', user.id)
                    'in'('uq.user', user)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('ugq.userGroup', UserGroup.fetchAllUserGroupByUser(user))
                    }
                }
            }
        }

        getLatestExQueryByOrigQueryId { Long id ->
            projections {
                property('id')
            }
            eq('originalQueryId', id)
            order("dateCreated", "desc")
            maxResults(1)
        }

        countOwnedByUserWithSearch { User user, String search, Boolean isQueryTargetReports ->
            projections {
                countDistinct("id")
            }
            ownedByUserWithSearchQuery( user, search, isQueryTargetReports)
        }
        ownedByUserWithSearch { User user, String search, Boolean isQueryTargetReports ->
            projections {
                distinct('id')
                property("name")
                property("description")
                property("qualityChecked")
                property("hasBlanks")
                property("state.isFavorite", "isFavorite")
                'owner' {
                    property("fullName", "fullName")
                }
            }
            ownedByUserWithSearchQuery( user, search, isQueryTargetReports)
            }
        ownedByUserWithSearchQuery { User user, String search, Boolean isQueryTargetReports  ->

            ownedByUser(user)
            def _search = search
            boolean qc = false
            if (search?.toLowerCase()?.startsWith("qced")) {
                qc = true
                _search = search.substring(4).trim()
            }
            and {
                if (qc) eq('qualityChecked', true)

                if (_search) {
                    or {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(_search)}%")
                        iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(_search)}%")
                        'owner' {
                            iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(_search)}%")
                        }
                    }
                }

                if( isQueryTargetReports){
                    eq('queryTarget', QueryTarget.REPORTS)
                }
            }
            createAlias('queryUserStates', 'state', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('user', user))
            and {
                order('qualityChecked', 'desc')
                order('state.isFavorite', 'asc')
                order('name', 'asc')
            }
        }

        fetchAllIdsByName { String name ->
            projections {
                property('id')
            }
            iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(name)}%")
        }

    }

    transient List<String> getFieldsToValidate() {
        return this.getClass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] }
    }

    boolean validateExcluding(List additionalFields = []) {
        List<String> allFields = this.getFieldsToValidate()
//Gather excluded fields
        List<String> defaultExcludedFields = ['dateCreated', 'lastUpdated', 'createdBy', 'modifiedBy'] + (additionalFields ?: [])
        //Add other fields if necessary
        List<String> allButExcluded = allFields - defaultExcludedFields
        return this.validate(allButExcluded)
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser?.isAdmin() || owner.id == currentUser?.id || (owner.id in currentUser.getUserTeamIds()))
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isAdmin() || owner.id == currentUser.id || isVisible(currentUser) || (owner.id in currentUser.getUserTeamIds()))
    }

    boolean isVisible(User currentUser) {
        if (userQueries?.user?.any { it.id == currentUser.id }) {
            return true
        }
        List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(currentUser).flatten()
        return (userGroupQueries?.userGroup?.any {
            it.id in userGroups*.id
        } || (owner.id in currentUser.getUserTeamIds()))
    }

    boolean isFavorite(User user) {
        return queryUserStates.find { item -> item.user == user }?.isFavorite
    }

    public Set<User> getShareWithUsers() {
        Set<User> users = []
        if (userQueries) {
            users.addAll(userQueries.collect { it.user })
        }
        return users
    }

    public Set<UserGroup> getShareWithGroups() {
        Set<UserGroup> userGroups = []
        if (userGroupQueries) {
            userGroups.addAll(userGroupQueries.collect { it.userGroup })
        }
        return userGroups
    }

    def getNameWithDescription() {
        return this.name + " " + (this?.description ? "(" + this.description + ")" : "") + " - Owner: " + this.owner.fullName
    }

    Integer getParameterSize() {}

    @Override
    boolean equals(other) {
        if (!(other instanceof SuperQuery)) {
            return false
        }

        this.name == other?.name &&
                this.createdBy == other?.createdBy && this.isDeleted == other?.isDeleted
    }

    @Override
    int hashCode() {
        def builder = new HashCodeBuilder()
        if (this.name) builder.append(this.name)
        if (this.createdBy) builder.append(this.createdBy)
        if (this.isDeleted) builder.append(this.isDeleted)
        builder.toHashCode()
    }

    //https://dev.to/joemccall86/groovy-s-compareto-operator-and-equality-312n
    //https://stackoverflow.com/questions/24963680/groovy-override-compareto
    @Override
    int compareTo(SuperQuery obj) {
        //ascending order
        int value = this.name <=> obj?.name

        if (!value) {
            value = this.createdBy <=> obj?.createdBy
        }
        if (!value) {
            value = this.isDeleted <=> obj?.isDeleted
        }
        return value
    }

    int countUsage() {
        return queryService.getUsagesCount(this)
    }

    static getActiveUsersAndUserGroups(User user, String term) {
        def result = [users: [], userGroups: []]
        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableQueriesSql = " and q.id in (select q1.id  from SuperQuery as q1 left join q1.userGroupQueries as ugq1 left join q1.userQueries as uq1 " +
                "where q1.isDeleted=false and (q1.owner=:user or uq1.user=:user or ugq1.userGroup.id in (:groupIdsForUser)))"
        String groupsSql = "from UserGroup as ug where " +
                (term?" lower(ug.name) like :term and ":"")+
                " ug.id in (select ugq.userGroup.id from SuperQuery as q join q.userGroupQueries as ugq where q.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableQueriesSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"") +
                " (u.id in (select uq.user.id from SuperQuery as q join q.userQueries as uq where q.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableQueriesSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))";
        Map groupParams = user.isAdmin() ? [:] : [user: user, groupIdsForUser: groupIdsForUser]
        if(term) groupParams.put('term','%'+term.toLowerCase()+'%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        Map userParams = user.isAdmin() ? [groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [user: user, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if(term) userParams.put('term','%'+term.toLowerCase()+'%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    public String toString() {
        return "$name - $owner"
    }

    static SuperQuery fetchOriginalByName(String name) {
        List<Long> ids = SuperQuery.createCriteria().list {
            projections {
                property('id')
            }
            eq('name', name)
            eq('isDeleted', false)
            eq('originalQueryId', 0L)
        }
        if (ids) {
            return SuperQuery.load(ids.first())
        }
        return null
    }
}
