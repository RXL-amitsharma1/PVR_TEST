databaseChangeLog = {
    changeSet(author: "meenal (generated)", id: "20230120043401-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CAPA_8D', columnName: 'ATTACHMENT_CHECKED')
            }
        }

        addColumn(tableName: "CAPA_8D") {
            column(name: "ATTACHMENT_CHECKED", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }

        sql("update CAPA_8D set ATTACHMENT_CHECKED = 1 where id in (select distinct(ISSUE_ID) from CAPA_8D_ATTACHMENT)")
        addNotNullConstraint(tableName: "CAPA_8D", columnName: "ATTACHMENT_CHECKED")
    }
}
