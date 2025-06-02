package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.UtilService
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportSubmission
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.LateEnum
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.grails.web.json.JSONArray

@Secured('permitAll')
class PeriodicReportConfigurationRestController extends RestfulController implements SanitizePaginationAttributes {

    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"

    def userService
    def executionStatusService
    def workflowJustificationService
    def periodicReportService
    GrailsApplication grailsApplication
    UtilService utilService

    PeriodicReportConfigurationRestController() {
        super(PeriodicReportConfiguration)
    }

    def index() {

        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params,currentUser,PeriodicReportConfiguration,[PeriodicReportConfiguration.class])
        boolean showXMLOption = grailsApplication.config.show.xml.option ?: false
        List<Long> idsForUser = ReportConfiguration.getAllIdsByFilter(filter, PeriodicReportConfiguration, showXMLOption, params.sort, params.order).list([max: params.max, offset: params.offset])
        int recordsFilteredCount = ReportConfiguration.countRecordsBySearchString(filter, showXMLOption).get()
        List<PeriodicReportConfiguration> configurationList = recordsFilteredCount ? PeriodicReportConfiguration.getAll(idsForUser) : []
        render([aaData         : briefProperties(configurationList),
                recordsTotal   : ReportConfiguration.countRecordsBySearchString(new LibraryFilter(currentUser, [PeriodicReportConfiguration.class]), showXMLOption).get(),
                recordsFiltered: recordsFilteredCount] as JSON)
    }

    def bulkSchedulingList() {
        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, currentUser, PeriodicReportConfiguration, [PeriodicReportConfiguration.class])
        List<Long> idsForUser = PeriodicReportConfiguration.fetchAllIdsForBulkUpdate(filter).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        int recordsFilteredCount = PeriodicReportConfiguration.countAllForBulkUpdate(filter).get()
        List<PeriodicReportConfiguration> configurationList = recordsFilteredCount ? PeriodicReportConfiguration.getAll(idsForUser) : []

        def aaData = configurationList.collect {
            periodicReportService.toBulkTableMap(it)
        }

        render([aaData         : aaData,
                recordsTotal   : PeriodicReportConfiguration.countAllForBulkUpdate(new LibraryFilter(currentUser)).get(),
                recordsFiltered: recordsFilteredCount] as JSON)
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def reportsList() {

        User currentUser = userService.getCurrentUser()
        sanitize(params)

        switch (params.sort) {
            case "user":
                params.sort = "owner.fullName"
                break
            case "version":
                params.sort = "numOfExecutions"
                break
            case "type":
                params.sort = "periodicReportType"
                break
            case "versionName":
                params.sort = "generatedReportName"
                break
            case "state":
                params.sort = "workflowState.name"
                break
        }
        LibraryFilter filter = new LibraryFilter(params, currentUser, ExecutedPeriodicReportConfiguration)


        showExecutedPeriodicReports(filter, params.max, params.offset, params.order, params.sort)
    }


    def latestPeriodicReport() {
        User currentUser = userService.getCurrentUser()
        sanitize(params)

        switch (params.sort) {
            case "user":
                params.sort = "owner.username"
                break
            case "version":
                params.sort = "numOfExecutions"
                break;
            case "type":
                params.sort = "periodicReportType"
                break;
        }
        LibraryFilter filter = new LibraryFilter([search: params.searchString,user: currentUser])
        filter.forPublisher=ViewHelper.isPvPModule(request)
//        showExecutedPeriodicReports(filter, params.max, params.offset, params.order, params.sort)
        List<Long> idsForUser = ExecutedPeriodicReportConfiguration.fetchAllBySearchStringAndStatusInList(filter).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order]).collect {
            it.first()
        }
        List<Map> periodicReports = formExecutedPeriodicReports(idsForUser, filter.user)
        def jsonResponse = [aaData: periodicReports] as JSON
        render(contentType: "application/json", text: jsonResponse.toString())
    }

    private void showExecutedPeriodicReports(LibraryFilter filter, int max, int offset, String direction, String sort) {
        List<Long> idsForUser = ExecutedPeriodicReportConfiguration.fetchAllBySearchStringAndStatusInList(filter, sort, direction).list([max: max, offset: offset]).collect {
            it.first()
        }
        List<Map> periodicReports = formExecutedPeriodicReports(idsForUser, filter.user)
        render([aaData: periodicReports, recordsFiltered: ExecutedPeriodicReportConfiguration.countAllBySearchStringAndStatusInList(filter).get(),
                recordsTotal: ExecutedPeriodicReportConfiguration.countAllBySearchStringAndStatusInList(new LibraryFilter(userService.currentUser, null, filter.includeArchived?.toString())).get()] as JSON)
    }

    private List<Map> formExecutedPeriodicReports(List<Long> idsForUser, User user) {
        List<ExecutedPeriodicReportConfiguration> periodicReportList = ExecutedPeriodicReportConfiguration.getAll(idsForUser)
        Locale locale = userService.getCurrentUser()?.preference?.locale
        Boolean isInDraftMode = false
        periodicReportList.collect {
            if(it.status in [ReportExecutionStatusEnum.GENERATED_DRAFT] || (it.status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED] && it.finalLastRunDate == null)){
                isInDraftMode = true
            }
            Boolean isArchived = it.executedReportUserStates.find { item -> item.user == user }?.isArchived
            if (isArchived == null) isArchived = it.archived as Boolean
            def publisherId
            def publisherName
            def casesOnly = it.status == ReportExecutionStatusEnum.GENERATED_CASES
            if (params.boolean("pvp")) {
                def publisher = it.publisherReports?.find { r ->
                    r.published
                }
                publisherId = publisher?.id
                publisherName = publisher?.name + ".docx"
            }
            [id          : it.id, type: message(message: it.periodicReportType), isPublisherReport: it.isPublisherReport, productSelection: it.productSelection ? (ViewHelper.getDictionaryValues(it.productSelection, DictionaryTypeEnum.PRODUCT)) : "", reportName: it.reportName, versionName:it.generatedReportName?:"", version: it.numOfExecutions, reportingDestinations: (it.allReportingDestinations?.join(",") ?: ""), date_range: getDateRangeString(locale, it.id),
             dueDate     : it.dueDate?.format(DateUtil.DATEPICKER_UTC_FORMAT) ?: '', indicator: getIndicator(it), license: ViewHelper.getLicenses(it),
             user        : it.owner.fullName, tags : ViewHelper.getCommaSeperatedFromList(it.tags), description: it.description, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), state: it.workflowState?.name, actions: it.workflowState?.reportActionsAsList?.join(","), hasGeneratedCasesData: it.hasGeneratedCasesData, status: it.status.toString(),
             currentState: it.workflowState, contributor:it.primaryPublisherContributor?.fullName, publisherName: publisherName, casesOnly: casesOnly, publisher: publisherId, actionItemStatus: it.actionItemStatus?.key, lastUpdated: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT), isArchived: isArchived, isFavorite: it.isFavorite(user), primaryReportingDestination: it.primaryReportingDestination, otherReportingDestinations: it.reportingDestinations?.sort {it}?.join(", "), isMultiIngredient: it.isMultiIngredient, includeWHODrugs: it.includeWHODrugs, isInDraftMode: isInDraftMode]
        }
    }

    def advancedPublisherWidgetSearch() {
        User currentUser = userService.getUser()
        def widgetFilter = params.wFilter ? JSON.parse(params.wFilter) : null
        sanitize(params)
        params.pvp = true
        List<Long> idsForUser = ExecutedPeriodicReportConfiguration.fetchByAdvancedPublisherWidgetFilter(widgetFilter, currentUser).list([max: params.max, offset: params.offset, sort: params.sort, order: params.direction]).collect {
            it.first()
        }
        List<Map> periodicReports = formExecutedPeriodicReports(idsForUser, currentUser)
        int total = ExecutedPeriodicReportConfiguration.fetchByAdvancedPublisherWidgetFilter(widgetFilter, currentUser).count()
        render([aaData: periodicReports, recordsFiltered: total, recordsTotal: total] as JSON)
    }

    def compliancePublisherWidgetSearch() {
        User currentUser = userService.getUser()
        def widgetFilter = params.wFilter ? JSON.parse(params.wFilter) : null
        if (widgetFilter?.dueDateRangeTo) widgetFilter.dueDateRangeTo = Date.parse(DateUtil.DATEPICKER_FORMAT, widgetFilter.dueDateRangeTo)
        if (widgetFilter?.dueDateRangeFrom) widgetFilter.dueDateRangeFrom = Date.parse(DateUtil.DATEPICKER_FORMAT, widgetFilter.dueDateRangeFrom)
        if (widgetFilter?.periodicReportType) {
            widgetFilter.periodicReportType = (widgetFilter.periodicReportType instanceof JSONArray) ?
                    widgetFilter.periodicReportType.collect { it as PeriodicReportTypeEnum } :
                    [widgetFilter.periodicReportType as PeriodicReportTypeEnum]
        }
        List<Long> idsForUser = ExecutedPeriodicReportConfiguration.fetchByCompliancePublisherWidgetFilter(widgetFilter, currentUser).list().collect {
            it.first()
        }
        List<ExecutedPeriodicReportConfiguration> periodicReportList = ExecutedPeriodicReportConfiguration.getAll(idsForUser)

        List<String> productFilter = widgetFilter?.product?.split("@!")?.findAll { it }
        List<String> destinationFilter = widgetFilter?.reportingDestinations?.split("@!")?.findAll { it }

        List dataRows = []
        Set products = []
        Set types = []
        Set destinations = []
        for (ExecutedPeriodicReportConfiguration report : periodicReportList) {
            Set<String> reportDestinations = report.getAllReportingDestinations()
            Set<ReportSubmission> reportSubmissions = report.reportSubmissions
            def allProducts = ViewHelper.getProductsAsList(report.productSelection)
            if (destinationFilter && !destinationFilter.find { d -> reportDestinations.find { it == d } }) continue
            if (productFilter && !productFilter.find { f -> allProducts.find { it.id == f } }) continue
            types << ViewHelper.getMessage(report.periodicReportType.getI18nKey())
            for (String dest : reportDestinations) {
                if (destinationFilter && !destinationFilter.find { dest == it }) continue
                destinations << dest
                ReportSubmission submission = reportSubmissions.find { it.reportingDestination == dest }
                Boolean late = !submission || (grailsApplication.config.submissions.late.find{ it -> it.name == submission.late}?.type!="NOT_LATE")
                for (def prod : allProducts) {
                    if (productFilter && !productFilter.find { prod.id == it }) continue
                    products << prod.text
                    dataRows << [id     : report.id, name: report.reportName, type: ViewHelper.getMessage(report.periodicReportType.getI18nKey()),
                                 product: prod.text, destination: dest, late: late]
                }

            }
        }
        Map result = [:]
        if (!widgetFilter || !widgetFilter.groupBy || widgetFilter.groupBy == "product_type") {
            result = createResultMap(products.asList(), "product", types.asList(), "type", dataRows, ViewHelper.getMessage("app.widget.button.compliancePublisher.product"))
            result.groupLabel = ViewHelper.getMessage("app.widget.button.quality.product.label")
            result.subgroupLabel = ViewHelper.getMessage("app.label.reportType")
        } else if (widgetFilter.groupBy == "product_destination") {
            result = createResultMap(products.asList(), "product", destinations.asList(), "destination", dataRows, ViewHelper.getMessage("app.widget.button.compliancePublisher.product"))
            result.groupLabel = ViewHelper.getMessage("app.widget.button.quality.product.label")
            result.subgroupLabel = ViewHelper.getMessage("app.label.reportSubmission.destinations")
        } else if (widgetFilter.groupBy == "destination_type") {
            result = createResultMap(destinations.asList(), "destination", types.asList(), "type", dataRows, ViewHelper.getMessage("app.widget.button.compliancePublisher.destination"))
            result.groupLabel = ViewHelper.getMessage("app.label.reportSubmission.destinations")
            result.subgroupLabel = ViewHelper.getMessage("app.label.reportType")
        } else if (widgetFilter.groupBy == "destination_product") {
            result = createResultMap(destinations.asList(), "destination", products.asList(), "product", dataRows, ViewHelper.getMessage("app.widget.button.compliancePublisher.destination"))
            result.groupLabel = ViewHelper.getMessage("app.label.reportSubmission.destinations")
            result.subgroupLabel = ViewHelper.getMessage("app.widget.button.quality.product.label")
        } else if (widgetFilter.groupBy == "type_product") {
            result = createResultMap(types.asList(), "type", products.asList(), "product", dataRows, ViewHelper.getMessage("app.widget.button.compliancePublisher.type"))
            result.groupLabel = ViewHelper.getMessage("app.label.reportType")
            result.subgroupLabel = ViewHelper.getMessage("app.widget.button.quality.product.label")
        } else if (widgetFilter.groupBy == "type_destination") {
            result = createResultMap(types.asList(), "type", destinations.asList(), "destination", dataRows, ViewHelper.getMessage("app.widget.button.compliancePublisher.type"))
            result.groupLabel = ViewHelper.getMessage("app.label.reportType")
            result.subgroupLabel = ViewHelper.getMessage("app.label.reportSubmission.destinations")
        }

        render(result as JSON)
    }

    protected Map createResultMap(List group, String groupPropertyName, List subgroup, String subgroupPropertyName, List dataRows, String label) {
        Map result = [X        : group,
                      label    : label,
                      subgroups: [],
                      aaData   : [],
                      total    : new int[group.size()],
                      late     : new int[group.size()],
                      rate     : new int[group.size()],
                      ontime   : new int[group.size()]]
        subgroup.each { result.put(it, new int[group.size()]) }
        dataRows.each { row ->
            int index = result.X.indexOf(row.get(groupPropertyName))
            result.total[index]++
            result.get(row.get(subgroupPropertyName))[index]++
            if (row.late)
                result.late[index]++
            else
                result.ontime[index]++

        }
        subgroup.each {
            result.subgroups.add([name: it instanceof PeriodicReportTypeEnum ? it.name() : it, data: result.get(it)])
        }
        result.total.eachWithIndex { int entry, int i ->
            result.rate[i] = Math.round(result.ontime[i] / result.total[i] * 100)
        }
        result.total.eachWithIndex { int entry, int i ->
            result.aaData << [name: group[i], total: result.total[i], late: result.late[i], rate: result.rate[i]]
        }
        return result
    }

    def getReportingProducts(String term) {

        Set result = []
        ExecutedPeriodicReportConfiguration.findAllByIsDeletedAndProductSelectionIlike(false, "%" + term + "%")?.collect {
            result.addAll(ViewHelper.getProductsAsList(it.productSelection, term))
        }
        render([items: result, total_count: -1] as JSON)
    }


    private String getIndicator(ExecutedPeriodicReportConfiguration report) {
        Date now = new Date();
        Date soon = now + 30;
        if (report.dueDate > now && report.dueDate < soon) return "yellow"
        if (report.dueDate < now && !report.reportSubmissions.find {
            it.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMITTED || it.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED
        }) return "red"
        return ""
    }

    private List briefProperties(List<PeriodicReportConfiguration> configurations) {
        return configurations.collect {
            [id: it.id, reportName: it.reportName, description: it.description, isPublisherReport:it.isPublisherReport, numOfExecutions: it.numOfExecutions, tags: ViewHelper.getCommaSeperatedFromList(it.tags), qualityChecked: it.qualityChecked, dateCreated: it.dateCreated, lastUpdated: it.lastUpdated, createdBy: it.owner.fullName, primaryReportingDestination: it.primaryReportingDestination, isFavorite: it.isFavorite(userService.currentUser)]
        }
    }

    def generateDraft(ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration) {
        if (!executedPeriodicReportConfiguration || !executedPeriodicReportConfiguration.hasGeneratedCasesData) {
            response.status = 400
            render(['error': 'Not found'] as JSON)
            return
        }
        if (executedPeriodicReportConfiguration.running) {
            log.warn("Execution for ExecutedPeriodicReport Id ${executedPeriodicReportConfiguration.id} is already executing...")
            render([warning: message(code: "app.periodicReportConfiguration.draft.already.executing")] as JSON)
            return
        }
        ReportActionEnum reportAction = params.reportAction as ReportActionEnum
        executionStatusService.generateDraft(executedPeriodicReportConfiguration, reportAction)
        render([success: true, message: message(code: 'app.Configuration.RunningMessage')] as JSON)
    }

    def defineJustification() {
        session.bulkJustification = params.bulkJustification
        render params.bulkJustification
    }

    String getDateRangeString(Locale locale, Long exConfigId){
        Sql sql = new Sql(utilService.getReportConnectionForPVR())
        String executedGlobalDateRangeQuery = "select DATE_RNG_START_ABSOLUTE, DATE_RNG_END_ABSOLUTE from EX_GLOBAL_DATE_RANGE_INFO where ID in (select EX_GLOBAL_DATE_RANGE_INFO_ID from EX_RCONFIG where ID = ${exConfigId})"
        GroovyRowResult executedGlobalDateRange = sql.firstRow(executedGlobalDateRangeQuery)
        Date startDate = executedGlobalDateRange.DATE_RNG_START_ABSOLUTE?.timestampValue()
        Date endDate = executedGlobalDateRange.DATE_RNG_END_ABSOLUTE?.timestampValue()
        sql.close()
        String dateFormat = DateUtil.getShortDateFormatForLocale(locale)
        String formattedStart = startDate.format(dateFormat)
        String formattedEnd = endDate.format(dateFormat)
        return (locale?.language == 'ja') ? "${formattedStart} ~ ${formattedEnd}" : "${formattedStart} to ${formattedEnd}"
    }
}
