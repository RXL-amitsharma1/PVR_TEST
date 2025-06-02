databaseChangeLog = {
    changeSet(author: "sergey", id: "202407221643-1") {
        createTable(tableName: "DRILLDOWN_ACCESS_TRACKER") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DRILLDOWN_ACCESS_TRACKER_PK")
            }
            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "REPORT_ID", type: "varchar2(255 char)")
            column(name: "STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "LAST_ACCESS", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sergey", id: "202407221643-2") {
        createIndex(indexName: "DRDN_AC_TR_REPORT_ID_uniq", tableName: "DRILLDOWN_ACCESS_TRACKER", unique: "true") {
            column(name: "REPORT_ID")
        }
    }
}
