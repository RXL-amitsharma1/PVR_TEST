databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "201910071570613045-1") {

        createTable(tableName: "UNIT_CONFIGURATION") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "UNIT_CONFIGURATION_PK")
            }

            column(name: "UNIT_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "UNIT_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ORGANIZATION_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ORGANIZATION_COUNTRY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "UNIT_REGISTERED_ID", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "REGISTERED_WITH", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ADDRESS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "CITY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "STATE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "POSTAL_CODE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "PHONE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "E2B_VALIDATION", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "UNIT_RETIRED", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "201910071570613045-3") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "UNIT_CONFIGURATION", columnName: "ORGANIZATION_TYPE")
        }
        renameColumn(tableName: "UNIT_CONFIGURATION", oldColumnName: "ORGANIZATION_TYPE", newColumnName: "ORG_TYPE_ID")
    }

    changeSet(author: "anurag (generated)", id: "201910071570613045-4") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "UNIT_CONFIGURATION", columnName: "ORGANIZATION_COUNTRY")
        }
        renameColumn(tableName: "UNIT_CONFIGURATION", oldColumnName: "ORGANIZATION_COUNTRY", newColumnName: "ORG_COUNTRY")
    }

    changeSet(author: "anurag (generated)", id: "201910071570613045-5") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "UNIT_CONFIGURATION", columnName: "ADDRESS")
        }
        renameColumn(tableName: "UNIT_CONFIGURATION", oldColumnName: "ADDRESS", newColumnName: "ADDRESS1")
        dropColumn(columnName: "E2B_VALIDATION", tableName: "UNIT_CONFIGURATION")
        dropColumn(columnName: "UNIT_RETIRED", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "ORG_COUNTRY", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "REGISTERED_WITH", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "ADDRESS1", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "CITY", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "STATE", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "PHONE", tableName: "UNIT_CONFIGURATION")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "EMAIL", tableName: "UNIT_CONFIGURATION")
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "E2B_VALIDATION", type: "number(1,0)", defaultValue:0) {
                constraints(nullable: "true")
            }
            column(name: "UNIT_RETIRED", type: "number(1,0)", defaultValue:0) {
                constraints(nullable: "false")
            }
            column(name: "ADDRESS2", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "TITLE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FIRST_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "MIDDLE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "LAST_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "DEPARTMENT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "FAX", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "201910071570613045-6") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "UNIT_CONFIGURATION", columnName: "REGISTERED_WITH")
        }
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "POSTAL_CODE", tableName: "UNIT_CONFIGURATION")
        sql('update UNIT_CONFIGURATION set REGISTERED_WITH = null')
        sql('update UNIT_CONFIGURATION set UNIT_TYPE = UPPER(UNIT_TYPE) where UNIT_TYPE is not null')
    }

    changeSet(author: "anurag (generated)", id: "201910071570613045-7") {
        sql('update UNIT_CONFIGURATION set TITLE = UPPER(TITLE) where TITLE is not null')
    }
}