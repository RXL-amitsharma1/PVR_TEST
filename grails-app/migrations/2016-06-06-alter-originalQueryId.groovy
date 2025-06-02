databaseChangeLog = {

    changeSet(author: "michaelmorett", id: "SuperQuery") {

        addColumn(tableName: "SUPER_QUERY") {
            column(name: "ORIG_QUERY_ID_NEW", type: "number(19,0)")
        }

        sql("update SUPER_QUERY set ORIG_QUERY_ID_NEW = ORIG_QUERY_ID;")
        sql("alter table SUPER_QUERY drop column ORIG_QUERY_ID;")
        sql("alter table SUPER_QUERY rename column ORIG_QUERY_ID_NEW to ORIG_QUERY_ID;")
    }

}
