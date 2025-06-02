databaseChangeLog = {

    changeSet(author: "ashishdhami", id: "120520231640") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'XML_TEMPLT_NODE', columnName: 'E2B_ELEMENT_NAME')
            }
        }
        addColumn(tableName: "XML_TEMPLT_NODE") {
            column(name: "E2B_ELEMENT_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
}