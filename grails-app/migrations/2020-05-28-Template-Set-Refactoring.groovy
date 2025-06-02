databaseChangeLog = {
    changeSet(author: "gologuzov (generated)", id: "1590667246453-1") {
        dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_SET", constraintName: "FK9AF470EB9C3FCD05")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-2") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_SET", constraintName: "FK6BB481DF88B11947")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-3") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_SET_CLL", constraintName: "FK97FEAEA346D1FA5E")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-4") {
        renameTable(oldTableName: "TEMPLT_SET_CLL", newTableName: "TEMPLT_SET_NESTED")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-5") {
        renameColumn(tableName: "TEMPLT_SET_NESTED", oldColumnName: "CLL_TEMPLT_ID", newColumnName: "NESTED_TEMPLT_ID")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-6") {
        renameColumn(tableName: "TEMPLT_SET_NESTED", oldColumnName: "CLL_TEMPLT_IDX", newColumnName: "NESTED_TEMPLT_IDX")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-7") {
        addForeignKeyConstraint(baseColumnNames: "NESTED_TEMPLT_ID", baseTableName: "TEMPLT_SET_NESTED", constraintName: "FK_oll0njbg7a0969i500grqktb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-8") {
        addColumn(tableName: "TEMPLT_SET") {
            column(name: "LINK_SECTIONS_BY_GROUPING", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-9") {
        addColumn(tableName: "TEMPLT_SET") {
            column(name: "SECTION_BREAK_BY_EACH_TEMPLATE", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1590667246453-10") {
        update(tableName: "TEMPLT_SET") {
            column(name: "LINK_SECTIONS_BY_GROUPING", valueNumeric: 1)
        }
    }
}