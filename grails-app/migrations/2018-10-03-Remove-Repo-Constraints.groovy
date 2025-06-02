databaseChangeLog = {
    changeSet(author: "gologuzov (generated)", id: "1538574964190-104") {
        dropForeignKeyConstraint(baseTableName: "REPO_FILE_RESOURCE", constraintName: "FK52FF9964E0996AE9")
    }

    changeSet(author: "gologuzov (generated)", id: "1538574964190-105") {
        dropForeignKeyConstraint(baseTableName: "REPO_FOLDER", constraintName: "FKA4C55DBE0996AE9")
    }
}