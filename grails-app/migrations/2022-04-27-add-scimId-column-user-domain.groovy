databaseChangeLog = {
    changeSet(author: "Pranjal (generated)", id: "24052022170400-1") {
        addColumn(tableName: "PVUSER") {
            column(name: "SCIM_ID", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachin verma", id: "19102022143800-1") {
        addColumn(tableName: "USER_GROUP") {
            column(name: "SCIM_ID", type: "varchar2(255 char)")
        }
    }
}
