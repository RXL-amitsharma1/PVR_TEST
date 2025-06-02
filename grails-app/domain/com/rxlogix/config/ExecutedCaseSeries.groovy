package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.MiscUtil
import grails.gorm.DetachedCriteria
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions

class ExecutedCaseSeries extends BaseCaseSeries {

    boolean isTemporary = false
    String caseSeriesOwner = Constants.PVR_CASE_SERIES_OWNER //To identify whether the executed case series was generated from PVR/PVS
    SuperQuery executedGlobalQuery
    ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation
    List<QueryValueList> executedGlobalQueryValueLists
    //Exection Status Fields
    EmailConfiguration emailConfiguration
    Boolean isSpotfireCaseSeries = false
    String associatedSpotfireFile
    static hasOne = [executedDeliveryOption: ExecutedCaseDeliveryOption]

    ExecutedCaseSeries(CaseSeries caseSeries) {
        setProperties(caseSeries.properties)
    }

    String generateUniqueName(String name, User owner) {
        String newName = new String(name)
        if (ExecutedCaseSeries.countBySeriesNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
            log.error("ExecutedCaseSeries UniqueName issue from PeriodicReport for ${newName} and ${owner.id}")
            int count = 1
            newName = "$name ($count)"
            while (ExecutedCaseSeries.countBySeriesNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
                newName = "Copy of $name (${++count})"
            }
        }
        return newName
    }

    ExecutedCaseSeries(ExecutedPeriodicReportConfiguration executedReportConfiguration, boolean generateCumulative) {
        seriesName = generateUniqueName((executedReportConfiguration.reportName + (generateCumulative ? "-CUM" : "") + "-" + executedReportConfiguration.numOfExecutions), executedReportConfiguration.owner)
        numExecutions = executedReportConfiguration.numOfExecutions
        dateRangeType = executedReportConfiguration.dateRangeType
        owner = executedReportConfiguration.owner
        tenantId = executedReportConfiguration.tenantId
        asOfVersionDate = executedReportConfiguration.asOfVersionDate
        ExecutedCaseDeliveryOption executedCaseDeliveryOption = new ExecutedCaseDeliveryOption(sharedWith: executedReportConfiguration?.executedDeliveryOption?.sharedWith,
                sharedWithGroup: executedReportConfiguration?.executedDeliveryOption?.sharedWithGroup,
                emailToUsers: executedReportConfiguration?.executedDeliveryOption?.emailToUsers,
                attachmentFormats: executedReportConfiguration?.executedDeliveryOption?.attachmentFormats)
        executedDeliveryOption = executedCaseDeliveryOption
        if (!generateCumulative) {
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(MiscUtil.getObjectProperties(executedReportConfiguration.executedGlobalDateRangeInformation, CaseSeriesDateRangeInformation.propertiesToUseForCopying))
        } else {
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(dateRangeEndAbsolute: executedReportConfiguration.executedGlobalDateRangeInformation?.dateRangeEndAbsolute, dateRangeStartAbsolute: BaseDateRangeInformation.MIN_DATE, dateRangeEnum: com.rxlogix.enums.DateRangeEnum.CUMULATIVE)
        }
        evaluateDateAs = executedReportConfiguration.evaluateDateAs
        excludeFollowUp = executedReportConfiguration.excludeFollowUp
        includeLockedVersion = executedReportConfiguration.includeLockedVersion
        includeAllStudyDrugsCases = executedReportConfiguration.includeAllStudyDrugsCases
        excludeNonValidCases = executedReportConfiguration.excludeNonValidCases
        excludeDeletedCases = executedReportConfiguration.excludeDeletedCases
        suspectProduct = executedReportConfiguration.suspectProduct
        productSelection = executedReportConfiguration.productSelection
        productGroupSelection = executedReportConfiguration.productGroupSelection
        eventSelection = executedReportConfiguration.usedEventSelection
        eventGroupSelection = executedReportConfiguration.usedEventGroupSelection
        studySelection = executedReportConfiguration.studySelection
        executedGlobalQuery = executedReportConfiguration.executedGlobalQuery
        locale = executedReportConfiguration.locale
        createdBy = executedReportConfiguration.createdBy
        modifiedBy = executedReportConfiguration.modifiedBy
        isMultiIngredient = executedReportConfiguration.isMultiIngredient
        includeWHODrugs = executedReportConfiguration.includeWHODrugs
        executedReportConfiguration.executedGlobalQueryValueLists.each {
            ExecutedQueryValueList queryValueList = new ExecutedQueryValueList(query: it.query)
            it.parameterValues.each { parameterValue ->
                ParameterValue tempValue = null
                if (parameterValue instanceof ExecutedCustomSQLValue) {
                    tempValue = new CustomSQLValue(parameterValue.properties)
                } else if (parameterValue instanceof ExecutedQueryExpressionValue) {
                    tempValue = new QueryExpressionValue(parameterValue.properties)
                }
                queryValueList.addToParameterValues(tempValue)
            }
            this.addToExecutedGlobalQueryValueLists(queryValueList)
        }
        executing = true
    }

