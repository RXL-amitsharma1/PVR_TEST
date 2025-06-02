databaseChangeLog = {

    changeSet(author: "rxl-shivamg1", id: "202401201556-1") {
        sql("ALTER TABLE DRILLDOWN_DATA DROP COLUMN CASE_VERSION")
        sql("ALTER TABLE DRILLDOWN_DATA DROP COLUMN SENDER_ID")
        sql("ALTER TABLE DRILLDOWN_DATA ADD (CASE_VERSION NUMBER GENERATED ALWAYS AS (json_value(cll_row_data, '\$.masterVersionNum' RETURNING NUMBER ))); ");
        sql("ALTER TABLE DRILLDOWN_DATA ADD (SENDER_ID NUMBER GENERATED ALWAYS AS (json_value(cll_row_data, '\$.pvcIcSenderId' RETURNING NUMBER ))); ");
    }
}
