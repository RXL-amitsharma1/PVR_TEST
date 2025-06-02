databaseChangeLog = {
    changeSet(author: "shikhars", id: "210820201426") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "QUALITY_CASE_META_DATA")
        }
        sql("drop table QUALITY_CASE_META_DATA cascade constraints")
    }

    changeSet(author: "shikhars", id: "210820201426-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "CASE_DATA_QUALITY")
        }
        sql("drop table CASE_DATA_QUALITY cascade constraints")
    }
}