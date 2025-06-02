package com.rxlogix.publisher

import groovy.transform.CompileStatic

@CompileStatic
class PublisherContentTypeException extends Exception {
    PublisherContentTypeException(String message) {
        super(message)
    }
}
