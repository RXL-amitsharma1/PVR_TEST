package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.KeywordEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.MiscUtil
import grails.async.Promises
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.hibernate.FlushMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import grails.core.GrailsApplication
import com.rxlogix.Constants
import grails.util.Holders

import javax.xml.ws.Response

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class QueryController {

    public static String CUSTOM_SQL_VALUE_REGEX_CONSTANT = /:([a-zA-Z][a-zA-Z0-9_-]*)/

    GrailsApplication grailsApplication
    def queryService
    def reportFieldService
    def userService
    def CRUDService
    def sqlService
    def importService
    def sessionFactory
    def signalIntegrationService
    def PVCMIntegrationService

    static allowedMethods = [delete: ['DELETE','POST']]

    @Secured(['ROLE_QUERY_VIEW'])
    def index() {
    }

    @Secured(['ROLE_QUERY_VIEW'])
    def view(Long id) {
        SuperQuery query = SuperQuery.read(id) // Tests have an issue with .get().

        render(view: "view", model: [editable  : false, queryType: query.queryType, query: query,
                                     isExecuted: false, title: message(code: "app.label.viewQuery"), currentUser: userService.currentUser])
    }

    @Secured(['ROLE_QUERY_VIEW'])
    def viewExecutedQuery(Long id) {
        SuperQuery query = SuperQuery.read(id)
        User currentUser = userService.getUser()
        render(view: "view", model: [editable: false, queryType: query.queryType, currentQuery: SuperQuery.get(query.originalQueryId),
                                     query   : query, isExecuted: true, currentUser: currentUser,
                                     title   : message(code: "app.label.viewExecutedQuery")])
    }

    //Load JSON option is available only for DEV user.
    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def load() {
        render(view: 'load')
    }

    @Secured(['ROLE_QUERY_CRUD'])
    def create() {
        User currentUser = userService.getUser()
        render(view: "create", model: [editable: true, currentUser: currentUser, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser)])
    }

    @Secured(['ROLE_QUERY_CRUD'])
    def save() {
        SuperQuery superQueryInstance = getSuperQueryInstance()
        if (!superQueryInstance) {
            notSaved()
            return
        }

        User user = userService.getUser()
        String currUser= user.username
        superQueryInstance.owner = user
        populateModel(superQueryInstance)

        //todo:  Until we move this to the domain object constraints, we'll go ahead and attempt the update regardless of outcome. - morett
        //todo:  This will collect all validation errors at once vs. piecemeal. - morett
        superQueryInstance = preValidateQuery(superQueryInstance)
        try {
            if (superQueryInstance.hasErrors()) { //To handle pre validation conditions done in preValidateQuery.
                throw new ValidationException("SuperQuery preValidate has added validation issues", superQueryInstance.errors)
            }
            superQueryInstance = (SuperQuery) CRUDService.save(superQueryInstance)
            createExecutedQuery(superQueryInstance)
            PVCMIntegrationService.checkAndInvokeRoutingCondition(superQueryInstance, false)
        } catch (ValidationException ve) {
            //To Avoid flushing of session and custom validation error changes not to losse..
            sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
            render view: "create", model: [editable: true, query: superQueryInstance, currentUser: user, sourceProfiles: SourceProfile.sourceProfilesForUser(user)]
            return
        }
        if (grailsApplication.config.pvsignal.url) {
            Locale locale = GrailsHibernateUtil.unwrapIfProxy(superQueryInstance.owner?.preference)?.locale
            Promises.task {
                Logger logger = LoggerFactory.getLogger(IcsrController.class.getName())
                User.withNewSession {
                    try {
                        signalIntegrationService.cacheTableInsertionHandler(superQueryInstance, false, currUser, locale)
                    } catch (e) {
                        logger.error("Fatal error while cache table insertion, Query ID -> ${superQueryInstance.id ?: superQueryInstance.name}", e)
                    }
                }
            }
        } else {
            log.info("Cache table insertion skipped as pvsignal is not integrated, Query ID -> ${superQueryInstance.id ?: superQueryInstance.name}")
        }
        if (session.editingConfiguration) {
            session.editingConfiguration.queryId = superQueryInstance.id
            redirect(controller: session.editingConfiguration.controller, action: session.editingConfiguration.action, params: [id: session.editingConfiguration.configurationId, continueEditing: true, queryId: superQueryInstance.id])
        } else if (session.editingAutoReasonOfDelay) {
            session.editingAutoReasonOfDelay.queryId = superQueryInstance.id
            redirect(controller: session.editingAutoReasonOfDelay.controller, action: session.editingAutoReasonOfDelay.action, params: [id: session.editingAutoReasonOfDelay.autoReasonOfDelayId, continueEditing: true, queryId: superQueryInstance.id])
        } else if (session.editingInboundCompliance) {
            session.editingInboundCompliance.queryId = superQueryInstance.id
            redirect(controller: session.editingInboundCompliance.controller, action: session.editingInboundCompliance.action, params: [id: session.editingInboundCompliance.inboundComplianceId, continueEditing: true, queryId: superQueryInstance.id])
        } else {
            flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.query'), superQueryInstance.name])
            redirect(action: "view", id: superQueryInstance.id)

        }

    }

    @Secured(['ROLE_QUERY_CRUD'])
    def edit(Long id) {
        SuperQuery queryInstance = SuperQuery.read(id)
        if (!queryInstance) {
            notFound()
            return
        }

        User currentUser = userService.getUser()


        if (queryInstance.isEditableBy(currentUser)) {

            if (queryInstance.nonValidCases || queryInstance.icsrPadderAgencyCases || queryInstance.deletedCases) {
                if (!currentUser.admin) {
                    flash.warn = message(code: "app.query.edit.fail", args: [queryInstance.name])
                    redirect(action: "index")
                }
            }

            int usage=showUsageMessage(queryInstance)

            render(view: "edit", model: [editable: true, usage:(usage>0), query: queryInstance, isAdmin: currentUser.admin, isDevUser: userService.isCurrentUserDev(), currentUser: currentUser, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser)])
        } else {
            flash.warn = message(code: "app.query.edit.fail", args: [queryInstance.name])
            redirect(view: "index")
        }

    }

    @Secured(['ROLE_QUERY_CRUD'])
    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        SuperQuery superQueryInstance = SuperQuery.findById(params.id)
        if (!superQueryInstance) {
            notFound()
            return
        }
        if(params.version && (superQueryInstance.version > params.long('version'))) {
            flash.error = message(code:'app.query.update.lock.permission', args: [superQueryInstance.name])
            redirect(action: 'edit', id: superQueryInstance.id)
            return;
        }
        User user = userService.getUser()
        String currUser = user.username
        if (superQueryInstance.isEditableBy(user)) {

            if (superQueryInstance.nonValidCases || superQueryInstance.icsrPadderAgencyCases || superQueryInstance.deletedCases) {
                if (!user.isAdmin()) {
                    flash.warn = message(code: "app.query.edit.fail", args: [superQueryInstance.name])
                    redirect(view: "index")
                }
            }
            boolean isPreviouslyTagExist = superQueryInstance?.tags?.any { it.name == Holders.config.getProperty('pvcm.workflowTag')}
            superQueryInstance.shareWithUsers
            superQueryInstance.shareWithGroups
            populateModel(superQueryInstance)
            if(superQueryInstance.hasErrors()){
                //To Avoid flushing of session and custom validation error changes not to losse..
                sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
                render view: "edit", model: [editable: true, query: superQueryInstance, currentUser: user, sourceProfiles: SourceProfile.sourceProfilesForUser(user)]
                return
            }
            List<Long> oldQueryList= (superQueryInstance instanceof QuerySet)? superQueryInstance.getPersistentValue("queries").collect{it.id}:null
            //todo:  Until we move this to the domain object constraints, we'll go ahead and attempt the update regardless of outcome. - morett
            //todo:  This will collect all validation errors at once vs. piecemeal. - morett
            superQueryInstance = preValidateQuery(superQueryInstance)

            if(superQueryInstance instanceof QuerySet){
                Map dataMap = MiscUtil.parseJsonText(superQueryInstance.getJSONQuery())
                Map allMap = dataMap.all
                List containerGroupsList = allMap.containerGroups
                if (!containerGroupsList) {
                    flash.error = message(code: "com.rxlogix.config.query.QuerySet.requireQuery",args:null,locale: Locale.ENGLISH)
                    redirect(action: 'edit', id: superQueryInstance.id)
                    return
                }
            }
            try {
                if (superQueryInstance.hasErrors()) { //To handle pre validation conditions done in preValidateQuery.
                    throw new ValidationException("SuperQuery preValidate has added validation issues", superQueryInstance.errors)
                }
                if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                    superQueryInstance.qualityChecked = false
                }
                superQueryInstance = (SuperQuery) CRUDService.update(superQueryInstance)
                createExecutedQuery(superQueryInstance)
                if (superQueryInstance.queryType == QueryTypeEnum.QUERY_BUILDER) {
                   updateBlankParamsForQueryBuilder(superQueryInstance)
                } else if (superQueryInstance.queryType == QueryTypeEnum.CUSTOM_SQL) {
                    updateBlankParamsForCustomQuery(superQueryInstance)
                } else {
                    updateBlankParamsForQuerySet((QuerySet)superQueryInstance,oldQueryList)
                }
                PVCMIntegrationService.checkAndInvokeRoutingCondition(superQueryInstance, isPreviouslyTagExist)
            } catch (ValidationException ve) {
                //To Avoid flushing of session and custom validation error changes not to losse..
                sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
                render view: "edit", model: [editable: true, query: superQueryInstance, currentUser: user, sourceProfiles: SourceProfile.sourceProfilesForUser(user)]
                return
            }

            // Notify PVSignal to update their tables with these new changes
            if (grailsApplication.config.pvsignal.url){
                signalIntegrationService.notifySignalForUpdate(superQueryInstance)
                Promises.task {
                    Locale locale = GrailsHibernateUtil.unwrapIfProxy(superQueryInstance.owner?.preference)?.locale
                    Logger logger = LoggerFactory.getLogger(IcsrController.class.getName())
                    User.withNewSession {
                        try {
                            signalIntegrationService.cacheTableInsertionHandler(superQueryInstance, true, currUser, locale)
                        } catch (e) {
                            logger.error("Fatal error while cache table insertion, Query ID -> ${superQueryInstance.id}", e)
                        }
                    }
                }
            } else {
                log.info("Cache table insertion skipped as pvsignal is not integrated, Query ID -> ${superQueryInstance.id}")
            }

            flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.query'), superQueryInstance.name])

            redirect(action: "view", id: superQueryInstance.id)

        } else {
            flash.warn = message(code: "app.query.edit.fail", args: [superQueryInstance.name])
        }
    }

    private updateBlankParamsForQuerySet(QuerySet querySet, List<Long> oldQueryList) {

        List<Long> newQueryList = querySet.queries.collect { it.id }
        List<Long> addQueryList = newQueryList - oldQueryList
        //--global query--
        (ReportConfiguration.findAllByGlobalQuery(querySet) + CaseSeries.findAllByGlobalQuery(querySet)).each { configuration ->
            configuration.globalQueryValueLists.collect { it }.each { gqv ->
                if (!newQueryList.find { it == gqv.query.id }) {
                    configuration.removeFromGlobalQueryValueLists(gqv)
                }
            }
            addQueryList.each { id ->
                SuperQuery queryToAdd = SuperQuery.read(id)
                if (queryToAdd instanceof CustomSQLQuery) {
                    Set<CustomSQLValue> blankParams = ((CustomSQLQuery) queryToAdd).customSQLValues
                    if (blankParams) {
                        QueryValueList qvl = new QueryValueList(query: queryToAdd)
                        blankParams.each { paramToAdd ->
                            qvl.addToParameterValues(new ParameterValue(key: paramToAdd.key, value: "", isFromCopyPaste: false))
                        }
                        configuration.addToGlobalQueryValueLists(qvl)
                    }

                } else {
                    Set<CustomSQLValue> blankParams = JSON.parse(((Query) queryToAdd).JSONQuery).blankParameters
                    if (blankParams) {
                        QueryValueList qvl = new QueryValueList(query: queryToAdd)
                        blankParams.each { paramToAdd ->
                            String specialKeyValue = paramToAdd.value?.startsWith("&") ? paramToAdd.value : ""
                            QueryExpressionValue pv = new QueryExpressionValue(key: paramToAdd.key, value: "",
                                    isFromCopyPaste: false, reportField: ReportField.findByNameAndIsDeleted(paramToAdd.field, false),
                                    operator: QueryOperatorEnum.valueOf(paramToAdd.op), specialKeyValue: specialKeyValue)
                            qvl.addToParameterValues(pv)
                        }
                        configuration.addToGlobalQueryValueLists(qvl)
                    }
                }
            }
            CRUDService.update(configuration)
        }

        //--section queries--
        TemplateQuery.findAllByQuery(querySet).each { templateQuery ->
            templateQuery.queryValueLists.collect { it }.each { gqv ->
                if (!newQueryList.find { it == gqv.query.id }) {
                    templateQuery.removeFromQueryValueLists(gqv)
                }
            }
            addQueryList.each { id ->
                SuperQuery queryToAdd = SuperQuery.read(id)
                if (queryToAdd instanceof CustomSQLQuery) {
                    Set<CustomSQLValue> blankParams = ((CustomSQLQuery) queryToAdd).customSQLValues
                    if (blankParams) {
                        QueryValueList qvl = new QueryValueList(query: queryToAdd)
                        blankParams.each { paramToAdd ->
                            qvl.addToParameterValues(new ParameterValue(key: paramToAdd.key, value: "", isFromCopyPaste: false))
                        }
                        templateQuery.addToQueryValueLists(qvl)
                    }
                } else {
                    Set<CustomSQLValue> blankParams = JSON.parse(((Query) queryToAdd).JSONQuery).blankParameters
                    if (blankParams) {
                        QueryValueList qvl = new QueryValueList(query: queryToAdd)
                        blankParams.each { paramToAdd ->
                            String specialKeyValue = paramToAdd.value?.startsWith("&") ? paramToAdd.value : ""
                            QueryExpressionValue pv = new QueryExpressionValue(key: paramToAdd.key, value: "",
                                    isFromCopyPaste: false, reportField: ReportField.findByNameAndIsDeleted(paramToAdd.field, false),
                                    operator: QueryOperatorEnum.valueOf(paramToAdd.op), specialKeyValue: specialKeyValue)
                            qvl.addToParameterValues(pv)
                        }
                        templateQuery.addToQueryValueLists(qvl)
                    }
                }
            }
            CRUDService.update(templateQuery)
        }
    }


    private updateBlankParamsForQueryBuilder(SuperQuery superQueryInstance) {
        List<Map> newParams = new JsonSlurper().parseText(superQueryInstance.JSONQuery.replace("\\","\\\\")).blankParameters as List<Map> ?: []
        Map matchedParam
        QueryValueList.findAllByQuery(superQueryInstance).each { queryValueList ->
            if (!(queryValueList instanceof ExecutedQueryValueList) && queryValueList.queryId == superQueryInstance.id) {
                List<Map> tempParams = newParams.clone()
                queryValueList.parameterValues.collect { it }.each { queryValue ->
                    if (queryValue.instanceOf(QueryExpressionValue)) {
                        matchedParam = tempParams.find {
                            it.field == queryValue.reportField.name && it.op == queryValue.operator.name() && !it.newValue
                        }?.clone()

                        if (!matchedParam)
                            matchedParam = tempParams.find { it.field == queryValue.reportField.name && !it.newValue }
                        if (matchedParam) {
                            matchedParam.newValue = queryValue.value ?: ""
                            matchedParam.isFromCopyPaste = queryValue.isFromCopyPaste ?: false
                            tempParams.removeAll{ it.key == matchedParam.key}
                        }
                        queryValueList.removeFromParameterValues(queryValue)
                    } else {
                        log.error("Invalid QEV value for Query ${superQueryInstance.id} with ${queryValue.id} with ${queryValue?.id} and ${queryValue?.getClass()}")
                    }

                    if (matchedParam) {
                        String specialKeyValue = matchedParam.value?.startsWith("&") ? matchedParam.value : ""
                        QueryExpressionValue pv = new QueryExpressionValue(key: queryValue.key, value: queryValue.value,
                                isFromCopyPaste: queryValue.isFromCopyPaste, reportField: ReportField.findByNameAndIsDeleted(matchedParam.field, false),
                                operator: QueryOperatorEnum.valueOf(matchedParam.op), specialKeyValue: specialKeyValue)
                        queryValueList.addToParameterValues(pv)


                        CRUDService.update(queryValueList)
                    }
                }
                if (tempParams) {
                    tempParams.each {
                        QueryExpressionValue pv = new QueryExpressionValue(key: it.key, value: it.value,
                                reportField: ReportField.findByNameAndIsDeleted(it.field, false),
                                operator: QueryOperatorEnum.valueOf(it.op))
                        queryValueList.addToParameterValues(pv)
                        CRUDService.update(queryValueList)
                    }
                }
            }
        }
    }

    private updateBlankParamsForCustomQuery(SuperQuery superQueryInstance) {
        Set<CustomSQLValue> newParams= ((CustomSQLQuery)superQueryInstance).customSQLValues?: []
        QueryValueList.findAllByQuery(superQueryInstance).each { qvl ->
            if (!(qvl instanceof ExecutedQueryValueList)) {
                qvl.parameterValues.collect { it }.each { qv ->
                    def matchedParam = newParams.find { it.key == qv.key && !it.value }
                    if (matchedParam) {
                        matchedParam.value = qv.value
                        matchedParam.isFromCopyPaste = qv.isFromCopyPaste
                    }
                    qvl.removeFromParameterValues(qv)
                }
                newParams.each { paramToAdd ->
                    qvl.addToParameterValues(new ParameterValue(key: paramToAdd.key, value: paramToAdd.value, isFromCopyPaste: paramToAdd.isFromCopyPaste ?: false))
                }

                if (qvl.parameterValues.size() == 0) {
                    TemplateQuery templateQuery = TemplateQuery.getByQueryValueLists(qvl).list()[0]
                    if (templateQuery) {
                        templateQuery?.removeFromQueryValueLists(qvl)
                        CRUDService.update(templateQuery)
                    }
                    CRUDService.delete(qvl)
                } else
                    CRUDService.update(qvl)
            }
        }
    }
    @Secured(['ROLE_QUERY_CRUD'])
    def delete() {
        User currentUser = userService.getUser()
        SuperQuery queryInstance = SuperQuery.get(params.id)
        if (!queryInstance) {
            notFound()
            return
        }
        if (queryInstance.nonValidCases) {
            flash.error = message(code: "app.nonValidQuery.delete.fail", args: [queryInstance.name])
            redirect(view: "index")
            return
        }
        if (queryInstance.icsrPadderAgencyCases ) {
            flash.error = message(code: "app.icsrPadderAgencyCasesQuery.delete.fail", args: [queryInstance.name])
            redirect(view: "index")
            return
        }
        if (queryInstance.deletedCases) {
            flash.error = message(code: "app.deletedCasesQuery.delete.fail", args: [queryInstance.name])
            redirect(view: "index")
            return
        }

        if (!queryInstance.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.query.delete.permission", args: [queryInstance.name])
            redirect(view: "index")
            return
        }

        int usageCount = queryService.getUsagesCount(queryInstance)
        if (usageCount) {
            flash.error = """${
                message(code: "app.query.delete.usage", args: [queryInstance.name, usageCount])
            }
                    <linkQuery>${createLink(controller: 'query', action: 'checkUsage', id: params.id)}"""
            redirect(view: "index")
            return
        }

        int usageQuerySetCount = queryService.getUsagesCountQuerySet(queryInstance)
        if (usageQuerySetCount) {
            flash.error = """${
                message(code: "app.query.delete.usage.querySet", args: [queryInstance.name, usageQuerySetCount])
            }
                    <linkQuery>${
                createLink(controller: 'query', action: 'checkUsageQuerySet', id: params.id)
            }"""
            redirect(view: "index")
            return
        }

        try {
            CRUDService.softDelete(queryInstance, queryInstance.name, params.deleteJustification)
            PVCMIntegrationService.checkAndInvokeRoutingCondition(queryInstance, false)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.query'), queryInstance.name])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.query'), queryInstance.name])
                    redirect(action: "view", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveJSONQueries() {
        if (params?.JSONQueries) {
            def listOfQueriesJSON = "[${params?.JSONQueries}]"

            JSONElement listOfQueries
            try {
                listOfQueries = JSON.parse(listOfQueriesJSON)
            } catch (ConverterException ce) {
                flash.error = message(code: "app.load.import.json.parse.fail")
                redirect(action: "index")
                return
            }

            //Do not attempt the load QuerySets.  This is not supported at this time.  Requires refactoring and some design changes to support that.
            def hasQuerySet = listOfQueries.find{it.class != String && it.queryType.name == QueryTypeEnum.SET_BUILDER.name()}
            if (hasQuerySet) {
                flash.error = message(code: "app.load.import.querySet.not.supported")
                redirect(action: "index")
                return
            }

            List<SuperQuery> queries

            List success = []
            List failed = []

            try {
                queries = importService.importQueries(listOfQueries)
            }
            catch (Exception e){
                failed.add(e.getMessage())
            }

            queries.each {
                if (!it.hasErrors()) {
                    success.add(it.name)
                } else {
                    log.error("Failed to import $it. ${it.errors}")
                    failed.add(it.name)
                }
            }
            if (success.size() > 0) {
                flash.message = message(code: "app.load.import.success", args: [success])
                if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                    PVCMIntegrationService.invokeRoutingConditionAPI()
                }
            }
            if (failed.size() > 0) {
                flash.error = message(code: "app.load.import.fail", args: [failed])
            }
        } else {
            flash.warn = message(code: "app.load.import.noData")
            redirect(action: "load")
            return
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_QUERY_CRUD'])
    def copy(Long id) {
        SuperQuery queryInstance = SuperQuery.read(id)
        if (!queryInstance) {
            notFound()
            return
        }
        if (queryInstance.nonValidCases) {
            flash.error = message(code: "app.nonValidQuery.copy.fail", args: [queryInstance.name])
            redirect(view: "index")
            return
        }
        if (queryInstance.icsrPadderAgencyCases) {
            flash.error = message(code: "app.icsrPadderAgencyCasesQuery.copy.fail", args: [queryInstance.name])
            redirect(view: "index")
            return
        }
        if (queryInstance.deletedCases) {
            flash.error = message(code: "app.deletedCasesQuery.copy.fail", args: [queryInstance.name])
            redirect(view: "index")
            return
        }

        User currentUser = userService.getUser()
        String currUsername = currentUser.username
        SuperQuery copiedQuery = queryService.copyQuery(queryInstance, currentUser)

        try {
            copiedQuery = (SuperQuery) CRUDService.save(copiedQuery)
            createExecutedQuery(copiedQuery)
            PVCMIntegrationService.checkAndInvokeRoutingCondition(copiedQuery, false)
        } catch (ValidationException ve) {
            chain(action: "index", model: [theInstance: copiedQuery])
            return
        }
        if (grailsApplication.config.pvsignal.url) {
            Promises.task {
                Locale locale = GrailsHibernateUtil.unwrapIfProxy(superQueryInstance.owner?.preference)?.locale
                Logger logger = LoggerFactory.getLogger(IcsrController.class.getName())
                User.withNewSession {
                    try {
                        signalIntegrationService.cacheTableInsertionHandler(copiedQuery, false, currUsername, locale)
                    } catch (e) {
                        logger.error("Fatal error while cache table insertion, Query ID -> ${superQueryInstance.id}", e)
                    }
                }
            }
        } else {
            log.info("Cache table insertion skipped as pvsignal is not integrated, Query ID -> ${superQueryInstance.id}")
        }

        flash.message = message(code: "app.copy.success", args: [copiedQuery.name])
        redirect(action: "view", id: copiedQuery.id, model: [superQueryInstance: superQueryInstance, status: OK])
    }

    def favorite() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        SuperQuery query = params.id ? SuperQuery.get(params.id) : null
        if (!query) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.query'), params.id]) as String)
        } else {
            try {
                queryService.setFavorite(query, params.boolean("state"))
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    Response userReportFieldsOptsBySource(Integer sourceId) {
        render([data: reportFieldsToMap(reportFieldService.getReportFieldsForQuery(sourceId))] as JSON)
    }

    Response userDefaultReportFieldsOpts() { //UI caching based on username and lastModified.
        header('Cache-Control', "public, max-age=${(Math.max(0, (new Date() + 30).time - new Date().time) / 1000L).toLong()}")
        header('Pragma', "cache")
        render([data: reportFieldsToMap(reportFieldService.getReportFieldsForQuery())] as JSON)
    }

    def possibleValues(String lang, String field) {
        if (field != null) {
            render((reportFieldService.getSelectableValuesForFields(lang).get(field) ?: []) as JSON)
            return
        }
        render reportFieldService.getSelectableValuesForFields(lang) as JSON
    }

    def possiblePaginatedValues(String lang, String field, String term, Integer max, Integer page) {
        boolean isFaersTarget = params.isFaersTarget?.toBoolean()
        def jsonData = []
        max = max ?: 30
        page = page ?: 1
        Integer offset = Math.max(page - 1, 0) * max
        term = term?.trim() ?: ""

        if (field != null) {
            List results = reportFieldService.getNonCacheSelectableValuesForFields(field, lang, term, max, offset, isFaersTarget)
            results.each {
                // Select2 needs both an id and text. Both fields are used in the UI.
                jsonData << new JSONObject(id: it, text: it)
            }
        }
        render jsonData as JSON
    }

    def extraValues(String lang) {
        render reportFieldService.getExtraValuesForFields(lang, Tenants.currentId() as Long) as JSON
    }

    def getStringOperators() {
        render operatorsI18n(QueryOperatorEnum.stringOperators) as JSON
    }

    def getNumOperators() {
        render operatorsI18n(QueryOperatorEnum.numericOperators) as JSON
    }

    def getDateOperators() {
        render operatorsI18n(QueryOperatorEnum.dateOperators) as JSON
    }

    def getValuelessOperators() {
        render operatorsI18n(QueryOperatorEnum.valuelessOperators) as JSON
    }
    def getEmbaseOperators() {
        render operatorsI18n(QueryOperatorEnum.embaseOperators) as JSON
    }

    def getAllKeywords() {
        render getKeyWords() as JSON
    }

    /**
     * AJAX call used by autocomplete textfields.
     */
    def ajaxReportFieldSearch = {
        def jsonData = []

        if (params.term?.length() > 1) {
            List results = reportFieldService.retrieveValuesFromDatabaseSingle(ReportField.findByNameAndIsDeleted(params.field,false), params.term, params.lang?:userService.user?.preference?.locale?.language)

            results.each {
                // Select2 needs both an id and text. Both fields are used in the UI.
                jsonData << new JSONObject(id: it, text: it)
            }
        }

        render jsonData as JSON
    }

    def queryExpressionValuesForQuery(Long queryId) {
        List<Map> result = queryService.queryExpressionValuesForQuery(queryId) ?: []
        render(result as JSON)
    }

    def queryExpressionValuesForQuerySet(Long queryId) {
        List<List<Map>> result = queryService.queryExpressionValuesForQuerySet(queryId) ?: []
        render(result as JSON)
    }

    def customSQLValuesForQuery(Long queryId) {
        List<Map> result = queryService.customSQLValuesForQuery(queryId) ?: []
        render(result as JSON)
    }

    private def getKeyWords() {
        KeywordEnum.values().collect { KeywordEnum keyword ->
            return [value: keyword.name(), display: message(code: keyword.getI18nKey())]
        }
    }

    private def operatorsI18n(QueryOperatorEnum[] operators) {
        operators.collect { QueryOperatorEnum operator ->
            return [value: operator.name(), display: message(code: operator.getI18nKey())]
        }
    }

    private SuperQuery getSuperQueryInstance() {
        SuperQuery superQueryInstance = null
        String queryType = params.queryType

        if (queryType == QueryTypeEnum.QUERY_BUILDER.name()) {
            superQueryInstance = new Query()
        } else if (queryType == QueryTypeEnum.SET_BUILDER.name()) {
            superQueryInstance = new QuerySet()
        } else if (queryType == QueryTypeEnum.CUSTOM_SQL.name()) {
            superQueryInstance = new CustomSQLQuery()
        }

        superQueryInstance
    }

    private int showUsageMessage(SuperQuery queryInstance) {
        int totalUsageCount = 0
        String warningMessage = ""
        int usageCountQuerySet = queryService.getUsagesCountQuerySet(queryInstance)
        if (usageCountQuerySet) {
            warningMessage += """${message(code: "app.query.usage.querySet", args: [usageCountQuerySet])}
                        <linkQuery>${createLink(controller: 'query', action: 'checkUsageQuerySet', id: params.id)}</linkQuery>"""
            totalUsageCount += usageCountQuerySet
        }
        // As we were showing QuerySet usuage first
        int usageCount = queryService.getUsagesCount(queryInstance)
        if (usageCount) {
            warningMessage += """${message(code: "app.query.usage.reports", args: [usageCount])}
                            <linkQuery>${createLink(controller: 'query', action: 'checkUsage', id: params.id)}"""
            totalUsageCount += usageCount
        }
        flash.warn = warningMessage
        return totalUsageCount
    }

    //todo: combine with version in TemplateController - morett
    private void addTags(SuperQuery query) {
        query?.tags?.clear()
        if (params.tags) {
            if (params.tags.class == String) {
                params.tags = [params.tags]
            }
            List updatedTags = params.tags

            updatedTags.unique().each {
                Tag tag = Tag.findByName(it)
                if (!tag) {
                    tag = new Tag(name: it)
                }

                query.addToTags(tag)
            }
        }
    }

    @Secured(['ROLE_QUERY_CRUD'])
    def checkUsage(Long id) {
        SuperQuery query = SuperQuery.read(id)
        List configs = queryService.getUsages(query).collect {
            [url        : generateUsageUrl(it),
             name       : (it instanceof InboundCompliance ? it.senderName : (it instanceof ReportConfiguration ? it.reportName : it.seriesName)),
             description: it.description,
             dateCreated: it.dateCreated,
             fullName   : it.owner.fullName,
             tags       : it.tags,
             tenantId   : it.tenantId
            ]
        }
        render(view: "checkUsage", model: [usages: configs, query: query.name])
    }

    @Secured(['ROLE_QUERY_CRUD'])
    def checkUsageQuerySet(Long id) {
        SuperQuery query = SuperQuery.read(id)
        List<QuerySet> usages = queryService.getUsagesQuerySet(query)
        render(view: "checkUsageQuerySet", model: [usages: usages, query: query.name])
    }

    def validateValue() {
        Map map = [uploadedValues: "", message: "", success: false]
        boolean isFaersTarget = params.isFaersTarget?.toBoolean()
        String selectedField = params.selectedField
        List<String> list = params.values.split(";").collect { it.trim() }.findAll { it }
        if (list) {
            Map<String, List> validationResult = importService.getValidInvalidValues(list, selectedField, userService.user?.preference?.locale?.toString(), isFaersTarget)
            String template = g.render(template: '/query/importValueModal', model: [validValues: validationResult.validValues, invalidValues: validationResult.invalidValues, duplicateValues: importService.getDuplicates(list)])
            map.uploadedValues = template
            map.success = true
        }
        render map as JSON
    }

    def importExcel() {
        Map map = [uploadedValues: "", message: "", success: false]
        String selectedField = params.selectedField
        MultipartFile file = request.getFile('file')

        List list = importService.readFromExcel(file)
        if (list) {
            map.uploadedValues = list.join(';')
            map.success = true
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render map as JSON
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.query'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
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
    private populateModel(SuperQuery superQueryInstance) {
        bindData(superQueryInstance, params, [exclude: ['tags', 'sharedWith']])
        setQueryDropdown(superQueryInstance, params.queryDropdown)
        setCommonFields(superQueryInstance)
        bindSharedWith(superQueryInstance, params.list('sharedWith'))
    }

    //Populate query type dropdown for PVR-44232
    private void setQueryDropdown(SuperQuery query,String queryDropdown) {
        switch (queryDropdown) {
            case "NON_VALID_CASES":
                query.nonValidCases = true
                query.deletedCases = false
                query.icsrPadderAgencyCases = false
                break
            case "DELETED_CASES":
                query.nonValidCases = false
                query.deletedCases = true
                query.icsrPadderAgencyCases = false
                break
            case "ICSR_PADER_AGENCY_CASES":
                query.nonValidCases = false
                query.deletedCases = false
                query.icsrPadderAgencyCases = true
                break
        }
    }

    private setCommonFields(SuperQuery superQuery) {
        addTags(superQuery)
    }

    /*
    =================================================================================================================
    The methods below (or substance of) were moved from QueryService to allow for binding in the Controller prior
    to calling CRUDService.  They need further refactoring and/or some pieces moved to constraints. -morett
    =================================================================================================================
    */

    private preValidateQueryBuilder(Query query) {
//        query.queryExpressionValues?.each {
//            query.removeFromQueryExpressionValues(it)
//        }

        query.queryExpressionValues?.clear() // remove old values

        if (query.hasBlanks) { // add new values
            saveQueryExpressionValues(query)
        }

        // Re-assess Listedness
        if (!params.containsKey("reassessListedness")) {
            query.reassessListedness = null
        }

        return query
    }

    private preValidateSetBuilder(QuerySet querySet) {
        querySet?.queries?.clear()
        List<SuperQuery> queries = sqlService.getQueriesFromJSON(querySet.JSONQuery).findAll { it }
        queries.each {
            querySet.addToQueries(it)
            if (it.hasBlanks) {
                querySet.hasBlanks = true
            }
        }
        if (querySet.validateExcluding() && !sqlService?.validateQuerySet(querySet)) {
            querySet.errors.rejectValue('JSONQuery', 'com.rxlogix.config.query.QuerySet.requireQuery')
        }
        return querySet
    }

    private preValidateCustomSqlQuery(CustomSQLQuery customSQLQuery) {
        customSQLQuery.setJSONQuery(null)

        customSQLQuery.hasBlanks = saveCustomSQLValues(customSQLQuery)

        if (customSQLQuery.validateExcluding()) {
            if (customSQLQuery.hasBlanks) {
                if (sqlService?.validateTemplateQuerySQL(customSQLQuery.customSQLQuery)) {
                    customSQLQuery.errors.rejectValue('customSQLQuery', 'com.rxlogix.invalid.custom.sql.table.name')
                }
            } else {
                if (!sqlService.validateCustomSQL(CustomSQLQuery.getSqlQueryToValidate(customSQLQuery), false)) {
                    customSQLQuery.errors.rejectValue('customSQLQuery', 'com.rxlogix.config.query.customSQLQuery')
                }
            }
        }
        return customSQLQuery
    }

    private SuperQuery preValidateQuery(SuperQuery query) {

        if (query.queryType == QueryTypeEnum.QUERY_BUILDER) {
            query = preValidateQueryBuilder((Query) query)
        } else if (query.queryType == QueryTypeEnum.SET_BUILDER) {
            query = preValidateSetBuilder((QuerySet) query)
        } else if (query.queryType == QueryTypeEnum.CUSTOM_SQL) {
            query = preValidateCustomSqlQuery((CustomSQLQuery) query)
        }

        return query
    }

    private boolean saveCustomSQLValues(CustomSQLQuery query) {
        query.customSQLValues?.clear()
        String base = query.customSQLQuery
        List<String> keys = base?.findAll(CUSTOM_SQL_VALUE_REGEX_CONSTANT)
        keys?.unique()?.each {
            CustomSQLValue toAdd = new CustomSQLValue(key: it, value: "")
            query.addToCustomSQLValues(toAdd)
        }
        return keys?.size() > 0
    }

    private void bindSharedWith(SuperQuery query, List<String> sharedWith) {
        Set<Long> userGroups = []
        Set<Long> users = []
        sharedWith?.each { String shared ->
            if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                userGroups.add(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            } else if (shared.startsWith(Constants.USER_TOKEN)) {
                users.add(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
            }
        }
        Set<UserQuery> userQueries = query.userQueries ? new HashSet<UserQuery>(query.userQueries) : []
        Set<UserGroupQuery> userGroupQueries = query.userGroupQueries ? new HashSet<UserGroupQuery>(query.userGroupQueries) : []
        params.put('oldSharedWith', userQueries?.collect { it?.user } + userGroupQueries?.collect { it?.userGroup })
        query.userQueries?.clear()
        query.userGroupQueries?.clear()
        users.each { Long id ->
            if (userQueries.any { it.userId == id }) {
                query.addToUserQueries(userQueries.find { it.userId == id })
            } else {
                query.addToUserQueries(new UserQuery(user: User.read(id)))
            }
        }
        userGroups.each { Long id ->
            if (userGroupQueries.any { it.userGroupId == id }) {
                query.addToUserGroupQueries(userGroupQueries.find { it.userGroupId == id })
            } else {
                query.addToUserGroupQueries(new UserGroupQuery(userGroup: UserGroup.read(id)))
            }
        }
    }

    private void saveQueryExpressionValues(Query query) {
        JSONObject obj = new JSONObject(query.getJSONQuery())
        JSONArray keys = obj.getJSONArray("blankParameters")
        for (int i = 0; i < keys.length(); i++) {
            JSONObject expressionToAdd = keys.get(i)
            String specialKeyValue = expressionToAdd.get("value") && expressionToAdd.get("value").startsWith("&") ? expressionToAdd.get("value") : ""
            QueryExpressionValue toAdd = new QueryExpressionValue(reportField: ReportField.findByNameAndIsDeleted(expressionToAdd.get("field"),false),
                    operator: QueryOperatorEnum.valueOf(expressionToAdd.get("op")), value: "", specialKeyValue: specialKeyValue,
                    key: expressionToAdd.get("key"))
            query.addToQueryExpressionValues(toAdd)
        }
    }

    private List reportFieldsToMap(List<Map<String, List>> data) {
        List<String> caseNumberFieldNames = SourceProfile.fetchAllCaseNumberFieldNames()
        data.collect { group ->
            [group   : (message(code: "app.reportFieldGroup.${group.text[0]}")),
             children: group.children.collect { field ->
                 [name                : field.name,
                  dictionary          : field.dictionaryType?.toString() ?: '',
                  level               : field.dictionaryLevel ?: '',
                  validatable         : field.isImportValidatable(caseNumberFieldNames),
                  isAutocomplete      : field.isAutocomplete,
                  dataType            : field.dataType,
                  isText              : field.isText,
                  description         : message(code: ('app.reportField.' + field.name + '.label.description'), default: ''),
                  displayText         : message(code: "app.reportField.${field.name}"),
                  isNonCacheSelectable: field.nonCacheSelectable
                 ]
             }.sort { it.displayText }]
        }
    }

    private void createExecutedQuery(SuperQuery queryInstance) {
        if(!(queryInstance instanceof QuerySet)) {
            queryService.createExecutedQuery(queryInstance)
        }
    }

    private String generateUsageUrl(def config) {
        String url = null
        if (config.class == CaseSeries) {
            url = createLink(controller: 'caseSeries', action: 'show', params: [id: config.id])
        } else if (config.class == Configuration) {
            url = createLink(controller: 'configuration', action: 'view', params: [id: config.id])
        } else if (config.class == PeriodicReportConfiguration) {
            url = createLink(controller: 'periodicReport', action: 'view', params: [id: config.id])
        } else if (config.class == IcsrReportConfiguration) {
            url = createLink(controller: 'icsrReport', action: 'view', params: [id: config.id])
        } else if (config.class == IcsrProfileConfiguration) {
            url = createLink(controller: 'icsrProfileConfiguration', action: 'view', params: [id: config.id])
        } else if (config.class == InboundCompliance) {
            url = createLink(controller: 'inboundCompliance', action: 'view', params: [id: config.id])
        }
        return url
    }
}
