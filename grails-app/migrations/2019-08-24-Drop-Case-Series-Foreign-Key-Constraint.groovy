databaseChangeLog = {
    changeSet(author: "sargam (generated)", id: "201908241031-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKC472BE887F6E20B9')
        }
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE887F6E20B9")
    }
    changeSet(author: "sargam (generated)", id: "201908241031-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKC472BE881441117B')
        }
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE881441117B")
    }
    changeSet(author: "sargam (generated)", id: "201908241031-3") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKC472BE88C502EF3D')
        }
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88C502EF3D")
    }
}
