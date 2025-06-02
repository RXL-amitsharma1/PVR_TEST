package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.QualityService
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.grails.core.exceptions.GrailsRuntimeException
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
import org.springframework.web.context.request.RequestContextHolder
import grails.converters.JSON
import java.text.DateFormat
import java.text.Format
import java.text.SimpleDateFormat
import com.rxlogix.Constants

import static com.rxlogix.enums.ReportFormatEnum.*
import static com.rxlogix.enums.FrequencyEnum.*

@CollectionSnapshotAudit
@DirtyCheck
abstract class ReportConfiguration extends BaseConfiguration {

    transient def userService
    transient def utilService

    GlobalDateRangeInformation globalDateRangeInformation
//    DeliveryOption deliveryOption = new DeliveryOption()
    List<TemplateQuery> templateQueries = [] // why?
    int asOfVersionDateDelta
    boolean executing = false
    boolean generateCaseSeries = false
    boolean generateDraft = false
    boolean isTemplate = false
    boolean qbeForm = false
    SuperQuery globalQuery
    List<QueryValueList> globalQueryValueLists;
    EmailConfiguration emailConfiguration
    Set<ReportTask> reportTasks
    ReportConfiguration configurationTemplate


    static hasMany = [attachments:PublisherSource, publisherConfigurationSections: PublisherConfigurationSection, templateQueries: TemplateQuery, globalQueryValueLists: QueryValueList, configurationUserStates: ConfigurationUserState, reportTasks:ReportTask]
    static hasOne = [deliveryOption: DeliveryOption]
    static mappedBy = [templateQueries: 'report']
    static transients = ['executionStatus']

