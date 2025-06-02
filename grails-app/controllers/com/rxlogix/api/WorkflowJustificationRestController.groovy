package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.config.AutoAssignment
import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.InboundDrilldownMetadata
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.QualitySampling
import com.rxlogix.config.QualitySubmission
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.WorkflowJustification
import com.rxlogix.config.WorkflowRule
import com.rxlogix.config.WorkflowState
import com.rxlogix.enums.AssignmentRuleEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherExecutedTemplate
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.publisher.WordTemplateExecutor
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.hibernate.Transaction

@Secured('permitAll')
class WorkflowJustificationRestController {

    def CRUDService
    def customMessageService
    def executionStatusService
    def periodicReportService
    def userService
    def reportRequestService
    def ldapService
    def qualityService
    def workflowService
    def publisherService

    def index(Long executedReportConfigurationId, String initialState) {
        ExecutedReportConfiguration executedReportConfigurationInstance=ExecutedReportConfiguration.get(executedReportConfigurationId)
        List<Map> workflowJustificationList = []
        Map stateList = periodicReportService.targetStatesAndApplications(executedReportConfigurationId, initialState)
        if(executedReportConfigurationInstance){
            workflowJustificationList= WorkflowJustification.findAllByExecutedReportConfiguration(executedReportConfigurationInstance, [sort: 'dateCreated', order: 'desc']).collect {
                it.toWorkflowJustificationMap()
            }
        }
        render([currentState:executedReportConfigurationInstance.workflowState,stateList:stateList,workflowJustificationList:workflowJustificationList,reportId:params.id,
                actionItems:(executedReportConfigurationInstance.actionItems?.findAll{it.status != StatusEnum.CLOSED}?.size())
        ] as JSON)
    }

    def workFlowForQuality(Long qualityData, String initialState, String dataType) {
        def qualityDataObj = null
        List<Map> workflowJustificationList = []
        if(dataType.equals(PvqTypeEnum.CASE_QUALITY.name())){
            qualityDataObj = QualityCaseData.get(qualityData)
            List<QualityCaseData> qualityCaseDataList = QualityCaseData.findAllByCaseNumberAndTenantIdAndIsDeletedAndErrorType(qualityDataObj.caseNumber  , Tenants.currentId() as Long , false, qualityDataObj.errorType)
            workflowJustificationList= WorkflowJustification.findAllByQualityCaseDataInList(qualityCaseDataList, [sort: 'dateCreated', order: 'desc']).collect {
                it.toWorkflowJustificationMap()
            }.unique { "${it.fromState}-${it.toState}" }
        }else if(dataType.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())) {
            qualityDataObj= QualitySubmission.get(qualityData)
            List<QualitySampling> qualitySamplingList = QualitySubmission.findAllByCaseNumberAndTenantIdAndIsDeletedAndErrorType(qualityDataObj.caseNumber  , Tenants.currentId() as Long , false, qualityDataObj.errorType)
            workflowJustificationList= WorkflowJustification.findAllByQualitySubmissionInList(qualitySamplingList, [sort: 'dateCreated', order: 'desc']).collect {
                it.toWorkflowJustificationMap()
            }.unique { "${it.fromState}-${it.toState}" }
        } else  {
            qualityDataObj = QualitySampling.get(qualityData)
            List<QualitySampling> qualitySamplingList = QualitySampling.findAllByCaseNumberAndTypeAndTenantIdAndIsDeletedAndErrorType(qualityDataObj.caseNumber, qualityDataObj.getType(), Tenants.currentId() as Long , false, qualityDataObj.errorType)
            workflowJustificationList= WorkflowJustification.findAllByQualitySamplingInList(qualitySamplingList, [sort: 'dateCreated', order: 'desc']).collect {
                it.toWorkflowJustificationMap()
            }.unique { "${it.fromState}-${it.toState}" }
        }

        Map stateList = qualityService.qualityTargetStatesAndApplications(qualityData, initialState, dataType)

