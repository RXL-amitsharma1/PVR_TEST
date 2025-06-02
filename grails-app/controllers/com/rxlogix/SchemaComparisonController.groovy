package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import grails.converters.JSON
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.security.access.annotation.Secured

import java.sql.ResultSetMetaData
import java.sql.Types

@Secured(["ROLE_ADMIN"])
class SchemaComparisonController implements SanitizePaginationAttributes {
    def dataSource_schemaComparator
    def qualityService

    protected getComparatorDataSource() {
        return dataSource_schemaComparator
    }

    def index() {
        getLatestComparisonInfo()
    }

    Map getLatestComparisonInfo() {
        Sql sql = new Sql(getComparatorDataSource())
        Boolean isRunning = false
        Boolean isSuccess = true
        String label = "-"
        String type = "-"
        try {
            Map data = [:] //SCHVAL_STATUS
            sql.rows("SELECT *  FROM SCHVAL_CONSTANTS ")?.each {
                data.put(it["SCHVAL_KEY"]?.toString(), it["SCHEVAL_VALUE"]?.toString())
            }

            isRunning = data.get("SCHVAL_STATUS")?.toString() == "1"
            isSuccess = data.get("SCHVAL_STATUS")?.toString() == "3"
            label = "SRC:" + data.get("SRC_DB_DETAIL") + "/" + data.get("SRC_USER") + " VS TGT:" + data.get("TGT_DB_DETAIL") + "/" + data.get("TGT_USER")
            type = data.get("COMPARISION_TYPE")?.toString()


        } finally {
            sql?.close()
        }
        [isRunning: isRunning, isSuccess: isSuccess, label: label, type: type]
    }


    def data() {

        List result = []
        Map comparisonInfo = getLatestComparisonInfo()
        if (!comparisonInfo.isRunning && comparisonInfo.type != "0" && comparisonInfo.isSuccess) {

            Sql sql = new Sql(getComparatorDataSource())
            List tables = sql.rows("SELECT DISTINCT OBJECT_NAME FROM USER_OBJECTS WHERE OBJECT_TYPE = 'TABLE' and OBJECT_NAME like '%_DIFF'")?.collect { it[0]?.toString() }
            try {
                tables.each {
                    if (!(it in ["SCHVAL_DIFF", "V\$PARAMETER_DIFF", "USER_SYS_PRIVS_DIFF", "ALL_TAB_PRIVS_DIFF"])) {
                        def rows = sql.rows("SELECT count(1) FROM " + it)
                        int count = rows[0]?.getAt(0) as Integer
                        result << [name: it, count: count]
                    }
                }

            } finally {
                sql?.close()
            }
        }
        [tables: result] + comparisonInfo
    }

    def dataDiff() {
        String tableName = params.table
        [fields: getFieldsType(tableName)]
    }

    def runCompare() {
        Map stat = getLatestComparisonInfo()
        if (!stat.isRunning) {
            Sql sql = new Sql(getComparatorDataSource())
            try {
                sql.call('{call  PKG_SCHVAL_UTILITY.p_execute_schval(?,?,?,?,?,?,?)}', [params.DB1, params.DB2, params.schema1, params.schema2, params.pass1, params.pass2, params.profile])
                flash.message = "Schema comparison process started successfully!"
            } catch (Exception e) {
                String s = e.getMessage()
                if (s.indexOf("ISSUE OCCURRED WITH DB LINKS")) s = "Can not connect to schema";
                log.error("Error occurred starting schema comparison process", e)
                flash.error = "Error occurred starting schema comparison process, please check that schemas parameters are correct:" + s
            } finally {
                sql?.close()
            }
        } else {
            flash.error = "Comparison is already running!"
        }
        redirect(url: request.getHeader('referer'))
    }

    def dataDiffList() {
        String tableName = params.table
        params.order = params.order ?: "asc"
        sanitize(params)
        render fetchDataFromTable(tableName, params.searchString, params.tableFilter, params.sort, params.order, params.offset, params.max) as JSON
    }

    Map fetchDataFromTable(String tableName, String searchString, String tableFilter, String sort, String order, Integer offset, Integer max) {
        Sql sql = new Sql(getComparatorDataSource())
        List fields = getFieldsType(tableName)
        def result = [aaData: [], fields: fields]
        try {

            if (tableName) {
                String sortString = sort ? " order by " + sort + " " + order : ""
                String where = createWhere(fields, searchString, tableFilter)
                String sqlString = "SELECT * FROM (SELECT  t.*, ROWNUM rn  FROM ( " +
                        "select * from " + tableName + " where 1=1 " + where + sortString +
                        ") t WHERE rownum <= ${offset + max} ) WHERE rn > ${offset}"

                List<GroovyRowResult> rows = sql.rows(sqlString)


                rows.eachWithIndex { data, index ->
                    def row = [:]
                    for (int i = 0; i < fields.size(); i++) {
                        def val = getNormalizedValue(data.getProperty(fields[i].columnName), fields[i].dataType)
                        row[fields[i].columnName] = val
                    }
                    result.aaData << row
                }
                sqlString = "select count(*) from " + tableName + " where 1=1 " + where
                rows = sql.rows(sqlString)
                result.recordsFiltered = rows[0]?.getAt(0) as Integer
                sqlString = "select count(*) from " + tableName
                rows = sql.rows(sqlString)
                result.recordsTotal = rows[0]?.getAt(0) as Integer
            }
        } finally {
            sql?.close()
        }
        return result
    }

