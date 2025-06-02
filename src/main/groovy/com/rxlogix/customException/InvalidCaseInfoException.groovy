package com.rxlogix.customException

import groovy.transform.CompileStatic

@CompileStatic
class InvalidCaseInfoException extends RuntimeException {

    InvalidCaseInfoException(String message) {
        super(message)
    }
}
