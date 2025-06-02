databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "20240604093100-1") {
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "WORLD_MAP", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sergey (generated)", id: "20240604093100-2") {
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "WORLD_MAP_CONFIG", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
}