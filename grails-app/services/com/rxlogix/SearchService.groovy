package com.rxlogix


import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole

class SearchService {

    def customMessageService
    def userService

    static transactional = false

    /**
     * Helper method to isolate/abstract away the actual search for the Users.
     * @return
     */
    def getUserList(Map params) {

        def usernameFullnameFilter = params.usernameFullname ?: null
        def emailFilter = params.email?: null
        def accountEnabledFilter = params.accountEnabled != "undefined" ? params.accountEnabled : false
        def rolesFilter = params.roles != "null" ? params.roles : null
        def userGroupsFilter = params.userGroups != "null" ? params.userGroups : null
        def localeFilter = params.locale
        def timeZoneFilter = params.timeZone

        def rolesList = []
        def userGroupsList = []

        if (rolesFilter) {
            def rolesIdList = rolesFilter.tokenize(",").collect { it as long }
            for (Long id : rolesIdList) {
                rolesList << Role.get(id)
            }
        }

        if (userGroupsFilter) {
            def userGroupsIdList = userGroupsFilter.tokenize(",").collect { it as long }
            for (Long id : userGroupsIdList) {
                userGroupsList << UserGroup.get(id)
            }
        }

        /*List must contain a value or you'll get a ORA-00936: missing expression.
          If no users in list, we send back a 0L, which will never match a userId. - morett
        */
        def userRolesIdList = rolesList ? UserRole.findAllByRoleInList(rolesList).collect { it.user.id } ?:0L : 0L
        def userGroupsUserIdList = userGroupsList ? UserGroupUser.findAllByUserGroupInList(userGroupsList).collect { it.user.id } ?:0L : 0L

        def query = User.where {

            if (usernameFullnameFilter) {
                username =~ "%" + usernameFullnameFilter.trim() + "%" || fullName =~ "%" + usernameFullnameFilter.trim() + "%"
            }

            if (emailFilter) {
                email =~ "%" + emailFilter.trim() + "%"
            }

            if (accountEnabledFilter && params.enabled) {
                enabled == accountEnabledFilter
            }

            if (rolesFilter) {
                id in userRolesIdList
            }

            if (userGroupsFilter) {
                id in userGroupsUserIdList
            }

            if (localeFilter) {
                preference.locale == localeFilter
            }

            if (timeZoneFilter) {
                preference.timeZone == timeZoneFilter
            }

        }

        //Establish initial order for util:remoteSortableColumn
        params.order = params.order ?: "asc"

        List userInstanceList = []
        def userInstanceTotal = 0

        def trueCriteria = query.criteria.size()
        if (trueCriteria > 0) {
            userInstanceList = query.list(params)
            userInstanceTotal = query.count()
        }
        [userInstanceList, userInstanceTotal]
    }


    /**
     * Searches for Users by a search term and returns a list of values containing the username, fullName and email which is
     * used to populated a Select2 dropdown to add a new user.
     * @param term
     * @return
     */
    def ajaxSearchUser(String term) {

        def query = User.where {

            username =~ "%" + term.trim() + "%"
//                fullName =~ "%" + usernameFullNameEmailFilter.trim() + "%"
//                email =~ "%" + usernameFullNameEmailFilter.trim() + "%"
        }

        List userInstanceList = []
        userInstanceList = query.list()
        userInstanceList
    }

}
