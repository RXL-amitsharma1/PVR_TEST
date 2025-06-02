databaseChangeLog = {

    changeSet(author: "Shubham Sharma", id: "202203110250-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_TYPE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_TYPE", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_ADDRESS1')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_ADDRESS1", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_ADDRESS2')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_ADDRESS2", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_STATE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_STATE", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_POST_CODE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_POST_CODE", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_CITY')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_CITY", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_PHONE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_PHONE", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_FAX')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_FAX", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shubham Sharma", id: "202203110250-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'RECIPIENT_EMAIL')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_EMAIL", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }
}