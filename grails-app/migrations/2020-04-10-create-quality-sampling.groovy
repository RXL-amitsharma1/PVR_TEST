databaseChangeLog = {
    changeSet(author: "shikhars", id: "140420200300") {
        createTable(tableName: "QUALITY_SAMPLING") {
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
        sql("ALTER TABLE QUALITY_SAMPLING ADD CONSTRAINT CHK_JSON_PVQ_SAMPLING CHECK(METADATA IS JSON) ENABLE")
        sql("ALTER TABLE QUALITY_SAMPLING MODIFY METADATA NOT NULL")
        addPrimaryKey(columnNames: "ID", constraintName: "QUALITY_SAMPLING_PK", tableName: "QUALITY_SAMPLING")
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_USER", baseTableName: "QUALITY_SAMPLING", constraintName: "FK_QUALITY_SAMPLING_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        sql("CREATE SEQUENCE QUALITY_SAMPLING_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }

    changeSet(author: "anurag (generated)", id: "211020202400-2") {
        sql("alter table QUALITY_SAMPLING modify REPORT_ID number(19, 0);")
    }

    changeSet(author: "sarthak (generated)", id: "2519888129033-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'EXECUTED_TEMPLATE_ID')
            }
        }

        addColumn(tableName: "QUALITY_SAMPLING") {

            column(name: "EXECUTED_TEMPLATE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

    }
}