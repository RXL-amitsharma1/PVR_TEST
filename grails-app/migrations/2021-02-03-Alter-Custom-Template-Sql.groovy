databaseChangeLog = {

    changeSet(author: "Sachin Verma", id: "202102030210-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SQL_TEMPLT', columnName: 'COLUMN_NAMES_CLOB')
        }

        dropColumn(tableName: "SQL_TEMPLT", columnName: "COLUMN_NAMES_CLOB")
    }

    changeSet(author: "Sachin Verma", id: "202102030210-3") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SQL_TEMPLT', columnName: 'COLUMN_NAMES')
            not {
                columnExists(tableName: 'SQL_TEMPLT', columnName: 'COLUMN_NAMES_CLOB')
            }
        }

        addColumn(tableName: "SQL_TEMPLT") {
            column(name: "COLUMN_NAMES_CLOB", type: "clob")
        }

        sql('UPDATE SQL_TEMPLT SET COLUMN_NAMES_CLOB=COLUMN_NAMES')

        dropColumn(tableName: "SQL_TEMPLT", columnName: "COLUMN_NAMES")

        renameColumn(tableName: "SQL_TEMPLT", oldColumnName: "COLUMN_NAMES_CLOB", newColumnName: "COLUMN_NAMES")

    }

}