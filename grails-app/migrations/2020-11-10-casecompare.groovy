databaseChangeLog = {
    changeSet(author: "sergey", id: "2020111014571-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'value')
            }
        }

        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "mandatory_type", type: "varchar2(255 char)") { constraints(nullable: "true") }
            column(name: "field_name", type: "varchar2(255 char)") { constraints(nullable: "true") }
            column(name: "value", type: "varchar2(4000 char)") { constraints(nullable: "true") }
            column(name: "field_location", type: "varchar2(255 char)") { constraints(nullable: "true") }
        }
    }

    changeSet(author: "sergey", id: "2020111014571-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'value')
            }
        }

        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "mandatory_type", type: "varchar2(255 char)") { constraints(nullable: "true") }
            column(name: "field_name", type: "varchar2(255 char)") { constraints(nullable: "true") }
            column(name: "value", type: "varchar2(4000 char)") { constraints(nullable: "true") }
            column(name: "field_location", type: "varchar2(255 char)") { constraints(nullable: "true") }
        }
    }

    changeSet(author: "sergey", id: "2020111014571-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'value')
            }
        }

        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "mandatory_type", type: "varchar2(255 char)") { constraints(nullable: "true") }
            column(name: "field_name", type: "varchar2(255 char)") { constraints(nullable: "true") }
            column(name: "value", type: "varchar2(4000 char)") { constraints(nullable: "true") }
            column(name: "field_location", type: "varchar2(255 char)") { constraints(nullable: "true") }
        }
    }
}