package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

@Secured(['ROLE_SYSTEM_CONFIGURATION'])
class AutoReasonOfDelayController {

    def userService;
    def configurationService;
    def CRUDService;
    def reportExecutorService;
    def autoReasonOfDelayService;

    def index() {
        List<AutoReasonOfDelay> autoReasonOfDelayList = AutoReasonOfDelay.findAll()
        if(autoReasonOfDelayList.size() > 0){
            redirect(action: "edit", id: autoReasonOfDelayList.get(0).getId())
        }else{
            redirect(action: "create")
        }
    }

    def create() {
        def lateList = (reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC) as JSON).toString()
        def rootCauseList = (reportExecutorService.getRootCauseList() as JSON).toString()
        def rootCauseClassList = (reportExecutorService.getRootCauseClassList() as JSON).toString()
        def rootCauseSubCategoryList = (reportExecutorService.getRootCauseSubCategoryList() as JSON).toString()
        def responsiblePartyList = (reportExecutorService.getResponsiblePartyList() as JSON).toString()
        AutoReasonOfDelay autoReasonOfDelayInstance = new AutoReasonOfDelay()
        def fromSession = autoReasonOfDelayService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.autoReasonOfDelayParams) {
            initConfigurationFromMap(autoReasonOfDelayInstance, fromSession.autoReasonOfDelayParams)
        }
        Map queryRCAIndex = fromSession.queryRCAIndex
        boolean queryBlanks = false
        if (params.selectedQuery) {
            SuperQuery query = SuperQuery.get(params.selectedQuery)
            if (query) {
                QueryRCA queryRCA = new QueryRCA(query: query)
                autoReasonOfDelayInstance.addToQueriesRCA(queryRCA)
                if (query.hasBlanks) {
                    queryBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.query.notFound', args: [params.selectedQuery])
            }
        }

        render(view: "create", model: [queryRCAIndex: queryRCAIndex, queryBlanks: queryBlanks,
                lateList: lateList, rootCauseList: rootCauseList, rootCauseClassList: rootCauseClassList,  responsiblePartyList: responsiblePartyList, rootCauseSubCategoryList: rootCauseSubCategoryList,
                autoReasonOfDelayInstance: autoReasonOfDelayInstance, sourceProfiles: SourceProfile.sourceProfilesForUser(getCurrentUser())])
    }

    def edit(Long id) {
        AutoReasonOfDelay autoReasonOfDelayInstance = id ? AutoReasonOfDelay.read(id) : null
        if (!autoReasonOfDelayInstance) {
            notFound()
            return
        }
        def fromSession = autoReasonOfDelayService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.autoReasonOfDelayParams && (id == Long.parseLong(fromSession.autoReasonOfDelayParams.id))) {
            initConfigurationFromMap(autoReasonOfDelayInstance, fromSession.autoReasonOfDelayParams)
        }
        Map queryRCAIndex = fromSession.queryRCAIndex
        User currentUser = getCurrentUser()
        def lateList = (reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC,true) as JSON).toString()
        def rootCauseList = (reportExecutorService.getRootCauseList(ReasonOfDelayAppEnum.PVC,true) as JSON).toString()
        def rootCauseClassList = (reportExecutorService.getRootCauseClassList(true) as JSON).toString()
        def rootCauseSubCategoryList = (reportExecutorService.getRootCauseSubCategoryList(true) as JSON).toString()
        def responsiblePartyList = (reportExecutorService.getResponsiblePartyList(true) as JSON).toString()

        autoReasonOfDelayInstance.scheduleDateJSON = configurationService.correctSchedulerJSONForCurrentDate(autoReasonOfDelayInstance.scheduleDateJSON, autoReasonOfDelayInstance.nextRunDate)
        if(autoReasonOfDelayInstance && autoReasonOfDelayInstance.executing) {
            flash.warn = message(code: "app.autoReasonOfDelay.warning.message")
        }
        render(view: "edit", model: [queryRCAIndex: queryRCAIndex,
                                     lateList: lateList, rootCauseList: rootCauseList, rootCauseClassList: rootCauseClassList,  responsiblePartyList: responsiblePartyList, rootCauseSubCategoryList: rootCauseSubCategoryList,
                autoReasonOfDelayInstance : autoReasonOfDelayInstance, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser)])
    }

    private void initConfigurationFromMap(AutoReasonOfDelay autoReasonOfDelayInstance, Map map) {
        params.putAll(map)
        autoReasonOfDelayInstance.setIsEnabled(false)
        autoReasonOfDelayInstance.nextRunDate = null
        populateModel(autoReasonOfDelayInstance)
        autoReasonOfDelayService.initConfigurationQueriesFromSession(session, autoReasonOfDelayInstance)
        session.removeAttribute("editingAutoReasonOfDelay")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @Transactional
    def save() {
        if (request.method == 'GET') {
            notSaved()
            return
        }

        AutoReasonOfDelay autoReasonOfDelayInstance = new AutoReasonOfDelay()
        autoReasonOfDelayInstance.nextRunDate = null
        populateModel(autoReasonOfDelayInstance)
        try {
            autoReasonOfDelayInstance = (AutoReasonOfDelay) CRUDService.save(autoReasonOfDelayInstance)
        } catch (ValidationException ve) {
            autoReasonOfDelayInstance.errors = ve.errors
            def lateList = (reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC) as JSON).toString()
            def rootCauseList = (reportExecutorService.getRootCauseList() as JSON).toString()
            def rootCauseClassList = (reportExecutorService.getRootCauseClassList() as JSON).toString()
            def rootCauseSubCategoryList = (reportExecutorService.getRootCauseSubCategoryList() as JSON).toString()
            def responsiblePartyList = (reportExecutorService.getResponsiblePartyList() as JSON).toString()
            render view: "create", model: [autoReasonOfDelayInstance : autoReasonOfDelayInstance,
                                           lateList: lateList, rootCauseList: rootCauseList, rootCauseClassList: rootCauseClassList,  responsiblePartyList: responsiblePartyList, rootCauseSubCategoryList: rootCauseSubCategoryList,
                                          configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in auto reason of delay -> save", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'index')
            return
        }

        flash.message = message(code: 'auto.rod.created.message')
        redirect(action: "edit", id: autoReasonOfDelayInstance.id)
    }

    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        AutoReasonOfDelay autoReasonOfDelayInstance = AutoReasonOfDelay.lock(params.id)
        if (!autoReasonOfDelayInstance) {
            notFound()
            return
        }

        if(autoReasonOfDelayInstance && autoReasonOfDelayInstance.executing) {
            flash.warn = message(code: "app.autoReasonOfDelay.warning.message")
        }else{
            autoReasonOfDelayInstance.nextRunDate = null
            populateModel(autoReasonOfDelayInstance)
            try {
                autoReasonOfDelayInstance = (AutoReasonOfDelay) CRUDService.update(autoReasonOfDelayInstance)
            } catch (ValidationException ve) {
                autoReasonOfDelayInstance.errors = ve.errors
                def lateList = (reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC) as JSON).toString()
                def rootCauseList = (reportExecutorService.getRootCauseList(ReasonOfDelayAppEnum.PVC,true) as JSON).toString()
                def rootCauseClassList = (reportExecutorService.getRootCauseClassList() as JSON).toString()
                def rootCauseSubCategoryList = (reportExecutorService.getRootCauseSubCategoryList() as JSON).toString()
                def responsiblePartyList = (reportExecutorService.getResponsiblePartyList() as JSON).toString()
                render view: "edit", model: [autoReasonOfDelayInstance : autoReasonOfDelayInstance,
                                             lateList: lateList, rootCauseList: rootCauseList, rootCauseClassList: rootCauseClassList,  responsiblePartyList: responsiblePartyList, rootCauseSubCategoryList: rootCauseSubCategoryList,
                                             sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
                return
            } catch (Exception ex) {
                log.error("Unexpected error in auto Reason of Delay -> update", ex)
                flash.error = message(code: "app.error.500")
                redirect(action: 'index', id: autoReasonOfDelayInstance?.id)
                return
            }
            flash.message = message(code: 'auto.rod.updated.message')
        }
        redirect(action: "edit", id: autoReasonOfDelayInstance.id)
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback
    private populateModel(AutoReasonOfDelay autoReasonOfDelayInstance) {
        //Do not bind in any other way because of the clone contained in the params
        autoReasonOfDelayInstance?.emailToUsers?.clear()
        bindData(autoReasonOfDelayInstance, params, [exclude: ["queriesRCA", "isEnabled", "globalDateRangeInformationAutoROD"]])
        setNextRunDateAndScheduleDateJSON(autoReasonOfDelayInstance)
        assignParameterValuesToGlobalDateRange(autoReasonOfDelayInstance)
        bindNewqueryRCAList(autoReasonOfDelayInstance)
        bindExistingQueryRCAEdits(autoReasonOfDelayInstance)
        if(!params.id){
            autoReasonOfDelayInstance.owner = getCurrentUser()
        }
    }

    private void setNextRunDateAndScheduleDateJSON(AutoReasonOfDelay autoReasonOfDelayInstance) {
        autoReasonOfDelayInstance.nextRunDate = null
        if (autoReasonOfDelayInstance.scheduleDateJSON && autoReasonOfDelayInstance.isEnabled) {
            if (com.rxlogix.util.MiscUtil.validateScheduleDateJSON(autoReasonOfDelayInstance.scheduleDateJSON)) {
                autoReasonOfDelayInstance.nextRunDate = configurationService.getNextDate(autoReasonOfDelayInstance)
                return
            }
        } else {
            autoReasonOfDelayInstance.scheduleDateJSON = null
        }
        autoReasonOfDelayInstance.nextRunDate = null
    }

    private void assignParameterValuesToGlobalDateRange(AutoReasonOfDelay autoReasonOfDelayInstance) {
        GlobalDateRangeInformationAutoROD globalDateRangeInformationAutoROD = autoReasonOfDelayInstance.globalDateRangeInformationAutoROD
        if (!globalDateRangeInformationAutoROD) {
            globalDateRangeInformationAutoROD = new GlobalDateRangeInformationAutoROD()
            autoReasonOfDelayInstance.globalDateRangeInformationAutoROD = globalDateRangeInformationAutoROD
            if (autoReasonOfDelayInstance.id) {
                globalDateRangeInformationAutoROD.autoReasonOfDelay = autoReasonOfDelayInstance
                globalDateRangeInformationAutoROD.save()
            }
        }
        bindData(globalDateRangeInformationAutoROD, params.globalDateRangeInformationAutoROD, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        configurationService.fixBindDateRange(globalDateRangeInformationAutoROD, autoReasonOfDelayInstance, params)
    }

    private bindExistingQueryRCAEdits(AutoReasonOfDelay autoReasonOfDelayInstance) {
        //handle edits to the existing Template Queries queriesRCA
        List<QueryRCA> deleteQueryRCAList = []
        autoReasonOfDelayInstance?.queriesRCA?.eachWithIndex() { queryRCA, i ->
            if (params.get("queriesRCA[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                queryRCA.properties = bindingMap
                queryRCA = (QueryRCA) userService.setOwnershipAndModifier(queryRCA)
                //Set the back reference on dateRangeInformationForQueryRCA object to QueryRCA; binding via bindingMap won't do this
                DateRangeInformationQueryRCA dateRangeInformationForQueryRCA = queryRCA.dateRangeInformationForQueryRCA

                setDateRangeInformationRCA(i, dateRangeInformationForQueryRCA, autoReasonOfDelayInstance)
                dateRangeInformationForQueryRCA.queryRCA = queryRCA
                if (params.("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.relativeDateRangeValue") && params.("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.relativeDateRangeValue") =~ "-?\\d+") {
                    dateRangeInformationForQueryRCA.relativeDateRangeValue = (params.("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.relativeDateRangeValue")) as Integer
                }
                assignParameterValuesToQueryRCA(autoReasonOfDelayInstance, queryRCA, i)
            }else{
                deleteQueryRCAList.add(queryRCA)
            }
        }
        deleteQueryRCAList.each { obj ->
            autoReasonOfDelayInstance.removeFromQueriesRCA(obj)
            obj.delete()
        }
        autoReasonOfDelayInstance
    }

    private void setDateRangeInformationRCA(int i, DateRangeInformationQueryRCA dateRangeInformationForQueryRCA, AutoReasonOfDelay autoReasonOfDelayInstance) {
        def dateRangeEnum = params.("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.dateRangeEnum")
        if (dateRangeEnum) {
            dateRangeInformationForQueryRCA?.dateRangeEnum = dateRangeEnum
            if (dateRangeEnum == com.rxlogix.enums.DateRangeEnum.CUSTOM.name()) {
                dateRangeInformationForQueryRCA?.dateRangeEnum = dateRangeEnum
                Locale locale = userService.currentUser?.preference?.locale
                dateRangeInformationForQueryRCA.dateRangeStartAbsolute = com.rxlogix.util.DateUtil.getStartDate(params.("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.dateRangeStartAbsolute"), locale)
                dateRangeInformationForQueryRCA.dateRangeEndAbsolute = com.rxlogix.util.DateUtil.getEndDate(params.("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.dateRangeEndAbsolute"), locale)
            } else {
                dateRangeInformationForQueryRCA?.dateRangeStartAbsolute = null
                dateRangeInformationForQueryRCA?.dateRangeEndAbsolute = null
            }
        }
    }

    private void assignParameterValuesToQueryRCA(AutoReasonOfDelay autoReasonOfDelayInstance, QueryRCA queryRCA, int i) {
        //TODO: This has been done for Audit Log, need to find alternative solution for this, unnecessarily keeping old values in ParameterValue Table.
        if (queryRCA.queryValueLists) {
            params.put("oldQueryValueList${queryRCA.id}", queryRCA.queryValueLists.toString())
        }

        queryRCA.queryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        queryRCA.queryValueLists?.clear()

        if (params.containsKey("queryRCA" + i + ".qev[0].key")) {

            // for each single query
            int start = 0
            params.("queriesRCA[" + i + "].validQueries").split(",").each { queryId -> // if query set
                QueryValueList queryValueList = new QueryValueList(query: queryId)

                int size = SuperQuery.get(queryId).getParameterSize()

                // if query set, iterate each query in query set
                for (int j = start; params.containsKey("queryRCA" + i + ".qev[" + j + "].key") && j < (start + size); j++) {
                    ParameterValue tempValue
                    String key = params.("queryRCA" + i + ".qev[" + j + "].key")
                    String value = params.("queryRCA" + i + ".qev[" + j + "].value")
                    if (value && value.startsWith(";")) {
                        value = value.substring(1)
                    }
                    String specialKeyValue = params.("queryRCA" + i + ".qev[" + j + "].specialKeyValue")

                    boolean isFromCopyPaste = false
                    if (params.("queryRCA" + i + ".qev[" + j + "].copyPasteValue")) {
                        value = params.("queryRCA" + i + ".qev[" + j + "].copyPasteValue")
                    }
                    if (params.("queryRCA" + i + ".qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("queryRCA" + i + ".qev[" + j + "].field"), false)
                    if (params.containsKey("queryRCA" + i + ".qev[" + j + "].field")) {
                        tempValue = new QueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("queryRCA" + i + ".qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new CustomSQLValue(key: key, value: value)
                    }
                    queryValueList.addToParameterValues(tempValue)
                }

                start += size
                queryRCA.addToQueryValueLists(queryValueList)
            }
        }
    }

    private bindNewqueryRCAList(AutoReasonOfDelay autoReasonOfDelayInstance) {
        //bind new Template Queries as appropriate
        for (int i = autoReasonOfDelayInstance.queriesRCA.size(); params.containsKey("queriesRCA[" + i + "].id"); i++) {
            if (params.get("queriesRCA[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                QueryRCA queryRCA = new QueryRCA(bindingMap)

                queryRCA = (QueryRCA) userService.setOwnershipAndModifier(queryRCA)
                //Set the back reference on DateRangeInformationForQueryRCA object to QueryRCA; binding via bindingMap won't do this
                DateRangeInformationQueryRCA dateRangeInformationForQueryRCA = queryRCA.dateRangeInformationForQueryRCA

                setDateRangeInformationRCA(i, dateRangeInformationForQueryRCA, autoReasonOfDelayInstance)
                dateRangeInformationForQueryRCA.queryRCA = queryRCA
                dateRangeInformationForQueryRCA.relativeDateRangeValue = params.int("queriesRCA[" + i + "].dateRangeInformationForQueryRCA.relativeDateRangeValue")
                assignParameterValuesToQueryRCA(autoReasonOfDelayInstance, queryRCA, i)
                autoReasonOfDelayInstance.addToQueriesRCA(queryRCA)
            }

        }
    }

    private getBindingMap(int i) {
        def bindingMap = [
                query                  : params.("queriesRCA[" + i + "].query"),
                operator               : params.("queriesRCA[" + i + "].operator"),
                dynamicFormEntryDeleted: params.("queriesRCA[" + i + "].dynamicFormEntryDeleted") ?: false,
                lateId                 : params.("queriesRCA[" + i + "].lateId") ?: null,
                rootCauseId            : params.("queriesRCA[" + i + "].rcCustomExpression") ? null : (params.("queriesRCA[" + i + "].rootCauseId") ?: null),
                rcCustomExpression     : params.("queriesRCA[" + i + "].rcCustomExpression") ?: null,
                rootCauseClassId       : params.("queriesRCA[" + i + "].rcClassCustomExp") ? null : (params.("queriesRCA[" + i + "].rootCauseClassId") ?: null),
                rcClassCustomExp       : params.("queriesRCA[" + i + "].rcClassCustomExp") ?: null,
                rootCauseSubCategoryId : params.("queriesRCA[" + i + "].rcSubCatCustomExp") ? null : (params.("queriesRCA[" + i + "].rootCauseSubCategoryId") ?: null),
                rcSubCatCustomExp      : params.("queriesRCA[" + i + "].rcSubCatCustomExp") ?: null,
                responsiblePartyId     : params.("queriesRCA[" + i + "].rpCustomExpression") ? null : (params.("queriesRCA[" + i + "].responsiblePartyId") ?: null),
                rpCustomExpression     : params.("queriesRCA[" + i + "].rpCustomExpression") ?: null,
                summary                : params.("queriesRCA[" + i + "].summarySql") ? null : (params.("queriesRCA[" + i + "].summary") ?: null),
                actions                : params.("queriesRCA[" + i + "].actionsSql") ? null : (params.("queriesRCA[" + i + "].actions") ?: null),
                investigation          : params.("queriesRCA[" + i + "].investigationSql") ? null : (params.("queriesRCA[" + i + "].investigation") ?: null),
                summarySql             : params.("queriesRCA[" + i + "].summarySql") ?: null,
                actionsSql             : params.("queriesRCA[" + i + "].actionsSql") ?: null,
                investigationSql       : params.("queriesRCA[" + i + "].investigationSql") ?: null,
                assignedToUser         : params.("queriesRCA[" + i + "].assignedToUser")?params.("queriesRCA[" + i + "].assignedToUser").split("_")[1]:null,
                assignedToUserGroup    : params.("queriesRCA[" + i + "].assignedToGroup")?params.("queriesRCA[" + i + "].assignedToGroup").split("_")[1]:null,
                sameAsRespParty        : params.("queriesRCA[" + i + "].sameAsRespParty")? true: false
        ]
        bindingMap
    }

    private notSaved() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.saved.message')
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private User getCurrentUser()
    {
        return userService.getUser()
    }

    def createQuery() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        saveConfigurationMapToSession()
        redirect(controller: "query", action: 'create')
    }

    private void saveConfigurationMapToSession() {
        Map editingAutoReasonOfDelayMap = [autoReasonOfDelayParams: (params as JSON).toString(), autoReasonOfDelayId: params.id, action: params.id ? "edit" : "create", controller: "autoReasonOfDelay", queryRCAIndex: params.queryRCAIndex]
        session.setAttribute("editingAutoReasonOfDelay", editingAutoReasonOfDelayMap)
    }

    def jobExecutionHistory(){
        render(view: "/job/jobExecutionHistory")
    }

    def fetchEvaluateCaseDatesForDatasource(Long dataSourceId){
        SourceProfile sourceProfile = SourceProfile.read(dataSourceId)
        if(!sourceProfile){
            return [] as JSON
        }
        render ViewHelper.getCaseDateI18nForLatestVersion() as JSON
    }

    def fetchEvaluateCaseDateSubmissionForDatasource(Long dataSourceId){
        SourceProfile sourceProfile = SourceProfile.read(dataSourceId)
        if(!sourceProfile){
            return [] as JSON
        }
        if(sourceProfile.includeLatestVersionOnly){
            render ViewHelper.getCaseDateI18nForLatestVersion() as JSON
        } else {
            render ViewHelper.getEvaluateCaseDateForSub() as JSON
        }
    }
}