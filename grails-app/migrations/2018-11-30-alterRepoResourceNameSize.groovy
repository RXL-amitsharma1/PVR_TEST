databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "2519888029031-2") {
        sql("alter table REPO_RESOURCE modify NAME VARCHAR2(1006 CHAR);")
        sql("alter table REPO_RESOURCE modify LABEL VARCHAR2(1000 CHAR);")
    }
}

