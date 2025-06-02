databaseChangeLog = {
    changeSet(author: "vivek", id: "202401031942-1") {
        sql("UPDATE PREFERENCE SET THEME = 'Gradient Blue (default)' WHERE THEME IS NULL")
    }
}
