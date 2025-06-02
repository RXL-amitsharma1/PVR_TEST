package com.rxlogix

import com.rxlogix.config.CaseLineListingTemplate
import com.rxlogix.config.Configuration
import com.rxlogix.config.Dashboard
import com.rxlogix.config.DataTabulationTemplate
import com.rxlogix.config.IcsrReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper

class QueryTemplateJSONService {

    static transactional = false

    QueryService queryService
    TemplateService templateService
    def importService
    def grailsApplication
    def configurationService

    List<Map> fetchAllQueries() {
        List<Map> queryList = SuperQuery.findAllByIsDeletedAndOriginalQueryId(false, 0L).collect {
            fetchMapFromObject(it)
        }
        return queryList
    }

    List<Map> fetchAllTemplates() {
        List<Map> templateList = ReportTemplate.findAllByIsDeletedAndOriginalTemplateId(false, 0L).collect {
            fetchMapFromObject(it)
        }
        return templateList
    }

    List<Map> fetchAllDashboards() {
        List<Map> templateList = Dashboard.findAllByIsDeleted(false).collect { Dashboard it ->
            return [
                    'id'            : it.id,
                    'name'          : it.label,
                    'qualityChecked': "-",
                    'owner'         : it.owner,
                    'dateCreated'   : it.dateCreated,
                    'lastUpdated'   : it.lastUpdated,
                    'type'          : ViewHelper.getI18nMessageForString(it.dashboardType.i18nKey)
            ]

        }
        return templateList
    }

    List<Map> fetchAllConfigurations() {
        List<Map> templateList = ReportConfiguration.findAllByIsDeleted(false).collect {
            String type
            if (it instanceof Configuration) {
                type = "app.configurationType.ADHOC_REPORT"
            } else if (it instanceof PeriodicReportConfiguration) {
                type = "app.configurationType.PERIODIC_REPORT"
            } else if (it instanceof IcsrReportConfiguration) {
                type = "app.configurationType.ICSR_REPORT"
            } else {
                type = "app.label.new.icsr.profile"
            }
            return [
                    'id'            : it.id,
                    'name'          : it.reportName,
                    'qualityChecked': it.qualityChecked,
                    'owner'         : it.owner,
                    'dateCreated'   : it.dateCreated,
                    'lastUpdated'   : it.lastUpdated,
                    'type'          : ViewHelper.getI18nMessageForString(type)

            ]
        }
        return templateList
    }

    List fetchQueriesJSON(String id) {
        List jsons = []
        def list = MiscUtil.parseJsonText(id)
        list.each {
            SuperQuery superQuery = SuperQuery.get(it.id)
            if (superQuery) {
                jsons.add(queryService.getQueryAsJSON(superQuery).toString(true))
            }
        }
        return jsons
    }

    List fetchTemplatesJSON(String id) {
        List jsons = []
        def list = MiscUtil.parseJsonText(id)
        List<ReportTemplate> templateList = list.collect {ReportTemplate.get(it.id)}?.findAll{it}?.sort{ReportTemplate template->
            if(template instanceof CaseLineListingTemplate){
                return (template.getAllSelectedFieldsInfo().find{it.drillDownTemplate}?1:0)
            }
            if(template instanceof DataTabulationTemplate){
                return (template.getAllSelectedFieldsInfo().find{it.drillDownTemplate}?1:0)
            }
            return 0
        }
        templateList?.each {ReportTemplate reportTemplate ->
                jsons.add(templateService.getTemplateAsJSON(reportTemplate).toString(true))
        }
        return jsons
    }

    List fetchConfigurationJSON(String id) {
        List jsons = []
        def list = MiscUtil.parseJsonText(id)
        list.each {
            ReportConfiguration reportConfiguration = ReportConfiguration.get(it.id)
            if (reportConfiguration) {
                jsons.add(configurationService.getConfigurationAsJSON(reportConfiguration).toString(true))
            }
        }
        return jsons
    }

    List fetchDashboardJSON(String id) {
        List jsons = []
        def list = MiscUtil.parseJsonText(id)
        list.each {
            Dashboard dashboard = Dashboard.get(it.id)
            if (dashboard) {
                jsons.add(importService.getDashboardAsJSON(dashboard).toString(true))
            }
        }
        return jsons
    }

    File writeJsonToFile(List jsons, String fileName) {
        File file = new File(grailsApplication.config.tempDirectory + '/' + fileName + "_" + new Date().getTime())
        String jsonString = String.join(", \n", jsons)
        file.write(jsonString)
        return file
    }

    Map fetchMapFromObject(object) {
        Map map = [:]
        map['id'] = object.id
        map['name'] = object.name
        map['qualityChecked'] = object.qualityChecked
        map['owner'] = object.owner
        map['dateCreated'] = object.dateCreated
        map['lastUpdated'] = object.lastUpdated
        if (object instanceof SuperQuery)
            map['type'] = ViewHelper.getI18nMessageForString(object.queryType.i18nKey)
        else if (object instanceof ReportTemplate)
            map['type'] = ViewHelper.getI18nMessageForString(object.templateType.i18nKey)
        return map
    }

}
