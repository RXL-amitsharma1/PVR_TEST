databaseChangeLog = {
    changeSet(author: "sergey", id: "2020092216370000") {
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "TYPE", type: "varchar2(255 char)")
        }
        sql("UPDATE QUALITY_SAMPLING SET TYPE = 'SAMPLING'")
    }
    changeSet(author: "sergey", id: "2020092216370000-1") {
        sql("delete from localization");
    }
}