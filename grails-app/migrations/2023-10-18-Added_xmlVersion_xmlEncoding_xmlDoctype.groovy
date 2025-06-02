databaseChangeLog = {
    changeSet(author: "VivekKumar (generated)", id: "202310181902-1") {
        dropColumn(columnName: "MESSAGE_HEADER", tableName: "UNIT_CONFIGURATION")
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181904-1") {
        dropColumn(columnName: "MESSAGE_HEADER", tableName: "EX_RCONFIG")
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181906-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'XML_VERSION')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "XML_VERSION", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181908-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'XML_ENCODING')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "XML_ENCODING", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181910-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'XML_DOCTYPE')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "XML_DOCTYPE", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181912-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'XML_VERSION')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "XML_VERSION", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181914-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'XML_ENCODING')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "XML_ENCODING", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "VivekKumar (generated)", id: "202310181916-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'XML_DOCTYPE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "XML_DOCTYPE", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }

}
