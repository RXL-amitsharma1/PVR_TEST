package com.rxlogix.publisher

import groovy.transform.CompileStatic

@CompileStatic
class PublisherPermissionException extends Exception {
    PublisherPermissionException(String message) {
        super(message)
    }
}
