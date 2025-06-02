package com.rxlogix.config


import com.google.gson.Gson
import com.rxlogix.ChartOptionsUtils
import com.rxlogix.Constants
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.reportTypes.CrosstabReportBuilder
import com.rxlogix.dynamicReports.reportTypes.crosstab.HeaderTabDTO
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReasonOfDelayFieldEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JsonDataSource
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.web.multipart.MultipartFile

import javax.transaction.Transactional
import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class AdvancedReportViewerController {

    def templateService
    def reportExecutorService
    def CRUDService
    def userService
    def qualityService
    def sqlGenerationService
    def lockObjectService
    def workflowService
    def dashboardService
    def notificationService
    def dynamicReportService
    def commentService

    final static Integer BULK_UPDATE_MAX_ROWS = 500

    def view(long id) {
        ReportResult reportResult = ReportResult.get(id)
        if (reportResult.parentId) //that means it is CLL drilldown
            forward action: 'viewCll', id: id
        else if (reportResult.getExecutedTemplateQuery().usedTemplate instanceof ExecutedDataTabulationTemplate)
            forward action: 'viewTab', id: id
        else if (reportResult.getExecutedTemplateQuery().usedTemplate instanceof ExecutedCustomSQLTemplate)
            forward action: 'viewCustomSql', id: id
        else if (reportResult.getExecutedTemplateQuery().usedTemplate instanceof ExecutedNonCaseSQLTemplate)
            forward action: 'viewNonCaseSql', id: id
        else
            forward action: 'viewCll', id: id
    }

    def viewWidget(long id) {
        Map data = dashboardService.getReportWidgetDate(id)
        if (!data || !data.reportResultId) {
            render message(code: "app.label.widget.noData")
            return
        }
        forward action: 'view', id: data.reportResultId
    }

    def viewTab(long id) {
        User currentUser = userService.currentUser
        ReportResult reportResult = ReportResult.get(id)
        if (!reportResult) {
            render message(code: "app.label.widget.noData")
            return
        }
        if (!currentUser || !reportResult?.isViewableBy(currentUser)) {
            if (params.widget)
                render message(code: "error.403.message")
            else
                forward controller: "errors", action: "forbidden"
            return
        }
        ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery()
        Map data = getResultTabTable(ex, reportResult)
        if(ex.executedConfiguration.pvcDirty)
            data.warn = message(code: "app.pvc.dirty")
        data.reportName = ex.executedConfiguration.reportName
        data.sectionName =  dynamicReportService.getReportNameAsTitle(ex.executedConfiguration, ex, false)
        data.showChartSheet = ex.usedTemplate.showChartSheet
        data.template = ex.usedTemplate
        data.exConfigId = ex.executedConfiguration.id
        notificationService.deleteNotification(ex.executedConfiguration.id, NotificationApp.PVC_REPORT)

        List<ReportResult> drillDowns = ReportResult.findAllByDrillDownSource(ex)
        data.measures = drillDowns?.collect { it.measure }
        data.index=params.index
        data.widget=params.widget
        data.hideTable=params.boolean("hideTable")
        data.displayLength = params.displayLength
        data.drillDownToCaseList = ex.executedTemplate.drillDownToCaseList
        data.reportResultId = id
        data.isInDraftMode = (ex.executedConfiguration.class == PeriodicReportConfiguration) ? (ex.executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
        render view: (params.widget ? "tabWidget" : (params.frame ? "tabFrame" : "viewTab")), model: data
    }

    private List getCllTemplates(ExecutedReportConfiguration executedConfiguration) {
        executedConfiguration.executedTemplateQueries
                .findAll { it.usedTemplate.templateType == TemplateTypeEnum.CASE_LINE }
                ?.collect { [name: it.getTitle(), id: it.reportResult.id] }
    }

    def downloadAsyncExportFile() {
        notificationService.deleteNotificationById(params.id)
        byte[] data = new File(grailsApplication.config.tempDirectory + "/" + params.sourceFileName).bytes
        String ext = params.sourceFileName.substring(params.sourceFileName.lastIndexOf(".") + 1).toLowerCase()
        render(file: data, contentType: grailsApplication.config.grails.mime.types[ext], fileName: params.userFileName)
    }

    def viewCll(long id) {
        ReportResult reportResult
        ReportTemplate template
        String measure = getMeasure(params.cell)
        if (measure) {

            reportResult = ReportResult.findByParentAndMeasure(ReportResult.get(params.long("parentId")), measure)
            template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template)

        } else if (params.field) {
            reportResult = ReportResult.findByParentAndField(ReportResult.get(params.long("parentId")), params.field)
            template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template)
        } else {
            reportResult = ReportResult.get(id)
            template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.getExecutedTemplateQuery().executedTemplate)

        }
        User currentUser = userService.currentUser
        if (!currentUser || !reportResult?.isViewableBy(currentUser)) {
            if(params.widget)
                render message(code:"error.403.message")
            else
                forward controller: "errors", action: "forbidden"
            return
        }
        ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery();
        ExecutedReportConfiguration configuration = ex.executedConfiguration
        notificationService.deleteNotification(ex.executedConfiguration.id, NotificationApp.PVC_REPORT)

        //!!!!!!! redirect to special editable template!!!!!!!!!!
        if ((template.name == Holders.config.getProperty('pvcModule.late_processing_template') || template.name == Holders.config.getProperty('pvcModule.inbound_processing_template')) && !params.widget) {
            if(params.filter?.size()>1000){
                forward action: 'viewDelayReason'
                render ""
            } else {
                redirect action: 'viewDelayReason', params: [id: reportResult.id, filter: params.filter, field: params.field, fieldValues: params.fieldValues]
            }
            return
        }

        boolean isCllTemplate = template instanceof CaseLineListingTemplate

        Map data = isCllTemplate ? templateService.getResultTable((CaseLineListingTemplate) template) : [:]
        if(configuration.pvcDirty)
            data.warn = message(code: "app.pvc.dirty")
        data.reportName = configuration.reportName
        data.sectionName =  dynamicReportService.getReportNameAsTitle(configuration, ex, false)
        data.templateName = reportResult.parent ? template.name : ex.getTitle()
        data.template = template
        data.reportResultId = reportResult.id
        data.sectionId = ex.id
        data.index=params.index
        data.widget=params.widget
        data.hideTable=params.boolean("hideTable")
        data.breadcrumbs = reportResult.parent ? getBreadcrumbs(reportResult) : []
        List<ReportResult> drillDowns = ReportResult.findAllByParent(reportResult)
        data.drillDownFilerColumns = drillDowns?.collect { [drillDownReportId: it.id, drillDownColumn: it.field, drillDownFilterColumn: it.drillDownFilerColumns?.split(",")] }
        if (isCllTemplate) {
            data.suppressLabels = ((CaseLineListingTemplate) template).groupingList?.reportFieldInfoList?.findAll { it.suppressLabel }?.collect { it.reportField }?.join(',') ?: null
        } else {
            data.suppressLabels = null
        }
        render view: (params.widget ? "cllWidget" : "viewCll"), model: data
    }

    private List getBreadcrumbs(ReportResult r) {
        List<Map> breadcrumbs = []
        ReportResult reportResult = r
        while (reportResult.parent) {
            breadcrumbs.add(0, [id: reportResult.id, name: reportResult.template.name, action: "viewCll", controller: "advancedReportViewer"])
            reportResult = reportResult.parent
        }
        breadcrumbs.add(0, [id: reportResult.id, name: reportResult.executedTemplateQuery.getTitle(), action: "show", controller: "report"])
        breadcrumbs
    }

    private String getMeasure(cell) {
        if (cell?.split("_")?.size() != 3) return null
        return cell.split("_")[2]
    }

    private Map getResultTabTable(ExecutedTemplateQuery executedTemplateQuery, ReportResult reportResult) {
        Map result = [:]

        DataTabulationTemplate template = GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedTemplate)
        result.groupFields = template.groupingList?.reportFieldInfoList?.collect { it.reportField.name } ?: []
        result.rowFields = result.groupFields + (template.rowList?.reportFieldInfoList?.collect { it.reportField.name } ?: [])
        result.columnFields = []
        result.fieldsCodeNameMap = template.selectedFieldsRows?.collectEntries { [(it.reportField.name): it.renameValue ?: (ViewHelper.getMessage('app.reportField.' + it.reportField.name) ?: it.reportField.name)] } ?: [:]
        template.columnMeasureList.eachWithIndex { DataTabulationColumnMeasure block, int blockIndex ->
            result.columnFields.add([blockIndex: blockIndex + 1, columns: block.columnList?.reportFieldInfoList?.collect { it.reportField.name }])
            Map codeMAp = block.columnList?.reportFieldInfoList?.collectEntries { [(it.reportField.name): ViewHelper.getMessage('app.reportField.' + it.reportField.name)] }
            if (codeMAp)
                result.fieldsCodeNameMap.putAll(codeMAp)
        }

        JsonDataSource dataSource = ReportBuilder.createDataSource(reportResult)
        def jsonNodeItrList = dataSource.getAt("jsonNodesIterator")
        List dataList = []
        jsonNodeItrList.each {
            if (it.size() > 0) dataList << it
        }
        result.data = "[" + dataList.join(",") + "]"
        result.header = reportResult.data.crossTabHeader
        result.drilldownHeader = reportResult.data.crossTabHeader

        List topValues = ReportBuilder.getTopColumnsValues(reportResult)
        if (topValues) {
            JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
            result.header = ReportBuilder.filterHeader(tabHeaders, topValues, (JSONArray) JSON.parse(result.data)).toString()
        }
        if (template.supressHeaders) {
            JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
            List<HeaderTabDTO> headerTabs = tabHeaders.collect { header -> new HeaderTabDTO(header) }
            List headersList = CrosstabReportBuilder.getSupressColumnTree(headerTabs)
            def getLastLeaf
            getLastLeaf = { l ->
                List leaf = []
                l.each { k, v ->
                    if (v instanceof Map) leaf.addAll(getLastLeaf(v))
                    if (v instanceof HeaderTabDTO) leaf.add(v)
                }
                return leaf
            }
            List codes = []
            headersList.each { r ->
                codes.addAll(getLastLeaf(r))
            }
            result.header = (codes.collect { [(it.columnName): it.labels.findAll { it.trim() }.last()] } as JSON).toString()
            //sort columns according to suppresed headers
            int maxRows = codes.max { it -> it.labels.size() }.labels.size()
            List headerRows = []
            codes.each { column ->
                for (int i = 0; i < maxRows; i++) {
                    if (!headerRows[i]) headerRows[i] = []
                    String parent = ""
                    if (i > 0) {
                        for (int k = 0; k < i; k++)
                            parent += column.labels[k]
                    }
                    if (i >= column.labels.size()) {
                        headerRows[i].add([parent: parent, label: ""])
                    } else {
                        headerRows[i].add([parent: parent, label: column.labels[i]])
                    }
                }
            }
            int maxColumns = headerRows.max { it -> it.size() }.size()
            String current
            List tr = []
            headerRows = headerRows.findAll { row -> row.find { it.label.trim() } }
            for (int i = 0; i < headerRows.size(); i++) {
                List td = []
                for (int j = 0; j < maxColumns; j++) {
                    if (headerRows[i][j]?.label?.trim()) {
                        if (i < headerRows.size() - 1) {
                            int colspan = getColSpan(headerRows, i, j, maxColumns)
                            int rowspan = getRowSpan(headerRows, i, j)
                            td << [colspan: colspan, rowspan: rowspan, label: headerRows[i][j].label]
                            j = j + colspan - 1
                        } else {
                            td << [colspan: 1, rowspan: 1, label: headerRows[i][j].label]
                        }
                    }
                }
                tr << td
            }
            result.supressedHeader = tr
        }
        if (template.columnMeasureList?.find { cm -> cm.measures?.find { m -> m.colorConditions } }) {
            Map fieldConditionsMap = [:]
            JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
            tabHeaders.each {
                String code = it.collect { it.key }[0]
                List conditions = CrosstabReportBuilder.getColorConditionsForField(template, code)
                fieldConditionsMap.put(code, conditions)
            }
            if (fieldConditionsMap.find { k, v -> v.size() > 0 }) {
                def json = JSON.parse(result.data)
                json.each { Map row ->
                    row.each { field, value ->
                        List fieldConditions = fieldConditionsMap.get(field)
                        if (fieldConditions) {
                            for (int j = 0; j < fieldConditions.size(); j++) {
                                Map oneFieldCondition = fieldConditions[j];
                                Boolean conditionResult = false
                                for (int i = 0; i < oneFieldCondition.conditions.size(); i++) {
                                    String fieldPattern = CrosstabReportBuilder.formFiledNameForColorCondition(oneFieldCondition.conditions[i].field, field)
                                    conditionResult = CrosstabReportBuilder.evaluateColorSubCondition(row.get(fieldPattern), oneFieldCondition.conditions[i].operator as QueryOperatorEnum, oneFieldCondition.conditions[i].value)
                                    if (!conditionResult) break
                                }
                                if (conditionResult) {
                                    row.put(field, colorCell(oneFieldCondition.icon,oneFieldCondition.color, ""+value))
                                    break;
                                }
                            }
                        }
                    }
                }
                result.data = json.toString()
            }
        }
        return result
    }

    private int getRowSpan(List headerRows, int row, int column) {
        int rowSpan = 1
        for (int i = row + 1; i < headerRows.size(); i++) {
            if (!headerRows[i][column]?.label?.trim())
                rowSpan++
            else
                break
        }
        return rowSpan
    }

    private int getColSpan(List headerRows, int row, int column, int maxColumns) {
        String current = headerRows[row][column].label
        String parent = headerRows[row][column].parent
        int colSpan = 1
        for (int i = column + 1; i < maxColumns; i++) {
            if ((headerRows[row][i].label == current)&&(headerRows[row][i].parent == parent))
                colSpan++
            else
                break
        }
        return colSpan
    }

    def getChartWidgetDataAjax(long id) {
        ReportResult reportResult = ReportResult.get(id)
        def data = [:]
        if ((reportResult.executedTemplateQuery.executedTemplate instanceof ExecutedDataTabulationTemplate) &&reportResult.executedTemplateQuery.executedTemplate.worldMap) {
            data.type = "Map"
            data.options = templateService.getMapOptions(reportResult)
        } else {
            data.options = templateService.getChartOptions(reportResult, params.click)
        }
        data.latestComment = commentService.getReportResultChartAnnotation(reportResult.getId())
        render(ChartOptionsUtils.serializeToHtml(data))
    }

    def viewDelayReason(long id) {
        ReportResult reportResult = ReportResult.get(id)
        ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery()
        boolean inboundCompliance = false
        if((reportResult.template?:reportResult.executedTemplateQuery.executedTemplate).name == Holders.config.getProperty('pvcModule.inbound_processing_template')) {
            inboundCompliance = true
        }
        ExecutedReportConfiguration configuration = ex.executedConfiguration
        def filterCodesList = params.filter ? new JsonSlurper().parseText(params.filter) : []
        Map data = templateService.getResultTable(GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.getExecutedTemplateQuery().executedTemplate))
        data.reportName = configuration.reportName
        data.reportResultId = reportResult.id
        data.filterCodesList=(filterCodesList as JSON).toString()
        boolean hidden = true
        if (inboundCompliance) {
            data.lateList = (reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC_Inbound,hidden) as JSON).toString()
            data.correctiveActionList = (reportExecutorService.getCorrectiveActionList(ReasonOfDelayAppEnum.PVC_Inbound) as JSON).toString()
            data.preventativeActionList = (reportExecutorService.getPreventativeActionList(ReasonOfDelayAppEnum.PVC_Inbound) as JSON).toString()
            data.rootCauseList = (reportExecutorService.getRootCauseList(ReasonOfDelayAppEnum.PVC_Inbound,hidden) as JSON).toString()
        }
        else {
            data.lateList = (reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC,hidden) as JSON).toString()
            data.correctiveActionList = (reportExecutorService.getCorrectiveActionList(ReasonOfDelayAppEnum.PVC) as JSON).toString()
            data.preventativeActionList = (reportExecutorService.getPreventativeActionList(ReasonOfDelayAppEnum.PVC) as JSON).toString()
            data.rootCauseList = (reportExecutorService.getRootCauseList(ReasonOfDelayAppEnum.PVC,hidden) as JSON).toString()
        }

        data.responsiblePartyList = (reportExecutorService.getResponsiblePartyList(hidden) as JSON).toString()
        data.rootCauseSubCategoryList = (reportExecutorService.getRootCauseSubCategoryList(hidden) as JSON).toString()
        data.rootCauseClassList = (reportExecutorService.getRootCauseClassList(hidden) as JSON).toString()
        data.sectionId = ex.id
        data.configurationId = getConfigurationInstanceId(configuration)
        data.template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: ex.executedTemplate)
        data.breadcrumbs = reportResult.parent ? getBreadcrumbs(reportResult) : []
        data.filterCodes = filterCodesList ?: []
        data.isInbound = inboundCompliance
        data
    }

    Long getConfigurationInstanceId(executedConfiguration){
        ExecutionStatus executionStatus
        if(executedConfiguration instanceof ExecutedConfiguration) {
            executionStatus = ExecutionStatus.getExecutionStatusByExectutedEntity(executedConfiguration.id, ExecutingEntityTypeEnum.CONFIGURATION).list()[0]
        }

        if(executedConfiguration instanceof ExecutedPeriodicReportConfiguration) {
            executionStatus = ExecutionStatus.getExecutionStatusByExectutedEntity(executedConfiguration.id,ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION).list()[0]
        }

        return executionStatus?.entityId ?:  ReportConfiguration.findByReportNameAndOwner(executedConfiguration.reportName, executedConfiguration.owner)?.id
    }

    def getAllRcasForCase(Long caseId, String reportId, Long enterpriseId, Long senderId) {
        List dataList = reportExecutorService.getAllReasonOfDelayFromMart(caseId, reportId, enterpriseId, params.senderId as Long, params.masterVersionNum as Long)
        Map workflow = null
        List workflowList = null
        WorkflowState workflowState
        ReasonOfDelayAppEnum ownerApp
        def drilldownMetadata
        WorkflowConfigurationTypeEnum workflowType
        if(params.isInbound=='true') {
            drilldownMetadata = InboundDrilldownMetadata.getMetadataRecord([masterCaseId: params.caseId, masterVersionNum: params.masterVersionNum, senderId: params.senderId, tenantId: params.enterpriseId]).get()
            workflowType = WorkflowConfigurationTypeEnum.PVC_INBOUND
            ownerApp = ReasonOfDelayAppEnum.PVC_Inbound
        }else {
            drilldownMetadata = DrilldownCLLMetadata.getMetadataRecord([masterCaseId: params.caseId, processedReportId: params.reportId, tenantId: params.enterpriseId]).get()
            workflowType = WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY
            ownerApp = ReasonOfDelayAppEnum.PVC
        }
        workflow = drilldownMetadata.workflowState.toWorkflowStateMap()
        workflowList = WorkflowRule.getAllByConfigurationTypeAndInitialState(workflowType, drilldownMetadata.workflowState).list()?.collect {
            [id: it.id, name: it.targetState.name]
        }
        workflowState = drilldownMetadata.workflowState
        boolean differentAssigned = false
        boolean differentWorkflow = false
        if (params.selectedIds) {
            for (def it in JSON.parse(params.selectedIds)) {
                def ddmd
                if (it.senderId != null && (Long.valueOf(it.senderId) > -1)) {
                    ddmd = InboundDrilldownMetadata.getMetadataRecord([masterCaseId: it.caseId, masterVersionNum: it.versionNum, senderId: it.senderId, tenantId: it.tenantId]).get()
                }else {
                    ddmd = DrilldownCLLMetadata.getMetadataRecord([masterCaseId: it.caseId, processedReportId: it.processedReportId, tenantId: it.tenantId]).get()
                }
                if ((ddmd.assignedToUser?.id != drilldownMetadata.assignedToUser?.id) || (ddmd.assignedToUserGroup?.id != drilldownMetadata.assignedToUserGroup?.id)) differentAssigned = true
                if (ddmd.workflowState?.id != workflow.workflowStateId) differentWorkflow = true
                if (differentAssigned && differentWorkflow) break
            }
        }
        List editableList = RCAMandatory.getEditableRCAFields(ownerApp, workflowState, userService.currentUser).list()
        render([dataList       : dataList,
                assignedToUser : (differentAssigned || !drilldownMetadata.assignedToUser) ? "" : Constants.USER_TOKEN + drilldownMetadata.assignedToUser.id,
                assignedToGroup: (differentAssigned || !drilldownMetadata.assignedToUserGroup) ? "" : Constants.USER_GROUP_TOKEN + drilldownMetadata.assignedToUserGroup.id,
                workflow       : (differentWorkflow ? "" : workflow),
                workflowList   : workflowList,
                nonEditableList: ReasonOfDelayFieldEnum.findAll { !editableList*.toString().contains(it.toString()) },
                mandatoryList  : RCAMandatory.getMandatoryRCAFields(ownerApp, workflowState).list()] as JSON)
    }

    def getAllAttachments() {
        def metadataRecord
        if (params?.senderId != null && (Long.valueOf(params?.senderId) > -1)) {
            metadataRecord = InboundDrilldownMetadata.getMetadataRecord(params).get()
        }else {
            metadataRecord = DrilldownCLLMetadata.getMetadataRecord(params).get()
        }
        Set out = metadataRecord?.attachments?.collect { [name: it.name, id: it.id, createdBy:it.createdBy,
                                                          dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_FORMAT)] } ?: []
        render out as JSON;
    }

    def uploadAttachment() {
        def attachedFile
        for (int i = 0; i < params.int("fileNumber"); i++) {
            request.getFiles('file[' + i + "]").each { MultipartFile file ->
                if (file.size > 0) {
                    attachedFile = file
                }
            }

            if (params.selectedJson && params.selectedJson != "[]") {
                DrilldownCLLMetadata.withTransaction { status ->
                    for (Map it : JSON.parse(params.selectedJson)) {
                        def drilldownMetadata
                        if (it.senderId != null && (Long.valueOf(it.get('senderId'))) > -1) {
                            drilldownMetadata = InboundDrilldownMetadata.getMetadataRecord([masterCaseId: it.caseId, masterVersionNum: it.versionNum, senderId: it.senderId, tenantId: it.tenantId]).get()
                        } else {
                            drilldownMetadata = DrilldownCLLMetadata.getMetadataRecord([masterCaseId: it.caseId, processedReportId: it.processedReportId, tenantId: it.tenantId]).get()
                        }
                        DrilldownCLLMetadata d;
                        if (drilldownMetadata.workflowState?.isFinalState()) {
                            status.setRollbackOnly()
                            render "finalState"
                            return
                        }
                        bindFile(drilldownMetadata, attachedFile)
                        CRUDService.saveOrUpdate(drilldownMetadata)
                    }
                }
            } else {
                def drilldownCLLMetadata
                if (params.senderId != null && (Long.valueOf(params.get('senderId'))) > -1) {
                    drilldownCLLMetadata = InboundDrilldownMetadata.getMetadataRecord(params).get()
                } else {
                    drilldownCLLMetadata = DrilldownCLLMetadata.getMetadataRecord(params).get()
                }
                bindFile(drilldownCLLMetadata, attachedFile)
                CRUDService.saveOrUpdate(drilldownCLLMetadata)
            }
        }
        render "ok"
    }

    private void bindFile(metadataRecord, file) {
        PvcAttachment attachment = new PvcAttachment()
        attachment.name = file.originalFilename
        attachment.data = file.bytes
        attachment.createdBy = userService.currentUser.fullName
        attachment.modifiedBy = userService.currentUser.fullName
        metadataRecord.addToAttachments(attachment)
    }

    def downloadAttachment(Long id) {
        PvcAttachment attachment = PvcAttachment.get(id)
        render(file: attachment.data, fileName: attachment.name, contentType: "application/octet-stream")
    }

    def removeAttachments(Long attachmentId) {
        CRUDService.delete(PvcAttachment.get(attachmentId))
        render "ok"
    }

    def saveDelayReasonData() {
        List rcList = []
        List caseList = []
        String errorMessage = null
        String message = null
        List errorRows = null
        boolean isInbound = false
        ReportResult reportResult = ReportResult.get(params.long("reportResultId"))
        ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery()
        ExecutedReportConfiguration configuration = ex.executedConfiguration
        try {
            boolean transactionError = false
            WorkflowJustification.withTransaction { status ->

                List results = []
                WorkflowRule rule = params.workflowRule ? WorkflowRule.get(params.workflowRule as Long) : null

                if (!params?.selectedIds || params?.selectedIds == "[]") {
                    params?.selectedIds = [[caseId           : params?.caseId,
                                            caseNum          : params.caseNumber,
                                            cllRowId         : params?.cllRowId,
                                            processedReportId: params?.reportId,
                                            senderId         : params?.senderId,
                                            tenantId         : params?.enterpriseId,
                                            versionNum       : params?.versionNumber]]

                } else {
                    params?.selectedIds = JSON.parse(params?.selectedIds)
                }

                params?.selectedIds?.each { selected ->
                    def drilldownMetadata
                    if (params.isInbound instanceof String[] ? params.isInbound[0] : params.isInbound) {
                        isInbound = true
                        drilldownMetadata = InboundDrilldownMetadata.getMetadataRecord([masterCaseId: selected.caseId, tenantId: selected.tenantId, senderId: selected.senderId, masterVersionNum: selected.versionNum]).get()
                        caseList << [caseId: selected.caseId, enterpriseId: selected.tenantId, caseNumber: selected.caseNum, versionNumber: selected.versionNum, senderId: selected.senderId, workflowState: drilldownMetadata.workflowState, cllRowId: selected.cllRowId]
                    } else {
                        drilldownMetadata = DrilldownCLLMetadata.getMetadataRecord([masterCaseId: selected.caseId, processedReportId: selected.processedReportId, tenantId: selected.tenantId]).get()
                        caseList << [caseId: selected.caseId, enterpriseId: selected.tenantId, reportId: selected.processedReportId, caseNumber: selected.caseNum, versionNumber: selected.versionNum, workflowState: drilldownMetadata.workflowState, cllRowId: selected.cllRowId]
                    }
                }

                if (!params?.late) {
                    rcList << [late            : params.baseLate, rootCause: null, rootCauseClass: null, rootCauseSubCategory: null, responsibleParty: null,
                               correctiveAction: null, preventativeAction: null, flagPrimary: "true",
                               pvcLcpId        : null, actions: "", summary: "", investigation: "",
                               correctiveDate  : null, preventiveDate: null, flagIUD: "I"]
                } else if (params?.late instanceof String) {
                    rcList << [late            : params.late, rootCause: params.rootCause, responsibleParty: params.responsibleParty, rootCauseClass: params.rootCauseClass,
                               correctiveAction: params.correctiveAction, preventativeAction: params.preventativeAction, flagPrimary: params.flagPrimary, rootCauseSubCategory: params.rootCauseSubCategory,
                               pvcLcpId        : params.pvcLcpId, actions: params.actions.trim(), summary: params.summary.trim(), investigation: params.investigation.trim(),
                               correctiveDate  : params.correctiveDate, preventiveDate: params.preventiveDate, flagIUD: params.flagIUD]
                } else if (params?.late instanceof String[]) {
                    params?.late.eachWithIndex { it, i ->
                        rcList << [late            : params.late[i], rootCause: params.rootCause[i], responsibleParty: params.responsibleParty[i], rootCauseClass: params.rootCauseClass[i],
                                   correctiveAction: params.correctiveAction[i], preventativeAction: params.preventativeAction[i], flagPrimary: params.flagPrimary[i], rootCauseSubCategory: params.rootCauseSubCategory[i],
                                   pvcLcpId        : params.pvcLcpId[i], actions: params.actions[i].trim(), summary: params.summary[i].trim(), investigation: params.investigation[i].trim(),
                                   correctiveDate  : params.correctiveDate[i], preventiveDate: params.preventiveDate[i], flagIUD: params.flagIUD[i]]
                    }
                }
                (caseList, message) = getSaveDelayReasonData(caseList, rcList, isInbound)
                if (message != '') {
                    return
                }

                params?.selectedIds?.each { selected ->
                    def drilldownMetadata
                    if (params.isInbound instanceof String[] ? params.isInbound[0] : params.isInbound) {
                        drilldownMetadata = InboundDrilldownMetadata.getMetadataRecord([masterCaseId: selected.caseId, tenantId: selected.tenantId, senderId: selected.senderId, masterVersionNum: selected.versionNum]).get()
                    } else {
                        drilldownMetadata = DrilldownCLLMetadata.getMetadataRecord([masterCaseId: selected.caseId, processedReportId: selected.processedReportId, tenantId: selected.tenantId]).get()
                    }
                    WorkflowJustification currentWorkflowJustificationInstance
                    if (rule) {
                        currentWorkflowJustificationInstance = new WorkflowJustification(
                                fromState: rule.initialState,
                                toState: rule.targetState,
                                routedBy: userService.currentUser,
                                description: params.justification,
                                workflowRule: rule,
                        )
                        if (drilldownMetadata instanceof InboundDrilldownMetadata)
                            currentWorkflowJustificationInstance.inboundMetadata = drilldownMetadata
                        else
                            currentWorkflowJustificationInstance.drilldownCLLMetadata = drilldownMetadata
                        results << workflowService.assignPvcWorkflow(currentWorkflowJustificationInstance, true, null, true)
                    }
                    bindAssignedTo(params, drilldownMetadata, configuration.reportName, selected.caseId, selected.versionNum, [], currentWorkflowJustificationInstance)
                }
                if (results.find { !it.success }) {
                    errorMessage = message(code: "app.periodicReportConfiguration.state.update.warn")
                    errorRows = results.findAll { !it.success }?.collect { it.rowInfo }
                    status.setRollbackOnly()
                    response.status = 500
                    Map responseMap = [message: errorMessage, errorRows: errorRows]
                    render(contentType: "application/json", responseMap as JSON)
                    transactionError = true
                    return
                }
            }
            if (transactionError) {
                return
            }
            Map<Long, List> oldRcList = [:]
            caseList.each {
                oldRcList[it.caseId as Long] = reportExecutorService.getAllReasonOfDelayFromMart(it.caseId as Long, it.reportId, it.enterpriseId as Long, it.senderId as Long, it.versionNumber as Long)
            }
            if (rcList && caseList && caseList.size() > 0)
                reportExecutorService.saveFixedTemplate(caseList, rcList, isInbound)
            caseList.each {
                for (int i = 0; i < rcList.size(); i++) {
                    if (message == '')
                        reportExecutorService.captureAuditLogChangesForRODAssessment(oldRcList[it.caseId as Long][i], rcList[i], new ExecutedConfiguration(reportName: configuration.reportName), it.cllRowId)
                }
            }
            configuration.pvcDirty = true
            CRUDService.saveWithoutAuditLog(configuration)
            render message
        } catch (Exception e) {
            log.error("Error occurred saving ROD", e)
            errorMessage = e.getMessage()
            response.status = 500
            Map responseMap = [message: errorMessage, errorRows: []]
            render(contentType: "application/json", responseMap as JSON)
            return
        }
    }

    def getSaveDelayReasonData(List caseList, List rcsList, boolean isInbound) {
        String message =''
        ReasonOfDelayAppEnum ownerApp = isInbound ? ReasonOfDelayAppEnum.PVC_Inbound : ReasonOfDelayAppEnum.PVC
        List updatedCaseList = []
        caseList.each { c ->
            List<String> rcaMandatoryFields = RCAMandatory.getMandatoryRCAFields(ownerApp, c?.workflowState).list()*.toString()
            List<String> errorFields = []
            rcsList.sort { a, b -> b.flagPrimary <=> a.flagPrimary }.each {
                boolean isPrimary = it?.flagPrimary?.equalsIgnoreCase("true")
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Issue_Type.toString()) && !it.late) {
                    errorFields.add(ReasonOfDelayFieldEnum.Issue_Type.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause.toString()) && !it.rootCause) {
                    errorFields.add(ReasonOfDelayFieldEnum.Root_Cause.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause_Class.toString()) && !it.rootCauseClass) {
                    errorFields.add(ReasonOfDelayFieldEnum.Root_Cause_Class.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.toString()) && !it.rootCauseSubCategory) {
                    errorFields.add(ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Resp_Party.toString()) && !it.responsibleParty) {
                    errorFields.add(ReasonOfDelayFieldEnum.Resp_Party.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Corrective_Action.toString()) && !it.correctiveAction) {
                    errorFields.add(ReasonOfDelayFieldEnum.Corrective_Action.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Preventive_Action.toString()) && !it.preventativeAction) {
                    errorFields.add(ReasonOfDelayFieldEnum.Preventive_Action.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Corrective_Date.toString()) && !it.correctiveDate) {
                    errorFields.add(ReasonOfDelayFieldEnum.Corrective_Date.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Preventive_Date.toString()) && !it.preventiveDate) {
                    errorFields.add(ReasonOfDelayFieldEnum.Preventive_Date.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Investigation.toString()) && !it.investigation) {
                    errorFields.add(ReasonOfDelayFieldEnum.Investigation.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Summary.toString()) && !it.summary) {
                    errorFields.add(ReasonOfDelayFieldEnum.Summary.getMessage())
                }
                if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Actions.toString()) && !it.actions) {
                    errorFields.add(ReasonOfDelayFieldEnum.Actions.getMessage())
                }
                errorFields = errorFields.unique()
            }
            if (errorFields.size() >0) {
                message += errorFields.join(', ') + " can't be empty for "
                DrilldownCLLData drilldownCLLData = DrilldownCLLData.findById(Long.valueOf(c?.cllRowId))
                Map dataMap = new JsonSlurper().parseText(drilldownCLLData.cllRowData)
                if (isInbound)
                    message += " Case #: " + dataMap['masterCaseNum'] + " Fup #: " + dataMap['masterFupNum'] + " Sender Name:" + dataMap['pvcIcSenderName'] +   " in Workflow State : " + c?.workflowState
                else
                    message += " Case #: " + dataMap['masterCaseNum'] + " Fup #: " + dataMap['masterFupNum'] + " in Workflow State : " + c?.workflowState
                message += ", please fill the required field(s).<br>"
            }
            else {
                updatedCaseList.add(c)
            }
        }
        return [updatedCaseList, message]
    }

    def viewNonCaseSql(long id) {
        ReportResult reportResult = ReportResult.get(id)
        ExecutedNonCaseSQLTemplate template  = GrailsHibernateUtil.unwrapIfProxy(reportResult.template) ?: reportResult.getExecutedTemplateQuery().executedTemplate
        Map data = getResultNonCaseSqlTable(template,reportResult)
        setDataDetails(data, template, reportResult)
        data.showChartSheet = template.showChartSheet
        data.drilldownView = "viewNonCaseSql"
        render view: (params.widget ? "cllWidget" : "viewCll"), model: data
    }

    def viewCustomSql(long id) {
        ReportResult reportResult = ReportResult.get(id)
        ExecutedCustomSQLTemplate template  = GrailsHibernateUtil.unwrapIfProxy(reportResult.template)?: reportResult.getExecutedTemplateQuery().executedTemplate
        Map data = getResultCustomSqlTable(template,reportResult)
        data.drilldownView = "viewCustomSql"
        setDataDetails(data, template, reportResult)
        render view: (params.widget ? "cllWidget" : "viewCll"), model: data
    }

    private Map getResultCustomSqlTable(ExecutedCustomSQLTemplate template, ReportResult reportResult) {
        Map result = [:]
        List<String> columnNamesList = getColumnNamesList(template.columnNamesList)
        setResult(result, reportResult, columnNamesList)
        return result
    }

    private Map getResultNonCaseSqlTable(ExecutedNonCaseSQLTemplate template, ReportResult reportResult) {
        Map result = [:]
        List<String> columnNamesList = getColumnNamesList(template.columnNamesList)
        setResult(result, reportResult, columnNamesList)
        return result
    }

    void setDataDetails(Map data, template, reportResult){
        ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery()
        ExecutedReportConfiguration configuration = ex.executedConfiguration
        data.reportName = configuration.reportName
        data.sectionName =  dynamicReportService.getReportNameAsTitle(configuration, ex, false)
        data.templateName = reportResult.parent ? template.name : ex.getTitle()
        data.template = template
        data.reportResultId = reportResult.id
        data.sectionId = ex.id
        data.index = params.index
        data.widget = params.widget
        data.hideTable = params.boolean("hideTable")
        data.breadcrumbs = reportResult.parent ? getBreadcrumbs(reportResult) : []
        List<ReportResult> drillDowns = ReportResult.findAllByParent(reportResult)
        data.drillDownFilerColumns = drillDowns?.collect { [drillDownReportId: it.id, drillDownColumn: it.field?.replace("CHART_COLUMN_", "")?.toUpperCase(), drillDownFilterColumn: getColumnNamesList(it.drillDownFilerColumns)] }
        data
    }

    private void setResult(Map result, ReportResult reportResult, List<String> columnNamesList ){
        result.rowColumns = []
        result.groupColumns = []
        result.header = columnNamesList
        result.fieldTypeMap = getFieldTypeMap(columnNamesList)
        result.fieldsCodeNameMap = getFieldCodeNamesMap(columnNamesList)
        def filterCodesList = params.filter ? new JsonSlurper().parseText(params.filter) : []
    }

    private List<String> getColumnNamesList(String columnNames) {
        return templateService.getColumnNamesList(columnNames)
    }

    private Map<String,String> getFieldCodeNamesMap(List<String> columnNamesList){
        Map<String,String> fieldTypeMap = [:]
        columnNamesList.each { col ->
            fieldTypeMap.put(col,col)
        }
        return fieldTypeMap
    }

    private Map<String,String> getFieldTypeMap(List<String> columnNamesList){
        Map<String,String> fieldCodesMap = [:]
        columnNamesList.each { col ->
            fieldCodesMap.put(col,'String')
        }
        return fieldCodesMap
    }

    def show() {
        forward(controller: "report", action: "show", params: params)
    }

    void updateSingleAssigne(String caseId, String versionNum, String tenantId, String processedReportId, String senderId, String reportName, allowedAssignedToIds) {
        def drilldownMetadata
        if (senderId && (Long.valueOf(senderId) > -1)) {
            drilldownMetadata = InboundDrilldownMetadata.getMetadataRecord([masterCaseId: caseId, masterVersionNum: versionNum, tenantId: tenantId, senderId: senderId]).get()
        } else {
            drilldownMetadata = DrilldownCLLMetadata.getMetadataRecord([masterCaseId: caseId, processedReportId: processedReportId, tenantId: tenantId]).get()
        }
        bindAssignedTo(params, drilldownMetadata, reportName, caseId, versionNum, allowedAssignedToIds)
    }

    @Transactional
    def updateAssignedOwner() {

        try {
            ExecutedReportConfiguration entity = ReportResult.findById(params.reportResultId).executedTemplateQuery.executedConfiguration
            if (params.selectedJson && params.selectedJson != "[]") {
                def json = JSON.parse(params.selectedJson)
                String senderId = params.senderId
                Set allowedAssignedToIds = []
                List roles = senderId && (Long.valueOf(senderId) > -1) ? Constants.ALLOWED_ASSIGNED_TO_ROLES_PVC_INB : Constants.ALLOWED_ASSIGNED_TO_ROLES_PVC
                if (params.field == Constants.USER_GROUP_TOKEN)
                    allowedAssignedToIds = userService.allowedAssignedToUserListPvcPvq(null, 0, Integer.MAX_VALUE, params.assignedToGroup, roles)?.items?.collect { it.id } ?: []
                else
                    allowedAssignedToIds = userService.allowedAssignedToGroupListPvcPvq(null, 0, Integer.MAX_VALUE, params.assignedToUser, roles)?.items?.collect { it.id } ?: []

                json.each {
                    updateSingleAssigne(it.caseId, it.versionNum, it.tenantId, it.processedReportId, it.senderId, entity.reportName, allowedAssignedToIds)
                }
            } else {
                updateSingleAssigne(params.masterCaseId, params.masterVersionNum, params.tenantId, params.processedReportId, params.senderId, entity.reportName, null)
            }
            render "Ok"
            return
        } catch (Exception e) {
            log.error("Error occurred in assigning owner", e)
            render(status: 500, text: e.getMessage())
        }
    }

    private bindAssignedTo(Map params, def drilldownMetadata, String reportName, String caseId, String versionNum, allowedAssignedToIds, WorkflowJustification currentWorkflowJustificationInstance = null) {
        User previousUser = drilldownMetadata.assignedToUser
        UserGroup previousUserGroup = drilldownMetadata.assignedToUserGroup
        if (!params.field || params.field == Constants.USER_TOKEN) {
            if (allowedAssignedToIds && drilldownMetadata.assignedToUserGroup && !allowedAssignedToIds.contains(Constants.USER_GROUP_TOKEN + drilldownMetadata.assignedToUserGroup.id)) throw new IllegalArgumentException(ViewHelper.getMessage("app.label.assignedTo.incompatibleError"))
            drilldownMetadata.assignedToUser = params.assignedToUser ? User.read(Long.valueOf(params.assignedToUser.replaceAll(Constants.USER_TOKEN, ''))) : null
            if(previousUser?.id!=drilldownMetadata.assignedToUser?.id){
                drilldownMetadata.assigner = userService.currentUser
                drilldownMetadata.assigneeUpdatedDate = new Date()
            }
        }
        if (!params.field || params.field == Constants.USER_GROUP_TOKEN) {
            if (allowedAssignedToIds && drilldownMetadata.assignedToUser && !allowedAssignedToIds.contains(Constants.USER_TOKEN + drilldownMetadata.assignedToUser.id)) throw new IllegalArgumentException(ViewHelper.getMessage("app.label.assignedTo.incompatibleError"))
            drilldownMetadata.assignedToUserGroup = params.assignedToGroup ? UserGroup.read(Long.valueOf(params.assignedToGroup.replaceAll(Constants.USER_GROUP_TOKEN, ''))) : null
            if(previousUserGroup?.id!=drilldownMetadata.assignedToUserGroup?.id){
                if(!previousUser) {
                    drilldownMetadata.assigner = userService.currentUser
                    drilldownMetadata.assigneeUpdatedDate = new Date()
                }
            }
        }
        drilldownMetadata.updateAssignedToName()
        CRUDService.saveOrUpdate(drilldownMetadata)
        if ((previousUserGroup?.id != drilldownMetadata.assignedToUserGroup?.id) || (previousUser?.id != drilldownMetadata.assignedToUser?.id)) {
            ExecutedConfiguration temp = new ExecutedConfiguration(reportName: reportName)
            AuditLogConfigUtil.logChanges(temp, [assignedToUser: drilldownMetadata.assignedToUser, assignedToUserGroup: drilldownMetadata.assignedToUserGroup], [assignedToUser: previousUser, assignedToUserGroup: previousUserGroup], Constants.AUDIT_LOG_UPDATE
                    , " - " + ViewHelper.getMessage("auditLog.drilldown.assignedTo.extraValue", caseId, versionNum))
            temp.discard()
        }
        if (!currentWorkflowJustificationInstance) {
            currentWorkflowJustificationInstance = new WorkflowJustification()
            currentWorkflowJustificationInstance.fromState = drilldownMetadata.workflowState
            currentWorkflowJustificationInstance.toState = drilldownMetadata.workflowState
            currentWorkflowJustificationInstance.routedBy = userService.currentUser
            currentWorkflowJustificationInstance.workflowRule = null
            if (drilldownMetadata instanceof InboundDrilldownMetadata)
                currentWorkflowJustificationInstance.inboundMetadata = drilldownMetadata
            else
                currentWorkflowJustificationInstance.drilldownCLLMetadata = drilldownMetadata
        }
        currentWorkflowJustificationInstance.assignedToUser = drilldownMetadata.assignedToUser
        currentWorkflowJustificationInstance.assignedToUserGroup = drilldownMetadata.assignedToUserGroup
        CRUDService.save(currentWorkflowJustificationInstance)

    }

    def caseForm(String caseNumber, Integer versionNumber) {
        if (!sqlGenerationService.isCaseNumberExistsForTenant(caseNumber, versionNumber)) {
            response.sendError(403, "You are not authorized to view or data doesn't exist")
            return
        }
        Map caseInfo = sqlGenerationService.getCaseMetadataDetails(caseNumber, versionNumber)
        def attachmentsList = qualityService.fetchAttachments(caseNumber)
        Gson gson = new Gson()
        [caseInfo: caseInfo, caseNumber: caseNumber, versionNumber: versionNumber, attachmentsList: attachmentsList]

    }

    def cllAjax(){
        def filterCodesList = templateService.getFilterDataForInteractiveOutput(params.filterData)
        def searchData = templateService.getSearchDataForInteractiveOutput(params.tableFilter)
        String globalSearchData = params.globalSearch?.trim()
        def additionalFilterMap = params.additionalFilterMap ? new JsonSlurper().parseText(params.additionalFilterMap) : [:]
        String assignedToFilter = (params.assignedToFilter?.length() == 0) ? null : params.assignedToFilter
        Long reportResultId = params.long('reportResultId')
        ReportResult reportResult = ReportResult.get(reportResultId)
        Long executedReportId = reportResult.getExecutedTemplateQuery().executedConfiguration.id
        def template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.getExecutedTemplateQuery().executedTemplate)
        Map data = templateService.getResultTable(template)

        Map tableData = [:]
        //!!!!!!! redirect to special editable template!!!!!!!!!!
        if ((template.name == Holders.config.pvcModule.late_processing_template || template.name == Holders.config.pvcModule.inbound_processing_template) && !params.widget) {
            DrilldownAccessTracker.withNewSession {
                DrilldownAccessTracker accessTracker = DrilldownAccessTracker.findByReportResultId(reportResultId)
                if (!accessTracker) {

                    Long dataCount = DrilldownCLLData.countByReportResultId(reportResultId)
                    accessTracker = new DrilldownAccessTracker(lastAccess: new Date(), reportResultId: reportResultId,
                            state: dataCount > 0 ? DrilldownAccessTracker.State.ACTIVE : DrilldownAccessTracker.State.ARCHIVED)
                    accessTracker.save(flush: true, failOnError: true)//no need for auditlog, need to save immediately
                }
                if (accessTracker?.getState() == DrilldownAccessTracker.State.ARCHIVED) {
                    try {
                        accessTracker.setState(DrilldownAccessTracker.State.RELOADING)
                        accessTracker.save(flush: true, failOnError: true)
                        //no need for auditlog, need to save immediately
                        templateService.fetchFromRptRsltAndInserttoDDCLL(reportResult)

                        if (notificationService.addNotificationListener(NotificationApp.PVC_REPORT, executedReportId, userService.currentUser?.id, reportResultId)) {
                            render([aaData: [], recordsTotal: 0, recordsFiltered: 0, warning: true] as JSON)
                            return
                        } else {
                            //due to synchronized processing data loading just completed - showing data
                        }
                    } catch (OptimisticLockingFailureException e) {
                        //OptimisticLockingFailureException is fine here if other user
                        // is trying to open the same report at the same time -
                        // so just ignoring and showing warnning message to reopen report later
                        if (notificationService.addNotificationListener(NotificationApp.PVC_REPORT, executedReportId, userService.currentUser?.id, reportResultId)) {
                            render([aaData: [], recordsTotal: 0, recordsFiltered: 0, warning: true] as JSON)
                            return
                        } else {
                            //due to synchronized processing data loading just completed - showing data
                        }
                    } catch (Exception e) {
                        accessTracker.setState(DrilldownAccessTracker.State.ARCHIVED)
                        accessTracker.save(flush: true, failOnError: true)
                        log.error("Unexpected server error", e);
                        render([aaData: [], recordsTotal: 0, recordsFiltered: 0, internalError: true] as JSON)
                        return
                    }
                } else if (accessTracker.getState() in [DrilldownAccessTracker.State.RELOADING, DrilldownAccessTracker.State.ARCHIVING]) {
                    if (notificationService.addNotificationListener(NotificationApp.PVC_REPORT, executedReportId, userService.currentUser?.id, reportResultId)) {
                        render([aaData: [], recordsTotal: 0, recordsFiltered: 0, warning: true] as JSON)
                        return
                    } else {
                        //due to synchronized processing data loading just completed - showing data
                    }
                }
                try {
                    accessTracker.setLastAccess(new Date())
                    accessTracker.save(flush: true, failOnError: true)
                } catch (OptimisticLockingFailureException e) {
                    //OptimisticLockingFailureException is fine here if other user
                    // is trying to open the same report at the same time -
                    // so just ignoring it
                }
            }
            boolean isInbound = (template.name == Holders.config.getProperty('pvcModule.inbound_processing_template')) ? true : false
            if (params.cllRecordId) {
                tableData = templateService.getCllDrilldownDataAjax([], reportResult,
                        params.long('start'), params.long('length'), params.direction, params.sort, searchData, globalSearchData, data.fieldTypeMap, data.templateHeader, ['cllRecordId': params.cllRecordId, pvcLcpFlagPrimary: "1"], assignedToFilter, null)
            } else if(params.linkFilter){
                 tableData = templateService.getCllDrilldownDataAjax([], reportResult,
                        params.long('start'), params.long('length'), params.direction, params.sort, searchData, globalSearchData, data.fieldTypeMap, data.templateHeader, ['cllRecordId': params.cllRecordId, pvcLcpFlagPrimary: "1"], assignedToFilter, null,params.linkFilter)
            } else {
                tableData = templateService.getCllDrilldownDataAjax(filterCodesList, reportResult,
                        params.long('start'), params.long('length'), params.direction, params.sort, searchData, globalSearchData, data.fieldTypeMap, data.templateHeader, [pvcLcpFlagPrimary: "1"], assignedToFilter, null)
            }
            reportExecutorService.appendReasonOfDelayDataFromMart([data: tableData.aaData, header: data.templateHeader],"dd-MMM-yyyy", isInbound)
        } else {
            tableData = templateService.getDataFromReportResultData(reportResult, data.header, data.fieldTypeMap, filterCodesList, params.long('start'), params.long('length'), params.direction, params.sort, searchData, globalSearchData, null, true, null)
        }
        //color cells
        List allFieldsFullInfo = template.getSelectedFieldsFullInfo()
        if (allFieldsFullInfo.find { it.reportFieldInfo.colorConditions }) {
            List allFieldConditions = []
            List columnsLabels = []
            List columnsTypes = []
            data.templateHeader.eachWithIndex { code, index ->
                String currentLabel = data.fieldsCodeNameMap[code]
                columnsTypes.add(data.fieldTypeMap[code])
                String json = allFieldsFullInfo.find {
                    String label = it.reportFieldInfo.renameValue ?: it.reportFieldInfo.reportField.getDisplayName()
                    label.trim() == currentLabel.trim()
                }.reportFieldInfo.colorConditions
                if (json) {
                    allFieldConditions.add(JSON.parse(json))
                } else {
                    allFieldConditions.add(null)
                }
                columnsLabels.add(currentLabel.trim())
            }

            tableData.aaData.each { row ->
                row.eachWithIndex {  value, index ->
                    List fieldConditions = allFieldConditions[index]
                    if (fieldConditions) {
                        for (int j = 0; j < fieldConditions.size(); j++) {
                            Map oneFieldCondition = fieldConditions[j];
                            Boolean conditionResult = false
                            for (int i = 0; i < oneFieldCondition.conditions.size(); i++) {
                                int colIndex = columnsLabels.indexOf(oneFieldCondition.conditions[i].field.trim())
                                String val = columnsTypes[colIndex] == "Date" ? Date.parse(Constants.DateFormat.WITH_TZ, row[colIndex]).format(DateUtil.DATEPICKER_FORMAT) : row[colIndex]
                                conditionResult = CrosstabReportBuilder.evaluateColorSubCondition(val, oneFieldCondition.conditions[i].operator as QueryOperatorEnum, oneFieldCondition.conditions[i].value)
                                if (!conditionResult) break
                            }
                            if (conditionResult) {
                                String val = columnsTypes[index] == "Date" ? Date.parse(Constants.DateFormat.WITH_TZ, value).format(DateUtil.DATEPICKER_FORMAT) : value.toString()
                                row[index] = colorCell(oneFieldCondition.icon, oneFieldCondition.color, val)
                                break;
                            }
                        }
                    }
                }
            }
        }
        render(tableData as JSON)
    }

    def getSimilarCases() {
        def filterCodesList = params.filterData && (params.filterData != 'null') ? new JsonSlurper().parseText(params.filterData) : []
        if (params?.caseNum) {
            filterCodesList.add(['field':'masterCaseNum', value:params?.caseNum])
        }
        if (params?.versionNum) {
            filterCodesList.add(['field':'masterVersionNum', value:params?.versionNum])
        }
        ReportResult reportResult = ReportResult.get(params.long('reportResultId'))
        def template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.getExecutedTemplateQuery().executedTemplate)
        Map data = templateService.getResultTable(template)

        List searchData = templateService.getSearchDataForInteractiveOutput(params.tableFilter)
        String globalSearchData = params.globalSearch?.trim()
        String assignedToFilter = (params.assignedToFilter?.length() == 0) ? null : params.assignedToFilter
        Map tableData = [:]
        tableData = templateService.getCllDrilldownDataAjax(filterCodesList, reportResult, (params?.caseNum ? 0 : params.long('start')), BULK_UPDATE_MAX_ROWS + 1, params.direction,
                params.sort, searchData, globalSearchData, data.fieldTypeMap, data.templateHeader, [pvcLcpFlagPrimary: "1"], assignedToFilter, null, params.linkFilter)
        render tableData.aaData as JSON
    }

    private String colorCell(String icon, String color, String value) {
        //<i c=color> is a marker to color cell using js. Using short attribute name and tag i to make marker compact and do not increase response size significantly
        return "<i c='" + color + "'></i>" + CrosstabReportBuilder.formatConditionalCell(icon, value, true)
    }

    def notCllAjax() {
        def filterCodesList = params.filterData ? new JsonSlurper().parseText(params.filterData) : []
        def searchData = templateService.getSearchDataForInteractiveOutput(params.tableFilter)
        String globalSearchData = params.globalSearch
        def additionalFilterMap = params.additionalFilterMap ? new JsonSlurper().parseText(params.additionalFilterMap) : [:]

        ReportResult reportResult = ReportResult.get(params.long('reportResultId'))
        def template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.getExecutedTemplateQuery().executedTemplate)
        List<String> columnNamesList = getColumnNamesList(template.columnNamesList)
        Map tableData = templateService.getDataFromReportResultData(reportResult, columnNamesList, null, filterCodesList,
                params.long('start'), params.long('length'), params.direction, params.sort, searchData, globalSearchData, null, false, params.rowIdFilter)
        render(tableData as JSON)
    }

    def lockRca() {
        User currentUser = userService.currentUser
        List<Long> ids = params.ids?.split(";")?.collect { it as Long }
        User lockOwner = lockObjectService.lock(ids, currentUser)
        if (currentUser.id != lockOwner.id)
            render([locked: true, name: lockOwner.fullName] as JSON)
        else
            render([locked: false] as JSON)
    }

    def unlockRca() {
        User currentUser = userService.currentUser
        lockObjectService.unlock(params.ids?.split(";")?.collect { it as Long }, currentUser)
        render "ok"
    }
}
