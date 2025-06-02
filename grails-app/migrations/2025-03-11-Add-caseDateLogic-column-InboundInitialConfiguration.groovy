databaseChangeLog = {
    changeSet(author: "gunjan", id: "202503111225") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'INBOUND_INITIAL_CONF', columnName: 'CASE_DATE_LOGIC')
            }
        }
        addColumn(tableName: "INBOUND_INITIAL_CONF") {
            column(name: "CASE_DATE_LOGIC", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gunjan", id: "202503111225_1") {
        sql("ALTER TABLE INBOUND_INITIAL_CONF MODIFY CASE_DATE_LOGIC DEFAULT 1")
        sql("UPDATE INBOUND_INITIAL_CONF SET CASE_DATE_LOGIC = 1 WHERE CASE_DATE_LOGIC = 0")
    }

}