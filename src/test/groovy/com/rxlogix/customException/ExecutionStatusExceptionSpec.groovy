package com.rxlogix.customException

import spock.lang.Specification

class ExecutionStatusExceptionSpec extends Specification {

    def "test executionStatusExceptionError"() {
        given: ExecutionStatusException executionStatusException

        when: executionStatusException = new ExecutionStatusException(errorMessage, errorCause)

        then:
            executionStatusException.errorMessage == errorMessage
            executionStatusException.errorCause == errorCause

        where:

        errorMessage            |   errorCause
        "Test Error Message"    |   "Test Error Cause"
    }

    def "test executionStatusException"() {
        given: ExecutionStatusException executionStatusException

        when: executionStatusException = new ExecutionStatusException(templateId, queryId, sectionName, errorMessage, errorCause, querySql, reportSql, headerSql)

        then:
        executionStatusException.templateId == templateId
        executionStatusException.queryId == queryId
        executionStatusException.sectionName == sectionName
        executionStatusException.errorMessage == errorMessage
        executionStatusException.errorCause == errorCause
        executionStatusException.querySql == querySql
        executionStatusException.reportSql == reportSql
        executionStatusException.headerSql == headerSql

        where:

        templateId | queryId | sectionName    | errorMessage          | errorCause         | querySql    | reportSql    | headerSql
        101L       | 102L    | "Test Section" | "Test Error Message"  | "Test Error Cause" | "Sql Query" | "Report Sql" | "Header Sql"
    }
}