databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1497018554418-1") {
        sql("update RWIDGET set WIDGET_TYPE='AGGREGATE_REPORTS_SUMMARY' where WIDGET_TYPE='AGGREGATE_REPORTS_STATUS'")
    }
}