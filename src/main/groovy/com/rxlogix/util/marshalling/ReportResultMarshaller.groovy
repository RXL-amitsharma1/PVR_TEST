package com.rxlogix.util.marshalling
import com.rxlogix.config.ReportResult
import com.rxlogix.util.ViewHelper
import grails.converters.JSON

class ReportResultMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ReportResult) {
            ReportResult result ->
            def map = [:]
            map['id'] = result.id
            map['reportName'] = result.executedTemplateQuery.executedConfiguration.reportName
            map['description'] = result.executedTemplateQuery.executedConfiguration.description
            map['owner'] = result.executedTemplateQuery.executedConfiguration.owner.fullName
            map['dateCreated'] = result.dateCreated
            map['lastUpdated'] = result.lastUpdated
            map['tags'] = ViewHelper.getCommaSeperatedFromList(result.executedTemplateQuery.executedConfiguration.tags)
            map['version'] = result.sequenceNo
            map['executionStatus'] = result.executionStatus.value()
//            map['status'] = result.getStatusForUser()?.getStatus()?.value()
            map['frequency'] = result.executedTemplateQuery.executedConfiguration.getFrequency()
            map['runDate'] = result.runDate
            map['executionTime'] = result.totalTime
                map['configId'] = result.executedTemplateQuery.executedConfiguration.id
            return map
        }
    }
}
