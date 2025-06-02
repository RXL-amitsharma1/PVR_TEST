databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "202210131022-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_TEMPLT_NODE', columnName: 'E2B_ELEMENT')
            }
        }
        addColumn(tableName: "XML_TEMPLT_NODE") {
            column(name: "E2B_ELEMENT", type: "varchar2(255)")
            column(name: "SOURCE_FIELD_LABEL", type: "varchar2(4000)")
        }

    }
    changeSet(author: "forxsv (generated)", id: "202210131022-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_TEMPLT_NODE', columnName: 'SOURCE_FIELD_LABEL_VAL')
            }
        }
        addColumn(tableName: "XML_TEMPLT_NODE") {
            column(name: "SOURCE_FIELD_LABEL_VAL", type: "varchar2(4000)")
        }

    }
}
