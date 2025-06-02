package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class ExecutedCaseSeriesRestController extends RestfulController implements SanitizePaginationAttributes {

    def userService

    ExecutedCaseSeriesRestController() {
        super(ExecutedCaseSeries)
    }

    def index() {
        sanitize(params)
        User currentUser = userService.currentUser
        String sort = params.sort ? (params.sort != "owner")? params.sort : 'owner.fullName' : 'dateCreated'
        LibraryFilter filter = new LibraryFilter(params,currentUser,ExecutedCaseSeries)
        def recordTotal = ExecutedCaseSeries.countCaseSeriesBySearchString(new LibraryFilter(currentUser, null, params.includeArchived)).get()
        List<ExecutedCaseSeries> caseSeriesList = ExecutedCaseSeries.fetchCaseSeriesBySearchString(filter).list(
                [max: params.max, offset: params.offset, sort: sort, order: params.order]).collect {
            ExecutedCaseSeries.load(it.first())
        }

        render([aaData         : transformData(caseSeriesList, currentUser),
                recordsTotal   : recordTotal,
                recordsFiltered: ExecutedCaseSeries.countCaseSeriesBySearchString(filter).get()] as JSON)
    }

    private transformData(caseSeriesList, user) {
        caseSeriesList.collect {
            [
                    id                    : it.id,
                    seriesName            : it.seriesName,
                    numExecutions         : it.numExecutions,
                    description           : it.description,
                    qualityChecked        : it.qualityChecked,
                    lastUpdated           : it.lastUpdated?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                    dateCreated           : it.dateCreated?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                    tags                  : ViewHelper.getCommaSeperatedFromList(it.tags),
                    owner                 : it.owner,
                    isArchived            : it.executedCaseSeriesStates.find { item -> item.user == user }?.isArchived,
                    isFavorite            : it.isFavorite(user),
                    associatedSpotfireFile: it.associatedSpotfireFile
            ]
        }
    }

    def getExecutedCaseSeriesList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        def items = ExecutedCaseSeries.availableByUser(userService.currentUser, term).list([max: max, offset: Math.max(page - 1, 0) * max]).collect {
            [id: it.id, text: it.seriesName + " - " + it.numExecutions, qced: it.qualityChecked, isFavorite: it.isFavorite]
        }
        render([items      : items,
                total_count: ExecutedCaseSeries.countAvailableByUser(userService.currentUser, term).get()] as JSON)
    }

    def getExecutedCaseSeriesItem() {
        def item = ExecutedCaseSeries.read(params.long("id"))
        User user=userService.currentUser
        boolean isFavorite = item?.executedCaseSeriesStates?.find{it.user.id == user.id}?.isFavorite
        render([id: item.id, text: item.seriesName + " - " + item.numExecutions, qced: item.qualityChecked, isFavorite: isFavorite] as JSON)
    }

    def getSharedWithUsers() {
        if (params.id) {
            ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(params.id)
            if (executedCaseSeries) {
                def result = [users: executedCaseSeries.executedDeliveryOption.sharedWith, groups: executedCaseSeries.executedDeliveryOption.sharedWithGroup]
                render result as JSON
            }
        }
    }
}
