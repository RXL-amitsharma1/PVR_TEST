package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.QueryParamDTO
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTarget
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.AgencyName
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.context.NoSuchMessageException

import java.text.SimpleDateFormat

@Transactional
class QueryService {
    def userService
    def sqlGenerationService
    def messageSource
    def reportFieldService
    def customMessageService
    def CRUDService

    // Re-assess Listedness
    private static final RLDS = "RLDS"

    @Transactional(readOnly = true)
    SuperQuery copyQuery(SuperQuery originalQuery, User owner) {
        SuperQuery newQuery = null
        if (originalQuery.queryType == QueryTypeEnum.QUERY_BUILDER) {
            newQuery = new Query(originalQuery.properties)
            newQuery.queryExpressionValues = []
            originalQuery.queryExpressionValues.each {
                newQuery.addToQueryExpressionValues(new QueryExpressionValue(key: it.key,
                        reportField: it.reportField, operator: it.operator, value: "", specialKeyValue: it.specialKeyValue, isFromCopyPaste: it.isFromCopyPaste))
            }
        } else if (originalQuery.queryType == QueryTypeEnum.SET_BUILDER) {
            newQuery = new QuerySet(originalQuery.properties)
            // Can not share the same query collection
            newQuery.queries = null
            List<SuperQuery> queries = []
            originalQuery.queries.each {
                queries.add(it)
            }
            newQuery.queries = queries
        } else if (originalQuery.queryType == QueryTypeEnum.CUSTOM_SQL) {
            newQuery = new CustomSQLQuery(originalQuery.properties)
            newQuery.customSQLValues = []
            originalQuery.customSQLValues.each {
                newQuery.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: "", isFromCopyPaste: it.isFromCopyPaste))
            }
        }

        newQuery.name = generateUniqueName(originalQuery, owner)
        newQuery.userQueries = null
        newQuery.userGroupQueries = null
        newQuery.queryUserStates = null
        newQuery.qualityChecked = false
        newQuery.createdBy = owner.username
        newQuery.owner = owner
        newQuery.originalQueryId = 0L
        return newQuery
    }

    public SuperQuery createExecutedQuery(SuperQuery query) throws Exception {
        query = GrailsHibernateUtil.unwrapIfProxy(query)
        SuperQuery executedQuery = null
        Map oldAndNewQueries = [:]
        if (query) {
            if (query instanceof Query) {
                executedQuery = new ExecutedQuery(query.properties)
                executedQuery.originalQueryId = query.id
                executedQuery.queryExpressionValues = []
                query.queryExpressionValues.each {
                    executedQuery.addToQueryExpressionValues(new ExecutedQueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: "", specialKeyValue: it.specialKeyValue))
                }
            } else if (query instanceof CustomSQLQuery) {
                executedQuery = new ExecutedCustomSQLQuery(query.properties)
                executedQuery.originalQueryId = query.id
                executedQuery.customSQLValues = []
                query.customSQLValues.each {
                    executedQuery.addToCustomSQLValues(new ExecutedCustomSQLValue(key: it.key, value: ""))
                }
            } else if (query instanceof QuerySet) {
                QuerySet querySet = (QuerySet) query
                executedQuery = new ExecutedQuerySet(name: query.name,
                        description: query.description,
                        createdBy: query.createdBy,
                        dateCreated: query.dateCreated,
                        lastUpdated: query.lastUpdated,
                        isDeleted: query.isDeleted,
                        hasBlanks: query.hasBlanks,
                        tags: query.tags,
                        queryType: query.queryType,
                        originalQueryId: query.id,
                        owner: query.owner,
                        modifiedBy: query.modifiedBy
                )
                executedQuery.queries = []
                querySet.queries.each {
                    if (query instanceof ExecutedQuerySet){
                        SuperQuery executedNestedQuery = createExecutedQuery(GrailsHibernateUtil.unwrapIfProxy(it))
                        executedNestedQuery.originalQueryId = it.id
                        CRUDService.saveWithoutAuditLog(executedNestedQuery)
                        executedQuery.addToQueries(executedNestedQuery)
                    } else {
                        Long exId = SuperQuery.getLatestExQueryByOrigQueryId(Long.valueOf(it.id)).get()
                        SuperQuery current = SuperQuery.read(exId)
                        current = GrailsHibernateUtil.unwrapIfProxy(current)
                        executedQuery.addToQueries(current)
                        oldAndNewQueries.(it.id) = current.id
                    }
                    executedQuery.JSONQuery = assignQueriesFromMap(query.JSONQuery, oldAndNewQueries)
                }

                /*query.queries.each {
                    it = MiscUtil.unwrapProxy(it) */
                    /*SuperQuery current = null
                    if (it.queryType == QueryTypeEnum.QUERY_BUILDER) {
                        current = new ExecutedQuery(it.properties)
                        current.originalQueryId = it.id
                        current.queryExpressionValues = []
                        it.queryExpressionValues.each {
                            current.addToQueryExpressionValues(new ExecutedQueryExpressionValue(key: it.key,
                                    reportField: it.reportField, operator: it.operator, value: ""))
                        }
                    } else if (it.queryType == QueryTypeEnum.CUSTOM_SQL) {
                        current = new ExecutedCustomSQLQuery(it.properties)
                        current.originalQueryId = it.id
                        current.customSQLValues = []
                        it.customSQLValues.each {
                            current.addToCustomSQLValues(new ExecutedCustomSQLValue(key: it.key, value: ""))
                        }

                    } else {
                        log.info("Unexpected type in queries set from QuerySet: $it.queryType")
                    }
                    //Here setSharedWithQuery() is used to set the sharedWith for QUERY_BUILDER and CUSTOM_SQL used in the QUERY_SET.
                    setSharedWithQuery(current, it)
                    CRUDService.saveWithoutAuditLog(current)*/
            }

            //Here setSharedWithQuery() is used to set the sharedWith for QUERY_BUILDER, CUSTOM_SQL and QUERY_SET.
            setSharedWithQuery(executedQuery, query)
            CRUDService.saveWithoutAuditLog(executedQuery)
        }
        return executedQuery
    }

    void setSharedWithQuery(SuperQuery executedQuery, SuperQuery query){
        executedQuery.userQueries = null
        executedQuery.userGroupQueries = null
        executedQuery.queryUserStates = null
        if(query.userQueries){
            query.userQueries.each { userquery ->
                executedQuery.addToUserQueries(new UserQuery(user: userquery.user))
            }
        }
        if(query.userGroupQueries){
            query.userGroupQueries.each { userGroupQuery ->
                executedQuery.addToUserGroupQueries(new UserGroupQuery(userGroup: userGroupQuery.userGroup))
            }
        }
        if(query.queryUserStates){
            query.queryUserStates.each { queryUserStates ->
                executedQuery.addToQueryUserStates(new QueryUserState(user: queryUserStates.user, isFavorite: queryUserStates.isFavorite))
            }
        }
    }

    String generateUniqueName(SuperQuery query, User owner) {
        String prefix=ViewHelper.getMessage("app.configuration.copy.of") + " "
        String newName=trimName(prefix,query.name,"");
        if (SuperQuery.countByNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
            int count = 1
            newName=trimName(prefix,query.name,"($count)")
            while (SuperQuery.countByNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
                newName=trimName(prefix,query.name,"(${count++})")
            }
        }

        return newName
    }

    String trimName(String prefix, String name, String postfix) {
        int maxSize = SuperQuery.constrainedProperties.name.maxSize
        int overflow = prefix.length() + name.length() + postfix.length() - maxSize
        if (overflow > 0) {
            return prefix + name.substring(0,name.length() - overflow) + postfix
        }
        return prefix + name + postfix
    }

    /**
     * Expects dateString in format "dd-MMM-yyyy"
     * @param expressionDate saved date as a string from expression
     * @param days how many days we add to the date
     * @return date as a String in format "dd-MMM-yyyy"
     */
    String convertExpressionDateStringToDate(String expressionDate, int days) {
        SimpleDateFormat parseFrom = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        SimpleDateFormat formatTo = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        Calendar cal = Calendar.getInstance()
        cal.setTime(parseFrom.parse(expressionDate))
        cal.add(Calendar.DATE, days) // add one day to the date // assumed format for date dd-MMM-yyyy
        return formatTo.format(cal.getTime())
    }

    // This is used to replace query ids with executedQuery ids
    String assignQueriesFromMap(String JSONQuery, Map oldAndNewQueries) {
        Map dataMap = MiscUtil.parseJsonText(JSONQuery)
        List containerGroupsList = dataMap.all.containerGroups
        for (int i = 0; i < containerGroupsList.size(); i++) {
            assignInsideGroup(containerGroupsList[i], oldAndNewQueries)
        }
        return new JsonBuilder(dataMap).toString()
    }

    //helper method, don't call this
    private void assignInsideGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Map oldAndNewQueries) {
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                assignInsideGroup(expressionsList[i], oldAndNewQueries)
            }
        } else {
            if (groupMap.containsKey('query')) {
                if (oldAndNewQueries.containsKey(groupMap.query)) {
                    groupMap.query = oldAndNewQueries[groupMap.query]
                }
            }
        }
    }

    List getQueryList(String search, Integer offset, Integer max, Boolean isQueryTargetReports) {
        SuperQuery.ownedByUserWithSearch(userService.getUser(), search, isQueryTargetReports).list([max: max, offset: offset]).collect {
            [id       : it[0], text: it[1] + " " + (it[2] ? "(" + it[2] + ")" : "") + " - Owner: " + it[6],
             hasBlanks: it[4], qced: it[3], isFavorite: it[5]]
        }
    }

    int getQueryListCount(String search, Boolean isQueryTargetReports) {
        return SuperQuery.countOwnedByUserWithSearch(userService.getUser(), search, isQueryTargetReports).get()
    }

    Map getNonSetQueriesData(String search, Long oldSelectedId, int offset, int max) {
        List<SuperQuery> list = []
        //        TODO For Query optimization need to find solution http://stackoverflow.com/questions/37704333/grails-table-per-subclass-inheritance-and-discriminato
        List<Long> customQueryIdsForUsers = CustomSQLQuery.getCustomQueriesByUser(userService.getUser(), oldSelectedId, search).list([order: 'asc', sort: 'name', fetch: [owner: 'join']]).collect {
            it.first()
        }
        customQueryIdsForUsers.collate(999).each {
            list += CustomSQLQuery.findAllByIdInList(it)
        }
        List<Long> queryIdsForUsers = Query.getQueriesByUser(userService.getUser(), oldSelectedId, search).list([order: 'asc', sort: 'name', fetch: [owner: 'join']]).collect {
            it.first()
        }
        queryIdsForUsers.collate(999).each {
            list += Query.findAllByIdInList(it)
        }
        return [list               : MiscUtil.getPagedResult(list.sort {
            it.nameWithDescription
        }, offset, max), totalCount: list.size()]
    }

    @ReadOnly('pva')
    Map getAgenciesNames(String search, int offset, int max) {
        List result = AgencyName.'pva'.createCriteria().list([max: max, offset: offset, order: 'asc', sort: 'name']) {
            if (search) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            isNotNull('name')
        }
        return [list: result, totalCount: result.totalCount]
    }


    Map getIcsrCriteriaNames(String search, int offset, int max) {
        return [list: [], totalCount: 0]
    }
    // Used in query/view.gsp
    @NotTransactional
    JSON getQueryAsJSON(SuperQuery query) {
        HashMap queryMap = new HashMap(query.properties)

        if (query.queryType == QueryTypeEnum.QUERY_BUILDER) {
            queryMap.remove("queryExpressionValues")
            queryMap.queryExpressionValues = query.queryExpressionValues.collect { qev ->
                return [id: qev.id, reportField: [name: qev.reportField.name], operator: qev.operator, key: qev.key, value: qev.value, specialKeyValue: qev.specialKeyValue]
            }
        }

        queryMap.remove("queryService")
        queryMap.remove("userService")
        queryMap.remove("sqlService")

        return queryMap as JSON
    }

    @Transactional(readOnly = true)
    List getUsages(SuperQuery query) {
        def entities = []
        entities << TemplateQuery.queryUsedByConfigurations(query).list()
        entities << CaseSeries.findAllByIsDeletedAndGlobalQuery(false, query)
        entities << ReportConfiguration.findAllByIsDeletedAndGlobalQuery(false, query)
        entities << QueryCompliance.queryUsedByInboundConfigurations(query).list()
        if(query.nonValidCases){
            entities << ReportConfiguration.findAllByExcludeNonValidCases(true)
        }
        if(query.deletedCases){
            entities << ReportConfiguration.findAllByExcludeDeletedCases(true)
        }
        return entities.flatten().unique { a, b -> a.id <=> b.id }
    }

    @Transactional(readOnly = true)
    int getUsagesCount(SuperQuery query) {
        int count = 0
        count += TemplateQuery.queryUsedByConfigurationsCount(query).get()
        count += CaseSeries.countByIsDeletedAndGlobalQuery(false, query)
        count += ReportConfiguration.countByIsDeletedAndGlobalQuery(false, query)
        count += QueryCompliance.queryUsedByInboundConfigurationsCount(query).get()
        if(query.nonValidCases){
            count += ReportConfiguration.countByExcludeNonValidCases(true)
        }
        if(query.deletedCases){
            count += ReportConfiguration.countByExcludeDeletedCases(true)
        }
        return count
    }

    @Transactional(readOnly = true)
    List<QuerySet> getUsagesQuerySet(SuperQuery query) {
        return QuerySet.usuageByQuery(query).listDistinct()
    }

    @Transactional(readOnly = true)
    int getUsagesCountQuerySet(SuperQuery query) {
        return QuerySet.countUsuageByQuery(query).get()
    }

    @Transactional(readOnly = true)
    boolean isQueryUpdateable(SuperQuery superQuery) {
        return !(getUsagesCount(superQuery) > 0 || QuerySet.usuageByQuery(superQuery).list().unique {
            it.id
        }.find {
            if (getUsagesCount(it)) {
                return true
            }
        })
    }

    public String buildSetSQLFromJSON(String jsonQuery, List<Tuple2> values = []) {
        Map dataMap = MiscUtil.parseJsonText(jsonQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String result = ""
        Locale locale = Locale.default
        result = "(${buildSetCriteriaFromGroup(containerGroupsList, locale, values)})"
        return result
    }

    // helper method, don't call this
    private String buildSetCriteriaFromGroup(
            List groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Locale locale, List<Tuple2> values) {
        def result = ""
        return convertQueryToWhereClause(groupMap.get(0), result, values)
    }

    private String convertQueryToWhereClause(Map groupMap, String result, List<Tuple2> values) {
        List expressionsList = groupMap.expressions
        boolean hasKeyword = groupMap.keyword
        Locale locale = Locale.default

        for (int i = 0; i < expressionsList.size(); i++) {
            if (hasKeyword && i > 0) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.setOperator.${groupMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.setOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.setOperator.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }

            if (expressionsList.get(i).getAt("index") != null) {
                def queryIndex = expressionsList.get(i).getAt("query");
                String setCriteriaFromSubGroup = SuperQuery.get(queryIndex).getJSONQuery()
                if(setCriteriaFromSubGroup) {
                    result = constructSubReport(setCriteriaFromSubGroup, locale, result, values)
                }
                else{
                    result += "(" + SuperQuery.get(queryIndex).getName() + ")"
                }
            } else {
                result = convertQueryToWhereClause(expressionsList.get(i), result, values)
            }
        }
        return result;
    }

    private String constructSubReport(String setCriteriaFromSubGroup, Locale locale, String result, List<Tuple2> values) {
        Map dataMap = MiscUtil.parseJsonText(setCriteriaFromSubGroup)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        def expressionList = containerGroupsList.get(0)


        List innerExpression = expressionList.getAt("expressions")
        String keyword = expressionList.getAt("keyword")
        result += getExpressionWithValue(innerExpression, keyword, locale, values)
        return result
    }

    String getExpressionWithValue(List exp, String keyword, Locale locale, List<Tuple2> values) {
        String message = ""
        message += "("
        exp.eachWithIndex { it, index ->
            if (it.containsKey("expressions")) {
                message += getExpressionWithValue(it.getAt("expressions"), it.getAt("keyword") ?: null, locale, values)
            } else {
                String field = it.field
                String expressionValue = null
                Iterator<Tuple2> iterator = values.iterator();
                while (iterator.hasNext()) {
                    Tuple2 fieldValueTuple = iterator.next();
                    if (fieldValueTuple.first == field) {
                        expressionValue = fieldValueTuple.second
                        iterator.remove();
                        break;
                    }
                }
                message += "(${customMessageService.getMessage("app.reportField.${it.field}", null, locale)}  ${it.op}  ${expressionValue ?: it.value})"
            }
            if (index != (exp.size() - 1))
                message += " ${keyword ? keyword.toUpperCase() : ''} "
        }
        message += ")"
        return message
    }

    String generateReadableQueryFromExTemplateQuery(ExecutedTemplateQuery executedTemplateQuery, int reassessIndex) {
        if (executedTemplateQuery) {
            return generateReadableQuery(executedTemplateQuery.executedQueryValueLists, executedTemplateQuery.executedQuery.JSONQuery, executedTemplateQuery.executedConfiguration, reassessIndex)
        }
    }

    // reassessIndex is not used when generating readable queries
    String generateReadableQuery(List<ExecutedQueryValueList> queryValueLists, String JSONQuery, def executedConfiguration, int reassessIndex) {
        String result = ""

        Set<QueryExpressionValue> blanks = []
        queryValueLists?.each {
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    blanks.add(it)
                }
            }
        }

        Map dataMap = MiscUtil.parseJsonText(JSONQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        Locale locale = executedConfiguration?.hasProperty('locale') ? executedConfiguration.locale : new Locale('en')
        for (int i = 0; i < containerGroupsList.size(); i++) {
            if ((allMap.keyword) && (i > 0)) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.keyword.${allMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.keyword.${allMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }
            Date nextRunDate = executedConfiguration.hasProperty('nextRunDate') ? executedConfiguration.nextRunDate:null
            def executed = buildCriteriaFromGroup(true, containerGroupsList[i],
                    nextRunDate,
                    "UTC", blanks, locale, reassessIndex)
            result += "(${executed})"
        }
        return result
    }

    // reassessIndex is not used when generating readable queries
    String generateReadableQueryForAuditLog(List<QueryExpressionValue> queryExpressionValues, String jsonQuery, int reassessIndex) {
        if(jsonQuery == null) {
            return null
        }
        String result = "" //jsonQuery

        result += "\n\n Criteria: \n"

        Set<QueryExpressionValue> blanks = []
        queryExpressionValues?.each {
            // reportField type is not checked here because Query is used here instead of SuperQuery
            blanks.add(it)
        }

        Map dataMap = MiscUtil.parseJsonTextForQuery(jsonQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        Locale locale = Locale.default
        for (int i = 0; i < containerGroupsList.size(); i++) {
            if ((allMap.keyword) && (i > 0)) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.keyword.${allMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.keyword.${allMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }
            def executed = buildCriteriaFromGroupForAuditLog(false, containerGroupsList[i], null, "UTC", blanks, locale, reassessIndex)
            result += "${executed}"
        }

        return result
    }

    // helper method, don't call this
    private def buildCriteriaFromGroup(boolean isExecuted,
                                       Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
                                       Date nextRunDate, String timezone, Set<QueryExpressionValue> blanks, Locale locale, int reassessIndex) {
        String result = ""
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    String keyword
                    try {
                        keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, locale)
                    } catch (NoSuchMessageException exception) {
                        log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                        keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                    }
                    result += " $keyword "
                }
                def executed = buildCriteriaFromGroup(isExecuted, expressionsList[i], nextRunDate, timezone, blanks, locale, reassessIndex)
                result += "(${executed})";
            }
        } else {
            if (groupMap.keyword) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }

            ReportField reportField = ReportField.findByNameAndIsDeleted(groupMap.field,false)

            // Extra Values
            HashMap extraValues = [:]
            if (reportField) {
                result += convertExpressionToWhereClause(
                        new Expression(reportField: reportField, value: groupMap.value, operator: groupMap.op as QueryOperatorEnum),
                        nextRunDate, timezone, blanks, extraValues, locale, reassessIndex, isExecuted)
            }
        }
        return result
    }

    private def buildCriteriaFromGroupForAuditLog(boolean isExecuted,
                                       Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
                                       Date nextRunDate, String timezone, Set<QueryExpressionValue> blanks, Locale locale, int reassessIndex) {
        String result = ""
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    String keyword
                    try {
                        keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, locale)
                    } catch (NoSuchMessageException exception) {
                        log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                        keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                    }
                    result += "\n$keyword\n"
                }
                def executed = buildCriteriaFromGroupForAuditLog(isExecuted, expressionsList[i], nextRunDate, timezone, blanks, locale, reassessIndex)
                result += "(${executed}";
                if(expressionsList[i].RLDS)
                    result += " (Datasheet: " + expressionsList[i].RLDS
                if(expressionsList[i].RLDS_OPDS)
                    result += ", Reassess on Primary Datasheet: " + expressionsList[i].RLDS_OPDS + ")"
                result += ")"
            }
        } else {
            if (groupMap.keyword) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }

            ReportField reportField = ReportField.findByNameAndIsDeleted(groupMap.field,false)

            // Extra Values
            HashMap extraValues = [:]
            if (reportField) {
                result += convertExpressionToWhereClause(
                        new Expression(reportField: reportField, value: groupMap.value, operator: groupMap.op as QueryOperatorEnum),
                        nextRunDate, timezone, blanks, extraValues, locale, reassessIndex, isExecuted)
            }
        }
        return result
    }

    private String convertExpressionToWhereClause(Expression e, Date nextRunDate, String timezone, Set<QueryExpressionValue> blanks,
                                                  HashMap extraValues, Locale locale, int reassessIndex, boolean isExecuted) {
        String result = ""
        String columnName = ""
        String op = ""

        // PVR-1355 Akash
        // Re-assess Listedness needs to be completed in this method.
        if (!extraValues.isEmpty()) {
            String RLDSValue = extraValues.get(RLDS)
            if (RLDSValue) {
                /*
                    Add code here to process Re-assess Listedness Datasheet.

                    e.value is the value of the listedness (i.e. Listed, Unlisted, Unknown).

                    RLDSValue is the value of the datasheet (i.e. JPN, CCDS, C1).

                    RLDSValue cannot be blank.

                    Store the string in result, which is returned.
                 */

                // TODO: convert to readable query
                if (e.operator == QueryOperatorEnum.IS_EMPTY) {
                    result = "${columnName} IS NULL"
                } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
                    result = "${columnName} IS NOT NULL"
                } else if (e.reportField.isString() || e.reportField.isEmbase()) {
                    //First, check our custom operator. String comparisons are case insensitive.
                    String listednessList = ""
                    List tokens1 = e.normalizeValue.split(/;/) as List
                    tokens1.eachWithIndex { it, index ->
                        listednessList += "UPPER(\'${it}\'),"
                    }

                    String datasheetList = ""
                    List tokens2 = RLDSValue.split(/;/) as List
                    tokens2.eachWithIndex { it, index ->
                        datasheetList += "UPPER(\'${it}\'),"
                    }

                    if (e.operator == QueryOperatorEnum.EQUALS) {
                        result = "UPPER(${columnName}) in (${listednessList.substring(0, listednessList.length() - 1)})"
                        // check listedness
                        result += " and UPPER(gdrq.datasheet_name) in (${datasheetList.substring(0, datasheetList.length() - 1)})"
                        // check datasheet
                        result += " and gdrq.row_num = ${reassessIndex}" // check sequence number

                    } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                        result = "UPPER(${columnName}) not in (${listednessList.substring(0, listednessList.length() - 1)})"
                        // check listedness
                        result += " and UPPER(gdrq.datasheet_name) not in (${datasheetList.substring(0, datasheetList.length() - 1)})"
                        // check datasheet
                        result += " and gdrq.row_num = ${reassessIndex}" // check sequence number

                    } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                        result = "UPPER(${columnName}) LIKE UPPER('%${e.normalizeValue}%')"
                    } else if (e.operator == QueryOperatorEnum.ADVANCE_CONTAINS) {
                        result = " REGEXP_LIKE(UPPER(${columnName}), '${e.normalizeValue}')"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.normalizeValue}%')"
                    } else if (e.operator == QueryOperatorEnum.START_WITH) {
                        result = "UPPER(${columnName}) LIKE UPPER('${e.normalizeValue}%')"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('${e.normalizeValue}%')"
                    } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                        result = "UPPER(${columnName}) LIKE UPPER('%${e.normalizeValue}')"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.normalizeValue}')"
                    }
                }
                return result
            }
        }
        if (e.value == null || e.value.equals("") || e.value.matches(Constants.POI_INPUT_PATTERN_REGEX)) {
            ParameterValue qev = blanks.find { qev ->
                e.reportField == qev.reportField && e.operator == qev.operator
            }

            if (qev) {
                e.value = qev.value
                blanks.remove(qev)
            } else {
                log.info("Could not find blank value to give value!")
            }
        }

        try {
            columnName = messageSource.getMessage("app.reportField.$e.reportField.name", null, locale)
        } catch (NoSuchMessageException exception) {
            log.warn("No app.reportField found for locale: $locale. Defaulting to English locale.")
            columnName = messageSource.getMessage("app.reportField.$e.reportField.name", null, Locale.ENGLISH)
        }

        try {
            op = messageSource.getMessage(e.operator.getI18nKey(), null, locale)
        } catch (NoSuchMessageException exception) {
            log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
            op = messageSource.getMessage(e.operator.getI18nKey(), null, Locale.ENGLISH)
        }

        if (e.value?.startsWith(Constants.RPT_INPUT_PREFIX)) { //TODO need to align all checks properly
            def value = e.value.replace(Constants.RPT_INPUT_PREFIX, '')
            try {
                e.value = messageSource.getMessage("app.reportField.$value", null, locale)
            } catch (NoSuchMessageException exception) {
                log.warn("No app.reportField found for locale: $locale. Defaulting to English locale.")
                e.value = messageSource.getMessage("app.reportField.$value", null, Locale.ENGLISH)
            }
        }

        if (e.reportField.dataType == PartialDate.class) {
            result = generatePartialDateWhereClause(e, nextRunDate, timezone, columnName, isExecuted, locale)
            return result
        }

        if (e.operator == QueryOperatorEnum.IS_EMPTY) {
            result = "${columnName} Is null"
        } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
            result = "${columnName} Is not null"
        } else if (e.reportField.isString() || e.reportField.isEmbase()) {
            //First, check our custom operator. String comparisons are case insensitive.
            if (e.operator == QueryOperatorEnum.EQUALS) {
                //Second, check if we have multiselect
                if (e?.value?.indexOf(";") == -1) {
                    result = "${columnName} = ${e.normalizeValue}"
                } else {
                    //Multiselect Select2
                    List tokens = e?.normalizeValue?.split(/;/) ? e.normalizeValue.split(/;/) as List : ['']
                    StringBuilder values = new StringBuilder()

                    tokens.eachWithIndex { it, index ->
                        if (index > 0) {
                            values.append(" OR ${columnName} = ${it}")
                        } else {
                            values.append("${columnName} = ${it}")
                        }
                    }

                    result += values
                }
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                //Second, check if we have multiselect
                if (e?.value?.indexOf(";") == -1) {
                    result = "${columnName} <> ${e.normalizeValue}"
                } else {
                    //Multiselect Select2
                    List tokens = e?.normalizeValue?.split(/;/) ? e.normalizeValue.split(/;/) as List : ['']
                    StringBuilder values = new StringBuilder()

                    tokens.eachWithIndex { it, index ->
                        if (index > 0) {
                            values.append(" AND ${columnName} <> ${it}")
                        } else {
                            values.append("${columnName} <> ${it}")
                        }
                    }

                    result += values
                }
            } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                result = "${columnName} $op ${e.normalizeValue}"
            } else if (e.operator == QueryOperatorEnum.ADVANCE_CONTAINS) {
                result = "${columnName} $op ${e.normalizeValue}"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                result = "${columnName} $op ${e.normalizeValue}"
            } else if (e.operator == QueryOperatorEnum.START_WITH) {
                result = "${columnName} $op ${e.normalizeValue}"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                result = "${columnName} $op ${e.normalizeValue}"
            } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                result = "${columnName} $op ${e.normalizeValue}"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                result = "${columnName} $op ${e.normalizeValue}"
            }
        } else if (e.reportField.isNumber()) {
            result = "${columnName} ${e.operator.value()} ${e.value}"
        } else if (e.reportField.isDate()) {
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName, isExecuted, locale)
        }
        return result
    }

    String generatePartialDateWhereClause(Expression e, Date nextRunDate, String timezone, String columnName, boolean isExecuted, Locale locale) {
        if (e.value.matches(sqlGenerationService.PARTIAL_DATE_YEAR_ONLY)) { //??-???-yyyy
            String monthAndYear = e.value.substring(6)
            String startDate = "01-JAN${monthAndYear}"
            return generatePartialDateWhereClause(isExecuted, e.operator, e.value, false, startDate, nextRunDate, timezone, columnName, locale)
        } else if (e.value.matches(sqlGenerationService.PARTIAL_DATE_MONTH_AND_YEAR)) { //??-MMM-yyyy
            String monthAndYear = e.value.substring(2)
            String startDate = "01${monthAndYear}"
            return generatePartialDateWhereClause(isExecuted, e.operator, e.value, true, startDate, nextRunDate, timezone, columnName, locale)

        }
        return generateDateWhereClause(e, nextRunDate, timezone, columnName, isExecuted, locale)
    }

    // Helper method
    String generatePartialDateWhereClause(boolean isExecuted, QueryOperatorEnum operator, String value, boolean hasMonth,
                                          String startDate, Date nextRunDate, String timezone, String columnName, Locale locale) {
        String result = ""
        def startDates
        def endDates

        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        Date convertedStartDate = dateFormat.parse(startDate)
        Calendar c = Calendar.getInstance();
        c.setTime(convertedStartDate)
        if (!hasMonth) {
            c.set(Calendar.MONTH, Calendar.DECEMBER)
        }
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        Date convertedEndDate = c.time

        if (operator == QueryOperatorEnum.EQUALS) {
            startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
//            endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
            result = """${columnName} = ${convertDateToReadableDateString(startDates[0])}"""
//                      AND${columnName} <= ${convertDateToReadableDateString(endDates[1])}"""
        } else if (operator == QueryOperatorEnum.NOT_EQUAL) {
            startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
//            endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
            result = """NOT (${columnName} = ${convertDateToReadableDateString(startDates[0])})"""
//                          AND       ${columnName} <= ${convertDateToReadableDateString(endDates[1])})"""
        } else if (operator == QueryOperatorEnum.LESS_THAN) {
            startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
            result = "${columnName} < ${convertDateToReadableDateString(startDates[0])}"
        } else if (operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
            endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
            result = "${columnName} <= ${convertDateToReadableDateString(endDates[1])}"
        } else if (operator == QueryOperatorEnum.GREATER_THAN) {
            endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
            result = "${columnName} > ${convertDateToReadableDateString(endDates[1])}"
        } else if (operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
            startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
            result = "${columnName} >= ${convertDateToReadableDateString(startDates[0])}"
        } else if (operator == QueryOperatorEnum.YESTERDAY || operator == QueryOperatorEnum.LAST_MONTH ||
                operator == QueryOperatorEnum.LAST_WEEK || operator == QueryOperatorEnum.LAST_YEAR) {
            if (!isExecuted) {
                result = "${columnName} = " + messageSource.getMessage(operator.i18nKey, null, locale)
            } else {
                startDates = RelativeDateConverter.(operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(startDates[0])} AND
                            ${columnName} <= ${convertDateToReadableDateString(startDates[1])}"""
            }
        } else if (operator == QueryOperatorEnum.LAST_X_DAYS || operator == QueryOperatorEnum.LAST_X_MONTHS ||
                operator == QueryOperatorEnum.LAST_X_WEEKS || operator == QueryOperatorEnum.LAST_X_YEARS) {
            if (!isExecuted) {
                String op = messageSource.getMessage(operator.i18nKey, null, locale)
                result = "${columnName} = " + op.replace('X', value)
            } else {
                startDates = RelativeDateConverter.(operator.value())(nextRunDate, Integer.parseInt(value), timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(startDates[0])} AND
                            ${columnName} ${convertDateToReadableDateString(startDates[1])}"""
            }
        }  else if (operator == QueryOperatorEnum.TOMORROW || operator == QueryOperatorEnum.NEXT_MONTH ||
                operator == QueryOperatorEnum.NEXT_WEEK || operator == QueryOperatorEnum.NEXT_YEAR) {
            if (!isExecuted) {
                result = "${columnName} = " + messageSource.getMessage(operator.i18nKey, null, locale)
            } else {
                startDates = RelativeDateConverter.(operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(startDates[0])} AND
                            ${columnName} <= ${convertDateToReadableDateString(startDates[1])}"""
            }
        } else if (operator == QueryOperatorEnum.NEXT_X_DAYS || operator == QueryOperatorEnum.NEXT_X_MONTHS ||
                operator == QueryOperatorEnum.NEXT_X_WEEKS || operator == QueryOperatorEnum.NEXT_X_YEARS) {
            if (!isExecuted) {
                String op = messageSource.getMessage(operator.i18nKey, null, locale)
                result = "${columnName} = " + op.replace('X', value)
            } else {
                startDates = RelativeDateConverter.(operator.value())(nextRunDate, Integer.parseInt(value), timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(startDates[0])} AND
                            ${columnName} <= ${convertDateToReadableDateString(startDates[1])}"""
            }
        }
        return result
    }

    String generateDateWhereClause(Expression e, Date nextRunDate, String timezone, String columnName, boolean isExecuted, Locale locale) {
        String result = ""
        def dates
        def startDate = "null"
        def endDate = "null"
        boolean isDate = e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)
        if (isDate && !(e.operator in [QueryOperatorEnum.LAST_X_DAYS, QueryOperatorEnum.LAST_X_MONTHS,
                                        QueryOperatorEnum.LAST_X_WEEKS, QueryOperatorEnum.LAST_X_YEARS,
                                        QueryOperatorEnum.YESTERDAY, QueryOperatorEnum.LAST_MONTH,
                                        QueryOperatorEnum.LAST_WEEK, QueryOperatorEnum.LAST_YEAR,
                                        QueryOperatorEnum.NEXT_X_DAYS, QueryOperatorEnum.NEXT_X_MONTHS,
                                        QueryOperatorEnum.NEXT_X_WEEKS, QueryOperatorEnum.NEXT_X_YEARS,
                                        QueryOperatorEnum.TOMORROW, QueryOperatorEnum.NEXT_MONTH,
                                        QueryOperatorEnum.NEXT_WEEK, QueryOperatorEnum.NEXT_YEAR])) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            startDate = convertDateToReadableDateString(dates[0])
            endDate = convertDateToReadableDateString(dates[1])
        } else if(e.value) {
            startDate = e.value
            endDate = e.value
        }

        if (e.operator == QueryOperatorEnum.EQUALS ) {
            result = """${columnName} = ${startDate}"""
//                      AND ${columnName} <= ${convertDateToReadableDateString(dates[1])}"""
        } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
            result = """NOT (${columnName} = ${startDate})"""
