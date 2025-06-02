import com.rxlogix.config.EtlSchedule
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "gautam (generated)", id: "2022101012442-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "ETL_SCHEDULE", columnName: "EMAIL_TRIGGER")
            }
        }
        addColumn(tableName: "ETL_SCHEDULE") {
            column(name: "EMAIL_TRIGGER", type: "NUMBER(1)", defaultValue: 1) {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202305311228-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "ETL_SCHEDULE", columnName: "EMAIL_FOR_LONG_RUNNING")
            }
        }
        addColumn(tableName: "ETL_SCHEDULE") {
            column(name: "EMAIL_FOR_LONG_RUNNING", type: "NUMBER(1)", defaultValue: 1) {
                constraints(nullable: "false")
            }
        }
    }
}
