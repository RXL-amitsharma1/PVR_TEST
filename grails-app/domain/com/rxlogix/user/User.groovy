package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.Tenant
import com.rxlogix.enums.AuthType
import com.rxlogix.enums.UserType
import com.rxlogix.util.ViewHelper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.springframework.web.context.request.RequestContextHolder
@CollectionSnapshotAudit
class User {
    static auditable = [ignore:['passwordDigests','apiToken','version', 'lastUpdated', 'dateCreated', 'createdBy','modifiedBy','badPasswordAttempts','lastToLastLogin','lastLogin','password']]
    def customMessageService

    String username
    String password = ""
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Preference preference = new Preference()
    String fullName
    boolean isBlinded = false
    boolean isProtected = false
    String email

    Integer badPasswordAttempts = 0
    Date lastToLastLogin
    Date lastLogin

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    Set<Tenant> tenants = []

    String apiToken
    String scimId
    //for differentiating LDAP and NON-LDAP User
    UserType type = UserType.LDAP
    Date passwordModifiedTime

    AuthType authType = null

    Set<String> passwordDigests = new HashSet<>()

    transient boolean mandatoryAudit = false



    static hasMany = [reportTemplates: ReportTemplate, tenants: Tenant , passwordDigests:String]

    static transients = ['reportingFields', 'reportRequestorValue', 'reportRequestorKey' , 'authType']

    @SuppressWarnings("GroovyAssignabilityCheck")
    static mapping = {
        preference lazy:true
        table name: "PVUSER"

        username column: "USERNAME"
        enabled column: "ENABLED"
        accountExpired column: "ACCOUNT_EXPIRED"
        accountLocked column: "ACCOUNT_LOCKED"
        passwordExpired column: "PASSWORD_EXPIRED"
        preference column: "PREFERENCE_ID"
        badPasswordAttempts column: "BAD_PASSWORD_ATTEMPTS"
        fullName column: "FULLNAME"
        email column: "EMAIL"
        isBlinded column: "IS_BLINDED"
        isProtected column: "IS_PROTECTED"
        apiToken sqlType: "varchar2(4000)"
        lastToLastLogin column: 'LAST_TO_LAST_LOGIN'
        lastLogin column: 'LAST_LOGIN'
        tenants joinTable: [name: "PVUSER_TENANTS", column: "TENANT_ID", key: "PVUSER_ID"]
        scimId column: 'SCIM_ID'
        type column: 'USER_TYPE'
        autoTimestamp false
    }

    static constraints = {
        username blank: false, unique: true, maxSize: 255 ,validator: { val, obj ->
            if (!obj.id || obj.isDirty("username")) {
                Boolean exists = false
                exists = User.countByUsernameIlike(obj.username)
                if (exists) return "com.rxlogix.user.User.name.unique.per.user"
            }
        }
        fullName (nullable: true, maxSize: 200)
        email nullable: true, maxSize: 200, validator: { val, obj ->
            if (!obj.id || obj.isDirty("email")) {
                if (obj.email && User.countByEmailIlike(obj.email)) return "com.rxlogix.config.Email.email.unique"
            }
        }
        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
        isBlinded nullable: false
        apiToken nullable: true, blank: true, validator: { val, obj ->
            if (!val || (obj.getId() && !obj.isDirty('apiToken'))) {
                return true
            }
            return !User.countByIdNotEqualAndApiToken(obj.id, val)
        }
        lastToLastLogin nullable: true
        lastLogin nullable: true
        isProtected nullable: true
        tenants minSize: 1
        scimId(nullable: true,unique: true)
        password nullable: true, validator: { val, obj ->
            if (obj.type.equals(UserType.NON_LDAP)) {
                if (!val) {
                    return false
                }
            }
            return true
        }
        passwordModifiedTime nullable: true

    }

    def beforeInsert() {
        lastUpdated = new Date()
        dateCreated = new Date()
    }

    def beforeUpdate() {
        def dirtyProperties = this.dirtyPropertyNames
        if (dirtyProperties.contains('lastLogin') || (dirtyProperties.contains('badPasswordAttempts') && !dirtyProperties.contains('accountLocked'))){
            return
        } else {
            lastUpdated = new Date()
        }
    }

