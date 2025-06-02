package com.rxlogix

import com.rxlogix.config.EmailTemplate
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.EmailTemplateTypeEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class EmailTemplateController {

    static allowedMethods = [save: "POST", update: ['PUT','POST']]

    def messageSource
    def CRUDService
    def userService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {}

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {
        def emails = EmailTemplate.findAllByIsDeleted(false).collect {
            it.toMap()
        }
        response.status = 200
        render emails as JSON
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create() {
        EmailTemplate emailTemplate = new EmailTemplate()
        render view: "create", model: [emailTemplateInstance: emailTemplate]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save() {
        EmailTemplate emailTemplate = new EmailTemplate()
        bindData(emailTemplate, params)
        emailTemplate.owner = userService.currentUser
        try {
            CRUDService.save(emailTemplate)
        } catch (ValidationException ve) {
            render view: "create", model: [emailTemplate: emailTemplate]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.emailTemplate.appName', default: 'Email Template'), emailTemplate.name])}"
        redirect(action: "index")
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit(Long id) {
        EmailTemplate emailTemplate = EmailTemplate.read(id)
        if (!emailTemplate) {
            notFound()
        }
        render view: "edit", model: [emailTemplate: emailTemplate]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update() {
        EmailTemplate emailTemplate =  EmailTemplate.get(params.long("id"))
        if (!emailTemplate) {
            notFound()
        }
        bindData(emailTemplate, params)
        try {
            CRUDService.update(emailTemplate)
        } catch (ValidationException ve) {
            render view: "edit", model: [emailTemplate: emailTemplate]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.emailTemplate.appName', default: 'Email Template'), emailTemplate.name])
        redirect(action: 'index')
    }

    def axajList(Boolean isUserSpecificTemplate, String searchTerm) {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        searchTerm = searchTerm ? "%${searchTerm}%" : '%'
        try {
            List<EmailTemplate> list = isUserSpecificTemplate ? EmailTemplate.findAllByIsDeletedAndOwnerAndTypeAndNameIlike(false, userService.currentUser, EmailTemplateTypeEnum.USER, searchTerm) :
                    EmailTemplate.findAllByIsDeletedAndTypeAndNameIlike(false, EmailTemplateTypeEnum.PUBLIC, searchTerm)

            responseDTO.setSuccessResponse(list*.toContentMap())
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    def axajSave() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try {
            EmailTemplate emailTemplate = new EmailTemplate()
            if (params.id) {
                emailTemplate = EmailTemplate.get(params.id as Long)
            } else {
                emailTemplate.name = params.name
                emailTemplate.description = params.description
                emailTemplate.type = EmailTemplateTypeEnum.USER
                emailTemplate.owner = userService.currentUser
            }
            emailTemplate.body = params.body
            CRUDService.update(emailTemplate)
            responseDTO.setSuccessResponse(emailTemplate.id)
        } catch (ValidationException ve) {
            responseDTO.setFailureResponse(ve.errors)
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    def axajDelete() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try {
            EmailTemplate emailTemplate = EmailTemplate.get(params.id as Long)
            CRUDService.delete(emailTemplate)
        } catch (Exception e) {
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def show(Long id) {
        EmailTemplate emailTemplate = EmailTemplate.read(id)
        if (!emailTemplate) {
            notFound()
        }
        render view: "show", model: [emailTemplate: emailTemplate]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete(Long id) {
        EmailTemplate emailTemplate = EmailTemplate.get(id)
        if (!emailTemplate) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(emailTemplate, emailTemplate.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.emailTemplate.appName', default: 'Email Template'), emailTemplate.name])}"
        } catch (ValidationException ve) {
            flash.error = message(code: "app.label.emailTemplate.delete.error.message")
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.emailTemplate.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
