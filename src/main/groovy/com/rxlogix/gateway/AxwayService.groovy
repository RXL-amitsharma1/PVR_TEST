package com.rxlogix.gateway

import com.rxlogix.enums.IcsrCaseStateEnum
import grails.gorm.transactions.Transactional
import grails.util.Holders


@Transactional
class AxwayService {

    def CRUDService


    Date getMDNDate(String r3FileName) {
        if (!Holders.config.getProperty('icr.gateway.axway.active', Boolean)) {
            return null
        }
        AxwayMessage axwayMessage = AxwayMessage.getTransmittedMessage(r3FileName).get()
        if (axwayMessage) {
            return AxwayMessage.getMDNMessage(axwayMessage.id).get()?.msgDeliveryDateTime
        }
        return null
    }


    Map getTransmitDateForFile(String r3FileName) {
        if (!Holders.config.getProperty('icr.gateway.axway.active', Boolean)) {
            return null
        }
        AxwayMessage axwayMessage = AxwayMessage.getTransmittedMessage(r3FileName).get()
        Long linkedMessage = AxwayMessage.getMDNMessage(axwayMessage.id).count()
        String status = axwayMessage?.status
        if (axwayMessage && linkedMessage && status == "TRANSMITTED") {
            return [status: IcsrCaseStateEnum.TRANSMITTED, date: axwayMessage.msgDeliveryDateTime]
        } else if (status == "TRANSMISSION_ERROR") {
            return [status: IcsrCaseStateEnum.TRANSMISSION_ERROR, date: null]
        }
        return null
    }


    Date getAckReceiveDateForFile(String ackFileName){
        if (!Holders.config.getProperty('icr.gateway.axway.active', Boolean)) {
            return null
        }
        AxwayMessage axwayMessage = AxwayMessage.getAckMessage(ackFileName).get()
        if (axwayMessage) {
            return axwayMessage.msgDeliveryDateTime
        }
        return null
    }

    def setTransmitDateForFile(String fileName, String status){
        AxwayMessage axwayMessage = AxwayMessage.findByFileName(fileName)
        if(axwayMessage != null){
            axwayMessage.deliveredTime = System.currentTimeMillis()
            axwayMessage.status = status
            def axwayMessage1 = AxwayMessage.findByLinkedMessageId(axwayMessage.id)
            axwayMessage1.deliveredTime = System.currentTimeMillis()
            axwayMessage1.status = status
            CRUDService.update(axwayMessage1)
            CRUDService.update(axwayMessage)
        } else {
            String documentType = ''
            if(fileName.toLowerCase().endsWith('.xml')) {
                documentType = 'XML'
            } else if(fileName.toLowerCase().endsWith('.pdf')) {
                documentType = 'PDF'
            } else if(fileName.toLowerCase().endsWith('.bin') || fileName.toLowerCase().endsWith('.exe')) {
                documentType = 'Binary'
            }
            axwayMessage = new AxwayMessage(documentType: documentType, direction: 'Outbound', currentState: 'Delivered', fileName: fileName, linkedMessageId: null, deliveredTime: System.currentTimeMillis(), status: status).save(flush: true, failOnError: true)
            new AxwayMessage(documentType: 'Receipt', direction: 'Inbound', currentState: 'Delivered', fileName: null , linkedMessageId: axwayMessage.id, deliveredTime: System.currentTimeMillis(), status: status).save(flush: true, failOnError: true)
        }
    }

    def setAckReceiveDateForFile(String ackfileName, String status){
        AxwayMessage axwayMessage  = AxwayMessage.findByFileName(ackfileName)
        if(axwayMessage != null && axwayMessage.direction == 'Inbound'){
            axwayMessage.deliveredTime = System.currentTimeMillis()
            axwayMessage.status = status
            CRUDService.update(axwayMessage)
        } else {
            String documentType = ''
            if(ackfileName.toLowerCase().endsWith('.xml')) {
                documentType = 'XML'
            } else if(ackfileName.toLowerCase().endsWith('.ack')) {
                documentType = 'ACK'
            } else if(ackfileName.toLowerCase().endsWith('.bin') || ackfileName.toLowerCase().endsWith('.exe')) {
                documentType = 'Binary'
            }
            new AxwayMessage(documentType: documentType, direction: 'Inbound', currentState: 'Delivered', fileName: ackfileName, linkedMessageId: null, deliveredTime: System.currentTimeMillis(), status: status).save(flush: true, failOnError: true)
        }
    }
}
