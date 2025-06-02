package com.rxlogix.config

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

class ExecutedQueryCompliance {

    String criteriaName
    SuperQuery executedQuery
    List<QueryValueList> executedQueryValueLists
    Integer allowedTimeframe = 0
    ReportResult finalReportResult

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static belongsTo = [executedInboundCompliance: ExecutedInboundCompliance]

    static hasMany = [executedQueryValueLists: ExecutedQueryValueList]

//    static transients = ['reportResult']


    static mapping = {
        table name: "EX_QUERY_COMPLIANCE"
        tablePerHierarchy false
        criteriaName column: "CRITERIA_NAME"
        executedQuery column: "EX_QUERY_ID", cascade: "none"
        executedQueryValueLists joinTable: [name: "EX_QRS_COMPLIANCE_QUERY_VALUES", column: "EX_QUERY_VALUE_ID", key: "EX_QUERY_COMPLIANCE_ID"], indexColumn: [name: "EX_QUERY_VALUE_IDX"]
        allowedTimeframe column: "ALLOWED_TIMEFRAME"
        finalReportResult column: "REPORT_RESULT_ID", cascade: 'all'
        executedInboundCompliance column: "EX_INBOUND_COMPLIANCE_ID", cascade: "none"
    }

    static constraints = {
        finalReportResult(nullable: true)
        executedQuery(nullable: false)
        executedQueryValueLists(nullable: true)
    }

    transient BaseInboundCompliance getUsedConfiguration() {
        return GrailsHibernateUtil.unwrapIfProxy(executedInboundCompliance)
    }

    SuperQuery getUsedQuery() {
        return GrailsHibernateUtil.unwrapIfProxy(executedQuery)
    }

    List<QueryValueList> getUsesQueryValueLists() {
        return executedQueryValueLists
    }

    public String toString() {
        return "$executedQuery.name"
    }

}
