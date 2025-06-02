databaseChangeLog = {
    changeSet(author: "shikhars", id: "111220201535") {
        createTable(tableName: "ADVANCED_ASSIGNMENT") {

            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "CATEGORY", type: "varchar2(255 char)")

            column(name: "ASSIGNED_USER", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(4000 char)")

            column(name: "QUALITY_CHECKED", type: "number(1,0)")

            column(name: "ASSIGNMENT_QUERY", type: "clob")

            column(name: "ISDELETED", type: "number(1,0)")

            column(name: "TENANT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }
        }
        addPrimaryKey(columnNames: "ID", constraintName: "ADVANCED_ASSIGNMENT_PK", tableName: "ADVANCED_ASSIGNMENT")
        sql("CREATE SEQUENCE ADVANCED_ASSIGNMENT_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }
}