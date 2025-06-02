databaseChangeLog = {
    changeSet(author: "Meenal (generated)", id: "202205171740-22") {
        modifyDataType(columnName: "REPORT_NAME", newDataType: "varchar2(500 char)", tableName: "RCONFIG")
        modifyDataType(columnName: "REPORT_NAME", newDataType: "varchar2(500 char)", tableName: "EX_RCONFIG")
        modifyDataType(columnName: "REPORT_NAME", newDataType: "varchar2(555 char)", tableName: "EX_STATUS")
        modifyDataType(columnName: "REPORT_NAME", newDataType: "varchar2(555 char)", tableName: "REPORT_REQUEST")
        modifyDataType(columnName: "REPORT_NAME", newDataType: "varchar2(555 char)", tableName: "SIGNAL_REPORT")
        modifyDataType(columnName: "TITLE", newDataType: "varchar2(555 char)", tableName: "EX_TEMPLT_QUERY")
        modifyDataType(columnName: "TITLE", newDataType: "varchar2(555 char)", tableName: "TEMPLT_QUERY")
        modifyDataType(columnName: "series_name", newDataType: "varchar2(555 char)", tableName: "case_series")
        modifyDataType(columnName: "SERIES_NAME", newDataType: "varchar2(555 char)", tableName: "EX_CASE_SERIES")
        modifyDataType(columnName: "SECTION_NAME", newDataType: "varchar2(555 char)", tableName: "EX_STATUS")
    }
}