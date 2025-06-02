import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "Shubham Sharma (generated)", id: "202005140342-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "EX_STATUS") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_SERIES', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "CASE_SERIES") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }

    }

    changeSet(author: "Shubham Sharma (generated)", id: "202005140342-3") {
        //        TO handle upgrades where default tenant id is different than 1
        grailsChange {
            change {
                Integer defaultTenantId = Holders.config.get('pvreports.multiTenancy.defaultTenant') as Integer
                if (defaultTenantId != 1) {
                    sql.execute("UPDATE RCONFIG SET TENANT_ID = ? where TENANT_ID= 1", [defaultTenantId])
                    sql.execute("UPDATE EX_RCONFIG SET TENANT_ID = ? where TENANT_ID= 1", [defaultTenantId])
                    sql.execute("UPDATE EX_STATUS SET TENANT_ID = ? where TENANT_ID= 1", [defaultTenantId])
                    sql.execute("UPDATE CASE_SERIES SET TENANT_ID = ? where TENANT_ID= 1", [defaultTenantId])
                    sql.execute("UPDATE EX_CASE_SERIES SET TENANT_ID = ? where TENANT_ID= 1", [defaultTenantId])
                }
            }
        }
    }

}