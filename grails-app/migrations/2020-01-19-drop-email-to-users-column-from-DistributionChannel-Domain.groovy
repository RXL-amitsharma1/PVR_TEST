databaseChangeLog = {
    changeSet(author: "anurag (generated)", id: "190120200545-1") {
        dropColumn(columnName: "EMAIL_TO_USERS", tableName: "DISTRIBUTION_CHANNEL")
    }
}