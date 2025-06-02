databaseChangeLog = {
    changeSet(author: "prashantsahi (generated)", id: "1488888888888-12") {
        sql(" merge into EX_TEMPLT_QUERY a\n" +
                " using (select id, ROW_NUMBER() OVER (PARTITION BY EX_RCONFIG_ID ORDER BY EX_TEMPLT_QUERY_IDX ) rnum from EX_TEMPLT_QUERY) b\n" +
                " on (a.id = b.id)\n" +
                " when matched then \n" +
                " update set EX_TEMPLT_QUERY_IDX = b.rnum - 1;");

    }
}