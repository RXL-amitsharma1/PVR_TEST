databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1487936665557-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCL_ALL_STUD_DRUG_CASES')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCL_ALL_STUD_DRUG_CASES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE RCONFIG SET INCL_ALL_STUD_DRUG_CASES = 0;")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "INCL_ALL_STUD_DRUG_CASES")
    }


    changeSet(author: "sachinverma (generated)", id: "1487936665557-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCL_ALL_STUD_DRUG_CASES')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCL_ALL_STUD_DRUG_CASES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE EX_RCONFIG SET INCL_ALL_STUD_DRUG_CASES=0;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "INCL_ALL_STUD_DRUG_CASES")
    }


    changeSet(author: "sachinverma (generated)", id: "1487936665557-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_SERIES', columnName: 'INCL_ALL_STUD_DRUG_CASES')
            }
        }
        addColumn(tableName: "CASE_SERIES") {
            column(name: "INCL_ALL_STUD_DRUG_CASES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE CASE_SERIES SET INCL_ALL_STUD_DRUG_CASES = 0;")
        addNotNullConstraint(tableName: "CASE_SERIES", columnName: "INCL_ALL_STUD_DRUG_CASES")
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665557 - 4") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'INCL_ALL_STUD_DRUG_CASES')
            }
        }
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "INCL_ALL_STUD_DRUG_CASES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE EX_CASE_SERIES SET INCL_ALL_STUD_DRUG_CASES = 0;")
        addNotNullConstraint(tableName: "EX_CASE_SERIES", columnName: "INCL_ALL_STUD_DRUG_CASES")
    }


    changeSet(author: "sachinverma (generated)", id: "1487936665557 - 5") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'INCL_ALL_STUD_DRUG_CASES')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "INCL_ALL_STUD_DRUG_CASES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE REPORT_REQUEST SET INCL_ALL_STUD_DRUG_CASES = 0;")
        addNotNullConstraint(tableName: "REPORT_REQUEST", columnName: "INCL_ALL_STUD_DRUG_CASES")
    }

}
