package com.rxlogix.config

import com.rxlogix.OrderByUtil
import com.rxlogix.UtilService
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import org.hibernate.FetchMode
import static com.rxlogix.enums.ReportFormatEnum.*
import static com.rxlogix.enums.FrequencyEnum.*
class ExecutionStatus {

    Long entityId
    Long executedEntityId
    String periodicReportType = ""
    ExecutingEntityTypeEnum entityType
    ReportExecutionStatusEnum executionStatus = ReportExecutionStatusEnum.GENERATING
    ReportExecutionStatusEnum aggregateReportStatus
    Long startTime = 0L
    Long endTime = 0L
    Long reportVersion
    Long queryId
    Long templateId
    String sectionName
    String message
    String stackTrace
    Date nextRunDate
    User owner
    Set<User> sharedWith = []
    FrequencyEnum frequency
    String reportName
    List<ReportFormatEnum> attachmentFormats = []
    String reportSql
    String querySql
    String headerSql
    Boolean isDeleted = false

    //For case series integration with PVS
    String callbackURL
    CallbackStatusEnum callbackStatus

    Date dateCreated
    Date lastUpdated

    Long runDuration

    Long tenantId

    String executedOn
    Boolean isPriorityReport = false

    static hasMany = [attachmentFormats: ReportFormatEnum, sharedWith: User]

    static mapping = {
        table name: "EX_STATUS"
        id column: "ID"
        startTime column: "START_TIME"
        endTime column: "END_TIME"
        reportVersion column: "RPT_VERSION"
        queryId column: "QUERY_ID"
        templateId column: "TEMPLATE_ID"
        sectionName column: "SECTION_NAME"
        stackTrace column: "STACK_TRACE", sqlType: DbUtil.longStringType
        frequency column: "FREQUENCY"
        executionStatus column: "EX_STATUS"
        aggregateReportStatus column: "AGG_RPT_STATUS"
        message column: "MESSAGE", sqlType: DbUtil.longStringType
        entityId column: "ENTITY_ID"
        executedEntityId column: "EXECUTED_ENTITY_ID"
        entityType column: "ENTITY_TYPE"
        periodicReportType column: "REPORT_TYPE"
        reportSql column: "REPORT_SQL", sqlType: DbUtil.longStringType
        querySql column: "QUERY_SQL", sqlType: DbUtil.longStringType
        headerSql column: "HEADER_SQL", sqlType: DbUtil.longStringType
        attachmentFormats(joinTable: [name: "EX_STATUSES_RPT_FORMATS", column: "RPT_FORMAT", key: "EX_STATUS_ID"], indexColumn: [name: "RPT_FORMAT_IDX"])
        sharedWith joinTable: [name: "EX_STATUSES_SHARED_WITHS", column: "SHARED_WITH_ID", key: "EX_STATUS_ID"]
        runDuration formula: '(END_TIME - START_TIME)'
        callbackURL column: "CALLBACK_URL"
        callbackStatus column: "CALLBACK_STATUS"
        tenantId column: "TENANT_ID"
        executedOn column: "SERVER_NAME"
        isPriorityReport column: "IS_PRIORITY_REPORT"
        isDeleted column: "IS_DELETED"
    }

    static constraints = {
        startTime (nullable: true)
        endTime(nullable: true)
        aggregateReportStatus(nullable: true)
        executedEntityId(nullable: true)
        message(maxSize: 64 * 1024, nullable: true)
        queryId(nullable: true)
        templateId(nullable: true)
        sectionName(nullable: true)
        frequency(nullable: true)
        stackTrace(maxSize: 64 * 1024, nullable: true)
        attachmentFormats(nullable: true, blank: true)
        sharedWith(nullable: true, blank: true)
        reportSql(maxSize: 6 * 1024 * 1024, nullable: true)          // 6M
        querySql(maxSize: 6 * 1024 * 1024, nullable: true)           // 6M
        headerSql(maxSize: 6 * 1024 * 1024, nullable: true)         // 6M
        callbackURL(nullable: true, blank: true)
        callbackStatus(nullable: true, blank: true)
        periodicReportType (nullable: true)
        executedOn (nullable: true)
    }

