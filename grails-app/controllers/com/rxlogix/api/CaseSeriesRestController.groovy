package com.rxlogix.api

import com.rxlogix.CRUDService
import com.rxlogix.LibraryFilter
import com.rxlogix.config.BaseCaseSeries
import com.rxlogix.config.CaseSeries
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class CaseSeriesRestController extends RestfulController implements SanitizePaginationAttributes {

    def userService
    def caseSeriesService
    CRUDService CRUDService

    CaseSeriesRestController() {
        super(CaseSeries)
    }

    def index() {
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params,userService.getUser(),CaseSeries)
        String sort = params.sort ? (params.sort != "owner")? params.sort : 'owner.fullName' : 'dateCreated'
        def total = CaseSeries.countCaseSeriesBySearchString(new LibraryFilter(userService.getUser())).get()
        List<CaseSeries> caseSeriesList = CaseSeries.fetchCaseSeriesBySearchString(filter, sort, params.order).list([max: params.max, offset: params.offset]).collect {
            return CaseSeries.load(it.first())
        }
        List<Map> caseSeries = caseSeriesList.collect {
            [id            : it.id,
             seriesName    : it.seriesName,
             description   : it.description,
             numExecutions : it.numExecutions,
             qualityChecked: it.qualityChecked,
             lastUpdated   : it.lastUpdated,
             dateCreated   : it.dateCreated,
             tags          : ViewHelper.getCommaSeperatedFromList(it.tags),
             owner         : it.owner,
             isFavorite    : it.isFavorite(userService.currentUser)]
        }

        render([aaData: caseSeries, recordsTotal: total, recordsFiltered: CaseSeries.countCaseSeriesBySearchString(filter).get()] as JSON)
    }

    def scheduledCaseSeriesList(params, Map sharedWith) {
        sanitize(params)
        switch (params.sort) {
            case "version":
                params.sort = "numExecutions"
                break
            case "runDate":
                params.sort = "nextRunDate"
                break
            case "owner":
                params.sort = "owner.fullName"
                break
        }
        User currentUser = userService.getUser()
        List<Long> alreadyRunningConfigurationIds = CaseSeries.alreadyRunningConfigurationIds
        List<CaseSeries> caseSeriesList = CaseSeries.fetchScheduledCaseSeriesBySearchString(params.searchString, alreadyRunningConfigurationIds, currentUser, sharedWith).list([max: params.max, offset: params.offset, sort: params.sort, order: params.direction])?.collect {
            return CaseSeries.load(it.first())
        }
        render([aaData: caseSeriesMap(caseSeriesList), recordsTotal: CaseSeries.countScheduledCaseSeriesBySearchString(null, alreadyRunningConfigurationIds, currentUser, sharedWith).get(), recordsFiltered: CaseSeries.countScheduledCaseSeriesBySearchString(params.searchString, alreadyRunningConfigurationIds, currentUser, sharedWith).get()] as JSON)
    }

    private caseSeriesMap(List<CaseSeries> caseSerieses) {
        def caseSeriesList = []
        caseSerieses.each {
            caseSeriesList += [ id             : it.id,
                                seriesName     : it.seriesName,
                                version        : it.numExecutions + 1,
                                frequency      : caseSeriesService.calculateFrequency(it)?.name(),
                                runDate        : it.nextRunDate,
                                owner          : it.owner.fullName,
                                executionStatus: ReportExecutionStatusEnum.SCHEDULED.value(),
                                errorMessage   : "",
                                errorTitle     : "",
                                sharedWith     : it?.allSharedUsers?.unique()?.fullName,
                                deliveryMedia  : it?.deliveryOption?.attachmentFormats?.displayName?.join(", "),
                                dateCreated    : it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                exeutionStId   : 0
            ]
        }
        return caseSeriesList
    }

    def unschedule() {
        Long id = params.id as Long
        try {
            if (id) {
                CaseSeries caseSeries = CaseSeries.get(id)
                if (caseSeries.isEditableBy(userService.currentUser)) {
                    if (!caseSeries.executing) {
                        caseSeries.setIsEnabled(false)
                        CRUDService.update(caseSeries)
                    } else {
                        response.status = 406
                        Map responseMap = [
                                message: message(code: "app.configuration.unscheduled.fail", args: [caseSeries.reportName]),
                                status: 406
                        ]
                        render(contentType: "application/json", responseMap as JSON)
                        return
                    }
                } else {
                    response.status = 401
                    Map responseMap = [
                            message: message(code: "app.configuration.edit.permission", args: [caseSeries.reportName]),
                            status: 401
                    ]
                    render(contentType: "application/json", responseMap as JSON)
                    return
                }
            }
            render([success: true] as JSON)
        } catch (Exception ex) {
            log.error("UnKnown Error occurred while unscheduling configuration for: ${id} ",ex)
            response.status = 500
            Map responseMap = [
                    message: message(code: "default.server.error.message"),
                    status: 500
            ]
            render(contentType: "application/json", responseMap as JSON)
        }
    }
}
