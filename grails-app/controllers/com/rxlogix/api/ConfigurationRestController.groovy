package com.rxlogix.api


import com.rxlogix.LibraryFilter
import com.rxlogix.ParamsUtils
import com.rxlogix.config.*
import com.rxlogix.enums.ConfigurationTypeEnum
import com.rxlogix.enums.KillStatusEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.*
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import com.rxlogix.enums.ExecutingEntityTypeEnum

@Secured('permitAll')
class ConfigurationRestController extends RestfulController implements SanitizePaginationAttributes {

    def configurationService
    def userService
    def customMessageService
    def CRUDService

    private static final List<Long> DEFAULT_DATE_RANGE_LIST = [1L,2L,3L,4L,5L,6L,7L,8L,10L]
    static allowedMethods = [list: 'POST',executionStatus: 'POST']


    ConfigurationRestController() {
        super(Configuration)
    }

    def index() {
        list()
    }

    def list() {
        if(params.sort=='configurationType'){
            params.sort='class'
        }
        sanitize(params)

        User currentUser = userService.getUser()
        List<Class> configurationTypes = (params.boolean("mixedTypes", false) ? [Configuration, PeriodicReportConfiguration] : [Configuration])
        LibraryFilter filter = new LibraryFilter(params, currentUser, Configuration, configurationTypes)
        boolean showXMLOption = grailsApplication.config.show.xml.option ?: false
        List<Long> idsForUser = ReportConfiguration.getAllIdsByFilter(filter, Configuration, showXMLOption, params.sort, params.order)
                .list([max: params.max, offset: params.offset])
        int recordsFilteredCount = ReportConfiguration.countRecordsByFilter(filter, showXMLOption).get()
        List<ReportConfiguration> configurationList = recordsFilteredCount ? ReportConfiguration.getAll(idsForUser) : []

        int recordsTotalCount = ReportConfiguration.countRecordsByFilter(new LibraryFilter(currentUser, configurationTypes), showXMLOption).get()
        render([aaData: briefProperties(configurationList, params.boolean("showSections")), recordsTotal: recordsTotalCount, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private List briefProperties(List<ReportConfiguration> configurations, Boolean showSections) {
        List result = []
        configurations.each {
            ConfigurationTypeEnum configurationType = ConfigurationTypeEnum.ADHOC_REPORT
            if (it instanceof PeriodicReportConfiguration) {
                configurationType = ConfigurationTypeEnum.PERIODIC_REPORT
            }
            Map row = createBriefRow(it, configurationType)
            if (showSections) {
                row.sections = []
                it.templateQueries.eachWithIndex { TemplateQuery templateQuery, int i ->
                    row.sections.add([sectionNumber: i, sectionName: templateQuery.title ?: templateQuery.template.name])
                }
            }
            result.add(row)

        }
        result
    }

    private Map createBriefRow(ReportConfiguration it, configurationType) {
        return [id         : it.id, configurationType: customMessageService.getMessage(configurationType.i18nKey),
                isAdhocType:(configurationType==ConfigurationTypeEnum.ADHOC_REPORT),
                reportName : it.reportName, description: it.description, numOfExecutions: it.numOfExecutions,
                tags       : ViewHelper.getCommaSeperatedFromList(it.tags), qualityChecked: it.qualityChecked,
                dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), createdBy: it.owner.fullName,
                lastUpdated: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                isFavorite : it.isFavorite(userService.currentUser)
        ]
    }

    def addUserDictionaryValue() {
        String name = params.term
        UserDictionary.UserDictionaryType type = params.type as UserDictionary.UserDictionaryType
        if (!UserDictionary.findAllByNameAndType(name, type)) {
            UserDictionary userDictionary = new UserDictionary(name: name, type: type)
            CRUDService.save(userDictionary)
        }
        render "ok"
    }

    def executionStatus(String status, boolean isICSRProfile) {
        User currentUser = userService.getUser()
        Map shareWith = [:]
        if (params.sharedwith instanceof String) {
            shareWith = ParamsUtils.parseSharedWithParam([], currentUser.id)
        }else {
            List<String> sharedWithList = params.list('sharedwith[]')
            shareWith = ParamsUtils.parseSharedWithParam(sharedWithList, currentUser.id)
        }
        List<Closure> advancedFilterCriteria = FilterUtil.buildCriteria(FilterUtil.convertToJsonFilter(params.tableFilter), ExecutionStatus, currentUser.preference)
        sanitize(params)
        //Default sort when sorting is removed from table
        if(!params.sort || params.sort == 'dateCreated')
            params.sort = 'runDate'

        switch (status) {
            case ReportExecutionStatusEnum.SCHEDULED.getKey():
                showExecutionsScheduled(params.searchString, params.max, params.offset, params.sort, params.order, currentUser, advancedFilterCriteria, shareWith, isICSRProfile)
                break
            case ReportExecutionStatusEnum.COMPLETED.getKey():
                showExecutionsCompleted(params.searchString, params.max, params.offset, params.sort, params.order, currentUser, advancedFilterCriteria, shareWith, isICSRProfile)
                break
            case ReportExecutionStatusEnum.ERROR.getKey():
                showExecutionsError(params.searchString, params.max, params.offset, params.sort, params.order, currentUser, advancedFilterCriteria, shareWith, isICSRProfile)
                break
            case ReportExecutionStatusEnum.BACKLOG.getKey():
                showExecutionsBacklog(params.searchString, params.max, params.offset, params.sort, params.order, currentUser, advancedFilterCriteria, shareWith, isICSRProfile)
                break
            default:
                showExecutionsInProgress(params.searchString, params.max, params.offset, params.sort, params.order, currentUser, advancedFilterCriteria, shareWith, isICSRProfile)
        }
    }

    private void showExecutionsBacklog(String searchString, int max, int offset, String sort, String direction, User currentUser, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile) {
        List<Long> executionStatusIdsList = ExecutionStatus.fetchAllBySearchStringAndBackLogStatus(searchString, currentUser, advancedFilterCriteria,shareWith, isICSRProfile, sort, direction).list([max: max, offset: offset]).collect {
            it.first()
        }
        List<ExecutionStatus> executionStatusList = executionStatusIdsList.collect { ExecutionStatus.read(it) }
        int recordsTotal = ExecutionStatus.fetchAllBySearchStringAndBackLogStatus(null, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        int recordsFilteredCount = ExecutionStatus.fetchAllBySearchStringAndBackLogStatus(searchString, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        render([aaData: reportConfigurationMapForError(executionStatusList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private void showExecutionsInProgress(String searchString, int max, int offset, String sort, String direction, User currentUser, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile) {
        List<Long> executionStatusIdsList = ExecutionStatus.fetchAllBySearchStringAndInProgressStatus(searchString, currentUser, advancedFilterCriteria, shareWith, isICSRProfile, sort, direction).list([max: max, offset: offset]).collect {
            it.first()
        }
        List<ExecutionStatus> executionStatusList = executionStatusIdsList.collect { ExecutionStatus.read(it) }
        int recordsTotal = ExecutionStatus.fetchAllBySearchStringAndInProgressStatus(null, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        int recordsFilteredCount = ExecutionStatus.fetchAllBySearchStringAndInProgressStatus(searchString, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        render([aaData: reportConfigurationMapForError(executionStatusList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private void showExecutionsError(String searchString, int max, int offset, String sort, String direction, User currentUser, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile) {
        List<Long> executionStatusIdsList = ExecutionStatus.fetchAllBySearchStringAndErrorStatus(searchString, currentUser, advancedFilterCriteria, shareWith, isICSRProfile, sort, direction).list([max: max, offset: offset]).collect {
            it.first()
        }
        List<ExecutionStatus> executionStatusList = executionStatusIdsList.collect { ExecutionStatus.read(it) }
        int recordsTotal = ExecutionStatus.fetchAllBySearchStringAndErrorStatus(null, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        int recordsFilteredCount = ExecutionStatus.fetchAllBySearchStringAndErrorStatus(searchString, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        render([aaData: reportConfigurationMapForError(executionStatusList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private void showExecutionsCompleted(String searchString, int max, int offset, String sort, String direction, User currentUser, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile) {
        List<Long> executionStatusIdsList = ExecutionStatus.fetchAllBySearchStringAndCompletedStatus(searchString, currentUser, advancedFilterCriteria, shareWith, isICSRProfile, sort, direction).list([max: max, offset: offset]).collect {
            it.first()
        }
        List<ExecutionStatus> executionStatusList = executionStatusIdsList.collect { ExecutionStatus.findById(it, [fetch:[owner:'eager',attachmentFormats:'eager',sharedWith:'eager']]) }
        int recordsTotal = ExecutionStatus.fetchAllBySearchStringAndCompletedStatus(null, currentUser,advancedFilterCriteria, shareWith, isICSRProfile).count()
        int recordsFilteredCount = ExecutionStatus.fetchAllBySearchStringAndCompletedStatus(searchString, currentUser, advancedFilterCriteria, shareWith, isICSRProfile).count()
        render([aaData: executedReportConfigurationMap(executionStatusList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }


    private void showExecutionsScheduled(String searchString, int max, int offset, String sort, String direction, User currentUser, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile) {
        List<Long> alreadyRunningConfigurationIds = ReportConfiguration.alreadyRunningConfigurationIds
        List<ExecutingEntityTypeEnum> profileEntityTypes = [ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_ICSR_PROFILE_CONFIGURATION]

        List<Long> backLogConfigurationIds
        if (isICSRProfile){
            backLogConfigurationIds = ExecutionStatus.findAllByExecutionStatusAndEntityTypeInList(ReportExecutionStatusEnum.BACKLOG, profileEntityTypes).collect {it.entityId}
        } else {
            backLogConfigurationIds = ExecutionStatus.findAllByExecutionStatusAndEntityTypeNotInList(ReportExecutionStatusEnum.BACKLOG, profileEntityTypes).collect {it.entityId}
        }

        List<Long> executionStatusIdsList = ReportConfiguration.fetchAllScheduledForUser(searchString, alreadyRunningConfigurationIds, backLogConfigurationIds, currentUser, advancedFilterCriteria, shareWith, isICSRProfile, sort, direction).list([max: max, offset: offset])?.collect {
            it.first()
        }
        List<ReportConfiguration> executionStatusList = executionStatusIdsList.collect { ReportConfiguration.read(it) }
        render([aaData: reportConfigurationMap(executionStatusList), recordsTotal: ReportConfiguration.fetchAllScheduledForUser(null, alreadyRunningConfigurationIds, backLogConfigurationIds, currentUser, advancedFilterCriteria, shareWith, isICSRProfile).count(), recordsFiltered: ReportConfiguration.fetchAllScheduledForUser(searchString, alreadyRunningConfigurationIds, backLogConfigurationIds, currentUser, advancedFilterCriteria, shareWith, isICSRProfile).count()] as JSON)
    }

    List getExecutedMap(List<ExecutionStatus> data, boolean caseSeriesReq){
        List executedReportsToFetch = []
        data.each {
            executedReportsToFetch << it.executedEntityId
            if (it.entityType in [
                    ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION,
                    ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION,
                    ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION,
                    ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION,
                    ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION,
                    ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL,
                    ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION,
                    ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION,
                    ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES,
                    ExecutingEntityTypeEnum.EXCECUTED_CASESERIES,
                    ExecutingEntityTypeEnum.EXECUTED_ICSR_CONFIGURATION
            ]) executedReportsToFetch << it.entityId
        }
        Map<Long, ExecutedReportConfiguration> executedReportsMap =
                executedReportsToFetch ? ExecutedReportConfiguration.findAllByIdInList(executedReportsToFetch).collect { it }.collectEntries { [(it.id): it] } : [:]
        Map<Long, ExecutedCaseSeries> executedCaseSeriesMap = [:]
        if (caseSeriesReq){
            executedCaseSeriesMap = executedReportsToFetch ? ExecutedCaseSeries.findAllByIdInList(executedReportsToFetch).collect { it }.collectEntries { [(it.id): it] } : [:]
        }
        return [executedReportsMap, executedCaseSeriesMap]
    }

    private executedReportConfigurationMap(List<ExecutionStatus> data) {
        List executedMap = getExecutedMap(data, true)
        Map<Long, ExecutedReportConfiguration> executedReportsMap = executedMap[0]
        Map<Long, ExecutedCaseSeries> executedCaseSeriesMap = executedMap[1]
        def executedReportConfigurationList = []
               data.each {
                   executedReportConfigurationList += getDataMap(it, executedReportsMap, executedCaseSeriesMap)
        }
        return executedReportConfigurationList
    }

    private reportConfigurationMap(List<ReportConfiguration> reportConfigurations) {
        def reportConfigurationList = []
        reportConfigurations.each {
            reportConfigurationList += [id             : it.id,
                                        reportName     : it.reportName,
                                        version        : it.numOfExecutions + 1,
                                        frequency      : configurationService.calculateFrequency(it)?.name(),
                                        runDate        : it.nextRunDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                        executionTime  : getExpectedExecutionTime(it, it.numOfExecutions),
                                        owner          : it.owner.fullName,
                                        executionStatus: ReportExecutionStatusEnum.SCHEDULED.value(),
                                        errorMessage   : "",
                                        errorTitle     : "",
                                        sharedWith     : it?.allSharedUsers?.unique()?.fullName,
                                        deliveryMedia  : it?.deliveryOption?.attachmentFormats?.sort {it?.name()}?.displayName?.join(", "),
                                        dateCreated    : it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                        exeutionStId   : 0,
                                        periodicReportType: getPeriodicReportType(it),
                                        isPriorityReport: it.isPriorityReport,
                                        isEntityTypeCanBePriority: ""
            ]
        }
        return reportConfigurationList
    }

    private String getPeriodicReportType(ReportConfiguration reportConfiguration){
        if(reportConfiguration instanceof PeriodicReportConfiguration){
            return reportConfiguration.periodicReportType.name()
        }
        return ""
    }

    private reportConfigurationMapForError(List<ExecutionStatus> executionStatuses) {
        //TODO: May be execution status should also have information on delivery media and attachment formats ASK for now it shows one for configuration which is not a snap shot??
        def reportConfigurationList = []
        List executedMap = getExecutedMap(executionStatuses, false)
        Map<Long, ExecutedReportConfiguration> executedReportsMap = executedMap[0]
        executionStatuses.each {
            reportConfigurationList += getDataMap(it, executedReportsMap)
        }
        return reportConfigurationList
    }

    // For Product Dictionary.

    def getSelectedProduct() {
        String currentLang = getLanguage()
        def product = getProductInstance(params.dictionaryLevel, params.productId, null, currentLang, "false")
        int level = Integer.parseInt(params.dictionaryLevel)
        render(['id': getIdFieldForProduct(product), 'name': getNameFieldForProduct(product), 'nextLevelItems': getChildProducts(product, level, currentLang), 'lang': currentLang] as JSON)
    }

    def getPreLevelProductParents() {
        int level = Integer.parseInt(params.dictionaryLevel)
        JSONArray parents = new JSONArray()
        String currentLang = getLanguage()
        params.productIds.split(",").each {
            def product = getProductInstance(params.dictionaryLevel, it, null, currentLang, "false")
            getParentProducts(product, level, parents, currentLang)
        }
        respond parents, [formats: ['json']]
    }

    private String getLanguage() {
        return params.lang_code ?: userService.user?.preference?.locale?.language
    }

    private def getChildProducts(def item, int level, String currentLang) {
        JSONArray children = new JSONArray()
        switch (level) {
            case 1:
                List<LmProduct> relatedProducts = LmIngredient.fetchProductsByIngredient(item.ingredientId as BigDecimal, currentLang)
                relatedProducts.each {
                    LmProductFamily family = LmProductFamily.findByProductFamilyIdAndLang(it.productFamilyId, currentLang)
                    if(family){
                        JSONObject child = new JSONObject(['id': family.productFamilyId, 'name': family.name, 'level': level + 1, 'lang': family.lang])
                        if (!children.contains(child)) {
                            children.add(child)
                        }
                    }
                }
                break
            case 2:
                List<LmProduct> relatedProducts = LmProduct.findAllByProductFamilyIdAndLang(item.productFamilyId, currentLang)
                relatedProducts.each {
                    children.add(new JSONObject(['id': it.productId, 'name': it.name, 'level': level + 1, 'lang': it.lang]))
                }
                break
            case 3:
                List<LmLicense> licenses = LmProduct.fetchLicensesByProduct(item.productId as BigDecimal, currentLang)
                licenses.each {
                    children.add(new JSONObject(['id': it.licenseId, 'name': it.tradeName, 'level': level + 1, 'lang': it.lang]))
                }
                break
        }
        return children.sort { a, b -> a.name <=> b.name }
    }

    private def getParentProducts(def item, int level, JSONArray parents, String currentLang) {
        switch (level) {
            case 2:
                def relatedProducts = LmProduct.findAllByProductFamilyIdAndLang(item.productFamilyId, currentLang)
                relatedProducts.each {
                    List<LmIngredient> ingredients = LmProduct.fetchIngredientsByProduct(it.productId, currentLang)
                    ingredients.each {
                        JSONObject parent = new JSONObject(['id': it.ingredientId, 'name': it.ingredient, 'level': level - 1, 'lang': it.lang])
                        if (!parents.contains(parent)) {
                            parents.add(parent)
                        }
                    }
                }
                break
            case 3:
                LmProductFamily family = LmProductFamily.findByProductFamilyIdAndLang(item.productFamilyId, currentLang)
                JSONObject parent = new JSONObject(['id': family.productFamilyId, 'name': family.name, 'level': level - 1, 'lang': family.lang])
                if (!parents.contains(parent)) {
                    parents.add(parent)
                }
                break
            case 4:
                List<LmProduct> relatedProducts = LmLicense.fetchProductsByLicense(item.licenseId as BigDecimal, currentLang)
                relatedProducts.each {
                    JSONObject parent = new JSONObject(['id': it.productId, 'name': it.name, 'level': level - 1, 'lang': it.lang])
                    if (!parents.contains(parent)) {
                        parents.add(parent)
                    }
                }
                break
        }
        return parents.sort { a, b -> a.name <=> b.name }
    }

    def searchProducts() {
        String currentLang = userService.user?.preference?.locale?.language
        def productList = getProductInstance(params.dictionaryLevel, null, params.contains, currentLang, params.exact_search, params.delimiter)
        JSONArray showProducts = new JSONArray()
        productList.each {
            JSONObject item = new JSONObject()
            item.level = params.dictionaryLevel
            item.id = getIdFieldForProduct(it)
            item.name = getNameFieldForProduct(it)
            item.lang = it.lang
            showProducts.add(item)
        }
        respond showProducts.sort { a, b -> a.name <=> b.name }, [formats: ['json']]
    }

    private static getNameFieldForProduct(def product) {
        if (product.hasProperty("name")) {
            return product.name
        } else if (product.hasProperty("ingredient")) {
            return product.ingredient
        } else if (product.hasProperty("tradeName")) {
            return product.tradeName
        }
    }

    private static BigDecimal getIdFieldForProduct(def product) {
        if (product.hasProperty("licenseId")) {
            return product.licenseId
        } else if (product.hasProperty("productId")) {
            return product.productId
        } else if (product.hasProperty("ingredientId")) {
            return product.ingredientId
        } else {
            return product.productFamilyId
        }
    }

    private getProductInstance(String level, String productId, String searchTerm, String currentLang, String exactSearch, String delimiter = null) {
        List<String> searchItems;
        if (searchTerm && delimiter) {
            searchItems = Arrays.asList(searchTerm.split(delimiter));
        }
        String productSectorId = params.productSector
        String productSectorTypeId = params.productSectorType
        String deviceTypeId = params.deviceType
        String companyUnitId = params.companyUnit
        switch (level) {
            case "1":
                if (productId) return LmIngredient.findByIngredientIdAndLang(productId as BigDecimal, currentLang)
                else if (searchItems || searchTerm) {
                    List<BigDecimal> lmIngredientIdsList
                    if(productSectorId || productSectorTypeId || deviceTypeId || companyUnitId){
                        lmIngredientIdsList = LmIngredient.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
                        if (!lmIngredientIdsList) {
                            return []
                        }
                    }

                    List<LmIngredient> lmIngredientList = []
                    if (lmIngredientIdsList) {
                        lmIngredientIdsList.collate(999).each { list ->
                            lmIngredientList += getProductInstanceList(searchItems, searchTerm, exactSearch, true, list, LmIngredient, "ingredientId", "ingredient")
                        }
                    } else {
                        lmIngredientList += getProductInstanceList(searchItems, searchTerm, exactSearch, false, null, LmIngredient, "ingredientId", "ingredient")
                    }
                    return lmIngredientList
                }
                break
            case "2":
                if (productId) return LmProductFamily.findByProductFamilyIdAndLang(productId as BigDecimal, currentLang)
                else if (searchItems || searchTerm) {
                    List<BigDecimal> lmProductFamilyIdsList
                    if(productSectorId || productSectorTypeId || deviceTypeId || companyUnitId){
                        lmProductFamilyIdsList = LmProductFamily.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
                        if (!lmProductFamilyIdsList) {
                            return []
                        }
                    }

                    List<LmProductFamily> lmProductFamilyList = []
                    if (lmProductFamilyIdsList) {
                        lmProductFamilyIdsList.collate(999).each { list ->
                            lmProductFamilyList += getProductInstanceList(searchItems, searchTerm, exactSearch, true, list, LmProductFamily, "productFamilyId", "name")
                        }
                    } else {
                        lmProductFamilyList += getProductInstanceList(searchItems, searchTerm, exactSearch, false, null, LmProductFamily, "productFamilyId", "name")
                    }
                    return lmProductFamilyList
                }
                break
            case "3":
                if (productId) return LmProduct.findByProductIdAndLang(productId as BigDecimal, currentLang)
                else if (searchItems || searchTerm) {
                    List<BigDecimal> lmProductIdsList
                    if(productSectorId || productSectorTypeId || deviceTypeId || companyUnitId){
                        lmProductIdsList = LmProduct.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
                        if (!lmProductIdsList) {
                            return []
                        }
                    }

                    List<LmProduct> lmProductList = []
                    if (lmProductIdsList) {
                        lmProductIdsList.collate(999).each { list ->
                            lmProductList += getProductInstanceList(searchItems, searchTerm, exactSearch, true, list, LmProduct, "productId","name")
                        }
                    } else {
                        lmProductList += getProductInstanceList(searchItems, searchTerm, exactSearch, false, null, LmProduct, "productId", "name")
                    }
                    return lmProductList
                }
                break
            case "4":
                if (productId) return LmLicense.findByLicenseIdAndLang(productId as BigDecimal, currentLang)
                else if (searchItems || searchTerm) {
                    List<BigDecimal> lmLicenseIdsList
                    if(productSectorId || productSectorTypeId || deviceTypeId || companyUnitId){
                        lmLicenseIdsList = LmLicense.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
                        if (!lmLicenseIdsList) {
                            return []
                        }
                    }

                    List<LmLicense> lmLicenseList = []
                    if (lmLicenseIdsList) {
                        lmLicenseIdsList.collate(999).each { list ->
                            lmLicenseList += getProductInstanceList(searchItems, searchTerm, exactSearch, true, list, LmLicense, "licenseId", "tradeName")
                        }
                    } else {
                        lmLicenseList += getProductInstanceList(searchItems, searchTerm, exactSearch, false, null, LmLicense, "licenseId", "tradeName")
                    }
                    return lmLicenseList
                }
                break
        }
    }

    // For Study Dictionary.

    def getSelectedStudy() {
        String currentLang = getLanguage()
        def study = getStudyInstance(params.dictionaryLevel, params.studyId, null, currentLang, "false")
        int level = Integer.parseInt(params.dictionaryLevel)
        render(['id': getIdFieldForStudies(study), 'name': getNameFieldForStudy(study), 'nextLevelItems': getChildStudies(study, level, currentLang), 'lang': currentLang] as JSON)
    }

    def getPreLevelStudyParents() {
        int level = Integer.parseInt(params.dictionaryLevel)
        JSONArray parents = new JSONArray()
        String currentLang = getLanguage()
        params.studyIds.split(",").each {
            def study = getStudyInstance(params.dictionaryLevel, it, null, currentLang, "false")
            getParentStudies(study, level, parents, currentLang)
        }
        respond parents, [formats: ['json']]
    }

    private def getChildStudies(def item, int level, String currentLang) {
        JSONArray children = new JSONArray()
        switch (level) {
            case 1:
                List<LmStudies> relatedStudies = LmProtocols.fetchStudiesByProtocols(item.protocolId as BigDecimal,currentLang)
                relatedStudies.each {
                    JSONObject child = new JSONObject(['id': getIdFieldForStudies(it), 'name': it.studyNum, 'level': level + 1,'lang': it.lang])
                    if (!children.contains(child)) {
                        children.add(child)
                    }
                }
                break
        }
        return children.sort { a, b -> a.name <=> b.name }
    }

    private def getParentStudies(def item, int level, JSONArray parents, String currentLang) {
        switch (level) {
            case 2:
                Set<LmProtocols> lmProtocols = LmStudies.fetchProtocolsByStudy(item.studyId,currentLang)
                lmProtocols.each {
                    JSONObject parent = new JSONObject(['id': getIdFieldForStudies(it), 'name': it.description, 'level': level - 1, 'lang': it.lang])
                    if (!parents.contains(parent)) {
                        parents.add(parent)
                    }
                }
                break
        }
        return parents.sort { a, b -> a.name <=> b.name }
    }

    def searchStudies() {
        def showStudies = [] as List

        String currentLang = userService.user?.preference?.locale?.language
        def studyList = getStudyInstance(params.dictionaryLevel?.toString(), null, params.contains?.toString(), currentLang, params.exact_search?.toString(), params.delimiter?.toString(), params.imp.toBoolean())
        studyList.each {
            JSONObject item = new JSONObject()
            item.level = params.ref_level ?: params.dictionaryLevel
            item.id = getIdFieldForStudies(it)
            item.name = getNameFieldForStudy(it)
            item.lang = it.lang
            showStudies.add(item)
        }
        respond showStudies.sort { a, b -> a.name <=> b.name }, [formats: ['json']]
    }

    def killExecution(Long id) {
        log.info('Kill report execution request created by user: ' + userService.currentUser?.username + ' for execution id: ' + id)
        checkRightsAndExecute { executingObject, executionStatus ->
            ReportExecutionKillRequest reportExecutionKillRequest = new ReportExecutionKillRequest(executionStatusId: executionStatus.id,killStatus: KillStatusEnum.NEW)
            CRUDService.save(reportExecutionKillRequest)
        }
    }

    def updatePriority(Long configId, String exStatus, Long exStatusId) {
        try {
            if(exStatus == ReportExecutionStatusEnum.SCHEDULED.getKey() || exStatus == ReportExecutionStatusEnum.BACKLOG.getKey()) {
                if (exStatus == ReportExecutionStatusEnum.SCHEDULED.getKey()) {
                    Configuration configuration = Configuration.findByIdAndExecutingAndNextRunDateIsNotNull(configId, false)
                    if (configuration) {
                        configuration.isPriorityReport = true
                        CRUDService.update(configuration)

                    }
                }
                ExecutionStatus executionStatus = new ExecutionStatus()
                if(exStatusId > 0) {
                    //When User is on the Queue Page
                    executionStatus = ExecutionStatus.findById(exStatusId)
                }else {
                    //When User wait for sometime and then perform the action from scheduled Page
                    executionStatus = ExecutionStatus.findAllByEntityIdAndExecutionStatusAndEntityType(configId, ReportExecutionStatusEnum.BACKLOG, ExecutingEntityTypeEnum.CONFIGURATION, [sort: 'dateCreated', order: 'desc'])[0]
                }
                if (executionStatus) {
                    executionStatus.isPriorityReport = true
                    CRUDService.update(executionStatus)
                }
            }
            render([success: true] as JSON)
        }catch(Exception e) {
            log.error("Unknown Error occurred while updating the priority configuration for: ${configId} ",e)
            response.status = 500
            Map responseMap = [
                    message:  message(code: "default.server.error.message"),
                    status: 500
            ]
            render(contentType: "application/json", responseMap as JSON)
        }
    }

    private static getNameFieldForStudy(def study) {
        if (study.hasProperty("studyNum")) {
            return study.studyNum
        } else if (study.hasProperty("description")) {
            return study.description
        } else if (study.hasProperty("center")) {
            return study.center
        }
    }

    private static BigDecimal getIdFieldForStudies(def studies) {
        if (studies.hasProperty("protocolId")) {
            return studies.protocolId
        }
        else{
            return studies.studyId
        }
    }

    private getStudyInstance(String level, String studyId, String searchTerm, String currentLang, String exactSearch, String delimiter = null, Boolean imp = false) {
        List<String> searchItems;
        if (searchTerm && delimiter) {
            searchItems = Arrays.asList(searchTerm.split(delimiter));
        }

        switch (level) {
            case "1":
                if (studyId) return LmProtocols.findByProtocolIdAndLang(studyId as BigDecimal,currentLang)
                else if (exactSearch == "true") {
                    if (searchItems) return LmProtocols.createCriteria().list {
                        or {
                            searchItems.each { item -> eq("description", item.trim(), [ignoreCase: true]) }
                        }
                    } else if (searchTerm) return LmProtocols.createCriteria().list {
                         eq("description", searchTerm.trim(), [ignoreCase: true])
                    }
                } else {
                    if (searchItems) return LmProtocols.createCriteria().list {
                         or {
                            searchItems.each { item -> iLikeWithEscape("description", "%${EscapedILikeExpression.escapeString(item.trim())}%") }
                        }
                    } else if (searchTerm) return LmProtocols.createCriteria().list {
                         iLikeWithEscape("description", "%${EscapedILikeExpression.escapeString(searchTerm.trim())}%")
                    }
                }
                break
            case "2":
                if (studyId) return LmStudies.findByStudyIdAndLang(studyId as BigDecimal,currentLang)
                else if (exactSearch == "true") {
                    if (searchItems) return LmStudies.createCriteria().list {
                         or {
                            searchItems.each { item -> eq("studyNum", item.trim(), [ignoreCase: true]) }
                        }
                    } else if (searchTerm) return LmStudies.createCriteria().list {
                        eq("studyNum", searchTerm.trim(), [ignoreCase: true])
                    }
                } else {
                    if (searchItems) return LmStudies.createCriteria().list {
                        or {
                            searchItems.each { item -> iLikeWithEscape("studyNum", "%${EscapedILikeExpression.escapeString(item.trim())}%") }
                        }
                    } else if (searchTerm) return LmStudies.createCriteria().list {
                        iLikeWithEscape("studyNum", "%${EscapedILikeExpression.escapeString(searchTerm.trim())}%")
                    }
                }

                break
            case "3":
                List<BigDecimal> productIdList = []
                Set<BigDecimal> studyKeyList = []
                if (searchItems) {
                    productIdList = LmDrugWithStudy.createCriteria().list {
                        or {
                            searchItems.each { item -> eq("name", "${item.trim()}", [ignoreCase: false]) }
                        }
                        projections {
                            property('productId')
                        }
                    }
                } else {
                    productIdList = LmDrugWithStudy.findAllByNameLike("${searchTerm}")?.productId
                }
                productIdList.collate(999).each { List products ->
                    LmStudyDrugs.createCriteria().list {
                        inList 'productId', products
                        if (imp) {
                            eq 'isImp', true
                        }
                    }*.studyId?.each {
                        studyKeyList.add(it)
                    }
                }
                List<LmStudies> lmStudiesList = []
                if (studyKeyList) {
                    studyKeyList.toList().collate(999).each { list ->
                        lmStudiesList += LmStudies.createCriteria().list {
                            inList("studyId", list)
                           }
                    }
                }
                return lmStudiesList
                break

            case "4":
                List<String> compoundIdList = []
                Set<BigDecimal> studyKeyList = []
                if (searchItems) {
                    compoundIdList = LmCompound.createCriteria().list {
                        or {
                            searchItems.each { item -> eq("number", "${item.trim()}", [ignoreCase: false]) }
                        }
                        projections {
                            property('number')
                        }
                    }
                } else {
                    compoundIdList = LmCompound.findAllByNumberLike("${searchTerm}")?.number
                }

                compoundIdList.collate(999).each { List compounds ->
                    LmStudyCompound.findAllByCompoundIdInList(compounds)*.studyId?.each {
                        studyKeyList.add(it)
                    }
                }
                List<LmStudies> lmStudiesList = []
                if (studyKeyList) {
                    studyKeyList.toList().collate(999).each { list ->
                        lmStudiesList += LmStudies.createCriteria().list {
                            inList("studyId", list)
                         }
                    }
                }
                return lmStudiesList
                break
        }
    }

    private int getExpectedExecutionTime(BaseConfiguration reportConfiguration, int num) {
        if (num >= 1) {
            return (reportConfiguration.totalExecutionTime).div(num)
        }
        return 0
    }


    private Map getDataMap(ExecutionStatus executionStatus, Map<Long, ExecutedReportConfiguration> executedReportsMap, Map<Long, ExecutedCaseSeries> executedCaseSeriesMap = [:]) {
        User currUser = userService.currentUser
        Map data = [:]
        if (executionStatus.executionStatus in ReportExecutionStatusEnum.completedStatusesList) {
            ExecutedReportConfiguration executedReportConfiguration = executedReportsMap.get(executionStatus.executedEntityId)
            ExecutedCaseSeries executedCaseSeries = executedCaseSeriesMap.get(executionStatus.executedEntityId)
            boolean isFinal = executionStatus.entityType == ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL
            boolean isDeleted = false;
            if(executedReportConfiguration){
                isDeleted = (executedReportConfiguration.isDeleted || executedReportConfiguration.executedReportUserStates.find { item -> item.user == currUser }?.isDeleted)
            } else if(executedCaseSeries){
                isDeleted = (executedCaseSeries.isDeleted || executedCaseSeries.executedCaseSeriesStates.find { item -> item.user == currUser }?.isDeleted)
            }
            data = [id             : executionStatus.entityId,
                    reportName: executionStatus.reportName + (executionStatus.aggregateReportStatus ? " (${ViewHelper.getMessage(executionStatus.aggregateReportStatus.getI18nValueForAggregateReportStatus())})" : ""),
                    periodicReportType     : executionStatus.periodicReportType,
                    version        : executionStatus.reportVersion,
                    frequency      : executionStatus.frequency?.name(),
                    runDate        : executedReportConfiguration ? (isFinal ? executedReportConfiguration.finalLastRunDate : executedReportConfiguration.lastRunDate) : executionStatus.lastUpdated,
                    executionTime  : executionStatus.executionTime,
                    owner          : executionStatus.owner.fullName,
                    executionStatus: executionStatus.executionStatus?.value(),
                    errorMessage   : executionStatus.message,
                    errorTitle     : executionStatus.sectionName,
                    sharedWith     : executionStatus.sharedWith?.unique()?.fullName,
                    deliveryMedia  : executionStatus.attachmentFormats?.sort {it?.name()}?.displayName?.join(", "),
                    dateCreated    : getExecutedEntityGenerateDate(executionStatus, executedReportsMap)?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                    exeutionStId   : executionStatus.id,
                    isPriorityReport: executionStatus.isPriorityReport,
                    isDeleted      : isDeleted,
                    isEntityTypeCanBePriority      :   ""]
        } else if (executionStatus.executionStatus in (ReportExecutionStatusEnum.inProgressStatusesList + [ReportExecutionStatusEnum.BACKLOG, ReportExecutionStatusEnum.ERROR])) {
            data = [id             : executionStatus.entityId,
                    reportName: executionStatus.reportName + (executionStatus.aggregateReportStatus ? " (${ViewHelper.getMessage(executionStatus.aggregateReportStatus.getI18nValueForAggregateReportStatus())})" : ""),
                    periodicReportType     : executionStatus.periodicReportType,
                    version        : executionStatus.reportVersion,
                    frequency      : executionStatus.frequency?.name(),
                    runDate        : executionStatus.nextRunDate,
                    executionTime  : executionStatus.executionTime,
                    owner          : executionStatus.owner.fullName,
                    executionStatus: executionStatus.executionStatus?.value() ?: ReportExecutionStatusEnum.ERROR.value(),
                    errorMessage   : executionStatus.message,
                    errorTitle     : executionStatus.sectionName,
                    sharedWith     : executionStatus.sharedWith?.fullName,
                    deliveryMedia  : executionStatus.attachmentFormats?.sort {it?.name()}?.displayName?.join(", "),
                    dateCreated    : executionStatus.dateCreated,
                    exeutionStId   : executionStatus.id,
                    isPriorityReport : executionStatus.isPriorityReport,
                    isDeleted      : "",
                    isEntityTypeCanBePriority : ExecutionStatus.canReportExecuteForPriority(executionStatus.getEntityClass())]
        }
        return data
    }

    Date getExecutedEntityGenerateDate(ExecutionStatus executionStatus, Map<Long, ExecutedReportConfiguration> executedReportsMap) {
        Date date = null
        switch (executionStatus.entityType) {
            case ExecutingEntityTypeEnum.CONFIGURATION:
                date = executedReportsMap.get(executionStatus.executedEntityId)?.dateCreated
                break;
            case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                date = executedReportsMap.get(executionStatus.executedEntityId)?.dateCreated
                break;
            case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                date = executedReportsMap.get(executionStatus.executedEntityId)?.dateCreated
                break;
            case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                date = executedReportsMap.get(executionStatus.executedEntityId)?.dateCreated
                break;
            case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
            case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                date = executedReportsMap.get(executionStatus.entityId)?.lastUpdated
                break;
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
            case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                date = executedReportsMap.get(executionStatus.entityId)?.lastUpdated
                break;
            case ExecutingEntityTypeEnum.CASESERIES:
                date = executedReportsMap.get(executionStatus.executedEntityId)?.dateCreated
                break;
            case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
            case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                date = executedReportsMap.get(executionStatus.entityId)?.lastUpdated
                break;
            case ExecutingEntityTypeEnum.EXECUTED_ICSR_CONFIGURATION:
                date = executedReportsMap.get(executionStatus.entityId)?.lastUpdated
                break;
        }
        return date
    }

    def getConfigurationPOIInputsParams(Long id) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(id)
        List data = []
        if (executedReportConfiguration && executedReportConfiguration.poiInputsParameterValues) {
            data = executedReportConfiguration.poiInputsParameterValues.collect { it.key }
        }
        render([data: data] as JSON)
    }

    def unschedule() {
        Long id = params.id as Long
        try {
            if (id) {
                ReportConfiguration configurationInstance = ReportConfiguration.get(id)
                if (configurationInstance.isEditableBy(userService.currentUser)) {
                    if (!configurationInstance.executing) {
                        configurationInstance.setIsEnabled(false)
                        configurationInstance.setIsPriorityReport(false)
                        CRUDService.update(configurationInstance)
                    } else {
                        response.status = 406
                        Map responseMap = [
                                message: message(code: "app.configuration.unscheduled.fail", args: [configurationInstance.reportName]),
                                status: 406
                        ]
                        render(contentType: "application/json", responseMap as JSON)
                        return
                    }
                } else {
                    response.status = 401
                    Map responseMap = [
                            message: message(code: "app.configuration.edit.permission", args: [configurationInstance.reportName]),
                            status: 401
                    ]
                    render(contentType: "application/json", responseMap as JSON)
                    return
                }
            }
            render([success: true] as JSON)
        } catch (Exception ex) {
            log.error("UnKnown Error occurred while unscheduling configuration for: ${id} ", ex)
            response.status = 500
            Map responseMap = [
                    message: message(code: "default.server.error.message"),
                    status: 500
            ]
            render(contentType: "application/json", responseMap as JSON)
        }
    }

    def removeFromBacklog() {
        checkRightsAndExecute { executingObject, executionStatus ->
            // TODO we would need to check if we can stop Unscheduling future executions as well.
            if (ExecutionStatus.removeFromBacklog(executionStatus.id) && executingObject instanceof BaseConfiguration) {
                if (executingObject instanceof PeriodicReportConfiguration)
                    executingObject.executing = false
                executingObject.setIsEnabled(false)
                executingObject.setIsPriorityReport(false)
                CRUDService.update(executingObject)
            }
        }
    }

    def checkRightsAndExecute(closure) {

        Long id = params.id as Long
        try {
            if (id) {
                ExecutionStatus executionStatus = ExecutionStatus.read(id)
                Object executingObject = executionStatus ? executionStatus?.getEntityClass()?.get(executionStatus.entityId) : null
                if (executionStatus && executingObject) {
                    User user = userService.currentUser
                    if (user.isAdmin() || executingObject.owner?.id == user.id) {
                        closure.call(executingObject, executionStatus)
                    } else {
                        response.status = 401
                        Map responseMap = [
                                message: message(code: "app.configuration.edit.permission", args: [executingObject.hasProperty('reportName') ? executingObject.reportName : '']),
                                status: 401
                        ]
                        render(contentType: "application/json", responseMap as JSON)
                        return
                    }
                } else {
                    response.status = 404
                    Map responseMap = [
                            message:  message(code: 'default.not.found.message', args: [message(code: 'app.label.executionStatus'), id]),
                            status: 404
                    ]
                    render(contentType: "application/json", responseMap as JSON)
                    return
                }
            }
            render([success: true] as JSON)
        } catch (Exception ex) {
            log.error("UnKnown Error occurred while checkRightsAndExecute configuration for: ${id} ",ex)
            response.status = 500
            Map responseMap = [
                    message:  message(code: "default.server.error.message"),
                    status: 500
            ]
            render(contentType: "application/json", responseMap as JSON)
        }
    }

    def getProductSectorList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        List<LmProductSector> items = LmProductSector.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'name']) {
            if (term) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }

        render([items : items.collect {
            [id: it.id, text: it.name]
        }, total_count: items.totalCount] as JSON)
    }

    def getProductSectorTypeList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        List<LmProductSectorType> items = LmProductSectorType.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'name']) {
            if (term) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }

        render([items : items.collect {
            [id: it.id, text: it.name]
        }, total_count: items.totalCount] as JSON)
    }

    def getProductsList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        String currentLang = userService.user?.preference?.locale?.language

        List<LmDrugWithStudy> items = LmDrugWithStudy.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'name']) {
            if (term) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }

        render([items : items.collect {
            [id: it.name, text: it.name]     //TODO need to check if it's expected - Sachin
        }, total_count: items.totalCount] as JSON)
    }

    def getDeviceTypeList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        List<LmDeviceType> items = LmDeviceType.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'name']) {
            if (term) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }
        render([items : items.collect {
            [id: it.id, text: it.name]
        }, total_count: items.totalCount] as JSON)
    }

    def getCompanyUnitList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        List<LmCompanyUnit> items = LmCompanyUnit.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'name']) {
            if (term) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }
        render([items : items.collect {
            [id: it.id, text: it.name]
        }, total_count: items.totalCount] as JSON)
    }

    def getCompoundsList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        String currentLang = userService.user?.preference?.locale?.language

        List<LmCompound> items = LmCompound.createCriteria().list([offset: Math.max(page - 1, 0) * max, max: max, order: 'asc', sort: 'number']) {
            if (term) {
                iLikeWithEscape('number', "%${EscapedILikeExpression.escapeString(term)}%")
            }
        }

        render([items : items.collect {
            [id: it.number, text: it.number] //TODO need to check if it's expected - Sachin both same
        }, total_count: items.totalCount] as JSON)
    }

    private List getProductInstanceList(searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName){
         return clz.createCriteria().list {
            and {
                if (searchItems) {
                    or {
                        searchItems.each { item ->
                            if (exactSearch == "true") {
                                eq("${propertyName}", item.trim(), [ignoreCase: true])
                            } else {
                                iLikeWithEscape("${propertyName}", "%${EscapedILikeExpression.escapeString(item.trim())}%")
                            }
                        }
                    }
                } else {
                    if (exactSearch == "true") {
                        eq("${propertyName}", searchTerm.trim(), [ignoreCase: true])
                    } else {
                        iLikeWithEscape("${propertyName}", "%${EscapedILikeExpression.escapeString(searchTerm.trim())}%")
                    }
                }
                if (filterList) {
                    inList("${fieldName}", list)
                }

            }

        }
    }

    def fetchDateRangeTypesForDatasource(Long dataSourceId){
            SourceProfile sourceProfile = SourceProfile.read(dataSourceId)
            if(!sourceProfile){
                return [] as JSON
            }

            List dateRangeTypeList = []

            if(sourceProfile.dateRangeTypes){
                if(params.boolean('userSpecificDateRangeType')){
                    List<Long> userDateRangeTypes = User.getDateRangeTypesForUser(userService.getCurrentUser())
                    dateRangeTypeList = sourceProfile.dateRangeTypes*.id.toList().intersect(userDateRangeTypes)
                    if(dateRangeTypeList.size() == 0){
                        dateRangeTypeList = DEFAULT_DATE_RANGE_LIST
                    }
                } else{
                    dateRangeTypeList = sourceProfile.dateRangeTypes*.id.toList()
                }
                render ViewHelper.getDateRangeTypeI18nInList(dateRangeTypeList) as JSON
            } else {
                if(params.boolean('userSpecificDateRangeType')){
                    dateRangeTypeList = User.getDateRangeTypesForUser(userService.getCurrentUser())
                    if(dateRangeTypeList.size() == 0){
                        dateRangeTypeList = DEFAULT_DATE_RANGE_LIST
                    }
                    render ViewHelper.getDateRangeTypeI18nInList(dateRangeTypeList) as JSON
                }else {
                    render ViewHelper.getDateRangeTypeI18n() as JSON
                }
            }
    }

    def fetchEvaluateCaseDatesForDatasource(Long dataSourceId, Boolean showAllversions){
        SourceProfile sourceProfile = SourceProfile.read(dataSourceId)
        if(!sourceProfile){
            return [] as JSON
        }
        if(sourceProfile.includeLatestVersionOnly){
            render ViewHelper.getCaseDateI18nForLatestVersion() as JSON
        } else {
            render ViewHelper.getEvaluateCaseDateI18n(showAllversions == null ? true : showAllversions) as JSON
        }
    }

    def fetchEvaluateCaseDateSubmissionForDatasource(Long dataSourceId){
        SourceProfile sourceProfile = SourceProfile.read(dataSourceId)
        if(!sourceProfile){
            return [] as JSON
        }
        if(sourceProfile.includeLatestVersionOnly){
            render ViewHelper.getCaseDateI18nForLatestVersion() as JSON
        } else {
            render ViewHelper.getEvaluateCaseDateSubmissionI18n() as JSON
        }
    }

    def bulkSchedulingList() {
        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, currentUser, Configuration, [Configuration.class])
        List<Long> sharedWithIds = Configuration.ownedByUser(filter.user).list()
        List<Long> idsForUser = sharedWithIds ? Configuration.fetchAllIdsForBulkUpdate(filter, sharedWithIds).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order]) : []
        int recordsFilteredCount = sharedWithIds ? Configuration.countAllForBulkUpdate(filter, sharedWithIds).get() : 0
        List<Configuration> configurationList = recordsFilteredCount ? Configuration.getAll(idsForUser) : []

        if(params.sort == "DeliveryOption.sharedWith"){
            configurationList.sort {x,y->
                !x.deliveryOption.sharedWith[0] ? !y.deliveryOption.sharedWith[0] ? 0 : 1 : !y.deliveryOption.sharedWith[0] ? -1 : x.deliveryOption?.sharedWith[0].fullName.toUpperCase() <=> y.deliveryOption?.sharedWith[0].fullName.toUpperCase()
            }
            if (params.order=="desc") {
                configurationList = configurationList.reverse()
            }
        }

        if(params.sort == "DeliveryOption.emailToUsers"){
            configurationList.sort {x,y->
                !x.deliveryOption.emailToUsers[0] ? !y.deliveryOption.emailToUsers[0] ? 0 : 1 : !y.deliveryOption.emailToUsers[0] ? -1 : x.deliveryOption?.emailToUsers[0]?.toUpperCase() <=> y.deliveryOption?.emailToUsers[0]?.toUpperCase()
            }
            if (params.order=="desc") {
                configurationList = configurationList.reverse()
            }
        }

        def aaData = configurationList.collect {
            configurationService.toBulkTableMap(it)
        }

        render([aaData         : aaData,
                recordsTotal   : sharedWithIds ? Configuration.countAllForBulkUpdate(new LibraryFilter(currentUser), sharedWithIds).get() : 0,
                recordsFiltered: recordsFilteredCount] as JSON)
    }
    def getConfigurationsList(String term, Integer page, Integer max) {
        forSelectBox(params)
        params.advancedFilter = true
        params.tableFilter = " {\"reportName\":{\"type\":\"text\",\"name\":\"reportName\",\"value\":\"$term\"}}"

        User currentUser = userService.getUser()
        List<Class> configurationTypes = [Configuration, PeriodicReportConfiguration]
        LibraryFilter filter = new LibraryFilter(params, currentUser, Configuration, configurationTypes)
        List<Long> idsForUser = ReportConfiguration.getAllIdsByFilter(filter, Configuration).list([max: params.max, offset: params.offset, sort: "reportName", order: "asc"])
        int recordsFilteredCount = ReportConfiguration.countRecordsByFilter(filter).get()
        List<ReportConfiguration> configurationList = recordsFilteredCount ? ReportConfiguration.getAll(idsForUser) : []

        render([items : configurationList.collect {
            [id: it.id, text: it.reportName]
        }, total_count: recordsFilteredCount] as JSON)
    }

    def redirectViewConfiguration() {
        def config = ReportConfiguration.get(params.long("id"))
        if (config instanceof Configuration)
            redirect(controller: "configuration", action: "view", params: params)
        else
            redirect(controller: "periodicReport", action: "view", params: params)
    }
}