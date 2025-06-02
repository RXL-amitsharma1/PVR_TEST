package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.Dashboard
import com.rxlogix.config.DateRangeType
import com.rxlogix.config.SourceProfile
import com.rxlogix.enums.DashboardEnum
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import java.sql.SQLException

import static org.springframework.http.HttpStatus.*

@Secured(['ROLE_USER_MANAGER'])
class UserGroupController implements SanitizePaginationAttributes{

    def CRUDService
    def userService
    def reportFieldService
    def utilService
    def signalIntegrationService
    GrailsApplication grailsApplication

    static allowedMethods = [save:'POST', update:['PUT','POST'], delete: ['DELETE','POST']]

    def index() { }

    /**
     * This action is responsible got rendering the create action.
     * @return
     */
    def create() {
        UserGroup defaultRRAssignedTO = UserGroup.getDefaultReportRequestAssignedTo()
        [userGroupInstance           : new UserGroup(), roleList: sortedRoles(), userGroupRoleList: [], userGroupUserList: [],
         allUserList                 : userService.findAllUsersHavingFullName(), dashboardList: [], managers: [], allEnabledUserList: [],
         availableDashboards         : getAvailableDashboards(), sourceProfileList: SourceProfile.sortedSourceProfiles(),
         canUpdateDefaultRRAssignedTo: (defaultRRAssignedTO == null)]
    }

    List getAvailableDashboards() {
        Dashboard.findAllByDashboardTypeInListAndIsDeleted([DashboardEnum.PVR_PUBLIC, DashboardEnum.PVQ_PUBLIC, DashboardEnum.PVC_PUBLIC], false)
                .collect { [id: it.id, name: ((it.label ?: "-") + "(" + ViewHelper.getMessage(it.dashboardType.i18nKey)) + ")"] }
    }
    /**
     * This action is responsible to show the user group.
     * @return
     */
    def show(UserGroup userGroup) {
        if (!userGroup) {
            notFound()
            return
        }
        List<String> userGroupAuthority = userGroup.authorities.authority
        userGroupAuthority = userGroupAuthority.collect {
            message(code: "app.role.${it}", args: [])
        }.sort { it }
        List<String> userNames = userService.getAllEnabledUsers(userGroup).collect { it.username }
        List<String> dashboardNames = Dashboard.selectPublicByGroupId(userGroup.id).list()*.label.sort { it }
        render view: "show", model: [userGroupAuthority: userGroupAuthority, userGroupUser: userNames, userGroupInstance: userGroup, dashboardList: dashboardNames]
    }

    /**
     * This action is responsible to edit the user group.
     * @return
     */
    def edit(UserGroup userGroup) {
        if (!userGroup) {
            notFound()
            return
        }
        UserGroup defaultRRAssignedTO = UserGroup.getDefaultReportRequestAssignedTo()
        def userGroups = UserGroupUser.findAllByUserGroup(userGroup)
        Map map = buildUserModel(userGroup)
        map.put('userGroupRoleList', UserGroupRole.findAllByUserGroup(userGroup)*.role)
        map.put('userGroupUserList', userGroups*.user)
        map.put('managers', userGroups?.collect { (it.manager ? it.user.id : null) }?.findAll())
        map.put('allUserList', userService.findAllUsersHavingFullName())
        map.put('allEnabledUserList',userService.getAllEnabledUsers(userGroup))
        map.put('dashboardList', Dashboard.selectPublicByGroupId(userGroup.id).list())
        map.put('availableDashboards', getAvailableDashboards())
        map.put('sourceProfileList', SourceProfile.sortedSourceProfiles())
        map.put('canUpdateDefaultRRAssignedTo', ((defaultRRAssignedTO == null) || (defaultRRAssignedTO == userGroup)))
        render view: 'edit', model: map
    }