    ExecutedCaseSeries(ExecutedIcsrReportConfiguration executedReportConfiguration, boolean generateCumulative) {
        seriesName = generateUniqueName((executedReportConfiguration.reportName + (generateCumulative ? "-CUM" : "") + "-" + executedReportConfiguration.numOfExecutions), executedReportConfiguration.owner)
        numExecutions = executedReportConfiguration.numOfExecutions
        dateRangeType = executedReportConfiguration.dateRangeType
        owner = executedReportConfiguration.owner
        tenantId = executedReportConfiguration.tenantId
        asOfVersionDate = executedReportConfiguration.asOfVersionDate
        ExecutedCaseDeliveryOption executedCaseDeliveryOption = new ExecutedCaseDeliveryOption(sharedWith: executedReportConfiguration?.executedDeliveryOption?.sharedWith,
                sharedWithGroup: executedReportConfiguration?.executedDeliveryOption?.sharedWithGroup,
                emailToUsers: executedReportConfiguration?.executedDeliveryOption?.emailToUsers,
                attachmentFormats: executedReportConfiguration?.executedDeliveryOption?.attachmentFormats)
        executedDeliveryOption = executedCaseDeliveryOption
        if (!generateCumulative) {
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(MiscUtil.getObjectProperties(executedReportConfiguration.executedGlobalDateRangeInformation, CaseSeriesDateRangeInformation.propertiesToUseForCopying))
        } else {
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(dateRangeEndAbsolute: executedReportConfiguration.executedGlobalDateRangeInformation?.dateRangeEndAbsolute, dateRangeStartAbsolute: BaseDateRangeInformation.MIN_DATE, dateRangeEnum: com.rxlogix.enums.DateRangeEnum.CUMULATIVE)
        }
        evaluateDateAs = executedReportConfiguration.evaluateDateAs
        excludeFollowUp = executedReportConfiguration.excludeFollowUp
        includeLockedVersion = executedReportConfiguration.includeLockedVersion
        includeAllStudyDrugsCases = executedReportConfiguration.includeAllStudyDrugsCases
        excludeNonValidCases = executedReportConfiguration.excludeNonValidCases
        excludeDeletedCases = executedReportConfiguration.excludeDeletedCases
        suspectProduct = executedReportConfiguration.suspectProduct
        productSelection = executedReportConfiguration.productSelection
        productGroupSelection = executedReportConfiguration.productGroupSelection
        eventSelection = executedReportConfiguration.usedEventSelection
        eventGroupSelection = executedReportConfiguration.usedEventGroupSelection
        studySelection = executedReportConfiguration.studySelection
        executedGlobalQuery = executedReportConfiguration.executedGlobalQuery
        locale = executedReportConfiguration.locale
        createdBy = executedReportConfiguration.createdBy
        modifiedBy = executedReportConfiguration.modifiedBy
        isMultiIngredient = executedReportConfiguration.isMultiIngredient
        includeWHODrugs = executedReportConfiguration.includeWHODrugs
        executedReportConfiguration.executedGlobalQueryValueLists.each {
            ExecutedQueryValueList queryValueList = new ExecutedQueryValueList(query: it.query)
            it.parameterValues.each { parameterValue ->
                ParameterValue tempValue = null
                if (parameterValue instanceof ExecutedCustomSQLValue) {
                    tempValue = new CustomSQLValue(parameterValue.properties)
                } else if (parameterValue instanceof ExecutedQueryExpressionValue) {
                    tempValue = new QueryExpressionValue(parameterValue.properties)
                }
                queryValueList.addToParameterValues(tempValue)
            }
            this.addToExecutedGlobalQueryValueLists(queryValueList)
        }
        executing = true
    }
    ExecutedCaseSeries(ExecutedTemplateQuery executedTemplateQuery) {
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.executedConfiguration
        Boolean isExecutedPeriodicReport = executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration
        numExecutions = 1
        tenantId = executedReportConfiguration.tenantId
        dateRangeType = executedReportConfiguration.dateRangeType
        asOfVersionDate = executedReportConfiguration.asOfVersionDate
        DateRangeEnum dateRangeEnum = executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum
        if (dateRangeEnum != DateRangeEnum.CUMULATIVE) {
            BaseDateRangeInformation dateRangeInformation = isExecutedPeriodicReport ? executedReportConfiguration.executedGlobalDateRangeInformation : executedTemplateQuery.executedDateRangeInformationForTemplateQuery
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(MiscUtil.getObjectProperties(dateRangeInformation, CaseSeriesDateRangeInformation.propertiesToUseForCopying))
        } else {
            executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(dateRangeEndAbsolute: asOfVersionDate ?: new Date(), dateRangeStartAbsolute: BaseDateRangeInformation.MIN_DATE, dateRangeEnum: DateRangeEnum.CUMULATIVE)
        }
        evaluateDateAs = executedReportConfiguration.evaluateDateAs
        excludeFollowUp = executedReportConfiguration.excludeFollowUp
        includeLockedVersion = executedReportConfiguration.includeLockedVersion
        includeAllStudyDrugsCases = executedReportConfiguration.includeAllStudyDrugsCases
        excludeNonValidCases = executedReportConfiguration.excludeNonValidCases
        excludeDeletedCases = executedReportConfiguration.excludeDeletedCases
        suspectProduct = executedReportConfiguration.suspectProduct
        productSelection = executedReportConfiguration.productSelection
        productGroupSelection = executedReportConfiguration.productGroupSelection
        eventSelection = executedReportConfiguration.usedEventSelection
        eventGroupSelection = executedReportConfiguration.usedEventGroupSelection
        studySelection = executedReportConfiguration.studySelection
        isMultiIngredient = executedReportConfiguration.isMultiIngredient
        includeWHODrugs = executedReportConfiguration.includeWHODrugs
        executedGlobalQuery = executedTemplateQuery.executedQuery ?: isExecutedPeriodicReport ? executedReportConfiguration.executedGlobalQuery : null
        List<ExecutedQueryValueList> executedQueryValueLists = executedTemplateQuery.executedQueryValueLists ?: isExecutedPeriodicReport ? executedReportConfiguration.executedGlobalQueryValueLists : null
        if(executedQueryValueLists){
            executedQueryValueLists.each {
                ExecutedQueryValueList queryValueList = new ExecutedQueryValueList(query: it.query)
                it.parameterValues.each { parameterValue ->
                    ParameterValue tempValue = null
                    if (!parameterValue.hasProperty("reportField")) {
                        tempValue = new CustomSQLValue(parameterValue.properties)
                    } else {
                        tempValue = new QueryExpressionValue(parameterValue.properties)
                    }
                    queryValueList.addToParameterValues(tempValue)
                }
                this.addToExecutedGlobalQueryValueLists(queryValueList)
            }
        }
        executing = false
    }