        render([currentState:qualityDataObj.workflowState,stateList:stateList,workflowJustificationList:workflowJustificationList,reportId:params.id,
                actionItems: 0] as JSON)
    }

    def workFlowForReasonOfDelay(String caseId, String processReportId, String tenantId, String initialState, String drillDownCLLMetaDataId, String cllRowId, String senderId, String versionNum) {
        List<Map> workflowJustificationList = []
        Map metadataParams = [:]
        metadataParams.masterCaseId = Long.valueOf(caseId)
        metadataParams.processedReportId = processReportId
        metadataParams.tenantId = Long.valueOf(tenantId)
        metadataParams.senderId = senderId != null ? Long.valueOf(senderId) : null
        metadataParams.masterVersionNum = Long.valueOf(versionNum)

        boolean isInbound = false

        def metadataRecord
        if (metadataParams.senderId && metadataParams.senderId > -1) {
            metadataRecord = InboundDrilldownMetadata.getMetadataRecord(metadataParams).get()
            if(metadataRecord){
                workflowJustificationList= WorkflowJustification.findAllByInboundMetadata(metadataRecord, [sort: 'dateCreated', order: 'desc']).collect {
                    it.toWorkflowJustificationMap()
                }
            }
            isInbound=true
        }
        else {
            metadataRecord = DrilldownCLLMetadata.getMetadataRecord(metadataParams).get()
            if(metadataRecord){
                workflowJustificationList= WorkflowJustification.findAllByDrilldownCLLMetadata(metadataRecord, [sort: 'dateCreated', order: 'desc']).collect {
                    it.toWorkflowJustificationMap()
                }
            }
        }

        Map stateList = workFlowStatesAndApplications(initialState, isInbound)

        render([currentState:metadataRecord?.workflowState ? metadataRecord.workflowState : WorkflowState.defaultWorkState, stateList:stateList,workflowJustificationList:workflowJustificationList,reportId:cllRowId,
                actionItems: 0] as JSON)
    }

    Map workFlowStatesAndApplications(String initialState, boolean isInbound) {

        List states = []
        Map actions = [:]
        Map rules = [:]
        Map needApproval = [:]
        WorkflowState initialStateObj = WorkflowState.findByNameAndIsDeleted(initialState, false)
        WorkflowConfigurationTypeEnum configurationTypeEnum = isInbound ? WorkflowConfigurationTypeEnum.PVC_INBOUND : WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY
        List<WorkflowRule> workflowRules = WorkflowRule.getAllByConfigurationTypeAndInitialState(configurationTypeEnum,initialStateObj).list()
        if(workflowRules){
            workflowRules?.each {
                actions.put(it.targetState?.name, it.targetState?.reportActionsAsList)
                states.add(it.targetState)
                rules.put(it.id, it.targetState)
                needApproval.put(it.id, it.needApproval)
            }
        }
        [actions: actions, states: states, rules: rules, needApproval: needApproval]
    }


    def reportRequest(Long reportRequestId, String initialState) {
        ReportRequest reportRequest = ReportRequest.get(reportRequestId)
        List<Map> workflowJustificationList = []
        Map stateList = getTargetStates(initialState, WorkflowConfigurationTypeEnum.REPORT_REQUEST)
        if (reportRequest) {
            workflowJustificationList = WorkflowJustification.findAllByReportRequest(reportRequest, [sort: 'dateCreated', order: 'desc']).collect {
                it.toWorkflowJustificationMap()
            }
        }
        render([currentState: reportRequest.workflowState, stateList: stateList, workflowJustificationList: workflowJustificationList, reportId: params.id,
                actionItems : 0] as JSON)
    }

    def publisherDocument(Long reportId, String initialState) {
        PublisherReport report = PublisherReport.get(reportId)
        List<Map> workflowJustificationList = []
        boolean qc = params.publisherDocumentType==WorkflowConfigurationTypeEnum.PUBLISHER_FULL_QC.name()
        Map stateList = getTargetStates(initialState, qc?WorkflowConfigurationTypeEnum.PUBLISHER_FULL_QC:WorkflowConfigurationTypeEnum.PUBLISHER_FULL)

        if (report) {
            if(qc){
                workflowJustificationList = WorkflowJustification.findAllByPublisherReportQc(report, [sort: 'dateCreated', order: 'desc']).collect {it.toWorkflowJustificationMap()}
            }else {
                workflowJustificationList = WorkflowJustification.findAllByPublisherReport(report, [sort: 'dateCreated', order: 'desc']).collect {it.toWorkflowJustificationMap()}
            }
        }
        render([currentState: report.workflowState ?: WorkflowState.getDefaultWorkState(), stateList: stateList, workflowJustificationList: workflowJustificationList, reportId: params.id,
                actionItems : 0] as JSON)
    }

    def publisherSection(Long sectionId, String initialState) {
        PublisherConfigurationSection section = PublisherConfigurationSection.get(sectionId)
        List<Map> workflowJustificationList = []
        Map stateList = getTargetStates(initialState, WorkflowConfigurationTypeEnum.PUBLISHER_SECTION)

        if (section) {
            workflowJustificationList = WorkflowJustification.findAllByPublisherSection(section, [sort: 'dateCreated', order: 'desc']).collect {
                it.toWorkflowJustificationMap()
            }
        }
        render([currentState: section.workflowState?:WorkflowState.getDefaultWorkState(), stateList: stateList, workflowJustificationList: workflowJustificationList, reportId: params.id,
                actionItems : 0] as JSON)
    }


    private Map getTargetStates(String initialState, WorkflowConfigurationTypeEnum type) {
        List states = []
        Map actions = [:]
        Map rules = [:]
        WorkflowState initialStateObj = WorkflowState.findByNameAndIsDeleted(initialState, false)
        List<WorkflowRule> workflowRules = WorkflowRule.findAllByConfigurationTypeEnumAndInitialStateAndIsDeleted(type, initialStateObj, false)
        if (workflowRules) {
            workflowRules?.each {
                actions.put(it.targetState?.name, it.targetState?.reportActionsAsList)
                states.add(it.targetState)
                rules.put(it.id, it.targetState)
            }
        }
        [actions: actions, states: states, rules: rules]
    }

    def save(WorkflowJustification workflowJustificationInstance) {
        def workflowRule = workflowJustificationInstance.workflowRule
        if (workflowRule.needApproval) {
            if (!params.password) {
                sendResponse(500, message(code: "app.label.workflow.rule.fillLogon").toString());
                return
            }
            if (!ldapService.isLoginPasswordValid(userService.currentUser.username, params.password)) {
                sendResponse(500, message(code: "app.label.workflow.rule.approvl.fail").toString());
                return
            }
        }
        workflowJustificationInstance.routedBy=userService.currentUser
        ExecutedReportConfiguration executedReportConfiguration= workflowJustificationInstance.executedReportConfiguration
        try {
            if (executedReportConfiguration.running) {
                sendResponse(500, message(code: "workflowJustification.executedReportConfiguration.executing", args: ["${executedReportConfiguration.reportName}-${executedReportConfiguration.numOfExecutions}"]).toString())
                return
            }
            if (!workflowRule.canExecute(userService.currentUser)) {
                sendResponse(500, message(code: "app.label.workflow.rule.forbidden", args: ["${executedReportConfiguration.reportName}-${executedReportConfiguration.numOfExecutions}"]).toString())
                return
            }
            if (WorkflowState.findByIdAndIsDeleted(params.toState.id, true)) {
                sendResponse(500, message(code: "app.label.workflow.rule.change.fail", args: ["${executedReportConfiguration.reportName}-${executedReportConfiguration.numOfExecutions}"]).toString())
                return
            }
            CRUDService.save(workflowJustificationInstance)
            executedReportConfiguration.workflowState = workflowJustificationInstance.toState
            CRUDService.update(executedReportConfiguration)

            def responseObject =[code: "app.periodicReportConfiguration.state.update.success", action: null]
            if (workflowRule && workflowRule.defaultReportAction && (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration || executedReportConfiguration instanceof ExecutedIcsrReportConfiguration || executedReportConfiguration instanceof ExecutedConfiguration )) {
                handleReportAction(executedReportConfiguration, workflowRule.defaultReportAction, responseObject)
            }
            sendResponse(200, message(code: responseObject.code).toString(), responseObject.action, null, workflowJustificationInstance.toState.name,  executedReportConfiguration.workflowState?.reportActionsAsList?.join(","))
        } catch (ValidationException ex){
            sendResponse(404, ex.errors.allErrors.collect{ message(error: it)}.join(","));
        }
        catch (Exception ex) {
            log.error("Unexpected error in workFlowJustification -> save", ex)
            sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.failure").toString());
        }
    }

    def saveReportRequest(WorkflowJustification workflowJustificationInstance) {
        WorkflowRule workflowRule = workflowJustificationInstance.workflowRule
        workflowJustificationInstance.routedBy = userService.currentUser
        ReportRequest reportRequest = workflowJustificationInstance.reportRequest
        try {
            if (!workflowRule.canExecute(userService.currentUser)) {
                sendResponse(500, message(code: "app.label.workflow.rule.forbidden", args: ["${reportRequest.reportName}-${reportRequest.owner}"]).toString())
                return
            }
            CRUDService.save(workflowJustificationInstance)
            def oldReportRequestRef = reportRequestService.getReportRequestMap(reportRequest)
            reportRequest.workflowState = workflowJustificationInstance.toState
            CRUDService.update(reportRequest)

            Set<String> recipients = reportRequestService.getNotificationRecipients(reportRequest, Constants.WORKFLOW_UPDATE)
            def repReqSubject = ViewHelper.getMessage('app.notification.reportRequest.email.updated')
            reportRequestService.sendReportRequestNotification(reportRequest, recipients, 'update', oldReportRequestRef, repReqSubject)
            sendResponse(200, message(code: "app.periodicReportConfiguration.state.update.success").toString())
        } catch (ValidationException ex) {
            sendResponse(404, ex.errors.allErrors.collect { message(error: it) }.join(","));
        }
        catch (Exception ex) {
            log.error("Unexpected error in workFlowJustification -> saveReportRequest", ex)
            sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.failure").toString());
        }
    }

    def saveQualityWorkFlow(WorkflowJustification workflowJustificationInstance) {
        if (!params.selectedJson || params.selectedJson == "[]") {
            Long id = workflowJustificationInstance.qualitySubmission?.id
            id=id?:workflowJustificationInstance.qualityCaseData?.id
            id=id?:workflowJustificationInstance.qualitySampling?.id
            params.selectedJson = "["+id+"]";
        }
        try {
            String errorMessage = null
            List errorRows = null
            if (params.selectedJson && params.selectedJson != "[]") {
                List results = []

                Map casesToUpdate = qualityService.getIdToUpdateWorkflow(params.dataType, JSON.parse(params.selectedJson).collect { it as Long }, workflowJustificationInstance.fromState.id)
                if (casesToUpdate.badIds) {
                    sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.warn"), null, casesToUpdate.badIds)
                    return
                }
                WorkflowJustification.withTransaction { status ->

                    for (def it : casesToUpdate.goodIds) {
                        WorkflowJustification currentWorkflowJustificationInstance = new WorkflowJustification(workflowJustificationInstance.properties)
                        if(currentWorkflowJustificationInstance.qualityCaseData)  currentWorkflowJustificationInstance.qualityCaseData = QualityCaseData.get(it as Long)
                        if(currentWorkflowJustificationInstance.qualitySubmission)  currentWorkflowJustificationInstance.qualitySubmission = QualitySubmission.get(it as Long)
                        if(currentWorkflowJustificationInstance.qualitySampling)  currentWorkflowJustificationInstance.qualitySampling = QualitySampling.get(it as Long)
                        results << workflowService.assignPvqWorkflow(currentWorkflowJustificationInstance,true,false)
                    }
                    if (results.find { !it.success }) {
                        if(results.find { !it.message }){
                            errorMessage = message(code: "app.periodicReportConfiguration.state.update.warn")
                        }
                        errorMessage = results.find { !it.success }?.message
                        errorRows = results.findAll { !it.success }?.collect { it.rowInfo }
                        status.setRollbackOnly()
                    }
                }
            }
            if (errorMessage) {
                sendResponse(500, errorMessage, null, errorRows)
            } else {
                sendResponse(200, message(code: "app.periodicReportConfiguration.state.update.success").toString())
            }
        } catch (ValidationException ex) {
            sendResponse(404, ex.errors.allErrors.collect { message(error: it) }.join(","));
        }
        catch (Exception ex) {
            log.error("Unexpected error in workFlowJustification -> savePVCWorkFlow", ex)
            sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.failure").toString());
            ex.printStackTrace()
        }
    }

    def savePVCWorkFlow(WorkflowJustification workflowJustificationInstance) {
        if (workflowJustificationInstance?.inboundMetadata?.senderId != null && Long.valueOf(workflowJustificationInstance?.inboundMetadata?.senderId) > -1)
            workflowJustificationInstance.drilldownCLLMetadata = null
        else
            workflowJustificationInstance.inboundMetadata = null
        try {
            String errorMessage = null
            List errorRows = null
            if (params.selectedJson && params.selectedJson != "[]") {
                List results = []
                WorkflowJustification.withTransaction { status ->

                    for (def it : JSON.parse(params.selectedJson)) {
                        WorkflowJustification currentWorkflowJustificationInstance = new WorkflowJustification(workflowJustificationInstance.properties)
                        if (it?.senderId != null && (Long.valueOf(it?.senderId) > -1)) {
                            currentWorkflowJustificationInstance.inboundMetadata = new InboundDrilldownMetadata(caseId: it.caseId as Long, tenantId: it.tenantId as Long, caseVersion: it.versionNum as Long, senderId: it.senderId as Long)
                            currentWorkflowJustificationInstance.drilldownCLLMetadata = null
                        }
                        else {
                            currentWorkflowJustificationInstance.drilldownCLLMetadata.caseId = it.caseId as Long
                            currentWorkflowJustificationInstance.drilldownCLLMetadata.processedReportId = it.processedReportId as Long
                            currentWorkflowJustificationInstance.drilldownCLLMetadata.tenantId = it.tenantId as Long
                            currentWorkflowJustificationInstance.inboundMetadata = null
                        }
                        results << workflowService.assignPvcWorkflow(currentWorkflowJustificationInstance,true,it.cllRowId)
                    }
                    if (results.find { !it.success }) {
                        if(results.find { !it.message }){
                            errorMessage = message(code: "app.periodicReportConfiguration.state.update.warn")
                        }else{
                            errorMessage = results.findAll { !it.success }?.collect { it.message }
                        }
                        errorRows = results.findAll { !it.success }?.collect { it.rowInfo }
                        status.setRollbackOnly()
                    }
                }
            } else {
                Map result = workflowService.assignPvcWorkflow(workflowJustificationInstance,true,params.cllRowId)
                if (!result.success) errorMessage = result.message
            }
            if (errorMessage) {
                sendResponse(500, errorMessage, null, errorRows)
            } else {
                sendResponse(200, message(code: "app.periodicReportConfiguration.state.update.success").toString())
            }
        } catch (ValidationException ex) {
            sendResponse(404, ex.errors.allErrors.collect { message(error: it) }.join(","));
        }
        catch (Exception ex) {
            log.error("Unexpected error in workFlowJustification -> savePVCWorkFlow", ex)
            sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.failure").toString());
        }
    }

    def savePublisherSection(WorkflowJustification workflowJustificationInstance) {
        workflowJustificationInstance.routedBy = userService.currentUser
        Set<PublisherConfigurationSection> sections = [workflowJustificationInstance.publisherSection]
        def responseObject = [code: "app.periodicReportConfiguration.state.update.success", action: null]
        try {
            if (params.setWorkflowStateForAll) {
                sections = workflowJustificationInstance.publisherSection.executedConfiguration.publisherConfigurationSections.findAll {
                    it.isVisible(userService.currentUser) && (it.workflowState?.id == workflowJustificationInstance.fromState?.id)
                }
            }
            sections.each { section ->
                def workflowRule = workflowJustificationInstance.workflowRule
                if (!workflowRule.canExecute(userService.currentUser)) {
                    sendResponse(500, message(code: "app.label.workflow.rule.forbidden"))
                    return
                }
                WorkflowJustification wf = new WorkflowJustification(
                        fromState: workflowJustificationInstance.fromState,
                        toState: workflowJustificationInstance.toState,
                        routedBy: workflowJustificationInstance.routedBy,
                        description: workflowJustificationInstance.description,
                        publisherSection: section,
                        workflowRule: workflowJustificationInstance.workflowRule)

                section.workflowState = workflowJustificationInstance.toState


                if (workflowRule && workflowRule.defaultReportAction) {
                    if (workflowRule.defaultReportAction == ReportActionEnum.PUBLISHER_GEN_DRAFT) {
                        if (!section.getLastPublisherExecutedTemplates() && (section.publisherTemplate || section.filename)) {
                            publisherService.processSection(section, true)
                            publisherService.pushTheLastSectionChanges(section)
                        }
                    } else if (workflowRule.defaultReportAction == ReportActionEnum.PUBLISHER_FINAL) {
                        PublisherExecutedTemplate executedTemplate = section.getLastPublisherExecutedTemplates()
                        if (!executedTemplate) {
                            sendResponse(500, message(code: "app.label.PublisherTemplate.error.noExecutedTemplate").toString());
                            return
                        }
                        if (executedTemplate && (executedTemplate.status != PublisherExecutedTemplate.Status.FINAL)) {
                            publisherService.pullTheLastSectionChanges(section)
                            publisherService.updatePendingParameters(section)
                            executedTemplate = section.getLastPublisherExecutedTemplates()
                            if (!(section.pendingManual == 0 && section.pendingVariable == 0)) {
                                sendResponse(500, message(code: "app.label.PublisherTemplate.error.noCompletedDocument").toString());
                                return
                            }
                            executedTemplate.status = PublisherExecutedTemplate.Status.FINAL
                            CRUDService.update(executedTemplate)
                        }
                    }
                }
                CRUDService.update(section)
                CRUDService.save(wf)
            }
            sendResponse(200, message(code: responseObject.code).toString(), responseObject.action)
        } catch (ValidationException ex) {
            sendResponse(404, ex.errors.allErrors.collect { message(error: it) }.join(","));
        }
        catch (Exception ex) {
            ex.printStackTrace(System.out)
            sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.failure").toString());
        }
    }

    def savePublisherDocument(WorkflowJustification workflowJustificationInstance) {
        workflowJustificationInstance.routedBy = userService.currentUser
        Set<PublisherReport> reports = [workflowJustificationInstance.publisherReport]
        def responseObject = [code: "app.periodicReportConfiguration.state.update.success", action: null]
        try {
            if (params.setWorkflowStateForAll) {
                reports = workflowJustificationInstance.publisherReport.executedReportConfiguration.publisherReports.findAll {
                    it.isVisible(userService.currentUser) && (it.workflowState?.id == workflowJustificationInstance.fromState?.id)
                }
            }
            reports.each { report ->
                def workflowRule = workflowJustificationInstance.workflowRule
                if (!workflowRule.canExecute(userService.currentUser)) {
                    sendResponse(500, message(code: "app.label.workflow.rule.forbidden"))
                    return
                }
                boolean qc = params.publisherDocumentType == WorkflowConfigurationTypeEnum.PUBLISHER_FULL_QC.name()
                WorkflowJustification wf = new WorkflowJustification(
                        fromState: workflowJustificationInstance.fromState,
                        toState: workflowJustificationInstance.toState,
                        routedBy: workflowJustificationInstance.routedBy,
                        description: workflowJustificationInstance.description,
                        publisherReport: (qc?null:report),
                        publisherReportQc: (qc?report:null),
                        workflowRule: workflowJustificationInstance.workflowRule)

                report."${qc ? 'qcWorkflowState' : 'workflowState'}" = workflowJustificationInstance.toState
                if (workflowRule && workflowRule.defaultReportAction == ReportActionEnum.PUBLISHER_GEN_DRAFT) {
                    report.published = true
                }
                CRUDService.update(report)
                CRUDService.save(wf)
            }
            sendResponse(200, message(code: responseObject.code).toString(), responseObject.action)
        } catch (ValidationException ex) {
            sendResponse(404, ex.errors.allErrors.collect { message(error: it) }.join(","));
        }
        catch (Exception ex) {
            ex.printStackTrace(System.out)
            sendResponse(500, message(code: "app.periodicReportConfiguration.state.update.failure").toString());
        }
    }

    private def handleReportAction(ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction, def responseObject) {
        switch (reportAction) {
            case ReportActionEnum.GENERATE_DRAFT:
            case ReportActionEnum.GENERATE_CASES:
            case ReportActionEnum.GENERATE_CASES_DRAFT:
            case ReportActionEnum.GENERATE_FINAL:
            case ReportActionEnum.GENERATE_CASES_FINAL:
                if (executedPeriodicReportConfiguration
                        && executedPeriodicReportConfiguration.hasGeneratedCasesData
                        && !executedPeriodicReportConfiguration.running) {
                    executionStatusService.generateDraft(executedPeriodicReportConfiguration, reportAction)
                    responseObject.code = "app.periodicReportConfiguration.state.update.${reportAction.key}"
                } else {
                    log.warn("Execution for ExecutedPeriodicReport Id ${executedPeriodicReportConfiguration.id} is skipped...")
                }
                break;
            case ReportActionEnum.MARK_AS_SUBMITTED:
                if (executedPeriodicReportConfiguration instanceof ExecutedPeriodicReportConfiguration) {
                    responseObject.code = "app.periodicReportConfiguration.state.update.${reportAction.key}"
                    responseObject.action = "markAsSubmitted"
                }
                break;
            case ReportActionEnum.SEND_TO_DMS:
                if (executedPeriodicReportConfiguration instanceof ExecutedPeriodicReportConfiguration) {
                    responseObject.code = "app.periodicReportConfiguration.state.update.${reportAction.key}"
                    responseObject.action = "sendToDms"
                }
                break;
            case ReportActionEnum.ARCHIVE:
                executedPeriodicReportConfiguration.archived = true
                executedPeriodicReportConfiguration.executedReportUserStates?.each { if (!it.isFavorite) it.isArchived = true }
                CRUDService.update(executedPeriodicReportConfiguration)
                break
        }
    }

    private def sendResponse(stat, msg, act = null, Collection errorRows = null, String worflowName = null, String actions = null) {
        response.status = stat
        Map responseMap = [
                message  : msg,
                status   : stat,
                action   : act,
                errorRows: errorRows,
                worflowName:worflowName
        ]
        if (actions) responseMap.actions = actions
        render(contentType: "application/json", responseMap as JSON)
    }
}
