package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ReportResult
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class ReportResultRestController extends RestfulController implements SanitizePaginationAttributes {
    def userService

    static allowedMethods = [executedPeriodicReports: ['POST']]

    ReportResultRestController() {
        super(ReportResult)
    }

    def index() {
        executedPeriodicReports()
    }

    def executedPeriodicReports() {
        sanitize(params)
        prepareSortParam()

        User currentUser = userService.getCurrentUser()
        LibraryFilter filter = new LibraryFilter(params, currentUser, ExecutedConfiguration)

        List<Long> idsForUser = ExecutedConfiguration.fetchAllBySearchStringAndStatusInList(filter)
                .list([max: params.max, offset: params.offset, sort: params.sort, order: params.order]).collect {it.first()}
        List<ExecutedConfiguration> periodicReportList = ExecutedConfiguration.getAll(idsForUser)

        def filteredCount = ExecutedConfiguration.countAllBySearchStringAndStatusInList(filter).get()
        LibraryFilter totalCountFilter = new LibraryFilter(user: filter.user, forPvq: filter.forPvq, manualAdvancedFilter: filter.manualAdvancedFilter, includeArchived: filter.includeArchived)
        def count = ExecutedConfiguration.countAllBySearchStringAndStatusInList(totalCountFilter).get()
        render getExecutedConfigMaps(periodicReportList, filteredCount, count, filter.user, filter.includeArchived) as JSON
    }

    def latestAdhocReport() {
        if (ViewHelper.isPvPModule(request)) {
            Map result = [aaData: [], recordsFiltered: 0, recordsTotal: 0]
            render result as JSON
            return
        }
//        forward(action: 'index', params: params)
        sanitize(params)
        prepareSortParam()
        User currentUser = userService.getCurrentUser()
        LibraryFilter filter = new LibraryFilter(params, currentUser, ExecutedConfiguration)

        List<Long> idsForUser = ExecutedConfiguration.fetchAllBySearchStringAndStatusInList(filter)
                .list([max: params.max, offset: params.offset, sort: params.sort, order: params.order]).collect {it.first()}
        List<ExecutedConfiguration> periodicReportList = ExecutedConfiguration.getAll(idsForUser)
        def configsMap = []
        periodicReportList.each {
            String status = it.workflowState?.name
            Boolean isArchived = false;
            configsMap += [state: it.workflowState?.name, reportName: it.reportName, description: it.description, owner: it.owner.fullName, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                           tags : ViewHelper.getCommaSeperatedFromList(it.tags), id: it.id, numOfExecutions: it.numOfExecutions, status: status, actions: it.workflowState?.reportActionsAsList?.join(","), actionItemStatus: it.actionItemStatus?.key, isArchived: isArchived, isFavorite: it.isFavorite(currentUser), isMultiIngredient: it.isMultiIngredient, includeWHODrugs: it.includeWHODrugs]
        }
        def jsonResponse = [aaData: configsMap] as JSON
        render(contentType: "application/json", text: jsonResponse.toString())
    }

    private getExecutedConfigMaps(List<ExecutedConfiguration> executedConfiguration, def totalFilteredCount, def totalCount, User currentUser, Boolean includeArchived) {
        def configsMap = []
        executedConfiguration.each {
            String status = it.workflowState?.name
            Boolean isArchived = false;
            if (includeArchived)
                isArchived = it.executedReportUserStates.find { item -> item.user == currentUser }?.isArchived
            if (isArchived == null) isArchived = it.archived as Boolean
            configsMap += [state: it.workflowState?.name, reportName: it.reportName, description: it.description, owner: it.owner.fullName, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                           tags : ViewHelper.getCommaSeperatedFromList(it.tags), id: it.id, numOfExecutions: it.numOfExecutions, status: status, actions: it.workflowState?.reportActionsAsList?.join(","), actionItemStatus: it.actionItemStatus?.key, isArchived: isArchived, isFavorite: it.isFavorite(currentUser), isMultiIngredient: it.isMultiIngredient, includeWHODrugs: it.includeWHODrugs]
        }
        return [aaData: configsMap, recordsFiltered: totalFilteredCount, recordsTotal: totalCount]
    }

    def getSharedWithUsers() {
        if (params.id) {
            ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.id)
            if (executedConfiguration) {
                def result = [users: executedConfiguration.executedDeliveryOption.sharedWith, groups: executedConfiguration.executedDeliveryOption.sharedWithGroup]
                render result as JSON
            }
        }
    }

    def getEmailToUsers() {
        if (params.id) {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params.id)
            if (executedConfiguration) {
                respond executedConfiguration.executedDeliveryOption.emailToUsers, [formats: ['json']]
            } else {
                // no such result
            }
        } else {
            // no valid id
        }
    }

    def showExecutedICSRReports() {
        User currentUser = userService.getCurrentUser()
        sanitize(params)
        prepareSortParam()

        LibraryFilter filter = new LibraryFilter(params,currentUser,ExecutedConfiguration)
        List<Long> idsForUser = ExecutedConfiguration.fetchAllBySearchStringAndStatusInList(filter).list([max: params.max, offset: params.offset, sort: params.sort, order: params.direction]).collect {
            it.first()
        }
        List<ExecutedConfiguration> periodicReportList = ExecutedConfiguration.getAll(idsForUser)
        render getExecutedConfigMaps(periodicReportList, ExecutedConfiguration.countAllBySearchStringAndStatusInList(filter).get(),
                ExecutedConfiguration.countAllBySearchStringAndStatusInList(new LibraryFilter(filter.user, null, filter.includeArchived?.toString() )).get(),
                filter.user, filter.includeArchived) as JSON
    }

    def getReportsList() {
        forSelectBox(params)
        User currentUser = userService.getUser()
        List<Long> idsForUser = ExecutedReportConfiguration.fetchAllByReportName(currentUser, params.term).list([max: params.max, offset: params.offset, sort: "reportName", order: "asc"])
        int recordsFilteredCount = ExecutedReportConfiguration.countAllByReportName(currentUser, params.term).get()
        List<ExecutedReportConfiguration> configurationList = recordsFilteredCount ? ExecutedReportConfiguration.getAll(idsForUser) : []
        render([items : configurationList.collect {
            [id: it.id, text: (it.reportName + " (version " + it.numOfExecutions + ")")]
        }, total_count: recordsFilteredCount] as JSON)
    }

    private void prepareSortParam() {
        switch (params.sort) {
            case "owner":
                params.sort = "owner.fullName"
                break
            case "version":
                params.sort = "numOfExecutions"
                break
            case "state":
                params.sort = 'workflowState.name'
        }
    }
}
