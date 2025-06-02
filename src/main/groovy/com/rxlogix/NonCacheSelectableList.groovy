package com.rxlogix

import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
class NonCacheSelectableList implements SelectableList {
    def dataSource
    String sqlString

    @Override
    List<Object> getSelectableList(String lang) {
        //No implementation as we don't want to cache.
        return []
    }

    List<Object> getPaginatedSelectableList(String searchTerm, int max, int offset, int tenantId) {
        List list  = sqlString.split(" ")
        def wildCharacterStrings = '%_'
        wildCharacterStrings.each{
            if(searchTerm.contains(it)){
                searchTerm = searchTerm.replaceAll("${it}", "=${it}")
            }
        }
        def searchTermIndex = -1;
        int idx = 0;
        for(x in list){
            if(x==':SEARCH_TERM'){
                searchTermIndex = idx
                break;
            }
            idx = idx+1

        }
        if(searchTermIndex!=-1){
            list.add(searchTermIndex+1,"ESCAPE "+"'='")
        }
        def newSqlString = list.join(" ")
        Sql sql = new Sql(dataSource)
        List<Object> result = [];
        try {
            // The SQL only has one column to return
            result = sql.rows(newSqlString + " OFFSET :offset ROWS FETCH NEXT :max ROWS ONLY", [max: max, offset: offset, TENANT_ID: tenantId, SEARCH_TERM: (searchTerm ? "%${searchTerm}%" : '%').toUpperCase()]).collect {
                it[0]
            }
        } catch (Exception e) {
            log.error("exception while executing sql in table column selectable list for SQL ${sqlString} with message ${e.message}")
        } finally {
            sql?.close()
        }
        return result
    }
}