package com.rxlogix

import com.rxlogix.config.OdataSettings
import com.rxlogix.odata.*
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.ODataApplicationException
import org.apache.olingo.server.api.ODataHttpHandler
import org.apache.olingo.server.api.ServiceMetadata
import org.apache.olingo.commons.api.edmx.EdmxReference
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.ResultSetMetaData

class OdataService {

    static transactional = false

    def userService

    DataSource getDS(String dsName) {
        return OdataSettingsCache.getDS(dsName)
    }

    String getDSUserName(String dsName) {
        return OdataSettings.findByDsNameAndIsDeleted(dsName,false).dsLogin
    }

    List<Map> getFieldsType(String dsName, String tableName, def configAllowedFields) {
        List<Map> allowedFields = []
        Boolean isAllFieldsAllowed = (configAllowedFields == "All")
        Sql sql = new Sql(getDS(dsName))
        try {
            //using "select * from" to get MetaData 10 times faster then using DatabaseMetaData md = dataSource_pva.getConnection().getMetaData();
            sql.rows("SELECT * FROM " + tableName + "  WHERE ROWNUM <= 1") { ResultSetMetaData meta ->
                int count = meta.columnCount
                for (int i = 1; i <= count; i++) {
                    def fieldConfig = isAllFieldsAllowed ? null : configAllowedFields.find { k, v ->
                        (v == meta.getColumnName(i))
                    }
                    def label = isAllFieldsAllowed ? meta.getColumnName(i) : fieldConfig?.key
                    if (label)
                        allowedFields << [columnName: meta.getColumnName(i),
                                          label     : label,
                                          dataType  : meta.getColumnType(i)
                        ]
                }
            }
        } finally {
            sql?.close()
        }
        allowedFields
    }

    Map getDataForEntity(String dsName, String tableName, String configLimitQuery, configAllowedFields, Map params) {
        DataSource dataSource = getDS(dsName)
        Sql sql = new Sql(dataSource)
        def result = [data: [], meta: [:]]
        try {
            List<String> requestedFields = params.select?.split(",")?.collect { it.trim() }?.findAll { it }
            if (!tableName) throw new IllegalArgumentException("Entity not found!")
            String limitQuery = configLimitQuery ?: ""

            int max = params.top ? params.top as Integer : 20
            if (max < 0) max = 20
            if (max > 1000) max = 1000
            int offset = params.skip ? params.skip as Integer : 0
            if (offset < 0) offset = 0
            String where = params.filter ? (" and (" + params.filter + ") ") : ""

            if (tableName) {
                def allowedFields = requestedFields ? configAllowedFields.findAll { (it.label in requestedFields) } : configAllowedFields
                String sort = formOrderbySubQuery(params.orderby, allowedFields) ?: ""
                def values = params.paramValues ?: []

                if (limitQuery) limitQuery = " and (" + limitQuery + ") "
                String sqlString = "SELECT * FROM (SELECT  t.*, ROWNUM rn  FROM ( " +
                        "select * from " + tableName + " where 1=1 " + limitQuery + where + sort +
                        ") t WHERE rownum <= ${offset + max + 1} ) WHERE rn > ${offset}"

                List<GroovyRowResult> rows = sql.rows(sqlString, values)
                if (rows.size() > max) {
                    result.meta << [hasNext: true]
                }
                rows.eachWithIndex { data, index ->
                    if (index < max) {
                        def row = []
                        for (int i = 0; i < allowedFields.size(); i++) {
                            def val = getNormalizedValue(data.getProperty(allowedFields[i].columnName), allowedFields[i].dataType)
                            row << [label: allowedFields[i].label, value: val, dataType: allowedFields[i].dataType]
                        }
                        result.data.add(row)
                    }
                }
                if (params.count) {
                    sqlString = "select count(*) from " + tableName + " where 1=1 " + limitQuery + where
                    rows = sql.rows(sqlString, values)

                    result.meta << [count: rows[0]?.getAt(0) as Integer]
                }
            }
        } finally {
            sql?.close()
        }
        return result
    }

