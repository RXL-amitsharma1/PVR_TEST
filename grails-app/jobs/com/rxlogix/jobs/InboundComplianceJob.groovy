package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class InboundComplianceJob {
    def reportExecutorService

    static concurrent = false
    static group = "RxLogixPVR"

    static triggers = {
        cron name: 'InboundComplianceTrigger', startDelay: 300000, cronExpression: '0 0 0 ? * * *'
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
            reportExecutorService.executeInboundCompliance()
        } catch (Exception e) {
            log.error("Exception in Inbound Compliance Job: ${e.message}")
        }
    }
}
