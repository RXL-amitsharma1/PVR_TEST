package com.rxlogix.publisher


import com.rxlogix.config.BasicPublisherSource
import com.rxlogix.config.ExecutedPublisherSource
import com.rxlogix.config.publisher.PublisherCommonParameter
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.config.publisher.PublisherTemplateParameter
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

@Secured(["isAuthenticated()"])
class PublisherCommonParameterController {

    def messageSource
    def publisherService
    def publisherSourceService
    def userService
    def CRUDService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {
        def templates = PublisherCommonParameter.findAllByIsDeleted(false).collect {
            it.toMap()
        }
        response.status = 200
        render templates as JSON
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create(PublisherCommonParameter instance) {
        render view: "create", model: [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save() {
        PublisherCommonParameter instance = new PublisherCommonParameter()
        try {
            bindData(instance, params)
            if (userService.getUser())
                instance.createdBy=userService.getUser().username
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "create", model: [instance: instance]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.PublisherCommonParameter.appName'), instance.name])}"
        redirect(action: "index")
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit(Long id) {
        PublisherCommonParameter instance = PublisherCommonParameter.read(id)
        if (!instance) {
            notFound()
        }
        render view: "edit", model: [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update() {
        PublisherCommonParameter instance = PublisherCommonParameter.get(params.id)
        if (!instance) {
            notFound()
        }

        try {
            bindData(instance, params)
            CRUDService.update(instance)
        } catch (ValidationException ve) {
            render view: "edit", model: [instance: instance]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.PublisherCommonParameter.appName'), instance.name])
        redirect(action: 'index')
    }


    def show(Long id) {
        PublisherCommonParameter instance = PublisherCommonParameter.read(id)
        if (!instance) {
            notFound()
        }
        render view: "show", model: [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete(Long id) {
        PublisherCommonParameter instance = PublisherCommonParameter.get(id)
        if (!instance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(instance, instance.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.PublisherCommonParameter.appName'), instance.name])}"
        } catch (ValidationException ve) {
            flash.error = message(code: "app.label.PublisherCommonParameter.delete.error.message")
        }
        redirect(action: "index")
    }


    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.PublisherCommonParameter.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: HttpStatus.NOT_FOUND }
        }
    }
}
