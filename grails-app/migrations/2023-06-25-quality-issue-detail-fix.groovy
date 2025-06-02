databaseChangeLog = {

    changeSet(author: "sergey", id: "20241128184233-1") {
            preConditions(onFail: 'MARK_RAN') {
                sqlCheck(expectedResult: 'N', "SELECT Nullable FROM user_tab_columns WHERE table_name = 'QUALITY_ISSUE_DETAIL' AND column_name = 'ROOT_CAUSE_ID';")
                sqlCheck(expectedResult: 'N', "SELECT Nullable FROM user_tab_columns WHERE table_name = 'QUALITY_ISSUE_DETAIL' AND column_name = 'RESPONSIBLE_PARTY_ID';")
            }
            dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "ROOT_CAUSE_ID", tableName: "QUALITY_ISSUE_DETAIL")
            dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "RESPONSIBLE_PARTY_ID", tableName: "QUALITY_ISSUE_DETAIL")
    }

}
