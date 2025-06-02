databaseChangeLog = {
    changeSet(author: "RishabhJ", id: "202111251437-1") {
        sql("update publisher_tpl_prm set type='TEXT' where type='CODE' ")
    }
}