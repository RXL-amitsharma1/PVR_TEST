databaseChangeLog = {
    changeSet(author: "vivek", id: "202502211236-1") {
        sql("CREATE SEQUENCE FILE_NAME_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 999 CYCLE;")
    }
}