databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1499690466666-111") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DATE_RANGE_TYPE")
            }
        }

        createTable(tableName: "DATE_RANGE_TYPE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DATE_RANGE_TYPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }

        sql("insert into DATE_RANGE_TYPE(id, version, is_deleted, name) values(1,0,0,'dcdtDatecol1');\n" +
                "insert into DATE_RANGE_TYPE(id, version, is_deleted, name) values(2,0,0,'dcdtDatecol9');\n" +
                "insert into DATE_RANGE_TYPE(id, version, is_deleted, name) values(3,0,0,'dcdtDatecol2');\n" +
                "insert into DATE_RANGE_TYPE(id, version, is_deleted, name) values(4,0,0,'dcdtDatecol3');\n" +
                "insert into DATE_RANGE_TYPE(id, version, is_deleted, name) values(5,0,0,'dcdtDatecol10');\n" +
                "insert into DATE_RANGE_TYPE(id, version, is_deleted, name) values(6,0,1,'CM_INIT_REPT_DATE_J');\n");
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690466666-77") {
        createIndex(indexName: "NAME_uniq_1499690456887", tableName: "DATE_RANGE_TYPE", unique: "true") {
            column(name: "NAME")
        }
    }
}
