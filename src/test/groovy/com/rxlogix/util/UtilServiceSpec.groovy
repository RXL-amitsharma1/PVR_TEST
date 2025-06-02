package com.rxlogix.util

import com.rxlogix.CRUDService
import com.rxlogix.ReportFieldService
import com.rxlogix.UserService
import com.rxlogix.UtilService
import com.rxlogix.config.ReportField
import com.rxlogix.dto.PrivacyProfileResponseDTO
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.transform.CompileDynamic
import spock.lang.Specification

@CompileDynamic
class UtilServiceSpec extends Specification implements DataTest, ServiceUnitTest<UtilService> {
    
    protected void setupSpec() {
        mockDomains FieldProfile, ReportField, UserGroup, UserGroupUser, User
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue ="", String transactionId = "" -> }
        UserGroup.metaClass.static.findAllByFieldProfile(_) >> {[]}
    }

    def "test updatePrivacyFieldProfile success"() {
        given:
        Holders.config.pvsignal.url = ""
        FieldProfile privacyFieldProfile = new FieldProfile(name: "Privacy Profile Test").save(flush: true, validate: false, failOnError: true)
        FieldProfile.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return privacyFieldProfile
        }

        ReportField reportField = new ReportField(name: "test", isDeleted: false)
        ReportField.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return reportField
        }

        ReportFieldService mockReportFieldService = Mock(ReportFieldService)
        mockReportFieldService.getFieldVariableForUniqueId(_ as String, _ as String, _ as Long) >> { return "test" }
        service.reportFieldService = mockReportFieldService

        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.instantUpdateWithoutAuditLog(_) >> { return privacyFieldProfile }
        service.CRUDService = mockCRUDService

        UserService mockUserService = Mock(UserService)
        mockUserService.updateBlindedFlagForUsersAndGroups() >> {}
        mockUserService.updateProtectedFlagForUsersAndGroups() >> {}
        service.userService = mockUserService
        
        String fields = '{"privacyFieldList":[{"fieldId":"test","tenantId":1,"langId":"en"}]}'

        when:
        PrivacyProfileResponseDTO responseMap = service.updatePrivacyFieldProfile(JSON.parse(fields) as Map<String, List<Map>>, privacyFieldProfile)

        then:
        responseMap.code == 200
        responseMap.status == "SUCCESS"
        responseMap.message == "Privacy Field Profile updated successfully"
    }

    def "test validateFieldJSON Invalid JSON"() {
        given:
        String fields = '{"privacyFieldList":[{"tenantId":1,"langId":"en"}]}'

        when:
        String missingKey = service.validateFieldJSON(JSON.parse(fields) as List<Map>)

        then:
        missingKey == 'fieldId'
    }

    def "test validateFieldJSON valid JSON"() {
        given:
        String fields = '{"privacyFieldList":[{"fieldId":"test","tenantId":1,"langId":"en"}]}'

        when:
        String missingKey = service.validateFieldJSON(JSON.parse(fields) as List<Map>)

        then:
        missingKey == null
    }

    def "test updatePrivacyFieldProfile partial content success"() {
        given:
        Holders.config.pvsignal.url = ""
        FieldProfile privacyFieldProfile = new FieldProfile(name: "Privacy Profile Test").save(flush: true, validate: false, failOnError: true)
        FieldProfile.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return privacyFieldProfile
        }

        ReportField reportField = new ReportField(name: "test1", isDeleted: false)
        ReportField.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return name == "test1" ? reportField : null
        }

        ReportFieldService mockReportFieldService = Mock(ReportFieldService)
        mockReportFieldService.getFieldVariableForUniqueId(_ as String, _ as String, _ as Long) >> { String fieldId, String langId, Long tenantId ->
            return fieldId
        }
        service.reportFieldService = mockReportFieldService

        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.instantUpdateWithoutAuditLog(_) >> { return privacyFieldProfile }
        service.CRUDService = mockCRUDService

        UserService mockUserService = Mock(UserService)
        mockUserService.updateBlindedFlagForUsersAndGroups() >> {}
        mockUserService.updateProtectedFlagForUsersAndGroups() >> {}
        service.userService = mockUserService

        String fields = '{"privacyFieldList":[{"fieldId":"test1","tenantId":1,"langId":"en"},{"fieldId":"test2","tenantId":1,"langId":"en"}]}'

        when:
        PrivacyProfileResponseDTO responseMap = service.updatePrivacyFieldProfile(JSON.parse(fields) as Map<String, List<Map>>, privacyFieldProfile)

        then:
        responseMap.code == 206
        responseMap.status == "SUCCESS"
        responseMap.message == "Privacy Field Profile updated successfully, 1 report fields either not found or are deleted in PVR"
    }

    def "test updatePrivacyFieldProfile cache gets cleared"() {
        given:
        Holders.config.pvsignal.url = ""
        FieldProfile privacyFieldProfile = new FieldProfile(name: "Privacy Profile Test").save(flush: true, validate: false, failOnError: true)
        FieldProfile.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return privacyFieldProfile
        }

        ReportField reportField = new ReportField(name: "test", isDeleted: false)
        ReportField.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return reportField
        }

        User user = new User(username: "test.user", fullName: "test user").save(flush: true, validate: false, failOnError: true)

        UserGroup userGroup = new UserGroup(name: "test group", fieldProfile: privacyFieldProfile).save(flush: true, validate: false, failOnError: true)
        UserGroup.metaClass.static.findAllByFieldProfile = { FieldProfile fieldProfile ->
            return [userGroup]
        }

        UserGroupUser.metaClass.static.countByUserAndUserGroupInList = { User user1, List userGroups ->
            return 1
        }

        ReportFieldService mockReportFieldService = Mock(ReportFieldService)
        mockReportFieldService.getFieldVariableForUniqueId(_ as String, _ as String, _ as Long) >> { return "test" }
        service.reportFieldService = mockReportFieldService

        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.instantUpdateWithoutAuditLog(_) >> { return privacyFieldProfile }
        service.CRUDService = mockCRUDService

        UserService mockUserService = Mock(UserService)
        mockUserService.updateBlindedFlagForUsersAndGroups() >> {}
        mockUserService.updateProtectedFlagForUsersAndGroups() >> {}
        mockUserService.getUser() >> { return user }
        service.userService = mockUserService

        String fields = '{"privacyFieldList":[{"fieldId":"test","tenantId":1,"langId":"en"}]}'

        when:
        PrivacyProfileResponseDTO responseMap = service.updatePrivacyFieldProfile(JSON.parse(fields) as Map<String, List<Map>>, privacyFieldProfile)

        then:
        1 * mockReportFieldService.clearCacheReportFields()
    }
}
