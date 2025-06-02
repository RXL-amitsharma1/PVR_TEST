package com.rxlogix.customException

import grails.validation.ValidationErrors
import spock.lang.Specification

class OwnershipExceptionSpec extends Specification {

    def "test OwnershipException"() {
        given: OwnershipException ownershipException
        ValidationErrors validationError = new ValidationErrors(new String())

        when: ownershipException = new OwnershipException(className, name, msg, validationError)

        then:
        ownershipException.className == className
        ownershipException.name == name

        where:
        className     | name        | msg
        "Test Classs" | "Test Name" | "Test Msg"
    }
}