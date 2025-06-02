package com.rxlogix

import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
class TableColumnSelectableList implements SelectableList {
    def dataSource
    String sqlString

    @Override
    List<Object> getSelectableList(String lang) {
        Sql sql = new Sql(dataSource)
        List<Object> result=[];
        try {
            // The SQL only has one column to return
            result= sql.rows(sqlString).collect { it[0] }
        } catch (Exception e) {
            log.error("exception while executing sql in table column selectable list")
        } finally {
            sql?.close()
        }
        return result
    }
}