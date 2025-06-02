databaseChangeLog = {

    changeSet(author: "meenal", id: "202312151157-1") {
        sql("update ICSR_TEMPLT_QUERY set DIST_CHANNEL_NAME='EXTERNAL_FOLDER' where DIST_CHANNEL_NAME='OTHER_GATEWAY' ")
    }

    changeSet(author: "meenal", id: "202312151157-2") {
        sql("update EX_ICSR_TEMPLT_QUERY set DIST_CHANNEL_NAME='EXTERNAL_FOLDER' where DIST_CHANNEL_NAME='OTHER_GATEWAY' ")
    }

}
