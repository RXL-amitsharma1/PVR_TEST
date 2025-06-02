databaseChangeLog = {
        changeSet(author: "shikhars", id: "100420202400") {
        createTable(tableName: "QUALITY_CASE_DATA") {

            column(name: "ID", type: "number(19,0)"){
                constraints(nullable: false)
            }

            column(name: "REPORT_ID", type: "number(6,0)")

            column(name: "CASE_NUM", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "ERROR_TYPE", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "METADATA", type: "clob")

            column(name: "TRIAGE_ACTION", type: "number(1,0)")

            column(name: "ISDELETED", type: "number(1,0)")

            column(name: "PRIORITY", type: "varchar2(255 char)")

            column(name: "JUSTIFICATION", type: "varchar2(4000 char)")

            column(name: "ENTRY_TYPE", type: "varchar2(1 char)"){
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_USER", type: "number(19,0)")

            column(name: "CREATED_BY", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)"){
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "TIMESTAMP(6)"){
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "TIMESTAMP(6)"){
                constraints(nullable: "false")
            }
        }
        sql("ALTER TABLE QUALITY_CASE_DATA ADD CONSTRAINT CHK_JSON_PVQuality CHECK(METADATA IS JSON) ENABLE")
        sql("ALTER TABLE QUALITY_CASE_DATA MODIFY METADATA NOT NULL")
        addPrimaryKey(columnNames: "ID", constraintName: "QUALITY_CASE_DATA_PK", tableName: "QUALITY_CASE_DATA")
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_USER", baseTableName: "QUALITY_CASE_DATA", constraintName: "FK_QUALITY_CASE_DATA_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        sql("CREATE SEQUENCE QUALITY_CASE_DATA_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }

    changeSet(author: "anurag (generated)", id: "211020202400-2") {
        sql("alter table QUALITY_CASE_DATA modify REPORT_ID number(19, 0);")
    }
    changeSet(author: "sarthak (generated)", id: "2519888129032-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'EXECUTED_TEMPLATE_ID')
            }
        }

        addColumn(tableName: "QUALITY_CASE_DATA") {

            column(name: "EXECUTED_TEMPLATE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

    }
}