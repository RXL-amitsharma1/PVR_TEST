databaseChangeLog = {

    changeSet(author: "pragyatiwari (generated)", id: "143205042023-1") {
        createTable(tableName: "BALANCE_MINUS_QUERY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true")
            }
            column(name: "START_DATETIME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "REPEAT_INTERVAL", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "SELECTED_TIME_ZONE", type: "varchar2(255)", defaultValue: "UTC") {
                constraints(nullable: "true")
            }
            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "CREATED_BY", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "false")
            }
            column(name: "MODIFIED_BY", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "false")
            }
        }

        createTable(tableName: "BQMQ_SECTION"){
            column(name: "ID", type: "number(19,0)"){
                constraints(nullable:"false", primaryKey: "true")
            }
            column(name: "EXECUTE_FOR", type: "varchar2(50 char)") {
                constraints(nullable: "false")
            }
            column(name: "EXECUTION_START_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "EXECUTION_END_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "X_VALUE", type: "number(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "FLAG_CASE_EXCLUDE", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "SRC_PROFILE_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BMQUERY_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_SECTION_IDX", type: "NUMBER(10)"){
                constraints(nullable: "true")
            }
        }
        //addForeignKeyConstraint(baseColumnNames: "SRC_PROFILE_ID", baseTableName: "BALANCE_MINUS_QUERY_SECTION", constraintName: "FKBALANCE_MINUS_QUERY_SECTION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SOURCE_PROFILE")

        createTable(tableName: "BQMQ_INCLUDE_CASE"){
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true")
            }
            column(name: "CASE_NUMBER", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
            column(name: "SRC_PROFILE_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_SECTION_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_INCLUDE_CASE_IDX", type: "NUMBER(10)"){
                constraints(nullable: "true")
            }
        }

        createTable(tableName: "BQMQ_EXCLUDE_CASE"){
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true")
            }
            column(name: "CASE_NUMBER", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
            column(name: "SRC_PROFILE_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_SECTION_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_EXCLUDE_CASE_IDX", type: "NUMBER(10)"){
                constraints(nullable: "true")
            }
        }

        createTable(tableName: "BQMQ_DISTINCT_TABLE"){
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true")
            }
            column(name: "ENTITY", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
            column(name: "SRC_PROFILE_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_SECTION_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "BQMQ_DISTINCT_TABLE_IDX", type: "NUMBER(10)"){
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "anurag (generated)", id: "081725082023-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'BALANCE_MINUS_QUERY', columnName: 'DISABLED')
            }
        }
        addColumn(tableName: "BALANCE_MINUS_QUERY") {
            column(name: "DISABLED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "081725082023-3") {
        addForeignKeyConstraint(baseColumnNames: "BQMQ_SECTION_ID", baseTableName: "BQMQ_DISTINCT_TABLE", constraintName: "FK50866F07B2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "BQMQ_SECTION", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "BQMQ_SECTION_ID", baseTableName: "BQMQ_INCLUDE_CASE", constraintName: "FK50866F08B2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "BQMQ_SECTION", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "BQMQ_SECTION_ID", baseTableName: "BQMQ_EXCLUDE_CASE", constraintName: "FK50866F09B2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "BQMQ_SECTION", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "BMQUERY_ID", baseTableName: "BQMQ_SECTION", constraintName: "FK50866F06B2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "BALANCE_MINUS_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "081725082023-4") {
        modifyDataType(columnName:"CASE_NUMBER", tableName:"BQMQ_INCLUDE_CASE", newDataType:"VARCHAR2(32000 CHAR)")
    }

    changeSet(author: "anurag", id: "081725082023-5") {
        modifyDataType(columnName:"CASE_NUMBER", tableName:"BQMQ_EXCLUDE_CASE", newDataType:"VARCHAR2(32000 CHAR)")
    }

    changeSet(author: "anurag", id: "070920231133-6") {
        modifyDataType(columnName:"CASE_NUMBER", tableName:"BQMQ_INCLUDE_CASE", newDataType:"VARCHAR2(255 CHAR)")
        modifyDataType(columnName:"CASE_NUMBER", tableName:"BQMQ_EXCLUDE_CASE", newDataType:"VARCHAR2(255 CHAR)")
        addNotNullConstraint(tableName: "BQMQ_INCLUDE_CASE", columnName: "CASE_NUMBER")
        addNotNullConstraint(tableName: "BQMQ_EXCLUDE_CASE", columnName: "CASE_NUMBER")
    }

    changeSet(author: "anurag (generated)", id: "081725082023-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'BQMQ_SECTION', columnName: 'INDX')
            }
        }
        addColumn(tableName: "BQMQ_SECTION") {
            column(name: "INDX", type: "number(10,0)", defaultValue:0) {
                constraints(nullable: "true")
            }
        }
    }

}