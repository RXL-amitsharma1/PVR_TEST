package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.config.ExecutedInboundCompliance
import com.rxlogix.config.InboundCompliance
import com.rxlogix.config.ResultInboundCompliance
import com.rxlogix.config.SuperQuery
import com.rxlogix.enums.ExecutionStatusConfigTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class InboundComplianceRestController extends RestfulController implements SanitizePaginationAttributes{

    def userService
    def inboundComplianceService

    InboundComplianceRestController() {
        super(InboundCompliance)
    }

    def list() {
        sanitize(params)
        def inboundComplianceNameQuery = InboundCompliance.getAllInboundComplianceBySearchString(params.searchString)
        List inboundComplianceIdsList = inboundComplianceNameQuery.list([max: params.max, offset: params.offset, sort: params.sort=='owner.fullName'?'ownerFullName':params.sort, order: params.order]).collect {it[0]}
        List<InboundCompliance>inboundComplianceList = InboundCompliance.getAll(inboundComplianceIdsList)
        List<Map> inboundConfigurations = inboundComplianceList.collect{
            List<String> tagNames= it.tags.collect { tag -> tag.name }
            [id:it.id, senderName:it.senderName, description:it.description, tags: tagNames, qualityChecked:it.qualityChecked, owner: it.owner, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), lastUpdated: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT), createdBy: it.owner.fullName]
        }
        render([aaData: inboundConfigurations, recordsTotal: InboundCompliance.getAllInboundComplianceBySearchString(null).count(), recordsFiltered: InboundCompliance.countAllInboundComplianceBySearch(params.searchString).get()] as JSON)
    }

    def generatedList() {
        User currentUser = userService.getCurrentUser()
        sanitize(params)
        params.sort = (params.sort == "owner") ? "owner.fullName" : params.sort
        LibraryFilter filter = new LibraryFilter(params,currentUser, ExecutedInboundCompliance)

        showExecutedReports(filter, params.max, params.offset, params.order, params.sort)
    }

    private void showExecutedReports(LibraryFilter filter, int max, int offset, String direction, String sort) {
        List<Long> idsForUser = ExecutedInboundCompliance.fetchAllBySearchStringAndStatusInList(filter).list([max: max, offset: offset, sort: sort, order: direction]).collect {
            it.first()
        }
        List<ExecutedInboundCompliance> reportList = ExecutedInboundCompliance.getAll(idsForUser)

        render getExecutedConfigMaps(reportList, ExecutedInboundCompliance.countAllBySearchStringAndStatusInList(filter).get(),
                ExecutedInboundCompliance.countAllBySearchStringAndStatusInList(new LibraryFilter(filter.user, null, filter.includeArchived?.toString() )).get(),
                filter.user, filter.includeArchived) as JSON
    }

    private getExecutedConfigMaps(List<ExecutedInboundCompliance> executedConfiguration, Long totalFilteredCount, Long totalCount, User currentUser, Boolean includeArchived) {
        def configsMap = []
        executedConfiguration.each {
            configsMap += [senderName: it.senderName, description: it.description, owner: it.owner.fullName, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), inboundComplianceId: it.inboundCompliance?.id,
                           tags : ViewHelper.getCommaSeperatedFromList(it.tags), id: it.id]
        }
        return [aaData: configsMap, recordsFiltered: totalFilteredCount, recordsTotal: totalCount]
    }

    def result(Long id) {
        sanitize(params)
        def inboundComplianceNameQuery = ResultInboundCompliance.getAllResultByIdAndBySearchString(id, params.searchString)
        List<ResultInboundCompliance> resultInboundComplianceList = inboundComplianceNameQuery.list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        List<Map> resultInboundCompliances = resultInboundComplianceList.collect{
            [
                    tenantId                : it.tenantId,
                    senderName              : it.senderName,
                    senderReceiptDate       : it.senderReceiptDate,
                    safetyRecieptDate       : it.safetyRecieptDate,
                    caseCreationDate        : it.caseCreationDate,
                    daysToProcess           : it.daysToProcess,
                    ruleId                  : it.ruleId,
                    queryName               : SuperQuery.get(it.ruleId)?.name?:"N/A",
                    status                  : it.status,
                    caseNum                 : it.caseNum,
                    criteriaName            : it.criteriaName?:"N/A",
                    exInboundComplianceId   : it.exInboundComplianceId,
                    versionNum              : it.versionNum
            ]
        }
        render([aaData: resultInboundCompliances, recordsTotal: ResultInboundCompliance.getAllResultByIdAndBySearchString(id, null).count(), recordsFiltered: inboundComplianceNameQuery.count()] as JSON)
    }

    def executionStatus(String status){
        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, currentUser, ExecutedInboundCompliance)
        switch (params.sort) {
            case "executionTime":
                params.sort = "runDuration"
                break
            case "owner":
                params.sort = "owner.fullName"
                break
            case "runDate":
                params.sort = "inboundCompliance.lastRunDate"
                break
            case "version":
                params.sort = "inboundCompliance.version"
                break
        }
        switch (status) {
            case ReportExecutionStatusEnum.COMPLETED.getKey():
                showExecutionCompleted(filter, params.max, params.offset, params.order, params.sort)
                break
            case ReportExecutionStatusEnum.ERROR.getKey():
                showExecutionError(filter, params.max, params.offset, params.order, params.sort)
                break
            default:
                showExecutionInProgress(filter, params.max, params.offset, params.order, params.sort)
        }
    }

    private void showExecutionInProgress(LibraryFilter filter, int max, int offset, String direction, String sort){
        List<Long> idsForUser = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndInProgressStatus(filter).list([max: max, offset: offset, sort: sort, order: direction]).collect {
            it.first()
        }
        List<ExecutedInboundCompliance> executedInboundComplianceList = idsForUser.collect { ExecutedInboundCompliance.read(it) }

        int recordsTotal = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndInProgressStatus(filter).count()
        int recordsFilteredCount = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndInProgressStatus(filter).count()

        render([aaData: inboundComplianceMap(executedInboundComplianceList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private void showExecutionCompleted(LibraryFilter filter, int max, int offset, String direction, String sort){
        List<Long> inboundComplianceIdsList = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndCompletedStatus(filter).list([max: max, offset: offset, sort: sort, order: direction]).collect {
            it.first()
        }
        List<ExecutedInboundCompliance> executedInboundComplianceList = inboundComplianceIdsList.collect { ExecutedInboundCompliance.read(it) }

        int recordsTotal = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndCompletedStatus(filter).count()
        int recordsFilteredCount = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndCompletedStatus(filter).count()
        render([aaData: inboundComplianceMap(executedInboundComplianceList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private void showExecutionError(LibraryFilter filter, int max, int offset, String direction, String sort){
        List<Long> inboundComplianceIdsList = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndErrorStatus(filter).list([max: max, offset: offset, sort: sort, order: direction]).collect {
            it.first()
        }
        List<ExecutedInboundCompliance> executedInboundComplianceList = inboundComplianceIdsList.collect { ExecutedInboundCompliance.read(it) }

        int recordsTotal = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndErrorStatus(filter).count()
        int recordsFilteredCount = ExecutedInboundCompliance.fetchAllInboundBySearchStringAndErrorStatus(filter).count()
        render([aaData: inboundComplianceMap(executedInboundComplianceList), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    private inboundComplianceMap(List<ExecutedInboundCompliance> executedInboundComplianceList){
        def inboundComplianceList = []
        executedInboundComplianceList.each{
            inboundComplianceList += [
                                        id             : it.id,
                                        senderName     : it.senderName,
                                        version        : it.numOfICExecutions,
                                        lastUpdated    : it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                        owner          : it.owner.fullName,
                                        runDate        : it.inboundCompliance.lastRunDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                        executionTime  : it.getRunDuration(),
                                        executionStatus: it.status.value(),
                                        message        : it.errorDetails,
                                        messageTitle   : it.message,
                                        inboundComplianceId: it.inboundCompliance.getId()
                    ]
        }
        inboundComplianceList
    }
}
