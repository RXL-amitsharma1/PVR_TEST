package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.pvdictionary.event.*
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.orm.HibernateCriteriaBuilder
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class MedDraChangeService {
    SqlGenerationService sqlGenerationService
    def CRUDService
    def userService
    def eventDictionaryService

    Map checkUsage(Integer level, String from) {
        List<Configuration> draftConfigurationList = selectAdhoc(from)
        List<PeriodicReportConfiguration> draftPeriodicReportList = selectAggregate(from)
        List<CaseSeries> draftCaseSeriesList = selectCaseSeries(from)
        def queryMap = processQuery(level, from, null).collect { [id: it.id, name: it.name] }
        def templateMap = processTemplate(level, from, null).collect { [id: it.id, name: it.name] }
        def adhocMap = processAdhoc(level, from, null, draftConfigurationList).collect { [id: it.id, name: it.reportName] }
        def aggregateMap = processAggregate(level, from, null, draftPeriodicReportList).collect { [id: it.id, name: it.reportName] }
        def caseSeriesMap = processCaseSeries(level, from, null, draftCaseSeriesList).collect { [id: it.id, name: it.seriesName] }

        return [query: queryMap, template: templateMap, adhoc: adhocMap, aggregate: aggregateMap, caseSeries: caseSeriesMap]
    }

    Map checkAllUsage(def bulk) {
        def result = [query: new HashSet(), template: new HashSet(), adhoc: new HashSet(), aggregate: new HashSet(), caseSeries: new HashSet()]

        bulk.each {
            def up = checkUsage(it.level as Integer, it.from)
            result.query.addAll(up.query)
            result.template.addAll(up.template)
            result.adhoc.addAll(up.adhoc)
            result.aggregate.addAll(up.aggregate)
            result.caseSeries.addAll(up.caseSeries)
        }
        return result
    }

    Map bulkUpdate(def bulk) {
        def result = [query: new HashSet(), template: new HashSet(), adhoc: new HashSet(), aggregate: new HashSet(), caseSeries: new HashSet()]

        bulk.each {
            def up = update(it.level as Integer, it.from, it.to)
            result.query.addAll(up.query)
            result.template.addAll(up.template)
            result.adhoc.addAll(up.adhoc)
            result.aggregate.addAll(up.aggregate)
            result.caseSeries.addAll(up.caseSeries)
        }
        return result
    }

    Map update(Integer level, String from, String to) {
        getEventInstance(level, from) //check that term exists or throw exception

        List<Configuration> draftConfigurationList = selectAdhoc(from)
        List<PeriodicReportConfiguration> draftPeriodicReportList = selectAggregate(from)
        List<CaseSeries> draftCaseSeriesList = selectCaseSeries(from)
        
        draftConfigurationList?.each{
            it.refresh()
        }
        draftPeriodicReportList?.each{
            it.refresh()
        }
        draftCaseSeriesList?.each{
            if(it.globalQueryValueLists)
                it.globalQueryValueLists*.refresh()
        }

        def queryMap = processQuery(level, from, to).collect { [id: it.id, name: it.name] }
        def templateMap = processTemplate(level, from, to).collect { [id: it.id, name: it.name] }
        def adhocMap = processAdhoc(level, from, to, draftConfigurationList).collect { [id: it.id, name: it.reportName] }
        def aggregateMap = processAggregate(level, from, to, draftPeriodicReportList).collect { [id: it.id, name: it.reportName] }
        def caseSeriesMap = processCaseSeries(level, from, to, draftCaseSeriesList).collect { [id: it.id, name: it.seriesName] }

        return [query: queryMap, template: templateMap, adhoc: adhocMap, aggregate: aggregateMap, caseSeries: caseSeriesMap]
    }

    //--- query : we change values only in Query instances, we do not touch custom SQL queries
    Collection processQuery(Integer level, String from, String to) {
        List<SuperQuery> draftQueryList = Query.findAll { eq('isDeleted', false); ilike('JSONQuery', '%' + from + '%'); eq('originalQueryId', 0L) }
        List<SuperQuery> queryList = []
        draftQueryList.each { query ->
            query.JSONQuery = query.JSONQuery.replaceAll(/\{[^\{\}]*["']field["'][^\{\}]*\}/) {
                validateJsonParameter(it, level, from, to, queryList, query)
            }
            if (to) CRUDService.update(query, [validate: false])
        }

        return queryList
    }

    //--- templtes : we change values only in CaseLineListingTemplate instances, we do not touch custom and DT templates
    Collection processTemplate(Integer level, String from, String to) {
        List<ReportTemplate> templateList = []
        List<ReportTemplate> draftTemplateList = CaseLineListingTemplate.createCriteria().list { eq('isDeleted', false); eq('class', CaseLineListingTemplate); ilike('JSONQuery', '%' + from + '%') }

        draftTemplateList.each { template ->
            template.JSONQuery = template.JSONQuery.replaceAll(/\{[^\{\}]*["']field["'][^\{\}]*\}/) {
                validateJsonParameter(it, level, from, to, templateList, template)
            }
            if (to) CRUDService.update(template)
        }
        return templateList
    }

    //-- adhoc : we change values only in eventSelection and in parameters for Query
    Collection processAdhoc(Integer level, String from, String to, draftConfigurationList) {
        Set<Configuration> adhocList = []


        draftConfigurationList.each { adhoc ->
            if (adhoc.eventSelection) {
                def json = JSON.parse(adhoc.eventSelection)
                if (json.getOrDefault(level.toString(), null)?.find { it.name.equalsIgnoreCase(from) }) {
                    if (to) {
                        def toChange = json.get("" + level).find { it.name.equalsIgnoreCase(from) }
                        toChange.name = to
                        toChange.id = getEventInstance(level, from).id
                        adhoc.eventSelection = json.toString()
                        adhoc = CRUDService.update(adhoc)
                    }
                    adhocList << adhoc
                }
            }
            adhoc.templateQueries.each { tq ->
                tq?.queryValueLists?.each { qvl ->
                    validateParameter(qvl, level, from, to, adhocList, adhoc)
                }

            }
        }
        if (to)
            adhocList.each { CRUDService.update(it, [flush:true]) }
        return adhocList
    }

    //-- aggregate : we change values only in parameters for Query
    Collection processAggregate(Integer level, String from, String to, draftPeriodicReportList) {
        Set<PeriodicReportConfiguration> aggregateList = []


        draftPeriodicReportList.each { aggregate ->
            aggregate.templateQueries.each { tq ->
                tq?.queryValueLists?.each { qvl ->
                    validateParameter(qvl, level, from, to, aggregateList, aggregate)
                }
            }
            aggregate.globalQueryValueLists?.each { qvl ->
                validateParameter(qvl, level, from, to, aggregateList, aggregate)
            }
        }
        if (to)
            aggregateList.each { CRUDService.update(it, [flush:true]) }
        return aggregateList
    }

    //-- caseseries : we change values only in parameters for Query
    Collection processCaseSeries(Integer level, String from, String to, draftCaseSeriesList) {
        Set<CaseSeries> caseSeriesList = []
        draftCaseSeriesList.each { caseseries ->
            if (caseseries.eventSelection) {
                def json = JSON.parse(caseseries.eventSelection)
                if (json.getOrDefault(level.toString(), null)?.find { it.name.equalsIgnoreCase(from) }) {
                    if (to) {
                        def toChange = json.get(level.toString()).find { it.name.equalsIgnoreCase(from) }
                        toChange.name = to
                        toChange.id = getEventInstance(level, from).id
                        caseseries.eventSelection = json.toString()
                        caseseries = CRUDService.update(caseseries)
                    }
                    caseSeriesList << caseseries
                }
            }
        }

        draftCaseSeriesList.each { caseSeries ->
            caseSeries.globalQueryValueLists?.each { qvl ->
                validateParameter(qvl, level, from, to, caseSeriesList, caseSeries)
            }
        }
        if (to)
            caseSeriesList.each { CRUDService.update(it, [flush:true]) }
        return caseSeriesList
    }

    private List<CaseSeries> selectCaseSeries(String from) {
        return CaseSeries.createCriteria().list {
            eq('isDeleted', false)
            or{
                ilike('eventSelection', '%' + from + '%')
                createAlias('globalQueryValueLists', 'globalQueryValueLists', CriteriaSpecification.LEFT_JOIN)
                createAlias('globalQueryValueLists.parameterValues', 'globalQueryParameterValues', CriteriaSpecification.LEFT_JOIN)
                ilike('globalQueryParameterValues.value', '%' + from + '%')
            }
        }
    }

    private List<PeriodicReportConfiguration> selectAggregate(String from) {
        return PeriodicReportConfiguration.createCriteria().list {
            eq('isDeleted', false);
            createAlias('templateQueries', 'templateQueries', CriteriaSpecification.LEFT_JOIN)
            createAlias('templateQueries.queryValueLists', 'queryValueLists', CriteriaSpecification.LEFT_JOIN)
            createAlias('queryValueLists.parameterValues', 'queryParameterValues', CriteriaSpecification.LEFT_JOIN)
            createAlias('globalQueryValueLists', 'globalQueryValueLists', CriteriaSpecification.LEFT_JOIN)
            createAlias('globalQueryValueLists.parameterValues', 'globalQueryParameterValues', CriteriaSpecification.LEFT_JOIN)

            or {
                ilike('queryParameterValues.value', '%' + from + '%')
                ilike('globalQueryParameterValues.value', '%' + from + '%')
            }
        }
    }

    private List<Configuration> selectAdhoc(String from) {
        return Configuration.createCriteria().list {
            eq('isDeleted', false);
            createAlias('templateQueries', 'templateQueries', CriteriaSpecification.LEFT_JOIN)
            createAlias('templateQueries.queryValueLists', 'queryValueLists', CriteriaSpecification.LEFT_JOIN)
            createAlias('queryValueLists.parameterValues', 'queryParameterValues', CriteriaSpecification.LEFT_JOIN)

            or {
                ilike('eventSelection', '%' + from + '%')
                ilike('queryParameterValues.value', '%' + from + '%')
            }
        }
    }

    private String validateJsonParameter(String json, Integer level, String from, String to, Collection resultList, def instance) {
        def fieldDescription = JSON.parse(json)
        if (fieldDescription?.value?.split(";")?.find { it.equalsIgnoreCase(from) }) {
            ReportField reportField = ReportField.findByNameAndIsDeleted(fieldDescription?.field, false)
            if (reportField && reportField.dictionaryType == DictionaryTypeEnum.EVENT && reportField.dictionaryLevel == level) {
                if (to) {
                    fieldDescription?.value = fieldDescription.value.split(";").collect { it.trim().equalsIgnoreCase(from) ? to : it }.join(";")
                }
                resultList << instance;
                return fieldDescription.toString()
            }
        }
        return fieldDescription.toString()
    }

    private void validateParameter(QueryValueList qvl, Integer level, String from, String to, Collection resultList, def instance) {
        qvl?.parameterValues?.each { pv ->
            if (pv.hasProperty("reportField")) {//we update only Query parameters (not CustomSQLQuery)
                if (pv.reportField && pv.reportField.dictionaryType == DictionaryTypeEnum.EVENT &&
                        pv.reportField.dictionaryLevel == level && pv.value && pv.value.split(";").find { it.trim().equalsIgnoreCase(from) }) {
                    if (to) {
                        pv.value = pv.value.split(";").collect { it.trim().equalsIgnoreCase(from) ? to : it }.join(";")
                    }
                    resultList << instance;
                }
            }
        }
    }

    @ReadOnly('pva')
    @WithoutTenant
    private def getEventInstance(Integer level, String term) {

        String currentLang = (sqlGenerationService.getPVALanguageId(userService.user?.preference?.locale?.language)) as String
        HibernateCriteriaBuilder criteria
        def searchResult
        switch (level) {
            case 1:
                criteria = LmEventDicView1.'pva'.createCriteria()
                break
            case 2:
                criteria = LmEventDicView2.'pva'.createCriteria()
                break
            case 3:
                criteria = LmEventDicView3.'pva'.createCriteria()
                break
            case 4:
                criteria = LmEventDicView4.'pva'.createCriteria()
                break
            case 5:
                criteria = LmEventDicView5.'pva'.createCriteria()
                break
            case 6:
                criteria = LmEventDicView6.'pva'.createCriteria()
                break
            case 7:
                criteria = LmEventDicView7.'pva'.createCriteria()
                break
            case 8:
                criteria = LmEventDicView8.'pva'.createCriteria()
                break
        }
        if (level < 9)
            searchResult = criteria.list {
                eq("lang", currentLang)
                ilike("name", term)
            }

        if (!searchResult) throw new RuntimeException(ViewHelper.getMessage("controlPanel.medDra.exception.doesNotExist", [term]))
        return [id: (eventDictionaryService.getIdFieldForEvent(searchResult[0]) as String), name: searchResult[0].name]
    }
}
