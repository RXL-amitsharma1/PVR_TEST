databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202210121130-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_TEMPLT_NODE', columnName: 'CUSTOM_SQL_FIELD_INFO_ID')
            }
        }
        addColumn(tableName: "XML_TEMPLT_NODE") {
            column(name: "CUSTOM_SQL_FIELD_INFO_ID", type: "VARCHAR(256)")
        }
        sql("update XML_TEMPLT_NODE set CUSTOM_SQL_FIELD_INFO_ID=null");
    }

    changeSet(author: "ShubhamRx (generated)", id: "202210121130-2") {
        dropForeignKeyConstraint(baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_3y8dymnhcbv3rju8xpl73kvnq")
    }

    changeSet(author: "ShubhamRx (generated)", id: "202210121130-3") {
        dropForeignKeyConstraint(baseTableName: "XML_TEMPLT_CLL", constraintName: "FK_optmpal6wijw0la7fnpdctflp")
    }

    changeSet(author: "gologuzov (generated)", id: "202210121130-4") {
        addForeignKeyConstraint(baseColumnNames: "CLL_TEMPLT_ID", baseTableName: "XML_TEMPLT_CLL", constraintName: "FK_optmpal6wijw0la7fnpdctflp1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "gologuzov (generated)", id: "202210121130-5") {
        addForeignKeyConstraint(baseColumnNames: "CLL_TEMPLT_ID", baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_3y8dymnhcbv3rju8xpl73kvnq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "ShubhamRx (generated)", id: "202210280320-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_TEMPLT_NODE', columnName: 'CUSTOM_SQL_FILTER_FIELD_INFO')
            }
        }
        addColumn(tableName: "XML_TEMPLT_NODE") {
            column(name: "CUSTOM_SQL_FILTER_FIELD_INFO", type: "VARCHAR(256)")
        }
        sql("update XML_TEMPLT_NODE set CUSTOM_SQL_FILTER_FIELD_INFO=null");
    }
}
