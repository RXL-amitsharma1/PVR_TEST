package com.rxlogix.util.marshalling

import com.rxlogix.config.DataTabulationTemplate
import com.rxlogix.config.NonCaseSQLTemplate
import com.rxlogix.config.ReportTemplate
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

class ReportTemplateMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ReportTemplate) { ReportTemplate template ->
            DataTabulationTemplate dataTabulationTemplate = null
            NonCaseSQLTemplate nonCaseSQLTemplate = null
            switch (template.class) {
                case DataTabulationTemplate : dataTabulationTemplate = (DataTabulationTemplate) GrailsHibernateUtil.unwrapIfProxy(template)
                    break
                case NonCaseSQLTemplate : nonCaseSQLTemplate = (NonCaseSQLTemplate) GrailsHibernateUtil.unwrapIfProxy(template)
                    break
            }
            def map= [:]
            map['id'] = template.id
            map['category'] = template.category?.name?:" ";
            map['name'] = template.name
            map['description'] = template.description
            map['dateCreated'] = template.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT)
//            map['selectedFieldsColumns'] = template.selectedFieldsColumns
            map['lastUpdated'] = template.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT)
            map['lastExecuted'] = template.lastExecuted?.format(DateUtil.DATEPICKER_UTC_FORMAT)
            map['tags'] = ViewHelper.getCommaSeperatedFromList(template.tags)
            map['owner'] = template.owner
            map['createdBy'] = template.createdBy
            map['isDeleted'] = template.isDeleted
            map['checkUsage'] = template.countUsage()
            map['qualityChecked'] = template.qualityChecked
            map['type'] = ViewHelper.getI18nMessageForString(template.templateType.i18nKey)
            if (dataTabulationTemplate) {
                map['chartCustomOptions'] = dataTabulationTemplate.chartCustomOptions
            } else if (nonCaseSQLTemplate) {
                map['chartCustomOptions'] = nonCaseSQLTemplate.chartCustomOptions
            } else {
                map['chartCustomOptions'] = null
            }
            map['useFixedTemplate'] = template.useFixedTemplate
            map['maxChartPoints'] = template.maxChartPoints
            if (template.fixedTemplate?.data) {
                map['fixedTemplate'] = [
                        name: template.fixedTemplate.name,
                        data: Base64.encoder.encodeToString(template.fixedTemplate?.data)
                ]
            }
            return map
        }
    }
}
