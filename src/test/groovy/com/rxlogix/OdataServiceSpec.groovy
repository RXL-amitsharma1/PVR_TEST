package com.rxlogix


import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import groovy.sql.Sql
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.sql.Types

@ConfineMetaClassChanges([OdataService])
class OdataServiceSpec extends Specification implements ServiceUnitTest<OdataService> {

    @Shared
    SimpleDriverDataSource reportDataSource

    @Shared
    def allowedFields = [
            [columnName: "ID", label: "ID", dataType: Types.NUMERIC],
            [columnName: "NUMBER_FIELD", label: "numberField", dataType: Types.NUMERIC],
            [columnName: "DATE_FIELD", label: "dateField", dataType: Types.DATE],
            [columnName: "STRING_FIELD", label: "stringField", dataType: Types.VARCHAR]
    ]

    def setup() {
        service.metaClass.getDS = { String s -> return getReportConnection() }
        service.metaClass.getDSUserName = { String s -> return "%" }
        service.userService = makeUserService()
    }

    def cleanup() {
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..10) { true }
        return userMock.proxyInstance()
    }

    void "test getDsTables method"() {
        when:
        List result = service.getDsTables("test")
        then:
        result.size() > 0
        result.contains("TEST_TABLE")
    }

    void "test getDsTableFields method"() {
        when:
        Map result = service.getDsTableFields("test", "TEST_TABLE")
        then:
        result.entity == "TestTable"
        result.fields.ID == "ID"
        result.fields.numberField == "NUMBER_FIELD"
        result.fields.dateField == "DATE_FIELD"
        result.fields.stringField == "STRING_FIELD"
        result.fields.clobField == "CLOB_FIELD"
    }

    void "test getFieldsType"() {
        when:
        def allowedFieldsMap = [
                ID         : "ID",
                numberField: "NUMBER_FIELD",
                dateField  : "DATE_FIELD",
                stringField: "STRING_FIELD",
                clobField  : "CLOB_FIELD",
        ]
        List result = service.getFieldsType("test", "TEST_TABLE", allowedFieldsMap)
        then:
        result[0] == [columnName: "ID", label: "ID", dataType: Types.NUMERIC]
        result[1] == [columnName: "NUMBER_FIELD", label: "numberField", dataType: Types.NUMERIC]
        result[2] == [columnName: "DATE_FIELD", label: "dateField", dataType: Types.TIMESTAMP]
        result[3] == [columnName: "STRING_FIELD", label: "stringField", dataType: Types.VARCHAR]
        result[4] == [columnName: "CLOB_FIELD", label: "clobField", dataType: Types.CLOB]
    }


    void "test getDataForEntity method"() {
        when:

        Map result = service.getDataForEntity("test", "TEST_TABLE", configLimitQuery, allowedFields, params)

        then:
        result.data.size() == size
        result.data[0].size() == fieldsNumber
        result.data[0][0].value == value
        result.meta == meta
        where:
        size | value | meta       | fieldsNumber | configLimitQuery | params
        10   | 0     | [:]        | 4            | null             | [:]
        4    | 6     | [:]        | 1            | "ID>5"           | [select: "ID"]
        4    | 9     | [:]        | 2            | "ID>5"           | [orderby: "ID desc", select: "ID,numberField"]
        10   | 9     | [:]        | 4            | null             | [orderby: "stringField desc, ID desc"]
        2    | 7     | [count: 4] | 4            | "ID>5"           | [skip: 2, top: 2, count: true, orderby: "stringField desc, ID desc"]
        2    | 7     | [count: 4] | 4            | null             | [skip: 2, top: 2, count: true, filter: "ID>5", orderby: "stringField desc, ID desc"]
    }

    void "test getEntity method"() {
        when:

        List result = service.getEntity("test", "TEST_TABLE", null, allowedFields, 0)

        then:
        result.size() == 4
        result[0].value == 0
        result[0].label == "ID"
        result[0].dataType == Types.NUMERIC
        result[1].value == 1
        result[1].label == "numberField"
        result[1].dataType == Types.NUMERIC
        result[2].value == Date.parse("yyyy-MM-dd", "2016-01-01")
        result[2].label == "dateField"
        result[2].dataType == Types.DATE
        result[3].value == "string value0"
        result[3].label == "stringField"
        result[3].dataType == Types.VARCHAR

    }

    void "test that getEntity returns empty result"() {
        when:
        List result = service.getEntity("test", "TEST_TABLE", "ID>5", allowedFields, 0)
        then:
        result == []
    }


    void "test createEntity method"() {
        when:
        service.createEntity("test", "TEST_TABLE", ["ID", "NUMBER_FIELD", "DATE_FIELD", "STRING_FIELD"], [11, 0, Date.parse("yyyy-MM-dd", "2016-01-01"), "str"])
        List result = service.getEntity("test", "TEST_TABLE", null, allowedFields, 11)
        then:
        result[0].value == 11
        result[1].value == 0
        result[2].value == Date.parse("yyyy-MM-dd", "2016-01-01")
        result[3].value == "str"
    }

    void "test updateEntity method"() {
        when:
        int i = service.updateEntity("test", "TEST_TABLE", ["STRING_FIELD"], ["str"], 0)
        List result = service.getEntity("test", "TEST_TABLE", null, allowedFields, 0)
        then:
        i == 1
        result[0].value == 0
        result[3].value == "str"
    }

    void "test updateEntity method - can not update limited objects"() {
        when:
        int i = service.updateEntity("test", "TEST_TABLE", ["STRING_FIELD"], ["str"], 1, "ID>5")
        List result = service.getEntity("test", "TEST_TABLE", null, allowedFields, 1)
        then:
        i == 0
        result[0].value == 1
        result[3].value == "string value1"
    }

    void "test deleteEntity method"() {
        when:
        service.createEntity("test", "TEST_TABLE", ["ID", "NUMBER_FIELD", "DATE_FIELD", "STRING_FIELD"], [15, 0, Date.parse("yyyy-MM-dd", "2016-01-01"), "str"])
        int i = service.deleteEntity("test", "TEST_TABLE", 15)
        List result = service.getEntity("test", "TEST_TABLE", null, allowedFields, 15)
        then:
        i == 1
        result == []
    }

    void "test deleteEntity method - can not delete limited objects"() {
        when:
        int i = service.deleteEntity("test", "TEST_TABLE", 5, "ID>5")
        List result = service.getEntity("test", "TEST_TABLE", null, allowedFields, 5)
        then:
        i == 0
        result.size() > 0

    }

    private getReportConnection() {
        if (!reportDataSource) {
            reportDataSource = new SimpleDriverDataSource()
            reportDataSource.driverClass = org.h2.Driver
            reportDataSource.url = "jdbc:h2:mem:test;MODE=Oracle;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1"
            createTestTable(reportDataSource)
        }
        return reportDataSource
    }

    private void createTestTable(SimpleDriverDataSource reportDataSource) {
        Sql sql = new Sql(reportDataSource)
        String sqlString = "CREATE TABLE TEST_TABLE(" +
                "    ID NUMBER," +
                "    NUMBER_FIELD NUMBER," +
                "    DATE_FIELD DATE," +
                "    STRING_FIELD VARCHAR2(256)," +
                "    CLOB_FIELD CLOB)"

        sql.executeUpdate(sqlString)
        Date date = Date.parse("yyyy-MM-dd", "2016-01-01")
        (0..9).each {
            sql.executeUpdate("insert into TEST_TABLE values (?,?,?,?,?)", [it, it + 1, date + it, "string value" + it, "clob string value" + it].toArray())
        }

    }
}
