package com.rxlogix

import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class LibraryFilter {
    //common
    String search
    User user
    Boolean favoriteSort = false
    Boolean forPvq = false
    Map sharedWith = null
    List<Closure> advancedFilterCriteria
    Map<String,String> manualAdvancedFilter = null

    //executed
    Boolean includeArchived = false

    //configuration
    List<Class> configurationClasses
    List<TemplateTypeEnum> templateTypes
    Boolean showChartSheet = false
    ReportSubmissionStatusEnum submission
    Boolean forPublisher
    Boolean allReportsForPublisher
    String periodicReportType
    Boolean icsrWidgets = false

    //reportRequest
    Boolean aggregateOnly = false


    LibraryFilter(GrailsParameterMap params, User user, Class clazz, configurationClasses = null) {

        forPublisher = ViewHelper.isPvPModule(params.getRequest())
        allReportsForPublisher = params.allReportsForPublisher == "true"
        search = params.searchString?.trim()
        this.user = user
        includeArchived = (params.includeArchived == "true")
        if (params.sharedwith instanceof String) {
            sharedWith = ParamsUtils.parseSharedWithParam([params.sharedwith], user?.id)
        }else {
            List<String> sharedWithList = params.list('sharedwith[]')
            sharedWith = ParamsUtils.parseSharedWithParam(sharedWithList, user?.id)
        }
        favoriteSort = (params.sort == "isFavorite")
        advancedFilterCriteria = FilterUtil.buildCriteria(FilterUtil.convertToJsonFilter(params.tableFilter), clazz, user?.preference)
        manualAdvancedFilter = FilterUtil.convertToJsonFilter(params.tableFilter).findAll{it.value?.type == "manual"}.collectEntries {[(it.key): it.value.value]}
        if(params.templateTypes){
            templateTypes = []
            params.templateTypes.split(",").each {
                templateTypes.add(TemplateTypeEnum.valueOf(it.trim()))
            }
        }
        showChartSheet = params.boolean('showChartSheet')

        this.configurationClasses = configurationClasses
        submission = params.submission ? ReportSubmissionStatusEnum.valueOf(params.submission) : null
        aggregateOnly = params.aggregateOnly?:false
        periodicReportType = params.periodicReportType
        icsrWidgets = params.icsrWidgets
        forPvq=params.forPvq?params.getBoolean("forPvq"):false
    }

    LibraryFilter(User user, configurationClasses = null, String includeArchived = null ) {
        this.user = user
        this.configurationClasses = configurationClasses
        this.includeArchived = (includeArchived == "true")
    }

    LibraryFilter(Map map) {
        forPublisher = map.pvp
        search = map.search
        allReportsForPublisher = map.allReportsForPublisher == "true"
        user = map.user
        favoriteSort = map.favoriteSort
        sharedWith = map.sharedWith
        advancedFilterCriteria = map.advancedFilterCriteria
        manualAdvancedFilter = map.manualAdvancedFilter
        includeArchived = map.includeArchived
        configurationClasses = map.configurationClasses
        templateTypes = map.templateTypes
        showChartSheet = map.showChartSheet
        submission = map.submission
        aggregateOnly = map.aggregateOnly
        forPvq = map.forPvq
    }
}
