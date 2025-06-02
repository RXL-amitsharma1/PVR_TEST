package com.rxlogix


import grails.gorm.multitenancy.Tenants
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

@DisallowConcurrentExecution
@Slf4j
class ReportExecutionKillJob implements Job {

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Tenants.withId(Holders.config.getProperty('pvreports.multiTenancy.allTenant', Integer)) {
            ReportExecutorService reportExecutorService = Holders.applicationContext.getBean("reportExecutorService")
            try {
                reportExecutorService.killReportExecution()
            }
            catch (Exception e) {
                log.error("Exception in ReportExecutionKillJob", e)
            }
        }
    }
}
