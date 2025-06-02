databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202411251148-01") {

        preConditions(onFail: 'MARK_RAN') {

            not {

                columnExists(tableName: 'RCONFIG', columnName: 'JAPAN_PROFILE')

            }

        }

        addColumn(tableName: "RCONFIG") {

            column(name: "JAPAN_PROFILE", type: "NUMBER(1,0)", defaultValue: 0) {

                constraints(nullable: "true")

            }

        }

    }

    changeSet(author: "meenal (generated)", id: "202411251148-02") {

        preConditions(onFail: 'MARK_RAN') {

            not {

                columnExists(tableName: 'EX_RCONFIG', columnName: 'JAPAN_PROFILE')

            }

        }

        addColumn(tableName: "EX_RCONFIG") {

            column(name: "JAPAN_PROFILE", type: "NUMBER(1,0)", defaultValue: 0) {

                constraints(nullable: "true")

            }

        }

    }

}

