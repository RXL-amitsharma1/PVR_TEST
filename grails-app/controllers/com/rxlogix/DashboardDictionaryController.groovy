package com.rxlogix

import com.rxlogix.config.Dashboard
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.ReportWidget
import com.rxlogix.enums.DashboardEnum
import com.rxlogix.enums.WidgetTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONElement

@Secured(["isAuthenticated()"])
class DashboardDictionaryController {

    static allowedMethods = [save: "POST", update: ['PUT','POST'], delete: ['DELETE','POST']]

    def messageSource
    def userService
    def CRUDService
    def importService

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def index() {}

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def list() {
        def dashboards = Dashboard.findAllByIsDeleted(false).collect {
            it.toMap()
        }
        response.status = 200
        render dashboards as JSON
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def create() {
        render view: "create", model: [dashboard: new Dashboard(), users: userService.getAllowedSharedWithUsersForCurrentUser(), userGroups: userService.getAllowedSharedWithGroupsForCurrentUser()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def save() {
        Dashboard dashboard = new Dashboard()
        dashboard.owner = userService.currentUser
        bindData(dashboard, params, ["sharedWith"])
        bindSharedWith(dashboard, params.list('sharedWith'), false)
        try {
            CRUDService.save(dashboard)
        } catch (ValidationException ve) {
            render view: "create", model: [dashboard: dashboard, users: userService.getAllowedSharedWithUsersForCurrentUser(), userGroups: userService.getAllowedSharedWithGroupsForCurrentUser()]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.dashBoard', default: 'Dashboard'), dashboard.label ?: ""])}"
        redirect(action: "index")
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def edit(Long id) {
        Dashboard dashboard = Dashboard.read(id)
        if (!dashboard) {
            notFound()
            return
        }
        render view: "edit", model: [dashboard: dashboard, users: userService.getAllowedSharedWithUsersForCurrentUser(), userGroups: userService.getAllowedSharedWithGroupsForCurrentUser()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def update(Long id) {
        Dashboard dashboard = Dashboard.get(id)
        if (!dashboard) {
            notFound()
            return
        }
        bindData(dashboard, params, ["sharedWith"])
        bindSharedWith(dashboard, params.list('sharedWith'), true)
        try {
            CRUDService.update(dashboard)
        } catch (ValidationException ve) {
            render view: "edit", model: [dashboard: dashboard, users: userService.getAllowedSharedWithUsersForCurrentUser(), userGroups: userService.getAllowedSharedWithGroupsForCurrentUser()]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.dashBoard', default: 'Dashboard'), dashboard.label ?: ""])
        redirect(action: 'index')
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION', 'ROLE_USER_MANAGER'])
    def show(Long id) {
        Dashboard dashboard = Dashboard.read(id)
        if (!dashboard) {
            notFound()
            return
        }
        render view: "show", model: [dashboard: dashboard, json: importService.getDashboardAsJSON(dashboard), users: userService.getAllowedSharedWithUsersForCurrentUser(), userGroups: userService.getAllowedSharedWithGroupsForCurrentUser()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION', 'ROLE_USER_MANAGER'])
    def importJson() {
        if (params?.json) {
            JSONElement listOfDashboards
            try {
                listOfDashboards = JSON.parse("[${params?.json}]")
            } catch (ConverterException ce) {
                flash.error = message(code: "app.load.import.json.parse.fail")
                redirect(action: "index")
                return
            }
            List<Dashboard> dashboardList = importService.importDashboards(listOfDashboards)
            List success = []
            List failed = []
            dashboardList.each {
                if (!it.hasErrors()) {
                    success.add(it.label)
                } else {
                    log.error("Failed to import $it. ${it.errors}")
                    failed.add(it.label)
                }
            }
            if (success.size() > 0) {
                flash.message = message(code: "app.load.import.success", args: [success])
            }
            if (failed.size() > 0) {
                flash.error = message(code: "app.load.import.fail", args: [failed])
            }
        } else {
            flash.warn = message(code: "app.load.import.noData")
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION', 'ROLE_USER_MANAGER'])
    def delete(Long id) {
        Dashboard dashboard = Dashboard.get(id)
        if (!dashboard) {
            notFound()
            return
        }
        if (dashboard.dashboardType in [DashboardEnum.PVR_MAIN, DashboardEnum.PVR_MAIN]) {
            flash.error = message(code: "app.label.dashboard.delete.main.error.message")
        } else {
            try {
                CRUDService.softDelete(dashboard, dashboard.label, params.deleteJustification)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.dashBoard', default: 'Dashboard'), dashboard.label ?: ""])}"
            } catch (ValidationException ve) {
                flash.error = message(code: "app.label.dashboard.delete.error.message")
            }
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.dashBoard'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private void bindSharedWith(Dashboard instance, List<String> sharedWith, Boolean isUpdate = false) {
        List<User> allowedUsers = userService.getAllowedSharedWithUsersForCurrentUser();
        List<UserGroup> allowedGroups = userService.getAllowedSharedWithGroupsForCurrentUser();

        if (isUpdate) {
            if (instance.sharedWith) {
                allowedUsers.addAll(instance.sharedWith)
                allowedUsers.unique { it.id }
            }
            if (instance.sharedWithGroup) {
                allowedGroups.addAll(instance.sharedWithGroup)
                allowedGroups.unique { it.id }
            }
            instance.sharedWith?.clear()
            instance.sharedWithGroup?.clear()
        }

        if (sharedWith) {
            sharedWith.each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup && allowedGroups.find { it.id == userGroup.id }) {
                        instance.addToSharedWithGroup(userGroup)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    if (user && allowedUsers.find { it.id == user.id }) {
                        instance.addToSharedWith(user)
                    }
                }
            }
        }
    }

    def editModal(Long id){
        Dashboard dashboard = Dashboard.read(id)
        if (!dashboard) {
            notFound()
            return
        }

        List users = userService.getAllowedSharedWithUsersForCurrentUser().collect{
            [id: it.id, name: it.fullName, selected: dashboard.sharedWith?.id.contains(it.id)]
        }

        List userGroups = userService.getAllowedSharedWithGroupsForCurrentUser().collect{
            [id: it.id, name: it.name, selected: dashboard.sharedWithGroup?.id.contains(it.id)]
        }

        render ([dashboardLabel: dashboard.label, dashboardIcon: dashboard.icon, dashboardType: dashboard.dashboardType.name(), isAdmin:userService.isAnyGranted("ROLE_SYSTEM_CONFIGURATION"),
                 hasParent     : (dashboard.parentId && (dashboard.parentId != "0")), userGroups: userGroups, users: users] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION','ROLE_USER_MANAGER'])
    def updateModal(Long id){
        Dashboard updateDashboard = Dashboard.get(id)
        if (!updateDashboard) {
            notFound()
            return
        }
        updateDashboard.label = params.label
        updateDashboard.icon = params.icon
        updateDashboard.dashboardType = params.type
        updateDashboard.sharedWith?.clear()
        updateDashboard.sharedWithGroup?.clear()
        if(params['sharedWith[]']){
            List<String> sharedWithList = params.list('sharedWith[]')
            sharedWithList.each{ String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    updateDashboard.addToSharedWithGroup(userGroup)
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    updateDashboard.addToSharedWith(user)
                }
            }
        }
        try{
            CRUDService.update(updateDashboard)
        }catch(Exception e){
            render (status:500)
        }
        render (status:200)
    }
}