    def ajaxFetchUsersByGroup(Long id){
        if (id == null) { // create page
            render status: 200
        }
        UserGroup userGroup = UserGroup.get(id);
        if (userGroup == null){
            log.error("User Group not found for id: ${id}, during UserGroupController -> ajaxFetchUsersByGroup");
            render status: NOT_FOUND
        }
        sanitize(params)

       int recordsFilteredCount = UserGroupUser.countAllByUserGroupAndSearchString(userGroup, params.searchString).get() ?: 0
       List records = recordsFilteredCount ? UserGroupUser.fetchAllByUserGroupAndSearchString(userGroup, params.searchString).list([max: params.max, offset: params.offset, sort: "user.fullName", order: params.direction])
                .collect{[id: it[0].id, fullName: it[0].fullName, manager: it[2]]} : []
       int recordsTotal = UserGroupUser.countAllByUserGroupAndSearchString(userGroup).get() ?: 0

        render([aaData: records, recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }
    /**
     * This action is responsible to delete the user group.
     * @return
     */
    @Transactional
    def delete(UserGroup userGroupInstance) {
        if (!userGroupInstance) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (!currentUser.isAdmin()) {
            flash.warn = message(code: "app.userGroup.delete.permission", args: [userGroupInstance.name])
            redirect(view: "index")
            return
        }
        try {
            UserGroupUser.removeAll(userGroupInstance)
            CRUDService.softDelete(userGroupInstance, userGroupInstance.name, params.deleteJustification)
            userService.updateBlindedFlagForUsersAndGroups()
            userService.updateProtectedFlagForUsersAndGroups()
            clearCacheForQueryReportField(userGroupInstance)
            if (grailsApplication.config.pvsignal.url)
                signalIntegrationService.updateBlindedDataToSignal(userGroupInstance)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.userGroup'), userGroupInstance.name])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.userGroup'), userGroupInstance.name])
                    redirect(action: "show", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }
    }


