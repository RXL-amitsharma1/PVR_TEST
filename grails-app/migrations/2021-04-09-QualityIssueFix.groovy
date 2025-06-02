databaseChangeLog = {

    changeSet(author: "sergey", id: "20210409072800-1") {
            dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "CORRECTIVE_ACTION_ID", tableName: "QUALITY_ISSUE_DETAIL")
            dropNotNullConstraint(columnDataType: "TIMESTAMP(6)", columnName: "CORRECTIVE_DATE", tableName: "QUALITY_ISSUE_DETAIL")
            dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "PREVENTATIVE_ACTION_ID", tableName: "QUALITY_ISSUE_DETAIL")
            dropNotNullConstraint(columnDataType: "TIMESTAMP(6)", columnName: "PREVENTATIVE_DATE", tableName: "QUALITY_ISSUE_DETAIL")
    }

}
