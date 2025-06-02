databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1521471162397-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "ODATA_SETTINGS")
            }
        }
        createTable(tableName: "ODATA_SETTINGS") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ODATA_SETTINGPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "DS_LOGIN", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DS_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DS_PASSWORD", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DS_URL", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "SETTINGS", type: "clob")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1521471162396-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "CASE_DATA_QUALITY")
        }
        sql("drop table PVR_SETTINGS cascade constraints")
    }
    changeSet(author: "forxsv (generated)", id: "1521471162396-3") {
        sql("delete from localization");
    }
}
