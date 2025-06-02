databaseChangeLog = {
    changeSet(author: "Nitin Nepalia (generated)", id: "211220211130-1") {
        createTable(tableName: "ALLOWED_ATTACHMENTS") {
            column(name: "UNITI_CONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            
            column(name: "ATTACHMENT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }
}
