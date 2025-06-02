databaseChangeLog = {

    changeSet(author: "jitin (generated)", id: "1519888029031-1") {

        createTable(tableName: "ACTION_ITEM_CATEGORY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_ITEM_CPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "KEY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "FOR_PVQ", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-22") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTION_ITEM', columnName: 'ACTION_CATEGORY')
        }
        sql("""INSERT ALL
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('1', '1','Report Request','REPORT_REQUEST','Action Item related to a report request', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('2', '1','Request Missing Information', 'REQUEST_MISSING_INFORMATION', 'Action Item to address missing report information', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('3', '1','Process Case', 'PROCESS_CASE', 'Action Item related to case processing', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('4', '1','Aggregate Report', 'PERIODIC_REPORT', 'Action Item related to Aggregate report', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('5', '1','Configure Report', 'CONFIGURE_REPORT', 'Action Item to configure a new report', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('6', '1','Adhoc Report', 'ADHOC_REPORT', 'Action Item related to Adhoc Report', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('7', '1','Report Review', 'REVIEW_REPORT', 'Action Item related to Report Review', 0)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('8', '1','Quality Module', 'QUALITY_MODULE', 'Action item for PVQuality', 1)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('9', '1','Preventive', 'QUALITY_MODULE_PREVENTIVE', 'Action item for corrective action in PVQuality', 1)
   INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES ('10', '1','Corrective', 'QUALITY_MODULE_CORRECTIVE', 'Action item for preventive action in PVQuality', 1)
   SELECT 1 FROM DUAL;""")
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTION_ITEM', columnName: 'action_category_id')
            }
        }

        addColumn(tableName: "ACTION_ITEM") {

            column(name: "action_category_id", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "jitin (generated)", id: "1519888029031-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTION_ITEM', columnName: 'action_category_id')
        }
        sql("""update action_item ai
                set ai.action_category_id = (select case when ai.action_category='REPORT_REQUEST'
                then ac.id
                when ai.action_category='REQUEST_MISSING_INFORMATION'
                then ac.id
                when ai.action_category='PROCESS_CASE'
                then ac.id
                when ai.action_category='PERIODIC_REPORT'
                then ac.id
                when ai.action_category='CONFIGURE_REPORT'
                then ac.id
                when ai.action_category='ADHOC_REPORT'
                then ac.id
                when ai.action_category='QUALITY_MODULE'
                then ac.id
                when ai.action_category='REVIEW_REPORT'
                then ac.id
                when ai.action_category='QUALITY_MODULE_PREVENTIVE'
                then ac.id
                when ai.action_category='QUALITY_MODULE_CORRECTIVE'
                then ac.id
                end
                from ACTION_ITEM_CATEGORY ac
                where ai.action_category=ac.key)""")

        addNotNullConstraint(tableName: "ACTION_ITEM", columnName: "action_category_id")
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-5") {
        createIndex(indexName: "NAME_uniq_1519888013154", tableName: "ACTION_ITEM_CATEGORY", unique: "true") {
            column(name: "NAME")
        }
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTION_ITEM', columnName: 'ACTION_CATEGORY')
        }
        dropColumn(columnName: "ACTION_CATEGORY", tableName: "ACTION_ITEM")
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-7") {
        addForeignKeyConstraint(baseColumnNames: "action_category_id", baseTableName: "ACTION_ITEM", constraintName: "FKE077AB7CB6118D58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM_CATEGORY", referencesUniqueColumn: "false")
    }


    changeSet(author: "jitin (generated)", id: "1519888029031-8") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_TASK', columnName: 'action_category_id')
            }
        }

        addColumn(tableName: "REPORT_TASK") {

            column(name: "action_category_id", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "jitin (generated)", id: "1519888029031-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_TASK', columnName: 'action_category_id')
        }
        sql("""update REPORT_TASK rt
                set rt.action_category_id = (select case when rt.action_category='REPORT_REQUEST'
                then ac.id
                when rt.action_category='REQUEST_MISSING_INFORMATION'
                then ac.id
                when rt.action_category='PROCESS_CASE'
                then ac.id
                when rt.action_category='PERIODIC_REPORT'
                then ac.id
                when rt.action_category='CONFIGURE_REPORT'
                then ac.id
                when rt.action_category='ADHOC_REPORT'
                then ac.id
                when rt.action_category='QUALITY_MODULE'
                then ac.id
                when rt.action_category='REVIEW_REPORT'
                then ac.id
                when rt.action_category='QUALITY_MODULE_PREVENTIVE'
                then ac.id
                when rt.action_category='QUALITY_MODULE_CORRECTIVE'
                then ac.id
                end
                from ACTION_ITEM_CATEGORY ac
                where rt.action_category=ac.key)""")

        addNotNullConstraint(tableName: "REPORT_TASK", columnName: "action_category_id")
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-11") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_TASK', columnName: 'ACTION_CATEGORY')
        }
        dropColumn(columnName: "ACTION_CATEGORY", tableName: "REPORT_TASK")
    }

    changeSet(author: "jitin (generated)", id: "1519888029031-12") {
        addForeignKeyConstraint(baseColumnNames: "action_category_id", baseTableName: "REPORT_TASK", constraintName: "FKE077AB7CB6119D58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM_CATEGORY", referencesUniqueColumn: "false")
    }
}