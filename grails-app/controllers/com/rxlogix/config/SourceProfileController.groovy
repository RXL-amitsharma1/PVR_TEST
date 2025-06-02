package com.rxlogix.config

import com.rxlogix.enums.SourceProfileTypeEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class SourceProfileController {

    def CRUDService

    def index() {}

    def list() {
        def sourceProfiles = SourceProfile.findAllByIsDeletedAndSourceProfileTypeEnum(false,SourceProfileTypeEnum.SINGLE)?.collect {
            [
                    sourceProfileId: it.id,
                    sourceId       : it.sourceId,
                    sourceName     : it.sourceName,
                    sourceAbbrev   : it.sourceAbbrev,
                    isCental       : it.isCentral
            ]
        }
        response.status = 200
        render sourceProfiles as JSON
    }

    def edit(Long id) {
        SourceProfile sourceProfile = SourceProfile.findByIdAndIsDeleted(id, false)
        if (!sourceProfile) {
            notFound()
        }
        render view: "edit", model: [sourceProfileInstance: sourceProfile]
    }

    def update(Long id) {
        SourceProfile sourceProfile = SourceProfile.findByIdAndIsDeleted(id, false)
        if (!sourceProfile) {
            notFound()
            return
        }
        List<String> oldDateRangeValues = new ArrayList<String>()
        sourceProfile.dateRangeTypes.each{
            oldDateRangeValues.add(it.toString())
        }
        params.put('oldDateRangeValues', oldDateRangeValues)
        sourceProfile.dateRangeTypes.clear()
        bindData(sourceProfile, params, ["isCentral"])
        Boolean central = params.boolean("isCentral")
        try {
            if (central) {
                SourceProfile.findAllByIsCentralAndIsDeleted(true, false).collect {
                    it.isCentral = false
                    CRUDService.updateWithoutAuditLog(it)
                }
                sourceProfile.isCentral = true
            } else if (sourceProfile.isCentral && !central) {
                flash.error = message(code: "sourceProfile.select.one.central.source", default: "One Data Source must be select as the Central Source")
                render view: "edit", model: [sourceProfileInstance: sourceProfile]
                return
            }
            CRUDService.update(sourceProfile)
        } catch (ValidationException ve) {
            render view: "edit", model: [sourceProfileInstance: sourceProfile]
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'app.sourceProfile.label', default: 'Source Profile'), sourceProfile.sourceName])
        redirect(action: 'index')
    }

    def show(Long id) {
        SourceProfile sourceProfile = SourceProfile.findByIdAndIsDeleted(id, false)
        if (!sourceProfile) {
            notFound()
            return
        }
        render view: "show", model: [sourceProfileInstance: sourceProfile]
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.sourceProfile.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
