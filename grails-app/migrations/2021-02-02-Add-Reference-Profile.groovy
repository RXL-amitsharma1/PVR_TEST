databaseChangeLog = {
    changeSet(author: "sachinverma (generated)", id: "202102020001-1") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "REFERENCE_PROFILE_NAME", type: "varchar2(255 char)")
        }
    }
}