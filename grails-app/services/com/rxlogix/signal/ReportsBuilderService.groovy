package com.rxlogix.signal

import com.rxlogix.config.BaseDateRangeInformation
import com.rxlogix.ConfigurationService
import com.rxlogix.Constants
import com.rxlogix.config.*
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.ParameterValueDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.dto.reports.integration.ExecutedConfigurationDTO
import com.rxlogix.dto.reports.integration.ExecutedTemplateQueryDTO
import com.rxlogix.enums.*
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import java.util.Date


@Transactional
class ReportsBuilderService {
    def CRUDService
    def configurationService
    GrailsApplication grailsApplication
    def queryService
    def reportExecutorService
    def executedConfigurationService
    def etlJobService

    public static String CUSTOM_SQL_VALUE_REGEX_CONSTANT = /:([a-zA-Z][a-zA-Z0-9_-]*)/

    /**
     * Method to create the Executed Configuration object from passed ExecutedConfigurationDTO.
     * @param ExecutedConfigurationDTO
     * @return ExecutedConfiguration
     */
    ExecutedConfiguration createExecutedConfiguration(ExecutedConfigurationDTO exConfigurationDTO) {
        log.info("Creating createExecutedConfiguration")
        User owner = User.findByUsernameIlike(exConfigurationDTO.ownerName) ?: User.findByUsername("admin")
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(
                reportName: exConfigurationDTO.reportName,
                description: exConfigurationDTO.description,
                dateRangeType: DateRangeType.findByName(exConfigurationDTO.dateRangeType),
                asOfVersionDate: exConfigurationDTO.asOfVersionDate,
                evaluateDateAs: exConfigurationDTO.evaluateDateAs,
                executedGlobalDateRangeInformation: createExGlobalDateRangeInfo(exConfigurationDTO.executedGlobalDateRangeInformation, exConfigurationDTO.asOfVersionDate, exConfigurationDTO.evaluateDateAs),
                excludeFollowUp: exConfigurationDTO.excludeFollowUp,
                includeLockedVersion: exConfigurationDTO.includeLockedVersion,
                excludeNonValidCases: exConfigurationDTO.excludeNonValidCases,
                excludeDeletedCases: exConfigurationDTO.excludeDeletedCases,
                suspectProduct: exConfigurationDTO.suspectProduct,
                limitPrimaryPath: exConfigurationDTO.limitPrimaryPath,
                includeMedicallyConfirmedCases: exConfigurationDTO.includeMedicallyConfirmedCases,
                productSelection: exConfigurationDTO.productSelection,
                productGroupSelection: exConfigurationDTO.productGroupSelection,
                studySelection: exConfigurationDTO.studySelection,
                eventSelection: exConfigurationDTO.eventSelection,
                eventGroupSelection: exConfigurationDTO.eventGroupSelection,
                owner: owner,
                scheduleDateJSON: getRunOnceScheduledDateJson(owner),
                executedDeliveryOption: createExecutedDeliveryOption(exConfigurationDTO, owner),
                createdBy: owner.username,
                modifiedBy: owner.username,
                numOfExecutions: ExecutedConfiguration.countByReportNameAndOwner(exConfigurationDTO.reportName, owner) + 1,
                signalConfiguration: true,
                sourceProfile: SourceProfile.sourceProfilesForUser(owner)[0],
                nextRunDate: new Date(),
                tenantId: exConfigurationDTO.tenantId ?: Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Long),
                executedETLDate: exConfigurationDTO.executedETLDate ?: etlJobService?.lastSuccessfulEtlStartTime() as Date ,
                isMultiIngredient: exConfigurationDTO.isMultiIngredient,
                includeWHODrugs: exConfigurationDTO.includeWHODrugs,
                includeAllStudyDrugsCases: exConfigurationDTO.includeAllStudyDrugsCases,
                considerOnlyPoi: exConfigurationDTO.considerOnlyPoi,
                studyMedicationType: exConfigurationDTO.studyMedicationType
        )

