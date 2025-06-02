package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * queries should contain non-executed Queries and CustomSQLQueries only.
 * queries cannot contain QuerySet or ExecutedQuerySet.
 */
@CollectionSnapshotAudit
class QuerySet extends SuperQuery {
    static auditable = true
    List<SuperQuery> queries

    static hasMany = [queries: SuperQuery]

    static namedQueries = {
        usuageByQuery { SuperQuery query ->
            eq('isDeleted', false)
            'queries' {
                eq('id', query?.id)
            }
        }

        countUsuageByQuery { SuperQuery query ->
            projections {
                countDistinct('id')
            }
            usuageByQuery(query)
        }
    }

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false

        table name: "QUERY_SET"
        queries joinTable: [name:"QUERY_SETS_SUPER_QRS", column: "SUPER_QUERY_ID", key: "QUERY_SET_ID"],
                indexColumn: [name: "SUPER_QUERY_IDX"], fetch: 'join', cascade: 'none'
    }

    static constraints = {
        queries(nullable: false)
        JSONQuery(blank: false) //business validation within preValidateQuery method of QueryCOntroller
    }

    @Override
    transient List<String> getFieldsToValidate() {
        return this.getClass().getSuperclass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] } + this.getClass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] }
    }


    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            newValues.put("JSONQuery", queryService.buildSetSQLFromJSON(JSONQuery))
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("JSONQuery")) {
            newValues.put("JSONQuery", queryService.buildSetSQLFromJSON(JSONQuery))
            oldValues.put("JSONQuery", queryService.buildSetSQLFromJSON(this.getPersistentValue("JSONQuery")))
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    Integer getParameterSize() {
        int size = 0
        queries.each {  query ->
            size += GrailsHibernateUtil.unwrapIfProxy(query).getParameterSize()
        }
        return size
    }

    @Override
    public String toString() {
        super.toString()
    }
}
