databaseChangeLog = {

    changeSet(author: "sachinverma", id: "1506961508288-1") {
        sql("update ACTION_ITEM set APP_TYPE='ADHOC_REPORT' where APP_TYPE='Adhoc Report'");
        sql("update ACTION_ITEM set APP_TYPE='PERIODIC_REPORT' where APP_TYPE='Periodic Report'");
    }
}