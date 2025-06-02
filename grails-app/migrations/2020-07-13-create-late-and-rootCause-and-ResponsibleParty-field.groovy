databaseChangeLog = {
    changeSet(author: "anurag", id: "130720201431-1") {
        createTable(tableName: "LATE") {

            column(name: "ID", type: "number(19,0)"){
                constraints(nullable: false)
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: false)
            }
        }
        addPrimaryKey(columnNames: "ID", constraintName: "LATE_PK", tableName: "LATE")
    }


    changeSet(author: "anurag", id: "130720201431-2") {
        createTable(tableName: "ROOT_CAUSE") {

            column(name: "ID", type: "number(19,0)"){
                constraints(nullable: false)
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: false)
            }

            column(name: "LATE_ID", type : "number(19,0)") {
                constraints(nullable: false)
            }
        }
        addForeignKeyConstraint(baseColumnNames: "LATE_ID", baseTableName: "ROOT_CAUSE", constraintName: "FKC472BE989C7C455F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "LATE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "ID", constraintName: "ROOT_CAUSE_PK", tableName: "ROOT_CAUSE")
    }

//    changeSet(author: "anurag", id: "130720201431-3") {
//        createTable(tableName: "RESPONSIBLE_PARTY") {
//            column(name: "ID", type: "number(19, 0)"){
//                constraints(nullable: "false")
//            }
//
//            column(name: "NAME", type: "varchar2(255 char)") {
//                constraints(nullable: "false")
//            }
//
//            column(name: "ROOT_CAUSE_ID", type: "number(19, 0)"){
//                constraints(nullable: "false")
//            }
//        }
//        addForeignKeyConstraint(baseColumnNames: "ROOT_CAUSE_ID", baseTableName: "RESPONSIBLE_PARTY", constraintName: "FKC472BE989C7C455E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ROOT_CAUSE", referencesUniqueColumn: "false")
//        addPrimaryKey(columnNames: "ID", constraintName: "RESPONSIBLE_PARTY_PK", tableName: "RESPONSIBLE_PARTY")
//    }
}