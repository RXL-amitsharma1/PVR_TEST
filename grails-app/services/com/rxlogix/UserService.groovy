package com.rxlogix

import com.rxlogix.config.BaseDeliveryOption
import com.rxlogix.config.Email
import com.rxlogix.config.Notification
import com.rxlogix.config.ReportField
import com.rxlogix.config.Tenant
import com.rxlogix.enums.UserType
import com.rxlogix.user.*
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserService {

    def springSecurityService
    def brokerMessagingTemplate
    def hazelService
    GrailsApplication grailsApplication
    def roleHierarchy
    def passwordService
    def CRUDService
    def utilService
    def dataSource_pva

    /*
    ==============================================================================================
    Keep methods below non-transactional
    ==============================================================================================
    */

    User getUser() {
        return springSecurityService.loggedIn && springSecurityService?.principal?.id ? User.get(springSecurityService.principal.id) : null
    }

    def setOwnershipAndModifier(Object object) {
        if (springSecurityService.isLoggedIn()) {
            def user = getUser()
            //Set one time only
            if (object.hasProperty("createdBy")) {
                if (!object.createdBy) {
                    object.createdBy = user.username
                }
            }
            if (object.hasProperty("modifiedBy")) {
                object.modifiedBy = user.username
            }
        }
        return object
    }

    List<Map<String, String>> getAllEmails(config) {
        List<Map<String, String>> allEmails = getAllEmails();
        def emailsInConfiguration = config?.deliveryOption?.emailToUsers
        if (emailsInConfiguration) {
            def toAdd = emailsInConfiguration?.findAll { !allEmails.find { eml -> eml.key == it } }?.collect { [key: it, value: it + " - " + it] }
            return (allEmails + toAdd).sort { it.value.toLowerCase() }
        } else {
            return allEmails
        }
    }

    List<Map<String, String>> getAllEmailsForCC(String emails) {
        List<Map<String, String>> allEmails = getAllEmails();
        String[] emailsInCC = emails?.split(",")
        if (emails) {
            def toAdd = emailsInCC?.findAll { !allEmails.find { eml -> eml.key == it } }?.collect { [key: it, value: it + " - " + it] }
            return (allEmails + toAdd).sort { it.value.toLowerCase() }
        } else {
            return allEmails
        }
    }

    List<Map<String, String>> getAllEmails() {
        Map resultMap = Email.findAllByIsDeletedAndTenantId(false, Tenants.currentId().toString().toLong()).collectEntries() { [(it.email), it.description + " - " + it.email] }
        activeUsersByTenant.each { resultMap[it.email] = it.fullName + " - " + it.email }
        return resultMap.collect { k, v -> [key: k, value: v] }.sort { it.value.toLowerCase() }
    }

    List<Map<String, String>> getAllEmails(Integer tenantId) {
        def users = getActiveUsersByTenant(tenantId).collect { [id: it.email, text: it.fullName + " - " + it.email] }.findAll { it.id }
        def emailsFromDictionary = Email.findAllByIsDeletedAndTenantId(false, tenantId.toLong()).collect { [id: it.email, text: it.description + " - " + it.email] }
        return (emailsFromDictionary + users).unique { it.id }.sort { it.text.toLowerCase() }
    }

    List<User> getActiveUsersByTenant(Integer tenantId = null) {
        Long currentTenantId = tenantId ? tenantId.toLong() : Tenants.currentId().toString().toLong()
        List<User> userList =  User.createCriteria().list([sort: 'username', order: 'asc']) {
            eq('enabled', true)
            createAlias('tenants','t')
            eq('t.id', currentTenantId )
        }
        return userList
    }

    List<User> getActiveUsers() {
        return User.findAllByEnabled(true,[sort: 'username',order: 'asc'])
    }

    List<UserGroup> getActiveGroups() {
        return UserGroup.findAllByIsDeleted(false,[sort: 'name',order: 'asc'])
    }

    List<User> getAllowedSharedWithUsersForCurrentUser(String search = null) {
        String _search = search?.toLowerCase()
        User currentUser = getUser()
        List<User> users = [];
        if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_ALL")) {
            users = getActiveUsers()
        } else if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_GROUP")) {
            def activeGroups = UserGroup.fetchAllUserGroupByUser(currentUser)
            activeGroups.each { users.addAll(it.users) }
            users = users.findAll{it}.unique { it.id }.sort { it.username }
        }
        if (users.size() == 0) users = [currentUser]
        if (search)
            return users.findAll{it}.findAll { (it.fullName ?: it.username).toLowerCase().indexOf(_search) > -1 }
        else
            return users
    }

    List<UserGroup> getAllowedSharedWithGroupsForCurrentUser(String search = null) {
        String _search = search?.toLowerCase()
        User currentUser = getUser()
        List<UserGroup> groups = []
        if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_ALL")) {
            groups = getActiveGroups()
        } else if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_GROUP")) {
            groups = UserGroup.fetchAllUserGroupByUser(currentUser).findAll{it}.sort { it.name }
        }
        if (search)
            return groups.findAll{it}.findAll { it.name.toLowerCase().indexOf(_search) > -1 }
        else
            return groups
    }

    void removeUserFromDeliveryOptionSharedWith(User user, BaseDeliveryOption deliveryOption, Long ownerId) {
        if (!deliveryOption) {
            return
        }
        User userToDelete = deliveryOption.sharedWith.find {it.id == user.id}
        if (!userToDelete || userToDelete.id==ownerId) {
            return
        }
        deliveryOption.removeFromSharedWith(userToDelete)
        CRUDService.saveWithoutAuditLog(deliveryOption)
    }

    def getAdminUsers() {
        def adminUsers = UserRole.findAllByRoleInList([Role.findAllByAuthority("ROLE_ADMIN"), Role.findAllByAuthority("ROLE_DEV")])*.user
        return adminUsers
    }

    @Transactional
    User createUser(String username, Preference pref, List<String> roles, String createdBy, Tenant defaulTenant , UserType userType = UserType.NON_LDAP, String fullName = null) {

        def user = User.findByUsernameIlike(username)
        if (!user) {
            user = new User(username: username, preference: pref, createdBy: createdBy, modifiedBy: createdBy, type: userType, fullName: fullName)
            user.preference.actionItemEmail = AIEmailPreference.getDefaultValues(user.preference)
            user.preference.reportRequestEmail = ReportRequestEmailPreference.getDefaultValues(user.preference)
            user.preference.pvcEmail = PVCEmailPreference.getDefaultValues(user.preference)
            user.preference.pvqEmail = PVQEmailPreference.getDefaultValues(user.preference)
            user.addToTenants(defaulTenant)
        }
        if(user.type == UserType.NON_LDAP)
            user.password = springSecurityService.encodePassword(Holders.config?.password?.defaultUserPassword ?: 'changeit')

        if(user.username ==  utilService.getJobUser()){
            user.enabled = false;
        }
        user.save(flush:true)
        roles.each { UserRole.create(user, Role.findByAuthority(it), true) }

        user
    }

    User getCurrentUser() {
        return (User) springSecurityService.currentUser
    }

    boolean isCurrentUserAdmin() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }

    boolean isCurrentUserDev() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_DEV")
    }

    boolean isAnyGranted(String role) {
        return SpringSecurityUtils.ifAnyGranted(role)
    }

    //Utility method for finding Role access which's not currently loggedIn.
    boolean hasAccessToRole(User user, String role) {
        List<GrantedAuthority> authorities = UserRole.findAllByUser(user).collect {
            new SimpleGrantedAuthority(it.role.authority)
        }
        return roleHierarchy.getReachableGrantedAuthorities(authorities)?.find { it.authority == role }
    }

