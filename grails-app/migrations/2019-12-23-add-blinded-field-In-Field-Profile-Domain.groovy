databaseChangeLog = {

    changeSet(author: "sargam (generated)", id: "231220190720-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'USER_GROUP', columnName: 'IS_PROTECTED')
            }
        }
        addColumn(tableName: "USER_GROUP") {
            column(name: "IS_PROTECTED", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "231220190720-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'IS_PROTECTED')
            }
        }
        addColumn(tableName: "PVUSER") {
            column(name: "IS_PROTECTED", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "231220190720-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'field_profile_rpt_field', columnName: 'FIELD_PROFILE_PROTECTED_ID')
            }
        }
        addColumn(tableName: "field_profile_rpt_field") {
            column(name: "FIELD_PROFILE_PROTECTED_ID", type: "number(19,0)")
        }
        addForeignKeyConstraint(baseColumnNames: "FIELD_PROFILE_PROTECTED_ID", baseTableName: "field_profile_rpt_field", constraintName: "FK_l1202rs5uo2ewldbx29vgsk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FIELD_PROFILE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sargam (generated)", id: "231220190720-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'FIELD_PROFILE_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "FIELD_PROFILE_ID", type: "number(19,0)")
        }
        addForeignKeyConstraint(baseColumnNames: "FIELD_PROFILE_ID", baseTableName: "RCONFIG", constraintName: "FK_l1202rs5uo2ewldbx29fpid", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FIELD_PROFILE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sargam (generated)", id: "231220190720-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FIELD_PROFILE_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "FIELD_PROFILE_ID", type: "number(19,0)")
        }
        addForeignKeyConstraint(baseColumnNames: "FIELD_PROFILE_ID", baseTableName: "EX_RCONFIG", constraintName: "FK_l1202rs5uo2ewldbex19fpid", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FIELD_PROFILE", referencesUniqueColumn: "false")
    }
}