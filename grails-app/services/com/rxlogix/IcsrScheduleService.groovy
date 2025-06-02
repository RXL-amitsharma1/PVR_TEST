package com.rxlogix

import com.hazelcast.cp.lock.FencedLock
import com.hazelcast.cp.CPSubsystem
import com.rxlogix.config.*
import com.rxlogix.customException.CaseScheduleException
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.customException.InvalidCaseInfoException
import com.rxlogix.dto.ReportMeasures
import com.rxlogix.enums.DistributionChannelEnum
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.mapping.IcsrCaseMessageQueue
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import com.rxlogix.mapping.IcsrCaseProcessingQueueHist
import com.rxlogix.mapping.IcsrManualLockedCase
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.grails.core.exceptions.GrailsRuntimeException
import com.rxlogix.mapping.IcsrCaseLocalCpData
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.SQLException
import java.sql.Timestamp
import com.rxlogix.util.FileUtil
import org.apache.commons.io.FilenameUtils
import grails.util.Holders
import java.sql.Clob
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import java.util.concurrent.TimeUnit
import com.rxlogix.admin.AdminIntegrationApiService
import groovyx.net.http.Method
import org.springframework.transaction.annotation.Propagation

class IcsrScheduleService {

    static transactional = false

    def sqlGenerationService
    def CRUDService
    def queryService
    def templateService
    def utilService
    def executorThreadInfoService
    def executedConfigurationService
    def grailsApplication
    def reportResultService
    def icsrReportService
    def notificationService
    def dataSource_pva
    def userService
    def dynamicReportService
    def e2BAttachmentService
    def reportExecutorService
    def icsrProfileAckService
    def hazelService
    AdminIntegrationApiService adminIntegrationApiService

    static final CASE_RECEIPT_DATE = "CM_INIT_REPT_DATE"
    final static String PVCM_LOCAL_CP_ENDPOINT = "api/icsr/localCpData"


    ExecutedReportConfiguration executeSchedulesForConfiguration(IcsrProfileConfiguration scheduledConfiguration, ExecutionStatus executionStatus) throws Exception {
        ExecutedReportConfiguration executedConfiguration = null
        try {
            List casesToProcess = findCasesToProcessForConfig(scheduledConfiguration.id)
            if (!casesToProcess) {
                log.warn("No cases found for processing for Icsr Profile Configuration: (ID: ${scheduledConfiguration.id})")
                executionStatus.executionStatus = ReportExecutionStatusEnum.COMPLETED
                CRUDService.updateWithoutAuditLog(executionStatus)
                return null
            }
            log.trace("Icsr Profile ${scheduledConfiguration.id} Cases found for execution: ${casesToProcess*.caseId} ")
            executedConfiguration = executeReportJobSchedule(scheduledConfiguration, executionStatus, casesToProcess)
            executionStatus.executedEntityId = executedConfiguration?.id
            executionStatus.executionStatus = ReportExecutionStatusEnum.DELIVERING
            CRUDService.updateWithoutAuditLog(executionStatus)
        } catch (Exception e) {
            log.error("Error while generating report configuration", e)
            saveExceptionMessage(e, executionStatus, ReportExecutionStatusEnum.ERROR)
            throw e
        }
        return executedConfiguration
    }

