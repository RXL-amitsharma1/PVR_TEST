package com.rxlogix.api

import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.IcsrReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportRequest
import com.rxlogix.enums.AssignedToFilterEnum
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserRole
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SecurityUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import grails.util.Holders
import org.apache.http.HttpStatus

@Secured('permitAll')
class UserRestController extends RestfulController implements SanitizePaginationAttributes {

    def searchService
    def userService
    CRUDService CRUDService

    UserRestController() {
        super(User)
    }

    def keepAlive() { //Used for IdleTimeOut jQuery Plugin open for all
        render([status: 'ok'] as JSON)
    }

    def index() {
        if(!SpringSecurityUtils.ifAnyGranted("ROLE_USER_MANAGER")){
            forward controller: "errors", action: "forbidden"
        }
        sanitize(params)
        params.accountEnabled = params.enabled ?: 'yes'
        def userInstanceList = []
        def userInstanceTotal = 0
        (userInstanceList, userInstanceTotal) = searchService.getUserList(params)
        List<Map> users = userInstanceList.collect { userInstance ->
            def roles = UserRole.createCriteria().list {
                projections {
                    'role' {
                        property("authority")
                    }
                }
                eq('user', userInstance)
            }
            roles = roles.collect { message(code: "app.role.${it}", args: []) }.sort { it }.join(", ")

            [id        : userInstance.id,
             username  : userInstance.username,
             fullName  : userInstance.fullName,
             email     : userInstance.email,
             enabled   : userInstance.enabled,
             lastLogin : userInstance.lastLogin?.format(DateUtil.DATEPICKER_UTC_FORMAT),
             roles     : roles,
             userGroups: UserGroup.fetchAllUserGroupByUser(userInstance as User).name?.join(", ")]
        }
        render([aaData: users, recordsTotal: User.count(), recordsFiltered: userInstanceTotal] as JSON)
    }

    def listUsers() {
        def users = userService.findAllUsersHavingFullName().collect {
            [id: it.id, fullName: it.fullName, username: it.username]
        }
        render text: users as JSON, contentType: 'application/json', status: HttpStatus.SC_OK
    }

    def userList() {
        forSelectBox(params)
        def users = User.findAllByFullNameIlike("%" + params.term + "%", [max: params.max, offset: params.offset]).collect {
            [id: it.id, text: it.fullName]
        }
        def out = [items: users, total_count: User.count()]
        render(out as JSON)

    }

