package com.rxlogix

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rxlogix.config.*
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReasonOfDelayFieldEnum
import com.rxlogix.enums.ReasonOfDelayLateTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.grails.web.json.JSONArray
import org.springframework.security.access.annotation.Secured

import static org.springframework.http.HttpStatus.NOT_FOUND
import javax.persistence.EntityExistsException

@Secured(["isAuthenticated()"])
class ReasonOfDelayController {
    def reportExecutorService
    def reasonOfDelayService
    def CRUDService

    def rodMapping() {
    }

    def getLateMapping() {
        boolean hidden =  params.boolean('hidden')
        render(['lateJson': JSON.parse(getLateJson()), 'rootCauseJson': JSON.parse(getRootCauseJson(null, hidden)), 'rootCauseClassJson': JSON.parse(getRootCauseClassJson(null,hidden))] as JSON)
    }

    def getRootCauseMapping() {
        boolean hidden =  params.boolean('hidden')
        render(['rootCauseJson': JSON.parse(getRootCauseJson(null, hidden)), 'responsiblePartyJson': JSON.parse(getResponsiblePartyJson(null, hidden)), 'rootCauseSubJson': JSON.parse(getRootCauseSubCategoryJson(null, hidden))] as JSON)
    }

    def getRootCauseList(){
        render (['rootCauseJson': JSON.parse(getRootCauseJson(params.ownerApp, params.boolean('hidden')))] as JSON)
    }

    def getResponsiblePartyList(){
        render (['responsiblePartyJson': JSON.parse(getResponsiblePartyJson(params.ownerApp, params.boolean('hidden')))] as JSON)
    }

    def getRootCauseSubCategoryList() {
        render(['rootCauseSubCategoryJson': JSON.parse(getRootCauseSubCategoryJson(params.ownerApp, params.boolean('hidden')))] as JSON)
    }

    def getRootCauseClassList() {
        render(['rootCauseClassJson': JSON.parse(getRootCauseClassJson(params.ownerApp, params.boolean('hidden')))] as JSON)
    }

    String getLateJson(){
        List<Late> lateList = reportExecutorService.getLateList(params.boolean('hidden'))
        JSONArray lateArray = new JSONArray()
        lateList.each{
            if(it!=null) {
                Map objMap = [:]
                objMap['id'] = it.id
                objMap['textDesc'] = it.textDesc
                objMap['ownerApp'] = it.ownerApp
                objMap['linkedIds'] = it.rootCauseIds
                objMap['rootCauseClassIds'] = it.rootCauseClassIds
                objMap['type'] = message(code: ReasonOfDelayLateTypeEnum.lateTypeIdKeyMap.get(it.lateType)?.getI18nKey())
                objMap['typeId'] = it.lateType
                objMap['hiddenDate'] = it.hiddenDate
                lateArray.add((objMap as JSON))
            }
        }
        return lateArray.toString()
    }

    String getRootCauseJson(String ownerApp,boolean hidden){
        List<RootCause> rootCauseList = reportExecutorService.getRootCauseList(null,hidden)
        JSONArray rootCauseArray = new JSONArray()
        rootCauseList.each{
            if(it!=null && ((ownerApp == null) || (ownerApp == it.ownerApp))) {
                Map objMap = [:]
                objMap['id'] = it.id
                objMap['textDesc'] = it.textDesc
                objMap['ownerApp'] = it.ownerApp
                objMap['linkIds'] = it.linkIds
                objMap['rootCauseSubIds'] = it.rootCauseSubCategoryIds
                objMap['hiddenDate'] = it.hiddenDate
                rootCauseArray.add((objMap as JSON))
            }
        }
        return rootCauseArray.toString()
    }

