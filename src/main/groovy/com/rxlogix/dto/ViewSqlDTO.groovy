package com.rxlogix.dto

import oracle.sql.CLOB

/**
 * Created by prashantsahi on 13/11/17.
 */
class ViewSqlDTO {
    String rowId
    String scriptName
    String executingSql
    String executionTime
    String rowsUpsert

    ViewSqlDTO(def resultSet) {
        rowId = resultSet.ROWNUM
        scriptName = resultSet.SCRIPT_NAME
        executingSql = clobToText(resultSet.EXECUTING_SQL)
        executionTime = (Long) Math.ceil(resultSet.EXECUTION_TIME_MINS as Double)
        rowsUpsert = resultSet.ROW_COUNT
    }

    String clobToText(CLOB clob) {
        clob?.characterStream?.text
    }
}
