package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.AssignmentRuleEnum
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReasonOfDelayFieldEnum
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import org.hibernate.Session

class WorkflowService {

    static transactional = false

    def executionStatusService
    def CRUDService
    def reportSubmissionService
    def dmsService
    def userService
    def qualityService
    def seedDataService
    def reportExecutorService
    def customMessageService

    void moveAutomationStatuses() {
        Set<WorkflowRule> autoRules = WorkflowRule.findAllByIsDeletedAndAutoExecuteInDaysIsNotNull(false)
        if (autoRules) {
            Set<WorkflowRule> pvrRules = autoRules.findAll { it.configurationTypeEnum in [WorkflowConfigurationTypeEnum.PERIODIC_REPORT, WorkflowConfigurationTypeEnum.ADHOC_REPORT] }
            if (pvrRules && pvrRules.size() > 0) {
                WorkflowJustification.getAllLatestJustifications(pvrRules*.initialState.toList())?.each { transition ->
                    WorkflowRule rule = pvrRules.find { it.initialState == transition.toState }
                    Date date = rule.autoExecuteExcludeWeekends ? DateUtil.minusDaysSkippingWeekends(new Date(), rule.autoExecuteInDays) : (new Date().minus(rule.autoExecuteInDays))
                    if (transition.dateCreated < date) {
                        ExecutedReportConfiguration report = ExecutedReportConfiguration.findByIdAndIsDeleted(transition.executedReportConfigurationId, false)
                        if (report?.instanceOf(ExecutedPeriodicReportConfiguration) || report?.instanceOf(ExecutedConfiguration)) {
                            moveStatus(report, rule)
                        }
                    }
                }
            }
            autoRules.each { rule ->
                Date date = rule.autoExecuteExcludeWeekends ? DateUtil.minusDaysSkippingWeekends(new Date(), rule.autoExecuteInDays) : (new Date().minus(rule.autoExecuteInDays))
                if (rule.configurationTypeEnum in [WorkflowConfigurationTypeEnum.PERIODIC_REPORT, WorkflowConfigurationTypeEnum.ADHOC_REPORT]) {
                    if (rule.initialStateId == WorkflowState.getDefaultWorkState().id) {
                        List<ExecutedReportConfiguration> reportsToProcess = ExecutedReportConfiguration.findAllByIsDeletedAndLastRunDateLessThanAndWorkflowState(false, date, WorkflowState.getDefaultWorkState())
                        reportsToProcess?.each { report ->
                            if (((rule.configurationTypeEnum == WorkflowConfigurationTypeEnum.PERIODIC_REPORT) && report.instanceOf(ExecutedPeriodicReportConfiguration)) ||
                                    ((rule.configurationTypeEnum == WorkflowConfigurationTypeEnum.ADHOC_REPORT) && report.instanceOf(ExecutedConfiguration)))
                                moveStatus(report, rule)
                        }
                    }
                } else if (rule.configurationTypeEnum in WorkflowConfigurationTypeEnum.getAllQuality()) {
                    if (rule.configurationTypeEnum == WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA) {
                        List<QualityCaseData> list = QualityCaseData.findAllByIsDeletedAndWorkflowStateUpdatedDateLessThanAndWorkflowState(false, date, rule.initialState)
                        if (list) {
                            qualityService.getIdToUpdateWorkflow(PvqTypeEnum.CASE_QUALITY.toString(), list*.id, rule.initialStateId).
                                    goodIds?.each { movePvqPvcStatus(QualityCaseData.get(it), rule) }
                        }
                    } else if (rule.configurationTypeEnum == WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION) {
                        List<QualitySubmission> list = QualitySubmission.findAllByIsDeletedAndWorkflowStateUpdatedDateLessThanAndWorkflowState(false, date, rule.initialState)
                        if (list) {
                            qualityService.getIdToUpdateWorkflow(PvqTypeEnum.SUBMISSION_QUALITY.toString(), list*.id, rule.initialStateId).
                                    goodIds?.each { movePvqPvcStatus(QualitySubmission.get(it), rule) }
                        }
                    } else {
                        String samplingName = WorkflowConfigurationTypeEnum.getSamplingNameByType(rule.configurationTypeEnum)
                        List<QualitySampling> list = QualitySampling.findAllByIsDeletedAndWorkflowStateUpdatedDateLessThanAndWorkflowStateAndType(false, date, rule.initialState, samplingName)
                        if (list) {
                            qualityService.getIdToUpdateWorkflow(samplingName, list*.id, rule.initialStateId).
                                    goodIds?.each { movePvqPvcStatus(QualitySampling.get(it), rule) }
                        }
                    }
                } else if (rule.configurationTypeEnum == WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY) {
                    DrilldownCLLMetadata.findAllByWorkflowStateUpdatedDateLessThanAndWorkflowState(date, rule.initialState)
                            ?.each { movePvqPvcStatus(it, rule) }
                } else if (rule.configurationTypeEnum == WorkflowConfigurationTypeEnum.PVC_INBOUND) {
                    InboundDrilldownMetadata.findAllByWorkflowStateUpdatedDateLessThanAndWorkflowState(date, rule.initialState)
                            ?.each { movePvqPvcStatus(it, rule) }
                }
            }
        }
    }

