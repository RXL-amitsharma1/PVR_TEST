import com.rxlogix.UserService

import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "sergey ", id: "20230920020205-20") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "audit_log_old")
        }
        sql("drop table audit_child_log cascade constraints")
        sql("drop table audit_log cascade constraints")
        renameTable(oldTableName: "audit_log_old", newTableName: "audit_log")
    }
    changeSet(author: "sergey ", id: "20230920020204-21") {

        sql("create table audit_log_bk AS (select * from audit_log)")
        sql("create table AUDIT_LOG_FIELD_CHANGE_bk AS (select * from AUDIT_LOG_FIELD_CHANGE)")
    }

    changeSet(author: "sergey ", id: "20230920020205-21") {
        renameTable(oldTableName: "AUDIT_LOG_FIELD_CHANGE", newTableName: "audit_child_log")
        sql("ALTER TABLE audit_child_log RENAME COLUMN ORIGINAL to OLD_VALUE")
        sql("ALTER TABLE audit_child_log RENAME COLUMN NEW to NEW_VALUE")
        sql("ALTER TABLE audit_child_log RENAME COLUMN FIELD_NAME to PROPERTY_NAME")
        sql("ALTER TABLE audit_child_log RENAME COLUMN AUDIT_LOG_ID to AUDIT_TRAIL_ID")

        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "CREATED_BY", tableName: "audit_child_log")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "MODIFIED_BY", tableName: "audit_child_log")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "ENTITY_ID", tableName: "audit_child_log")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "ENTITY_NAME", tableName: "audit_child_log")
        dropNotNullConstraint(columnDataType: "NUMBER(19)", columnName: "VERSION", tableName: "audit_child_log")



        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "CREATED_BY", tableName: "AUDIT_LOG")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "MODIFIED_BY", tableName: "AUDIT_LOG")
        dropNotNullConstraint(columnDataType: "NUMBER(19)", columnName: "VERSION", tableName: "AUDIT_LOG")
        sql("ALTER TABLE AUDIT_LOG RENAME COLUMN TIMEZONE to TIME_ZONE")
        sql("ALTER TABLE AUDIT_LOG RENAME COLUMN PARENT_OBJECT to ENTITY_NAME")
        sql("ALTER TABLE AUDIT_LOG ADD ENTITY_ID varchar2(255 char)")
        sql("UPDATE AUDIT_LOG set ENTITY_ID= to_char(PARENT_OBJECT_ID)")
        sql("update AUDIT_LOG set CATEGORY ='LOGIN_SUCCESS' where CATEGORY='LOGIN_SUCCESSFUL'")
        sql("update AUDIT_LOG set CATEGORY ='LOGIN_FAILED' where CATEGORY='LOGIN_FAILURE'")
        sql("update AUDIT_LOG set CATEGORY ='INSERT' where CATEGORY='CREATED'")
        sql("update AUDIT_LOG set CATEGORY ='UPDATE' where CATEGORY='MODIFIED'")
        sql("update AUDIT_LOG set CATEGORY ='DELETE' where CATEGORY='DELETED'")
        addColumn(tableName: "AUDIT_LOG") {
            column(name: "fullname", type: "varchar2(255 char)")
            column(name: "uri", type: "varchar2(255 char)")
            column(name: "application_Name", type: "varchar2(255 char)")
            column(name: "transaction_id", type: "varchar2(255 char)")
            column(name: "property_name", type: "varchar2(500 char)")
            column(name: "persisted_object_version", type: "number(19,0)")
            column(name: "module_name", type: "varchar2(255 char)")
            column(name: "sent_On_Server", type: "number(1,0)")
            column(name: "is_First_Entry_In_Transaction", type: "number(1,0)")
            column(name: "section_Child_Module", type: "number(1,0)")
            column(name: "entity_value", type: "clob")
        }
    }

    changeSet(author: "ShivamRx(generate)", id: "202311301646-1") {
        sql("update AUDIT_LOG set APPLICATION_NAME = 'PV Reports'")
        sql("update AUDIT_LOG set SECTION_CHILD_MODULE = '0' where FULLNAME IS NULL AND USERNAME <> 'System'")
    }

    changeSet(author: "ShivamRx (generated)", id: "202311301655-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'AUDIT_LOG_TR_INDX')
            }
        }
        createIndex(indexName: "AUDIT_LOG_TR_INDX", tableName: "AUDIT_LOG") {
            column(name: "TRANSACTION_ID")
        }
    }

    changeSet(author: "ShivamRx (generated)", id: "202311301646-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'AUDIT_LOG_DATE_INDX')
            }
        }
        createIndex(indexName: "AUDIT_LOG_DATE_INDX", tableName: "AUDIT_LOG") {
            column(name: "DATE_CREATED")
        }
    }

}