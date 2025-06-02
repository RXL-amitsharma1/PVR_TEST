databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1457241970883-1") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "USED_FOR_CASES_EX_CONFIG_ID", type: "number(19,0)")
        }
    }


    changeSet(author: "sachinverma (generated)", id: "1457241970883-3") {
        addColumn(tableName: "RCONFIG") {
            column(name: "USE_CASES_EX_CONFIG_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457241970883-9") {
        modifyDataType(columnName: "SUBMISSION_DATE", newDataType: "VARCHAR(4000)", tableName: "RPT_SUBMISSION")
    }

    changeSet(author: "sachinverma (generated)", id: "1457241970883-10") {
        addForeignKeyConstraint(baseColumnNames: "USED_FOR_CASES_EX_CONFIG_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE8850B507D5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1457241970883-11") {
        addForeignKeyConstraint(baseColumnNames: "USE_CASES_EX_CONFIG_ID", baseTableName: "RCONFIG", constraintName: "FK68917214A18B40BA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

}
