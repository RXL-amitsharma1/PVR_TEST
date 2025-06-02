databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "157190286724102019-1") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "ICSR_TEMPLT_QUERY")
            }
        }

        createTable(tableName: "ICSR_TEMPLT_QUERY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ICSR_TEMPLT_QUERY_PK")
            }

            column(name: "AUTHORIZATION_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "DUE_DAYS", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "MSG_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DIST_CHANNEL_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ORDER_NO", type: "number(19,0)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "anurag (generated)", id: "157190286724102019-2") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "EX_ICSR_TEMPLT_QUERY")
            }
        }

        createTable(tableName: "EX_ICSR_TEMPLT_QUERY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_ICSR_TEMPLT_QUERY_PK")
            }

            column(name: "AUTHORIZATION_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "DUE_DAYS", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "MSG_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DIST_CHANNEL_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ORDER_NO", type: "number(19,0)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "anurag (generated)", id: "157190286724102019-3") {
        sql("update ICSR_TEMPLT_QUERY set AUTHORIZATION_TYPE = 'MARKETED_DRUG' where AUTHORIZATION_TYPE='Marketed Drug'")
        sql("update ICSR_TEMPLT_QUERY set AUTHORIZATION_TYPE = 'INVESTIGATIONAL_DRUG' where AUTHORIZATION_TYPE='Investigational Drug'")
        sql('update ICSR_TEMPLT_QUERY set MSG_TYPE = UPPER(MSG_TYPE) where MSG_TYPE is not null')
        sql('update ICSR_TEMPLT_QUERY set DIST_CHANNEL_NAME = UPPER(DIST_CHANNEL_NAME) where DIST_CHANNEL_NAME is not null')
    }

    changeSet(author: "anurag (generated)", id: "157190286724102019-4") {
        sql("update EX_ICSR_TEMPLT_QUERY set AUTHORIZATION_TYPE = 'MARKETED_DRUG' where AUTHORIZATION_TYPE='Marketed Drug'")
        sql("update EX_ICSR_TEMPLT_QUERY set AUTHORIZATION_TYPE = 'INVESTIGATIONAL_DRUG' where AUTHORIZATION_TYPE='Investigational Drug'")
        sql('update EX_ICSR_TEMPLT_QUERY set MSG_TYPE = UPPER(MSG_TYPE) where MSG_TYPE is not null')
        sql('update EX_ICSR_TEMPLT_QUERY set DIST_CHANNEL_NAME = UPPER(DIST_CHANNEL_NAME) where DIST_CHANNEL_NAME is not null')
    }

}