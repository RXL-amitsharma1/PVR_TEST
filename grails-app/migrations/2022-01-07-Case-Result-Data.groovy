databaseChangeLog = {

    changeSet(author: "Sachin Verma", id: "202201071850-1") {
        createTable(tableName: "CASE_RESULT_DATA") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_RESULT_DATA_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "TOTAL_TIME", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "VALUE", type: "blob")

            column(name: "IS_ENCRYPTED", type: "number(1,0)"){
                constraints(nullable: "false")
            }

            column(name: "CASE_NUM", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "VERSION_NUM", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "EXECUTED_ON", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Sachin Verma", id: "202201071850-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'AUTO_SCHEDULING')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "AUTO_SCHEDULING", type: "NUMBER(19,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Sachin Verma", id: "202201071850-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'AUTO_SCHEDULING')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AUTO_SCHEDULING", type: "NUMBER(19,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Sachin Verma", id: "202201071850-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'MANUAL_SCHEDULING')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "MANUAL_SCHEDULING", type: "NUMBER(19,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Sachin Verma", id: "202201071850-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'MANUAL_SCHEDULING')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "MANUAL_SCHEDULING", type: "NUMBER(19,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Sachin Verma", id: "202201071850-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'CASE_RESULT_DATA', columnName: 'EXECUTED_ON')
        }
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "EXECUTED_ON", tableName: "CASE_RESULT_DATA")
    }

    changeSet(author: "anurag (generated)", id: "20229151217-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'AUTO_GENERATE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "AUTO_GENERATE", type: "NUMBER(1,0)", defaultValue: 1) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "20229151217-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'AUTO_GENERATE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AUTO_GENERATE", type: "NUMBER(1,0)", defaultValue: 1) {
                constraints(nullable: "true")
            }
        }
    }
}