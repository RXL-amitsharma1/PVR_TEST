databaseChangeLog = {
    changeSet(author: "shikhars", id: "210820202020") {
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "TENANT_ID", type: "NUMBER(19,0)")
        }

        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "TENANT_ID", type: "NUMBER(19,0)")
        }

        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "TENANT_ID", type: "NUMBER(19,0)")
        }

        sql("UPDATE QUALITY_CASE_DATA SET TENANT_ID = 1")
        sql("UPDATE QUALITY_SUBMISSION SET TENANT_ID = 1")
        sql("UPDATE QUALITY_SAMPLING SET TENANT_ID = 1")
    }
}