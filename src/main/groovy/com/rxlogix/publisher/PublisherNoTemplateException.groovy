package com.rxlogix.publisher

import groovy.transform.CompileStatic

@CompileStatic
class PublisherNoTemplateException extends Exception {
    PublisherNoTemplateException(String message) {
        super(message)
    }
}
