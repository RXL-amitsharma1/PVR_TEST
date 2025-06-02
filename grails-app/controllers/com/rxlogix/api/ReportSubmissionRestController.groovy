package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.SubmittedCaseDTO
import com.rxlogix.config.Capa8D
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ReportSubmission
import com.rxlogix.config.SubmissionAttachment
import com.rxlogix.config.UnitConfiguration
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import grails.core.GrailsApplication

@Secured('permitAll')
class ReportSubmissionRestController extends RestfulController implements SanitizePaginationAttributes {
    def userService
    def reportExecutorService
    GrailsApplication grailsApplication

    ReportSubmissionRestController() {
        super(ReportSubmission)
    }

    def index() {

        User currentUser = userService.getUser()
        sanitize(params)
        ReportSubmissionStatusEnum status = params.status ? ReportSubmissionStatusEnum.valueOf(params.status) : null
        String caseSeriesSearch = params.caseSeriesSearch?.trim()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, currentUser,ReportSubmission)
        List<Long> executedIdList = null
        if (caseSeriesSearch) {
            executedIdList = reportExecutorService.getSubmissionsByCaseNumber(caseSeriesSearch)?:[0L]
        }

        List<Long> reportSubmissionIdList = ReportSubmission.fetchReportSubmissionBySearchString(filter, status, executedIdList, params.boolean('icsr'), params.sort as String, params.order as String).list([max: params.max, offset: params.offset]).collect { it.first() }
        List<ReportSubmission> filteredReportSubmissionList = ReportSubmission.getAll(reportSubmissionIdList)
        def filteredReportSubmissionListMap = filteredReportSubmissionList.collect {
            String pvrDateRangeStart = it.executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeStartAbsolute?.format(DateUtil.DATEPICKER_UTC_FORMAT)
            String pvrDateRangeEnd = it.executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute?.format(DateUtil.DATEPICKER_UTC_FORMAT)
            String reportType = message(message: it.executedReportConfiguration.periodicReportType)
            Boolean isInDraftMode = false
            if (it.executedReportConfiguration.finalLastRunDate == null) {
                isInDraftMode = true
            }
            [id: it.id, exConfigId: it.executedReportConfiguration.id,
             reportType: reportType,
             isPublisherReport: it.executedReportConfiguration.isPublisherReport,
             productSelection: ViewHelper.getDictionaryValues(it.executedReportConfiguration, DictionaryTypeEnum.PRODUCT),
             reportName: it.executedReportConfiguration.reportName,
             pvrDateRangeStart: pvrDateRangeStart,
             pvrDateRangeEnd:pvrDateRangeEnd,
             modifiedBy:it.modifiedBy,
             lastUpdated:it.lastUpdated,
             reportingDestination: it.reportingDestination,
             reportSubmissionStatus: it.reportSubmissionStatus.key,
             submissionDate: it.submissionDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
             dueDate: it.dueDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
             isPrimaryDestination: it.isPrimary,
             late: [isInTime:(grailsApplication.config.submissions.late.find{ val-> val.name == it.late}?.type == "NOT_LATE"),  late:it.late, lateReasons:it.lateReasons?.sort{ r-> !r.isPrimary}],
             capa  : Capa8D.findBySubmission(it)?.id,
             attacments: it.attachments?.collect{a->[id:a.id,name:a.name]},
             license: ViewHelper.getLicenses(it.executedReportConfiguration),
             isInDraftMode: isInDraftMode]
        }
        Integer totalRecords = ReportSubmission.fetchReportSubmissionBySearchString(new LibraryFilter(userService.getUser()), null, null,params.boolean('icsr')).list()?.size()

        render([aaData: filteredReportSubmissionListMap, recordsFiltered: ReportSubmission.fetchReportSubmissionBySearchString(filter, status, executedIdList, params.boolean('icsr')).list()?.size(), recordsTotal: totalRecords] as JSON)
    }

    def casesList(ReportSubmission reportSubmission){
        sanitize(params)
        params.sort == "dateCreated" ? params.sort = "caseNumber" : params.sort
        List resultQuery = reportExecutorService.getSubmittedCases(reportSubmission, params.offset + 1, params.max, params.sort, params.order, params.searchString)
        List<SubmittedCaseDTO> casesList = resultQuery.last()
        Integer filteredCount = resultQuery.first()
        Integer total = resultQuery.get(1)

        render([aaData : casesList.collect {
            it.asMap()
        }, recordsTotal: total, recordsFiltered: filteredCount] as JSON)
    }

    def getEmails() {
        if (!params."destinations[]") {
            render([emails: [], templates: []] as JSON)
            return
        }
        List destinations = []
        if (params."destinations[]" instanceof String)
            destinations = [params."destinations[]"]
        else
            destinations.addAll(params."destinations[]")
        List<UnitConfiguration> unitConfigurations = UnitConfiguration.findAllByUnitNameInList(destinations)
        List emailTemplates = unitConfigurations?.collect { it.emailTemplate?.toContentMap() }?.findAll()?.unique { it.id } ?: []
        render([emails: unitConfigurations?.collect { it.email }?.findAll(), templates: emailTemplates] as JSON)
    }
}