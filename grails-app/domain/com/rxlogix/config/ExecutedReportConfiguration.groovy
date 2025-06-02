package com.rxlogix.config

import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.DbUtil
import grails.gorm.DetachedCriteria
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions
import com.rxlogix.Constants
@DirtyCheck
@CollectionSnapshotAudit
abstract class ExecutedReportConfiguration extends BaseConfiguration {
    static auditable =  [ignore: ["actionItems"]]

    ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation;
    ExecutedDeliveryOption executedDeliveryOption
    ReportExecutionStatusEnum executionStatus // this doesn't actually get created because of the getExectionStatus() method at bottom!!
    List<ExecutedTemplateQuery> executedTemplateQueries = [] // why?
    SuperQuery executedGlobalQuery
    List<ExecutedQueryValueList> executedGlobalQueryValueLists
    boolean hasGeneratedCasesData = false
    ReportExecutionStatusEnum status = ReportExecutionStatusEnum.ERROR
    ExecutedCaseSeries caseSeries
    ExecutedCaseSeries cumulativeCaseSeries
    WorkflowState workflowState
    EmailConfiguration emailConfiguration
    Date executedETLDate
    Locale locale = new Locale('en')
    transient def userService
    boolean archived = false
    Boolean pvcDirty = false
    String studyDrugs
    String clazz

    //PVS use only
    boolean considerOnlyPoi = false
    boolean studyMedicationType = false

    static hasMany = [attachments                  : ExecutedPublisherSource, executedTemplateQueries: ExecutedTemplateQuery, sharedWith: SharedWith,
                      executedReportUserStates     : ExecutedReportUserState, comments: Comment, publisherReports: PublisherReport,
                      executedGlobalQueryValueLists: ExecutedQueryValueList, actionItems: ActionItem, publisherConfigurationSections: PublisherConfigurationSection]
    static hasOne = [executedDeliveryOption: ExecutedDeliveryOption]
    static mappedBy = [executedTemplateQueries: 'executedConfiguration']

    static mapping = {
        table name: "EX_RCONFIG"

        actionItems joinTable: [name: "EX_RCONFIG_ACTION_ITEMS", column: "ACTION_ITEM_ID", key: "EX_RCONFIG_ID"]
        executedTemplateQueries joinTable: [name: "EX_TEMPLT_QUERY", column: "ID", key: "EX_RCONFIG_ID"], indexColumn: [name: "EX_TEMPLT_QUERY_IDX"], cascade: 'all-delete-orphan'
        sharedWith joinTable: [name: "SHARED_WITH", column: "ID", key: "EX_RCONFIG_ID"]
        executedReportUserStates joinTable: [name: "EX_RCONFIG_USER_STATE", column: "ID", key: "EX_RCONFIG_ID"]
        tags joinTable: [name: "EX_RCONFIGS_TAGS", column: "TAG_ID", key: "EXC_RCONFIG_ID"], indexColumn: [name: "TAG_IDX"]
        poiInputsParameterValues joinTable: [name: "EX_RCONFIGS_POI_PARAMS", column: "PARAM_ID", key: "EXC_RCONFIG_ID"]
        workflowState column: "WORKFLOW_STATE_ID"
        executedDeliveryOption cascade: "all", fetch: 'join'
        comments cascade: 'all-delete-orphan', joinTable: [name: "ex_rconfig_comment_table", column: "comment_id", key: "EXC_RCONFIG_ID"]
        executedGlobalQuery column: "EX_GLOBAL_QUERY_ID"
        executedGlobalQueryValueLists joinTable: [name: "EX_GLOBAL_QUERY_VALUES", column: "EX_QUERY_VALUE_ID", key: "EX_GLOBAL_QUERY_ID"], indexColumn: [name: "EX_QUERY_VALUE_IDX"]
        hasGeneratedCasesData column: "GENERATED_CASES_DATA"
        status column: 'EX_STATUS'
        cumulativeCaseSeries column: "CUM_CASE_SERIES_ID"
        caseSeries column: "CASE_SERIES_ID"
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        locale column: "LANG_ID"
        archived column: "ARCHIVED"
        pvcDirty column: "PVC_DIRTY"
        studyDrugs column: 'STUDY_DRUGS', sqlType: DbUtil.stringType
        executedGlobalDateRangeInformation column: "EX_GLOBAL_DATE_RANGE_INFO_ID"
        executedETLDate column: "EX_ETL_DATE"
        clazz formula: 'CLASS'
        attachments cascade: "all-delete-orphan"
    }

