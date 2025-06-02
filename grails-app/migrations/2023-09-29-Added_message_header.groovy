databaseChangeLog = {

    changeSet(author: "VivekKumar (generated)", id: "202309291815") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'MESSAGE_HEADER')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "MESSAGE_HEADER", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "VivekKumar (generated)", id: "202309291829") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'MESSAGE_HEADER')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "MESSAGE_HEADER", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }
}