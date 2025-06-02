package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.ComparisonQueue
import com.rxlogix.config.ComparisonResult
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.util.Holders
import org.springframework.security.access.annotation.Secured

@Secured(["ROLE_ADMIN"])
class ComparisonController implements SanitizePaginationAttributes {
    def comparisonService
    def userService
    def CRUDService
    def qualityService

    def index() {

        [tab: "result"]
    }

    def listQueue() {
        sanitize(params)
        ComparisonQueue.Status status = ComparisonQueue.Status.valueOf(params.status)
        List<ComparisonQueue> comparisonQueueList = ComparisonQueue.fetchBySearchString(params.searchString, status).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        def aaData = comparisonQueueList.collect {
            [id          : it.id,
             entityId1   : it.entityId1,
             entityId2   : it.entityId2,
             entityName1 : it.entityName1,
             entityName2 : it.entityName2,
             entityType  : it.entityType,
             status      : it.status.name(),
             dateCompared: it.dateCompared?.format(DateUtil.DATEPICKER_DATE_TIME_FORMAT) ?: "",
             dateCreated : it.dateCreated?.format(DateUtil.DATEPICKER_DATE_TIME_FORMAT),
             message     : it.message ?: ""]
        }
        render([aaData         : aaData, recordsTotal: ComparisonQueue.fetchBySearchString(null, status).count(),
                recordsFiltered: ComparisonQueue.fetchBySearchString(params.searchString, status).count()] as JSON)

    }

    def listResults() {
        sanitize(params)
        List<ComparisonResult> comparisonResultList = ComparisonResult.fetchBySearchString(params.searchString).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        def aaData = comparisonResultList.collect {
            [id         : it.id,
             entityId1  : it.entityId1,
             entityId2  : it.entityId2,
             entityName1: it.entityName1,
             entityName2: it.entityName2,
             result     : it.result,
             supported  : it.supported,
             message    : it.message,
             dateCreated: it.dateCreated?.format(DateUtil.DATEPICKER_DATE_TIME_FORMAT) ?: ""
            ]
        }
        render([aaData         : aaData, recordsTotal: ComparisonResult.fetchBySearchString(null).count(),
                recordsFiltered: ComparisonResult.fetchBySearchString(params.searchString).count()] as JSON)

    }

    def comparison(Long id) {
        ComparisonResult comparisonResult = ComparisonResult.get(id)
        ComparisonService.ComparisonResultDTO res = (JSON.parse(comparisonResult.data)) as ComparisonService.ComparisonResultDTO
        [result: res]
    }

    def bulkCompare() {
        String namePrefix = params.prefix ?: Holders.config.getProperty('report.comparison.prefix')
        String timeZone = userService.currentUser?.preference?.timeZone ?: "UTC"
        Date nextRunDate = new Date()
        if (params.nextRunDate) {
            try {
                nextRunDate = DateUtil.parseDateWithTimeZone(params.nextRunDate + " " + params.time, DateUtil.DATEPICKER_FORMAT + " hh:mm a", timeZone)
            } catch (Exception e) {
                flash.error = message(code: "app.comparison.params.error1")
                redirect(url: request.getHeader('referer'))
                return
            }
        }
        StringBuilder log = new StringBuilder();
        boolean errors = false
        List reportsToRun
        try {
            reportsToRun = params.ids?.split(",")?.collect {
                ExecutedReportConfiguration cfg = ExecutedReportConfiguration.get(it.trim() as Long)
                if (!cfg) {
                    log.append("Report for id = ${it} was not found \n\n")
                    errors = true
                }
                cfg
            }?.findAll { it }
        } catch (Exception e) {
            flash.error = message(code: "app.comparison.params.error2")
            redirect(url: request.getHeader('referer'))
            return
        }
        if (params.fromDate && params.toDate) {
            Date from, to
            try {
                from = DateUtil.parseDateWithTimeZone(params.fromDate + " " + params.fromTime, DateUtil.DATEPICKER_FORMAT + " hh:mm a", timeZone)
                to = DateUtil.parseDateWithTimeZone(params.toDate + " " + params.toTime, DateUtil.DATEPICKER_FORMAT + " hh:mm a", timeZone)
            } catch (Exception e) {
                //nothing required here, all reqired actions in case of exception in below code
            }
            if (from == null) {
                flash.error = message(code: "app.comparison.params.error3")
                redirect(url: request.getHeader('referer'))
                return
            }
            if (to == null) {
                flash.error = message(code: "app.comparison.params.error5")
                redirect(url: request.getHeader('referer'))
                return
            }
            if(from>to){
                flash.error = message(code: "app.comparison.params.error6")
                redirect(url: request.getHeader('referer'))
                return
            }
            reportsToRun = ExecutedReportConfiguration.findAllByDateCreatedGreaterThanAndDateCreatedLessThanAndIsDeleted(from, to, false)
        }

        reportsToRun?.each { ExecutedReportConfiguration exc ->
            if (exc) {
                try {
                    ReportConfiguration configuration = comparisonService.createCopy(exc, userService.currentUser, namePrefix, nextRunDate, true, null)
                    ComparisonQueue comparisonTask = new ComparisonQueue(
                            entityId1: exc.id,
                            entityId2: configuration.id,
                            entityName1: exc.reportName + " v" + exc.numOfExecutions,
                            entityName2: configuration.reportName,
                            entityType: exc.class.toString(),
                            status: ComparisonQueue.Status.WAITING
                    )
                    CRUDService.save(comparisonTask)
                    log.append("Configuration for id ${exc.id} successfully created")
                } catch (Exception e) {
                    log.append("Unexpecteed error occurred for report with id = ${exc.id}: ${e.getMessage()} \n\n")
                    e.printStackTrace()
                    errors = true
                }
            }
        }

        if (errors) {
            flash.warn = message(code: "app.comparison.params.error4") + "\n\n " + log.toString()
        } else {
            flash.message = message(code: "app.comparison.params.success")
        }


        redirect(url: request.getHeader('referer'))
    }

