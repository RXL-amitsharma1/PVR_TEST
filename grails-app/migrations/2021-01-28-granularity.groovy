databaseChangeLog = {

    changeSet(author: "sergey", id: "202101261721-1") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "GRANULARITY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey", id: "202101261721-2") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "GRANULARITY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey", id: "202101261721-3") {
        addColumn(tableName: "ICSR_TEMPLT_QUERY") {
            column(name: "GRANULARITY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

}