databaseChangeLog = {

    changeSet(author: "shikhars", id: "090220212053") {
        createTable(tableName: "CORRECTIVE_ACTION") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CORRECTIVE_ACTION_ID")
            }

            column(name: "TEXT_DESC", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }

            column(name: "OWNER_APP", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }

        sql("CREATE SEQUENCE CORRECTIVE_ACTION_SEQ INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")

        createTable(tableName: "PREVENTATIVE_ACTION") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PREVENTATIVE_ACTION_ID")
            }

            column(name: "TEXT_DESC", type: "varchar2(24000 char)") {
                constraints(nullable: "true")
            }

            column(name: "OWNER_APP", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }

        sql("CREATE SEQUENCE PREVENTATIVE_ACTION_SEQ INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }
}

