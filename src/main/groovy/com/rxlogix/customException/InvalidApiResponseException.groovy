package com.rxlogix.customException

import groovy.transform.CompileStatic

@CompileStatic
class InvalidApiResponseException extends RuntimeException {

    InvalidApiResponseException(String api, int statusCode) {
        super("Invalid response code: ${statusCode} received from api: ${api}")
    }

}
