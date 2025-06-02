package com.rxlogix.dto

import grails.converters.JSON

class DataTableDTO {
    List aaData = []
    Integer recordsTotal
    Integer recordsFiltered
    int httpCode = 200
    String stackTrace
    String message = ''

    void setFailureResponse(Exception ex, String message = null) {
        this.message = message ?: ex.message
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        stackTrace = errors.toString()
        httpCode = 500
    }

    Integer getRecordsTotal() {
        if (this.recordsTotal == null) return aaData.size()
        recordsTotal
    }

    Integer getRecordsFiltered() {
        if (this.recordsFiltered == null) return aaData.size()
        recordsFiltered
    }

    def toAjaxResponse() {
        if (httpCode == 200)
            return [status: httpCode, contentType: "application/json", encoding: "UTF-8",
                    text  : [aaData: aaData, recordsTotal: getRecordsTotal(), recordsFiltered: getRecordsFiltered()] as JSON]
        else
            return [status: httpCode, contentType: "application/json", encoding: "UTF-8",
                    text  : [message: message, stackTrace: stackTrace] as JSON]
    }
}
