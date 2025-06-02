databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "201909231570613062-1") {
        createTable(tableName: "ICSR_ORGANIZATION_TYPE"){
            column(name:"ID", type: "number(19,0)"){
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ICSR_ORGANIZATION_TYPE_PK")
            }

            column(name:"ORG_NAME_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name:"NAME", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

        }
    }

}