package com.rxlogix

import com.rxlogix.config.AffiliateEtlStatus
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.EtlSchedule
import com.rxlogix.config.EtlStatus
import com.rxlogix.config.PreMartEtlStatus
import com.rxlogix.customException.EtlUpdateException
import com.rxlogix.enums.EtlStatusEnum
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class EtlScheduleController implements SanitizePaginationAttributes{

     def CRUDService
    def etlJobService
    def userService

    def index() {
        def etlSchedule = null
        def etlStatus = null
        def preMartEtlStatus = null
        boolean isPreMartStatusApplicable = false
        boolean isAffEtlStatusApplicable = false
        def affEtlStatus = null
        try {
            etlSchedule = etlJobService.getSchedule()
            //Fetch the etl status and pass in the params map.
            etlStatus = etlJobService.getEtlStatus()

            isPreMartStatusApplicable = etlJobService.checkPreMartEtlStatusApplicable()
            if(isPreMartStatusApplicable) {
                preMartEtlStatus = etlJobService.getPreMartEtlStatus()
            }

            isAffEtlStatusApplicable = etlJobService.checkAffEtlStatusApplicable()
            if(isAffEtlStatusApplicable) {
                affEtlStatus = etlJobService.getAffiliateEtlStatus()
            }

        } catch (Exception ex) {
            log.error("Exception occurred while fetching ETL information and status of mart and pre mart --> index()",ex)
            flash.error = message(code: "app.etl.exception.label")
        }
        render view: "index", model: [etlScheduleInstance: etlSchedule,
                                      isPreMartStatusApplicable: isPreMartStatusApplicable,
                                      preMartEtlStatus: preMartEtlStatus?.status,
                                      preMartLastRunDateTime: preMartEtlStatus?.lastRunDateTime,
                                      isAffEtlStatusApplicable : isAffEtlStatusApplicable,
                                      affEtlStatus : affEtlStatus?.status,
                                      affEtlLastRunDateTime: affEtlStatus?.lastRunDateTime,
                                      etlStatus: etlStatus?.status,
                                      lastRunDateTime: etlStatus?.lastRunDateTime]
    }

    def edit() {
        def etlScheduleInstance = etlJobService.getSchedule()

        if(etlScheduleInstance.isDisabled) {
            render view: "enable", model: [etlScheduleInstance: etlScheduleInstance]
        }else{
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstance]
        }

    }

    def update(EtlSchedule etlScheduleInstance) {
        if (!etlScheduleInstance) {
            notFound()
            return
        }

        def etlScheduleInstancePre = etlScheduleInstance

        if(etlScheduleInstancePre.startDateTime < getCurrentDate()){
            flash.error = message(code: "update.start.date.time.etl")
            redirect action: "edit"
            return
        }

        try {
            bindData(etlScheduleInstance, params, [exclude: ["emailToUsers", "emailConfiguration", "sendSuccessEmail", "pauseLongRunningETL", "sendEmailETLInterval", "emailTrigger", "emailTriggerForLongRunning"]])
            if(isEtlRunning()){
                flash.error = message(code: "etl.running.update.request.failed")
                redirect action: "edit"
                return
            }
            etlJobService.update(etlScheduleInstance)
            etlScheduleInstance = bindEmailConfiguration(etlScheduleInstance)
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)
        } catch (grails.validation.ValidationException ve) {
            etlScheduleInstance.errors = ve.errors
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstancePre]
            return
        } catch (EtlUpdateException ex) {
            flash.error = message(code: "modify.schedule.request.invalid.date")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstancePre]
            return
        } catch(Exception ve) {
            flash.error = message(code: "modify.schedule.request.failed")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstancePre]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'etlSchedule.label'), etlScheduleInstance.scheduleName])
                redirect action: "index"
            }

        }
    }

    private boolean isEtlRunning() {
        EtlStatus etlStatus = etlJobService.getEtlStatus()
        if(etlStatus.status.equals(EtlStatusEnum.RUNNING)){
            return true
        }
        boolean isAffEtlStatusApplicable = etlJobService.checkAffEtlStatusApplicable()
        if(isAffEtlStatusApplicable) {
            AffiliateEtlStatus affEtlStatus = etlJobService.getAffiliateEtlStatus()
            if(affEtlStatus.status.equals(EtlStatusEnum.RUNNING)){
                return true
            }
        }
        boolean isPreMartStatusApplicable = etlJobService.checkPreMartEtlStatusApplicable()
        if(isPreMartStatusApplicable) {
            PreMartEtlStatus preMartEtlStatus = etlJobService.getPreMartEtlStatus()
            if(preMartEtlStatus.status.equals(EtlStatusEnum.RUNNING)){
                return true
            }
        }
        return false
    }

    private EtlSchedule bindEmailConfiguration(EtlSchedule etlScheduleInstance) {
        EmailConfiguration emailConfiguration = new EmailConfiguration()
        if(params.emailConfiguration.subject){
            bindData(emailConfiguration, params.emailConfiguration)
            CRUDService.saveWithoutAuditLog(emailConfiguration)
            etlScheduleInstance.emailConfiguration = emailConfiguration
        }else{
            etlScheduleInstance.emailConfiguration = null
        }
        if(params.emailToUsers){
            if (params.emailToUsers instanceof String) {
                etlScheduleInstance.emailToUsers = params.emailToUsers
            } else {
                etlScheduleInstance.emailToUsers = params.emailToUsers.join(",")
            }
        }else {
            etlScheduleInstance.emailToUsers = null
        }
        return etlScheduleInstance
    }

    def disable() {
        def etlScheduleInstance = etlJobService.getSchedule()

        etlScheduleInstance.isDisabled = true;

        try {
            EtlStatus etlstatus = etlJobService.getEtlStatus()
            if(etlstatus.status.equals(EtlStatusEnum.RUNNING)){
                etlScheduleInstance.isDisabled = false
                flash.error = message(code: "etl.running.disable.schedule.request.failed")
                render view: "edit", model: [etlScheduleInstance: etlScheduleInstance]
                return
            }
            etlJobService.disable(etlScheduleInstance)
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)

        } catch (Exception ve) {
            etlScheduleInstance.isDisabled = false
            flash.error = message(code: "disable.schedule.request.failed")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstance]
            return
        }



        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'etlSchedule.label'), etlScheduleInstance.scheduleName])
                redirect action: "index"
            }


        }

    }

    def enable(EtlSchedule etlScheduleInstance) {

        etlScheduleInstance = etlJobService.getSchedule()

        //Change the isDisabled flag to false.
        etlScheduleInstance.isDisabled = false;
        

        try {
            etlJobService.enable()
            etlScheduleInstance = bindEmailConfiguration(etlScheduleInstance)
            if(getCurrentDate() > etlScheduleInstance.startDateTime){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat.WITHOUT_SECONDS)
                simpleDateFormat.timeZone = TimeZone.getTimeZone( "${userService.currentUser?.preference?.timeZone}" )
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.MINUTE, 1);
                etlScheduleInstance.startDateTime = simpleDateFormat.format(cal.getTime());
            }
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)
        } catch (Exception ve) {
            etlScheduleInstance.isDisabled = true;
            render view: "enable", model: [etlScheduleInstance: etlScheduleInstance]
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.enabled.message', args: [message(code: 'etlSchedule.label'), etlScheduleInstance.scheduleName])
                redirect action: "index"
            }

        }
    }

    def initialize() {

        def etlScheduleInstance = etlJobService.getSchedule()

        EtlSchedule etlSchedule = EtlSchedule.first()

        etlSchedule?.emailTrigger = true

        etlScheduleInstance.isInitial = false;

        try {
            etlJobService.initialize(etlScheduleInstance)
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)
            AuditLogConfigUtil.logChanges(etlScheduleInstance, [state: "${message(code: 'app.etl.run.message')}"], [state: ""], Constants.AUDIT_LOG_UPDATE)
        } catch (Exception ve) {
            flash.error = message(code: "request.to.run.initial.etl.failed")
            redirect action: "index"
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.etlinitialized.message')
                redirect action: "index"
            }

        }
    }

    def pauseEtl() {
        try {
            etlJobService.pauseEtl()
            EtlSchedule etlSchedule = new EtlSchedule()
            AuditLogConfigUtil.logChanges(etlSchedule, [state: "${message(code: 'app.etl.pause.message')} (Justification: ${params.pauseJustification})"], [state: ""], Constants.AUDIT_LOG_UPDATE, "ETL")
            etlSchedule.discard()
        } catch (Exception ve) {
            render status: 500, contentType: "application/json", text: '{"success": false, "error": "An error occurred."}'
        }
        render status: 200, contentType: "application/json", text: '{"success": true}'
    }

    def resumeEtl() {
        try {
            etlJobService.resumeEtl()
            EtlSchedule etlSchedule = new EtlSchedule()
            AuditLogConfigUtil.logChanges(etlSchedule, [state: "${ViewHelper.getMessage('app.etl.resume.message')}"], [state: ""], Constants.AUDIT_LOG_UPDATE, "ETL")
            etlSchedule.discard()
        } catch (Exception ve) {
            flash.error = message(code: "etl.request.to.resumed.failed")
            redirect action: "index"
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'etl.resumed.success.message')
                redirect action: "index"
            }
        }

    }

    def getEtlStatus() {
        def etlStatus = etlJobService.getEtlStatus()
        def statusJson = [
                "status": etlStatus?.status?.name(),
                "statusValue": etlStatus?.status?.value()
        ]
        response.status = 200
        render statusJson as JSON;
    }

    def getPreMartEtlStatus() {
        boolean isPreMartStatusApplicable = etlJobService.checkPreMartEtlStatusApplicable()
        def preMartEtlStatus = null
        if(isPreMartStatusApplicable) {
            preMartEtlStatus = etlJobService.getPreMartEtlStatus()
        }
        def statusJson = [
                "isPreMartStatusApplicable": isPreMartStatusApplicable,
                "preMartEtlStatus": preMartEtlStatus?.status?.name(),
                "preMartEtlStatusValue": preMartEtlStatus?.status?.value()
        ]
        response.status = 200
        render statusJson as JSON;
    }

    def getAffiliateEtlStatus() {
        boolean isAffEtlStatusApplicable = etlJobService.checkAffEtlStatusApplicable()
        def affEtlStatus = null
        if(isAffEtlStatusApplicable) {
            affEtlStatus = etlJobService.getAffiliateEtlStatus()
        }
        def statusJson = [
                "isAffEtlStatusApplicable": isAffEtlStatusApplicable,
                "affEtlStatus": affEtlStatus?.status?.name(),
                "affEtlStatusValue": affEtlStatus?.status?.value()
        ]
        response.status = 200
        render statusJson as JSON;
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'etlSchedule.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private String getCurrentDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat.WITHOUT_SEC_TZ)
        simpleDateFormat.timeZone = TimeZone.getTimeZone( "${userService.currentUser?.preference?.timeZone}" )
        Date date = new Date()
        return simpleDateFormat.format(date)
    }

    private notSaved() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.saved.message')
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

}
