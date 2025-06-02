databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "202006095709138-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "CAPA_8D", columnName: "ISSUE_NUMBER")
            }
        }
        addColumn(tableName: "CAPA_8D") {
            column(name: "ISSUE_NUMBER", type: "varchar2(255 char)") {
                constraints(nullable: "false", unique: "true")
            }

            column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}