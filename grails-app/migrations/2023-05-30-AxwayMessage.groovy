databaseChangeLog = {
    changeSet(author: "meenal (generated)", id: "202205301140-1") {
        createTable(tableName: "AXWAY_MESSAGES") {

            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CURRENTSTATETYPE", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "CONSUMPTIONFILENAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "DELIVEREDTIME", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "DOCUMENTCLASS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "REFTOOID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "DIRECTION", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
        addPrimaryKey(columnNames: "ID", constraintName: "AXWAY_MESSAGE_PK", tableName: "AXWAY_MESSAGES")
        sql("CREATE SEQUENCE AXWAY_MESSAGE_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }

}
