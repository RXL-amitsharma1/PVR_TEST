databaseChangeLog = {

    changeSet(author: "Chetan (generated)", id: "1460366239483-1") {
        createTable(tableName: "localization") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "localizationPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "varchar2(250 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "loc", type: "varchar2(4 char)") {
                constraints(nullable: "false")
            }

                column(name: "relevance", type: "number(3,0)") {
                constraints(nullable: "false")
            }

            column(name: "text", type: "varchar2(2000 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chetan (generated)", id: "1460366239483-7") {
        createIndex(indexName: "localizations_idx", tableName: "localization") {
            column(name: "code")
        }
    }

    changeSet(author: "Chetan (generated)", id: "1460366239483-8") {
        createIndex(indexName: "unique_loc", tableName: "localization", unique: "true") {
            column(name: "code")

            column(name: "loc")
        }
    }
}

