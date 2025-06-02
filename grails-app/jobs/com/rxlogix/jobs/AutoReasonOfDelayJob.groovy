package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class AutoReasonOfDelayJob {

    def reportExecutorService

    static concurrent = false
    static group = "RxLogixPVR"

    static triggers = {
        cron name: 'AutoReasonOfDelayTrigger', startDelay: 300000, cronExpression: '0 0/1 * * * ? *'
    }

    @WithoutTenant
    def execute() {

        //check that the PV central is enabled or not
        if (!Holders.config.getProperty('pv.app.pvcentral.enabled', Boolean)) {
            return
        }

        //check that the we are showing the pvc module or not
        if (!Holders.config.getProperty('show.pvc.module', Boolean)) {
            return
        }

        try {
            //This method is used to assign the delays to the queries which qualify
            reportExecutorService.insertAutoRODIntoGttValues()
        } catch (Exception e) {
            log.error("Exception in Auto Reason of Delay Scheduled Job: ${e.message}")
        }
    }
}
