databaseChangeLog = {
    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-1") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'CASE_SERIES', columnName: 'SCHEDULE_DATE')
            }
        }

        addColumn(tableName: "CASE_SERIES") {
            column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-2") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'CASE_SERIES', columnName: 'NEXT_RUN_DATE')
            }
        }

        addColumn(tableName: "CASE_SERIES") {
            column(name: "NEXT_RUN_DATE", type: "timestamp")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-3") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'CASE_SERIES', columnName: 'IS_ENABLED')
            }
        }

        addColumn(tableName: "CASE_SERIES") {
            column(name: "IS_ENABLED", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-4") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'CASE_SERIES', columnName: 'SELECTED_TIME_ZONE')
            }
        }

        addColumn(tableName: "CASE_SERIES") {
            column(name: "SELECTED_TIME_ZONE", type: "varchar2(255)", defaultValue: "UTC")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-5") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'SELECTED_TIME_ZONE')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "SELECTED_TIME_ZONE", type: "varchar2(255)", defaultValue: "UTC")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-6") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'IS_ENABLED')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "IS_ENABLED", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-7") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'NEXT_RUN_DATE')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "NEXT_RUN_DATE", type: "timestamp")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "20210215033033-8") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'SCHEDULE_DATE')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")
        }
    }
}