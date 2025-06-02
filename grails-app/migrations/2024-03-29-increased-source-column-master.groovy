databaseChangeLog = {
    changeSet(author: "amityadav", id: "202403291643") {
        sql("alter table SOURCE_TABLE_MASTER modify TABLE_ALIAS varchar2(50 char)")
    }
//increased the column size of rpt_field and localization table
    changeSet(author: "amityadav", id: "202405011229") {
        sql("alter table RPT_FIELD modify NAME varchar2(500 char)")
    }

    changeSet(author: "amityadav", id: "202405011230") {
        sql("alter table LOCALIZATION modify CODE varchar2(500 char)")
    }

}