    String getResponsiblePartyJson(String ownerApp,boolean hidden){
        List<ResponsibleParty> responsiblePartyList = reportExecutorService.getResponsiblePartyList(hidden)
        JSONArray responsiblePartyArray = new JSONArray()
        responsiblePartyList.each{
            if(it!=null && ((ownerApp == null) || (ownerApp == it.ownerApp))) {
                Map objMap = [:]
                objMap['id'] = it.id
                objMap['textDesc'] = it.textDesc
                objMap['ownerApp'] = it.ownerApp
                objMap['linkIds'] = it.linkIds
                objMap['hiddenDate'] = it.hiddenDate
                responsiblePartyArray.add((objMap as JSON))
            }
        }
        return responsiblePartyArray.toString()
    }

    String getRootCauseSubCategoryJson(String ownerApp,boolean hidden) {
        JSONArray json = new JSONArray()
        reportExecutorService.getRootCauseSubCategoryList(hidden).each {
            if (it != null && ((ownerApp == null) || (ownerApp == it.ownerApp))) {
                Map objMap = [:]
                objMap['id'] = it.id
                objMap['textDesc'] = it.textDesc
                objMap['ownerApp'] = it.ownerApp
                objMap['linkIds'] = it.linkIds
                objMap['hiddenDate'] = it.hiddenDate
                json.add((objMap as JSON))
            }
        }
        return json.toString()
    }

    String getRootCauseClassJson(String ownerApp,boolean hidden) {
        JSONArray json = new JSONArray()
        reportExecutorService.getRootCauseClassList(hidden).each {
            if (it != null && ((ownerApp == null) || (ownerApp == it.ownerApp))) {
                Map objMap = [:]
                objMap['id'] = it.id
                objMap['textDesc'] = it.textDesc
                objMap['ownerApp'] = it.ownerApp
                objMap['linkIds'] = it.linkIds
                objMap['hiddenDate'] = it.hiddenDate
                json.add((objMap as JSON))
            }
        }
        return json.toString()
    }

    String getLateJsonByApp(String ownerApp){
        Gson gson = new GsonBuilder().serializeNulls().create()
        boolean hidden = false
        String lateJson = gson.toJson(reportExecutorService.getLateList(hidden))
        return lateJson
    }

    def saveLate(){
        validateLabel()
        if(params.id){
            redirect (action: "editLate", params: params)
        }else {
            String lateLabel = params.label
            String ownerApp = params.ownerApp
            List<Long> rootCauseIds = toIdsList(params.list("mapping[]"))
            Long lateType = params.long('type')
            List<Long> rootCauseClassIds = toIdsList(params.list("rootCauseClass[]"))
            try {
                reasonOfDelayService.createLateMapping(lateLabel, rootCauseIds, ownerApp, lateType, rootCauseClassIds,params.boolean('hide'))
            } catch (EntityExistsException e) {
                log.error("Error occurred while saving late", e)
                response.status = 409;
                return
            }
            render "Ok"
        }
    }

    def saveRootCause(){
        validateLabel()
        if(params.id){
            redirect (action: "editRootCause", params: params)
        }else {
            String rootCauseLabel = params.label
            String ownerApp = params.ownerApp
            List<Long> responsiblePartyIds = toIdsList(params.list("mapping[]"))
            List<Long> rootCauseSubIds = toIdsList(params.list("rootCauseSub[]"))
            try {
                reasonOfDelayService.createRootCauseMapping(rootCauseLabel, responsiblePartyIds, ownerApp, rootCauseSubIds,params.boolean('hide'))
            } catch (EntityExistsException e) {
                log.error("Error occurred while saving root cause", e)
                response.status = 409;
                return
            }
            render "Ok"
        }
    }

    def saveResponsibleParty(){
        validateLabel()
        if(params.id){
            redirect (action: "editResponsibleParty", params: params)
        }else {
            String responsiblePartyLabel = params.label
            String ownerApp = params.ownerApp
            try {
                reasonOfDelayService.createResponsibleParty(responsiblePartyLabel, ownerApp,params.boolean('hide'))
            } catch (EntityExistsException e) {
                log.error("Error occurred while saving responsible party", e)
                response.status = 409;
                return
            }
            render "Ok"
        }
    }

