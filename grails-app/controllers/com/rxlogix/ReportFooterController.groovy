package com.rxlogix

import com.rxlogix.config.ReportFooter
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class ReportFooterController {

    def CRUDService

    static allowedMethods = [save: "POST", update: ['PUT','POST'], delete: ['DELETE','POST']]

    def index() {}

    def list() {
        def reportFooters = ReportFooter.findAllByIsDeleted(false).collect {
            it.toReportFooterMap()
        }
        response.status = 200
        render reportFooters as JSON
    }

    def create(ReportFooter reportFooter) {
        render view: "create", model: [reportFooterInstance: reportFooter]
    }

    def save(ReportFooter reportFooter) {

        try {
            CRUDService.save(reportFooter)
        } catch (ValidationException ve) {
            render view: "create", model: [reportFooterInstance: reportFooter]
            return
        }
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.reportFooter.appName', default: 'Report Footer'), reportFooter.footer])}"
        redirect(action: "index")
    }

    def edit(Long id) {
        ReportFooter reportFooter = ReportFooter.read(id)
        if (!reportFooter) {
            notFound()
        }
        render view: "edit", model: [reportFooterInstance: reportFooter]
    }

    def update(ReportFooter reportFooter) {

        if (!reportFooter) {
            notFound()
        }

        try {
            CRUDService.update(reportFooter)
        } catch (ValidationException ve) {
            render view: "edit", model: [reportFooterInstance: reportFooter]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.reportFooter.appName', default: 'Footer'), reportFooter.footer])
        redirect(action: 'index')
    }


    def show(Long id) {
        ReportFooter reportFooter = ReportFooter.read(id)
        if (!reportFooter) {
            notFound()
        }
        render view: "show", model: [reportFooterInstance: reportFooter]
    }

    def delete(Long id) {
        ReportFooter reportFooter = ReportFooter.get(id)
        if (!reportFooter) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(reportFooter, reportFooter.footer, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.reportFooter.appName', default: 'Footer'), reportFooter.footer])}"
        } catch (ValidationException ve) {
            flash.error = message(code:  "app.label.reportFooter.delete.error.message")
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.reportFooter.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