    Set<Role> getAuthorities() {
        Set<Role> roles = []
        roles.addAll(UserRole.findAllByUser(this).collect { it.role } as Set)
        roles.addAll(UserGroup.fetchAllAuthorityByUser(this))
        roles
    }

    static Set<ReportField> getBlindedFieldsForUser(User user) {
        Set<ReportField> result = []
        UserGroup.fetchAllUserGroupByUser(user)?.each { ug ->
            List<ReportField> blindedFields = FieldProfileFields.findAllByFieldProfileAndIsBlinded(ug.fieldProfile, true).collect { it.reportField }
            if (blindedFields) {
                result.addAll(blindedFields)
            }
        }
        result.findAll { it.sourceId }
    }

    static Set<ReportField> getProtectedFieldsForUser(User user) {
        Set<ReportField> result = []
        UserGroup.fetchAllUserGroupByUser(user)?.each { ug ->
            List<ReportField> protectedFields = FieldProfileFields.findAllByFieldProfileAndIsProtected(ug.fieldProfile, true).collect { it.reportField }
            if (protectedFields) {
                result.addAll(protectedFields)
            }
        }
        result.findAll { it.sourceId }
    }

    static Set<ReportField> getProtectedFieldsForUserForPVS(User user) {
        Set<ReportField> result = []
        UserGroup.fetchAllUserGroupByUser(user)?.each { ug ->
            List<ReportField> protectedFields = FieldProfileFields.findAllByFieldProfileAndIsProtected(ug.fieldProfile, true).collect { it.reportField }
            List<ReportField> hiddenFields = FieldProfileFields.findAllByFieldProfileAndIsHidden(ug.fieldProfile, true).collect { it.reportField }
            if (protectedFields) {
                result.addAll(protectedFields)
            }
            if (hiddenFields) {
                result.addAll(hiddenFields)
            }
        }
        result.findAll { it.sourceId }
    }

