databaseChangeLog = {

    changeSet(author: "sergey", id: "202103308141977-1") {
        createTable(tableName: "SYSTEM_NOTES") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SYSTEM_NOTES_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TITLE", type: "VARCHAR2(4000)")

            column(name: "DESCRIPTION", type: "clob")
            column(name: "DETAILS", type: "clob")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

           column(name: "PUBLISHED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey", id: "202103308141977-2") {
        createTable(tableName: "SYS_NOTE_NOTIFIER") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SYS_NOTE_NOTIFIER_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "NOTIFICATION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey", id: "202103308141977-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SYS_NOTE_NOTIFIER_USR')
            }
        }
        createIndex(indexName: "IDX_SYS_NOTE_NOTIFIER_USR", tableName: "SYS_NOTE_NOTIFIER") {
            column(name: "USER_ID")
        }
    }
    changeSet(author: "sergey", id: "202103308141977-4") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "SYS_NOTE_NOTIFIER", constraintName: "FKPVUSER_SYS_NOTE_NOTIFIER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER")
    }
    changeSet(author: "sergey", id: "202103308141977-5") {
        addForeignKeyConstraint(baseColumnNames: "NOTIFICATION_ID", baseTableName: "SYS_NOTE_NOTIFIER", constraintName: "FKSYS_NOTE_SYS_NOTES", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SYSTEM_NOTES")
    }


    changeSet(author: "sergey", id: "202103308141977-7") {
        createTable(tableName: "INTERACTIVE_HELP") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "INTERACTIVE_HELP_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TITLE", type: "VARCHAR2(4000)")
            column(name: "PAGE", type: "VARCHAR2(4000)")

            column(name: "DESCRIPTION", type: "clob")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "PUBLISHED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey (generated)", id: "202103308141977-8") {
        createTable(tableName: "SYSTEM_NOTES_USER_GROUPS") {
            column(name: "SYS_NOTE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_GROUP_ID", type: "number(19,0)")

            column(name: "USER_GROUP_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sergey", id: "202103308141977-9") {
        addForeignKeyConstraint(baseColumnNames: "SYS_NOTE_ID", baseTableName: "SYSTEM_NOTES_USER_GROUPS", constraintName: "FKSNUGSYS_NOTE_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SYSTEM_NOTES")
    }
    changeSet(author: "sergey", id: "202103308141977-10") {
        addForeignKeyConstraint(baseColumnNames: "USER_GROUP_ID", baseTableName: "SYSTEM_NOTES_USER_GROUPS", constraintName: "FKSNUGUSER_GROUP_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP")
    }

    changeSet(author: "sergey (generated)", id: "202103308141977-11") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "HELP_LINK")
            }
        }
        createTable(tableName: "HELP_LINK") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "HELP_LINK_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "LINK", type: "VARCHAR2(4000)")
            column(name: "PAGE", type: "VARCHAR2(4000)")
        }
    }


}