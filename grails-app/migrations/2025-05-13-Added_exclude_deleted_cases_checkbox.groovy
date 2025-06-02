databaseChangeLog = {
    changeSet(author: "Gunjan", id: "202404091550-001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")
        }
        sql("update RCONFIG set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091550-002") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")
        }
        sql("update EX_RCONFIG set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091557-003") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_SERIES', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "CASE_SERIES") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")
        }
        sql("update CASE_SERIES set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "CASE_SERIES", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091550-004") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")
        }
        sql("update EX_CASE_SERIES set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "EX_CASE_SERIES", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091550-005") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'INBOUND_COMPLIANCE', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "INBOUND_COMPLIANCE") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")
        }
        sql("update INBOUND_COMPLIANCE set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "INBOUND_COMPLIANCE", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091550-006") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_INBOUND_COMPLIANCE', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")
        }
        sql("update EX_INBOUND_COMPLIANCE set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "EX_INBOUND_COMPLIANCE", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091550-007") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'EXCLUDE_DELETED_CASES')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "EXCLUDE_DELETED_CASES", type: "number(1,0)")

        }
        sql("update REPORT_REQUEST set EXCLUDE_DELETED_CASES = CASE WHEN EXCLUDE_NON_VALID_CASES = 1 THEN 1 ELSE 0 END;")
        addNotNullConstraint(tableName: "REPORT_REQUEST", columnName: "EXCLUDE_DELETED_CASES")
    }

    changeSet(author: "Gunjan", id: "202404091550-008") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SUPER_QUERY', columnName: 'DELETED_CASES')
            }
        }
        addColumn(tableName: "SUPER_QUERY") {
            column(name: "DELETED_CASES", type: "number(1,0)") {
            }
        }
        grailsChange {
            change {
                sql.execute("UPDATE SUPER_QUERY SET DELETED_CASES = (CASE WHEN NAME = 'Deleted Cases' THEN '1' ELSE '0' END )")
                confirm "Successfully set default value for SUPER_QUERY DELETED_CASES."
            }
        }
        addNotNullConstraint(tableName: "SUPER_QUERY", columnName: "DELETED_CASES", columnDataType: "number(1,0)")
    }
}