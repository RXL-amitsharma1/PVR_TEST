databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1500013040665-1") {
        sql("UPDATE rpt_field_info t\n" +
                "   SET custom_expression = REPLACE(t.custom_expression, '''Blinded''', '''Redacted''')\n" +
                "  where BLINDED = 1;")
        renameColumn(tableName: "RPT_FIELD_INFO", oldColumnName: "BLINDED", newColumnName: "REDACTED")
    }

    changeSet(author: "prashantsahi (generated)", id: "1500013040665-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'BLINDED')
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "BLINDED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }

        sql("update RPT_FIELD_INFO set BLINDED = 0;")
        addNotNullConstraint(tableName: "RPT_FIELD_INFO", columnName: "BLINDED")
    }

    changeSet(author: "prashantsahi (generated)", id: "1500013040665-3") {
        sql("delete from localization");
    }
}
