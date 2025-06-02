package com.rxlogix

import com.rxlogix.config.CustomSQLValue
import com.rxlogix.config.GlobalDateRangeInbound
import com.rxlogix.config.GlobalDateRangeInformation
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.InboundCompliance
import com.rxlogix.config.ParameterValue
import com.rxlogix.config.QueryCompliance
import com.rxlogix.config.QueryExpressionValue
import com.rxlogix.config.QueryValueList
import com.rxlogix.config.ReportField
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.Tag
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.jobs.InboundComplianceJob
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.User
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND


@Secured(["isAuthenticated()"])
class InboundComplianceController {

    def userService
    def configurationService
    def CRUDService
    def reportExecutorService
    def inboundComplianceService
    def executorThreadInfoService

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    def index() {

    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    def create() {
        InboundCompliance configurationInstance = new InboundCompliance()
        def fromSession = inboundComplianceService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.inboundComplianceParams) {
            initConfigurationFromMap(configurationInstance, fromSession.inboundComplianceParams)
        }
        Map queryComplianceIndex = fromSession.queryComplianceIndex
        boolean queryBlanks = false
        if (params.selectedQuery) {
            SuperQuery query = SuperQuery.get(params.selectedQuery)
            if (query) {
                QueryCompliance queryCompliance = new QueryCompliance(query: query)
                configurationInstance.addToQueriesCompliance(queryCompliance)
                if (query.hasBlanks) {
                    queryBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.query.notFound', args: [params.selectedQuery])
            }
        }
        render(view: "create", model: [queryComplianceIndex: queryComplianceIndex, queryBlanks: queryBlanks,
                                       configurationInstance: configurationInstance, sourceProfiles: SourceProfile.sourceProfilesForUser(getCurrentUser())])
    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    @Transactional
    def save() {
        if (request.method == 'GET') {
            notSaved()
            return
        }

        InboundCompliance configurationInstance = new InboundCompliance()
        configurationInstance.isDeleted=false
        populateModel(configurationInstance)
        try {
            configurationInstance = (InboundCompliance) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            render view: "create", model: [configurationInstance : configurationInstance,
                                           configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Unexpected error while saving Inbound Compliance -> save", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'list')
            return
        }

        flash.message = message(code: 'inbound.compliance.created.message')
        redirect(action: "list")
    }

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    def list() {
    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    def initialize(Long id) {
        InboundCompliance inboundCompliance
        try {
            inboundCompliance = InboundCompliance.get(id)
            inboundCompliance.isICInitialize=true
            inboundCompliance = (InboundCompliance) CRUDService.update(inboundCompliance)
            if(inboundCompliance && inboundCompliance.isICInitialize) {
                InboundComplianceJob.triggerNow()
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Unexpected error while initializing Inbound Compliance -> initialize", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'list')
            return
        }
        flash.message = message(code: 'app.sender.initialize.triggered.sender.msg', args: [inboundCompliance.senderName])
        redirect(controller: 'inboundCompliance', action: 'executionStatus')
    }

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    def view(Long id) {
        InboundCompliance configuration = InboundCompliance.read(id) as InboundCompliance
        if (!configuration) {
            notFound()
            return
        }
        String configurationJson = null
        if (params.viewConfigJSON && SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            configurationJson = configurationService.getConfigurationAsJSON(configuration).toPrettyString()
        }
        render(view: "view", model: [queriesCompliance : configuration.queriesCompliance, configurationInstance: configuration,
                                     isExecuted     : false,
                                     viewSql        : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(configuration) : null, configurationJson: configurationJson])
    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    def edit(Long id) {
        InboundCompliance configurationInstance = id ? InboundCompliance.read(id) : null
        if (!configurationInstance) {
            notFound()
            return
        }
        def fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.inboundComplianceParams && (id == Long.parseLong(fromSession.inboundComplianceParams.id))) {
            initConfigurationFromMap(configurationInstance, fromSession.inboundComplianceParams)
        }
        Map queryComplianceIndex = fromSession.queryComplianceIndex
        User currentUser = getCurrentUser()
        if (configurationInstance.executing) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.senderName])
            redirect(action: "list")
            return
        }
        if (!configurationInstance?.isEditableBy(currentUser)) {

            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.senderName])
            redirect(action: "list")
        } else {
            render(view: "edit", model: [configurationInstance : configurationInstance,
                                         queryComplianceIndex: queryComplianceIndex, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser)])
        }
    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        InboundCompliance configurationInstance = InboundCompliance.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if (params.version && (configurationInstance.version > params.long('version'))) {
            flash.error = message(code: 'app.configuration.update.lock.permission', args: [configurationInstance.senderName])
            redirect(action: 'edit', id: configurationInstance.id)
            return;
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: configurationInstance.id)
            return
        }
        populateModel(configurationInstance)
        try {
            if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
                configurationInstance.qualityChecked = false
            }
            configurationInstance = (InboundCompliance) CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            render view: "edit", model: [configurationInstance : configurationInstance,
                                         configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser)]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in while updating Inbound Compliance -> update", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'edit', id: configurationInstance?.id)
            return
        }
        redirect(action: "view", id: configurationInstance.id)
    }

    @Secured(["ROLE_PVC_INBOUND_EDIT"])
    def delete(InboundCompliance inboundCompliance) {

        if (!inboundCompliance) {
            notFound()
            return
        }

        User currentUser = userService.currentUser

        if (!inboundCompliance.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.configuration.delete.permission", args: [inboundCompliance.senderName])
            redirect(view: "list")
            return
        }

        try {
            CRUDService.softDelete(inboundCompliance, inboundCompliance.senderName, params.deleteJustification)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), inboundCompliance.senderName])
            redirect action: "list", method: "GET"
        } catch (ValidationException ve) {
            flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.configuration'), inboundCompliance.senderName])
            redirect(action: "view", id: params.id)
        }

    }


    private void initConfigurationFromMap(InboundCompliance configurationInstance, Map map) {
        params.putAll(map)
        populateModel(configurationInstance)
        inboundComplianceService.initConfigurationQueriesFromSession(session, configurationInstance)
        session.removeAttribute("editingInboundCompliance")
    }

    private populateModel(InboundCompliance configurationInstance) {
        clearListFromConfiguration(configurationInstance)
        bindData(configurationInstance, params, [exclude: ["queriesCompliance", "tags", "globalDateRangeInbound"]])
        bindSuspectProduct(configurationInstance)
        setAttributeTags(configurationInstance)
        bindExistingQueryComplianceEdits(configurationInstance)
        bindNewQueryComplianceList(configurationInstance)
        inboundComplianceService.removeRemovedQueriesCompliance(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        configurationInstance = (InboundCompliance) userService.setOwnershipAndModifier(configurationInstance)
        bindGlobalDateRangeInbound(configurationInstance)
        if(!params.id){
            configurationInstance.owner = getCurrentUser()
        }
        if (configurationInstance.includeWHODrugs) {
            configurationInstance.isMultiIngredient = true
        }
    }

    private bindGlobalDateRangeInbound(InboundCompliance inboundCompliance) {
        GlobalDateRangeInbound globalDateRangeInbound = inboundCompliance.globalDateRangeInbound
        if (!globalDateRangeInbound) {
            globalDateRangeInbound = new GlobalDateRangeInbound()
            inboundCompliance.globalDateRangeInbound = globalDateRangeInbound
        }
        bindData(globalDateRangeInbound, params.globalDateRangeInbound, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        configurationService.fixBindDateRange(globalDateRangeInbound, inboundCompliance, params)
    }

    private clearListFromConfiguration(InboundCompliance configurationInstance) {
        configurationInstance?.tags?.clear()
//        configurationInstance?.poiInputsParameterValues?.clear()
        return configurationInstance
    }

    private bindSuspectProduct(InboundCompliance configurationInstance) {
        if (!configurationInstance.productSelection && !configurationInstance.validProductGroupSelection && !configurationInstance.isTemplate) {
            configurationInstance.suspectProduct = false

        }
    }

    private InboundCompliance setAttributeTags(InboundCompliance configurationInstance) {

        if (params?.tags) {
            if (configurationInstance?.tags) {
                configurationInstance?.tags.each {
                    configurationInstance.removeFromTags(it)
                }
            }
            if (params.tags.class == String) {
                params.tags = [params.tags]
            }
            List updatedTags = params.tags
            updatedTags.unique().each {
                Tag tag = Tag.findByName(it)
                if (!tag) {
                    tag = new Tag(name: it).save()
                }
                configurationInstance.addToTags(tag)
            }
        }
        return configurationInstance
    }

    private bindExistingQueryComplianceEdits(InboundCompliance configurationInstance) {
        //handle edits to the existing Template Queries queriesCompliance
        configurationInstance?.queriesCompliance?.eachWithIndex() { queryCompliance, i ->
                LinkedHashMap bindingMap = getBindingMap(i)
                queryCompliance.properties = bindingMap
                queryCompliance = (QueryCompliance) userService.setOwnershipAndModifier(queryCompliance)
                assignParameterValuesToQueryCompliance(configurationInstance, queryCompliance, i)
        }
        configurationInstance
    }

    private void assignParameterValuesToQueryCompliance(InboundCompliance configurationInstance, QueryCompliance queryCompliance, int i) {
        //TODO: This has been done for Audit Log, need to find alternative solution for this, unnecessarily keeping old values in ParameterValue Table.
        if (queryCompliance.queryValueLists) {
            params.put("oldQueryValueList${queryCompliance.id}", queryCompliance.queryValueLists.toString())
        }

        queryCompliance.queryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        queryCompliance.queryValueLists?.clear()

        if (params.containsKey("queryCompliance" + i + ".qev[0].key")) {

            // for each single query
            int start = 0
            params.("queriesCompliance[" + i + "].validQueries").split(",").each { queryId -> // if query set
                QueryValueList queryValueList = new QueryValueList(query: queryId)

                int size = SuperQuery.get(queryId).getParameterSize()

                // if query set, iterate each query in query set
                for (int j = start; params.containsKey("queryCompliance" + i + ".qev[" + j + "].key") && j < (start + size); j++) {
                    ParameterValue tempValue
                    String key = params.("queryCompliance" + i + ".qev[" + j + "].key")
                    String value = params.("queryCompliance" + i + ".qev[" + j + "].value")
                    if (value && value.startsWith(";")) {
                        value = value.substring(1)
                    }
                    String specialKeyValue = params.("queryCompliance" + i + ".qev[" + j + "].specialKeyValue")

                    boolean isFromCopyPaste = false
                    if (params.("queryCompliance" + i + ".qev[" + j + "].copyPasteValue")) {
                        value = params.("queryCompliance" + i + ".qev[" + j + "].copyPasteValue")
                    }
                    if (params.("queryCompliance" + i + ".qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("queryCompliance" + i + ".qev[" + j + "].field"), false)
                    if (specialKeyValue) {
                        if (value && !configurationInstance.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: specialKeyValue, value: value, isFromCopyPaste: isFromCopyPaste))
                        } else if (configurationInstance.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            ParameterValue parameterValue = configurationInstance.poiInputsParameterValues.find {
                                it.key == specialKeyValue
                            }
                            value = parameterValue?.value
                            isFromCopyPaste = parameterValue?.isFromCopyPaste
                        }
                    }
                    if (params.containsKey("queryCompliance" + i + ".qev[" + j + "].field")) {
                        tempValue = new QueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("queryCompliance" + i + ".qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new CustomSQLValue(key: key, value: value)
                    }
                    queryValueList.addToParameterValues(tempValue)
                }

                start += size
                queryCompliance.addToQueryValueLists(queryValueList)
            }
        }
    }

    private bindNewQueryComplianceList(InboundCompliance configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = configurationInstance.queriesCompliance.size(); params.containsKey("queriesCompliance[" + i + "].id"); i++) {
            if (params.get("queriesCompliance[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                QueryCompliance queryCompliance = new QueryCompliance(bindingMap)

                queryCompliance = (QueryCompliance) userService.setOwnershipAndModifier(queryCompliance)

                assignParameterValuesToQueryCompliance(configurationInstance, queryCompliance, i)
                configurationInstance.addToQueriesCompliance(queryCompliance)
            }

        }
    }

    private bindTemplatePOIInputs(InboundCompliance configurationInstance) {
        for (int i = 0; params.containsKey("poiInput[" + i + "].key"); i++) {
            String key = params.("poiInput[" + i + "].key")
            String value = params.("poiInput[" + i + "].value")
            if (!configurationInstance.poiInputsParameterValues*.key?.contains(key) && value) {
                configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: key, value: value))
            }
        }
    }

    private getBindingMap(int i) {
        def bindingMap = [
                query                  : params.("queriesCompliance[" + i + "].query"),
                operator               : params.("queriesCompliance[" + i + "].operator"),
                dynamicFormEntryDeleted: params.("queriesCompliance[" + i + "].dynamicFormEntryDeleted") ?: false,
                allowedTimeframe         : params.("queriesCompliance[" + i + "].allowedTimeframe") ?: 0,
                criteriaName        : params.("queriesCompliance[" + i + "].criteriaName")
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
        Map editingInboundComplianceMap = [inboundComplianceParams: (params as JSON).toString(), inboundComplianceId: params.id, action: params.id ? "edit" : "create", controller: "inboundCompliance", queryComplianceIndex: params.queryComplianceIndex]
        session.setAttribute("editingInboundCompliance", editingInboundComplianceMap)
    }

    private validateTenant(Long tenantId){
        if(tenantId && (tenantId != (Tenants.currentId() as Long)) && !SpringSecurityUtils.ifAnyGranted("ROLE_DEV")){
            log.error("Request and Session tenant mismatch issue for User ${currentUser?.username} in ConfigurationController")
            return false
        }
        return true
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

    @Secured(["ROLE_PVC_INBOUND_VIEW"])
    public executionStatus(){
        render(view: "executionStatus", model: [isAdmin: userService.isCurrentUserAdmin()])
    }
}
