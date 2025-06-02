package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.UtilService
import com.rxlogix.config.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

@Secured('permitAll')
class IcsrReportConfigurationRestController extends RestfulController implements SanitizePaginationAttributes {

    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"

    def userService
    def executionStatusService
    def periodicReportService
    UtilService utilService

    IcsrReportConfigurationRestController() {
        super(IcsrReportConfiguration)
    }

    def index() {

        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params,currentUser,IcsrReportConfiguration,[IcsrReportConfiguration.class])
        boolean showXMLOption = grailsApplication.config.show.xml.option ?: false
        List<Long> idsForUser = ReportConfiguration.getAllIdsByFilter(filter, IcsrReportConfiguration, showXMLOption, params.sort, params.order).list([max: params.max, offset: params.offset])
        int recordsFilteredCount = ReportConfiguration.countRecordsBySearchString(filter, showXMLOption).get()
        List<IcsrReportConfiguration> configurationList = recordsFilteredCount ? IcsrReportConfiguration.getAll(idsForUser) : []
        render([aaData         : briefProperties(configurationList),
                recordsTotal   : ReportConfiguration.countRecordsBySearchString(new LibraryFilter(currentUser, [IcsrReportConfiguration.class]), showXMLOption).get(),
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
        }
        LibraryFilter filter = new LibraryFilter(params, currentUser, ExecutedIcsrReportConfiguration)


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
        showExecutedPeriodicReports(filter, params.max, params.offset, params.order, params.sort)
    }

    private void showExecutedPeriodicReports(LibraryFilter filter, int max, int offset, String direction, String sort) {
        List<Long> idsForUser = ExecutedIcsrReportConfiguration.fetchAllBySearchStringAndStatusInList(filter).list([max: max, offset: offset, sort: sort, order: direction]).collect {
            it.first()
        }
        List<ExecutedIcsrReportConfiguration> periodicReportList = ExecutedIcsrReportConfiguration.getAll(idsForUser)
        Locale locale = userService.getCurrentUser()?.preference?.locale
        List<Map> periodicReports = periodicReportList.collect {
            Boolean isArchived = it.executedReportUserStates.find { item -> item.user == filter.user }?.isArchived
            if (isArchived == null) isArchived = it.archived as Boolean
            [id          : it.id, type: message(message: it.periodicReportType), productSelection: ViewHelper.getDictionaryValues(it, DictionaryTypeEnum.PRODUCT), reportName: it.reportName, version: it.numOfExecutions, reportingDestinations: (it.allReportingDestinations?.join(",") ?: ""), date_range: getDateRangeString(locale, it.id),
             dueDate     : it.dueDate?.format(DateUtil.DATEPICKER_UTC_FORMAT) ?: '', indicator: getIndicator(it),
             user        : it.owner.fullName, description: it.description, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), state: it.workflowState?.name, actions: it.workflowState?.reportActionsAsList?.join(","), hasGeneratedCasesData: it.hasGeneratedCasesData, status: it.status.toString(),
             currentState: it.workflowState, actionItemStatus: it.actionItemStatus?.key, lastUpdated: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT), isArchived: isArchived, isFavorite: it.isFavorite(filter.user), recipient: it.recipientOrganizationName, sender: it.senderOrganizationName]
        }
        render([aaData      : periodicReports, recordsFiltered: ExecutedIcsrReportConfiguration.countAllBySearchStringAndStatusInList(filter).get(),
                recordsTotal: ExecutedIcsrReportConfiguration.countAllBySearchStringAndStatusInList(new LibraryFilter(userService.currentUser, null, filter.includeArchived?.toString())).get()] as JSON)
    }

    private String getIndicator(ExecutedIcsrReportConfiguration report) {
        Date now = new Date();
        Date soon = now + 30;
        if (report.dueDate > now && report.dueDate < soon) return "yellow"
        if (report.dueDate < now && !report.reportSubmissions.find {
            it.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMITTED || it.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED
        }) return "red"
        return ""
    }

    private List briefProperties(List<ExecutedIcsrReportConfiguration> configurations) {
        return configurations.collect {
            [id: it.id, reportName: it.reportName, description: it.description, numOfExecutions: it.numOfExecutions, tags: ViewHelper.getCommaSeperatedFromList(it.tags), qualityChecked: it.qualityChecked, dateCreated: it.dateCreated, lastUpdated: it.lastUpdated, createdBy: it.owner.fullName, primaryReportingDestination: it.primaryReportingDestination, isFavorite: it.isFavorite(userService.currentUser)]
        }
    }

    def generateDraft(ExecutedIcsrReportConfiguration executedPeriodicReportConfiguration) {
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

    def resultCaseList(Long id) {
        ExecutedTemplateQuery templateQuery = ReportResult.read(id)?.executedTemplateQuery
        if (!templateQuery) {
            log.warn("Invalid request for IcsrReport resultList ${id}")
            render([aaData: [], recordsTotal: 0, recordsFiltered: 0] as JSON)
            return
        }
        sanitize(params)
        params.sort = (params.sort == "dateCreated") ? "caseNumber" : params.sort
        IcsrReportCase.withNewSession {
            def icsrCasesQuery = IcsrReportCase.getAllBySearchterm(templateQuery.id, params.searchString?.toString())
            def cases = icsrCasesQuery.list([max: params.max, offset: params.offset, sort: params.sort, order: params.order]).collect {
                [id: it.uniqueIdentifier(), caseNumber: it.caseNumber, versionNumber: it.versionNumber, productName: it.productName, eventPreferredTerm: it.eventPreferredTerm, susar: it.susar, profileName: it.profileName, downgrade: it.downgrade, processedReportId: it.processedReportId]
            }
            render([aaData: cases, recordsTotal: IcsrReportCase.getAllBySearchterm(templateQuery.id, null).count(), recordsFiltered: icsrCasesQuery.count()] as JSON)
        }
    }

    String getDateRangeString(Locale locale, Long exConfigId){
        Sql sql = new Sql(utilService.getReportConnectionForPVR())
        String executedGlobalDateRangeQuery = "select DATE_RNG_START_ABSOLUTE, DATE_RNG_END_ABSOLUTE from EX_GLOBAL_DATE_RANGE_INFO where ID in (select EX_GLOBAL_DATE_RANGE_INFO_ID from EX_RCONFIG where ID = ${exConfigId})"
        GroovyRowResult executedGlobalDateRange = sql.firstRow(executedGlobalDateRangeQuery)
        Date startDate = executedGlobalDateRange.DATE_RNG_START_ABSOLUTE?.timestampValue()
        Date endDate = executedGlobalDateRange.DATE_RNG_END_ABSOLUTE?.timestampValue()
        sql.close()
        String dateFormat = DateUtil.getShortDateFormatForLocale(locale)
        return "${startDate.format(dateFormat)} to ${endDate.format(dateFormat)}"
    }

}
