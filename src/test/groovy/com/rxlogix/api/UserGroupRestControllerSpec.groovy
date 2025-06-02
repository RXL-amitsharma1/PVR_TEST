package com.rxlogix.api


import com.rxlogix.user.FieldProfile
import com.rxlogix.user.UserGroup
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([UserGroup])
class UserGroupRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<UserGroupRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains FieldProfile, UserGroup
    }

    void "test index name matches search"(){
        UserGroup userGroup = new UserGroup(name: "userGroup",description: "description",createdBy: "user",fieldProfile: new FieldProfile(name: "fieldProfile"))
        userGroup.save(failOnError:true,validate:false,flush:true)
        UserGroup.metaClass.static.findUserGroupBySearchString = { String search, String sortBy, String sortDirection = "asc"->
            new Object() {
                List list(Object o) {
                    return [[userGroup]]
                }

                int count() {
                    return 1
                }
            }
        }
        when:
        params.searchString = "fieldProfile"
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        response.json.aaData[0].size() == 7
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test index with no search"(){
        UserGroup userGroup = new UserGroup(name: "fieldProfile",description: "description",createdBy: "user")
        userGroup.save(failOnError:true,validate:false,flush:true)
        UserGroup.metaClass.static.findUserGroupBySearchString = { String search, String sortBy, String sortDirection = "asc"->
            new Object() {
                List list(Object o) {
                    return [[userGroup]]
                }

                int count() {
                    return 1
                }
            }
        }
        when:
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        response.json.aaData[0].size() == 7
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }
}
