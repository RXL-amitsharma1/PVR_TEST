databaseChangeLog = {
    changeSet(author: "jitin (generated) ", id: "1536123112619-7") {
        //Update the foreing key bindings between DATE_RANGE and TEMPLT_QUERY
        sql("alter table DATE_RANGE add TEMPLATE_QUERY_ID NUMBER(19)");
        sql("UPDATE DATE_RANGE SET TEMPLATE_QUERY_ID = (select TEMPLT_QUERY.ID from TEMPLT_QUERY WHERE DATE_RANGE_ID = DATE_RANGE.ID)");
        sql("alter table TEMPLT_QUERY drop column DATE_RANGE_ID");

        //Update the foreing key bindings between EX_DATE_RANGE and EX_TEMPLT_QUERY
        sql("alter table EX_DATE_RANGE add EX_TEMPLATE_QUERY_ID NUMBER(19)");
        sql("UPDATE EX_DATE_RANGE SET EX_TEMPLATE_QUERY_ID = (select EX_TEMPLT_QUERY.ID from EX_TEMPLT_QUERY WHERE EX_DATE_RANGE_INFO_ID = EX_DATE_RANGE.ID)");
        sql("alter table EX_TEMPLT_QUERY drop column EX_DATE_RANGE_INFO_ID");
    }
}
