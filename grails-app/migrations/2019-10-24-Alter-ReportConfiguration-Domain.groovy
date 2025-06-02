databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "157190913824102019-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RCONFIG", columnName: "RECIPIENT_ORG_ID")
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "RECIPIENT_ORG_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_COUNTRY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECEIVER_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_PARTNER_REG_WITH", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "COMPARATOR_REPORTING", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "AUTO_TRANSMIT", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "DAY_EARLIER_HOLIDAY", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_TITLE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_FIRST_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_MIDDLE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_LAST_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_ORG_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_DEPT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_COUNTRY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_PARTNER_REG_WITH", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_VALIDATION", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FROM_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "ADDRESS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "CITY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "STATE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "POSTAL_CODE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "PHONE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EMAIL", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FAX", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FDA_SENDER_ADDRESS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EB_DISTRIBUTION_CHANNEL_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

        }
    }


    changeSet(author: "anurag (generated)", id: "157190913824102019-2") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "RECIPIENT_ORG_NAME")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_ORG_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_TYPE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_COUNTRY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECEIVER_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "RECIPIENT_PARTNER_REG_WITH", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "COMPARATOR_REPORTING", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "AUTO_TRANSMIT", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "DAY_EARLIER_HOLIDAY", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_TITLE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_FIRST_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_MIDDLE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_LAST_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_ORG_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_TYPE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_DEPT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_COUNTRY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_PARTNER_REG_WITH", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_VALIDATION", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FROM_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "ADDRESS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "CITY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "STATE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "POSTAL_CODE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "PHONE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EMAIL", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FAX", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "FDA_SENDER_ADDRESS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "EX_EB_DISTRIBUTION_CHANNEL_ID", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

        }
    }

    changeSet(author: "sachinverma (generated)", id: "157190913824102019-3") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "EX_RCONFIG", columnName: "ADDRESS")
        }
        renameColumn(tableName: "EX_RCONFIG", oldColumnName: "ADDRESS", newColumnName: "ADDRESS1")
        renameColumn(tableName: "EX_RCONFIG", oldColumnName: "FDA_SENDER_ADDRESS", newColumnName: "ADDRESS2")

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ASSIGNED_GROUP_TO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "157190913824102019-4") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "RCONFIG", columnName: "ADDRESS")
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ASSIGNED_GROUP_TO", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        renameColumn(tableName: "RCONFIG", oldColumnName: "ADDRESS", newColumnName: "ADDRESS1")
        renameColumn(tableName: "RCONFIG", oldColumnName: "FDA_SENDER_ADDRESS", newColumnName: "ADDRESS2")
        dropColumn(columnName: "RECIPIENT_TYPE_ID", tableName: "RCONFIG")
        dropColumn(columnName: "RECIPIENT_COUNTRY", tableName: "RCONFIG")
        dropColumn(columnName: "RECEIVER_ID", tableName: "RCONFIG")
        dropColumn(columnName: "RECIPIENT_PARTNER_REG_WITH", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_TITLE", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_FIRST_NAME", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_MIDDLE_NAME", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_LAST_NAME", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_TYPE_ID", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_DEPT", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_COUNTRY", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_ID", tableName: "RCONFIG")
        dropColumn(columnName: "SENDER_PARTNER_REG_WITH", tableName: "RCONFIG")
        dropColumn(columnName: "CITY", tableName: "RCONFIG")
        dropColumn(columnName: "STATE", tableName: "RCONFIG")
        dropColumn(columnName: "POSTAL_CODE", tableName: "RCONFIG")
        dropColumn(columnName: "PHONE", tableName: "RCONFIG")
        dropColumn(columnName: "EMAIL", tableName: "RCONFIG")
        dropColumn(columnName: "FAX", tableName: "RCONFIG")

    }

    changeSet(author: "sachinverma (generated)", id: "157190913824102019-5") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "RCONFIG", columnName: "ASSIGNED_TO")
        }
        dropColumn(columnName: "ASSIGNED_TO", tableName: "RCONFIG")
        dropColumn(columnName: "ASSIGNED_GROUP_TO", tableName: "RCONFIG")
        addColumn(tableName: "RCONFIG") {
            column(name: "ASSIGNED_TO", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ASSIGNED_GROUP_TO", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "157190913824102019-6") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "EX_RCONFIG", columnName: "ASSIGNED_TO")
        }
        dropColumn(columnName: "ASSIGNED_TO", tableName: "EX_RCONFIG")
        dropColumn(columnName: "ASSIGNED_GROUP_TO", tableName: "EX_RCONFIG")
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ASSIGNED_TO", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ASSIGNED_GROUP_TO", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "157190913824102019-7") {
        sql('update EX_RCONFIG set SENDER_TITLE = UPPER(SENDER_TITLE) where SENDER_TITLE is not null')
    }

}
