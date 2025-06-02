databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "202105130956-1") {
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "parent_entity_key", type: "varchar2(1000 char)")
        }
        createIndex(indexName: "par_ent_key_uniq_202105130956", tableName: "ACTION_ITEM", unique: "false") {
            column(name: "parent_entity_key")
        }
    }
    changeSet(author: "sergey (generated)", id: "202105130956-7") {
        createTable(tableName: "ACTION_PLAN_SUMMARY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_PLAN_SUMPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "NOTE", type: "VARCHAR2(4000 char)") {
                constraints(nullable: "true")
            }
            column(name: "FROM_DATE", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "TO_DATE", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "parent_entity_key", type: "varchar2(1000 char)") {
                constraints(nullable: "false")
            }
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey (generated)", id: "202105130956-8") {

        createIndex(indexName: "par_ent_key_uniq_202105291242", tableName: "ACTION_PLAN_SUMMARY", unique: "false") {
            column(name: "parent_entity_key")
        }
    }

    changeSet(author: "sergey (generated)", id: "202105130956-9") {
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "period_from", type: "timestamp")
        }
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "period_to", type: "timestamp")
        }
    }
}

