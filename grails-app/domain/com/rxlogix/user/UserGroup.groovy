package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.config.DateRangeType
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.SuperQuery
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.web.context.request.RequestContextHolder

@CollectionSnapshotAudit
class UserGroup implements Serializable {

    transient def customMessageService
    static auditable = true
    @AuditEntityIdentifier
    String name
    String description
    FieldProfile fieldProfile
    boolean isDeleted = false
    boolean isBlinded = false
    boolean isProtected = false
    boolean defaultRRAssignTo = false
    SuperQuery dataProtectionQuery
    Set<DateRangeType> dateRangeTypes = []
    String scimId

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static hasMany = [sourceProfiles: SourceProfile, dateRangeTypes: DateRangeType]

    static mapping = {
        table name: "USER_GROUP"
        name column: "NAME"
        description column: "DESCRIPTION"
        fieldProfile column: "FIELD_PROFILE_ID"
        isDeleted column: "IS_DELETED"
        isBlinded column: "IS_BLINDED"
        defaultRRAssignTo column: "DEFAULT_ASSIGN_TO"
        sourceProfiles joinTable: [name: "USER_GRP_SRC_PROFILE", column: "SRC_PROFILE_ID", key: "USER_GROUP_ID"], indexColumn: [name: "SRC_PROFILE_IDX"]
        dataProtectionQuery column: "DATA_PROTECTION_QUERY_ID"
        isProtected column: "IS_PROTECTED"
        dateRangeTypes joinTable: [name: "USER_GRP_DATE_RANGE_TYPES", column: "DATE_RANGE_TYPE_ID", key: "USER_GRP_ID"]
    }


    static constraints = {
        name(nullable: false, maxSize: 255, validator: { val, obj ->
            if (!obj.id || obj.isDirty("name")) {
                long count = UserGroup.createCriteria().count {
                    ilike('name', "${obj.name}")
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }

                if (count) {
                    return "com.rxlogix.user.UserGroup.name.unique"
                }
            }
        })
        description(nullable: true, maxSize: 4000)
        isBlinded nullable: false
        fieldProfile nullable: true
        dataProtectionQuery nullable: true

        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
        isProtected nullable: true
        scimId nullable: true
    }

    static UserGroup getDefaultReportRequestAssignedTo() {
        return findByDefaultRRAssignToAndIsDeleted(true, false)
    }

    static transients = ['authorities', 'users', 'reportRequestorValue', 'reportRequestorKey']


