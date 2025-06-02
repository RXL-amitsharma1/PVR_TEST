package com.rxlogix.util.marshalling

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.util.ViewHelper
import grails.converters.JSON

class ExecutedConfigurationMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ExecutedConfiguration
        ) { ExecutedConfiguration q ->
            def map = [:]
            map['id'] = q.id
            map['reportName'] = q.reportName
            map['description'] = q.description
            map['owner'] = q.owner.fullName
            map['dateCreated'] = q.dateCreated
            map['isDeleted'] = q.isDeleted
            map['tags'] = ViewHelper.getCommaSeperatedFromList(q.tags)
            map['templateQueries'] = q.executedTemplateQueries
            map['version'] = q.numOfExecutions
            return map
        }
    }
}
