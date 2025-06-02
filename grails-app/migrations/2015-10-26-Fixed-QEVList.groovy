databaseChangeLog = {

    changeSet(author: "Sherry (generated)", id: "1445970996835-1") {
        addColumn(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
            column(name: "EX_QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-2") {
        addColumn(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
            column(name: "EX_TEMPLT_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-3") {
        addColumn(tableName: "QUERIES_QRS_EXP_VALUES") {
            column(name: "query_expression_values_idx", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-4") {
        addColumn(tableName: "TEMPLT_QRS_QUERY_VALUES") {
            column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-5") {
        addColumn(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
            column(name: "TEMPLT_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-6") {
        addColumn(tableName: "VALUES_PARAMS") {
            column(name: "PARAM_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-11") {
        dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK3D9BA7612F961DEA")
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-12") {
        dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK8603209D2F961DEA")
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-13") {
        dropForeignKeyConstraint(baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK6E9963CA7D28A09C")
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-14") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKB366EAE98C474AC5")
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-15") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKC9A04E158C474AC5")
    }

    changeSet(author: "Sherry (generated)", id: "1445970996835-16") {
        dropForeignKeyConstraint(baseTableName: "VALUES_PARAMS", constraintName: "FK36882F431DA5B73A")
    }
}
