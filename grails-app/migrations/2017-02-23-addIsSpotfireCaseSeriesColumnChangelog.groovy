databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1487936665556-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'IS_SPOTFIRE_CASE_SERIES')
            }
        }
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "IS_SPOTFIRE_CASE_SERIES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE EX_CASE_SERIES SET IS_SPOTFIRE_CASE_SERIES=1 WHERE (DATE_RANGE_TYPE is NULL AND EVALUATE_DATE_AS is NULL);")
        sql("UPDATE EX_CASE_SERIES SET IS_SPOTFIRE_CASE_SERIES=0 WHERE (DATE_RANGE_TYPE IS NOT NULL AND EVALUATE_DATE_AS IS NOT NULL);")
        addNotNullConstraint(tableName: "EX_CASE_SERIES", columnName: "IS_SPOTFIRE_CASE_SERIES")
    }

}
