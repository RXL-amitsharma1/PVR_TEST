package com.rxlogix

import com.rxlogix.config.ReportRequest
import com.rxlogix.config.ReportRequestField
import com.rxlogix.config.ReportRequestLinkType
import com.rxlogix.config.ReportRequestPriority
import com.rxlogix.config.ReportRequestType
import com.rxlogix.config.UserDictionary
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class ReportRequestTypeController {

    def CRUDService

    static allowedMethods = [save:'POST',update: ['PUT','POST'], delete: ['DELETE','POST']]

    def index() {}

    def list() {
        def reportRequestTypes = ReportRequestType.findAllByIsDeleted(false).collect {
            it.toReportRequestTypeMap()
        }
        response.status = 200
        render reportRequestTypes as JSON
    }

    def listPriority() {
        def reportRequestTypes = ReportRequestPriority.findAllByIsDeleted(false).collect {
            it.toReportRequestTypeMap()
        }
        response.status = 200
        render reportRequestTypes as JSON
    }

    def listLink() {
        def reportRequestTypes = ReportRequestLinkType.findAllByIsDeleted(false).collect {
            it.toReportRequestTypeMap()
        }
        response.status = 200
        render reportRequestTypes as JSON
    }

    def listUsedDictionary() {
        def reportRequestTypes = UserDictionary.findAllByTypeAndIsDeleted(params.type as UserDictionary.UserDictionaryType, false).collect {
            it.toReportRequestTypeMap()
        }
        response.status = 200
        render reportRequestTypes as JSON
    }

    def listFields() {
        def reportRequestTypes = ReportRequestField.findAllByIsDeleted(false).collect {
            it.toMap()
        }
        response.status = 200
        render reportRequestTypes as JSON
    }

    def create() {
        def instance
        if (params.type == "priority")
            instance = new ReportRequestPriority()
        else if (params.type == "link")
            instance = new ReportRequestLinkType()
        else if (params.type == "type")
            instance = new ReportRequestType()
        else if (params.type == "field")
            instance = new ReportRequestField()
        else
            instance = new UserDictionary(type: params.type as UserDictionary.UserDictionaryType)
        render view: "create", model: [reportRequestTypeInstance: instance, aggEditable: true]
    }

    def save() {
        def instance
        if (params.type == "priority")
            instance = new ReportRequestPriority()
        else if (params.type == "link")
            instance = new ReportRequestLinkType()
        else if (params.type == "type")
            instance = new ReportRequestType()
        else if (params.type == "field")
            instance = new ReportRequestField()
        else
            instance = new UserDictionary(type: params.type as UserDictionary.UserDictionaryType)
        bindData(instance, params)
        try {
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "create", model: [reportRequestTypeInstance: instance, aggEditable: true]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [getInstanceLabelName(instance), instance?.name])}"
        redirect(action: "index", params: [type: params.type])
    }

    def edit() {
        boolean aggEditable=true
        def instance
        if (params.type == "priority")
            instance = ReportRequestPriority.get(params.long("id"))
        else if (params.type == "link")
            instance = ReportRequestLinkType.get(params.long("id"))
        else if (params.type == "type") {
            instance = ReportRequestType.get(params.long("id"))
            aggEditable = !(ReportRequest.findAllByReportRequestTypeAndIsDeleted(instance, false)?.size() > 0)
        } else if (params.type == "field")
            instance = ReportRequestField.get(params.long("id"))
        else
            instance = UserDictionary.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        render view: "edit", model: [reportRequestTypeInstance: instance, aggEditable: aggEditable]
    }

    def update() {
        def instance
        if (params.type == "priority")
            instance = ReportRequestPriority.get(params.long("id"))
        else if (params.type == "link")
            instance = ReportRequestLinkType.get(params.long("id"))
        else if (params.type == "type")
            instance = ReportRequestType.get(params.long("id"))
        else if (params.type == "field")
            instance = ReportRequestField.get(params.long("id"))
        else
            instance = UserDictionary.get(params.long("id"))

        if (!instance) {
            notFound()
        }
        bindData(instance, params)
        try {
            CRUDService.update(instance)
        } catch (ValidationException ve) {
            render view: "edit", model: [reportRequestTypeInstance: instance]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [getInstanceLabelName(instance), instance?.name])
        redirect(action: 'index', params: [type: params.type])
    }


    def show() {
        def instance
        if (params.type == "priority")
            instance = ReportRequestPriority.get(params.long("id"))
        else if (params.type == "link")
            instance = ReportRequestLinkType.get(params.long("id"))
        else if (params.type == "type")
            instance = ReportRequestType.get(params.long("id"))
        else if (params.type == "field")
            instance = ReportRequestField.get(params.long("id"))
        else
            instance = UserDictionary.get(params.long("id"))

        if (!instance) {
            notFound()
        }

        render view: "show", model: [reportRequestTypeInstance: instance]
    }

    def delete() {
        println "params.type :: "+params.type
        def instance
        if (params.type == "priority")
            instance = ReportRequestPriority.get(params.long("id"))
        else if (params.type == "link")
            instance = ReportRequestLinkType.get(params.long("id"))
        else if (params.type == "type")
            instance = ReportRequestType.get(params.long("id"))
        else if (params.type == "field")
            instance = ReportRequestField.get(params.long("id"))
        else instance = UserDictionary.get(params.long("id"))

        if (!instance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(instance, instance.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [getInstanceLabelName(instance), instance?.name])}"
        } catch (ValidationException ve) {
            flash.error = message(code: "default.unable.deleted.message", args: [getInstanceLabelName(instance)])
        }
        redirect(action: "index", params: [type: params.type])
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.dictionary.element'), params.id])
                redirect action: "index", method: "GET", params: [type: params.type]
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private String getInstanceLabelName(Object instance) {
        String name = instance.getClass()?.simpleName
        String code = ""
        Map labelCodes = [
                (UserDictionary.UserDictionaryType.PSR_TYPE_FILE): "psrTypeFile",
                (UserDictionary.UserDictionaryType.INN)          : "inn",
                (UserDictionary.UserDictionaryType.DRUG)         : "drugCode"
        ]
        if (name == "UserDictionary"  && labelCodes.containsKey(instance.type)) {
            code = "app.label.reportRequest.${labelCodes[instance.type]}"
        } else {
            code = "app.label.${name?.uncapitalize()}.appName"
        }
        return message(code: code, default: 'Entry')
    }
}
