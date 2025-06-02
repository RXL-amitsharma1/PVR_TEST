package com.rxlogix

import com.rxlogix.config.DistributionChannel
import com.rxlogix.config.DmsConfiguration
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.ExecutedCustomSQLValue
import com.rxlogix.config.ExecutedDateRangeInformation
import com.rxlogix.config.ExecutedDeliveryOption
import com.rxlogix.config.ExecutedGlobalDateRangeInformation
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrTemplateQuery
import com.rxlogix.config.ExecutedQueryExpressionValue
import com.rxlogix.config.ExecutedQueryValueList
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ExecutedTemplateValueList
import com.rxlogix.config.ITemplateSet
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.ParameterValue
import com.rxlogix.config.QuerySet
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.TemplateSet
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class ExecutedIcsrConfigurationService {

    def CRUDService
    def etlJobService
    def sqlGenerationService
    def queryService
    def executedConfigurationService

    ExecutedReportConfiguration createFromExecutedIcsrConfiguration(ExecutedReportConfiguration executedSourceConfiguration, Date scheduleDate) throws Exception {

        ExecutedDeliveryOption executedDeliveryOption = executedSourceConfiguration.executedDeliveryOption ? new ExecutedDeliveryOption(sharedWith: executedSourceConfiguration.executedDeliveryOption.sharedWith,
                sharedWithGroup: executedSourceConfiguration.executedDeliveryOption.sharedWithGroup,
                emailToUsers: executedSourceConfiguration.executedDeliveryOption.emailToUsers,
                attachmentFormats: executedSourceConfiguration.executedDeliveryOption.attachmentFormats,
                oneDriveFolderName: executedSourceConfiguration.executedDeliveryOption.oneDriveFolderName,
                oneDriveSiteId: executedSourceConfiguration.executedDeliveryOption.oneDriveSiteId,
                oneDriveFolderId: executedSourceConfiguration.executedDeliveryOption.oneDriveFolderId,
                oneDriveUserSettings: executedSourceConfiguration.executedDeliveryOption.oneDriveUserSettings,
                oneDriveFormats: executedSourceConfiguration.executedDeliveryOption.oneDriveFormats,
                additionalAttachments: executedSourceConfiguration.executedDeliveryOption.additionalAttachments) : null
        executedDeliveryOption.oneDriveSiteId = executedSourceConfiguration.executedDeliveryOption.oneDriveSiteId

        ExecutedReportConfiguration executedConfiguration
        Map mapData = [reportName                : executedSourceConfiguration.reportName,
                       owner                     : User.read(executedSourceConfiguration.owner.id), locale: executedSourceConfiguration.owner.preference.locale, scheduleDateJSON: executedSourceConfiguration.scheduleDateJSON, nextRunDate: scheduleDate,
                       description               : executedSourceConfiguration.description, dateCreated: executedSourceConfiguration.dateCreated, lastUpdated: executedSourceConfiguration.lastUpdated,
                       isDeleted                 : executedSourceConfiguration.isDeleted, isEnabled: executedSourceConfiguration.isEnabled,
                       dateRangeType             : executedSourceConfiguration.dateRangeType, sourceProfile: executedSourceConfiguration.sourceProfile,
                       productSelection          : executedSourceConfiguration.productSelection, studySelection: executedSourceConfiguration.studySelection,
                       productGroupSelection     : executedSourceConfiguration.productGroupSelection,
                       configSelectedTimeZone    : executedSourceConfiguration.configSelectedTimeZone,
                       evaluateDateAs            : executedSourceConfiguration.evaluateDateAs,
                       excludeFollowUp           : executedSourceConfiguration.excludeFollowUp, includeLockedVersion: executedSourceConfiguration.includeLockedVersion, includeAllStudyDrugsCases: executedSourceConfiguration.includeAllStudyDrugsCases, excludeNonValidCases: executedSourceConfiguration.excludeNonValidCases, excludeDeletedCases: executedSourceConfiguration.excludeDeletedCases,
                       suspectProduct            : executedSourceConfiguration.suspectProduct,
                       adjustPerScheduleFrequency: executedSourceConfiguration.adjustPerScheduleFrequency,
                       executedDeliveryOption    : executedDeliveryOption,
                       createdBy                 : executedSourceConfiguration.getOwner().username, modifiedBy: executedSourceConfiguration.modifiedBy, numOfExecutions: executedSourceConfiguration.numOfExecutions + 1, hasGeneratedCasesData: executedSourceConfiguration.hasGeneratedCasesData, executionStatus: ReportExecutionStatusEnum.COMPLETED,
                       lastRunDate               : new Date(), limitPrimaryPath: executedSourceConfiguration.limitPrimaryPath, blankValuesJSON: executedSourceConfiguration.blankValuesJSON, includeMedicallyConfirmedCases: executedSourceConfiguration.includeMedicallyConfirmedCases,
                       emailConfiguration        : executedSourceConfiguration.emailConfiguration ? (EmailConfiguration) CRUDService.saveWithoutAuditLog(new EmailConfiguration(executedSourceConfiguration.emailConfiguration.properties)) : null, qualityChecked: executedSourceConfiguration.qualityChecked, tenantId: executedSourceConfiguration.tenantId
        ]

        if (executedSourceConfiguration.instanceOf(ExecutedIcsrProfileConfiguration)) {
            executedConfiguration = new ExecutedIcsrProfileConfiguration(mapData)
            executedConfiguration.with {
                recipientCompanyName = executedSourceConfiguration.recipientCompanyName
                recipientUnitOrganizationName = executedSourceConfiguration.recipientUnitOrganizationName
                recipientTitle = executedSourceConfiguration.recipientTitle
                recipientFirstName = executedSourceConfiguration.recipientFirstName
                recipientMiddleName = executedSourceConfiguration.recipientMiddleName
                recipientLastName = executedSourceConfiguration.recipientLastName
                recipientDept = executedSourceConfiguration.recipientDept
                recipientOrganizationName = executedSourceConfiguration.recipientOrganizationName
                recipientTypeName = executedSourceConfiguration.recipientTypeName
                recipientTypeId = executedSourceConfiguration.recipientTypeId
                recipientCountry = executedSourceConfiguration.recipientCountry
                receiverId = executedSourceConfiguration.receiverId
                xsltName = executedSourceConfiguration.xsltName
                recipientPartnerRegWith = executedSourceConfiguration.recipientPartnerRegWith
                comparatorReporting = executedSourceConfiguration.comparatorReporting
                autoTransmit = false
                autoSubmit = false
                autoGenerate = executedSourceConfiguration.autoGenerate
                awareDate = executedSourceConfiguration.awareDate
                multipleReport = executedSourceConfiguration.multipleReport
                localCpRequired = executedSourceConfiguration.localCpRequired
                deviceReportable = executedSourceConfiguration.deviceReportable
                preferredTimeZone = executedSourceConfiguration.preferredTimeZone
                submissionDateFrom = executedSourceConfiguration.submissionDateFrom
                senderOrganizationName = executedSourceConfiguration.senderOrganizationName
                senderTypeName = executedSourceConfiguration.senderTypeName
                senderTypeId = executedSourceConfiguration.senderTypeId
                senderCountry = executedSourceConfiguration.senderCountry
                senderId = executedSourceConfiguration.senderId
                senderPartnerRegWith = executedSourceConfiguration.senderPartnerRegWith
                assignedValidation = executedSourceConfiguration.assignedValidation
                address1 = executedSourceConfiguration.address1
                address2 = executedSourceConfiguration.address2
                city = executedSourceConfiguration.city
                state = executedSourceConfiguration.state
                postalCode = executedSourceConfiguration.postalCode
                phone = executedSourceConfiguration.phone
                email = executedSourceConfiguration.email
                senderTitle = executedSourceConfiguration.senderTitle
                senderFirstName = executedSourceConfiguration.senderFirstName
                senderMiddleName = executedSourceConfiguration.senderMiddleName
                senderLastName = executedSourceConfiguration.senderLastName
                senderDept = executedSourceConfiguration.senderDept
                senderCompanyName = executedSourceConfiguration.senderCompanyName
                senderUnitOrganizationName = executedSourceConfiguration.senderUnitOrganizationName
                recipientPrefLanguage = executedSourceConfiguration.recipientPrefLanguage
                senderPrefLanguage = executedSourceConfiguration.senderPrefLanguage
                fax = executedSourceConfiguration.fax
                if (executedSourceConfiguration.e2bDistributionChannel) {
                    e2bDistributionChannel = (DistributionChannel) CRUDService.saveWithoutAuditLog(new DistributionChannel(executedSourceConfiguration.e2bDistributionChannel.properties))
                }
                fieldProfile = executedSourceConfiguration.fieldProfile
                needPaperReport = executedSourceConfiguration.needPaperReport
                adjustDueDate = executedSourceConfiguration.adjustDueDate
                if (adjustDueDate) {
                    dueDateOptionsEnum = executedSourceConfiguration.dueDateOptionsEnum
                    dueDateAdjustmentEnum = executedSourceConfiguration.dueDateAdjustmentEnum
                    calendars = executedSourceConfiguration.calendars.collect { it }
                }
                includeProductObligation = executedSourceConfiguration.includeProductObligation
                includeStudyObligation = executedSourceConfiguration.includeStudyObligation
                allowedAttachments = executedSourceConfiguration.allowedAttachments
                autoScheduling = executedSourceConfiguration.autoScheduling
                manualScheduling = executedSourceConfiguration.manualScheduling
                recipientType = executedSourceConfiguration.recipientType
                recipientAddress1 = executedSourceConfiguration.recipientAddress1
                recipientAddress2 = executedSourceConfiguration.recipientAddress2
                recipientState = executedSourceConfiguration.recipientState
                recipientPostcode = executedSourceConfiguration.recipientPostcode
                recipientCity = executedSourceConfiguration.recipientCity
                recipientPhone = executedSourceConfiguration.recipientPhone
                recipientFax = executedSourceConfiguration.recipientFax
                recipientEmail = executedSourceConfiguration.recipientEmail
                includeNonReportable = executedSourceConfiguration.includeNonReportable
                holderId = executedSourceConfiguration.holderId
                senderHolderId = executedSourceConfiguration.senderHolderId
                includeOpenCases = executedSourceConfiguration.includeOpenCases
                unitAttachmentRegId = executedSourceConfiguration.unitAttachmentRegId
                authorizationTypes = executedSourceConfiguration.authorizationTypes.collect { it }
                isJapanProfile = executedSourceConfiguration.isJapanProfile
                isProductLevel = executedSourceConfiguration.isProductLevel
            }
        }
        executedConfiguration.isPublisherReport = executedSourceConfiguration.isPublisherReport
        executedSourceConfiguration.publisherConfigurationSections?.each {
            executedConfiguration.addToPublisherConfigurationSections(new PublisherConfigurationSection(
                    name: it.name,
                    taskTemplate: it.taskTemplate,
                    sortNumber: it.sortNumber,
                    templateFileData: it.templateFileData,
                    filename: it.filename,
                    assignedToGroup: it.assignedToGroup,
                    author: it.author,
                    reviewer: it.reviewer,
                    destination: it.destination,
                    approver: it.approver,
                    dueDate: it.dueInDays ? (new Date()).plus(it.dueInDays) : null,
                    publisherTemplate: it.publisherTemplate,
                    parameterValues: it.parameterValues?.collectEntries { k, v -> [(k): v] },
                    workflowState: WorkflowState.getDefaultWorkState(),
            ));
        }
        executedConfiguration.executedETLDate = etlJobService?.lastSuccessfulEtlStartTime() as Date
        executedConfiguration.dmsConfiguration = executedSourceConfiguration.dmsConfiguration ? (DmsConfiguration) CRUDService.saveWithoutAuditLog(new DmsConfiguration(executedSourceConfiguration.dmsConfiguration.properties)) : null
        executedConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(executedSourceConfiguration)

        addExecutedGlobalQueryRelated(executedSourceConfiguration, executedConfiguration)

        if (executedSourceConfiguration.asOfVersionDate) {
            executedConfiguration.setAsOfVersionDate(executedSourceConfiguration.asOfVersionDate)
        }

        executedConfiguration.signalConfiguration = executedSourceConfiguration.signalConfiguration

        executedSourceConfiguration.tags?.each { executedConfiguration.addToTags(it) }
        executedSourceConfiguration.poiInputsParameterValues?.each {
            executedConfiguration.addToPoiInputsParameterValues(new ParameterValue(key: it.key, value: it.value))
        }

        executedSourceConfiguration.executedTemplateQueries.each {
            createExecutedTemplateQueryAndReportResult(it, executedConfiguration, executedSourceConfiguration.getOwner(), it.executedTemplate)
        }

        return executedConfiguration
    }

    /*void getPersistedReportResult(ExecutedTemplateQuery executedTemplateQuery, ExecutedReportConfiguration executedReportConfiguration, User owner) throws Exception {
        createExecutedTemplateQueryAndReportResult(executedTemplateQuery, executedReportConfiguration, owner, executedTemplateQuery.executedTemplate)
    }*/

    void createExecutedTemplateQueryAndReportResult(ExecutedTemplateQuery exTemplateQuery, ExecutedReportConfiguration executedReportConfiguration, User owner, ReportTemplate executedTemplate) {
        ExecutedTemplateQuery executedTemplateQuery = addExecutedTemplateQueryToExecutedConfiguration(exTemplateQuery, executedTemplate)
        executedReportConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
    }

    private addExecutedTemplateQueryToExecutedConfiguration(ExecutedTemplateQuery executedTemplateQuery, ReportTemplate exTemplate) {
        executedTemplateQuery = (ExecutedIcsrTemplateQuery) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery)
        ReportTemplate executedTemplate
        ReportTemplate originalTemplate = GrailsHibernateUtil.unwrapIfProxy(ReportTemplate.read(exTemplate.originalTemplateId))
        if (exTemplate instanceof ITemplateSet) {
            executedTemplate = executedConfigurationService.createReportTemplate(originalTemplate)
        } else {
            Long exId = ReportTemplate.getLatestExRptTempltByOrigTempltId(Long.valueOf(originalTemplate.id)).get()
            executedTemplate = ReportTemplate.read(exId)
        }
        ExecutedDateRangeInformation executedDateRangeInformation = new ExecutedDateRangeInformation(
                dateRangeEnum: executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum,
                relativeDateRangeValue: executedTemplateQuery.executedDateRangeInformationForTemplateQuery.relativeDateRangeValue,
                dateRangeEndAbsolute: executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute, dateRangeStartAbsolute: executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute,
                executedAsOfVersionDate: executedTemplateQuery.executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate
        )

        SuperQuery executedQuery = null
        if (executedTemplateQuery.executedQuery) {
            SuperQuery originalQuery = GrailsHibernateUtil.unwrapIfProxy(SuperQuery.read(executedTemplateQuery.executedQuery.originalQueryId))
            if (originalQuery instanceof QuerySet) {
                executedQuery = queryService.createExecutedQuery(originalQuery)
            } else {
                Long exId = SuperQuery.getLatestExQueryByOrigQueryId(originalQuery.id).get()
                executedQuery = SuperQuery.read(exId)
            }
        }

        String title = executedTemplateQuery.title

        Map executedTemplateQueryData = [executedTemplate                            : executedTemplate,
                                         executedQuery                               : executedQuery,
                                         executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                                         queryLevel                                  : executedTemplateQuery.queryLevel, title: title,
                                         header                                      : executedTemplateQuery.header, footer: executedTemplateQuery.footer,
                                         createdBy                                   : executedTemplateQuery.createdBy, modifiedBy: executedTemplateQuery.modifiedBy, templtReassessDate: executedTemplateQuery.templtReassessDate,
                                         headerProductSelection                      : executedTemplateQuery.headerProductSelection, headerDateRange: executedTemplateQuery.headerDateRange, granularity: executedTemplateQuery.granularity, reassessListednessDate: executedTemplateQuery.reassessListednessDate,
                                         draftOnly                                   : executedTemplateQuery.draftOnly, blindProtected: executedTemplateQuery.blindProtected, privacyProtected: executedTemplateQuery.privacyProtected, displayMedDraVersionNumber: executedTemplateQuery.displayMedDraVersionNumber ?: false
        ]

        ExecutedIcsrTemplateQuery exTemplateQuery = new ExecutedIcsrTemplateQuery(executedTemplateQueryData)
        exTemplateQuery.with {
            authorizationType = executedTemplateQuery.authorizationType
            productType = executedTemplateQuery.productType
            dueInDays = executedTemplateQuery.dueInDays
            icsrMsgType = executedTemplateQuery.icsrMsgType
            icsrMsgTypeName = executedTemplateQuery.icsrMsgTypeName
            distributionChannelName = executedTemplateQuery.distributionChannelName
            orderNo = executedTemplateQuery.orderNo
            emailConfiguration = executedTemplateQuery.emailConfiguration ? new EmailConfiguration(executedTemplateQuery.emailConfiguration.properties) : null
            isExpedited = executedTemplateQuery.isExpedited
        }
        exTemplateQuery.userGroup = executedTemplateQuery.userGroup
        exTemplateQuery.dueInDays = executedTemplateQuery.dueInDays

        executedTemplateQuery.executedTemplateValueLists.each {
            ExecutedTemplateValueList executedTVL = new ExecutedTemplateValueList(template: it.template)
            it.parameterValues.each {
                ParameterValue executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                executedTVL.addToParameterValues(executedValue)
            }
            exTemplateQuery.addToExecutedTemplateValueLists(executedTVL)
        }

        executedTemplateQuery.executedQueryValueLists.each {
            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: it.query)
            it.parameterValues.each {
                ParameterValue executedValue
                if (it.hasProperty('reportField')) {
                    executedValue = new ExecutedQueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value, specialKeyValue: it.specialKeyValue)
                } else {
                    executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                }
                executedQVL.addToParameterValues(executedValue)
            }
            exTemplateQuery.addToExecutedQueryValueLists(executedQVL)
        }

        return exTemplateQuery
    }

    private void addExecutedGlobalQueryRelated(ExecutedReportConfiguration executedSourceConfiguration, ExecutedReportConfiguration executedReportConfiguration) {
        if (executedSourceConfiguration.executedGlobalDateRangeInformation) {
            executedReportConfiguration.executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(
                    dateRangeEnum: executedSourceConfiguration.executedGlobalDateRangeInformation.dateRangeEnum,
                    relativeDateRangeValue: executedSourceConfiguration.executedGlobalDateRangeInformation.relativeDateRangeValue,
                    dateRangeEndAbsolute: executedSourceConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute,
                    dateRangeStartAbsolute: executedSourceConfiguration.executedGlobalDateRangeInformation.dateRangeStartAbsolute,
                    executedAsOfVersionDate: executedSourceConfiguration.executedGlobalDateRangeInformation.executedAsOfVersionDate,
                    executedReportConfiguration: executedReportConfiguration
            )
        }

        if (executedSourceConfiguration.executedGlobalQuery) {
            executedSourceConfiguration.executedGlobalQuery = GrailsHibernateUtil.unwrapIfProxy(executedSourceConfiguration.executedGlobalQuery)
            if (executedSourceConfiguration.executedGlobalQuery instanceof QuerySet) {
                executedReportConfiguration.executedGlobalQuery = queryService.createExecutedQuery(SuperQuery.read(executedSourceConfiguration.executedGlobalQuery.originalQueryId))
            } else {
                Long exId = SuperQuery.getLatestExQueryByOrigQueryId(Long.valueOf(executedSourceConfiguration.executedGlobalQuery.originalQueryId)).get()
                executedReportConfiguration.executedGlobalQuery = SuperQuery.read(exId)
            }
        }

        executedSourceConfiguration.executedGlobalQueryValueLists?.each {
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
}
