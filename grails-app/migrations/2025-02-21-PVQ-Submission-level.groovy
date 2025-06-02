databaseChangeLog = {
    changeSet(author: "sergey(generated)", id: "2024120281758-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'SUBMISSION_IDENTIFIER')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "SUBMISSION_IDENTIFIER", type: "varchar2(255 char)", defaultValue: "-")
            constraints(nullable: "false")
        }
    }

}
