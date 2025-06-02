databaseChangeLog = {
    changeSet(author: "shikhar", id: "20200206145500") {
        createTable(tableName: "SRC_PROFILE_DATE_RANGE_MAP") {
            column(name: "SRC_PROFILE_ID", type: "number(19,0)")
            column(name: "DATE_RANGE_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "shikhar", id: "20200205145600") {
        addPrimaryKey(columnNames: "SRC_PROFILE_ID, DATE_RANGE_ID", constraintName: "SRCPROFILEDATERANGEMAP_PK", tableName: "SRC_PROFILE_DATE_RANGE_MAP")
    }

    changeSet(author: "sachin", id: "20200209145601") {
        addColumn(tableName: "SOURCE_PROFILE") {
            column(name: "INCLU_LATEST_VER_ONLY", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}