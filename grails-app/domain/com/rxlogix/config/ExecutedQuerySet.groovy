package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit

/**
 * Executed versions of queries are stored in "queries" variable,
 * inherited from parent QuerySet.
 * Only executed versions of queries should be stored for ExecutedQuerySets.
 * queries cannot contain QuerySet or ExecutedQuerySet.
 */
@CollectionSnapshotAudit
class ExecutedQuerySet extends QuerySet {
    static auditable = false
    Long originalQueryId

    static mapping = {
        table name: "EX_QUERY_SET"

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
