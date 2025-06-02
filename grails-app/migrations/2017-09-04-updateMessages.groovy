databaseChangeLog = {

    changeSet(author: "sgologuzov", id: "1504542457580-1") {
        sql("delete from localization");
    }
}