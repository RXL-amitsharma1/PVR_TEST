package com.rxlogix

import com.rxlogix.config.AdvancedAssignment
import com.rxlogix.user.User
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND


@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class AdvancedAssignmentController {

    def userService
    def advancedAssignmentService
    def CRUDService

    static allowedMethods = [save:'POST', update: ['PUT','POST'], delete: ['DELETE','POST']]

    def index() {
        render view: "index"
    }

    def list() {
        def advancedAssignments = AdvancedAssignment.findAllByIsDeletedAndTenantId(false, Tenants.currentId() as Long)?.collect {
            it.toAdvancedAssignmentMap()
        }
        response.status = 200
        render advancedAssignments as JSON
    }

    def create(AdvancedAssignment advancedAssignment) {
        User assignedUser = userService.getCurrentUser()
        render view: "create", model: [advancedAssignmentInstance: advancedAssignment,
                               ownerUsername: assignedUser.fullName, ownerUserId: assignedUser.id]
    }

    def save(){
        AdvancedAssignment advancedAssignmentInstance = new AdvancedAssignment()
        bindData(advancedAssignmentInstance, params, ['assignedUsername'])
        advancedAssignmentInstance.tenantId = Tenants.currentId() as Long
        try{
            CRUDService.save(advancedAssignmentInstance)
        }catch(Exception e){
            log.error("Error in creating advanced assignment", e)
            flash.error = "${message(code: 'app.advanced.assignment.create.error')}"
            render view: "create", model: [advancedAssignmentInstance: advancedAssignmentInstance,
                                            ownerUsername: params.assignedUsername, ownerUserId: params.assignedUserId]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.advanced.assignment.appName', default: 'Advanced Assignment'), advancedAssignmentInstance.name])}"
        redirect(action: "index")
    }

    def edit(Long id){
        AdvancedAssignment advancedAssignment = AdvancedAssignment.get(id)
        if(!advancedAssignment){
            notFound()
        }
        User assignedUser = advancedAssignment.assignedUser
        render view: "edit", model: [advancedAssignmentInstance: advancedAssignment,
                                     ownerUsername: assignedUser.fullName, ownerUserId: assignedUser.id]
    }

    def update(Long id){
        AdvancedAssignment advancedAssignment = AdvancedAssignment.get(id)
        if(!advancedAssignment){
            notFound()
        }
        bindData(advancedAssignment, params, ['id', 'assignedUsername', 'assignedUser'])
        User assignedUser = advancedAssignment.assignedUser
        try{
            CRUDService.update(advancedAssignment)
        }catch(Exception e){
            log.error("Error in creating advanced assignment", e)
            flash.error = "${message(code: 'app.advanced.assignment.edit.error')}"
            render view: "edit", model: [advancedAssignmentInstance: advancedAssignment,
                                         ownerUsername: assignedUser.fullName,
                                         ownerUserId: assignedUser.id]
            return
        }
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.advanced.assignment.appName', default: 'Advanced Assignment'), advancedAssignment.name])}"
        redirect(action: "index")
    }

    def show(Long id) {
        AdvancedAssignment advancedAssignment = AdvancedAssignment.read(id)
        if (!advancedAssignment) {
            notFound()
        }
        User assignedUser = advancedAssignment.assignedUser
        render view: "show", model: [advancedAssignmentInstance: advancedAssignment,
                                     ownerUsername: assignedUser.fullName,
                                     ownerUserId: assignedUser.id]
    }

    def delete(Long id) {
        AdvancedAssignment advancedAssignment = AdvancedAssignment.get(id)
        if (!advancedAssignment) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(advancedAssignment, advancedAssignment.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.advanced.assignment.appName', default: 'Advanced Assignment'), advancedAssignment.name])}"
        } catch (Exception e) {
            log.error("Error in deleting advanced assignment", e)
            flash.error = "${message(code: 'app.advanced.assignment.delete.error')}"
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.advanced.assignment.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
