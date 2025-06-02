databaseChangeLog = {

    changeSet(author: "Sergey Gologuzov", id: "1460928349662-1") {
        addColumn(tableName: "argus_column_master") {
            column(name: "min_columns", type: "number")
        }
        sql("update argus_column_master set min_columns = 9 where report_item = 'CM_CASE_NUM';")
        sql("update argus_column_master set min_columns = 9 where report_item = 'CM_RPT_TYPE_ID';")
    }
}