    List<Map> getEntity(String dsName, String tableName, String configLimitQuery, allowedFields, id) {
        Sql sql = new Sql(getDS(dsName))
        List<Map> result = []
        try {
            if (!tableName) throw new IllegalArgumentException("Entity not found!")
            String limitQuery = configLimitQuery ?: ""
            String idColumn = allowedFields.find { it.label == "ID" }?.columnName
            if (tableName && idColumn) {
                if (limitQuery) limitQuery = " and (" + limitQuery + ") "
                String sqlString = "select * from " + tableName + " where " + idColumn + " = :id " + limitQuery

                List<GroovyRowResult> rows = sql.rows(sqlString, ["id": id])
                def r = rows[0]
                if (r) {
                    for (int i = 0; i < allowedFields.size(); i++) {
                        def val = getNormalizedValue(r.getProperty(allowedFields[i].columnName), allowedFields[i].dataType)
                        result << [label: allowedFields[i].label, value: val, dataType: allowedFields[i].dataType]
                    }
                }
            }
        } finally {
            sql?.close()
        }
        return result
    }

    protected static String formOrderbySubQuery(String orderby, List allowedFields) {
        Set<Map> subQueryEntries = (orderby?.split(",")?.collect {
            List<String> record = it.trim().tokenize(" ").collect { s -> s.trim() }
            String order = "asc"
            if (record[1] && record[1] == "desc") order = "desc"
            String columnName = allowedFields.find { (it.label == record[0] && (it.dataType < 1000)) }?.columnName
            if (!columnName) throw new IllegalArgumentException("Illegal field name in 'orderby' parameter!")
            [sql: (columnName + " " + order), labels: it.trim()]
        } as Set)?.findAll { it.sql }
        String order = subQueryEntries?.collect { it.sql }?.join(",")
        return order ? (" order by " + order) : ""
    }

    List<String> getDsTables(String dsName) {
        List<String> result = []
        if (dsName) {
            DatabaseMetaData md = null
            try {
                md = getDS(dsName).getConnection().getMetaData();
                String[] types = ["TABLE", "VIEW"]
                ResultSet rs = md.getTables(null, getDSUserName(dsName).toUpperCase(), "%", types);
                while (rs.next()) {
                    result << rs.getString(3)
                }
            } catch (Exception e) {
                e.printStackTrace(System.out)
            } finally {
                md?.connection?.close()
            }
        }
        result
    }

    Map getDsTableFields(String dsName, String tableName) {
        Sql sql = new Sql(getDS(dsName))
        Map result = [:]
        if (tableName.contains("'") || tableName.contains("\"") || tableName.contains("(") || tableName.contains(")") || tableName.contains(" ") || tableName.contains(";"))
            throw new IllegalArgumentException("Table name contains forbidden symbols!")
        try {
            //using "select * from" to get MetaData 10 times faster then using DatabaseMetaData md = dataSource_pva.getConnection().getMetaData();
            sql.rows("SELECT * FROM " + tableName + "  WHERE ROWNUM <= 1") { ResultSetMetaData meta ->
                int count = meta.columnCount
                for (int i = 1; i <= count; i++) {
                    result << [(toCamelCase(meta.getColumnName(i))): meta.getColumnName(i)]
                }
            }
        } finally {
            sql?.close()
        }
        String entity = toCamelCase(tableName);
        entity = entity.substring(0, 1).toUpperCase() + entity.substring(1)
        [entity: entity, fields: result]
    }

    String toCamelCase(String name) {
        if (name.equalsIgnoreCase("id")) return "ID"
        List<String> result = []
        name.split("_").collect { it.toLowerCase() }.eachWithIndex { it, index ->
            if (index > 0)
                result << (it.substring(0, 1).toUpperCase() + it.substring(1))
            else
                result << it
        }
        result.join()
    }


