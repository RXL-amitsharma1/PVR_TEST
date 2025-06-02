databaseChangeLog = {
    changeSet(author: "sachinverma (generated)", id: "1483874440935-1") {
        addColumn(tableName: "RCONFIG") {
            column(name: "PRIMARY_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1483874440935-2") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PRIMARY_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1483874440935-3") {
        addColumn(tableName: "RPT_SUBMISSION") {
            column(name: "PRIMARY_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1483874440935-4") {
        sql("delete from localization");
    }

}
