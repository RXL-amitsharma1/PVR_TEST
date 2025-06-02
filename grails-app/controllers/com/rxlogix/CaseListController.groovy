package com.rxlogix


import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.commandObjects.CaseCommand
import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import net.sf.dynamicreports.report.exception.DRException
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.governors.MaxPagesGovernorException
import net.sf.jasperreports.governors.TimeoutGovernorException
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class CaseListController implements SanitizePaginationAttributes {

    def reportExecutorService
    def dynamicReportService
    def userService
    def importService
    def CRUDService
    def notificationService
    GrailsApplication grailsApplication
    def caseSeriesService

    def index(boolean cumulativeType) {
        ExecutedCaseSeries caseSeries = params.cid ? ExecutedCaseSeries.get(params.long('cid')) : null
        ExecutedReportConfiguration executedReportConfiguration = params.id ? ExecutedReportConfiguration.read(params.long('id')) : null
        if (!caseSeries) {
            caseSeries = cumulativeType ? (executedReportConfiguration?.cumulativeCaseSeries) : (executedReportConfiguration?.caseSeries)
        }
        if(!caseSeries){
            flash.error = message(code: "app.caseList.not.available", args: [params.reportName])
            redirect(controller: 'executedCaseSeries', action: "index")
            return
        }

        if (!caseSeries.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(controller: 'executedCaseSeries', action: "index")
            return
        }

        notificationService.deleteNotificationByExecutionStatusId(userService.getCurrentUser(), caseSeries.id, NotificationApp.CASESERIES)
        if (caseSeries.executing) {
            redirect(controller: 'executedCaseSeries', action: 'show', id: caseSeries.id)
            return
        }
        executedReportConfiguration = caseSeries.findAssociatedConfiguration()
        if (!cumulativeType && caseSeries.id == executedReportConfiguration?.cumulativeCaseSeriesId) {
            cumulativeType = true
        }
        ExecutedTemplateQuery sourceSection = null
        if (params.parentId) {
            sourceSection = ReportResult.read(params.long('parentId'))?.executedTemplateQuery
        }
        if (params.detailed && grailsApplication.config.getProperty("caseSeries.list.detailed.enabled", Boolean)) {
            render view: 'detailed', model: [executedReportConfiguration: executedReportConfiguration, caseSeries: caseSeries,
                                             cumulativeType             : cumulativeType, name: caseSeries.seriesName, id: caseSeries.id,
                                             canAddEditCases            : executedReportConfiguration ? executedReportConfiguration.canAddEditCases() : true,
                                             cumulativeCaseSeries       : executedReportConfiguration?.cumulativeCaseSeries, showVersionColumn: ApplicationSettings.first().defaultUiSettings ? "false" : "true", sourceSection: sourceSection]
            return
        }

        [executedReportConfiguration: executedReportConfiguration, caseSeries: caseSeries,
         cumulativeType             : cumulativeType, name: caseSeries.seriesName, id: caseSeries.id,
         canAddEditCases            : executedReportConfiguration ? executedReportConfiguration.canAddEditCases() : true,
         cumulativeCaseSeries       : executedReportConfiguration?.cumulativeCaseSeries, showVersionColumn: ApplicationSettings.first().defaultUiSettings ? "false" : "true", sourceSection: sourceSection]
    }

    def list(Long id) {
        ExecutedCaseSeries caseSeries = ExecutedCaseSeries.read(id)
        if (!caseSeries) {
            notFound()
            return
        }

        if (!caseSeries.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(controller: 'executedCaseSeries', action: "index")
            return
        }

        if (!params.outputFormat) {
            params.outputFormat = ReportFormatEnum.HTML.name()
        }

        if (params.outputFormat != ReportFormatEnum.HTML.name()) {
            params.isTemporary = caseSeries.isTemporary
            String fileDownloadName = FileNameCleaner.cleanFileName(caseSeries.seriesName)
            fileDownloadName = dynamicReportService.truncateFileName(fileDownloadName , Constants.MAX_OFFICE_FILE_NAME_LENGTH)
            switch (params.caseListType) {
                case "openCaseList": fileDownloadName = fileDownloadName.concat("-open")
                    break
                case "removedCaseList": fileDownloadName = fileDownloadName.concat("-removed")
                    break
            }
            File reportFile = null
            try {
                reportFile = dynamicReportService.createCaseListReport(caseSeries, params)
                GrailsWebRequest webRequest =
                        (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
                webRequest.setRenderView(false)
                MultipartFileSender.renderFile(reportFile, fileDownloadName, params.outputFormat as String, dynamicReportService.getContentType(params.outputFormat), request, response, false)
                fileDownloadName = fileDownloadName.replaceAll(" ", "_") + "." + params.outputFormat
                AuditLogConfigUtil.logChanges(caseSeries, [outputFormat: params.outputFormat, fileName: fileDownloadName, exportedDate: new Date()],
                        [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(params.outputFormat).displayName))
            } catch (DRException e) {
                log.error(e.message)
                if (e.cause instanceof MaxPagesGovernorException) {
                    flash.error = message(code: "app.report.maxPages", args: [e.cause.maxPages])
                    redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
                } else if (e.cause instanceof TimeoutGovernorException) {
                    flash.error = message(code: "app.report.timeout", args: [e.cause.timeout])
                    redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
                } else if (e.getMessage().contains("components reaches outside available width")) {
                    flash.error = message(code: "app.report.maxWidth")
                    redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
                } else if (e.getMessage().contains("components reaches outside available height")) {
                    flash.error = message(code: "app.report.maxHeight")
                    redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
                } else if (e.cause instanceof JRException && e.getMessage().contains("Infinite loop creating new page")) {
                    flash.error = message(code: "app.report.maxColumns")
                    redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
                } else {
                    log.error("Unexpected error", e)
                    flash.error = message(code: "default.server.error.message")
                    redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
                }
            } catch (IOException e) {
                log.warn("IOException occurred while rendering file ${reportFile.name}. Error: ${e.getMessage()}")
                flash.error = message(code: "default.server.error.message")
                redirect(controller: 'caseList', action: "index", params: [cid: caseSeries.id])
            }

        } else {
            sanitize(params)
            if(params.detailed){
                params.sort == "dateCreated" ? params.sort = "CASE_NUM" : params.sort
                Map searchString = params.searchString ? (MiscUtil.parseJsonText(params.searchString) as Map) : [:]
                List resultQuery = reportExecutorService.getDetailedCaseOfSeries(id, params.offset + 1, params.max, params.sort, params.order, searchString, caseSeries.caseSeriesOwner)
                List casesList = resultQuery.last()
                Integer total = resultQuery.get(1)
                Integer filteredCount = resultQuery.first()
                render([aaData : casesList, recordsTotal: total, recordsFiltered: filteredCount] as JSON)
                return
            }
            params.sort == "dateCreated" ? params.sort = "caseNumber" : params.sort
            List resultQuery = reportExecutorService.getCaseOfSeries(id, params.offset + 1, params.max, params.sort, params.order, params.searchString, caseSeries.caseSeriesOwner)
            List<CaseDTO> casesList = resultQuery.last()
            Integer total = resultQuery.get(1)
            Integer filteredCount = resultQuery.first()
            render([aaData : casesList.collect {
                it.asMap()
            }, recordsTotal: total, recordsFiltered: filteredCount] as JSON)
        }
    }

    def openCasesList(ExecutedCaseSeries  caseSeries){
        if(!caseSeries) {
            notFound()
            return
        }
        List<CaseDTO> casesList = reportExecutorService.getOpenCaseOfSeries(caseSeries.id, caseSeries.caseSeriesOwner)
        respond casesList.collect {
            it.asMap()
        }, [formats: ['json']]
    }

    def removedCasesList(ExecutedCaseSeries  caseSeries){
        if(!caseSeries) {
            notFound()
            return
        }
        List<CaseDTO> casesList = reportExecutorService.getRemovedCaseOfSeries(caseSeries.id, caseSeries.caseSeriesOwner)
        respond casesList.collect {
            it.asMap()
        }, [formats: ['json']]
    }


    def refreshCaseList(ExecutedCaseSeries caseSeries) {
        if (caseSeries) {
            if (!caseSeries.isViewableBy(userService.currentUser)) {
                flash.warn = message(code: "app.warn.noPermission")
                redirect(controller: 'executedCaseSeries', action: "index")
                return
            }
            ExecutedCaseSeries cumulativeCaseSeries = null
            ExecutedPeriodicReportConfiguration exPRConfig = (ExecutedPeriodicReportConfiguration) caseSeries.findAssociatedConfiguration()
            if (exPRConfig?.caseSeries && exPRConfig?.cumulativeCaseSeries) {
                cumulativeCaseSeries = (caseSeries.id == exPRConfig?.caseSeries?.id) ? exPRConfig?.cumulativeCaseSeries : exPRConfig?.caseSeries
            }

            if (caseSeries.executing || cumulativeCaseSeries?.executing) {
                redirect(controller: 'executedCaseSeries', action: 'show', id: caseSeries.executing ? caseSeries.id : cumulativeCaseSeries?.id)
                return
            }
            setExecutionStatus(caseSeries)
            if (cumulativeCaseSeries) {
                setExecutionStatus(cumulativeCaseSeries)
            }
            redirect(controller: 'executionStatus', action: "list")
            return
        }
        flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.case.series'), params.id])
        redirect(controller: 'executedCaseSeries', action: 'index')
    }

    @Secured(['ROLE_CASE_SERIES_EDIT'])
    def addCaseToList(CaseCommand caseCommand) {
        if (!caseCommand.validate()) {
            log.warn(caseCommand.errors.allErrors?.toString())
            sendResponse(500, message(code: "app.error.fill.all.required").toString())
            return
        }
        try {
            String description = ""
            if (caseCommand.file) {
                Set<String> caseNumberAndVersion = importService.readFromExcel(caseCommand.file)
                if(caseNumberAndVersion){
                    Set warnings  = reportExecutorService.saveCaseSeriesInDB(caseNumberAndVersion, caseCommand.executedCaseSeries, caseCommand.justification)
                    String delimiter = grailsApplication.config.caseSeries.bulk.addCase.delimiter
                    Set valid = (caseNumberAndVersion.collect { it.toUpperCase().split(delimiter)[0] } - warnings)
                    description = g.message(code: "auditLog.add.cases", args: [caseCommand.executedCaseSeries.seriesName,valid.join(', ')])
                    sendResponse(200, message(code: "caseCommand.import.caseNumber.success",args: [caseNumberAndVersion.size(),valid?.size(),warnings?.size()]).toString())
                } else{
                    sendResponse(500, message(code: "app.label.no.data.excel.error").toString())
                }
            } else {
                if(caseCommand.versionNumber && !caseCommand.versionNumber.matches("^[0-9]*\$")) {
                    response.status = 500
                    sendResponse(500, message(code: "caseCommand.invalid.versionNumber").toString())
                    return
                } else {
                    Set<String> warnings = reportExecutorService.addCaseToGeneratedList(caseCommand, userService.currentUser)
                    if(warnings){
                        response.status = 500
                        sendResponse(500, message(code: "caseCommand.${warnings.first()}").toString())
                        return
                    } else {
                        description = g.message(code: "auditLog.add.cases", args: [caseCommand.executedCaseSeries.seriesName,"${caseCommand.versionNumber?(caseCommand.caseNumber+'-'+caseCommand.versionNumber):caseCommand.caseNumber}"])
                        sendResponse(200, message(code: "caseCommand.add.caseNumber.success").toString())
                    }
                }
            }
            dynamicReportService.deleteAllCaseSeriesCachedFile(caseCommand.executedCaseSeries)
            auditLogCases(caseCommand.executedCaseSeries, description, caseCommand.justification)
        }  catch (Exception ex) {
            log.error("Unexpected error in caseList -> addCaseToList", ex)
            sendResponse(500, message(code: "default.server.error.message").toString())
        }
    }
    @Secured(['ROLE_CASE_SERIES_EDIT'])
    def removeCaseFromList(Long cid) {
        List<CaseCommand> caseCommandList = []
        ExecutedCaseSeries caseSeries = ExecutedCaseSeries.get(cid)
        String justification = ""
        request.JSON.each {
            caseCommandList.add(new CaseCommand(caseNumber: it.caseNumber, versionNumber: it.versionNumber,justification:it.justification, executedCaseSeries: caseSeries))
            justification = it.justification
        }
        if (caseCommandList.find { !it.validate() }) {
            sendResponse(500, message(code: "app.error.fill.all.required").toString())
            return
        }
        try {
            reportExecutorService.removeCaseFromGeneratedList(caseCommandList, userService.currentUser, caseSeries)
            String description = g.message(code: "auditLog.remove.cases", args: [caseSeries.seriesName, caseCommandList.collect {it.versionNumber?(it.caseNumber+'-'+it.versionNumber):it.caseNumber}.join(", ")])
            dynamicReportService.deleteAllCaseSeriesCachedFile(caseSeries)
            auditLogCases(caseSeries, description, justification)
            flash.message = message(code: 'caseCommand.remove.caseNumbers.success');
            render([success: true, message: message(code: 'caseCommand.remove.caseNumbers.success')] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected Error in caseList -> removeCaseFromList", ex)
            sendResponse(500, message(code: "default.server.error.message").toString())
        }
    }
    @Secured(['ROLE_CASE_SERIES_EDIT'])
    def moveCasesToList(Long cid) {
        List<CaseCommand> caseCommandList = []
        ExecutedCaseSeries caseSeries = ExecutedCaseSeries.get(cid)
        request.JSON.each {
            caseCommandList.add(new CaseCommand(caseNumber: it.caseNumber, versionNumber: it.versionNumber,justification:it.justification, executedCaseSeries: caseSeries))
        }
        if (caseCommandList.find { !it.validate() }) {
            sendResponse(500, message(code: "app.error.fill.all.required").toString())
            return
        }
        try {
            User user = userService.currentUser
            caseCommandList.each {CaseCommand caseCommand ->
                reportExecutorService.addCaseToGeneratedList(caseCommand, user)
            }
            dynamicReportService.deleteAllCaseSeriesCachedFile(caseSeries,true)
            flash.message = message(code: 'caseCommand.removedToAdd.caseNumbers.success');
            render([success: true, message: message(code: 'caseCommand.removedToAdd.caseNumbers.success')] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected Error in caseList -> moveCasesToList", ex)
            sendResponse(500, message(code: "default.server.error.message").toString())
        }
    }

    def updateTags(Long cid) {
        ExecutedCaseSeries caseSeries = ExecutedCaseSeries.get(cid)
        Long caseNumber = Long.parseLong(params.caseNumber)
        try {
            reportExecutorService.saveTags(params.caseLevelTags,params.globalTags, caseNumber,cid, caseSeries.caseSeriesOwner)
            dynamicReportService.deleteAllCaseSeriesCachedFile(caseSeries,true)
            auditLogTagsForCase(caseSeries,"Updated Tags for case: $params.caseNumberValue", params.caseLevelTags, params.globalTags, params.oldCaseLevelTags, params.oldGlobalTags)
            flash.message = message(code: 'caseCommand.add.tags.success')
            render([success: true, message: message(code: 'caseCommand.add.tags.success')] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected error in caseList -> updateTags", ex)
            sendResponse(500, message(code: "default.server.error.message").toString())
        }
    }

    def updateCommentToCaseNumber() {
        Long caseNumberUniqueId = Long.parseLong(params.caseNumberUniqueId)
        String comments = params.comments
        String caseNumber = params.caseNumber
        String oldComment = params.oldComment
        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(params.long('executedCaseSeries'))
        if (!(caseNumberUniqueId && comments)) {
            sendResponse(500, message(code: "app.error.fill.all.required").toString())
            return
        }
        try {
            reportExecutorService.saveCaseComment(caseNumberUniqueId, comments);
            dynamicReportService.deleteAllCaseSeriesCachedFile(executedCaseSeries,true)
            auditLogCommentsForCase(executedCaseSeries,"Updated Comments for case: $caseNumber", comments, oldComment)
            flash.message = message(code: 'caseCommand.add.comment.success');
            render([success: true, message: message(code: 'caseCommand.add.comment.success')] as JSON)
        } catch (Exception ex) {
            log.error("Unexpected error in caseList -> updateCommentToCaseNumber", ex)
            sendResponse(500, message(code: "default.server.error.message").toString())
        }
    }

    def fetchAllTags() {
        forSelectBox(params)
        List<PvrTags> caseLevelTags = []
        int total = 0
        PvrTags.withNewSession {
            caseLevelTags = caseSeriesService.getAllTags(params)
            total = PvrTags.countByType(params.type)
        }
        render ([items: caseLevelTags.collect {[id: it.name, text:it.name]},total_count: total] as JSON)
    }

    //Method to prepare the response.
    private def sendResponse(int stat, String msg) {
        response.status = stat
        Map responseMap = [
                message: msg,
                status: stat
        ]
        render(contentType: "application/json", responseMap as JSON)
    }

    void auditLogCases(ExecutedCaseSeries executedCaseSeries, String description, String justification) {
        AuditLogConfigUtil.logChanges(executedCaseSeries, ["Event": description, "Justification": justification], [:], Constants.AUDIT_LOG_UPDATE)
    }

    void auditLogTagsForCase(ExecutedCaseSeries executedCaseSeries, String description, String caseLevelTags, String globalTags, String oldCaseLevelTags,String oldGlobalTags) {
        AuditLogConfigUtil.logChanges(executedCaseSeries, ["Event": description, caseLevelTags:caseLevelTags, globalLevelTags:globalTags], [caseLevelTags:oldCaseLevelTags, globalLevelTags:oldGlobalTags], Constants.AUDIT_LOG_UPDATE)
    }

    void auditLogCommentsForCase(ExecutedCaseSeries executedCaseSeries, String description, String comments, String oldComment) {
        AuditLogConfigUtil.logChanges(executedCaseSeries, ["Event": description, comments:comments], [comments:oldComment], Constants.AUDIT_LOG_UPDATE)
    }

    private void setExecutionStatus(ExecutedCaseSeries caseSeries) {
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: caseSeries.id, entityType: ExecutionStatus.getEntityTypeFromClass(caseSeries.class), reportVersion: caseSeries.numExecutions,
                startTime: System.currentTimeMillis(), owner: caseSeries.owner, reportName: caseSeries.seriesName,
                attachmentFormats: caseSeries.executedDeliveryOption?.attachmentFormats, sharedWith: caseSeries?.allSharedUsers, tenantId: caseSeries?.tenantId)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.frequency = FrequencyEnum.RUN_ONCE
        executionStatus.nextRunDate = new Date()
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
        flash.message = message(code: 'app.refreshCaseSeries.progress')
        auditLogCases(caseSeries, g.message(code: "auditLog.refresh.caseseries", args: [caseSeries.seriesName]), "")
    }

    private notFound(String errorCode='app.executedPeriodicReportConfiguration.label') {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: errorCode), params.id])
                redirect controller: 'caseSeries', action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

}
