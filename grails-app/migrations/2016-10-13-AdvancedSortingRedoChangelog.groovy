databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1476355628448-29") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'CLL_TEMPLT', columnName: 'ADVANCED_SORTING')
        }

        dropColumn(columnName: "ADVANCED_SORTING", tableName: "CLL_TEMPLT")
    }

}
