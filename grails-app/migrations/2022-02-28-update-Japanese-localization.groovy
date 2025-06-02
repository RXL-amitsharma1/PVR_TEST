databaseChangeLog = {

    changeSet(author: "Rishabh Jain", id: "202202281730-1") {
        sql("delete from localization where loc='ja' ")
    }
}