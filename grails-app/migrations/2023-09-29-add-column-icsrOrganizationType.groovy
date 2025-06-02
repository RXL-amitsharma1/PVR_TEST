databaseChangeLog = {

    changeSet(author: "meenal(generated)", id: "202309291500-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_ORGANIZATION_TYPE', columnName: 'E2B_R2')
            }
        }
        addColumn(tableName: "ICSR_ORGANIZATION_TYPE") {
            column(name: "E2B_R2", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal(generated)", id: "202309291500-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_ORGANIZATION_TYPE', columnName: 'E2B_R3')
            }
        }
        addColumn(tableName: "ICSR_ORGANIZATION_TYPE") {
            column(name: "E2B_R3", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal(generated)", id: "202309291500-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_ORGANIZATION_TYPE', columnName: 'DISPLAY')
            }
        }
        addColumn(tableName: "ICSR_ORGANIZATION_TYPE") {
            column(name: "DISPLAY", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "meenal(generated)", id: "202309291500-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_ORGANIZATION_TYPE', columnName: 'IS_ACTIVE')
            }
        }
        addColumn(tableName: "ICSR_ORGANIZATION_TYPE") {
            column(name: "IS_ACTIVE", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "meenal(generated)", id: "202309291500-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_ORGANIZATION_TYPE', columnName: 'TENANT_ID')
            }
        }
        addColumn(tableName: "ICSR_ORGANIZATION_TYPE") {
            column(name: "TENANT_ID", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

}
