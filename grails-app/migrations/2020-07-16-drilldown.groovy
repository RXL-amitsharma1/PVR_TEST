databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "20200617104300-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "DTAB_MEASURE", columnName: "DRILL_TEMPLATE_ID")
            }
        }
        addColumn(tableName: "DTAB_MEASURE") {
            column(name: "DRILL_TEMPLATE_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20200617104300-3") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_RESULT", columnName: "DRILL_DOWN_SOURCE_ID")
            }
        }
        addColumn(tableName: "RPT_RESULT") {
            column(name: "DRILL_DOWN_SOURCE_ID", type: "number(19,0)")
            column(name: "MEASURE", type: "varchar2(255 char)")
            column(name: "TEMPLATE_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20200617104300-4") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_RESULT", columnName: "VIEW_SETTINGS")
            }
        }
        addColumn(tableName: "RPT_RESULT") {
            column(name: "VIEW_SETTINGS", type: "varchar2(4000 char)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20200617104300-5") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_FIELD_INFO", columnName: "DRILL_DOWN_ID")
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "DRILL_DOWN_ID", type: "number(19,0)")
            column(name: "DRILL_DOWN_FIELDS", type: "varchar2(4000 char)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20200617104300-6") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_RESULT", columnName: "FILTER_COLUMNS")
            }
        }
        addColumn(tableName: "RPT_RESULT") {
            column(name: "FILTER_COLUMNS", type: "varchar2(4000 char)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20200617104300-8") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_RESULT", columnName: "COLUMN")
            }
        }
        addColumn(tableName: "RPT_RESULT") {
            column(name: "FIELD_NAME", type: "varchar2(4000 char)")
            column(name: "PARENT_ID", type: "number(19,0)")
        }
    }
}