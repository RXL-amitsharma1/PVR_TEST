databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202410221147-1") {

        preConditions(onFail: 'MARK_RAN') {

            not {

                columnExists(tableName: 'RCONFIG', columnName: 'MULTI_REPORT')

            }

        }

        addColumn(tableName: "RCONFIG") {

            column(name: "MULTI_REPORT", type: "NUMBER(1,0)", defaultValue: 0) {

                constraints(nullable: "true")

            }

        }

    }

    changeSet(author: "meenal (generated)", id: "202410221147-2") {

        preConditions(onFail: 'MARK_RAN') {

            not {

                columnExists(tableName: 'EX_RCONFIG', columnName: 'MULTI_REPORT')

            }

        }

        addColumn(tableName: "EX_RCONFIG") {

            column(name: "MULTI_REPORT", type: "NUMBER(1,0)", defaultValue: 0) {

                constraints(nullable: "true")

            }

        }

    }

}
