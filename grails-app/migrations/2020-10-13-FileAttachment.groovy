databaseChangeLog = {

    changeSet(author: "sargam (generated)", id: "202010130258-1") {
        createTable(tableName: "FILE_ATTACHMENT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "FILE_ATTACH_ATPK")
            }
            column(name: "DATA", type: "blob") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "202010130258-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST_ATTACH', columnName: 'FILE_ATTACHMENT_ID')
            }
        }
        addColumn(tableName: "REPORT_REQUEST_ATTACH") {
            column(name: "FILE_ATTACHMENT_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sargam (generated)", id: "202010130258-3") {
        addForeignKeyConstraint(baseColumnNames: "FILE_ATTACHMENT_ID", baseTableName: "REPORT_REQUEST_ATTACH", constraintName: "FK_jy6noas2jdkktee1dwle2hrbf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FILE_ATTACHMENT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sargam (generated)", id: "202010130258-4") {
        sql("insert  into FILE_ATTACHMENT (id,data) select id, to_lob(data) from REPORT_REQUEST_ATTACH")
        sql("UPDATE REPORT_REQUEST_ATTACH SET FILE_ATTACHMENT_ID=ID")
    }

    changeSet(author: "sargam (generated)", id: "202010150658-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'REPORT_REQUEST_ID_ATTFK')
            }
        }
        createIndex(indexName: "REPORT_REQUEST_ID_ATTFK", tableName: "REPORT_REQUEST_ATTACH", unique: "false") {
            column(name: "REPORT_REQUEST_ID")
        }
    }

    changeSet(author: "sargam (generated)", id: "202012180159-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'FILE_ATTACHMENT_ID_ATTFK')
            }
        }
        createIndex(indexName: "FILE_ATTACHMENT_ID_ATTFK", tableName: "REPORT_REQUEST_ATTACH", unique: "false") {
            column(name: "FILE_ATTACHMENT_ID")
        }
    }

}
