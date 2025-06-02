databaseChangeLog = {

//refer https://community.oracle.com/thread/413705
// Change DataType from varchar2 to CLOB

    changeSet(author: "prashantsahi (generated)", id: "1475955487919-13") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "EVENT_SELECTION_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update REPORT_REQUEST set EVENT_SELECTION_COPY = EVENT_SELECTION;")

        dropColumn(tableName: "REPORT_REQUEST", columnName: "EVENT_SELECTION")

        renameColumn(tableName: "REPORT_REQUEST", oldColumnName: "EVENT_SELECTION_COPY", newColumnName: "EVENT_SELECTION")
    }

    changeSet(author: "prashantsahi (generated)", id: "1475955487919-14") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "PRODUCT_SELECTION_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update REPORT_REQUEST set PRODUCT_SELECTION_COPY = PRODUCT_SELECTION;")

        dropColumn(tableName: "REPORT_REQUEST", columnName: "PRODUCT_SELECTION")

        renameColumn(tableName: "REPORT_REQUEST", oldColumnName: "PRODUCT_SELECTION_COPY", newColumnName: "PRODUCT_SELECTION")
    }

    changeSet(author: "prashantsahi (generated)", id: "1475955487919-15") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "STUDY_SELECTION_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update REPORT_REQUEST set STUDY_SELECTION_COPY = STUDY_SELECTION;")

        dropColumn(tableName: "REPORT_REQUEST", columnName: "STUDY_SELECTION")

        renameColumn(tableName: "REPORT_REQUEST", oldColumnName: "STUDY_SELECTION_COPY", newColumnName: "STUDY_SELECTION")
    }

}