    def saveRootCauseSub() {
        validateLabel()
        if (params.id) {
            redirect(action: "editRootCauseSub", params: params)
        } else {
            try {
                reasonOfDelayService.createRootCauseSub(params.label, params.ownerApp,params.boolean('hide'))
            } catch (EntityExistsException e) {
                log.error("Error occurred while saving Root Cause Sub-category", e)
                response.status = 409;
                return
            }
            render "Ok"
        }
    }

    def saveRootCauseClass() {
        validateLabel()
        if (params.id) {
            redirect(action: "editRootCauseClass", params: params)
        } else {
            try {
                reasonOfDelayService.createRootCauseClass(params.label, params.ownerApp,params.boolean('hide'))
            } catch (EntityExistsException e) {
                log.error("Error occurred while saving Root Cause Classification", e)
                response.status = 409;
                return
            }

            render "Ok"
        }
    }



    def hideWarning(Long id, String active) {
       def showWarning = reasonOfDelayService.hideWarning(id,active)
        if(showWarning){
            render true
        }else{
            render false
        }
    }


    def deleteLate(Long id, String ownerApp) {
        reasonOfDelayService.deleteLateMapping(id, ownerApp, params.deleteJustification)
        redirect action: "rodMapping"
    }

    def deleteRootCause(Long id, String ownerApp) {
        reasonOfDelayService.deleteRootCauseMapping(id, ownerApp, params.deleteJustification)
        redirect action: "rodMapping"
    }

    def deleteResponsibleParty(Long id, String ownerApp) {
        reasonOfDelayService.deleteResponsiblePartyMapping(id, ownerApp, params.deleteJustification)
        redirect action: "rodMapping"
    }

    def deleteRootCauseSub(Long id, String ownerApp) {
        reasonOfDelayService.deleteRootCauseSubMapping(id, ownerApp, params.deleteJustification)
        redirect action: "rodMapping"
    }

    def deleteRootCauseClass(Long id, String ownerApp) {
        reasonOfDelayService.deleteRootCauseClassMapping(id, ownerApp, params.deleteJustification)
        redirect action: "rodMapping"
    }

    def editLate(){
        validateLabel()
        boolean hide = params.boolean('hide')
        Long id = params.long('id')
        String lateLabel = params.label
        String ownerApp = params.ownerApp
        List<Long> rootCauseIds = toIdsList(params.list("mapping[]"))
        List<Long> rootCauseClassIds = toIdsList(params.list("rootCauseClass[]"))
        Long lateType = params.long('type')
        try {
            reasonOfDelayService.editLateMapping(id, lateLabel, rootCauseIds, ownerApp, lateType,rootCauseClassIds,hide)
        } catch (EntityExistsException e) {
            log.error("Error occurred while updating late", e)
            response.status = 409;
            return
        }
        render "Ok"
    }

    def editRootCause(){
        validateLabel()
        boolean hide = params.boolean('hide')
        Long id = params.long('id')
        String rootCauseLabel = params.label
        String ownerApp = params.ownerApp
        List<Long> responsiblePartyIds = toIdsList(params.list("mapping[]"))
        List<Long> rootCauseSubIds = toIdsList(params.list("rootCauseSub[]"))
        try {
            reasonOfDelayService.editRootCauseMapping(id, rootCauseLabel, responsiblePartyIds, ownerApp, rootCauseSubIds,hide)
        } catch (EntityExistsException e) {
            log.error("Error occurred while updating root cause", e)
            response.status = 409;
            return
        }
        render "Ok"
    }

    private List<Long> toIdsList(List<String> list) {
        return list?.collect { it as Long } ?: []
    }

    def editResponsibleParty(){
        validateLabel()
        boolean hide = params.boolean('hide')
        Long id = params.long('id')
        String responsiblePartyLabel = params.label
        String ownerApp = params.ownerApp
        try {
            reasonOfDelayService.editResponsiblePartyMapping(id, responsiblePartyLabel, ownerApp,hide)
        } catch (EntityExistsException e) {
            log.error("Error occurred while updating responsible party", e)
            response.status = 409;
            return
        }
        render "Ok"
    }

