package com.rxlogix.util.marshalling

import com.rxlogix.config.SuperQuery
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON

class QueryMarshaller {
    void register() {
        JSON.registerObjectMarshaller(SuperQuery) { SuperQuery q ->
            def map = [:]
            map['id'] = q.id
            map['name'] = q.name
            map['description'] = q.description
            map['createdBy'] = q.createdBy
            map['modifiedBy'] = q.modifiedBy
            map['owner'] = q.owner
            map['dateCreated'] = q.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT)
            map['lastUpdated'] = q.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT)
            map['lastExecuted'] = q.lastExecuted?.format(DateUtil.DATEPICKER_UTC_FORMAT)
            map['isDeleted'] = q.isDeleted
            map['tags'] = ViewHelper.getCommaSeperatedFromList(q.tags)
            map['type'] = ViewHelper.getI18nMessageForString(q.queryType.i18nKey)
            map['checkUsage'] = q.countUsage()
            map['qualityChecked'] = q.qualityChecked
            return map
        }
    }
}
