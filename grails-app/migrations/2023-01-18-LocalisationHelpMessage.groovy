databaseChangeLog = {

    changeSet(author: "sergey", id: "20210118141913-1") {
        createTable(tableName: "LOCALIZATION_HELP") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "LOCALIZATION_HELP_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "LOCALIZATION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "clob")

        }
    }

    changeSet(author: "sergey (generated)", id: "20210118141913-3") {
        addForeignKeyConstraint(baseColumnNames: "LOCALIZATION_ID", baseTableName: "LOCALIZATION_HELP", constraintName: "FK_LOCALIZATION_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "LOCALIZATION", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey", id: "20210118141056-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: "LOCALIZATION_ID_PIDX1", tableName: "LOCALIZATION_HELP")
            }
        }
        createIndex(indexName: "LOCALIZATION_ID_PIDX1", tableName: "LOCALIZATION_HELP", unique: "true") {
            column(name: "LOCALIZATION_ID")
        }
    }

    changeSet(author: "sergey", id: "20210118141913-5") {
        createTable(tableName: "RELEASE_NOTES") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RELEASE_NOTES_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "RELEASE_NUMBER", type: "VARCHAR2(255)")
            column(name: "TITLE", type: "VARCHAR2(4000)")

            column(name: "DESCRIPTION", type: "clob")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }


changeSet(author: "sergey", id: "20210118141914-6") {
        createTable(tableName: "RELEASE_NOTES_ITEM") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RELEASE_NOTES_ITEM_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "HAS_DESCRIPTION", type: "number(1,0)")
            column(name: "RELEASE_NOTE_ID", type: "number(19,0)")
            column(name: "TITLE", type: "VARCHAR2(4000)")

            column(name: "DESCRIPTION", type: "clob")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey (generated)", id: "20210118141914-7") {
        addForeignKeyConstraint(baseColumnNames: "RELEASE_NOTE_ID", baseTableName: "RELEASE_NOTES_ITEM", constraintName: "FK_RELEASE_NOTE_ID_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RELEASE_NOTES", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey", id: "20210118141914-8") {
        createIndex(indexName: "RELEASE_NOTE_ID_PIDX1", tableName: "RELEASE_NOTES_ITEM") {
            column(name: "RELEASE_NOTE_ID")
        }
    }
    changeSet(author: "sergey", id: "20210118141919-9") {
        createTable(tableName: "RELEASE_NOTIFIER") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RELEASE_NOTIFIER_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "RELEASE_NUMBER", type: "VARCHAR2(255)"){
                constraints(nullable: "true")
            }

            column(name: "USER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

        }
    }
    changeSet(author: "sergey (generated)", id: "20210118141919-10") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "RELEASE_NOTIFIER", constraintName: "FK_RELEASE_NOTIFIER_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey", id: "20210118141919-19") {
        createIndex(indexName: "RELEASE_NOTIFIER_PIDX1", tableName: "RELEASE_NOTIFIER") {
            column(name: "USER_ID")
        }
    }

    changeSet(author: "sergey", id: "20210118141919-12") {
        sql("INSERT into RELEASE_NOTIFIER (ID, VERSION, USER_ID) select HIBERNATE_SEQUENCE.nextval,1, ID from PVUSER")
    }
    changeSet(author: "sergey (generated)", id: "20210118141919-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RELEASE_NOTES_ITEM', columnName: 'SUMMARY')
            }
        }
        addColumn(tableName: "RELEASE_NOTES_ITEM") {
            column(name: "SUMMARY", type: "VARCHAR2(4000)") {
                constraints(nullable: "true")
            }

            column(name: "SHORT_DESCRIPTION", type: "VARCHAR2(4000)") {
                constraints(nullable: "true")
            }
            column(name: "HIDDEN", type: "number(1,0)") {
                constraints(nullable: "true")
            }
            column(name: "SORT_NUMBER", type: "number(10,0)") {
                constraints(nullable: "true")
            }
        }
    }

}