package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.Email
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.dto.ResponseDTO
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class EmailController {

    def messageSource
    def userService
    def CRUDService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {}

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {
        def emails = Email.findAllByIsDeletedAndTenantId(false, Tenants.currentId().toString().toLong()).collect {
            it.toEmailMap()
        }
        response.status = 200
        render emails as JSON
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create(Email email) {
        render view: "create", model: [emailInstance: email]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save(Email email) {
        try {
            email.tenantId = Tenants.currentId() as Long
            CRUDService.save(email)
        } catch (ValidationException ve) {
            render view: "create", model: [email: email]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.email.appName', default: 'Email'), email.email])}"
        redirect(action: "index")
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit(Long id) {
        Email email = Email.read(id)
        if (!email) {
            notFound()
        }
        render view: "edit", model: [email: email]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update(Long id) {
        Email email = Email.get(id)
        if (!email) {
            notFound()
        }
        bindData(email, [email: params.email, description: params.description])
        try {
            CRUDService.update(email)
        } catch (ValidationException ve) {
            render view: "edit", model: [email: email]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.email.appName', default: 'Email'), email.email])
        redirect(action: 'index')
    }

    def axajAdd() {
        ResponseDTO responseDTO = new ResponseDTO(message: "ok")
        try {
            Email email = new Email(email: params.email, description: params.description)
            email.tenantId = Tenants.currentId() as Long
            CRUDService.update(email)
        } catch (ValidationException ve) {
            responseDTO.setFailureResponse(ve.errors)
        }
        render responseDTO as JSON
    }

    def ajaxAddAll() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try {
            request.JSON.each {
                Email email = new Email(email: it.email, description: it.description)
                email.tenantId = Tenants.currentId() as Long
                CRUDService.save(email)
            }
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    
    def show(Long id) {
        Email email = Email.read(id)
        if (!email) {
            notFound()
        }
        render view: "show", model: [email: email]
    }
    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete(Long id) {
        Email email = Email.get(id)
        if (!email) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(email, email.email, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.email.appName', default: 'Email'), email.email])}"
        } catch (ValidationException ve) {
            flash.error = message(code: "app.label.email.delete.error.message")
        }
        redirect(action: "index")
    }

    def allEmails(Long id) {
        if (id) {
            ReportConfiguration configuration = Configuration.read(id) ?: PeriodicReportConfiguration.read(id)
            render(userService.getAllEmails(configuration) as JSON)
            return
        }
        render(userService.getAllEmails() as JSON)
    }

    def allEmailsForCC(String emails) {
        render(userService.getAllEmailsForCC(emails) as JSON)
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.email.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
