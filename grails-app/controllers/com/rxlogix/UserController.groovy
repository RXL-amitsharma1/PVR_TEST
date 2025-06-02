package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.TransferTypeEnum
import com.rxlogix.enums.UserType
import com.rxlogix.helper.LocaleHelper
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.*
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.apache.http.HttpStatus

import java.text.MessageFormat

import static org.springframework.http.HttpStatus.*

@Secured(['ROLE_USER_MANAGER'])
class UserController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE", listRefresh: "POST", transferOwnershipOfAssets: "POST"]

    def CRUDService
    def ownershipService
    def searchService
    def userService
    def ldapService
    def passwordService
    def emailService

    def index() {
        redirect action: "search"
    }

    def show(User userInstance) {
        if (!userInstance) {
            notFound()
            return
        }
        TransferItemsDTO owned = new TransferItemsDTO(
                configurations: Configuration.findAllByIsDeletedAndOwner(false, userInstance, [sort: "reportName"]),
                periodicConfigurations: PeriodicReportConfiguration.findAllByIsDeletedAndOwner(false, userInstance, [sort: "reportName"]),
                executedConfigurations: ExecutedConfiguration.executeQuery("select distinct c.id, c.reportName from ExecutedConfiguration c where c.isDeleted=false and c.owner=:previous and c not in " +
                        "(select executedConfiguration from ExecutedReportUserState where user=:previous and isDeleted=true) order by c.reportName", [previous: userInstance]).collect {
                    [id: it[0], reportName: it[1]]
                },
                executedPeriodicConfigurations: ExecutedPeriodicReportConfiguration.executeQuery("select distinct c.id, c.reportName from ExecutedPeriodicReportConfiguration c where c.isDeleted=false and c.owner=:previous and c not in " +
                        "(select executedConfiguration from ExecutedReportUserState where user=:previous and isDeleted=true) order by c.reportName ", [previous: userInstance]).collect {
                    [id: it[0], reportName: it[1]]
                },
                caseSeries: CaseSeries.findAllByIsDeletedAndOwner(false, userInstance, [sort: "seriesName"]),
                executedCaseSeries: ExecutedCaseSeries.executeQuery("select distinct c.id, c.seriesName from ExecutedCaseSeries c where c.isDeleted=false and c.isTemporary=false and c.owner=:previous and c not in " +
                        "(select executedCaseSeries from ExecutedCaseSeriesUserState where user=:previous and isDeleted=true) order by c.seriesName", [previous: userInstance]).collect {
                    [id: it[0], seriesName: it[1]]
                },
                queries: SuperQuery.findAllByOwnerAndIsDeletedAndOriginalQueryId(userInstance, false, 0L, [sort: 'name']),
                templates: ReportTemplate.findAllByOwnerAndIsDeletedAndOriginalTemplateId(userInstance, false, 0L, [sort: 'name']),
                actionItems: ActionItem.findAllByCreatedByAndIsDeleted(userInstance.username, false),
                reportRequests: ReportRequest.findAllByOwnerAndIsDeleted(userInstance, false))

        TransferItemsDTO shared = new TransferItemsDTO(
                configurations: Configuration.executeQuery("select distinct c.id, c.reportName from Configuration c join c.deliveryOption.sharedWith sw where c.isDeleted=false and sw=:previous order by c.reportName", [previous: userInstance]).collect {
                    [id: it[0], reportName: it[1]]
                },
                periodicConfigurations: Configuration.executeQuery("select distinct c.id, c.reportName from PeriodicReportConfiguration c join c.deliveryOption.sharedWith sw where c.isDeleted=false and sw=:previous order by c.reportName", [previous: userInstance]).collect {
                    [id: it[0], reportName: it[1]]
                },
                executedConfigurations: ExecutedConfiguration.executeQuery("select distinct c.id, c.reportName from ExecutedConfiguration c join c.executedDeliveryOption.sharedWith sw where c.isDeleted=false and sw=:previous and c not in " +
                        "(select executedConfiguration from ExecutedReportUserState where user=:previous and isDeleted=true) order by c.reportName", [previous: userInstance]).collect {
                    [id: it[0], reportName: it[1]]
                },
                executedPeriodicConfigurations: ExecutedPeriodicReportConfiguration.executeQuery("select distinct c.id, c.reportName from ExecutedPeriodicReportConfiguration c join c.executedDeliveryOption.sharedWith sw where c.isDeleted=false and sw=:previous and c not in " +
                        "(select executedConfiguration from ExecutedReportUserState where user=:previous and isDeleted=true) order by c.reportName ", [previous: userInstance]).collect {
                    [id: it[0], reportName: it[1]]
                },
                caseSeries: CaseSeries.executeQuery("select distinct c.id, c.seriesName from CaseSeries c join c.deliveryOption.sharedWith sw where c.isDeleted=false and sw=:previous order by c.seriesName", [previous: userInstance]).collect {
                    [id: it[0], seriesName: it[1]]
                },
                executedCaseSeries: ExecutedCaseSeries.executeQuery("select distinct c.id, c.seriesName from ExecutedCaseSeries c join c.executedDeliveryOption.sharedWith sw where c.isDeleted=false and c.isTemporary=false and sw=:previous and c not in " +
                        "(select executedCaseSeries from ExecutedCaseSeriesUserState where user=:previous and isDeleted=true) order by c.seriesName", [previous: userInstance]).collect {
                    [id: it[0], seriesName: it[1]]
                },

                queries: SuperQuery.executeQuery("select distinct c.id, c.name from SuperQuery c join c.userQueries u where c.isDeleted=false and c.originalQueryId=0 and  u.user=:previous order by c.name", [previous: userInstance]).collect {
                    [id: it[0], name: it[1]]
                },
                templates: ReportTemplate.executeQuery("select distinct c.id, c.name from ReportTemplate c join c.userTemplates u where c.isDeleted=false and c.originalTemplateId=0 and u.user=:previous order by c.name", [previous: userInstance]).collect {
                    [id: it[0], name: it[1]]
                },
                actionItems: ActionItem.findAllByIsDeletedAndAssignedTo(false, userInstance, [sort: 'description']),
                reportRequests: ReportRequest.findAllByIsDeletedAndAssignedTo(false, userInstance, [sort: 'description']),
                requestedReportRequests: ReportRequest.executeQuery("FROM ReportRequest as r WHERE :user in elements(r.requesters) order by r.description", [user: userInstance])
        )
        List<String> role = UserRole.createCriteria().list {
            projections {
                'role' {
                    property("authority")
                }
            }
            eq('user', userInstance)
        }
        role = role.collect { message(code: "app.role.${it}", args: []) }.sort { it }
        List<String> usergroup = UserGroup.fetchAllUserGroupByUser(userInstance)?.name.sort { it }
        render view: "show", model: [userInstance: userInstance, owned: owned, shared: shared, roles: role, usergroups: usergroup]
    }

    def create() {
        def command = new UserCommand()
        render view: "create", model: [userInstance: command]
    }

    def save(UserCommand command) {
        def userInstance = new User(command.properties)
        //pull down the fullName and email from LDAP server
        String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
        def   ldapEntry = ldapService.getLdapEntry("$uid=$userInstance.username")
        if(userInstance.type == UserType.LDAP) {
            userInstance.fullName = ldapEntry[0]?.getFullName()
            userInstance.email = ldapEntry[0]?.getEmail()
        }
        else{
            if (ldapEntry) {
                flash.error = message(code: 'com.rxlogix.user.User.name.unique.per.user') as Object
                render view: 'create', model: [userInstance: command]
                return
            }
            String newPassword = passwordService.generateDefaultPassword()
            userService.changePassword(userInstance, newPassword)
           emailService.sendPasswordChangeEmail(userInstance.username, newPassword, [userInstance.email])
        }
        userInstance.preference.actionItemEmail = AIEmailPreference.getDefaultValues(userInstance.preference)
        userInstance.preference.reportRequestEmail = ReportRequestEmailPreference.getDefaultValues(userInstance.preference)
        userInstance.preference.pvcEmail = PVCEmailPreference.getDefaultValues(userInstance.preference)
        userInstance.preference.pvqEmail = PVQEmailPreference.getDefaultValues(userInstance.preference)


        //Bind Role checkboxes to command object (used in validation round trips only)
        for (String key in params.keySet()) {
            if (Role.findByAuthority(key) && 'on' == params.get(key)) {
                command.roles.add(key)
            }
        }
        try {
            userInstance = (User) CRUDService.save(userInstance)
            addRoles userInstance
        } catch (ValidationException ve) {
            command.errors = ve.errors
            render view: 'create', model: [userInstance: command]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), userInstance.username])
                redirect userInstance
            }
            '*' { respond userInstance, [status: CREATED] }
        }
    }

    def edit(User userInstance) {
        if (!userInstance) {
            notFound()
            return
        }
        render view: 'edit', model: buildUserModel(userInstance)
    }

    def update(Long id) {
        User userInstance = User.get(id)
        if (userInstance == null) {
            notFound()
            return
        }
        params.put('existingTenants', userInstance.tenants.collect())
        userInstance.tenants.clear()
        bindData(userInstance, params, ['tenants'])
        params.tenantsValueString?.split(Constants.MULTIPLE_AJAX_SEPARATOR)?.each {
            if (it) {
                userInstance.addToTenants(Tenant.load(it.toLong()))
            }
        }
        try {
            def dirtyProperties = userInstance.dirtyPropertyNames
            if (dirtyProperties.contains("accountLocked")) {
                //Account is unlocked; reset bad password attempts
                if (!userInstance.accountLocked) {
                    userInstance.badPasswordAttempts = 0
                }
            }

            if(!params.containsKey('enabled')) userInstance.enabled = false
            if(!params.containsKey('accountLocked')) userInstance.accountLocked = false
            if(!params.containsKey('accountExpired')) userInstance.accountExpired = false


            userInstance = (User) CRUDService.update(userInstance)

            UserRole.removeAll userInstance
            addRoles userInstance

        } catch (ValidationException ve) {
            render view: 'edit', model: buildUserModel(userInstance)
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label'), userInstance.username])
                redirect userInstance
            }
            '*' { respond userInstance, [status: OK] }
        }
    }

    def delete(User userInstance) {

        if (userInstance == null) {
            notFound()
            return
        }

        try {
            CRUDService.delete(userInstance)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label'), userInstance.username])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'user.label'), userInstance.username])
                    redirect(action: "show", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }

    }

    /**
     * Display Search form.
     * @return
     */
    def search() {
        boolean hasConfigTemplateCreatorRole = userService.getUser().isConfigurationTemplateCreator()
        render (view: "search", model: [hasConfigTemplateCreatorRole: hasConfigTemplateCreatorRole])
    }

    /**
     * Get the search results.
     * @return
     */
    def listRefresh() {
        def userInstanceList = []
        def userInstanceTotal = 0
        (userInstanceList, userInstanceTotal) = searchService.getUserList(params)

        render(template: "userSearchResultsTable",
                model: [userInstanceList : userInstanceList,
                        userInstanceTotal: userInstanceTotal]
        )
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    protected void addRoles(User userInstance) {
        for (String key in params.keySet()) {
            if (Role.findByAuthority(key) && 'on' == params.get(key)) {
                UserRole.create userInstance, Role.findByAuthority(key), true
            }
        }
    }

    protected List sortedRoles() {
        Role.list().sort { it.authority }
    }

    protected Map buildUserModel(userInstance) {

        String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
        String authoritiesPropertyName = SpringSecurityUtils.securityConfig.userLookup.authoritiesPropertyName

        List roles = sortedRoles()
        Set userRoleNames = UserRole.createCriteria().list {
            projections {
                'role' {
                    property("authority")
                }
            }
            eq('user', userInstance)
        }

        def granted = [:]
        def notGranted = [:]
        for (role in roles) {
            String authority = role[authorityFieldName]
            if (userRoleNames.contains(authority)) {
                granted[(role)] = userRoleNames.contains(authority)
            } else {
                notGranted[(role)] = userRoleNames.contains(authority)
            }
        }

        return [userInstance: userInstance, roleMap: granted + notGranted, currentLocale: LocaleHelper.convertLocaleToMap(getSessionLocale()), theUserTimezone: userInstance?.preference?.timeZone]
    }

    /**
     * Ajax call used by autocomplete textfield.
     */
    def ajaxLdapSearch = {
        if(request.xhr){
            def jsonData = []
            String usersSearchFilter = grailsApplication.config.grails.plugin.springsecurity.ldap.users.search.filter
            if (params.term?.length() > 2 && MiscUtil.isValidPattern(params.term)) {
                def results = []
                results.addAll(ldapService.searchLdapToAddUser(MessageFormat.format(usersSearchFilter, [params.term].toArray())))
                results = results as Set

                for (List ldapResult in results) {
                    for (Map resultMap : ldapResult) {
                        resultMap.each { k, v ->
                            jsonData << [id: k, text: "${v}"]
                        }
                    }
                }
            }

            render text: jsonData as JSON, contentType: 'text/plain'
        } else {
            response.sendError 404
        }
    }

    /**
     * Ajax call used by autocomplete textfield.
     */
    def ajaxUserSearch = {
        def jsonData = []

        def users = searchService.ajaxSearchUser(params.term)

        for (User user : users) {
            jsonData << [id: user.id, text: "${user?.username + " - " + user?.fullName + " - " + user?.email}"]
        }

        render text: jsonData as JSON, contentType: 'text/plain'
    }

    /**
     * Ajax call list all users
     * @return
     */
    def listUsers() {
        def users = userService.findAllUsersHavingFullName().collect {
            [id: it.id, fullName: it.fullName, username: it.username]
        }

        render text: users as JSON, contentType: 'application/json', status: HttpStatus.SC_OK
    }

    @Secured(['ROLE_ADMIN'])
    def transferOwnershipOfAssets() {
        def transferCriteria = [:]
        transferCriteria.previousOwner = User.get(params.previousOwner)

        transferCriteria.transferType = params.transferTypeValue as TransferTypeEnum
        if (transferCriteria.transferType == TransferTypeEnum.OWNERSHIP) {
            transferCriteria.newUser = User.get(params.newOwnerValue)
        } else {
            if (params.sharedWithValue && params.sharedWithValue.startsWith(Constants.USER_GROUP_TOKEN))
                transferCriteria.newUserGroup = UserGroup.get(Long.valueOf(params.sharedWithValue.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            if (params.sharedWithValue && params.sharedWithValue.startsWith(Constants.USER_TOKEN)) {
                transferCriteria.newUser = User.get(Long.valueOf(params.sharedWithValue.replaceAll(Constants.USER_TOKEN, '')))
            }
        }

        transferCriteria.templates = params.list("template").collect({ it as Long })
        transferCriteria.queries = params.list("query").collect({ it as Long })
        transferCriteria.configurations = params.list("configuration").collect({ it as Long })
        transferCriteria.periodicConfigurations = params.list("periodicConfiguration").collect({ it as Long })
        transferCriteria.executedConfigurations = params.list("executedConfiguration").collect({ it as Long })
        transferCriteria.executedPeriodicConfigurations = params.list("executedPeriodicConfiguration").collect({
            it as Long
        })
        transferCriteria.caseSeries = params.list("caseSeries").collect({ it as Long })
        transferCriteria.executedCaseSeries = params.list("executedCaseSeries").collect({ it as Long })
        transferCriteria.actionItems = params.list("actionItem").collect({ it as Long })
        transferCriteria.reportRequests = params.list("reportRequest").collect({ it as Long })
        transferCriteria.requesterRequests = params.list("requesterRequest").collect({ it as Long })
        try {
            ownershipService.updateOwners(transferCriteria);
        } catch (ValidationException ex) {

            List<String> errMsg = []
            ex.errors?.allErrors?.each {
                errMsg.add(message(code: it.codes?.last()))
            }

            flash.error = message(code: 'default.ownershipchange.failed.message',
                    args: [errMsg ? errMsg.toString() : ex.message])
            redirect(action: "show", id: transferCriteria.previousOwner.id)
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.ownershipchange.message')
                redirect(action: "show", id: transferCriteria.previousOwner.id)
            }
            '*' { respond transferCriteria.newUser, [status: CREATED] }
        }
    }

    def availableTenants(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        List<Tenant> items = Tenant.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'name']) {
            eq('active', true)
            if (term) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }

        render([items : items.collect {
            [id: it.id, text: it.displayName]
        }, total_count: items.totalCount] as JSON)
    }

    private Locale getSessionLocale() {
        return session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' ?: org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
    }
}

