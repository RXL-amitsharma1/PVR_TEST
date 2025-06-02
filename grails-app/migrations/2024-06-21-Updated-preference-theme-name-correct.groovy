databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202406211030-1") {
        sql("UPDATE PREFERENCE SET THEME = 'gradient_blue' WHERE THEME IS NULL OR THEME IN ('Gradient Blue', 'Gradient Blue (default)')")
    }

    changeSet(author: "sergey", id: "20240717093100-1") {
        addColumn(tableName: "PREFERENCE") {
            column(name: "user_preferences", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
}
