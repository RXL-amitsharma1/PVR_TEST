databaseChangeLog = {
    changeSet(author: "meenal", id: "202401151218-1") {
        sql("UPDATE PREFERENCE SET THEME = 'Gradient Blue' WHERE THEME = 'Gradient Blue (default)'")
    }
}
