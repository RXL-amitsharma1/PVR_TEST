package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportRequestFrequencyEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.joda.time.DateTimeZone

class CalendarController implements SanitizePaginationAttributes {

    def calendarService
    def userService
    def configurationService
    def qualityService
    def CRUDService

    @Secured(['ROLE_CALENDAR'])
    def index() {}

    @Secured(['ROLE_CALENDAR'])
    def events() {
        User user = userService.currentUser
        Preference preference = user?.preference
        String userTimeZone = preference?.timeZone
        Locale locale = preference?.locale
        Date startDate = DateUtil.getStartDate(params.start.toString(), userTimeZone, locale)
        Date endDate = DateUtil.getEndDate(params.end.toString(), userTimeZone, locale)
        List<CalendarEventDTO> eventsArray = calendarService.events(user, startDate, endDate)
        render eventsArray*.toMap() as JSON
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def reports() {
        List<ReportRequestType> reportRequestTypes = ReportRequestType.findAllByAggregate(true)
        render(view: "reports", model: [reportRequestTypes: (reportRequestTypes as JSON).toString()])
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def reportsList() {

        Map result = getAllScheduledReportList()
        List reports = result.reports
        reports = reports.subList(Math.max(0, params.offset), Math.min(params.offset + params.max, reports.size()))
        render([aaData         : reports,
                recordsTotal   : result.recordsTotal,
                recordsFiltered: result.recordsFiltered] as JSON)
    }

    Map getAllScheduledReportList() {
        User currentUser = userService.getUser()
        sanitize(params)
        def tableFilter = JSON.parse(params.tableFilter) ?: [:]
        params.tableFilter = null
        String search = params.searchString
        params.searchString = null
        LibraryFilter filter = new LibraryFilter(params, currentUser, PeriodicReportConfiguration, [PeriodicReportConfiguration.class])
        filter.aggregateOnly = false
        if (!filter.manualAdvancedFilter) filter.manualAdvancedFilter = [:]
        filter.manualAdvancedFilter["running"] = true
        List reports = []
        if (params.boolean("showReportFilter")) {
            List<Long> idsForUser = ReportConfiguration.getAllIdsByFilter(filter, PeriodicReportConfiguration, false).list()
            List<PeriodicReportConfiguration> configurationList = idsForUser ? PeriodicReportConfiguration.getAll(idsForUser) : []

            List<SchedulerConfigParams> configParams = configurationList ? SchedulerConfigParams.findAllByIsDeletedAndConfigurationInList(false, configurationList) : []
            Map<String, SchedulerConfigParams> configParamsMap = configParams.collectEntries { [("" + it.configurationId + it.runDate.getTime()): it] }

            configurationList.each { PeriodicReportConfiguration config ->
                reports.addAll(getAllScheduledReports(config, params.boolean("nextOnlyFilter"), configParamsMap))
            }
        }
        if (params.boolean("showReportRequestFilter")) {
            List<ReportRequestType> reportTypes =  ReportRequestType.findAllByAggregate(true)
            List <WorkflowState> wf = WorkflowState.findAllByFinalState(true)
            List<ReportRequest> reportRequestList = ReportRequest.getReportRequestForShowReportRequestFilter(reportTypes, wf).list()

            reportRequestList.each { ReportRequest rr ->
                reports.addAll(getAllScheduledReportRequests(rr, params.boolean("nextOnlyFilter")))
            }
        }
        int total = reports.size()
        reports = filterRows(search, tableFilter, reports)
        if (params.sort) {
            reports = reports.sort { it."${params.sort}" }
            if (params.direction == "desc") reports = reports.reverse()
        }
        int recordsFiltered = reports.size()

       return [ reports         : reports,
                recordsTotal   : total,
                recordsFiltered: recordsFiltered]
    }

    boolean isMatchFullSearch(String search, Object value) {
        if (!value) return false
        String toCheck = (value instanceof Date) ? value.format(DateUtil.DATEPICKER_FORMAT) : value.toString().toLowerCase()
        return toCheck.contains(search)
    }

    private List filterRows(String search, Map filter, List rows) {
        User currentUser = userService.getUser()
        def userPreference = currentUser.preference
        List result = []
        rows.each {
            boolean match = true
            if (search) {
                List fieldsForFillSearch = ["reportName", "reportType", "product", "contributors", "destinations", "dueDate", "runDate", "startDate", "endDate"]
                String searchLow = search.toLowerCase()
                boolean fullMatch = false
                for (String field : fieldsForFillSearch) {
                    if (isMatchFullSearch(searchLow, it[field])) {
                        fullMatch = true; break
                    }
                }
                match = fullMatch
            }
            if (!match) return //go to next row
            if (filter) {
                for (String key : filter.keySet()) {
                    if (key in ['product', 'reportType', 'reportName', 'destinations']) {
                        String filterValue = filter[key]?.value?.toLowerCase()
                        if (filterValue && !it[key]?.toLowerCase()?.contains(filterValue)) {
                            match = false
                            break
                        }
                    }
                    if (key in ['runDate', 'dueDate', 'startDate', 'endDate']) {
                        Date value = it[key]
                        def value1 = filter[key]?.value1
                        def value2 = filter[key]?.value2
                        boolean ignoreTimeZone = (key == "dueDate")

                        if (value1 && value2) {
                            def dt1 = ignoreTimeZone ? DateUtil.parseDate(value1 as String, DateUtil.DATEPICKER_FORMAT) : DateUtil.parseDateWithTimeZone(value1 as String, DateUtil.DATEPICKER_FORMAT, userPreference.timeZone)
                            def dt2 = (ignoreTimeZone ? DateUtil.parseDate(value2 as String, DateUtil.DATEPICKER_FORMAT) : DateUtil.parseDateWithTimeZone(value2 as String, DateUtil.DATEPICKER_FORMAT, userPreference.timeZone)) + 1
                            if (!((dt1 <= it[key]) && (dt2 >= it[key]))) {
                                match = false; break;
                            }
                        } else if (value1) {
                            def dt1 = ignoreTimeZone ? DateUtil.parseDate(value1 as String, DateUtil.DATEPICKER_FORMAT) : DateUtil.parseDateWithTimeZone(value1 as String, DateUtil.DATEPICKER_FORMAT, userPreference.timeZone)
                            if (dt1 > it[key]) {
                                match = false; break;
                            }
                        } else if (value2) {
                            def dt2 = (ignoreTimeZone ? DateUtil.parseDate(value2 as String, DateUtil.DATEPICKER_FORMAT) : DateUtil.parseDateWithTimeZone(value2 as String, DateUtil.DATEPICKER_FORMAT, userPreference.timeZone)) + 1
                            if (dt2 < it[key]) {
                                match = false; break;
                            }
                        }
                    }
                }
            }
            if (!match) return //go to next row
            result << it
        }
        return result

    }

    private List getAllScheduledReportRequests(ReportRequest reportRequest, boolean nextOnlyFilter) {
//        User currentUser=userService.currentUser
//        if(!reportRequest.isViewableBy(currentUser)) return []
        Map reportCommon = getReportRequestMap(reportRequest)
        List result = []
        List<Date> nextRunDates = getReportRequestRunDates(reportRequest)
        if (nextOnlyFilter) {
            nextRunDates = [nextRunDates[0]]
        }
        nextRunDates.eachWithIndex { Date d, i ->
            Map report = new LinkedHashMap(reportCommon)

            report.startDate = (i == 0) ? reportRequest.reportingPeriodStart : nextRunDates[i - 1]
            report.endDate = d
            report.dueDate = d.plus(reportRequest.dueInToHa ?: 0)
            report.runDate = report.dueDate
            report.configParamsId = "" + reportRequest.id
            Set comments = reportRequest.comments
            report.comment = comments ? comments?.sort { it.id }?.last()?.reportComment : ""
            report.allComment = comments ? comments?.sort { it.id }?.collect{it.reportComment}?.join("; ") : ""
            result << report
        }
        result
    }

    List<Date> getReportRequestRunDates(ReportRequest reportRequest) {
        if (reportRequest.frequency == ReportRequestFrequencyEnum.RUN_ONCE) return [reportRequest.reportingPeriodEnd]
        List result = []
        Date current = reportRequest.reportingPeriodStart
        int X = reportRequest.frequencyX ?: 1
        for (int i = 0; i < (reportRequest.occurrences ?: 1); i++) {
            if (reportRequest.frequency == ReportRequestFrequencyEnum.HOURLY) current = new Date(current.getTime() + 1000L * 60 * 60 * X)
            else if (reportRequest.frequency == ReportRequestFrequencyEnum.DAILY) current = current.plus((i * X))
            else if (reportRequest.frequency == ReportRequestFrequencyEnum.WEEKLY) current = plusWeek(current, (i * X))
            else if (reportRequest.frequency == ReportRequestFrequencyEnum.MONTHLY) current = plusMonth(current, (i * X))
            else if (reportRequest.frequency == ReportRequestFrequencyEnum.YEARLY) current = plusMonth(current, (i * 12 * X))

            result << current
        }
        return result
    }

    private Date plusMonth(Date d, int n) {
        use(TimeCategory) {
            return d + n.months
        }
    }

    private Date plusWeek(Date d, int n) {
        use(TimeCategory) {
            return d + n.weeks
        }
    }

    private List getAllScheduledReports(PeriodicReportConfiguration config, boolean nextOnlyFilter, Map<String, SchedulerConfigParams> configParamsMap) {

        Map reportCommon = getReportMap(config)
        Date sevenYearsForward = new Date().plus(365 * 7)
        List result = []
        List<Date> nextRunDates = [config.nextRunDate]
        if(!nextOnlyFilter)
            nextRunDates.addAll(configurationService.getFutureRunDates(config, null, sevenYearsForward)?:[])
        nextRunDates.each {
            Map report = new LinkedHashMap(reportCommon)
            List startEnd = config.globalDateRangeInformation.getReportStartAndEndDateForDate(it)
            if((config.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) && (startEnd[1] < it)) startEnd[1] = it //fix for cumulative date range
            report.startDate = startEnd[0]
            report.endDate = startEnd[1]
            report.runDate = it
            if (config.gantt)
                report.dueDate = it + config.gantt.dueDays ?: 0
            else
                report.dueDate = startEnd[1] + (config.dueInDays ?: 0)
            report.dueDate = report.dueDate
            SchedulerConfigParams configParams = configParamsMap.get("" + config.id + it.getTime())
            report.configParamsId = "" + config.id + "_" + it.getTime()
            if (configParams) {
                report.configParamsId = configParams.id
                Map contributors = configParams.getContributorsAsMap()
                if (contributors.contributors) {
                    report.contributors = contributors.contributors
                    report.primaryId = contributors.primaryId
                    report.contributorsId = contributors.contributorsId
                } else {
                    report.contributors = ""
                    report.primaryId = ""
                    report.contributorsId = ""
                }
                if (configParams.comments) {
                    report.comment = configParams.comments?.sort { it.id }?.last()?.textData
                    report.allComment = configParams.comments ? configParams.comments?.sort { it.id }?.collect{it.textData}?.join("; ") : ""
                }
            }
            result << report
        }
        result
    }

    Map getReportMap(PeriodicReportConfiguration config) {
        return [
                "configId"        : config.id,
                reportName        : config.reportName,
                isPublisherReport : config.isPublisherReport,
                reportType        : ViewHelper.getMessage(config.periodicReportType.i18nKey),
                product           : ViewHelper.getDictionaryValues(config, DictionaryTypeEnum.PRODUCT),
                owner             : config.owner?.fullName,
                contributors      : config.allPublisherContributors?.collect { it.fullName }?.join(", "),
                primaryId         : config.primaryPublisherContributor?.id,
                contributorsId    : config.publisherContributors?.collect { it.id }?.join(","),
                primaryDestination: config.primaryReportingDestination,
                destinations      : config.allReportingDestinations?.join(", "),
        ]
    }

    Map getReportRequestMap(ReportRequest rr) {
        return [
                "configId"        : rr.id,
                reportName        : rr.reportName,
                isPublisherReport : false,
                isReportRequest   : true,
                reportType        : rr.reportRequestType.name,
                product           : ViewHelper.getDictionaryValues(rr, DictionaryTypeEnum.PRODUCT),
                owner             : rr.owner?.fullName,
                contributors      : rr.allPublisherContributors?.collect { it.fullName }?.join(", "),
                primaryId         : rr.primaryPublisherContributor?.id,
                contributorsId    : rr.publisherContributors?.collect { it.id }?.join(","),
                primaryDestination: rr.primaryReportingDestination,
                destinations      : rr.allReportingDestinations?.join(", "),
        ]
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def updateContributors(String configParamsId, Long primaryId) {
        SchedulerConfigParams configParams = calendarService.getSchedulerConfigParams(configParamsId, true)
        if (primaryId) configParams.primaryPublisherContributor = User.get(primaryId) else  configParams.primaryPublisherContributor = null

        configParams.publisherContributors?.collect{it}.each{
            configParams.removeFromPublisherContributors(it)
        }
        configParams.publisherContributors?.clear()
        if(params."contributorsId[]" instanceof String){
            configParams.addToPublisherContributors(User.get(params."contributorsId[]" as Long))
        }else {
            params."contributorsId[]"?.each {
                configParams.addToPublisherContributors(User.get(it as Long))
            }
        }
        CRUDService.save(configParams)
        render configParams.getContributorsAsMap() as JSON

    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def exportToExcel() {

        TimeZone timezone = TimeZone.getTimeZone(userService.currentUser?.preference?.timeZone ?: DateTimeZone.UTC.ID)
        def data = []
        Map result = getAllScheduledReportList()
        result.reports.each {
            data.add([
                    it.reportType,
                    it.product,
                    it.reportName,
                    it.startDate?.format(DateUtil.DATEPICKER_FORMAT, timezone),
                    it.endDate?.format(DateUtil.DATEPICKER_FORMAT, timezone),
                    it.dueDate?.format(DateUtil.DATEPICKER_FORMAT, timezone),
                    it.runDate?.format(DateUtil.DATEPICKER_DATE_TIME_FORMAT, timezone),
                    it.destinations,
                    it.contributors,
                    it.allComment
            ])
        }

        def metadata = [sheetName: "Configurations",
                        columns  : [
                                [title: ViewHelper.getMessage("app.label.reportType"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportSubmission.cases.productName"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportName"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.reportingPeriodStart"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.reportingPeriodEnd"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportSubmission.dueDate"), width: 25],
                                [title: ViewHelper.getMessage("app.label.runDate"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportingDestinations"), width: 25],
                                [title: ViewHelper.getMessage("app.publisher.publisherContributors"), width: 25],
                                [title: ViewHelper.getMessage("quality.capa.comments.label"), width: 25]

                        ]]
        byte[] file = qualityService.exportToExcel(data, metadata)
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: System.currentTimeMillis() + ".xlsx")
    }

}
