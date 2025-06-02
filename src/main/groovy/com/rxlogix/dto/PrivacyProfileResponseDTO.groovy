package com.rxlogix.dto

import com.rxlogix.Constants
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.apache.http.HttpStatus

@CompileStatic
@ToString(includes = ['status', 'message'])
class PrivacyProfileResponseDTO {
    int code
    String status
    String message

    void setFailureResponse(String message) {
        this.code = HttpStatus.SC_BAD_REQUEST
        this.status = Constants.FAILURE
        this.message = message
    }

    void setSuccessResponse(int code, String message) {
        this.code = code
        this.status = Constants.SUCCESS
        this.message = message
    }
}
