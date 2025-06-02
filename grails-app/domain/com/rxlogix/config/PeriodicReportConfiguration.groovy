package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.config.publisher.Gantt
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class PeriodicReportConfiguration extends ReportConfiguration {
    static auditable = [ignore: ["generatedReportName", "executing", "nextRunDate", "totalExecutionTime", "numOfExecutions", "isEnabled"]]
    PeriodicReportTypeEnum periodicReportType
    boolean includePreviousMissingCases = false
    boolean includeOpenCasesInDraft = false
    boolean includeLockedVersion = false //As we don't want to show incase of PeriodicReports
    String primaryReportingDestination
    String generatedReportName
    User primaryPublisherContributor
    String generateSpotfire

    static List<String> propertiesToUseWhileCopying = ['description', 'isDeleted', 'tags', 'dateRangeType', 'sourceProfile', 'productSelection', 'studySelection', 'eventSelection', 'productGroupSelection', 'eventGroupSelection', 'configSelectedTimeZone', 'asOfVersionDate', 'evaluateDateAs', 'excludeFollowUp', 'includeLockedVersion', 'includeOpenCasesInDraft', 'includeAllStudyDrugsCases', 'adjustPerScheduleFrequency', 'suspectProduct', 'includePreviousMissingCases', 'globalQuery', 'generateCaseSeries', 'generateDraft', 'periodicReportType', 'excludeNonValidCases', 'limitPrimaryPath', 'blankValuesJSON', 'includeMedicallyConfirmedCases', 'primaryReportingDestination', 'dueInDays', 'tenantId', "isMultiIngredient", "includeWHODrugs"]

    static hasMany = [reportingDestinations: String, publisherContributors: User]
    Integer dueInDays
    Gantt gantt

    static mapping = {
        includePreviousMissingCases column: 'INCLUDE_PREV_MISS_CASES'
        includeOpenCasesInDraft column: 'INCLUDE_OPEN_CASES_DRAFT'
        dueInDays column: 'DUE_IN_DAYS'
        periodicReportType column: "PR_TYPE"
        reportingDestinations joinTable: [name: "RCONFIG_REPORT_DESTS", column: "REPORT_DESTINATION", key: "RCONFIG_ID"]
        publisherContributors joinTable: [name: "RCONFIG_P_C_USERS", column: "USER_ID", key: "RCONFIG_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        primaryReportingDestination column: "PRIMARY_DESTINATION"
        primaryPublisherContributor column: "PRIMARY_P_CONTRIBUTOR"
        generatedReportName column: "GENERATED_RPT_NAME"
        gantt column: "GANTT_ID"
        generateSpotfire column: "GENERATE_SPOTFIRE"
    }

    static constraints = {
        reportName(validator: { val, obj ->
            if (!val || !val.trim()) {
                return "com.rxlogix.config.Configuration.reportName.nullable"
            }
            // Check for invalid/dangerous content
            if (!MiscUtil.validateContent(val)) {
                return "com.rxlogix.config.Configuration.reportName.invalid.content"
            }
            //Name is unique to user
            if (!obj.id || obj.isDirty("reportName") || obj.isDirty("owner")) {
                long count = PeriodicReportConfiguration.createCriteria().count{
                    eq('reportName', val, [ignoreCase : true])
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.configuration.name.unique.per.user"
                }
            }
        })
        primaryPublisherContributor nullable: true
        publisherContributors nullable: true
        dueInDays nullable: true, max: 365, min: 0
        gantt nullable: true
        primaryReportingDestination nullable: false
        generatedReportName nullable: true, maxSize: 255
        generateSpotfire(nullable: true, maxSize: 8000)
        description(validator: { val, obj ->
            if (!MiscUtil.validateContent(val)) {
                return "com.rxlogix.config.Configuration.description.invalid.content"
            }
        })
    }

    static namedQueries = {

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        ownedByUser { User user ->
            eq('isDeleted', false)
            sharedWithUser(user)
        }

        sharedWithUser { User user ->
            if (!user?.isAdmin()) {
                createAlias('deliveryOption', 'do', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
                or {
                    user.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', user.id)
                    'in'('sw.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(user).id)
                    }
                }
            }
        }

        countAllForBulkUpdate { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            idsForBulkUpdate(filter)
        }

        fetchAllIdsForBulkUpdate { LibraryFilter filter ->
            projections {
                distinct('id')
                property("reportName")
                property("primaryReportingDestination")
                property("dateRangeType")
                property("configurationTemplate")
                property("periodicReportType")
                property("scheduleDateJSON")
                property("dueInDays")
                property("nextRunDate")
            }
            idsForBulkUpdate(filter)
        }

        idsForBulkUpdate { LibraryFilter filter ->
            createAlias('globalDateRangeInformation', 'globalDateRangeInformation', CriteriaSpecification.LEFT_JOIN)
            'in'('class', PeriodicReportConfiguration.name)
            ownedByUser(filter.user)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            if (filter.search) {

                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('productSelection', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('primaryReportingDestination', "%${EscapedILikeExpression.escapeString(filter.search)}%")

                }
            }
            if (filter.manualAdvancedFilter && filter.manualAdvancedFilter["status"] == "SCHEDULED") {
                and {
                    isNotNull('nextRunDate')
                    eq('isEnabled', true)
                }
            }
            if (filter.manualAdvancedFilter && filter.manualAdvancedFilter["status"] == "UNSCHEDULED") {
                or {
                    isNull('nextRunDate')
                    eq('isEnabled', false)
                }
            }
            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
        }
    }

    @Override
    boolean isEditableBy(User currentUser) {
        return super.isEditableBy(currentUser) && currentUser.hasRole("ROLE_PERIODIC_CONFIGURATION_CRUD")
    }

    @Override
    String getUsedEventSelection() {
        return null
    }

    @Override
    String getUsedEventGroupSelection() {
        return null
    }

    @Override
    String getConfigType() {
        return ConfigTypes.PERIODIC_REPORT_CONFIGURATION
    }

    Set<User> getAllPublisherContributors() {
        Set set = []
        if (this.publisherContributors) set.addAll(this.publisherContributors)
        if (this.primaryPublisherContributor) set.add(this.primaryPublisherContributor)
        return set
    }

    @Override
    List<Date> getReportMinMaxDate() {
        //As in Periodic reports Interval / Cummulative would be dependent on Global criteria only.
        return [globalDateRangeInformation.dateRangeStartAbsolute, globalDateRangeInformation.dateRangeEndAbsolute]
    }

    Set<String> getAllReportingDestinations(){
        Set<String> destinations = new LinkedHashSet<>([])
        if(reportingDestinations){
            destinations.addAll(reportingDestinations)
        }
        if(primaryReportingDestination){
            destinations.add(primaryReportingDestination)
        }
        return destinations
    }

    @Override
    public String toString() {
        super.toString()
    }

    @Override
    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        Map values = super.appendAuditLogCustomProperties(newValues, oldValues)
        newValues = values.newValues
        oldValues = values.oldValues
        if (newValues && (oldValues == null)) {
            if(newValues?.get("reportTasks"))
                newValues.put("reportTasks", createReportTaskView(reportTasks))
        }

        if (newValues && oldValues) {
            if ((newValues?.get("reportTasks") || (oldValues?.get("reportTasks")))) {
                newValues.put("reportTasks", createReportTaskView(reportTasks))
                oldValues.put("reportTasks", createReportTaskView(oldValues.reportTasks))
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }

    String createReportTaskView(Collection<ReportTask> reportTasks){
        StringBuilder sb = new StringBuilder()
        reportTasks.sort {it.dateCreated}.each {
            sb.append("Type: Aggregate Report\n")
            sb.append("Description : ${it.description} \n")
            sb.append("Asigned to: ${(it.assignedTo?.fullNameAndUserName?:it.assignedGroupTo?.name)?:"Owner"}\n " )
            sb.append("Priority: ${it.priority} \n")
            sb.append("Create in : ${it.createDateShift} days \n")
            sb.append("Due Date in : ${it.dueDateShift} days \n")
            sb.append("Base Date : ${ViewHelper.getMessage(it.baseDate.getI18nKey())} \n\n")
        }
        return sb.toString()
    }

}
