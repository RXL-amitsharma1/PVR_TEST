package com.rxlogix

import com.rxlogix.config.CorrectiveAction
import com.rxlogix.config.PreventativeAction
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.springframework.security.access.annotation.Secured

@Secured(["ROLE_SYSTEM_CONFIGURATION"])
class CapaController {
    def reportExecutorService
    def capaService

    def capaList() {
    }

    def getCorrectiveMapping(){
        List<CorrectiveAction> correctiveActionList = reportExecutorService.getCorrectiveActionList()
        JSONArray correctiveActionArray = new JSONArray()
        correctiveActionList.each{
            if(it!=null) {
                Map objMap = [:]
                objMap['id'] = it['CORRECTIVE_ACTION_ID']
                objMap['textDesc'] = it['TEXT_DESC']
                objMap['ownerApp'] = it['OWNER']
                correctiveActionArray.add((objMap as JSON))
            }
        }
        render (['correctiveActionJson': JSON.parse(correctiveActionArray.toString())] as JSON)
    }

    def getPreventativeMapping(){
        List<PreventativeAction> preventativeActionList = reportExecutorService.getPreventativeActionList()
        JSONArray preventativeActionArray = new JSONArray()
        preventativeActionList.each{
            if(it!=null) {
                Map objMap = [:]
                objMap['id'] = it["PREVENTATIVE_ACTION_ID"]
                objMap['textDesc'] = it['TEXT_DESC']
                objMap['ownerApp'] = it['OWNER']
                preventativeActionArray.add((objMap as JSON))
            }
        }
        render (['preventativeActionJson': JSON.parse(preventativeActionArray.toString())] as JSON)
    }

    def saveCAPA(){
        validateLabel()
        if(params.id){
            capaService.editCAPA(params.long('id'), params.textDesc, params.ownerApp, params.long('capaType'))
        }else{
            capaService.createCAPA(params.textDesc, params.ownerApp, params.long('capaType'))
        }
        render 'Ok'
    }

    def deleteCorrective(Long id,String ownerApp){
        capaService.deleteCAPA(id, 0L,ownerApp)
        redirect action: 'capaList'
    }

    def deletePreventative(Long id,String ownerApp){
        capaService.deleteCAPA(id, 1L,ownerApp)
        redirect action: 'capaList'
    }

    void validateLabel() {
        if (!params.textDesc || (params.textDesc ==~ /.*[#;'<>"].*/)) throw new IllegalArgumentException("Label should not be empty and should not contain special characters!")
    }
}
