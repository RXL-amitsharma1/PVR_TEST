databaseChangeLog = {
    changeSet(author: "sachins", id: "25112020-1001-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'RESPONSIBLE_PARTY')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "RESPONSIBLE_PARTY", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachins", id: "25112020-1001-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'RESPONSIBLE_PARTY')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "RESPONSIBLE_PARTY", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachins", id: "25112020-1001-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'RESPONSIBLE_PARTY')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "RESPONSIBLE_PARTY", type: "varchar2(255 char)")
        }
    }
}