databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202410041551-1") {
        createTable(tableName: "E2B_LOCALE_NAME") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "E2B_LOCALE_NAME_PK")
            }

            column(name: "LOCALE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "LOCALE_ELEMENT_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }

        addColumn(tableName: "XML_TEMPLT_NODE") {
            column(name: "E2B_ELEMENT_NAME_LOCALE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}
