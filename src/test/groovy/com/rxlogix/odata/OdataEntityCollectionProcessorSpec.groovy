package com.rxlogix.odata

import com.rxlogix.OdataService
import com.rxlogix.config.OdataSettings
import grails.converters.JSON
import grails.testing.gorm.DataTest
import groovy.transform.CompileDynamic
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.ODataHttpHandler
import org.apache.olingo.server.api.ServiceMetadata
import org.apache.olingo.commons.api.edmx.EdmxReference
import org.junit.jupiter.api.BeforeAll
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.sql.Types

@ConfineMetaClassChanges([OdataService])
@CompileDynamic
class OdataEntityCollectionProcessorSpec extends Specification implements DataTest{
    
    @SuppressWarnings("JUnitPublicNonTestMethod")
    def setupSpec(){
        mockDomains OdataSettings
    }

    private void init(Map resultParams, String query, MockHttpServletResponse res) {
        OdataSettings odataSettings = new OdataSettings(dsName: "test", settings: """ {"test": {"attr1": "abc", "attr2": "abc"}}""", dsPassword: "test3", dsLogin: "test4", dsUrl: "test5", createdBy: "normalUser", modifiedBy: "normalUser")
        odataSettings.save(flush: true, failOnError: true)
        OdataEntityCollectionProcessor odataEntityCollectionProcessor = new OdataEntityCollectionProcessor();
        OdataSettingsCache.metaClass.static.getEntityFields = { String dsName, String entityName -> return OdataTestUtil.entityFields["CaseSeries"] }
        OdataSettingsCache.metaClass.static.getEntityMap = { String dsName, String entityName -> return OdataTestUtil.entityMap["CaseSeries"] }
        OdataSettingsCache.metaClass.static.getEntitiesMap = { String dsName -> return OdataTestUtil.entityMap["CaseSeries"] }
        OdataService odataService = new OdataService()
        odataService.metaClass.getDataForEntity = { String dsName, String tableName, String configLimitQuery, configAllowedFields, Map params ->
            resultParams.putAll(params)
            return [data: [[
                                   [value: 1, label: "ID", dataType: Types.DECIMAL],
                                   [value: 2, label: "numberField", dataType: Types.DECIMAL],
                                   [value: "test", label: "stringField", dataType: Types.VARCHAR],
                                   [value: new Date(2018, 1, 1), label: "dateField", dataType: Types.DATE]
                           ]], meta: [:]]
        }
        odataEntityCollectionProcessor.metaClass.getOdataService = { ->
            return odataService
        }
        OdataEdmProvider oep = new OdataEdmProvider()
        oep.metaClass.getOdataService = { ->
            return odataService
        }
        OdataSettingsCache.metaClass.static.getOdataService = { ->
            return odataService
        }
        MockHttpServletRequest req = new MockHttpServletRequest()

        req.setMethod("GET")
        req.setServletPath("/odata/")
        req.setRequestURI("/odata/CaseSeries")
        req.setQueryString(query)
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(oep, new ArrayList<EdmxReference>());
        ODataHttpHandler handler = odata.createHandler(edm)
        handler.register(odataEntityCollectionProcessor)
        handler.process(req, res)
    }

    void "test readEntityCollection method with select param"() {
        when:
        MockHttpServletResponse res = new MockHttpServletResponse()
        Map resultParams = [:]
        init(resultParams, "\$select=ID", res)
        def result = JSON.parse(res.getContentAsString())
        then:
        result.value.size() == 1
        result.value[0].size() == 1
        result.value[0].ID == 1

    }