class UserCommand {
    def userService

    String username
    String fullName
    String email
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Preference preference = new Preference()
    List roles = []
    String tenantsValueString
    String apiToken
    UserType type = UserType.LDAP
    Integer badPasswordAttempts = 0
    Date passwordModifiedTime

    static constraints = {
        importFrom User, exclude: ["password", "email", "fullName", "username"]
        preference validator: { val, obj ->
            String username = obj.userService.getUser()?.username
            obj.preference.createdBy = username
            obj.preference.modifiedBy = username
            return true
        }
        username blank: false, maxSize: 255, validator: { val, obj ->
            if (User.findByUsernameIlike(val)) {
                return "com.rxlogix.UserCommand.username.unique"
            }
        }
        email nullable: true, maxSize: 255, blank: true, email: true, validator: { val, obj ->
            if (obj.type.equals(UserType.NON_LDAP)) {
                if (!val) {
                    return "com.rxlogix.UserCommand.email.unique"
                }
            }
        }
        fullName nullable: true, maxSize: 255, blank: true, validator: { val, obj ->
            if (obj.type.equals(UserType.NON_LDAP)) {
                if (!val) {
                    return "com.rxlogix.UserCommand.fullName.nullable"
                }
            }
        }
        userService nullable: true
    }

    List<Tenant> getTenants() {
        return this.tenantsValueString?.split(Constants.MULTIPLE_AJAX_SEPARATOR)?.findAll { it }?.collect { Tenant.load(it.toLong()) }?:[]
    }

    public getId(){
        return null
    }
}


class TransferItemsDTO {
    List<Configuration> configurations
    List<PeriodicReportConfiguration> periodicConfigurations
    List<ExecutedConfiguration> executedConfigurations
    List<ExecutedPeriodicReportConfiguration> executedPeriodicConfigurations
    List<CaseSeries> caseSeries
    List<ExecutedCaseSeries> executedCaseSeries
    List<SuperQuery> queries
    List<ReportTemplate> templates
    List<ActionItem> actionItems
    List<ReportRequest> reportRequests
    List<ReportRequest> requestedReportRequests
}
