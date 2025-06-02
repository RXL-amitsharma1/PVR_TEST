databaseChangeLog = {
    changeSet(author: "anurag", id: "200920202020-1-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'WORKFLOW_STATE_ID')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "WORKFLOW_STATE_ID", type: "NUMBER(19,0)")
        }

        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "WORKFLOW_STATE_ID", type: "NUMBER(19,0)")
        }

        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "WORKFLOW_STATE_ID", type: "NUMBER(19,0)")
        }

        sql("UPDATE QUALITY_CASE_DATA SET WORKFLOW_STATE_ID = 14")
        sql("UPDATE QUALITY_SUBMISSION SET WORKFLOW_STATE_ID = 14")
        sql("UPDATE QUALITY_SAMPLING SET WORKFLOW_STATE_ID = 14")
    }

    changeSet(author: "anurag", id: "200920202020-2-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_JUSTIFICATION', columnName: 'QUALITY_CASE_DATA')
            }
        }
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "QUALITY_CASE_DATA", type: "NUMBER(19,0)")
            column(name: "QUALITY_SUBMISSION", type: "NUMBER(19,0)")
            column(name: "QUALITY_SAMPLING", type: "NUMBER(19,0)")
        }
    }

    changeSet(author: "anurag", id: "200920202020-3-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'QUALITY_ISSUE_TYPE')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "QUALITY_ISSUE_TYPE", type: "varchar2(255 char)")
            column(name: "ROOT_CAUSE", type: "varchar2(255 char)")
        }

        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "QUALITY_ISSUE_TYPE", type: "varchar2(255 char)")
            column(name: "ROOT_CAUSE", type: "varchar2(255 char)")
        }

        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "QUALITY_ISSUE_TYPE", type: "varchar2(255 char)")
            column(name: "ROOT_CAUSE", type: "varchar2(255 char)")
        }

    }

    changeSet(author: "anurag", id: "141220202020") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_JUSTIFICATION', columnName: 'DRILLDOWN_METADATA')
            }
        }
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "DRILLDOWN_METADATA", type: "NUMBER(19,0)")
        }
    }
}