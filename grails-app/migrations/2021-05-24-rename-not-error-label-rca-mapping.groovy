databaseChangeLog = {
    changeSet(author: "shikhars", id: "240520211326") {
        sql("update localization set text='No Error' where code='rod.lateType.NOT_ERROR'")
    }
}