    static namedQueries = {
        findUserGroupBySearchString { String search, String sortBy, String sortDirection = "asc" ->
            createAlias('fieldProfile', 'fp', CriteriaSpecification.LEFT_JOIN)
            if (search) {
                or {
                    if ("All Fields".toLowerCase().contains(search.toLowerCase())) {
                        isNull('fieldProfile')
                    }
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('fp.name', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq('isDeleted', false)
            if (sortBy) {
                if (sortBy == 'fieldProfile') {
                    order('fp.name', "${sortDirection}")
                } else if (sortBy == 'owner.fullName') {
                    order('createdBy', "${sortDirection}")
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        getAllByFullName { String search ->
            ilike('name', '%' + search + '%')
        }
    }


    Set<Role> getAuthorities() {
        UserGroupRole.findAllByUserGroup(this).collect { it.role } as Set<Role>
    }


    Set<User> getUsers() {
        UserGroupUser.findAllByUserGroup(this).collect { it.user } as Set<User>
    }

    Set<User> getManagerUsers() {
        UserGroupUser.findAllByUserGroupAndManager(this, true).collect { it.user } as Set<User>
    }

    static Set<FieldProfile> fetchAllFieldProfileByUser(User user) {
        fetchAllUserGroupByUser(user)*.fieldProfile.findAll { it } as Set<FieldProfile>
    }

    static Set<SuperQuery> fetchAllDataProtectionQueriesByUser(User user) {
        fetchAllUserGroupByUser(user)*.dataProtectionQuery.findAll { it } as Set<SuperQuery>
    }

    static Set<Role> fetchAllAuthorityByUser(User user) {
        fetchAllAuthorityByUserGroup(fetchAllUserGroupByUser(user))
    }

    static List<UserGroup> fetchAllUserGroupByUser(User user) {
        return UserGroupUser.createCriteria().list {
            projections {
                distinct('userGroup')
            }
            eq('user.id', user?.id)
            userGroup {
                eq('isDeleted', false)
            }
        }
    }

    static int countAllUserGroupByUser(User user) {
        return UserGroupUser.createCriteria().get {
            projections {
                countDistinct('userGroup')
            }
            eq('user.id', user?.id)
            userGroup {
                eq('isDeleted', false)
            }
        }
    }


    static Set<Role> fetchAllAuthorityByUserGroup(List<UserGroup> userGroupList) {
        userGroupList ? UserGroupRole.findAllByUserGroupInList(userGroupList.findAll { !it.isDeleted }).collect { it.role } as Set<Role> : []
    }


    String toString() {
        name
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        def params = RequestContextHolder?.requestAttributes?.params ?: [:]
        if (newValues && (oldValues == null)) {
            newValues.put("roles", newRoles(params)?.join(", ") ?: "")
            newValues.put("users", newUsers(params)?.join(", ") ?: "")
            newValues.put("managers", newManagers(params)?.join(", ") ?: "")
            if (newValues.fieldProfile == null) {
                newValues.put("fieldProfile", "All Fields")
            }
        }
        if (newValues && oldValues) {
            List<String> newRoles = newRoles(params);
            List<String> oldRoles = oldRoles(this);
            if (((newRoles - oldRoles) + (oldRoles - newRoles)).size() > 0) {
                newValues.put("roles", newRoles?.join(", ") ?: "")
                oldValues.put("roles", oldRoles?.join(", ") ?: "")
            }
            List<String> newUsers = newUsers(params);
            List<String> oldUsers = oldUsers(this);
            if (((newUsers - oldUsers) + (oldUsers - newUsers)).size() > 0) {
                newValues.put("users", newUsers?.join(", ") ?: "")
                oldValues.put("users", oldUsers?.join(", ") ?: "")
            }
            List<String> newManagers = newManagers(params);
            List<String> oldManagers = oldManagers(this);
            if (((newManagers - oldManagers) + (oldManagers - newManagers)).size() > 0) {
                newValues.put("managers", newManagers?.join(", ") ?: "")
                oldValues.put("managers", oldManagers?.join(", ") ?: "")
            }
            if (newValues.fieldProfile == null) {
                newValues.put("fieldProfile", "All Fields")
            }
            if (oldValues.fieldProfile == null) {
                oldValues.put("fieldProfile", "All Fields")
            }
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    private List<String> newRoles(Map params) {
        List<String> roles = []
        UserGroup.withNewSession {
            for (String key in params.keySet()) {
                Role r = Role.findByAuthority(key)
                if (r && 'on' == params.get(key)) {
                    roles << ViewHelper.getMessage("app.role." + r.authority)
                }
            }
        }
        return roles
    }

    private List<String> oldRoles(userInstance) {
        UserGroupRole.withNewSession {
            return UserGroupRole.findAllByUserGroup(this)?.collect { ViewHelper.getMessage("app.role." + it.role.authority) }
        }
    }

    private List<String> newUsers(Map params) {
        List<String> users = []
        UserGroup.withNewSession {
            if (params.list('selectedUsers')) {
                for (String id in params.list('selectedUsers')) {
                    User u = User.findById(Long.valueOf(id))
                    if (u) {
                        users << u.getFullNameAndUserName()
                    }
                }
            }
        }
        return users
    }

    private List<String> oldUsers(userInstance) {
        UserGroupUser.withNewSession {
            return UserGroupUser.findAllByUserGroup(userInstance)?.collect { it.user.getFullNameAndUserName() }
        }
    }

    private List<String> newManagers(Map params) {

        List<Long> managers = params.findAll { k, v -> (k.toString().startsWith(Constants.GROUP_MANAGER) && v == "on") }.collect { k, v -> k.split("_")[1] as Long }
        List<String> newManagers = []

        UserGroup.withNewSession {
            for (String id in managers) {
                if (User.findById(Long.valueOf(id))) {
                    newManagers.add(User.findById(Long.valueOf(id)).getFullNameAndUserName())
                }
            }
        }
        return newManagers
    }

    private List<String> oldManagers(UserGroup userInstance) {
        UserGroupUser.withNewSession {
            return userInstance.getManagerUsers()?.collect { it.getFullNameAndUserName() }
        }
    }

    String rolesToString(List roles) {
        roles?.findAll { it }?.collect { ViewHelper.getMessage("app.role.${it.authority}") }?.join("; ") ?: ""
    }

    def getReportRequestorKey() {
        "${Constants.USER_GROUP_TOKEN}${this.id}"
    }

    def getReportRequestorValue() {
        this.name
    }
}
