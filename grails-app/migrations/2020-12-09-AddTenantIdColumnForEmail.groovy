    import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "ShubhamRx(generated)", id: "20201209045834-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EMAIL', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "EMAIL") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }

        grailsChange {
            change {
                Integer defaultTenantId = Holders.config.get('pvreports.multiTenancy.defaultTenant') as Integer
                if (defaultTenantId != 1) {
                    sql.execute("UPDATE EMAIL SET TENANT_ID = ? where TENANT_ID= 1", [defaultTenantId])
                }
            }
        }
    }
}