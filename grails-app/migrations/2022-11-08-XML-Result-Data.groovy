databaseChangeLog = {

    changeSet(author: "ShubhamRx", id: "20221108141912-1") {
        createTable(tableName: "XML_RESULT_DATA") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "XML_RESULT_DATA_PK")
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

            column(name: "EXECUTED_ON", type: "varchar2(255 char)")
        }
    }


    changeSet(author: "anurag (generated)", id: "202304241237-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_RESULT_DATA', columnName: 'IS_ATTACHMENT_EXIST')
            }
        }
        addColumn(tableName: "XML_RESULT_DATA") {
            column(name: "IS_ATTACHMENT_EXIST", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202304241237-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_RESULT_DATA', columnName: 'ATTACHMENT_DATA')
            }
        }
        addColumn(tableName: "XML_RESULT_DATA") {
            column(name: "ATTACHMENT_DATA", type: "blob")
        }
    }


}