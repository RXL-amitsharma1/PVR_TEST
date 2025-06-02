databaseChangeLog = {

    changeSet(author: "meenal(generated)", id: "202407081758-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'PREF_LANGUAGE')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "PREF_LANGUAGE", type: "varchar2(255 char)")
        }
        sql("UPDATE UNIT_CONFIGURATION set PREF_LANGUAGE = 'eng' where PREF_LANGUAGE is null")
    }

    changeSet(author: "meenal(generated)", id: "202407081758-2") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "RECEIVER_PREF_LANGUAGE")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECEIVER_PREF_LANGUAGE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal(generated)", id: "202407081758-3") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "SENDER_PREF_LANGUAGE")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SENDER_PREF_LANGUAGE", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }

}
