databaseChangeLog = {

    changeSet(author: "pragyaTiwari", id: '202404051107-1'){
        createTable(tableName: "BULK_DOWNLOAD_ICSR_REPORTS"){
            column(name: "ID", type: "NUMBER(19,0)"){
                constraints(primaryKey: "true", nullable: "false")
            }
            column(name: "DOWNLOAD_DATA", type: "CLOB"){
                constraints(nullable: "false")
            }
            column(name: "DOWNLOAD_PVUSER_ID", type: "NUMBER(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "DATE_CREATED", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }
            column(name: "LAST_UPDATED", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }
            column(name: "CREATED_BY", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "false")
            }
            column(name: "MODIFIED_BY", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "false")
            }
        }
    }

}