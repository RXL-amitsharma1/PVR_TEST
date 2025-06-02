package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.caseSeries.integration.CaseSeriesListDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.enums.*
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.NotTransactional
import grails.validation.ValidationException
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.PeriodList
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.RRule
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat

@Transactional
class CaseSeriesService {

    def CRUDService
    def userService
    def notificationService
    def reportExecutorService
    def queryService
    def customMessageService
    def utilService
    def reportService

    /**
     * Service method to fetch the case series based on id.
     * @param id
     * @return
     */
    def getCaseSeriesById(id) {
        CaseSeries.get(id)
    }

    /**
     * Service method to save the case series.
     * @param caseSeriesInstance
     * @return
     * @throws ValidationException
     */
    def save(ExecutedCaseSeries executedCaseSeries, Set caseNumberAndVersion) throws ValidationException {
        if (ExecutedCaseSeries.countBySeriesNameIlike(executedCaseSeries.seriesName) == 0) {
            CRUDService.save(executedCaseSeries)
            Set<String> warnings = reportExecutorService.saveCaseSeriesInDB(caseNumberAndVersion, executedCaseSeries)
            if(warnings){
                throw new ValidationException("Case series error while saving to PV Mart : ${warnings.join(",")} ",executedCaseSeries.errors)
            }
        } else {
            executedCaseSeries.errors.rejectValue("seriesName", "exist")
            throw new ValidationException("Case series with same name exists", executedCaseSeries.errors)
        }
    }

    /**
     * Service method to update the case series.
     * @param caseSeriesInstance
     * @return
     * @throws ValidationException
     */
    def update(caseSeriesInstance) throws ValidationException {
        CRUDService.update(caseSeriesInstance)
    }

    /**
     * Service method to delete the case series.
     * @param taskTemplateInstace
     * @return
     */
    def delete(taskTemplateInstace) throws ValidationException {
        CRUDService.delete(taskTemplateInstace)
    }

    def setOwnerAndNameForPreview(ExecutedCaseSeries caseSeriesInstance) {
        caseSeriesInstance.seriesName = Constants.PREVIEW_QUERY + caseSeriesInstance.executedGlobalQuery.name
        caseSeriesInstance.owner = userService.getUser()
        caseSeriesInstance.isTemporary = true
        return caseSeriesInstance
    }

    def deleteTemporaryCaseSeries() {
        List<ExecutedCaseSeries> executedCaseSeriesList = ExecutedCaseSeries.findAllByIsTemporaryAndLastUpdatedLessThanEquals(true, new Date() - 1)
        executedCaseSeriesList.each { executedCaseSeries ->
            Tenants.withId(executedCaseSeries.tenantId as Integer){
                notificationService.deleteNotification(executedCaseSeries?.id, NotificationApp.CASESERIES)
                reportExecutorService.removePreviewCaseSeries(executedCaseSeries, executedCaseSeries.owner)
                ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.findByCaseSeries(executedCaseSeries)
                if (executedConfiguration) {
                    executedConfiguration.caseSeries = null
                    CRUDService.updateWithoutAuditLog(executedConfiguration)
                }
                executedConfiguration = ExecutedReportConfiguration.findByCumulativeCaseSeries(executedCaseSeries)
                if (executedConfiguration) {
                    executedConfiguration.cumulativeCaseSeries = null
                    CRUDService.updateWithoutAuditLog(executedConfiguration)
                }
                ExecutedConfiguration executedConfig = ExecutedConfiguration.findByUsedCaseSeries(executedCaseSeries)
                if (executedConfig) {
                    executedConfig.usedCaseSeries = null
                    CRUDService.updateWithoutAuditLog(executedConfig)
                }
                CRUDService.delete(executedCaseSeries)
            }
        }
    }

