databaseChangeLog = {

    changeSet(author: "sachins", id: "2021081710009-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'VERSION_NUM')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "VERSION_NUM", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update QUALITY_CASE_DATA A set A.VERSION_NUM = A.METADATA.masterVersionNum where A.ISDELETED = 0 and A.METADATA is not null")
    }

    changeSet(author: "sachins", id: "2021081710009-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'VERSION_NUM')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "VERSION_NUM", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update QUALITY_SUBMISSION A set A.VERSION_NUM = A.METADATA.masterVersionNum where A.ISDELETED = 0 and A.METADATA is not null")
    }

    changeSet(author: "sachins", id: "2021081710009-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'VERSION_NUM')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "VERSION_NUM", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update QUALITY_SAMPLING A set A.VERSION_NUM = A.METADATA.masterVersionNum where A.ISDELETED = 0 and A.METADATA is not null")
    }
}