    private void saveExceptionMessage(Exception ex, ExecutionStatus executionStatus, ReportExecutionStatusEnum status) {
        Long queryId = 0L
        Long templateId = 0L
        String sectionName = ''
        String stackTrace = ex.stackTrace.toString()
        String querySql = ''
        String headerSql = ''
        String reportSql = ''
        if (ex instanceof ExecutionStatusException) {
            queryId = ex.queryId
            templateId = ex.templateId
            sectionName = ex.sectionName
            stackTrace = ex.errorCause
            querySql = ex.querySql
            headerSql = ex.headerSql
            reportSql = ex.reportSql
        }
        ExecutionStatus.withNewSession {
            if (executionStatus && executionStatus.refresh()) {
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

    private ExecutedReportConfiguration executeReportJobSchedule(ReportConfiguration configuration, ExecutionStatus executionStatus, List<IcsrCaseProcessingQueue> casesToProcess) throws Exception {
        Long startTime
        Date scheduledDate = executionStatus.nextRunDate
        ExecutedReportConfiguration executedConfiguration = null
        ReportConfiguration lockedConfiguration = ReportConfiguration.get(configuration.id)

        log.info("Executing Icsr Profile Configuration: (ID: ${configuration.id})")

        try {
            if (lockedConfiguration.isEnabled) {
                // Validating Configuration once again before using
                if (!lockedConfiguration.validate()) {
                    throw new ValidationException("Validation Exception in Icsr Profile Configuration", lockedConfiguration.errors)
                }
                lockedConfiguration.executing = true
                CRUDService.instantUpdateWithoutAuditLog(lockedConfiguration)
                executedConfiguration = executedConfigurationService.createExecutedConfiguration(lockedConfiguration, scheduledDate, true)
                CRUDService.instantSaveWithoutAuditLog(executedConfiguration)
                CRUDService.instantUpdateWithoutAuditLog(lockedConfiguration)
                //Adding ExecutionId for currently Processing cases
                updateExecutionIdForCases(executedConfiguration.id, casesToProcess)
                startTime = System.currentTimeMillis()
                executeIcsrSchedule(lockedConfiguration, executedConfiguration, startTime, casesToProcess)
                log.info("Execution of Icsr Profile Configuration took ${executedConfiguration.totalExecutionTime}ms for [C:${lockedConfiguration?.id}, EC: ${executedConfiguration.id}]")
            }
        }
        catch (Exception e) {
            log.error("Unable to finish running Icsr lockedConfiguration.id=${lockedConfiguration.id}", e)
            Exception e1
            if (!(e instanceof ExecutionStatusException)) {
                String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
                StringWriter sw = new StringWriter()
                e.printStackTrace(new PrintWriter(sw))
                String exceptionAsString = sw.toString()
                if (!message) {
                    message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
                }
                e1 = new ExecutionStatusException(errorMessage: message, errorCause: exceptionAsString)
                throw e1
            } else {
                throw e
            }
        }
        return executedConfiguration
    }

    private void dumpCaseForScheduledProcessing(Sql sql, List<IcsrCaseProcessingQueue> cases) {
        StringBuilder casesSql = new StringBuilder("Begin\n")
        cases.each {
            casesSql.append("insert into GTT_VERSIONS_BASE (TENANT_ID,CASE_ID,VERSION_NUM,PROD_HASH_CODE,AUTH_ID,RPT_CATEGORY_ID) values(${it.tenantId},${it.caseId},${it.versionNumber},'${it.prodHashCode}',${it.approvalId},${it.reportCategoryId});\n")
        }
        casesSql.append("END;")
        log.trace("=========== Icsr queries cases insert =========")
        log.trace(casesSql.toString())
        sql.execute(casesSql.toString());
    }


    private void executeIcsrSchedule(ReportConfiguration configuration, ExecutedReportConfiguration executedConfiguration, Long startTime, List<IcsrCaseProcessingQueue> casesToProcess) {
        Long templateId = 0L
        Long queryId = 0L
        String sectionName = ""
        Sql sql
        try {
            Locale locale = executedConfiguration.locale
            sql = new Sql(utilService.getReportConnection())
            sqlGenerationService.setCurrentSqlInfo(sql, locale)
            int totalSections = executedConfiguration.executedTemplateQueriesForProcessing.size()
            boolean isIcsrProfile = (executedConfiguration instanceof ExecutedIcsrProfileConfiguration)
            dumpCaseForScheduledProcessing(sql, casesToProcess)
            sql.execute("insert into GTT_ICSR_CONSTANTS (EXECUTION_ID,PROFILE_ID) values(?,?)", [executedConfiguration.id, configuration.id])
            executedConfiguration.executedTemplateQueriesForProcessing.eachWithIndex { it, index ->
                long sectionStartTime = System.currentTimeMillis()
                templateId = it.executedTemplateId
                queryId = it.executedQueryId
                sectionName = (it?.title) ? (it.title) : (it.executedConfiguration.reportName)
                if (isIcsrProfile && index == totalSections - 1) {
                    log.debug("Executing last section of the report: ${executedConfiguration.reportName}, ${it.id}")
                    sql.call("{? = call PKG_CREATE_REPORT_SQL.F_SET_FLAG_LAST_SECTION(1)}", [Sql.INTEGER]) { Integer result ->
                        if (!result) {
                            throw new GrailsRuntimeException("Exception at DB end while marking section as last section execution for ${executedConfiguration.reportName} so failing section")
                        }
                    }
                }
                Long templateQueryId = configuration.templateQueries.get(index).id
                executeCasesAgainstIcsrQuery(sql, it, locale, templateQueryId)
                log.info("Execution of Icsr TemplateQuery took ${System.currentTimeMillis() - sectionStartTime}ms for [C:${executedConfiguration.id}, ExTemplateQuery: ${it.id}, T:${templateId}, Q:${queryId}]")

            }
            //Need to call after calling all the sections.
            sql.call('{call PKG_E2B_PROCESSING.P_UPDATE_PROFILE_QUEUE(8)}')

            executedConfiguration.status = ReportExecutionStatusEnum.COMPLETED
            executedConfiguration.totalExecutionTime = (System.currentTimeMillis() - startTime)
            CRUDService.instantUpdateWithoutAuditLog(executedConfiguration)
            //todo: currently commenting audit log for ICSR jobs. Need to check performance impact and implementation in future.
            //createAutoScheduleAuditLog(casesToProcess)
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
            throw e1
        } finally {
            checkIfCasesQualifiedForProfile(configuration,casesToProcess)
            checkIfCasesProcessedForAllProfiles(casesToProcess)
            sqlGenerationService.cleanGttCsvTables(executedConfiguration, sql)
            sql?.close()
        }
    }

    public void checkIfCasesQualifiedForProfile(ReportConfiguration configuration, List<IcsrCaseProcessingQueue> casesToProcess) {
        IcsrCaseProcessingQueue.'pva'.withNewTransaction {
            List<Long> caseQueueIds = casesToProcess*.id
            def qualifiedCases = IcsrCaseProcessingQueue.'pva'.findAllByIdInListAndStatusAndProfileId(caseQueueIds, IcsrCaseStatusEnum.QUALIFIED, configuration.id)
            if (qualifiedCases) {
                createLocalCpProfileEntries(configuration, qualifiedCases)
            }
        }
    }

    public void checkIfCasesProcessedForAllProfiles(List<IcsrCaseProcessingQueue> casesToProcess) {
        IcsrCaseProcessingQueue.'pva'.withNewTransaction {
            casesToProcess.each { caseQueue ->
                if (!IcsrCaseProcessingQueue.'pva'.countByCaseQueueIdAndStatusInList(caseQueue.caseQueueId, [IcsrCaseStatusEnum.START, IcsrCaseStatusEnum.DB_INPROGRESS, IcsrCaseStatusEnum.DB_IN_PROGRESS])) {
                    log.info("${caseQueue.caseId} has Completed its processing, Calculating its Earliest Due Date...")
                    calculateDueDate(caseQueue.caseQueueId, caseQueue.caseId, caseQueue.versionNumber, caseQueue.tenantId, caseQueue.caseModificationDate)
                }
            }
        }
    }

    @WithoutTenant
    void generateCasesResultData() {
        Tenant.findAllByActive(true).each {
            int slots = executorThreadInfoService.availableSlotsForCasesGeneration()
            if (slots < 1) {
                log.debug('No slots available for generation of cases data')
                return
            }
            List<IcsrCaseProcessingQueue> casesToProcess = findCasesEligibleToGenerateForTenant(it.id.toInteger(), slots, executorThreadInfoService.totalCurrentlyGeneratingCases)
            log.info("IcsrCaseGenerateDataJob ${slots} Available : All Qualified cases for process "+casesToProcess.size())
            casesToProcess.collect { executorThreadInfoService.addToTotalCurrentlyGeneratingCases(it.id) }
            GParsPool.withPool(slots) {
                casesToProcess?.eachParallel { IcsrCaseProcessingQueue icsrCaseProcessingQueue ->
                    if (icsrCaseProcessingQueue) {
                        final Thread currentThread = Thread.currentThread()
                        final String initialThreadName = currentThread.name
                        currentThread.setName("RxThread-CR-" + icsrCaseProcessingQueue.id + '-' + icsrCaseProcessingQueue.caseNumber)
                        log.info("IcsrCaseGenerateDataJob Generating Case Job Started For Thread = "+currentThread.getName())
                        log.info("IcsrCaseGenerateDataJob Queue Id = "+icsrCaseProcessingQueue.id)
                        IcsrCaseTracking icsrCaseTrackingInstance = null
                        Tenants.withId(icsrCaseProcessingQueue.tenantId as Integer) {
                            try {
                                User.withNewSession {
                                    icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                                    if (!icsrCaseTrackingInstance) {
                                        log.error("Unable to find entry in Case Tracking View for ${icsrCaseProcessingQueue.caseNumber} and Queue Id : ${icsrCaseProcessingQueue.id}")
                                        throw new ExecutionStatusException("No Case Entry found in Case Tracking view", "No Case Entry found in Case Tracking view")
                                    }
                                    Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
                                    ExecutedIcsrProfileConfiguration executedConfiguration = ExecutedIcsrProfileConfiguration.read(icsrCaseProcessingQueue.executionId)
                                    ExecutedTemplateQuery executedTemplateQuery = (ExecutedIcsrTemplateQuery) ExecutedTemplateQuery.read(icsrCaseProcessingQueue.executedTemplateQueryId)
                                    if (executedConfiguration && executedTemplateQuery) {
                                        processDataForCase(executedConfiguration, executedTemplateQuery, icsrCaseProcessingQueue)
                                        //To check if case generation is successful or not, refreshing the case processing queue object
                                        IcsrCaseProcessingQueue.'pva'.withNewSession {
                                            icsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.read(icsrCaseProcessingQueue.id)
                                        }
                                        log.info("IcsrCaseGenerateDataJob Case status after generation process end = "+icsrCaseProcessingQueue)
                                        //todo: currently commenting audit log for ICSR jobs. Need to check performance impact and implementation in future.
                                        /*IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                                        Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                                        AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                                                , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.generated", icsrCaseTrackingInstance?.caseNumber, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance?.recipient))*/
                                        if (executedConfiguration.autoTransmit && icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.GENERATED) {
                                            Boolean needToCheckPreviousVersion = grailsApplication.config.getProperty('pvr.icsr.enforce.transmission.in.version.sequence', Boolean)
                                            if (needToCheckPreviousVersion && checkPreviousVersionIsTransmitted(icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber.intValue(), executedConfiguration.reportName, executedConfiguration.recipientOrganizationName, executedTemplateQuery.executedTemplate.originalTemplateId)) {
                                                transmitCase(executedConfiguration, executedTemplateQuery.id, icsrCaseProcessingQueue)
                                            } else if (!needToCheckPreviousVersion) {
                                                transmitCase(executedConfiguration, executedTemplateQuery.id, icsrCaseProcessingQueue)
                                            }
                                        } else if(executedTemplateQuery.distributionChannelName == DistributionChannelEnum.PAPER_MAIL && executedConfiguration.autoSubmit && icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.GENERATED){
                                            String timeZoneId = executedConfiguration.preferredTimeZone ?: "UTC"
                                            Date submissionDate = new Date()
                                            Date localDate = DateUtil.covertToDateWithTimeZone(submissionDate, Constants.DateFormat.NO_TZ, timeZoneId)
                                            icsrProfileAckService.submitCase(executedConfiguration, executedTemplateQuery.id, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, submissionDate, "Application", localDate, timeZoneId)
                                        }
                                    }
                                }
                            } catch (ExecutionStatusException e) {
                                log.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${e.message}")
                                try {
                                    updateErrorForCase(icsrCaseProcessingQueue, e, icsrCaseTrackingInstance)
                                } catch (ex) {
                                    log.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                                }
                            } finally {
                                executorThreadInfoService.removeFromTotalCurrentlyGeneratingCases(icsrCaseProcessingQueue.id)
                                log.info("IcsrCaseGenerateDataJob Job Completed for "+currentThread.getName())
                                log.info("IcsrCaseGenerateDataJob Id exists in ETIS = "+(icsrCaseProcessingQueue.id in executorThreadInfoService.totalCurrentlyGeneratingCases))
                                currentThread.setName(initialThreadName)
                            }
                        }
                    }
                }
            }
        }
    }

    void makeManualCasesReadyForGeneration() {
        Sql sql = new Sql(utilService.getReportConnection())
        try {
            IcsrManualLockedCase.'pva'.list([max: grailsApplication.config.getProperty('icsr.case.manual.process.batch.size', Integer)]).groupBy {
                it.tenantId
            }.each { grp ->
                Tenants.withId(grp.key) {
                    grp.value.each { IcsrManualLockedCase icsrManualLockedCase ->
                        try {
                            IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.findByReportName(icsrManualLockedCase.profileName)
                            if (icsrManualLockedCase.flagNullification == 0) {
                                // Case will processed as usual
                                IcsrCaseProcessingQueue caseObj = IcsrCaseProcessingQueue.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrManualLockedCase.exIcsrTemplateQueryId, icsrManualLockedCase.caseNumber, icsrManualLockedCase.versionNumber)
                                if (!caseObj) {
                                    log.error("Case was not added manually for $icsrManualLockedCase.exIcsrTemplateQueryId , $icsrManualLockedCase.caseNumber, $icsrManualLockedCase.versionNumber. System error please check DB data")
                                    return
                                }
                                log.debug("Processing case $icsrManualLockedCase.caseId for profile $icsrManualLockedCase.profileName")
                                log.info("Marking Manual Case as QUALIFIED")
                                log.info(caseObj.id + " : " + caseObj.caseNumber + " : " + caseObj.profileId + " : " + caseObj.status)
                                // todo: commenting this code as DB does not require this proc call any more. Need to remove this whole block once this flow works fine.
                                /*ExecutedTemplateQuery.withNewSession {
                                    ExecutedTemplateQuery executedTemplateQuery = ExecutedIcsrTemplateQuery.read(icsrManualLockedCase.exIcsrTemplateQueryId)
                                    if(!executedTemplateQuery){
                                        log.error("Config not found for $icsrManualLockedCase.exIcsrTemplateQueryId , $icsrManualLockedCase.caseNumber, $icsrManualLockedCase.versionNumber. System error please check DB data")
                                        return
                                    }
                                    logIcsrCaseToScheduledTracking(null, executedTemplateQuery, icsrManualLockedCase.caseNumber, icsrManualLockedCase.versionNumber, icsrManualLockedCase.dueInDays, icsrManualLockedCase.isExpedited, icsrManualLockedCase.icsrTemplateQueryId, "Application")
                                }*/
                                sql.call('{call PKG_E2B_PROCESSING.P_UPDATE_MANUAL_FLAG(?,?,?,?,?,?,?,?)}', [icsrManualLockedCase.caseId, icsrManualLockedCase.versionNumber, icsrManualLockedCase.profileName, icsrManualLockedCase.tenantId, icsrManualLockedCase.prodHashCode, icsrManualLockedCase.profileId, icsrManualLockedCase.processedReportId, caseObj.id])
                                log.debug("Processing case $icsrManualLockedCase.caseId for profile $icsrManualLockedCase.profileName got completed")
                            } else if(icsrManualLockedCase.flagNullification == 1 || icsrManualLockedCase.flagNullification == 3 || (icsrManualLockedCase.flagNullification == 2 && icsrProfileConfiguration?.autoScheduleFUPReport)) {
                                // if nullification OR follow up is true for the case with checkbox checked, the Case will be added to Schedule
                                IcsrTemplateQuery icsrTemplateQuery = IcsrTemplateQuery.read(icsrManualLockedCase.icsrTemplateQueryId)
                                if (!icsrProfileConfiguration || !icsrTemplateQuery) {
                                    log.error("Config OR Section not found for $icsrManualLockedCase.exIcsrTemplateQueryId , $icsrManualLockedCase.caseNumber, $icsrManualLockedCase.versionNumber. System error please check DB data")
                                    return
                                }
                                String caseNumber = icsrManualLockedCase.caseNumber
                                Long versionNumber = icsrManualLockedCase.versionNumber
                                Integer dueInDays = icsrManualLockedCase.dueInDays
                                Boolean isExpedited = icsrManualLockedCase.isExpedited
                                addCaseToSchedule(icsrProfileConfiguration, icsrTemplateQuery, caseNumber, versionNumber, dueInDays, isExpedited, "Application", "Application", icsrManualLockedCase.prodHashCode, icsrManualLockedCase.authorizationTypeId, icsrManualLockedCase.approvalId, icsrManualLockedCase.reportCategoryId, icsrManualLockedCase.flagNullification, new Date())
                                log.debug("Added case $icsrManualLockedCase.caseId for profile $icsrManualLockedCase.profileName for Nullification Process")
                            } else if(icsrManualLockedCase.flagNullification == 2 && !icsrProfileConfiguration?.autoScheduleFUPReport) {
                                // if there is follow up for the case with checkbox unchecked, then Case will be ignored
                                sql.call('{call PKG_E2B_PROCESSING.P_UPDATE_PVR_ICSR_MNL_NF_CL(?,?,?,?)}', [icsrManualLockedCase.caseId, icsrManualLockedCase.tenantId, icsrManualLockedCase.versionNumber, icsrManualLockedCase.processedReportId])
                            }
                        } catch (e) {
                            log.error("Failed to make manual to qualified for $icsrManualLockedCase.caseId for profile $icsrManualLockedCase.profileName", e)
                        }
                    }
                }
            }
        } finally {
            sql?.close()
        }
    }


    @Transactional
    void processDataForCase(ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery executedTemplateQuery, def icsrCaseProcessingQueue, String status = null, boolean manual = false, IcsrCaseTracking icsrCaseTrackingInstance = null) {
        Long templateId = 0L
        Long queryId = 0L
        String sectionName = ""
        Sql sql
        try {
            Locale locale = executedConfiguration.locale
            sql = new Sql(utilService.getReportConnection())
            sqlGenerationService.setCurrentSqlInfoForCaseGeneration(sql, locale)
            int icsrPreviewFlag = manual ? 1 : 0
            int nullificationFlag = icsrCaseProcessingQueue.flagUiNillif ? 1 : 0
            List<Map> authorizationType = []
            AuthorizationType.withNewSession {
                executedConfiguration.authorizationTypes.each {
                    String authName = AuthorizationType.findByIdAndLangId(it, 1)?.name
                    authorizationType.add([id: it, name: authName])
                }
            }
            log.info("${Thread.currentThread().getName()} Dump Cases For Schedule Processing")
            dumpCaseForScheduledProcessing(sql, [icsrCaseProcessingQueue])
            sql.execute("insert into GTT_ICSR_CONSTANTS (EXECUTION_ID,PROFILE_ID) values(?,?)", [icsrCaseProcessingQueue.id, icsrCaseProcessingQueue.profileId])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_ICSR_PREVIEW','${icsrPreviewFlag}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('QUERY_LEVEL','${executedConfiguration.deviceReportable ? QueryLevelEnum.PRODUCT : executedTemplateQuery?.queryLevel?.name()}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_SENDER_ORG_NAME',${executedConfiguration.senderOrganizationName?.replaceAll("(?i)'", "''")})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_SENDER_COMPANY_NAME',${executedConfiguration.senderCompanyName?.replaceAll("(?i)'", "''")})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('REPORT_NAME',${executedConfiguration.reportName?.replaceAll("(?i)'", "''")})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_COUNTRY_CODE',${executedConfiguration.recipientCountry?.replaceAll("(?i)'", "''")})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_ORG_NAME',${executedConfiguration.recipientOrganizationName?.replaceAll("(?i)'", "''")})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_TYPE',${executedConfiguration.recipientType?.replaceAll("(?i)'", "''")})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_PMDA','${executedConfiguration.isPMDAReport()?1:0}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_JPN_PROFILE','${executedConfiguration.isJapanProfile?1:0}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_DEVICE','${executedConfiguration.deviceReportable?1:0}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_RESEARCH','${executedConfiguration.multipleReport?1:0}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('PRODUCT_BASED_SCHEDULING','${executedConfiguration.isProductLevel?1:0}')")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_UI_NULLIFICATION','${nullificationFlag}')")
            if (authorizationType.size() > 0) {
                authorizationType.each {
                    sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_ID','${it.id ?: ""}')")
                    sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_DESC','${it.name ?: ""}')")
                }
            } else {
                sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_ID','')")
                sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_DESC','')")
            }
            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Call P_EXEC_ICSR_LOCKED")
            sql.call("{call PKG_E2B_PROCESSING.P_EXEC_ICSR_LOCKED}");
            generateIcsrCaseData(sql, executedTemplateQuery, locale, icsrCaseProcessingQueue, status, manual, icsrCaseTrackingInstance)
            IcsrCaseProcessingQueue.'pva'.withNewSession {
                icsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.read(icsrCaseProcessingQueue.id)
            }
            if(!manual){
                try{
                    if(icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.GENERATION_ERROR) {
                        reportExecutorService.sendGenerationErrorEmail(executedConfiguration, icsrCaseProcessingQueue, executedTemplateQuery, "Error" )
                    } else {
                        reportExecutorService.sendGenerationSuccessEmail(executedConfiguration, icsrCaseProcessingQueue, executedTemplateQuery, "Generated" )
                    }
                } catch (ex) {
                    log.error("IcsrCaseGenerateDataJob Error while sending email for successful profiles execution due to: ${ex.message}")
                }
            }
            log.info("IcsrCaseGenerateDataJob Generation of Icsr TemplateQuery for [C:${executedConfiguration.id}, RR: ${executedTemplateQuery?.id}, T:${templateId}, Q:${queryId}]")
        } catch (Exception e) {
            log.error("Unable to finish running icsr executedConfiguration.id=${executedConfiguration.id}", e)
            if (e instanceof SQLException && manual) {
                throw e
            }
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
            if(!manual){
                try{
                    reportExecutorService.sendGenerationErrorEmail(executedConfiguration, icsrCaseProcessingQueue, executedTemplateQuery, "Error" )

                } catch (ex) {
                    log.error("Error while sending email for profiles execution failure due to: ${ex.message}")
                }
            }
            throw e1
        } finally {
            if (sql) {
                sqlGenerationService.cleanGttCsvTables(executedConfiguration, executedTemplateQuery, sql)
                sql?.close()
            }
        }
    }

