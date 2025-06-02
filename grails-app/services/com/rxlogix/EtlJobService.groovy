package com.rxlogix

import com.rxlogix.config.AffiliateEtlStatus
import com.rxlogix.config.EtlSchedule
import com.rxlogix.config.EtlStatus
import com.rxlogix.config.PreMartEtlStatus
import com.rxlogix.customException.EtlUpdateException

import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.grails.core.exceptions.GrailsRuntimeException

import java.sql.Timestamp

@Transactional(readOnly = true)
class EtlJobService {

    def dataSource_pva
    def userService
    def emailService

    /**
     * Method to schedule the etl operation.
     * @param schedule
     * @return
     */
    def enable() {

        def sql =  new Sql(dataSource_pva)

        try {
            sql.call("{call PKG_ETL_JOB.p_enable_incr_job()}")
        } catch(Exception ex){
           log.error(ex.message)
           throw ex;
        }finally{
            if(sql) {
                sql.close()
            }
        }

    }

    /**
     * Method to update the etl schedule.
     * @param schedule
     * @return
     */
    def update(EtlSchedule schedule) {

        def sql =  new Sql(dataSource_pva)

        try {
            def start = DateUtil.StringToDate(schedule.startDateTime, Constants.DateFormat.WITHOUT_SECONDS)
            def startDate = new Timestamp(start.getTime());

            Timestamp currentTimestamp = new Timestamp(new Date().getTime());
            if (startDate.before(currentTimestamp)) {
                throw new EtlUpdateException()
            }

            //Passing the value 'N' as from app the etl will not be initial but incremental.
            //Since this is called from the update thus 0 is passed.
            sql.call("{call PKG_ETL_JOB.p_create_job(?, ?, ?, ?)}", [startDate, getRecurrenceForETL(schedule.repeatInterval), 'N', 0])
        }catch(Exception ex){
            log.error(ex.message)
            throw ex
        }finally{
            if(sql) {
                sql.close()
            }
        }
    }

    /**
     * Method called to execute the etl operation when the run now is clicked.
     * @param schedule
     * @return
     */
    def initialize(EtlSchedule schedule) {

        def sql =  new Sql(dataSource_pva)
        try {
            def start = DateUtil.StringToDate(schedule.startDateTime, Constants.DateFormat.WITHOUT_SECONDS)
            def startDate = new java.sql.Timestamp(start.getTime());
            println "etl start date :: "+startDate

            //Passing the value 'N' as from app the etl will not be initial but incremental.
            //Also the repeat interval parameter is sent as null.
            //Since this is called from run now flow thus 1 is passed.
            sql.call("{call PKG_ETL_JOB.p_create_job(?, ?, ?, ?)}", [startDate, null, 'N', 1])

        } catch(Exception ex){
            log.error(ex.message)
            throw ex
        } finally {
            if(sql) {
                sql.close()
            }
        }
    }

    def pauseEtl() {
        Sql sql =  new Sql(dataSource_pva)
        try {
            sql.execute("begin PKG_ETL_JOB.P_STOP_ETL; end;")
        } catch(Exception ex){
            log.error(ex.message)
            throw ex;
        }finally{
            sql?.close()
        }
    }

    def resumeEtl() {
        Sql sql =  new Sql(dataSource_pva)
        try {
            sql.execute("begin PKG_ETL_JOB.P_RESUME_ETL; end;")
        } catch(Exception ex){
            log.error(ex.message)
            throw ex;
        }finally{
            sql?.close()
        }
    }

    /**
     * Method to disable the etl schedule.
     * @param schedule
     * @return
     */
    def disable(EtlSchedule schedule) {
        def sql =  new Sql(dataSource_pva)

        try{
            sql.call("{call PKG_ETL_JOB.p_disable_job('N')}")
        } catch(Exception ex) {
            log.error(ex.message)
            throw ex
        } finally {
            if(sql) {
                sql.close()
            }
        }
    }

    /**
     * This method fetches the schedule from the pva database
     */
    EtlSchedule getSchedule(boolean isRunNow = false) {
        def sql =  new Sql(dataSource_pva)

        EtlSchedule etlSchedule = EtlSchedule.first()

        if (etlSchedule && isRunNow) {
            try {
                def rows = sql.rows("SELECT * from V_PVR_SCHEDULER_JOBS WHERE ETL_MODE='INCR'")
                rows.collect {
                    etlSchedule.scheduleName = it.SCHEDULE_NAME
                    etlSchedule.startDateTime = it.START_DATETIME
                    etlSchedule.repeatInterval = it.REPEAT_INTERVAL
                    etlSchedule.isDisabled = it.DISABLED
                    etlSchedule.isInitial = it.IS_INITIAL
                }
            } catch(Exception ex) {
                log.error(ex.message)
            } finally {
                if(sql) {
                    sql.close()
                }
            }
        }
       return etlSchedule
    }

