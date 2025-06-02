package com.rxlogix.api

import com.rxlogix.config.ReportRequest
import com.rxlogix.user.FieldProfile
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([FieldProfile])
class FieldProfileRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<FieldProfileRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomain FieldProfile
    }

    void "test index name matches search"(){
        FieldProfile fieldProfile = new FieldProfile(name: "fieldProfile",description: "description",createdBy: "user")
        fieldProfile.save(failOnError:true,validate:false,flush:true)
        FieldProfile.metaClass.static.fetchAllFieldProfileBySearchString = { String search -> return fieldProfile}
        when:
        params.searchString = "fieldProfile"
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        response.json.aaData[0].size() == 6
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test index with no search"(){
        FieldProfile fieldProfile = new FieldProfile(name: "fieldProfile",description: "description",createdBy: "user")
        fieldProfile.save(failOnError:true,validate:false,flush:true)
        FieldProfile.metaClass.static.fetchAllFieldProfileBySearchString = { String search -> return fieldProfile}
        when:
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        response.json.aaData[0].size() == 6
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }
}
