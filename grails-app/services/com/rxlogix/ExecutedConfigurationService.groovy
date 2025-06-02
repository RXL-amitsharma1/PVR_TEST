package com.rxlogix

import com.rxlogix.config.CaseLineListingTemplate
import com.rxlogix.config.Configuration
import com.rxlogix.config.CustomSQLTemplate
import com.rxlogix.config.CustomSQLValue
import com.rxlogix.config.DataTabulationColumnMeasure
import com.rxlogix.config.DataTabulationMeasure
import com.rxlogix.config.DataTabulationTemplate
import com.rxlogix.config.DistributionChannel
import com.rxlogix.config.DmsConfiguration
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.ExecutedCaseLineListingTemplate
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedCustomSQLTemplate
import com.rxlogix.config.ExecutedCustomSQLValue
import com.rxlogix.config.ExecutedDataTabulationTemplate
import com.rxlogix.config.ExecutedDateRangeInformation
import com.rxlogix.config.ExecutedDeliveryOption
import com.rxlogix.config.ExecutedGlobalDateRangeInbound
import com.rxlogix.config.ExecutedGlobalDateRangeInformation
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutedIcsrTemplateQuery
import com.rxlogix.config.ExecutedInboundCompliance
import com.rxlogix.config.ExecutedNonCaseSQLTemplate
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedPublisherSource
import com.rxlogix.config.ExecutedQueryCompliance
import com.rxlogix.config.ExecutedQueryExpressionValue
import com.rxlogix.config.ExecutedQueryValueList
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ExecutedTemplateSet
import com.rxlogix.config.ExecutedTemplateValueList
import com.rxlogix.config.ExecutedXMLTemplate
import com.rxlogix.config.ITemplateSet
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.IcsrReportConfiguration
import com.rxlogix.config.IcsrTemplateQuery
import com.rxlogix.config.InboundCompliance
import com.rxlogix.config.NonCaseSQLTemplate
import com.rxlogix.config.ParameterValue
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.QueryCompliance
import com.rxlogix.config.QuerySet
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportFieldInfoList
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SchedulerConfigParams
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.TemplateQuery
import com.rxlogix.config.TemplateSet
import com.rxlogix.config.TemplateUserState
import com.rxlogix.config.UserGroupTemplate
import com.rxlogix.config.UserTemplate
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.XMLTemplate
import com.rxlogix.config.XMLTemplateNode
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.mapping.PvcmFieldLabel
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.joda.time.Period
import org.springframework.transaction.annotation.Propagation
import com.rxlogix.config.MessageType

class ExecutedConfigurationService {

