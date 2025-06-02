package com.rxlogix.customException


class ExecutionStatusException extends Exception {
    Long queryId
    Long templateId
    String sectionName
    String errorMessage
    String errorCause
    String reportSql
    String querySql
    String headerSql

    ExecutionStatusException() {}

    ExecutionStatusException(Map m) {
        super(m.errorMessage?.toString())
        this.queryId = m.queryId
        this.sectionName = m.sectionName
        this.templateId = m.templateId
        this.errorMessage = m.errorMessage
        this.errorCause = m.errorCause
        this.reportSql = m.reportSql
        this.querySql = m.querySql
        this.headerSql = m.headerSql
    }

    public ExecutionStatusException(String errorMessage, String errorCause) {
        super(errorMessage)
        this.errorMessage = errorMessage
        this.errorCause = errorCause
    }

    public ExecutionStatusException(Long templateId,  Long queryId, String sectionName, String errorMessage, String errorCause, String querySql, String reportSql, String headerSql) {
        super(errorMessage)
        this.queryId = queryId
        this.sectionName = sectionName
        this.templateId = templateId
        this.errorMessage = errorMessage
        this.errorCause = errorCause
        this.reportSql =  reportSql
        this.querySql = querySql
        this.headerSql = headerSql
    }
}
