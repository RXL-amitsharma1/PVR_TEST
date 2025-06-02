package com.rxlogix.util.marshalling

import com.rxlogix.config.ReportConfiguration
import com.rxlogix.util.ViewHelper
import grails.converters.JSON

class ConfigurationMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ReportConfiguration
        ) { ReportConfiguration q ->
            def map = [:]
            map['id'] = q.id
            map['reportName'] = q.reportName
            map['description'] = q.description
            map['owner'] = [id: q.owner.id, username: q.owner.username, fullname: q.owner.fullName]
            map['createdBy'] = q.owner.fullName
            map['dateCreated'] = q.dateCreated
            map['lastUpdated'] = q.lastUpdated
            map['isDeleted'] = q.isDeleted
            map['tags'] = ViewHelper.getCommaSeperatedFromList(q.tags)
            map['templateQueries'] = q.templateQueries
            map['noOfExecution'] = q.numOfExecutions
            map['configId'] = q.id
            return map
        }
    }
}