    def sqlGenerationService
    def queryService
    def CRUDService
    def templateService
    def configurationService
    def etlJobService
    def emailService
    def executedConfigurationService
    def sqlService
    UserService userService


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    ExecutedReportConfiguration createExecutedConfiguration(ReportConfiguration configuration, Date scheduledDate, boolean withOutResult = false) throws Exception {

        ExecutedDeliveryOption executedDeliveryOption = configuration?.deliveryOption ? new ExecutedDeliveryOption(sharedWith: (configuration?.deliveryOption?.sharedWith + configuration?.deliveryOption?.executableBy)?.unique(),
                sharedWithGroup: (configuration?.deliveryOption?.sharedWithGroup + configuration?.deliveryOption?.executableByGroup)?.unique(),
                emailToUsers: configuration?.deliveryOption?.emailToUsers,
                attachmentFormats: configuration?.deliveryOption?.attachmentFormats,
                oneDriveFolderName: configuration?.deliveryOption?.oneDriveFolderName,
                oneDriveSiteId: configuration?.deliveryOption?.oneDriveSiteId,
                oneDriveFolderId: configuration?.deliveryOption?.oneDriveFolderId,
                oneDriveUserSettings: configuration?.deliveryOption?.oneDriveUserSettings,
                oneDriveFormats: configuration?.deliveryOption?.oneDriveFormats,
                additionalAttachments: configuration?.deliveryOption?.additionalAttachments) : null
        executedDeliveryOption?.oneDriveSiteId = configuration?.deliveryOption?.oneDriveSiteId

        ExecutedReportConfiguration executedConfiguration
        Map mapData = [reportName                : configuration.reportName,
                       owner                     : User.read(configuration.owner.id), locale: configuration.owner.preference.locale, scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: scheduledDate,
                       description               : configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                       isDeleted                 : configuration.isDeleted, isEnabled: configuration.isEnabled,pvqType: configuration.pvqType,
                       dateRangeType             : configuration.dateRangeType, sourceProfile: configuration.sourceProfile,
                       productSelection          : configuration.productSelection, studySelection: configuration.studySelection,
                       productGroupSelection     : configuration.productGroupSelection,
                       configSelectedTimeZone    : configuration.configSelectedTimeZone,
                       evaluateDateAs            : configuration.evaluateDateAs,
                       excludeFollowUp           : configuration.excludeFollowUp, includeLockedVersion: configuration.includeLockedVersion, includeAllStudyDrugsCases: configuration.includeAllStudyDrugsCases, excludeNonValidCases: configuration.excludeNonValidCases, excludeDeletedCases: configuration.excludeDeletedCases,
                       suspectProduct            : configuration.suspectProduct,
                       adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                       executedDeliveryOption    : executedDeliveryOption,
                       createdBy                 : configuration.getOwner().username, modifiedBy: configuration.modifiedBy, numOfExecutions: configuration.numOfExecutions + 1, hasGeneratedCasesData: configuration.generateCaseSeries, executionStatus: ReportExecutionStatusEnum.COMPLETED,
                       lastRunDate               : new Date(), limitPrimaryPath: configuration?.limitPrimaryPath, blankValuesJSON: configuration?.blankValuesJSON, includeMedicallyConfirmedCases: configuration?.includeMedicallyConfirmedCases,
                       emailConfiguration        : configuration.emailConfiguration ? (EmailConfiguration) CRUDService.saveWithoutAuditLog(new EmailConfiguration(configuration.emailConfiguration.properties)) : null, qualityChecked: configuration.qualityChecked, tenantId: configuration.tenantId,
                       isMultiIngredient         : configuration.isMultiIngredient, includeWHODrugs: configuration.includeWHODrugs, removeOldVersion: configuration.removeOldVersion
        ]
        if (configuration.instanceOf(Configuration)) {
            executedConfiguration = new ExecutedConfiguration(mapData)
            executedConfiguration.includeNonSignificantFollowUp = configuration.includeNonSignificantFollowUp
            executedConfiguration.eventSelection = configuration.eventSelection
            executedConfiguration.eventGroupSelection = configuration.eventGroupSelection
            executedConfiguration.usedCaseSeries = configuration.useCaseSeries
        } else if (configuration.instanceOf(IcsrReportConfiguration)) {
            executedConfiguration = new ExecutedIcsrReportConfiguration(mapData)
            executedConfiguration.with {
                includePreviousMissingCases = configuration.includePreviousMissingCases
                includeOpenCasesInDraft = configuration.includeOpenCasesInDraft
                workflowState = WorkflowState.defaultWorkState
                dueInDays = configuration.dueInDays
                periodicReportType = configuration.periodicReportType
                primaryReportingDestination = configuration.primaryReportingDestination
                if (configuration.reportingDestinations) {
                    reportingDestinations = new HashSet<String>(configuration.reportingDestinations)
                }
                recipientCompanyName = configuration.recipientOrganization?.organizationName
                recipientUnitOrganizationName = configuration.recipientOrganization?.unitOrganizationName
                recipientTitle = configuration.recipientOrganization?.title
                recipientFirstName = configuration.recipientOrganization?.firstName
                recipientMiddleName = configuration.recipientOrganization?.middleName
                recipientLastName = configuration.recipientOrganization?.lastName
                recipientDept = configuration.recipientOrganization?.department
                recipientOrganizationName = configuration.recipientOrganization?.unitName
                recipientTypeName = configuration.recipientOrganization?.organizationType?.name
                recipientCountry = configuration.recipientOrganization?.organizationCountry
                receiverId = configuration.recipientOrganization?.unitRegisteredId
                xsltName = configuration.recipientOrganization?.xsltName
                recipientPartnerRegWith = configuration.recipientOrganization?.registeredWith?.unitName
                senderOrganizationName = configuration.senderOrganization?.unitName
                senderTypeName = configuration.senderOrganization?.organizationType?.name
                senderCountry = configuration.senderOrganization?.organizationCountry
                senderId = configuration.senderOrganization?.unitRegisteredId
                senderPartnerRegWith = configuration.senderOrganization?.registeredWith?.unitName
                address1 = configuration.senderOrganization?.address1
                address2 = configuration.senderOrganization?.address2
                city = configuration.senderOrganization?.city
                state = configuration.senderOrganization?.state
                postalCode = configuration.senderOrganization?.postalCode
                phone = configuration.senderOrganization?.phone
                email = configuration.senderOrganization?.email
                senderTitle = configuration.senderOrganization?.title
                senderFirstName = configuration.senderOrganization?.firstName
                senderMiddleName = configuration.senderOrganization?.middleName
                senderLastName = configuration.senderOrganization?.lastName
                senderDept = configuration.senderOrganization?.department
                senderCompanyName = configuration.senderOrganization?.organizationName
                senderUnitOrganizationName = configuration.senderOrganization?.unitOrganizationName
                senderHolderId = configuration.senderOrganization?.holderId
                fax = configuration.senderOrganization?.fax
                referenceProfileName = configuration.referenceProfile?.reportName
                allowedAttachments = configuration.recipientOrganization?.allowedAttachments?.collect{it}.join(',').toString()
                recipientType = configuration.recipientOrganization?.organizationType?.name
                recipientAddress1 = configuration.recipientOrganization?.address1
                recipientAddress2 = configuration.recipientOrganization?.address2
                recipientState = configuration.recipientOrganization?.state
                recipientPostcode = configuration.recipientOrganization?.postalCode
                recipientCity = configuration.recipientOrganization?.city
                recipientPhone = configuration.recipientOrganization?.phone
                recipientFax = configuration.recipientOrganization?.fax
                recipientEmail = configuration.recipientOrganization?.email
                preferredTimeZone = configuration.recipientOrganization?.preferredTimeZone
                holderId = configuration.recipientOrganization?.holderId
                xmlVersion = configuration.recipientOrganization?.xmlVersion
                xmlEncoding = configuration.recipientOrganization?.xmlEncoding
                xmlDoctype = configuration.recipientOrganization?.xmlDoctype
                unitAttachmentRegId = configuration.recipientOrganization?.unitAttachmentRegId
                recipientPrefLanguage = configuration.recipientOrganization?.preferredLanguage
                senderPrefLanguage = configuration.senderOrganization?.preferredLanguage
            }
        } else if (configuration.instanceOf(IcsrProfileConfiguration)) {
            executedConfiguration = new ExecutedIcsrProfileConfiguration(mapData)
            executedConfiguration.with {
                recipientCompanyName = configuration.recipientOrganization?.organizationName
                recipientUnitOrganizationName = configuration.recipientOrganization?.unitOrganizationName
                recipientTitle = configuration.recipientOrganization?.title
                recipientFirstName = configuration.recipientOrganization?.firstName
                recipientMiddleName = configuration.recipientOrganization?.middleName
                recipientLastName = configuration.recipientOrganization?.lastName
                recipientDept = configuration.recipientOrganization?.department
                recipientOrganizationName = configuration.recipientOrganization?.unitName
                recipientTypeName = configuration.recipientOrganization?.organizationType?.name
                recipientTypeId = configuration.recipientOrganization?.organizationType?.org_name_id
                recipientCountry = configuration.recipientOrganization?.organizationCountry
                receiverId = configuration.recipientOrganization?.unitRegisteredId
                xsltName = configuration.recipientOrganization?.xsltName
                recipientPartnerRegWith = configuration.recipientOrganization?.registeredWith?.unitName
                comparatorReporting = configuration.comparatorReporting
                autoTransmit = configuration.autoTransmit
                autoSubmit = configuration.autoSubmit
                submissionDateFrom = configuration.submissionDateFrom
                senderOrganizationName = configuration.senderOrganization?.unitName
                senderTypeName = configuration.senderOrganization?.organizationType?.name
                senderTypeId = configuration.senderOrganization?.organizationType?.org_name_id
                senderCountry = configuration.senderOrganization?.organizationCountry
                senderId = configuration.senderOrganization?.unitRegisteredId
                senderPartnerRegWith = configuration.senderOrganization?.registeredWith?.unitName
                assignedValidation = configuration.assignedValidation
                address1 = configuration.senderOrganization?.address1
                address2 = configuration.senderOrganization?.address2
                city = configuration.senderOrganization?.city
                state = configuration.senderOrganization?.state
                postalCode = configuration.senderOrganization?.postalCode
                phone = configuration.senderOrganization?.phone
                email = configuration.senderOrganization?.email
                senderTitle = configuration.senderOrganization?.title
                senderFirstName = configuration.senderOrganization?.firstName
                senderMiddleName = configuration.senderOrganization?.middleName
                senderLastName = configuration.senderOrganization?.lastName
                senderDept = configuration.senderOrganization?.department
                senderCompanyName = configuration.senderOrganization?.organizationName
                senderUnitOrganizationName = configuration.senderOrganization?.unitOrganizationName
                senderHolderId = configuration.senderOrganization?.holderId
                fax = configuration.senderOrganization?.fax
                if (configuration.e2bDistributionChannel) {
                    e2bDistributionChannel = (DistributionChannel) CRUDService.saveWithoutAuditLog(new DistributionChannel(configuration.e2bDistributionChannel.properties))
                }
                fieldProfile = configuration.fieldProfile
                needPaperReport = configuration.needPaperReport
                adjustDueDate = configuration.adjustDueDate
                if (adjustDueDate){
                    dueDateOptionsEnum = configuration.dueDateOptionsEnum
                    dueDateAdjustmentEnum = configuration.dueDateAdjustmentEnum
                    calendars = configuration.calendars.collect { it }
                }
                includeProductObligation = configuration.includeProductObligation
                includeStudyObligation = configuration.includeStudyObligation
                allowedAttachments = configuration.recipientOrganization?.allowedAttachments?.collect{it}?.join(',')?.toString()
                autoScheduling = configuration.autoScheduling
                manualScheduling = configuration.manualScheduling
                recipientType = configuration.recipientOrganization?.organizationType?.name
                recipientAddress1 = configuration.recipientOrganization?.address1
                recipientAddress2 = configuration.recipientOrganization?.address2
                recipientState = configuration.recipientOrganization?.state
                recipientPostcode = configuration.recipientOrganization?.postalCode
                recipientCity = configuration.recipientOrganization?.city
                recipientPhone = configuration.recipientOrganization?.phone
                recipientFax = configuration.recipientOrganization?.fax
                recipientEmail = configuration.recipientOrganization?.email
                autoGenerate = configuration.autoGenerate
                localCpRequired = configuration.localCpRequired
                autoScheduleFUPReport = configuration.autoScheduleFUPReport
                awareDate = configuration.awareDate
                multipleReport = configuration.multipleReport
                deviceReportable = configuration.deviceReportable
                preferredTimeZone = configuration.recipientOrganization?.preferredTimeZone
                includeNonReportable = configuration.includeNonReportable
                holderId = configuration.recipientOrganization?.holderId
                includeOpenCases = configuration.includeOpenCases
                xmlVersion = configuration.recipientOrganization?.xmlVersion
                xmlEncoding = configuration.recipientOrganization?.xmlEncoding
                xmlDoctype = configuration.recipientOrganization?.xmlDoctype
                unitAttachmentRegId = configuration.recipientOrganization?.unitAttachmentRegId
                recipientPrefLanguage = configuration.recipientOrganization?.preferredLanguage
                senderPrefLanguage = configuration.senderOrganization?.preferredLanguage
                isJapanProfile = configuration.isJapanProfile
                isProductLevel = configuration.isProductLevel
                authorizationTypes = configuration.authorizationTypes.collect { it }
            }
        }else {
            executedConfiguration = new ExecutedPeriodicReportConfiguration(mapData)
            SchedulerConfigParams configParams = SchedulerConfigParams.findByIsDeletedAndConfigurationAndRunDate(false, configuration, scheduledDate)
            if (configParams) {
                configParams?.publisherContributors?.each {
                    executedConfiguration.addToPublisherContributors(it)
                }
                executedConfiguration.primaryPublisherContributor = configParams.primaryPublisherContributor
            } else {
                executedConfiguration.primaryPublisherContributor = configuration.primaryPublisherContributor
                configuration.publisherContributors?.each {
                    executedConfiguration.addToPublisherContributors(it)
                }
            }
            executedConfiguration.with {
                includePreviousMissingCases = configuration.includePreviousMissingCases
                includeOpenCasesInDraft = configuration.includeOpenCasesInDraft
                workflowState = WorkflowState.defaultWorkState
                dueInDays = configuration.dueInDays
                periodicReportType = configuration.periodicReportType
                primaryReportingDestination = configuration.primaryReportingDestination
                if (configuration.reportingDestinations) {
                    reportingDestinations = new HashSet<String>(configuration.reportingDestinations)
                }
            }
        }
        executedConfiguration.isPublisherReport = configuration.isPublisherReport
        configuration.publisherConfigurationSections?.each {
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
        executedConfiguration.dmsConfiguration = ((configuration.dmsConfiguration && !configuration.dmsConfiguration.isDeleted) ? (DmsConfiguration) CRUDService.saveWithoutAuditLog(new DmsConfiguration(configuration.dmsConfiguration.properties)) : null)
        executedConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(configuration)

        addExecutedGlobalQueryRelated(configuration, executedConfiguration)
        if ((configuration.instanceOf(PeriodicReportConfiguration) || configuration.instanceOf(IcsrReportConfiguration)) && executedConfiguration.executedGlobalDateRangeInformation) {
            if (configuration.instanceOf(PeriodicReportConfiguration) && executedConfiguration.gantt)
                executedConfiguration.dueDate = new Date() + executedConfiguration.gantt.dueDays
            else
                executedConfiguration.dueDate = executedConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute + (executedConfiguration.dueInDays ?: 0)
        }
        if (configuration?.asOfVersionDate) {
            executedConfiguration.setAsOfVersionDate(configuration.asOfVersionDate)
        }

        //Setting up the signal configuration.
        executedConfiguration.signalConfiguration = configuration.signalConfiguration

        //handle PersistentLists and PersistentSets individually so we don't point to same collection object
        configuration?.tags?.each { executedConfiguration.addToTags(it) }
        configuration?.poiInputsParameterValues?.each {
            executedConfiguration.addToPoiInputsParameterValues(new ParameterValue(key: it.key, value: it.value))
        }
        if (configuration.instanceOf(PeriodicReportConfiguration)) {
            configuration?.attachments?.each { att ->
                executedConfiguration.addToAttachments(new ExecutedPublisherSource(name: att.name,
                        data: att.data, sortNumber: att.sortNumber, userGroup: att.userGroup, ext: att.ext,
                        path: att.path, fileSource: att.fileSource, fileType: att.fileType,script: att.script,
                        oneDriveFolderName: att.oneDriveFolderName, oneDriveFolderId: att.oneDriveFolderId,
                        oneDriveSiteId: att.oneDriveSiteId, oneDriveUserSettings: att.oneDriveUserSettings
                ));
            }
        }

        configuration.templateQueries.each {
            getPersistedReportResult(it, executedConfiguration, configuration.getOwner(),withOutResult)
            if (!(configuration instanceof IcsrProfileConfiguration)) {
                queryService.updateLastExecutionDate(it.query)
                templateService.updateLastExecutionDate(it.template)
            }
        }

        configuration.templateQueries.each {
            adjustCustomDateRanges(it)
        }
        if(configuration.instanceOf(PeriodicReportConfiguration) && configuration.generatedReportName){
            try {
                String generatedName = emailService.insertValues(configuration.generatedReportName, executedConfiguration)
                if (generatedName.size() > 255) generatedName = generatedName.substring(0, 255)
                executedConfiguration.generatedReportName = generatedName
            }catch (Exception e){
                executedConfiguration.generatedReportName = configuration.generatedReportName
            }
        }
        return executedConfiguration
    }

    private adjustCustomDateRanges(TemplateQuery templateQuery) {
        if (templateQuery instanceof IcsrTemplateQuery) {
            return
        }
        ReportConfiguration configuration = templateQuery.report
        def runOnce = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.RUN_ONCE)
        def hourly = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.HOURLY)
        def minutely = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.MINUTELY)

        if (!runOnce && !hourly && !minutely) {
            if (templateQuery.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute &&
                    templateQuery.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute) {
                Period period = configurationService.getDeltaPeriod(configuration)
                templateQuery.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute =
                        configurationService.getNextDateAsPerScheduler(templateQuery.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute, period)
                templateQuery.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute =
                        configurationService.getNextDateAsPerScheduler(templateQuery.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute, period)
            }
        }
    }


    private void addExecutedGlobalQueryRelated(ReportConfiguration configuration, ExecutedReportConfiguration executedReportConfiguration) {

        if (configuration.globalDateRangeInformation) {
            Date startDate = configuration.globalDateRangeInformation?.getReportStartAndEndDate()[0] ?: new Date()
            Date endDate = configuration.globalDateRangeInformation?.getReportStartAndEndDate()[1] ?: new Date()
            if(configuration.globalDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE && !(configuration instanceof IcsrProfileConfiguration)&& !(configuration instanceof PeriodicReportConfiguration) && !(configuration instanceof IcsrReportConfiguration) ){
                endDate = configuration.reportMinMaxDate[1]
            }
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
            configuration.globalQuery = GrailsHibernateUtil.unwrapIfProxy(configuration.globalQuery)
            if (configuration.globalQuery instanceof QuerySet) {
                executedReportConfiguration.executedGlobalQuery = queryService.createExecutedQuery(configuration.globalQuery)
            } else {
                Long exId = SuperQuery.getLatestExQueryByOrigQueryId(Long.valueOf(configuration.globalQuery.id)).get()
                executedReportConfiguration.executedGlobalQuery = SuperQuery.read(exId)
            }

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

    void getPersistedReportResult(TemplateQuery templateQuery, ExecutedReportConfiguration executedConfiguration, User owner, boolean withOutResult = false) throws Exception {
        ReportTemplate  template = GrailsHibernateUtil.unwrapIfProxy(templateQuery.template)
        if (template instanceof TemplateSet) {
            /*
             * If template type is TemplateSet and sectionBreakByEachTemplate is false,
             * creates executed template query and report result for TemplateSet also
             */
            if (!template.sectionBreakByEachTemplate) {
                createExecutedTemplateQueryAndReportResult(templateQuery, executedConfiguration, owner, templateQuery.template, withOutResult)
            } else {
                /*
                 * If template type is TemplateSet, creates executed template query
                 * and report result for each nested template
                 */
                ((TemplateSet) template).nestedTemplates.each {
                    createExecutedTemplateQueryAndReportResult(templateQuery, executedConfiguration, owner, GrailsHibernateUtil.unwrapIfProxy(it), withOutResult)
                }
            }
        } else {
            createExecutedTemplateQueryAndReportResult(templateQuery, executedConfiguration, owner, template, withOutResult)
        }
    }

    void createExecutedTemplateQueryAndReportResult(TemplateQuery templateQuery, ExecutedReportConfiguration executedConfiguration, User owner, ReportTemplate template, boolean withOutResult = false) {
        ExecutedTemplateQuery executedTemplateQuery = addExecutedTemplateQueryToExecutedConfiguration(templateQuery, template)
        if (!withOutResult) {
        ReportResult result = new ReportResult(executionStatus: ReportExecutionStatusEnum.SCHEDULED, scheduledBy: owner)
        CRUDService.userService.setOwnershipAndModifier(result)
        executedTemplateQuery.setReportResult(result)
        }
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
    }

    def checkDDAndCreateExecuted(ReportTemplate template){
        ReportTemplate executedTemplate = null
        if(template instanceof DataTabulationTemplate){
            boolean checkIfDrilldown = false
            for (int i = 0; i < template.columnMeasureList.size(); i++) {
                for (int j = 0; j < template.columnMeasureList[i].measures.size(); j++) {
                    if (template.columnMeasureList[i].measures[j].drillDownTemplate) {
                        executedTemplate = createReportTemplate(template)
                        checkIfDrilldown = true
                        break
                    }
                }
                if (checkIfDrilldown) {
                    break
                }
            }
        }

        else if(template instanceof CustomSQLTemplate || template instanceof NonCaseSQLTemplate){
            if(template.drillDownTemplate && template.id != template.drillDownTemplate.id){
                executedTemplate = createReportTemplate(template)
            }
        }
        return executedTemplate
    }


    /**
     * Creates an executedTemplateQueries from a TemplateQuery and adds it to an ExecutedConfiguration
     * @param executedConfig
     * @param templateQuery
     */
    private addExecutedTemplateQueryToExecutedConfiguration(TemplateQuery templateQuery, ReportTemplate template) throws Exception {

        //ExecutedTemplate
        ReportTemplate executedTemplate = null
        template = GrailsHibernateUtil.unwrapIfProxy(template)
        if (template instanceof ITemplateSet) {
            executedTemplate = createReportTemplate(template)
        }
        else{
            executedTemplate = checkDDAndCreateExecuted(template)
        }

        if(executedTemplate == null) {
            Long exId = ReportTemplate.getLatestExRptTempltByOrigTempltId(Long.valueOf(template.id)).get()
            executedTemplate = ReportTemplate.read(exId)
            if(template.ciomsI || template.medWatch) {
                String toValidate = CustomSQLTemplate.getSqlQueryToValidate(template)
                template.columnNamesList = sqlService.getColumnsFromSqlQuery(toValidate, false, false).toListString()
                if (!(template.columnNamesList.equals(executedTemplate.columnNamesList))) {
                    executedTemplate = createReportTemplate(template)
                    CRUDService.updateWithoutAuditLog(template)
                }
            }
        }
        ReportConfiguration reportConfiguration = templateQuery.report
        Date startDate = templateQuery?.dateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[0] ?: new Date()
        Date endDate = templateQuery?.dateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[1] ?: new Date()
        if (templateQuery.dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
            endDate = reportConfiguration.reportMinMaxDate[1] ?: endDate
        }
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum,
                relativeDateRangeValue: templateQuery?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue,
                dateRangeEndAbsolute: endDate, dateRangeStartAbsolute: startDate,
                //TODO: for version as of generated date should be implemented
                executedAsOfVersionDate: reportConfiguration.getAsOfVersionDateCustom(true) ?: endDate
        )

        SuperQuery executedQuery = null

        if(templateQuery.query) {
            templateQuery.query = GrailsHibernateUtil.unwrapIfProxy(templateQuery.query)
            if (templateQuery.query instanceof QuerySet) {
                executedQuery = queryService.createExecutedQuery(templateQuery.query)
            }else {
                Long exId = SuperQuery.getLatestExQueryByOrigQueryId(Long.valueOf(templateQuery.query?.id)).get()
                executedQuery = SuperQuery.read(exId)
            }
        }

        String title = templateQuery?.title
        if (!(templateQuery.report instanceof IcsrProfileConfiguration)) {
            title = title ?: templateQuery.report.reportName
        }

        Map executedTemplateQueryData = [executedTemplate                            : executedTemplate,
                                         executedQuery                               : executedQuery,
                                         executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery,
                                         queryLevel                                  : templateQuery.queryLevel, title: title,
                                         issueType: templateQuery.issueType, rootCause: templateQuery.rootCause, responsibleParty: templateQuery.responsibleParty, assignedToUser: templateQuery.assignedToUser, assignedToGroup: templateQuery.assignedToGroup, priority: templateQuery.priority,
                                         investigation: templateQuery.investigation, summary: templateQuery.summary, actions: templateQuery.actions, investigationSql: templateQuery.investigationSql, actionsSql: templateQuery.actionsSql, summarySql: templateQuery.summarySql,
                                         header                                      : templateQuery?.header, footer: templateQuery?.footer,
                                         createdBy                                   : templateQuery.createdBy, modifiedBy: templateQuery.modifiedBy, templtReassessDate: templateQuery?.templtReassessDate,
                                         headerProductSelection                      : templateQuery?.headerProductSelection, headerDateRange: templateQuery?.headerDateRange, granularity: templateQuery?.granularity, reassessListednessDate: templateQuery?.reassessListednessDate,
                                         draftOnly                                   : templateQuery?.draftOnly, blindProtected: templateQuery?.blindProtected, privacyProtected: templateQuery?.privacyProtected, displayMedDraVersionNumber: templateQuery?.displayMedDraVersionNumber ?: false]

        ExecutedTemplateQuery executedTemplateQuery = null
        if (templateQuery instanceof IcsrTemplateQuery) {
            executedTemplateQuery = new ExecutedIcsrTemplateQuery(executedTemplateQueryData)
            String msgTypeName = null
            Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
            MessageType.'pva'.withNewTransaction {
                msgTypeName = MessageType.'pva'.findByIdAndLangId(templateQuery.icsrMsgType, langId)?.description
            }
            executedTemplateQuery.with {
                authorizationType = templateQuery.authorizationType
                productType = templateQuery.productType
                dueInDays = templateQuery.dueInDays
                icsrMsgType = templateQuery.icsrMsgType
                icsrMsgTypeName = msgTypeName
                distributionChannelName = templateQuery.distributionChannelName
                orderNo = templateQuery.orderNo
                emailConfiguration = templateQuery.emailConfiguration && !templateQuery.emailConfiguration.isDeleted ? new EmailConfiguration(templateQuery.emailConfiguration.properties) : null
                isExpedited = templateQuery.isExpedited
            }
        } else {
            executedTemplateQuery = new ExecutedTemplateQuery(executedTemplateQueryData)
        }
        executedTemplateQuery.userGroup = templateQuery.userGroup
        executedTemplateQuery.dueInDays = templateQuery.dueInDays
        templateQuery.templateValueLists.each {
            ExecutedTemplateValueList executedTVL = new ExecutedTemplateValueList(template: it.template)
            it.parameterValues.each {
                ParameterValue executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                executedTVL.addToParameterValues(executedValue)
            }
            executedTemplateQuery.addToExecutedTemplateValueLists(executedTVL)
        }

        templateQuery.queryValueLists.each {
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
            executedTemplateQuery.addToExecutedQueryValueLists(executedQVL)
        }
        return executedTemplateQuery
    }

    ReportTemplate createReportTemplate(ReportTemplate template) throws Exception {
        template = GrailsHibernateUtil.unwrapIfProxy(template)
        ReportTemplate executedTemplate = null
        if (template?.instanceOf(NonCaseSQLTemplate)) {
            executedTemplate = new ExecutedNonCaseSQLTemplate(template.properties)
            if(template?.drillDownTemplate && template.id != template.drillDownTemplate.id) {
                executedTemplate.drillDownTemplate = createReportTemplate(template?.drillDownTemplate)
            }
            executedTemplate.customSQLValues = []
            template.customSQLValues.each {
                executedTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (template?.instanceOf(CustomSQLTemplate)) {
            executedTemplate = new ExecutedCustomSQLTemplate(template.properties)
            if(template?.drillDownTemplate && template.id != template.drillDownTemplate.id){
                executedTemplate.drillDownTemplate = createReportTemplate(template?.drillDownTemplate)
            }
            executedTemplate.customSQLValues = []
            template.customSQLValues.each {
                executedTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (template?.instanceOf(DataTabulationTemplate)) {
            executedTemplate = createExecutedDataTabulationTemplate(template)
        } else if (template?.instanceOf(CaseLineListingTemplate)) {
            CaseLineListingTemplate cllTemplate = (CaseLineListingTemplate) template
            executedTemplate = new ExecutedCaseLineListingTemplate(template.properties)
            executedTemplate.columnList = null
            ReportFieldInfoList cllColumns = new ReportFieldInfoList()
            cllTemplate.columnList?.reportFieldInfoList?.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                cllColumns.addToReportFieldInfoList(reportFieldInfo)
            }
            executedTemplate.columnList = cllColumns
        } else if (template?.instanceOf(TemplateSet)) {
            TemplateSet templateSet = (TemplateSet) template
            executedTemplate = new ExecutedTemplateSet(templateSet.properties)
            executedTemplate.linkSectionsByGrouping = templateSet.linkSectionsByGrouping
            executedTemplate.nestedTemplates = []
            templateSet.nestedTemplates.each {
                if (template instanceof ExecutedTemplateSet) {//condition for report comparison feature
                    ReportTemplate executedNestedTemplate = createReportTemplate(GrailsHibernateUtil.unwrapIfProxy(it))
                    executedNestedTemplate.originalTemplateId = it.id
                    CRUDService.saveWithoutAuditLog(executedNestedTemplate)
                    executedTemplate.addToNestedTemplates(executedNestedTemplate)
                } else {
                    Long exId = ReportTemplate.getLatestExRptTempltByOrigTempltId(Long.valueOf(it.id)).get()
                    ReportTemplate current = ReportTemplate.read(exId)
                    executedTemplate.addToNestedTemplates(current)
                }
            }
        } else if (template?.instanceOf(XMLTemplate)) {
            XMLTemplate xmlTemplate = (XMLTemplate) template
            Map templateProps = MiscUtil.getObjectProperties(xmlTemplate)
            templateProps.remove('nestedTemplates')
            templateProps.remove('rootNode')
            executedTemplate = new ExecutedXMLTemplate(templateProps)
            executedTemplate.nestedTemplates = []
            executedTemplate.rootNode = null
            xmlTemplate.nestedTemplates.each {
                if (template instanceof ExecutedXMLTemplate) {//condition for report comparison feature
                    ReportTemplate executedNestedTemplate = createReportTemplate(GrailsHibernateUtil.unwrapIfProxy(it))
                    executedNestedTemplate.originalTemplateId = it.id
                    CRUDService.saveWithoutAuditLog(executedNestedTemplate)
                    executedTemplate.addToNestedTemplates(executedNestedTemplate)
                } else {
                    Long exId = ReportTemplate.getLatestExRptTempltByOrigTempltId(Long.valueOf(it.id)).get()
                    ReportTemplate current = ReportTemplate.read(exId)
                    executedTemplate.addToNestedTemplates(current)
                }
            }
            executedTemplate.rootNode = createExecutedXmlTemplateNode(executedTemplate, xmlTemplate.rootNode)
        }
        if (template.originalTemplateId == 0){
            executedTemplate.originalTemplateId = template.id
        }
        setSharedWithTemplate(executedTemplate, template)
        CRUDService.saveWithoutAuditLog(executedTemplate)

        return executedTemplate
    }

    private createExecutedDataTabulationTemplate(DataTabulationTemplate template) {
        Map properties = new HashMap<>(template.properties)
        List<DataTabulationColumnMeasure> savedColumnMeasureList = []
        ExecutedDataTabulationTemplate executedDataTabulationTemplate = new ExecutedDataTabulationTemplate(properties)
        executedDataTabulationTemplate.columnMeasureList = []
        template.columnMeasureList.each { columnMeasure ->
            DataTabulationColumnMeasure dtColumnMeasure = new DataTabulationColumnMeasure(columnMeasure.properties)
            dtColumnMeasure.columnList = null
            dtColumnMeasure.measures = []

            columnMeasure.measures.each { measure ->
                DataTabulationMeasure dtMeasure = new DataTabulationMeasure(measure.properties)
                dtMeasure.percentageAxisLabel = measure.percentageAxisLabel
                dtMeasure.valueAxisLabel = measure.valueAxisLabel
                if (measure.drillDownTemplate) {
                    ExecutedCaseLineListingTemplate executedCaseLineListingTemplate = executedConfigurationService.createReportTemplate(measure.drillDownTemplate)
                    dtMeasure.drillDownTemplate = executedCaseLineListingTemplate
                }
                dtColumnMeasure.addToMeasures(dtMeasure)
            }

            ReportFieldInfoList dtColumns = new ReportFieldInfoList()
            columnMeasure.columnList?.reportFieldInfoList?.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                dtColumns.addToReportFieldInfoList(reportFieldInfo)
            }
            dtColumnMeasure.columnList = dtColumns
            savedColumnMeasureList.add(dtColumnMeasure)
        }
        savedColumnMeasureList.each {
            executedDataTabulationTemplate.addToColumnMeasureList(it)
        }
        // this is causing an issue, executedDataTabulationTemplate no workie
        executedDataTabulationTemplate.originalTemplateId = template.id
        return executedDataTabulationTemplate
    }


    private XMLTemplateNode createExecutedXmlTemplateNode(XMLTemplate exEmlTemplate, XMLTemplateNode original) {
        Map properties = new HashMap<>(MiscUtil.getObjectProperties(original))
        properties['children'] = null
        properties['parent'] = null
        properties['template'] = null
        XMLTemplateNode copy = new XMLTemplateNode(properties)
        copy.sourceFieldLabelVal = copy.sourceFieldLabel ? getSourceFieldLabel(copy.sourceFieldLabel) : null
        copy.template = original.template ? exEmlTemplate.nestedTemplates.find {
            it.originalTemplateId == original.template.id
        } : null
        //Because in Executed CLL templates reportFieldInfo doesn't change from original, it uses same reference of original so no need to copy.
        for (XMLTemplateNode child : original.children) {
            copy.addToChildren(createExecutedXmlTemplateNode(exEmlTemplate, child))
        }
        return copy
    }

    @ReadOnly(connection = 'pva')
    String getSourceFieldLabel(String label) {
        return PvcmFieldLabel.read(label)?.displayText
    }

    private void setSharedWithTemplate(ReportTemplate executedTemplate, ReportTemplate template) {
        executedTemplate.userTemplates = null
        executedTemplate.userGroupTemplates = null
        executedTemplate.templateUserStates = null
        if (template.userTemplates) {
            template.userTemplates.each { userTemplate ->
                executedTemplate.addToUserTemplates(new UserTemplate(user: userTemplate.user))
            }
        }
        if (template.userGroupTemplates) {
            template.userGroupTemplates.each { userGroupTemplate ->
                executedTemplate.addToUserGroupTemplates(new UserGroupTemplate(userGroup: userGroupTemplate.userGroup))
            }
        }
        if (template.templateUserStates) {
            template.templateUserStates.each {templateUserState ->
                executedTemplate.addToTemplateUserStates(new TemplateUserState(user: templateUserState.user, isFavorite: templateUserState.isFavorite))
            }
        }
    }


    private ExecutedInboundCompliance executeInboundForConfiguration(InboundCompliance inboundCompliance) throws Exception {
        ExecutedInboundCompliance executedConfiguration = null
        log.info("Executing Inbound Compliance: (ID: ${inboundCompliance.id})")
        try {
            if (!inboundCompliance.isDisabled) {
                // Validating Configuration once again before using
                if (!inboundCompliance.validate()) {
                    throw new ValidationException("Validation Exception in Configuration", inboundCompliance.errors)
                }
                executedConfiguration = createExecutedInboundConfiguration(inboundCompliance, true)
                CRUDService.instantSaveWithoutAuditLog(executedConfiguration)
            }
        } catch (Exception e) {
            log.error("Unable to finish running lockedConfiguration.id=${inboundCompliance.id}", e)
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

    @Transactional(propagation = Propagation.REQUIRED)
    ExecutedInboundCompliance createExecutedInboundConfiguration(InboundCompliance configuration, boolean withOutResult = false) throws Exception {

        ExecutedInboundCompliance executedConfiguration
        Map mapData = [senderName                : configuration.senderName,
                       owner                     : User.read(configuration.owner.id),
                       locale                    : configuration.owner.preference.locale,
                       description               : configuration.description,
                       dateCreated               : configuration.dateCreated,
                       lastUpdated               : configuration.lastUpdated,
                       isDeleted                 : configuration.isDeleted,
                       isDisabled                : configuration.isDisabled,
                       dateRangeType             : configuration.dateRangeType,
                       sourceProfile             : configuration.sourceProfile,
                       productSelection          : configuration.productSelection,
                       studySelection            : configuration.studySelection,
                       productGroupSelection     : configuration.productGroupSelection,
                       excludeNonValidCases      : configuration.excludeNonValidCases,
                       excludeDeletedCases       : configuration.excludeDeletedCases,
                       suspectProduct            : configuration.suspectProduct,
                       createdBy                 : configuration.getOwner().username,
                       modifiedBy                : configuration.modifiedBy,
                       status                    : ReportExecutionStatusEnum.GENERATING,
                       blankValuesJSON           : configuration?.blankValuesJSON,
                       qualityChecked            : configuration.qualityChecked,
                       tenantId                  : configuration.tenantId,
                       isICInitialize            : configuration.isICInitialize,
                       inboundCompliance         : configuration,
                       isMultiIngredient         : configuration.isMultiIngredient,
                       numOfICExecutions         : configuration.numOfICExecutions+1,
                       includeWHODrugs      : configuration.includeWHODrugs
        ]
        executedConfiguration = new ExecutedInboundCompliance(mapData)
//        executedConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(configuration)

        addExecutedInboundGlobalQueryRelated(configuration, executedConfiguration)

        //handle PersistentLists and PersistentSets individually so we don't point to same collection object
        configuration?.tags?.each { executedConfiguration.addToTags(it) }
        configuration?.poiInputsParameterValues?.each {
            executedConfiguration.addToPoiInputsParameterValues(new ParameterValue(key: it.key, value: it.value))
        }

        configuration.queriesCompliance.each {
            getPersistedReportResult(it, executedConfiguration, configuration.getOwner(),withOutResult)
            queryService.updateLastExecutionDate(it.query)
        }
        return executedConfiguration
    }

    private void addExecutedInboundGlobalQueryRelated(InboundCompliance configuration, ExecutedInboundCompliance executedInboundConfiguration) {

        if (configuration.globalDateRangeInbound) {
            Date startDate = configuration.globalDateRangeInbound?.getReportStartAndEndDate()[0] ?: new Date()
            Date endDate = configuration.globalDateRangeInbound?.getReportStartAndEndDate()[1] ?: new Date()
            executedInboundConfiguration.executedGlobalDateRangeInbound = new ExecutedGlobalDateRangeInbound(
                    dateRangeEnum: configuration.globalDateRangeInbound?.dateRangeEnum,
                    relativeDateRangeValue: configuration.globalDateRangeInbound?.relativeDateRangeValue,
                    dateRangeEndAbsolute: endDate, dateRangeStartAbsolute: startDate,
                    executedInboundConfiguration: executedInboundConfiguration
            )
        }
    }

    void getPersistedReportResult(QueryCompliance queryCompliance, ExecutedInboundCompliance executedConfiguration, User owner, boolean withOutResult = false) throws Exception {

        ExecutedQueryCompliance executedQueryCompliance = addExecutedQueryComplianceToExecutedConfiguration(queryCompliance)
        executedConfiguration.addToExecutedQueriesCompliance(executedQueryCompliance)

    }

    private addExecutedQueryComplianceToExecutedConfiguration(QueryCompliance queryCompliance) throws Exception {

        SuperQuery executedQuery = null
        if(queryCompliance.query) {
            queryCompliance.query = GrailsHibernateUtil.unwrapIfProxy(queryCompliance.query)
            if (queryCompliance.query instanceof QuerySet) {
                executedQuery = queryService.createExecutedQuery(queryCompliance.query)
            }else {
                Long exId = SuperQuery.getLatestExQueryByOrigQueryId(Long.valueOf(queryCompliance.query?.id)).get()
                executedQuery = SuperQuery.read(exId)
            }
        }
        Map executedQueryComplianceData = [criteriaName                                : queryCompliance.criteriaName,
                                           executedQuery                               : executedQuery,
                                           createdBy                                   : queryCompliance.createdBy,
                                           modifiedBy                                  : queryCompliance.modifiedBy,
                                           allowedTimeframe                            : queryCompliance.allowedTimeframe]

        ExecutedQueryCompliance executedQueryCompliance = new ExecutedQueryCompliance(executedQueryComplianceData)

        queryCompliance.queryValueLists.each {
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
            executedQueryCompliance.addToExecutedQueryValueLists(executedQVL)
        }
        return executedQueryCompliance
    }

}
