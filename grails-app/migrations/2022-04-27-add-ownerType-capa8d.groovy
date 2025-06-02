databaseChangeLog = {

    changeSet(author: "himanshi", id: "270420221809-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CAPA_8D', columnName: 'OWNER_TYPE')
            }
        }
        addColumn(tableName: "CAPA_8D") {
            column(name: "OWNER_TYPE", type: "varchar(255)"){
                constraints(nullable: "true")
            }
        }
        sql("update capa_8d set owner_type='PVC' where id in (select issue_id from ddwn_mdata_issues);")
        sql("update capa_8d set owner_type='PVQ' where id not in (select issue_id from ddwn_mdata_issues);")
        addNotNullConstraint(tableName: "CAPA_8D", columnName: "OWNER_TYPE")

    }
}

