databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1510826186962-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'USER_GROUP', columnName: 'IS_BLINDED')
            }
        }
        addColumn(tableName: "USER_GROUP") {
            column(name: "IS_BLINDED", type: "number(1,0)")
        }
        sql("update USER_GROUP set IS_BLINDED = 0;")
        addNotNullConstraint(tableName: "USER_GROUP", columnName: "IS_BLINDED")
    }

    changeSet(author: "forxsv (generated)", id: "1510826186962-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'IS_BLINDED')
            }
        }
        addColumn(tableName: "PVUSER") {
            column(name: "IS_BLINDED", type: "number(1,0)")
        }
        sql("update PVUSER set IS_BLINDED = 0;")
        addNotNullConstraint(tableName: "PVUSER", columnName: "IS_BLINDED")
    }

    changeSet(author: "forxsv (generated)", id: "1510826186962-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'field_profile_rpt_field', columnName: 'field_profile_blinded_id')
            }
        }
        addColumn(tableName: "field_profile_rpt_field") {
            column(name: "field_profile_blinded_id", type: "number(19,0)")
        }
        addForeignKeyConstraint(baseColumnNames: "field_profile_blinded_id", baseTableName: "field_profile_rpt_field", constraintName: "FK_l6665rs5uo2ewldbx29vvxl6a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FIELD_PROFILE", referencesUniqueColumn: "false")
    }

}
