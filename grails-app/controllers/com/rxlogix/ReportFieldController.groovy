package com.rxlogix

import com.rxlogix.commandObjects.ReportFieldCO
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.*

@Secured(['ROLE_ADMIN'])
class ReportFieldController {

    static allowedMethods = [save: "POST", update: ['PUT','POST'], delete: ['DELETE','POST']]

    def reportFieldService
    def CRUDService
    def userService

    def index() {
        render view: 'index', model: [isCreatedByUserList: ReportField.findAllByIsCreatedByUser(true)]
    }

    def create() {
        render view: "create", model: [fields        : reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                                       selectedLocale: userService.currentUser.preference.locale, fieldGroup: ReportFieldGroup.findAllByIsDeleted(false)]
    }

    def save(ReportFieldCO reportFieldCO) {
        Map model = [fields        : reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                     selectedLocale: userService.currentUser.preference.locale,
                     fieldGroup    : ReportFieldGroup.findAllByIsDeleted(false)]
        ReportField reportField
        if (reportFieldCO.validate()) {
            try {
                reportField = new ReportField()
                reportField.properties = reportFieldCO.properties
                reportField.isCreatedByUser = true
                CRUDService.save(reportField)
            } catch (ValidationException ve) {
                model.reportFieldInstance = reportField
                render view: "create", model: model
                return
            }
        } else {
            log.warn(reportFieldCO.errors.allErrors?.toString())
            model.reportFieldInstance = reportFieldCO
            render view: "create", model: model
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.reportField.appName'), reportField.name])
        redirect(action: "index")
    }

    def show(Long id) {
        ReportField reportField = id ? ReportField.read(id) : null
        if (!reportField) {
            notFound()
        }
        render view: "show", model: [reportFieldInstance: reportField]
    }

    def edit(Long id) {
        ReportField reportField = id ? ReportField.read(id) : null
        if (!reportField) {
            notFound()
            return
        }
        render view: "edit", model: [reportFieldInstance: reportField, fields: reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                                     selectedLocale     : userService.currentUser.preference.locale, fieldGroup: ReportFieldGroup.findAllByIsDeleted(false), isCentralField:reportFieldService.isLinkedField(reportField.name)]
    }

    def update(ReportFieldCO reportFieldCO) {
        Map model = [fields        : reportFieldService.getAllReportFieldsWithGroupsForTemplates(),
                     selectedLocale: userService.currentUser.preference.locale,
                     fieldGroup    : ReportFieldGroup.findAllByIsDeleted(false)]
        ReportField reportField = reportFieldCO.id ? ReportField.get(reportFieldCO.id) : null
        if (!reportField) {
            notFound()
            return
        }
        reportField.properties = reportFieldCO.properties
        if (reportFieldCO.validate()) {
            try {
                if(ReportField.findByIdAndIsCreatedByUser(reportFieldCO.id,true)) {
                    reportField.isCreatedByUser = true
                }
                CRUDService.update(reportField)
            } catch (ValidationException ve) {
                model.reportFieldInstance = reportField
                render view: "edit", model: model
                return
            }
        } else {
            log.warn(reportFieldCO.errors.allErrors?.toString())
            model.reportFieldInstance = reportFieldCO
            render view: "edit", model: model
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.reportField.appName'), reportField.name])
        redirect(action: "index")
    }

    def delete(ReportFieldCO reportFieldCO) {
        ReportField reportField = reportFieldCO.id ? ReportField.get(reportFieldCO.id) : null
        if (!reportField) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (!currentUser.isAdmin()) {
            flash.warn = message(code: "app.fieldProfile.delete.permission", args: [reportField.name])
            redirect(view: "index")
            return
        }
        try {
            CRUDService.softDelete(reportField, reportField.name, params.deleteJustification)
            reportFieldService.clearCacheReportFields()
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.fieldProfile.label'), reportField.name])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.fieldProfile.label'), reportField.name])
                    redirect(action: "show", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.reportField.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
