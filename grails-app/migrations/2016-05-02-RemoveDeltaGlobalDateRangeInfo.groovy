databaseChangeLog = {

    changeSet(author: "michaelmorett (generated)", id: "1462664215910-1") {
        dropColumn(columnName: "DATE_RNG_END_DELTA", tableName: "GLOBAL_DATE_RANGE_INFO")
    }

    changeSet(author: "michaelmorett (generated)", id: "1462664215910-2") {
        dropColumn(columnName: "DATE_RNG_START_DELTA", tableName: "GLOBAL_DATE_RANGE_INFO")
    }

}