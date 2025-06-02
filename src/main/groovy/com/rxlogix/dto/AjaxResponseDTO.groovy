package com.rxlogix.dto

import com.rxlogix.UserService
import com.rxlogix.response.ResponseAlert
import com.rxlogix.response.ResponseAlertType
import grails.converters.JSON
import grails.util.Holders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.Errors

class AjaxResponseDTO<T> extends ResponseDTO<T>{
    private static Logger logger = LoggerFactory.getLogger(getClass())
    int httpCode = 200
    String stackTrace = ''
    String additionalData = ''

    List alerts = []

    static AjaxResponseDTO success() {
        AjaxResponseDTO response = new AjaxResponseDTO()
        response.status = true
        response
    }

    AjaxResponseDTO<T> withData(T data) {
        this.data = data
        this
    }

    static AjaxResponseDTO unsuccess() {
        AjaxResponseDTO response = new AjaxResponseDTO()
        response.status = false
        response
    }

    void setFailureResponse(String message, int _httpCode = 500) {
        super.setFailureResponse(message)
        this.httpCode = _httpCode
    }

    void setFailureResponse(Exception ex, String message = null, int _httpCode = 500) {
        this.message = message ?: ex.message
        this.status = false
        UserService userService
        try {
            userService = Holders.applicationContext.getBean("userService")
        } catch (Exception e) {
            logger.error("Unexpected error fetching bean userService", e)
        }
        if (userService?.isCurrentUserDev()) {
            StringWriter errors = new StringWriter()
            ex.printStackTrace(new PrintWriter(errors))
            this.stackTrace = errors.toString()
        } else {
            if (this.message && this.message == ex.message) {
                //setting stackTrace empty in case of message set as ex.message to exclude text duplication
                this.stackTrace = ''
            } else {
                this.stackTrace = ex.message
            }
        }
        this.httpCode = _httpCode
        this.additionalData = additionalData
    }

    def toAjaxResponse() {
        [status: httpCode, contentType: "application/json", encoding: "UTF-8", additionalData: additionalData, text: this as JSON]
    }

    def toJsonAjaxResponse() {
        [success: this.status, data: this.data, alerts: this.alerts] as JSON
    }

    void setFailureResponse(Errors errors, int _httpCode) {
        this.httpCode = _httpCode
        this.status = false
        this.message = errors.allErrors.collect { error ->
            Holders.applicationContext.getBean("messageSource").getMessage(error, LocaleContextHolder.getLocale())
        }.join(";")
    }

    @Override
    void setFailureResponse(Errors errors) {
        this.httpCode = 500
        super.setFailureResponse(errors)
    }

    AjaxResponseDTO withSuccessAlert(String messageText) {
        this.alerts << new ResponseAlert(ResponseAlertType.SUCCESS, messageText)
        this
    }

    AjaxResponseDTO withDangerAlert(String messageText) {
        this.alerts << new ResponseAlert(ResponseAlertType.DANGER, messageText)
        this
    }

    AjaxResponseDTO withWarningAlert(String messageText) {
        this.alerts << new ResponseAlert(ResponseAlertType.WARNING, messageText)
        this
    }

    AjaxResponseDTO withInfoAlert(String messageText) {
        this.alerts << new ResponseAlert(ResponseAlertType.INFO, messageText)
        this
    }

    AjaxResponseDTO withSuccessAlertCode(String messageCode) {
        this.alerts << new ResponseAlert(ResponseAlertType.SUCCESS, null, messageCode)
        this
    }

    AjaxResponseDTO withDangerAlertCode(String messageCode) {
        this.alerts << new ResponseAlert(ResponseAlertType.DANGER, null, messageCode)
        this
    }

    AjaxResponseDTO withWarningAlertCode(String messageCode) {
        this.alerts << new ResponseAlert(ResponseAlertType.WARNING, null, messageCode)
        this
    }

    AjaxResponseDTO withInfoAlertCode(String messageCode) {
        this.alerts << new ResponseAlert(ResponseAlertType.INFO, null, messageCode)
        this
    }
}

