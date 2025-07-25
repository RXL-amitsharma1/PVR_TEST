databaseChangeLog = {
    changeSet(author: "Pragya(generated)", id: "202211261207-0"){
        modifyDataType(columnName: "USERNAME", newDataType: "varchar2(255 byte)", tableName: "PVUSER")
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(255 byte)", tableName: "PVUSER")
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(255 byte)", tableName: "PVUSER")
        modifyDataType(columnName: "ENTITY_NAME", newDataType: "varchar2(700 byte)", tableName: "AUDIT_LOG_FIELD_CHANGE")
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(255 byte)", tableName: "AUDIT_LOG")
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(255 byte)", tableName: "AUDIT_LOG")
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(255 byte)", tableName: "PREFERENCE")
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(255 byte)", tableName: "PREFERENCE")
    }
    changeSet(author: "Pragya", id: "202211261207-1"){
        modifyDataType(columnName:"CREATED_BY", tableName:"RCONFIG", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"EX_RCONFIG", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"APPLICATION_SETTINGS", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"RPT_TEMPLT", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"SUPER_QUERY", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"AUDIT_LOG_FIELD_CHANGE", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"COGNOS_REPORT", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"FIELD_PROFILE", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"ACCESS_CONTROL_GROUP", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"ROLE", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"CREATED_BY", tableName:"USER_GROUP", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"RCONFIG", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"EX_RCONFIG", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"APPLICATION_SETTINGS", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"RPT_TEMPLT", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"SUPER_QUERY", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"AUDIT_LOG_FIELD_CHANGE", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"COGNOS_REPORT", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"FIELD_PROFILE", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"ACCESS_CONTROL_GROUP", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"ROLE", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"USER_GROUP", newDataType:"VARCHAR2(255)")
    }
}
