package com.rxlogix.config

import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReasonOfDelayFieldEnum
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class RCAMandatorySpec extends Specification implements DomainUnitTest<RCAMandatory> {

    def setup() {
    }

    RCAMandatory rcaMandatory
    def createNewRCAMandatory() {
        rcaMandatory = new RCAMandatory()
        rcaMandatory.id=1L
        rcaMandatory.ownerApp= ReasonOfDelayAppEnum.PVC
        rcaMandatory.field= ReasonOfDelayFieldEnum.Issue_Type
        rcaMandatory.mandatoryInStates = []
        rcaMandatory.editableInStates = []
        rcaMandatory.editableByUsers = []
        rcaMandatory.editableByGroups = []
    }

    void "test ownerApp cannot be null"() {
        given:
        createNewRCAMandatory()
        when:"OwnerApp equals value"
        rcaMandatory.ownerApp=value
        then:
        rcaMandatory.validate()==result
        where:
        value                            | result
        null                             | false
        ReasonOfDelayAppEnum.PVC         | true
        ReasonOfDelayAppEnum.PVQ         | true
        ReasonOfDelayAppEnum.PVC_Inbound | true
    }

    void "test Field cannot be null"() {
        given:
        createNewRCAMandatory()
        when:"Field equals value"
        rcaMandatory.field=value
        then:
        rcaMandatory.validate()==result
        where:
        value                                    | result
        null                                     | false
        ReasonOfDelayFieldEnum.Issue_Type        | true
        ReasonOfDelayFieldEnum.Root_Cause        | true
        ReasonOfDelayFieldEnum.Resp_Party        | true
        ReasonOfDelayFieldEnum.Corrective_Action | true
        ReasonOfDelayFieldEnum.Preventive_Action | true
        ReasonOfDelayFieldEnum.Corrective_Date   | true
        ReasonOfDelayFieldEnum.Preventive_Date   | true
        ReasonOfDelayFieldEnum.Investigation     | true
        ReasonOfDelayFieldEnum.Summary           | true
        ReasonOfDelayFieldEnum.Actions           | true
    }
}
