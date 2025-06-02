databaseChangeLog = {
    changeSet(author: 'rxl-shivamg1', id: '202501071211-1') {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "TEMPLATE_REMEDIATION")
            }
        }
        createTable(tableName: 'TEMPLATE_REMEDIATION') {
            column(name: 'IS_REMEDIATION_REQUIRED', type: 'number(1,0)') {
                constraints(nullable: 'false')
            }
            column(name: 'LAST_REMEDIATION_DATE', type: 'timestamp') {
                constraints(nullable: 'false')
            }
        }
        sql("INSERT INTO TEMPLATE_REMEDIATION (IS_REMEDIATION_REQUIRED,LAST_REMEDIATION_DATE) VALUES (1, CURRENT_TIMESTAMP)")
    }
}
