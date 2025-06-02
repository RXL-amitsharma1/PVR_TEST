databaseChangeLog = {
    changeSet(author: "sargam", id: "202007140302") {
        sql("UPDATE REPORT_REQUEST_TYPE SET IS_DELETED=1 WHERE rowid NOT IN(SELECT MIN(rowid) FROM REPORT_REQUEST_TYPE GROUP BY name);")
    }
}