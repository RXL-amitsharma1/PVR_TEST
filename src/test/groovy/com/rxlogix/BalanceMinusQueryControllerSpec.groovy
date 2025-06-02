package com.rxlogix

import com.rxlogix.config.SourceProfile
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class BalanceMinusQueryControllerSpec extends Specification implements DataTest, ControllerUnitTest<BalanceMinusQueryController> {

    List<SourceProfile> sourceProfiles

    def setupSpec() {
        mockDomain SourceProfile
    }

    def "test index success"(){
        given:
        def sourceProfile1 = new SourceProfile(id: 1, sourceName: 'testSource1', sourceAbbrev: 'ARG')
        sourceProfile1.save(validate: false);
        def sourceProfile2 = new SourceProfile(id: 2, sourceName: 'testSource2', sourceAbbrev: 'PVCM')
        sourceProfile2.save(validate: false);
        sourceProfiles = [sourceProfile1, sourceProfile2]

        when:
        params['sourceProfiles'] = sourceProfiles
        controller.index(sourceProfile1.id)

        then:
        response.status==200
    }

}