//Use following method for sending push notifications to user
    void pushNotificationToBrowser(Notification notification, User user, Boolean remove = false) {
        if (!user) {
            log.error("No user found to broadcast message")
            return
        }
        try {
            Map message = notification.toMap(user)
            if (remove) message.delete = true
            ConfigObject hazelcast = grailsApplication.config.hazelcast
            if (hazelcast.enabled) {
                String notificationChannel = hazelcast.notification.channel
                hazelService.publishToTopic(notificationChannel, ((message as JSON)).toString())
            } else {
                brokerMessagingTemplate.convertAndSend(user.notificationChannel, ((message as JSON)).toString())
            }
        } catch (Exception ex) {
            log.error("Error while pushing notification to browser for user:${user?.id} and notification : ${notification?.id} " + ex)
        }
    }

    List<User> findAllUsersHavingFullName(String sortBy = 'fullName', String order = 'asc') {
        return User.findAllByFullNameIsNotNullAndEnabled(true, [sort: sortBy, order:order])
    }

    User getUserByUsername(String username) {
        User.findByUsernameIlike(username)
    }

    void updateBlindedFlagForUsersAndGroups() {
        List<FieldProfile> blindedProfiles = FieldProfileFields.findAllByIsBlinded(true).collect {it.fieldProfile}.unique()
        Set<UserGroup> blindedGroups = blindedProfiles ? UserGroup.findAllByIsDeletedAndFieldProfileInList(false, blindedProfiles) : []
        Set<User> blindedUsers = (blindedGroups*.getUsers()).flatten()
        UserGroup.executeUpdate("update UserGroup set isBlinded=false")
        User.executeUpdate("update User set isBlinded=false")
        if (blindedGroups) {
            UserGroup.executeUpdate("update UserGroup set isBlinded=true where id in :ids", [ids: blindedGroups*.id])
        }
        if (blindedUsers) {
            User.executeUpdate("update User set isBlinded=true where id in :ids", [ids: blindedUsers*.id])
        }
    }

    void updateProtectedFlagForUsersAndGroups() {
        List<FieldProfile> protectedProfiles = FieldProfileFields.findAllByIsProtected(true).collect {it.fieldProfile}.unique()
        Set<UserGroup> protectedGroups = protectedProfiles ? UserGroup.findAllByIsDeletedAndFieldProfileInList(false, protectedProfiles) : []
        Set<User> protectedUsers = (protectedGroups*.getUsers()).flatten()
        UserGroup.executeUpdate("update UserGroup set isProtected=false")
        User.executeUpdate("update User set isProtected=false")
        if (protectedGroups) {
            UserGroup.executeUpdate("update UserGroup set isProtected=true where id in :ids", [ids: protectedGroups*.id])
        }
        if (protectedUsers) {
            User.executeUpdate("update User set isProtected=true where id in :ids", [ids: protectedUsers*.id])
        }
    }

    def getAdminUserEmailIds() {
        def adminUserEmailIds = UserRole.findAllByRoleInList([Role.findAllByAuthority("ROLE_ADMIN")])*.user.email
        return adminUserEmailIds
    }

    @Transactional
    User changePassword(User user, String newPassword) {
        user.password = springSecurityService.encodePassword(newPassword)
        user.accountLocked = false
        user.passwordModifiedTime = new Date()
        user.passwordDigests.clear()
        user.addToPasswordDigests(passwordService.digestPassword(newPassword))
        user
    }

    def addUsers(Map user,Preference preference,String fullName){
        User userInstance = new User()
        userInstance.fullName = fullName
        userInstance.username = user.USERNAME.toString().trim()
        userInstance.email = user.EMAIL
        userInstance.enabled = (user.ENABLED.toString().trim().toLowerCase() =='yes' ? true : false)
        userInstance.accountLocked = (user.ACCOUNT_LOCKED.toString().trim().toLowerCase() =='yes' ? true : false)
        userInstance.accountExpired = (user.ACCOUNT_EXPIRED.toString().trim().toLowerCase() =='yes' ? true : false)
        userInstance.preference = preference
        userInstance.preference.actionItemEmail = AIEmailPreference.getDefaultValues(preference)
        userInstance.preference.reportRequestEmail = ReportRequestEmailPreference.getDefaultValues(preference)
        userInstance.preference.pvcEmail = PVCEmailPreference.getDefaultValues(preference)
        userInstance.preference.pvqEmail = PVQEmailPreference.getDefaultValues(preference)
        userInstance.badPasswordAttempts = 0
        userInstance.apiToken = null
        String userType = user.USER_TYPE.toString().toLowerCase().trim()
        if(userType.equals('non_ldap') || userType.equals('non ldap')){
            userInstance.type = UserType.NON_LDAP
            userInstance.password = springSecurityService.encodePassword(Holders.config?.password?.defaultUserPassword ?: 'changeit')
        }
        else{
            userInstance.type = UserType.LDAP
        }
        if(!Holders.config.getProperty('pvreports.multiTenancy.enabled', Boolean)){
            Set<Tenant> tenants = []
            tenants.add(Tenant.findById(Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Long)))
            userInstance.tenants = tenants
        }
        return userInstance
    }

    List<String> getAllGroupsForUser(String groups){
        List groupsToBeAdded = []
        List allGroups = groups?.split(",")?.findAll { it }?.collect {
            it.trim() as String
        } ?: []
        allGroups.each{
            if (UserGroup.findByName(it)) {
                groupsToBeAdded.add(it)
            }
        }
        return groupsToBeAdded
    }

    List<String> getAllRolesForUser(String roles){
        List rolesList = Role.list()
        List rolesToBeAdded = []
        List allRoles = roles?.trim()?.split(",")?.findAll { it }?.collect {
            it.trim() as String
        } ?: []
        allRoles.each { itr ->
            Role r = rolesList.find { role -> (itr == role.toString()) }
            if (r) {
                rolesToBeAdded.add(r.authority.toString())
            }
        }
        return rolesToBeAdded
    }

    void addAllGroupsAndRolesForUser(def userInstance , List allGroupsAdded, allRolesAdded) {

        if (allRolesAdded) {
            allRolesAdded.each { roles ->
                UserRole.create userInstance, Role.findByAuthority(roles), true
            }
        }

        if (allGroupsAdded) {
            allGroupsAdded.each { groups ->
                UserGroup userGroup = UserGroup.findByName(groups)
                if(userGroup) {
                    UserGroupUser.create userGroup, userInstance, false, true
                }
            }
        }
    }

    void writeUsersToExcel(Sheet sheet){
            User.list().eachWithIndex { user  , index ->
                String userGroups = UserGroupUser.findAllByUser(user).userGroup.name?.collect { it.replaceAll(',', '\\,') }.join(',')
                String userRoles  = UserRole.findAllByUser(user).role?.collect { it.toString().replaceAll(',', '\\,') }.join(',')
                Row rowIterator = sheet.createRow(index + 1)
                def singleUserList = [user.username?:"", user.fullName?:"" , user.email?:"" , user.enabled?"Yes":"No" , user.accountLocked ? "Yes":"No" , user.accountExpired?"Yes":"No", (user.preference.locale == Locale.ENGLISH) ? "English":"Japanese" , user.preference.timeZone , userGroups , userRoles , user.type.toString()?:"" , user.tenants.join(", ")]
                for (int i = 0; i < 12; i++) {
                    Cell cellIterator = rowIterator.createCell(i)
                    cellIterator.setCellValue(singleUserList.get(i))
                }
        }
    }

    //It return the List of users who are not disabled
    List<User> getAllEnabledUsers(UserGroup userGroup) {
        return UserGroupUser.fetchAllByUserGroupAndSearchString(userGroup).list()?.collect{it[0]}?:[]
    }

    List<String> fetchUniqueFieldIdList(List<String> reportFieldNameList) {
        Sql sql = new Sql(dataSource_pva)
        List<String> uniqueFieldIds = new ArrayList<>()
        try {
            String insertStatement = "begin execute immediate 'begin  pkg_pvr_app_util.p_truncate_table(''GTT_TAG_LIST''); end;';"

            reportFieldNameList.each {
                insertStatement += "Insert into GTT_TAG_LIST (TAG_TEXT) VALUES ('${it}');\n"
            }
            insertStatement += "END;"
            sql.execute(insertStatement)
            String sqlString = "select unique_field_id from rpt_field where name in (select TAG_TEXT from GTT_TAG_LIST) and unique_field_id is not null"
            List<GroovyRowResult> results = sql.rows(sqlString)
            results.each {
                uniqueFieldIds.add(it?.getAt("UNIQUE_FIELD_ID"))
            }
            return uniqueFieldIds
        } catch (Exception e) {
            log.error("Some Error Occured while fetching uniqueFieldIds for reportFieldNameList: ${reportFieldNameList}")
            log.error(e.printStackTrace())
            return
        } finally {
            sql?.close()
        }
    }

    String getUserPreferences(String key) {
        String preferences = getCurrentUser()?.preference?.userPreferences
        if (preferences) {
            return JSON.parse(preferences)[key]
        }
        return null
    }

    Map allowedAssignedToUserListPvcPvq(String term, Integer page, Integer max, String userGroupIdWithPrefix, List roles) {
        if (!max) max = 30
        if (!page) page = 1
        if (term) term = term?.trim()
        int offset = Math.max(page - 1, 0) * max

        Set<User> activeUsers = getAllowedSharedWithUsersForCurrentUser(term)
        if (!userGroupIdWithPrefix) {
            List rolesList = Role.findAllByAuthorityInList(roles)
            Set userWithRoleIds = UserRole.findAllByRoleInList(rolesList)?.collect { it.userId }
            List userGroupRoleList = com.rxlogix.user.UserGroupRole.findAllByRoleInList(rolesList).collect { it.userGroup }
            userWithRoleIds.addAll(com.rxlogix.user.UserGroupUser.findAllByUserGroupInList(userGroupRoleList)?.collect { it.userId })
            activeUsers = activeUsers.findAll { userWithRoleIds.contains(it.id) }
        }
        List<User> userList = activeUsers.unique { it.id }.collect { [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: it.isBlinded] }.sort {
            it.text?.toLowerCase()
        }
        if (userGroupIdWithPrefix) {
            String[] parts = userGroupIdWithPrefix.split("_")
            if (parts.length > 1 && parts[1]) {
                Long id = Long.parseLong(parts[1])
                UserGroup userGroup = UserGroup.get(id)
                Set ids = userGroup.users?.collect { Constants.USER_TOKEN + it.id } ?: []
                userList = userList.findAll { ids.contains(it.id) }
            }
        }
        def items = userList.subList(Math.min(offset, userList.size()), Math.min(offset + max, userList.size()))
        return [items: items, total_count: userList.size()]
    }

    Map allowedAssignedToGroupListPvcPvq(String term, Integer page, Integer max, String userIdWithPrefix, List roles) {
        if (!max) max = 30
        if (!page) page = 1
        if (term) term = term?.trim()
        int offset = Math.max(page - 1, 0) * max
        Set<UserGroup> activeGroups = getAllowedSharedWithGroupsForCurrentUser(term)?.findAll { UserGroup group ->
            group.authorities.find { Role role -> role.authority in roles }
        }

        List<User> groupList = activeGroups.unique { it.id }.collect { [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: it.isBlinded] }.sort {
            it.text?.toLowerCase()
        }
        if (userIdWithPrefix) {
            String[] parts = userIdWithPrefix.split("_")
            if (parts.length > 1 && parts[1]) {
                Long id = Long.parseLong(parts[1])
                User user = User.get(id)
                Set ids = UserGroup.fetchAllUserGroupByUser(user)?.collect { Constants.USER_GROUP_TOKEN + it.id } ?: []
                groupList = groupList.findAll { ids.contains(it.id) }
            }
        }
        def items = groupList.subList(Math.min(offset, groupList.size()), Math.min(offset + max, groupList.size()))
        return [items: items, total_count: groupList.size()]
    }

    void addToFieldsWithFlag(FieldProfile fieldProfile, ReportField field, boolean isBlinded, boolean isProtected, boolean isHidden) {
        FieldProfileFields profileField = new FieldProfileFields(fieldProfile: fieldProfile, reportField: field, isBlinded: isBlinded, isProtected: isProtected, isHidden: isHidden)
        CRUDService.save(profileField)
    }
}