    static hasMany = [executedGlobalQueryValueLists: ExecutedQueryValueList, executedCaseSeriesStates: ExecutedCaseSeriesUserState]

    static constraints = {
        executedGlobalQuery (nullable: true)
        emailConfiguration nullable: true
        seriesName(nullable: false, blank: false, maxSize: 555,validator: { val, obj ->
            //Name is unique to user and numExecutions
            if (!obj.id || obj.isDirty("seriesName") || obj.isDirty("owner")) {
                long count = ExecutedCaseSeries.createCriteria().count {
                    eq('seriesName', val, [ignoreCase: true])
                    eq('owner', obj.owner)
                    eq('numExecutions', obj.numExecutions)
                    executedCaseSeriesStates {
                        eq("isDeleted", false)
                    }
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.executed.caseSeries.seriesName.unique.per.user"
                }
            }
        })
        caseSeriesOwner(nullable: false, blank: false)
        associatedSpotfireFile(nullable:true)
    }

    static mapping = {
        table name: "EX_CASE_SERIES"

        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        emailConfiguration cascade: 'all'
        tags joinTable: [name: "EX_CASE_SERIES_TAGS", column: "TAG_ID", key: "EX_CASE_SERIES_ID"], indexColumn: [name: "TAG_IDX"], cascade: 'all-delete-orphan'
        executedDeliveryOption cascade: 'all'
        isTemporary column: "IS_TEMPORARY"
        isSpotfireCaseSeries column: "IS_SPOTFIRE_CASE_SERIES"
        executedGlobalQuery column: "EX_SUPER_QUERY_ID"
        executedCaseSeriesDateRangeInformation column: "EX_CS_DATE_RANGE_INFO_ID"
        executedGlobalQueryValueLists joinTable: [name: "EX_CASE_SERIES_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "EX_CASE_SERIES_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        executedCaseSeriesStates joinTable: [name: "EX_CASE_SERIES_USER_STATE", column: "ID", key: "EX_CASE_SERIES_ID"]
        caseSeriesOwner column: "CASE_SERIES_OWNER"
        associatedSpotfireFile column: "ASSOCIATED_SPOTFIRE_FILE"
    }

    static namedQueries = {
        getByOriginalQueryId { Long queryId, User owner ->
            eq('owner', owner)
            'executedGlobalQuery' {
                eq('originalQueryId', queryId)
            }
            eq('isTemporary', true)
            searchByTenant(Tenants.currentId() as Long)
        }
        fetchAllOwners { User user, search ->
            projections {
                distinct("owner")
            }
            ownedByAndSharedWithUser(user, user.isAdmin())
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        countAllOwners { User user, search ->
            projections {
                countDistinct("owner")
            }
            ownedByAndSharedWithUser(user, user.isAdmin())
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        fetchDistinctSpotfireFileNames { User user ->
            projections {
                distinct("associatedSpotfireFile")
            }
            isNotNull("associatedSpotfireFile")
            ownedByAndSharedWithUser(user, user.isAdmin())
        }

        countCaseSeriesBySearchString { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            fetchCaseSeriesBySearchStringQuery(filter)
        }
        fetchCaseSeriesBySearchString { LibraryFilter filter ->
            projections {
                distinct('id')
                property("dateCreated")
                property("qualityChecked")
                property("lastUpdated")
                property("seriesName")
                property("numExecutions")
                property("description")
                'owner' {
                    property("fullName", "fullName")
                }
                property("state.isFavorite", "isFavorite")
            }
            fetchCaseSeriesBySearchStringQuery(filter)
        }
        fetchCaseSeriesBySearchStringQuery { LibraryFilter filter ->

            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if (filter.search) {
                or {
                    iLikeWithEscape('seriesName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            ownedByAndSharedWithUser(filter.user,filter.user?.isAdmin())
            if (!filter.user?.isAdmin() || filter.sharedWith?.ownerId || filter.sharedWith?.usersId || filter.sharedWith?.groupsId || filter.sharedWith?.team) {
                or {
                    if (filter.sharedWith?.ownerId) {
                        eq('owner.id', filter.sharedWith.ownerId)
                    }
                    if (filter.sharedWith?.usersId) {
                        or {
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
            }

            if (filter.advancedFilterCriteria) {
                createAlias('exDO.emailToUsers', 'emails', CriteriaSpecification.LEFT_JOIN)
                createAlias('emailConfiguration', 'emc', CriteriaSpecification.LEFT_JOIN)
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            eq('isTemporary', false)
            createAlias('executedCaseSeriesStates', 'state', CriteriaSpecification.LEFT_JOIN, Restrictions.eq('user', filter.user))
            or {
                isNull('state.id')
                and {
                    eq('state.isDeleted', false)
                    if (!filter.includeArchived) eq('state.isArchived', false)
                }
            }
            eq('executing', false)
            if (filter.favoriteSort) {
                and {
                    order('state.isFavorite', 'asc')
                    order('lastUpdated', 'desc')
                }
            }
        }

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        countAvailableByUser { User user, String search ->
            projections {
                countDistinct("id")
            }
            availableByUserQuery(user, search)
        }
        availableByUser { User user, String search ->
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                distinct('id', 'id')
                property("seriesName", "seriesName")
                property("numExecutions", "numExecutions")
                property("qualityChecked", "qualityChecked")
                property("st.isFavorite","isFavorite")
            }
            availableByUserQuery(user, search)
        }
        availableByUserQuery { User user, String search ->

            ownedByAndSharedWithUser(user, user.isAdmin())
            eq('isTemporary', false)
            eq('executing', false)
            def _search = search
            boolean qc = false
            if (search && search.toLowerCase().startsWith("qced")) {
                qc = true
                _search = search.substring(5).trim()
            }
            and {
                if (qc) eq('qualityChecked', true)
                if (_search) iLikeWithEscape('seriesName', "%${EscapedILikeExpression.escapeString(_search)}%")
            }
            createAlias('executedCaseSeriesStates', 'st', org.hibernate.criterion.CriteriaSpecification.LEFT_JOIN, org.hibernate.criterion.Restrictions.eq('st.user', user))
            or {
                isNull('st.id')
                and {
                    eq('st.isDeleted', false)
                    eq('st.isArchived', false)
                }
            }
            and {
                order('st.isFavorite', 'asc')
                order('qualityChecked', 'desc')
                order('seriesName', 'asc')
                order('numExecutions', 'asc')

            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        ownedByAndSharedWithUserAndSearchString { User user, String searchString ->
            projections {
                distinct("seriesName")
                property("caseSeriesOwner")
                'owner' {
                    property("fullName", "fullName")
                    property("id", "ownerId")
                }
            }
            ownedByAndSharedWithUser(user,user.isAnyAdmin())
            eq('isTemporary', false)
            if (searchString) {
                iLikeWithEscape('seriesName', "%${EscapedILikeExpression.escapeString(searchString)}%")
            }
        }

        fetchLatestByOwnerAndSeriesName { Long userId, String seriesName, String caseSeriesOwner ->
            fetchByOwnerAndSeriesName(userId,seriesName)
            eq("caseSeriesOwner", caseSeriesOwner)
            eq('isTemporary', false)
            order('numExecutions','dec')
        }

        fetchByOwnerAndSeriesName { Long userId, String seriesName ->
            projections {
                property("id")
                property("seriesName")
                property("caseSeriesOwner")
                'owner' {
                    property("fullName", "fullName")
                }
            }
            eq("seriesName", seriesName)
            eq('owner.id', userId)
            eq('isDeleted', false)
            eq('executing', false)
            eq('isTemporary', false)
        }

        ownedByAndSharedWithUser { User currentUser, Boolean isAdmin ->
            createAlias('executedDeliveryOption', 'exDO', CriteriaSpecification.LEFT_JOIN)
            createAlias('exDO.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('exDO.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            eq('executing', false)
            if (!isAdmin) {
                or {
                    currentUser.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser.id)
                    'in'('sw.id', currentUser.id)
                    if (UserGroup.countAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }

        getAllExecutedCaseSeriesIdByUserAndBetweenDates { User user, Date startDate, Date endDate ->
            getAllExecutedCaseSeriesIdByUser(user)
            gte('nextRunDate', startDate)
            le('nextRunDate', endDate)
            eq('caseSeriesOwner', Constants.PVR_CASE_SERIES_OWNER)
            eq('isTemporary', false)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }

        getAllExecutedCaseSeriesIdByUser { User user ->
            projections {
                distinct('id')
            }
            eq('isDeleted', false)
            if (!user.isAdmin()) {
                createAlias('executedDeliveryOption', 'exDO', CriteriaSpecification.LEFT_JOIN)
                createAlias('exDO.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
                createAlias('exDO.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
                or {
                    eq('owner.id', user?.id)
                    'in'('sw.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(user).id)
                    }
                }
            }
        }

        fetchTemporaryCaseSeriesFor { CaseSeries cs ->
            eq('seriesName', cs.seriesName)
            eq('isTemporary', true)
            eq('owner.id', cs.owner?.id)
            eq('caseSeriesOwner', Constants.PVR_CASE_SERIES_OWNER)
            eq('tenantId', cs.tenantId)
        }

    }

    ExecutedReportConfiguration findAssociatedConfiguration(){
        if(this.id){
            return ExecutedReportConfiguration.findByCaseSeriesOrCumulativeCaseSeries(this, this)
        }
        return null
    }

    String getSeriesNameWithNumExecutions() {
        return (seriesName ?: "") + " - " + (numExecutions ?: 0)
    }

    String getReportName(){
        return seriesName
    }

    boolean isFavorite(User user) {
        return executedCaseSeriesStates.find { item -> item.user == user }?.isFavorite
    }

    public Set<User> getAllSharedUsers(){
        Set<User> users = []
        users.addAll(executedDeliveryOption.sharedWith)
        executedDeliveryOption.sharedWithGroup.each {UserGroup userGroup->
            users.addAll(userGroup.users)
        }
        users
    }

    static getActiveUsersAndUserGroups(User user, String term) {
        def result = [users: [], userGroups: []]

        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableReportsSql = " where rc.id in (select rc1.id from ExecutedCaseSeries as rc1 left join rc1.executedDeliveryOption as dop1 left join dop1.sharedWithGroup as swg1 left join dop1.sharedWith as swu1 " +
                "where rc1.owner.id=:userid or swu1.id=:userid or swg1.id in (:groupIdsForUser))"
        String groupsSql = "from UserGroup as ug where " +
                (term?" lower(ug.name) like :term and ":"")+
                "ug.id in (select swg.id from ExecutedCaseSeries as rc join rc.executedDeliveryOption as dop join dop.sharedWithGroup as swg " +
                (user.isAdmin() ? "" : userViewableReportsSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"") +
                " (u.id in (select swu.id from ExecutedCaseSeries as rc join rc.executedDeliveryOption as dop join dop.sharedWith as swu" +
                (user.isAdmin() ? "" : userViewableReportsSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))"
        Map groupParams = user.isAdmin() ? [:] : [userid: user.id, groupIdsForUser: groupIdsForUser]
        if(term) groupParams.put('term','%'+term.toLowerCase()+'%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        Map userParams = user.isAdmin() ? [groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [userid: user.id, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if(term) userParams.put('term','%'+term.toLowerCase()+'%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    def deleteForUser(User user) {
        ExecutedCaseSeriesUserState state = ExecutedCaseSeriesUserState.findByUserAndExecutedCaseSeries(user, this)
        if (!state) {
            state = new ExecutedCaseSeriesUserState(user: user, executedCaseSeries: this, isArchived: false)
        }
        state.isDeleted = true
        state.save()
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || isVisible(currentUser) || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    public boolean isVisible(User currentUser) {
        return executedDeliveryOption.isSharedWith(currentUser)
    }

    @Override
    public String toString() {
        super.toString()
    }
}
