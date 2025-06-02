package com.rxlogix.dto

import grails.util.Holders
import groovy.transform.ToString
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.Errors

@ToString(includes = ['status', 'message'])
class ResponseDTO<T> {

    boolean status = true
    String message = ''
    T data

    void setFailureResponse(String message) {
        this.message = message
        this.status = false
    }

    void setFailureResponse(T data, String message) {
        this.message = message
        this.data = data
        this.status = false
    }

    void setFailureResponse(Exception ex) {
        this.message = ex.message
        this.status = false
    }

    void setFailureResponse(Errors errors) {
        this.status = false
        this.message = errors.allErrors.collect { error ->
            Holders.applicationContext.getBean("messageSource").getMessage(error, LocaleContextHolder.getLocale())
        }.join(";")
    }

    void setSuccessResponse(T data, String message = "") {
        this.message = message
        this.data = data
        this.status = true
    }

    ResponseDTO next(Closure closure) {
        if (status) {
            return closure(this)
        }
        return this
    }

    T fetchData() {
        this.status ? this.data : null
    }
}

