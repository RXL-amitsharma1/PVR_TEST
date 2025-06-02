package com.rxlogix.config


import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class WorkflowStateSpec extends Specification implements DomainUnitTest<WorkflowState> {

    def setup() {
    }

    def cleanup() {
    }


    void "test the workflow state persistence"() {

        setup:
        def workflowState = new WorkflowState([name : "New", description : "Test description", display : true, createdBy: "TestUser",
            modifiedBy: "TestUser", finalState : false])
        when:
        workflowState.save()
        then:
        workflowState.id != null

    }

    void "test the workflow state persistence with validation errors"() {

        setup:
        def workflowState = new WorkflowState()
        when:
        workflowState.save()
        then:
        workflowState.id == null
    }
}
