package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.mapping.CaseInfo
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.grails.web.json.JSONElement
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, CaseInfo])
class ImportServiceSpec extends Specification implements DataTest, ServiceUnitTest<ImportService> {

    @Shared List parsedshareWithUsers
    @Shared List parsedTagsshareWithGroups
    @Shared List parsedTags
    @Shared String queryJSON
    @Shared String customSQLQueryJSON
    @Shared User user

    def setupSpec() {
        mockDomains Tenant, Role, User, UserRole, Query, QueryExpressionValue, CustomSQLValue, CustomSQLQuery,ReportField
    }

    def setup() {
        user = makeDevUser()
        parsedTags = []
        parsedshareWithUsers = []
        parsedTagsshareWithGroups = []

        queryJSON = """{
           "queryType":{
              "enumType":"com.rxlogix.enums.QueryTypeEnum",
              "name":"QUERY_BUILDER"
           },
           "nameWithDescription":"PVR-ST-003-001 Query  - Owner: Admin User",
           "ownerId":17,
           "modifiedBy":"admin",
           "instanceIdentifierForAuditLog":"PVR-ST-003-001 Query",
           "factoryDefault":false,
           "description":null,
           "name":"PVR-ST-003-001 Query",
           "tags":[        ],
           "queryExpressionValues":[        ],
           "parameterSize":0,
           "nonValidCases":false,
           "deletedCases":false,
           "icsrPadderAgencyCases":false,
           "JSONQuery":"{ \\"all\\": { \\"containerGroups\\": [   { \\"expressions\\": [  { \\"index\\": \\"0\\", \\"field\\": \\"studyStudyNum\\", \\"op\\": \\"EQUALS\\", \\"value\\": \\"Rx-STDID001\\" }  ] }   ] } }",
           "originalQueryId":0,
           "lastUpdated":"2016-05-19T17:39:46Z",
           "isDeleted":false,
           "reassessListedness":null,
           "hasBlanks":false,
           "createdBy":"dev",
           "qualityChecked":true,
           "owner":{
              "id":17,
              "username":"admin",
              "fullName":"Admin User"
           },
           "dateCreated":"2016-05-19T17:39:46Z"
        }""".stripIndent()

        customSQLQueryJSON = """{
           "nameWithDescription":"Custom Country = JP (Custom SQL Query) - Owner: Dev User",
           "queryType":{
              "enumType":"com.rxlogix.enums.QueryTypeEnum",
              "name":"CUSTOM_SQL"
           },
           "ownerId":15,
           "modifiedBy":"dev",
           "instanceIdentifierForAuditLog":"Custom Country = JP-Morett CustomSqlValues",
           "factoryDefault":false,
           "customSQLValues":[
              {
                 "class":"com.rxlogix.config.CustomSQLValue",
                 "id":5096,
                 "isFromCopyPaste":false,
                 "key":":country",
                 "value":null
              }
           ],
           "description":"Custom SQL Query",
           "customSQLQuery":"Join VW_LCO_COUNTRY lco on (cm.OCCURED_COUNTRY_ID = lco.COUNTRY_ID) where ((UPPER(COUNTRY) = UPPER(':country')))",
           "name":"Custom Country = JP",
           "tags":[

           ],
           "parameterSize":1,
           "nonValidCases":false,
           "deletedCases":false,
           "icsrPadderAgencyCases":false,
           "originalQueryId":0,
           "JSONQuery":null,
           "lastUpdated":"2016-05-25T22:16:17Z",
           "isDeleted":false,
           "hasBlanks":true,
           "createdBy":"dev",
           "qualityChecked":true,
           "owner":{
              "id":15,
              "username":"dev",
              "fullName":"Dev User"
           },
           "dateCreated":"2016-05-25T20:47:34Z"
        }""".stripIndent()

    }

    def cleanup() {

    }

    void "createQuery"() {
        given:
        JSONElement jsonObject = JSON.parse(queryJSON)

        when:
        Query query = service.createQuery(jsonObject, parsedTags, user)

        then:
        assert query.name == "PVR-ST-003-001 Query"
    }

    void "createCustomSQLQuery" () {
        given:
        JSONElement jsonObject = JSON.parse(customSQLQueryJSON)

        when:
        CustomSQLQuery customSQLQuery = service.createCustomSQLQuery(jsonObject, parsedTags, user)

        then:
        assert customSQLQuery.name == "Custom Country = JP"
        assert customSQLQuery.customSQLValues[0].key == ':country'
        assert customSQLQuery.parameterSize == 1
    }


    void "getValidInvalidValues with invalid input data"() {
        given:
        List<Object> values = ['India', 'America']
        String selectedField = null
        Tenants.metaClass.static.currentId = { ->
            return 1
        }
        service.targetDatastore = new SimpleMapDatastore(['pva'], CaseInfo)

        // Pass the 'isFaersTarget' boolean parameter (set to false or true based on your test)
        boolean isFaersTarget = false  // or true if needed

        when:
        Map<String, List> result = service.getValidInvalidValues(values, selectedField, "en", isFaersTarget)

        then:
        assert result.validValues == []
        assert result.invalidValues == values
    }


    void "getValidInvalidValues with valid input data"() {
        given:
        // Mocking createCriteria for CaseInfo to return valid values
        CaseInfo.metaClass.static.createCriteria = {
            new Object() {
                List<String> list(Closure cl) {
                    ["12US000408", "16US000585"]  // Valid case numbers
                }
            }
        }

        // Mocking the tenant ID
        Tenants.metaClass.static.currentId = { ->
            return 1
        }

        // The values to be validated
        List<Object> values = ['12US000408', 'ZU1S000585', 'A6US00123', '16US000585']

        // The field to check for
        String selectedField = CaseLineListingTemplate.CLL_TEMPLATE_REPORT_FIELD_NAME

        // Mock the target datastore
        service.targetDatastore = new SimpleMapDatastore(['pva'], CaseInfo)

        // Determine whether to use FAERS or PVA data
        boolean isFaersTarget = false  // You can change this to true if needed

        when:
        // Call the method with the added boolean argument
        Map<String, List> result = service.getValidInvalidValues(values, selectedField, "en", isFaersTarget)

        then:
        // Check that the valid values are correctly returned
        assert result.validValues == ['12US000408', '16US000585']
        // Check that the invalid values are correctly returned
        assert result.invalidValues == ['ZU1S000585', 'A6US00123']
    }


    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    private User makeDevUser() {
        String username = "unitTest"
        def preference = new Preference(locale: new Locale("en"), createdBy: username, modifiedBy: username)
        def devRole = new Role(authority: 'ROLE_DEV', createdBy: username, modifiedBy: username).save(flush: true)
        def devUser = new User(username: 'dev', password: 'dev', fullName: "Dev User", preference: preference, createdBy: username, modifiedBy: username)
        devUser.addToTenants(tenant)
        devUser.save(failOnError: true)
        UserRole.create(devUser, devRole, true)
        return devUser
    }
}
