package com.rxlogix.config

import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['inboundCompliance'])
class QueryCompliance {
    static auditable =  true
    @AuditEntityIdentifier
    String criteriaName
    SuperQuery query
    List<QueryValueList> queryValueLists
    Integer allowedTimeframe = 0

    int index
    boolean dynamicFormEntryDeleted

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static propertiesToUseWhileCopying = ['query', 'criteriaName','allowedTimeframe']

    static transients = ['dynamicFormEntryDeleted']

    static belongsTo = [inboundCompliance: InboundCompliance]
    static hasMany = [queryValueLists: QueryValueList]

    static mapping = {
        table name: "QUERY_COMPLIANCE"
        tablePerHierarchy false
        query column: "SUPER_QUERY_ID", cascade: "none"
        queryValueLists joinTable: [name: "QRS_COMPLIANCE_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "QUERY_COMPLIANCE_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        allowedTimeframe column: "ALLOWED_TIMEFRAME"
        criteriaName column: "CRITERIA_NAME"
        inboundCompliance column: "INBOUND_COMPLIANCE_ID",  cascade: "none"
        index column: "INDX"

    }

    static constraints = {
        query(nullable: false)
        queryValueLists(cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists?.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.QueryCompliance.parameterValues.valueless"
            }
            return hasValues
        })
        allowedTimeframe(nullable: true)
        criteriaName(nullable: false, maxSize: 255)
        dynamicFormEntryDeleted(bindable: true)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)

    }

    InboundCompliance getUsedConfiguration() {
        return GrailsHibernateUtil.unwrapIfProxy(inboundCompliance)
    }

    SuperQuery getUsedQuery() {
//        Added to JavaAssist Proxy Object cast exception http://stackoverflow.com/questions/5622481/removing-proxy-part-of-grails-domain-object
        return GrailsHibernateUtil.unwrapIfProxy(query)
    }

    List<QueryValueList> getUsesQueryValueLists() {
        return queryValueLists
    }

    static namedQueries = {

        usuageByQuery { SuperQuery superQuery ->
            'query' {
                eq('isDeleted', false)
                eq('id', superQuery?.id)
            }
            'inboundCompliance' {
                eq('isDeleted', false)
            }
        }

        queryUsedByInboundConfigurationsCount { SuperQuery query ->
            projections {
                countDistinct 'inboundCompliance'
            }
            usuageByQuery(query)
        }

        queryUsedByInboundConfigurations { SuperQuery query ->
            projections {
                distinct 'inboundCompliance'
            }
            usuageByQuery(query)
        }

    }
    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && oldValues?.keySet()?.contains("queryValueLists")) {
            withNewSession {
                QueryCompliance tq = QueryCompliance.read(id);
                if (oldValues?.keySet()?.contains("queryValueLists")) oldValues.put("queryValueLists", tq.queryValueLists?.toString())
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }


}