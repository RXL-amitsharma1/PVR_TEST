package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class ExecutedCustomSQLQuery extends CustomSQLQuery {
    static auditable = false
    Long originalQueryId

    static mapping = {
        table name: "EX_CUSTOM_SQL_QUERY"

        originalQueryId column: "ORIG_QUERY_ID"
    }

    static constraints  = {
        originalQueryId(nullable:false)
    }

    @Override
    public String toString() {
        super.toString()
    }
}
