databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202406101225-1") {
        sql("UPDATE PREFERENCE SET THEME = 'gradient_blue' WHERE THEME IS NULL OR THEME IN ('Gradient Blue', 'Gradient Blue (default)')")
    }
}
