databaseChangeLog = {

    changeSet(author: "anurag", id: "110920190118-1") {
        sql("update ICSR_TEMPLT_QUERY set MSG_TYPE='BACKLOG' where MSG_TYPE='CTASR' or MSG_TYPE='MASTERICHICSR' ")
    }
}