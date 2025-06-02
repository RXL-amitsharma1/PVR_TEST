package com.rxlogix


import grails.testing.web.controllers.ControllerUnitTest
import groovy.sql.Sql
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Specification

import java.sql.Types

class SchemaComparisonControllerSpec extends Specification implements ControllerUnitTest<SchemaComparisonController> {
    def setup() {
    }

    def cleanup() {
    }


    void "test getLatestComparisonInfo()"() {
        given:
        controller.dataSource_schemaComparator = new JdbcDataSource()
        Sql.metaClass.rows = { String sql ->
            return [
                    ["SCHVAL_KEY": "SCHVAL_STATUS", "SCHEVAL_VALUE": "3"],
                    ["SCHVAL_KEY": "SRC_DB_DETAIL", "SCHEVAL_VALUE": "DB1"],
                    ["SCHVAL_KEY": "SRC_USER", "SCHEVAL_VALUE": "USER1"],
                    ["SCHVAL_KEY": "TGT_DB_DETAIL", "SCHEVAL_VALUE": "DB2"],
                    ["SCHVAL_KEY": "TGT_USER", "SCHEVAL_VALUE": "USER2"],
                    ["SCHVAL_KEY": "COMPARISION_TYPE", "SCHEVAL_VALUE": "0"],
            ]
        }

        when:
        Map result = controller.getLatestComparisonInfo()

        then:
        result.isRunning == false
        result.isSuccess == true
        result.type == "0"
        result.label == "SRC:DB1/USER1 VS TGT:DB2/USER2"

    }

    void "test index()"() {
        given:
        controller.metaClass.getLatestComparisonInfo = { [isRunning: false, isSuccess: true, label: "label", type: "1"] }
        when:
        Map result = controller.index()
        then:
        result.isRunning == false
        result.isSuccess == true
        result.type == "1"
    }

    void "test data()"() {
        given:
        controller.metaClass.getLatestComparisonInfo = { [isRunning: false, isSuccess: true, label: "label", type: "1"] }
        controller.dataSource_schemaComparator = new JdbcDataSource()
        Sql.metaClass.rows = { String sql ->
            if (sql.contains("count(1)"))
                return [["5"]]
            else
                return [["SCHVAL_DIFF"], ["RCONGIF"], ["EX_RCONGIF"]]
        }

        when:
        Map result = controller.data()

        then:
        result.tables.size() == 2
        result.tables[0].name == "RCONGIF"
        result.tables[0].count == 5
    }

    void "test dataDiff()"() {
        given:
        controller.dataSource_schemaComparator = new JdbcDataSource()
        controller.metaClass.getFieldsType = { String tableName ->
            [
                    [columnName: "string", dataType: Types.VARCHAR, isSortable: true],
                    [columnName: "number", dataType: Types.BIGINT, isSortable: true],
                    [columnName: "date", dataType: Types.TIMESTAMP, isSortable: true]
            ]
        }
        params.table = "tabe"

        when:
        def result = controller.dataDiff()

        then:
        result.fields.size() == 3
    }

    void "test createWhere()"() {
        given:
        List<Map> fields = [
                [columnName: "string", dataType: Types.VARCHAR, isSortable: true],
                [columnName: "number", dataType: Types.BIGINT, isSortable: true],
                [columnName: "date", dataType: Types.TIMESTAMP, isSortable: true]
        ]
        String search = "search"
        String tableFilter = """{
"string":{"name":"string", "value":"fltrString"},
"number":{"name":"number", "value":"fltrNumber"},
"date":{"name":"date", "value":"fltrDate"}
}"""
        when:
        String result = controller.createWhere(fields, search, tableFilter)

        then:
        result == " and ( UPPER(string) like '%SEARCH%' or cast( number AS char( 256 ) ) like '%SEARCH%' or TO_CHAR(date,'DD-MON-YYYY HH24:MI:SS') LIKE  '%SEARCH%') and TO_CHAR(date,'DD-MON-YYYY HH24:MI:SS') LIKE  '%FLTRDATE%' and cast( number AS char( 256 ) ) like '%FLTRNUMBER%' and  UPPER(string) like '%FLTRSTRING%'";
    }

    void "test runCompare()"() {
        given:
        controller.metaClass.getLatestComparisonInfo = { [isRunning: false, isSuccess: true, label: "label", type: "1"] }
        controller.metaClass.getComparatorDataSource = { new JdbcDataSource() }
        Sql.metaClass.call = { String sql, List list -> }

        when:
        controller.runCompare()

        then:
        response.status == 302
        flash.message == "Schema comparison process started successfully!"
    }

    void "test dataDiffList()"() {
        given:
        controller.dataSource_schemaComparator = new JdbcDataSource()
        controller.metaClass.getLatestComparisonInfo = { [isRunning: false, isSuccess: true, label: "label", type: "1"] }
        controller.metaClass.getComparatorDataSource = { new JdbcDataSource() }
        controller.metaClass.createWhere = { List<Map> fields, String search -> return " and some_where_clause " }
        controller.metaClass.getNormalizedValue = { Object val, Integer dataType -> return val }
        controller.metaClass.getFieldsType = { String tableName ->
            [
                    [columnName: "string", dataType: Types.VARCHAR, isSortable: true],
                    [columnName: "number", dataType: Types.BIGINT, isSortable: true]
            ]
        }
        String resultSql
        Sql.metaClass.rows = { String sql ->
            if (sql.contains("count")) {
                return [[2]]
            } else {
                resultSql = sql;
                return [[getProperty: { String f -> return f == "string" ? "sVal" : 1 }], [getProperty: { String f -> return f == "string2" ? "sVal" : 2 }]]
            }
        }
        params.table = "table"
        when:
        controller.dataDiffList()

        then:
        response.json.aaData.size() == 2

    }
}
