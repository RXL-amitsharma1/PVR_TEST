package com.rxlogix.jobs

import com.rxlogix.Constants
import com.rxlogix.EtlJobService
import com.rxlogix.api.ConfigurationRestController
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.EtlSchedule
import com.rxlogix.config.EtlStatus
import com.rxlogix.enums.EtlStatusEnum
import com.rxlogix.util.DateUtil
import grails.util.Holders
import org.quartz.CronTrigger
import java.util.concurrent.TimeUnit

import static com.rxlogix.api.ConfigurationRestController.*

class EtlScheduleMonitoringJob {
    def etlJobService
    def reportExecutorService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'EtlScheduleMonitoringTrigger', startDelay: 5000, cronExpression: Holders.config.getProperty('etl.monitoring.cron.schedule', '0 0/15 * * * ? *'), misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
    }

    def execute() {
        try{
            EtlSchedule etlSchedule = etlJobService.getSchedule()
            EtlStatus etlStatus = (EtlStatus)etlJobService.getEtlStatus()
            if(etlStatus.getStatus().equals(EtlStatusEnum.FAILED)){
                etlSchedule?.emailTriggerForLongRunning = true
                boolean isemailTriggerFailure = etlSchedule.getEmailTrigger()
                if(isemailTriggerFailure) {
                    etlSchedule?.emailTrigger = false
                    etlJobService.sendEmailBasedOnStatus(etlSchedule, etlStatus.getStatus().toString())
                }
            }else if(etlStatus.getStatus().equals(EtlStatusEnum.SUCCESS)){
                etlSchedule?.emailTriggerForLongRunning = true
                boolean isSendSuccessEmailChecked = etlSchedule.getSendSuccessEmail() ? true : false
                boolean isemailTriggerSuccess = etlSchedule.getEmailTrigger()
                if(isSendSuccessEmailChecked && isemailTriggerSuccess){
                    etlSchedule?.emailTrigger = false
                    etlJobService.sendEmailBasedOnStatus(etlSchedule, etlStatus.getStatus().toString())
                }
            }else if(etlStatus.getStatus().equals(EtlStatusEnum.RUNNING)){
                etlSchedule?.emailTrigger = true
                Date startDate = etlJobService?.lastSuccessfulEtlStartTime() ? new Date(etlJobService?.lastSuccessfulEtlStartTime()?.getTime()) : null
                Date currentTimestamp = new Date()
                if (startDate?.before(currentTimestamp)) {
                    long diffInMillies = currentTimestamp.getTime() - startDate?.getTime()
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies)
                    if ((minutes > etlSchedule.getSendEmailETLInterval()) && etlSchedule?.emailTriggerForLongRunning) {
                        log.info("ETL is long running. Sending email with status.")
                        etlJobService.sendEmailBasedOnStatus(etlSchedule, etlStatus.getStatus().toString())
                        etlSchedule?.emailTriggerForLongRunning = false
                    }
                }
            }
        }catch(Exception e){
            log.error("Exception in ActionItemTasksJob: ${e.message}")
        }
    }
}