    Class getEntityClass() {
        Class entityClass = null
        switch (entityType) {
            case ExecutingEntityTypeEnum.CONFIGURATION:
                entityClass = Configuration.class
                break;
            case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                entityClass = PeriodicReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                entityClass = IcsrReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                entityClass = IcsrProfileConfiguration.class
                break;
            case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
            case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                entityClass = ExecutedConfiguration.class
                break;
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
            case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                entityClass = ExecutedPeriodicReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.CASESERIES:
                entityClass = CaseSeries.class
                break;
            case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
            case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                entityClass = ExecutedCaseSeries.class
                break;
            case ExecutingEntityTypeEnum.EXECUTED_ICSR_CONFIGURATION:
                entityClass = ExecutedIcsrReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.EXECUTED_ICSR_PROFILE_CONFIGURATION:
                entityClass = ExecutedIcsrProfileConfiguration.class
                break;

        }
        return entityClass
    }


    Class getExecutedEntityClass() {
        Class entityClass = null
        switch (entityType) {
            case ExecutingEntityTypeEnum.CONFIGURATION:
                entityClass = ExecutedConfiguration.class
                break;
            case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
                entityClass = ExecutedPeriodicReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
                entityClass = ExecutedIcsrReportConfiguration.class
                break;

            case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                entityClass = ExecutedIcsrReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION:
            case ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION:
                entityClass = ExecutedConfiguration.class
                break;
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL:
            case ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION:
            case ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION:
                entityClass = ExecutedPeriodicReportConfiguration.class
                break;
            case ExecutingEntityTypeEnum.CASESERIES:
                entityClass = ExecutedCaseSeries.class
                break;
            case ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES:
            case ExecutingEntityTypeEnum.EXCECUTED_CASESERIES:
                entityClass = ExecutedCaseSeries.class
                break;
        }
        return entityClass
    }


    Integer getExecutionTime() {
        if (endTime) {
            return endTime - startTime
        }
        switch (entityType) {
            case ExecutingEntityTypeEnum.CONFIGURATION:
            case ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION:
            case ExecutingEntityTypeEnum.ICSR_CONFIGURATION:
            case ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION:
                return getEntityClass().get(entityId)?.expectedExecutionTime
                break;
            default:
                return 0
                break;
        }
        return null
    }

    //TODO need to fix in future due to EXECUTED_PERIODIC_NEW_SECTION changes
    static ExecutingEntityTypeEnum getEntityTypeFromClass(Class entityClass){
        switch (entityClass) {
            case Configuration.class:
                return ExecutingEntityTypeEnum.CONFIGURATION
                break;
            case PeriodicReportConfiguration.class:
                return ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION
                break;
            case ExecutedConfiguration.class:
                return ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION
                break;
            case ExecutedPeriodicReportConfiguration.class:
                return ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION
                break;
            case CaseSeries.class:
                return ExecutingEntityTypeEnum.CASESERIES
                break;
            case ExecutedCaseSeries.class:
                return ExecutingEntityTypeEnum.EXCECUTED_CASESERIES
                break;
            case IcsrReportConfiguration.class:
                return ExecutingEntityTypeEnum.ICSR_CONFIGURATION
                break;
            case ExecutedIcsrReportConfiguration.class:
                return ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION
                break;

            case IcsrProfileConfiguration.class:
                return ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION
                break;
            case ExecutedIcsrProfileConfiguration.class:
                return ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION
                break;

        }
        return null
    }

    static boolean canReportExecuteForPriority(Class entityClass){
        switch (entityClass) {
            case Configuration.class:
            case PeriodicReportConfiguration.class:
            case ExecutedConfiguration.class:
            case ExecutedPeriodicReportConfiguration.class:
            case IcsrReportConfiguration.class:
            case ExecutedIcsrReportConfiguration.class:
                return true
                break;
            case IcsrProfileConfiguration.class:
            case ExecutedIcsrProfileConfiguration.class:
                return false
                break;

        }
        return false
    }

