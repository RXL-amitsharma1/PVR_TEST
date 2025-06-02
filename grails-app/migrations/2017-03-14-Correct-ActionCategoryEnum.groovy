databaseChangeLog = {

    changeSet(author: "Sachin (generated)", id: "1474699999999-1") {

        sql("update ACTION_ITEM set ACTION_CATEGORY = 'REPORT_REQUEST' where ACTION_CATEGORY = 'Report Request'")
        sql("update ACTION_ITEM set ACTION_CATEGORY = 'REQUEST_MISSING_INFORMATION' where ACTION_CATEGORY = 'Request Missing Information'")
        sql("update ACTION_ITEM set ACTION_CATEGORY = 'PROCESS_CASE' where ACTION_CATEGORY = 'Process Case'")
        sql("update ACTION_ITEM set ACTION_CATEGORY = 'PERIODIC_REPORT' where ACTION_CATEGORY = 'Periodic Report'")
        sql("update ACTION_ITEM set ACTION_CATEGORY = 'CONFIGURE_REPORT' where ACTION_CATEGORY = 'Configure Report'")
    }
}