    def compare(Long id1, Long id2) {
        ExecutedReportConfiguration report1 = ExecutedReportConfiguration.get(id1)
        ExecutedReportConfiguration report2 = ExecutedReportConfiguration.get(id2)
        if (report1 && report2) {
            comparisonService.compareAndSave(report1, report2)
            flash.message = message(code: "app.comparison.success")
        } else {
            flash.error = message(code: "app.comparison.reportNotFound", args: ["" + (!report1 ? id1 : id2)])
        }
        redirect(url: request.getHeader('referer'))
    }

    def createCopy(Long id) {
        String prefix = Holders.config.getProperty('report.comparison.prefix')
        Date nextRunDate = new Date()
        ExecutedReportConfiguration exc = ExecutedReportConfiguration.get(id)
        ReportConfiguration configuration = comparisonService.createCopy(exc, userService.currentUser, prefix, nextRunDate, true, null)
        ComparisonQueue comparisonTask = new ComparisonQueue(
                entityId1: id,
                entityId2: configuration.id,
                entityName1: exc.reportName + " v" + exc.numOfExecutions,
                entityName2: configuration.reportName,
                entityType: exc.class.toString(),
                status: ComparisonQueue.Status.WAITING
        )
        CRUDService.save(comparisonTask)
        flash.message = message(code: "app.comparison.copy.success", args: [configuration.reportName]);
        redirect(url: request.getHeader('referer'))
    }

    private void addRow(Set<String> rowList, String type, String name, List headerKeys, List data) {
        rowList?.each {
            List row = []
            if (type == "DT") {
                def jsonRow = JSON.parse(it)
                headerKeys.each { key -> row << jsonRow[key] }
            } else {
                row.addAll(it.split("~@~"))
            }
            row << name
            data << row
        }
    }

    def exportToExcel(Integer id, Integer index) {
        ComparisonResult comparisonResult = ComparisonResult.get(id)
        ComparisonService.ComparisonResultDTO res = (JSON.parse(comparisonResult.data)) as ComparisonService.ComparisonResultDTO
        ComparisonService.SectionComparisonResultDTO section = res.sections[index]
        def metadata
        List data = []

        if (section.type in ["DT", "CLL"]) {

            metadata = [sheetName: section.title, columns: (section.header + ["Report"]).collect { [title: it.replaceAll("<br>", "\n"), width: 25] }]
            addRow(section.report1Row, section.type, res.reportName1, section.headerKeys, data)
            addRow(section.report2Row, section.type, res.reportName2, section.headerKeys, data)
        }
        byte[] file = qualityService.exportToExcel(data, metadata)
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: System.currentTimeMillis() + ".xlsx")
    }
}
