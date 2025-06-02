databaseChangeLog = {
    
    changeSet(author: "Sergey", id: "202203091200-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'DUE_DATE')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "ASSIGNED_TO_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }


}
