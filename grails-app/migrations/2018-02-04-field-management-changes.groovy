databaseChangeLog = {

    changeSet(author: "sargam ", id: "1510826186963-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD', columnName: 'OVERRIDE')
            }
        }
        addColumn(tableName: "RPT_FIELD") {
            column(name: "OVERRIDE", type: "number(1,0)", defaultValue:1)
        }
    }

    changeSet(author: "sargam ", id: "1510826186963-2") {
            dropNotNullConstraint(columnDataType: "varchar2(80 char)", columnName: "SOURCE_COLUMN_MASTER_ID", tableName: "RPT_FIELD")
        }
    changeSet(author: "sargam ", id: "1510826186963-3") {
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DATA_TYPE", tableName: "RPT_FIELD")
    }
    changeSet(author: "sargam ", id: "1510826186963-4") {
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DATE_FORMAT", tableName: "RPT_FIELD")
    }
    }
