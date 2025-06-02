databaseChangeLog = {
    changeSet(author: "sachinverma (generated)", id: "1457250051526-6") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from user_tab_columns where table_name = 'RPT_SUBMISSION' and column_name = 'SUBMISSION_DATE' and data_type='TIMESTAMP(6)';")
        }
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "SUBMISSION_DATE", tableName: "RPT_SUBMISSION")
        renameColumn(tableName: "RPT_SUBMISSION", oldColumnName: "SUBMISSION_DATE", newColumnName: "SUBMISSION_DATE_BACK_UP", columnDataType: "varchar2(255 char)")
        addColumn(tableName: "RPT_SUBMISSION") {
            column(name: "SUBMISSION_DATE", type: "timestamp")
        }
//        Todo temporary fix (Need to check alternate solution for varchar column we can use like TO_DATE(SUBMISSION_DATE_BACK_UP,'') )16-MAR-16 12.00.00.000000000 AM
        sql("update RPT_SUBMISSION set SUBMISSION_DATE = SYSDATE");
    }

    changeSet(author: "sachinverma (generated)", id: "1457241970883-9") {
        modifyDataType(columnName: "COMMENT_DATA", newDataType: "VARCHAR(4000)", tableName: "RPT_SUBMISSION")
    }

}
