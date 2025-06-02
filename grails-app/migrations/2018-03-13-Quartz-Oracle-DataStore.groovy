
databaseChangeLog = {
    changeSet(author: "aditya", id: "1335831637231-1") {
        sqlFile( path: "liquibase/quartz_oracle_tables.sql", "stripComments": true)
    }
}