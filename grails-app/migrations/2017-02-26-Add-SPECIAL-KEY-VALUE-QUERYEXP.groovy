databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1486653279245-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY_EXP_VALUE', columnName: 'SPL_VAL_KEY')
            }
        }
        addColumn(tableName: "QUERY_EXP_VALUE") {
            column(name: "SPL_VAL_KEY", type: "varchar2(255 char)")
        }
    }

}
