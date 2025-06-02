package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.MiscUtil
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class IcsrReportConfiguration extends ReportConfiguration {
    static auditable =  [ignore:["executing", "nextRunDate", "totalExecutionTime", "numOfExecutions", "isEnabled"]]
    PeriodicReportTypeEnum periodicReportType
    boolean includePreviousMissingCases = false
    boolean includeOpenCasesInDraft = false
    boolean includeLockedVersion = false //As we don't want to show incase of PeriodicReports
    boolean excludeDeletedCases = false
    String primaryReportingDestination
    UnitConfiguration recipientOrganization
    UnitConfiguration senderOrganization
    IcsrProfileConfiguration referenceProfile

    static transients = ['referenceProfile']

    static List<String> propertiesToUseWhileCopying = ['scheduleDateJSON', 'description', 'isDeleted', 'tags', 'dateRangeType', 'sourceProfile', 'productSelection', 'studySelection', 'eventSelection', 'productGroupSelection', 'eventGroupSelection', 'configSelectedTimeZone', 'asOfVersionDate', 'evaluateDateAs', 'excludeFollowUp', 'includeLockedVersion', 'includeOpenCasesInDraft', 'includeAllStudyDrugsCases', 'adjustPerScheduleFrequency', 'suspectProduct', 'includePreviousMissingCases', 'globalQuery', 'generateCaseSeries', 'generateDraft', 'periodicReportType', 'excludeNonValidCases', 'limitPrimaryPath', 'blankValuesJSON', 'includeMedicallyConfirmedCases', 'primaryReportingDestination', 'dueInDays', 'recipientOrganization', 'senderOrganization', 'tenantId', 'isMultiIngredient', 'includeWHODrugs']

    static hasMany = [reportingDestinations:String]
    Integer dueInDays

    static mapping = {
        includePreviousMissingCases column: 'INCLUDE_PREV_MISS_CASES'
        includeOpenCasesInDraft column: 'INCLUDE_OPEN_CASES_DRAFT'
        dueInDays column: 'DUE_IN_DAYS'
        periodicReportType column: "PR_TYPE"
        reportingDestinations joinTable: [name: "RCONFIG_REPORT_DESTS", column: "REPORT_DESTINATION", key: "RCONFIG_ID"]
        primaryReportingDestination column: "PRIMARY_DESTINATION"
        senderOrganization column: "RECIPIENT_ORG_ID"
        recipientOrganization column: 'SENDER_ORG_ID'
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
                long count = IcsrReportConfiguration.createCriteria().count{
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
        referenceProfile(bindable: true, nullable: true)
        productSelection(nullable: true)
        dueInDays nullable: true
        primaryReportingDestination nullable: true
        periodicReportType nullable: true
        recipientOrganization nullable: false
        senderOrganization nullable: false
        description(validator: { val, obj ->
            if (!MiscUtil.validateContent(val)) {
                return "com.rxlogix.config.Configuration.description.invalid.content"
            }
        })
    }

    static namedQueries = {
        fetchSheduledConfigurations{
            gte 'nextRunDate', new Date()
            eq 'isEnabled', true
            eq 'isDeleted', false
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
            }
            idsForBulkUpdate(filter)
        }

        idsForBulkUpdate { LibraryFilter filter ->
            createAlias('globalDateRangeInformation', 'globalDateRangeInformation', CriteriaSpecification.LEFT_JOIN)
            'in'('class', IcsrReportConfiguration.name)
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
    String getUsedEventSelection() {
        return null
    }

    @Override
    List<Date> getReportMinMaxDate() {
        //As in Periodic reports Interval / Cummulative would be dependent on Global criteria only.
        return [globalDateRangeInformation.dateRangeStartAbsolute, globalDateRangeInformation.dateRangeEndAbsolute]
    }

    transient IcsrProfileConfiguration getReferenceProfile() {
        return (IcsrProfileConfiguration) this.configurationTemplate
    }

    transient IcsrProfileConfiguration setReferenceProfile(IcsrProfileConfiguration icsrProfileConfiguration) {
        this.configurationTemplate = icsrProfileConfiguration
    }

    Set<String> getAllReportingDestinations(){
        Set<String> destinations = new HashSet<>([])
        if(reportingDestinations){
            destinations.addAll(reportingDestinations)
        }
        if(primaryReportingDestination){
            destinations.add(primaryReportingDestination)
        }
        return destinations
    }

    @Override
    String getConfigType() {
        return ConfigTypes.ICSR_REPORT_CONFIGURATION
    }
}
