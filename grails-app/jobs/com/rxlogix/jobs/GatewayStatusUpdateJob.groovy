package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class GatewayStatusUpdateJob {
    def icsrProfileAckService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'GatewayStatusUpdateTrigger', startDelay: 5000, cronExpression: Holders.config.getProperty('icsr.ack.gateway.status.cron.schedule', '0 20 * * * ? *')
        // each hour
    }

    @WithoutTenant
    def execute() {
        if (!Holders.config.getProperty('show.xml.option', Boolean)) {
            return
        }

        if (!Holders.config.getProperty('icsr.profiles.execution', Boolean)) {
            return
        }

        try {
            icsrProfileAckService.updateStatusesFromGateway()
        } catch (Exception e) {
            log.error("Exception in GatewayStatusUpdateJob: ${e.message}")
        }
    }
}
