databaseChangeLog = {

    changeSet(author: "ankita (generated)", id: "1500558380864-1") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "DISPLAY_MEDDRA", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update TEMPLT_QUERY set DISPLAY_MEDDRA = 0;")


    }
    changeSet(author: "ankita (generated)", id: "1500558380864-2") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "DISPLAY_MEDDRA", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_TEMPLT_QUERY set DISPLAY_MEDDRA = 0;")

    }
    changeSet(author: "ankita (generated)", id: "1500558380864-3") {
        addColumn(tableName: "RPT_RESULT") {
            column(name: "MEDDRA_VERSION", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update RPT_RESULT set MEDDRA_VERSION = null;")

    }

}

