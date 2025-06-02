databaseChangeLog = {


    changeSet(author: "prashantsahi (generated)", id: "1500467221148-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DATE_RANGE_TYPE', columnName: 'SORT_ORDER')
            }
        }

        addColumn(tableName: "DATE_RANGE_TYPE") {
            column(name: "SORT_ORDER", type: "number(10,0)")
            constraints(nullable: "true")
        }
        sql("update DATE_RANGE_TYPE set SORT_ORDER = 0;")
    }

}
