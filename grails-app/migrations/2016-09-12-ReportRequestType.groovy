import com.rxlogix.config.ReportRequestType
import com.rxlogix.enums.ReportRequestTypeEnum

databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1473669034141-1") {
        createTable(tableName: "REPORT_REQUEST_TYPE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_REQ_TYP_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "clob")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1555662378671-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST_TYPE', columnName: 'aggregate')
            }
        }
        addColumn(tableName: "REPORT_REQUEST_TYPE") {
            column(name: "aggregate", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

   changeSet(author: "sachinverma (generated)", id: "1473684391568-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'REPORT_REQUEST_TYPE_ID')
                changeSetExecuted(id: '1473684391568-1')
            }
        }

        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "REPORT_REQUEST_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1473684391568-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_REQUEST', columnName: 'REPORT_TYPE')
        }
        dropColumn(tableName: "REPORT_REQUEST", columnName: "REPORT_TYPE")
    }

    changeSet(author: "sachinverma (generated)", id: "1473684391568-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'REPORT_REQUEST', foreignKeyName: 'FKDB16FA64ED1D9EFC')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "report_request_type_id", baseTableName: "REPORT_REQUEST", constraintName: "FKDB16FA64ED1D9EFC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST_TYPE", referencesUniqueColumn: "false")
    }

}
