package com.rxlogix

import com.rxlogix.commandObjects.CaseCommand
import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.*
import com.rxlogix.customException.CaseSubmissionException
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.customException.InvalidCaseInfoException
import com.rxlogix.dto.*
import com.rxlogix.dynamicReports.reportTypes.crosstab.TransponsedDataSource
import com.rxlogix.enums.*
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import com.rxlogix.mapping.IcsrCaseQueue
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import com.rxlogix.signal.SignalReportInfo
import com.rxlogix.test.TestUtils
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SpotfireUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovyx.gpars.GParsPool
import oracle.jdbc.OracleTypes
import org.grails.core.exceptions.GrailsRuntimeException
import org.grails.web.json.JSONObject
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl
import org.joda.time.Period
import org.springframework.transaction.annotation.Propagation

import java.nio.channels.ClosedByInterruptException
import java.sql.Clob
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.text.SimpleDateFormat
import com.rxlogix.user.User
import com.rxlogix.helper.LocaleHelper

class ReportExecutorService {

    static transactional = false

    static final CASE_RECEIPT_DATE = "CM_INIT_REPT_DATE"
    private static final String DATETIME_FRMT = "dd-MMM-yyyy HH:mm:ss"
    private static final String DATETIME_FMT_ORA = "dd-MM-yyyy HH24:MI:SS"

    def configurationService
    def sqlGenerationService
    def CRUDService
    def emailService
    def oneDriveRestService
    def dmsService
    GrailsApplication grailsApplication
    def queryService
    def dynamicReportService
    def executionStatusService
    def userService
    def notificationService
    def caseSeriesService
    def signalIntegrationService
    def taskTemplateService
    def spotfireService
    def hazelService
    def icsrReportService
    def dataSource_pva
    def ganttService
    def utilService
    def detailedCaseSeriesService
    def icsrScheduleService
    def executorThreadInfoService
    def executedConfigurationService
    def reportResultService
    def reportService
    def etlJobService
    def icsrProfileAckService
    def sessionFactory;

    static final List<String> ROD_NON_DOMAIN_KEYS = ['correctiveDate', 'preventiveDate', 'actions', 'summary', 'investigation']
    static final List<String> ALLOWED_SUBMISSION_TYPES = ["Periodic", "Expedited"]


    /**
     * Look for and execute reports which need to be run
     * @return
     */
    void runConfigurations() throws Exception {
        List<Long> currentlyRunningIds = executorThreadInfoService.totalCurrentlyRunningIds
        List<Long> currentlyRunningConfigurations = ExecutionStatus.createCriteria().list([readOnly: true]) {
            projections {
                property('entityId')
            }
            or {
                if (currentlyRunningIds) {
                    and {
                        inList("id", currentlyRunningIds)
                        inList("entityType", [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION, ExecutingEntityTypeEnum.ICSR_CONFIGURATION])
                    }
                }
                and {
                    inList('executionStatus', [ReportExecutionStatusEnum.BACKLOG, ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.DELIVERING])
                    inList('entityType', [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION, ExecutingEntityTypeEnum.ICSR_CONFIGURATION])
                }
            }
        }
        ReportConfiguration scheduledConfiguration = ReportConfiguration.nextConfigurationToExecute(currentlyRunningConfigurations, [Configuration.class, PeriodicReportConfiguration.class, IcsrReportConfiguration.class]).get()
        if (scheduledConfiguration) {
            createExecutionStatus(scheduledConfiguration)
        }
    }

    void runCaseSeries() throws Exception {
        List<Long> currentlyRunningIds = executorThreadInfoService.totalCurrentlyRunningIds
        List<Long> currentlyRunningConfigurations = ExecutionStatus.createCriteria().list([readOnly: true]) {
            projections {
                property('entityId')
            }
            or {
                if (currentlyRunningIds) {
                    and {
                        inList("id", currentlyRunningIds)
                        eq("entityType", ExecutingEntityTypeEnum.CASESERIES)
                    }
                }
                and {
                    inList('executionStatus', [ReportExecutionStatusEnum.BACKLOG, ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.DELIVERING])
                    eq('entityType', ExecutingEntityTypeEnum.CASESERIES)
                }
            }
        }
        CaseSeries scheduledCaseSeries = CaseSeries.nextCaseSeriesToExecute(currentlyRunningConfigurations).get()
        if (scheduledCaseSeries) {
            createExecutionStatus(scheduledCaseSeries)
        }
    }

