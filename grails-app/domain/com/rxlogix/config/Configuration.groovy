package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@DirtyCheck
@CollectionSnapshotAudit
class Configuration extends ReportConfiguration {
    static auditable =  [ignore:["executing", "nextRunDate", "totalExecutionTime", "numOfExecutions", "isEnabled"]]
    String eventSelection
    ExecutedCaseSeries useCaseSeries
    String eventGroupSelection

    static mapping = {
        // workaround to pull in mappings from super class that is not a domain
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        useCaseSeries column: "USE_CASE_SERIES_ID"
        eventGroupSelection column: "EVENT_GROUP_SELECTION", sqlType: DbUtil.longStringType
    }

    static constraints = {
        reportName(validator: {val, obj ->
            if (!val || !val.trim()) {
                return "com.rxlogix.config.Configuration.reportName.nullable"
            }
            // Check for invalid/dangerous content
            if (!MiscUtil.validateContent(val)) {
                return "com.rxlogix.config.Configuration.reportName.invalid.content"
            }

            //Name is unique to user
            if (!obj.id || obj.isDirty("reportName") || obj.isDirty("owner")) {
                long count = Configuration.createCriteria().count{
                    ilike('reportName', "${val}")
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    if (obj.id){ne('id', obj.id)}
                }
                if (count) {
                    return "com.rxlogix.config.configuration.name.unique.per.user"
                }
            }
        })
        eventSelection(nullable:true)
        useCaseSeries nullable: true
        eventGroupSelection(nullable:true)
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
            projections {
                property('id')
            }
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

        fetchAllIdsForBulkUpdate { LibraryFilter filter, List<Long> sharedWithIds ->
            projections {
                distinct('id')
                property("reportName")
                property("nextRunDate")
            }
            idsForBulkUpdate(filter, sharedWithIds)
        }

        countAllForBulkUpdate { LibraryFilter filter, List<Long> sharedWithIds ->
            projections {
                countDistinct("id")
            }
            idsForBulkUpdate(filter, sharedWithIds)
        }

        idsForBulkUpdate { LibraryFilter filter, List<Long> sharedWithIds ->
            if (sharedWithIds){
                or {
                    sharedWithIds?.collate(999)?.each {
                        'in'('id', it)
                    }
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            if (filter.forPvq) {
                isNotNull('pvqType')
            } else {
                isNull('pvqType')
            }
            if(filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('productSelection', "%${EscapedILikeExpression.escapeString(filter.search)}%")
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
        return eventSelection
    }

    @Override
    String getUsedEventGroupSelection() {
        return eventGroupSelection
    }

    @Override
    String getConfigType() {
        return ConfigTypes.CONFIGURATION
    }

    @Override
    String toString() {
        super.toString()
    }

    @Override
    boolean isEditableBy(User currentUser) {
        return super.isEditableBy(currentUser) && currentUser.hasRole("ROLE_CONFIGURATION_CRUD")
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
            sb.append("Type: Adhoc Report\n")
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
