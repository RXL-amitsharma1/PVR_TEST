package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.CaseScheduleException
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DistributionChannelEnum
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.jobs.ICSRCaseGenerateDataJob
import com.rxlogix.jobs.ICSRScheduleExecutionJob
import com.rxlogix.jobs.ICSRScheduleProcessingJob
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import org.hibernate.FlushMode
import com.rxlogix.mapping.SafetyCalendar

@Secured(["isAuthenticated()"])
class IcsrProfileConfigurationController {

    def CRUDService
    def userService
    def icsrProfileConfigurationService
    def configurationService
    def reportExecutorService
    def icsrReportService
    def dynamicReportService
    def emailService
    def sessionFactory
    def icsrScheduleService

    static allowedMethods = [delete: ['DELETE','POST']]

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def index() {
    }

    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    def create() {
        IcsrProfileConfiguration icsrProfileConfInstance = new IcsrProfileConfiguration()
        def fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.configurationParams) {
            initConfigurationFromMap(icsrProfileConfInstance, fromSession.configurationParams)
        }
        Map templateQueryIndex = fromSession.templateQueryIndex
        boolean templateBlanks = false
        if (params.selectedTemplate) {
            ReportTemplate template = ReportTemplate.get(params.selectedTemplate)
            if (template) {
                IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(template: template)
                if (template.instanceOf(DataTabulationTemplate) && template.isGranularity()){
                    templateQuery.granularity = GranularityEnum.MONTHLY
                }
                icsrProfileConfInstance.addToTemplateQueries(templateQuery)
                if (template.hasBlanks) {
                    templateBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.template.notFound', args: [params.selectedTemplate])
            }
        }

        boolean queryBlanks = false
        if (params.selectedQuery) {
            SuperQuery query = SuperQuery.get(params.selectedQuery)
            if (query) {
                IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(query: query)
                icsrProfileConfInstance.addToTemplateQueries(templateQuery)
                if (query.hasBlanks) {
                    queryBlanks = true
                }
            } else {
                flash.error = message(code: 'app.configuration.query.notFound', args: [params.selectedQuery])
            }
        }
        User user = userService.currentUser
        render(view: "create", model: [configurationInstance: icsrProfileConfInstance, queryBlanks: false,
                                       templateBlanks       : false, configSelectedTimeZone: params?.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(user), hasConfigTemplateCreatorRole: user.isConfigurationTemplateCreator(), fieldProfiles: UserGroup.fetchAllFieldProfileByUser(user), isForIcsrProfile: true])
    }

    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    def edit() {
        Long id = params.id as Long
        IcsrProfileConfiguration icsrProfileConfInstance = id ? IcsrProfileConfiguration.read(id) : null
        if (!icsrProfileConfInstance) {
            notFound()
            return
        }
        def fromSession = configurationService.fetchConfigurationMapFromSession(params, session)
        if (fromSession.configurationParams && (id == Long.parseLong(fromSession.configurationParams.id))) {
            initConfigurationFromMap(icsrProfileConfInstance, fromSession.configurationParams)
        }
        Map templateQueryIndex = fromSession.templateQueryIndex
        User currentUser = userService.currentUser
        if (icsrProfileConfInstance.running) {
            flash.warn = message(code: "app.configuration.running.fail", args: [icsrProfileConfInstance.reportName])
            redirect(action: "index")
        }
        if (!icsrProfileConfInstance?.isEditableBy(currentUser)) {

            flash.warn = message(code: "app.configuration.edit.permission", args: [icsrProfileConfInstance.reportName])
            redirect(action: "index")
        } else {
            List<String> calendarNames = []
            SafetyCalendar.withNewSession {
                calendarNames = icsrProfileConfInstance.calendars.collect {  [id: it, name: SafetyCalendar.read(it).name] }
            }
            render(view: "edit", model: [configurationInstance: icsrProfileConfInstance, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser), hasConfigTemplateCreatorRole: currentUser.isConfigurationTemplateCreator(), fieldProfiles: UserGroup.fetchAllFieldProfileByUser(currentUser), isForIcsrProfile: true, calendarValue: (calendarNames as JSON).toString()])
        }
    }


    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    def copy(IcsrProfileConfiguration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = userService.currentUser
        def savedConfig = configurationService.copyConfig(originalConfig, currentUser)
        if (savedConfig.hasErrors()) {
            chain(action: "index", model: [theInstance: savedConfig])
        } else {
            flash.message = message(code: "app.copy.success", args: [savedConfig.reportName])
            redirect(action: "view", id: savedConfig.id)
        }
    }

    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    def delete(IcsrProfileConfiguration icsrProfileConfInstance) {

               if (!icsrProfileConfInstance) {
            notFound()
            return
        }

        User currentUser = userService.currentUser

        if (!icsrProfileConfInstance.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.configuration.delete.permission", args: [icsrProfileConfInstance.reportName])
            redirect(view: "index")
            return
        }

        try {
            CRUDService.softDelete(icsrProfileConfInstance, icsrProfileConfInstance.reportName, params.deleteJustification)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.configuration'), icsrProfileConfInstance.reportName])
            redirect action: "index", method: "GET"
        } catch (ValidationException ve) {
            flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.configuration'), icsrProfileConfInstance.reportName])
            redirect(action: "view", id: params.id)
        }

    }

    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    @Transactional
    def save() {

        if (request.method == 'GET') {
            notSaved()
            return
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'create')
            return
        }
        IcsrProfileConfiguration icsrProfileConfInstance = new IcsrProfileConfiguration()
        try {
            populateModel(icsrProfileConfInstance)
            icsrProfileConfInstance = preValidateTemplate(icsrProfileConfInstance)
            if (icsrProfileConfInstance.hasErrors()) {
                throw new ValidationException("Template preValidate has added validation issues", icsrProfileConfInstance.errors)
            }
            icsrProfileConfigurationService.saveUpdate(icsrProfileConfInstance)
        } catch (ValidationException ve) {
            log.warn("IcsrProfileConfigurationCntroller: Failed to save due to ${ve.message}")
            sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
            icsrProfileConfInstance.errors = ve.errors
            List<String> calendarNames = []
            SafetyCalendar.withNewSession {
                calendarNames = icsrProfileConfInstance.calendars.collect {  [id: it, name: SafetyCalendar.read(it).name] }
            }
            render view: "create", model: [configurationInstance: icsrProfileConfInstance, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser),calendarValue: (calendarNames as JSON).toString(), isForIcsrProfile: true]
            return
        } catch (Exception ex) {
            log.error('Unexpected error in icsrProfileConfiguration -> save',ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'create')
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.icsr.partner.profile.menuItem'), ""])
        redirect(action: "view", id: icsrProfileConfInstance.id)
    }

    private IcsrProfileConfiguration preValidateTemplate(IcsrProfileConfiguration configurationInstance) {
        for (int i = 0; i < configurationInstance.templateQueries.size(); i++) {
            if (configurationInstance?.templateQueries[i]?.distributionChannelName == DistributionChannelEnum.PV_GATEWAY || configurationInstance?.templateQueries[i]?.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER) {
                if (!configurationInstance.e2bDistributionChannel.reportFormat) {
                    configurationInstance.errors.rejectValue('e2bDistributionChannel.reportFormat', 'com.rxlogix.config.IcsrPartnerProfile.reportFormat.nullable')
                }
                if (!configurationInstance.e2bDistributionChannel.outgoingFolder) {
                    configurationInstance.errors.rejectValue('e2bDistributionChannel.outgoingFolder', 'com.rxlogix.config.DistributionChannel.outgoingFolder.nullable')
                }
            } else if (configurationInstance?.templateQueries[i]?.distributionChannelName == DistributionChannelEnum.EMAIL) {
                if (!configurationInstance?.templateQueries[i]?.emailConfiguration?.to || !configurationInstance?.templateQueries[i]?.emailConfiguration?.validate()) {
                    configurationInstance.errors.rejectValue('templateQueries[' + i + '].emailConfiguration.to', 'com.rxlogix.config.TemplateQuery.emailConfigurations.nullable')
                }
            }
            if(!configurationInstance?.templateQueries[i]?.validate(["dueInDays"])){
                configurationInstance.errors.rejectValue('templateQueries[' + i + '].dueInDays', 'com.rxlogix.config.configuration.dueInDays.positiveNumber')
            }
            if(!configurationInstance?.templateQueries[i]?.validate(["icsrMsgType"])){
                configurationInstance.errors.rejectValue('templateQueries[' + i + '].icsrMsgType', 'com.rxlogix.config.TemplateQuery.msgType.nullable')
            }
        }
        return configurationInstance
    }

    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    @Transactional
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        IcsrProfileConfiguration icsrProfileConfInstance = IcsrProfileConfiguration.lock(params.id)
        if (!icsrProfileConfInstance) {
            notFound()
            return
        }
        if (params.version && (icsrProfileConfInstance.version > params.long('version'))) {
            flash.error = message(code: 'app.configuration.update.lock.permission', args: [icsrProfileConfInstance.reportName])
            redirect(action: 'edit', id: icsrProfileConfInstance.id)
            return;
        }
        if(!validateTenant(params.long('tenantId'))){
            flash.error = message(code: "invalid.tenant")
            redirect(action: 'edit', id: icsrProfileConfInstance.id)
            return
        }
        try {
            populateModel(icsrProfileConfInstance)
            if (!userService.isAnyGranted("ROLE_QUALITY_CHECK")) {
                icsrProfileConfInstance.qualityChecked = false
            }
            icsrProfileConfInstance = preValidateTemplate(icsrProfileConfInstance)
            if (icsrProfileConfInstance.hasErrors()) {
                throw new ValidationException("Template preValidate has added validation issues", icsrProfileConfInstance.errors)
            }
            icsrProfileConfigurationService.saveUpdate(icsrProfileConfInstance)
        } catch (ValidationException ve) {
            log.warn("IcsrProfileConfigurationCntroller: Failed to update due to ${ve.message}")
            sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
            List<String> calendarNames = []
            SafetyCalendar.withNewSession {
                calendarNames = icsrProfileConfInstance.calendars.collect {  [id: it, name: SafetyCalendar.read(it).name] }
            }
            render view: "edit", model: [configurationInstance : icsrProfileConfInstance,
                                         configSelectedTimeZone: params.configSelectedTimeZone, sourceProfiles: SourceProfile.sourceProfilesForUser(userService.currentUser),calendarValue: (calendarNames as JSON).toString(), isForIcsrProfile: true]
            return
        } catch (Exception ex) {
            log.error('Unexpected error in icsrProfileConfiguration -> update',ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'edit', id: icsrProfileConfInstance?.id)
            return
        }
        flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label'), icsrProfileConfInstance.reportName])

        if(params.reportId) {
            redirect(controller: "report", action: "showFirstSection", id: params.reportId)
        } else{
            redirect(action: "view", id: icsrProfileConfInstance.id)
        }
    }


    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def view() {
        Long id = params.id as Long
        IcsrProfileConfiguration icsrProfileConfInstance = id ? IcsrProfileConfiguration.read(id) : null
        if (!icsrProfileConfInstance) {
            notFound()
        }
        String configurationJson = null
        if (params.viewConfigJSON && SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            configurationJson = configurationService.getConfigurationAsJSON(icsrProfileConfInstance)
        }
        render view: "view", model: [icsrProfileConfInstance: icsrProfileConfInstance, templateQueries: icsrProfileConfInstance.templateQueries, isExecuted     : false, viewSql        : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(icsrProfileConfInstance) : null, configurationJson: configurationJson]
    }

    private notSaved() {
        flash.error = message(code: 'default.not.saved.message')
        redirect action: "index", method: "GET"
    }

    protected void notFound() {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.icsr.partner.profile.menuItem'), params.id])
        redirect action: "index", method: "GET"
    }

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def viewCases() {
        render view: (params.frame ? "tabFrame" : "viewCases"), model: [isIcsrViewTracking: true, caseNumber: params.caseNumber, versionNumber: params.versionNumber, exIcsrProfileId: params.exIcsrProfileId, exIcsrTemplateQueryId: params.exIcsrTemplateQueryId, status: params.status]
    }

    private clearListFromConfiguration(IcsrProfileConfiguration configurationInstance) {
        configurationInstance?.calendars?.clear()
        configurationInstance?.deliveryOption?.emailToUsers?.clear()
        configurationInstance?.poiInputsParameterValues?.clear()
        configurationInstance?.authorizationTypes?.clear()
        return configurationInstance
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback
    private populateModel(IcsrProfileConfiguration configurationInstance) {
        //Do not bind in any other way because of the clone contained in the params
        clearListFromConfiguration(configurationInstance)
        bindData(configurationInstance, params, [exclude: ["templateQueries", "emailConfiguration", "e2bDistributionChannel",'globalQueryValueLists','sharedWith','calenderNameControl']])
        bindLockedVersionAndSuspectProduct(configurationInstance)
        assignParameterValuesToGlobalQuery(configurationInstance)
        bindExistingTemplateQueryEdits(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        configurationService.removeRemovedTemplateQueries(configurationInstance)
        bindTemplatePOIInputs(configurationInstance)
        bindSharedWith(configurationInstance, params.list('sharedWith'), configurationInstance.id ? true : false)
        bindCalendar(configurationInstance, params)
        bindDistributionChannel(configurationInstance)
        if (configurationInstance.includeWHODrugs) {
            configurationInstance.isMultiIngredient = true
        }
        bindAuthorizationType(configurationInstance, params)
        configurationInstance.setRuleEvaluation(params.ruleEvaluation)
    }

    private void bindAuthorizationType(IcsrProfileConfiguration configurationInstance, Map params) {
        if(params.authorizationTypeId) {
            params.list('authorizationTypeId').each {
                configurationInstance.addToAuthorizationTypes(Long.parseLong(it))
            }
        } else {
            if (!configurationInstance.isJapanProfile) {
                if (configurationInstance.deviceReportable) {
                    List<Long> deviceAuthorizationList = []
                    AuthorizationType.withNewSession {
                        deviceAuthorizationList = AuthorizationType.findAllByNameInList(['Marketed Device', 'Investigational Device']).collect {
                            it.id as long
                        }
                    }
                    configurationInstance.authorizationTypes = deviceAuthorizationList
                } else {
                    List<Long> drugAuthorizationList = []
                    AuthorizationType.withNewSession {
                        drugAuthorizationList = AuthorizationType.findAllByNameInList(['Marketed Drug', 'Investigational Drug']).collect {
                            it.id as long
                        }
                    }
                    configurationInstance.authorizationTypes = drugAuthorizationList
                }
            }
        }
    }
    
    private void bindCalendar(IcsrProfileConfiguration configurationInstance, Map params) {
        configurationInstance.calendars = []
        if(params.calenderNameControl && !params.calenderNameControl.equals("dummy")) {
            if(params.calenderNameControl.contains("dummy@!")) {
                params.calenderNameControl = params.calenderNameControl.split("dummy@!")[1]
            }
            params.list('calenderNameControl').each {
                configurationInstance.addToCalendars(Long.parseLong(it))
            }
        }
    }

    private void bindLockedVersionAndSuspectProduct(IcsrProfileConfiguration configurationInstance) {
        configurationInstance.includeLockedVersion = true
        configurationInstance.suspectProduct = true
    }

    private void bindSharedWith(IcsrProfileConfiguration configurationInstance, List<String> sharedWith, Boolean isUpdate = false) {
        List<User> allowedUsers = userService.getAllowedSharedWithUsersForCurrentUser();
        List<UserGroup> allowedGroups = userService.getAllowedSharedWithGroupsForCurrentUser();
        if (isUpdate) {
            if (configurationInstance.getShareWithUsers()) {
                allowedUsers.addAll(configurationInstance.getShareWithUsers())
                allowedUsers.unique { it.id }
            }
            if (configurationInstance.getShareWithGroups()) {
                allowedGroups.addAll(configurationInstance.getShareWithGroups())
                allowedGroups.unique { it.id }
            }
            configurationInstance?.deliveryOption?.sharedWith?.clear()
            configurationInstance?.deliveryOption?.sharedWithGroup?.clear()
        }

        if (sharedWith) {
            if(!configurationInstance.deliveryOption){ //TODO need to check if needed.
                configurationInstance.deliveryOption = new DeliveryOption()
                configurationInstance.deliveryOption.report = configurationInstance
            }
            sharedWith.each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup && allowedGroups.find { it.id == userGroup.id }) {
                        configurationInstance.deliveryOption.addToSharedWithGroup(userGroup)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    if (user && allowedUsers.find { it.id == user.id }) {
                        configurationInstance.deliveryOption.addToSharedWith(user)
                    }
                }
            }
        }
    }

    private bindDistributionChannel(IcsrProfileConfiguration configurationInstance) {
        boolean e2bSelected = false
        for(int i=0; i < configurationInstance.templateQueries.size(); i++){
            if(!configurationInstance.templateQueries[i].distributionChannelName.value().toString().equals(DistributionChannelEnum.PAPER_MAIL.name())){
                e2bSelected = true
                break
            }
        }
        if (e2bSelected) {
            DistributionChannel distributionChannel = configurationInstance.e2bDistributionChannel ?: new DistributionChannel()
            params.put('oldE2bDistributionChannel', MiscUtil.getObjectProperties(distributionChannel, distributionChannel.auditLogPropertiesList))
            bindData(distributionChannel, params.e2bDistributionChannel)
            if(distributionChannel){
                User user = userService.currentUser
                if(!distributionChannel.id){
                    distributionChannel.createdBy = user.username
                }
                distributionChannel.modifiedBy = user.username
                configurationInstance.e2bDistributionChannel = distributionChannel
            }
        } else if(configurationInstance.e2bDistributionChannel){
            params.put('oldE2bDistributionChannel', MiscUtil.getObjectProperties(configurationInstance.e2bDistributionChannel, configurationInstance.e2bDistributionChannel.auditLogPropertiesList))
        }
    }

    private getBindingMap(int i) {
        def bindingMap = [
                template               : params.("templateQueries[" + i + "].template"),
                query                  : params.("templateQueries[" + i + "].query"),
                dynamicFormEntryDeleted: params.("templateQueries[" + i + "].dynamicFormEntryDeleted") ?: false,
                blindProtected         : params.("templateQueries[" + i + "].blindProtected") ?: false,
                privacyProtected       : params.("templateQueries[" + i + "].privacyProtected") ?: false,
                title                  : params.("templateQueries[" + i + "].title") ?: null,
                authorizationType      : params.("templateQueries[" + i + "].authorizationType"),
                dueInDays              : params.("templateQueries[" + i + "].dueInDays"),
                icsrMsgType            : params.("templateQueries[" + i + "].msgType"),
                distributionChannelName: params.("templateQueries[" + i + "].distributionChannelName"),
                orderNo                : i,
                isExpedited            : params.("templateQueries[" + i + "].isExpedited") ?:false
        ]

        if (bindingMap.distributionChannelName.toString() == DistributionChannelEnum.EMAIL.name()) { //TODO need to correct.
            bindingMap.put("to",params.("templateQueries[" + i + "].emailConfiguration.to"))
            bindingMap.put("cc",params.("templateQueries[" + i + "].emailConfiguration.cc"))
            bindingMap.put("subject",params.("templateQueries[" + i + "].emailConfiguration.subject"))
            bindingMap.put("body",params.("templateQueries[" + i + "].emailConfiguration.body"))
            bindingMap.put("deliveryReceipt",params.("templateQueries[" + i + "].emailConfiguration.deliveryReceipt") ?: false)
        }
        bindingMap
    }

    private bindNewTemplateQueries(IcsrProfileConfiguration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = configurationInstance.templateQueries.size(); params.containsKey("templateQueries[" + i + "].id"); i++) {
            if (params.get("templateQueries[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                IcsrTemplateQuery templateQueryInstance = new IcsrTemplateQuery(bindingMap)
                if (templateQueryInstance.distributionChannelName == DistributionChannelEnum.EMAIL) {
                    EmailConfiguration emailConfiguration = new EmailConfiguration(bindingMap)
                    templateQueryInstance.emailConfiguration = emailConfiguration.save()
                }
                templateQueryInstance = (IcsrTemplateQuery) userService.setOwnershipAndModifier(templateQueryInstance)
                //Set the back reference on DateRangeInformationForTemplateQuery object to IcsrTemplateQuery; binding via bindingMap won't do this
                DateRangeInformation dateRangeInformationForTemplateQuery = templateQueryInstance.dateRangeInformationForTemplateQuery
                dateRangeInformationForTemplateQuery.dateRangeEnum = DateRangeEnum.CUMULATIVE //For IcsrProfile always CUMULATIVE as no globalDateRangeInfo
                dateRangeInformationForTemplateQuery.templateQuery = templateQueryInstance
                assignParameterValuesToTemplateQuery(configurationInstance, templateQueryInstance, i)
                configurationInstance.addToTemplateQueries(templateQueryInstance)
            }

        }
    }

    private bindExistingTemplateQueryEdits(IcsrProfileConfiguration configurationInstance) {
        //handle edits to the existing Template Queries
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            if (templateQuery.distributionChannelName == DistributionChannelEnum.EMAIL) {
                EmailConfiguration emailConfiguration = templateQuery.emailConfiguration ?: new EmailConfiguration()
                bindData(emailConfiguration, bindingMap)
                emailConfiguration.isDeleted = false
                templateQuery.emailConfiguration = emailConfiguration.id ? emailConfiguration : emailConfiguration.save()
            } else {
                if (templateQuery.emailConfiguration){
                    EmailConfiguration emailConfiguration = templateQuery.emailConfiguration
                    templateQuery.emailConfiguration = null
                    emailConfiguration.isDeleted = true
                }
            }
            templateQuery = (IcsrTemplateQuery) userService.setOwnershipAndModifier(templateQuery)
            //Set the back reference on DateRangeInformationForTemplateQuery object to IcsrTemplateQuery; binding via bindingMap won't do this
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery
            dateRangeInformationForTemplateQuery.dateRangeEnum = DateRangeEnum.CUMULATIVE //For IcsrProfile always CUMULATIVE as no globalDateRangeInfo
            dateRangeInformationForTemplateQuery.templateQuery = templateQuery
            assignParameterValuesToTemplateQuery(configurationInstance, templateQuery, i)
        }
        configurationInstance
    }

    private bindTemplatePOIInputs(IcsrProfileConfiguration configurationInstance) {
        for (int i = 0; params.containsKey("poiInput[" + i + "].key"); i++) {
            String key = params.("poiInput[" + i + "].key")
            String value = params.("poiInput[" + i + "].value")
            if (!configurationInstance.poiInputsParameterValues*.key?.contains(key) && value) {
                configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: key, value: value))
            }
        }
    }

    private void assignParameterValuesToGlobalQuery(IcsrProfileConfiguration configurationInstance) {
        configurationService.bindParameterValuesToGlobalQuery(configurationInstance, params)
    }


    private void assignParameterValuesToTemplateQuery(IcsrProfileConfiguration configurationInstance, IcsrTemplateQuery templateQuery, int i) {
        if (templateQuery.queryValueLists) {
            params.put("oldQueryValueList${templateQuery.id}", templateQuery.queryValueLists.toString())
        }
        if (templateQuery.templateValueLists) {
            params.put("oldTemplateValueList${templateQuery.id}", templateQuery.templateValueLists.toString())
        }
        templateQuery.queryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        templateQuery.queryValueLists?.clear()
        templateQuery.templateValueLists?.each {
            it.parameterValues?.each {
                CustomSQLValue.get(it.id)?.delete() // CustomSQLTemplateValue?
            }
            it.parameterValues?.clear()
        }
        templateQuery.templateValueLists?.clear()

        if (params.containsKey("templateQuery" + i + ".qev[0].key")) {

            // for each single query
            int start = 0
            params.("templateQueries[" + i + "].validQueries").split(",").each { queryId -> // if query set
                QueryValueList queryValueList = new QueryValueList(query: queryId)

                int size = SuperQuery.get(queryId).getParameterSize()

                // if query set, iterate each query in query set
                for (int j = start; params.containsKey("templateQuery" + i + ".qev[" + j + "].key") && j < (start + size); j++) {
                    ParameterValue tempValue
                    String key = params.("templateQuery" + i + ".qev[" + j + "].key")
                    String value = params.("templateQuery" + i + ".qev[" + j + "].value")
                    String specialKeyValue = params.("templateQuery" + i + ".qev[" + j + "].specialKeyValue")

                    boolean isFromCopyPaste = false
                    if (params.("templateQuery" + i + ".qev[" + j + "].copyPasteValue")) {
                        value = params.("templateQuery" + i + ".qev[" + j + "].copyPasteValue")
                    }
                    if (params.("templateQuery" + i + ".qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("templateQuery" + i + ".qev[" + j + "].field"), false)
                    if (specialKeyValue) {
                        if (!configurationInstance.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            configurationInstance.addToPoiInputsParameterValues(new ParameterValue(key: specialKeyValue, value: value, isFromCopyPaste: isFromCopyPaste))
                        } else if (configurationInstance.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            ParameterValue parameterValue = configurationInstance.poiInputsParameterValues.find {
                                it.key == specialKeyValue
                            }
                            value = parameterValue?.value
                            isFromCopyPaste = parameterValue?.isFromCopyPaste
                        }
                    }

                    if (params.containsKey("templateQuery" + i + ".qev[" + j + "].field")) {
                        tempValue = new QueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("templateQuery" + i + ".qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new CustomSQLValue(key: key, value: value)
                    }
                    queryValueList.addToParameterValues(tempValue)
                }

                start += size
                templateQuery.addToQueryValueLists(queryValueList)
            }
        }

        if (params.containsKey("templateQuery" + i + ".tv[0].key")) {
            TemplateValueList templateValueList = new TemplateValueList(template: params.("templateQueries[" + i + "].template"))

            for (int j = 0; params.containsKey("templateQuery" + i + ".tv[" + j + "].key"); j++) {
                ParameterValue tempValue
                tempValue = new CustomSQLValue(key: params.("templateQuery" + i + ".tv[" + j + "].key"),
                        value: params.("templateQuery" + i + ".tv[" + j + "].value"))
                templateValueList.addToParameterValues(tempValue)
            }
            templateQuery.addToTemplateValueLists(templateValueList)
        }
    }

    def loadIcsrReportSubmissionForm(Long icsrTempQueryId, String caseNumber, Integer versionNumber, Boolean noSubmisson) {
        IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
            return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(icsrTempQueryId, caseNumber, versionNumber)
        }
        String id = icsrCaseTracking.uniqueIdentifier()
        String profileName = icsrCaseTracking.profileName
        String currentState = icsrCaseTracking.e2BStatus
        Date dueDate = icsrCaseTracking.dueDate
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrProfileConfiguration.read(icsrCaseTracking.exIcsrProfileId)
        String recipientOrganizationName = executedIcsrProfileConfiguration?.recipientOrganizationName
        String preferredTimeZone = executedIcsrProfileConfiguration?.preferredTimeZone
        TimeZoneEnum timeZone = TimeZoneEnum.values().find { it.timezoneId == preferredTimeZone}
        render(template: "/icsrProfileConfiguration/icsrCaseTrackingSubmissionForm", model: [id: id, profileName: profileName, currentState: currentState, dueDate: dueDate, recipient: recipientOrganizationName, noSubmisson: noSubmisson, timeZone: timeZone])
    }

    def loadIcsrReportSubmission(Long icsrTempQueryId, String caseNumber, Boolean noSubmisson) {
        Integer versionNumber = params.int('versionNumber')
        IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
            return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(icsrTempQueryId, caseNumber, versionNumber)
        }
        String id = icsrCaseTracking.uniqueIdentifier()
        String profileName = icsrCaseTracking.profileName
        String currentState = icsrCaseTracking.e2BStatus
        Date dueDate = icsrCaseTracking.dueDate
        Date submissionDate = icsrCaseTracking.preferredDateTime
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrProfileConfiguration.read(icsrCaseTracking.exIcsrProfileId)
        String recipientOrganizationName = executedIcsrProfileConfiguration?.recipientOrganizationName
        String preferredTimeZone = executedIcsrProfileConfiguration?.preferredTimeZone
        TimeZoneEnum timeZone = TimeZoneEnum.values().find { it.timezoneId == preferredTimeZone}
        ExecutedIcsrTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(icsrTempQueryId)
        String distributionChannel = executedTemplateQuery.distributionChannelName
        render(template: "/icsrProfileConfiguration/caseTrackingSubmissionHistoryForm", model: [id: id, profileName: profileName, currentState: currentState, dueDate: dueDate, recipient: recipientOrganizationName, noSubmisson: noSubmisson, timeZone: timeZone, submissionDate: submissionDate, distributionChannel: distributionChannel])
    }

    def loadBulkIcsrReportSubmissionForm() {
        TimeZoneEnum timeZone = TimeZoneEnum.values().find { it.timezoneId == params.preferredTimeZone}
        render(template: "/icsrProfileConfiguration/icsrBulkCaseTrackingSubmissionForm", model: [id: params.bulkIds, recipient: params.recipient, timeZone: timeZone ])
    }

    def createQuery() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        saveConfigurationMapToSession()
        redirect(controller: "query", action: 'create')
    }

    def createTemplate() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        saveConfigurationMapToSession()
        redirect(controller: "template", action: 'create', params: [templateType: params.templateType])
    }

    @Secured(['ROLE_DEV'])
    def triggerJobNow() {
        if (Holders.config.getProperty('show.xml.option', Boolean) && Holders.config.getProperty('icsr.profiles.execution', Boolean)) {
            ICSRScheduleExecutionJob.triggerNow()
            flash.message = message(code:'app.icsrProfileConf.label.scheduled.triggered')
        } else {
            flash.error = "Icsr profiles execution job is disabled"
        }
        redirect(action: 'index')
    }

    @Secured(['ROLE_DEV'])
    def triggerExecutionJobNow() {
        if (Holders.config.getProperty('show.xml.option', Boolean) && Holders.config.getProperty('icsr.profiles.execution', Boolean)) {
            ICSRScheduleProcessingJob.triggerNow()
            flash.message = message(code:'app.icsrProfileConf.label.executionJobs.triggered')
            redirect(controller: 'executionStatus', action: 'list', params: [isICSRProfile: true])
            return
        }
        flash.error = "Icsr profiles execution job is disabled"
        redirect(action: 'index')
    }

    @Secured(['ROLE_DEV'])
    def triggerGenerateJobNow() {
        if (Holders.config.getProperty('show.xml.option', Boolean) && Holders.config.getProperty('icsr.profiles.execution', Boolean)) {
            ICSRCaseGenerateDataJob.triggerNow()
            flash.message = message(code:'app.icsrProfileConf.label.generate.data.triggered')
            redirect(controller: 'icsr', action: 'executionStatus')
            return
        }
        flash.error = "Icsr profiles execution job is disabled"
        redirect(action: 'index')
    }

    @Secured(['ROLE_ICSR_DISTRIBUTION'])
    def manualScheduleCase(Long profileId, Long templateQueryId, String caseNumberWithVersion, Integer dueInDays, Boolean isExpedited, String deviceId, Long authorizationType, String approvalNumber) {
        IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
        TemplateQuery templateQuery = profileConfiguration?.templateQueries?.find{it.id == templateQueryId}

        if (!profileConfiguration || !caseNumberWithVersion || !templateQuery || dueInDays < 0) {
            flash.error = message(code: "icsr.add.manual.case.invalid")
            redirect(action: "viewCases")
            return
        }
        String caseNumber = caseNumberWithVersion.split(Constants.CASE_VERSION_SEPARATOR).first()
        Long version = caseNumberWithVersion.split(Constants.CASE_VERSION_SEPARATOR).last().toLong()
        User user = userService.getUser()
        final Date scheduleDate = new Date()
        Long approvalId = approvalNumber ? approvalNumber.split(Constants.CASE_VERSION_SEPARATOR).first().toLong() : -1
        Long reportCategoryId = approvalNumber ? approvalNumber.split(Constants.CASE_VERSION_SEPARATOR).last().toLong() : -1
        try {
            icsrScheduleService.addCaseToSchedule(profileConfiguration, templateQuery, caseNumber, version, dueInDays, isExpedited, user?.username, user?.fullName, deviceId, authorizationType, approvalId, reportCategoryId, null, scheduleDate)
        } catch (CaseScheduleException e) {
            flash.error = message(code: "icsr.add.manual.case.failure")
            redirect(action: "viewCases")
            return
        }
        flash.message = message(code: "icsr.add.manual.case.success", args: [caseNumber + " v" + version, profileConfiguration.reportName])
        redirect(action: "viewCases", params: [status: IcsrCaseStatusEnum.SCHEDULED])
    }

    @Secured(['ROLE_ICSR_PROFILE_EDITOR'])
    def reProcess(String caseNumberWithVersion){
        if (!caseNumberWithVersion) {
            flash.error = message(code: 'icsr.reEvaluate.manual.case.invalid')
            redirect(action: "viewCases")
            return
        }
        String caseNumber = caseNumberWithVersion.split(Constants.CASE_VERSION_SEPARATOR).first()
        Long version = caseNumberWithVersion.split(Constants.CASE_VERSION_SEPARATOR).last().toLong()
        icsrScheduleService.addForReEvaluate(caseNumber, version)
        flash.message = message(code: 'icsr.reEvaluate.manual.case.success', args: [caseNumber + " v" + version])
        redirect(action: "viewCases")
    }

    private validateTenant(Long tenantId){
        if(tenantId && (tenantId != (Tenants.currentId() as Long)) && !SpringSecurityUtils.ifAnyGranted("ROLE_DEV")){
            log.error("Request and Session tenant mismatch issue for User ${userService?.currentUser?.username} in IcsrProfileConfigurationController")
            return false
        }
        return true
    }

    def listProfiles() {
        def icsrProfilequery = IcsrProfileConfiguration.fetchByProfileName(userService.currentUser, userService.currentUser.isICSRAdmin(), false, params.term)
        render icsrProfilequery.list().collect {
            [id: it[0], reportName: it[1]]
        } as JSON
    }

    private void saveConfigurationMapToSession() {
        Map editingConfigurationMap = [configurationParams: (params as JSON).toString(), configurationId: params.id, action: params.id ? "edit" : "create", controller: "icsrProfileConfiguration", templateQueryIndex: params.templateQueryIndex]
        session.setAttribute("editingConfiguration", editingConfigurationMap)
    }

    private void initConfigurationFromMap(IcsrProfileConfiguration configurationInstance, Map map) {
        params.putAll(map)
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        populateModel(configurationInstance)
        configurationInstance.reportName = params.reportName
        configurationService.initConfigurationTemplatesFromSession(session, configurationInstance)
        configurationService.initConfigurationQueriesFromSession(session, configurationInstance)
        session.removeAttribute("editingConfiguration")
    }
}
