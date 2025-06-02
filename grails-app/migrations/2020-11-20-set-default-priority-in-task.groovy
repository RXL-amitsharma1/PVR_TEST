databaseChangeLog = {
    changeSet(author: "sargam", id: "20201130012054-1") {
        sql("UPDATE TASK SET PRIORITY='MEDIUM' WHERE PRIORITY='Medium'")
    }
}