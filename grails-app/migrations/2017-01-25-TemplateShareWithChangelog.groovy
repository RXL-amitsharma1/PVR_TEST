databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-1") {
        createTable(tableName: "RPT_TEMPLATE_USER") {
            column(name: "report_template_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-2") {
        createTable(tableName: "RPT_TEMPLATE_USER_GROUP") {
            column(name: "report_template_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "user_group_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-3") {
        addPrimaryKey(columnNames: "report_template_id, user_id", constraintName: "RPT_TEMPLATE_USER_PK", tableName: "RPT_TEMPLATE_USER")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-4") {
        addPrimaryKey(columnNames: "report_template_id, user_group_id", constraintName: "RPT_TEMPLATE_USER_GROUP_PK", tableName: "RPT_TEMPLATE_USER_GROUP")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-5") {
        addForeignKeyConstraint(baseColumnNames: "report_template_id", baseTableName: "RPT_TEMPLATE_USER", constraintName: "FK5211BDC7772A2CA1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-8") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "RPT_TEMPLATE_USER", constraintName: "FK5211BDC78987134F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-6") {
        addForeignKeyConstraint(baseColumnNames: "report_template_id", baseTableName: "RPT_TEMPLATE_USER_GROUP", constraintName: "FK80336DE7772A2CA1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-7") {
        addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "RPT_TEMPLATE_USER_GROUP", constraintName: "FK80336DE7B308F9EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-15") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'RPT_TEMPLT', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM RPT_TEMPLT WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM USER_GROUP")
            }
        }
        sql("insert into USER_GROUP(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(1,'All Users', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-16") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'RPT_TEMPLT', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM RPT_TEMPLT WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '1', "SELECT COUNT(1) FROM USER_GROUP WHERE NAME='All Users' AND ID=1;")
            }
        }
        sql("insert into RPT_TEMPLATE_USER_GROUP (user_group_id,report_template_id) SELECT 1,id FROM RPT_TEMPLT WHERE IS_PUBLIC = 1;")

    }

    changeSet(author: "prashantsahi (generated)", id: "1485462406666-17") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RPT_TEMPLT', columnName: 'IS_PUBLIC')
        }
        dropColumn(columnName: "IS_PUBLIC", tableName: "RPT_TEMPLT")
    }

}
