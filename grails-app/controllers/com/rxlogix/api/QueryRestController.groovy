package com.rxlogix.api


import com.rxlogix.LibraryFilter

import com.rxlogix.config.ReportField
import com.rxlogix.config.SuperQuery
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class QueryRestController implements SanitizePaginationAttributes {

    def reportFieldService
    def queryService
    def userService
    def springSecurityService

    static allowedMethods = [list: 'POST']

    def index() {
        list()
    }

    def list() {
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, userService.getUser(),SuperQuery)

        List<Long> idsForUser = SuperQuery.fetchAllIdsBySearchString(filter, params.sort, params.order).list([max: params.max, offset: params.offset]).collect {
            it.first()
        }
        int recordsFilteredCount = SuperQuery.countRecordsBySearchString(filter).get()
        List<SuperQuery> reportTemplateList = idsForUser ? SuperQuery.getAll(idsForUser) : []
        int recordsTotal = SuperQuery.countRecordsBySearchString(new LibraryFilter(userService.getUser())).get()
        render([aaData: reportTemplateList.collect { toMap(it) }, recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    def getQueryList(String term, Integer page, Integer max, Boolean notBlank, Boolean isQueryTargetReports) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        render([items : queryService.getQueryList(term, Math.max(page - 1, 0) * max, max, isQueryTargetReports).findResults  {
            (notBlank && it.hasBlanks) ? null :
                    [id: it.id, text: it.text, hasBlanks: it.hasBlanks, qced: it.qced, isFavorite: it.isFavorite]
        }, total_count: queryService.getQueryListCount(term, isQueryTargetReports)] as JSON)
    }

    def getNonSetQueries(Long oldSelectedValue, String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        Map queryResultdata = queryService.getNonSetQueriesData(term, oldSelectedValue, Math.max(page - 1, 0) * max, max)
        render([items : queryResultdata.list.collect {
            [id: it.id, text: it.nameWithDescription]
        }, total_count: queryResultdata.totalCount] as JSON)
    }

    def getReportingDestinations(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        Map queryResultdata = queryService.getAgenciesNames(term, Math.max(page - 1, 0) * max, max)
        render([items : queryResultdata.list.collect {
            [id: it.name, text: it.name]
        }, total_count: queryResultdata.totalCount] as JSON)
    }

    def getIcsrReportingDestinations(String term, Integer page, Integer max) {
//        if (!max) {
//            max = 30
//        }
//        if (!page) {
//            page = 1
//        }
//        if (term) {
//            term = term?.trim()
//        }
//        Map queryResultdata = queryService.getIcsrCriteriaNames(term, Math.max(page - 1, 0) * max, max)
//        render([items : queryResultdata.list.collect {
//            [id: it.criteriaName, text: it.criteriaName]
//        }, total_count: queryResultdata.totalCount] as JSON)
        forward(action:'getReportingDestinations')
    }

    def getQueryNameDescription(Long id) {
        SuperQuery superQuery = SuperQuery.read(id)
        render([text: superQuery?.nameWithDescription, qced: superQuery?.qualityChecked,  isFavorite: superQuery.isFavorite(userService.currentUser)] as JSON)
    }

    def reportFieldsForQueryValue() {
        ReportField field = ReportField.findByName(params.name)
        if(field)
        render([name       : field.name, dictionary: field.dictionaryType?.toString() ?: '', level: field.dictionaryLevel ?: '', validatable: field.isImportValidatable(), isAutocomplete: field.isAutocomplete, dataType: field.dataType, isText: field.isText,
                description: message(code: "app.reportField." + field.name + ".label.description", default: ''), displayText: message(code: "app.reportField.${field.name}"), isNonCacheSelectable: field.nonCacheSelectable] as JSON)
        render ([] as JSON)

    }

    private Map toMap(SuperQuery q) {
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
        map['queryType'] = ViewHelper.getI18nMessageForString(q.queryType.i18nKey)
        map['checkUsage'] = q.countUsage()
        map['qualityChecked'] = q.qualityChecked
        map['isFavorite'] = q.isFavorite(userService.currentUser)
        return map
    }
}