    void movePvqPvcStatus(def entity, WorkflowRule rule) {
        rule = rule.load(rule.id)
        try {
            movePvqPvcStatusTrn(entity, rule)
        } catch (Exception ex) {
            log.error("Workflow state for entity: ${entity.id} was not changed due to the following error ", ex)
        }
    }

    @Transactional
    void movePvqPvcStatusTrn(def entity, WorkflowRule rule) {
        if (!rule.targetState?.isDeleted) { //If Target state is deleted then no state change.
            WorkflowJustification justification = new WorkflowJustification()
            justification.fromState = rule.initialState
            justification.toState = rule.targetState
            justification.routedBy = seedDataService.getApplicationUserForSeeding() ?: rule.owner
            justification.description = ViewHelper.getMessage("app.label.workflow.rule.executedAutomatically")
            justification.qualitySampling = (entity instanceof QualitySampling ? entity : null)
            justification.qualityCaseData = (entity instanceof QualityCaseData ? entity : null)
            justification.qualitySubmission = (entity instanceof QualitySubmission ? entity : null)
            justification.drilldownCLLMetadata = (entity instanceof DrilldownCLLMetadata ? entity : null)
            justification.inboundMetadata = (entity instanceof InboundDrilldownMetadata ? entity : null)
            justification.workflowRule = rule
            CRUDService.save(justification)
            if ((entity instanceof DrilldownCLLMetadata) || (entity instanceof InboundDrilldownMetadata))
                assignPvcWorkflow(justification, false)
            else
                assignPvqWorkflow(justification, false)
        }
    }


    private void moveStatus(ExecutedReportConfiguration report, WorkflowRule rule) {
        rule = rule.load(rule.id)
        if (!report.running && (!rule.targetState?.isDeleted)) { //If Target state is deleted then no state change.
            ExecutedReportConfiguration.withSession { Session session ->
                try {
                    report = report.attach()
                    ExecutedReportConfiguration.withNewTransaction { status ->
                        if (rule.defaultReportAction) {
                            Tenants.withId(report.tenantId as Integer){
                                handleReportAction(report, rule.defaultReportAction)
                            }
                        }
                        report.setWorkflowState(rule.targetState)
                        CRUDService.update(report)
                        WorkflowJustification justification = new WorkflowJustification()
                        justification.fromState = rule.initialState
                        justification.toState = rule.targetState
                        justification.routedBy = seedDataService.getApplicationUserForSeeding() ?: rule.owner
                        justification.description = ViewHelper.getMessage("app.label.workflow.rule.executedAutomatically")
                        justification.executedReportConfiguration = report
                        justification.workflowRule = rule
                        CRUDService.save(justification)
                        session.flush()
                    }
                } catch (Exception ex) {
                    log.error("Workflow state for executed report: ${report.id} was not changed due to the following error ", ex)
                } finally {
                    try {
                        session.clear()
                        if (!session.isConnected()) {
                            log.error("Session got disconnected due to previous errors")
                        }
                    } catch (Exception e) {
                        log.error(e.message)
                    }
                }
            }
        }
    }