    ExecutedCaseSeries updateDetailsFrom(CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeries) {
        ExecutedCaseDeliveryOption caseExecutedDeliveryOption = new ExecutedCaseDeliveryOption(
                sharedWith: caseSeries?.deliveryOption?.sharedWith,
                sharedWithGroup: caseSeries?.deliveryOption?.sharedWithGroup,
                emailToUsers: caseSeries?.deliveryOption?.emailToUsers,
                attachmentFormats: caseSeries?.deliveryOption?.attachmentFormats)
        executedCaseSeries.with {
            if (!executedDeliveryOption)
                executedDeliveryOption = caseExecutedDeliveryOption
            seriesName = seriesName ?: caseSeries.seriesName
            tenantId = caseSeries.tenantId
            description = caseSeries.description
            dateRangeType = caseSeries.dateRangeType
            asOfVersionDate = caseSeries.asOfVersionDate
            evaluateDateAs = caseSeries.evaluateDateAs
            excludeFollowUp = caseSeries.excludeFollowUp
            includeLockedVersion = caseSeries.includeLockedVersion
            includeAllStudyDrugsCases = caseSeries.includeAllStudyDrugsCases
            excludeNonValidCases = caseSeries.excludeNonValidCases
            excludeDeletedCases = caseSeries.excludeDeletedCases
            suspectProduct = caseSeries.suspectProduct
            productSelection = caseSeries.productSelection
            productGroupSelection = caseSeries.productGroupSelection
            studySelection = caseSeries.studySelection
            eventSelection = caseSeries.eventSelection
            eventGroupSelection = caseSeries.eventGroupSelection
            numExecutions = caseSeries.numExecutions
            qualityChecked = caseSeries.qualityChecked
            emailConfiguration = caseSeries.emailConfiguration ? (EmailConfiguration) CRUDService.save(new EmailConfiguration(caseSeries.emailConfiguration.properties)) : null
            owner = caseSeries.owner
            locale = caseSeries.locale
            tenantId = caseSeries.tenantId
            nextRunDate = caseSeries.nextRunDate
            isMultiIngredient = caseSeries.isMultiIngredient
            includeWHODrugs = caseSeries.includeWHODrugs
        }
        caseSeries.tags?.each { executedCaseSeries.addToTags(it) }
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = executedCaseSeries.executedCaseSeriesDateRangeInformation
        CaseSeriesDateRangeInformation caseSeriesDateRangeInformation = caseSeries.caseSeriesDateRangeInformation
        if (!executedCaseSeriesDateRangeInformation) {
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation()
            executedCaseSeries.executedCaseSeriesDateRangeInformation = executedCaseSeriesDateRangeInformation
        }
        executedCaseSeriesDateRangeInformation.with {
            relativeDateRangeValue = caseSeriesDateRangeInformation.relativeDateRangeValue
            dateRangeEnum = caseSeriesDateRangeInformation.dateRangeEnum

            if (dateRangeEnum != DateRangeEnum.CUSTOM && dateRangeEnum != DateRangeEnum.CUMULATIVE) {
                def startAndEndDate = RelativeDateConverter.(dateRangeEnum.value())(new Date(), relativeDateRangeValue ?: 1, 'UTC')
                dateRangeStartAbsolute = startAndEndDate[0]
                dateRangeEndAbsolute = startAndEndDate[1]
            } else if (dateRangeEnum == DateRangeEnum.CUSTOM) {
                dateRangeStartAbsolute = caseSeriesDateRangeInformation.dateRangeStartAbsolute
                dateRangeEndAbsolute = caseSeriesDateRangeInformation.dateRangeEndAbsolute
            } else {
                dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
                dateRangeEndAbsolute = caseSeriesDateRangeInformation?.asOfVersionDate ?: new Date()
            }
        }

        executedCaseSeries.executedGlobalQueryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        executedCaseSeries.executedGlobalQueryValueLists?.clear()
        executedCaseSeries.executedGlobalQuery = queryService.createExecutedQuery(caseSeries.globalQuery)
        caseSeries.globalQueryValueLists?.each {
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
            executedCaseSeries.addToExecutedGlobalQueryValueLists(executedQVL)
        }
        executedCaseSeries
    }

