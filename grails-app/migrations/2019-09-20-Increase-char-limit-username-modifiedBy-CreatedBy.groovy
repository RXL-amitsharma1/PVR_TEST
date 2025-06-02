databaseChangeLog = {

    changeSet(author: "ankita", id: "201909201848-1") {

        modifyDataType(columnName:"USERNAME", tableName:"PVUSER", newDataType:"VARCHAR2(100)")

        modifyDataType(columnName:"CREATED_BY", tableName:"RCONFIG", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"EX_RCONFIG", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"APPLICATION_SETTINGS", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"RPT_TEMPLT", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"SUPER_QUERY", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"AUDIT_LOG", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"AUDIT_LOG_FIELD_CHANGE", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"COGNOS_REPORT", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"FIELD_PROFILE", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"PVUSER", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"ACCESS_CONTROL_GROUP", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"ROLE", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"USER_GROUP", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"CREATED_BY", tableName:"PREFERENCE", newDataType:"VARCHAR2(100)")


        modifyDataType(columnName:"MODIFIED_BY", tableName:"RCONFIG", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"EX_RCONFIG", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"APPLICATION_SETTINGS", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"RPT_TEMPLT", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"SUPER_QUERY", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"AUDIT_LOG", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"AUDIT_LOG_FIELD_CHANGE", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"COGNOS_REPORT", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"FIELD_PROFILE", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"PVUSER", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"ACCESS_CONTROL_GROUP", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"ROLE", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"USER_GROUP", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"PREFERENCE", newDataType:"VARCHAR2(100)")
    }
}