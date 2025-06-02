databaseChangeLog = {



    changeSet(author: "sergey", id: "202403151705-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'NONCASE_SQL_TEMPLT', columnName: 'COL_NAME_LIST_CLOB')
        }

        dropColumn(tableName: "NONCASE_SQL_TEMPLT", columnName: "COL_NAME_LIST_CLOB")
    }

    changeSet(author: "sergey", id: "202403151705-3") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'NONCASE_SQL_TEMPLT', columnName: 'COL_NAME_LIST')
            not {
                columnExists(tableName: 'NONCASE_SQL_TEMPLT', columnName: 'COL_NAME_LIST_CLOB')
            }
        }

        addColumn(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "COL_NAME_LIST_CLOB", type: "clob")
        }

        sql('UPDATE NONCASE_SQL_TEMPLT SET COL_NAME_LIST_CLOB=COL_NAME_LIST')

        dropColumn(tableName: "NONCASE_SQL_TEMPLT", columnName: "COL_NAME_LIST")

        renameColumn(tableName: "NONCASE_SQL_TEMPLT", oldColumnName: "COL_NAME_LIST_CLOB", newColumnName: "COL_NAME_LIST")

    }

}