    static boolean isDev() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_DEV")
    }

    boolean hasRole(String role) {
        return SpringSecurityUtils.ifAnyGranted(role)
    }

    boolean isAdmin() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }

    //Used in PVS to check whether the passed user is admin/super admin or not. Cannot use above method as the passed user is not logged in.
    boolean isAnyAdmin() {
        authorities?.authority?.any { it == 'ROLE_DEV' || it == 'ROLE_ADMIN' }
    }

    boolean isICSRAdmin() {
        authorities?.authority?.any { it == 'ROLE_DEV' || it == 'ROLE_ADMIN' || it == 'ROLE_ICSR_DISTRIBUTION_ADMIN'}
    }

    boolean hasICSRActionRoles() {
        authorities?.authority?.any { it == 'ROLE_DEV' || it == 'ROLE_ADMIN' || it == 'ROLE_ICSR_DISTRIBUTION_ADMIN' || it == 'ROLE_ICSR_DISTRIBUTION' }
    }

    boolean hasICSRAccess() {
        authorities?.authority?.any { it == 'ROLE_DEV' || it == 'ROLE_ADMIN' || it == 'ROLE_ICSR_DISTRIBUTION_ADMIN' || it == 'ROLE_ICSR_DISTRIBUTION' || it == 'ROLE_ICSR_PROFILE_EDITOR' || it == 'ROLE_ICSR_PROFILE_VIEWER' }
    }

    boolean isConfigurationTemplateCreator() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_CONFIG_TMPLT_CREATOR")
    }

    //purposely not a getter
    String checkFullName() {
        return this.fullName?: customMessageService.getMessage("user.notInLDAP.error")
    }

    //purposely not a getter
    String checkEmail() {
        return this.email?: customMessageService.getMessage("user.notInLDAP.error")
    }

    String getFullNameAndUserName() {
        return getFullName() + " (" + username + ")"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        def params = RequestContextHolder?.requestAttributes?.params?:[:]
        if (newValues && (oldValues==null) && params?.containsKey("_ROLE_ADMIN")) {
            newValues.put("roles", rolesToString(newRoles(params)))
        }
        if (newValues && oldValues && params?.containsKey("apiToken")) {
            List<String> newRoles = newRoles(params)
            List<String> oldRoles = oldRoles()
            if (((newRoles - oldRoles) + (oldRoles - newRoles)).size() > 0) {
                newValues.put("roles", rolesToString(newRoles))
                oldValues.put("roles", rolesToString(oldRoles))
            }
        }
        if (preference?.isDirty() || preference?.actionItemEmail?.isDirty() || preference?.reportRequestEmail?.isDirty() || preference?.pvcEmail?.isDirty() || preference?.pvqEmail?.isDirty()) {
            newValues.put("preference", "updated") //we need some dummy not empty value to save root (User) entity. it is hidden on UI
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    private List<Role> newRoles( Map params) {
        List<Role> roles = []

        User.withNewSession {
            for (String key in params.keySet()) {
                if (Role.findByAuthority(key) && 'on' == params.get(key)) {
                    roles << Role.findByAuthority(key)
                }
            }
        }
        return roles?.findAll { it }?.collect { it.authority }
    }

    private List<String> oldRoles() {
        User.withNewSession {
            return UserRole.findAllByUser(this)?.collect { it.role }?.findAll { it }?.collect { it.authority }
        }
    }

    String rolesToString(List roles) {
        roles?.findAll { it }?.collect { ViewHelper.getMessage("app.role.${it}") }?.sort()?.join("; ") ?: ""
    }

    def getInstanceIdentifierForAuditLog() {
        return getFullNameAndUserName()
    }

    String getNotificationChannel() {
        def notificationMappingUrl = Holders.config?.grails?.plugin?.springwebsocket?.notificationChanelPrefix
        if (!notificationMappingUrl) {
            log.error("Couldn't get notification channel mapping for user : ${username} , AMConfig: ${Holders.config?.spring?.websocket}")
        }
        return ((notificationMappingUrl ?: "") + username.encodeAsMD5())
    }

    def getReportRequestorKey() {
        "${Constants.USER_TOKEN}${this.id}"
    }

    def getReportRequestorValue() {
        this.fullName ?:this.username
    }

    static List findAllReportRequestor() {
        List reportRequestors = []
        reportRequestors.addAll(User.list())
        reportRequestors.addAll(UserGroup.findAllByIsDeleted(false))
        reportRequestors
    }

    String toString(){
        return getFullNameAndUserName()
    }

    Set<Long> getUserTeamIds() {
        List<UserGroup> userGroupList = UserGroupUser.findAllByUserAndManager(this, true)?.collect { it.userGroup }?.findAll { !it.isDeleted }
        Set<Long> team = []
        if(userGroupList){
            UserGroupUser.findAllByUserGroupInList(userGroupList)?.collect { it.user }?.each { team << it.id }
        }
        if (!team) team << this.id
        return team
    }

    static List<Long> getDateRangeTypesForUser(User user){
        Set<Long> dateRangeTypeSet = []
        List<UserGroup> ugList = UserGroup.fetchAllUserGroupByUser(user)
        ugList.each{ug ->
            ug.dateRangeTypes.each{
                dateRangeTypeSet.add(it.id)
            }
        }
        dateRangeTypeSet.toList()
    }

    static namedQueries = {
        getAllByFullName { String search ->
            ilike('fullName', '%'+search+'%')
        }
    }

    Map toUserMap() {
        [
                username: username,
                enabled: enabled,
                accountExpired: accountExpired,
                accountLocked: accountLocked,
                passwordExpired : passwordExpired,
                fullName: fullName,
                isBlinded: isBlinded,
                isProtected: isProtected,
                email: email,
                tenants : tenants,
                type : type,
                authType : authType

        ]
    }

    static List<User> getAllSelectedUsers(selectedUsers){
        return User.createCriteria().list {
            or{
                selectedUsers.collect{it.toLong()}.collate(999).each{
                    'in'('id', it)
                }
            }
        }
    }

}