    /**
     * This action is responsible to save the user group.
     * @return
     */
    def save() {
        UserGroup userGroup = new UserGroup()
        if (!params.list('sourceProfiles')) {
            flash.error = "${message(code: "com.rxlogix.config.UserGroup.sourceProfiles.nullable")}"
            render view: "create", model: createModelFromParams(userGroup)
            return
        }
        populateModel(userGroup)
        try{
            CRUDService.save(userGroup)
            populateAuthority(userGroup)
            populateUsers(userGroup)
            userService.updateBlindedFlagForUsersAndGroups()
            userService.updateProtectedFlagForUsersAndGroups()
            userGroup.refresh()
            populateDashboards(userGroup)
            clearCacheForQueryReportField(userGroup)
            if (grailsApplication.config.pvsignal.url)
                signalIntegrationService.updateBlindedDataToSignal(userGroup)
            session.team = null
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'userGroup.label', default: 'User Group'), userGroup?.name])}"
            redirect(action: "index")
        } catch (ValidationException ve) {
            userGroup.errors=ve.errors
            render view: "create", model: createModelFromParams(userGroup)
            return;
        } catch (Exception ex) {
            flash.error = "${message(code: 'app.label.userGroup.save.exception')}"
            render view: "create", model: createModelFromParams(userGroup)
        }
    }

    /**
     * This action is to update the user group.
     * @return
     */
    def update(UserGroup userGroup) {
        if (!userGroup) {
            notFound()
            return
        }
        Set<User> oldUsers = userGroup.getUsers()
        FieldProfile fieldProfile = FieldProfile.findById(params.previousFieldId)
        if (!params.list('sourceProfiles')) {
            flash.error = "${message(code: "com.rxlogix.config.UserGroup.sourceProfiles.nullable", default:"Atleast one Data Source must be selected")}"
            render view: "edit", model: createModelFromParams(userGroup)
            return
        }
        //Bind the user group instance.
        populateModel(userGroup)
        try {
            CRUDService.update(userGroup)
            populateAuthority(userGroup, true)
            populateUsers(userGroup, true)
            userService.updateBlindedFlagForUsersAndGroups()
            userService.updateProtectedFlagForUsersAndGroups()
            userGroup.refresh()
            populateDashboards(userGroup, true)
            Set<User> newUsers = userGroup.getUsers()
            boolean isFieldProfileUpdated = false
            if(userGroup?.fieldProfile != fieldProfile) {
                isFieldProfileUpdated = true
                log.info("Old field profile: ${fieldProfile?.id} and new field profile ${userGroup?.fieldProfile?.id} are different, clearing cache")
                clearCacheForQueryReportField(userGroup)
            }

            if (isFieldProfileUpdated || newUsers != oldUsers) {
                if (grailsApplication.config.pvsignal.url) {
                    signalIntegrationService.updateBlindedDataToSignal(userGroup)
                }
            }
            session.team = null
            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'User Group'), userGroup?.name])}"
            redirect(action: "index")
        } catch (ValidationException ve) {
            userGroup.errors=ve.errors
            render view: "edit", model: createModelFromParams(userGroup)
        } catch (Exception ex) {
            ex.printStackTrace()
            flash.error = "${message(code: 'app.label.userGroup.save.exception')}"
            render view: "edit", model: createModelFromParams(userGroup)
        }
    }

    private notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'userGroup.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private Map createModelFromParams(UserGroup userGroup) {
        def userGroupRoleList = []
        Map roleMap = [:]
        for (String key in params.keySet()) {
            Role role = Role.findByAuthority(key)
            if (role && ('on' == params.get(key))) {
                userGroupRoleList << role.authority
            }
        }

        if (params.action == 'update') {
            roleMap = buildUserModel(userGroup).roleMap
        }

        def dashboardList = params.list('dashboardId') ? Dashboard.findAllByIdInList(params.list('dashboardId')) : []
        return [userGroupInstance  : userGroup, roleList: sortedRoles(),
                userGroupRoleList  : userGroupRoleList, sourceProfileList: SourceProfile.sortedSourceProfiles(),
                userGroupUserList  : params.list('selectedUsers') ? User.getAllSelectedUsers(params.list('selectedUsers')) : [],
                managers           : params.findAll { k, v -> (k.toString().startsWith(Constants.GROUP_MANAGER) && v == "on") }.collect { k, v -> k.split("_")[1] as Long },
                availableDashboards: getAvailableDashboards(),
                dashboardList      : dashboardList,
                roleMap            : roleMap,
                allUserList                 : userService.findAllUsersHavingFullName()
        ]
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private populateModel(UserGroup userGroup) {
        //Do not bind in any other way because of the clone contained in the params
        checkForNull(userGroup, params)
        bindData(userGroup, params, [exclude: ['sourceProfiles', 'dateRangeTypes']])
        bindSourceProfile(userGroup, params.list('sourceProfiles'))
        bindDateRangeTypes(userGroup, params.list('dateRangeTypes'))
    }

    private void checkForNull(UserGroup userGroup, Map params) {
        if (!params.fieldProfile) userGroup.fieldProfile = null
        if (!params.dataProtectionQuery) userGroup.dataProtectionQuery = null
        if (!params.defaultRRAssignTo) userGroup.defaultRRAssignTo = false
    }

    private void bindSourceProfile(UserGroup userGroup, List sourceProfiles) {
        if (sourceProfiles) {
            userGroup.sourceProfiles?.clear()
            sourceProfiles.each { id ->
                userGroup.addToSourceProfiles(SourceProfile.findById(Long.valueOf(id)))
            }
        }
    }

    private void bindDateRangeTypes(UserGroup userGroup, List dateRangeTypes){
        if(dateRangeTypes){
            userGroup.dateRangeTypes.clear()
            dateRangeTypes.each{id ->
                userGroup.addToDateRangeTypes(DateRangeType.findById(Long.valueOf(id)))
            }
        }
        else{
            userGroup.dateRangeTypes.clear()
        }
    }

    private populateAuthority(UserGroup userGroup, Boolean isUpdate=false) {
        if(isUpdate) {
            UserGroupRole.removeAll(userGroup, true)
        }
        //Bind Role (used in validation round trips only)
        for (String key in params.keySet()) {
            Role role = Role.findByAuthority(key)
            if (role && ('on' == params.get(key))) {
                 UserGroupRole.create(userGroup, role, true)
            }
        }
    }

    @Transactional
    private populateUsers(UserGroup userGroup, Boolean isUpdate=false) {
        List<Long> managers = params.findAll { k, v -> (k.toString().startsWith(Constants.GROUP_MANAGER) && v == "on") }.collect { k, v -> k.split("_")[1] as Long }
        Long userGroupId = userGroup.getId()
        List<Long> selectedUserIds = params.list('selectedUsers').collect { it.toLong() }
        Sql pvrSql = new Sql(utilService.getReportConnectionForPVR())
        try {
            if (isUpdate) {
                List<Long> newUserIds = []
                List<Long> toBeRemoved = []
                if (selectedUserIds) {
                    for (Long selectedUserId : selectedUserIds) {
                        if (UserGroupUser.exists(userGroupId, selectedUserId)) { // check if user exists
                            // check manager value and add in toBeRemoved
                            if ((UserGroupUser.isManager(userGroupId, selectedUserId)) != managers.contains(selectedUserId)) {
                                toBeRemoved.add(selectedUserId)
                                newUserIds.add(selectedUserId)
                            }
                        } else {
                            // doesn't exist, add new user
                            newUserIds.add(selectedUserId)
                        }
                    }
                }

                // User existing previously and now removed
                List<Long> removedIds = []
                String userRemoveQuery = "select user_id from PVUSERGROUPS_USERS where USER_GROUP_ID = ${userGroupId}"
                def presentUsersResult = pvrSql.rows(userRemoveQuery)
                removedIds = presentUsersResult.user_id - selectedUserIds
                toBeRemoved += removedIds

                if (toBeRemoved) {
                    List<List<Long>> batches = toBeRemoved.collate(999)
                    batches.each { batch ->
                        String batchString =  batch.join(",")
                        String deleteSqlQuery = "delete from PVUSERGROUPS_USERS where USER_GROUP_ID = ${userGroupId} and USER_ID in (${batchString})"
                        pvrSql.execute(deleteSqlQuery)
                    }
                }
                // update user to be added after processing
                selectedUserIds = newUserIds;
            }
            if (selectedUserIds) {
                for (Long selectedUserId : selectedUserIds) {
                    Map params = [userId: selectedUserId, userGroupId: userGroupId, isManager: managers.contains(selectedUserId)]
                    String insertQuery = "insert into PVUSERGROUPS_USERS(USER_ID, USER_GROUP_ID, MANAGER) values(:userId, :userGroupId, :isManager)"
                    pvrSql.execute(insertQuery, params);
                }
            }

        } catch (Exception e){
            log.error("Exception during UserGroupController -  populateUsers", e)
            throw e
        } finally{
            pvrSql.close()
        }
    }

    void populateDashboards(UserGroup userGroup, Boolean isUpdate = false) {
        List<Long> oldListId = []
        if (isUpdate) {
            oldListId = Dashboard.selectPublicByGroupId(userGroup.id).list()*.id
        }

        List<Long> newListId = params.list('dashboardId').collect { it as Long }
        List<Long> toRemove = oldListId - newListId
        List<Long> toAdd = newListId - oldListId
        toRemove.each {
            Dashboard d = Dashboard.get(it)
            d.removeFromSharedWithGroup(userGroup)
            CRUDService.update(d)
        }
        toAdd.each {
            Dashboard d = Dashboard.get(it)
            d.addToSharedWithGroup(userGroup)
            CRUDService.update(d)
        }
    }

    /**
     * Ajax call used by autocomplete textfield.
     */
    def ajaxProfileSearch = {
        def jsonData = []
        if (params.term?.length() > 2) {
            String searchTerm = "%" + params.term + "%"
            List<FieldProfile> fieldProfiles = FieldProfile.findAllByNameIlikeAndIsDeleted("%${searchTerm}%", false)
            fieldProfiles.each {fieldProfile->
                jsonData << [id: fieldProfile.id, text: fieldProfile.name]
            }
        }
        render text: jsonData as JSON, contentType: 'text/plain'
    }


    protected Map buildUserModel(UserGroup userGroupInstance) {

        String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
        String authoritiesPropertyName = SpringSecurityUtils.securityConfig.userLookup.authoritiesPropertyName

        List roles = sortedRoles()
        Set userRoleNames = userGroupInstance[authoritiesPropertyName].collect { it[authorityFieldName] }
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
        return [userGroupInstance: userGroupInstance, roleMap: granted + notGranted, roleList: roles]
    }

    private List<Role> sortedRoles() {
        Role.list().sort { it.authority }
    }

    private void clearCacheForQueryReportField(UserGroup userGroup) {
        if(userGroup.fieldProfile && UserGroupUser.countByUserAndUserGroup(userService.getUser(), userGroup)) {
            log.info("Clearing cache for QUERY Report field group (For updation in user group - ${userGroup?.name} having field profile ${userGroup?.fieldProfile?.name})")
            reportFieldService.clearCacheReportFields()
        }
    }
}
