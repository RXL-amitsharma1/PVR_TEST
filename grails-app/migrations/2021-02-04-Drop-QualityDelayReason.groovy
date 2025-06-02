databaseChangeLog = {

    changeSet(author: "Sachin Sharma", id: "202102041001-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'QUALITY_CASE_DELAY_REASONS', foreignKeyName: 'FK4F45B41C6458487')
        }
        dropForeignKeyConstraint(baseTableName: "QUALITY_CASE_DELAY_REASONS", constraintName: "FK4F45B41C6458487")
    }

    changeSet(author: "Sachin Sharma", id: "202102041002-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'QUALITY_CASE_DELAY_REASONS', foreignKeyName: 'FK4G45A41C6458465')
        }
        dropForeignKeyConstraint(baseTableName: "QUALITY_CASE_DELAY_REASONS", constraintName: "FK4G45A41C6458465")
    }

    changeSet(author: "Sachin Sharma", id: "202102041003-3") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            tableExists(tableName: "QUALITY_CASE_DELAY_REASONS")
        }
        dropTable(tableName: "QUALITY_CASE_DELAY_REASONS")
    }

    changeSet(author: "Sachin Sharma", id: "202102042001-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'QUALITY_SUB_DELAY_REASONS', foreignKeyName: 'FK4H45E41C6458475')
        }
        dropForeignKeyConstraint(baseTableName: "QUALITY_SUB_DELAY_REASONS", constraintName: "FK4H45E41C6458475")
    }

    changeSet(author: "Sachin Sharma", id: "202102042002-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'QUALITY_SUB_DELAY_REASONS', foreignKeyName: 'FK4G65A715F07FED5')
        }
        dropForeignKeyConstraint(baseTableName: "QUALITY_SUB_DELAY_REASONS", constraintName: "FK4G65A715F07FED5")
    }

    changeSet(author: "Sachin Sharma", id: "202102042003-3") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            tableExists(tableName: "QUALITY_SUB_DELAY_REASONS")
        }
        dropTable(tableName: "QUALITY_SUB_DELAY_REASONS")
    }

    changeSet(author: "Sachin Sharma", id: "202102043001-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'QUALITY_SAMPL_DELAY_REASONS', foreignKeyName: 'FK4J65A985F07WEG5')
        }
        dropForeignKeyConstraint(baseTableName: "QUALITY_SAMPL_DELAY_REASONS", constraintName: "FK4J65A985F07WEG5")
    }

    changeSet(author: "Sachin Sharma", id: "202102043001-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'QUALITY_SAMPL_DELAY_REASONS', foreignKeyName: 'FK4U65A995FQ7FEH5')
        }
        dropForeignKeyConstraint(baseTableName: "QUALITY_SAMPL_DELAY_REASONS", constraintName: "FK4U65A995FQ7FEH5")
    }

    changeSet(author: "Sachin Sharma", id: "202102043003-3") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            tableExists(tableName: "QUALITY_SAMPL_DELAY_REASONS")
        }
        dropTable(tableName: "QUALITY_SAMPL_DELAY_REASONS")
    }

    changeSet(author: "Sachin Sharma", id: "202102044001-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            tableExists(tableName: "QUALITY_DELAY_REASON")
        }
        dropTable(tableName: "QUALITY_DELAY_REASON")
    }
}