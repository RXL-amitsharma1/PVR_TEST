package com.rxlogix

import com.rxlogix.config.CustomSQLTemplate
import com.rxlogix.config.QuerySet
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.SuperQuery
import com.rxlogix.util.MiscUtil
import grails.util.Holders
import groovy.sql.Sql

import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.util.regex.Matcher

class SqlService {

    static transactional = false
    def dataSource_pva
    def dataSource
    def grailsApplication
    public static String COLUMN_LABEL_REGEX_CONSTANT = /[ ぁ-ゔゞァ-・ヽヾ゛゜ーa-zA-Z0-9-_々〆〤①-⑨￥#一-龯ｧ-ﾝﾞﾟぁ-ゞ日本にある１つケーキ)「′、「」、。・（）{}]+/

    public boolean validateCustomSQL(String toValidate, boolean usePvrDB) {
        if(toValidate.toUpperCase().contains("SKIP_VALIDATION")){
            return true
        }
        Sql sql = new Sql(dataSource_pva)
        if (usePvrDB) {
            sql = new Sql(dataSource)
        }
        try {
            sql.firstRow(toValidate)
        } catch (Exception e) {
            log.error("Failed to validate SQL statement: " + toValidate + " --> "+ e.localizedMessage)
            return false
        } finally {
            sql?.close()
        }
        return true
    }
    String removeTableWithOracleTableAsString(String sqlToValidate){
        def Words_Between_Inverted_Comma = []
        def previdx = -1
        sqlToValidate.eachWithIndex{ch,idx->
            if(ch=="'"&&previdx ==-1){
                previdx = idx
            }
            else if(ch=="'"){
                Words_Between_Inverted_Comma.push(sqlToValidate.substring(previdx+1,idx))
                previdx=-1
            }
        }
        Words_Between_Inverted_Comma.each{it->
            String wordsConsideredAsString = it
            sqlToValidate = sqlToValidate.replace("'"+wordsConsideredAsString+"'","")

        }
        return sqlToValidate

    }
    Boolean validateTemplateQuerySQL(String sqlToValidate, Boolean usePvrDB = false) {
        Matcher matcher
        Sql sql = usePvrDB ? new Sql(dataSource) : new Sql(dataSource_pva)
        List<String> systemDatabaseTablesList = []
        List<String> excludeSystemTableList = grailsApplication.config.exclude.system.table.list ? grailsApplication.config.exclude.system.table.list.flatten() : []
        try {
            String sql_statement = "select table_name from dict"
            sql.eachRow(sql_statement, []) { row ->
                    systemDatabaseTablesList.add(/\b${row.TABLE_NAME.replace('$', '\\$')}\b/)
            }
            excludeSystemTableList = excludeSystemTableList.collect { tableNames ->
                /\b${tableNames.toUpperCase()}\b/
            }
            systemDatabaseTablesList -= excludeSystemTableList
        } catch (Exception ex) {
            log.error(ex.printStackTrace(), ex)
        } finally {
            sql?.close()
        }
        sqlToValidate = removeTableWithOracleTableAsString(sqlToValidate)
        systemDatabaseTablesList.any { tableName ->
            matcher = sqlToValidate.toUpperCase() =~ tableName
        }
        matcher.getCount() as Boolean
    }

    public List<String> getColumnsFromSqlQuery(String sqlQuery, boolean usePvrDB, boolean throwException) {
        Sql sql = new Sql(dataSource_pva)
        if (usePvrDB) {
            sql = new Sql(dataSource)
        }
        try {
            List<String> columnNamesList = []
            def metaDataClosure = { ResultSetMetaData resultSetMetaData ->
                for (int i = 1; i <= resultSetMetaData.columnCount; i++) {
                    String currentCol = resultSetMetaData.getColumnLabel(i)
                    columnNamesList.add(currentCol)
                }
            }
            sql.rows(sqlQuery, 1, 1, metaDataClosure)
            return columnNamesList
        } catch (SQLException e) {
            log.error("Failed to save the new column list names")
            if (throwException) {
                throw e
            }
        } finally {
            sql?.close()
        }
        return []
    }

    public boolean validateColumnName(String toValidate, boolean usePvrDB) {
        boolean invalidColumnName = true
        List<String> rejectedColumnNamesList = []
        try {
            List<String> columns = getColumnsFromSqlQuery(toValidate, usePvrDB, true)
            rejectedColumnNamesList = columns.findAll {
                (!(it ==~ COLUMN_LABEL_REGEX_CONSTANT))
            }
            if (!rejectedColumnNamesList && columns && columns.toListString().size() < CustomSQLTemplate.constrainedProperties.columnNamesList.maxSize) {
                invalidColumnName = false
            }
        } catch (e) {
            log.error(e.message)
        }
        if (rejectedColumnNamesList) {
            log.warn("Rejected columns: $rejectedColumnNamesList")
        }
        return !invalidColumnName
    }

    public boolean validateQuerySet(QuerySet querySet) {
        Set queries = getQueriesFromJSON(querySet.JSONQuery)
        boolean valid = true
        queries.each {
            if (!it || it.isDeleted) {
                valid = false
            }
        }
        return valid
    }

    // This is used to get all the queries referenced by this JSONQuery
    public List<SuperQuery> getQueriesFromJSON(String JSONQuery) {
        List<SuperQuery> result = []
        Map dataMap = MiscUtil.parseJsonText(JSONQuery)
        List containerGroupsList = dataMap.all.containerGroups
        for (int i = 0; i < containerGroupsList.size(); i++) {
            searchInsideGroup(containerGroupsList[i], result)
        }
        return result
    }

    private void searchInsideGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            List<SuperQuery> result) {
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                searchInsideGroup(expressionsList[i], result)
            }
        } else {
            if (groupMap.containsKey('query')) {
                SuperQuery current = SuperQuery.get(groupMap.query)
                result.add(current)
            }
        }
    }
}