    void "test readEntityCollection method"() {

        when:
        MockHttpServletResponse res = new MockHttpServletResponse()
        Map resultParams = [:]
        init(resultParams, query, res)
        then:

        JSON.parse(res.getContentAsString()).value.size() == 1
        resultParams == value
        where:
        query                                                                              | value
        //check empty query
        ""                                                                                 | [:]
        //check handlePagination
        "\$top=1&\$skip=10&\$count=true"                                                   | [top: 1, skip: 10, count: true]
        "\$top=1&\$count=true"                                                             | [top: 1, count: true]
        "\$top=1"                                                                          | [top: 1]
        //check handleOrderby
        "\$orderby=ID asc"                                                                 | [orderby: "ID asc"]
        //check filter and FilterExpressionVisitor
        //logical and math functions
        "\$filter=ID eq 1"                                                                 | [filter: "(ID = 1)", paramValues: [:]]
        "\$filter=(ID add 1) gt 1"                                                         | [filter: "((ID + 1) > 1)", paramValues: [:]]
        "\$filter=(ID sub 1) lt 1"                                                         | [filter: "((ID - 1) < 1)", paramValues: [:]]
        "\$filter=(ID div 1) ge 1"                                                         | [filter: "((ID / 1) >= 1)", paramValues: [:]]
        "\$filter=(ID mul 1) le 1"                                                         | [filter: "((ID * 1) <= 1)", paramValues: [:]]
        "\$filter=(ID mod 1) ne 1"                                                         | [filter: "(mod(ID,1) <> 1)", paramValues: [:]]
        // test "(", ")", "or", "and", string and date values
        "\$filter=(ID eq 1) or (stringField eq '1')"                                       | [filter: "((ID = 1) or (STRING_FIELD = :param_1))", paramValues: [param_1: "1"]]
        "\$filter=ID eq 1 and stringField eq '1'"                                          | [filter: "((ID = 1) and (STRING_FIELD = :param_1))", paramValues: [param_1: "1"]]
        "\$filter=dateField eq 2018-01-01T00:00:00Z"                                       | [filter: "(DATE_FIELD =  TO_TIMESTAMP_TZ('2018-01-01T00:00:00Z','YYYY-MM-DD\"T\"hh24:mi:sstzh:tzm'))", paramValues: [:]]
        "\$filter=ID eq 1 and (dateField eq 2018-01-01T00:00:00Z or stringField eq 'str')" | [filter: "((ID = 1) and ((DATE_FIELD =  TO_TIMESTAMP_TZ('2018-01-01T00:00:00Z','YYYY-MM-DD\"T\"hh24:mi:sstzh:tzm')) or (STRING_FIELD = :param_1)))", paramValues: [param_1: "str"]]
        // test string functions
        "\$filter=ID eq 1 and contains(stringField,'str')"                                 | [filter: "((ID = 1) and (STRING_FIELD like :param_1))", paramValues: [param_1: "%str%"]]
        "\$filter=ID eq 1 and startswith(stringField,'str')"                               | [filter: "((ID = 1) and (STRING_FIELD like :param_1))", paramValues: [param_1: "str%"]]
        "\$filter=ID eq 1 and endswith(stringField,'str')"                                 | [filter: "((ID = 1) and (STRING_FIELD like :param_1))", paramValues: [param_1: "%str"]]
        "\$filter=ID eq 1 and length(stringField) eq 1"                                    | [filter: "((ID = 1) and (LENGTH(STRING_FIELD) = 1))", paramValues: [:]]
        "\$filter=ID eq 1 and indexof(stringField,'a') eq 1"                               | [filter: "((ID = 1) and (INSTR(STRING_FIELD,:param_1) = 1))", paramValues: [param_1: "a"]]
        "\$filter=ID eq 1 and substring(stringField,1,2) eq 'a'"                           | [filter: "((ID = 1) and (SUBSTR(STRING_FIELD , 1 , 2) = :param_1))", paramValues: [param_1: "a"]]
        "\$filter=ID eq 1 and substring(stringField,1) eq 'a'"                             | [filter: "((ID = 1) and (SUBSTR(STRING_FIELD , 1) = :param_1))", paramValues: [param_1: "a"]]
        "\$filter=ID eq 1 and tolower(stringField) eq 'a'"                                 | [filter: "((ID = 1) and (LOWER(STRING_FIELD) = :param_1))", paramValues: [param_1: "a"]]
        "\$filter=ID eq 1 and toupper(stringField) eq 'a'"                                 | [filter: "((ID = 1) and (UPPER(STRING_FIELD) = :param_1))", paramValues: [param_1: "a"]]
        "\$filter=ID eq 1 and trim(stringField) eq 'a'"                                    | [filter: "((ID = 1) and (TRIM(BOTH ' ' FROM  STRING_FIELD) = :param_1))", paramValues: [param_1: "a"]]
        "\$filter=ID eq 1 and concat(stringField,'str') eq 'str2'"                         | [filter: "((ID = 1) and ((STRING_FIELD || :param_1) = :param_2))", paramValues: [param_1: "str", param_2: "str2"]]
        // test date functions
        "\$filter=(year(dateField) eq 2017)"                                               | [filter: "(EXTRACT(YEAR FROM DATE_FIELD) = 2017)", paramValues: [:]]
        "\$filter=(month(dateField) eq 2017)"                                              | [filter: "(EXTRACT(MONTH FROM DATE_FIELD) = 2017)", paramValues: [:]]
        "\$filter=(day(dateField) eq 2017)"                                                | [filter: "(EXTRACT(DAY FROM DATE_FIELD) = 2017)", paramValues: [:]]
        "\$filter=(hour(dateField) eq 2017)"                                               | [filter: "(EXTRACT(HOUR FROM DATE_FIELD) = 2017)", paramValues: [:]]
        "\$filter=(minute(dateField) eq 2017)"                                             | [filter: "(EXTRACT(MINUTE FROM DATE_FIELD) = 2017)", paramValues: [:]]
        "\$filter=(second(dateField) eq 2017)"                                             | [filter: "(EXTRACT(SECOND FROM DATE_FIELD) = 2017)", paramValues: [:]]
        "\$filter=dateField gt now()"                                                      | [filter: "(DATE_FIELD > CURRENT_DATE)", paramValues: [:]]
    }
}
