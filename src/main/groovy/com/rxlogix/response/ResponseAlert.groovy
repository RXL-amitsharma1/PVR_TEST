package com.rxlogix.response

class ResponseAlert {
    ResponseAlertType type
    String message
    String messageCode

    ResponseAlert(ResponseAlertType type, String message, String messageCode = null) {
        this.type = type
        this.message = message
        this.messageCode = messageCode
    }
}
