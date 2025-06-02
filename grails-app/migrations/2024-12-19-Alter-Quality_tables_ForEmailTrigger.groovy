databaseChangeLog = {
    changeSet(author: "gunjan", id: "202412191121501-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'ASSIGNER')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "ASSIGNER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412191121501-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'ASSIGNEE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "ASSIGNEE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412191121501-3") {
        sql("UPDATE QUALITY_CASE_DATA set ASSIGNEE_UPDATED_DATE = SYSDATE WHERE ASSIGNEE_UPDATED_DATE IS NULL;")
    }

    changeSet(author: "gunjan", id: "202412191121501-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'ASSIGNER')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "ASSIGNER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412191121501-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'ASSIGNEE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "ASSIGNEE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412191121501-6") {
        sql("UPDATE QUALITY_SUBMISSION set ASSIGNEE_UPDATED_DATE = SYSDATE WHERE ASSIGNEE_UPDATED_DATE IS NULL;")
    }

    changeSet(author: "gunjan", id: "202412191121501-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'ASSIGNER')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "ASSIGNER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412191121501-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'ASSIGNEE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "ASSIGNEE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412191121501-9") {
        sql("UPDATE QUALITY_SAMPLING set ASSIGNEE_UPDATED_DATE = SYSDATE WHERE ASSIGNEE_UPDATED_DATE IS NULL;")
    }
}