    private void generateIcsrCaseData(Sql sql, ExecutedTemplateQuery executedTemplateQuery, Locale locale, def icsrCaseProcessingQueue, String status, boolean manual, IcsrCaseTracking icsrCaseTrackingInstance = null) throws Exception {
        def startTime = System.currentTimeMillis()
        String querySql = null
        def reportSql = null
        String headerSql = null
        boolean bVoidedFlag = sqlGenerationService.isVoidedFlagOn(executedTemplateQuery, locale)
        boolean previewProcError = false

        // construct w/ Connection object to ensure it will get cleaned up in finally block via Sql.close()

        // TODO: Use executed templates and queries
        BaseConfiguration configuration = executedTemplateQuery.usedConfiguration
        try {
            //Needed for CIOMS as global level
            int nullificationFlag = icsrCaseProcessingQueue.flagUiNillif ? 1 : 0
            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Init Report GTT")
            String initialParamsInsert = sqlGenerationService.initializeReportGtts(executedTemplateQuery, executedTemplateQuery.usedTemplate, false, locale)

            if (initialParamsInsert) {
                sql.execute(initialParamsInsert)
            }

            String queryLevel = QueryLevelEnum.CASE
            if (configuration.deviceReportable) {
                queryLevel = QueryLevelEnum.PRODUCT
            }
            sql.execute("DELETE FROM GTT_REPORT_INPUT_PARAMS WHERE PARAM_KEY = 'QUERY_LEVEL'")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('QUERY_LEVEL','${queryLevel}')")

            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Process Template Started")
            List sqlResult = reportResultService.processTemplate(executedTemplateQuery, false, sql, locale,manual, true)
            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Process Template Ended")

            reportSql = sqlResult[0]
            headerSql = sqlResult[1]

            ReportMeasures reportMeasures = new ReportMeasures()
            Map headers = [:]

            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Process Output Started")
            File tempFile = reportResultService.processOutputResult(reportSql, sql, bVoidedFlag, configuration, executedTemplateQuery, false, null, headers, reportMeasures)
            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Process Output Ended")

            log.info("IcsrCaseGenerateDataJob Time to execute Icsr case CSV: ${System.currentTimeMillis() - startTime}ms")

            //==========================================================================================================
            //Populate of ReportResultData
            //==========================================================================================================
            IcsrCaseTracking icsrCaseTracking = null
            Map oldValues = [:]
            icsrCaseTracking = icsrCaseTrackingInstance ?: icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
            if(icsrCaseTracking?.flagLocalCp == 1 || icsrCaseTracking?.flagLocalCp == 2) {
                IcsrCaseTracking currentIcsrCaseTrackingInstance = icsrCaseTrackingInstance ?: icsrProfileAckService.getIcsrTrackingRecord(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(currentIcsrCaseTrackingInstance)
            }
            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Populate Case Result Data Started")
            populateCaseResultData(reportMeasures, tempFile, startTime, icsrCaseProcessingQueue)
            log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Populate Case Result Data Ended")
            if (manual) {
                try{
                    sql.call('{call PKG_E2B_PROCESSING.P_UPDATE_PECT_XML_FLAG(?, ?, ?, ?, ?, ?, ?, ?, ?)}', [icsrCaseProcessingQueue.caseId, icsrCaseProcessingQueue.versionNumber, configuration.reportName, (Tenants.currentId() as Long), status, icsrCaseProcessingQueue.prodHashCode, icsrCaseProcessingQueue.profileId, icsrCaseTracking.processedReportId, icsrCaseTracking.exIcsrTemplateQueryId])
                } catch (SQLException sqe) {
                    previewProcError = true
                    throw sqe
                }
            } else {
                ExecutedIcsrProfileConfiguration executedConfiguration = executedTemplateQuery.executedConfiguration as ExecutedIcsrProfileConfiguration
                if(executedConfiguration && executedConfiguration.autoGenerate) {
                    sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.auto.generate.comment')})")
                }else {
                    IcsrCaseTracking.withNewSession {
                        icsrCaseTracking = IcsrCaseTracking.findByProfileNameAndTenantIdAndCaseNumberAndVersionNumber(configuration.reportName, (Tenants.currentId() as Long), icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                    }
                    //flagLocalCp=1 means user clicked on Local Cp Completed button and flagLocalCp=2 means user clicked on auto Generate button
                    if(icsrCaseTracking.flagLocalCp == 1) {
                        sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.manual.local.cp.generate.comment')})")
                    }else if(icsrCaseTracking.flagLocalCp == 2) {
                        sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.manual.generate.comment')})")
                    }
                }
                log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Delete GTT_REPORT_INPUT_PARAM and Insert GTT_REPORT_INPUT_PARAM")
                sql.execute("DELETE FROM GTT_REPORT_INPUT_PARAMS WHERE PARAM_KEY = 'TEMPLATE_ID'")
                sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_UI_NULLIFICATION','${nullificationFlag}')")
                sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('TEMPLATE_ID', ${executedTemplateQuery.usedTemplate.originalTemplateId})")
                log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Log Cases TO Tracking Started")
                final Date generationDate = new Date()
                if (!executedTemplateQuery.executedTemplate.isMedWatchTemplate() && !executedTemplateQuery.executedTemplate.isCiomsITemplate()) {
                    insertSimpleXMLDetailsToCSubmissions(executedTemplateQuery, icsrCaseTracking, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, generationDate, sql)
                }
                logCasesToTracking(sql, generationDate)
                log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} Log Cases TO Tracking Ended")
                sql.call('{call PKG_E2B_PROCESSING.P_UPDATE_PROFILE_QUEUE(5)}')
                log.info("IcsrCaseGenerateDataJob ${Thread.currentThread().getName()} P_UPDATED_PROFILE_QUEUE Called")
            }
            if(icsrCaseTracking?.flagLocalCp == 1 || icsrCaseTracking?.flagLocalCp == 2) {
                IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                String code = icsrCaseTracking.flagLocalCp == 1 ? "auditLog.entityValue.icsr.local.cp" : "auditLog.entityValue.icsr.manual.generate"
                AuditLogConfigUtil.logChanges(newIcsrCaseTrackingInstance, newValues, oldValues
                        , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage(code, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, newIcsrCaseTrackingInstance?.profileName, ExecutedIcsrTemplateQuery.read(newIcsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, newIcsrCaseTrackingInstance?.recipient))
            }
            log.debug("For Icsr Configuration: ${configuration}, case: ${icsrCaseProcessingQueue.caseId} \n querySql: ${querySql},\n reportSql: ${reportSql}, \n headerSql: ${headerSql}")

        } catch (Exception e) {
            if (e instanceof SQLException && previewProcError) {
                log.error("case was updated while preview generation was in progress", e)
                throw e
            } else {
                log.error("Real error for Icsr case data generation", e)
                ExecutionStatusException e1 = generateReportResultExceptionHandler(e, querySql, reportSql, headerSql, configuration)
                throw e1
            }
        }
    }

    void insertSimpleXMLDetailsToCSubmissions(ExecutedTemplateQuery executedTemplateQuery, IcsrCaseTracking icsrCaseTracking, String caseNumber, Long versionNumber, final Date generationDate, Sql sql) {
        Long processedReportId = icsrCaseTracking.processedReportId
        Long profileId = icsrCaseTracking.profileId
        Long recipientId = UnitConfiguration.findByUnitName(icsrCaseTracking.recipient)?.id
        Long caseId = icsrCaseTracking.caseId
        Long tenantId = Tenants.currentId() as Long
        try {
            IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
            Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
            File reportFile = dynamicReportService.createXMLReport(executedTemplateQuery, false, [exIcsrTemplateQueryId:executedTemplateQuery.id, caseNumber: caseNumber, versionNumber: versionNumber], generationDate, isJapanProfile)
            Clob e2bText = sql.connection.createClob()
            e2bText.setString(1, reportFile.text.replaceAll("(?i)'", "''").toString())
            String insertQuery = "Insert into C_SUBMISSIONS_E2B_XML (TENANT_ID, FLAG_DB_SOURCE, PROCESSED_REPORT_ID, FILENAME, XML, DATE_REC_CREATION, DATE_REC_MODIFICATION, PROFILE_ID, RECEPIENT_ID, CASE_ID, VERSION_NUM) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            sql.execute(insertQuery, [tenantId, 1, processedReportId, reportFile.name, e2bText, new Timestamp(new Date().time), new Timestamp(new Date().time), profileId, recipientId, caseId, versionNumber])
        } catch (Exception e) {
            log.error("Error while inserting Simple XML data into C_SUBMISSIONS_E2B_XML table")
            log.error(e.getMessage())
            e.printStackTrace()
            throw e
        }
    }

    void logCasesToTracking(Sql sql, final Date generationDate) {
        sql.query("{call PKG_E2B_PROCESSING.p_e2b_initiate_e2B_tracking(:E2B_STATUS,:CASE_ID,:CASE_VERSION,:GENERATION_DATE)}", [E2B_STATUS: IcsrCaseStateEnum.GENERATED.toString(), CASE_ID: null, CASE_VERSION: null, GENERATION_DATE: new Timestamp(generationDate.time)]) { rs ->
        }
    }

    private void populateCaseResultData(ReportMeasures reportMeasures, File tempFile,
                                        long start, def icsrCaseProcessingQueue) {
        log.debug("storing temp Icsr result for ${icsrCaseProcessingQueue}, ${reportMeasures.rowCount} rows, temp filesize: ${tempFile.size()} bytes")
        // wrap up some metrics and set result
        CaseResultData data = null
        if (icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.QUALIFIED || icsrCaseProcessingQueue.deletePreview) {
            data = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
            log.debug("Using CaseData: ${data?.id} as delete preview came as true")
        }
        if (!data) {
            data = new CaseResultData()
        }
        // should we add a check for value size here and show a nice error instead of spamming out the huge validation error?
        data.encryptedValue = tempFile.bytes
        data.totalTime = System.currentTimeMillis() - start
        data.caseNumber = icsrCaseProcessingQueue.caseNumber
        data.versionNumber = icsrCaseProcessingQueue.versionNumber
        data.executedTemplateQueryId = icsrCaseProcessingQueue.executedTemplateQueryId
        data.executedON = executedOn
        data.save(flush: true, failOnError: true)
        //tempFile.delete() TODO need to check due to 4.1.8
    }

    private void executeCasesAgainstIcsrQuery(Sql sql, ExecutedTemplateQuery executedTemplateQuery, Locale locale, Long templateQueryId) throws Exception {
        String querySql = null
        String caseListInsertSql = null
        boolean hasQuery = true
        def reportSql = null
        String headerSql = null

        // construct w/ Connection object to ensure it will get cleaned up in finally block via Sql.close()

        // TODO: Use executed templates and queries
        BaseConfiguration configuration = executedTemplateQuery.usedConfiguration
        try {
            //==========================================================================================================
            //Create the Query SQL
            Long caseSeriesId = executedTemplateQuery.executedConfiguration.caseSeriesId
//            We are always generating caseIds for PeriodicReports
            //Create Query SQL
            //==========================================================================================================
            (hasQuery, caseListInsertSql, querySql) = sqlGenerationService.createQuerySql(executedTemplateQuery, configuration, hasQuery, caseListInsertSql, querySql, caseSeriesId, false, locale)

            String initialParamsInsert = sqlGenerationService.initializeReportGtts(executedTemplateQuery, executedTemplateQuery.usedTemplate, hasQuery, locale)
            sql.execute(initialParamsInsert)


            initializeGttTablesForIcsr(sql, executedTemplateQuery, hasQuery, locale)
            //==========================================================================================================
            //Create Report SQL
            //==========================================================================================================
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.auto.schedule.comment')})")
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ORIGINAL_SECTION_ID','${templateQueryId}')")
            sql.call("{call PKG_E2B_PROCESSING.P_EXEC_ICSR_SCHEDULED(?)}",[ new Timestamp(new Date().getTime())])
        } catch (Exception e) {
            log.error("Error on executeCasesAgainstIcsrQuery execution", e)
            ExecutionStatusException e1 = generateReportResultExceptionHandler(e, querySql, reportSql, headerSql, configuration)
            throw e1
        }
    }

    private void initializeGttTablesForIcsr(Sql sql, BaseTemplateQuery templateQuery, boolean hasQuery, Locale locale) {
        log.trace("Executing Version SQL")
        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_VERSIONS')}")

        // Add query reasses...
        List<String> reassessPopulateQuerySql = sqlGenerationService.setReassessContextForQuery(templateQuery)
        reassessPopulateQuerySql.each {
            if (it.length() > 0) {
                sql.call(it)
            }
        }
        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_DS_REASSESS_QUERY')}")

        String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(templateQuery, locale, templateQuery.usedConfiguration?.excludeNonValidCases,templateQuery.usedConfiguration?.excludeDeletedCases)
        if (insertQueryData) {
            sql.execute(insertQueryData)
        }
        String reassessPopulateSql = sqlGenerationService.setReassessContextForTemplate(templateQuery, hasQuery)
        //==========================================================================================================
        if (reassessPopulateSql.length() > 0) {
            sql.call(reassessPopulateSql)
        }
        sql.call("{call PKG_PVR_APP_UTIL.p_gather_statistics('GTT_DATASHEET_REASSESS')}")
    }


    @ReadOnly('pva')
    List<IcsrCaseProcessingQueue> findCasesToProcessForConfig(Long configId) {
        List<IcsrCaseProcessingQueue> totalCases = IcsrCaseProcessingQueue.'pva'.findAllByProfileIdAndStatus(configId, IcsrCaseStatusEnum.START, [max: grailsApplication.config.getProperty('icsr.case.prcocessing.batch.size', Integer, 100), sort: 'dateCreated', order: 'asc'])
        List<IcsrCaseProcessingQueue> casesToProcess = []
        //Logic to process only one case number and version per profile and delete duplicate by (caseNum and versionNum)
        totalCases.groupBy { it.caseNumber }.each {
            casesToProcess.add(it.value.sort { a, b ->
                a.versionNumber <=> b.versionNumber ?: b.caseQueueId <=> a.caseQueueId
            }.first())
        }
        List<IcsrCaseProcessingQueue> duplicateCases = []
        casesToProcess.each { caseObj ->
            List cases = totalCases.findAll {
                it.id != caseObj.id && it.caseNumber == caseObj.caseNumber && it.versionNumber == caseObj.versionNumber && it.prodHashCode == caseObj.prodHashCode && it.approvalId == caseObj.approvalId && it.reportCategoryId == caseObj.reportCategoryId
            }
            duplicateCases.addAll(cases)
        }
        deleteDuplicateCases(duplicateCases*.id)
        return casesToProcess
    }

    List<AuthorizationType> getAuthType(String locale = null) {
        Integer langId = sqlGenerationService.getPVALanguageId(locale ?: userService.currentUser?.preference?.locale?.toString() ?: 'en')
        AuthorizationType.'pva'.withNewSession {
            List<AuthorizationType> authType = AuthorizationType.'pva'.findAllByIsActiveAndIsDisplayAndLangId(true, true, langId ?: 1)
            def authTypelist = authType.findAll { it }.collect {
                [id: it.id, name: it.name]
            }
            return authTypelist
        }
    }

    List<MessageType> getMsgType() {
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        MessageType.'pva'.withNewSession {
            MessageType.'pva'.findAllByIsActiveAndDisplayAndLangId(true, true, langId, [sort: "id", order: "asc"])
                    .collect { [id: it.id, name: it.description] }
        }
    }

    @Transactional('pva')
    void deleteDuplicateCases(List<BigDecimal> ids) {
        if (!ids) {
            return
        }
        log.info("Marking delete duplicate icsr cases from $ids while collecting duplicate cases for batch execution")
        IcsrCaseProcessingQueue.'pva'.executeUpdate('update IcsrCaseProcessingQueue ic set ic.status=:status where ic.id in :ids', [ids: ids, status: IcsrCaseStatusEnum.DELETED])
    }

    @ReadOnly('pva')
    @WithoutTenant
    List<IcsrCaseProcessingQueue> findCasesEligibleToGenerateForTenant(Integer tenantId, Integer max, List<BigDecimal> currentlyRunningIds) {
        if (max < 1) {
            return []
        }

        CPSubsystem cpSubsystem = hazelService.getHazelcastInstance().getCPSubsystem()
        FencedLock lock = cpSubsystem.getLock("findCasesEligibleToGenerateForTenant-" + tenantId)

        try {
            log.info("Acquiring lock to get QUALIFIED cases")
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                log.info("Lock Aquired")
                try {
                    Long totalIcsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.findAllByTenantIdAndStatusAndIdNotInList(tenantId, IcsrCaseStatusEnum.QUALIFIED, currentlyRunningIds ?: [new BigDecimal(-1)]).size()
                    List<IcsrCaseProcessingQueue> newIcsrCaseProcessingQueueList = new ArrayList<>();

                    if (totalIcsrCaseProcessingQueue > 0) {
                        int maxCount = 300
                        Long offset = 0
                        try {
                            while (offset < totalIcsrCaseProcessingQueue) {
                                List<IcsrCaseProcessingQueue> icsrCaseProcessingQueues = IcsrCaseProcessingQueue.'pva'.findAllByTenantIdAndStatusAndIdNotInList(tenantId, IcsrCaseStatusEnum.QUALIFIED, currentlyRunningIds ?: [new BigDecimal(-1)], [max: maxCount, offset: offset, sort: 'lastUpdated', order: 'asc'])
                                for (int i = 0; i < icsrCaseProcessingQueues.size(); i++) {
                                    IcsrCaseProcessingQueue icsrCaseProcQueue = icsrCaseProcessingQueues.get(i)
                                    //icsrCaseProcessingQueues?.each { icsrCaseProcQueue ->
                                    ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrProfileConfiguration.read(icsrCaseProcQueue.executionId)
                                    if (executedIcsrProfileConfiguration) {
                                        if (executedIcsrProfileConfiguration.autoGenerate) {
                                            newIcsrCaseProcessingQueueList.add(icsrCaseProcQueue)
                                        } else {
                                            IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.findByExIcsrTemplateQueryIdAndTenantIdAndCaseNumberAndVersionNumber(icsrCaseProcQueue.executedTemplateQueryId, tenantId, icsrCaseProcQueue.caseNumber, icsrCaseProcQueue.versionNumber, [sort: 'processedReportId', order: 'desc'])
                                            if (icsrCaseTracking && (icsrCaseTracking.flagLocalCp == 1 || icsrCaseTracking.flagLocalCp == 2)) {
                                                newIcsrCaseProcessingQueueList.add(icsrCaseProcQueue)
                                                log.info("Case added in queue for local cp Profile Name : ${icsrCaseTracking.profileName}");
                                            }
                                        }
                                    }
                                    if (newIcsrCaseProcessingQueueList.size() == max) {
                                        return newIcsrCaseProcessingQueueList;
                                    }
                                }
                                offset += maxCount
                            }
                        } catch (Exception e) {
                            log.error("Error during finding Eligible Case for Generation -> findCasesEligibleToGenerateForTenant", e)
                        }
                    } else {
                        log.debug("No Record in the Icsr Case Processing Queue table to be processed")
                    }
                    return newIcsrCaseProcessingQueueList;
                } finally {
                    log.info("Lock Releasing")
                    lock.unlock()
                    log.info("Lock Released")
                }
            } else {
                log.warn("Unable to acquire lock for findCasesEligibleToGenerateForTenant for tenant: " + tenantId)
                return []
            }
        } catch (Exception e) {
            log.error("Error acquiring Hazelcast lock for findCasesEligibleToGenerateForTenant", e)
            return []
        }
    }

    @Transactional('pva')
    void updateExecutionIdForCases(Long executedConfigurationId, List<IcsrCaseProcessingQueue> casesToProcess) {
        IcsrCaseProcessingQueue.'pva'.executeUpdate('update IcsrCaseProcessingQueue ic set ic.executionId=:executionId, ic.executedOn=:executedOn where ic.id in :ids', [executionId: executedConfigurationId, ids: casesToProcess*.id, executedOn: executedOn])

    }

    @Transactional('pva')
    void updateErrorForCase(def caseToProcess, Exception ex, IcsrCaseTracking icsrCaseTrackingInstance = null) {
        if (caseToProcess instanceof IcsrCaseProcessingQueue) {
            IcsrCaseProcessingQueue.'pva'.executeUpdate('update IcsrCaseProcessingQueue ic set ic.status=:status, ic.executedOn=:executedOn, ic.errorMessage=:errorMessage, ic.errorCause =:errorCause where ic.id =:id', [id: caseToProcess.id, status: IcsrCaseStatusEnum.GENERATION_ERROR, executedOn: executedOn, errorMessage: ex.getMessage(), errorCause: ex.getCause()?.toString()])
        }
        if (icsrCaseTrackingInstance) updateE2bStatus(icsrCaseTrackingInstance)
    }

    void addCaseToSchedule(IcsrProfileConfiguration profileConfiguration, TemplateQuery templateQuery, String caseNumber, Long version, Integer dueInDays, Boolean isExpedited, String username, String fullName, String prodHashCode = "-1", Long authroizationTypeId, Long approvalId, Long reportCategoryId, Integer flagNullification = null, final Date scheduleDate = new Date(), Boolean flagRegenerate = false, String regenerateComment = null, Long processedReportId = null) {
        log.info("Request received for manual add case for ${caseNumber}:${version} for ${profileConfiguration.reportName}")
        ExecutedReportConfiguration executedReportConfiguration
        IcsrProfileConfiguration.withTransaction {
            //let only template of interest there in Executed Profile.
            profileConfiguration.templateQueries.removeAll { it.id != templateQuery.id }
            executedReportConfiguration = executedConfigurationService.createExecutedConfiguration(profileConfiguration, null, true)
            profileConfiguration.discard()
            if (authroizationTypeId) {
                executedReportConfiguration.authorizationTypes.clear()
                executedReportConfiguration.authorizationTypes.add(authroizationTypeId)
            }
            if(dueInDays) {
                executedReportConfiguration.executedTemplateQueries*.dueInDays = dueInDays as Integer
            }
            CRUDService.saveWithoutAuditLog(executedReportConfiguration)
        }
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        ExecutedTemplateQuery executedTemplateQuery = executedReportConfiguration.executedTemplateQueriesForProcessing.first()
        logIcsrCaseToScheduledTracking(profileConfiguration, executedTemplateQuery, caseNumber, version, dueInDays, isExpedited, templateQuery.id, username, prodHashCode, approvalId, reportCategoryId, flagNullification, scheduleDate, flagRegenerate, regenerateComment, processedReportId)
        //todo: currently commenting audit log for ICSR jobs. Need to check performance impact and implementation in future.
        //if flagNullification is null, then it is manual schedule. Need to audit log for this.
        if (!flagNullification) {
            IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, version)
            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
            if(flagRegenerate){
                AuditLogConfigUtil.logChanges(newIcsrCaseTrackingInstance, newValues, [:]
                        , Constants.AUDIT_LOG_INSERT, ViewHelper.getMessage("auditLog.entityValue.icsr.regenerated", caseNumber, version, profileConfiguration.reportName, executedTemplateQuery?.executedTemplate?.name, profileConfiguration?.recipientOrganization?.unitName), ("" + System.currentTimeMillis()), username, fullName)

            }else{
                AuditLogConfigUtil.logChanges(newIcsrCaseTrackingInstance, newValues, [:]
                        , Constants.AUDIT_LOG_INSERT, ViewHelper.getMessage("auditLog.entityValue.icsr.scheduled.manual", caseNumber, version, profileConfiguration.reportName, executedTemplateQuery?.executedTemplate?.name, profileConfiguration?.recipientOrganization?.unitName), ("" + System.currentTimeMillis()), username, fullName)
                Long tenantId = newIcsrCaseTrackingInstance?.tenantId
                Long caseId = newIcsrCaseTrackingInstance?.caseId
                Long intakeCaseId = fetchIntakeCaseId(caseId, version, tenantId)
                IcsrCaseLocalCpData.'pvcm'.withNewTransaction {
                    IcsrCaseLocalCpData icsrCaseLocalCpData = IcsrCaseLocalCpData.'pvcm'.findByIntakeCaseIdAndProfileIdAndIsDeleted(intakeCaseId, profileConfiguration.id, false)
                    if(icsrCaseLocalCpData){
                        notifyLocalCpCompletionApi(intakeCaseId)
                    }
                }
            }

        }

    }

