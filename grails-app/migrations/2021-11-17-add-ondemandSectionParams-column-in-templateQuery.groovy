databaseChangeLog = {

    changeSet(author: "anurag", id: "202111171256-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'ON_DEMAND_SECTION_PARAMS')
            }
        }
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "ON_DEMAND_SECTION_PARAMS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

}