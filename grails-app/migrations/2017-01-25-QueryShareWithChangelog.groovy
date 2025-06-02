databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-1") {
        createTable(tableName: "SUPER_QUERY_USER") {
            column(name: "query_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-2") {
        createTable(tableName: "SUPER_QUERY_USER_GROUP") {
            column(name: "query_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "user_group_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-42") {
        addPrimaryKey(columnNames: "query_id, user_id", constraintName: "SUPER_QUERY_UPK", tableName: "SUPER_QUERY_USER")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-43") {
        addPrimaryKey(columnNames: "query_id, user_group_id", constraintName: "SUPER_QUERY_UGPK", tableName: "SUPER_QUERY_USER_GROUP")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-48") {
        addForeignKeyConstraint(baseColumnNames: "query_id", baseTableName: "SUPER_QUERY_USER", constraintName: "FKA331EC669F3E4B5D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-49") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "SUPER_QUERY_USER", constraintName: "FKA331EC668987134F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-50") {
        addForeignKeyConstraint(baseColumnNames: "query_id", baseTableName: "SUPER_QUERY_USER_GROUP", constraintName: "FKB2F7E9469F3E4B5D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-51") {
        addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "SUPER_QUERY_USER_GROUP", constraintName: "FKB2F7E946B308F9EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-63") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'SUPER_QUERY', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM SUPER_QUERY WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM USER_GROUP")
            }
        }
        sql("insert into USER_GROUP(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(1,'All Users', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486552398877-64") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'SUPER_QUERY', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM SUPER_QUERY WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '1', "SELECT COUNT(1) FROM USER_GROUP WHERE NAME='All Users' and ID=1;")
            }
        }
        sql("insert into SUPER_QUERY_USER_GROUP (user_group_id,query_id) select 1,id from SUPER_QUERY WHERE IS_PUBLIC = 1;")

    }

    changeSet(author: "prashantsahi (generated)", id: "1485256332112-65") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SUPER_QUERY', columnName: 'IS_PUBLIC')
        }
        dropColumn(columnName: "IS_PUBLIC", tableName: "SUPER_QUERY")
    }
}
