package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.SourceProfileTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static com.rxlogix.util.Strings.collapseWhitespace

@ConfineMetaClassChanges([ExecutedReportConfiguration, ViewHelper])
class SqlGenerationServiceSpec extends Specification implements DataTest, ServiceUnitTest<SqlGenerationService> {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    @Shared caseMasterTable
    @Shared lmCountriesTable
    @Shared caseMasterColumnCountry
    @Shared caseInformationRFG
    @Shared countryOfIncidenceRF

    def setupSpec() {
        mockDomains Query, ReportField, ReportFieldGroup, SourceTableMaster, SourceColumnMaster, TemplateQuery, ExecutedConfiguration,DateRangeInformation,DataTabulationTemplate, ExecutedTemplateQuery
        // force the tests to run in the TEST_TZ
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))

        // Build report field data
        buildReportFields()
    }

    void "Test   initializeReportGtts"() {
        given:
        ExecutedDateRangeInformation dateRangeInformation = new ExecutedDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE)
        ExecutedTemplateQuery templateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: dateRangeInformation)
        dateRangeInformation.executedTemplateQuery = templateQuery
        DataTabulationTemplate reportTemplate = new DataTabulationTemplate()
        ExecutedConfiguration configuration = new ExecutedConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(), executedTemplateQueries: [templateQuery])
        configuration.metaClass.getReportMinMaxDate = { return [new Date(), new Date()] }
        templateQuery.metaClass.getUsedConfiguration = { return configuration }
        templateQuery.metaClass.getUsedTemplate = { return reportTemplate }
        templateQuery.metaClass.getUsedQuery = { return null }
        ExecutedReportConfiguration.metaClass.static.findById = { Long id -> configuration }

        configuration.sourceProfile = new SourceProfile(sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE)

        when:
        reportTemplate.positiveCountOnly = positiveCountOnly
        String result = service.initializeReportGtts(templateQuery, reportTemplate, false, Locale.US)
        then:
        result.contains(insert)
        where:
        positiveCountOnly << [true, false]
        insert << [" Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_TO_PERIOD_COUNT_GT_0','1');",
                   " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_TO_PERIOD_COUNT_GT_0','0');"]
    }

    void buildReportFields() {
        caseMasterTable = new SourceTableMaster(tableName: "V_C_IDENTIFICATION", tableAlias: "cm", tableType: "C", caseJoinOrder: 1)
        lmCountriesTable = new SourceTableMaster(tableName: "VW_LCO_COUNTRY", tableAlias: "lco", tableType: "L", caseJoinOrder: null)
        caseMasterColumnCountry = new SourceColumnMaster(tableName: caseMasterTable, columnName: "COUNTRY_ID",
                primaryKey: null, lmTableName: lmCountriesTable, lmJoinColumn: "COUNTRY_ID",
                lmDecodeColumn: "COUNTRY", columnType: "N", reportItem: "CM_COUNTRY_ID",lang: "en")
        caseInformationRFG = new ReportFieldGroup(name: "Case Information")

        //Purposely leaving out listDomainClass
        countryOfIncidenceRF = new ReportField(name: "masterCountryId",
                fieldGroup: caseInformationRFG, sourceColumnId: caseMasterColumnCountry.reportItem,
                dataType: String.class, sourceId: 1)
    }

    void saveReportFields() {
        caseMasterTable.save(failOnError: true)
        lmCountriesTable.save(failOnError: true)
        caseMasterColumnCountry.save(failOnError: true)
        caseInformationRFG.save(failOnError: true)
        countryOfIncidenceRF.save(failOnError: true)
    }

    def cleanupSpec() {
        TimeZone.setDefault(ORIGINAL_TZ) // set the TZ back to what it was
    }

    void "Where: No Query"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        result == ""
    }

    void "Where: E0"() {
        given: "JSON Query String from client"
        String JSONQuery = """ { "all": { "containerGroups": [   { "expressions": [  
                                { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" }  ] }  ] } }""" ;
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        result == "((UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES')))"
    }

    void "Where: E0 and E1"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   { "expressions": [  
                                { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" },  
                                { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ] , 
                                "keyword": "and" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        result == "((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES')))"
    }

    void "Where: E0 or E1"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   { "expressions": [  
                            { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                            { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ] , 
                            "keyword": "or" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        result == "((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or (UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES')))"
    }

    void "Where: E2 or (E0 and E1)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
                { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "CHAD" } ,  
                { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ] , "keyword": "and" }  ] , 
                "keyword": "or" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or ((UPPER(lco_1.COUNTRY) = UPPER('CHAD')) and 
             (UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES'))))""" );
    }

    void "Where: E2 and E3 and (E0 or E1)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
            { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , 
              "keyword": "or" }  ] , "keyword": "and" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or  (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))))""" )
    }

    void "Where: E2 and E3 and (E0 or E1) and (E4 and E5)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  { "index": "5", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "or" }  ,  { "expressions": [  { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  { "index": "4", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "or" }  ] , "keyword": "and" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or  (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))) and 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or  (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))))""" )
    }

    void "Where: (E2 and E3 and (E0 or E1)) and (E6 and E7 and (E4 or E5))"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
            { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "4", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "or" }  ] , "keyword": "and" }  ,  
            { "expressions": [  { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "5", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
            { "expressions": [  { "index": "7", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "6", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , 
                  "keyword": "or" }  ] , "keyword": "and" }  ] , "keyword": "and" } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or  (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))) and 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or  (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))))""" )
    }

    void "Where: (E2 and E3) or (E0 and E1)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ,  
                                { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ] , "keyword": "and" }  ,  
            { "expressions": [  { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ,  
                                { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" } ] , "keyword": "and" }  ] , "keyword": "or" } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES')) and (UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES'))) or 
            ((UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES')) and (UPPER(lco_1.COUNTRY) = UPPER('UNITED STATES')))""" )
    }

    void "Where: ((E2 and E3) or (E0 and E1))"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "expressions": [  { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "and" }  ,  
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "and" }  ] , "keyword": "or" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            (((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))) or 
             ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))))""" )
    }

    void "Where: (E0) or (E1) or (E2)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" }  ] }  ,  
            { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" }  ] }  ,  
            { "expressions": [  { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" }  ] }  ] , "keyword": "or" } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))) or ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))) or 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))""" )
    }

    void "Where: (E0 and E1) or (E2 or E3) or (E4 and E5)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "and" }  ,  
            { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "or" }  ,  
            { "expressions": [  { "index": "4", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "5", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "and" }  ] , "keyword": "or" } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))) or 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN'))) or 
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))""" )
    }

    void "Where: (E0 and (E1 or E2)) or (E3)"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
            { "expressions": [  { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "or" }  ] , "keyword": "and" },
            { "expressions": [  { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" }  ] }  ] , "keyword": "or" } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
            ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or 
             (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))) or ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))""" )
    }

    void "Where: (E0 and (E1 or E2)) or ((E3 and (E4)))"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
            { "expressions": [  { "index": "1", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
            { "index": "2", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ] , "keyword": "or" }  ] , "keyword": "and" }  ,  
            { "expressions": [  { "expressions": [  { "index": "3", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" } ,  
                                { "expressions": [  { "index": "4", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN" }  ] }  ] , 
                                    "keyword": "and" }   ] }  ] , "keyword": "or" } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """
        ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) or 
         (UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))) or (((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')) and 
        ((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')))))""")
    }

    void "Multiselect: 2 countries"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN;BURKINA FASO" }  ] }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        result == "((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN') OR UPPER(lco_1.COUNTRY) = UPPER('BURKINA FASO')))"
    }

    void "Multiselect: 3 countries"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN;BURKINA FASO;CHINA" }  ] }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause";
        collapseWhitespace( result ) == collapseWhitespace( """((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN') OR UPPER(lco_1.COUNTRY) = UPPER('BURKINA FASO') OR UPPER(lco_1.COUNTRY) = UPPER('CHINA')))""" );
    }

    void "Multiselect: Not equal"() {
        given: "JSON Query String from client"
        String JSONQuery = """{ "all": { "containerGroups": [   
            { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "AFGHANISTAN;BURKINA FASO" },
            { "index": "1", "field": "masterCountryId", "op": "NOT_EQUAL", 
                    "value": "AFGHANISTAN;ANDORRA;ANTARCTICA;BURKINA FASO;BURUNDI;CHAD;CHILE;CHINA" } ] , "keyword": "and" }   ] } }""";
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        collapseWhitespace( result ) == collapseWhitespace( """((UPPER(lco_1.COUNTRY) = UPPER('AFGHANISTAN')
            OR UPPER(lco_1.COUNTRY) = UPPER('BURKINA FASO')) and (UPPER(lco_1.COUNTRY) <> UPPER('AFGHANISTAN')
            AND UPPER(lco_1.COUNTRY) <> UPPER('ANDORRA') AND UPPER(lco_1.COUNTRY) <> UPPER('ANTARCTICA')
            AND UPPER(lco_1.COUNTRY) <> UPPER('BURKINA FASO') AND UPPER(lco_1.COUNTRY) <> UPPER('BURUNDI')
            AND UPPER(lco_1.COUNTRY) <> UPPER('CHAD') AND UPPER(lco_1.COUNTRY) <> UPPER('CHILE')
            AND UPPER(lco_1.COUNTRY) <> UPPER('CHINA')))""" )
    }

    void "Test replaceMapInString"(){
        given:
        String sqlQuery = 'select * from dual where fullname  = :name'
        Map mp  = [':name' : 'sarthak']
        when: "The query is parsed"
        String result = service.replaceMapInString(sqlQuery,mp)
        then:
        result == 'select * from dual where fullname  = sarthak'
    }


    void "Where: ADVANCE_CONTAINS"() {
        given: "JSON Query String from client"
        String JSONQuery = """ { "all": { "containerGroups": [   { "expressions": [  
                                { "index": "0", "field": "masterCountryId", "op": "ADVANCE_CONTAINS", "value": "UN.*" }  ] }  ] } }""" ;
        saveReportFields()
        when: "The query is parsed"
        String result = service.buildFilterSQLFromJSON(JSONQuery, null, null, null, 0,EvaluateCaseDateEnum.LATEST_VERSION,null, new Locale("en")).result
        then: "The correct where clause"
        result == "(( REGEXP_LIKE(UPPER(lco_1.COUNTRY), 'UN.*') AND lco_1.COUNTRY IS NOT NULL))"

    }

    void "test pkg_reassess_listedness call sql "() {
        given:
        Date d = Date.parse("dd-MM-yyyy", "29-05-2023")
        TemplateQuery tq = new TemplateQuery()
        tq.metaClass.getUsedTemplate = {
            [reassessListedness      : ReassessListednessEnum.BEGINNING_OF_THE_REPORTING,
             getAllSelectedFieldsInfo: { ->
                 [new ReportFieldInfo(datasheet: "A", onPrimaryDatasheet: true), new ReportFieldInfo(datasheet: "B", onPrimaryDatasheet: false)] }
            ]
        }
        tq.metaClass.getUsedConfiguration = { [reportMinMaxDate: [d]] }
        tq.metaClass.getEndDate = { d }
        when:
        String result = service.setReassessContextForTemplate(tq, false)

        then:
        result == "{call pkg_reassess_listedness.p_report('A,1,B,0','29-05-2023',0)}"
    }

    void "test fetchDatasheet "() {
        given:
        List expression = []
        String JSONQuery = ("""{"all":{"containerGroups":[ {"expressions":[ {"index":"0","field":"dvListednessReassessQuery","op":"EQUALS","value":"No","RLDS":"CCDS(ADULT)","RLDS_OPDS":"false"}, {"index":"1","field":"dvListednessReassessQuery","op":"EQUALS","value":"No","RLDS":"JPPI","RLDS_OPDS":"true"}, {"index":"2","field":"dvListednessReassessQuery","op":"EQUALS","value":"","RLDS":"CORE","RLDS_OPDS":"false","key":"1"}],"keyword":"and"} ] } ,"blankParameters":[{"key":1,"field":"dvListednessReassessQuery","op":"EQUALS","value":""}]}""").replace("\\", "\\\\")
        Map queryJSON = MiscUtil.parseJsonText(JSONQuery)
        Map allMap = queryJSON.all
        List containerGroupsList = allMap.containerGroups
        def expressionList = containerGroupsList.get(0)
        when:
        String result = service.fetchDatasheet(expressionList.getAt('expressions'))

        then:
        result == "CCDS(ADULT),0,JPPI,1,CORE,0,"
    }
}