        exConfigurationDTO.executedTemplateQueryDTOList.each { ExecutedTemplateQueryDTO exTQDTO ->
            ReportTemplate template = ReportTemplate.read(exTQDTO.templateId)
            template.modifiedBy = owner.username
            template.createdBy = owner.username

            if ((template instanceof TemplateSet)){
                if (!template.sectionBreakByEachTemplate) {
                    executedConfiguration.addToExecutedTemplateQueries(createExecutedTemplateQuery(exConfigurationDTO, template, exTQDTO, owner, executedConfiguration))
                } else {
                    template.nestedTemplates.each {
                        it.createdBy = owner.username
                        it.modifiedBy = owner.username
                        executedConfiguration.addToExecutedTemplateQueries(createExecutedTemplateQuery(exConfigurationDTO, GrailsHibernateUtil.unwrapIfProxy(it), exTQDTO, owner, executedConfiguration))
                    }
                }
            }
            else
                executedConfiguration.addToExecutedTemplateQueries(createExecutedTemplateQuery(exConfigurationDTO, template, exTQDTO, owner, executedConfiguration))
        }
        if (exConfigurationDTO.pvrCumulativeCaseSeriesId) {
            ExecutedCaseSeries cumulativeCaseSeries = ExecutedCaseSeries.read(exConfigurationDTO.pvrCumulativeCaseSeriesId)
            executedConfiguration.cumulativeCaseSeries = cumulativeCaseSeries
        }
        if (exConfigurationDTO.pvrCaseSeriesId) {
            ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.read(exConfigurationDTO.pvrCaseSeriesId)
            executedConfiguration.usedCaseSeries = executedCaseSeries
        }
        executedConfiguration.workflowState = WorkflowState.defaultWorkState
        CRUDService.save(executedConfiguration)
        executedConfiguration
    }

    ExecutedGlobalDateRangeInformation createExGlobalDateRangeInfo(ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation, Date asOfVersionDate, EvaluateCaseDateEnum evaluateDateAs){
        Date dateRangeEndAbsolute = executedGlobalDateRangeInformation.dateRangeEndAbsolute
        executedGlobalDateRangeInformation.executedAsOfVersionDate = getAsOfVersionDateCustom(asOfVersionDate, evaluateDateAs) ?: dateRangeEndAbsolute
        return executedGlobalDateRangeInformation
    }

    ExecutedTemplateQuery createExecutedTemplateQuery(ExecutedConfigurationDTO exConfigurationDTO, ReportTemplate template, ExecutedTemplateQueryDTO exTQDTO, User owner, ExecutedConfiguration executedConfiguration){
        ReportTemplate executedTemplate = executedConfigurationService.createReportTemplate(template)

        SuperQuery executedQuery = null
        SuperQuery query = SuperQuery.read(exTQDTO.queryId)
        if (query) {
            if (query instanceof QuerySet) {
                query.queries.each {
                    it.createdBy = owner.username
                    it.modifiedBy = owner.username
                }
            }
            query.createdBy = owner.username
            query.modifiedBy = owner.username

            executedQuery = queryService.createExecutedQuery(query)
        }

        ExecutedDateRangeInformation exDateRangeInfo = createDateRangeInfo(exTQDTO.executedTemplateQueryDateRangeInfoDTO, exConfigurationDTO.asOfVersionDate, exConfigurationDTO.evaluateDateAs, executedConfiguration)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: executedTemplate, executedQuery: executedQuery,
                createdBy: owner.username, modifiedBy: owner.username, executedDateRangeInformationForTemplateQuery: exDateRangeInfo,
                title: exTQDTO.title, header: exTQDTO.header, footer: exTQDTO.footer, headerDateRange: exTQDTO.headerDateRange, granularity: exTQDTO.granularity, reassessListednessDate: exTQDTO.reassessListednessDate,
                templtReassessDate: exTQDTO.templtReassessDate, headerProductSelection: exTQDTO.headerProductSelection, queryLevel: exTQDTO.queryLevel as QueryLevelEnum, blindProtected: exTQDTO.blindProtected ?: false, privacyProtected: exTQDTO.privacyProtected ?: false)

        exDateRangeInfo.executedTemplateQuery = executedTemplateQuery

        ReportResult result = new ReportResult(executionStatus: ReportExecutionStatusEnum.SCHEDULED, scheduledBy: owner)
        executedTemplateQuery.setReportResult(result)

        if (query) {
            addExecutedQueryValueLists(executedTemplateQuery, exTQDTO.executedQueryValueListDTOList, query)
        }
        executedTemplateQuery
    }

    void addExecutedQueryValueLists(ExecutedTemplateQuery executedTemplateQuery, List<QueryValueListDTO> executedQueryValueLists, SuperQuery query) {
        executedQueryValueLists?.each { QueryValueListDTO queryValueListDTO ->
            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: query)
            queryValueListDTO.parameterValues?.each { ParameterValueDTO parameterValueDTO ->
                ParameterValue executedValue
                if (parameterValueDTO.reportFieldName) {
                    ReportField reportField = ReportField.findByName(parameterValueDTO.reportFieldName)
                    executedValue = new ExecutedQueryExpressionValue(key: parameterValueDTO.key,
                            reportField: reportField, operator: parameterValueDTO.operator, value: parameterValueDTO.value)
                } else {
                    executedValue = new ExecutedCustomSQLValue(key: parameterValueDTO.key, value: parameterValueDTO.value)
                }
                executedQVL.addToParameterValues(executedValue)
            }
            executedTemplateQuery.addToExecutedQueryValueLists(executedQVL)
        }

    }

    ExecutedDateRangeInformation createDateRangeInfo(ExecutedDateRangeInfoDTO executedTemplateQueryDateRangeInfoDTO, Date asOfVersionDate, EvaluateCaseDateEnum evaluateDateAs, ExecutedConfiguration executedConfiguration) {
        ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery = new ExecutedDateRangeInformation()
        executedDateRangeInformationForTemplateQuery.with {
            relativeDateRangeValue = executedTemplateQueryDateRangeInfoDTO.relativeDateRangeValue
            dateRangeEnum = executedTemplateQueryDateRangeInfoDTO.dateRangeEnum
            executedAsOfVersionDate = getAsOfVersionDateCustom(asOfVersionDate, evaluateDateAs) ?: dateRangeEndAbsolute
            //Conditions based on Date Range type to identify Start date and end date for report criteria.
            if (dateRangeEnum == DateRangeEnum.CUSTOM){
                dateRangeStartAbsolute = executedTemplateQueryDateRangeInfoDTO.dateRangeStartAbsolute
                dateRangeEndAbsolute = executedTemplateQueryDateRangeInfoDTO.dateRangeEndAbsolute
            } else if (dateRangeEnum == DateRangeEnum.CUMULATIVE){
                dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
                dateRangeEndAbsolute = executedConfiguration.reportMinMaxDate[1] ?: executedAsOfVersionDate ?: new Date()
            } else if (dateRangeEnum == DateRangeEnum.PR_DATE_RANGE) {
                dateRangeEnum = executedConfiguration.executedGlobalDateRangeInformation.dateRangeEnum
                relativeDateRangeValue = executedConfiguration.executedGlobalDateRangeInformation.relativeDateRangeValue ?: 1
            } else {
                List dateRangeList = RelativeDateConverter.(dateRangeEnum.value())(new Date(executedConfiguration.nextRunDate?.getTime()), relativeDateRangeValue, 'UTC')
                dateRangeStartAbsolute = dateRangeList[0] ?: new Date()
                dateRangeEndAbsolute = dateRangeList[1] ?: new Date()
            }
        }

        return executedDateRangeInformationForTemplateQuery
    }

    def getAsOfVersionDateCustom(Date asOfVersionDate, EvaluateCaseDateEnum evaluateDateAs) {
        if (asOfVersionDate) {
            return asOfVersionDate
        } else if (evaluateDateAs == EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD) {
            return null
        } else if (evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
            return new Date()
        } else {
            return null
        }
    }

    ExecutedDeliveryOption createExecutedDeliveryOption(ExecutedConfigurationDTO executedConfigurationDTO, User owner) {
        log.info("SharedWith Groups : ${executedConfigurationDTO.sharedWithGroups} \n SharedWith Users : ${executedConfigurationDTO.sharedWithUsers}")
        List<User> shareWithUsers = executedConfigurationDTO.sharedWithUsers ? User.findAllByUsernameInList(executedConfigurationDTO.sharedWithUsers) : [owner]
        List<UserGroup> sharedWithGroups = executedConfigurationDTO.sharedWithGroups ? UserGroup.findAllByIsDeletedAndNameInList(false, executedConfigurationDTO.sharedWithGroups) : []
        ExecutedDeliveryOption executedDeliveryOption = new ExecutedDeliveryOption(
                sharedWith: shareWithUsers,
                sharedWithGroup: sharedWithGroups,
                emailToUsers: [],
                attachmentFormats: null)
        return executedDeliveryOption
    }

    def createExecutionStatus(ExecutedConfiguration executedConfiguration, ExecutingEntityTypeEnum executingEntityTypeEnum, String callbackURL = null) {
        log.info("Creating execution status for signal report : ${executedConfiguration.reportName}")
        ExecutionStatus executionStatus = new ExecutionStatus(
                entityId: executedConfiguration.id, entityType: executingEntityTypeEnum,
                reportVersion: executedConfiguration.numOfExecutions, startTime: System.currentTimeMillis(),
                owner: executedConfiguration.owner, reportName: executedConfiguration.reportName,
                attachmentFormats: executedConfiguration.executedDeliveryOption?.attachmentFormats,
                sharedWith: executedConfiguration?.allSharedUsers, callbackURL: callbackURL, callbackStatus: CallbackStatusEnum.UNACKNOWLEDGED,tenantId: executedConfiguration?.tenantId)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.frequency = FrequencyEnum.RUN_ONCE
        executionStatus.nextRunDate = new Date()
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

    private getRunOnceScheduledDateJson(User user, Date date = new Date()) {
        def startupTime = (date).format(DateUtil.JSON_DATE)
        def timeZone = DateUtil.getTimezoneForRunOnce(user)
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""
    }
}