    def editRootCauseSub() {
        validateLabel()
        try {
            reasonOfDelayService.editRootCauseSubMapping(params.long('id'), params.label, params.ownerApp,params.boolean('hide'))
        } catch (EntityExistsException e) {
            log.error("Error occurred while updating Root Cause Sub-category", e)
            response.status = 409;
            return
        }
        render "Ok"
    }

    def editRootCauseClass() {
        validateLabel()
        try {
            reasonOfDelayService.editRootCauseClassMapping(params.long('id'), params.label, params.ownerApp,params.boolean('hide'))
        } catch (EntityExistsException e) {
            log.error("Error occurred while updating Root Cause Classification", e)
            response.status = 409;
            return
        }
        render "Ok"
    }

    def getRODLateTypeEnum(String ownerApp) {
        List lateTypeList = []
        ReasonOfDelayLateTypeEnum.values().each{
            if(it.ownerApp == ownerApp){
                lateTypeList.add([id: it.value(), text: message(code: it.getI18nKey())])
            }
        }
        render lateTypeList as JSON
    }

    void validateLabel() {
        if (!params.label || (params.label ==~ /.*[#;'<>"].*/)) throw new IllegalArgumentException("Label should not be empty and should not contain special characters!")
    }

    def addWorkflowState() {
        ReasonOfDelayAppEnum ownerApp = (ReasonOfDelayAppEnum) params.ownerApp
        ReasonOfDelayFieldEnum field = (ReasonOfDelayFieldEnum) params.field
        def workflowStateIds = params.workflowStateIds ? new JsonSlurper().parseText(params.workflowStateIds) : []
        boolean addMandatory = params.addMandatory ? Boolean.parseBoolean(params.addMandatory) : false
        boolean addEditable = params.addEditable ? Boolean.parseBoolean(params.addEditable) : false
        RCAMandatory rcaMandatory = RCAMandatory.findByOwnerAppAndField(ownerApp, field)
        if (!rcaMandatory) {
            notFound()
        } else {
            if (addMandatory)
                rcaMandatory.mandatoryInStates = []
            else if (addEditable)
                rcaMandatory.editableInStates = []
            if (workflowStateIds && workflowStateIds.size()>0) {
                workflowStateIds.each { it ->
                    if (addMandatory)
                        rcaMandatory.addToMandatoryInStates(WorkflowState.findById(Long.valueOf(it)))
                    else if (addEditable)
                        rcaMandatory.addToEditableInStates(WorkflowState.findById(Long.valueOf(it)))
                }
            }
            CRUDService.update(rcaMandatory)
        }
        render "ok"
    }

    def addEditableBy() {
        ReasonOfDelayAppEnum ownerApp = (ReasonOfDelayAppEnum) params.ownerApp
        ReasonOfDelayFieldEnum field = (ReasonOfDelayFieldEnum) params.field
        RCAMandatory rcaMandatory = RCAMandatory.findByOwnerAppAndField(ownerApp, field)
        def editableBy = params.editableBy ? new JsonSlurper().parseText(params.editableBy) : []
        if (!rcaMandatory) {
            notFound()
        } else {
            rcaMandatory.editableByGroups = []
            rcaMandatory.editableByUsers = []
            if (editableBy && editableBy!='') {
                editableBy.each { String  it->
                    if (it.startsWith(Constants.USER_GROUP_TOKEN)) {
                        UserGroup userGroup = UserGroup.get(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        rcaMandatory.addToEditableByGroups(userGroup)
                    } else if (it.startsWith(Constants.USER_TOKEN)) {
                        User user = User.get(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))
                        rcaMandatory.addToEditableByUsers(user)
                    }
                }
            }
            CRUDService.update(rcaMandatory)
        }
        render "ok"
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'auditLog.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
