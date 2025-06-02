databaseChangeLog = {

    changeSet(author: "anurag  (generated)", id: "2208202032546-1") {
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "AUTHORIZATION_TYPE", tableName: "ICSR_TEMPLT_QUERY")
        dropNotNullConstraint(columnDataType: "NUMBER(19,0)", columnName: "DUE_DAYS", tableName: "ICSR_TEMPLT_QUERY")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DIST_CHANNEL_NAME", tableName: "ICSR_TEMPLT_QUERY")
        dropNotNullConstraint(columnDataType: "NUMBER(19,0)", columnName: "ORDER_NO", tableName: "ICSR_TEMPLT_QUERY")
    }

    changeSet(author: "anurag  (generated)", id: "2208222032546-2") {
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "AUTHORIZATION_TYPE", tableName: "EX_ICSR_TEMPLT_QUERY")
        dropNotNullConstraint(columnDataType: "NUMBER(19,0)", columnName: "DUE_DAYS", tableName: "EX_ICSR_TEMPLT_QUERY")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DIST_CHANNEL_NAME", tableName: "EX_ICSR_TEMPLT_QUERY")
        dropNotNullConstraint(columnDataType: "NUMBER(19,0)", columnName: "ORDER_NO", tableName: "EX_ICSR_TEMPLT_QUERY")
    }
}