    private Object getNormalizedValue(Object val, Integer dataType) {
        if (val && (dataType == Types.CLOB)) {
            if (val.length() < 1000) {
                return val.getSubString(1, (int) val.length())
            } else {

                return val.getSubString(1, 1000) + "...(1000 first charactes shown of ${val.length()})"
            }
        }
        if (val && (dataType == Types.DATE)) return (val as Date).format("dd-MMM-yyyy hh:mm:ss")
        if (val && (dataType == Types.TIMESTAMP)) {
            if (val instanceof oracle.sql.TIMESTAMP) {
                return new Date(java.sql.Timestamp.valueOf(val.toString()).getTime()).format("dd-MMM-yyyy hh:mm:ss")
            } else {
                return (val as Date).format("dd-MMM-yyyy hh:mm:ss")
            }
        }
        return val?.toString() ?: ""
    }

    protected List<Map> getFieldsType(String tableName) {
        List<Map> allowedFields = []
        Sql sql = new Sql(getComparatorDataSource())
        try {
            sql.rows("SELECT * FROM " + tableName + "  WHERE ROWNUM <= 1") { ResultSetMetaData meta ->
                for (int i = 1; i <= meta.columnCount; i++) {
                    Boolean sortable = (meta.getColumnType(i) in [Types.VARCHAR, Types.NCHAR, Types.CHAR, Types.DECIMAL, Types.BIGINT, Types.DOUBLE, Types.FLOAT, Types.INTEGER, Types.NUMERIC, Types.TINYINT, Types.DATE, Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE])
                    allowedFields << [columnName: meta.getColumnName(i), dataType: meta.getColumnType(i), isSortable: sortable]
                }
            }
        } finally {
            sql?.close()
        }
        allowedFields
    }

    protected createWhere(List<Map> fields, String search, tableFilter) {
        StringBuilder sb = new StringBuilder()
        if (search) {
            String s = search.replaceAll("'", "''").toUpperCase()
            sb.append(" and (")
            fields.eachWithIndex { it, i ->
                if (i > 0) sb.append(" or ")
                sb.append(getLikeExpression(it.dataType, it.columnName, s))
            }
            sb.append(")")
        }
        if (tableFilter) {
            JSON.parse(tableFilter).each { filter ->
                def field = fields.find { it.columnName == filter.value.name }
                if (field && filter.value?.value) {
                    String s = filter.value.value.replaceAll("'", "''").toUpperCase()
                    sb.append(" and " + getLikeExpression(field.dataType, field.columnName, s))
                }
            }
        }
        return sb.toString()
    }

    private String getLikeExpression(int dataType, String columnName, String s) {
        if (dataType in [Types.VARCHAR, Types.NCHAR, Types.CHAR, Types.CLOB, Types.NCLOB, Types.LONGNVARCHAR, Types.NVARCHAR, Types.LONGVARCHAR]) return (" UPPER(${columnName}) like '%${s}%'")
        if (dataType in [Types.DECIMAL, Types.BIGINT, Types.DOUBLE, Types.FLOAT, Types.INTEGER, Types.NUMERIC, Types.TINYINT]) return ("cast( ${columnName} AS char( 256 ) ) like '%${s}%'")
        if (dataType in [Types.DATE, Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE]) return ("TO_CHAR(${columnName},'DD-MON-YYYY HH24:MI:SS') LIKE  '%${s}%'")

    }

    def exportToExcel() {
        String tableName = params.table
        params.order = params.order ?: "asc"
        sanitize(params)
        Map output = fetchDataFromTable(tableName, params.searchString, params.tableFilter, params.sort, params.order, 0, Integer.MAX_VALUE)
        def metadata = [sheetName: tableName,
                        columns  : output.fields.collect { [title: it.columnName, width: 25] }]
        List data = output.aaData.collect { row ->
            output.fields.collect { field -> row[field.columnName] }
        }
        byte[] file = qualityService.exportToExcel(data, metadata)
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: System.currentTimeMillis() + ".xlsx")
    }
}
