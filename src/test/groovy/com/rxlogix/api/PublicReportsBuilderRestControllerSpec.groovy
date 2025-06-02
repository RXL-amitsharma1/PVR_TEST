package com.rxlogix.api

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.dto.reports.integration.ExecutedConfigurationDTO
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.public_api.PublicReportsBuilderRestController
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

import static org.apache.http.HttpStatus.SC_OK

class PublicReportsBuilderRestControllerSpec extends Specification implements ControllerUnitTest<PublicReportsBuilderRestController> {


    def setup() {
        GroovyMock(User, global: true)
        User.findByUsernameIlike(_) >> new User(username: "testUser", fullName: "Test User")

    }

    void "test importConfiguration method with empty request"() {
        given:
        controller.reportsBuilderService = [createExecutedConfiguration: { ExecutedConfigurationDTO executedConfigurationDTO ->
            throw new ValidationException("Validation Exception", new User().errors)
        }]
        GroovyMock(User, global: true)
        when:
        controller.importConfiguration()

        then:
        response.status == SC_OK
        response.json.message
        response.json.status == false
        response.json.data == null
    }


    void "test importConfiguration method"() {
        given:
        // Mock the service methods
        controller.reportsBuilderService = [
                createExecutedConfiguration: { ExecutedConfigurationDTO executedConfigurationDTO ->
                    def ex = new ExecutedConfiguration()
                    ex.id = 12  // Ensure this ID is set and will be returned in response
                    return ex
                },
                createExecutionStatus: { ExecutedConfiguration executedConfiguration, ExecutingEntityTypeEnum executingEntityTypeEnum, String callbackUrl ->
                    // Assuming the method is just for setup, no changes required
                }
        ]

        // Mocking request body
        request.JSON = '{"property1": 1, "property2": 2}'

        when:
        controller.importConfiguration()

        then:
        response.status == SC_OK
        response.json.data == 12  // Ensure 'data' matches the ID set in createExecutedConfiguration
        response.json.status == true
        !response.json.message
    }
}