    private void handleReportAction(ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction) {
        switch (reportAction) {
            case ReportActionEnum.GENERATE_DRAFT:
            case ReportActionEnum.GENERATE_CASES:
            case ReportActionEnum.GENERATE_CASES_DRAFT:
            case ReportActionEnum.GENERATE_FINAL:
            case ReportActionEnum.GENERATE_CASES_FINAL:
                if (executedPeriodicReportConfiguration instanceof ExecutedPeriodicReportConfiguration
                        && executedPeriodicReportConfiguration.hasGeneratedCasesData
                        && !executedPeriodicReportConfiguration.running) {
                    ExecutingEntityTypeEnum entityType = executionStatusService.getEntityType(reportAction)
                    ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedPeriodicReportConfiguration.id, entityType: entityType, reportVersion: executedPeriodicReportConfiguration.numOfExecutions,
                            startTime: System.currentTimeMillis(), owner: executedPeriodicReportConfiguration.owner, reportName: executedPeriodicReportConfiguration.reportName,
                            attachmentFormats: executedPeriodicReportConfiguration?.executedDeliveryOption?.attachmentFormats, sharedWith: executedPeriodicReportConfiguration?.allSharedUsers?.unique(), tenantId: executedPeriodicReportConfiguration?.tenantId)
                    executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
                    executionStatus.frequency = FrequencyEnum.RUN_ONCE
                    executionStatus.nextRunDate = new Date()
                    CRUDService.instantSaveWithoutAuditLog(executionStatus)
                }
                break;
            case ReportActionEnum.MARK_AS_SUBMITTED:
                if (executedPeriodicReportConfiguration instanceof ExecutedPeriodicReportConfiguration){
                Map params = [comment         : ViewHelper.getMessage("app.label.workflow.rule.executedAutomatically"),
                              scheduleDateJSON: '{"startDateTime":"' + (new Date()).format("yyyy-MM-dd'T'HH:mmXXX") + '"}',
                              dueDate         : executedPeriodicReportConfiguration.dueDate,
                              dateCreated     : new Date(),
                              lastUpdated     : new Date(),
                              createdBy       : "application",
                              modifiedBy      : "application"

                ]
                reportSubmissionService.submitReport(executedPeriodicReportConfiguration, [executedPeriodicReportConfiguration.getPrimaryReportingDestination()] as Set<String>, params)
                }
                break;
            case ReportActionEnum.SEND_TO_DMS:
                if (executedPeriodicReportConfiguration instanceof ExecutedPeriodicReportConfiguration)
                dmsService.uploadReport(executedPeriodicReportConfiguration)
                break;
            case ReportActionEnum.ARCHIVE:
                executedPeriodicReportConfiguration.archived = true
                executedPeriodicReportConfiguration.executedReportUserStates?.each {
                    if (!it.isFavorite) it.isArchived = true
                }
                CRUDService.update(executedPeriodicReportConfiguration)
                break
        }
    }

    Map assignPvcWorkflow(WorkflowJustification workflowJustificationInstance, Boolean userValidation = true, String cllRowId = null, Boolean editROD = false) {
        Map result = [success: true, message: null, rowInfo: null]
        User user = !userValidation ? workflowJustificationInstance.routedBy : userService.currentUser
        boolean canExecute = false
        boolean isInbound = false
        workflowJustificationInstance.routedBy = user
        def drillDownDataMetaData = workflowJustificationInstance.drilldownCLLMetadata?:workflowJustificationInstance.inboundMetadata
        Map metadataParams = [:]
        WorkflowRule workFlowRule = null
        def metadataRecord
        metadataParams.masterCaseId = drillDownDataMetaData.caseId
        metadataParams.tenantId = drillDownDataMetaData.tenantId
        if(drillDownDataMetaData instanceof DrilldownCLLMetadata) {
            metadataParams.processedReportId = drillDownDataMetaData.processedReportId
            metadataRecord = DrilldownCLLMetadata.getMetadataRecord(metadataParams).get()
        }
        else if (drillDownDataMetaData instanceof InboundDrilldownMetadata) {
            isInbound = true
            metadataParams.masterVersionNum = drillDownDataMetaData.caseVersion
            metadataParams.senderId = drillDownDataMetaData.senderId
            metadataRecord = InboundDrilldownMetadata.getMetadataRecord(metadataParams).get()
        }
        result.rowInfo = metadataParams
        if (metadataRecord == null) {
            metadataRecord = CRUDService.save(drillDownDataMetaData)
            workFlowRule = WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY)
        } else {
            if (workflowJustificationInstance.fromStateId != metadataRecord.workflowStateId) {
                result.success = false
                return result
            }
            ReasonOfDelayAppEnum ownerApp = isInbound ? ReasonOfDelayAppEnum.PVC_Inbound : ReasonOfDelayAppEnum.PVC
            if(!editROD) {
                List<String> rcaMandatoryFields = RCAMandatory.getMandatoryRCAFields(ownerApp, metadataRecord?.workflowState).list()*.toString()
                List dataList = reportExecutorService.getAllReasonOfDelayFromMart(metadataParams.masterCaseId as Long, metadataParams.processedReportId, metadataParams.tenantId as Long, metadataParams.senderId as Long, metadataParams.masterVersionNum as Long)
                String message = ''
                List<String> errorFields = []
                if (dataList.isEmpty() && rcaMandatoryFields) {
                    rcaMandatoryFields.each { field ->
                        ReasonOfDelayFieldEnum enumValue = ReasonOfDelayFieldEnum.valueOf(field)
                        if (enumValue && !errorFields.contains(enumValue)) {
                            errorFields.add(enumValue.getMessage())
                        }
                    }

                }
                dataList.sort { a, b -> b.primaryFlag <=> a.primaryFlag }.each {
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Issue_Type.toString()) && !it.lateValue) {
                        errorFields.add(ReasonOfDelayFieldEnum.Issue_Type.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause.toString()) && !it.rootCauseValue) {
                        errorFields.add(ReasonOfDelayFieldEnum.Root_Cause.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause_Class.toString()) && !it.rootCauseClassValue) {
                        errorFields.add(ReasonOfDelayFieldEnum.Root_Cause_Class.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.toString()) && !it.rootCauseSubCategoryValue) {
                        errorFields.add(ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Resp_Party.toString()) && !it.responsiblePartyValue) {
                        errorFields.add(ReasonOfDelayFieldEnum.Resp_Party.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Corrective_Action.toString()) && !it.correctiveActionValue) {
                        errorFields.add(ReasonOfDelayFieldEnum.Corrective_Action.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Preventive_Action.toString()) && !it.preventiveActionValue) {
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
                if (errorFields.size() > 0) {
                    result.success = false
                    message += errorFields.join(', ') + " can't be empty for "
                    if(cllRowId) {
                        DrilldownCLLData drilldownCLLData = DrilldownCLLData.findById(Long.valueOf(cllRowId))
                        Map dataMap = new JsonSlurper().parseText(drilldownCLLData.cllRowData)
                        message += " Case #: " + dataMap['masterCaseNum'] + " Fup #: " + dataMap['masterFupNum'] + " in Workflow State : " + metadataRecord?.workflowState
                        message += ", please fill the required field(s).<br>"
                    }
                    result.message = message
                    log.error("Workflow state for entity: ${workflowJustificationInstance?.id}  and was not changed due to mandatory fileds: ${message}PVC/Inbound Case")
                    return result
                }
            }
            drillDownDataMetaData.id = metadataRecord.id
            if(drillDownDataMetaData instanceof DrilldownCLLMetadata)
                workflowJustificationInstance.drilldownCLLMetadata = drillDownDataMetaData
            else if (drillDownDataMetaData instanceof InboundDrilldownMetadata)
                workflowJustificationInstance.inboundMetadata = drillDownDataMetaData
            workFlowRule = workflowJustificationInstance.workflowRule
            if (!userValidation) {
                canExecute = true
            }
            workflowJustificationInstance.assignedToUser = metadataRecord.assignedToUser
            workflowJustificationInstance.assignedToUserGroup = metadataRecord.assignedToUserGroup
        }
        if (!canExecute && !workFlowRule.canExecute(user)) {
            result.success = false
            result.message = ViewHelper.getMessage("app.label.workflow.rule.forbidden")
            return result
        }
        if (WorkflowState.findByIdAndIsDeleted(workFlowRule.targetState.id, true)) {
            result.success = false
            result.message = ViewHelper.getMessage("app.label.workflow.rule.change.fail")
            return result
        }
        CRUDService.save(workflowJustificationInstance)
        metadataRecord.workflowState = workflowJustificationInstance.toState
        metadataRecord.workflowStateUpdatedDate = new Date()
        metadataRecord.updateDueDate(workFlowRule)
        CRUDService.saveOrUpdate(metadataRecord)
        boolean isBasicRule = (workFlowRule?.assignmentRule == AssignmentRuleEnum.BASIC_RULE.name()) && workFlowRule?.assignedToUserGroup && (workFlowRule?.assignToUserGroup || workFlowRule?.autoAssignToUsers)
        boolean isAdvanceRule = workFlowRule?.assignmentRule == AssignmentRuleEnum.ADVANCED_RULE.name()
        if (workFlowRule && (isBasicRule || isAdvanceRule)) {
            AutoAssignment autoAssignment = new AutoAssignment()
            autoAssignment.caseId = metadataParams.masterCaseId
            autoAssignment.tenantId = metadataParams.tenantId
            if (metadataParams.processedReportId && (drillDownDataMetaData instanceof DrilldownCLLMetadata)) {
                autoAssignment.processedReportId = metadataParams.processedReportId
                autoAssignment.moduleName = WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY.getKey()
            }
            else if (metadataParams.senderId && (drillDownDataMetaData instanceof InboundDrilldownMetadata)) {
                autoAssignment.senderId = metadataParams.senderId
                autoAssignment.versionNumber = metadataParams.masterVersionNum
                autoAssignment.moduleName = WorkflowConfigurationTypeEnum.PVC_INBOUND.getKey()
            }
            autoAssignment.workflowRule = workFlowRule
            CRUDService.save(autoAssignment)
        }
        return result
    }

    Map assignPvqWorkflow(WorkflowJustification workflowJustificationInstance, Boolean userValidation = true, Boolean initStateValidation = true) {
        Map result = [success: true, message: null, rowInfo: null]
        boolean canExecute = false
        User user = userValidation ? userService.currentUser : workflowJustificationInstance.routedBy
        def workflowRule = workflowJustificationInstance.workflowRule
        workflowJustificationInstance.routedBy = user
        def qualityData = null
        WorkflowRule workFlowRule = workflowJustificationInstance.workflowRule
        String moduleName = ""
        String qualityType = ""
        String samplingType = ""
        if (workflowJustificationInstance.qualityCaseData) {
            qualityData = workflowJustificationInstance.qualityCaseData
            moduleName = WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA.getKey()
            qualityType = PvqTypeEnum.CASE_QUALITY.name()
        } else if (workflowJustificationInstance.qualitySubmission) {
            qualityData = workflowJustificationInstance.qualitySubmission
            moduleName = WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION.getKey()
            qualityType = PvqTypeEnum.SUBMISSION_QUALITY.name()
        } else if (workflowJustificationInstance.qualitySampling) {
            qualityData = workflowJustificationInstance.qualitySampling
            moduleName = qualityData.type
            qualityType = qualityData.type
            samplingType = qualityData.type
        }
        result.rowInfo = qualityData.id
        WorkflowState wfs = qualityData.workflowState
        List <String> rcaMandatoryFields = RCAMandatory.getMandatoryRCAFields(ReasonOfDelayAppEnum.PVQ, wfs).list()*.toString()
        Map data = [:]
        data.dataType = qualityType
        data.id = qualityData.id
        Map dataMap = qualityService.getAllIssueRcaForId(data, Tenants.currentId() as Long)
        List<Map> dataList = dataMap.data
        String message = ''
        List<String> errorFields = []
        if (dataList.isEmpty() && rcaMandatoryFields) {
            rcaMandatoryFields.each { field ->
                ReasonOfDelayFieldEnum enumValue = ReasonOfDelayFieldEnum.valueOf(field)
                if (enumValue && !errorFields.contains(enumValue)) {
                    errorFields.add(enumValue.getMessage())
                }
            }

        }
        dataList.sort { a, b -> b.primaryFlag <=> a.primaryFlag }.each {
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Issue_Type.toString()) && !it.lateValue) {
                errorFields.add(ReasonOfDelayFieldEnum.Issue_Type.getMessage())
            }
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause.toString()) && !it.rootCauseValue) {
                errorFields.add(ReasonOfDelayFieldEnum.Root_Cause.getMessage())
            }
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause_Class.toString()) && !it.rootCauseClassValue) {
                errorFields.add(ReasonOfDelayFieldEnum.Root_Cause_Class.getMessage())
            }
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.toString()) && !it.rootCauseSubCategoryValue) {
                errorFields.add(ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.getMessage())
            }
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Resp_Party.toString()) && !it.responsiblePartyValue) {
                errorFields.add(ReasonOfDelayFieldEnum.Resp_Party.getMessage())
            }
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Corrective_Action.toString()) && !it.correctiveActionValue) {
                errorFields.add(ReasonOfDelayFieldEnum.Corrective_Action.getMessage())
            }
            if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Preventive_Action.toString()) && !it.preventiveActionValue) {
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

        if (errorFields.size() > 0) {
            result.success = false
            message += rcaMandatoryFields.join(', ') + " " +  customMessageService.getMessage('app.workflow.mandatory.error.fields') + qualityData.caseNumber + " " +
                    customMessageService.getMessage('app.workflow.mandatory.state.fields') + wfs + " " + customMessageService.getMessage('app.workflow.mandatory.fields')

            result.message = message
            log.error("Workflow state for entity: ${workflowJustificationInstance?.id}  and was not changed due to mandatory fileds: ${message}Quality Case")
            return result
        }

        if (qualityData.workflowStateId != workFlowRule.initialStateId) {
            result.success = false
            return result
        }

        if (!workflowRule.canExecute(user)) {
            result.success = false
            result.message = ViewHelper.getMessage("app.label.workflow.rule.forbidden")
            return result
        }
        if (WorkflowState.findByIdAndIsDeleted(workFlowRule.targetState.id, true)) {
            result.success = false
            result.message = ViewHelper.getMessage("app.label.workflow.rule.change.fail")
            return result
        }
        workflowJustificationInstance.assignedToUser = qualityData.assignedToUser
        workflowJustificationInstance.assignedToUserGroup = qualityData.assignedToUserGroup
        CRUDService.save(workflowJustificationInstance)
        qualityData.workflowState = workflowJustificationInstance.toState
        qualityData.workflowStateUpdatedDate = new Date()
        qualityData.updateDueDate(workFlowRule)
        CRUDService.update(qualityData)
        boolean isBasicRule = (workFlowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.BASIC_RULE.name()) && workFlowRule?.assignedToUserGroup && (workFlowRule?.assignToUserGroup || workFlowRule?.autoAssignToUsers)
        boolean isAdvanceRule = workFlowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.ADVANCED_RULE.name()
        boolean correctProcessedReportId = (moduleName!=PvqTypeEnum.SUBMISSION_QUALITY.name()) || (qualityData.hasProperty("submissionIdentifier") && qualityData.submissionIdentifier && qualityData.submissionIdentifier.isNumber())
        if (workFlowRule && (isBasicRule || isAdvanceRule) && correctProcessedReportId) {
            AutoAssignment autoAssignment = new AutoAssignment()
            autoAssignment.caseNumber = qualityData.caseNumber
            autoAssignment.tenantId = qualityData.tenantId
            autoAssignment.processedReportId = qualityData.hasProperty("submissionIdentifier") ? qualityData.submissionIdentifier : null
            autoAssignment.moduleName = moduleName
            autoAssignment.workflowRule = workFlowRule
            autoAssignment.versionNumber = qualityData.versionNumber
            autoAssignment.type = samplingType
            CRUDService.save(autoAssignment)
        }

        return result

    }
}