    //This method is used using job trigger & will assign the user/usergroup based on workflow rule.
    void runRuleForAssigningUsers(){
        Long totalAutoAssignment = AutoAssignment.count()
        if(totalAutoAssignment > 0){
            Sql sql
            Sql pvrsql
            int maxCount = Holders.config.getProperty('job.autoAssignment.maxRecordProcess', Integer)
            def loopCount = Math.ceil(totalAutoAssignment / maxCount)
            try {
                sql = new Sql(getReportConnection())
                pvrsql = new Sql(getReportConnectionForPVR())
                Date startTime = null
                for(int i=0; i < loopCount; i++) {
                    startTime = new Date();
                    def autoAssignmentList = AutoAssignment.findAll([max: maxCount, sort: "id"])
                    log.debug("Start Time For GGT Insertion: " + startTime)
                    String cleaningUpGttTable = "begin execute immediate ' begin pkg_pvr_app_util.p_truncate_table(''gtt_cll_report_data''); end;'; end;"
                    sql.execute(cleaningUpGttTable)
                    String insertStatement = "Begin "
                    autoAssignmentList?.each {
                        Integer set_id = it.workflowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.ADVANCED_RULE.name() ? 2 : 1
                        Integer assignedToUserGroup = it?.workflowRule?.assignToUserGroup ? 1 : 0
                        Integer autoAssignToUsers = it?.workflowRule?.autoAssignToUsers ? 1 : 0
                        String advancedRuleQuery = it.workflowRule?.advancedAssignment?.assignmentQuery?.replaceAll("(?i)'", "''")
                        String moduleName = ""
                        String type = null
                        Long version = null
                        Long senderId = null
                        if (it.moduleName.equals(WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA.getKey())) {
                            moduleName = "PVQ_CASE"
                            version = it.versionNumber
                        } else if (it.moduleName.equals(WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION.getKey())) {
                            moduleName = "PVQ_SUBMISSION"
                            version = it.versionNumber
                        } else if (it.moduleName.equals(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY.getKey())) {
                            moduleName = "PVC"
                        } else if (it.moduleName.equals(WorkflowConfigurationTypeEnum.PVC_INBOUND.getKey())) {
                            moduleName = "PVC_INBOUND"
                            version = it.versionNumber
                            senderId = it.senderId
                        } else {
                            moduleName = "PVQ_SAMPLING"
                            version = it.versionNumber
                            type = "'" + it.type + "'"
                        }
                        String userGroupIds = it.workflowRule?.assignedToUserGroup?.collect {
                            it.id as Long
                        }.join(",")
                        String caseNumber = it.caseNumber != null ? "'" + it.caseNumber + "'" : null
                        Date assigneeUpdatedDate = new Date()
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh.mm.ss.SSSSS a");
                        String formattedDate = sdf.format(assigneeUpdatedDate);
                        User assigner= User.findByUsername(utilService.getJobUser())
                        if(!assigner){
                            log.info("Application user is not present in the system")
                        }

                        insertStatement += "Insert into GTT_CLL_REPORT_DATA (SET_ID, CASE_NUM, CASE_ID, TENANT_ID, PK_1, PK_2, PK_3, PK_4, PK_10, TEXT_1, TEXT_2, TEXT_3, CLOB_1, NUMBER_50, TEXT_50)\n" +
                                "VALUES (${set_id}, ${caseNumber}, ${it.caseId}, ${it.tenantId}, ${it.processedReportId}, ${assignedToUserGroup}, ${autoAssignToUsers}, ${version}, ${senderId}, '${userGroupIds}', '${moduleName}', ${type}, '${advancedRuleQuery}','${assigner?.id}', " +
                                "'" + formattedDate + "');"
                    }
                    insertStatement += " END;"
                    sql.execute(insertStatement)
                    Date endTime = new Date();
                    log.debug("End Time For GTT Table : " + endTime)
                    TimeDuration duration = TimeCategory.minus(endTime, startTime)
                    log.debug("Difference in time to execution for GTT Table "+maxCount+ " records : " + duration)
                    startTime = new Date();
                    log.debug("Start Time For F ASSIGN USER : " + startTime)

                    //Assignment will be done through mart side and data will be populated in our PVQ/PVC table.
                    sql.call("{?= call f_assign_user()}", [Sql.NUMERIC]) { result ->
                        if (result > 0) {
                            endTime = new Date();
                            log.debug("End Time for F ASSIGN USER: " + endTime)
                            duration = TimeCategory.minus(endTime, startTime)
                            log.debug("Difference in time to execute for F ASSiGN User "+maxCount+ " records : " + duration)
                            WorkflowState workFlowInitialState = WorkflowState.findByName(WorkflowState.TRIGGER)
                            WorkflowState workFlowFinalState = WorkflowState.findByName(WorkflowState.NEW_NAME)
                            autoAssignmentList.each {
                                if (it.workflowRule.initialState != workFlowInitialState && it.workflowRule.targetState != workFlowFinalState) {
                                    WorkflowJustification workflowJustification = new WorkflowJustification()
                                    workflowJustification.fromState = it.workflowRule.initialState
                                    workflowJustification.toState = it.workflowRule.targetState
                                    workflowJustification.routedBy = User.findByUsername("admin")
                                    workflowJustification.workflowRule = it.workflowRule
                                    def dataObj = null
                                    if (it.moduleName.equals(WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA.getKey())) {
                                        dataObj = QualityCaseData.findByCaseNumberAndTenantIdAndVersionNumberAndIsDeleted(it.caseNumber, it.tenantId, it.versionNumber,false)
                                        workflowJustification.qualityCaseData = dataObj
                                    } else if (it.moduleName.equals(WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION.getKey())) {
                                        dataObj = QualitySubmission.findByCaseNumberAndTenantIdAndVersionNumberAndIsDeletedAndSubmissionIdentifier(it.caseNumber, it.tenantId, it.versionNumber, false, it.processedReportId)
                                        workflowJustification.qualitySubmission = dataObj
                                    } else if (it.moduleName.equals(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY.getKey())) {
                                        Map metadataParams = [:]
                                        metadataParams.masterCaseId = Long.valueOf(it.caseId)
                                        metadataParams.processedReportId = it.processedReportId
                                        metadataParams.tenantId = Long.valueOf(it.tenantId)
                                        dataObj = DrilldownCLLMetadata.getMetadataRecord(metadataParams).get()
                                        workflowJustification.drilldownCLLMetadata = dataObj

                                    } else if (it.moduleName.equals(WorkflowConfigurationTypeEnum.PVC_INBOUND.getKey())) {
                                        Map metadataParams = [:]
                                        metadataParams.masterCaseId = Long.valueOf(it.caseId)
                                        metadataParams.tenantId = Long.valueOf(it.tenantId)
                                        metadataParams.masterVersionNum = Long.valueOf(it.versionNumber)
                                        metadataParams.senderId = Long.valueOf(it.senderId)
                                        dataObj = InboundDrilldownMetadata.getMetadataRecord(metadataParams).get()
                                        workflowJustification.inboundMetadata = dataObj

                                    } else {
                                        dataObj = QualitySampling.findByCaseNumberAndTenantIdAndVersionNumberAndTypeAndIsDeleted(it.caseNumber, it.tenantId, it.versionNumber, it.type, false)
                                        workflowJustification.qualitySampling = dataObj
                                    }
                                    if(dataObj){
                                        workflowJustification.assignedToUser = dataObj.assignedToUser
                                        workflowJustification.assignedToUserGroup = dataObj.assignedToUserGroup
                                    }
                                    CRUDService.save(workflowJustification)
                                }
                            }
                            pvrsql.executeUpdate("delete from AUTO_ASSIGNMENT where id <= " + autoAssignmentList[autoAssignmentList.size() - 1].id)
                            startTime = null
                            endTime = null
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error during reportExecutorService -> runRuleForAssigningUsers", e)
            } finally {
                sql?.close()
                pvrsql?.close()
            }
        }else{
            log.info("No Record in the Auto Assignment table to be processed")
        }
    }

    void executeEntities(String priorityType) throws Exception {
        List<ExecutionStatus> currentlyRunningExecutions = (executorThreadInfoService.totalCurrentlyRunningIds)?.collect {
            ExecutionStatus.load(it)
        }
        ExecutionStatus executionStatus = ExecutionStatus.getExecutionToExecuted(currentlyRunningExecutions, executorThreadInfoService.getStatusOfRunPriorityOnly(),false).get()
        if (executionStatus)
            Tenants.withId((executionStatus.tenantId as Integer)) {
                executeExecutionStatus(executionStatus, priorityType)
            }
    }

    void executeExecutionStatus(ExecutionStatus executionStatus, String priorityType) {
        log.trace("Executing ExecutionStatus with ID ${executionStatus?.id}")
        boolean isExecutionRunningInState = false
        if(priorityType.equals(Constants.ICSR_PROFILE)) {
            isExecutionRunningInState = executorThreadInfoService.totalCurrentlyRunningIcsrIds?.contains(executionStatus.id)
        }else {
            isExecutionRunningInState = executorThreadInfoService.totalCurrentlyRunningIds?.contains(executionStatus.id)
        }
        if (executionStatus && !isExecutionRunningInState) {
            final Thread currentThread = Thread.currentThread()
            final String initialThreadName = currentThread.name
            currentThread.setName("RxThread-" + executionStatus.id + "-" + executionStatus.reportName)
            ExecutedReportConfiguration executedConfiguration
            try {
                def runningInstance = executionStatus.entityClass?.get(executionStatus.entityId)
                if (runningInstance) {
                    executorThreadInfoService.addToTotalCurrentlyRunningIds(executionStatus.id, priorityType)
                    executionStatus.executionStatus = ReportExecutionStatusEnum.GENERATING
                    executionStatus.startTime = System.currentTimeMillis()
                    executionStatus.executedOn = utilService.hostIdentifier
                    setExecutedPeriodicReportType(executionStatus, runningInstance)
                    CRUDService.instantSaveWithoutAuditLog(executionStatus)

                    switch (executionStatus.entityType) {
                        case ExecutingEntityTypeEnum.CONFIGURATION:
                        case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                        case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                            executedConfiguration = executeReportsForConfiguration(runningInstance, executionStatus)
                            if (executedConfiguration) {
                                deliverExecutedReport(executedConfiguration, executionStatus)
                            }
                            if (runningInstance.removeOldVersion) {
                                ExecutedReportConfiguration.findAllByOwnerAndReportNameAndStatusAndIsDeletedAndIdNotEqual(executedConfiguration.owner, executedConfiguration.reportName,
                                        ReportExecutionStatusEnum.COMPLETED, false, executedConfiguration.id).each {
                                    it.isDeleted = true
                                    CRUDService.save(it)
                                    ExecutionStatus previousExStatus = ExecutionStatus.findByExecutedEntityId(it.id)
                                    previousExStatus.isDeleted=true
                                    previousExStatus.save()
                                    Notification.findAllByExecutedConfigId(it.id).collect { n -> n }.each { n ->
                                        previousExStatus?.sharedWith?.each { User u ->
                                            userService.pushNotificationToBrowser(n, u, true)
                                        }
                                        n.delete()
                                    }
                                }
                            }
                            break;
                        case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
                            executeExecutedAdhocReport(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;
                        case ExecutingEntityTypeEnum.CASESERIES:
                            ExecutedCaseSeries executedCaseSeries = executeCaseSeries(runningInstance, executionStatus)
                            if (executedCaseSeries) {
                                deliverExecutedReport(executedCaseSeries, executionStatus)
                            }
                            break;
                        case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
                        case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                            executeRefreshCaseSeries(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;
                        case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES:
                            refreshCaseSeries(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;
                        case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT:
                        case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
                            if (executionStatus.entityType == ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT)
                                refreshCaseSeries(runningInstance, executionStatus)
                            executeDraftReport(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;
                        case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL:
                        case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
                            if (executionStatus.entityType == ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL)
                                refreshCaseSeries(runningInstance, executionStatus)
                            executeFinalReport(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;

                        case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
                        case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
                            executeAddNewTemplateQuery(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;
                        case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                        case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                            executeAddNewTemplateQuery(runningInstance, executionStatus)
                            deliverExecutedReport(runningInstance, executionStatus)
                            break;
                        case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                            executedConfiguration = icsrScheduleService.executeSchedulesForConfiguration(runningInstance, executionStatus)
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("Exception in ReportExecutorService for ExectionStataus ID : ${executionStatus.id}, Entity ID : ${executionStatus.entityId}, EntityType : ${executionStatus.entityType}", e)
            }
            finally {
                try {
                    setExecutionResultValues(executionStatus)
                } catch (Exception ex) {
                    log.error("Error while setting execution status for ES: ${executionStatus?.id}", ex)
                    try {
                        ExecutionStatus.withNewSession {
                            if (executionStatus.executionStatus in [ReportExecutionStatusEnum.DELIVERING, ReportExecutionStatusEnum.WARN]) {
                                executionStatus.executionStatus = ReportExecutionStatusEnum.WARN
                                ExecutionStatus.executeUpdate("update ExecutionStatus set executionStatus=:executionStatus where id =:id", [id: executionStatus.id, executionStatus: ReportExecutionStatusEnum.WARN])
                            } else {
                                executionStatus.executionStatus = ReportExecutionStatusEnum.ERROR
                                ExecutionStatus.executeUpdate("update ExecutionStatus set executionStatus=:executionStatus where id =:id", [id: executionStatus.id, executionStatus: ReportExecutionStatusEnum.ERROR])
                            }
                        }
                    } catch (Exception e) {
                        log.error("Exception was not handled by HQL as well for executionStatus: ${executionStatus.id}", e)
                    }
                }
                executorThreadInfoService.removeFromTotalCurrentlyRunningIds(executionStatus.id)

                if (executionStatus.callbackURL && executionStatus.entityType == ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES) {
                    signalIntegrationService.notifyExecutedCaseSeriesStatus(executionStatus)
                } else if (executionStatus.callbackURL && executionStatus.entityType == ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION) {
                    signalIntegrationService.notifyExecutedConfigurationStatus(executionStatus)
                }

                if (executionStatus.entityType in [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,ExecutingEntityTypeEnum.ICSR_CONFIGURATION]) {
                    //TODO HOOK for Signal Reports Status update we need to make this code more configurable.
                    SignalReportInfo signalReportInfo = SignalReportInfo.findByConfiguration(Configuration.read(executionStatus.entityId))
                    if (executedConfiguration && executedConfiguration.signalConfiguration && executionStatus.executionStatus in [ReportExecutionStatusEnum.WARN, ReportExecutionStatusEnum.COMPLETED]) {
                        signalIntegrationService.saveSignalReportResult(executedConfiguration, signalReportInfo)
                    }

                    if (executionStatus.executionStatus == ReportExecutionStatusEnum.ERROR || executionStatus.executionStatus == ReportExecutionStatusEnum.WARN) {
                        if (executedConfiguration && executedConfiguration.signalConfiguration) {
                            signalIntegrationService.sendErrorNotification(executedConfiguration, signalReportInfo)
                        }
                        try {
                            emailService.emailFailureNotification(executionStatus)
                        } catch (Exception e1) {
                            log.error("Unable to deliver report failure notification for Configuration.id=${executionStatus.entityId}", e1)
                        }
                    }
                }
                currentThread.setName(initialThreadName)
            }
        }
    }

    void killConfigurationExecution(Long id) {
        try {
            ExecutorThreadInfoDTO executorThreadInfoDTO = executorThreadInfoService.currentlyRunning.get(id)
            if (executorThreadInfoDTO) {
                log.debug("Killing configuration with info ${executorThreadInfoService.currentlyRunning.get(id)}")
                killSqlProcess(executorThreadInfoService.currentlyRunning.get(id)?.currentSqlInfoId)
                log.debug("Killed sql process configuration with info ${executorThreadInfoService.currentlyRunning.get(id)?.currentSqlInfoId}")
                Thread thread = executorThreadInfoService.currentlyRunning.get(id)?.threadObj
                thread.sleep(3000)
                thread?.interrupt()
                log.debug("Killed Thread process configuration with info ${executorThreadInfoService.currentlyRunning.get(id)?.threadObj}")
            } else {
                log.error("Execution Thread not found for id ${id}")
            }
        } catch (SecurityException sec) {
            log.error("SecurityException: ", sec)

        } catch (InterruptedException interrup) {
            log.error("InterruptedException: ", interrup)

        } catch (ClosedByInterruptException cBI) {
            log.error("ClosedByInterruptException: ", cBI)
        }
    }

    void killCaseGenerationExecution(BigDecimal id) {
        try {
            ExecutorThreadInfoDTO executorThreadInfoDTO = executorThreadInfoService.currentlyGeneratingCases.get(id)
            if (executorThreadInfoDTO) {
                log.debug("Killing Icsr case generation with info ${executorThreadInfoService.currentlyGeneratingCases.get(id)}")
                killSqlProcess(executorThreadInfoService.currentlyGeneratingCases.get(id)?.currentSqlInfoId)
                log.debug("Killed sql process Icsr case generation with info ${executorThreadInfoService.currentlyGeneratingCases.get(id)?.currentSqlInfoId}")
                Thread thread = executorThreadInfoService.currentlyGeneratingCases.get(id)?.threadObj
                thread?.interrupt()
                log.debug("Killed Thread process Icsr case generation with info ${executorThreadInfoService.currentlyGeneratingCases.get(id)?.threadObj}")
            } else {
                log.error("Execution Thread not found for Icsr case generation id ${id}")
            }
        } catch (SecurityException sec) {
            log.error("SecurityException: ", sec)

        } catch (InterruptedException interrup) {
            log.error("InterruptedException: ", interrup)

        } catch (ClosedByInterruptException cBI) {
            log.error("ClosedByInterruptException: ", cBI)
        }
    }

    def deliverExecutedReport(executedConfiguration, ExecutionStatus executionStatus) {
        try {
            setDeliveringStatus(executionStatus)
            clearFileCache(executedConfiguration)
            deliverReport(executedConfiguration)
        } catch (Exception e) {
            //save Warn message
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.WARN)
            log.error("!!...............Error ...........DELIVERING..........!!" + e)
        }
    }

    private clearFileCache(executedConfiguration) {
        ReportFormatEnum.values().each { outputFormat ->
            for (String locale : LocaleHelper.getSupportedLocales().collect { it.language }) {
                String reportFileName = dynamicReportService.getReportFilename(executedConfiguration.reportName, outputFormat.name(), locale)
                File reportFile = new File(dynamicReportService.getReportsDirectory() + reportFileName)
                if (reportFile?.exists() && !reportFile.isDirectory()) {
                    reportFile.delete()
                }
            }
        }
    }

    private setDeliveringStatus(ExecutionStatus executionStatus) {
        if (executionStatus?.executionStatus != ReportExecutionStatusEnum.ERROR) {
            executionStatus?.executionStatus = ReportExecutionStatusEnum.DELIVERING
        }
        CRUDService.updateWithoutAuditLog(executionStatus)
    }

    
    private executeReportsForConfiguration(ReportConfiguration scheduledConfiguration, ExecutionStatus executionStatus) throws Exception {
        ExecutedReportConfiguration executedConfiguration = null
        try {
            executedConfiguration = executeReportJob(scheduledConfiguration, executionStatus)
            if ((scheduledConfiguration instanceof PeriodicReportConfiguration) || (scheduledConfiguration instanceof Configuration)) {
                taskTemplateService.createActionItems(scheduledConfiguration, executedConfiguration)
            }
            if (scheduledConfiguration instanceof PeriodicReportConfiguration) {
                taskTemplateService.createPublisherActionItems(executedConfiguration)
                generateSpotfireForReport(scheduledConfiguration, executedConfiguration, executionStatus)
            }
            executionStatus.executedEntityId = executedConfiguration?.id
            CRUDService.updateWithoutAuditLog(executionStatus)
            if(scheduledConfiguration.isPriorityReport){
                scheduledConfiguration.isPriorityReport = false
                CRUDService.updateWithoutAuditLog(scheduledConfiguration)
            }
        } catch (Exception e) {
            log.error("Error while generating report configuration", e)
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw e
        }
        return executedConfiguration
    }

    private ExecutedCaseSeries executeCaseSeries(CaseSeries caseSeries, ExecutionStatus executionStatus) {
        ExecutedCaseSeries executedCaseSeries = null
        try {
            caseSeries?.refresh()
            caseSeries.executing = true
            CRUDService.instantSaveWithoutAuditLog(caseSeries)
            executedCaseSeries = new ExecutedCaseSeries()
            caseSeriesService.updateDetailsFrom(caseSeries, executedCaseSeries)
            executedCaseSeries.numExecutions ++
            executedCaseSeries.createdBy = executionStatus.owner.username
            executedCaseSeries.modifiedBy = executionStatus.owner.username
            executedCaseSeries.executing = true
            CRUDService.save(executedCaseSeries)
            generateCasesResult(ExecutorDTO.create(executedCaseSeries), false)
            executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
            executionStatus.executedEntityId = executedCaseSeries.id
            CRUDService.instantSaveWithoutAuditLog(executionStatus)
        } catch (Exception ex) {
            log.error("Error while generating caseseres", ex)
            saveCaseSeriesException(executionStatus, ex)
            throw ex
        }
        if(caseSeries.generateSpotfire){
            generateSpotfire(caseSeries,executedCaseSeries, executionStatus)
        }
        return executedCaseSeries
    }

    private generateSpotfire(CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeries, ExecutionStatus executionStatus) {
        try {

            if (caseSeries.generateSpotfire) {
                def settings = JSON.parse(caseSeries.generateSpotfire)
                String fileName = spotfireService.getUniqueName(settings.fullFileName)
                executedCaseSeries.associatedSpotfireFile = fileName
                CRUDService.save(executedCaseSeries)
                spotfireService.reserveFileName(fileName)
                Set<String> products = prepareFamilyIds(executedCaseSeries.productSelection, executedCaseSeries.productGroupSelection)
                def params = spotfireService.generateReportParams(products, Date.parse("yyyy-MM-dd", "1900-01-01"), new Date(), new Date(), executedCaseSeries.id, settings.type, fileName, Constants.SPOTFIRE_CASE_SERIES)
                if (Holders.config.getProperty('spotfire.fileBasedReportGen', Boolean)) {
                    log.info("PVR will generate spotfire report into a file folder ${Holders.config.getProperty('spotfire.fileFolder')}")
                    def xml = SpotfireUtil.composeXmlBodyForTask(params)
                    File file = SpotfireUtil.generateAutomationXml(new File(Holders.config.getProperty('spotfire.fileFolder')), xml)
                    log.info("File [${file.getAbsoluteFile()}] is generated")
                } else {

                    def respMsg = spotfireService.invokeReportGenerationAPI(params)
                    JsonSlurper slurper = new JsonSlurper()
                    def jsonRsp = slurper.parseText(respMsg)
                    if (!jsonRsp.JobId)
                        throw new Exception("Failed to generate report")
                }
            }
        } catch (Exception ex) {
            log.error("Failed generate spotfire report file", ex)
            List messages = MiscUtil.getExceptionMessage(ex)
            executionStatus.executionStatus = ReportExecutionStatusEnum.WARN
            executionStatus.message = messages[0]
            executionStatus.stackTrace = messages[1]
            executionStatus.save(failOnError: true, flush: true)
            throw ex
        }
    }

    private generateSpotfireForReport(PeriodicReportConfiguration periodicReportConfiguration, ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration, ExecutionStatus executionStatus) {
        try {
            if (periodicReportConfiguration.generateSpotfire) {
                def settings = JSON.parse(periodicReportConfiguration.generateSpotfire)
                int executedReportIdSize=String.valueOf(executedPeriodicReportConfiguration.caseSeries.id).length()
                int numberOfExecutionsSize=String.valueOf(executedPeriodicReportConfiguration.numOfExecutions).length()
                int postfixSize=executedReportIdSize+numberOfExecutionsSize+2
                int maxSize=CaseSeries.constrainedProperties.generateSpotfire.maxSize
                String fileName = spotfireService.getUniqueName(settings.fullFileName)
                if((fileName.length()+postfixSize)>maxSize){
                    fileName=fileName.substring(0,(maxSize-postfixSize)+1)
                }
                fileName=fileName + "_" + executedPeriodicReportConfiguration.numOfExecutions + "_" + executedPeriodicReportConfiguration.caseSeries.id
                executedPeriodicReportConfiguration.associatedSpotfireFile = fileName
                CRUDService.save(executedPeriodicReportConfiguration)
                spotfireService.reserveFileName(fileName)
                Set<String> products = prepareFamilyIds(executedPeriodicReportConfiguration.productSelection, executedPeriodicReportConfiguration.productGroupSelection)
                def params = spotfireService.generateReportParams(products, Date.parse("yyyy-MM-dd", "1900-01-01"), new Date(), new Date(), executedPeriodicReportConfiguration.caseSeries.id, settings.type, fileName, Constants.SPOTFIRE_CASE_SERIES)
                if (Holders.config.getProperty('spotfire.fileBasedReportGen', Boolean)) {
                    log.info("PVR will generate spotfire report into a file folder ${Holders.config.getProperty('spotfire.fileFolder')}")
                    def xml = SpotfireUtil.composeXmlBodyForTask(params)
                    File file = SpotfireUtil.generateAutomationXml(new File(Holders.config.getProperty('spotfire.fileFolder')), xml)
                    log.info("File [${file.getAbsoluteFile()}] is generated")
                } else {

                    def respMsg = spotfireService.invokeReportGenerationAPI(params)
                    JsonSlurper slurper = new JsonSlurper()
                    def jsonRsp = slurper.parseText(respMsg)
                    if (!jsonRsp.JobId)
                        throw new Exception("Failed to generate report")
                }
            }
        } catch (Exception ex) {
            log.error("Failed generate spotfire report file", ex)
            List messages = MiscUtil.getExceptionMessage(ex)
            executionStatus.executionStatus = ReportExecutionStatusEnum.WARN
            executionStatus.message = messages[0]
            executionStatus.stackTrace = messages[1]
            executionStatus.save(failOnError: true, flush: true)
            throw ex
        }
    }

    private Set<String> prepareFamilyIds(String productDictionarySelection, String productGroupSelection) {
        Set<String> familiesId = []
        final Sql sql
        try {
            sql = new Sql(dataSource_pva)
            initializeGTTForSpotfire(sql, productDictionarySelection, productGroupSelection)
            sql.call("{call f_resolve_dict_values(${Sql.VARCHAR})}") { String familyId ->
                if (familyId) {
                    familiesId.addAll(familyId.split(","))
                }
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage())
        } finally {
            sql?.close()
        }
        familiesId.removeAll([null])
        familiesId
    }

    private void initializeGTTForSpotfire(Sql sql, String productSelection, String productGroupSelection){
        String initialParamsInsert = sqlGenerationService.initializeGTTForSpotfire(productSelection, productGroupSelection)
        if (initialParamsInsert) {
            sql.execute(initialParamsInsert)
        }
    }

    private executeAddNewTemplateQuery(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        ExecutedTemplateQuery executedTemplateQuery = null
        try {
            executedTemplateQuery = executionStatus.queryId ? ExecutedTemplateQuery.get(executionStatus.queryId) : executedReportConfiguration.executedTemplateQueries.find {
                it.reportResult?.executionStatus == ReportExecutionStatusEnum.SCHEDULED
            }

            ResponseDTO<Map> transactionResponseDTO = generateFinalReport(executedTemplateQuery, executionStatus, false)

            if (!transactionResponseDTO.status) {
                throw new ExecutionStatusException(transactionResponseDTO.data)
            }
        } catch (Exception ex) {
            log.error("Error while adding new tenplate and generate", ex)
            //following code updates execution status of manually added report section, in case of error
            if(executedTemplateQuery && executedTemplateQuery.reportResult) {
                ReportResult.executeUpdate("update ReportResult set executionStatus=:executionStatus where id =:id", [id: executedTemplateQuery.reportResult.id, executionStatus: ReportExecutionStatusEnum.ERROR])
            }
            saveExceptionMessage(ex, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw ex
        }
    }

    private executeRefreshCaseSeries(ExecutedCaseSeries executedCaseSeries, ExecutionStatus executionStatus) {
        try {
            if(executionStatus.entityType == ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES){
                refreshSingleCaseSeries(executedCaseSeries, executionStatus.owner.username, false)
            } else{
                refreshSingleCaseSeries(executedCaseSeries, executionStatus.owner.username, !executedCaseSeries.isTemporary)
            }
            executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
            CRUDService.instantSaveWithoutAuditLog(executionStatus)
        } catch (Exception ex) {
            log.error("Error while refreshing case series", ex)
            saveCaseSeriesException(executionStatus, ex)
            throw ex
        }
    }

    private refreshCaseSeries(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        try {
            if (executedReportConfiguration.cumulativeCaseSeries) {
                refreshSingleCaseSeries(executedReportConfiguration.cumulativeCaseSeries, executionStatus.owner.username, true)
            }
            refreshSingleCaseSeries(executedReportConfiguration.caseSeries, executionStatus.owner.username, true)
            executionStatus.aggregateReportStatus = ReportExecutionStatusEnum.GENERATED_CASES
            CRUDService.instantSaveWithoutAuditLog(executionStatus)
        } catch (Exception e) {
            log.error("Error while refreshing case series for configuration", e)
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw e
        }
    }

    private refreshSingleCaseSeries(ExecutedCaseSeries executedCaseSeries, String userName, Boolean refresh) {
        executedCaseSeries?.refresh()
        executedCaseSeries.modifiedBy = userName
        executedCaseSeries.executing = true
        CRUDService.saveOrUpdate(executedCaseSeries)
        generateCasesResult(ExecutorDTO.create(executedCaseSeries), refresh)
        dynamicReportService.deleteAllCaseSeriesCachedFile(executedCaseSeries, true)
        executedCaseSeries.executing = false
        CRUDService.saveOrUpdate(executedCaseSeries)
    }

    private executeDraftReport(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        try {
            generateDraftReport(executedReportConfiguration, executionStatus)
        } catch (Exception e) {
            log.error("Error while generating draft report", e)
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw e
        }
    }


    private executeFinalReport(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        try {
            generateFinalDraftReport(executedReportConfiguration, executionStatus)
        } catch (Exception e) {
            log.error("Error while generating final report", e)
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw e
        }
    }

    private executeExecutedAdhocReport(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        try {
            generateAdhocReport(executedReportConfiguration, executionStatus)
        } catch (Exception e) {
            log.error("Error while re-generating executed adhoc report", e)
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw e
        }
    }

    void saveCaseSeriesException(ExecutionStatus executionStatus, def exception) {
        List messages = MiscUtil.getExceptionMessage(exception)
        executionStatus.executionStatus = ReportExecutionStatusEnum.ERROR
        executionStatus.message = messages[0]
        executionStatus.stackTrace = messages[1]
        executionStatus.save(failOnError: true, flush: true)
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setExecutionResultValues(ExecutionStatus executionStatus) {
        executionStatus.refresh()
        def lockedInstance = executionStatus.entityClass?.lock(executionStatus.entityId)
        if(lockedInstance) {
            switch (executionStatus.entityType) {
                case ExecutingEntityTypeEnum.CONFIGURATION:
                case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                    // In case of some timeout and tries to update the values doesn't allow to execute the finally block
                    log.info("Execution Status of Running Configuration -> ${executionStatus.executionStatus}")
                    if (executionStatus.executionStatus != ReportExecutionStatusEnum.GENERATING) {
                        lockedInstance.numOfExecutions++
                        setTotalExecutionTimeForConfiguration(lockedInstance, (System.currentTimeMillis() - executionStatus.startTime))
                        adjustAsOfVersionDate(lockedInstance)
                        lockedInstance.setNextRunDate(configurationService.getNextDate(lockedInstance))
                        if (!(lockedInstance instanceof IcsrProfileConfiguration) && lockedInstance.nextRunDate == null) {
                            lockedInstance.setIsEnabled(false)
                        }
                        lockedInstance.executing = false
                        lockedInstance.isPriorityReport = false
                        if (executionStatus.executionStatus == ReportExecutionStatusEnum.DELIVERING) {
                            executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
                        }
                        CRUDService.update(lockedInstance)
                        executionStatus?.sharedWith?.each {
                            notificationService.addNotification(executionStatus.executedEntityId, executionStatus, it)
                        }
                        log.debug("########## Marking config as non executing ##############")

                    }
                    break;

                case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                    // In case of some timeout and tries to update the values doesn't allow to execute the finally block
                    if (executionStatus.executionStatus != ReportExecutionStatusEnum.GENERATING) {
                        lockedInstance.numOfExecutions++
                        setTotalExecutionTimeForConfiguration(lockedInstance, (System.currentTimeMillis() - executionStatus.startTime))
                        adjustAsOfVersionDate(lockedInstance)
                        lockedInstance.setNextRunDate(null)
                        if (!(lockedInstance instanceof IcsrProfileConfiguration) && lockedInstance.nextRunDate == null) {
                            lockedInstance.setIsEnabled(false)
                        }
                        lockedInstance.executing = false
                        if (executionStatus.executionStatus == ReportExecutionStatusEnum.DELIVERING) {
                            executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
                        }
                        CRUDService.update(lockedInstance)
                        log.debug("########## Marking config as non executing ##############")

                    }
                    break;

                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
                case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION:
                case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
                case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
                case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
                case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                    switch (lockedInstance.status) {
                        case ReportExecutionStatusEnum.GENERATING_NEW_SECTION:
                        case ReportExecutionStatusEnum.GENERATING:
                        case ReportExecutionStatusEnum.GENERATING_ON_DEMAND_SECTION:
                            if (lockedInstance.status == ReportExecutionStatusEnum.GENERATING_NEW_SECTION && executionStatus.entityType == ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION){
                                lockedInstance.executionStatus = ReportExecutionStatusEnum.GENERATED_DRAFT
                            } else if (lockedInstance.status == ReportExecutionStatusEnum.GENERATING_ON_DEMAND_SECTION && executionStatus.entityType in [ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION, ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION]){
                                lockedInstance.executionStatus = ReportExecutionStatusEnum.COMPLETED
                            } else if (executionStatus.entityType in [ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION, ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION]) {
                                lockedInstance.executionStatus = ReportExecutionStatusEnum.COMPLETED
                            }
                            break;

                        case ReportExecutionStatusEnum.GENERATING_DRAFT:
                            lockedInstance.executionStatus = ReportExecutionStatusEnum.GENERATED_CASES
                            break;

                        case ReportExecutionStatusEnum.GENERATING_FINAL_DRAFT:
                            lockedInstance.executionStatus = ReportExecutionStatusEnum.GENERATED_DRAFT
                            break;
                    }
                    if (executionStatus.executionStatus == ReportExecutionStatusEnum.DELIVERING) {
                        executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
                    }
                    CRUDService.update(lockedInstance)
                    executionStatus?.sharedWith?.each {
                        notificationService.addNotification(executionStatus.entityId, executionStatus, it)
                    }
                    break;

            // following both executes together don't put break in between by doubt
                case ExecutingEntityTypeEnum.CASESERIES:
                    if (executionStatus.executionStatus != ReportExecutionStatusEnum.GENERATING) {
                        lockedInstance.numExecutions++
                        adjustAsOfVersionDate(lockedInstance)
                        lockedInstance.setNextRunDate(configurationService.getNextDate(lockedInstance))
                        if (lockedInstance.nextRunDate == null) {
                            lockedInstance.setIsEnabled(false)
                        }
                        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(executionStatus.executedEntityId)
                        if (executedCaseSeries) {
                            executedCaseSeries.executing = false
                            CRUDService.instantSaveWithoutAuditLog(executedCaseSeries)
                        }
                    }
                case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
                    if (executionStatus.executionStatus != ReportExecutionStatusEnum.GENERATING) {
                        lockedInstance.executing = false
                        if (executionStatus.executionStatus == ReportExecutionStatusEnum.DELIVERING) {
                            executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
                        }
                        CRUDService.instantSaveWithoutAuditLog(lockedInstance)
                        NotificationLevelEnum level
                        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(executionStatus.entityId)
                        if (!executedCaseSeries?.isTemporary || executionStatus?.entityType == ExecutingEntityTypeEnum.EXCECUTED_CASESERIES) {
                            executionStatus?.sharedWith?.each {
                                if (executionStatus.executionStatus != ReportExecutionStatusEnum.COMPLETED) {
                                    switch (executionStatus.executionStatus) {
                                        case ReportExecutionStatusEnum.ERROR:
                                            level = NotificationLevelEnum.ERROR
                                            notificationService.addNotification(it,
                                                    'app.notification.caseSeries.failed', executionStatus.reportName, executionStatus.id, level, NotificationApp.CASESERIES)
                                            break

                                        case ReportExecutionStatusEnum.WARN:
                                            level = NotificationLevelEnum.WARN
                                            notificationService.addNotification(it,
                                                    'app.notification.caseSeries.failed', executionStatus.reportName, executionStatus.executedEntityId ?: executionStatus.entityId, level, NotificationApp.CASESERIES)
                                            break
                                    }

                                } else {
                                    notificationService.addNotification(it,
                                            'app.notification.caseSeries.generated', executionStatus.reportName, executionStatus.executedEntityId ?: executionStatus.entityId, NotificationLevelEnum.INFO, NotificationApp.CASESERIES)
                                }
                            }
                        }
                    }
                    break;
            }
            executionStatus.endTime = System.currentTimeMillis()
            CRUDService.instantUpdateWithoutAuditLog(executionStatus)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    ExecutionStatus createExecutionStatus(ReportConfiguration scheduledConfiguration) {
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: scheduledConfiguration.id, entityType: ExecutionStatus.getEntityTypeFromClass(scheduledConfiguration.class), reportVersion: scheduledConfiguration.numOfExecutions + 1,
                startTime: System.currentTimeMillis(), nextRunDate: scheduledConfiguration.nextRunDate, owner: scheduledConfiguration.owner, reportName: scheduledConfiguration.reportName,
                attachmentFormats: scheduledConfiguration?.deliveryOption?.attachmentFormats, sharedWith: scheduledConfiguration?.allSharedUsers?.unique(), tenantId: scheduledConfiguration?.tenantId)
        executionStatus.frequency = configurationService.calculateFrequency(scheduledConfiguration)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.isPriorityReport = scheduledConfiguration.isPriorityReport
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    ExecutionStatus createExecutionStatus(CaseSeries caseSeriesInstance) {
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: caseSeriesInstance.id, entityType: ExecutionStatus.getEntityTypeFromClass(caseSeriesInstance.class), reportVersion: caseSeriesInstance.numExecutions + 1,
                startTime: System.currentTimeMillis(), owner: caseSeriesInstance.owner, reportName: caseSeriesInstance.seriesName,
                attachmentFormats: caseSeriesInstance?.deliveryOption?.attachmentFormats, sharedWith: caseSeriesInstance?.allSharedUsers, tenantId: caseSeriesInstance?.tenantId)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.frequency = caseSeriesService.calculateFrequency(caseSeriesInstance)
        executionStatus.nextRunDate = caseSeriesInstance.nextRunDate
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

    def saveExceptionMessage(Exception ex, ExecutionStatus executionStatus, ReportExecutionStatusEnum status) {
        Long queryId = 0L
        Long templateId = 0L
        String sectionName = ''
        String stackTrace = ex.stackTrace.toString()
        String querySql = ''
        String headerSql = ''
        String reportSql = ''
        if(ex instanceof ExecutionStatusException) {
            queryId = ex.queryId
            templateId = ex.templateId
            sectionName = ex.sectionName
            stackTrace = ex.errorCause
            querySql = ex.querySql
            headerSql = ex.headerSql
            reportSql = ex.reportSql
        }
        ExecutionStatus.withNewSession {
            if(executionStatus && executionStatus.refresh()) {
                executionStatus.message = ex.message
                executionStatus.queryId = queryId
                executionStatus.templateId = templateId
                executionStatus.sectionName = sectionName
                executionStatus.stackTrace = stackTrace
                executionStatus.executionStatus = status
                executionStatus.querySql = querySql
                executionStatus.headerSql = headerSql
                executionStatus.reportSql = reportSql
                CRUDService.instantSaveWithoutAuditLog(executionStatus)
            } else {
                log.info("cannot find the execution status")
            }
        }
    }

    private void setExecutedPeriodicReportType(ExecutionStatus executionStatus,def reportConfiguration){
        if(reportConfiguration instanceof PeriodicReportConfiguration || reportConfiguration instanceof ExecutedPeriodicReportConfiguration)
        {
            executionStatus.periodicReportType = reportConfiguration.periodicReportType.getKey()
        }
        else{
            executionStatus.periodicReportType = " "
        }

    }

    private ExecutedReportConfiguration executeReportJob(ReportConfiguration configuration, ExecutionStatus executionStatus) throws Exception {
        def startTime
        ReportResult result
        Date scheduledDate = executionStatus.nextRunDate
        ExecutedReportConfiguration executedConfiguration = null
        ReportConfiguration lockedConfiguration = ReportConfiguration.get(configuration.id)

        log.info("Executing Configuration: (ID: ${configuration.id})")

        try {
            if (lockedConfiguration.isEnabled) {
                // Validating Configuration once again before using
                if (!lockedConfiguration.validate()) {
                    throw new ValidationException("Validation Exception in Configuration", lockedConfiguration.errors)
                }
                lockedConfiguration.executing = true
                CRUDService.update(lockedConfiguration)
                executedConfiguration = executedConfigurationService.createExecutedConfiguration(lockedConfiguration, scheduledDate)

                if (lockedConfiguration instanceof PeriodicReportConfiguration || lockedConfiguration instanceof Configuration || lockedConfiguration instanceof IcsrReportConfiguration) {
                    adjustGlobalCustomDateRanges(lockedConfiguration)
                    CRUDService.update(lockedConfiguration)
                }
                executedConfiguration.workflowState = WorkflowState.defaultWorkState
                if (executedConfiguration instanceof ExecutedPeriodicReportConfiguration) {
                    ExecutedCaseSeries caseSeries = new ExecutedCaseSeries(executedConfiguration, false)
                    executedConfiguration.caseSeries = caseSeries
                    caseSeries.isTemporary = !lockedConfiguration.generateCaseSeries
                    CRUDService.instantSaveWithoutAuditLog(caseSeries)
                    if (lockedConfiguration.globalDateRangeInformation.dateRangeEnum != DateRangeEnum.CUMULATIVE) {
                        ExecutedCaseSeries cumulativeCaseSeries = new ExecutedCaseSeries(executedConfiguration, true)
                        cumulativeCaseSeries.isTemporary = !lockedConfiguration.generateCaseSeries
                        CRUDService.instantSaveWithoutAuditLog(cumulativeCaseSeries)
                        executedConfiguration.cumulativeCaseSeries = cumulativeCaseSeries
                    }
                }
                CRUDService.instantSaveWithoutAuditLog(executedConfiguration)
                def list = ReportRequest.findAllByLinkedConfigurationsLike('%:' + lockedConfiguration.id + ',%')
                list?.each { reportRequest ->
                    def link = []
                    link.add([id: executedConfiguration.id, name: (executedConfiguration.reportName + " (version " + executedConfiguration.numOfExecutions + ")")])
                    reportRequest.linkedGeneratedReports = (link as JSON).toString()
                    reportRequest.save()
                }
                if (executedConfiguration instanceof ExecutedPeriodicReportConfiguration) {
                    //creating plan from template
                    executedConfiguration.gantt = ganttService.createForReport(configuration.gantt, executedConfiguration, new Date(), executedConfiguration.owner)
                    CRUDService.instantSaveWithoutAuditLog(executedConfiguration)
                }
                CRUDService.update(lockedConfiguration)
                startTime = System.currentTimeMillis()
                boolean needToUseAlreadyGeneratedCases = false
                if (lockedConfiguration.generateCaseSeries || lockedConfiguration.instanceOf(PeriodicReportConfiguration)) {
                    needToUseAlreadyGeneratedCases = true
                    if (executedConfiguration.cumulativeCaseSeries) {
                        generateCasesResult(ExecutorDTO.create(executedConfiguration.cumulativeCaseSeries), false)
                        executedConfiguration.cumulativeCaseSeries.executing = false
                        CRUDService.instantSaveWithoutAuditLog(executedConfiguration.cumulativeCaseSeries)
                    }
                    generateCasesResult(ExecutorDTO.create(executedConfiguration.caseSeries), false)
                    executedConfiguration.caseSeries.executing = false
                    CRUDService.instantSaveWithoutAuditLog(executedConfiguration.caseSeries)
                    if (lockedConfiguration.generateCaseSeries && !lockedConfiguration.generateDraft) {
                        executedConfiguration.status = ReportExecutionStatusEnum.GENERATED_CASES
                        executedConfiguration.totalExecutionTime = (System.currentTimeMillis() - startTime)
                        executionStatus.aggregateReportStatus = ReportExecutionStatusEnum.GENERATED_CASES
                        CRUDService.instantSaveWithoutAuditLog(executionStatus)
                        CRUDService.update(lockedConfiguration)
                        CRUDService.updateWithoutAuditLog(executedConfiguration)
                        log.info("Execution of Configuration took ${executedConfiguration.totalExecutionTime}ms for [C:${lockedConfiguration?.id}, EC: ${executedConfiguration.id}]")
                        return executedConfiguration
                    }
                    if (lockedConfiguration.generateCaseSeries && lockedConfiguration.generateDraft) {
                        executeDraftReport(executedConfiguration, executionStatus)
                        return executedConfiguration
                    }
                }
                generateReport(executedConfiguration, startTime, needToUseAlreadyGeneratedCases, null, lockedConfiguration.id)
                log.info("Execution of Configuration took ${executedConfiguration.totalExecutionTime}ms for [C:${lockedConfiguration?.id}, EC: ${executedConfiguration.id}]")
            }
        }
        catch (Exception e) {
            log.error("Unable to finish running lockedConfiguration.id=${lockedConfiguration.id}", e)
            Exception e1
            if (!(e instanceof ExecutionStatusException)) {
                String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
                StringWriter sw = new StringWriter()
                e.printStackTrace(new PrintWriter(sw))
                String exceptionAsString = sw.toString()
                if (!message) {
                    message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
                }
                e1 = new ExecutionStatusException([errorMessage: message, errorCause: exceptionAsString])
                throw e1
            } else {
                throw e
            }
        }
        return executedConfiguration
    }

    private void generateReport(ExecutedReportConfiguration executedConfiguration, Long startTime,
                                boolean needToUseAlreadyGeneratedCases = false,
                                SignalReportInfo signalReportInfo = null, Long reportId = 0) {
        Long templateId = 0L
        Long queryId = 0L
        String sectionName = ""
        Sql sql
        try {
            Locale locale = executedConfiguration.locale
            sql = new Sql(getReportConnection())
            sqlGenerationService.setCurrentSqlInfo(sql, locale)
            String meddraVersion = null
            if (locale.equals(Locale.JAPANESE)) {
                meddraVersion = sql.firstRow("select MEDDRA_VERSION FROM VW_PVR_MEDDRA_VERSION_J")?.MEDDRA_VERSION
            } else {
                meddraVersion = sql.firstRow("select MEDDRA_VERSION FROM VW_PVR_MEDDRA_VERSION")?.MEDDRA_VERSION
            }
            boolean isJDSUR = false
            boolean isReSD = false
            if (executedConfiguration instanceof ExecutedPeriodicReportConfiguration && executedConfiguration.periodicReportType == PeriodicReportTypeEnum.PADER) {
                sql.call("{call pkg_pader_line_listing.p_init}")
            }

            if (executedConfiguration instanceof ExecutedPeriodicReportConfiguration && executedConfiguration.periodicReportType == PeriodicReportTypeEnum.JDSUR) {
                isJDSUR = true
            }
            if (executedConfiguration instanceof ExecutedPeriodicReportConfiguration && executedConfiguration.periodicReportType == PeriodicReportTypeEnum.RESD) {
                isReSD = true
            }
            if(executedConfiguration.instanceOf(ExecutedIcsrProfileConfiguration)){
                sql.call("{call PKG_E2B_PROCESSING.P_POP_ICSRPROF_BASE_VERSION()}")
            }

            sql?.execute("insert into GTT_ICSR_CONSTANTS (EXECUTION_ID,PROFILE_ID) values(?,?)", [executedConfiguration.id, reportId])
            logToSubmissionHistory(sql, executedConfiguration)
            int totalSections = executedConfiguration.executedTemplateQueriesForProcessing.size()
            boolean isIcsrProfile = (executedConfiguration instanceof ExecutedIcsrProfileConfiguration)
            executedConfiguration.executedTemplateQueriesForProcessing.eachWithIndex { it,index->
                templateId = it.reportResult.executedTemplateQuery?.executedTemplateId
                queryId = it.reportResult.executedTemplateQuery?.executedQueryId
                sectionName = (it?.title) ? (it.title) : (it.executedConfiguration.reportName)
                if (it.displayMedDraVersionNumber || it.usedTemplate.useFixedTemplate) {
                    it.reportResult.medDraVersion = meddraVersion
                }
                if (isIcsrProfile && index == totalSections - 1) {
                    log.debug("Executing last section of the report: ${executedConfiguration.reportName}, ${it.id}")
                    sql.call("{? = call PKG_CREATE_REPORT_SQL.F_SET_FLAG_LAST_SECTION(1)}", [Sql.INTEGER]) { Integer result ->
                        if (!result) {
                            throw new GrailsRuntimeException("Exception at DB end while marking section as last section execution for ${executedConfiguration.reportName} so failing section")
                        }
                    }
                }
                if (it.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                    generateReportResult(sql, it.reportResult, locale, needToUseAlreadyGeneratedCases)
                } else {
                    generateReportResultCSV(sql, it.reportResult, locale, needToUseAlreadyGeneratedCases, false)
                }
                if (isJDSUR || it.usedTemplate?.fixedTemplate?.name?.trim()?.contains("Seiyakukyo Data Tabulation")) {
                    String clinicalCompoundNumber = null
                    sql.call("{? = call f_fetch_japan_ccn()}", [Sql.VARCHAR]) { String sqlValue->
                        clinicalCompoundNumber = sqlValue
                    }
                    it.reportResult.clinicalCompoundNumber = clinicalCompoundNumber
                }
                if (isReSD) {
                    String reSDmeddraVersion = sql.firstRow("select version_number  from vw_meddra_tenant_mapping")?.version_number
                    it.reportResult.medDraVersion = reSDmeddraVersion
                }
                if (it.reportResult.executionStatus != ReportExecutionStatusEnum.ERROR) {
                    it.reportResult.executionStatus = ReportExecutionStatusEnum.COMPLETED
                }
                CRUDService.saveWithoutAuditLog(it.reportResult)
                if (it.usedTemplate.templateType in [TemplateTypeEnum.CUSTOM_SQL, TemplateTypeEnum.NON_CASE]) {
                    def rootTemplate = it.usedTemplate
                    checkAndExecuteCustomSqlDrillDowns(sql, it, it.usedTemplate, it.reportResult, rootTemplate, locale, needToUseAlreadyGeneratedCases)
                }
                if (it.usedTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
                    def rootTemplate = it.usedTemplate
                    checkAndExecuteDrillDowns(sql, it, it.usedTemplate, it.reportResult, rootTemplate, locale, needToUseAlreadyGeneratedCases)
                }
                if (it.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                    ExecutedDataTabulationTemplate executedDataTabulationTemplate = ((ExecutedDataTabulationTemplate) it.usedTemplate)
                    executedDataTabulationTemplate.columnMeasureList.eachWithIndex { col, ind ->
                        col.measures.each { measure ->
                            if (measure.drillDownTemplate) {
                                CaseLineListingTemplate template = measure.drillDownTemplate
                                template.interactiveOutput = (template.interactiveOutput || executedDataTabulationTemplate.interactiveOutput)
                                if (!template.JSONQuery) template.JSONQuery = executedDataTabulationTemplate.JSONQuery
                                ReportResult drillDown = new ReportResult()
                                drillDown.drillDownSource = it
                                drillDown.measure = measure.type.code + "1" + (ind + 1)
                                drillDown.template = template
                                drillDown.parent = it.reportResult
                                drillDown.scheduledBy = it.reportResult.scheduledBy
                                drillDown.executionStatus = ReportExecutionStatusEnum.GENERATING
                                drillDown.save(flush: true, failOnError: true)
                                def rootTemplate = it.usedTemplate
                                it.executedTemplate = drillDown.template
                                //temporary changing template to CLL to execute drilldown
                                //todo: PVC POC :run similar templates ones
                                generateReportResultCSV(sql, drillDown, locale, needToUseAlreadyGeneratedCases, true)
                                it.executedTemplate = rootTemplate
                                if (drillDown.executionStatus != ReportExecutionStatusEnum.ERROR) {
                                    drillDown.executionStatus = ReportExecutionStatusEnum.COMPLETED
                                }
                                CRUDService.saveWithoutAuditLog(drillDown)

                                checkAndExecuteDrillDowns(sql, it, template, drillDown, rootTemplate, locale, needToUseAlreadyGeneratedCases)
                            }
                        }
                    }
                }
                log.info("Execution of TemplateQuery took ${it.reportResult.totalTime}ms for [C:${executedConfiguration.id}, RR: ${it.reportResult.id}, T:${templateId}, Q:${queryId}]")

            }

            if (executedConfiguration instanceof ExecutedIcsrProfileConfiguration && executedConfiguration.autoTransmit) {
                executedConfiguration.executedTemplateQueriesForProcessing.each {
                    icsrReportService.transmitAllCases(it, (ExecutedIcsrProfileConfiguration) executedConfiguration)
                }
            }

            if (!executedConfiguration.hasGeneratedCasesData) {
                executedConfiguration.status = ReportExecutionStatusEnum.COMPLETED
//                TODO need to add SQL logic to delete all generated cases
            }
            if (executedConfiguration instanceof ExecutedPeriodicReportConfiguration && executedConfiguration.periodicReportType == PeriodicReportTypeEnum.PADER) {
                sql.call("{call pkg_pader_line_listing.p_cleanup}")
            }
            executedConfiguration.totalExecutionTime = (System.currentTimeMillis() - startTime)
            CRUDService.instantSaveWithoutAuditLog(executedConfiguration)
            if (executedConfiguration instanceof ExecutedIcsrProfileConfiguration)
                markProfileExecutionStatus(sql, executedConfiguration, false)

        } catch (Exception e) {
            log.error("Unable to finish running executedConfiguration.id=${executedConfiguration.id}", e)
            Exception e1
            if (e instanceof ExecutionStatusException) {
                e1 = e as ExecutionStatusException
                e1.templateId = templateId
                e1.queryId = queryId
                e1.sectionName = sectionName
            } else {
                String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
                StringWriter sw = new StringWriter()
                e.printStackTrace(new PrintWriter(sw))
                String exceptionAsString = sw.toString()
                if (!message) {
                    message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
                }
                e1 = new ExecutionStatusException(templateId: templateId, queryId: queryId, sectionName: sectionName, errorMessage: message, errorCause: exceptionAsString)
            }
            if (executedConfiguration instanceof ExecutedIcsrProfileConfiguration)
                markProfileExecutionStatus(sql, executedConfiguration, true)
            throw e1
        } finally {
            sqlGenerationService.cleanGttCsvTables(executedConfiguration, sql)
            sql?.close()
        }
    }

    void checkAndExecuteDrillDowns(sql, ExecutedTemplateQuery drillDownSource, ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, ReportResult parent, ReportTemplate rootTemplate, Locale locale, boolean needToUseAlreadyGeneratedCases) {
        executedCaseLineListingTemplate.columnList?.reportFieldInfoList?.each { ReportFieldInfo col ->
            if (col.drillDownTemplate) {
                ExecutedCaseLineListingTemplate template = executedConfigurationService.createReportTemplate(col.drillDownTemplate)
                col.drillDownTemplate = template
                col.markDirty()
                CRUDService.saveWithoutAuditLog(col)
                template.interactiveOutput = (template.interactiveOutput || rootTemplate.interactiveOutput)
                if (!template.JSONQuery) template.JSONQuery = executedCaseLineListingTemplate.JSONQuery
                ReportResult drillDown = new ReportResult()
                drillDown.drillDownSource = drillDownSource
                drillDown.drillDownFilerColumns = col.drillDownFilerColumns
                drillDown.field = col.reportField.name
                drillDown.template = template
                drillDown.parent = parent
                drillDown.scheduledBy = drillDownSource.reportResult.scheduledBy
                drillDown.executionStatus = ReportExecutionStatusEnum.GENERATING
                CRUDService.saveWithoutAuditLog(drillDown)

                drillDownSource.executedTemplate = template
                //temporary changing template to CLL to execute drilldown
                //todo: PVC POC :run similar templates ones
                generateReportResultCSV(sql, drillDown, locale, needToUseAlreadyGeneratedCases, true)
                drillDownSource.executedTemplate = rootTemplate
                if (drillDown.executionStatus != ReportExecutionStatusEnum.ERROR) {
                    drillDown.executionStatus = ReportExecutionStatusEnum.COMPLETED
                }
                CRUDService.saveWithoutAuditLog(drillDown)
                checkAndExecuteDrillDowns(sql, drillDownSource, template, drillDown, rootTemplate, locale, needToUseAlreadyGeneratedCases)
            }
        }
    }

    void checkAndExecuteCustomSqlDrillDowns(sql, ExecutedTemplateQuery drillDownSource, ReportTemplate customSQLTemplate, ReportResult parent, ReportTemplate rootTemplate, Locale locale, boolean needToUseAlreadyGeneratedCases) {
        if (customSQLTemplate.drillDownTemplate) {
            ReportTemplate template = customSQLTemplate.drillDownTemplate
            ReportResult drillDown = new ReportResult()
            drillDown.drillDownSource = drillDownSource
            drillDown.drillDownFilerColumns = customSQLTemplate.drillDownFilerColumns
            drillDown.field = customSQLTemplate.drillDownField
            drillDown.template = template
            drillDown.parent = parent
            drillDown.scheduledBy = drillDownSource.reportResult.scheduledBy
            drillDown.executionStatus = ReportExecutionStatusEnum.GENERATING
            CRUDService.saveWithoutAuditLog(drillDown)
            drillDownSource.executedTemplate = template
            generateReportResultCSV(sql, drillDown, locale, needToUseAlreadyGeneratedCases, true)
            drillDownSource.executedTemplate = rootTemplate
            if (drillDown.executionStatus != ReportExecutionStatusEnum.ERROR) {
                drillDown.executionStatus = ReportExecutionStatusEnum.COMPLETED
            }
            CRUDService.saveWithoutAuditLog(drillDown)
            checkAndExecuteCustomSqlDrillDowns(sql, drillDownSource, template, drillDown, rootTemplate, locale, needToUseAlreadyGeneratedCases)
        }
    }

    private setTotalExecutionTimeForConfiguration(ReportConfiguration configuration, long executionTime) throws Exception {
        configuration.totalExecutionTime += executionTime
        CRUDService.update(configuration)
    }

    private adjustAsOfVersionDate(def configuration) {
        if(!configuration.scheduleDateJSON){
            return
        }
        //todo:  combine with adjustCustomDateRanges()

        def runOnce = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.RUN_ONCE)
        def hourly = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.HOURLY)
        def minutely = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.MINUTELY)

        if (!runOnce && !hourly && !minutely) {
            if (configuration.asOfVersionDate) {
                Period period = configurationService.getDeltaPeriod(configuration)
                configuration.asOfVersionDate =
                        configurationService.getNextDateAsPerScheduler(configuration.asOfVersionDate, period)
            }
        }
    }

    @Transactional
    public void saveExecutedTemplateQuery(ExecutedTemplateQuery executedTemplateQuery, ReportTemplate template, SuperQuery superQuery, Boolean isExecuteRptFromCount) {
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.executedConfiguration
        executedTemplateQuery.title = executedTemplateQuery.title ?: executedReportConfiguration.reportName
        executedTemplateQuery.executedQuery = superQuery ? queryService.createExecutedQuery(superQuery) : null
        Sql sql = new Sql(dataSource_pva)
        Locale locale = executedReportConfiguration.locale
        String meddraVersion = null
        try {
            if (locale.equals(Locale.JAPANESE)) {
                meddraVersion = sql.firstRow("select MEDDRA_VERSION FROM VW_PVR_MEDDRA_VERSION_J")?.MEDDRA_VERSION
            } else {
                meddraVersion = sql.firstRow("select MEDDRA_VERSION FROM VW_PVR_MEDDRA_VERSION")?.MEDDRA_VERSION
            }
        } catch (Exception ex) {
            log.error("save Executed Template Query",ex)
        } finally {
            sql?.close()
        }
        executedTemplateQuery.executedTemplate = executedConfigurationService.createReportTemplate(template)
        ReportResult result = new ReportResult(executionStatus: ReportExecutionStatusEnum.SCHEDULED, scheduledBy: userService.currentUser)
        userService.setOwnershipAndModifier(result)
        executedTemplateQuery.setReportResult(result)
        result.medDraVersion = meddraVersion
        Date lastUpdated = executedReportConfiguration.lastUpdated
        userService.setOwnershipAndModifier(executedTemplateQuery)
        CRUDService.instantSaveWithoutAuditLog(executedTemplateQuery)
        CRUDService.saveOrUpdate(executedReportConfiguration)
        String trn=System.currentTimeMillis().toString()
        AuditLogConfigUtil.logChanges(executedReportConfiguration,["lastUpdated":executedReportConfiguration.lastUpdated],["lastUpdated":lastUpdated],Constants.AUDIT_LOG_UPDATE,"", trn)
        AuditLogConfigUtil.logChanges(executedTemplateQuery,
                ["template"       : template.name,
                 "query"          : superQuery?.name,
                 "queryLevel"     : executedTemplateQuery.queryLevel.name(),
                 "queryValueLists": executedTemplateQuery.executedQueryValueLists?.toString(),
                 "dateRange"      : executedTemplateQuery.executedDateRangeInformationForTemplateQuery?.toString()
                ],
                [:], Constants.AUDIT_LOG_UPDATE,"", trn)

        executionStatusService.saveSectionAndExecuteService(executedTemplateQuery, isExecuteRptFromCount)
    }

    private adjustGlobalCustomDateRanges(ReportConfiguration configuration) {
        def runOnce = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.RUN_ONCE)
        def hourly = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.HOURLY)
        def minutely = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.MINUTELY)
        if (!runOnce && !hourly && !minutely) {
            if (configuration.globalDateRangeInformation.dateRangeStartAbsolute &&
                    configuration.globalDateRangeInformation.dateRangeEndAbsolute) {
                Period period = configurationService.getDeltaPeriod(configuration)
                configuration.globalDateRangeInformation.dateRangeStartAbsolute =
                        configurationService.getNextDateAsPerScheduler(configuration.globalDateRangeInformation.dateRangeStartAbsolute, period)
                configuration.globalDateRangeInformation.dateRangeEndAbsolute =
                        configurationService.getNextDateAsPerScheduler(configuration.globalDateRangeInformation.dateRangeEndAbsolute, period)
            }
        }
    }

    /**
     * Create an executed lockedConfiguration from a lockedConfiguration. Still needs TemplateQuery association.
     * @param lockedConfiguration
     * @param templateQuery
     * @param result
     * @throws Exception
     */

    // seting executionStatus here is pointless as its asynchronous
    void deliverReport(executedConfiguration) throws Exception {
        try {
            emailService.emailReportOutput(executedConfiguration)
            dmsService.uploadReport(executedConfiguration)
            if(executedConfiguration instanceof ExecutedReportConfiguration) {
                oneDriveRestService.saveReports(executedConfiguration)
            }
        } catch (Exception e) {
            log.error("Unable to deliver for Configuration.id=${executedConfiguration.reportName}", e)
            String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
            StringWriter sw = new StringWriter()
            e.printStackTrace(new PrintWriter(sw))
            String exceptionAsString = sw.toString()
            if (!message) {
                message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
            }
            Exception e1 = new ExecutionStatusException(errorCause: exceptionAsString, errorMessage: message)
            throw e1
        }
    }

    /**
     * For debug purposes return a list of String representing the SQL that is used to generate a report given a Configuration
     * @param configuration
     * @return List < Map >  with the innermost Map containing the SQL string for the following keys [templateQueryId, versionSql, querySql, reportSql, headerSql]
     */
    public List debugReportSQL(ReportConfiguration configuration) throws Exception {
        Tenants.withId((configuration.tenantId as Integer)){
            final Sql sql = new Sql(getReportConnection())
            Locale locale = configuration.owner.preference.locale
            sqlGenerationService.setSqlSessionContext(sql, locale)
            def formatter = new BasicFormatterImpl()
            def sqlList = []

            try {
                sqlList = configuration.templateQueries.collect { TemplateQuery tq ->
                    def sqlMap = [templateQueryId: "${tq.id}", versionSql: "", querySql: "", reportSql: "", headerSql: "", gttSql: ""]
                    boolean hasQuery = false
                    if (tq.usedQuery || configuration.productSelection || configuration.studySelection
                            || (configuration.usedEventSelection) || configuration.excludeNonValidCases
                            || configuration.suspectProduct || configuration.excludeDeletedCases) {
                        hasQuery = true
                    }
                    boolean bVoidedFlag = sqlGenerationService.isVoidedFlagOn(tq, locale)
                    String initialParamsInsert = sqlGenerationService.initializeReportGtts(tq, tq.usedTemplate, hasQuery, locale, 0, 0, 0, false, false, true)
                    String icsrQueryInsert = sqlGenerationService.initializeForICSRPadder(configuration, locale)
                    String templateReassesssString = sqlGenerationService.setReassessContextForTemplate(tq, hasQuery)
                    String queryReassessString = ""
                    List<String> queryReassessList = sqlGenerationService.setReassessContextForQuery(tq)
                    queryReassessList.each {
                        if (it.length() > 0) {
                            queryReassessString += it
                        }
                    }

                    String versionSql = initialParamsInsert.replace("END;", "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DEBUG_INFO','1'); END; ")
                    // Global Query gets handled in Periodic via CaseSeries
                    if ((configuration.instanceOf(Configuration) || configuration.instanceOf(IcsrReportConfiguration)) && configuration.globalQuery) {
                        versionSql += sqlGenerationService.getInsertStatementsToInsert(
                                configuration.globalQuery, null,
                                configuration.globalQueryValueLists, configuration.poiInputsParameterValues ?: (new HashSet<ParameterValue>()), locale, false, null, 0 , false)
                    }
                    versionSql += "@ begin"
                    if (icsrQueryInsert) {
                        versionSql = versionSql + icsrQueryInsert + "pkg_create_version_sql.p_get_icsr_agency_name; "
                    }

                    sqlMap.versionSql = formatter.format(versionSql + " pkg_app_lang_context.p_set_context_value('${locale}'); pkg_create_version_sql.p_main; end;")
                    sqlMap.querySql = sqlGenerationService.getInsertStatementsToInsert(tq, locale, tq.usedConfiguration.excludeNonValidCases,tq.usedConfiguration.excludeDeletedCases)  + "@ begin " +
                            queryReassessString.replace("{call ", "").replace("}", ";") + " pkg_create_report_sql.p_main_query; End; "
                    List reportAndHeaderSqlList = reportResultService.processTemplate(tq, tq.query ? true : false, sql, locale, false, false, true)
                    String caseListingReportSql = "declare x clob; begin x := pkg_create_report_sql.p_main(); end;"
                    String reportCstmCalls = "@ " + templateReassesssString.replace("{call ", "begin ").replace("}", " end;") + " @"
                    if (tq.usedTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
                        sqlMap.reportSql = reportCstmCalls + formatter.format(caseListingReportSql)
                    } else {
                        sqlMap.reportSql = "begin  pkg_create_report_sql.p_main_tabulation; End;"
                    }
                    sqlMap.headerSql = formatter.format((reportAndHeaderSqlList[1] ?: "") as String)
                    sqlMap.gttSql = formatter.format((reportAndHeaderSqlList[2] ?: "") as String)

                    if (tq.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                        sqlMap.gttSql = reportCstmCalls + sqlMap.gttSql
                    }

                    return sqlMap
                }
            } finally {
                sql?.close()
            }

            return sqlList
        }
    }

    //This method is used for viewing the SQL for the executed Reports for debuging purpose.
    public List<Map> debugExecutedReportSQL(ExecutedReportConfiguration executedReportConfiguration, Boolean basicSqlFlag = true) throws Exception {
        Tenants.withId((executedReportConfiguration.tenantId as Integer)) {
            Locale locale = executedReportConfiguration.owner.preference.locale
            final Sql sql = new Sql(getReportConnection())
            sqlGenerationService.setSqlSessionContext(sql, locale)
            List<Map> list = []
            try {
                executedReportConfiguration.executedTemplateQueries.each { ExecutedTemplateQuery executedTemplateQuery ->
                    list << fetchSectionSql(executedReportConfiguration.id, executedTemplateQuery.id, basicSqlFlag, sql)
                }
            } finally {
                sql?.close()
            }
            list
        }
    }

    //This method is used for viewing the SQL for the executed Case Series for debuging purpose.
    public Map debugCaseSeriesSql(ExecutedCaseSeries executedCaseSeries, Boolean basicSqlFlag = true) {
        Tenants.withId((executedCaseSeries.tenantId as Integer)){
            Locale locale = executedCaseSeries.owner.preference.locale
            final Sql sql = new Sql(getReportConnection())
            try {
                sqlGenerationService.setSqlSessionContext(sql, locale)
                fetchSectionSql(executedCaseSeries.id, executedCaseSeries.id, basicSqlFlag, sql)
            } catch (Exception ex) {
                log.error("debug Case Series Sql",ex)
            } finally {
                sql?.close()
            }
        }
    }

    //This method is used for viewing the SQL for the executed Reports for debuging purpose.
    public List<Map> debugExecutedInboundSQL(ExecutedInboundCompliance executedInboundCompliance, Boolean basicSqlFlag = true) throws Exception {
        Tenants.withId((executedInboundCompliance.tenantId as Integer)) {
            Locale locale = executedInboundCompliance.owner.preference.locale
            final Sql sql = new Sql(getReportConnection())
            sqlGenerationService.setSqlSessionContext(sql, locale)
            List<Map> list = []
            try {
                executedInboundCompliance.executedQueriesCompliance.each { ExecutedQueryCompliance executedQueryCompliance ->
                    list << fetchSectionSql(executedInboundCompliance.id, executedQueryCompliance.id, basicSqlFlag, sql)
                }
            } finally {
                sql?.close()
            }
            list
        }
    }

    public Map fetchSectionSql(Long objectId, Long sectionId, Boolean basicSqlFlag = true, Sql sql) {
        List<ViewSqlDTO> viewSqlDTOList = []
        String basicSql = """select rownum , script_name , executing_sql , abs (extract( day from diff ))*24*60 +
                        abs(extract( hour from diff ))*60 + abs(extract( minute from diff ) * 60 + extract(second from diff)) execution_time_mins,row_count
                        from (select id , script_name , executing_sql , log_date - finish_time as diff, row_count
                        from pvr_app_log
                        where object_id =${objectId}
                        and section_id = NVL(${sectionId},-1)
                        and VIEW_SQL_MODE = 1
                        order by id )"""

        String advanceSql = """select rownum , script_name , executing_sql , abs (extract( day from diff ))*24*60 +
                        abs(extract( hour from diff ))*60 + abs(extract( minute from diff ) * 60 + extract(second from diff)) execution_time_mins,row_count
                        from (select id , script_name , executing_sql , log_date - finish_time as diff, row_count
                        from pvr_app_log
                        where object_id =${objectId}
                        and section_id = NVL(${sectionId},-1)
                        order by id )"""
        sql.rows(basicSqlFlag ? basicSql : advanceSql).each {
            viewSqlDTOList.add(new ViewSqlDTO(it))
        }
        return [id: sectionId, data: viewSqlDTOList]
    }

    public Map debugGlobalQuerySQL(def configuration) throws Exception {
        ExecutorDTO executorDTO = ExecutorDTO.create(configuration)
        def formatter = new BasicFormatterImpl()
        def sqlMap = [globalVersionSql: "", globalQuerySql: "", caseSeriesSql: ""]
        String initialParamsInsert = sqlGenerationService.initializeCaseSeriesGtts(executorDTO)
        String initializeICSRPadderInsert = sqlGenerationService.initializeForICSRPadder(executorDTO)
        List<String> reassessPopulateQuerySql = sqlGenerationService.setReassessContextForQuery(executorDTO.globalQuery, executorDTO.startDate, executorDTO.endDate)
        String reportReassessSqls = ""
        reassessPopulateQuerySql.each {
            if (it.length() > 0) {
                reportReassessSqls += it.replace("{call ", "").replace("}", ";")
            }
        }
        if (initializeICSRPadderInsert) {
            initialParamsInsert = initialParamsInsert + initializeICSRPadderInsert + "@ begin pkg_create_version_sql.p_get_icsr_agency_name end;"
        }
        String versionSql = initialParamsInsert.replace("END;", "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DEBUG_INFO','1'); END;") +
                " @ begin pkg_app_lang_context.p_set_context_value('${executorDTO.locale}'); pkg_create_version_sql.p_main; end; "
        String caseSeriesSql = ""
        ExecutorDTO executorDTO_cs
        def executedConfiguration = null
        if (configuration instanceof BaseConfiguration) {
            executedConfiguration = executedConfigurationService.createExecutedConfiguration(configuration, null)
            if (configuration?.globalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE) {
                executorDTO_cs = executedConfiguration ? ExecutorDTO.create(new ExecutedCaseSeries(executedConfiguration, true)) : null
                caseSeriesSql += getCaseSeriesSQL(executorDTO_cs,1,executorDTO)
            }
            executorDTO_cs = executedConfiguration ? ExecutorDTO.create(new ExecutedCaseSeries(executedConfiguration, false)) : null
            caseSeriesSql += getCaseSeriesSQL(executorDTO_cs,0,executorDTO)
        } else if (configuration instanceof BaseCaseSeries) {
            executedConfiguration = new ExecutedCaseSeries()
            caseSeriesService.updateDetailsFrom(configuration, executedConfiguration)
            executorDTO_cs = ExecutorDTO.create(executedConfiguration)
            caseSeriesSql += getCaseSeriesSQL(executorDTO_cs, 0, executorDTO)
        }

        sqlMap.globalVersionSql = formatter.format(versionSql)
        sqlMap.globalQuerySql = sqlGenerationService.getInsertStatementsToInsert(executorDTO.globalQuery, null, executorDTO.globalQueryValueLists, executorDTO.poiInputsParameterValues ?: (new HashSet<ParameterValue>()), executorDTO.locale, executorDTO.excludeNonValidCases, executorDTO.owner ,null, executorDTO.excludeDeletedCases) + "@ begin " + reportReassessSqls + " pkg_create_report_sql.p_main_query; end; "
        sqlMap.caseSeriesSql = "\n"+caseSeriesSql
        return sqlMap
    }

    private String getCaseSeriesSQL(ExecutorDTO executorDTO_cs, int isCumulative, ExecutorDTO executorDTO){
        String appendQuery = ""
        if(executorDTO_cs) {
            Boolean hasQuery = executorDTO_cs.globalQuery || executorDTO_cs.excludeNonValidCases || executorDTO_cs?.dateRangeType?.name == Constants.EVENT_RECEIPT_DATE_PVR || executorDTO_cs.excludeDeletedCases
            appendQuery += "BEGIN PKG_QUERY_HANDLER.p_save_case_list('${executorDTO_cs?.name?.replaceAll("(?i)'", "''")}', null, ${executorDTO_cs.numOfExecutions}, ${isCumulative}, ${executorDTO_cs?.owner?.id},${executorDTO.name ? "'${executorDTO.name?.replaceAll("(?i)'", "''")}'" : null}, null, null, ${hasQuery ? 1 : 0}, 0, '${executorDTO_cs.caseSeriesOwner}');\tEND;\n"
            if (isCumulative==0 && executorDTO.includePreviousMissingCases && executorDTO.reportId) {
                appendQuery += "BEGIN pkg_submission_tracking.p_find_cases_missed (null, '${executorDTO_cs.caseSeriesOwner}');\tEND;\n"
            }
        }
        return appendQuery
    }
    /**
     * Look up configured columns for a ReportTemplate and executes the query to fetch the results and converted to JSON
     * Set the resultData object on ReportResult
     *
     * @param configuration
     * @return
     */

    private def generateReportResult(Sql sql, ReportResult result,Locale locale, boolean needToUseAlreadyGeneratedCases= false) throws Exception {
        def start = System.currentTimeMillis()
        def sectionStart = System.currentTimeMillis()

        String querySql = null
        String caseListInsertSql = null
        boolean hasQuery = true
        def reportSql = null
        String headerSql = null

        // construct w/ Connection object to ensure it will get cleaned up in finally block via Sql.close()

        // TODO: Use executed templates and queries
        ExecutedTemplateQuery executedTemplateQuery = result.executedTemplateQuery
        BaseConfiguration configuration = executedTemplateQuery.usedConfiguration

        def formatter = new BasicFormatterImpl()

        try {
            String versionSql = sqlGenerationService.generateVersionSQL(executedTemplateQuery, locale)
            boolean bVoidedFlag = sqlGenerationService.isVoidedFlagOn(executedTemplateQuery, locale)
            versionSql = sqlGenerationService.processDictionariesWithNoDLPRev(ExecutorDTO.create(configuration), versionSql)
            String versionFilterInsertSql = sqlGenerationService.processDictionariesWithDLPRev(executedTemplateQuery, locale)

            String versionInsertSql = sqlGenerationService.generateVersionTableInsert(versionSql)
            versionFilterInsertSql = sqlGenerationService.generateFinalVersionTableInsert(versionFilterInsertSql)

            //==========================================================================================================
            //Create the Query SQL
            Long caseSeriesId = result.executedTemplateQuery.executedConfiguration.caseSeriesId
//            We are always generating caseIds for PeriodicReports
            if (executedTemplateQuery.executedConfiguration.instanceOf(ExecutedPeriodicReportConfiguration)) {
                caseSeriesId = getCaseSeriesIdToUse(executedTemplateQuery)
                needToUseAlreadyGeneratedCases = true
            }
            //Create Query SQL
            //==========================================================================================================
            (hasQuery, caseListInsertSql, querySql) = sqlGenerationService.createQuerySql(executedTemplateQuery, configuration, hasQuery, caseListInsertSql, querySql, caseSeriesId, needToUseAlreadyGeneratedCases, locale)

            String initialParamsInsert = sqlGenerationService.initializeReportGtts(executedTemplateQuery, executedTemplateQuery.usedTemplate, hasQuery, locale)
            if (initialParamsInsert) {
                sql.execute(initialParamsInsert)
            }

            String icsrQueryInsert = sqlGenerationService.initializeForICSRPadder(configuration, locale)
            if (icsrQueryInsert) {
                sql.execute(icsrQueryInsert)
                sql.call("{call pkg_create_version_sql.p_get_icsr_agency_name}")
                // TODO call package PKG_NAME_ICSR_REPLACE
            }

            log.debug("VersionSQL = ${formatter.format(versionInsertSql)}")

            if(executedTemplateQuery.onDemandSectionParams) {
                Map dataMap = MiscUtil.parseJsonText(executedTemplateQuery.onDemandSectionParams)
                ReportResult reportResult = ReportResult.findById(dataMap.reportResultId)

                ReportResultData reportResultData = reportResult.data
                Map<String, ?> dataRow = reportService.getOutputJSONROW(reportResultData, dataMap.rowId)
                List<Tuple2<String, String>> caseParams = reportService.getCrosstabCaseIds(dataRow, dataMap.columnName)
                StringBuilder insertSql = new StringBuilder("Begin ")
                caseParams.each { pair ->
                    insertSql.append("Insert into GTT_VERSIONS (TENANT_ID, CASE_ID, VERSION_NUM) values (${Tenants.currentId() as Long}, ${pair.first}, ${pair.second});\n")
                }
                insertSql.append('END;')
                sql.execute(insertSql.toString().trim())

                String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(executedTemplateQuery, locale, executedTemplateQuery.usedConfiguration?.excludeNonValidCases, executedTemplateQuery.usedConfiguration?.excludeDeletedCases)
                if (insertQueryData) {
                    sql.execute(insertQueryData)
                }

                sql.call("{call pkg_create_report_sql.p_main_query}")

                result.versionTime = System.currentTimeMillis() - sectionStart
                result.versionRows = 0
                result.versionRowsFilter = 0
                result.filterVersionTime = System.currentTimeMillis() - sectionStart
                result.reAssessTime = System.currentTimeMillis() - sectionStart
                result.queryRows = 0
                result.queryTime = System.currentTimeMillis() - sectionStart

            }else {
                result = initializeGttTables(sql, result, sectionStart, executedTemplateQuery, hasQuery, formatter, caseListInsertSql, locale)
            }

            //==========================================================================================================
            //Create Report SQL
            //==========================================================================================================

            sectionStart = System.currentTimeMillis()
            List sqlResult = reportResultService.processTemplate(executedTemplateQuery, hasQuery, sql, locale)


            if (executedTemplateQuery.usedTemplate.templateType == TemplateTypeEnum.NON_CASE) {
                NonCaseSQLTemplate template = (NonCaseSQLTemplate) executedTemplateQuery.usedTemplate
                if (template.usePvrDB) {
                    //close previous connection and redefine connection to use PVR datasource
                    sql.close()
                    sql = new Sql(getReportConnectionForPVR())
                }
            }

            ReportMeasures reportMeasures = new ReportMeasures()
            Map headers = [:]
            reportSql = sqlResult[0]
            headerSql = sqlResult[1]
            String gttSql = sqlResult[2]

            File tempFile = reportResultService.processOutputDataTab(reportSql, headerSql, bVoidedFlag, reportMeasures, executedTemplateQuery, sql, headers)


            def headerList = headers? headers.get(executedTemplateQuery.usedTemplate.id) :[]

            logCasesToSubmissionHistory(sql, executedTemplateQuery)

            result.reportTime = System.currentTimeMillis() - sectionStart
            log.info("Time to execute report: ${result.reportTime}ms")

            //==========================================================================================================
            //Populate of ReportResultData
            //==========================================================================================================

            populateReportResultData("reportSQL", reportMeasures, result, tempFile, headerList, versionSql, querySql, reportSql, gttSql, headerSql, start)
            result.save(flush: true)
            logCasesToTracking(sql, executedTemplateQuery)
            log.debug("For Configuration: ${configuration}, \n querySql: ${querySql},\n reportSql: ${reportSql}, \n headerSql: ${headerSql}")
            return [querySql, reportSql, headerSql]

        } catch (Exception e) {
            log.error("Error in generate report result", e)
            // TODO will remove it once merging task completes
            ReportResult.executeUpdate("update ReportResult set executionStatus=:executionStatus where id =:id", [id: executedTemplateQuery.reportResult.id, executionStatus: ReportExecutionStatusEnum.ERROR])
            ExecutionStatusException e1 = generateReportResultExceptionHandler(e, querySql, reportSql, headerSql, configuration)

            throw e1
        }
    }

    public def getCaseData(String caseNumber, Integer versionNumber) throws Exception {
        Sql sql = new Sql(getReportConnection())
        try {
            String caseRecordSql = "SELECT tenant_id, case_id FROM V_C_IDENTIFICATION WHERE case_num=? AND version_num=" + versionNumber
            def caseInfo = sql.firstRow(caseRecordSql, [caseNumber])
            if (caseInfo) {
                sql.execute("begin pkg_cioms_i.p_main_case(?, ?, ?, ?, ?); end;", [caseInfo.tenant_id, caseInfo.case_id, versionNumber, protectPrivacy, blinded])
                reportSql += " WHERE cm.case_id=${caseInfo.case_id} AND cm.version_num=${versionNumber} AND cci.tenant_id=${caseInfo.tenant_id} AND cci.flag_blinded=${blinded} AND cci.flag_protect_privacy=${protectPrivacy}"
            }
        } catch (Exception ex) {
            log.error("get Case Data",ex)
        } finally {
            sql?.close()
        }
    }

    public def generateSingleCIOMSReport(ReportResult result, String caseNumber, Integer versionNumber, boolean blind, boolean privacy, Long processReportId = 0L, String prodHashCode = null) throws Exception {
        def start = System.currentTimeMillis()
        def sectionStart = System.currentTimeMillis()

        // Added flags according to improvemrnt https://rxlogixdev.atlassian.net/browse/PVR-5292
        def blinded = blind ? 1 : 0
        def protectPrivacy = privacy ? 1 : 0

        // construct w/ Connection object to ensure it will get cleaned up in finally block via Sql.close()
        Sql sql = new Sql(getReportConnection())

        String caseRecordSql = "SELECT tenant_id, case_id FROM V_SAFETY_IDENTIFICATION WHERE case_num=? AND version_num="+versionNumber
        def caseInfo = sql.firstRow(caseRecordSql, [caseNumber])
        if (caseInfo) {
            ExecutedTemplateQuery executedTemplateQuery = result.executedTemplateQuery
            BaseConfiguration configuration = executedTemplateQuery.usedConfiguration
            if (executedTemplateQuery instanceof ExecutedIcsrTemplateQuery) {
                Locale locale = userService.currentUser?.preference?.locale ?: configuration.owner?.preference?.locale
                String query = sqlGenerationService.initializeReportGtts(executedTemplateQuery, executedTemplateQuery.usedTemplate, executedTemplateQuery.executedQuery ? true : false, locale, 0, 0, processReportId)
                sql.execute(query)
            }
            if (executedTemplateQuery.executedTemplate?.ciomsI) {
                sql.execute("begin pkg_cioms_i.p_main_case(?, ?, ?, ?, ?); end;", [caseInfo.tenant_id, caseInfo.case_id, versionNumber, protectPrivacy, blinded])
            } else if (executedTemplateQuery.executedTemplate?.medWatch) {
                boolean deviceReportable = false
                if (executedTemplateQuery.usedConfiguration instanceof ExecutedIcsrProfileConfiguration || executedTemplateQuery.usedConfiguration instanceof IcsrProfileConfiguration) {
                    deviceReportable = executedTemplateQuery.usedConfiguration.deviceReportable
                }
                sql.execute("begin pkg_case_mw_form.p_main_case_mw(?, ?, ?, ?, ?, ?); end;", [privacy, blind, caseInfo.tenant_id, caseInfo.case_id, versionNumber, deviceReportable ? prodHashCode : "-1"])
            }

            try {
                //==========================================================================================================
                //Create Report SQL
                //==========================================================================================================

                sectionStart = System.currentTimeMillis()
                CustomSQLTemplate template = executedTemplateQuery.usedTemplate
                String reportSql = template.customSQLTemplateSelectFrom
                if (executedTemplateQuery.executedTemplate?.ciomsI) {
                    reportSql += " WHERE cm.case_id=${caseInfo.case_id} AND cm.version_num=${versionNumber} AND cci.tenant_id=${caseInfo.tenant_id} AND cci.flag_blinded=${blinded} AND cci.flag_protect_privacy=${protectPrivacy}"
                } else if (executedTemplateQuery.executedTemplate?.medWatch) {
                    if(reportSql.contains('cmwd')){
                        reportSql += " WHERE cm.case_id=${caseInfo.case_id} AND cm.version_num=${versionNumber} AND cmw.tenant_id=${caseInfo.tenant_id} AND cmw.flag_blinded=${blinded} AND cmw.flag_privacy_protected=${protectPrivacy} AND NVL(cmwd.flag_blinded,${blinded}) = ${blinded} AND NVL(cmwd.flag_privacy_protected,${protectPrivacy}) = ${protectPrivacy}"
                    } else {
                        reportSql += " WHERE cm.case_id=${caseInfo.case_id} AND cm.version_num=${versionNumber} AND cmw.tenant_id=${caseInfo.tenant_id} AND cmw.flag_blinded=${blinded} AND cmw.flag_privacy_protected=${protectPrivacy}"
                    }
                }

                ReportMeasures reportMeasures = new ReportMeasures()

                File tempFile = reportResultService.processOutputCiomsResult(reportSql, sql, configuration, executedTemplateQuery, reportMeasures)

                result.reportTime = System.currentTimeMillis() - sectionStart
                log.info("Time to execute report CSV: ${result.reportTime}ms")

                //==========================================================================================================
                //Populate of ReportResultData
                //==========================================================================================================

                populateReportResultData("reportSQLCSV", reportMeasures, result, tempFile, null, null, null, reportSql, null, null, start)
            } catch (Exception e) {
                log.error("Error in generate cioms result", e)
                ReportResult.executeUpdate("update ReportResult set executionStatus=:executionStatus where id =:id", [id: executedTemplateQuery.reportResult.id, executionStatus: ReportExecutionStatusEnum.ERROR])
                ExecutionStatusException e1 = generateReportResultExceptionHandler(e, null, null, null, configuration)
                throw e1
            } finally {
                sql?.close()
            }
        }
    }

    def getDrilldownOnDemandSection(String onDemandSectionParams){
        Map dataMap = MiscUtil.parseJsonText(onDemandSectionParams)
        ReportResult reportResult = ReportResult.findById(dataMap.reportResultId)
        Map<String, ?> dataRow = reportService.getOutputJSONROW(reportResult.data, dataMap.rowId)
        return (dataRow?.collect { it.key.startsWith(TransponsedDataSource.ROW_PREFIX) ? it.value : null} - null)?.join(", ")
    }

    private def generateReportResultCSV(Sql sql, ReportResult result, Locale locale, boolean needToUseAlreadyGeneratedCases, boolean isDrilldownCll = false) throws Exception {
        def start = System.currentTimeMillis()
        def sectionStart = System.currentTimeMillis()
        String querySql = null
        String caseListInsertSql = null
        boolean hasQuery = true
        def reportSql = null
        String headerSql = null

        // construct w/ Connection object to ensure it will get cleaned up in finally block via Sql.close()

        // TODO: Use executed templates and queries
        ExecutedTemplateQuery executedTemplateQuery = result.executedTemplateQuery
        BaseConfiguration configuration = executedTemplateQuery.usedConfiguration

        def formatter = new BasicFormatterImpl()
        try {
            String versionSql = sqlGenerationService.generateVersionSQL(executedTemplateQuery, locale)
            boolean bVoidedFlag = sqlGenerationService.isVoidedFlagOn(executedTemplateQuery, locale)
            versionSql = sqlGenerationService.processDictionariesWithNoDLPRev(ExecutorDTO.create(configuration), versionSql)
            String versionFilterInsertSql = sqlGenerationService.processDictionariesWithDLPRev(executedTemplateQuery, locale)

            String versionInsertSql = sqlGenerationService.generateVersionTableInsert(versionSql)
            versionFilterInsertSql = sqlGenerationService.generateFinalVersionTableInsert(versionFilterInsertSql)

            //==========================================================================================================
            //Create the Query SQL
            Long caseSeriesId = result.executedTemplateQuery.executedConfiguration.caseSeriesId
//            We are always generating caseIds for PeriodicReports
            if (executedTemplateQuery.executedConfiguration.instanceOf(ExecutedPeriodicReportConfiguration)) {
                caseSeriesId = getCaseSeriesIdToUse(executedTemplateQuery)
                needToUseAlreadyGeneratedCases = true
            }
            //Create Query SQL
            //==========================================================================================================
            (hasQuery, caseListInsertSql, querySql) = sqlGenerationService.createQuerySql(executedTemplateQuery, configuration, hasQuery, caseListInsertSql, querySql, caseSeriesId, needToUseAlreadyGeneratedCases, locale)

            String initialParamsInsert = sqlGenerationService.initializeReportGtts(executedTemplateQuery, executedTemplateQuery.usedTemplate, hasQuery, locale, 0, 0, 0, isDrilldownCll)
            sql.execute(initialParamsInsert)

            String icsrQueryInsert = sqlGenerationService.initializeForICSRPadder(configuration, locale)
            if(executedTemplateQuery instanceof ExecutedIcsrTemplateQuery){
                sql.call("{call pkg_create_version_sql.p_report_filters}")
            }
            if (icsrQueryInsert) {
                sql.execute(icsrQueryInsert)
                sql.call("{call pkg_create_version_sql.p_get_icsr_agency_name}")
                // TODO call package PKG_NAME_ICSR_REPLACE
            }

            log.debug("VersionSQL = ${formatter.format(versionInsertSql)}")

            if(executedTemplateQuery.onDemandSectionParams) {
                Map dataMap = MiscUtil.parseJsonText(executedTemplateQuery.onDemandSectionParams)
                ReportResult reportResult = ReportResult.findById(dataMap.reportResultId)

                ReportResultData reportResultData = reportResult.data
                Map<String, ?> dataRow = reportService.getOutputJSONROW(reportResultData, dataMap.rowId)
                List<Tuple2<String, String>> caseParams = reportService.getCrosstabCaseIds(dataRow, dataMap.columnName)
                StringBuilder insertSql = new StringBuilder("Begin ")
                caseParams.each { pair ->
                    insertSql.append("Insert into GTT_VERSIONS (TENANT_ID, CASE_ID, VERSION_NUM) values (${Tenants.currentId() as Long}, ${pair.first}, ${pair.second});\n")
                }
                insertSql.append('END;')
                sql.execute(insertSql.toString().trim())

                String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(executedTemplateQuery, locale, executedTemplateQuery.usedConfiguration?.excludeNonValidCases, executedTemplateQuery.usedConfiguration?.excludeDeletedCases)
                if (insertQueryData) {
                    sql.execute(insertQueryData)
                }

                sql.call("{call pkg_create_report_sql.p_main_query}")

                result.versionTime = System.currentTimeMillis() - sectionStart
                result.versionRows = 0
                result.versionRowsFilter = 0
                result.filterVersionTime = System.currentTimeMillis() - sectionStart
                result.reAssessTime = System.currentTimeMillis() - sectionStart
                result.queryRows = 0
                result.queryTime = System.currentTimeMillis() - sectionStart

            }else {
                result = initializeGttTables(sql, result, sectionStart, executedTemplateQuery, hasQuery, formatter, caseListInsertSql, locale)
            }
            //==========================================================================================================
            //Create Report SQL
            //==========================================================================================================
            sectionStart = System.currentTimeMillis()
            List sqlResult = reportResultService.processTemplate(executedTemplateQuery, hasQuery, sql, locale)

            reportSql = sqlResult[0]
            headerSql = sqlResult[1]
            String gttSql = sqlResult[2]

            logCasesToSubmissionHistory(sql, executedTemplateQuery)

            ReportMeasures reportMeasures = new ReportMeasures()

            if (executedTemplateQuery.usedTemplate.templateType == TemplateTypeEnum.NON_CASE) {
                NonCaseSQLTemplate template = (NonCaseSQLTemplate) executedTemplateQuery.usedTemplate
                if (template.usePvrDB) {
                    sql = new Sql(getReportConnectionForPVR())
                }
            }

            Map headers = [:]

            File tempFile = reportResultService.processOutputResult(reportSql, sql, bVoidedFlag, configuration, executedTemplateQuery, isDrilldownCll, result, headers, reportMeasures)

            result.reportTime = System.currentTimeMillis() - sectionStart
            log.info("Time to execute report CSV: ${result.reportTime}ms")

            //==========================================================================================================
            //Populate of ReportResultData
            //==========================================================================================================

            populateReportResultData("reportSQLCSV", reportMeasures, result, tempFile, headers, versionSql, querySql, reportSql, gttSql, headerSql, start)
            result.save(flush: true)

            logCasesToTracking(sql, executedTemplateQuery)

            log.debug("For Configuration: ${configuration}, \n querySql: ${querySql},\n reportSql: ${reportSql}, \n headerSql: ${headerSql}")

            return [querySql, reportSql, headerSql]

        } catch (Exception e) {
            log.error("Error on report execution", e)
            ReportResult.executeUpdate("update ReportResult set executionStatus=:executionStatus where id =:id", [id: executedTemplateQuery.reportResult.id, executionStatus: ReportExecutionStatusEnum.ERROR])
            ExecutionStatusException e1 = generateReportResultExceptionHandler(e, querySql, reportSql, headerSql, configuration)

            throw e1
        }
    }

    private Long getCaseSeriesIdToUse(ExecutedTemplateQuery executedTemplateQuery) {
        if (!executedTemplateQuery.usedConfiguration instanceof ExecutedPeriodicReportConfiguration
                && !executedTemplateQuery.usedConfiguration instanceof ExecutedIcsrReportConfiguration) {
            return null
        }
        if (executedTemplateQuery.usedConfiguration.cumulativeCaseSeries && (executedTemplateQuery.usedTemplate?.instanceOf(DataTabulationTemplate) || executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE)) {
            log.info("######### Using Cumultive Case Series Id for executedTemplateQuery: ${executedTemplateQuery.id} execution ########")
            return executedTemplateQuery.usedConfiguration.cumulativeCaseSeries.id
        }
        return executedTemplateQuery.usedConfiguration.caseSeries.id
    }

    private ReportResult initializeGttTables(Sql sql, ReportResult result, long sectionStart, BaseTemplateQuery templateQuery, boolean hasQuery, BasicFormatterImpl formatter, String caseListInsertSql, Locale locale) {
        BaseConfiguration configuration = templateQuery.usedConfiguration
        // Global Query gets handled in Periodic via CaseSeries
        if ((configuration instanceof ExecutedConfiguration || configuration instanceof ExecutedIcsrReportConfiguration) && configuration.executedGlobalQuery) {
            log.trace("Finding Global Query to execute")
            String globalAdhocQuery = sqlGenerationService.getInsertStatementsToInsert(
                    configuration.executedGlobalQuery, null,
                    configuration.executedGlobalQueryValueLists, configuration.poiInputsParameterValues ?: (new HashSet<ParameterValue>()), locale, false, null, 0 , false)

            if (globalAdhocQuery) {
                log.trace("Finding Global Query to execute")
                sql.execute(globalAdhocQuery)
            }
        }

        log.debug("Executing Version SQL")
        if(templateQuery instanceof ExecutedTemplateQuery && !templateQuery.executedConfiguration.instanceOf(ExecutedIcsrProfileConfiguration)){
            if (sqlGenerationService.isCumulativeCaseSeriesAvailable(templateQuery.usedConfiguration)) {
                sql.call("{call pkg_create_version_sql.P_RESET_VERSION_BASE_FLAG}")
            }
            sql.call("{call pkg_create_version_sql.p_main}")
        }
        result.versionTime = System.currentTimeMillis() - sectionStart
        result.versionRows = 0
        result.versionRowsFilter = 0
        result.filterVersionTime = System.currentTimeMillis() - sectionStart
        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_VERSIONS')}")
        sectionStart = System.currentTimeMillis()

        // Add query reasses...
        List<String> reassessPopulateQuerySql = sqlGenerationService.setReassessContextForQuery(templateQuery)
        reassessPopulateQuerySql.each {
            if (it.length() > 0) {
                sql.call(it)
            }
        }
        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_DS_REASSESS_QUERY')}")

        result.reAssessTime = System.currentTimeMillis() - sectionStart
        sectionStart = System.currentTimeMillis()
        // if (hasQuery) {
//            TODO need to remove later
//            log.debug("QuerySQL = ${hasQuery ? formatter.format(caseListInsertSql) : "None"}")
//            if (!sql.execute(caseListInsertSql)) { // there is an updateCount or there were no results
//                result.queryRows = sql.updateCount
//            }
        String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(templateQuery, locale, templateQuery.usedConfiguration?.excludeNonValidCases,templateQuery.usedConfiguration?.excludeDeletedCases)
        if (insertQueryData) {
            sql.execute(insertQueryData)
            sql.call("{call pkg_create_report_sql.p_main_query}")
            result.queryRows = 0 //TODO need to fix this by Amrit
        }
        result.queryTime = System.currentTimeMillis() - sectionStart
        // }
        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_QUERY_CASE_LIST')}")

        String reassessPopulateSql = sqlGenerationService.setReassessContextForTemplate(templateQuery, hasQuery)
        //==========================================================================================================
        if (reassessPopulateSql.length() > 0) {
            sql.call(reassessPopulateSql)
        }

        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_DATASHEET_REASSESS')}")
        return result
    }

    private ExecutionStatusException generateReportResultExceptionHandler(Exception e, String querySql, reportSql, String headerSql, BaseConfiguration configuration) {
        String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
        StringWriter sw = new StringWriter()
        e.printStackTrace(new PrintWriter(sw))
        String exceptionAsString = sw.toString()
        if (!message) {
            message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
        }
        Exception e1 = new ExecutionStatusException(errorMessage: message, errorCause: exceptionAsString,
                querySql: querySql, reportSql: reportSql, headerSql: headerSql)

        def error = """
                \n\n====================================================================================================
                \nError Generating Report Result: (ID: ${configuration.id})
                \nError Generating Report Result: ${message}
                \n\n====================================================================================================
                """.stripIndent()
        log.error(error)
        return e1
    }

    private void populateReportResultData(String method, ReportMeasures reportMeasures, ReportResult result, File tempFile,
                                          def headers, String versionSql, String querySql, reportSql, String gttSql,
                                          String headerSql, long start) {
        result.reportRows = reportMeasures.rowCount
        result.caseCount = reportMeasures.caseCount
        log.info("Executed ${method}, storing temp results for ${reportMeasures.rowCount} rows, temp filesize: ${tempFile.size()} bytes")

        // wrap up some metrics and set result
        ReportResultData data = new ReportResultData()
        // should we add a check for value size here and show a nice error instead of spamming out the huge validation error?
        //TODO need to check if empty check is good enough
        data.crossTabHeader = headers? new JsonBuilder(headers) : null
        data.encryptedValue = (reportMeasures.rowCount == 0L) ? null : tempFile.bytes
        // set the value to null if there were no rows, due to the "[]" hack above
        data.versionSQL = versionSql
        data.querySQL = querySql
        data.reportSQL = reportSql
        data.gttSQL = gttSql
        data.headerSQL = headerSql

        result.data = data
        result.totalTime = System.currentTimeMillis() - start
        result.runDate = new Date()

        //tempFile.delete() TODO need to check due to 4.1.8
    }

    /**
     * THIS IS ONLY REQUIRED TO ADDRESS THE ORACLE GLOBAL TEMPORARY TABLE CLEAN UP IN ORACLE
     *
     * Provide a DataSource that does NOT use a connection pool. This is done to leverage the Global Temporary Tables in Oracle,
     * which are used to store the temporary results of report execution, however they are bound to the session that calls them and will
     * automatically be cleaned up, a desired outcome, but only once the session and the connection bound to that session terminate.
     * Which does not occur at the end of report execution if you use a connection pool, it manages the destruction and re-use of the
     * underlying connections.
     *
     * @return Connection
     */
    private Connection getReportConnection() {
        return utilService.getReportConnection()
    }

    private Connection getReportConnectionForPVR() {
        return utilService.getReportConnectionForPVR()
    }


    private void addExecutedGlobalQueryRelated(ReportConfiguration configuration, ExecutedReportConfiguration executedReportConfiguration) {

        if (configuration.globalDateRangeInformation) {
            Date startDate = configuration.globalDateRangeInformation?.getReportStartAndEndDate()[0] ?: new Date()
            Date endDate = configuration.globalDateRangeInformation?.getReportStartAndEndDate()[1] ?: new Date()
            executedReportConfiguration.executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(
                    dateRangeEnum: configuration.globalDateRangeInformation?.dateRangeEnum,
                    relativeDateRangeValue: configuration.globalDateRangeInformation?.relativeDateRangeValue,
                    dateRangeEndAbsolute: endDate, dateRangeStartAbsolute: startDate,
                    //TODO: for version as of generated date should be implemented
                    executedAsOfVersionDate: configuration.getAsOfVersionDateCustom(true) ?: endDate,
                    executedReportConfiguration: executedReportConfiguration
            )
        }

        if (configuration.globalQuery) {
            executedReportConfiguration.executedGlobalQuery = queryService.createExecutedQuery(configuration.globalQuery)
        }
        configuration.globalQueryValueLists?.each {
            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: it.query)
            it.parameterValues.each {
                ParameterValue executedValue
                if (it.hasProperty('reportField')) {
                    executedValue = new ExecutedQueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value)
                } else {
                    executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                }
                executedQVL.addToParameterValues(executedValue)
            }
            executedReportConfiguration.addToExecutedGlobalQueryValueLists(executedQVL)
        }
    }


    public void generateCasesResult(ExecutorDTO executorDTO, Boolean refreshCaseSeries = false) {
        String querySql = null
        String caseListInsertSql = null
        def reportSql = null
        Boolean hasQuery = true
        def sectionStart = System.currentTimeMillis()
        Sql sql = new Sql(getReportConnection())
        def formatter = new BasicFormatterImpl()

        SuperQuery globalQuery = executorDTO.globalQuery

        try {
            sqlGenerationService.setCurrentSqlInfo(sql, executorDTO.locale)
            //==========================================================================================================
            //Create the Query SQL

            if (globalQuery || executorDTO.excludeNonValidCases || executorDTO?.dateRangeType?.name == Constants.EVENT_RECEIPT_DATE_PVR || executorDTO.excludeDeletedCases ) {
                querySql = processMultipleQueries(globalQuery, executorDTO)
                caseListInsertSql = sqlGenerationService.generateCaseListInsert(querySql, null, null, false)
            } else {
                hasQuery = false
            }
            //==========================================================================================================
            //Execute version SQL
            String initialParamsInsert = sqlGenerationService.initializeCaseSeriesGtts(executorDTO)
            if (initialParamsInsert) {
                sql.execute(initialParamsInsert)
            }
            String initializeICSRPadderInsert = sqlGenerationService.initializeForICSRPadder(executorDTO)
            if (initializeICSRPadderInsert) {
                sql.execute(initializeICSRPadderInsert)
                sql.call("{call pkg_create_version_sql.p_get_icsr_agency_name}")
                // TODO call package PKG_NAME_ICSR_REPLACE
            }
            log.debug("Executing Version Insert SQL")
            sql.call("{call pkg_create_version_sql.p_main}")
            sql.updateCount = 0
            executorDTO.reportResultDTO.versionTime = System.currentTimeMillis() - sectionStart
            executorDTO.reportResultDTO.filterVersionTime = System.currentTimeMillis() - sectionStart
            sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_VERSIONS')}")
            sectionStart = System.currentTimeMillis()
            // Add query reasses...
            List<String> reassessPopulateQuerySql = sqlGenerationService.setReassessContextForQuery(globalQuery, executorDTO.startDate, executorDTO.endDate)
            reassessPopulateQuerySql.each {
                if (it.length() > 0) {
                    sql.call(it)
                }
            }
            sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_DS_REASSESS_QUERY')}")
            executorDTO.reportResultDTO.reAssessTime = System.currentTimeMillis() - sectionStart
            sectionStart = System.currentTimeMillis()
            if (hasQuery) {
//                TODO need to remove later
//                log.debug("QuerySQL = ${hasQuery ? formatter.format(caseListInsertSql) : "None"}")
//                if (!sql.execute(caseListInsertSql)) { // there is an updateCount or there were no results
//                    executorDTO.reportResultDTO.queryRows = sql.updateCount
//                }
                String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(globalQuery, null, executorDTO.globalQueryValueLists, executorDTO.poiInputsParameterValues ?: (new HashSet<ParameterValue>()), executorDTO.locale, executorDTO.excludeNonValidCases, executorDTO.owner, executorDTO.excludeDeletedCases)
                if (insertQueryData) {
                    sql.execute(insertQueryData)
                    sql.call("{call pkg_create_report_sql.p_main_query}")
                    executorDTO.reportResultDTO.queryRows = 0 //TODO need to fix this this by Amrit
                }
                executorDTO.reportResultDTO.queryTime = System.currentTimeMillis() - sectionStart
            }

            sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_QUERY_CASE_LIST')}")

            if (refreshCaseSeries) {
                String refreshCaseSeriesQuery = "{call PKG_QUERY_HANDLER.p_refresh_case_list(${executorDTO.caseSeriesId}, ${executorDTO?.owner?.id}, ${hasQuery ? 1 : 0}, '${executorDTO.caseSeriesOwner}')}"
                sql.call(refreshCaseSeriesQuery)
            } else {
                String saveGeneratedCaseListQuery = "{call PKG_QUERY_HANDLER.p_save_case_list('${executorDTO?.name?.replaceAll("(?i)'", "''")}',${executorDTO.caseSeriesId},${executorDTO.numOfExecutions},${executorDTO.isCumulative != null ? (executorDTO.isCumulative ? 1 : 0) : null},${executorDTO?.owner?.id},${executorDTO.reportName ? "'${executorDTO.reportName?.replaceAll("(?i)'", "''")}'" : null},${executorDTO.reportId},null,${hasQuery ? 1 : 0}, 0, '${executorDTO.caseSeriesOwner}')}"
                sql.call(saveGeneratedCaseListQuery)
            }

            if (executorDTO.includePreviousMissingCases && executorDTO.reportId && !executorDTO.isCumulative) {
                sql.call("{call pkg_submission_tracking.p_find_cases_missed (${executorDTO.reportId}, '${executorDTO.caseSeriesOwner}')}")
            }

            log.info "Execution time details : ${executorDTO.reportResultDTO}"
        } catch (Exception e) {
            String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
            StringWriter sw = new StringWriter()
            e.printStackTrace(new PrintWriter(sw))
            String exceptionAsString = sw.toString()
            if (!message) {
                message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
            }
            Exception e1 = new ExecutionStatusException(errorMessage: message, errorCause: exceptionAsString,
                    querySql: querySql, reportSql: reportSql)

            def error = """
                \n\n====================================================================================================
                \nError Generating Report Result: (ID: ${executorDTO?.caseSeriesId})
                \nError Generating Report Result: ${message}
                \n\n====================================================================================================
                """.stripIndent()
            log.error(error, e)

            throw e1
        } finally {
            sql?.close()
        }

    }


    def generatePreviewDataSource(ReportTemplate template) {
        ReportTemplate.withNewSession {
            Locale locale = template.owner.preference.locale
            ReportConfiguration config = new PeriodicReportConfiguration(
                    evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                    dateRangeType: DateRangeType.findByName(CASE_RECEIPT_DATE), //TODO:Need to check for this.
                    periodicReportType: PeriodicReportTypeEnum.PBRER,
                    sourceProfile: TestUtils.createSourceProfile()
            )
            TemplateQuery templateQuery = new TemplateQuery(
                    template: template,
                    report: config,
                    queryLevel: QueryLevelEnum.CASE
            )
            def dateRangeInformation = new DateRangeInformation(
                    templateQuery: templateQuery,
                    dateRangeEnum: DateRangeEnum.PR_DATE_RANGE)
            def globalDateRangeInformation = new GlobalDateRangeInformation(
                    templateQuery: templateQuery,
                    dateRangeEnum: DateRangeEnum.CUSTOM,
                    dateRangeStartAbsolute: DateUtil.StringToDate('20160101', Constants.DateFormat.BASIC_DATE),
                    dateRangeEndAbsolute: DateUtil.StringToDate('20161231', Constants.DateFormat.BASIC_DATE)
            )
            config.globalDateRangeInformation = globalDateRangeInformation
            templateQuery.dateRangeInformationForTemplateQuery = dateRangeInformation

            Sql sql = new Sql(getReportConnection())
            try {
                //Execute version SQL
                String initialParamsInsert = sqlGenerationService.initializeReportGtts(templateQuery, template, true, locale)
                if (initialParamsInsert) {
                    def result = sql.execute(initialParamsInsert)
                    def countResult = sql.firstRow('SELECT COUNT(*) as cont FROM GTT_REPORT_INPUT_FIELDS')
                }

                def querySql = 'SELECT TENANT_ID, case_id, MAX(version_num) FROM C_IDENTIFICATION GROUP BY case_id, tenant_id'
                def caseListInsertSql = sqlGenerationService.generateCaseListInsert(querySql, null, null, false)
                if (caseListInsertSql) {
                    def result = sql.execute(caseListInsertSql)
                }
                //log.debug("Executing Version Insert SQL")
                //sql.call("{call pkg_create_version_sql.p_main}")
                List sqlResult = reportResultService.processTemplate(templateQuery, templateQuery.query as boolean, sql, locale)
                ReportMeasures reportMeasures = new ReportMeasures()

                def reportSql = sqlResult[0]
                def headerSql = sqlResult[1]
                String gttSql = sqlResult[2]


                //GZIPOutputStream zipStream = null

                ByteArrayOutputStream baos = new ByteArrayOutputStream()
                /*
                try {
                    //File tempFile = File.createTempFile(generateRandomName(), ".csv.gzip", new File(grailsApplication.config.tempDirectory as String))
                    //log.info("Temp directory: ${grailsApplication.config.tempDirectory} file: {$tempFile.name}")
                    //zipStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
                    //Writer writer = new OutputStreamWriter(zipStream, StandardCharsets.UTF_8)
                    Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)
                    //if (bVoidedFlag) {
                    //    reportSql = sqlGenerationService.replaceVoidedTable(reportSql)
                    //}
                    //log.debug("ReportSQL = ${formatter.format(reportSql)}")
                    //log.info("Running executeSingleReportSQLCSV() for Configuration: (ID:  ${configuration.id})")
                    try {
                        reportSql = "SELECT * FROM (${reportSql}) WHERE ROWNUM <= 100"
                        executeSingleReportSQLCSV(reportSql, sql, writer, reportMeasures, template)
                    } finally {
                        writer?.flush()
                        writer?.close()
                    }
                } finally {
                    //zipStream?.close()
                }
                */
                return baos.toByteArray()
            } catch (Exception ex) {
                log.error("generate Preview Data Source",ex)
            } finally {
                sql?.close()
            }
        }
    }


    private String processMultipleQueries(SuperQuery query, ExecutorDTO executorDTO) {
        //TODO: firstQuery will be the only query we execute after Configuration is changed.
        String querySQL = null
        if (query) {
            switch (query?.queryType) {
                case QueryTypeEnum.QUERY_BUILDER:
                    querySQL = "${sqlGenerationService.generateQuerySQL(executorDTO, query, false, false, 0)}"
                    break;
                case QueryTypeEnum.SET_BUILDER:
                    querySQL = "${sqlGenerationService.generateSetSQL(executorDTO, query)}"
                    break;
                case QueryTypeEnum.CUSTOM_SQL:
                    querySQL = "${sqlGenerationService.generateCustomQuerySQL(executorDTO, query)}"
                    break
            }
        } else {
            querySQL = "${sqlGenerationService.generateEmptyQuerySQL(executorDTO)}"
        }

        String result = "SELECT query.tenant_id, query.case_id, query.version_num"

        result += " FROM ($querySQL) query"

        boolean whereAdded = false

        if (executorDTO.usedEventSelection) {
            def eventSelectionSQL = sqlGenerationService.getQueryEventDictFilter(executorDTO)
            String withClause = eventSelectionSQL[0]
            if (withClause) {
                result = "$withClause $result"
            }

            if (executorDTO.productSelection && !executorDTO.suspectProduct) {
                result = """with $result,
                        (${eventSelectionSQL[1]}) eventSelection WHERE query.case_id = eventSelection.case_id
                        AND query.AE_REC_NUM = eventSelection.AE_REC_NUM"""
            } else if (executorDTO.productSelection && executorDTO.suspectProduct) {
                result = """with $result,
                        (${eventSelectionSQL[1]}) eventSelection,  (Select cp.case_id,
                        cp.prod_rec_num
                        FROM
                        C_PROD_IDENTIFICATION cp  WHERE cp.DRUG_TYPE=1) caseProduct  where
                        query.case_id = eventSelection.case_id AND
                        caseProduct.case_id = query.case_id And
                        eventSelection.case_id = caseProduct.case_id
                        AND query.AE_REC_NUM = eventSelection.AE_REC_NUM AND
                        caseProduct.prod_rec_num = eventSelection.AE_REC_NUM AND
                        eventSelection.AE_REC_NUM = caseProduct.prod_rec_num"""
            } else if (executorDTO.studySelection) {
                def studySelectionSQL = sqlGenerationService.getQueryStudyDictFilter(executorDTO)
                result = """with ${studySelectionSQL[0]}, $result, (${studySelectionSQL[1]}) studySelection,
                        (${eventSelectionSQL[1]}) eventSelection WHERE query.case_id = eventSelection.case_id
                        AND query.AE_REC_NUM = eventSelection.AE_REC_NUM AND query.case_id = studySelection.case_id"""
            } else {
                result = """with $result, (${eventSelectionSQL[1]}) eventSelection WHERE query.case_id = eventSelection.case_id
                        AND query.AE_REC_NUM = eventSelection.AE_REC_NUM"""
            }
            whereAdded = true

        } else {
            if (executorDTO.productSelection) {
                result = """$result
                        """
                whereAdded = false
            }
            //           Fix for running ProductSelection and Study Selection Together
            if ((!executorDTO.productSelection || (executorDTO.aClass == PeriodicReportConfiguration.class)) && executorDTO.studySelection) {
                def studySelectionSQL = sqlGenerationService.getQueryStudyDictFilter(executorDTO)
                result = "with ${studySelectionSQL[0]} $result, (${studySelectionSQL[1]}) studySelection"
                if (executorDTO.productSelection) {
                    String str = """( query.prod_exp_id IN ( SELECT product_id FROM ("""
                    String productFilter = " OR " + sqlGenerationService.appendProductFilterInfo(executorDTO, str) + ")))"

                    result += " WHERE (query.case_id = studySelection.case_id ${productFilter})"
                } else {
                    result += " WHERE query.case_id = studySelection.case_id"
                }
                whereAdded = true
            }
        }

        if (executorDTO.suspectProduct && executorDTO.productSelection && (executorDTO.usedEventSelection)) {

            result += sqlGenerationService.suspectProductSql()
            whereAdded = true
            log.debug("Suspect product value checked")
        }

        if (executorDTO.excludeNonValidCases) {
            SuperQuery nonValidQuery = SuperQuery.findByNonValidCasesAndIsDeletedAndOriginalQueryId(true,false,0L)
            if (nonValidQuery) {
                if (!whereAdded) {
                    result += " WHERE"
                } else {
                    result += " AND"
                }
                result += sqlGenerationService.excludeNonValidCases(nonValidQuery, executorDTO)
                log.debug("Excluding non-valid cases with 'Non-Valid Cases' query")

            } else {
                log.error("Non-valid query does not exist! None found by name: 'Non-Valid Cases'")
            }
        }
        if(executorDTO.excludeDeletedCases){
            SuperQuery deletedQuery = SuperQuery.findByDeletedCasesAndIsDeletedAndOriginalQueryId(true,false,0L)
            if (deletedQuery) {
                if (!whereAdded) {
                    result += " WHERE"
                } else {
                    result += " AND"
                }
                result += sqlGenerationService.excludeDeletedCases(deletedQuery, executorDTO)
                log.debug("Excluding deleted cases with 'Deleted Cases' query")

            } else {
                log.error("Deleted Cases query does not exist! None found by name: 'Non-Valid Cases'")
            }
        }

        return result
    }

    Set<String> addCaseToGeneratedList(CaseCommand caseCommand, User user) {
        Sql sql
        Set<String> warnings = []
        try {
            sql = new Sql(getReportConnection())
            Long startTime = System.currentTimeMillis()
            sql.execute("insert into GTT_FILTER_KEY_VALUES (CODE,TEXT) values(?,?)", [caseCommand.versionNumber ?: -1, caseCommand.caseNumber])
            ExecutedCaseSeries executedCaseSeries = caseCommand.executedCaseSeries
            String endDate = executedCaseSeries?.executedCaseSeriesDateRangeInformation?.reportStartAndEndDate[1]?.format(SqlGenerationService.DATE_FMT)
            String dateRangeType = executedCaseSeries.dateRangeType.name
            String evaluateDateAs = executedCaseSeries.evaluateDateAs
            String versionAsOfDate = executedCaseSeries.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ? executedCaseSeries?.asOfVersionDate?.format(SqlGenerationService.DATE_FMT) : null
            Integer includeLockedVersion = executedCaseSeries?.includeLockedVersion ? 1 : 0
            sql.call("{?= call PKG_QUERY_HANDLER.f_save_cstm_case_series(?,?,?,?,?,?,?,?,?,?,?,?)}",
                    [Sql.NUMERIC, executedCaseSeries.seriesName, executedCaseSeries.version, executedCaseSeries.owner.id, executedCaseSeries.id, caseCommand.justification, caseCommand.justification ? 0 : 1, endDate, dateRangeType, evaluateDateAs, versionAsOfDate, includeLockedVersion, executedCaseSeries.caseSeriesOwner]) { result ->
                if (result != 0) {
                    warnings = sql.rows("SELECT * from GTT_REPORT_INPUT_PARAMS").collect { it.PARAM_VALUE }
                }
            }
            Long endTime = System.currentTimeMillis()
            log.info("Time taken to save the Case Series \'${executedCaseSeries.seriesName}\' in DB : " + ((endTime - startTime) / 1000))
        } catch (Exception e) {
            log.error("Exception while adding case to generated list",e)
        } finally {
            sql?.close()
        }
        return warnings
    }


    void removePreviewCaseSeries(ExecutedCaseSeries executedCaseSeries, User user) {
        Sql sql = new Sql(getReportConnection())
        try {
            String deleteCaseSeries = "{call PKG_QUERY_HANDLER.p_remove_case_list('${user?.id}', ${executedCaseSeries?.id}, 1, '${executedCaseSeries.caseSeriesOwner}')}"
            sql.call(deleteCaseSeries)
        } finally {
            sql?.close()
        }
    }


    void removeCaseFromGeneratedList(List<CaseCommand> caseCommandList, User user, ExecutedCaseSeries caseSeries) {
        Sql sql = new Sql(getReportConnection())
        try {
            sql.call("{?= call PKG_QUERY_HANDLER.p_modify_case_with_version(?,?,?,?,?,?,?,?,?,?)}", [
                    Sql.NUMERIC,
                    -1,
                    caseCommandList*.caseNumber.join(','),
                    caseCommandList*.versionNumber.join(','),
                    user.id,
                    caseSeries.id,
                    caseCommandList*.justification[0],
                    0,
                    0,
                    -1,
                    caseSeries.caseSeriesOwner
            ]) { result ->
                if (result == 0) {
                    caseCommandList*.errors*.rejectValue("caseNumber", "caseCommand.caseNumber.invalid")
                    caseCommandList*.errors*.rejectValue("versionNumber", "caseCommand.versionNumber.invalid")
                    throw new ValidationException("Validation Exception in CaseCommand", caseCommandList.first().errors)
                }
                if (result < 0) {
                    throw new Exception("Database server side error")
                }
            }
        } finally {
            sql?.close()
        }
    }


    void saveTags(String caseLevelTags,String globalTags,Long caseNumber,Long cid, String owner) {
        Sql sql = new Sql(getReportConnection())
        try {
            sql.call("{ call pkg_tag_mapping.p_tag_mapping(?,?,?,?,?)}", [caseLevelTags,globalTags,caseNumber,cid,owner])
        } catch (Exception e) {
            log.error("Error while adding tag",e)
        }finally {
            sql?.close()
        }
    }

    void saveCaseComment(Long caseUniqueId, String comments) {
        Sql sql = new Sql(getReportConnection())
        try {
            sql.call("{ call PKG_QUERY_HANDLER.p_save_comments(?,?)}", [caseUniqueId, comments])
        } finally {
            sql?.close()
        }
    }

    private Map transformCaseRow(Map m, boolean forDownload) {
        m.each {
            Map config = detailedCaseSeriesService.primaryFields.get(it.key) ?: (detailedCaseSeriesService.secondaryFields.get(it.key))
            if (config) {
                switch (config.type) {
                    case 'Date':
                        //No date field transform if for download using
                        if(!forDownload)
                        it.value = (it.value ? it.value.format(DateUtil.DATEPICKER_UTC_FORMAT) : it.value)
                        break
                    case 'Boolean':
                        it.value = (it.value ? true : false)
                        break
                    case 'String[]':
                        it.value = (it.value != null ? it.value.tokenize(",") : [])
                        break
                    default:
                        def val = it.value
                        if (val instanceof Clob) {
                            Reader reader = val.getCharacterStream()
                            StringWriter writer = new StringWriter()
                            org.apache.commons.io.IOUtils.copy(reader, writer)
                            it.value = writer.toString()
                        } else {
                            it.value = val?.toString()
                        }
                }
            } else {
                it.value = it.value?.toString()
            }
        }
        return m
    }

    private String getSearchQuery(Map<String,String> searchMap){
        String search = ''
        searchMap.each {
            Map config = detailedCaseSeriesService.primaryFields.get(it.key
            )
            if(config.search && it.value != null && it.value != ''){
                search = search + " AND ${config.search} like :${it.key} "
            }
        }
        return search
    }


    List getDetailedCaseOfSeries(Long caseSeriesId, Integer offset, Integer max, String sort, String direction, Map<String,String> searchMap, String caseSeriesOwner, boolean forDownload = false) {
        Sql sql = new Sql(getReportConnection())
        List<Map> caseDTOList = []
        Integer count = 0
        Integer totalCount = 0
        try {
            int index = 0
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { stmt ->
                stmt.fetchSize = (max > 0 ? max : 300)
            }
            Long exPvdCaseSeriesId = sql.firstRow([EX_CASESERIES_ID: caseSeriesId, CASESERIES_OWNER: caseSeriesOwner], detailedCaseSeriesService.pvdExtractIdQuery).EX_PVD_CASESERIES_ID

            String searchQuery = getSearchQuery(searchMap)
            Map searchBind = searchMap.findAll {
                it.value != null && it.value != ''
            }.collectEntries { k, v -> [(k): v.toString().toUpperCase()] }
            sql.eachRow(detailedCaseSeriesService.getQueryToExecute(searchQuery, sort, direction), [EX_PVD_CASESERIES_ID: exPvdCaseSeriesId, EX_CASESERIES_ID: caseSeriesId, CASESERIES_OWNER: caseSeriesOwner] + searchBind, offset, max) { GroovyResultSet resultSet ->
                if (index == 0) {
                    count = (resultSet.cnt?.toInteger()) ?: 0
                    totalCount = (resultSet.total_count?.toInteger()) ?: 0
                }
                index++
                caseDTOList.add(transformCaseRow(resultSet.toRowResult(), forDownload))
            }
            return [count, totalCount, caseDTOList]
        } finally {
            sql?.close()
        }
    }


    List getCaseOfSeries(Long caseSeriesId, Integer offset, Integer max, String sort, String direction, String searchString, String caseSeriesOwner) {
        Sql sql = new Sql(getReportConnection())
        List<CaseDTO> caseDTOList = []
        Integer count = 0
        Integer totalCount = 0
        try {
            int index = 0
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { stmt ->
                stmt.fetchSize = (max > 0 ? max : 300)
            }
            sql.call("{?= call PKG_QUERY_HANDLER.f_get_cases_details(?,?,?,?,?,?,?)}",
                    [Sql.resultSet(OracleTypes.CURSOR), searchString, CaseDTO.getSortKey(sort), direction, offset, max, caseSeriesId, caseSeriesOwner]) { cursorResults ->
                cursorResults.eachRow { result ->
                    caseDTOList.add(new CaseDTO(result))
                    if (index == 0) {
                        count = (result.cnt?.toInteger()) ?: 0
                        totalCount = (result.total_count?.toInteger()) ?: 0
                    }
                    index++
                }
            }
            return [count, totalCount, caseDTOList]
        } finally {
            sql?.close()
        }
    }

    List<CaseDTO> getOpenCaseOfSeries(Long caseSeriesId, String caseSeriesOwner) {
        Sql sql = new Sql(getReportConnection())
        List<CaseDTO> caseDTOList = []
        try {
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { stmt ->
                stmt.fetchSize = grailsApplication.config.jdbcProperties.fetch_size ?: 50
            }
            sql.call("{?= call PKG_QUERY_HANDLER.f_fetch_open_cases(?,?)}",
                    [Sql.resultSet(OracleTypes.CURSOR), caseSeriesId, caseSeriesOwner]) { cursorResults ->
                cursorResults.eachRow { result ->
                    caseDTOList.add(new CaseDTO(result))
                }
            }
            return caseDTOList
        } finally {
            sql?.close()
        }
    }

    List<CaseDTO> getRemovedCaseOfSeries(Long caseSeriesId, String caseSeriesOwner) {
        Sql sql = new Sql(getReportConnection())
        List<CaseDTO> caseDTOList = []
        try {
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { stmt ->
                stmt.fetchSize = grailsApplication.config.jdbcProperties.fetch_size ?: 50
            }
            sql.call("{?= call PKG_QUERY_HANDLER.f_fetch_removed_cases(?,?)}", [Sql.resultSet(OracleTypes.CURSOR), caseSeriesId, caseSeriesOwner]) { cursorResults ->
                cursorResults.eachRow { result ->
                    caseDTOList.add(new CaseDTO(result))
                }
            }
            return caseDTOList
        } finally {
            sql?.close()
        }
    }

    List<Long> getSubmissionsByCaseNumber(String caseNumber) {
        Sql sql = new Sql(getReportConnection())
        List<Long> resultList = []
        try {
            sql.call("{?= call PKG_SUBMISSION_TRACKING.f_get_agg_report(?)}",
                    [Sql.resultSet(OracleTypes.CURSOR), caseNumber]) { cursorResults ->
                MiscUtil.resultSetToList(cursorResults).each { result ->
                    resultList.add(result.AGG_REPORT_ID as Long)
                }
            }
            return resultList
        } finally {
            sql?.close()
        }
    }

    List getSubmittedCases(ReportSubmission reportSubmission, Integer offset, Integer max, String sort, String direction, String searchString) {
        Sql sql = new Sql(getReportConnection())
        List<SubmittedCaseDTO> caseDTOList = []
        Integer count = 0
        Integer totalCount = 0
        try {
            int index = 0
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { stmt ->
                stmt.fetchSize = grailsApplication.config.jdbcProperties.fetch_size ?: 50
            }
            sql.call("{?= call PKG_SUBMISSION_TRACKING.f_get_init_fup_submission(?,?,?,?,?,?,?)}",
                    [Sql.resultSet(OracleTypes.CURSOR), searchString, SubmittedCaseDTO.getSortKey(sort), direction, offset, max, reportSubmission.executedReportConfigurationId, reportSubmission.reportingDestination]) { cursorResults ->
                MiscUtil.resultSetToList(cursorResults).each { result ->
                    caseDTOList.add(new SubmittedCaseDTO(result))
                    if (index == 0) {
                        count = (result.CNT?.toInteger()) ?: 0
                        totalCount = (result.TOTAL_COUNT?.toInteger()) ?: 0
                    }
                    index++
                }
            }
            return [count, totalCount, caseDTOList]
        } finally {
            sql?.close()
        }
    }

    @Transactional
    void generateDraftReport(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATING_DRAFT
        // Updated executed etl date for draft report, last run date already updating
        executedReportConfiguration.lastRunDate = new Date()
        executedReportConfiguration.executedETLDate = etlJobService?.lastSuccessfulEtlStartTime() as Date
        executedReportConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(executedReportConfiguration)
        CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
        generateReport(executedReportConfiguration, System.currentTimeMillis(), true)
        executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATED_DRAFT
        CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
        executionStatus.aggregateReportStatus = ReportExecutionStatusEnum.GENERATED_DRAFT
        executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
        Boolean isInDraftMode = (executedReportConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedReportConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
        deleteReportsCachedFilesIfAny(executedReportConfiguration, isInDraftMode)
    }

    @Transactional
    void generateFinalDraftReport(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATING_FINAL_DRAFT
        // Update etl date for final report -> finalExecutedETLDate and finalLastRunDate
        executedReportConfiguration.finalExecutedEtlDate = etlJobService?.lastSuccessfulEtlStartTime() as Date
        executedReportConfiguration.finalLastRunDate = new Date()
        executedReportConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(executedReportConfiguration)
        CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
        generateReport(executedReportConfiguration, System.currentTimeMillis(), true)
        executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT
        CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
        executionStatus.aggregateReportStatus = ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT
        executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
        Boolean isInDraftMode = (executedReportConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedReportConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
        deleteReportsCachedFilesIfAny(executedReportConfiguration, isInDraftMode)
    }

    @Transactional
    void generateAdhocReport(ExecutedReportConfiguration executedReportConfiguration, ExecutionStatus executionStatus) {
        executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATING
        executedReportConfiguration.lastRunDate = new Date()
        executedReportConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(executedReportConfiguration)
        CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
        generateReport(executedReportConfiguration, System.currentTimeMillis(), false)
        executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
        deleteReportsCachedFilesIfAny(executedReportConfiguration, false)
    }

    @Transactional
    ResponseDTO<Map> generateFinalReport(ExecutedTemplateQuery executedTemplateQueryInstance, ExecutionStatus executionStatus, Boolean needToUseAlreadyGeneratedCases = false) {

        ResponseDTO<Map> responseDTO = new ResponseDTO<Map>(data: [:])

        Long startTime = System.currentTimeMillis()
        Long templateId = 0L
        Long queryId = 0L
        String sectionName = ""
        Sql sql

        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQueryInstance.executedConfiguration
        ReportExecutionStatusEnum oldStatus = executedReportConfiguration.status

        try {
            if(executedTemplateQueryInstance.onDemandSectionParams){
                executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATING_ON_DEMAND_SECTION
            }else {
                executedReportConfiguration.status = ReportExecutionStatusEnum.GENERATING_NEW_SECTION
            }
            CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
            Locale locale = executedReportConfiguration.locale
            sql = new Sql(getReportConnection())
            sqlGenerationService.setCurrentSqlInfo(sql, locale)
            boolean isJDSUR = false
            if (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration && executedReportConfiguration.periodicReportType == PeriodicReportTypeEnum.PADER) {
                sql.call("{call pkg_pader_line_listing.p_init}")
            }

            if (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration && executedReportConfiguration.periodicReportType == PeriodicReportTypeEnum.JDSUR) {
                isJDSUR = true
            }

            templateId = executedTemplateQueryInstance.executedTemplateId
            queryId = executedTemplateQueryInstance.executedQueryId ?: null
            sectionName = executedTemplateQueryInstance.title?: executedTemplateQueryInstance.executedConfiguration.reportName

            if(executedTemplateQueryInstance?.displayMedDraVersionNumber) {
                String meddraVersion = null
                if (locale.equals(Locale.JAPANESE)) {
                    meddraVersion = sql.firstRow("select MEDDRA_VERSION FROM VW_PVR_MEDDRA_VERSION_J")?.MEDDRA_VERSION
                } else {
                    meddraVersion = sql.firstRow("select MEDDRA_VERSION FROM VW_PVR_MEDDRA_VERSION")?.MEDDRA_VERSION
                }
                executedTemplateQueryInstance.reportResult.medDraVersion = meddraVersion
            }

            if (executedTemplateQueryInstance.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                generateReportResult(sql, executedTemplateQueryInstance.reportResult, locale, needToUseAlreadyGeneratedCases)
            } else {
                generateReportResultCSV(sql, executedTemplateQueryInstance.reportResult, locale, needToUseAlreadyGeneratedCases, false)
            }

            if (isJDSUR) {
                String clinicalCompoundNumber = null
                sql.call("{? = call f_fetch_japan_ccn()}", [Sql.VARCHAR]) { String sqlValue->
                    clinicalCompoundNumber = sqlValue
                }
                executedTemplateQueryInstance.reportResult.clinicalCompoundNumber = clinicalCompoundNumber
            }
            if (executedTemplateQueryInstance.reportResult.executionStatus != ReportExecutionStatusEnum.ERROR) {
                executedTemplateQueryInstance.reportResult.executionStatus = ReportExecutionStatusEnum.COMPLETED
            }
            CRUDService.saveWithoutAuditLog(executedTemplateQueryInstance.reportResult)
            log.info("Execution of TemplateQuery took ${executedTemplateQueryInstance.reportResult.totalTime}ms for [C:${executedReportConfiguration.id}, RR: ${executedTemplateQueryInstance.reportResult.id}, T:${templateId}, Q:${queryId}]")
            if (!executedReportConfiguration.hasGeneratedCasesData) {
                executedReportConfiguration.status = ReportExecutionStatusEnum.COMPLETED
                //TODO need to add SQL logic to delete all generated cases
            }
            if (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration && executedReportConfiguration.periodicReportType == PeriodicReportTypeEnum.PADER) {
                sql.call("{call pkg_pader_line_listing.p_cleanup}")
            }
            executedReportConfiguration.totalExecutionTime = executedReportConfiguration.totalExecutionTime + (System.currentTimeMillis() - startTime)
            executedReportConfiguration.status = oldStatus
            CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
            executionStatus.aggregateReportStatus = ReportExecutionStatusEnum.GENERATED_NEW_SECTION
            CRUDService.instantSaveWithoutAuditLog(executionStatus)
        } catch (Exception e) {
            log.error("Unable to finish running executedConfiguration.id=${executedReportConfiguration.id}", e)

            responseDTO.setFailureResponse(e)
            transactionStatus.setRollbackOnly() // you get transactionStatus at runtime

            if (e instanceof ExecutionStatusException) {
                responseDTO.setData(e.properties)
            } else {
                String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
                StringWriter sw = new StringWriter()
                e.printStackTrace(new PrintWriter(sw))
                String exceptionAsString = sw.toString()
                if (!message) {
                    message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
                }
                responseDTO.data = [templateId: templateId, queryId: queryId, sectionName: sectionName, errorMessage: message, errorCause: exceptionAsString]
            }
            return responseDTO
        } finally {
            sql?.close()
        }
        executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
        Boolean isInDraftMode = (executedReportConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedReportConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
        deleteReportsCachedFilesIfAny(executedReportConfiguration, isInDraftMode)
        return responseDTO
    }

    Set<String> saveCaseSeriesInDB(Set<String> caseNumberAndVersion, ExecutedCaseSeries executedCaseSeries, String justification = null) {
        String addCaseDelimiter = grailsApplication.config.caseSeries.bulk.addCase.delimiter
        Sql sql
        Set<String> warnings = []
        try {
            sql = new Sql(getReportConnection())
            Long startTime = System.currentTimeMillis()
            sql.withBatch(1000) { stmt ->
                caseNumberAndVersion.each { casenum_version ->

                    //As discussed with Garima passed Version number as -1 if uploaded values has case number only.
                    List temp = casenum_version?.contains(addCaseDelimiter) ? casenum_version.split(addCaseDelimiter) : [casenum_version, -1]

                    // In GTT_FILTER_KEY_VALUES(CODE,TEXT), CODE = "Version Number" and TEXT = "Case Number"
                    stmt.addBatch("insert into GTT_FILTER_KEY_VALUES(CODE,TEXT) values('${temp[1] ?: -1}','${temp[0]}')")
                }
            }

            String endDate = executedCaseSeries?.executedCaseSeriesDateRangeInformation?.reportStartAndEndDate[1]?.format(SqlGenerationService.DATE_FMT)
            String dateRangeType = executedCaseSeries.dateRangeType?.name
            String evaluateDateAs = executedCaseSeries.evaluateDateAs
            String versionAsOfDate = executedCaseSeries.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ? executedCaseSeries?.asOfVersionDate?.format(SqlGenerationService.DATE_FMT) : null
            Integer includeLockedVersion = executedCaseSeries?.includeLockedVersion ? 1 : 0
            String caseSeriesOwner = executedCaseSeries.caseSeriesOwner
            sql.call("{?= call PKG_QUERY_HANDLER.f_save_cstm_case_series(?,?,?,?,?,?,?,?,?,?,?,?)}",
                    [Sql.NUMERIC, executedCaseSeries.seriesName, executedCaseSeries.version, executedCaseSeries.owner.id, executedCaseSeries.id, justification, justification ? 0 : 1, endDate, dateRangeType, evaluateDateAs, versionAsOfDate, includeLockedVersion, caseSeriesOwner]) { result ->
                if (result != 0) {
                    warnings = sql.rows("SELECT * from GTT_REPORT_INPUT_PARAMS").collect { it.PARAM_KEY }
                }
            }
            Long endTime = System.currentTimeMillis()
            log.info("Time taken to save the Case Series \'${executedCaseSeries.seriesName}\' in DB : " + ((endTime - startTime) / 1000))
        } catch (Exception e) {
            log.error("Error while saving case series : ${executedCaseSeries.seriesName}", e)
        } finally {
            sql?.close()
        }
        return warnings
    }

    void deleteReportsCachedFilesIfAny(ExecutedReportConfiguration executedReportConfiguration, boolean isInDraftMode) {
        executedReportConfiguration.executedTemplateQueries*.reportResult.each {
            dynamicReportService.deleteAllReportsCachedFile(it, isInDraftMode)
        }
        dynamicReportService.deleteAllExecutedCachedFile(executedReportConfiguration, isInDraftMode)
    }

    void killSqlProcess(String sqlInfo) {
        Sql sql = new Sql(getReportConnection())
        try {
            sql.call('{call PKG_KILL_SESSION.get_sid_serial_inst(?)}', [sqlInfo])
        } catch (Exception ex) {
            log.error("KillSql Process has some issue", ex)
        } finally {
            sql?.close()
        }
    }

    void logToSubmissionHistory(Sql sql, BaseConfiguration configuration) {
        if (configuration instanceof ExecutedPeriodicReportConfiguration && configuration.finalReportGettingGenerated()) {
            String initialParamsInsert = sqlGenerationService.initializeCaseSeriesGtts(ExecutorDTO.create(configuration))
            if (initialParamsInsert) {
                sql.execute(initialParamsInsert)
            }
            sql.query("{call PKG_SUBMISSION_TRACKING.P_POP_SUBMISSION_REPORT_HIST(?,?,?,?,?,?,?)}", [configuration.id, configuration.dateCreated ? new java.sql.Date(configuration.dateCreated.time) : null, 0, configuration.dueInDays, configuration.periodicReportType.name(), configuration.ownerId, configuration.primaryReportingDestination]) { rs ->
            }
        }
    }

    void logCasesToSubmissionHistory(Sql sql, BaseTemplateQuery templateQuery) {
        BaseConfiguration configuration = templateQuery.usedConfiguration
        if (configuration instanceof ExecutedPeriodicReportConfiguration && configuration.finalReportGettingGenerated() && !((ExecutedTemplateQuery) templateQuery).draftOnly) {
            sql.query("{call PKG_SUBMISSION_TRACKING.P_POP_PVR_SUBMISSION_CASE_LIST(?,?,?,?)}", [configuration?.id, templateQuery.usedTemplate?.id, templateQuery.usedQuery?.id, templateQuery.queryLevel?.name()]) { rs ->
            }
        }
    }

    void logCasesToTracking(Sql sql, BaseTemplateQuery templateQuery) {
        BaseConfiguration configuration = templateQuery.usedConfiguration
        if (configuration instanceof ExecutedIcsrProfileConfiguration || configuration instanceof ExecutedIcsrReportConfiguration) {
            sql.query("{call PKG_E2B_PROCESSING.p_e2b_initiate_e2B_tracking(:E2B_STATUS,:CASE_ID,:CASE_VERSION,:GENERATION_DATE)}", [E2B_STATUS: IcsrCaseStateEnum.GENERATED.toString(), CASE_ID: null, CASE_VERSION: null, GENERATION_DATE: new Timestamp(new Date().time)]) { rs ->
            }
        }
    }

    void logDestinationToSubmissionHistory(ReportSubmission reportSubmission) throws Exception {
        Sql sql = new Sql(getReportConnection())
        try {
            sql.query("{call PKG_SUBMISSION_TRACKING.P_POP_SUBMISSION_DEST_HIST(?,?,?,?,?,?)}", [reportSubmission.executedReportConfiguration.id, reportSubmission.reportingDestination, reportSubmission.submissionDate ? new java.sql.Date(reportSubmission.submissionDate.time) : null, reportSubmission.submissionRequired, reportSubmission.comment, false]) { rs ->
            }
        } catch (Exception ex) {
            log.error("log Destination To Submission History",ex)
            String userMessage = ViewHelper.getMessage("app.periodic.report.submission.error.messsage")
            throw new Exception(userMessage, ex)
        } finally {
            sql?.close()
        }
    }

    void logJPSRDestinationToSubmissionHistory(ReportSubmission reportSubmission) {
        Sql sql = new Sql(getReportConnection())
        try {
            sql.query("{call PKG_SUBMISSION_TRACKING.P_POP_JPSR_SUBMISSION_DATA(?)}", [reportSubmission.executedReportConfiguration.id]) { rs ->
            }
        } catch (Exception ex) {
            log.error("log JPSR Destination To Submission History",ex)
        } finally {
            sql?.close()
        }
    }

    void removeCaseFromTracking(String profileName, String caseNumber, Long versionNumber, String state, Long tenantId, Long processedRptId, Long exIcsrTemplateQueryId, String recipient, Date dueDate, Long justificationId, String justificationEn, String justificationJa) {
        Sql sql = new Sql(getReportConnection())
        try {
            Long caseId
            String insertStatement = ""
            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_FROM_STATE', '${state}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_TO_STATE','DELETE');";
            insertStatement += "END;"
            sql.execute(insertStatement)
            sql.query("{call PKG_E2B_PROCESSING.P_UPDATE_E2B_STATUS(:PROFILE_NAME, :QUERY_ID, :CASE_NUMBER, :VERSION_NUMBER, :PROCESSED_REPORT_ID , :STATUS, :SUBMISSION_DATE, :IS_LATE, :REPORTING_DESTINATION, :DUE_DATE, :COMMENT, :COMMENT_J, :JUSTIFICATION_ID, :ERROR, :SUBMISSION_DOCUMENT, :TRANSMISSION_DATE, :TRANSMITTED_DATE, :USER_NAME, :MODIFIED_DATE, :ACK_FILE_NAME, :ACK_DATE, :SUBMISION_FILENAME, :LOCAL_SUBMISSION_DATE, :TIME_ZONE_ID, :DATE_TRANSMISSION_ATTACH, :DATE_TRANSMITTED_ATTACH, :DATE_ACK_RECIEVED_ATTACH, :ATTACH_ACK_FILE_NAME, :PMDA_NUMBER, :AUTH_ID, :VOIDED_FLAG)}", [PROFILE_NAME: profileName, QUERY_ID: ExecutedIcsrTemplateQuery.read(exIcsrTemplateQueryId)?.usedQuery?.originalQueryId, CASE_NUMBER: caseNumber, VERSION_NUMBER: versionNumber, PROCESSED_REPORT_ID: processedRptId, STATUS: 'DELETE', SUBMISSION_DATE: null, IS_LATE: 0, REPORTING_DESTINATION: recipient, DUE_DATE: dueDate ? new Timestamp(dueDate.time) : null, COMMENT: justificationEn, COMMENT_J: justificationJa, JUSTIFICATION_ID: justificationId, ERROR: null, SUBMISSION_DOCUMENT: new byte[0], TRANSMISSION_DATE: null, TRANSMITTED_DATE: null, USER_NAME: null, MODIFIED_DATE: new Timestamp(new Date().time), ACK_FILE_NAME: null, ACK_DATE: null, SUBMISION_FILENAME: null, LOCAL_SUBMISSION_DATE: null, TIME_ZONE_ID: null, DATE_TRANSMISSION_ATTACH: null, DATE_TRANSMITTED_ATTACH: null, DATE_ACK_RECIEVED_ATTACH: null, ATTACH_ACK_FILE_NAME : null, PMDA_NUMBER : null, AUTH_ID : null, VOIDED_FLAG : 1]) { rs ->}
            String query = "SELECT DISTINCT CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE CASE_NUM = :caseNumber and VERSION_NUM =:versionNumber AND TENANT_ID =:tenantId"
            def row = sql.firstRow(query, [caseNumber: caseNumber, versionNumber: versionNumber, tenantId: tenantId])
            if (row) {
                caseId = row?.CASE_ID
            }
            icsrScheduleService.calculateDueDateForManual(caseId, versionNumber, tenantId, new Date(), Constants.DUE_DATE_TYPE_DELETE)
        } catch (Exception ex) {
            log.error("remove Case From Tracking",ex)
            throw ex
        } finally {
            sql?.close()
        }
    }

    def generateCaseSeriesByCaseCommandList(ExecutedCaseSeries executedCaseSeries, List<Tuple2<String, String>> caseIds, int count = -1, boolean refreshCases = false, boolean isDrillDownToCaseList = false) {
        Sql sql = new Sql(getReportConnection())
        String caseSeriesName = executedCaseSeries.seriesName
        long caseSeriesId = executedCaseSeries.id
        int numOfExecutions = 0
        int isCumulative = 0
        long ownerId = executedCaseSeries.ownerId
        String reportName = executedCaseSeries.reportName
        String caseSeriesOwner = executedCaseSeries.caseSeriesOwner
        def reportId = null
        int hasQuery = 0

        try {
            sql.call("{call PKG_QUERY_HANDLER.p_save_case_list(?,?,?,?,?,?,?,?,?,?,?)}", [
                    caseSeriesName,
                    caseSeriesId,
                    numOfExecutions,
                    isCumulative,
                    ownerId,
                    reportName,
                    reportId,
                    null,
                    hasQuery,
                    refreshCases ? 1 : 0,
                    caseSeriesOwner
            ])
            sql.call("{?= call PKG_QUERY_HANDLER.p_modify_case_with_version(?,?,?,?,?,?,?,?,?,?)}", [
                    Sql.NUMERIC,
                    isDrillDownToCaseList ? 0 : 1,
                    caseIds*.getFirst().join(','),
                    caseIds*.getSecond().join(','),
                    ownerId,
                    caseSeriesId,
                    null,
                    1,
                    0,
                    count,
                    caseSeriesOwner
            ]) { result ->
                if (result < 0) {
                    throw new Exception("Database server side error")
                }
            }
        } finally {
            sql?.close()
        }
    }

    void killReportExecution() {
        List<Long> ids = executorThreadInfoService.currentlyRunning.keySet().toList()
        if (ids && ReportExecutionKillRequest.countByKillStatusAndExecutionStatusIdInList(KillStatusEnum.NEW, ids)) {
            ReportExecutionKillRequest.findAllByKillStatusAndExecutionStatusIdInList(KillStatusEnum.NEW, ids).each {
                it.killStatus = KillStatusEnum.IN_PROGRESS
                log.info "******* KILL REQUEST ${it.id} IN PROGRESS *******"
                it.save(flush: true)
                killConfigurationExecution(it.executionStatusId)
                it.killStatus = KillStatusEnum.KILLED
                it.save(flush: true)
                ExecutionStatus executionStatus = ExecutionStatus.get(it?.executionStatusId)
                Object executingObject = executionStatus ? executionStatus?.getEntityClass()?.get(executionStatus.entityId) : null
                if (executionStatus && executingObject) {
                    executingObject.setNextRunDate(null)
                    if (!(executingObject instanceof IcsrProfileConfiguration)) {
                        executingObject.setIsEnabled(false)
                    }
                    if (executingObject?.executing) executingObject.executing = false
                    CRUDService.saveOrUpdate(executingObject)
                }
                executorThreadInfoService.removeFromTotalCurrentlyRunningIds(executionStatus.id)
                log.info "******* KILL REQUEST ${it.id} SUCCESSFUL *******"
                executionStatus?.sharedWith?.each {
                    notificationService.addNotification(executionStatus.entityId, executionStatus, it)
                }
            }
        }
    }

    void completedExecutionCleanup() {
        List<Long> ids = executorThreadInfoService.totalCurrentlyRunningIds
        if (ids) {
            ids.collate(499).each { List<Long> list ->
                ExecutionStatus.getCompletedIdsFromList(list).list().flatten().each {
                    executorThreadInfoService.removeFromTotalCurrentlyRunningIds(it)
                }
            }
        }
    }

    Integer checkIfTransmitted(Long tenantId, CaseSubmissionCO caseSubmissionCO) {
        IcsrCaseSubmission.fetchIfTransmitted(tenantId, caseSubmissionCO).count()
    }

    void logIcsrCaseToSubmissionHistory(Long tenantId, CaseSubmissionCO caseSubmissionCO) {
        ExecutedIcsrTemplateQuery executedTemplateQuery = ExecutedIcsrTemplateQuery.read(caseSubmissionCO?.queryId)
        if (!caseSubmissionCO?.submissionDate || !executedTemplateQuery) {
            log.debug("Not valid data for submission")
            throw new CaseSubmissionException(caseSubmissionCO, 'icsr.case.submit.invaild.data.error', 'Not valid data for submission')
        }
        Integer ifTransmitted = null
        IcsrCaseSubmission.'pva'.withNewSession{
            ifTransmitted = checkIfTransmitted(tenantId, caseSubmissionCO)
        }
        if (caseSubmissionCO.icsrCaseState == IcsrCaseStateEnum.SUBMITTED && grailsApplication.config.getProperty('icsr.case.workflow.transmitted.check', Boolean) && !ifTransmitted && !(executedTemplateQuery.distributionChannelName == DistributionChannelEnum.PAPER_MAIL)) {
            log.debug("Case ${caseSubmissionCO.caseNumber}-${caseSubmissionCO.versionNumber} has not been transmitted yet")
            throw new CaseSubmissionException(caseSubmissionCO, 'icsr.case.submit.not.transmitted.error', 'Case has not been transmitted yet')
        }
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseSubmissionCO.caseNumber, versionNumber: caseSubmissionCO.versionNumber)
        caseStateUpdateDTO.with {
            status = caseSubmissionCO.icsrCaseState?.toString()
            dueDate = caseSubmissionCO.dueDate
            submissionDate = caseSubmissionCO.submissionDate
            comment = caseSubmissionCO.comment
            commentJ = caseSubmissionCO.commentJ
            justificationId = caseSubmissionCO.justificationId
            reportingDestination = caseSubmissionCO.reportingDestinations
            attachment = caseSubmissionCO?.submissionDocument
            attachmentFilename = caseSubmissionCO?.submissionFilename
            lateReasons = caseStateUpdateDTO?.lateReasons
            lateFlag = false
            if (caseSubmissionCO?.late?.value() == 'Late') {
                lateFlag = true
            }
            processedReportId = caseSubmissionCO?.processedReportId
            updatedBy = caseSubmissionCO?.updatedBy
            localSubmissionDate = caseSubmissionCO?.localSubmissionDate
            timeZoneId = caseSubmissionCO?.timeZoneId
        }
        try {
            changeIcsrCaseStatus(caseStateUpdateDTO, caseSubmissionCO.currentState, caseSubmissionCO.icsrCaseState.toString())
        } catch (Exception ex) {
            throw ex
        }
    }

    void markCaseTransmitting(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, Date fileDate, String comments, String username = null) {
        log.debug("Request Received to mark case as TRANSMITTING")
        IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        Long prId = newIcsrCaseTrackingInstance?.processedReportId
        String currentState = newIcsrCaseTrackingInstance?.e2BStatus
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber)
        caseStateUpdateDTO.with {
            status = IcsrCaseStateEnum.TRANSMITTING.toString()
            comment = comments
            transmissionDate = fileDate
            processedReportId = prId
            updatedBy = username
        }
        changeIcsrCaseStatus(caseStateUpdateDTO, currentState, IcsrCaseStateEnum.TRANSMITTING.toString())
    }

    void markCaseTransmissionError(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, String comments) {
        log.debug("Request Received to mark case as TRANSMISSION ERROR")
        IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        Long prId = newIcsrCaseTrackingInstance?.processedReportId
        String currentState = newIcsrCaseTrackingInstance?.e2BStatus
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber)
        caseStateUpdateDTO.with {
            status = IcsrCaseStateEnum.TRANSMISSION_ERROR.toString()
            comment = comments
            transmissionDate = new Date()
            processedReportId = prId
        }
        changeIcsrCaseStatus(caseStateUpdateDTO, currentState, IcsrCaseStateEnum.TRANSMISSION_ERROR.toString())
    }

    void markCaseTransmitted(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, String comments, Date transmitDate, String e2bStatus = null, String username = null) {
        log.debug("Request Received to mark case as TRANSMITTED")
        IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        Long prId = newIcsrCaseTrackingInstance?.processedReportId
        String currentState = newIcsrCaseTrackingInstance?.e2BStatus
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber)
        caseStateUpdateDTO.with {
            status = e2bStatus ?: IcsrCaseStateEnum.TRANSMITTED.toString()
            comment = comments
            transmittedDate = transmitDate
            processedReportId = prId
            updatedBy = username
        }
        changeIcsrCaseStatus(caseStateUpdateDTO, currentState, e2bStatus ?: IcsrCaseStateEnum.TRANSMITTED.toString())
    }

    void markCaseTransmittingAttachment(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, Date fileDate, String comments) {
        log.debug("Request Received to mark case as TRANSMITTING ATTACHMENT")
        IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        Long prId = newIcsrCaseTrackingInstance?.processedReportId
        String currentState = newIcsrCaseTrackingInstance?.e2BStatus
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber)
        caseStateUpdateDTO.with {
            status = IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString()
            comment = comments
            dateTransmissionAttach = fileDate
            processedReportId = prId
        }
        changeIcsrCaseStatus(caseStateUpdateDTO, currentState, IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString())
    }

    void markCaseTransmissionAttachmentError(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, String comments) {
        log.debug("Request Received to mark case as TRANSMISSION ATTACHMENT ERROR")
        IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        Long prId = newIcsrCaseTrackingInstance?.processedReportId
        String currentState = newIcsrCaseTrackingInstance?.e2BStatus
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber)
        caseStateUpdateDTO.with {
            status = IcsrCaseStateEnum.TRANSMISSION_ERROR.toString()
            comment = comments
            dateTransmissionAttach = new Date()
            processedReportId = prId
        }
        changeIcsrCaseStatus(caseStateUpdateDTO, currentState, IcsrCaseStateEnum.TRANSMISSION_ERROR.toString())
    }

    void markCaseAttachmentAccepted(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, String comments, Date transmitDate, String e2bStatus = null) {
        log.debug("Request Received to mark case as Commit Received")
        IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        Long prId = newIcsrCaseTrackingInstance?.processedReportId
        String currentState = newIcsrCaseTrackingInstance?.e2BStatus
        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber)
        caseStateUpdateDTO.with {
            status = e2bStatus ?: IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT.toString()
            comment = comments
            dateTransmittedAttach = transmitDate
            processedReportId = prId
        }
        changeIcsrCaseStatus(caseStateUpdateDTO, currentState, e2bStatus ?: IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT.toString())
    }

    void changeIcsrCaseStatus(CaseStateUpdateDTO dto, String currentState, String icsrCaseState, String pmdaNumber = null, Long authId = null) {
        if (!dto.validate()) {
            log.error("Not valid data for case status update due to ${dto.errors.allErrors*.code.join(',')}")
            throw new GrailsRuntimeException("No valid data for case status update due to ${dto.errors.allErrors*.code.join(',')}")
        }
        if (!dto.updatedBy) {
            dto.updatedBy = userService.user?.username
        }
        User owner = User.findByUsername(dto.updatedBy)
        if(owner?.fullName)
            dto.updatedBy = owner.fullName

        Sql sql = new Sql(getReportConnection())
        try {
            String insertStatement = ""
            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_FROM_STATE', '${currentState}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_TO_STATE','${icsrCaseState}');";
            insertStatement += "END;"
            sql.execute(insertStatement)
            if (dto.lateReasons) {
                String queryString = "begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_filter_key_values''); end;'; "
                dto.lateReasons.each { LateReasonDTO lateReasonDTO ->
                    Boolean isNotDeleted = !(Boolean.valueOf(lateReasonDTO?.deleted))
                    if (isNotDeleted) {
                        queryString += "INSERT INTO GTT_FILTER_KEY_VALUES (CODE,TEXT) values ('${lateReasonDTO?.reason?.value()}', '${lateReasonDTO?.responsibleParty?.value()}');\n"
                    }
                }
                queryString += " END;"
                sql.execute(queryString)
            }
            sql.query("{call PKG_E2B_PROCESSING.P_UPDATE_E2B_STATUS(:PROFILE_NAME, :QUERY_ID, :CASE_NUMBER, :VERSION_NUMBER, :PROCESSED_REPORT_ID , :STATUS, :SUBMISSION_DATE, :IS_LATE, :REPORTING_DESTINATION, :DUE_DATE, :COMMENT, :COMMENT_J, :JUSTIFICATION_ID, :ERROR, :SUBMISSION_DOCUMENT, :TRANSMISSION_DATE, :TRANSMITTED_DATE, :USER_NAME, :MODIFIED_DATE, :ACK_FILE_NAME, :ACK_DATE, :SUBMISION_FILENAME, :LOCAL_SUBMISSION_DATE, :TIME_ZONE_ID, :DATE_TRANSMISSION_ATTACH, :DATE_TRANSMITTED_ATTACH, :DATE_ACK_RECIEVED_ATTACH, :ATTACH_ACK_FILE_NAME, :PMDA_NUMBER, :AUTH_ID)}", [PROFILE_NAME: dto.profileName, QUERY_ID: dto.executedIcsrTemplateQuery?.usedQuery?.originalQueryId, CASE_NUMBER: dto.caseNumber, VERSION_NUMBER: dto.versionNumber, PROCESSED_REPORT_ID: dto.processedReportId, STATUS: dto.status, SUBMISSION_DATE: dto.submissionDateTime, IS_LATE: (dto.lateFlag != null ? (dto.lateFlag ? 1 : 0) : null), REPORTING_DESTINATION: dto.reportingDestination, DUE_DATE: dto.dueDateTime, COMMENT: dto.comment, COMMENT_J: dto.commentJ, JUSTIFICATION_ID: dto.justificationId, ERROR: dto.errorText, SUBMISSION_DOCUMENT: dto.submissionDocument, TRANSMISSION_DATE: dto.transmissionDateTime, TRANSMITTED_DATE: dto.transmittedDateTime, USER_NAME: dto.updatedBy, MODIFIED_DATE: new Timestamp(new Date().time), ACK_FILE_NAME: dto.ackFileName, ACK_DATE: dto.ackReceiveDateTime, SUBMISION_FILENAME: dto.attachmentFilename, LOCAL_SUBMISSION_DATE: dto.localSubmissionDate, TIME_ZONE_ID: dto.timeZoneId, DATE_TRANSMISSION_ATTACH: dto.dateTimeTransmissionAttach, DATE_TRANSMITTED_ATTACH: dto.dateTimeTransmittedAttach, DATE_ACK_RECIEVED_ATTACH: dto.dateTimeAckRecievedAttach, ATTACH_ACK_FILE_NAME : dto.attachmentAckFileName, PMDA_NUMBER : pmdaNumber, AUTH_ID : authId]) { rs ->
            }
            if (dto.status == IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED.toString() || dto.status == IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.toString()) {
                Long tenantId = Tenants.currentId() as Long
                def row = sql.firstRow("SELECT DISTINCT CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE CASE_NUM = :caseNumber and VERSION_NUM = :versionNumber and tenant_id = :tenant_id", [caseNumber: dto.caseNumber, versionNumber: dto.versionNumber, tenant_id: tenantId])
                Long caseId = row?.CASE_ID
                if (!caseId) {
                    throw new InvalidCaseInfoException("Invalid case number and version data ($caseNumber : $versionNumber)")
                }
                IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.findByReportName(dto.profileName)
                log.info("Submission Not Required marked for caseId ${caseId}, Deleting Entry in IcsrCaseLocalCpData...")
                icsrScheduleService.deleteLocalCpProfileEntry(caseId, dto.versionNumber, tenantId, profileConfiguration, false)
                icsrScheduleService.calculateDueDateForManual(caseId, dto.versionNumber, tenantId, new Date(), Constants.DUE_DATE_TYPE_SRN)
            }
        }catch(Exception ex) {
            log.error("change Icsr Case Status ",ex)
            throw ex
        }finally {
            sql?.close()
        }
    }

    void logIcsrReportCasesToTracking(ReportResult sourceReportResult, ExecutedTemplateQuery targetExTemplateQuery, Map<String, Integer> caseNumbersWithVersion, Integer dueInDays, Boolean isExpedited) {
        Sql sql = new Sql(getReportConnection())
        try {
            StringBuilder insertSql = new StringBuilder("Begin ")
            ExecutedTemplateQuery sourceExecutedTemplateQuery = sourceReportResult.executedTemplateQuery
            ExecutedReportConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) targetExTemplateQuery.executedConfiguration
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('REPORT_NAME',:REPORT_NAME)", [REPORT_NAME: executedConfiguration.reportName])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_ORG_NAME',:E2B_RECEIVER_ORG_NAME)", [E2B_RECEIVER_ORG_NAME: executedConfiguration.recipientOrganizationName])
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_DUE_IN_DAYS','${dueInDays}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_EXPEDITED','${isExpedited ? 1 : 0}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('OBJECT_ID','${executedConfiguration.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('SECTION_ID','${targetExTemplateQuery.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_MSG_TYPE_ID','${targetExTemplateQuery.icsrMsgType}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_MSG_TYPE_DESC','${targetExTemplateQuery.icsrMsgTypeName}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('SOURCE_SECTION_ID','${sourceExecutedTemplateQuery.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('TEMPLATE_ID','${targetExTemplateQuery.usedTemplate?.originalTemplateId}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('IS_ICSR_REPORT','1');\n")
            caseNumbersWithVersion.keySet().collate(999).each {
                sql.rows("SELECT DISTINCT CASE_ID,SRC_CASE_NUM FROM V_C_IDENTIFICATION WHERE SRC_CASE_NUM in ('${it.join(',')}')").each {
                    insertSql.append("INSERT INTO gtt_query_case_list (tenant_id,case_id,version_num) VALUES(${Tenants.currentId() as Long},'${it.CASE_ID}',${caseNumbersWithVersion.get(it.SRC_CASE_NUM)});\n")
                }
            }
            insertSql.append('END;')
            sql.execute(insertSql.toString().trim())
            log.debug("Insert for transferring cases: ${insertSql.toString()}")
            sql.call("{call PKG_E2B_PROCESSING.p_move_adhoc_icsr_screen}")
        }catch (Exception ex) {
            log.error("log Icsr Report Cases To Tracking",ex)
        } finally {
            sql?.close()
        }
    }

    private void markProfileExecutionStatus(Sql sql, ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration, boolean failed) {
        sql.call('{call PKG_E2B_PROCESSING.P_UPDATE_ETL_PROCESSED_CASES(?,?)}', [failed ? 1 : 0, executedIcsrProfileConfiguration.reportName])
    }

    @ReadOnly(connection = 'pva')
    List getLateList(boolean hidden = false){
        List<Late> lateList = []
        if (hidden) {
            lateList = Late.findAll().findAll{it}.sort { a, b ->
                // Sort based on hiddenDate presence
                def hiddenDateA = a.hiddenDate != null
                def hiddenDateB = b.hiddenDate != null
                hiddenDateA.compareTo(hiddenDateB)
            }
        } else {
            lateList =Late.findAllByHiddenDateIsNull()?.findAll{it}
        }
        lateList = lateList?.sort { lateA, lateB ->
            lateA.textDesc.toLowerCase() <=> lateB.textDesc.toLowerCase()
        }
        return lateList
    }
    @ReadOnly(connection = 'pva')
    List getLateListForOwnerApp(ReasonOfDelayAppEnum app,boolean hidden = false){
        List<Late> lateList = []
        if(hidden){
            lateList = Late.findAllByOwnerApp(app).findAll{it}.sort { a, b ->
                def hiddenDateA = a.hiddenDate != null
                def hiddenDateB = b.hiddenDate != null
                hiddenDateA.compareTo(hiddenDateB)
            }

        }else{
            lateList = Late.findAllByOwnerAppAndHiddenDateIsNull(app).findAll{it}.sort { a, b ->
                def hiddenDateA = a.hiddenDate != null
                def hiddenDateB = b.hiddenDate != null
                hiddenDateA.compareTo(hiddenDateB)
            }
        }
        return lateList
    }

    List getRootCauseList(ReasonOfDelayAppEnum app = null,boolean hidden = false) {
        List<RootCause> rootCauseList = []
        RootCause.withNewSession {
            if (app) {
                if (hidden) {
                    rootCauseList = RootCause.findAllByOwnerApp(app).findAll{it}.sort { a, b ->
                        def hiddenDateA = a.hiddenDate != null
                        def hiddenDateB = b.hiddenDate != null
                        hiddenDateA.compareTo(hiddenDateB)
                    }
                } else {
                    rootCauseList = RootCause.findAllByOwnerAppAndHiddenDateIsNull(app).findAll{it}
                }
            }else {
                if(hidden){
                    rootCauseList= RootCause.findAll().findAll{it}.sort { a, b ->
                        def hiddenDateA = a.hiddenDate != null
                        def hiddenDateB = b.hiddenDate != null
                        hiddenDateA.compareTo(hiddenDateB)
                    }
                }else{
                    rootCauseList= RootCause.findAllByHiddenDateIsNull().findAll{it}
                }

            }
            rootCauseList = rootCauseList.sort { rootCauseA, rootCauseB ->
                rootCauseA.textDesc.toLowerCase() <=> rootCauseB.textDesc.toLowerCase()
            }
            return rootCauseList
        }
    }

    List getResponsiblePartyList(ReasonOfDelayAppEnum app, boolean hidden = false) {
        List<ResponsibleParty> responsiblePartyList = []
        ResponsibleParty.withNewSession {
            if (hidden) {
                responsiblePartyList = ResponsibleParty.findAllByOwnerApp(app).findAll{it}.sort { a, b ->
                    // Sort based on hiddenDate presence
                    def hiddenDateA = a.hiddenDate != null
                    def hiddenDateB = b.hiddenDate != null
                    hiddenDateA.compareTo(hiddenDateB)
                }
            } else {
                responsiblePartyList = ResponsibleParty.findAllByOwnerAppAndHiddenDateIsNull(app).findAll{it}
            }
        }
        return responsiblePartyList
    }
    @ReadOnly(connection = 'pva')
    List getRootCauseListByLateId(Long lateId){
        Late late = Late.findById(lateId)
        List<RootCause> rootCauseList = []
        late?.rootCauseIds?.each {
            rootCauseList.add(RootCause.findById(it));
        }
        return rootCauseList
    }

    List getResponsiblePartyList(boolean hidden = false){
        List<ResponsibleParty> responsiblePartyList = []
        ResponsibleParty.withNewSession {
            if (hidden) {
                responsiblePartyList = ResponsibleParty.findAll()?.findAll { it }?.sort { a, b ->
                    // Sort based on hiddenDate presence
                    def hiddenDateA = a.hiddenDate != null
                    def hiddenDateB = b.hiddenDate != null
                    hiddenDateA.compareTo(hiddenDateB)
                }
            } else {
                responsiblePartyList = ResponsibleParty.findAllByHiddenDateIsNull()?.findAll { it }
            }
        }
        responsiblePartyList = responsiblePartyList.sort { responsiblePartyA, responsiblePartyB ->
            responsiblePartyA.textDesc.toLowerCase() <=> responsiblePartyB.textDesc.toLowerCase()
        }
        return responsiblePartyList
    }

    @ReadOnly(connection = 'pva')
    List getResponsiblePartyListByRootCauseId(Long rootCauseId){
        RootCause rootCause = RootCause.findById(rootCauseId)
        List<ResponsibleParty> responsiblePartyList = []
        rootCause?.responsiblePartyIds?.each{
            responsiblePartyList.add(ResponsibleParty.findById(it))
        }
        return responsiblePartyList
    }

    List getRootCauseSubCategoryList(boolean hidden = false){
        List<RootCauseSubCategory> rootCauseSubCategories = []
        RootCauseSubCategory.withNewSession {
            if (hidden) {
                rootCauseSubCategories = RootCauseSubCategory.findAll().findAll{it}.sort { a, b ->
                    // Sort based on hiddenDate presence
                    def hiddenDateA = a.hiddenDate != null
                    def hiddenDateB = b.hiddenDate != null
                    hiddenDateA.compareTo(hiddenDateB)
                }
            } else {
                rootCauseSubCategories =RootCauseSubCategory.findAllByHiddenDateIsNull().findAll{it}
            }

        }
        rootCauseSubCategories = rootCauseSubCategories.sort { rootCauseSubCategoriesA, rootCauseSubCategoriesB ->
            rootCauseSubCategoriesA.textDesc.toLowerCase() <=> rootCauseSubCategoriesB.textDesc.toLowerCase()
        }
        return rootCauseSubCategories
    }

    @ReadOnly(connection = 'pva')
    List getRCSubCategoryListByRCId(Long rootCauseId){
        RootCause rootCause = RootCause.findById(rootCauseId)
        List<RootCauseSubCategory> rootCauseSubCategories = []
        rootCause?.rootCauseSubCategoryIds?.each {
            rootCauseSubCategories.add(RootCauseSubCategory.findById(it));
        }
        return rootCauseSubCategories
    }

    List getRootCauseClassList(boolean hidden = false) {
        List<RootCauseClassification> list = []
        RootCauseClassification.withNewSession {
            if (hidden) {
                list = RootCauseClassification.findAll().findAll{it}.sort { a, b ->
                    // Sort based on hiddenDate presence
                    def hiddenDateA = a.hiddenDate != null
                    def hiddenDateB = b.hiddenDate != null
                    hiddenDateA.compareTo(hiddenDateB)
                }
            } else {
                list = RootCauseClassification.findAllByHiddenDateIsNull().findAll{it}
            }
        }
        list = list.sort { listA, listB ->
            listA.textDesc.toLowerCase() <=> listB.textDesc.toLowerCase()
        }
        return list
    }

    @ReadOnly(connection = 'pva')
    List getRCClassificationListByLateId(Long lateId){
        Late late = Late.findById(lateId)
        List<RootCauseClassification> rootCauseClassificationList = []
        late?.rootCauseClassIds?.each {
            rootCauseClassificationList.add(RootCauseClassification.findById(it));
        }
        return rootCauseClassificationList
    }


    List getCorrectiveActionList(){
        List correctiveActionList = []
        Sql sql = new Sql(dataSource_pva)
        try {
            correctiveActionList = sql.rows("SELECT * FROM VW_PVC_CORR_ACT_DSP")
        } finally {
        sql?.close()
        }
        correctiveActionList.sort { a, b -> a.TEXT_DESC.toLowerCase() <=> b.TEXT_DESC.toLowerCase() }
        return correctiveActionList
    }

    List getPreventativeActionList(){
        List preventativeActionList = []
        Sql sql = new Sql(dataSource_pva)
        try {
            preventativeActionList = sql.rows("SELECT * FROM VW_PVC_PREV_ACT_DSP")
        } finally {
            sql?.close()
        }
        preventativeActionList.sort { a, b -> a.TEXT_DESC.toLowerCase() <=> b.TEXT_DESC.toLowerCase() }
        return preventativeActionList
    }

    List getCorrectiveActionList(ReasonOfDelayAppEnum app) {
        List<CorrectiveAction> correctiveActionList = []
        CorrectiveAction.withNewSession {
            correctiveActionList = CorrectiveAction.findAllByOwnerApp(app.name())
        }
        return correctiveActionList
    }

    List getPreventativeActionList(ReasonOfDelayAppEnum app) {
        List<PreventativeAction> preventativeActionList = []
        PreventativeAction.withNewSession {
            preventativeActionList = PreventativeAction.findAllByOwnerApp(app.name()).findAll{it}
        }
        return preventativeActionList
    }

    def saveFixedTemplate(List caseList, List rcsList, boolean isInbound) {
        User user = userService.currentUser
        Sql sql = new Sql(getReportConnection())
        try {
            StringBuilder deleteQuery = new StringBuilder()
            if (isInbound) {
                caseList.each { c ->
                    deleteQuery.append("INSERT INTO GTT_INBOUND_COMPLIANCE_RCA (CASE_ID, TENANT_ID, VERSION_NUM, SENDER_ID) values(" + c?.caseId + ", " + c?.enterpriseId + ", " + c?.versionNumber + ", " + c?.senderId + ");")
                }
                sql.execute("begin execute immediate ' begin  pkg_pvr_app_util.p_truncate_table(''GTT_INBOUND_COMPLIANCE_RCA''); end;';\n" + deleteQuery.toString() + "\n END;\n")
                sql.call("{?= call PKG_INBOUND_COMPLIANCE.F_DELETE_INBOUND_RCA()}", [Sql.NUMERIC()]) { value ->
                }
            } else {
                //Deleting Query will be executed only for the bulk up
                if (caseList.size() > 1) {
                    caseList.each { c ->
                        deleteQuery.append("INSERT INTO gtt_submission_late_case_proc (CASE_ID, TENANT_ID, PROCESSED_REPORT_ID, FLAG_BULK_DELETE) values(" + c?.caseId + ", " + c?.enterpriseId + ", " + c?.reportId + ", 1);")
                    }
                } else {
                    caseList.each { c ->
                        rcsList.sort { a, b -> b.flagPrimary <=> a.flagPrimary }.each {
                            //flag D means user deleted the row from the RCA modal
                            if (it?.flagIUD.equals("D")) {
                                deleteQuery.append("INSERT INTO gtt_submission_late_case_proc (CASE_ID, TENANT_ID, PROCESSED_REPORT_ID, ID, FLAG_IUD,MODIFIED_BY) values(" + c?.caseId + ", " + c?.enterpriseId + ", " + c?.reportId + ", " + it?.pvcLcpId + ", '" + it?.flagIUD + "'," + user.id + ");")
                            }
                        }
                    }
                }
                if(deleteQuery){
                    sql.execute("begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_submission_late_case_proc''); end;';\n" + deleteQuery.toString() + "\n END;\n")
                    sql.call("{?= call PKG_PVR_PVC_HANDLER.P_DELETE_RCA()}", [Sql.NUMERIC()]) { value ->
                }
                }
            }

            StringBuilder updateQuery = new StringBuilder()
            caseList.each { c ->
                rcsList.sort { a, b -> b.flagPrimary <=> a.flagPrimary }.each {
                    boolean isPrimary = it?.flagPrimary?.equalsIgnoreCase("true")
                    if (isInbound) {
                        updateQuery.append("INSERT INTO GTT_INBOUND_COMPLIANCE_RCA (CASE_ID, TENANT_ID, SENDER_ID, VERSION_NUM, FLAG_LATE_RCA, ROOT_CAUSE_ID, RESPONSIBLE_PARTY_ID, CORRECTIVE_ACTION_ID, PREVENTATIVE_ACTION_ID, CORRECTIVE_DATE, PREVENTATIVE_DATE, INVESTIGATION, SUMMARY, ACTIONS,FLAG_PRIMARY, ID, FLAG_NEW, ROOT_CAUSE_SUB_CAT_ID, ROOT_CAUSE_CLASS_ID) VALUES " +
                                "(" + c?.caseId + ", " + c?.enterpriseId + ", " + c?.senderId + ", " + c?.versionNumber + ", " + it?.late + ", " + (it?.rootCause ?: "null") + ", " + (it?.responsibleParty ?: "null") + ", " + (it?.correctiveAction ?: "null") + ", " + (it?.preventativeAction ?: "null") +
                                (it?.correctiveDate ? ", TO_DATE('" + it?.correctiveDate + "', 'DD-MON-YYYY'), " : ", null, ") + (it?.preventiveDate ? "TO_DATE('" + it?.preventiveDate + "', 'DD-MON-YYYY'), q'[" : " null, q'[") + it?.investigation + "]', q'[" + it?.summary + "]', q'[" + it?.actions + "]', " + (isPrimary ? "1" : "0") + ", null,1," + (it?.rootCauseSubCategory ?: "null") + ", " + (it?.rootCauseClass ?: "null") + ");\n")
                    } else {
                        updateQuery.append("INSERT INTO gtt_submission_late_case_proc (CASE_ID, TENANT_ID, PROCESSED_REPORT_ID, VERSION_NUM, LATE_ID, ROOT_CAUSE_ID, RESPONSIBLE_PARTY_ID, CORRECTIVE_ACTION_ID, PREVENTATIVE_ACTION_ID, CORRECTIVE_DATE, PREVENTATIVE_DATE, INVESTIGATION, SUMMARY, ACTIONS,FLAG_PRIMARY, ID, FLAG_NEW, ROOT_CAUSE_SUB_CAT_ID, ROOT_CAUSE_CLASS_ID,FLAG_IUD,MODIFIED_BY,CREATED_BY) VALUES " +
                                "(" + c?.caseId + ", " + c?.enterpriseId + ", " + c?.reportId + ", " + c?.versionNumber + ", " + it?.late + ", " + (it?.rootCause ?: "null") + ", " + (it?.responsibleParty ?: "null") + ", " + (it?.correctiveAction ?: "null") + ", " + (it?.preventativeAction ?: "null") +
                                (it?.correctiveDate ? ", TO_DATE('" + it?.correctiveDate + "', 'DD-MON-YYYY'), " : ", null, ") + (it?.preventiveDate ? "TO_DATE('" + it?.preventiveDate + "', 'DD-MON-YYYY'), q'[" : " null, q'[") + it?.investigation + "]', q'[" + it?.summary + "]', q'[" + it?.actions + "]', " + (isPrimary ? "1" : "0") + ", " + (it?.pvcLcpId ?: "null") + ", 1, "+ (it?.rootCauseSubCategory ?: "null") + ", " + (it?.rootCauseClass ?: "null") + ", '" + it?.flagIUD + "', " + user.id + "," + user.id + ");\n")
                    }
                }
            }

            if (updateQuery) {
                if (isInbound) {
                    String q = "begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_INBOUND_COMPLIANCE_RCA''); end;';\n" + updateQuery.toString() + "\n END;\n"
                    sql.execute(q)
                    sql.call("{call PKG_INBOUND_COMPLIANCE.P_POP_C_INBOUND_COMPLIANCE_RCA}")
                }
                else {
                    String q = "begin execute immediate ' begin pkg_pvr_app_util.p_truncate_table(''gtt_submission_late_case_proc''); end;';\n" + updateQuery.toString() + "\n END;\n"
                    sql.execute(q)
                    sql.call("{call PKG_PVR_PVC_HANDLER.P_LATE_CASE_PROCESSING}")
                }
            }

        } finally {
            sql?.close()
        }
    }

    def appendReasonOfDelayDataFromMart(Map data, String dateFormat = "dd-MMM-yyyy", boolean isInbound=false) {
        if (data.data) {
            Map index
            if (isInbound) {
                index = ["masterCaseNum", "masterFupNum", "pvcIcSenderName", "masterRptTypeId", "masterCountryId",
                         "masterInitReptDate", "ciDateFirstSafetyReceived", "assessSeriousness", "masterOwnerId",
                         "pvcIcFlagLate", "pvcIcRootCause", "pvcIcRcClass", "pvcIcRcSubCat", "pvcIcRespParty",
                         "pvcIcCorrAct", "pvcIcPrevAct", "pvcIcCorrDate", "pvcIcPrevDate", "pvcIcDtp", "masterCaseId", "masterEnterpriseId",
                         "pvcIcSenderId", "masterVersionNum"].collectEntries({ m -> [m, data.header.findIndexOf { it == m }] })
            }
            else {
                index = ["vcsProcessedReportId", "masterEnterpriseId", "masterCaseId", "masterVersionNum",
                             "pvcLcpLate",
                             "pvcLcpRootCause",
                             "pvcLcpRespParty",
                             "pvcLcpCorrAct",
                             "pvcLcpPrevAct",
                             "pvcLcpCorrDate", "pvcLcpRcClass", "pvcLcpRcSubCat",
                             "pvcLcpPrevDate", "pvcLcpInvestigation", "pvcLcpSummary", "pvcLcpActions"].collectEntries({ m -> [m, data.header.findIndexOf { it == m }] })
            }
            Sql sql = new Sql(dataSource_pva)
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { stmt ->
                stmt.fetchSize = 1000
            }
            try {
                Long startTime = System.currentTimeMillis()
                // StringBuilder queryString = new StringBuilder("select     CASE_ID,TENANT_ID,VERSION_NUM,PROCESSED_REPORT_ID,CASE_NUM,LATE,ROOT_CAUSE,REPONSIBLE_PARTY,CORRECTIVE_ACTION,CORRECTIVE_DATE,PREVENTATIVE_ACTION,PREVENTATIVE_DATE,WORKFLOW_STATE,ASSIGNED_TO,ID,FLAG_PRIMARY from VW_PVR_LATE_CASE_PROCESSING ")
                StringBuilder queryString
                if (isInbound) {
                    queryString = new StringBuilder("select distinct TENANT_ID,VERSION_NUM,SENDER_ID,CASE_ID,LATE,ROOT_CAUSE,REPONSIBLE_PARTY," +
                            "CORRECTIVE_ACTION,CORRECTIVE_DATE,PREVENTATIVE_ACTION,PREVENTATIVE_DATE, INVESTIGATION, SUMMARY, ACTIONS, ROOT_CAUSE_SUB_CATEGORY, " +
                            "ROOT_CAUSE_CLASSIFICATION from vw_c_inbound_compliance_rca where (CASE_ID, TENANT_ID, VERSION_NUM, SENDER_ID) IN (")
                    queryString.append(data.data.collect { row ->
                        "SELECT ${row[index.masterCaseId]},${row[index.masterEnterpriseId]},${row[index.masterVersionNum]},${row[index.pvcIcSenderId]} FROM DUAL"
                    }.join(" UNION "))
                }
                else {
                    queryString = new StringBuilder("select distinct TENANT_ID,VERSION_NUM,PROCESSED_REPORT_ID,CASE_ID,LATE,ROOT_CAUSE,REPONSIBLE_PARTY," +
                            "CORRECTIVE_ACTION,CORRECTIVE_DATE,PREVENTATIVE_ACTION,PREVENTATIVE_DATE, INVESTIGATION, SUMMARY, ACTIONS, ROOT_CAUSE_SUB_CATEGORY, " +
                            "ROOT_CAUSE_CLASSIFICATION from VW_PVR_LATE_CASE_PROCESSING where  (CASE_ID, TENANT_ID, PROCESSED_REPORT_ID) IN (")
                    queryString.append(data.data.collect { row ->
                        "SELECT ${row[index.masterCaseId]},${row[index.masterEnterpriseId]},${row[index.vcsProcessedReportId]} FROM DUAL"
                    }.join(" UNION "))
                }

                queryString.append(") AND FLAG_PRIMARY = 1")
                String query = queryString.toString()
                if (data.data.size() > 5000) query = "select * from VW_PVR_LATE_CASE_PROCESSING  where FLAG_PRIMARY = 1"
                Map martData = [:]
                sql.eachRow(query) { GroovyResultSet resultSet ->
                    Map rowMap = [:]
                    resultSet.toRowResult().eachWithIndex { it, i ->
                        String val = ""
                        if (it.value) {
                            val = (it.value instanceof Timestamp) ? (new SimpleDateFormat(dateFormat).format(it.value)) : it.value.toString()
                        }
                        rowMap.put(it.key, val)
                    }
                    if (isInbound) {
                        martData.put(rowMap["CASE_ID"] + "_" + rowMap["TENANT_ID"] + "_" + rowMap["VERSION_NUM"] + "_" + rowMap["SENDER_ID"], rowMap)
                    } else {
                        martData.put(rowMap["CASE_ID"] + "_" + rowMap["TENANT_ID"] + "_" + rowMap["PROCESSED_REPORT_ID"], rowMap)
                    }
                }
                data.data.collect { row ->
                    String key = isInbound ? (row[index.masterCaseId] + "_" + row[index.masterEnterpriseId] + "_" + row[index.masterVersionNum] + "_" + row[index.pvcIcSenderId]) :
                            (row[index.masterCaseId] + "_" + row[index.masterEnterpriseId] + "_" + row[index.vcsProcessedReportId])
                    Map martDataRow = martData[key]
                    if (martDataRow) {
                        if (isInbound) {
                            row[index.pvcIcFlagLate] = martDataRow["LATE"]
                            row[index.pvcIcRootCause] = martDataRow["ROOT_CAUSE"]
                            row[index.pvcIcRespParty] = martDataRow["REPONSIBLE_PARTY"]
                            row[index.pvcIcCorrAct] = martDataRow["CORRECTIVE_ACTION"]
                            row[index.pvcIcPrevAct] = martDataRow["PREVENTATIVE_ACTION"]
                            row[index.pvcIcCorrDate] = martDataRow["CORRECTIVE_DATE"]
                            row[index.pvcIcPrevDate] = martDataRow["PREVENTATIVE_DATE"]
                            row[index.pvcIcRcClass] = martDataRow["ROOT_CAUSE_CLASSIFICATION"]
                            row[index.pvcIcRcSubCat] = martDataRow["ROOT_CAUSE_SUB_CATEGORY"]
                        } else {
                            if (index.pvcLcpLate != -1) row[index.pvcLcpLate] = martDataRow["LATE"]
                            if (index.pvcLcpRootCause != -1) row[index.pvcLcpRootCause] = martDataRow["ROOT_CAUSE"]
                            if (index.pvcLcpRespParty != -1) row[index.pvcLcpRespParty] = martDataRow["REPONSIBLE_PARTY"]
                            if (index.pvcLcpCorrAct != -1) row[index.pvcLcpCorrAct] = martDataRow["CORRECTIVE_ACTION"]
                            if (index.pvcLcpPrevAct != -1) row[index.pvcLcpPrevAct] = martDataRow["PREVENTATIVE_ACTION"]
                            if (index.pvcLcpCorrDate != -1) row[index.pvcLcpCorrDate] = martDataRow["CORRECTIVE_DATE"]
                            if (index.pvcLcpPrevDate != -1) row[index.pvcLcpPrevDate] = martDataRow["PREVENTATIVE_DATE"]
                            if (index.pvcLcpInvestigation != -1) row[index.pvcLcpInvestigation] = martDataRow["INVESTIGATION"]
                            if (index.pvcLcpSummary != -1) row[index.pvcLcpSummary] = martDataRow["SUMMARY"]
                            if (index.pvcLcpActions != -1) row[index.pvcLcpActions] = martDataRow["ACTIONS"]
                            if (index.pvcLcpRcClass != -1) row[index.pvcLcpRcClass] = martDataRow["ROOT_CAUSE_CLASSIFICATION"]
                            if (index.pvcLcpRcSubCat != -1) row[index.pvcLcpRcSubCat] = martDataRow["ROOT_CAUSE_SUB_CATEGORY"]
                        }
                    }
                }
                log.info("ROD data fetched in ${(System.currentTimeMillis()-startTime)} miliseconds")
            } finally {
                sql?.close()
            }
        }
        data
    }

    def getAllReasonOfDelayFromMart(Long caseId, String reportId, Long enterpriseId, Long senderId, Long caseVersion) {
        Sql sql = new Sql(dataSource_pva)
        List martData = []
        try {
            String query = "select distinct ID, TENANT_ID,VERSION_NUM,CASE_ID,LATE,FLAG_PRIMARY,ROOT_CAUSE,REPONSIBLE_PARTY,CORRECTIVE_ACTION,CORRECTIVE_DATE,PREVENTATIVE_ACTION,PREVENTATIVE_DATE, INVESTIGATION, SUMMARY, ACTIONS,ROOT_CAUSE_SUB_CATEGORY,ROOT_CAUSE_CLASSIFICATION from "
            if (senderId > -1) {
                query += " VW_C_INBOUND_COMPLIANCE_RCA where CASE_ID=:caseId and TENANT_ID=:enterpriseId and VERSION_NUM=${Long.valueOf(caseVersion)} AND SENDER_ID=${Long.valueOf(senderId)}"
            }
            else {
                query += " VW_PVR_LATE_CASE_PROCESSING where CASE_ID=:caseId and TENANT_ID=:enterpriseId and PROCESSED_REPORT_ID=${reportId}"
            }

            sql.eachRow(query, [caseId: caseId, enterpriseId: enterpriseId]) { GroovyResultSet resultSet ->
                Map m = resultSet.toRowResult()
                martData << [pvcLcpId             : m["ID"], primaryFlag: m['FLAG_PRIMARY'], lateValue: m['LATE'], rootCauseValue: m['ROOT_CAUSE'],rootCauseClassValue:m['ROOT_CAUSE_CLASSIFICATION'],
                             responsiblePartyValue: m['REPONSIBLE_PARTY'],rootCauseSubCategoryValue: m['ROOT_CAUSE_SUB_CATEGORY'], correctiveActionValue: m['CORRECTIVE_ACTION'],
                             preventiveActionValue: m['PREVENTATIVE_ACTION'], investigation: m['INVESTIGATION'] ?: "", summary: m['SUMMARY'] ?: "", actions: m['ACTIONS'] ?: "",
                             correctiveDate       : (m['CORRECTIVE_DATE'] ? (new SimpleDateFormat("dd-MMM-yyyy").format(m['CORRECTIVE_DATE'])) : ""),
                             preventiveDate       : (m['PREVENTATIVE_DATE'] ? (new SimpleDateFormat("dd-MMM-yyyy").format(m['PREVENTATIVE_DATE'])) : ""),
                ]
            }
        } finally {
            sql?.close()
        }
        return martData
    }

    boolean importRcas(List list, Boolean submit, Boolean replace) {
        Sql sql = new Sql(getReportConnection())
        try {
            Map gtt = createImportRcaGtt(list)
            List toMart=gtt.rowsToInsert
            sql.execute(gtt.query)
            sql.call("{call PKG_PVR_PVC_HANDLER.P_IMPORT_LATE_CASE_PROC(?,?)}", [0, (replace ? "R" : "A")])
            sql.rows("select TEXT_11 from GTT_CLL_REPORT_DATA_TEMP").eachWithIndex { r, i ->
                toMart[i][Constants.Central.RCA_COLUMN_NUMBER] = getErrors(r[0])
            }
            if (submit && !list.find { it[Constants.Central.RCA_COLUMN_NUMBER] }) {
                gtt = createImportRcaGtt(list)
                sql.execute(gtt.query)
                sql.call("{call PKG_PVR_PVC_HANDLER.P_IMPORT_LATE_CASE_PROC(?,?)}", [1, (replace ? "R" : "A")])
                sql.rows("select TEXT_11 from GTT_CLL_REPORT_DATA_TEMP where NUMBER_2=1").eachWithIndex { r, i ->
                    toMart[i][Constants.Central.RCA_COLUMN_NUMBER] = getErrors(r[0])
                }
                return !list.find { it[Constants.Central.RCA_COLUMN_NUMBER] }
            }

        } finally {
            sql?.close()
        }
        return false
    }

    boolean importSubmissions(List list, Boolean submit) {
        Sql sql = new Sql(getReportConnection())
        try {
            Map gtt = createImportSubmissionGtt(list)
            List toMart = gtt.rowsToInsert
            sql.execute(gtt.query)

            sql.call("{call PKG_PVR_PVC_HANDLER.P_IMPORT_SUBMISSION_INFO(0)}")
            sql.rows("select TEXT_11 from GTT_CLL_REPORT_DATA_TEMP ORDER BY SORT_COL").eachWithIndex { r, i ->
                toMart[i][8] = getSubmissionErrors(r[0])
            }
            if (submit && !list.find { it[8] }) {
                gtt = createImportSubmissionGtt(list)
                sql.execute(gtt.query)
                sql.call("{call PKG_PVR_PVC_HANDLER.P_IMPORT_SUBMISSION_INFO(1)}")
                sql.rows("select TEXT_11 from GTT_CLL_REPORT_DATA_TEMP where NUMBER_2=1 ORDER BY SORT_COL").eachWithIndex { r, i ->
                    toMart[i][8] = getSubmissionErrors(r[0])
                }
                return !list.find { it[8] }
            }

        } finally {
            sql?.close()
        }
        return false
    }
    static Map responseErrorMapping

    private String getSubmissionErrors(String error) {
        if (error == "|SUCESSFULLY IMPORTED|") return null
        return error.toString().tokenize("|").findAll { it }.collect { it.trim() }.join(";")
    }

    private getErrors(String codeList) {
        if (!codeList)return null
        if(!responseErrorMapping){
            responseErrorMapping = [
                    SUBMISSION:ViewHelper.getMessage("app.pvc.mart.SUBMISSION"),
                    LATE:ViewHelper.getMessage("app.pvc.mart.LATE"),
                    ROOT_CAUSE:ViewHelper.getMessage("app.pvc.mart.ROOT_CAUSE"),
                    ROOT_CAUSE_SUB_CATEGORY:ViewHelper.getMessage("app.pvc.mart.ROOT_CAUSE_SUB"),
                    ROOT_CAUSE_CLASSIFICATION:ViewHelper.getMessage("app.pvc.mart.ROOT_CAUSE_CLASS"),
                    RESPONSIBLE_PARTY:ViewHelper.getMessage("app.pvc.mart.RESPONSIBLE_PARTY"),
                    CORRECTIVE_ACTION:ViewHelper.getMessage("app.pvc.mart.CORRECTIVE_ACTION"),
                    PREVENTATIVE_ACTION:ViewHelper.getMessage("app.pvc.PREVENTATIVE_ACTION.RESPONSIBLE_PARTY")]
        }
        codeList?.toString()?.tokenize("|")?.findAll{it}?.collect{responseErrorMapping[it.trim()]}?.join(";")
    }
    Map createImportSubmissionGtt(List list ){
        List toMart = []
        StringBuilder query = new StringBuilder()
        list.eachWithIndex { c ,i ->
            if(!c[8]) {
                toMart<<c
                query.append("INSERT INTO GTT_CLL_REPORT_DATA_TEMP (CASE_NUM, DATE_1, TEXT_1,DATE_2,DATE_3,NUMBER_1,NUMBER_2,TEXT_2,SORT_COL)" +
                        "values('" + c[0] + "', " +
                        (c[1] ? "TO_DATE('" + c[1] + "', 'DD-MON-YYYY')," : "null,") +
                        (c[2] ? "'" + c[2] + "'," : "null,") +
                        "TO_DATE('" + c[3] + "', 'DD-MON-YYYY')," +
                        "TO_DATE('" + c[4] + "', 'DD-MON-YYYY')," +
                        (c[5] ? "" + (c[5]=="Periodic"?"0":"1") + "," : "null,") +
                        (c[6] ? "" + c[6] + "," : "null,") +
                        (c[7] ? "'" + c[7] + "'," : "null,") +
                        (i + 1) +
                        ");\n");
            }
        }
        return [rowsToInsert: toMart, query:"begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_CLL_REPORT_DATA_TEMP''); end;';\n" + query.toString() + "\n END;\n"]
    }

    Map createImportRcaGtt(List list ){
        List toMart = []
        StringBuilder query = new StringBuilder()
        User user = userService.currentUser
        list.each { c ->
            if(!c[Constants.Central.RCA_COLUMN_NUMBER]) {
                toMart<<c
                query.append("INSERT INTO GTT_CLL_REPORT_DATA_TEMP (Case_num, TEXT_1, DATE_1, DATE_2, DATE_3,TEXT_2,TEXT_3,TEXT_10,TEXT_12,TEXT_4,TEXT_5,TEXT_6,DATE_4,DATE_5,TEXT_7,TEXT_8,VERSION_NUM,TEXT_21,TEXT_22,TEXT_23,NUMBER_1) " +
                        "values('" + c[0] + "', '" + c[1] + "', " +
                        "TO_DATE('" + c[2] + "', 'DD-MON-YYYY')," +
                        "TO_DATE('" + c[3] + "', 'DD-MON-YYYY')," +
                        (c[4] ? "TO_DATE('" + c[4] + "', 'DD-MON-YYYY')," : "null,") +
                        (c[5] ? "'" + c[5] + "'," : "null,") +
                        (c[6] ? "'" + c[6] + "'," : "null,") +
                        (c[7] ? "'" + c[7] + "'," : "null,") +
                        (c[8] ? "'" + c[8] + "'," : "null,") +
                        (c[9] ? "'" + c[9] + "'," : "null,") +
                        (c[10] ? "'" + c[10] + "'," : "null,") +
                        (c[11] ? "'" + c[11] + "'," : "null,") +
                        (c[12] ? "TO_DATE('" + c[12] + "', 'DD-MON-YYYY')," : "null,") +
                        (c[13] ? "TO_DATE('" + c[13] + "', 'DD-MON-YYYY')," : "null,") +
                        "null," +
                        "" + (c[14]?.toString()?.equalsIgnoreCase("yes") ? "'yes'," : "'no',") +
                        (c[15] ? c[15] + "," : "null,") +
                        (c[16] ? "'" + c[16] + "'," : "null,") +
                        (c[17] ? "'" + c[17] + "'," : "null,") +
                        (c[18] ? "'" + c[18] + "'," : "null,") +
                        (user.id? user.id  : "null")+ ");\n");
            }
        }
        return [rowsToInsert: toMart, query:"begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_CLL_REPORT_DATA_TEMP''); end;'; execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_submission_late_case_proc''); end;';\n" + query.toString() + "\n END;\n"]
    }

    void sendGenerationSuccessEmail(ExecutedIcsrProfileConfiguration configuration, IcsrCaseProcessingQueue icsrCaseProcessingQueue, ExecutedIcsrTemplateQuery executedTemplateQuery, String state) {
        List<String> recipients = configuration.executedDeliveryOption?.emailToUsers
        if(recipients?.size()){
            String emailSubject = createEmailSubject(configuration.reportName, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, state);
            String emailBody = createEmailBody(configuration, icsrCaseProcessingQueue, executedTemplateQuery.executedTemplate?.name, executedTemplateQuery.executedQuery?.name, state)
            emailService.sendEmail(recipients, emailBody, true, emailSubject)
        }
    }

    void sendGenerationErrorEmail(ExecutedIcsrProfileConfiguration configuration, IcsrCaseProcessingQueue icsrCaseProcessingQueue, ExecutedIcsrTemplateQuery executedTemplateQuery, String state) {
        List<String> recipients = configuration.executedDeliveryOption?.emailToUsers
        if(recipients?.size()){
            String emailSubject = createEmailSubject(configuration.reportName, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, state);
            String emailBody = createEmailBody(configuration, icsrCaseProcessingQueue, executedTemplateQuery.executedTemplate?.name, executedTemplateQuery.executedQuery?.name, state)
            emailService.sendEmail(recipients, emailBody, true, emailSubject)
        }
    }

    String createEmailSubject(String reportName, String caseNumber, Long versionNumber, String state){
        String emailSubject = "ICSR Report " + state + " - " + reportName + " - " + caseNumber + " (${versionNumber})"
        return emailSubject
    }

    String createEmailBody(ExecutedIcsrProfileConfiguration configuration, IcsrCaseProcessingQueue icsrCaseProcessingQueue, String templateName, String queryName, String state){
        Locale locale = userService.getCurrentUser()?.preference?.locale
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
        }
        String emailBody = ViewHelper.getMessage("app.label.hello.all") + "<br><br>"
        emailBody += "Please find ICSR Report details" + "<br><br>"
        emailBody += "Case Number : " + icsrCaseProcessingQueue.caseNumber + "<br>"
        emailBody += "Version: " + icsrCaseProcessingQueue.versionNumber.intValue() + "<br>"
        emailBody += "Recipient : " + configuration.recipientOrganizationName + "<br>"
        emailBody += "Profile Name : " + configuration.reportName + "<br>"
        emailBody += "Form : " + templateName + "<br>"
        emailBody += "Scheduling Criteria : " + (queryName ?: 'None') + "<br>"
        if(state != "Error"){
            emailBody += "Due in (Days): " + (icsrCaseTracking.dueInDays ?: 'None')+ "<br>"
            emailBody += "Due Date : " + icsrCaseTracking.dueDate + "<br>"
            emailBody += "State : " + icsrCaseTracking.e2BStatus + "<br>"
            emailBody += "Generation Date : " + icsrCaseTracking.generationDate + "<br><br>"
        }
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        return emailBody
    }

    void sendAllIcsrProfilesFailureEmailTo(String errorMessage) {
        Locale locale = userService.getCurrentUser()?.preference?.locale
        String[] recipients = grailsApplication.config.getProperty('icr.case.admin.emails').split(',')
        String emailSubject = ViewHelper.getMessage('app.emailService.icsr.execution.error.subject.label')
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += ViewHelper.getMessage('app.emailService.icsr.execution.error.message.label', errorMessage)
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        emailService.sendEmail(recipients, emailBody, true, emailSubject)
    }

    void insertAutoRODIntoGttValues() {
        Date currentDateTime = new Date()
        AutoReasonOfDelay autoReasonOfDelayInstance = AutoReasonOfDelay.first()
        User user = User.findByUsername(utilService.getJobUser())
        if(!user){
            log.info("Application user is not present in the system")
        }
        if(autoReasonOfDelayInstance && autoReasonOfDelayInstance.nextRunDate && currentDateTime.getTime() > autoReasonOfDelayInstance?.nextRunDate?.getTime()){
            autoReasonOfDelayInstance.executing = true
            scheduleAutoROD(autoReasonOfDelayInstance)
            List<QueryRCA> queryRCAList = autoReasonOfDelayInstance?.queriesRCA
            String queryName = null
            Sql sql = new Sql(getReportConnection())
            String cleaningUpGttTable = ("begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_tabulation''); end;';" +
                    "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_params''); end;';" +
                    "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_fields''); end;';" +
                    "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_filter_key_values''); end;';" +
                    "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_TABULATION_MEASURES''); end;';" +
                    "delete from GTT_QUERY_DETAILS; delete from GTT_QUERY_SETS; END;")
            log.info("Execution for Auto Reason Of Delay Started ...")
            JobExecutionHistory jobExecutionHistory = new JobExecutionHistory()
            jobExecutionHistory.jobTitle = Constants.AUTO_REASON_OF_DELAY
            jobExecutionHistory.jobStartRunDate = new Date()
            jobExecutionHistory.jobRunStatus = JobExecutionHistoryStatusEnum.IN_PROGRESS
            jobExecutionHistory.createdBy = "Application"
            jobExecutionHistory.modifiedBy = "Application"
            jobExecutionHistory = (JobExecutionHistory) CRUDService.save(jobExecutionHistory)
            try {
                queryRCAList.each { QueryRCA queryRCA ->
                    if (queryRCA) {
                        String insertStatement = ""
                        SuperQuery query = queryRCA?.query
                        queryName = query.name

                        String startDate = null
                        String endDate = null
                        if(queryRCA?.dateRangeInformationForQueryRCA.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE){
                            List dateRanges = autoReasonOfDelayInstance.globalDateRangeInformationAutoROD.getReportStartAndEndDate()
                            startDate = dateRanges[0]?.format(SqlGenerationService.DATE_FMT).toString()
                            endDate = dateRanges[1]?.format(SqlGenerationService.DATE_FMT).toString()
                        }else{
                            List dateRanges = queryRCA.dateRangeInformationForQueryRCA.getReportStartAndEndDate()
                            startDate = dateRanges[0]?.format(SqlGenerationService.DATE_FMT).toString()
                            endDate = dateRanges[1]?.format(SqlGenerationService.DATE_FMT).toString()
                        }
                        String dateRangeType = autoReasonOfDelayInstance?.dateRangeType?.name
                        String evaluateDateAs = autoReasonOfDelayInstance?.evaluateDateAs
                        String lateId = queryRCA?.lateId ?: ""
                        String rootCauseId = queryRCA?.rootCauseId ?: ""
                        String rcCustomExpression = fixCustomSql(queryRCA?.rcCustomExpression)
                        String rootCauseClassificationId = queryRCA?.rootCauseClassId ?: ""
                        String rcClassCustomExp = fixCustomSql(queryRCA?.rcClassCustomExp)
                        String rootCauseSubCategoryId = queryRCA?.rootCauseSubCategoryId ?: ""
                        String rcSubCatCustomExp = fixCustomSql(queryRCA?.rcSubCatCustomExp)
                        String responsiblePartyId = queryRCA?.responsiblePartyId ?: ""
                        String rpCustomExpression = fixCustomSql(queryRCA?.rpCustomExpression)
                        String actions = queryRCA?.actions ?: ""
                        String actionsSql = fixCustomSql(queryRCA?.actionsSql)
                        String investigation = queryRCA?.investigation ?: ""
                        String investigationSql = fixCustomSql(queryRCA?.investigationSql)
                        String summary = queryRCA?.summary ?: ""
                        String summarySql = fixCustomSql(queryRCA?.summarySql)
                        String assignToByDB = queryRCA?.sameAsRespParty

                        insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVALUATE_DATA_ASOF', '${evaluateDateAs}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE','${startDate}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE','${endDate}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATE_RANGE_TYPE','${dateRangeType}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ISSUE_TYPE','${lateId}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ROOT_CAUSE_ID', '${rootCauseId}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ROOT_CAUSE', '${rcCustomExpression}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ROOT_CAUSE_CLASS_ID', '${rootCauseClassificationId}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ROOT_CAUSE_CLASSIFICATION', '${rcClassCustomExp}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ROOT_CAUSE_SUB_CAT_ID', '${rootCauseSubCategoryId}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ROOT_CAUSE_SUB_CATEGORY', '${rcSubCatCustomExp}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('RESPONSIBLE_PARTY_ID', '${responsiblePartyId}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('RESPONSIBLE_PARTY', '${rpCustomExpression}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('USER_ID', '${user.id}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ASSIGNED_TO', '${assignToByDB}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUMMARY', '${summary}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INVESTIGATION', '${investigation}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ACTIONS', '${actions}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUMMARY_QUERY', '${summarySql}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INVESTIGATION_QUERY', '${investigationSql}');" +
                                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ACTIONS_QUERY', '${actionsSql}');";

                        insertStatement += "END;"
                        sql.execute(cleaningUpGttTable)
                        sql.execute(insertStatement)
                        log.debug("Insert for transferring data : ${insertStatement.toString()}")
                        sql.call("{call PKG_PVR_PVC_HANDLER.P_AUTO_ASSIGN_ROD_VERSION}")

                        Set<ParameterValue> poiInputParams =  [] //autoReasonOfDelayInstance?.poiInputsParameterValues ?:[]
                        Locale locale = autoReasonOfDelayInstance.owner.preference.locale
                        sqlGenerationService.setSqlSessionContext(sql, locale)
                        boolean nonValidCases = false
                        String insertStatement2 = sqlGenerationService.getInsertStatementsToInsert(queryRCA.usedQuery, null, queryRCA.usesQueryValueLists, poiInputParams, locale, nonValidCases, autoReasonOfDelayInstance.owner ,false);
                        sql.execute(insertStatement2)
                        log.debug("Insert for transferring query set and details : ${insertStatement2.toString()}")
                        sql.call("{call PKG_PVR_PVC_HANDLER.P_AUTO_ASSIGN_ROD_MAIN}")
                        def results = sql.rows("SELECT * FROM GTT_QUERY_CASE_LIST WHERE PROCESSED_REPORT_ID IS NOT NULL");
                        results?.each {
                            Map metadataParams = [:]
                            metadataParams.masterCaseId = it['CASE_ID'].longValue()
                            metadataParams.processedReportId = it['PROCESSED_REPORT_ID'].toString()
                            metadataParams.tenantId = it['TENANT_ID'].longValue()
                            saveOrUpdateMetaDataRecord(metadataParams, queryRCA, it['PARAM_1'].toString())
                        }
                    }
                }
                autoReasonOfDelayInstance.executing = false
                CRUDService.update(autoReasonOfDelayInstance)
                updateJobExecutionHistory(jobExecutionHistory, JobExecutionHistoryStatusEnum.SUCCESS, null)
                log.info("Execution for Auto Reason Of Delay End ...")
            } catch (Exception e) {
                autoReasonOfDelayInstance.executing = false
                updateJobExecutionHistory(jobExecutionHistory, JobExecutionHistoryStatusEnum.FAILURE, e.getMessage())
                log.error("Error while inserting GTT for templates", e)
                if(autoReasonOfDelayInstance?.emailToUsers.size() > 0){
                    sendAutoRODFailureEmailTo(autoReasonOfDelayInstance?.emailToUsers, queryName, e.getMessage())
                }
            } finally {
                sql?.close()
            }
        }
    }
    private static String fixCustomSql(String sql){
        return sql?.replaceAll("(?i)'", "''")?.replaceAll("(?i)null", "\'null\'")?:""
    }
    void saveOrUpdateMetaDataRecord(Map metadataParams, QueryRCA queryRCA, String assignToUserInRPCase){
        DrilldownCLLMetadata metadataRecord = DrilldownCLLMetadata.getMetadataRecord(metadataParams).get()
        if(!metadataRecord) {
            metadataRecord = new DrilldownCLLMetadata()
            metadataRecord.caseId = metadataParams.masterCaseId
            metadataRecord.processedReportId = metadataParams.processedReportId
            metadataRecord.tenantId = metadataParams.tenantId
            metadataRecord.workflowState = WorkflowState.defaultWorkState
            metadataRecord.workflowStateUpdatedDate = new Date()
            metadataRecord.updateDueDate(null)
            metadataRecord = getAssignUserOrUserGroup(metadataRecord, queryRCA, assignToUserInRPCase)
            CRUDService.save(metadataRecord)
        }else {
            metadataRecord = getAssignUserOrUserGroup(metadataRecord, queryRCA, assignToUserInRPCase)
            CRUDService.update(metadataRecord)
        }
        sessionFactory.getCurrentSession().evict(metadataRecord);
    }

    DrilldownCLLMetadata getAssignUserOrUserGroup(DrilldownCLLMetadata metadataRecord, QueryRCA queryRCA, String assignToUserInRPCase){
        if(metadataRecord != null && !(metadataRecord.assignedToUser) && queryRCA?.assignedToUser){
            metadataRecord.assignedToUser = queryRCA?.assignedToUser
        }else if(metadataRecord != null && !(metadataRecord.assignedToUser) && queryRCA.sameAsRespParty){
            User user = User.findByFullName(assignToUserInRPCase)
            metadataRecord.assignedToUser = user ?: null
        }
        if(metadataRecord != null  && !(metadataRecord.assignedToUserGroup) && queryRCA?.assignedToUserGroup) {
            metadataRecord.assignedToUserGroup = queryRCA?.assignedToUserGroup
        }else if(metadataRecord != null && !(metadataRecord.assignedToUser) && queryRCA.sameAsRespParty){
            User user = User.findByFullName(assignToUserInRPCase)
            metadataRecord.assignedToUser = user ?: null
        }
        metadataRecord.updateAssignedToName()
        metadataRecord.assigner = userService.currentUser
        metadataRecord.assigneeUpdatedDate = new Date()
        return metadataRecord
    }

    def scheduleAutoROD(AutoReasonOfDelay autoReasonOfDelayInstance){
        autoReasonOfDelayInstance.lastRunDate = autoReasonOfDelayInstance.nextRunDate
        if (MiscUtil.validateScheduleDateJSON(autoReasonOfDelayInstance.scheduleDateJSON)) {
            autoReasonOfDelayInstance.nextRunDate = configurationService.getNextDate(autoReasonOfDelayInstance)
        }else{
            autoReasonOfDelayInstance.nextRunDate = null
        }
    }

    def updateJobExecutionHistory(JobExecutionHistory jobExecutionHistory, JobExecutionHistoryStatusEnum status, String remarks){
        jobExecutionHistory.jobRunStatus = status
        jobExecutionHistory.jobEndRunDate = new Date()
        jobExecutionHistory.jobRunRemarks = remarks ?: null
        CRUDService.update(jobExecutionHistory)
    }

    void sendAutoRODFailureEmailTo(List<String> emailToUsers, String queryName, String errorMsg) {
        Locale locale = userService.getCurrentUser()?.preference?.locale
        String[] recipients = emailToUsers as String[]
        String emailSubject = "Auto Reason of Delay execution error";
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += "While Executing the below criteria, execution gets failed:<br>"
        emailBody += "<br> Query Name : "+queryName
        emailBody += "<br> Error : "+errorMsg
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        emailService.sendEmail(recipients, emailBody, true, emailSubject)
    }

    void captureAuditLogChangesForRODAssessment(Map oldRcMap, Map rcMap, def configuration, def cllRowId) {
        DrilldownCLLData drillDownData = DrilldownCLLData.findById(Long.valueOf(cllRowId))
        String caseNumber = new JsonSlurper().parseText(drillDownData.cllRowData)["masterCaseNum"]
        ["vcsProcessedReportId", "reportsAgencyId", "reportsDateSubmitted"].each {
            String val = new JsonSlurper().parseText(drillDownData.cllRowData)[it]
            if (val) caseNumber += " ," + ViewHelper.getMessage("app.reportField." + it) + ": " + val
        }
        if (rcMap.flagIUD == Constants.AuditLog.DELETE_FLAG) {
            Map deletionChangesMade = getDeletionChangesForRODAuditLog(oldRcMap)
            AuditLogConfigUtil.logChanges(configuration, deletionChangesMade.newValues, deletionChangesMade.oldValues, Constants.AUDIT_LOG_DELETE, Constants.HYPHEN + ViewHelper.getMessage("reason.of.delay.change", Constants.DELETED, caseNumber))
        } else if (rcMap.flagIUD == Constants.AuditLog.INSERT_FLAG) {
            Map additionChangesMade = getAdditionChangesForRODAuditLog(rcMap)
            AuditLogConfigUtil.logChanges(configuration, additionChangesMade.newValues, additionChangesMade.oldValues, Constants.AUDIT_LOG_INSERT, Constants.HYPHEN + ViewHelper.getMessage("reason.of.delay.change", Constants.ADDED, caseNumber))
        } else if (rcMap.flagIUD == Constants.AuditLog.UPDATE_FLAG) {
            Map modificationChangesMade = getModificationChangesForRODAuditLog(oldRcMap, rcMap)
            AuditLogConfigUtil.logChanges(configuration, modificationChangesMade.newValues, modificationChangesMade.oldValues, Constants.AUDIT_LOG_UPDATE, Constants.HYPHEN + ViewHelper.getMessage("reason.of.delay.change", Constants.UPDATED, caseNumber))
        }
    }

    void runICSRScheduledConfigurations() throws Exception {
        List<IcsrProfileConfiguration> scheduledConfigurations = IcsrProfileConfiguration.listOfEligibleForExecute([],[]).list([readOnly: true])
        log.debug("Icsr active found profiles: ${scheduledConfigurations.size()}")
        if (scheduledConfigurations) {
            Sql sql = new Sql(getReportConnection())
            try {
                scheduledConfigurations.groupBy { it.tenantId }.each { map ->
                    log.debug("Starting for tenant: ${map.key}")
                    Tenants.withId(map.key as Integer) {
                        IcsrCaseQueue.'pva'.withNewTransaction { status ->
                            try {
                                //Only Process entries having max Modification date for each case number and version number
                                processCaseQueue()
                                List<IcsrCaseQueue> casesQueue = IcsrCaseQueue.'pva'.findAllByStatusNotEqual(IcsrCaseStatusEnum.DELETED)
                                if (!casesQueue) {
                                    log.debug("No icsr cases found in queue for tenant ${map.key}")
                                    return
                                }
                                log.info("Found icsr cases ${casesQueue.size()} for processing for tenant ${map.key}")
                                casesQueue.each { caseQueue ->
                                    map.value.each { profile ->
                                        if (profile.isJapanProfile) {
                                            List approvalNumberList = []
                                            sql.call("{?= call PKG_E2B_PROCESSING.F_GET_AUTH_PARAMS(?,?,?,?,?,?,?,?,?)}",
                                                    [Sql.resultSet(OracleTypes.CURSOR), caseQueue.caseId, caseQueue.versionNumber, caseQueue.tenantId, null, profile.recipientOrganization.organizationCountry, profile.authorizationTypes.join(","), 1, (profile.multipleReport == true) ? 1 : 0, 0]) { cursorResult ->
                                                approvalNumberList = MiscUtil.resultSetToList(cursorResult).collect {
                                                    [PROD_HASH_CODE: it.PROD_HASH_CODE, AUTH_ID: it.AUTH_ID, RPT_CATEGORY_ID: it.RPT_CATEGORY_ID]
                                                }
                                            }
                                            if (approvalNumberList.any { it.PROD_HASH_CODE == "-1" }) {
                                                new IcsrCaseProcessingQueue(caseQueue, profile, "-1", -1, -1, IcsrCaseStatusEnum.NOT_MULTI_REPORTABLE).'pva'.save([failOnError: true])
                                            } else {
                                                approvalNumberList.each {
                                                    new IcsrCaseProcessingQueue(caseQueue, profile, it.PROD_HASH_CODE, it.AUTH_ID as Long, it.RPT_CATEGORY_ID as Long, IcsrCaseStatusEnum.START).'pva'.save([failOnError: true])
                                                }
                                            }
                                        } else if (!caseQueue.flagLocalProcessing) {
                                            if (profile.deviceReportable) {
                                                sql.call("{?= call PKG_PVR_ICSR_ROUTINE.F_GET_PROD_REC_NUM(?,?,?,?)}",
                                                        [Sql.resultSet(OracleTypes.CURSOR), caseQueue.caseId, caseQueue.versionNumber, caseQueue.tenantId, 0]) { cursorResult ->
                                                    List cursorList = MiscUtil.resultSetToList(cursorResult)?.PROD_HASH_CODE
                                                    cursorList.each {
                                                        String prodHashCode = it
                                                        if (prodHashCode == "-1") {
                                                            new IcsrCaseProcessingQueue(caseQueue, profile, prodHashCode, -1, -1, IcsrCaseStatusEnum.NO_DEVICE).'pva'.save([failOnError: true])
                                                        } else {
                                                            new IcsrCaseProcessingQueue(caseQueue, profile, prodHashCode, -1, -1, IcsrCaseStatusEnum.START).'pva'.save([failOnError: true])
                                                        }
                                                    }
                                                }
                                            } else {
                                                new IcsrCaseProcessingQueue(caseQueue, profile).'pva'.save([failOnError: true])
                                            }
                                        }
                                    }
                                }
                                casesQueue*.setStatus(IcsrCaseStatusEnum.DELETED)
                                casesQueue*.'pva'*.save([failOnError: true])
                            } catch (Exception ex) {
                                log.error("Error while pushing new cases to processing queue for tenant ${map.key}", ex)
                                status.setRollbackOnly()
                            }
                        }
                    }
                }
            } finally {
                sql?.close()
            }
        }
    }

    @Transactional
    ExecutionStatus createExecutionStatusForSchedule(ReportConfiguration scheduledConfiguration) {
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: scheduledConfiguration.id, entityType: ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION, reportVersion: scheduledConfiguration.numOfExecutions + 1,
                startTime: System.currentTimeMillis(), nextRunDate: scheduledConfiguration.nextRunDate, owner: scheduledConfiguration.owner, reportName: scheduledConfiguration.reportName,
                attachmentFormats: scheduledConfiguration?.deliveryOption?.attachmentFormats, sharedWith: scheduledConfiguration?.allSharedUsers?.unique(), tenantId: scheduledConfiguration?.tenantId)
        executionStatus.frequency = configurationService.calculateFrequency(scheduledConfiguration)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

    @WithoutTenant
    void runICSRScheduledProcessing() {
        List<Long> eligibleProfileIds = findAllEligibleProfileIdsForExecution()
        if (!eligibleProfileIds) {
            return
        }
        List<Long> currentlyRunningIds = executorThreadInfoService.totalCurrentlyRunningIcsrIds
        List<Long> currentlyRunningConfigurations = ExecutionStatus.createCriteria().list([readOnly: true]) {
            projections {
                property('entityId')
            }
            or {
                if (currentlyRunningIds) {
                    and {
                        inList("id", currentlyRunningIds)
                        inList("entityType", [ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION])
                    }
                }
                and {
                    inList('executionStatus', [ReportExecutionStatusEnum.BACKLOG, ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.DELIVERING])
                    inList('entityType', [ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION])
                }
            }
        }
        log.trace("Eligible Profile ids: ${eligibleProfileIds} for execution")
        List<IcsrProfileConfiguration> scheduledConfigurations = IcsrProfileConfiguration.listOfEligibleForExecute(eligibleProfileIds, currentlyRunningConfigurations).list([readOnly: true])
        log.debug("Found Profiles to execute for qualification: ${scheduledConfigurations.size()}")
        if (scheduledConfigurations) {
            log.trace("Profiles to execute: ${scheduledConfigurations*.id}")
            scheduledConfigurations.groupBy { it.tenantId }.each { map ->
                Tenants.withId(map.key as Integer) {
                    List<ExecutionStatus> executionStatuses = map.value.collect { IcsrProfileConfiguration configuration ->
                        configuration.nextRunDate = new Date()
                        return createExecutionStatusForSchedule(configuration)
                    }
                    Integer threadPoolSize = (grailsApplication.config.getProperty('icsr.profile.executor.size', Integer, 3))
                    GParsPool.withPool(threadPoolSize, new Thread.UncaughtExceptionHandler() {
                        @Override
                        void uncaughtException(Thread failedThread, Throwable throwable) {
                            log.error("Error processing background profile thread ${failedThread.name}", throwable)
                        }
                    }, {
                        executionStatuses.eachParallel { ExecutionStatus executionStatus ->
                            User.withNewSession {
                                if (!(executionStatus.id in executorThreadInfoService.totalCurrentlyRunningIcsrIds)) {
                                    executeExecutionStatus(ExecutionStatus.read(executionStatus.id), Constants.ICSR_PROFILE)
                                }else if(executionStatus.id in executorThreadInfoService.totalCurrentlyRunningIcsrIds) {
                                    log.info("Already the Report is running whose executionstatus id : "+executionStatus.id)
                                }else {
                                    log.info("Current ICSR report queue exceeds max size, skipping adding new reports")
                                }
                            }
                        }
                    })
                }
            }
            log.debug("Completed scheduled profiles execution")
        }
    }

    @ReadOnly('pva')
    List<Long> findAllEligibleProfileIdsForExecution(){
        return IcsrCaseProcessingQueue.'pva'.createCriteria().list {
            projections {
                distinct('profileId')
            }
            eq('status', IcsrCaseStatusEnum.START)
        }.flatten()
    }

    Map getModificationChangesForRODAuditLog(Map oldRcMap, Map rcMap) {
        Map changesMade = [oldValues: [:], newValues: [:]]
        JSONObject jsonObject = (JSONObject) oldRcMap
        Late.withNewSession {
            changesMade.newValues = newRODChangeLog(rcMap)
            changesMade.oldValues = oldRODChangeLog(jsonObject)
        }
        changesMade
    }

    private Map newRODChangeLog(rcMap) {
        return [
                Late                     : getRODAuditLogValueFromMap(rcMap, 'late'),
                Primary                  : Boolean.parseBoolean(rcMap.get('flagPrimary')),
                RootCause                : getRODAuditLogValueFromMap(rcMap, 'rootCause'),
                ResponsibleParty         : getRODAuditLogValueFromMap(rcMap, 'responsibleParty'),
                RootCauseClassification  : getRODAuditLogValueFromMap(rcMap, 'rootCauseClass'),
                RootCauseSubCategoryValue: getRODAuditLogValueFromMap(rcMap, 'rootCauseSubCategory'),
                CorrectiveAction         : getRODAuditLogValueFromMap(rcMap, 'correctiveAction'),
                PreventativeAction       : getRODAuditLogValueFromMap(rcMap, 'preventativeAction'),
                CorrectiveDate           : getRODAuditLogValueFromMap(rcMap, 'correctiveDate'),
                PreventativeDate         : getRODAuditLogValueFromMap(rcMap, 'preventiveDate'),
                Actions                  : getRODAuditLogValueFromMap(rcMap, 'actions'),
                Summary                  : getRODAuditLogValueFromMap(rcMap, 'summary'),
                Investigation            : getRODAuditLogValueFromMap(rcMap, 'investigation')
        ]
    }

    private Map oldRODChangeLog(jsonObject) {
        if (!jsonObject) return null
        return [
                Late                     : jsonObject.get('lateValue'),
                Primary                  : jsonObject.get('primaryFlag').equals("1"),
                RootCause                : getRODAuditLogValueFromJson(jsonObject, 'rootCauseValue'),
                ResponsibleParty         : getRODAuditLogValueFromJson(jsonObject, 'responsiblePartyValue'),
                RootCauseClassification  : getRODAuditLogValueFromJson(jsonObject, 'rootCauseClassValue'),
                RootCauseSubCategoryValue: getRODAuditLogValueFromJson(jsonObject, 'rootCauseSubCategoryValue'),
                CorrectiveAction         : getRODAuditLogValueFromJson(jsonObject, 'correctiveActionValue'),
                PreventativeAction       : getRODAuditLogValueFromJson(jsonObject, 'preventiveActionValue'),
                CorrectiveDate           : getRODAuditLogValueFromJson(jsonObject, 'correctiveDate'),
                PreventativeDate         : getRODAuditLogValueFromJson(jsonObject, 'preventiveDate'),
                Actions                  : getRODAuditLogValueFromJson(jsonObject, 'actions'),
                Summary                  : getRODAuditLogValueFromJson(jsonObject, 'summary'),
                Investigation            : getRODAuditLogValueFromJson(jsonObject, 'investigation')
        ]
    }

    private Map emptyRODChangeLog() {
        return [
                Late                     : null,
                Primary                  : null,
                RootCause                : null,
                ResponsibleParty         : null,
                RootCauseClassification  : null,
                RootCauseSubCategoryValue: null,
                CorrectiveAction         : null,
                PreventativeAction       : null,
                CorrectiveDate           : null,
                PreventativeDate         : null,
                Actions                  : null,
                Summary                  : null,
                Investigation            : null
        ]
    }

    Map getDeletionChangesForRODAuditLog(Map oldRcMap) {
        Map changesMade = [oldValues: [:], newValues: [:]]
        JSONObject jsonObject = (JSONObject) oldRcMap
        changesMade.newValues = emptyRODChangeLog()
        changesMade.oldValues = oldRODChangeLog(jsonObject)
        changesMade
    }


    Map getAdditionChangesForRODAuditLog(Map rcMap) {
        Map changesMade = [oldValues: [:], newValues: [:]]
        Late.withNewSession {
            changesMade.newValues = newRODChangeLog(rcMap)
            changesMade.oldValues = emptyRODChangeLog()
        }
        changesMade
    }

    static String getRODAuditLogValueFromJson(JSONObject jsonObject, String key){
        String value = jsonObject.get(key) ? jsonObject.get(key) : null
        value
    }

    static final List<String> ROD_DOMAIN_KEYS = ['late', 'rootCause', 'responsibleParty', 'rootCauseClass', 'rootCauseSubCategory',
                                                 'correctiveAction', 'preventativeAction']

    static String getRODAuditLogValueFromMap(Map rcMap, String key){
        String value
        if(key in ROD_NON_DOMAIN_KEYS) {
            value = (rcMap.get(key) != null && rcMap.get(key).length() > 0) ? rcMap.get(key) : null
        }else{
            String idVal = (rcMap.get(key)!=null && rcMap.get(key).length()>0) ? rcMap.get(key) : null
            if(idVal.equals(null)) {
                value = idVal
            } else {
                switch (key) {
                    case 'late':
                        value = Late.get(Long.valueOf(idVal))?.textDesc
                        break
                    case 'rootCause':
                        value = RootCause.get(Long.valueOf(idVal))?.textDesc
                        break
                    case 'responsibleParty':
                        value = ResponsibleParty.get(Long.valueOf(idVal))?.textDesc
                        break
                    case 'rootCauseClass':
                        value = RootCauseClassification.get(Long.valueOf(idVal))?.textDesc
                        break
                    case 'rootCauseSubCategory':
                        value = RootCauseSubCategory.get(Long.valueOf(idVal))?.textDesc
                        break
                    case 'correctiveAction':
                        value = CorrectiveAction.get(Long.valueOf(idVal))?.textDesc
                        break
                    case 'preventativeAction':
                        value = PreventativeAction.get(Long.valueOf(idVal))?.textDesc
                        break
                }
            }

        }
        value
    }

    void processCaseQueue() {
        IcsrCaseQueue.'pva'.withNewTransaction { status ->
            List<IcsrCaseQueue> insertCaseQueue = IcsrCaseQueue.'pva'.findAllByStatusNotEqual(IcsrCaseStatusEnum.DELETED)
            // Group entries by caseId, versionNumber, and tenantId
            def groupedEntries = insertCaseQueue.groupBy { [it.caseId, it.versionNumber, it.tenantId] }
            groupedEntries.each { key, entries ->
                // Find the max modification date within each group
                Date maxModificationDate = entries*.caseModificationDate.max()
                entries.each { entry ->
                    // Collect entries if the entry has a modification date earlier than the max date and case is locked or not locked
                    if (entry.caseModificationDate < maxModificationDate) {
                        try {
                            entry.status = IcsrCaseStatusEnum.DELETED
                            entry.'pva'.save([failOnError: true])
                        } catch (Exception e) {
                            log.error("Error while updating entries: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }

    @WithoutTenant
    void executeInboundCompliance() {
        Tenant.findAllByActive(true).each {
            int slots = executorThreadInfoService.availableSlotsForInboundsGeneration()
            if (slots < 1) {
                log.info('No slots available for inbound Compliance')
                return
            }
            String startDate
            String endDate = new Date().format(SqlGenerationService.DATE_FMT).toString()
            int isICInitializedGlobally = 0
            List<InboundCompliance> inboundComplianceInstances = []
            InboundInitialConfiguration inboundInitialConfiguration = InboundInitialConfiguration?.first()
            if(inboundInitialConfiguration) {
                //If Initialization set from Control Panel
                if (inboundInitialConfiguration.isICInitialize) {
                    isICInitializedGlobally = 1
                    startDate = DateUtil.StringFromDate(inboundInitialConfiguration.startDate, SqlGenerationService.DATE_FMT, userService.currentUser?.preference?.timeZone)
                    List<InboundCompliance> inboundComplianceList = InboundCompliance.findAllByIsDeletedAndIsDisabled(false, false)
                    inboundComplianceInstances.addAll(inboundComplianceList)
                } else {
                    //If Initialization set from (save & Initialize) Button
                    List<InboundCompliance> inboundComplianceLists = InboundCompliance.findAllByIsICInitializeAndIsDeletedAndIsDisabled(true, false, false)
                    if (inboundComplianceLists != null && inboundComplianceLists.size() > 0) {
                        inboundComplianceInstances.addAll(inboundComplianceLists)
                    } else {
                        // When user has not initialize from Control Panel or not from Configuration Page then we run only for the sender which exist in mart
                        Sql sql = new Sql(getReportConnection())
                        try {
                            List<String> results = []
                            boolean isSenderListExist = true
                            sql.call("{? = call PKG_INBOUND_COMPLIANCE.F_RETURN_SENDER_LIST()}", [Sql.VARCHAR]) { String sqlInfo ->
                                //No need to Run if there is no list avaliable in the DB
                                if(!sqlInfo){
                                    isSenderListExist = false
                                    log.info("Sender List is empty --> PKG_INBOUND_COMPLIANCE.F_RETURN_SENDER_LIST()")
                                }else {
                                    results = sqlInfo?.split(",").toList()
                                }
                            }
                            if(!isSenderListExist) {
                                return
                            }
                            List<InboundCompliance> inboundComplianceList = null
                            results?.collate(999)?.each {
                                inboundComplianceList = InboundCompliance.findAllBySenderNameInListAndIsDeletedAndIsDisabled(it, false, false)
                            }
                            inboundComplianceInstances.addAll(inboundComplianceList)
                        } catch (Exception e) {
                            e.printStackTrace()
                            log.info("!!...............Failed while creating Inbound Compliance List ..........!! :: ${e.message}")
                        } finally {
                            sql?.close()
                        }
                    }
                }
                inboundComplianceInstances*.discard()
                GParsPool.withPool(slots) {
                    inboundComplianceInstances?.collectParallel { InboundCompliance inboundComplianceInstance ->
                        if (inboundComplianceInstance && !(inboundComplianceInstance.id in executorThreadInfoService.totalCurrentlyGeneratingInbounds)) {
                            User.withNewSession {
                                inboundComplianceInstance = InboundCompliance.get(inboundComplianceInstance.id)

                                final Thread currentThread = Thread.currentThread()
                                final String initialThreadName = currentThread.name
                                currentThread.setName("RxThread-IC-" + inboundComplianceInstance.id + '-' + inboundComplianceInstance.senderName)
                                executorThreadInfoService.addToTotalCurrentlyGeneratingInbounds(inboundComplianceInstance.id)

                                Tenants.withId(inboundComplianceInstance.tenantId as Integer) {
                                    String senderName = inboundComplianceInstance?.senderName
                                    int isICInitialize = isICInitializedGlobally ?: (inboundComplianceInstance.isICInitialize ? 1 : 0)
                                    if (!startDate) {
                                        List dateRanges = inboundComplianceInstance.globalDateRangeInbound.getReportStartAndEndDate()
                                        startDate = dateRanges[0]?.format(SqlGenerationService.DATE_FMT).toString()
                                        endDate = dateRanges[1]?.format(SqlGenerationService.DATE_FMT).toString()
                                    }
                                    String dateRangeType = inboundComplianceInstance?.dateRangeType?.name
                                    String sourceType
                                    if (inboundComplianceInstance.sourceProfile.sourceProfileTypeEnum == SourceProfileTypeEnum.SINGLE) {
                                        sourceType = inboundComplianceInstance.sourceProfile.isCentral ? "CENTRAL" : "AFF_${inboundComplianceInstance.sourceProfile.sourceId}"
                                    } else {
                                        Set<SourceProfile> sourceProfiles = SourceProfile.sourceProfilesForUser(inboundComplianceInstance.owner) - SourceProfile.fetchAllDataSource()
                                        sourceType = (sourceProfiles.size() == 0) ? "CENTRAL" : sourceProfiles.collect {
                                            it.isCentral ? "CENTRAL" : "AFF_${it.sourceId}"
                                        }.join(",")
                                    }
                                    int productFilterFlag = (inboundComplianceInstance?.productSelection || inboundComplianceInstance?.validProductGroupSelection) ? 1 : 0
                                    int studyFilterFlag = inboundComplianceInstance?.studySelection ? 1 : 0
                                    int supectProductCheck = inboundComplianceInstance?.suspectProduct ? 1 : 0

                                    inboundComplianceInstance.executing = true
                                    inboundComplianceInstance = (InboundCompliance) CRUDService.instantUpdateWithoutAuditLog(inboundComplianceInstance)
                                    ExecutedInboundCompliance executedInboundCompliance = executedConfigurationService.executeInboundForConfiguration(inboundComplianceInstance)
                                    executedInboundCompliance.status = ReportExecutionStatusEnum.GENERATING
                                    executedInboundCompliance.startTime = System.currentTimeMillis()
                                    List<User> shareNotificationToUsers = [executedInboundCompliance.owner, userService.getUser()]
                                            .findAll { it != null }.unique()
                                    final Sql sql2 = new Sql(getReportConnection())
                                    try {
                                        String cleaningUpGttTable = ("begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_tabulation''); end;';" +
                                                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_params''); end;';" +
                                                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_fields''); end;';" +
                                                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_filter_key_values''); end;';" +
                                                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_TABULATION_MEASURES''); end;';" +
                                                "delete from GTT_QUERY_DETAILS; delete from GTT_QUERY_SETS; END;")
                                        List<QueryCompliance> queryComplianceList = inboundComplianceInstance?.queriesCompliance.sort {
                                            it.allowedTimeframe
                                        }
                                        List<ExecutedQueryCompliance> executedQueryComplianceList = executedInboundCompliance?.executedQueriesCompliance.sort {
                                            it.allowedTimeframe
                                        }
                                        queryComplianceList?.eachWithIndex { queryCompliance, index ->
                                            Boolean reAssesQueryFlag = (queryCompliance.usedQuery && queryCompliance.usedQuery?.queryType == QueryTypeEnum.QUERY_BUILDER && queryCompliance.usedQuery?.reassessForProduct)
                                            int reAssesListednessFlag = reAssesQueryFlag ? 1 : 0
                                            Long exQueryComplianceId = executedQueryComplianceList[index].executedQuery?.id
                                            String insertStatement = ""
                                            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_INBOUND_COMPLIANCE', '1');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IC_SENDER_NAME', '${senderName?.replaceAll("(?i)'", "''")}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_IC_INITIALIZE','${isICInitialize}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EX_INBOUND_COMPLIANCE_ID',${executedInboundCompliance?.id});" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EX_QUERY_ID',${exQueryComplianceId});" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE','${startDate}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE','${endDate}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATE_RANGE_TYPE','${dateRangeType}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SOURCE_TYPE','${sourceType}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_FILTER_FLAG','${productFilterFlag}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('STUDY_FILTER_FLAG','${studyFilterFlag}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUSPECT_PRODUCT_CHECK','${supectProductCheck}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REASSESS_LISTEDNESS_FLAG','${reAssesListednessFlag}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IC_CRITERIA_NAME','${queryCompliance?.criteriaName}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IC_ALLOWED_TIMEFRAME','${queryCompliance?.allowedTimeframe}');" +
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_MULTI_INGREDIENT','${executedInboundCompliance?.isMultiIngredient ? 1:0}');"+
                                                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_WHO_DRUGS','${executedInboundCompliance?.includeWHODrugs ? 1:0}');";

                                            if (inboundComplianceInstance.validProductGroupSelection) {
                                                JSON.parse(inboundComplianceInstance.validProductGroupSelection).each {
                                                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
                                                }
                                            }
                                            if (productFilterFlag == 1) // Ids used in product filter
                                            {
                                                List<Map> productDetails = MiscUtil?.getProductDictionaryValues(inboundComplianceInstance?.productSelection)
                                                List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
                                                productDetails.eachWithIndex { Map entry, int i ->
                                                    int keyId = productViewsList.get(i).keyId
                                                    entry.each { k, v ->
                                                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,'$k','${v?.replaceAll("(?i)'", "''")}'); "
                                                    }
                                                }
                                                if (inboundComplianceInstance?.productSelection)
                                                    JSON.parse(inboundComplianceInstance?.productSelection)["100"]?.each {
                                                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (299,'${it.id}','${it.name?.replaceAll("(?i)'", "''")}'); "
                                                    }
                                            }
                                            if (studyFilterFlag == 1) // Ids used in study filter
                                            {
                                                List<Map> studyDetails = MiscUtil?.getStudyDictionaryValues(inboundComplianceInstance?.studySelection)
                                                studyDetails.eachWithIndex { Map entry, int i ->
                                                    entry.each { k, v ->
                                                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (${(i + 5)},'$k','${v?.replaceAll("(?i)'", "''")}'); "
                                                    }
                                                }
                                            }
                                            insertStatement += "END;"
                                            sql2.execute(cleaningUpGttTable)
                                            sql2.execute(insertStatement)

                                            log.debug("Insert for transferring data : ${insertStatement.toString()}")

                                            Set<ParameterValue> poiInputParams = []  //inboundComplianceInstance?.poiInputsParameterValues ?:[]
                                            Locale locale = inboundComplianceInstance.owner.preference.locale
                                            sqlGenerationService.setSqlSessionContext(sql2, locale)
                                            boolean nonValidCases = false
                                            String insertStatement2 = sqlGenerationService.getInsertStatementsToInsert(queryCompliance.usedQuery, null, queryCompliance.usesQueryValueLists, poiInputParams, locale, nonValidCases, inboundComplianceInstance.owner, false);
                                            sql2.execute(insertStatement2)
                                            log.debug("Insert for transferring query set and details : ${insertStatement2.toString()}")
                                            sql2.call("{call PKG_INBOUND_COMPLIANCE.P_MAIN}")
                                            if (queryComplianceList.size() - 1 == index) {
                                                sql2.call("{call PKG_INBOUND_COMPLIANCE.P_POP_IC_CASE_TABLE_NL(?)}", [inboundComplianceInstance.senderName])
                                            }
                                        }
                                        executedInboundCompliance.status = ReportExecutionStatusEnum.COMPLETED
                                        executedInboundCompliance.endTime = System.currentTimeMillis()
                                        executedInboundCompliance = (ExecutedInboundCompliance) CRUDService.instantUpdateWithoutAuditLog(executedInboundCompliance)
                                        // Notification for Success
                                        shareNotificationToUsers.each{
                                            notificationService.addNotification(it,
                                                    'app.notification.inboundCompliance.generated', executedInboundCompliance.senderName, executedInboundCompliance.id, NotificationLevelEnum.INFO, NotificationApp.INBOUNDCOMPLIACE)
                                        }
                                    } catch (Exception e) {
                                        executedInboundCompliance.status = ReportExecutionStatusEnum.ERROR
                                        executedInboundCompliance.endTime = System.currentTimeMillis()
                                        List messages = MiscUtil.getExceptionMessage(e)
                                        executedInboundCompliance.message = messages[0]
                                        executedInboundCompliance.errorDetails = messages[1]
                                        executedInboundCompliance = (ExecutedInboundCompliance) CRUDService.instantUpdateWithoutAuditLog(executedInboundCompliance)
                                        log.info("!!...............Error ...........DELIVERING WHILE INBOUND COMPLIANCE..........!! :: ${e.message}")
                                        // Notification for Error
                                        shareNotificationToUsers.each{
                                            notificationService.addNotification(it,
                                                    'app.notification.inboundCompliance.failed', executedInboundCompliance.senderName, executedInboundCompliance.id, NotificationLevelEnum.ERROR, NotificationApp.INBOUNDCOMPLIACE)
                                        }
                                    } finally {
                                        executorThreadInfoService.removeFromTotalCurrentlyGeneratingInbounds(inboundComplianceInstance.id)
                                        currentThread.setName(initialThreadName)
                                        inboundComplianceInstance.executing = false
                                        inboundComplianceInstance.isICInitialize = false
                                        inboundComplianceInstance.numOfICExecutions++
                                        inboundComplianceInstance.lastRunDate = new Date()
                                        inboundComplianceInstance = (InboundCompliance) CRUDService.instantUpdateWithoutAuditLog(inboundComplianceInstance)
                                        sql2?.close()
                                    }
                                }
                                return true
                            }
                        }
                    }
                    try {
                        if(isICInitializedGlobally){
                            inboundInitialConfiguration.isICInitialize = false
                            CRUDService.instantSaveWithoutAuditLog(inboundInitialConfiguration)
                        }
                    }catch(Exception exp) {
                        exp.printStackTrace()
                        log.error("While updating the inboundInitialConfiguration getting failed :: ${exp.message}")
                    }
                }
            }
        }
    }

}



