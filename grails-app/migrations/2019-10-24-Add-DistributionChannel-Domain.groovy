databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "157190288724102019-1") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DISTRIBUTION_CHANNEL")
            }
        }

        createTable(tableName: "DISTRIBUTION_CHANNEL") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DISTRIBUTION_CHANNEL_PK")
            }

            column(name: "OUTGOING_FOLDER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "REPORT_FORMAT", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "INCOMING_FOLDER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "CASE_DOCUMENT", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "LITERATURE_DOCUMENT", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "CASE_ATTACHMENT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "LITERATURE_ATTACHMENT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EMAIL_FROM", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EMAIL_TO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EMAIL_SUBJECT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EMAIL_BODY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_FAX_NO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_FAX_NO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FAX_COMMENT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FOLLOW_UP_DAY_AFTER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "MARK_SUBMITTED_AFTER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "DELIVERY_RECIPIENT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
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
        }
    }

    changeSet(author: "anurag (generated)", id: "157259471831112019-2") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "DISTRIBUTION_CHANNEL", columnName: "MAX_REPORT_PER_MSG")
            }
        }
        dropColumn(columnName: "DELIVERY_RECIPIENT", tableName: "DISTRIBUTION_CHANNEL")
        addColumn(tableName: "DISTRIBUTION_CHANNEL") {
            column(name: "MAX_REPORT_PER_MSG", type: "number(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "DELIVERY_RECIPIENT", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "157259471831112019-3") {
        preConditions(onFail: "MARK_RAN") {
                columnExists(tableName: "DISTRIBUTION_CHANNEL", columnName: "EMAIL_FROM")
        }
        dropColumn(columnName: "EMAIL_FROM", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "EMAIL_TO", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "EMAIL_SUBJECT", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "EMAIL_BODY", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "RECIPIENT_FAX_NO", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "SENDER_FAX_NO", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "FAX_COMMENT", tableName: "DISTRIBUTION_CHANNEL")
        dropColumn(columnName: "FOLLOW_UP_DAY_AFTER", tableName: "DISTRIBUTION_CHANNEL")


        addColumn(tableName: "DISTRIBUTION_CHANNEL") {
            column(name: "EMAIL_TO_USERS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "157259471831112019-4") {
        sql("update DISTRIBUTION_CHANNEL set REPORT_FORMAT = 'EB_XML' where REPORT_FORMAT='E2B XML'")
        sql("update DISTRIBUTION_CHANNEL set REPORT_FORMAT = 'EB_PDF' where REPORT_FORMAT='E2B PDF'")
    }


}