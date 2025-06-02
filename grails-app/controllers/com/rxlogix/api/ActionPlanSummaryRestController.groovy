package com.rxlogix.api


import com.rxlogix.config.*
import com.rxlogix.enums.ActionItemFilterEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

import javax.xml.bind.ValidationException

@Secured('permitAll')
class ActionPlanSummaryRestController extends RestfulController implements SanitizePaginationAttributes {

    def CRUDService
    def userService

    ActionPlanSummaryRestController() {
        super(ActionPlanSummary)
    }

    def index() {
        //date intervals intersection  <=> (StartA <= EndB) and (EndA >= StartB)
        Date from = DateUtil.getDateWithDayStartTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.from))
        Date to = DateUtil.getDateWithDayEndTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.to))

        List<ActionPlanSummary> summaryList = params.boolean("all") ? ActionPlanSummary.findAllByParentEntityKeyAndIsDeleted(params.parentEntityKey, false) :
                ActionPlanSummary.findAllByParentEntityKeyAndIsDeletedAndFromLessThanEqualsAndToGreaterThanEquals(params.parentEntityKey, false, to, from)
        List out = summaryList.collect {
            [id     : it.id,
             date   : it.from.format(DateUtil.DATEPICKER_FORMAT) + " - " + it.to.format(DateUtil.DATEPICKER_FORMAT),
             current: ((DateUtil.getDateWithDayStartTime(it.from) == from) && (DateUtil.getDateWithDayEndTime(it.to) == to))
            ]
        }.sort {
            (it.current ? -Long.MAX_VALUE : -it.id)
        }
        render(out as JSON)
    }

    def save() {
        Date from = DateUtil.getDateWithDayStartTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.from))
        Date to = DateUtil.getDateWithDayEndTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.to))
        CRUDService.save(new ActionPlanSummary(from: from, to: to, parentEntityKey: params.parentEntityKey, textData: params.text))
        render "ok"
    }

    def delete() {
        ActionPlanSummary actionPlanSummary = ActionPlanSummary.get(params.long("id"))
        CRUDService.softDelete(actionPlanSummary, "")
        render "ok"
    }

    def update() {
        try{
            ActionPlanSummary s = ActionPlanSummary.get(params.long("id"))
            s.textData = params.text
            if(!s.textData){
                log.error("Validation exception while updating action plan")
                render([error:true, msg : message(code : "app.actionPlan.summaryText.blank")] as JSON)
                return
            }else if(s.textData.length()>4000){
                log.error("Validation exception while updating action plan")
                render([error:true, msg : message(code: "com.rxlogix.config.actionPlan.summary.maxSize.exceeded")] as JSON)
                return
            }
            CRUDService.save(s)
        } catch (Exception e){
            render(status: 500, msg: message(code : "default.system.error.message"))
        }
        render([success: true] as JSON)
    }

    def summary() {
        ActionPlanSummary s = ActionPlanSummary.get(params.long("id"))
        Map out = [
                id  : s.id,
                from: s.from.format(DateUtil.DATEPICKER_FORMAT),
                to  : s.to.format(DateUtil.DATEPICKER_FORMAT),
                text: s.textData
        ]
        render(out as JSON)
    }

}
