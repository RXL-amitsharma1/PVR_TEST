databaseChangeLog = {

    changeSet(author: "Garima (generated)", id: "149861248491-50") {
        sql("delete from localization");
    }

    changeSet(author: "Garima (generated)", id: "149861248491-51") {
        sql("update RCONFIG set PR_TYPE='PADER' where PR_TYPE='NDA'");
    }

    changeSet(author: "Garima (generated)", id: "149861248491-52") {
        sql("update EX_RCONFIG set PR_TYPE='PADER' where PR_TYPE='NDA'");
    }

}