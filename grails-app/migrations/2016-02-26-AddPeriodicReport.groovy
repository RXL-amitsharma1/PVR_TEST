databaseChangeLog = {
    changeSet(author: "sachinverma (generated)", id: "1456520183222-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DELIVERY', columnName: 'REPORT_ID')
            }
        }
        addColumn(tableName: "DELIVERY") {
            column(name: "REPORT_ID", type: "number(19,0)") {
                constraints(nullable: "true", unique: "false")
            }
        }
        sql("update DELIVERY delivery set REPORT_ID = (select ID from RCONFIG where DELIVERY_ID= delivery.ID)")
        addNotNullConstraint(tableName: "DELIVERY", columnName: "REPORT_ID")
        addUniqueConstraint(tableName: "DELIVERY", columnNames: "REPORT_ID", constraintName: "report_id_unique_c342", deferrable: "false", disabled: "false", initiallyDeferred: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456520183222-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_DELIVERY', columnName: 'EXECUTED_CONFIGURATION_ID')
            }
        }
        addColumn(tableName: "EX_DELIVERY") {
            column(name: "EXECUTED_CONFIGURATION_ID", type: "number(19,0)") {
                constraints(nullable: "true", unique: "false")
            }
        }
        sql("update EX_DELIVERY delivery set EXECUTED_CONFIGURATION_ID = (select ID from EX_RCONFIG where EX_DELIVERY_ID= delivery.ID)")
        addNotNullConstraint(tableName: "EX_DELIVERY", columnName: "EXECUTED_CONFIGURATION_ID")
        addUniqueConstraint(tableName: "EX_DELIVERY", columnNames: "EXECUTED_CONFIGURATION_ID", constraintName: "ex_conf_id_unique_c121", deferrable: "false", disabled: "false", initiallyDeferred: "false")

    }


    changeSet(author: "sachinverma (generated)", id: "1456520183222-31") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'EX_DELIVERY_ID')
        }
        dropColumn(columnName: "EX_DELIVERY_ID", tableName: "EX_RCONFIG")
    }

    changeSet(author: "sachinverma (generated)", id: "1456520183222-32") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'DELIVERY_ID')
        }
        dropColumn(columnName: "DELIVERY_ID", tableName: "RCONFIG")
    }

    changeSet(author: "sachinverma (generated)", id: "1456520183222-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'EX_DELIVERY', foreignKeyName: 'FK80034E00779FCE29')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "EXECUTED_CONFIGURATION_ID", baseTableName: "EX_DELIVERY", constraintName: "FK80034E00779FCE29", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456520183222-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'DELIVERY', foreignKeyName: 'FK5FBB0BF4717BD48A')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "REPORT_ID", baseTableName: "DELIVERY", constraintName: "FK5FBB0BF4717BD48A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }
}