    CaseSeries copyCaseSeries(CaseSeries originalCaseSeries) {
        User currentUser = userService.getUser()
        CaseDeliveryOption newDeliveryOption = new CaseDeliveryOption(sharedWith: [currentUser], emailToUsers: originalCaseSeries.deliveryOption.emailToUsers, attachmentFormats: originalCaseSeries.deliveryOption.attachmentFormats)

        CaseSeriesDateRangeInformation newCaseSeriesDateRangeInformation = new CaseSeriesDateRangeInformation(
                dateRangeEndAbsolute: originalCaseSeries.caseSeriesDateRangeInformation.dateRangeEndAbsolute,
                dateRangeStartAbsolute: originalCaseSeries.caseSeriesDateRangeInformation.dateRangeStartAbsolute,
                relativeDateRangeValue: originalCaseSeries.caseSeriesDateRangeInformation.relativeDateRangeValue,
                dateRangeEnum: originalCaseSeries.caseSeriesDateRangeInformation.dateRangeEnum)

        CaseSeries newCaseSeries = new CaseSeries(
                seriesName: generateUniqueNameForCaseSeries(originalCaseSeries.seriesName, currentUser),
                emailConfiguration: (originalCaseSeries.emailConfiguration && !originalCaseSeries.emailConfiguration.isDeleted) ? (EmailConfiguration) CRUDService.save(new EmailConfiguration(originalCaseSeries.emailConfiguration.properties)) : null,
                deliveryOption: newDeliveryOption,
                caseSeriesDateRangeInformation: newCaseSeriesDateRangeInformation,
                globalQuery: originalCaseSeries.globalQuery,
                description: originalCaseSeries.description,
                dateRangeType: originalCaseSeries.dateRangeType,
                asOfVersionDate: originalCaseSeries.asOfVersionDate,
                evaluateDateAs: originalCaseSeries.evaluateDateAs,
                excludeFollowUp: originalCaseSeries.excludeFollowUp,
                includeLockedVersion: originalCaseSeries.includeLockedVersion,
                includeAllStudyDrugsCases: originalCaseSeries.includeAllStudyDrugsCases,
                excludeNonValidCases: originalCaseSeries.excludeNonValidCases,
                excludeDeletedCases: originalCaseSeries.excludeDeletedCases,
                suspectProduct: originalCaseSeries.suspectProduct,
                qualityChecked: false,
                productSelection: originalCaseSeries.productSelection,
                productGroupSelection: originalCaseSeries.productGroupSelection,
                studySelection: originalCaseSeries.studySelection,
                eventSelection: originalCaseSeries.eventSelection,
                eventGroupSelection: originalCaseSeries.eventGroupSelection,
                owner: currentUser,
                locale: originalCaseSeries.locale,
                createdBy: currentUser.username,
                modifiedBy: currentUser.username,
                isDeleted: originalCaseSeries.isDeleted,
                tags: originalCaseSeries.tags,
                tenantId: originalCaseSeries.tenantId,
                isMultiIngredient: originalCaseSeries.isMultiIngredient,
                includeWHODrugs: originalCaseSeries.includeWHODrugs
        )
        originalCaseSeries.globalQueryValueLists?.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    queryValueList.addToParameterValues(new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value, specialKeyValue: it.specialKeyValue))
                } else {
                    queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                            value: it.value))
                }
            }
            newCaseSeries.addToGlobalQueryValueLists(queryValueList)
        }
        CRUDService.save(newCaseSeries)
        return newCaseSeries
    }

    String generateUniqueNameForCaseSeries(String seriesName, User owner) {
        String prefix = ViewHelper.getMessage("app.configuration.copy.of") + " "
        String newName = trimName(prefix,seriesName,"")
        if (CaseSeries.countBySeriesNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
            int count = 1
            newName = trimName(prefix,seriesName," ($count)")
            while (CaseSeries.countBySeriesNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
                newName = trimName(prefix,seriesName," (${++count})")
            }
        }
        return newName
    }

    String trimName(String prefix, String name, String postfix) {
        int maxSize = CaseSeries.constrainedProperties.seriesName.maxSize - 2
        int overflow = prefix.length() + name.length() + postfix.length() - maxSize
        if (overflow > 0) {
            return prefix + name.substring(0, maxSize - overflow) + postfix
        }
        return prefix + name + postfix
    }

    def getDateRangeValueForCriteria(ExecutedCaseSeries seriesInstance, Locale locale) {
        if (seriesInstance) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale))
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
            if (seriesInstance.executedCaseSeriesDateRangeInformation.dateRangeEnum.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                String dateValue = sdf.format(seriesInstance.executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute)
                // gives string date original value
                return customMessageService.getMessage("app.dateRangeType.executed.cumulative", dateValue)
            } else {
                String dateStartValue = sdf.format(seriesInstance.executedCaseSeriesDateRangeInformation.dateRangeStartAbsolute)
                // gives string date original value
                String dateEndValue = sdf.format(seriesInstance.executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute)
                // gives string date original value
                if (dateStartValue && dateStartValue == dateEndValue) {
                    return dateStartValue
                }
                return  customMessageService.getMessage("app.dateRangeType.executed.range", dateStartValue, dateEndValue)
            }
        }
    }

    def setFavorite(BaseCaseSeries caseSeries, Boolean isFavorite) {
        User user = userService.getUser()
        def caseSeriesUserState
        if (caseSeries instanceof CaseSeries) {
            caseSeriesUserState = CaseSeriesUserState.findByUserAndCaseSeries(user, caseSeries)
            if (!caseSeriesUserState)
                caseSeriesUserState = new CaseSeriesUserState(user: user, caseSeries: caseSeries)
        } else {
            caseSeriesUserState = ExecutedCaseSeriesUserState.findByUserAndExecutedCaseSeries(user, caseSeries)
            if (!caseSeriesUserState)
                caseSeriesUserState = new ExecutedCaseSeriesUserState(user: user, executedCaseSeries: caseSeries)
        }
        caseSeriesUserState.isFavorite = isFavorite ? true : null
        caseSeriesUserState.save()
    }

    Map shareExecutedCaseSeries(def params, ExecutedCaseSeries executedCaseSeries) {

        ExecutedCaseDeliveryOption executedDeliveryOption = executedCaseSeries.executedDeliveryOption

        // For Audit Log - Executed Delivery Option
        List<User> previousSharedWith = new ArrayList<User>()
        previousSharedWith.addAll(executedDeliveryOption.sharedWith)
        List<UserGroup>  previousGroupSharedWith = new ArrayList<UserGroup>()
        previousGroupSharedWith.addAll(executedDeliveryOption.sharedWithGroup)

        def allowedUsers = userService.getAllowedSharedWithUsersForCurrentUser();
        def allowedGroups = userService.getAllowedSharedWithGroupsForCurrentUser();

        Set<User> newUsers = []
        Set<UserGroup> newGroups = []
        params.sharedWith?.split(";")?.each { String shared ->
            if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                if (userGroup && allowedGroups.find { it.id == userGroup.id } && !executedDeliveryOption.sharedWithGroup.find { it.id == userGroup.id }) {
                    executedDeliveryOption.addToSharedWithGroup(userGroup)
                    newGroups << userGroup
                }
            } else if (shared.startsWith(Constants.USER_TOKEN)) {
                User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                if (user && allowedUsers.find { it.id == user.id } && !executedDeliveryOption.sharedWith.find { it.id == user.id }) {
                    executedDeliveryOption.addToSharedWith(user)
                    newUsers << user
                }
            }
        }

        CRUDService.saveWithoutAuditLog(executedCaseSeries)
        reportService.updateAuditLogShareWith(executedDeliveryOption, previousSharedWith, previousGroupSharedWith, executedCaseSeries, true)

        return [executedCaseSeries: executedCaseSeries, newUsers: newUsers, newGroups: newGroups]
    }

    boolean isDrillDownToCaseList(ReportResult reportResult){
        ReportTemplate usedTemplate = reportResult.executedTemplateQuery.usedTemplate
        if(usedTemplate instanceof DataTabulationTemplate){
            return usedTemplate.drillDownToCaseList
        }
        return false
    }

    ExecutedCaseSeries createExecutedCaseSeries(ExecutedCaseSeriesDTO executedCaseSeriesDTO) {
        User owner = User.findByUsernameIlike(executedCaseSeriesDTO.ownerName) ?: User.findByUsername(utilService.getApplicationUserForSeeding())
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(
                seriesName: executedCaseSeriesDTO.seriesName,
                description: executedCaseSeriesDTO.description,
                dateRangeType: DateRangeType.findByName(executedCaseSeriesDTO.dateRangeType),
                asOfVersionDate: executedCaseSeriesDTO.asOfVersionDate,
                evaluateDateAs: executedCaseSeriesDTO.evaluateDateAs,
                excludeFollowUp: executedCaseSeriesDTO.excludeFollowUp,
                includeLockedVersion: executedCaseSeriesDTO.includeLockedVersion,
                includeAllStudyDrugsCases: executedCaseSeriesDTO.includeAllStudyDrugsCases,
                excludeNonValidCases: executedCaseSeriesDTO.excludeNonValidCases,
                excludeDeletedCases: executedCaseSeriesDTO.excludeDeletedCases,
                suspectProduct: executedCaseSeriesDTO.suspectProduct,
                isTemporary: executedCaseSeriesDTO.isTemporary,
                productSelection: executedCaseSeriesDTO.productSelection,
                productGroupSelection: executedCaseSeriesDTO.productGroupSelection,
                studySelection: executedCaseSeriesDTO.studySelection,
                eventSelection: executedCaseSeriesDTO.eventSelection,
                eventGroupSelection: executedCaseSeriesDTO.eventGroupSelection,
                executedCaseSeriesDateRangeInformation: createDateRangeInformation(executedCaseSeriesDTO.executedCaseSeriesDateRangeInformation, executedCaseSeriesDTO.asOfVersionDate),
                owner: owner,
                executedGlobalQuery: queryService.createExecutedQuery(SuperQuery.findById(executedCaseSeriesDTO.globalQueryId)),
                executedDeliveryOption: createExecutedCaseDeliveryOption(owner, executedCaseSeriesDTO.sharedWithUsers, executedCaseSeriesDTO.sharedWithGroups),
                createdBy: owner.username,
                modifiedBy: owner.username,
                caseSeriesOwner: Constants.PVS_CASE_SERIES_OWNER,
                numExecutions: ExecutedCaseSeries.countBySeriesName(executedCaseSeriesDTO.seriesName),
                tenantId: executedCaseSeriesDTO.tenantId ?: 1L,
                isMultiIngredient: executedCaseSeriesDTO.isMultiIngredient,
                includeWHODrugs: executedCaseSeriesDTO.includeWHODrugs
        )
        addExecutedGlobalQueryValueLists(executedCaseSeries, executedCaseSeriesDTO.executedGlobalQueryValueLists)

        return (ExecutedCaseSeries) CRUDService.save(executedCaseSeries)
    }

    void addExecutedGlobalQueryValueLists(ExecutedCaseSeries executedCaseSeries, List<QueryValueListDTO> executedGlobalQueryValueLists) {
        executedGlobalQueryValueLists?.each {
            SuperQuery query = queryService.createExecutedQuery(SuperQuery.findById(it.queryId))
            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: query)
            it.parameterValues?.each {
                ParameterValue executedValue
                if (it.reportFieldName) {
                    ReportField reportField = ReportField.findByName(it.reportFieldName)
                    executedValue = new ExecutedQueryExpressionValue(key: it.key,
                            reportField: reportField, operator: it.operator, value: it.value)
                } else {
                    executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                }
                executedQVL.addToParameterValues(executedValue)
            }
            executedCaseSeries.addToExecutedGlobalQueryValueLists(executedQVL)
        }

    }

    ExecutedCaseSeriesDateRangeInformation createDateRangeInformation(ExecutedDateRangeInfoDTO executedCaseSeriesDateRangeInformationDTO, Date asOfVersionDate) {
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation()
        executedCaseSeriesDateRangeInformation.with {
            relativeDateRangeValue = executedCaseSeriesDateRangeInformationDTO.relativeDateRangeValue
            dateRangeEnum = executedCaseSeriesDateRangeInformationDTO.dateRangeEnum

            if (dateRangeEnum != DateRangeEnum.CUSTOM && dateRangeEnum != DateRangeEnum.CUMULATIVE) {
                def startAndEndDate = RelativeDateConverter.(dateRangeEnum.value())(new Date(), relativeDateRangeValue ?: 1, Constants.DEFAULT_SELECTED_TIMEZONE)
                dateRangeStartAbsolute = startAndEndDate[0]
                dateRangeEndAbsolute = startAndEndDate[1]
            } else if (dateRangeEnum == DateRangeEnum.CUSTOM) {
                dateRangeStartAbsolute = executedCaseSeriesDateRangeInformationDTO.dateRangeStartAbsolute
                dateRangeEndAbsolute = executedCaseSeriesDateRangeInformationDTO.dateRangeEndAbsolute
            } else {
                dateRangeStartAbsolute = BaseDateRangeInformation.MIN_DATE
                dateRangeEndAbsolute = asOfVersionDate ?: executedCaseSeriesDateRangeInformationDTO.dateRangeEndAbsolute
            }
        }

        return executedCaseSeriesDateRangeInformation
    }

    ExecutedCaseDeliveryOption createExecutedCaseDeliveryOption(User owner, List<String> sharedWithUsers, List<String> sharedWithGroups) {
        Set<User> users = sharedWithUsers ? User.findAllByUsernameInList(sharedWithUsers) : [owner]
        Set<UserGroup> groups = sharedWithGroups ? UserGroup.findAllByNameInList(sharedWithGroups) : []
        ExecutedCaseDeliveryOption caseExecutedDeliveryOption = new ExecutedCaseDeliveryOption(
                sharedWith: users,
                sharedWithGroup: groups,
                emailToUsers: [],
                attachmentFormats: null)

        return caseExecutedDeliveryOption
    }

    def createExecutionStatus(ExecutedCaseSeries executedCaseSeries, ExecutingEntityTypeEnum executingEntityTypeEnum, String callbackURL = null) {
        ExecutionStatus executionStatus = new ExecutionStatus(
                entityId: executedCaseSeries.id, entityType: executingEntityTypeEnum,
                reportVersion: executedCaseSeries.numExecutions, startTime: System.currentTimeMillis(),
                owner: executedCaseSeries.owner, reportName: executedCaseSeries.seriesName,
                attachmentFormats: executedCaseSeries.executedDeliveryOption?.attachmentFormats,
                sharedWith: executedCaseSeries?.allSharedUsers, callbackURL: callbackURL, callbackStatus: CallbackStatusEnum.UNACKNOWLEDGED,
                tenantId : executedCaseSeries.tenantId)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.frequency = FrequencyEnum.RUN_ONCE
        executionStatus.nextRunDate = new Date()
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

    List<PvrTags> getAllTags(Map params) {
        return PvrTags.findAllByTypeAndNameIlike(params.type,"%${params.term}%",[max:params.max,offset:params.offset])
    }

    List getAllConfiguredCaseSeries(User user,Integer max, Integer offset, String term) {
        CaseSeries.searchBySearchString(user, term).list([max: max, offset: offset, sort: 'seriesName', order: 'asc']).collect {
            [id: it[0], text: it[1]]
        }
    }

    Integer countAllConfiguredCaseSeries(User user,String term) {
        return CaseSeries.countBySearchString(user, term).get()
    }

    Map fetchExecutedCaseSeriesIdByConfigID(Long id) {
        ExecutionStatus executionStatus = ExecutionStatus.findAllByEntityIdAndExecutionStatusAndEntityType(id, ReportExecutionStatusEnum.COMPLETED, ExecutingEntityTypeEnum.CASESERIES, [sort: 'dateCreated', order: 'desc'])[0]
        ExecutedCaseSeries executedCaseSeries = executionStatus ? ExecutedCaseSeries.get(executionStatus.executedEntityId) : null
        if(executedCaseSeries) {
            String startDate = DateUtil.StringFromDate(executedCaseSeries.executedCaseSeriesDateRangeInformation.dateRangeStartAbsolute, "dd-MM-yyyy", null)
            String endDate = DateUtil.StringFromDate(executedCaseSeries.executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute, "dd-MM-yyyy", null)
            String spotfireFileName = executedCaseSeries.associatedSpotfireFile
            return [id: executedCaseSeries.id, dateRange: (startDate + " to " + endDate), spotfireFileName: spotfireFileName]
        }
        return null
    }

    List getAllExeuctedCaseSeriesByUser(User user, Integer max, Integer offset, String term) {
        ExecutedCaseSeries.ownedByAndSharedWithUserAndSearchString(user, term).list([max: max, offset: offset, sort: 'seriesName', order: 'asc']).collect {
            Long id = ExecutedCaseSeries.fetchLatestByOwnerAndSeriesName(it[3], it[0], it[1]).get()?.first()
            [id: id, text: it[0], caseSeriesOwner: it[1], owner: it[2]]
        }
    }

    Integer countAllExeuctedCaseSeriesByUser(User user, String term) {
        return ExecutedCaseSeries.ownedByAndSharedWithUserAndSearchString(user, term).count()
    }

    Map fetchExecutedCaseSeriesIdByExID(Long id) {
        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(id)
        if (executedCaseSeries) {
            User owner = executedCaseSeries?.owner
            String seriesName = executedCaseSeries?.seriesName
            def latestExecutedCaseSeries = ExecutedCaseSeries.fetchByOwnerAndSeriesName(owner.id, seriesName).list([sort: 'dateCreated', order: 'desc'])[0]
            return [id: latestExecutedCaseSeries[0], text: latestExecutedCaseSeries[1], caseSeriesOwner: latestExecutedCaseSeries[2], owner: latestExecutedCaseSeries[3]]
        }
        return null
    }

    def calculateFrequency(CaseSeries caseSeries) {
        if (caseSeries.scheduleDateJSON && caseSeries.nextRunDate) {
            if (caseSeries.scheduleDateJSON.contains(FrequencyEnum.HOURLY.name())) {
                return FrequencyEnum.HOURLY
            } else if (caseSeries.scheduleDateJSON.contains(FrequencyEnum.DAILY.name())) {
                if (caseSeries.scheduleDateJSON.contains("COUNT=1")) {
                    return FrequencyEnum.RUN_ONCE
                }
                return FrequencyEnum.DAILY
            } else if (caseSeries.scheduleDateJSON.contains(FrequencyEnum.WEEKLY.name())) {
                return FrequencyEnum.WEEKLY
            } else if (caseSeries.scheduleDateJSON.contains(FrequencyEnum.MONTHLY.name())) {
                return FrequencyEnum.MONTHLY
            } else if (caseSeries.scheduleDateJSON.contains(FrequencyEnum.YEARLY.name())) {
                return FrequencyEnum.YEARLY
            }
        }
        return FrequencyEnum.RUN_ONCE

    }

    String getScheduledDateJsonAfterDisable(CaseSeries caseSeries) {
        Map parsedScheduledDateJSON = JSON.parse(caseSeries.scheduleDateJSON)
        String startDateTime = parsedScheduledDateJSON?.startDateTime
        String timeZone = parsedScheduledDateJSON?.timeZone?.name
        String scheduledTimeZone = """name" :"${timeZone}","offset" : "${DateUtil.getOffsetString(timeZone)}"""
        return """{"startDateTime":"${
            startDateTime
        }","timeZone":{"${scheduledTimeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""
    }

    def updateExecutedCaseSeriesList(CaseSeriesListDTO caseSeriesListDTO) {
        caseSeriesListDTO.caseSeriesDTOList.each { caseSeriesDTO ->
            ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.findById(caseSeriesDTO.caseSeriesId)
            if (executedCaseSeries) {
                if (executedCaseSeries.caseSeriesOwner == Constants.PVS_CASE_SERIES_OWNER) {
                    executedCaseSeries.executedDeliveryOption?.sharedWith?.clear()
                    executedCaseSeries.executedDeliveryOption?.sharedWithGroup?.clear()
                    User owner = User.findByUsernameIlike(executedCaseSeries.owner?.username) ?: User.findByUsername(utilService.getApplicationUserForSeeding())
                    List<User> users = caseSeriesDTO.sharedWithUsers ? (User.findAllByUsernameInList(caseSeriesDTO.sharedWithUsers) ?: [owner]) : [owner]
                    List<UserGroup> groups = caseSeriesDTO.sharedWithGroups ? UserGroup.findAllByNameInList(caseSeriesDTO.sharedWithGroups) : []
                    executedCaseSeries.executedDeliveryOption.sharedWith = users
                    executedCaseSeries.executedDeliveryOption.sharedWithGroup = groups
                    if (caseSeriesDTO.isTemporary != null)
                        executedCaseSeries.isTemporary = caseSeriesDTO.isTemporary
                    CRUDService.update(executedCaseSeries)
                }
            }
        }
    }
}