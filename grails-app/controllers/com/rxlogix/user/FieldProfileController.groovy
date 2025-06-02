package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.util.ViewHelper
import javax.xml.ws.Response

import static org.springframework.http.HttpStatus.*

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class FieldProfileController {

    def reportFieldService
    def userService
    def CRUDService
    def importService
    def signalIntegrationService
    GrailsApplication grailsApplication

    def index() {}

    /**
     * This action is responsible got rendering the create action.
     * @return
     */
    def create() {
        [fieldProfileInstance: new FieldProfile(), reportFieldGroupList: ReportFieldGroup.findAllByIsDeleted(false, [sort: 'name'])]
    }

    /**
     * This action is responsible to show the field profile.
     * @return
     */
    def show(FieldProfile fieldProfile) {
        if (!fieldProfile) {
            notFound()
            return
        }

        List<String> reportFieldGroups = FieldProfile.fetchReportFieldGroups(fieldProfile)

        Map<String, String> fieldGroupNameMap = [:]

        reportFieldGroups.each {
            fieldGroupNameMap[it] = ViewHelper.getMessage("app.reportFieldGroup.${it}") ?: "app.reportFieldGroup.${it}"
        }

        render view: "show", model: [fieldProfileInstance: fieldProfile, fieldGroupNames: fieldGroupNameMap]
    }

    /**
     * This action is responsible to edit the field profile.
     * @return
     */
    def edit(FieldProfile fieldProfile) {
        if (!fieldProfile) {
            notFound()
            return
        }
        if (fieldProfile.name == Holders.config.getProperty("pvadmin.privacy.field.profile")) {
            return privacyProfileAccessDenied(fieldProfile.name, Constants.EDIT_METHOD)
        }

        render view: 'edit', model: [fieldProfileInstance: fieldProfile, reportFieldGroupList: ReportFieldGroup.findAllByIsDeleted(false, [sort: 'name'])]
    }

    /**
     * This action is responsible to delete the field profile.
     * @return
     */
    def delete(FieldProfile fieldProfile) {
        if (!fieldProfile) {
            notFound()
            return
        }
        if (fieldProfile.name == Holders.config.getProperty("pvadmin.privacy.field.profile")) {
            return privacyProfileAccessDenied(fieldProfile.name, Constants.DELETE_METHOD)
        }
        User currentUser = userService.getUser()
        if (!currentUser.isAdmin()) {
            flash.warn = message(code: "app.fieldProfile.delete.permission", args: [fieldProfile.name])
            redirect(view: "index")
            return
        }
        try {
            CRUDService.softDelete(fieldProfile, fieldProfile.name, params.deleteJustification)
            List<UserGroup> userGroupList = UserGroup.findAllByFieldProfileAndIsDeleted(fieldProfile, false)
            userGroupList.each { usergroup ->
                usergroup.fieldProfile = null
                usergroup.save(flush: true)
                if (grailsApplication.config.pvsignal.url)
                    signalIntegrationService.updateBlindedDataToSignal(usergroup)
            }
            UserGroup.executeUpdate("update UserGroup set fieldProfile=null where fieldProfile.id = :id", [id: fieldProfile.id])
            userService.updateBlindedFlagForUsersAndGroups()
            clearCacheForQueryReportField(fieldProfile)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.field.profile'), fieldProfile.name])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.field.profile'), fieldProfile.name])
                    redirect(action: "show", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }
    }

    /**
     * This action is responsible to save the field profile.
     * @return
     */
    def save() {
        FieldProfile fieldProfile = new FieldProfile()
        populateModel(fieldProfile)
        try {
            CRUDService.save(fieldProfile)
            populateReportFields(fieldProfile)
            userService.updateBlindedFlagForUsersAndGroups()
            userService.updateProtectedFlagForUsersAndGroups()
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.field.profile', default: 'Field Profile'), fieldProfile?.name])}"
            redirect(action: "index")
        } catch (ValidationException ve) {
            log.warn("Validation Error during fieldProfile -> save")
            render view: "create", model: [fieldProfileInstance: fieldProfile, reportFieldGroupList: ReportFieldGroup.findAllByIsDeleted(false, [sort: 'name']), checkBoxParameter: checkBoxParameter()]
            return;
        } catch (Exception ex) {
            log.error("Error during fieldProfile -> save", ex)
            flash.error = "${message(code: 'app.label.fieldProfile.save.exception')}"
            render view: "create", model: [fieldProfileInstance: fieldProfile, reportFieldGroupList: ReportFieldGroup.findAllByIsDeleted(false, [sort: 'name']), checkBoxParameter: checkBoxParameter()]
        }
    }

    /**
     * This action is to update the field profile.
     * @return
     */
    def update(FieldProfile fieldProfile) {
        if (!fieldProfile) {
            notFound()
            return
        }
        populateModel(fieldProfile)
        fieldProfile.lastUpdated = new Date()
        try {
            CRUDService.update(fieldProfile)
            populateReportFields(fieldProfile)
            userService.updateBlindedFlagForUsersAndGroups()
            userService.updateProtectedFlagForUsersAndGroups()
            clearCacheForQueryReportField(fieldProfile)
            if (grailsApplication.config.pvsignal.url) {
                List<UserGroup> userGroupList = UserGroup.findAllByFieldProfile(fieldProfile)
                userGroupList.each {
                    signalIntegrationService.updateBlindedDataToSignal(it)
                }
            }
            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.field.profile.label', default: 'Field Profile'), fieldProfile?.name])}"
            redirect(action: "index")
        } catch (ValidationException ve) {
            log.warn("Validation Error during fieldProfile -> update")
            render view: 'edit', model: [fieldProfileInstance: fieldProfile, reportFieldGroupList: ReportFieldGroup.findAllByIsDeleted(false, [sort: 'name']), checkBoxParameter: checkBoxParameter()]
        } catch (Exception ex) {
            log.error("Error during fieldProfile -> update", ex)
            flash.error = "${message(code: 'app.label.fieldProfile.save.exception')}"
            render view: 'edit', model: [fieldProfileInstance: fieldProfile, reportFieldGroupList: ReportFieldGroup.findAllByIsDeleted(false, [sort: 'name']), checkBoxParameter: checkBoxParameter()]
        }
    }

    private notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.field.profile.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private privacyProfileAccessDenied(String profileName, String method) {
        request.withFormat {
            form {
                flash.error = message(code: 'privacy.profile.non.editable', args: [method, profileName])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    def ajaxReportFieldByGroup() {
        String reportGroupName = params.name
        reportGroupName = reportGroupName?.replaceAll('groupName-', '')?.replaceAll('___', ' ') ?: ''
        ReportFieldGroup reportFieldGroup = ReportFieldGroup.findByNameAndIsDeleted(reportGroupName, false)
        List<ReportField> reportFields = reportFieldService.getReportFields(reportFieldGroup)
        render template: 'reportFieldDisplay', model: [reportFields: reportFields]
    }

    def exportJson() {
        List fieldProfilesJson = []
        FieldProfile.fetchAllFieldProfileBySearchString(null).list().each { FieldProfile fieldProfile ->
            Map propertiesMap = MiscUtil.getObjectProperties(fieldProfile)
            propertiesMap.put('blindedFields', FieldProfileFields.findAllByFieldProfileAndIsBlinded(fieldProfile, true).collect {it.reportField}*.name)
            propertiesMap.put('protectedFields', FieldProfileFields.findAllByFieldProfileAndIsProtected(fieldProfile, true).collect {it.reportField}*.name)
            propertiesMap.put('hiddenFields', FieldProfileFields.findAllByFieldProfileAndIsHidden(fieldProfile, true).collect {it.reportField}*.name)
            fieldProfilesJson.add(propertiesMap)
        }
        def contentType = "application/octet-stream"
        def filename = "fieldProfiles.json"
        response.setHeader("Content-Disposition", "attachment;filename=${filename}")
        render(contentType: contentType, text: fieldProfilesJson as JSON)
    }

    def importJson() {
        try {
            MultipartFile file = request.getFile('importJSONFile')
            importService.importFieldProfilesJson(file.inputStream.text)
            flash.message = message(code: 'fieldprofiles.json.import.success')
        } catch (Exception ex) {
            log.error("Failed to upload field profile json.", ex)
            flash.error = message(code: 'fieldprofiles.json.import.failure')
        }
        redirect(action: 'index')
    }

    private void populateModel(FieldProfile fieldProfile) {
        bindData(fieldProfile, params, [exclude: ['blindedReportFields', 'protectedReportFields', 'hiddenReportFields']])
    }

    private List checkBoxParameter() {
        List checkBoxParams = []
        for (String key in params.keySet()) {
            if ('on' == params.get(key)) {
                checkBoxParams.add(key)
            }
        }
        checkBoxParams
    }

    private void clearCacheForQueryReportField(FieldProfile fieldProfile) {
        List<UserGroup> groups = UserGroup.findAllByFieldProfile(fieldProfile)
        if (groups && UserGroupUser.countByUserAndUserGroupInList(userService.getUser(), groups)) {
            log.info("Clearing cache for QUERY Report field group (For updation in Field Profile - ${fieldProfile?.name})")
            reportFieldService.clearCacheReportFields()
        }
    }

    @Transactional
    private void populateReportFields(FieldProfile fieldProfile) {

        FieldProfileFields.executeUpdate("delete from FieldProfileFields where fieldProfile.id = :fp", [fp: fieldProfile.id])

        //Bind Report field checkboxes
        params.blindedReportFields.split(",").findAll { it?.isLong() }?.collate(Constants.MAX_LIST_SIZE_DB)?.each { List<String> blindedReportFieldList ->
            ReportField.findAllByIdInListAndIsDeleted(blindedReportFieldList.collect { it as Long }, false)?.each {
                userService.addToFieldsWithFlag(fieldProfile, it, true, false, false)
            }
        }

        params.protectedReportFields.split(",").findAll { it?.isLong() }?.collate(Constants.MAX_LIST_SIZE_DB)?.each { List<String> protectedReportFieldList ->
            ReportField.findAllByIdInListAndIsDeleted(protectedReportFieldList.collect { it as Long }, false)?.each {
                userService.addToFieldsWithFlag(fieldProfile, it, false, true, false)
            }
        }

        params.hiddenReportFields.split(",").findAll { it?.isLong() }?.collate(Constants.MAX_LIST_SIZE_DB)?.each { List<String> protectedReportFieldList ->
            ReportField.findAllByIdInListAndIsDeleted(protectedReportFieldList.collect { it as Long }, false)?.each {
                userService.addToFieldsWithFlag(fieldProfile, it, false, false, true)
            }
        }
    }

    Response loadFieldProfileData(Long id, String groupName) {
        FieldProfile fieldProfile = FieldProfile.get(id)
        if (!fieldProfile) {
            render status: NOT_FOUND
            return
        }

        List<Map> allFields = FieldProfile.fetchReportFields(fieldProfile, groupName)
        Map<Long, String> fieldNameMap = [:]

        allFields?.each {
            fieldNameMap[it.id] = ViewHelper.getMessage("app.reportField.${it.name}") ?: "app.reportField.${it.name}"
        }

        render ([reportFields: allFields, fieldNameMap: fieldNameMap] as JSON)
    }
}