    def lastSuccessfulEtlStartTime() {
        def sql =  new Sql(dataSource_pva)
        Timestamp startDateTime = null
        try {
            def rows = sql.rows("SELECT ETL_START_DATE as ETL_START_DATE from V_PVR_ETL_START_END_TIME")
            rows.collect {
                startDateTime = it.ETL_START_DATE
            }
        } catch(Exception ex) {
            log.error(ex.message)
        } finally {
            if(sql) {
                sql.close()
            }
        }
        return startDateTime
    }
    def getEtlStatus(){
        def etlStatus
        EtlStatus.withNewSession {
            etlStatus = EtlStatus.first()
        }
        etlStatus
    }

    def getAffiliateEtlStatus() {
        def affiliateEtlStatus
        AffiliateEtlStatus.withNewSession {
            affiliateEtlStatus = AffiliateEtlStatus.first()
        }
        affiliateEtlStatus
    }

    boolean checkPreMartEtlStatusApplicable() {
        Sql sql = new Sql(dataSource_pva)
        boolean val = false
        try {
            sql.rows("SELECT NVL(ETL_VALUE,0) as ETL_VALUE FROM PVR_ETL_CONSTANTS WHERE SOURCE_ID =1 AND ETL_KEY ='ENABLE_CONTINUOUS_ETL' AND SOURCE_ID =1").collect {
                val = it[0] != null && Integer.valueOf(it[0]) > 0 ? true : false
            }
        } catch (Exception ex) {
            log.error("Unable to get value while checking pre mart status applicable --> checkPreMartEtlStatusApplicable()",ex)
        } finally {
            sql?.close()
        }
        return val
    }

    boolean checkAffEtlStatusApplicable() {
        Sql sql = new Sql(dataSource_pva)
        boolean val = false
        try {
            def rows = sql.rows("SELECT count(*) as count FROM PVR_APP_SOURCE_INFO WHERE SOURCE_ABBRV = 'AFF'")
            val =  rows[0] != null && rows[0]?.getAt(0) as Integer ? true : false
        } catch (Exception ex) {
            log.error("Unable to get value while checking pre mart status applicable --> checkAffEtlStatusApplicable()",ex)
        } finally {
            sql?.close()
        }
        return val
    }


    def getPreMartEtlStatus() {
        def preMartEtlStatus
        PreMartEtlStatus.withNewSession {
            preMartEtlStatus = PreMartEtlStatus.first()
        }
        preMartEtlStatus
    }

    String getRecurrenceForETL(String recurrencePattern) throws Exception {
        if (recurrencePattern && !MiscUtil.validateRecurrence(recurrencePattern)) {
            throw new GrailsRuntimeException("### RepeatInterval isn't a valid recurrence pattern ###")
        }
//        Replacing COUNT = any_digit; or COUNT = any_digit end by blank as Oracle ETL doesn't handle COUNT
        return recurrencePattern?.replaceAll(~/COUNT=\d*($|;)/, "")
    }

    public sendEmailBasedOnStatus(EtlSchedule etlSchedule, String etlStatus) throws Exception {
        String[] emailTo = etlSchedule?.emailToUsers ? etlSchedule?.emailToUsers?.split(",") : []
        String[] emailCc = null
        if(etlSchedule?.emailConfiguration){
            emailCc = etlSchedule?.emailConfiguration?.cc ? etlSchedule?.emailConfiguration?.cc?.split(",") : []
        }
        String emailSubject = etlSchedule?.emailConfiguration?.subject ? emailService.insertValues(etlSchedule?.emailConfiguration?.subject, etlSchedule) : "[pv-reports] ETL Monitoring Status : \"${etlStatus}\""
        String emailBody = etlSchedule?.emailConfiguration?.body ? emailService.insertValues(etlSchedule?.emailConfiguration?.body, etlSchedule) : ViewHelper.getMessage("app.emailService.elt."+etlStatus.toLowerCase()+".default.email.body")
        if (etlSchedule?.emailConfiguration?.body) {
            emailBody = emailBody
        } else {
            emailBody += "<br><br>" + ViewHelper.getMessage("app.label.thanks") + "," + "<br>" + ViewHelper.getMessage("app.label.pv.reports")
        }
        emailService.sendEmailWithFiles(emailTo, emailCc, emailSubject, emailBody, true, null)
    }

}
