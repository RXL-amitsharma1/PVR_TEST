package com.rxlogix

import com.rxlogix.commandObjects.CustomReportFieldCO
import com.rxlogix.config.CustomReportField
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.dto.DataTableDTO
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_CUSTOM_FIELD'])
class CustomFieldController {

    static allowedMethods = [save: "POST", update: ['PUT','POST'], delete: ['DELETE','POST']]

    def CRUDService
    def reportFieldService
    def userService

    def index() {
    }

    def list() {
        DataTableDTO dataTableDTO = new DataTableDTO()
        try {
            dataTableDTO.aaData = CustomReportField.findAllByIsDeleted(false).collect { it.toMap() }
        } catch (Exception e) {
            dataTableDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render dataTableDTO.toAjaxResponse()
    }

    def create() {
        render view: "create", model: [fields        : reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                                       selectedLocale: userService.currentUser.preference.locale, fieldGroup: ReportFieldGroup.findAllByIsDeleted(false)]
    }

    def save(CustomReportFieldCO customReportFieldCO) {
        Map model = [fields        : reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                     selectedLocale: userService.currentUser.preference.locale,
                     fieldGroup    : ReportFieldGroup.findAllByIsDeleted(false)]
        CustomReportField customReportField
        if (customReportFieldCO.validate()) {
            try {
                customReportField = new CustomReportField()
                customReportField = preValidateTemplate(customReportField, customReportFieldCO)
                if(customReportField.hasErrors()) {
                    throw new ValidationException("Custom Report Field has added validation issues", customReportField.errors)
                }
                customReportField.properties = customReportFieldCO.properties
                CRUDService.save(customReportField)
            } catch (ValidationException ve) {
                customReportField.errors = ve.errors
                model.customFieldInstance = customReportField
                render view: "create", model: model
                return
            }
        } else {
            log.warn(customReportFieldCO.errors.allErrors?.toString())
            model.customFieldInstance = customReportFieldCO
            render view: "create", model: model
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.customField.appName'), customReportField.customName])
        redirect(action: "index")
    }

    def edit(Long id) {
        CustomReportField customFieldInstance = id ? CustomReportField.read(id) : null
        if (!customFieldInstance) {
            notFound()
        }
        render view: "edit", model: [customFieldInstance: customFieldInstance, fields: reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                                     selectedLocale     : userService.currentUser.preference.locale, fieldGroup: ReportFieldGroup.findAllByIsDeleted(false)]
    }

    def update(CustomReportFieldCO customReportFieldCO) {
        Map model = [fields        : reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                     selectedLocale: userService.currentUser.preference.locale,
                     fieldGroup    : ReportFieldGroup.findAllByIsDeleted(false)]
        CustomReportField customReportField = customReportFieldCO.id ? CustomReportField.get(customReportFieldCO.id) : null
        if (!customReportField) {
            notFound()
            return
        }
        customReportField.properties = customReportFieldCO.properties
        if (customReportFieldCO.validate()) {
            try {
                CRUDService.update(customReportField)
            } catch (ValidationException ve) {
                model.customFieldInstance = customReportField
                render view: "edit", model: model
                return
            }
        } else {
            log.warn(customReportFieldCO.errors.allErrors?.toString())
            model.customFieldInstance = customReportField
            render view: "edit", model: model
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.customField.appName'), customReportField.customName])
        redirect(action: "index")
    }


    def show(Long id) {
        CustomReportField customField = id ? CustomReportField.read(id) : null
        if (!customField) {
            notFound()
        }
        render view: "show", model: [customFieldInstance: customField]
    }

    def delete(Long id) {
        CustomReportField customField = id ? CustomReportField.get(id) : null
        if (!customField) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(customField, customField.name, params.deleteJustification)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.customField.appName'), customField.customName])
        } catch (ValidationException ve) {
            flash.error = message(code: "app.label.customField.delete.error.message")
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.customField.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private CustomReportField preValidateTemplate(CustomReportField customReportField, CustomReportFieldCO customReportFieldCO) {
        byte[] defaultExpression = customReportFieldCO.defaultExpression.getBytes("UTF-8")
        Integer defaultExpressionMaxSize = CustomReportField.constrainedProperties.defaultExpression.maxSize
        if (defaultExpression.length > defaultExpressionMaxSize) {
            customReportField.errors.rejectValue('defaultExpression', 'com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded', ["","","",defaultExpressionMaxSize] as Object[], "Custom Expression max size exceeded")
        }
        return customReportField
    }

}