    static namedQueries = {

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        searchByICSRProfileType { boolean isICSRProfile ->
            def profileEntityTypes = [ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_ICSR_PROFILE_CONFIGURATION]
            if (isICSRProfile){
                inList('entityType', profileEntityTypes)
            } else {
                not {
                    inList('entityType', profileEntityTypes)
                }
            }
        }

        getExecutionToExecuted { List<ExecutionStatus> currentlyExecuting, boolean statusOfRunPriorityOnly, boolean checkForPriorityRpt ->
            eq('executionStatus', ReportExecutionStatusEnum.BACKLOG)
            lt('nextRunDate', new Date())
            if(statusOfRunPriorityOnly || checkForPriorityRpt) {
                eq('isPriorityReport', true)
            }
            not {
                inList('entityType', [ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_ICSR_PROFILE_CONFIGURATION])
            }

            if (currentlyExecuting) {
                not {
                    inList('id', currentlyExecuting*.id)
                    or {
                        currentlyExecuting.groupBy { it.entityClass }.each { map ->
                            if (map?.value) {
                                and {
                                    inList('entityId', map.value*.entityId)
                                    switch (map.key) {
                                        case Configuration.class: eq('entityType', ExecutingEntityTypeEnum.CONFIGURATION)
                                            break
                                        case PeriodicReportConfiguration.class: eq('entityType', ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION)
                                            break
                                        case ExecutedConfiguration.class: inList('entityType', [ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION, ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION])
                                            break
                                        case ExecutedPeriodicReportConfiguration.class: inList('entityType', [ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION,ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION, ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION])
                                            break
                                        case CaseSeries.class: eq('entityType', ExecutingEntityTypeEnum.CASESERIES)
                                            break
                                        case ExecutedCaseSeries.class: inList('entityType', [ExecutingEntityTypeEnum.EXCECUTED_CASESERIES, ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES])
                                            break
                                        case IcsrReportConfiguration.class: eq('entityType', ExecutingEntityTypeEnum.ICSR_CONFIGURATION)
                                            break
                                        case ExecutedIcsrReportConfiguration.class: inList('entityType', [ExecutingEntityTypeEnum.EXECUTED_ICSR_CONFIGURATION,ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION])
                                            break
                                    }

                                }
                            }
                        }
                    }
                }
            }
            and {
                order('nextRunDate', 'asc')
                order('isPriorityReport', 'desc')
            }
        }

        fetchAllNonCompletedExecutions{
            not{
                inList("executionStatus", [ReportExecutionStatusEnum.WARN, ReportExecutionStatusEnum.COMPLETED])
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        fetchAllCompletedExecutions {
            inList("executionStatus", [ReportExecutionStatusEnum.WARN, ReportExecutionStatusEnum.COMPLETED])
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        sharedWithUser { User user ->
            if (!user.admin) {
                'sharedWith' {
                    eq("id", user.id)
                }
            }
        }

        getAllRecordsBySearchinString { String search, List<Closure> advancedFilterCriteria,Map shareWith, User user, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("nextRunDate")
                property("reportVersion")
                property("reportName")
                property("periodicReportType")
                'owner' {
                    property("fullName", "fullName")
                }
                if (sortBy && sortBy == 'executionTime') {
                    sqlProjection """(CASE WHEN {alias}.END_TIME IS NOT NULL AND {alias}.END_TIME > 0 THEN ({alias}.END_TIME - {alias}.START_TIME) ELSE (
                        CASE 
                            WHEN {alias}.ENTITY_TYPE IN ('${ExecutingEntityTypeEnum.CONFIGURATION.name()}', '${ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION.name()}', '${ExecutingEntityTypeEnum.ICSR_CONFIGURATION.name()}', '${ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION.name()}') 
                            THEN (SELECT CASE WHEN c.TOTAL_EXECUTION_TIME IS NOT NULL AND c.NUM_OF_EXECUTIONS > 0 THEN ROUND(c.TOTAL_EXECUTION_TIME/c.NUM_OF_EXECUTIONS) ELSE 0 END FROM RCONFIG c WHERE c.ID = {alias}.ENTITY_ID) 
                            ELSE 0 
                        END 
                    ) END) as execTime""".toString(), 'execTime', org.hibernate.type.StandardBasicTypes.LONG
                }
                if (sortBy && sortBy == 'sharedWith') {
                    sqlProjection "(SELECT count(essw.SHARED_WITH_ID) FROM EX_STATUSES_SHARED_WITHS essw WHERE essw.EX_STATUS_ID = {alias}.ID) AS sharedWithCount", 'sharedWithCount', INTEGER
                }
                if (sortBy && sortBy == 'deliveryMedia') {
                    sqlProjection """(select listagg(FORMAT, ', ') within group(order by FORMAT) from (select (CASE esrf.RPT_FORMAT 
                        WHEN '${HTML.name()}' THEN '${HTML.displayName}' WHEN '${PDF.name()}' THEN '${PDF.displayName}' WHEN '${XLSX.name()}' THEN '${XLSX.displayName}' WHEN '${DOCX.name()}' THEN '${DOCX.displayName}' WHEN '${PPTX.name()}' THEN '${PPTX.displayName}' WHEN '${XML.name()}' THEN '${XML.displayName}' WHEN '${ZIP.name()}' THEN '${ZIP.displayName}' WHEN '${R3XML.name()}' THEN '${R3XML.displayName}' WHEN '${CSV.name()}' THEN '${CSV.displayName}' 
                        ELSE esrf.RPT_FORMAT END) AS FORMAT from EX_STATUSES_RPT_FORMATS esrf where esrf.EX_STATUS_ID = {alias}.ID)
                    ) AS deliveryMediaNames""".toString(), 'deliveryMediaNames', org.hibernate.type.StandardBasicTypes.STRING
                }
                if (sortBy && sortBy == 'frequency') {
                    sqlProjection """(CASE {alias}.FREQUENCY 
                        WHEN '${MINUTELY.name()}' THEN '${ViewHelper.getMessage(MINUTELY.i18nKey)}' 
                        WHEN '${HOURLY.name()}' THEN '${ViewHelper.getMessage(HOURLY.i18nKey)}' 
                        WHEN '${DAILY.name()}' THEN '${ViewHelper.getMessage(DAILY.i18nKey)}' 
                        WHEN '${WEEKLY.name()}' THEN '${ViewHelper.getMessage(WEEKLY.i18nKey)}' 
                        WHEN '${WEEKDAYS.name()}' THEN '${ViewHelper.getMessage(WEEKDAYS.i18nKey)}' 
                        WHEN '${MONTHLY.name()}' THEN '${ViewHelper.getMessage(MONTHLY.i18nKey)}' 
                        WHEN '${YEARLY.name()}' THEN '${ViewHelper.getMessage(YEARLY.i18nKey)}' 
                        WHEN '${RUN_ONCE.name()}' THEN '${ViewHelper.getMessage(RUN_ONCE.i18nKey)}'  
                        ELSE '${ViewHelper.getMessage(RUN_ONCE.i18nKey)}' END) AS frequencyDesc""".toString(), 'frequencyDesc', org.hibernate.type.StandardBasicTypes.STRING
                }
            }
            fetchMode("owner", FetchMode.JOIN)
            if (search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
                    "owner" {
                        iLikeWithEscape("fullName", "%${search}%")
                    }
                    iLikeWithEscape('periodicReportType', "%${EscapedILikeExpression.escapeString(search)}%")

                }
            }
            if(shareWith.usersId){
                createAlias('sharedWith', 'sw')
                'in'('sw.id', shareWith.usersId)
            }
            if (shareWith?.team && user) {
                or {
                    user.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                }
            }
            if (shareWith?.ownerId) {
                eq('owner.id', shareWith.ownerId)
            }
            if(advancedFilterCriteria){
                advancedFilterCriteria.each{cl->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                eq('tenantId', Tenants.currentId() as Long)
            }
            eq('isDeleted', false)

            // For Reports and Profile Filter
            searchByICSRProfileType(isICSRProfile)

            if (sortBy) {
                if (sortBy == 'version') {
                    order("reportVersion", "${sortDirection}")
                } else if (sortBy == 'runDate') {
                    order("nextRunDate", "${sortDirection}")
                } else if (sortBy == 'owner') {
                    order("fullName", "${sortDirection}")
                } else if (sortBy == 'executionTime') {
                    order(OrderByUtil.formulaOrder("execTime", "${sortDirection}"))
                } else if (sortBy == 'sharedWith') {
                    order(OrderByUtil.formulaOrder("sharedWithCount", "${sortDirection}"))
                } else if (sortBy == 'deliveryMedia') {
                    order(OrderByUtil.formulaOrder("UPPER(TRIM(deliveryMediaNames))", "${sortDirection}"))
                } else if (sortBy == 'frequency') {
                    order(OrderByUtil.formulaOrder("UPPER(TRIM(frequencyDesc))", "${sortDirection}"))
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        fetchAllBySearchStringAndBackLogStatus { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" ->
            getAllRecordsBySearchinString(search, advancedFilterCriteria, shareWith, user, isICSRProfile, sortBy, sortDirection)
            eq("executionStatus", ReportExecutionStatusEnum.BACKLOG)
        }

        fetchAllBySearchStringAndInProgressStatus { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" ->
            getAllRecordsBySearchinString(search, advancedFilterCriteria, shareWith, user, isICSRProfile, sortBy, sortDirection)
            inList("executionStatus", ReportExecutionStatusEnum.inProgressStatusesList)
        }

        fetchAllBySearchStringAndErrorStatus { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" ->
            getAllRecordsBySearchinString(search, advancedFilterCriteria, shareWith, user, isICSRProfile, sortBy, sortDirection)
            eq("executionStatus", ReportExecutionStatusEnum.ERROR)
        }

        fetchAllBySearchStringAndCompletedStatus { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" ->
            getAllRecordsBySearchinString(search, advancedFilterCriteria,shareWith, user, isICSRProfile, sortBy, sortDirection)
            inList("executionStatus", ReportExecutionStatusEnum.completedStatusesList)
        }

        ownedByUser { User user ->
            sharedWithUser(user)
        }

        fetchAllOwners { User user, search ->
            projections {
                distinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        countAllOwners { User user, search ->
            projections {
                countDistinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        getExecutionStatusByExectutedEntity { entityId, entityType ->
            eq('executedEntityId', entityId)
            eq('entityType', entityType)
            order('lastUpdated', 'desc')
        }

        getCompletedIdsFromList{ List<Long> ids ->
            projections {
                property('id')
            }
            inList('executionStatus',[ReportExecutionStatusEnum.COMPLETED, ReportExecutionStatusEnum.ERROR, ReportExecutionStatusEnum.WARN])
            inList('id',ids)
            le('lastUpdated',new Date(System.currentTimeMillis() - (3600 * 6000)))
        }

        getExecutionStatusByEntity { entityId, entityType ->
            projections {
                distinct('executedEntityId')
            }
            entityId.collate(999).each {
                'in'('entityId', it)
            }
            eq('entityType', entityType)
        }

    }

    static removeFromBacklog(Long id) {
        executeUpdate("delete from ExecutionStatus where id=:id and executionStatus = :executionStatus",
                [id: id, executionStatus: ReportExecutionStatusEnum.BACKLOG])
    }

    static getActiveUsersAndUserGroups(User user, String term) {
        String userViewableReportsSql = " where es.id in (select rc1.id from ExecutionStatus as rc1 left join rc1.sharedWith as swu1 " +
                "where rc1.owner.id=:userid or swu1.id=:userid)"

        String usersSQL = "from User as u where " +
                (term ? " ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and " : "") +
                " (u.id in (select swu.id from ExecutionStatus as es join es.sharedWith as swu  " +
                (user.isAdmin() ? "" : userViewableReportsSql) + "))"

        Map<String, Object> userParams = user.isAdmin() ? [:] : [userid: user.id]
        if (term) userParams.put('term', '%' + term.toLowerCase() + '%')
        User.findAll(usersSQL, userParams, [sort: 'username'])
    }

    public String toString() {
        return "$reportName - $reportVersion"
    }
}