    @Transactional
    void createEntity(String dsName, String tableName, List columns, List values) throws ODataApplicationException {
        if (!userService.isCurrentUserAdmin())
            throw new ODataApplicationException("Operation forbidden for current user", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ROOT);
        Sql sql = new Sql(getDS(dsName))
        if (tableName.contains("'") || tableName.contains("\"") || tableName.contains("(") || tableName.contains(")") || tableName.contains(" ") || tableName.contains(";"))
            throw new IllegalArgumentException("Table name contains forbidden symbols!")
        if (columns.size() != values.size())
            throw new IllegalArgumentException("Number of columns not equals to number of values!")
        try {

            String sqlString = "insert into " + tableName + "(" + columns.join(",") + ") values (" + columns.collect { "?" }.join(",") + ")"

            sql.executeUpdate(sqlString, values)
        } finally {
            sql?.close()
        }
    }

    @Transactional
    int updateEntity(String dsName, String tableName, List columns, List values, id, String limitQuery = "") throws ODataApplicationException {
        if (!userService.isCurrentUserAdmin())
            throw new ODataApplicationException("Operation forbidden for current user", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ROOT);
        Sql sql = new Sql(getDS(dsName))
        if (tableName.contains("'") || tableName.contains("\"") || tableName.contains("(") || tableName.contains(")") || tableName.contains(" ") || tableName.contains(";"))
            throw new IllegalArgumentException("Table name contains forbidden symbols!")
        if (columns.size() != values.size())
            throw new IllegalArgumentException("Number of columns not equals to number of values!")
        try {
            String sqlString = "update " + tableName + " set " + columns.join("=?,") + "=? where ID = ?" + (limitQuery ? " and (" + limitQuery + ") " : "")
            return sql.executeUpdate(sqlString, (values + [id]))
        } finally {
            sql?.close()
        }
    }

    @Transactional
    int deleteEntity(String dsName, String tableName, id, String limitQuery = "") throws  ODataApplicationException{
        if (!userService.isCurrentUserAdmin())
            throw new ODataApplicationException("Operation forbidden for current user", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ROOT);
        Sql sql = new Sql(getDS(dsName))
        if (tableName.contains("'") || tableName.contains("\"") || tableName.contains("(") || tableName.contains(")") || tableName.contains(" ") || tableName.contains(";"))
            throw new IllegalArgumentException("Table name contains forbidden symbols!")

        try {
            String sqlString = "delete from " + tableName + " where ID = ? " + (limitQuery ? " and (" + limitQuery + ") " : "")
            return sql.executeUpdate(sqlString, [id])
        } finally {
            sql?.close()
        }
    }

    ODataHttpHandler getODataHttpHandler(){
        OData odata = OData.newInstance()
        ServiceMetadata edm = odata.createServiceMetadata(new OdataEdmProvider(), new ArrayList<EdmxReference>())
        ODataHttpHandler handler = odata.createHandler(edm)
        handler.register(new OdataEntityCollectionProcessor())
        handler.register(new OdataEntityProcessor())
        handler.register(new OdataPrimitiveProcessor())
        return handler
    }

    boolean checkIfDSExist(String dsName) {
        OdataSettings odataSettings = OdataSettings.findByDsNameAndIsDeleted(dsName, false)
        if (!odataSettings) {
            return false
        }
        if (odataSettings.lastUpdated != OdataSettingsCache.getDsCacheDate(dsName)) {
            OdataSettingsCache.clearCacheAndUpdateDateForDs(dsName, odataSettings.lastUpdated)
        }
        return true
    }

    private Object getNormalizedValue(Object val,Integer dataType){
        if (val && OdataUtils.isClob(dataType)) {
            if (val.length() < 4000) {
                return val.getSubString(1, (int) val.length())
            } else {
                log.debug("Huge Text column name: ${allowedFields[i].columnName} && size: ${val.length()}")
                return  "*** Huge Text can't extract ***"
            }
        }
        return val
    }

}
