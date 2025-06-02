package com.rxlogix.gateway

import grails.util.Holders

class AxwayMessage {

    String currentState
    String fileName
    Long deliveredTime
    String documentType
    Long linkedMessageId
    String direction
    String status

    static constraints = {
        fileName nullable: true
        linkedMessageId nullable: true
        status nullable: true
    }

    static mapping = {
        if (Holders.config.dataSources.axway) {
            datasource 'axway'
            id column: 'OID', generator: "assigned"
            table name: Holders.config.getProperty('icr.gateway.axway.table.name', 'AXWAY_MESSAGES')
        } else {
            id column: 'ID', generator: "sequence", params: [sequence: "AXWAY_MESSAGE_ID"]
            table name: 'AXWAY_MESSAGES'
        }
        version false
        deliveredTime column: 'DELIVEREDTIME'
        fileName column: 'CONSUMPTIONFILENAME'
        documentType column: 'DOCUMENTCLASS'
        linkedMessageId column: 'REFTOOID'
        direction column: 'DIRECTION'
        currentState column: 'CURRENTSTATETYPE'
        status column: 'STATUS'
    }

    static namedQueries = {
        getAckMessage { String ackFileName ->
            inList('documentType', ['XML', 'Binary', 'ACK'])
            eq('direction', 'Inbound')
            eq('currentState', 'Delivered')
            eq('fileName', ackFileName)
        }

        getTransmittedMessage { String fileName ->
            inList('documentType', ['XML', 'Binary', 'PDF'])
            eq('direction', 'Outbound')
            eq('currentState', 'Delivered')
            eq('fileName', fileName)
        }

        getMDNMessage { Long linkedMessageId ->
            eq('documentType', 'Receipt')
            eq('direction', 'Inbound')
            eq('currentState', 'Delivered')
            eq('linkedMessageId', linkedMessageId)
        }
    }

    transient Date getMsgDeliveryDateTime() {
        return (deliveredTime ? new Date(deliveredTime) : null)
    }
}
