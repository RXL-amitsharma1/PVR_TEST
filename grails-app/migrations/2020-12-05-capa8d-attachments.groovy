databaseChangeLog = {
    changeSet(author: "meenal (generated)", id: "202010111826-3") {
        createTable(tableName: "CAPA_8D_ATTACHMENT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CAPA_8D_ATTACHMENTPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "FILENAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "anurag", id: "202210241704-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CAPA_8D_ATTACHMENT', columnName: 'OWNER_TYPE')
            }
        }
        addColumn(tableName: "CAPA_8D_ATTACHMENT") {
            column(name: "OWNER_TYPE", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "meenalr", id: "202211101848-1") {
        addColumn(tableName: "CAPA_8D_ATTACHMENT") {
            column(name: "ISDELETED", type: "number(1,0)")
        }
    }
}