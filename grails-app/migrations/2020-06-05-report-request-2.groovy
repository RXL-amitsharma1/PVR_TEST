databaseChangeLog = {

    changeSet(author: "sergey khovrachev  (generated)", id: "202006051047-1") {
        createTable(tableName: "REPORT_REQUEST_FIELD") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_REQUEST_FIELD_PK")
            }

            column(name: "VERSION", type: "number(19,0)")
            column(name: "NAME", type: "varchar2(255 char)")
            column(name: "LABEL", type: "varchar2(255 char)")
            column(name: "TYPE", type: "varchar2(255 char)")
            column(name: "ALLOWED_VALUES", type: "varchar2(4000 char)")
            column(name: "INDX", type: "number(10,0)")
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "MASTER_PLANNING", type: "number(1,0)") {
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

    changeSet(author: "sergey khovrachev (generated)", id: "202006051047-6") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "custom_values", type: "clob")
        }
    }
}







