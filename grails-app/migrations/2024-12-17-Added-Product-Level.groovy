databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202412171951-01") {

        preConditions(onFail: 'MARK_RAN') {

            not {

                columnExists(tableName: 'RCONFIG', columnName: 'PRODUCT_LEVEL')

            }

        }

        addColumn(tableName: "RCONFIG") {

            column(name: "PRODUCT_LEVEL", type: "NUMBER(1,0)", defaultValue: 0) {

                constraints(nullable: "true")

            }

        }

    }

    changeSet(author: "ShubhamRx (generated)", id: "202412171951-02") {

        preConditions(onFail: 'MARK_RAN') {

            not {

                columnExists(tableName: 'EX_RCONFIG', columnName: 'PRODUCT_LEVEL')

            }

        }

        addColumn(tableName: "EX_RCONFIG") {

            column(name: "PRODUCT_LEVEL", type: "NUMBER(1,0)", defaultValue: 0) {

                constraints(nullable: "true")

            }

        }

    }

}