    static constraints = {
        actionItems(nullable: true)
        executedGlobalQuery (nullable: true)
        executedTemplateQueries minSize: 1
        cumulativeCaseSeries nullable: true
        caseSeries nullable: true, validator: {val,obj->
            if(obj.hasGeneratedCasesData && !val){
                return 'invalid'
            }
        }
        emailConfiguration nullable: true
        archived nullable: false
        studyDrugs nullable: true
        pvcDirty nullable: true
        executedGlobalDateRangeInformation nullable: true
        publisherConfigurationSections nullable: true
        executedETLDate nullable: true
        considerOnlyPoi nullable: false
        studyMedicationType nullable: false
    }

    transient String getGeneratedReportNameInBrackets() {
        return this.instanceOf(ExecutedPeriodicReportConfiguration) && generatedReportName ? (" (" + generatedReportName + ")") : ""
    }

    static namedQueries = {

        allCaseQualityReports {
            eq('isDeleted', false)
            like('pvqType',  "%"+PvqTypeEnum.CASE_QUALITY.name()+";%")
            order('dateCreated', 'desc')
        }

        allSubmissionQualityReports {
            eq('isDeleted', false)
            like('pvqType',  "%"+PvqTypeEnum.SUBMISSION_QUALITY.name()+";%")
            order('dateCreated', 'desc')
        }

        configurationsWithGeneratedCases {
            eq('hasGeneratedCasesData', true)
            eq('isDeleted', false)
            order('reportName', 'asc')
            order('numOfExecutions', 'desc')
        }

        viewableByUser { currentUser ->
            sharedWith {
                eq('isDeleted', false)
                eq('user', currentUser)
            }
        }

        fetchAllDeletedIdsViewableForUser { User user ->
            projections {
                distinct("id")
            }
            and {
                eq('isDeleted', true)
                if (!user.isAdmin()) {
                    viewableByUser(user)
                }
            }
        }

        getAllExecutedReportIdByReportTypeAndUser { User user ->
            projections {
                distinct('id')
            }
            eq('isDeleted', false)
            if (!user.isAdmin()) {
                createAlias('executedDeliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
                createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
                createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
                or {
                    eq('owner.id', user?.id)
                    'in'('sw.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(user).id)
                    }
                }
            }
        }

        getAllExecutedReportIdByReportTypeAndUserBetweenDates { User user, Date startDate, Date endDate ->
            getAllExecutedReportIdByReportTypeAndUser(user)
            gte('nextRunDate', startDate)
            le('nextRunDate', endDate)
            ne('class', ExecutedIcsrProfileConfiguration.class.name)  //Exclude ICSR Profile as its something which will execute continously.
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }

        }

        getByActionItem { actionItemId ->
            maxResults(1)
            actionItems {
                eq 'id', actionItemId
            }
        }

        sharedWithFilter { Map sharedWithMap, User user ->
            or {
                if (sharedWithMap?.ownerId) {
                    eq('owner.id', sharedWithMap.ownerId)
                }
                if (sharedWithMap?.usersId) {
                    or {
                        'in'('sw.id', sharedWithMap.usersId)
                        'in'('swg.id', new DetachedCriteria(UserGroupUser).build {
                            projections {
                                distinct('userGroup.id')
                            }
                            'in'('user.id', sharedWithMap.usersId)
                        })
                    }
                }
                if (sharedWithMap?.groupsId) {
                    'in'('swg.id', sharedWithMap.groupsId)
                }
                if (sharedWithMap?.team && user) {
                    or {
                        user.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    }
                }
            }
        }
        ownedByAndSharedWithUser { User currentUser, Boolean isAdmin, Boolean includeArchived ->
            createAlias('executedDeliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            createAlias('executedReportUserStates', 'state', CriteriaSpecification.LEFT_JOIN, Restrictions.eq('user', currentUser))
            eq("isDeleted", false)
            or {
                isNull('state.id')
                and {
                    eq('state.isDeleted', false)
                }
            }

            if (!includeArchived) {
                or {
                    and {
                        isNull('state.id')
                        eq("archived", false)
                    }
                    eq('state.isArchived', false)
                }
            }
            if (!isAdmin) {
                or {
                    currentUser.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser?.id)
                    'in'('sw.id', currentUser.id)
                    if (UserGroup.fetchAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }

        fetchAllByReportName { User user, String name ->
            projections {
                distinct('id')
                property("reportName")
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(name)}%")
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
        }

        countAllByReportName { User user, String name ->
            projections {
                countDistinct("id")
            }
            'in'("status", ReportExecutionStatusEnum.reportsListingStatuses)
            eq('isDeleted', false)
            iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(name)}%")
            ownedByAndSharedWithUser(user, user.isAdmin(), false)
        }

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        getLatestExecutedConfigurationForRerun{ ReportConfiguration reportConfiguration ->
            projections {
                property('id')
            }
            'in'('status' , ReportExecutionStatusEnum.inProgressStatusesList)
            eq('owner' , reportConfiguration.owner)
            eq('reportName' , reportConfiguration.reportName)
            order('dateCreated' , 'desc')
            maxResults(1)
        }

    }

    public getActionItemStatus() {
        Set liveActionItems = actionItems?.findAll { !it.isDeleted }
        if(!liveActionItems) return null
        boolean waiting=false

        for(ai in liveActionItems){
            if((ai.status!=StatusEnum.CLOSED) && (ai.dueDate<new Date())) return ActionItemGroupState.OVERDUE
            if(ai.status!=StatusEnum.CLOSED) waiting=true
        }
        return waiting?ActionItemGroupState.WAITING:ActionItemGroupState.CLOSED
    }

    boolean isViewableBy(User currentUser) {
        if(!currentUser) return false
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || isVisible(currentUser) || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    boolean isVisible(User currentUser) {
        return executedDeliveryOption?.isSharedWith(currentUser)
    }

    @Override
    transient boolean isRunning() {
        if (!getId()) {
            return false
        }
        if (this.instanceOf(ExecutedPeriodicReportConfiguration)) {
            return ExecutionStatus.countByEntityIdAndEntityTypeInListAndExecutionStatusInList(getId(), [ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL, ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION], [ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.BACKLOG]) > 0
        }
        return ExecutionStatus.countByEntityIdAndEntityTypeInListAndExecutionStatusInList(getId(), [ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION, ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION], [ReportExecutionStatusEnum.GENERATING, ReportExecutionStatusEnum.BACKLOG]) > 0
    }

    boolean canAddEditCases() {
        return !(status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED])
    }

// To check the execution status of each result, if anyone has error returns error as status
    def getExecutionStatus() {
        def val = ReportExecutionStatusEnum.COMPLETED.value()
        for (it in executedTemplateQueries) {
            if (it.reportResult?.executionStatus == ReportExecutionStatusEnum.ERROR) {
                val = ReportExecutionStatusEnum.ERROR.value()
                break
            } else if (it.reportResult?.executionStatus != ReportExecutionStatusEnum.COMPLETED) {
                val = it.reportResult?.executionStatus?.value()
                break
            }
        }
        return val
    }

    public Set<ReportField> getSelectedColumnsForSection() {
        Set columnsList = []
        for (it in executedTemplateQueries) {
            if(it.executedTemplate instanceof ExecutedCaseLineListingTemplate ) {
                columnsList.add(it.executedTemplate.getAllSelectedFieldsInfo().reportField as Set)
            }
        }
        return columnsList.flatten()
    }

    public String getReportNameAndVersionNumber(){
        return "${reportName} - ${numOfExecutions}"
    }

    /*
     * Gets list of executed template queries to be shown on criteria page
     */
    public List<ExecutedTemplateQuery> getExecutedTemplateQueriesForCriteria() {
        if(!containsTemplateSet()){
            return getExecutedTemplateQueriesForProcessing()
        }
        return executedTemplateQueries.findAll {getSectionExTempQueriesMap().keySet().contains(it)}
    }

    /*
     * Verifies whether configuration has at least one
     * executed template type as TemplateSet
     */
    boolean containsTemplateSet(){
        boolean hasTempSet = false
        executedTemplateQueries.each {
            //If templateSet
            if(isTemplateSet(it)) {
                hasTempSet = true
            }
        }
        return hasTempSet
    }

    public List<ExecutedTemplateQuery> getExecutedTemplateQueriesForProcessing() {
        return executedTemplateQueries.findAll { it }
    }

    public List<ExecutedTemplateQuery> fetchExecutedTemplateQueriesByCompletedStatus() {
        if ((this.getClass() == ExecutedPeriodicReportConfiguration) && (status in [ReportExecutionStatusEnum.GENERATED_DRAFT, ReportExecutionStatusEnum.GENERATING_DRAFT, ReportExecutionStatusEnum.GENERATED_CASES, ReportExecutionStatusEnum.GENERATING_NEW_SECTION, ReportExecutionStatusEnum.SUBMITTED]) && this.finalLastRunDate==null) {
            return executedTemplateQueries.findAll{it.draftReportResult && it.draftReportResult.executionStatus == ReportExecutionStatusEnum.COMPLETED }
        }
        //It it does not have TemplateSet
        if(!containsTemplateSet()){
            return executedTemplateQueries.findAll{it.finalReportResult && it.finalReportResult.executionStatus == ReportExecutionStatusEnum.COMPLETED }
        }
        //Otherwise if it has TemplateSet
        Map<ExecutedTemplateQuery, List<ExecutedTemplateQuery>> sectionExTempQueriesMap = getSectionExTempQueriesMap()
        return executedTemplateQueries.findAll{it.finalReportResult && it.finalReportResult.executionStatus == ReportExecutionStatusEnum.COMPLETED && (sectionExTempQueriesMap.keySet().contains(it))}
    }

    /*
     * Fetches Map where section are keys -> to be shown in dropdown on report/show page) and
     * values are list of executed template queries for which report has to be created
     * when that section is selected
     */
    def getSectionExTempQueriesMap(){
        /*
         * Keys of this map are used to show sections in dropdown box of report's show page,
         * and values (List of executed template queries) are used to create report for that section
         */
       Map<ExecutedTemplateQuery, List<ExecutedTemplateQuery>> sectionExTempQueriesMap = new LinkedHashMap<>()
        Iterator<ExecutedTemplateQuery> iter = executedTemplateQueries.iterator();
        //While it has more executed template queries
        while(iter.hasNext()){
            ExecutedTemplateQuery exTempQuery = iter.next()
            //If it's executedTemplate type is TemplateSet
            if(isTemplateSet(exTempQuery) && exTempQuery.executedTemplate.linkSectionsByGrouping && !signalConfiguration) {
                List<ExecutedTemplateQuery> nestedExTempQueries = []
                //Iterate through each nested template and add in list of executed template queries
                ((TemplateSet)exTempQuery.executedTemplate).nestedTemplates.each {
                    nestedExTempQueries.add(exTempQuery)
                }
                /* Puts that list of executed template queries of all nested
                 * templates as value in map where key is TemplateSet's executed template query
                 */
                sectionExTempQueriesMap.put(exTempQuery, nestedExTempQueries)
            }else{
                /*
                 * If not TemplateSet, put that executed template
                 * query as key and value both
                 */
                sectionExTempQueriesMap.put(exTempQuery, [exTempQuery])
            }
        }
        return sectionExTempQueriesMap
    }

    /*
     * Verifies if executed template is TemplateSet
     */
    boolean isTemplateSet(ExecutedTemplateQuery executedTemplateQuery ){
        if(executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET){
            return true
        }
        return false
    }

    boolean hasReachedToFinal(){
        return (status in [ReportExecutionStatusEnum.GENERATING_FINAL_DRAFT,ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT,ReportExecutionStatusEnum.SUBMITTED, ReportExecutionStatusEnum.COMPLETED])
    }

    @Override
    String getUsedEventSelection() {
        return null
    }

    @Override
    String getUsedEventGroupSelection() {
        return null
    }

    public Boolean getContainsData() {
        Boolean flag = false
        executedTemplateQueries.each {
            if (it.reportResult?.reportRows) {
                flag = true
                return false
            }
        }
        return flag
    }

    public Set<User> getAllSharedUsers(){
        Set<User> users = []
        users.addAll(executedDeliveryOption.sharedWith)
        executedDeliveryOption.sharedWithGroup.each {UserGroup userGroup->
            users.addAll(userGroup.users)
        }
        users.flatten()
    }

    @Override
    List<Date> getReportMinMaxDate() {
        Date minDate = null
        Date maxDate = null
        List<ExecutedTemplateQuery> executedTemplateQueries = getExecutedTemplateQueriesForProcessing()
        List<ExecutedTemplateQuery> templateQueriesForMinMax = executedTemplateQueries?.findAll {
            it.executedDateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.CUMULATIVE
        }
        // For PVR-21232 -- For the calculation of Max End i.e Max End Date = Max End Date from all the sections and global date which have definite dates.
        List<TemplateQuery> templateQueriesForMaxEnd = templateQueriesForMinMax?.findAll {
            it.executedDateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.PR_DATE_RANGE
        }
        if (templateQueriesForMinMax) {
            minDate = templateQueriesForMinMax*.startDate.min()
            maxDate = templateQueriesForMinMax*.endDate.max()
        } else if (executedTemplateQueries) {
            ExecutedTemplateQuery executedTemplateQuery = executedTemplateQueries.first()
            minDate = executedTemplateQuery.startDate
            maxDate = executedTemplateQuery.endDate
            if (executedGlobalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
                minDate = executedGlobalDateRangeInformation?.dateRangeStartAbsolute
                maxDate = executedGlobalDateRangeInformation?.dateRangeEndAbsolute
            }

        }
        if (templateQueriesForMaxEnd){
            maxDate = templateQueriesForMaxEnd*.endDate.max()
        }
        Date globalStartDate = null
        if (executedGlobalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
            globalStartDate = executedGlobalDateRangeInformation?.dateRangeStartAbsolute
        }
        Date globalEndDate  = null
        if (executedGlobalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
            globalEndDate = executedGlobalDateRangeInformation?.dateRangeEndAbsolute
        }
        minDate = (minDate && globalStartDate) ? [minDate, globalStartDate].min() : minDate
        maxDate = (maxDate && globalEndDate) ? [maxDate, globalEndDate].max() : maxDate
        return [minDate, maxDate]
    }

    public int isAnyCumulativeTQ(){
        executedTemplateQueries.any{ it.usedDateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUMULATIVE ||
                (it.usedTemplate instanceof DataTabulationTemplate && it.usedTemplate.hasCumulative())} ? 1 : 0
    }

    def deleteForUser(User user) {
        ExecutedReportUserState state = ExecutedReportUserState.findByUserAndExecutedConfiguration(user, this)
        if (!state) {
            state = new ExecutedReportUserState(user: user, executedConfiguration: this, isArchived: false)
        }
        state.isDeleted = true
        state.save()
    }

    boolean isFavorite(User user) {
        return executedReportUserStates.find { item -> item.user == user }?.isFavorite
    }

    static List<ExecutedReportConfiguration> getLatestCaseQualityReports() {
        return ExecutedReportConfiguration.findAllByPvqTypeLikeAndIsDeletedAndStatus("%"+PvqTypeEnum.CASE_QUALITY.name()+";%", false, ReportExecutionStatusEnum.COMPLETED)
    }

    static List<ExecutedReportConfiguration> getLatestSubmissionQualityReports() {
        return ExecutedReportConfiguration.findAllByPvqTypeLikeAndIsDeletedAndStatus("%"+PvqTypeEnum.SUBMISSION_QUALITY.name()+";%", false, ReportExecutionStatusEnum.COMPLETED)
    }

    String getAttachmentsString() {
        return attachments?.collect { it.name }?.join(",")
    }

    public Date getExecutedAsOfVersionDate(){
        Date executedAsOfVersionDate = null
        if(instanceOf(ExecutedConfiguration) && evaluateDateAs in [EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD, EvaluateCaseDateEnum.ALL_VERSIONS]){
            List executedSections = executedTemplateQueriesForProcessing
            if(executedSections){
                executedAsOfVersionDate = ExecutedDateRangeInformation.createCriteria().get{
                    projections{
                        max('executedAsOfVersionDate')
                    }
                    createAlias('executedTemplateQuery', 'exTq')
                    'in'('exTq.id', executedSections*.id)
                } as Date
            }
        }

        return executedAsOfVersionDate
    }

    @Override
    public String toString() {
        super.toString()
    }

    static List<List<ExecutedReportConfiguration>> fetchLatestExecutedConfigs(Map reportKeyToConfigMap) {
        List<List<ExecutedReportConfiguration>> executedConfigs = []
        reportKeyToConfigMap.values().toList().collate(Constants.COLLATE_LENGTH).each { batch ->
            List<ExecutedReportConfiguration> batchResults = ExecutedReportConfiguration.createCriteria().list {
                'in'('status', ReportExecutionStatusEnum.inProgressStatusesList)
                or {
                    batch.each { rc ->
                        and {
                            eq('owner', rc.owner)
                            eq('reportName', rc.reportName)
                        }
                    }
                }
                order('dateCreated', 'desc')
            }

            executedConfigs << batchResults
        }

        return executedConfigs
    }

}