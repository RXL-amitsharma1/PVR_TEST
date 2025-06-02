databaseChangeLog = {
    changeSet(author: "ShubhamRx", id: "202502061456-01") {
        createTable(tableName: "ICSR_PROFILE_AUTH_TYPE") {


            column(name: "RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTHORIZATION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202502061456-02") {
        sql("""INSERT INTO ICSR_PROFILE_AUTH_TYPE (AUTHORIZATION_ID, RCONFIG_ID)
            SELECT 
                AUTH_TYPE AS AUTHORIZATION_ID, 
                ID AS RCONFIG_ID
            FROM RCONFIG
            WHERE AUTH_TYPE IS NOT NULL
        """)
    }

    changeSet(author: "ShubhamRx", id: "202502061456-03") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "RCONFIG", columnName: "AUTH_TYPE")
        }

        dropColumn(tableName: "RCONFIG", columnName: "AUTH_TYPE")
    }

    changeSet(author: "ShubhamRx", id: "202502061456-04") {
        createTable(tableName: "EX_ICSR_PROFILE_AUTH_TYPE") {


            column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTHORIZATION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202502061456-05") {
        sql("""INSERT INTO EX_ICSR_PROFILE_AUTH_TYPE (AUTHORIZATION_ID, EX_RCONFIG_ID)
            SELECT 
                AUTH_TYPE AS AUTHORIZATION_ID, 
                ID AS EX_RCONFIG_ID
            FROM EX_RCONFIG
            WHERE AUTH_TYPE IS NOT NULL
        """)
    }

    changeSet(author: "ShubhamRx", id: "202502061456-06") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "EX_RCONFIG", columnName: "AUTH_TYPE")
        }

        dropColumn(tableName: "EX_RCONFIG", columnName: "AUTH_TYPE")
    }
}
