package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['configuration', 'periodicReportConfiguration'])
class PublisherSource extends BasicPublisherSource {
    static auditable =  [ignore:['data']]
    static belongsTo = [configuration: ReportConfiguration]
    static mapping = {
        tablePerHierarchy false
        table name: "CONFIGURATION_ATTACH"
    }

    String getInstanceIdentifierForAuditLog() {
        return "${name}"
    }

    public String toString() {
        return name
    }

    Map toMap() {
        Map result = super.toMap()
        result.configuration = this.configuration?.reportName
        return result
    }
}
