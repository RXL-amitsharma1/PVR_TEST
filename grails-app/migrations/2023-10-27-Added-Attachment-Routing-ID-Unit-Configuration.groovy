databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "202310271305-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'UNIT_ATTACHMENT_REG_ID')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "UNIT_ATTACHMENT_REG_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202310271305-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'UNIT_ATTACHMENT_REG_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "UNIT_ATTACHMENT_REG_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

}