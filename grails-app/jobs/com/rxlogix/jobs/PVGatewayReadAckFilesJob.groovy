package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class PVGatewayReadAckFilesJob {

    def gatewayIntegrationService

    static concurrent = false
    static group = "RxLogixPVR"

    static triggers = {
        cron name: 'PVGatewayReadAckFilesTrigger', startDelay: 5000, cronExpression: Holders.config.getProperty('pvgateway.read.ack.execution.cron.schedule', '0 20 * * * ? *') //every hour
    }

    @WithoutTenant
    def execute() {
        if (!Holders.config.getProperty('pvgateway.integrated', Boolean)) {
            return
        }
        try {
            gatewayIntegrationService.searchForAckFiles(Holders.config.getProperty('pvgateway.includedDownloadedFiles', Boolean))
        } catch (Exception e) {
            log.error("Error in PVGateway Read Ack Files job : ${e.getMessage()}")
        }
    }
}