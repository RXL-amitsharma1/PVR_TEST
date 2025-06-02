package com.rxlogix.util.marshalling

import com.rxlogix.response.ResponseAlertType
import grails.converters.JSON

class ResponseAlertTypeMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ResponseAlertType) { ResponseAlertType type ->
            type.getType()
        }
    }
}