    def generateAPIToken() {
        def currentUser = userService.currentUser

        if (currentUser) {
            String token = SecurityUtil.generateAPIToken(Holders.config.getProperty('rxlogix.pvr.api.token.key'),
                    currentUser.username,
                    UUID.randomUUID().toString(),
                    new Date())
            render text: [token: token] as JSON, contentType: 'application/json', status: HttpStatus.SC_OK
        }
    }
    def sharedWithWorkflowRuleList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max
        Set<UserGroup> activeGroups = userService.getAllowedSharedWithGroupsForCurrentUser(term)
        List<User> groupList = activeGroups.unique { it.id }.collect { [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded:it.isBlinded] }.sort{
            it.text?.toLowerCase()
        }
        def items = []
        def userList = []
        splitResult(items, offset, max, groupList, userList)
        render([items: items, total_count: groupList.size()] as JSON)
    }

    def sharedWithList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max

        Set<User> activeUsers = userService.getAllowedSharedWithUsersForCurrentUser(term)
        Set<UserGroup> activeGroups = userService.getAllowedSharedWithGroupsForCurrentUser(term)
        List<User> userList = activeUsers.unique { it.id }.collect { [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded:it.isBlinded] }.sort{
            it.text?.toLowerCase()
        }
        List<User> groupList = activeGroups.unique { it.id }.collect { [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded:it.isBlinded] }.sort{
            it.text?.toLowerCase()
        }
        def items = []
        splitResult(items, offset, max, groupList, userList)
        render([items: items, total_count: userList.size() + groupList.size()] as JSON)
    }

    def sharedWithValues() {
        def result = []
        params.ids?.split(";")?.each {
            if (it.startsWith(Constants.USER_GROUP_TOKEN)) {
                UserGroup userGroup = UserGroup.get(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                result << [id: it, text: userGroup.name, blinded:userGroup.isBlinded]
            } else if (it.startsWith(Constants.USER_TOKEN)) {
                User user = User.get(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))
                result << [id: it, text: user.fullName ?: user.username, blinded:user.isBlinded]
            } else if (it.startsWith(Constants.OWNER_SELECT_VALUE)) {
                result << [id: Constants.OWNER_SELECT_VALUE, text: ViewHelper.getMessage('app.label.iAmOwner')]
            } else if (it.startsWith(Constants.SHARED_WITH_ME_SELECT_VALUE)) {
                result << [id: Constants.SHARED_WITH_ME_SELECT_VALUE, text: ViewHelper.getMessage('app.label.sharedWithMe')]
            } else if (it.startsWith(Constants.TEAM_SELECT_VALUE)) {
                result << [id: Constants.TEAM_SELECT_VALUE, text: ViewHelper.getMessage('app.label.mayTeam')]
            } else if (it.startsWith(AssignedToFilterEnum.ME.name())) {
                result << [id: AssignedToFilterEnum.ME.name(), text: ViewHelper.getMessage(AssignedToFilterEnum.ME.getI18nKey())]
            } else if (it.startsWith(AssignedToFilterEnum.MY_GROUPS.name())){
                result << [id: AssignedToFilterEnum.MY_GROUPS.name(), text: ViewHelper.getMessage(AssignedToFilterEnum.MY_GROUPS.getI18nKey())]
            }
        }
        render(result as JSON)
    }

    def getPublisherContributors() {
        forSelectBox(params)
        //todo:limit to role
        def users = User.findAllByFullNameIlike("%" + params.term + "%", [max: params.max, offset: params.offset]).collect {
            [id: it.id, text: it.fullName]
        }
        def out = [items: users, total_count: User.count()]
        render(out as JSON)
    }

    def userListValue() {
        def result = []
        params.ids?.tokenize(Constants.MULTIPLE_AJAX_SEPARATOR)?.each {
            User user = User.get(Long.valueOf(it))
            result << [id: it, text: user.fullName ?: user.username]
        }
        render(result as JSON)
    }

    def userValue() {
        User user = User.get(params.id)
        render([id: user.id, text: user.fullName] as JSON)
    }

    def ownerFilterList(String term, Integer page, Integer max, String clazz) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        } else {
            term = ""
        }
        int offset = Math.max(page - 1, 0) * max
        def result = []
        int total
        if (clazz == Configuration.name) {
            result = ReportConfiguration.fetchAllOwners(userService.user, Configuration, term).list([max: max, offset: offset])
            total = ReportConfiguration.countAllOwners(userService.user, Configuration, term).get()
        } else if (clazz == PeriodicReportConfiguration.name) {
            result = ReportConfiguration.fetchAllOwners(userService.user, PeriodicReportConfiguration, term).list([max: max, offset: offset])
            total = ReportConfiguration.countAllOwners(userService.user, PeriodicReportConfiguration, term).get()
        } else if (clazz == IcsrReportConfiguration.name) {
            result = ReportConfiguration.fetchAllOwners(userService.user, IcsrReportConfiguration, term).list([max: max, offset: offset])
            total = ReportConfiguration.countAllOwners(userService.user, IcsrReportConfiguration, term).get()
        } else if (clazz == IcsrProfileConfiguration.name) {
            result = ReportConfiguration.fetchAllOwners(userService.user, IcsrProfileConfiguration, term).list([max: max, offset: offset])
            total = ReportConfiguration.countAllOwners(userService.user, IcsrProfileConfiguration, term).get()
        } else {
            result = Class.forName(clazz).fetchAllOwners(userService.user, term).list([max: max, offset: offset])
            total = Class.forName(clazz).countAllOwners(userService.user, term).get()
        }
        render([items: result.collect { [id: it.id, text: it.fullName] }, total_count: total] as JSON)
    }

    def sharedWithFilterList(String term, Integer page, Integer max, String clazz) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max
        def result = []
        if (clazz == Configuration.name)
            result = ReportConfiguration.getActiveUsersAndUserGroups(Configuration.name, userService.user, term)
        else if (clazz == PeriodicReportConfiguration.name)
            result = ReportConfiguration.getActiveUsersAndUserGroups(PeriodicReportConfiguration.name, userService.user, term)
        else if (clazz == IcsrReportConfiguration.name)
            result = ReportConfiguration.getActiveUsersAndUserGroups(IcsrReportConfiguration.name, userService.user, term)
        else
            result = Class.forName(clazz).getActiveUsersAndUserGroups(userService.user, term)
        List<User> activeUsers = result.users
        List<UserGroup> activeGroups = result.userGroups
        List<User> userList = activeUsers.unique { it.id }.collect { [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded:it.isBlinded] }
        List<User> groupList = activeGroups.unique { it.id }.collect { [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded:it.isBlinded] }
        def items = []
        if (clazz != ReportRequest.name) {
            if (!term || ViewHelper.getMessage('app.label.iAmOwner').toLowerCase().indexOf(term.toLowerCase()) > -1)
                items << [id: Constants.OWNER_SELECT_VALUE, text: ViewHelper.getMessage('app.label.iAmOwner')]
            if (!term || ViewHelper.getMessage('app.label.mayTeam').toLowerCase().indexOf(term.toLowerCase()) > -1)
                items << [id: Constants.TEAM_SELECT_VALUE, text: ViewHelper.getMessage('app.label.mayTeam')]
            if (!term || ViewHelper.getMessage('app.label.sharedWithMe').toLowerCase().indexOf(term.toLowerCase()) > -1)
                items << [id: Constants.SHARED_WITH_ME_SELECT_VALUE, text: ViewHelper.getMessage('app.label.sharedWithMe')]
        } else {
            if (!term || ViewHelper.getMessage('app.widget.reportRequest.assigned.team').toLowerCase().indexOf(term.toLowerCase()) > -1)
                items << [id: Constants.TEAM_SELECT_VALUE, text: ViewHelper.getMessage('app.widget.reportRequest.assigned.team')]
            if (!term || ViewHelper.getMessage('app.widget.reportRequest.assigned').toLowerCase().indexOf(term.toLowerCase()) > -1)
                items << [id: Constants.SHARED_WITH_ME_SELECT_VALUE, text: ViewHelper.getMessage('app.widget.reportRequest.assigned')]
        }
        offset = Math.max(0, offset - items.size())
        if (offset == 0)
            max = max - items.size()
        else
            items = []
        splitResult(items, offset, max, groupList, userList)
        render([items: items, total_count: userList.size() + groupList.size() + 3] as JSON)

    }

    private splitResult(items, offset, max, groupList, userList) {

        String groupLabel = ""
        String userLabel = ""
        def selectedGroupItems = []
        def selectedUserItems = []
        if (offset == 0 && groupList.size() > 0) {
            groupLabel = ViewHelper.getMessage("user.group.label")
            selectedGroupItems = groupList.subList(0, Math.min(offset + max, groupList.size()))
        } else if ((offset > 0) && (offset < groupList.size())) {
            groupLabel = ""
            selectedGroupItems = groupList.subList(offset, Math.min(offset + max, groupList.size()))
        }

        int userOffset = offset - groupList.size()
        int usermax = max - selectedGroupItems.size()
        if ((userOffset + max) > 0) {
            if (userOffset <= 0 && userList.size() > 0) {
                userLabel = ViewHelper.getMessage("user.label")
                selectedUserItems = userList.subList(0, Math.min(0 + usermax, userList.size()))
            } else if ((userOffset > 0) && (userOffset < userList.size())) {
                userLabel = ""
                selectedUserItems = userList.subList(userOffset, Math.min(userOffset + usermax, userList.size()))
            }
        }
        if (selectedGroupItems.size() > 0)
            items << ["text": groupLabel, "children": selectedGroupItems]
        if (selectedUserItems.size() > 0)
            items << ["text": userLabel, "children": selectedUserItems]
    }

    def sharedWithUserList() {
        forSelectBox(params)

        Set<User> activeUsers = ExecutionStatus.getActiveUsersAndUserGroups(userService.user, params.term)
        List<User> userList = activeUsers.unique { it.id }.collect { [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: it.isBlinded] }
        def items = []

        if (!params.term || ViewHelper.getMessage('app.label.iAmOwner').toLowerCase().indexOf(params.term.toLowerCase()) > -1)
            items << [id: Constants.OWNER_SELECT_VALUE, text: ViewHelper.getMessage('app.label.iAmOwner')]
        if (!params.term || ViewHelper.getMessage('app.label.sharedWithMe').toLowerCase().indexOf(params.term.toLowerCase()) > -1)
            items << [id: Constants.SHARED_WITH_ME_SELECT_VALUE, text: ViewHelper.getMessage('app.label.sharedWithMe')]
        params.offset = Math.max(0, params.offset - items.size())

        if (params.offset == 0)
            params.max = params.max - items.size()
        else
            items = []
        List selectedUserItems = []
        String userLabel = ViewHelper.getMessage("user.label")
        if (params.offset <= 0 && userList.size() > 0) {
            selectedUserItems = userList.subList(0, Math.min(0 + params.max, userList.size()))
        } else if ((params.offset > 0) && (params.offset < userList.size())) {
            userLabel = ""
            selectedUserItems = userList.subList(params.offset, Math.min(params.offset + params.max, userList.size()))
        }
        if (!selectedUserItems.isEmpty()) {
            items << ["text": userLabel, "children": selectedUserItems]
        }
        render([items: items, total_count: userList.size() + 3] as JSON)
    }

    def assignedToFilterList(String term, Integer page, Integer max) {
        def items = []
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max
        int totalCount = 0
        if(page==1){
            totalCount = 2 //Initialized to 2 for Me and My Groups list values
            items << [id: AssignedToFilterEnum.ME.name(), text: ViewHelper.getMessage(AssignedToFilterEnum.ME.getI18nKey())]
            items << [id: AssignedToFilterEnum.MY_GROUPS.name(), text: ViewHelper.getMessage(AssignedToFilterEnum.MY_GROUPS.getI18nKey())]
        }
        if(!SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA") && params.type!='pvq') {
            Set<User> activeUsers = userService.getAllowedSharedWithUsersForCurrentUser(term)
            Set<UserGroup> activeGroups = userService.getAllowedSharedWithGroupsForCurrentUser(term)
            List<User> userList = activeUsers.unique { it.id }.collect {
                [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: it.isBlinded]
            }
            List<User> groupList = activeGroups.unique { it.id }.collect {
                [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: it.isBlinded]
            }
            splitResult(items, offset, max, groupList, userList)
            totalCount += userList.size() + groupList.size()
        }
        if(!SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA_PVQ") && params.type!='pvc') {
            Set<User> activeUsers = userService.getAllowedSharedWithUsersForCurrentUser(term)
            Set<UserGroup> activeGroups = userService.getAllowedSharedWithGroupsForCurrentUser(term)
            List<User> userList = activeUsers.unique { it.id }.collect {
                [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: it.isBlinded]
            }
            List<User> groupList = activeGroups.unique { it.id }.collect {
                [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: it.isBlinded]
            }
            splitResult(items, offset, max, groupList, userList)
            totalCount += userList.size() + groupList.size()
        }
        render([items: items, total_count: totalCount] as JSON)

    }

    def sharedWithUserListPvc(String term, Integer page, Integer max) {
        Map result = userService.allowedAssignedToUserListPvcPvq(term, page, max, params.userGroup, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVC)
        render(result as JSON)
    }

    def sharedWithUserListPvcInb(String term, Integer page, Integer max) {
        Map result = userService.allowedAssignedToUserListPvcPvq(term, page, max, params.userGroup, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVC_INB)
        render(result as JSON)
    }

    def sharedWithUserListPvq(String term, Integer page, Integer max) {
        Map result = userService.allowedAssignedToUserListPvcPvq(term, page, max, params.userGroup, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVQ)
        render(result as JSON)
    }

    def sharedWithGroupListPvc(String term, Integer page, Integer max) {
        Map result = userService.allowedAssignedToGroupListPvcPvq(term, page, max, params.user, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVC)
        render(result as JSON)
    }

    def sharedWithGroupListPvcInb(String term, Integer page, Integer max) {
        Map result = userService.allowedAssignedToGroupListPvcPvq(term, page, max, params.user, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVC_INB)
        render(result as JSON)
    }

    def sharedWithGroupListPvq(String term, Integer page, Integer max) {
        Map result = userService.allowedAssignedToGroupListPvcPvq(term, page, max, params.user, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVQ)
        render(result as JSON)
    }
}
