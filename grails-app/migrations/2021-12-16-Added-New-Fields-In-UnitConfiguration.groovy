databaseChangeLog = {
    
    changeSet(author: "Nitin Nepalia", id: "202112161842-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'ORGANIZATION_NAME')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "ORGANIZATION_NAME", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia", id: "202112290958-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SENDER_COMPANY_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SENDER_COMPANY_NAME", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia", id: "202112290958-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECEIVER_COMPANY_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECEIVER_COMPANY_NAME", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202112290958-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALLOWED_ATTACHMENTS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALLOWED_ATTACHMENTS", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202112290958-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_TITLE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_TITLE", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202112290958-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_FIRST_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_FIRST_NAME", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202112290958-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_MIDDLE_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_MIDDLE_NAME", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202112290958-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_LAST_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_LAST_NAME", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia (generated)", id: "202112290958-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_DEPT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_DEPT", type: "varchar2(255 char)", defaultValue:"") {
                constraints(nullable: "true")
            }
        }
    }
}
