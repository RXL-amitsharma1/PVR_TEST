package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.ReportFieldSelectionTypeEnum
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, UserGroup, UserGroupUser])
class ReportFieldServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportFieldService> {
    @Shared def orgTmp

    @Shared caseMasterColumnCountry
    @Shared caseInformationRFG


    def setup() {
        // Build report field data
        buildReportFields()

        ReportField querySelectableField = new ReportField(name: "querySelectableField", fieldGroup: caseInformationRFG,
                sourceColumnId: caseMasterColumnCountry.reportItem, dataType: String.class, querySelectable: true,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: false, sourceId: 1).save(failOnError: true)
        ReportField templateCLLSelectableField = new ReportField(name: "templateCLLSelectableField", fieldGroup: caseInformationRFG,
                sourceColumnId: caseMasterColumnCountry.reportItem, dataType: String.class, querySelectable: false,
                templateCLLSelectable: true, templateDTRowSelectable: false, templateDTColumnSelectable: false, sourceId: 1).save(failOnError: true)
        ReportField templateDTRowSelectableField = new ReportField(name: "templateDTRowSelectableField", fieldGroup: caseInformationRFG,
                sourceColumnId: caseMasterColumnCountry.reportItem, dataType: String.class, querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: true, templateDTColumnSelectable: false, sourceId: 1).save(failOnError: true)
        ReportField templateDTColumnSelectableField = new ReportField(name: "templateDTColumnSelectableField", fieldGroup: caseInformationRFG,
                sourceColumnId: caseMasterColumnCountry.reportItem, dataType: String.class, querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, sourceId: 1).save(failOnError: true)

        ApplicationSettings applicationSettings = new ApplicationSettings().save(failOnError: true)
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

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: 'user', modifiedBy: 'user')
        def userRole = new Role(authority: 'isAuthenticated()', createdBy: 'user', modifiedBy: 'user').save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: 'user', modifiedBy: 'user')
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    void buildReportFields() {
        SourceTableMaster caseMasterTable = new SourceTableMaster(tableName: "V_C_IDENTIFICATION", tableAlias: "cm", tableType: "C", caseJoinOrder: 1).save(failOnError: true)
        SourceTableMaster lmCountriesTable = new SourceTableMaster(tableName: "VW_LCO_COUNTRY", tableAlias: "lco", tableType: "L", caseJoinOrder: null).save(failOnError: true)
        caseMasterColumnCountry = new SourceColumnMaster(tableName: caseMasterTable, columnName: "COUNTRY_ID",
                primaryKey: null, lmTableName: lmCountriesTable, lmJoinColumn: "COUNTRY_ID",
                lmDecodeColumn: "COUNTRY", columnType: "N", reportItem: "CM_COUNTRY_ID", lang: "en").save(failOnError: true)
        caseInformationRFG = new ReportFieldGroup(name: "Case Information")
        caseInformationRFG.id = System.currentTimeMillis()
        caseInformationRFG.save(failOnError: true)

        //Purposely leaving out listDomainClass
        ReportField countryOfIncidenceRF = new ReportField(name: "masterCountryId", fieldGroup: caseInformationRFG,
                sourceColumnId: caseMasterColumnCountry.reportItem, dataType: String.class, querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: false, sourceId: 1).save(failOnError: true)
    }

    def setupSpec() {
        mockDomains ReportField, Role, ReportFieldGroup, SourceTableMaster, SourceColumnMaster, ApplicationSettings, Tenant, User, Preference, UserRole, UserGroup, SourceProfile, UserGroupUser
        orgTmp = System.getProperty("java.io.tmpdir")
        System.setProperty("java.io.tmpdir", "./")
    }

    def cleanupSpec() {
        File f = new File(System.getProperty("java.io.tmpdir"), "selectable_list_file.dat")
        f.delete()
        System.setProperty("java.io.tmpdir", orgTmp)
    }
    @Ignore
    void "Test getting report fields only for Case Line Listing Template"() {
        given: "Some report fields with flags in setup"

        when: "Call getReportFields"
        List fields = service.getReportFields(ReportFieldSelectionTypeEnum.CLL, [:])

        then: "Return only CLL selectable fields"
        ReportField.findAll().size() == 5
        fields."children"[0].size() == 1
    }
    @Ignore
    void "Test getting report fields only for Data Tabulation Template Row"() {
        given: "Some report fields with flags in setup"

        when: "Call getReportFields"
        List fields = service.getReportFields(ReportFieldSelectionTypeEnum.DT_ROW, [:])

        then: "Return only Data Tabulation Template Row selectable fields"
        ReportField.findAll().size() == 5
        fields."children"[0].size() == 1
    }
    @Ignore
    void "Test getting report fields only for Data Tabulation Template Column"() {
        given: "Some report fields with flags in setup"

        when: "Call getReportFields"
        List fields = service.getReportFields(ReportFieldSelectionTypeEnum.DT_COLUMN, [:])

        then: "Return only Data Tabulation Template Column selectable fields"
        ReportField.findAll().size() == 5
        fields."children"[0].size() == 1
    }
    @Ignore
    void "Test getting report fields only for Query"() {
        given: "Some report fields with flags in setup"
        User normalUser = makeNormalUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return normalUser}
        service.userService = mockUserService
        UserGroup.metaClass.static.fetchAllFieldProfileByUser = { User user ->
            return []
        }
        UserGroupUser.metaClass.static.countByUser = { User user ->
            return null
        }
        User.metaClass.static.getBlindedFieldsForUser = { User user ->
            return []
        }

        when: "Call getReportFieldsForQuery"
        List fields = service.getReportFieldsForQuery()

        then: "Return only Query selectable fields"
        ReportField.findAll().size() == 5
    }

    def "serializeValues test "() {
        setup:
            def values = [key_a: "a", key_b: "b"]

        when:
            service.serializeValues(values,"en")
        then:
            new File(System.getProperty("java.io.tmpdir"), "selectable_list_file.dat_en").exists() == true
    }

    def "deserialize Value test"() {
        setup:
            def values = [key_a: "a", key_b: "b"]
            service.serializeValues(values,"en")
        when:
            def readValues= service.readValues("en")
        then:
            readValues.size() == 2
            readValues["key_a"] == "a"
    }

    def "serialize Single Value Field"(){
        given:
        def obj = [[lang_code:"en", display:"English"]]
        String reportFieldName = "testField"
        def singleFieldMap = ["testField":["1" , "2" , "3"]]

        when:
        def fieldMap =service.serializeValuesForSingleField(singleFieldMap, obj[0].lang_code, reportFieldName)

        then:
        fieldMap.size() == 3
        fieldMap["testField"] == ["1" , "2" , "3"]
    }


}
