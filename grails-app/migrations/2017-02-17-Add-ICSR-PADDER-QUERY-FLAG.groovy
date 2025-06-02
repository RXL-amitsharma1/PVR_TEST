databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1486653279244-1") {
        addColumn(tableName: "SUPER_QUERY") {
            column(name: "ICSR_PADDER_AGENCY_CASES", type: "number(1,0)")
        }
        sql("update SUPER_QUERY set ICSR_PADDER_AGENCY_CASES = 0")
    }

}
