import com.rxlogix.config.WorkflowState

databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1535796608599-1") {
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "REPORT_REQUEST", type: "number(19,0)")
        }
    }
    changeSet(author: "forxsv (generated)", id: "1535796608599-100") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "EX_REPORT", tableName: "WORKFLOW_JUSTIFICATION")
    }

    changeSet(author: "forxsv (generated)", id: "1535796608599-149") {
        addForeignKeyConstraint(baseColumnNames: "REPORT_REQUEST", baseTableName: "WORKFLOW_JUSTIFICATION", constraintName: "FK_cx9v4grm4vt7xqsjt0kjvo8v7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1535881335124-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'WORKFLOW_STATE_ID')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "WORKFLOW_STATE_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1535881335124-140") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'REPORT_REQUEST', foreignKeyName: 'FK_20v2rf1ngu1i4u8njc7ct8m3s')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_STATE_ID", baseTableName: "REPORT_REQUEST", constraintName: "FK_20v2rf1ngu1i4u8njc7ct8m3s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1535881335124-4") {
        preConditions(onFail: 'MARK_RAN') {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'status')
        }
        dropColumn(columnName: "status", tableName: "REPORT_REQUEST")
    }
}
