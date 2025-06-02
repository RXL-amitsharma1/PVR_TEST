databaseChangeLog = {
    changeSet(author: "sargam (generated)", id: "201220190812-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'NEED_PAPER_REPORT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "NEED_PAPER_REPORT", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sargam (generated)", id: "201220190812-2") {
        addColumn(tableName: "ICSR_TEMPLT_QUERY") {
            column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "201220190812-3") {
        addForeignKeyConstraint(baseColumnNames: "EMAIL_CONFIGURATION_ID", baseTableName: "ICSR_TEMPLT_QUERY", constraintName: "FKC472BE889C7C456F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EMAIL_CONFIGURATION", referencesUniqueColumn: "false")
    }

    changeSet(author: "sargam (generated)", id: "201220190812-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EMAIL_CONFIGURATION', columnName: 'DELIVERY_RECEIPT')
            }
        }
        addColumn(tableName: "EMAIL_CONFIGURATION") {
            column(name: "DELIVERY_RECEIPT", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "201220190812-5") {
        addColumn(tableName: "EX_ICSR_TEMPLT_QUERY") {
            column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "201220190812-6") {
        addForeignKeyConstraint(baseColumnNames: "EMAIL_CONFIGURATION_ID", baseTableName: "EX_ICSR_TEMPLT_QUERY", constraintName: "FKC482BD889C7C456G", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EMAIL_CONFIGURATION", referencesUniqueColumn: "false")
    }

    changeSet(author: "sargam (generated)", id: "201220190812-7") {
        sql("update RCONFIG set INCLUDE_LOCKED_VERSION=1, EXCLUDE_NON_VALID_CASES=1 where class='com.rxlogix.config.IcsrProfileConfiguration'")
    }


}