//                      AND ${columnName} <= ${convertDateToReadableDateString(dates[1])})"""
        } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
            result = "${columnName} < ${startDate}"
        } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
            result = "${columnName} <= ${endDate}"
        } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
            result = "${columnName} > ${startDate}"
        } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
            result = "${columnName} >= ${startDate}"
        } else if (e.operator in [QueryOperatorEnum.YESTERDAY, QueryOperatorEnum.LAST_MONTH,
                QueryOperatorEnum.LAST_WEEK, QueryOperatorEnum.LAST_YEAR]) {
            if (!isExecuted) {
                result = "${columnName} = " + messageSource.getMessage(e.operator.i18nKey, null, locale)
            } else {
                dates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(dates[0])} AND
                    ${columnName} <= ${convertDateToReadableDateString(dates[1])}"""
            }
        } else if (e.operator in [QueryOperatorEnum.LAST_X_DAYS, QueryOperatorEnum.LAST_X_MONTHS,
                QueryOperatorEnum.LAST_X_WEEKS, QueryOperatorEnum.LAST_X_YEARS]) {
            if (!isExecuted || !isDate) {
                String op = messageSource.getMessage(e.operator.i18nKey, null, locale)
                if (e.value) {
                    result = "${columnName} = " + op.replace('X', e.value)
                } else {
                    result = "${columnName} = " + op
                }

            } else {
                dates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(dates[0])} AND
                    ${columnName} <= ${convertDateToReadableDateString(dates[1])}"""
            }
        } else if (e.operator in [QueryOperatorEnum.TOMORROW, QueryOperatorEnum.NEXT_MONTH,
                                  QueryOperatorEnum.NEXT_WEEK, QueryOperatorEnum.NEXT_YEAR]) {
            if (!isExecuted) {
                result = "${columnName} = " + messageSource.getMessage(e.operator.i18nKey, null, locale)
            } else {
                dates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(dates[0])} AND
                    ${columnName} <= ${convertDateToReadableDateString(dates[1])}"""
            }
        }
        else if (e.operator in [QueryOperatorEnum.NEXT_X_DAYS, QueryOperatorEnum.NEXT_X_MONTHS,
                                QueryOperatorEnum.NEXT_X_WEEKS, QueryOperatorEnum.NEXT_X_YEARS]) {
            if (!isExecuted || !isDate) {
                String op = messageSource.getMessage(e.operator.i18nKey, null, locale)
                if (e.value) {
                    result = "${columnName} = " + op.replace('X', e.value)
                } else {
                    result = "${columnName} = " + op
                }

            } else {
                dates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= ${convertDateToReadableDateString(dates[0])} AND
                    ${columnName} <= ${convertDateToReadableDateString(dates[1])}"""
            }
        }
        return result
    }

    private String convertDateToReadableDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        return sdf.format(date)
    }

    @Transactional(readOnly = true)
    List<List<Map>> queryExpressionValuesForQuerySet(Long queryId) {
        List<List<Map>> result = []
        QuerySet.get(queryId)?.queries?.each { SuperQuery query ->
            if (query.hasBlanks) {
                List<Map> qvl = []
                if (query.queryType == QueryTypeEnum.QUERY_BUILDER) {
                    query.queryExpressionValues?.each {
                        qvl += [value  : it.value, operator: it.operator.name(), type: query.queryType.name(),
                                field  : it.reportField.name, key: it.key, id: it.id, queryName: query.name, specialKeyValue: it.specialKeyValue,
                                queryId: query.id]
                    }
                    if (query?.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE) {
                        qvl += [reassessListednessDate : DateUtil.dateRangeString(query.reassessListednessDate, Constants.DEFAULT_SELECTED_TIMEZONE), customReassessDate: true]
                    }
                }
                if (query.queryType == QueryTypeEnum.CUSTOM_SQL) {
                    query.customSQLValues?.each {
                        qvl += [value  : it.value, key: it.key, type: query.queryType.name(), queryName: query.name,
                                queryId: query.id]
                    }
                }
                result.add(qvl)
            }
        }
        return result
    }

    @Transactional(readOnly = true)
    List<Map> queryExpressionValuesForQuery(Long queryId) {
        Query query = Query.get(queryId)
        List<Map> result =  query?.queryExpressionValues?.collect {
            [value: it.value, operator: it.operator.name(),
             field: it.reportField.name, key: it.key, specialKeyValue: it.specialKeyValue]
        }
        if (query?.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE) {
            result.add([reassessListednessDate : DateUtil.dateRangeString(query.reassessListednessDate, Constants.DEFAULT_SELECTED_TIMEZONE), customReassessDate: true])
        }
        return result
    }

    @Transactional(readOnly = true)
    List<Map> customSQLValuesForQuery(Long queryId) {
        return CustomSQLQuery.get(queryId)?.customSQLValues?.collect {
            [value: it.value, key: it.key]
        }
    }

    void updateLastExecutionDate(SuperQuery query) {
        if (!query) {
            return
        }
        try {
            SuperQuery.executeUpdate("update SuperQuery set lastExecuted=:lastExecuted where id=:id ", [lastExecuted: new Date(), id: query.id])
            //As multiple updates happens at the same time.
        } catch (Exception ex) {
            log.error("Error while updating last executed of query: ${query?.id} ", ex)
        }

    }

    def setFavorite(SuperQuery query, Boolean state) {
        User user = userService.getUser()
        QueryUserState queryUserState = QueryUserState.findByUserAndQuery(user, query)
        if (!queryUserState) {
            queryUserState = new QueryUserState(user: user, query: query)
        }
        queryUserState.isFavorite = state ? true : null
        queryUserState.save()
    }

    //TODO: Need to add the check for FAERS and EVDAS queries once these flags are added in the Super Query domain.
    List<Map> getQueriesByUser(QueryParamDTO queryParamDTO) {
        List<Map> queryList = SuperQuery.createCriteria().list([max: queryParamDTO.max, offset: queryParamDTO.offset, fetch: [owner: 'join']]) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("description", "description")
                'owner'{
                    property("fullName", "owner")
                }
            }
            createAlias('userQueries', 'uq', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupQueries', 'ugq', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            eq("originalQueryId", 0L)
            if (queryParamDTO.isNonParameterisedQuery) {
                eq("hasBlanks", false)
            }
            if (queryParamDTO.isEvdasQuery) {
                eq('queryTarget', QueryTarget.EVDAS)
            } else if (queryParamDTO.isSafetyQuery){
                eq('queryTarget', QueryTarget.REPORTS)
            } else if (queryParamDTO.isFaersQuery) {
                eq('queryTarget', QueryTarget.FAERS)
            } else if (queryParamDTO.isEmbaseQuery) {
                eq('queryTarget', QueryTarget.EMBASE)
            } else {
                or {
                    eq('queryTarget', QueryTarget.REPORTS)
                    eq('queryTarget', QueryTarget.FAERS)
                }
            }
            if (!queryParamDTO.user.isAnyAdmin()) {
                or {
                    eq('owner.id', queryParamDTO.user.id)
                    eq('uq.user.id', queryParamDTO.user.id)
                    if (UserGroup.countAllUserGroupByUser(queryParamDTO.user)) {
                        'in'('ugq.userGroup', UserGroup.fetchAllUserGroupByUser(queryParamDTO.user))
                    }
                }
            }
            if (queryParamDTO.search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(queryParamDTO.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(queryParamDTO.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(queryParamDTO.search)}%")
                    }
                }
            }
            and {
                order('name', 'asc')
                'owner' {
                    order('fullName', 'asc')
                }
            }
        }
        return queryList
    }

    Integer countQueriesByUser(QueryParamDTO queryParamDTO) {
        Integer queryCount = SuperQuery.createCriteria().count {
            createAlias('userQueries', 'uq', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupQueries', 'ugq', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            eq("originalQueryId", 0L)
            if (queryParamDTO.isNonParameterisedQuery) {
                eq("hasBlanks", false)
            }
            if (queryParamDTO.isEvdasQuery) {
                eq('queryTarget', QueryTarget.EVDAS)
            } else if (queryParamDTO.isSafetyQuery){
                eq('queryTarget', QueryTarget.REPORTS)
            } else if (queryParamDTO.isFaersQuery) {
                eq('queryTarget', QueryTarget.FAERS)
            } else if (queryParamDTO.isEmbaseQuery) {
                eq('queryTarget', QueryTarget.EMBASE)
            } else {
                'in'('queryTarget',[QueryTarget.REPORTS, QueryTarget.FAERS])
            }
            if (!queryParamDTO.user.isAnyAdmin()) {
                or {
                    eq('owner.id', queryParamDTO.user.id)
                    eq('uq.user.id', queryParamDTO.user.id)
                    if (UserGroup.countAllUserGroupByUser(queryParamDTO.user)) {
                        'in'('ugq.userGroup', UserGroup.fetchAllUserGroupByUser(queryParamDTO.user))
                    }
                }
            }
            if (queryParamDTO.search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(queryParamDTO.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(queryParamDTO.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(queryParamDTO.search)}%")
                    }
                }
            }
        }
        return queryCount
    }

    List<Map> generateQueryListByIDs(List<String> idList) {
        List<Long> queryIdList = idList.collect {
            it as Long
        }
        List<Map> queryList = SuperQuery.createCriteria().list([sort: "name"]) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("description", "description")
            }
            'in'('id', queryIdList)
        }
        queryList
    }

    List<Map> generateNotDeletedQueryList() {
        List<Map> queryList = SuperQuery.createCriteria().list([sort: "name"]) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("description", "description")
            }
            eq('isDeleted', false)
        }
        queryList
    }

    SuperQuery findQueryByName(String queryName) {
        return SuperQuery.findByIsDeletedAndOriginalQueryIdAndNameIlike(false, 0, queryName)
    }

    //below method is used to find the queries list based on tag name
    List<Map> fetchQueriesByTag(String tagName) {
        List<Map> queryList = SuperQuery.createCriteria().list() {
            createAlias('tags', 'tg', CriteriaSpecification.LEFT_JOIN)
            eq('tg.name', tagName)
            eq("originalQueryId", 0L)
            projections {
                distinct('id')
                property('name')
                property('createdBy')
                property('dateCreated')
                property('modifiedBy')
                property('lastUpdated')
                property('isDeleted')
            }
        }?.collect { [id: it[0], name: it[1], createdBy: it[2], dateCreated: it[3], modifiedBy: it[4], lastUpdated: it[5], isDeleted: it[6]] }
        return queryList
    }

}