    static mapping = {
        autoTimestamp false
        table name: "RCONFIG"
        deliveryOption cascade: 'all', fetch: 'join'
        asOfVersionDateDelta column: "AS_OF_VERSION_DATE_DELTA"
        executing column: "EXECUTING"
        templateQueries joinTable: [name: "TEMPLT_QUERY", column: "ID", key: "RCONFIG_ID"], indexColumn: [name: "TEMPLT_QUERY_IDX"], cascade: "all-delete-orphan"
        tags joinTable: [name: "RCONFIGS_TAGS", column: "TAG_ID", key: "RCONFIG_ID"], indexColumn: [name: "TAG_IDX"]
        poiInputsParameterValues joinTable: [name: "RCONFIGS_POI_PARAMS", column: "PARAM_ID", key: "RCONFIG_ID"]
        generateCaseSeries column: "GENERATE_CASE_SERIES"
        generateDraft column: "GENERATE_DRAFT"
        globalQuery column: "GLOBAL_QUERY_ID"
        globalQueryValueLists joinTable: [name: "GLOBAL_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "RCONFIG_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        configurationUserStates joinTable: [name: "RCONFIG_USER_STATE", column: "ID", key: "RCONFIG_ID"]
        isTemplate column: "IS_TEMPLATE"
        qbeForm column: "SIMPLE_FORM"
        configurationTemplate column: "CONFIG_TEMPLATE_ID", cascade: 'none'
        globalDateRangeInformation column: "GLOBAL_DATA_RANGE_INFO_ID"
        attachments cascade: "all"
        publisherConfigurationSections cascade: "all"
    }

    static constraints = {
        attachments(nullable: true)
        publisherConfigurationSections nullable: true
        reportTasks(nullable: true, validator: { val, obj ->
            if (obj.reportTasks && obj.reportTasks.size() > 0) {
                for (int i = 0; i < obj.reportTasks.size(); i++) {
                    if (!(obj.reportTasks[i].description?.size() > 0))
                        return "com.rxlogix.config.TaskTemplate.reportTasks.description.empty"
                }
            }
        })
        deliveryOption(nullable: false)
        asOfVersionDateDelta(nullable: true)
        templateQueries(nullable: false, minSize: 1, validator: { val, obj ->
            if (val && !val.any { !it.draftOnly }) {
                return "com.rxlogix.config.templateQueries.atleast.one.without.draft"
            }
            if(val && val.any { it.query && it.query.queryTarget != QueryTarget.REPORTS }) {
                return "com.rxlogix.config.templateQueries.queryTarget.not.reports"
            }
        })
        tags (nullable: true)
        isTemplate validator: { val, obj ->
            if (val && obj.userService?.springSecurityService?.loggedIn && !obj.userService?.currentUser?.getAuthorities()?.find { it.authority in [Constants.Roles.CONFIG_TMPLT_CREATOR, Constants.Roles.ADMIN, Constants.Roles.SUPER_ADMIN] }) {
                return "com.rxlogix.config.ReportConfiguration.create.template.forbidden"
            }
        }
        qbeForm validator: { val, obj ->
            if (val && obj.userService?.springSecurityService?.loggedIn && !obj.userService?.currentUser?.getAuthorities()?.find { it.authority in ["ROLE_BQA_EDITOR", Constants.Roles.ADMIN, Constants.Roles.SUPER_ADMIN] }) {
                return "com.rxlogix.config.ReportConfiguration.create.template.forbidden"
            }
        }
        globalQuery(nullable: true)
        emailConfiguration nullable: true
        configurationTemplate nullable: true
        globalDateRangeInformation nullable: true
    }

    static namedQueries = {

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        nextConfigurationToExecute { List ids, List<Class> classes ->
            and {
                if (!(IcsrProfileConfiguration.class in classes)) {
                    lte 'nextRunDate', new Date()
                }
                eq 'isEnabled', true
                eq 'isDeleted', false
                if (classes) {
                    'in'('class', classes.name)
                }
                if (ids) {
                    not {
                        'in'('id', ids)
                    }
                }
            }
            order 'nextRunDate', 'asc'
        }

        fetchAllViewableByUser { User user ->
            eq('isDeleted', false)
            if (!user.isAdmin()) {
                createAlias('deliveryOption', 'do', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
                or {
                    'in'('sw.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(user).id)
                    }
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        fetchAllScheduledConfigurations {
            isNotNull('nextRunDate')
            eq('isDeleted', false)
            eq('executing', false)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        //Use the base class named query to get the filtered result for both the derived classes i.e. PeriodicReportConfiguration, Configuration.
        //clazz field is used to differentiate b/w the two derived classes.
        getAllRecordsBySearchString { String search, User user, List<Class> classes ->
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if (search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            if (classes) {
                'in'('class', classes.name)
            }

            ownedByUser(user)
        }

        //TODO: Remove this showXMLOption param and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
        countRecordsBySearchString { LibraryFilter filter, boolean showXMLOption = false ->
            countRecordsByFilter(filter, showXMLOption)
        }

        //TODO: Remove this showXMLOption param and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
        countRecordsByFilter { LibraryFilter filter, boolean showXMLOption = false ->
            projections {
                countDistinct("id")
            }
            getAllRecordsByFilter(filter)

            if(!showXMLOption){
                templateQueries{
                    template{
                        ne("templateType", TemplateTypeEnum.ICSR_XML)
                    }
                }
            }
        }

        fetchAllIdsBySearchString { String search, User user, Class clazz, Map sharedWith, Boolean favoriteSort = false ->
            def filter = new LibraryFilter([search: search,configurationClasses: [clazz],sharedWith: sharedWith,favoriteSort: favoriteSort,user:user])
            getAllIdsByFilter (filter, clazz)
        }

        fetchSheduledConfigurations {
            gte 'nextRunDate', new Date()
            eq 'isEnabled', true
            eq 'isDeleted', false
        }

        //TODO: Remove this showXMLOption param and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
        getAllIdsByFilter { LibraryFilter filter, Class clazz, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc"  ->
            // http://stackoverflow.com/questions/15275447/using-the-distinct-keyword-causes-this-error-not-a-selected-expression
            projections {
                distinct('id')
                property("dateCreated")
                property("lastUpdated")
                property("numOfExecutions")
                property("reportName")
                property("description")
                property("qualityChecked")
                property("class")
                property("isEnabled")
                if(PeriodicReportConfiguration.equals(clazz) || IcsrReportConfiguration.equals(clazz)){
                    property("primaryReportingDestination")
                }
                if (IcsrProfileConfiguration.equals(clazz)) {
                    createAlias("senderOrganization", "senderOrg", CriteriaSpecification.LEFT_JOIN)
                    createAlias("recipientOrganization", "recipientOrg", CriteriaSpecification.LEFT_JOIN)
                    createAlias("senderOrg.organizationType", "senderOrgType", CriteriaSpecification.LEFT_JOIN)
                    createAlias("recipientOrg.organizationType", "recipientOrgType", CriteriaSpecification.LEFT_JOIN)
                    property("senderOrg.unitName", "senderOrganization")
                    property("recipientOrg.unitName", "recipientOrganization")
                    property("senderOrgType.name", "senderType")
                    property("recipientOrgType.name", "recipientType")
                }
                'owner' {
                    property("fullName", "fullName")
                }
                property("state.isFavorite","isFavorite")
            }
            getAllRecordsByFilter(filter)

            if(!showXMLOption){
                templateQueries{
                    template{
                        ne("templateType", TemplateTypeEnum.ICSR_XML)
                    }
                }
            }
            if (sortBy) {
                if (sortBy == 'qualityChecked') {
                    order(OrderByUtil.booleanOrder(sortBy, sortDirection))
                } else if (sortBy == 'owner.fullName') {
                    order(new Order("fullName", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (['reportName', 'description', 'primaryReportingDestination'].contains(sortBy)) {
                    order(new Order("${sortBy}", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        ownedByUser { User user ->
            eq('isDeleted', false)
            sharedWithUser(user)
        }
        fetchAllTemplatesForUser {User user, Class clazz, String search = null->
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            if (search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq('isDeleted', false)
            eq('isTemplate', true)
            eq('class', clazz.name)
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

        fetchAllDeletedIdsViewableForUser { User user ->
            projections {
                distinct("id")
            }
            eq('isDeleted', true)
            sharedWithUser(user)
        }

        searchByICSRProfileType { boolean isICSRProfile ->
            def profileClasses = [IcsrProfileConfiguration.class, ExecutedIcsrProfileConfiguration.class]
            if (isICSRProfile){
                inList('class', profileClasses)
            } else {
                not {
                    inList('class', profileClasses)
                }
            }
        }

        fetchAllScheduledForUser { String search, List<Long> alreadyRunningConfigurationIds, backLogConfigurationIds, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("nextRunDate")
                property("numOfExecutions")
                property("reportName")
                'owner' {
                    property("fullName", "fullName")
                }
                if (sortBy && sortBy == 'executionTime') {
                    sqlProjection '(CASE WHEN TOTAL_EXECUTION_TIME IS NOT NULL AND NUM_OF_EXECUTIONS > 0 THEN ROUND(TOTAL_EXECUTION_TIME/NUM_OF_EXECUTIONS) ELSE 0 END) as reportExpectedExecTime', 'reportExpectedExecTime', org.hibernate.type.StandardBasicTypes.LONG
                }
                if (sortBy && sortBy == 'deliveryMedia') {
                    sqlProjection """(select listagg(FORMAT, ', ') within group(order by FORMAT) from (select (CASE drf.RPT_FORMAT 
                        WHEN '${HTML.name()}' THEN '${HTML.displayName}' WHEN '${PDF.name()}' THEN '${PDF.displayName}' WHEN '${XLSX.name()}' THEN '${XLSX.displayName}' WHEN '${DOCX.name()}' THEN '${DOCX.displayName}' WHEN '${PPTX.name()}' THEN '${PPTX.displayName}' WHEN '${XML.name()}' THEN '${XML.displayName}' WHEN '${ZIP.name()}' THEN '${ZIP.displayName}' WHEN '${R3XML.name()}' THEN '${R3XML.displayName}' WHEN '${CSV.name()}' THEN '${CSV.displayName}' 
                        ELSE drf.RPT_FORMAT END) AS FORMAT from DELIVERIES_RPT_FORMATS drf, delivery d  where drf.delivery_id = d.id AND d.REPORT_ID = {alias}.ID)
                    ) AS deliveryMediaNames""".toString(), 'deliveryMediaNames', org.hibernate.type.StandardBasicTypes.STRING
                }
                if (sortBy && sortBy == 'sharedWith') {
                    sqlProjection """(SELECT count(user_id) FROM ( 
                            SELECT dsw.SHARED_WITH_ID as user_id FROM DELIVERIES_SHARED_WITHS dsw, delivery d WHERE dsw.DELIVERY_ID = d.id AND d.REPORT_ID = {alias}.ID 
                            UNION 
                            SELECT de.EXECUTABLE_ID as user_id FROM DELIVERIES_EXECUTABLE de, delivery d WHERE de.DELIVERY_ID = d.id AND d.REPORT_ID = {alias}.ID 
                            UNION 
                            SELECT ugu.USER_ID as user_id FROM PVUSERGROUPS_USERS ugu, DELIVERIES_SHARED_WITH_GRPS dswg, delivery d WHERE ugu.USER_GROUP_ID = dswg.SHARED_WITH_GROUP_ID AND dswg.DELIVERY_ID = d.id AND d.REPORT_ID = {alias}.ID 
                            UNION 
                            SELECT ugu.USER_ID as user_id FROM PVUSERGROUPS_USERS ugu, DELIVERIES_EXECUTABLE_GRPS deg, delivery d WHERE ugu.USER_GROUP_ID = deg.EXECUTABLE_GROUP_ID AND deg.DELIVERY_ID = d.id AND d.REPORT_ID = {alias}.ID 
                        )) AS sharedWithCount""".toString(), 'sharedWithCount', INTEGER
                }
                if (sortBy && sortBy == 'frequency') {
                    sqlProjection """(CASE 
                        WHEN schedule_date is not null and next_run_date is not null THEN 
                            CASE 
                                WHEN INSTR(schedule_date, '${DAILY.name()}') > 0 THEN (CASE WHEN INSTR(schedule_date, 'COUNT=1') > 0 THEN '${ViewHelper.getMessage(RUN_ONCE.i18nKey)}' ELSE '${ViewHelper.getMessage(DAILY.i18nKey)}' END) 
                                WHEN INSTR(schedule_date, '${MINUTELY.name()}') > 0 THEN '${ViewHelper.getMessage(MINUTELY.i18nKey)}' 
                                WHEN INSTR(schedule_date, '${HOURLY.name()}') > 0 THEN '${ViewHelper.getMessage(HOURLY.i18nKey)}' 
                                WHEN INSTR(schedule_date, '${WEEKLY.name()}') > 0 THEN '${ViewHelper.getMessage(WEEKLY.i18nKey)}' 
                                WHEN INSTR(schedule_date, '${MONTHLY.name()}') > 0 THEN '${ViewHelper.getMessage(MONTHLY.i18nKey)}' 
                                WHEN INSTR(schedule_date, '${YEARLY.name()}') > 0 THEN '${ViewHelper.getMessage(YEARLY.i18nKey)}' 
                                WHEN INSTR(schedule_date, 'FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;') > 0 THEN '${ViewHelper.getMessage(WEEKDAYS.i18nKey)}' 
                                ELSE '${ViewHelper.getMessage(RUN_ONCE.i18nKey)}' 
                            END 
                        ELSE '${ViewHelper.getMessage(RUN_ONCE.i18nKey)}' 
                    END) as frequencyDesc""".toString(), 'frequencyDesc', org.hibernate.type.StandardBasicTypes.STRING
                }
            }
            isNotNull('nextRunDate')
            eq('executing', false)
            eq('isEnabled', true)
            fetchMode("owner", FetchMode.JOIN)
            // For Report and Profile Filter -  Execution Status
            searchByICSRProfileType(isICSRProfile)
            if (search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
                    "owner" {
                        iLikeWithEscape("fullName", "%${EscapedILikeExpression.escapeString(search)}%")
                    }
                }
            }
            if(shareWith.usersId){
                createAlias('deliveryOption', 'do', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
                'in'('sw.id', shareWith.usersId)
            }
            if (shareWith.ownerId) {
                'eq'('owner.id', shareWith.ownerId)
            }
            if(advancedFilterCriteria){
                advancedFilterCriteria.each{cl->
                    cl.delegate = delegate
                    cl.call()
                }
            }

            //            TODO in future need to move Oracle Views right now assuming deleted records won't be more than 30000
            if (alreadyRunningConfigurationIds?.size() > 0) {
                and {
                    alreadyRunningConfigurationIds.collate(999).each { list ->
                        not {
                            inList("id", list)
                        }
                    }
                }
            }

            if (backLogConfigurationIds?.size() > 0) {
                and {
                    backLogConfigurationIds.collate(999).each { list ->
                        not {
                            inList("id", list)
                        }
                    }
                }
            }

            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }

            if (sortBy) {
                if (sortBy == 'executionTime') {
                    order(OrderByUtil.formulaOrder("reportExpectedExecTime", "${sortDirection}"))
                } else if (sortBy == 'frequency') {
                    order(OrderByUtil.formulaOrder("UPPER(TRIM(frequencyDesc))", "${sortDirection}"))
                } else if (sortBy == 'version') {
                    order("numOfExecutions", "${sortDirection}")
                } else if (sortBy == 'runDate') {
                    order("nextRunDate", "${sortDirection}")
                } else if (sortBy == 'owner') {
                    order("fullName", "${sortDirection}")
                } else if (sortBy == 'reportName') {
                    order(OrderByUtil.trimOrderIgnoreCase("${sortBy}", "${sortDirection}"))
                } else if (sortBy == 'sharedWith') {
                    order(OrderByUtil.formulaOrder("sharedWithCount", "${sortDirection}"))
                } else if (sortBy == 'deliveryMedia') {
                    order(OrderByUtil.formulaOrder("UPPER(TRIM(deliveryMediaNames))", "${sortDirection}"))
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

        getAllScheduledReportForUserForStartDateAndEndDate { User user, Date startDate, Date endDate ->
            ownedByUser(user)
            isNotNull('nextRunDate')
            eq('isEnabled', true)
            gte('nextRunDate', startDate)
            lte('nextRunDate', endDate)
            ne('class', IcsrProfileConfiguration.class.name) //Exclude ICSR Profile as its something which will execute continously.
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        fetchAllOwners { User user, clazz, search ->
            projections {
                distinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        countAllOwners { User user, clazz, search ->
            projections {
                countDistinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        getAllRecordsByFilter { LibraryFilter filter ->
            createAlias('deliveryOption', 'do', CriteriaSpecification.LEFT_JOIN)
            createAlias('do.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('do.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            if (!filter.user?.isAdmin() || filter.sharedWith?.ownerId || filter.sharedWith?.usersId || filter.sharedWith?.groupsId || filter.sharedWith?.team) {

                or {
                    if (filter.sharedWith?.ownerId) {
                        eq('owner.id', filter.sharedWith.ownerId)
                    }
                    if (filter.sharedWith?.usersId) {
                        or{
                            'in'('sw.id', filter.sharedWith.usersId)
                            'in'('swg.id', new DetachedCriteria(UserGroupUser).build {
                                            projections {
                                                distinct('userGroup.id')
                                            }
                                            'in'('user.id', filter.sharedWith.usersId)
                                        })
                        }
                    }
                    if (filter.sharedWith?.groupsId) {
                        'in'('swg.id', filter.sharedWith.groupsId)
                    }
                    if (filter.sharedWith?.team && filter.user) {
                        or {
                            filter.user.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                        }
                    }
                }
                if (!filter.user?.isAdmin()) {
                    or {
                        filter.user?.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                        eq('owner.id', filter.user.id)
                        'in'('sw.id', filter.user.id)
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(filter.user)?.id ?: [0L])
                    }
                }
            }
            if (filter.forPublisher && !filter.allReportsForPublisher) {
                eq('isPublisherReport', true)
            }
//            createAlias('senderOrganization', 'senderOrganization', CriteriaSpecification.LEFT_JOIN)
//            createAlias('recipientOrganization', 'recipientOrganization', CriteriaSpecification.LEFT_JOIN)
//            createAlias('senderOrganization.organizationType', 'senderOrganizationType', CriteriaSpecification.LEFT_JOIN)
//            createAlias('recipientOrganization.organizationType', 'recipientOrganizationType', CriteriaSpecification.LEFT_JOIN)
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if(filter.forPvq){
                isNotNull('pvqType')
            } else {
                isNull('pvqType')
            }
            if (filter.search) {

                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('primaryReportingDestination', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    if (filter.search == "qced") {
                        eq('qualityChecked', true)
                    }
                }
            }
            if (filter?.manualAdvancedFilter?.running) {
                and {
                    isNotNull('nextRunDate')
                    eq('isEnabled', true)
                }
            }
            if(filter.advancedFilterCriteria){
                createAlias('do.emailToUsers', 'emails', CriteriaSpecification.LEFT_JOIN)
                createAlias('emailConfiguration', 'emc', CriteriaSpecification.LEFT_JOIN)
                filter.advancedFilterCriteria.each{cl->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (filter.configurationClasses) {
                'in'('class', filter.configurationClasses.name)
            }
            if (filter.templateTypes) {
                templateQueries {
                    'template' {
                        'in'('templateType', filter.templateTypes)
                        if (filter.showChartSheet != null) {
                            eq('showChartSheet', filter.showChartSheet)
                        }
                    }
                }
            }
            eq('isDeleted', false)
            createAlias('configurationUserStates', 'state', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('user', filter.user))
            if (filter.favoriteSort) {
                and {
                    order('state.isFavorite', 'asc')
                    order('lastUpdated', 'desc')
                }
            }
            if(filter.periodicReportType){
                eq('periodicReportType', filter.periodicReportType as PeriodicReportTypeEnum)
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }

        }
    }

    String getInstanceIdentifierForAuditLog() {
        return reportName
    }

    def getExecutionStatus() {
        return ReportExecutionStatusEnum.SCHEDULED.value()
    }

    def getDisplayDescription() {
        //todo:  Use i18n- morett
        description ? description : 'None'
    }

    @Override
    String getUsedEventSelection() {
        return null
    }

    @Override
    String getUsedEventGroupSelection() {
        return null
    }

    String getConfigType() {
        throw new GrailsRuntimeException("Report Configuration object is lazy initialized with out actual object. Please correct the code.")
    }

    static List<Long> getAlreadyRunningConfigurationIds() {
        return ReportConfiguration.executeQuery("select conf.id from ReportConfiguration conf, ExecutionStatus exStatus where conf.id = exStatus.entityId and exStatus.reportVersion=conf.numOfExecutions+1 and conf.isDeleted = true and exStatus.entityType in (:entityTypes)", [entityTypes: [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION]])
    }

    public Set<User> getShareWithUsers() {
        Set<User> users = []
        if (deliveryOption?.sharedWith) {
            users.addAll(deliveryOption.sharedWith)
        }
        return users
    }

    public Set<UserGroup> getShareWithGroups() {
        Set<UserGroup> userGroups = []
        if (deliveryOption?.sharedWithGroup) {
            userGroups.addAll(deliveryOption.sharedWithGroup)
        }
        return userGroups
    }

    public Set<User> getAllSharedUsers() {
        Set<User> users = []
        if (deliveryOption?.sharedWith)
            users.addAll(deliveryOption.sharedWith)
        deliveryOption?.sharedWithGroup?.each { UserGroup userGroup ->
            users.addAll(userGroup.users)
        }
        if (deliveryOption?.executableBy)
            users.addAll(deliveryOption?.executableBy)
        deliveryOption?.executableByGroup?.each { UserGroup userGroup ->
            users.addAll(userGroup.users)
        }
        users
    }

    @Override
    boolean isEditableBy(User currentUser) {
        return super.isEditableBy(currentUser) || checkExecutableBy(currentUser)
    }

    boolean checkExecutableBy(User currentUser) {
        if (deliveryOption?.executableBy?.any { it.id == currentUser.id }) {
            return true
        }
        List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(currentUser).flatten()
        return deliveryOption?.executableByGroup?.any { it.id in userGroups*.id }
    }

    public Set<User> getExecutableByUser() {
        Set<User> users = []
        if (deliveryOption?.executableBy) {
            users.addAll(deliveryOption.executableBy)
        }
        return users
    }

    public Set<UserGroup> getExecutableByGroup() {
        Set<UserGroup> userGroups = []
        if (deliveryOption?.executableByGroup) {
            userGroups.addAll(deliveryOption.executableByGroup)
        }
        return userGroups
    }

    public boolean isViewableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || isVisible(currentUser) || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())) || isEditableBy(currentUser))
    }

    public boolean isVisible(User currentUser) {
        return deliveryOption.isSharedWith(currentUser)
    }

    boolean isFavorite(User user) {
        return configurationUserStates.find { item -> item.user == user }?.isFavorite
    }

    @Override
    List<Date> getReportMinMaxDate() {
        Date minDate = null
        Date maxDate = null
        List<TemplateQuery> templateQueries = getTemplateQueries()
        List<TemplateQuery> templateQueriesForMinMax = templateQueries?.findAll {
            it.dateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.CUMULATIVE
        }

        // For PVR-21232 -- For the calculation of Max End i.e Max End Date = Max End Date from all the sections and global date which have definite dates.
        List<TemplateQuery> templateQueriesForMaxEnd = templateQueriesForMinMax?.findAll {
            it.dateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.PR_DATE_RANGE
        }
        if (templateQueriesForMinMax) {
            minDate = templateQueriesForMinMax*.startDate.min()
            maxDate = templateQueriesForMinMax*.endDate.max()
        } else if (templateQueries) {
            TemplateQuery templateQuery = templateQueries.first()
            minDate = templateQuery.startDate
            maxDate = templateQuery.endDate
            if (globalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
                minDate = globalDateRangeInformation?.dateRangeStartAbsolute
                maxDate = globalDateRangeInformation?.dateRangeEndAbsolute
            }
        }
        if (templateQueriesForMaxEnd){
            maxDate = templateQueriesForMaxEnd*.endDate.max()
        }
        Date globalStartDate = null
        if (globalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
            globalStartDate = globalDateRangeInformation?.dateRangeStartAbsolute
        }
        Date globalEndDate  = null
        if (globalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
            globalEndDate = globalDateRangeInformation?.dateRangeEndAbsolute
        }
        minDate = (minDate && globalStartDate) ? [minDate, globalStartDate].min() : minDate
        maxDate = (maxDate && globalEndDate) ? [maxDate, globalEndDate].max() : maxDate

        return [minDate, maxDate]
    }

    String getAttachmentsString() {
        return attachments?.collect { it.name }?.join(",")
    }

    @Override
    transient boolean isRunning() {
        return this.executing || (getId() && (ExecutionStatus.countByEntityIdAndEntityTypeInListAndExecutionStatusInList(getId(), [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,ExecutingEntityTypeEnum.ICSR_CONFIGURATION], [ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.BACKLOG]) > 0))
    }

    public int isAnyCumulativeTQ() {
        templateQueries.any {
            it.usedDateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUMULATIVE ||
                    (it.usedTemplate instanceof DataTabulationTemplate && it.usedTemplate.hasCumulative())
        } ? 1 : 0
    }

    static getActiveUsersAndUserGroups(String clazz,User user, String term) {
        def result = [users: [], userGroups: []]
        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableReportsSql = " and rc.id in (select rc1.id from ReportConfiguration as rc1 left join rc1.deliveryOption as dop1 left join dop1.sharedWithGroup as swg1 left join dop1.sharedWith as swu1 " +
                "where rc1.class=:clazz and rc1.isDeleted=false and (rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser)))"
        String groupsSql = "from UserGroup as ug where  " +
                (term?" lower(ug.name) like :term and ":"")+
                " ug.id in (select swg.id from ReportConfiguration as rc join rc.deliveryOption as dop join dop.sharedWithGroup as swg where rc.class=:clazz and rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"") +
                " (u.id in (select swu.id from ReportConfiguration as rc join rc.deliveryOption as dop join dop.sharedWith as swu where rc.class=:clazz and rc.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))"
        Map groupParams = user.isAdmin() ? [clazz: clazz] : [clazz: clazz, userid: user.id, groupIdsForUser: groupIdsForUser]
        if(term) groupParams.put('term','%'+term.toLowerCase()+'%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        Map userParams = user.isAdmin() ? [clazz: clazz, groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [clazz: clazz, userid: user.id, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if(term) userParams.put('term','%'+term.toLowerCase()+'%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    String getQueriesIdsAsString() {
        SuperQuery superQuery = globalQuery
        if (superQuery) {
            if (superQuery.queryType == QueryTypeEnum.SET_BUILDER) {
                superQuery = MiscUtil.unwrapProxy(superQuery)
                List <Long> ids = []
                superQuery.queries?.each { SuperQuery query ->
                    if (query.hasBlanks) {
                        ids.add(query.id)
                    }
                }
                return ids.join(',')
            } else {
                return superQuery.id
            }
        }
        return null
    }

    def getReportTasksAsJson(){
        reportTasks?.collect{it.toMap()} as JSON
    }

    @Override
    public String toString() {
        super.toString()
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && (this.dirtyPropertyNames?.contains("globalQueryValueLists") || oldValues?.keySet()?.contains("globalDateRangeInformation"))) {
            withNewSession {
                ReportConfiguration cfg = ReportConfiguration.read(id);
                if (oldValues?.keySet()?.contains("globalQueryValueLists")) {
                    oldValues.put("globalQueryValueLists", cfg.globalQueryValueLists?.toString())
                }
                if (oldValues?.keySet()?.contains("globalDateRangeInformation"))
                    oldValues.put("globalDateRangeInformation", GrailsHibernateUtil.unwrapIfProxy(cfg.globalDateRangeInformation))
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }

    def beforeInsert() {
        dateCreated = new Date()
        lastUpdated = new Date()
    }

    def beforeUpdate() {
        List dirtyProperties = this.dirtyPropertyNames
        List executionProperties = ["nextRunDate", "isEnabled", "totalExecutionTime", "executing", "numOfExecutions"]
        if (utilService.containsOnlyValues(dirtyProperties, executionProperties)) {
            return
        } else {
            lastUpdated = new Date()
        }
    }
}
