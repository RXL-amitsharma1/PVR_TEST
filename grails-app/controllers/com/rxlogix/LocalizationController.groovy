package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.localization.Localization
import com.rxlogix.util.AuditLogConfigUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class LocalizationController implements SanitizePaginationAttributes {

    static allowedMethods = [save: 'POST', update: 'POST']

    def CRUDService

    def index(){

    }

    def search(String q){
        render view: "index", model: [preSearchString: q]
    }

    def create(){

    }

    def save(){
        Localization loc = new Localization(params)
        try {
            CRUDService.save(loc)
            AuditLogConfigUtil.logChanges(loc, [text: loc.text], [:], Constants.AUDIT_LOG_INSERT,loc.code)
            Localization.resetThis(loc.code)
        } catch (ValidationException ve) {
            render view: "create", model: [locInstance: loc]
            return
        }
        render view: "show", model: [locInstance: loc]
    }

    def show(Long id){
        Localization loc = id ? Localization.read(id) : null
        if (!loc) {
            notFound()
            return
        }
        render view: "show", model: [locInstance: loc]
    }

    def edit(Long id){
        Localization loc = id ? Localization.read(id) : null
        if (!loc) {
            notFound()
            return
        }
        render view: "edit", model: [locInstance: loc]
    }

    def update(Long id, String text){
        Localization loc = id ? Localization.get(id) : null
        if (!loc) {
            notFound()
            return
        }
        try {
            String oldText = loc.text
            loc.text = text
            CRUDService.update(loc)
            AuditLogConfigUtil.logChanges(loc, [text: loc.text], [text: oldText], Constants.AUDIT_LOG_UPDATE,loc.code)
            Localization.resetThis(loc.code)
        } catch (ValidationException ve) {
            render(view: 'edit', model: [locInstance: loc])
            return
        }
        redirect(action: 'show', id: loc.id)
    }

    def list(){
        sanitize(params)
        params.sort = params.sort ? (params.sort=='dateCreated' ? 'code' : params.sort) : 'code'
        List<Localization> locList = Localization.fetchByString(params.searchString, false).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData         : locList.collect { [id: it.id, code: it.code, locale: it.locale, text: it.text] },
                recordsTotal   : Localization.fetchByString(null, false).count(),
                recordsFiltered: Localization.fetchByString(params.searchString, false).count()] as JSON)
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.warn = message(code: 'default.not.found.message', args: [message(code: 'app.label.localization'), params.id])
                redirect action: "index"
            }
            '*' { render status: NOT_FOUND }
        }
    }

}
