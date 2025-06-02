import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "Sachin Verma", id: "2020062315221-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "TENANT")
            }
        }
        createTable(tableName: "TENANT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TENANTPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ACTIVE", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

        }
        //Default tenant insert
        sql("insert into TENANT (ID,VERSION,NAME,IS_ACTIVE,DATE_CREATED,LAST_UPDATED) values(1,0,'DEFAULT',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)")
    }

    changeSet(author: "Sachin Verma", id: "2020062315221-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "PVUSER_TENANTS")
            }
        }
        createTable(tableName: "PVUSER_TENANTS") {
            column(name: "PVUSER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TENANT_ID", type: "number(19,0)")
        }
        //Default tenant insert
        sql("insert into PVUSER_TENANTS (PVUSER_ID,TENANT_ID) select ID,1 from PVUSER")
    }

    changeSet(author: "Sachin Verma", id: "2020062315221-3") {
//        TO handle upgrades where default tenant id is different than 1
        grailsChange {
            change {
                Integer defaultTenantId = Holders.config.get('pvreports.multiTenancy.defaultTenant') as Integer
                if (defaultTenantId != 1) {
                    sql.execute("UPDATE TENANT SET ID = ? where ID =1", [defaultTenantId])
                    sql.execute("UPDATE PVUSER_TENANTS SET TENANT_ID = ? where TENANT_ID =1", [defaultTenantId])
                }
            }
        }
    }

}