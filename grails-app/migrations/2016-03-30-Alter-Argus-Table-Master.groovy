databaseChangeLog = {

    changeSet(author: "Amrit Kaur", id: "Add HAS_ENTERPRISE_ID col to ARGUS_TABLE_MASTER") {
        addColumn(tableName: "ARGUS_TABLE_MASTER") {
            column(name: "HAS_ENTERPRISE_ID", type: "number")
        }
    }

}