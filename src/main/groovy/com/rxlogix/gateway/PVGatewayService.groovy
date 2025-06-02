package com.rxlogix.gateway

import com.rxlogix.GatewayIntegrationService
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.PVGatewayStatusEnum
import com.rxlogix.util.DateUtil
import grails.gorm.transactions.Transactional
import java.util.concurrent.TimeUnit

@Transactional
class PVGatewayService {

    GatewayIntegrationService gatewayIntegrationService


    Date getMDNDate(String fileName) {
        Map data = gatewayIntegrationService.getTransactions(fileName)
        if (data) {
            Long mdnDate = data["mdnDate"]
            if (!mdnDate) {
                log.error("Couldn't find mdnDate from PV-Gateway")
                return null
            } else {
                long milliseconds = (long) TimeUnit.SECONDS.toMillis(mdnDate)
                return new Date(milliseconds)
            }
        }
        return null
    }


    Map getTransmitDateForFile(String fileName) {
        Map data = gatewayIntegrationService.getTransactions(fileName)
        if (data) {
            String status = data["status"]
            Long mdnDate = data["mdnDate"]
            if(status in PVGatewayStatusEnum.fetchSuccessfulTransmissionStatus()*.toString() && mdnDate) {
                long milliseconds = (long) TimeUnit.SECONDS.toMillis(mdnDate)
                return [status:IcsrCaseStateEnum.TRANSMITTED.toString(), date:new Date(milliseconds)]
            } else if(status in PVGatewayStatusEnum.fetchFailedTransmissionStatus()*.toString()) {
                return [status:IcsrCaseStateEnum.TRANSMISSION_ERROR.toString(), date: null]
            }
        }
        return null
    }


    Date getAckReceiveDateForFile(String ackFileName) {
        Map data = gatewayIntegrationService.getTransactions(ackFileName)
        if (data) {
            Long ackDate = data["date"]
            if (!ackDate) {
                log.error("Couldn't find ackDate from PV-Gateway")
                return null
            } else {
                long milliseconds = (long) TimeUnit.SECONDS.toMillis(ackDate)
                return new Date(milliseconds)
            }
        }
        return null
    }
}