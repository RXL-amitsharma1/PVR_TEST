databaseChangeLog = {
    changeSet(author: "Nitin Nepalia (generated)", id: "241120211719-3") {
        createTable(tableName: "CONFIG_CALENDAR") {
          
            
            column(name: "RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            
            column(name: "CALENDAR_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }
    
    changeSet(author: "Nitin Nepalia (generated)", id: "241120211719-4") {
        createTable(tableName: "EX_CONFIG_CALENDAR") {
        
            
            column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            
            column(name: "CALENDAR_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }
}
