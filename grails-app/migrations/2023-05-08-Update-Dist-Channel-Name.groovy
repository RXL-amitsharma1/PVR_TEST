databaseChangeLog = {

    changeSet(author: "meenal", id: "202305081525-1") {
        sql("update ICSR_TEMPLT_QUERY set DIST_CHANNEL_NAME='PV_GATEWAY' where DIST_CHANNEL_NAME='E2B' ")
    }

    changeSet(author: "meenal", id: "202305081525-2") {
        sql("update EX_ICSR_TEMPLT_QUERY set DIST_CHANNEL_NAME='PV_GATEWAY' where DIST_CHANNEL_NAME='E2B' ")
    }

}
