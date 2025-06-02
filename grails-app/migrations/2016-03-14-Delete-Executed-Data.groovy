databaseChangeLog = {
    changeSet(author: "Pomi (generated)", id: "delete-executed-data-1457572933569-1") {
        sqlFile("path": "liquibase/dropExecutedData.sql", "stripComments": true)
    }
}