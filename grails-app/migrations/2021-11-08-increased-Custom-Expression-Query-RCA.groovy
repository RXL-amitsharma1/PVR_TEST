databaseChangeLog = {
    changeSet(author: "anurag (generated)", id: "202111081519-1") {
        sql("alter table QUERY_RCA modify RC_CUSTOM_EXPRESSION varchar2(32000 char)");
        sql("alter table QUERY_RCA modify RP_CUSTOM_EXPRESSION varchar2(32000 char)");
        sql("alter table QUERY_RCA modify RC_CLASS_CUSTOM_EXP varchar2(32000 char)");
        sql("alter table QUERY_RCA modify RC_SUB_CAT_CUSTOM_EXP varchar2(32000 char)");
    }
}