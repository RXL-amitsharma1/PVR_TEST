import com.rxlogix.config.EtlSchedule
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "202003191585-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "ETL_SCHEDULE", columnName: "EMAIL_CONFIGURATION_ID")
            }
        }
        addColumn(tableName: "ETL_SCHEDULE") {
            column(name: "EMAIL_CONFIGURATION_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "SEND_SUCCESS_EMAIL", type: "NUMBER(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }
            column(name: "PAUSE_LONG_RUNNING_ETL", type: "NUMBER(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }
            column(name: "SEND_EMAIL_ETL_INTERVAL", type: "NUMBER(3)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "202003191585-4") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "ETL_SCHEDULE", columnName: "EMAIL_TO_USERS")
            }
        }
        addColumn(tableName: "ETL_SCHEDULE") {
            column(name: "EMAIL_TO_USERS", type: "varchar2(4000 char)")
        }
        grailsChange {
            change {
                try {
                    EtlSchedule etlSchedule = ctx.getBean('etlJobService').getSchedule()
                    if(etlSchedule){
                        String emailList = Holders.config.get('etl.schedule.admin.emails')
                        if (emailList) {
                            sql.executeUpdate("update ETL_SCHEDULE set EMAIL_TO_USERS = ?", [emailList])
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating etl schedule, liquibase change-set 1535881335123-3 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
        addNotNullConstraint(tableName: "ETL_SCHEDULE", columnName: "EMAIL_TO_USERS", columnDataType: "varchar2(4000 char)")
    }

}