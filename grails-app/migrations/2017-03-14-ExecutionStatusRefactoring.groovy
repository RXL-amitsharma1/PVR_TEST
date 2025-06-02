databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1487936665569-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'ENTITY_ID')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "ENTITY_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665569-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'ENTITY_TYPE')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "ENTITY_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665569-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'EXECUTED_ENTITY_ID')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "EXECUTED_ENTITY_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665569-8") {
        sql("UPDATE EX_STATUS exStatus SET exStatus.ENTITY_ID = exStatus.CONFIG_ID, exStatus.EXECUTED_ENTITY_ID = exStatus.EXCONFIG_ID, exStatus.ENTITY_TYPE = 'CONFIGURATION'  where exStatus.CONFIG_ID is not null and exStatus.CONFIG_ID <> 0 and EXISTS (select confId.id from RCONFIG confId where exStatus.CONFIG_ID = confId.id and confId.class='com.rxlogix.config.Configuration');")
        sql("UPDATE EX_STATUS exStatus SET exStatus.ENTITY_ID = exStatus.CONFIG_ID, exStatus.EXECUTED_ENTITY_ID = exStatus.EXCONFIG_ID, exStatus.ENTITY_TYPE = 'PERIODIC_CONFIGURATION'  where exStatus.CONFIG_ID is not null and exStatus.CONFIG_ID <> 0 and EXISTS (select confId.id from RCONFIG confId where exStatus.CONFIG_ID = confId.id and confId.class='com.rxlogix.config.PeriodicReportConfiguration');")
        sql("UPDATE EX_STATUS exStatus SET exStatus.ENTITY_ID = exStatus.EXCONFIG_ID, exStatus.ENTITY_TYPE = 'EXECUTED_PERIODIC_CONFIGURATION' where exStatus.CONFIG_ID = 0 and EXISTS( select confId.id from EX_RCONFIG confId where exStatus.EXCONFIG_ID = confId.id and confId.class='com.rxlogix.config.ExecutedPeriodicReportConfiguration');")
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665569-5") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "CONFIG_ID", tableName: "EX_STATUS")
    }


    changeSet(author: "sachinverma (generated)", id: "1487936665569-9") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_SERIES', columnName: 'EXECUTING')
            }
        }
        addColumn(tableName: "CASE_SERIES") {
            column(name: "EXECUTING", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update CASE_SERIES set EXECUTING = 0;")
    }


    changeSet(author: "sachinverma (generated)", id: "1487936665569-10") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'EXECUTING')
            }
        }
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "EXECUTING", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_CASE_SERIES set EXECUTING = 0;")
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665569-12") {
        dropColumn(columnName: "EXECUTION_STATUS", tableName: "EX_CASE_SERIES")
        dropColumn(columnName: "MESSAGE", tableName: "EX_CASE_SERIES")
        dropColumn(columnName: "STACK_TRACE", tableName: "EX_CASE_SERIES")
        dropColumn(columnName: "ERROR_DATE", tableName: "EX_CASE_SERIES")
    }

}