    void generateCaseDataManual(def icsrCaseProcessingQueue, String status, IcsrCaseTracking icsrCaseTracking = null) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(icsrCaseProcessingQueue.executedTemplateQueryId)
        if (executedTemplateQuery && !(icsrCaseProcessingQueue.id in executorThreadInfoService.totalCurrentlyGeneratingCases)) {
            if(icsrCaseProcessingQueue instanceof IcsrCaseProcessingQueue || icsrCaseProcessingQueue instanceof IcsrCaseProcessingQueueHist)  {
                executorThreadInfoService.addToTotalCurrentlyGeneratingCases(icsrCaseProcessingQueue.id)
            }
            log.info("Generating case data for ${icsrCaseProcessingQueue.executedTemplateQueryId}, Case Number: ${icsrCaseProcessingQueue.caseNumber} - ${icsrCaseProcessingQueue.versionNumber}")
            try {
                IcsrCaseTracking icsrCaseTrackingInstance = icsrCaseTracking ?: icsrProfileAckService.getIcsrTrackingRecord(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
                processDataForCase(GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedConfiguration), executedTemplateQuery, icsrCaseProcessingQueue, status, true, icsrCaseTrackingInstance)
                IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsrCaseProcessingQueue.executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber)
                Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                        , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.generated", icsrCaseTrackingInstance?.caseNumber, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance?.recipient))
            } catch (SQLException dbe) {
                throw dbe
            } finally {
                if(icsrCaseProcessingQueue instanceof IcsrCaseProcessingQueue || icsrCaseProcessingQueue instanceof IcsrCaseProcessingQueueHist) {
                    executorThreadInfoService.removeFromTotalCurrentlyGeneratingCases(icsrCaseProcessingQueue.id)
                }
            }
        }
    }

    void addForReEvaluate(String caseNumber, Long versionNumber) {
        log.info("Adding case ${caseNumber}: ${versionNumber} for re-evaluation")
        Sql sql = new Sql(dataSource_pva)
        try {
            Long tenantId = Tenants.currentId() as Long
            def row = sql.firstRow("SELECT DISTINCT CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE CASE_NUM = :caseNumber and VERSION_NUM = :versionNumber and tenant_id = :tenant_id", [caseNumber: caseNumber, versionNumber: versionNumber, tenant_id: tenantId])
            Long caseId = row?.CASE_ID
            if (!caseId) {
                throw new InvalidCaseInfoException("Invalid case number and version data ($caseNumber : $versionNumber)")
            }
            sql.call("{call PKG_E2B_PROCESSING.P_ADD_TO_ICSR_QUEUE(?,?,?)}", [caseId, versionNumber, tenantId])
        } finally {
            sql?.close()
        }
    }

    private void logIcsrCaseToScheduledTracking(IcsrProfileConfiguration profileConfiguration, ExecutedTemplateQuery targetExTemplateQuery, String caseNumber, Long versionNumber, Integer dueInDays, Boolean isExpedited, Long templateQueryId, String username, String prodHashCode, Long approvalId, Long reportCategoryId, Integer flagNullification = null, final Date scheduleDate, Boolean flagRegenerate, String regenerateComment, Long processedReportId) {
        User user = User.findByUsername(username)
        username = user?.fullName?:username
        Sql sql = new Sql(utilService.getReportConnection())
        try {
            Long tenantId = Tenants.currentId() as Long
            StringBuilder insertSql = new StringBuilder("Begin ")
            ExecutedReportConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) targetExTemplateQuery.executedConfiguration
            List<Map> authorizationType = []
            AuthorizationType.withNewSession {
                executedConfiguration.authorizationTypes.each {
                    String authName = AuthorizationType.findByIdAndLangId(it, 1)?.name
                    authorizationType.add([id: it, name: authName])
                }
            }
            String dueDateOption = null
            String dueDateAdjustment = null
            String calendars = null
            Integer adjustDueDate = executedConfiguration.adjustDueDate?1:0
            if(adjustDueDate==1) {
                dueDateOption = executedConfiguration.dueDateOptionsEnum
                dueDateAdjustment = executedConfiguration.dueDateAdjustmentEnum
                calendars = executedConfiguration.calendars.collect { it }.join(',').toString()
            }
            String sectionTitle = targetExTemplateQuery.title ?: targetExTemplateQuery?.usedQuery?.name
            int isReport = (targetExTemplateQuery.usedTemplate.isCiomsITemplate() || targetExTemplateQuery.usedTemplate.isMedWatchTemplate()) ? 1 : 0
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('REPORT_NAME',:REPORT_NAME)", [REPORT_NAME: executedConfiguration.reportName])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_ORG_NAME',:E2B_RECEIVER_ORG_NAME)", [E2B_RECEIVER_ORG_NAME: executedConfiguration.recipientOrganizationName])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_ORG_ID',:E2B_RECEIVER_ORG_ID)", [E2B_RECEIVER_ORG_ID: UnitConfiguration.findByUnitName(executedConfiguration.recipientOrganizationName).getId()])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_COUNTRY_CODE',:E2B_RECEIVER_COUNTRY_CODE)", [E2B_RECEIVER_COUNTRY_CODE: executedConfiguration.recipientCountry?.replaceAll("(?i)'", "''")])
            sql.execute("insert into GTT_ICSR_CONSTANTS (EXECUTION_ID,PROFILE_ID) values(?,?)", [executedConfiguration.id, profileConfiguration.id])
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_DUE_IN_DAYS','${dueInDays}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_EXPEDITED','${isExpedited ? 1 : 0}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('OBJECT_ID','${executedConfiguration.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('SECTION_ID','${targetExTemplateQuery.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('TEMPLATE_NAME','${targetExTemplateQuery.usedTemplate.name}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('SECTION_TITLE','${sectionTitle ?: ""}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('IS_REPORT','${isReport}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_MSG_TYPE_ID','${targetExTemplateQuery.icsrMsgType}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_MSG_TYPE_DESC','${targetExTemplateQuery.icsrMsgTypeName}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ORIGINAL_SECTION_ID','${templateQueryId}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('TEMPLATE_ID','${targetExTemplateQuery.usedTemplate?.originalTemplateId}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_UI_NULLIFICATION','${flagNullification == 3 ? 1 : 0}');\n")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ICSR_AUTO_GENERATE','${executedConfiguration.autoGenerate?1:0}');\n")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ICSR_LOCAL_CP','${executedConfiguration.localCpRequired?1:0}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('QUERY_LEVEL','${executedConfiguration.deviceReportable ? QueryLevelEnum.PRODUCT : targetExTemplateQuery?.queryLevel?.name()}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATE_OPEN_CASES','${executedConfiguration.includeOpenCases ?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('INCLUDE_NONREPORTABLE_CASES','${executedConfiguration.includeNonReportable ?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_PMDA','${executedConfiguration.isPMDAReport()?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_JPN_PROFILE','${executedConfiguration.isJapanProfile?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_DEVICE','${executedConfiguration.deviceReportable?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_RESEARCH','${executedConfiguration.multipleReport?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('PRODUCT_BASED_SCHEDULING','${executedConfiguration.isProductLevel?1:0}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ICSR_AWARE_DATE','${executedConfiguration.awareDate?1:0}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ADJUST_DUE_DATE','${adjustDueDate}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('DUE_DATE_OPTION','${dueDateOption ?: ""}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('DUE_DATE_ADJUSTMENT','${dueDateAdjustment ?: ""}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('CALENDARS','${calendars ?: ""}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_REGENERATE','${flagRegenerate? 1 : 0}');\n")
            if(processedReportId)
                insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('PROCESSED_REPORT_ID','${processedReportId}');\n")
            if (authorizationType.size() > 0) {
                authorizationType.each {
                    insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_ID','${it.id ?: ""}');")
                    insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_DESC','${it.name ?: ""}');")
                }
            } else {
                insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_ID','');")
                insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_DESC','');")
            }
            def row = sql.firstRow("SELECT DISTINCT CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE CASE_NUM = :caseNumber and VERSION_NUM = :versionNumber and tenant_id = :tenant_id", [caseNumber: caseNumber, versionNumber: versionNumber, tenant_id: tenantId])
            Long caseId = row?.CASE_ID
            if (!caseId) {
                throw new InvalidCaseInfoException("Invalid case number and version data ($caseNumber : $versionNumber)")
            }
            insertSql.append("INSERT INTO GTT_VERSIONS_BASE (TENANT_ID,CASE_ID,VERSION_NUM,PROD_HASH_CODE,AUTH_ID,RPT_CATEGORY_ID) values(${tenantId},${caseId},${versionNumber},'${prodHashCode}',${approvalId},${reportCategoryId});\n")
            insertSql.append('END;')
            sql.execute(insertSql.toString().trim())
            log.debug("Insert for add case to scheduled: ${insertSql.toString()}")
            IcsrCaseProcessingQueue.'pva'.withNewTransaction { status ->
                try {
                    if (!IcsrCaseProcessingQueue.'pva'.countByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(targetExTemplateQuery.id, caseNumber, versionNumber)) {
                        new IcsrCaseProcessingQueue(executedTemplateQueryId: targetExTemplateQuery.id, caseId: caseId, caseNumber: caseNumber, versionNumber: versionNumber, status: IcsrCaseStatusEnum.MANUAL, executedON: executedOn, profileId: profileConfiguration.id, executionId: executedConfiguration.id, prodHashCode: prodHashCode, isPMDA: executedConfiguration.isPMDAReport(), approvalId: approvalId, reportCategoryId: reportCategoryId, flagUiNillif: flagNullification == 3 ? 1 : 0, flagJapanProfile: executedConfiguration.isJapanProfile).'pva'.save(flush:true, failOnError: true)
                    }
                    if ((flagNullification == 2 && profileConfiguration?.autoScheduleFUPReport) || flagNullification == 1 || (flagNullification == 3 && !flagRegenerate)){
                        sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.auto.schedule.comment')})")
                    }else if(flagRegenerate){
                        sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${regenerateComment})")
                    } else {
                        sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.manual.schedule.comment')})")
                    }
                    sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('USER_NAME', ${username})")
                } catch (e) {
                    status.setRollbackOnly()
                    throw e
                }
            }
            sql.call("{call PKG_E2B_PROCESSING.P_EXEC_ICSR_MANUAL(?)}", [new Timestamp(new Date().getTime())])
            if (!checkIfCaseScheduledSuccessful(caseNumber, versionNumber, targetExTemplateQuery.id, profileConfiguration.id) && !flagRegenerate) {
                log.info("Case can not be scheduled because it is already processed for the same profile")
                throw new CaseScheduleException("Case is Not manually scheduled because it is already marked as SUBMISSION NOT REQUIRED for same profile")
            }
            if (scheduleDate) {
                if(!flagRegenerate) {
                    IcsrCaseProcessingQueue icsrCaseProcessingQueue = null
                    IcsrCaseProcessingQueue.'pva'.withNewSession {
                        icsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(targetExTemplateQuery.id, caseNumber, versionNumber)
                    }
                    if (icsrCaseProcessingQueue) {
                        log.info("Manually Scheduled case for caseId ${caseId}. Creating Entry in IcsrCaseLocalCpData...")
                        createLocalCpProfileEntries(profileConfiguration, [icsrCaseProcessingQueue])
                    } else {
                        log.warn("No matching IcsrCaseProcessingQueue found for caseNumber: ${caseNumber}, version: ${versionNumber}")
                    }
                }
                log.info("Manually Scheduled case for caseId ${caseId}. Calculating its earliest due date...")
                calculateDueDateForManual(caseId, versionNumber, tenantId, scheduleDate)
            }
        } finally {
            sql?.close()
        }
    }


    boolean checkIfCaseScheduledSuccessful(String caseNumber, Long versionNumber, Long exTempQueryid, Long profileId) {
        boolean scheduledSuccess = true
        IcsrCaseProcessingQueue.'pva'.withNewTransaction {
            String status = IcsrCaseProcessingQueue.'pva'.findByCaseNumberAndVersionNumberAndExecutedTemplateQueryIdAndProfileId(caseNumber, versionNumber, exTempQueryid, profileId)?.status
            if (status == "NOT_QUALIFIED") {
                scheduledSuccess = false
            }
        }
        return scheduledSuccess
    }

    public void calculateDueDate(BigDecimal caseQueueId, Long caseId, Long versionNumber, Long tenantId, Date date) {
        IcsrCaseMessageQueue.'pvcm'.withNewTransaction {
            Long intakeCaseId = fetchIntakeCaseId(caseId, versionNumber, tenantId)
            log.info("Intake case id for ${caseId} is ${intakeCaseId}")
            insertDueDateIntoPVCMQueue(caseQueueId, caseId, versionNumber, tenantId, date, intakeCaseId, Constants.DUE_DATE_KEY)
            insertLocalDueDateIntoPVCMQueue(caseQueueId, caseId, versionNumber, tenantId, date, intakeCaseId, Constants.LOCAL_DUE_DATE_KEY)
        }
    }

    public void insertDueDateIntoPVCMQueue(BigDecimal caseQueueId, Long caseId, Long versionNumber, Long tenantId, Date date, Long intakeCaseId, String constant) {
        IcsrCaseMessageQueue icsrCaseMessageQueue = IcsrCaseMessageQueue.'pvcm'.findByCaseIdAndDateCreatedAndKey(intakeCaseId, date, constant)
        if (!icsrCaseMessageQueue) {
            log.error("Update Due Date: Not able to find case entry for ${caseId} - ${date}")
        } else {
            String earliestDueDate = fetchEarliestDueDate(caseId, versionNumber, tenantId)
            log.info("Earliest Due Date for ${caseId} is ${earliestDueDate}")
            if (IcsrCaseProcessingQueue.'pva'.countByCaseQueueIdAndStatusInList(caseQueueId, [IcsrCaseStatusEnum.DB_BATCH_ERROR])) {
                icsrCaseMessageQueue.value = earliestDueDate
                icsrCaseMessageQueue.lastUpdated = new Date()
                icsrCaseMessageQueue.status = earliestDueDate ? Constants.DUE_DATE_PARTIAL_COMPLETED : Constants.DUE_DATE_ERROR
                icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
            } else if (IcsrCaseProcessingQueue.'pva'.countByCaseQueueIdAndStatusInList(caseQueueId, IcsrCaseStatusEnum.getSuccessCaseStatusList())) {
                //If atleast 1 case is in Qualified state, then due date should be calculate
                icsrCaseMessageQueue.value = earliestDueDate
                icsrCaseMessageQueue.lastUpdated = new Date()
                icsrCaseMessageQueue.status = Constants.DUE_DATE_COMPLETED
                icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
            } else {
                //If No profile is in Qualified for case, then status should be updated
                icsrCaseMessageQueue.lastUpdated = new Date()
                icsrCaseMessageQueue.status = Constants.DUE_DATE_NOT_QUALIFIED
                icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
            }
        }
    }

    public void insertLocalDueDateIntoPVCMQueue(BigDecimal caseQueueId, Long caseId, Long versionNumber, Long tenantId, Date date, Long intakeCaseId, String constant) {
        IcsrCaseMessageQueue icsrCaseMessageQueue = IcsrCaseMessageQueue.'pvcm'.findByCaseIdAndDateCreatedAndKey(intakeCaseId, date, constant)
        if (!icsrCaseMessageQueue) {
            log.error("Update Local Due Date: Not able to find case entry for ${caseId} - ${date}")
        } else {
            List<Long> profileIds = []
            IcsrProfileConfiguration.withNewSession {
                profileIds = IcsrProfileConfiguration.fetchAllAwareDateProfileId().list()
            }
            String earliestDueDate = fetchLocalEarliestDueDate(caseId, versionNumber, tenantId, profileIds)
            log.info("Earliest Due Date for ${caseId} is ${earliestDueDate}")
            if (IcsrCaseProcessingQueue.'pva'.countByCaseQueueIdAndStatusInListAndProfileIdInList(caseQueueId, [IcsrCaseStatusEnum.DB_BATCH_ERROR], profileIds)) {
                icsrCaseMessageQueue.value = earliestDueDate
                icsrCaseMessageQueue.lastUpdated = new Date()
                icsrCaseMessageQueue.status = earliestDueDate ? Constants.DUE_DATE_PARTIAL_COMPLETED : Constants.DUE_DATE_ERROR
                icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
            } else if (IcsrCaseProcessingQueue.'pva'.countByCaseQueueIdAndStatusInListAndProfileIdInList(caseQueueId, IcsrCaseStatusEnum.getSuccessCaseStatusList(), profileIds)) {
                //If atleast 1 case is in Qualified state, then due date should be calculate
                icsrCaseMessageQueue.value = earliestDueDate
                icsrCaseMessageQueue.lastUpdated = new Date()
                icsrCaseMessageQueue.status = Constants.DUE_DATE_COMPLETED
                icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
            } else {
                //If No profile is in Qualified for case, then status should be updated
                icsrCaseMessageQueue.lastUpdated = new Date()
                icsrCaseMessageQueue.status = Constants.DUE_DATE_NOT_QUALIFIED
                icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
            }
        }
    }

    public void calculateDueDateForManual(Long caseId, Long versionNumber, Long tenantId, final Date date, String type = Constants.DUE_DATE_TYPE_MANUAL) {
        IcsrCaseMessageQueue.'pvcm'.withNewTransaction {
            Long intakeCaseId = fetchIntakeCaseId(caseId, versionNumber, tenantId)
            log.info("Intake case id for ${caseId} is ${intakeCaseId}")
            String earliestDueDate = fetchEarliestDueDate(caseId, versionNumber, tenantId)
            insertDueDateIntoPVCMQueueForManual(earliestDueDate, intakeCaseId, caseId, versionNumber, date, type, Constants.DUE_DATE_KEY)
            List<Long> profileIds = IcsrProfileConfiguration.fetchAllAwareDateProfileId().list()
            String earliestLocalDueDate = fetchLocalEarliestDueDate(caseId, versionNumber, tenantId, profileIds)
            insertDueDateIntoPVCMQueueForManual(earliestLocalDueDate, intakeCaseId, caseId, versionNumber, date, type, Constants.LOCAL_DUE_DATE_KEY)
        }
    }

    public String fetchEarliestDueDate(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String earliestDueDate = null
        try {
            String query = """SELECT  MIN(DATE_RPT_DUE) as due_date
                              FROM  
                              (  select 
                              DATE_RPT_DUE ,
                              CMF.XMIT_STATUS_DESC,
                              cm.VOIDED,
                              cm.processed_report_id,
                              cm.flag_nullification,
                              cm.flag_manual
                              FROM
                              C_SUBMISSIONS  CM JOIN C_SUBMISSIONS_FU CMF ON ( CM.TENANT_ID = CMF.TENANT_ID
                              AND CM.FLAG_DB_SOURCE = CMF.FLAG_DB_SOURCE 
                              AND CM.PROCESSED_REPORT_ID = CMF.PROCESSED_REPORT_ID
                              AND CM.REC_TYPE = CMF.REC_TYPE )
                              WHERE   CM.TENANT_ID = ?
                              AND     CM.CASE_ID = ?
                              AND     CM.VERSION_NUM = ?
                              )
                              WHERE NOT ( flag_nullification = 1
                              AND flag_manual IN ( 1, 2 ) )
                              AND XMIT_STATUS_DESC NOT IN ('SUBMISSION_NOT_REQUIRED', 'SUBMISSION_NOT_REQUIRED_FINAL') AND NVL(VOIDED,0) = 0"""
            earliestDueDate = sql.firstRow(query, [tenantId, caseId, versionNumber])["due_date"]
            earliestDueDate = earliestDueDate?.substring(0, 10) //Need only Date part and not the time part
        } catch (Exception e) {
            log.error("Error while fetching Earliest Due Date for ${caseId}")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        return earliestDueDate
    }

    public String fetchLocalEarliestDueDate(Long caseId, Long versionNumber, Long tenantId, List<Long> profileIds) {
        Sql sql = new Sql(utilService.getReportConnection())
        String earliestDueDate = null
        try {
            String query = """SELECT  MIN(DATE_RPT_DUE) as due_date
                              FROM  
                              (  select 
                              DATE_RPT_DUE ,
                              CMF.XMIT_STATUS_DESC,
                              cm.VOIDED,
                              cm.processed_report_id,
                              cm.flag_nullification,
                              cm.flag_manual,
                              cm.PROFILE_ID
                              FROM
                              C_SUBMISSIONS  CM JOIN C_SUBMISSIONS_FU CMF ON ( CM.TENANT_ID = CMF.TENANT_ID
                              AND CM.FLAG_DB_SOURCE = CMF.FLAG_DB_SOURCE 
                              AND CM.PROCESSED_REPORT_ID = CMF.PROCESSED_REPORT_ID
                              AND CM.REC_TYPE = CMF.REC_TYPE )
                              WHERE   CM.TENANT_ID = ?
                              AND     CM.CASE_ID = ?
                              AND     CM.VERSION_NUM = ?
                              )
                              WHERE NOT ( flag_nullification = 1
                              AND flag_manual IN ( 1, 2 ) )
                              AND XMIT_STATUS_DESC NOT IN ('SUBMISSION_NOT_REQUIRED', 'SUBMISSION_NOT_REQUIRED_FINAL') AND NVL(VOIDED,0) = 0 AND PROFILE_ID in (${(profileIds) ? profileIds.join(",") : null})"""
            earliestDueDate = sql.firstRow(query, [tenantId, caseId, versionNumber])["due_date"]
            earliestDueDate = earliestDueDate?.substring(0, 10) //Need only Date part and not the time part
        } catch (Exception e) {
            log.error("Error while fetching Earliest Due Date for ${caseId}")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        return earliestDueDate
    }

    public void insertDueDateIntoPVCMQueueForManual(String earliestDueDate, Long intakeCaseId, Long caseId, Long versionNumber, Date date, String type, String constant) {
        log.info("Earliest Due Date for ${caseId} is ${earliestDueDate}")
        IcsrCaseMessageQueue icsrCaseMessageQueue = IcsrCaseMessageQueue.'pvcm'.findByCaseIdAndDateCreatedAndKey(intakeCaseId, date, constant)
        if (!icsrCaseMessageQueue) {
            log.info("Icsr Message Not Found for ${caseId} - ${versionNumber} on ${date}")
            icsrCaseMessageQueue = new IcsrCaseMessageQueue(intakeCaseId, constant, earliestDueDate, date, date, type, Constants.DUE_DATE_COMPLETED, false)
            icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
        } else {
            log.info("Icsr Message Found for ${caseId} - ${versionNumber} on ${date}. Updating Ealiest Due Date for the same")
            icsrCaseMessageQueue.value = earliestDueDate
            icsrCaseMessageQueue.lastUpdated = new Date()
            icsrCaseMessageQueue.status = Constants.DUE_DATE_COMPLETED
            icsrCaseMessageQueue.'pvcm'.save([failOnError: true])
        }
    }

    public Long fetchIntakeCaseId(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        Long intakeCaseId = 0L
        try {
            intakeCaseId = sql.firstRow("select intake_case_id from v_tx_identification where case_id = ? and version_num = ? and tenant_id = ?", [caseId, versionNumber, tenantId])["INTAKE_CASE_ID"]
        } catch (Exception e) {
            log.error("Error while fetching Intake Case Id for ${caseId}")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        return intakeCaseId
    }

    public void logIcsrCaseToScheduleTrackingForNullification(IcsrProfileConfiguration profileConfiguration, ExecutedTemplateQuery targetExTemplateQuery, String caseNumber, Long versionNumber, Integer dueInDays, String comments, Long icsrTempQueryId, String prodHashCode, Long approvalId, Long reportCategoryId, User user = null) {
        Sql sql = new Sql(utilService.getReportConnection())
        try {
            Long tenantId = Tenants.currentId() as Long
            if (!user) {
                user = userService.getUser()
            }
            String username = user.fullName ?: user.username
            StringBuilder insertSql = new StringBuilder("Begin ")
            ExecutedReportConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) targetExTemplateQuery.executedConfiguration
            List<Map> authorizationType = []
            AuthorizationType.withNewSession {
                executedConfiguration.authorizationTypes.each {
                    String authName = AuthorizationType.findByIdAndLangId(it, 1)?.name
                    authorizationType.add([id: it, name: authName])
                }
            }
            String dueDateOption = null
            String dueDateAdjustment = null
            String calendars = null
            Integer adjustDueDate = executedConfiguration.adjustDueDate?1:0
            if(adjustDueDate==1) {
                dueDateOption = executedConfiguration.dueDateOptionsEnum
                dueDateAdjustment = executedConfiguration.dueDateAdjustmentEnum
                calendars = executedConfiguration.calendars.collect { it }.join(',').toString()
            }
            String sectionTitle = targetExTemplateQuery.title ?: targetExTemplateQuery?.usedQuery?.name
            int isReport = (targetExTemplateQuery.usedTemplate.isCiomsITemplate() || targetExTemplateQuery.usedTemplate.isMedWatchTemplate()) ? 1 : 0
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('REPORT_NAME',:REPORT_NAME)", [REPORT_NAME: executedConfiguration.reportName])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_ORG_NAME',:E2B_RECEIVER_ORG_NAME)", [E2B_RECEIVER_ORG_NAME: executedConfiguration.recipientOrganizationName])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_ORG_ID',:E2B_RECEIVER_ORG_ID)", [E2B_RECEIVER_ORG_ID: UnitConfiguration.findByUnitName(executedConfiguration.recipientOrganizationName).getId()])
            sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_RECEIVER_COUNTRY_CODE',:E2B_RECEIVER_COUNTRY_CODE)", [E2B_RECEIVER_COUNTRY_CODE: executedConfiguration.recipientCountry?.replaceAll("(?i)'", "''")])
            sql.execute("insert into GTT_ICSR_CONSTANTS (EXECUTION_ID,PROFILE_ID) values(?,?)", [executedConfiguration.id, profileConfiguration.id])
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_DUE_IN_DAYS','${dueInDays}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_EXPEDITED','${1}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('OBJECT_ID','${executedConfiguration.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('SECTION_ID','${targetExTemplateQuery.id}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('TEMPLATE_NAME','${targetExTemplateQuery.usedTemplate.name}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('SECTION_TITLE','${sectionTitle ?: ""}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('IS_REPORT','${isReport}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_MSG_TYPE_ID','${targetExTemplateQuery.icsrMsgType}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_MSG_TYPE_DESC','${targetExTemplateQuery.icsrMsgTypeName}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ORIGINAL_SECTION_ID','${icsrTempQueryId}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('TEMPLATE_ID','${targetExTemplateQuery.usedTemplate?.originalTemplateId}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_UI_NULLIFICATION','${1}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('NULLIF_COMMENT','${comments.replaceAll("(?i)'","''")}');\n")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ICSR_AUTO_GENERATE','${executedConfiguration.autoGenerate?1:0}');\n")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('QUERY_LEVEL','${executedConfiguration.deviceReportable ? QueryLevelEnum.PRODUCT : targetExTemplateQuery?.queryLevel?.name()}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATE_OPEN_CASES','${executedConfiguration.includeOpenCases ?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('INCLUDE_NONREPORTABLE_CASES','${executedConfiguration.includeNonReportable ?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_PMDA','${executedConfiguration.isPMDAReport()?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_JPN_PROFILE','${executedConfiguration.isJapanProfile?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_DEVICE','${executedConfiguration.deviceReportable?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_RESEARCH','${executedConfiguration.multipleReport?1:0}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('PRODUCT_BASED_SCHEDULING','${executedConfiguration.isProductLevel?1:0}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ICSR_AWARE_DATE','${executedConfiguration.awareDate?1:0}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('ADJUST_DUE_DATE','${adjustDueDate}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('DUE_DATE_OPTION','${dueDateOption ?: ""}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('DUE_DATE_ADJUSTMENT','${dueDateAdjustment ?: ""}');")
            insertSql.append("Insert into GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('CALENDARS','${calendars ?: ""}');")
            insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('FLAG_REGENERATE','${0}');\n")

            if (authorizationType.size() > 0) {
                authorizationType.each {
                    insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_ID','${it.id ?: ""}');")
                    insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_DESC','${it.name ?: ""}');")
                }
            } else {
                insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_ID','');")
                insertSql.append("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('E2B_AUTH_TYPE_DESC','');")
            }

            def row = sql.firstRow("SELECT DISTINCT CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE CASE_NUM = :caseNumber and VERSION_NUM = :versionNumber and tenant_id = :tenant_id", [caseNumber: caseNumber, versionNumber: versionNumber, tenant_id: tenantId])
            Long caseId = row?.CASE_ID
            if (!caseId) {
                throw new InvalidCaseInfoException("Invalid case number and version data ($caseNumber : $versionNumber)")
            }
            insertSql.append("INSERT INTO GTT_VERSIONS_BASE (TENANT_ID,CASE_ID,VERSION_NUM,PROD_HASH_CODE,AUTH_ID,RPT_CATEGORY_ID) values(${tenantId},${caseId},${versionNumber},'${prodHashCode}',${approvalId},${reportCategoryId});\n")
            insertSql.append('END;')
            sql.execute(insertSql.toString().trim())
            log.debug("Insert for add case to scheduled: ${insertSql.toString()}")
            IcsrCaseProcessingQueue.'pva'.withNewTransaction { status ->
                try {
                    if (!IcsrCaseProcessingQueue.'pva'.countByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(targetExTemplateQuery.id, caseNumber, versionNumber)) {
                        new IcsrCaseProcessingQueue(executedTemplateQueryId: targetExTemplateQuery.id, caseId: caseId, caseNumber: caseNumber, versionNumber: versionNumber, status: IcsrCaseStatusEnum.MANUAL, executedON: executedOn, profileId: profileConfiguration.id, executionId: executedConfiguration.id, flagUiNillif: 1, prodHashCode: prodHashCode, isPMDA: executedConfiguration.isPMDAReport(), approvalId: approvalId, reportCategoryId: reportCategoryId).'pva'.save(flush: true, failOnError: true)
                    }
                    sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('GENERATION_COMMENT', ${ViewHelper.getMessage('app.manual.schedule.comment')})")
                    sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('USER_NAME', ${username})")
                } catch (e) {
                    status.setRollbackOnly()
                    throw e
                }
            }
            sql.call("{call PKG_E2B_PROCESSING.P_EXEC_ICSR_MANUAL(?)}", [new Timestamp(new Date().getTime())])
            calculateDueDateForManual(caseId, versionNumber, tenantId, new Date())
        } finally {
            sql?.close()
        }
    }

    private String getExecutedOn() {
        return utilService.hostIdentifier
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
                \n\n=====================================ICSR Execution===============================================================
                \nError Generating Report Result: (ID: ${configuration.id})
                \nError Generating Report Result: ${message}
                \n\n====================================================================================================
                """.stripIndent()
        log.error(error)
        return e1
    }

    private void transmitCase(ExecutedIcsrProfileConfiguration executedConfiguration, Long executedTemplateQueryId, IcsrCaseProcessingQueue icsrCaseProcessingQueue) {
        try {
            icsrReportService.transmitCase(executedTemplateQueryId, icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber.toInteger(), ViewHelper.getMessage("app.auto.transmit.comment"))
        } catch (Exception ex) {
            log.error("Error while transmitting case for ${executedTemplateQueryId} - ${icsrCaseProcessingQueue.caseNumber} - ${icsrCaseProcessingQueue.versionNumber} ", ex)
            try {
                String errorData = ex.message ?: "Fatal unknown error while generating xml"
                icsrReportService.sendIcsrProfileFailureEmailTo(executedConfiguration, [(icsrCaseProcessingQueue.caseNumber): errorData])
                StringBuffer messageArgs = new StringBuffer(" " + executedConfiguration.reportName + ':')
                messageArgs.append(icsrCaseProcessingQueue.caseNumber)
                notificationService.addNotification(executedConfiguration?.owner, 'app.notification.failed.case.number', messageArgs.toString(), executedConfiguration.id, NotificationLevelEnum.INFO, NotificationApp.COMMENTS)
            } catch (e) {
                log.error("Fatal error while sending error message notification", e)
            }
        }
    }

    public boolean checkPreviousVersionIsTransmitted(String caseNumber, Integer versionNumber, String profileName, String recipient, Long templateId) {
        boolean canTransmit = false

        Sql sql = new Sql(utilService.getReportConnection())
        String queryToCheckPreviousVersionExists = """WITH max_version AS 
                                                      (SELECT nvl(MAX(version_num),0) AS prev_version_num FROM 
                                                      c_submissions WHERE case_num = :CASE_NUMBER AND 
                                                      version_num < :VERSION_NUMBER AND 
                                                      destination_desc = :RECIPIENT AND 
                                                      partner_name = :PROFILE_NAME AND
                                                      template_id = :TEMPLATE_ID) 
                                                      SELECT prev_version_num FROM max_version"""
        String queryToCheckPreviousVersionStatus = """WITH max_version AS
                                                      (
                                                      SELECT Nvl(Max(version_num), 0) AS prev_version_num
                                                      FROM   c_submissions
                                                      WHERE  case_num = :CASE_NUMBER
                                                      AND    version_num < :VERSION_NUMBER
                                                      AND    destination_desc = :RECIPIENT
                                                      AND    partner_name = :PROFILE_NAME
                                                      AND    template_id = :TEMPLATE_ID)
                                                      SELECT
                                                        CASE
                                                            WHEN xmit_status_desc IN ( 'SCHEDULED',
                                                            'GENERATED',
                                                            'TRANSMITTING',
                                                            'TRANSMISSION_ERROR' ) THEN 0
                                                        ELSE 1
                                                      END AS status from c_submissions a
                                                      JOIN   c_submissions_fu b
                                                      ON     (
                                                             a.case_id = b.case_id
                                                      AND    a.version_num = b.version_num
                                                      AND    a.tenant_id = b.tenant_id
                                                      AND    a.flag_db_source = b.flag_db_source
                                                      AND    a.processed_report_id = b.processed_report_id
                                                      AND    a.rec_type = b.rec_type )
                                                      WHERE  a.case_num = :CASE_NUMBER
                                                      AND    a.version_num =
                                                      (
                                                            SELECT prev_version_num
                                                            FROM   max_version )
                                                      AND    a.destination_desc = :RECIPIENT
                                                      AND    a.partner_name = :PROFILE_NAME
                                                      AND    template_id = :TEMPLATE_ID"""

        boolean previousVersionExists = false
        try {
            def prevVersion = sql.firstRow(queryToCheckPreviousVersionExists, [CASE_NUMBER: caseNumber, VERSION_NUMBER: versionNumber, PROFILE_NAME: profileName, RECIPIENT: recipient, TEMPLATE_ID: templateId])
            if (prevVersion != null) {
                previousVersionExists = (prevVersion.PREV_VERSION_NUM == 0) ? false : true
            }
            if (previousVersionExists) {
                def rs = sql.firstRow(queryToCheckPreviousVersionStatus, [CASE_NUMBER: caseNumber, VERSION_NUMBER: versionNumber, PROFILE_NAME: profileName, RECIPIENT: recipient, TEMPLATE_ID: templateId])
                if (rs != null) {
                    canTransmit = (rs.STATUS == 0) ? false : true
                }
            } else {
                canTransmit = true
            }
        } catch (Exception e) {
            log.error("Exception during checking previous version is transmitted !")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        return canTransmit
    }

    void transmitDataToGateway() {
        Tenant.findAllByActive(true).each {
            int slots = executorThreadInfoService.availableSlotsForTransmittingFiles()
            if (slots < 1) {
                log.debug('No slots available for transmitting attachment file')
                return
            }
            List<IcsrCaseTracking> icsrCaseTrackingList = IcsrCaseTracking.fetchAllPostiveAckXMLCases().list([sort: 'modifiedDate', order: 'asc', max: 500])
            //icsrCaseTrackingList*.discard()
            GParsPool.withPool(slots) {
                icsrCaseTrackingList?.eachParallel { IcsrCaseTracking icsrCaseTracking ->
                    XMLResultData.withNewSession {
                        XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrCaseTracking.exIcsrTemplateQueryId, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber)
                        if (xmlResultData && xmlResultData.isAttachmentExist) {
                            ExecutedIcsrTemplateQuery executedTemplateQuery = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)
                            log.info("Transmitting Status for ${icsrCaseTracking?.uniqueIdentifier()}")
                            String uniqueId = icsrCaseTracking?.exIcsrProfileId + "_" +icsrCaseTracking?.exIcsrTemplateQueryId + "_" +icsrCaseTracking?.caseNumber + "_" +icsrCaseTracking?.versionNumber
                            if (icsrCaseTracking && !(uniqueId in executorThreadInfoService.totalCurrentlyTransmittingFiles)) {
                                executorThreadInfoService.addToTotalCurrentlyTransmittingFiles(uniqueId)
                                final Thread currentThread = Thread.currentThread()
                                final String initialThreadName = currentThread.name
                                currentThread.setName("RxThread-CR-" + uniqueId)
                                Tenants.withId(icsrCaseTracking.tenantId as Integer) {
                                    Sql sql = new Sql(utilService.getReportConnection())
                                    String destinationFolder = null
                                    try {
                                        File simpleXMLFilename = dynamicReportService.createXMLReport(executedTemplateQuery, false, [caseNumber: icsrCaseTracking.caseNumber, versionNumber: icsrCaseTracking.versionNumber, exIcsrTemplateQueryId: executedTemplateQuery.id], icsrCaseTracking?.transmissionDate, icsrCaseTracking?.isJapanProfile())
                                        if (simpleXMLFilename) {
                                            destinationFolder = dynamicReportService.getReportsDirectory() + File.separator + FilenameUtils.removeExtension(simpleXMLFilename.name) + "_PDF"
                                            simpleXMLFilename = dynamicReportService.createDirInTempAndMoveFile(destinationFolder, simpleXMLFilename)
                                            Map<String, String> attachIdAndBookMarkMap = e2BAttachmentService.fetchAttachmentIds(simpleXMLFilename.text, true)
                                            if (attachIdAndBookMarkMap && attachIdAndBookMarkMap.size() > 0) {
                                                String mergedPdfName = executedTemplateQuery.id + "-" + icsrCaseTracking.caseNumber + "-" +icsrCaseTracking.versionNumber + Constants.PDF_EXT
                                                Map<String, String> attachFilepathAndBookmark = e2BAttachmentService.fetchAttachmentFiles(attachIdAndBookMarkMap, destinationFolder)
                                                Map<String, String> docSrcDetail = e2BAttachmentService.fetchDoumentSource(simpleXMLFilename.text)
                                                File file = dynamicReportService.mergeXMLAttachmentIntoPdf(destinationFolder, mergedPdfName, attachFilepathAndBookmark, docSrcDetail)
                                                if(file) {
                                                    double fileSizeInMB = FileUtil.getFileSizeMegaBytes(file)
                                                    def size = Holders.config.getProperty('icsr.file.attachment.size.mb', Integer)
                                                    if (size != null && fileSizeInMB <= size) {
                                                        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.usedConfiguration
                                                        if (file && executedReportConfiguration instanceof ExecutedIcsrProfileConfiguration) {
                                                            icsrReportService.transmitFile(executedTemplateQuery, icsrCaseTracking.caseNumber, Integer.valueOf(icsrCaseTracking.versionNumber.intValue()), file, executedReportConfiguration.e2bDistributionChannel?.outgoingFolder, icsrCaseTracking?.transmissionDate, ViewHelper.getMessage("app.auto.transmit.attachment.comment"), icsrCaseTracking.e2BStatus)
                                                            byte[] data = Files.readAllBytes(file.toPath())
                                                            xmlResultData.attachmentData = data
                                                            xmlResultData.save(flush: true, failOnError: true)
                                                            sqlGenerationService.insertAttachmentDetail(icsrCaseTracking?.tenantId, icsrCaseTracking?.processedReportId, file, null, null, null)
                                                        }
                                                    } else {
                                                        reportExecutorService.markCaseTransmissionAttachmentError(executedTemplateQuery, icsrCaseTracking.caseNumber, Integer.valueOf(icsrCaseTracking.versionNumber.intValue()), "Transmission Failed due to attachment size exceeded for ICSR Report")
                                                    }
                                                }else {
                                                    log.info("File is null for Executed TemplateQuery : ${executedTemplateQuery.id}, caseNumber: ${icsrCaseTracking.caseNumber}, versionNumber: ${icsrCaseTracking.versionNumber}")
                                                }
                                            } else {
                                                log.info("Attachment size is empty for Executed TemplateQuery : ${executedTemplateQuery.id}, caseNumber: ${icsrCaseTracking.caseNumber}, versionNumber: ${icsrCaseTracking.versionNumber}")
                                            }
                                        } else {
                                            log.info("R2 Xml file is empty based on Executed TemplateQuery : ${executedTemplateQuery.id}, caseNumber: ${icsrCaseTracking.caseNumber}, versionNumber: ${icsrCaseTracking.versionNumber}")
                                        }
                                    } catch (com.rxlogix.customException.NoDataFoundXmlException ndfe) {
                                        log.error("No data R2 xml found for ${icsrCaseTracking.uniqueIdentifier()} due to ${ndfe}")
                                        try {
                                            reportExecutorService.markCaseTransmissionAttachmentError(executedTemplateQuery, icsrCaseTracking.caseNumber, Integer.valueOf(icsrCaseTracking.versionNumber.intValue()), $ {
                                                ndfe.message
                                            })
                                        } catch (ex) {
                                            log.error("Failed to persist error for ${uniqueId} while transmtting data", ex)
                                        }
                                    } catch (RuntimeException runExp) {
                                        log.error("Unable to merge the attachment for ${icsrCaseTracking.uniqueIdentifier()} due to ${runExp.message}")
                                        runExp.printStackTrace()
                                        try {
                                            reportExecutorService.markCaseTransmissionAttachmentError(executedTemplateQuery, icsrCaseTracking.caseNumber, Integer.valueOf(icsrCaseTracking.versionNumber.intValue()), runExp.getMessage())
                                        } catch (ex) {
                                            log.error("Failed to persist error for ${uniqueId} while transmtting data", ex)
                                        }
                                    } catch (Exception e) {
                                        log.error("Error while transmitting attachment for ${uniqueId} ${icsrCaseTracking.caseNumber} case, error: ${e.message}")
                                        e.printStackTrace()
                                        try {
                                            reportExecutorService.markCaseTransmissionAttachmentError(executedTemplateQuery, icsrCaseTracking.caseNumber, Integer.valueOf(icsrCaseTracking.versionNumber.intValue()), e.getMessage())
                                        } catch (Exception ex) {
                                            log.error("Failed to persist error for ${uniqueId} while transmtting data", ex)
                                            ex.printStackTrace()
                                        }
                                    } finally {
                                        executorThreadInfoService.removeFromTotalCurrentlyTransmittingFiles(uniqueId)
                                        currentThread.setName(initialThreadName)
                                        Path destinationPath = Paths.get(destinationFolder)
                                        if (Files.exists(destinationPath)) {
                                            destinationPath.toFile().deleteDir()
                                        }
                                        sql?.close()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    def createAutoScheduleAuditLog(List<IcsrCaseProcessingQueue> processedCases) {
        IcsrCaseProcessingQueue.'pva'.withNewTransaction {
            processedCases.each { caseQueue ->
                IcsrCaseProcessingQueue qualifiedCase = IcsrCaseProcessingQueue.'pva'.findByCaseIdAndStatusInList(caseQueue.caseId, [IcsrCaseStatusEnum.QUALIFIED, IcsrCaseStatusEnum.SCHEDULED])
                if (qualifiedCase) {
                    IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(qualifiedCase.executedTemplateQueryId, qualifiedCase.caseNumber, qualifiedCase.versionNumber)
                    Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                    AuditLogConfigUtil.logChanges(newIcsrCaseTrackingInstance, newValues, [:], Constants.AUDIT_LOG_INSERT
                            , ViewHelper.getMessage("auditLog.entityValue.icsr.scheduled.auto", newIcsrCaseTrackingInstance?.caseNumber, newIcsrCaseTrackingInstance?.versionNumber, newIcsrCaseTrackingInstance?.profileName, ExecutedIcsrTemplateQuery.read(newIcsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, newIcsrCaseTrackingInstance?.recipient))
                }
            }
        }
    }

    @Transactional
    void updateE2bStatus(IcsrCaseTracking icsrCaseTrackingInstance) {
        Sql sql = new Sql(utilService.getReportConnection())
        try{
            String insertStatement = ""
            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_FROM_STATE', '${icsrCaseTrackingInstance.e2BStatus}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_TO_STATE','${IcsrCaseStateEnum.GENERATION_ERROR.toString()}');";
            insertStatement += "END;"
            sql.execute(insertStatement)
            sql.query("{call PKG_E2B_PROCESSING.P_UPDATE_E2B_STATUS(:PROFILE_NAME, :QUERY_ID, :CASE_NUMBER, :VERSION_NUMBER, :PROCESSED_REPORT_ID , :STATUS, :SUBMISSION_DATE, :IS_LATE, :REPORTING_DESTINATION, :DUE_DATE, :COMMENT, :COMMENT_J, :JUSTIFICATION_ID, :ERROR)}", [PROFILE_NAME: icsrCaseTrackingInstance.profileName, QUERY_ID: ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.usedQuery?.originalQueryId, CASE_NUMBER: icsrCaseTrackingInstance.caseNumber, VERSION_NUMBER: icsrCaseTrackingInstance.versionNumber, PROCESSED_REPORT_ID: icsrCaseTrackingInstance.processedReportId, STATUS: IcsrCaseStateEnum.GENERATION_ERROR.toString(), SUBMISSION_DATE: null, IS_LATE: null, REPORTING_DESTINATION: icsrCaseTrackingInstance.recipient, DUE_DATE: icsrCaseTrackingInstance.dueDate ? new Timestamp(icsrCaseTrackingInstance.dueDate.time) : null, COMMENT: "Error while Generating Report", COMMENT_J: null, JUSTIFICATION_ID: null, ERROR: null]) { rs ->
            }
        } catch(Exception ex) {
            log.error("change Icsr Case Status ",ex)
        }finally {
            sql?.close()
        }
    }

    void createLocalCpProfileEntries(IcsrProfileConfiguration profileConfiguration, List<IcsrCaseProcessingQueue> qualifiedCases) {
        if (profileConfiguration.localCpRequired) {
            Long profileId = profileConfiguration.id
            String profileName = profileConfiguration.reportName
            Long intakeCaseId = null
            IcsrCaseLocalCpData icsrCaseLocalCpData = null

            try {
                IcsrCaseLocalCpData.'pvcm'.withNewTransaction {
                    qualifiedCases.each { caseQueue ->
                        intakeCaseId = fetchIntakeCaseId(caseQueue.caseId, caseQueue.versionNumber, caseQueue.tenantId)
                        icsrCaseLocalCpData = IcsrCaseLocalCpData.'pvcm'.findByIntakeCaseIdAndProfileIdAndIsDeleted(intakeCaseId, profileId, false)
                        Date currentDate = new Date()

                        if (!icsrCaseLocalCpData) {
                            icsrCaseLocalCpData = new IcsrCaseLocalCpData(intakeCaseId, profileId, profileName, currentDate, currentDate, false)
                            icsrCaseLocalCpData.'pvcm'.save([failOnError: true])
                            log.info("Entry for caseId: ${intakeCaseId}, profileId: ${profileId} created in IcsrCaseLocalCpData.")
                        } else {
                            log.info("Entry for caseId: ${intakeCaseId}, profileId: ${profileId} already exists in IcsrCaseLocalCpData. Skipping creation.")
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error in createLocalCpProfileEntries for profileId: ${profileId}. Error: ${e.message}", e)
            }
        }
    }

    void deleteLocalCpProfileEntry(Long caseId, Long versionNumber, Long tenantId, IcsrProfileConfiguration profileConfiguration, boolean isLocalCp) {
        if (profileConfiguration.localCpRequired) {
            Long profileId = profileConfiguration.id
            Long intakeCaseId = fetchIntakeCaseId(caseId, versionNumber, tenantId)
            IcsrCaseLocalCpData icsrCaseLocalCpData = null

            try {
                IcsrCaseLocalCpData.'pvcm'.withNewTransaction {
                    icsrCaseLocalCpData = IcsrCaseLocalCpData.'pvcm'.findByIntakeCaseIdAndProfileIdAndIsDeleted(intakeCaseId, profileId, false)
                    if (icsrCaseLocalCpData) {
                        icsrCaseLocalCpData.isDeleted = true
                        icsrCaseLocalCpData.lastUpdated = new Date()
                        icsrCaseLocalCpData.'pvcm'.save([failOnError: true])
                        log.info("Deleted entry for caseId: ${intakeCaseId}, profileId: ${profileId} in IcsrCaseLocalCpData.")
                    } else {
                        log.info("No existing entry found for caseId: ${intakeCaseId}, profileId: ${profileId}. Skipping deletion.")
                    }
                }
                if(icsrCaseLocalCpData && isLocalCp) {
                    notifyLocalCpCompletionApi(intakeCaseId)
                }
            } catch (Exception e) {
                log.error("Error in deleteLocalCpProfileEntry for caseId: ${caseId}, profileId: ${profileId}. Error: ${e.message}", e)
            }
        }
    }

     void notifyLocalCpCompletionApi(Long intakeCaseId) {
         String url = Holders.config.getProperty('pvcm.api.attachment.url')
         String path = PVCM_LOCAL_CP_ENDPOINT
         Map data = ["intakeCaseId" : intakeCaseId]
         log.info("invoking the Local cp api : " + url + path)
         adminIntegrationApiService.postData(url, path, data, Method.POST, true)
     }
}
