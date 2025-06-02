databaseChangeLog = {
    changeSet(author: "gunjan", id: "202403141503-1") {
        sql("alter table PVC_ATTACH modify data blob;")
        sql("alter index PVC_ATTACHPK rebuild;")
    }
}