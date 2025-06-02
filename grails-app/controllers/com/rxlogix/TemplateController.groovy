package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.ReportFieldSelectionTypeEnum
import com.rxlogix.enums.SortEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.XMLNodeType
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.mapping.E2BLocaleName
import com.rxlogix.repo.RepoFileResource
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.apache.commons.lang.mutable.MutableBoolean
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.hibernate.FlushMode
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException

import javax.xml.ws.Response
import java.sql.Types

import static com.rxlogix.Constants.CUSTOM_SQL_VALUE_REGEX_CONSTANT
import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class TemplateController {
    def springSecurityService
    def userService
    def templateService
    def CRUDService
    def sqlService
    def importService
    def reportFieldService
    def sessionFactory
    def signalIntegrationService
    def executedConfigurationService
    def customMessageService

    static allowedMethods = [save:'POST', delete: ['DELETE','POST']]

    @Secured(['ROLE_TEMPLATE_VIEW','ROLE_TEMPLATE_SET_VIEW'])
    def index() {
        Map messageMap = [
                isDeleted  : 'app.template.warn.isDeleted',
                notFound   : 'default.not.found.message',
                noPermission: 'app.warn.noPermission'
        ]
        // Check if the messageKey exists in the params and map
        if (params?.messageKey && messageMap.containsKey(params.messageKey)) {
            flash.warn = customMessageService.getMessage(messageMap[params.messageKey])
        }
        render(view: "index")
    }

    @Secured(['ROLE_TEMPLATE_VIEW','ROLE_TEMPLATE_SET_VIEW','ROLE_ICSR_PROFILE_VIEWER'])
    //todo:  rename to show() - morett
    def view(Long id) {
        log.debug("View Template method Started for ${id}")
        long startTime = System.currentTimeMillis()
        ReportTemplate reportTemplateInstance = getReportTemplate(id)
        if (!reportTemplateInstance) {
            notFound()
            return
        }
        if (!hasPermissionToView(reportTemplateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
            return
        }
        User currentUser = userService.getUser()
        if (!reportTemplateInstance.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [reportTemplateInstance.name, message(code: "app.label.template.lower")])
            redirect(view: "index")
        } else {
            long endTime = System.currentTimeMillis();
            log.debug("View Template method Ended for ${id}, Total Time Taken = "+ ((endTime - startTime) / 60000) + " mins")
            [editable: false, template: reportTemplateInstance, isExecuted: false,currentUser:currentUser,
                                         title   : message(code: "app.label.viewTemplate"), selectedLocale: reportTemplateInstance.owner.preference.locale]
        }
    }

    @Secured(['ROLE_TEMPLATE_VIEW','ROLE_TEMPLATE_SET_VIEW','ROLE_ICSR_PROFILE_VIEWER'])
    def viewExecutedTemplate(Long id) {
        log.debug("View Executed Template method Started for ${id}")
        long startTime = System.currentTimeMillis()
        ReportTemplate executedTemplateInstance = getReportTemplate(id)
        if (!executedTemplateInstance) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (!hasPermissionToView(executedTemplateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
        } else {
            long endTime = System.currentTimeMillis();
            log.debug("View Executed Template method Ended for ${id}, Total Time Taken = "+ ((endTime - startTime) / 60000) + " mins")
            render(view: "view", model: [editable: false, currentTemplate: ReportTemplate.get(executedTemplateInstance.originalTemplateId), currentUser: currentUser,
                                         template: executedTemplateInstance, isExecuted: true, title: message(code: "app.label.viewExecutedTemplate"), selectedLocale: executedTemplateInstance.owner.preference?.locale])
        }
    }

    @Secured(['ROLE_TEMPLATE_CRUD', 'ROLE_TEMPLATE_SET_CRUD'])
    def create() {
        User currentUser = userService.getUser()
        ReportTemplate reportTemplateInstance = getReportTemplateInstance()
        if(!reportTemplateInstance) {
            redirect(action: 'index')
            return
        }
        if (!hasPermissionToEdit(params.templateType as TemplateTypeEnum)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
        } else
        //todo:  get rid of editable and fromCreate map entries
            render(view: "create", model: [reportTemplateInstance: reportTemplateInstance, fromCreate: true, editable: true, isAdmin: currentUser.admin, currentUser: currentUser, selectedLocale: currentUser.preference.locale, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser)])
    }

    @Secured(['ROLE_TEMPLATE_CRUD', 'ROLE_TEMPLATE_SET_CRUD'])
    def save() {
        ReportTemplate reportTemplateInstance = getReportTemplateInstance()
        if (!reportTemplateInstance) {
            notSaved()
            return
        }
        if (!hasPermissionToEdit(reportTemplateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
            return
        }

        reportTemplateInstance.owner = userService.getUser()

        populateModel(reportTemplateInstance)

        //todo:  Until we move this to the domain object constraints, we'll go ahead and attempt the update regardless of outcome. - morett
        //todo:  This will collect all validation errors at once vs. piecemeal. - morett
        reportTemplateInstance = templateService.preValidateTemplate(reportTemplateInstance, params)
        User currentUser = userService.getUser()
        Locale locale = currentUser.preference.locale
        if(reportTemplateInstance.instanceOf(CaseLineListingTemplate) && params.boolean('hasBlanks')==true){
            flash.error = message(code:'app.template.cll.custom.expression.blank.value')
            render view: "create", model: [reportTemplateInstance: reportTemplateInstance, editable: true, fromCreate: true, currentUser: currentUser, selectedLocale: locale]
            return
        }
        try {
            if (reportTemplateInstance.hasErrors()) { //To handle pre validation conditions done in preValidateTemplate.
                throw new ValidationException("Template preValidate has added validation issues", reportTemplateInstance.errors)
            }
            reportTemplateInstance = (ReportTemplate) CRUDService.save(reportTemplateInstance)
            templateService.createExecutedTemplate(reportTemplateInstance)
        } catch (ValidationException ve) {
            //todo:  get rid of editable and fromCreate map entries - morett
            //todo:  we should be able to send the reportTemplateInstance prepopulated with the data that the container needs.  -morett
            //To Avoid flushing of session and custom validation error changes not to losse..
            sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
            render view: "create", model: [reportTemplateInstance: reportTemplateInstance, editable: true, fromCreate: true, currentUser: currentUser, selectedLocale: locale]
            return
        }
        if (session.editingConfiguration) {
            session.editingConfiguration.templateId = reportTemplateInstance.id
            redirect(controller: session.editingConfiguration.controller, action: session.editingConfiguration.action, params: [id: session.editingConfiguration.configurationId, continueEditing: true, templateId: reportTemplateInstance.id])
        } else {
            flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.template'), reportTemplateInstance.name])
            redirect(action: "view", id: reportTemplateInstance.id)
        }

    }

    @Secured(['ROLE_TEMPLATE_CRUD', 'ROLE_TEMPLATE_SET_CRUD'])
    def edit(Long id) {
        ReportTemplate reportTemplateInstance = getReportTemplate(id)
        if (!reportTemplateInstance) {
            notFound()
            return
        }
        if (!hasPermissionToEdit(reportTemplateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
            return
        }
        User currentUser = userService.getUser()

        if (reportTemplateInstance.isEditableBy(currentUser)) {
            showUsageMessage(reportTemplateInstance)

            render(view: "edit", model: [editable: true, template: reportTemplateInstance, isAdmin: currentUser.admin, currentUser: currentUser, selectedLocale: currentUser.preference.locale, sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser)])
        } else {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
        }
    }

    @Secured(['ROLE_TEMPLATE_CRUD', 'ROLE_TEMPLATE_SET_CRUD'])
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        ReportTemplate reportTemplateInstance = ReportTemplate.get(params.id)
        if (!reportTemplateInstance) {
            notFound()
            return
        }
        if (!hasPermissionToEdit(reportTemplateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
            return
        }
        if(params.version && (reportTemplateInstance.version > params.long('version'))) {
            flash.error = message(code:'app.template.update.lock.permission', args: [reportTemplateInstance.name])
            redirect(action: 'edit', id: reportTemplateInstance.id)
            return
        }
        if(reportTemplateInstance.instanceOf(CaseLineListingTemplate) &&  params.boolean('hasBlanks')==true){
            flash.error = message(code:'app.template.cll.custom.expression.blank.value')
            redirect(action: 'edit', id: reportTemplateInstance.id)
            return
        }
        User currentUser = userService.getUser()
        if (reportTemplateInstance.isEditableBy(currentUser)) {
            reportTemplateInstance.shareWithUsers
            reportTemplateInstance.shareWithGroups
            populateModel(reportTemplateInstance)
            Locale userLocale = reportTemplateInstance.owner.preference.locale

            try {
                reportTemplateInstance = templateService.updateTemplate(reportTemplateInstance, params)
            } catch (ValidationException ve) {
                //To Avoid flushing of session and custom validation error changes not to losse..
                sessionFactory.currentSession.setFlushMode(FlushMode.MANUAL)
                render(view: "edit", model: [editable: true, template: reportTemplateInstance, currentUser: currentUser, selectedLocale: userLocale])
                return
            } catch (HibernateOptimisticLockingFailureException e) {
                flash.error = message(code: 'default.optimistic.locking.failure', args: [message(code: 'app.label.template'), reportTemplateInstance.name])
                redirect(action: "view", id: reportTemplateInstance.id)
                return
            }

            // Notify PVSignal to update their tables with these new changes
            if (grailsApplication.config.pvsignal.url){
                signalIntegrationService.notifySignalForUpdate(reportTemplateInstance)
            }

            flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.template'), reportTemplateInstance.name])
            redirect(action: "view", id: reportTemplateInstance.id)
        } else {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
        }
    }

    @Secured(['ROLE_TEMPLATE_CRUD', 'ROLE_TEMPLATE_SET_CRUD'])
    def delete() {
        User currentUser = userService.getUser()
        ReportTemplate templateInstance = ReportTemplate.get(params.id)
        if (!templateInstance) {
            notFound()
            return
        }
        if (!hasPermissionToEdit(templateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [templateInstance.name])
            redirect(view: "index")
            return
        }
        if (!templateInstance.isEditableBy(currentUser)) {
            flash.warn = message(code: "app.template.delete.permission", args: [templateInstance.name])
            redirect(view: "index")
            return
        }

        int templateUsed = templateService.getUsagesCount(templateInstance)
        if (templateUsed) {
            flash.error = """${message(code: "app.template.delete.usage", args: [templateInstance.name, templateUsed])}
                        <linkQuery>${createLink(controller: 'template', action: 'checkUsage', id: params.id)}"""
            redirect(view: "index")
            return
        }

        int usageTemplateSetCount = templateService.getUsagesCountTemplateSet(templateInstance)
        if (usageTemplateSetCount) {
            flash.error = """${
                message(code: "app.query.delete.usage.querySet", args: [templateInstance.name, usageTemplateSetCount])
            }
                    <linkQuery>${
                createLink(controller: 'template', action: 'checkUsageTemplateSet', id: params.id)
            }"""
            redirect(view: "index")
            return
        }

        try {
            CRUDService.softDelete(templateInstance, templateInstance.name,params.deleteJustification)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label.template'), templateInstance.name])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'app.label.template'), templateInstance.name])
                    redirect(action: "view", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }
        catch (HibernateOptimisticLockingFailureException e) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.optimistic.locking.failure', args: [message(code: 'app.label.template'), templateInstance.name])
                    redirect(action: "view", id: params.id)
                }
                '*' { render status: NO_CONTENT }
            }
        }

    }

    private void showUsageMessage(ReportTemplate templateInstance) {
        int usageCountTemplateSet = templateService.getUsagesCountTemplateSet(templateInstance)
        if (usageCountTemplateSet) {
            flash.warn = """${message(code: "app.query.usage.templateSet", args: [usageCountTemplateSet])}
                        <linkQuery>${createLink(controller: 'template', action: 'checkUsageTemplateSet', id: params.id)}"""
            return
        }
        // As we were showing TemplateSet usuage first
        int templateUsed = templateService.getUsagesCount(templateInstance)
        if (templateUsed) {
            flash.warn = """${message(code: "app.template.usage.reports", args: [templateUsed])}
                            <linkQuery>${createLink(controller: 'template', action: 'checkUsage', id: params.id)}"""
        }
    }

    @Secured(['ROLE_TEMPLATE_CRUD', 'ROLE_TEMPLATE_SET_CRUD'])
    def copy(Long id) {
        ReportTemplate reportTemplateInstance = getReportTemplate(id)
        if (!reportTemplateInstance) {
            notFound()
            return
        }
        if(!hasPermissionToEdit(reportTemplateInstance.templateType )) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
            return
        }
        User currentUser = userService.getUser()
        if (!reportTemplateInstance.isViewableBy(currentUser)) {
            redirect(view: "index")
            flash.warn = message(code: "app.userPermission.message", args: [reportTemplateInstance.name, message(code: "app.label.template.lower")])
        } else {
            ReportTemplate copiedTemplate = templateService.copyTemplate(reportTemplateInstance, currentUser)
            try {
                copiedTemplate = (ReportTemplate) CRUDService.save(copiedTemplate)
                templateService.createExecutedTemplate(copiedTemplate)
            } catch (ValidationException ve) {
                chain(action: "index", model: [theInstance: copiedTemplate])
                return
            }
            flash.message = message(code: "app.copy.success", args: [copiedTemplate.name])
            redirect(action: "view", id: copiedTemplate.id, model: [copiedTemplate: copiedTemplate, status: OK])
        }
    }

    //Load JSON option is available only for DEV user.
    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def load() {}

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveJSONTemplates() {
        if (params?.JSONTemplates) {
            def listOfTemplatesJSON = "[${params?.JSONTemplates}]"
            JSONElement listOfTemplates
            try {
                listOfTemplates = JSON.parse(listOfTemplatesJSON)
            } catch (ConverterException ce) {
                flash.error = message(code: "app.load.import.json.parse.fail")
                redirect(action: "index")
                return
            }

            List<Tuple2<ReportTemplate, Boolean>> templates
            List success = []
            List failed = []
            List warning = []

            try {
                templates = importService.importTemplates(listOfTemplates)
            }
            catch (Exception e){
                failed.add(e.getMessage())
            }
            templates.each {
                ReportTemplate template  = it.getFirst()
                if (!template.hasErrors()) {
                    if (it.getSecond()){
                        warning.add(template.name)
                    } else {
                        success.add(template.name)
                    }
                } else {
                    log.error("Failed to import $template. ${template.errors}")
                    failed.add(template.name)
                }
            }
            if (success.size() > 0) {
                flash.message = message(code: "app.load.import.success", args: [success])
            }
            if (failed.size() > 0) {
                flash.error = message(code: "app.load.import.fail", args: [failed])
            }
            if (warning.size() > 0) {
                flash.warn = message(code: "app.load.import.warning", args: [warning])
            }
        } else {
            flash.warn = message(code: "app.load.import.noData")
            redirect(action: "load")
            return
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_TEMPLATE_CRUD'])
    def checkUsage(Long id) {
        ReportTemplate reportTemplate = getReportTemplate(id)
        render(view: "checkUsage", model: [usages: templateService.getUsages(reportTemplate), template: reportTemplate.name])
    }

    @Secured(['ROLE_TEMPLATE_SET_CRUD'])
    def checkUsageTemplateSet(Long id) {
        ReportTemplate template = ReportTemplate.read(id)
        List<TemplateSet> usages = templateService.getUsagesTemplateSet(template)
        render(view: "checkUsageTemplateSet", model: [usages: usages, template: template.name])
    }

    def favorite() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        ReportTemplate reportTemplate = params.id ? ReportTemplate.get(params.id) : null
        if (!reportTemplate) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.template'), params.id]) as String)
        } else {
            try {
                templateService.setFavorite(reportTemplate, params.boolean("state"))
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    @Secured(['ROLE_TEMPLATE_VIEW','ROLE_TEMPLATE_SET_VIEW'])
    def getFixedTemplateFile(Long id) {
        ReportTemplate reportTemplateInstance = getReportTemplate(id)
        if (!reportTemplateInstance) {
            notFound()
            return
        }
        if (!hasPermissionToView(reportTemplateInstance.templateType)) {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
            return
        }
        User currentUser = userService.getUser()
        if (!reportTemplateInstance.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [reportTemplateInstance.name, message(code: "app.label.template.lower")])
            redirect(view: "index")
        }
        if (!reportTemplateInstance.fixedTemplate?.data) {
            flash.warn = message(code: "app.template.fixedTemplate.null", args: [reportTemplateInstance.name])
            return
        }
        String fileName = reportTemplateInstance.fixedTemplate.name
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "filename=${fileName}")
        response.outputStream << reportTemplateInstance.fixedTemplate.data
    }

    private populateModel(ReportTemplate reportTemplateInstance) {

        //todo:  some binding is being done in preValidateTemplate(); move some of that here where possible - morett

        bindData(reportTemplateInstance, params, ["tags", "sharedWith", "rootNode", 'cllTemplates', 'nestedTemplates', 'e2bElementNameLocale'])
        setCommonFields(reportTemplateInstance)
        bindSharedWith(reportTemplateInstance,params.list('sharedWith'))
        bindFixedTemlate(reportTemplateInstance, params)
        if ((reportTemplateInstance instanceof DataTabulationTemplate) && !reportTemplateInstance.showChartSheet) reportTemplateInstance.chartCustomOptions = null
        if (!params.reassessListedness) reportTemplateInstance.reassessListedness = null
        if((reportTemplateInstance instanceof DataTabulationTemplate) && params.worldMap) reportTemplateInstance.chartExportAsImage=true
    }

    private void bindSharedWith(ReportTemplate reportTemplateInstance, List<String> sharedWith) {
        Set<Long> userGroups = []
        Set<Long> users = []
        sharedWith?.each { String shared ->
            if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                userGroups.add(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            } else if (shared.startsWith(Constants.USER_TOKEN)) {
                users.add(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
            }
        }
        Set<UserTemplate> userTemplates = reportTemplateInstance.userTemplates ? new HashSet<UserTemplate>(reportTemplateInstance.userTemplates) : []
        Set<UserGroupTemplate> userGroupTemplates = reportTemplateInstance.userGroupTemplates ? new HashSet<UserGroupTemplate>(reportTemplateInstance.userGroupTemplates) : []
        params.put('oldSharedWith', userTemplates?.collect { it?.user } + userGroupTemplates?.collect { it?.userGroup })
        reportTemplateInstance.userTemplates?.clear()
        reportTemplateInstance.userGroupTemplates?.clear()
        users.each { Long id ->
            if (userTemplates.any { it.userId == id }) {
                reportTemplateInstance.addToUserTemplates(userTemplates.find { it.userId == id })
            } else {
                reportTemplateInstance.addToUserTemplates(new UserTemplate(user: User.read(id)))
            }
        }
        userGroups.each { Long id ->
            if (userGroupTemplates.any { it.userGroupId == id }) {
                reportTemplateInstance.addToUserGroupTemplates(userGroupTemplates.find { it.userGroupId == id })
            } else {
                reportTemplateInstance.addToUserGroupTemplates(new UserGroupTemplate(userGroup: UserGroup.read(id)))
            }
        }
    }

    private getBindingMap(def reportFieldInfo) {
        CustomReportField crf=reportFieldInfo.customFieldId ? CustomReportField.get(reportFieldInfo.customFieldId as Long): null
        def bindingMap = [
                reportField            : ReportField.findByNameAndIsDeleted(reportFieldInfo.reportFieldName,false),
                argusName              : reportFieldInfo.argusName,
                renameValue            : reportFieldInfo.renameValue?:(crf?crf.customName:null),
                customExpression       : reportFieldInfo.customExpression,
                datasheet              : reportFieldInfo.datasheet,
                onPrimaryDatasheet     : reportFieldInfo.onPrimaryDatasheet,
                stackId                : reportFieldInfo.stackId ?: -1,
                sortLevel              : reportFieldInfo.sortLevel ?: -1,
                sort                   : reportFieldInfo.sort ? SortEnum.valueOfName(reportFieldInfo.sort) : null,
                commaSeparatedValue    : reportFieldInfo.commaSeparatedValue ?: false,
                suppressRepeatingValues: reportFieldInfo.suppressRepeatingValues ?: false,
                suppressLabel          : reportFieldInfo.suppressLabel ?: false,
                blindedValue           : reportFieldInfo.blindedValue == true,
                redactedValue         : reportFieldInfo.redactedValue == true,
                hideSubtotal           : reportFieldInfo.hideSubtotal == true,
                advancedSorting        : reportFieldInfo.advancedSorting,
                setId                  : reportFieldInfo.setId?: 0,
                columnWidth            : reportFieldInfo.columnWidth ?: ReportFieldInfo.AUTO_COLUMN_WIDTH,
                customField            : crf,
                newLegendValue         : reportFieldInfo.newLegendValue,
                drillDownTemplate      : reportFieldInfo.drillDownTemplate,
                drillDownFilerColumns  : reportFieldInfo.drillDownFilerColumns,
                colorConditions        : reportFieldInfo.colorConditions,
        ]
        bindingMap
    }

    private getMeasureBindingMap(int colMeasIndex, int measIndex) {
        String relativeParam = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-relativeDateRangeValue")
        String sortParam = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-sort")
        String type = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-type")
        String percentageOption = type.equals(com.rxlogix.reportTemplate.MeasureTypeEnum.COMPLIANCE_RATE.name()) ? com.rxlogix.reportTemplate.PercentageOptionEnum.NO_PERCENTAGE :
                (params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-percentageOption") ?: PercentageOptionEnum.NO_PERCENTAGE)
        def bindingMap = [
                type            : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-type"),
                name            : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-name"),
                dateRangeCount  : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-dateRangeCount"),
                percentageOption: percentageOption,
                relativeDateRangeValue: (relativeParam?.isInteger() && ((relativeParam as Integer) > 0) ? (relativeParam as Integer) : 1),
                showTotal       : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-showTotal") ?: false,
                showTopX        : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-showTopX") ?: false,
                topXCount       : params.getInt("colMeas" + colMeasIndex + "-meas" + measIndex + "-topXCount"),
                topColumnType   : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-topColumnType"),
                topColumnX      : params.getInt("colMeas" + colMeasIndex + "-meas" + measIndex + "-topColumnX"),
                sortLevel       : params.getInt("colMeas" + colMeasIndex + "-meas" + measIndex + "-sortLevel", -1),
                sort            : sortParam ? SortEnum.valueOfName(sortParam) : null,
                drillDownTemplate  : params.long("colMeas" + colMeasIndex + "-meas" + measIndex + "-drillDown"),
                valuesChartType    : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-valuesChartType"),
                percentageChartType: params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-percentageChartType"),
                colorConditions: params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-colorConditions"),
                valueAxisLabel     : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-overrideValueAxisLabel") ? (params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-valueAxisLabel") ?: " ") : null,
                percentageAxisLabel: params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-overridePercentageAxisLabel") ? (params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-percentageAxisLabel") ?: " ") : null
        ]
        bindingMap
    }

    private getXMLTemplateNodeBindingMap(def node) {
        def bindingMap = [
                tagName                  : node.title,
                elementType              : node.data?.elementType,
                type                     : node.data?.type,
                orderingNumber           : node.data?.orderingNumber,
                template                 : ReportTemplate.get(node.data?.templateId),
                filterFieldInfo          : ReportFieldInfo.get(node.data?.filterFieldInfo?.id),
                reportFieldInfo          : ReportFieldInfo.get(node.data?.reportFieldInfo?.id),
                customSQLFieldInfo       : node.data?.customSQLFieldInfo?.id,
                customSQLFilterFiledInfo : node.data?.customSQLFilterFiledInfo?.id,
                value                    : node.data?.value,
                dateFormat               : node.data?.dateFormat,
                tagColor                 : node.data?.tagColor,
                e2bElement               : node.data?.e2bElement,
                e2bElementName           : node.data?.e2bElementName,
                sourceFieldLabel         : node.data?.sourceFieldLabel
        ]
        bindingMap
    }

    private ReportTemplate getReportTemplateInstance() {
        ReportTemplate reportTemplateInstance = null
        String templateType = params.templateType
        if (templateType == TemplateTypeEnum.CASE_LINE.name()) {
            reportTemplateInstance = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE)
        } else if (templateType == TemplateTypeEnum.DATA_TAB.name()) {
            reportTemplateInstance = new DataTabulationTemplate(templateType: TemplateTypeEnum.DATA_TAB)
        } else if (templateType == TemplateTypeEnum.CUSTOM_SQL.name()) {
            reportTemplateInstance = new CustomSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL)
        } else if (templateType == TemplateTypeEnum.NON_CASE.name()) {
            reportTemplateInstance = new NonCaseSQLTemplate(templateType: TemplateTypeEnum.NON_CASE)
        } else if (templateType == TemplateTypeEnum.TEMPLATE_SET.name()) {
            reportTemplateInstance = new TemplateSet(templateType: TemplateTypeEnum.TEMPLATE_SET)
        } else if (templateType == TemplateTypeEnum.ICSR_XML.name()) {
            reportTemplateInstance = new XMLTemplate(templateType: TemplateTypeEnum.ICSR_XML)
        }
        reportTemplateInstance
    }

    private void setCommonFields(ReportTemplate template) {
        addTags(template)

        if (template.templateType == TemplateTypeEnum.CASE_LINE) {
            template = (CaseLineListingTemplate) template
            def queryCriteria = MiscUtil.parseJsonText(template.JSONQuery)
            template.JSONQuery = queryCriteria.all.containerGroups ? template.JSONQuery : null

            template.columnList = createRFInfoList(params.columns)
            template.groupingList = createRFInfoList(params.grouping)
            template.rowColumnList = createRFInfoList(params.rowCols)
            templateService.fillCLLTemplateServiceFields(template)
        } else if (template.templateType == TemplateTypeEnum.DATA_TAB) {
            template = (DataTabulationTemplate) template
            template.groupingList = createRFInfoList(params.grouping)
            template.rowList = createRFInfoList(params.rows)
            template.columnMeasureList = null
            def queryCriteria = MiscUtil.parseJsonText(template.JSONQuery)
            template.JSONQuery = queryCriteria.all.containerGroups ? template.JSONQuery : null

            if (params.("validColMeasIndex") != "") {
                List validColMeasIndex = params.("validColMeasIndex").split(',')
                validColMeasIndex.each { index ->
                    int colMeasIndex = index.toInteger()
                    DataTabulationColumnMeasure columnMeasure = new DataTabulationColumnMeasure()

                    // create column list
                    String columns = params.("columns" + colMeasIndex)
                    columnMeasure.columnList = createRFInfoList(columns)

                    if (params.("showTotalIntervalCases" + colMeasIndex)) {
                        columnMeasure.showTotalIntervalCases = params.("showTotalIntervalCases" + colMeasIndex)
                    } else {
                        columnMeasure.showTotalIntervalCases = false
                    }

                    if (params.("showTotalCumulativeCases" + colMeasIndex)) {
                        columnMeasure.showTotalCumulativeCases = params.("showTotalCumulativeCases" + colMeasIndex)
                    } else {
                        columnMeasure.showTotalCumulativeCases = false
                    }

                    List validMeasureIndex = params.("colMeas" + colMeasIndex + "-validMeasureIndex").split(',')
                    validMeasureIndex.each {
                        if (it != "") {
                            int measIndex = it.toInteger()
                            LinkedHashMap bindingMap = getMeasureBindingMap(colMeasIndex, measIndex)
                            DataTabulationMeasure measure = new DataTabulationMeasure(bindingMap)
                            measure.valueAxisLabel = bindingMap.valueAxisLabel
                            measure.percentageAxisLabel = bindingMap.percentageAxisLabel
                            if (measure.dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT) {
                                // save date range with timezone
                                String dateFrom = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-customPeriodFrom")
                                String dateTo = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-customPeriodTo")
                                Locale locale = userService.user?.preference?.locale
                                measure.customPeriodFrom = DateUtil.getDateWithDayStartTime(DateUtil.parseDate(dateFrom, DateUtil.getShortDateFormatForLocale(locale)))
                                measure.customPeriodTo = DateUtil.getDateWithDayEndTime(DateUtil.parseDate(dateTo, DateUtil.getShortDateFormatForLocale(locale)))
                            }

                            measure.save(failOnError: true)
                            columnMeasure.addToMeasures(measure)
                        }
                    }
                    columnMeasure.save()
                    template.addToColumnMeasureList(columnMeasure)
                }
            }
            if (params.("chartCustomOptions") != "") {
                template.chartCustomOptions = params.("chartCustomOptions")
            }
        } else if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            template = (CustomSQLTemplate) template

            if (!template.customSQLTemplateWhere) {
                template.customSQLTemplateWhere = ''
            }
        } else if (template.templateType == TemplateTypeEnum.TEMPLATE_SET) {
            template = (TemplateSet) template
            params.put('oldNestedTemplates', template.nestedTemplates.collect())
            template?.nestedTemplates?.clear()

            params.templateSetNestedIds.split(',').each {
                ReportTemplate nestedTemplate = ReportTemplate.read(it)
                if (nestedTemplate) {
                    template.addToNestedTemplates(nestedTemplate)
                    if(nestedTemplate.showTempltReassessDate())
                        template.reassessListedness = ReassessListednessEnum.CUSTOM_START_DATE
                }
            }
        } else if (template.templateType == TemplateTypeEnum.ICSR_XML) {
            template = (XMLTemplate) template
            template?.nestedTemplates?.clear()
            if (params.rootNode) {
                XMLTemplateNode rootNode = createXMLTemplateNode(JSON.parse(params.rootNode))
                rootNode.save(failOnError: true)
                template.rootNode = rootNode
                template.nestedTemplates = getNestedTemplates(template.rootNode)
            }
        }
    }

    private XMLTemplateNode createXMLTemplateNode(JSONObject xmlTag) {
        XMLTemplateNode node
        if (xmlTag.key && !xmlTag.key.startsWith("_")) {
            node = XMLTemplateNode.get(xmlTag.key)
            node?.children?.clear()
        }
        if (!node) {
            node = new XMLTemplateNode()
        }
        bindData(node, getXMLTemplateNodeBindingMap(xmlTag), ["e2bElementNameLocale"])
        if (xmlTag.data.e2bLocale && xmlTag.data.e2bLocaleElementName)
            node.e2bElementNameLocale = new E2BLocaleName(e2bLocale: xmlTag.data.e2bLocale, e2bLocaleElementName: xmlTag.data.e2bLocaleElementName)
        else
            node.e2bElementNameLocale = null
        Collection<XMLTemplateNode> children = createXMLTemplateNodes(xmlTag.children)
        int orderingNumber = 0
        children?.each { child ->
            child.setOrderingNumber(orderingNumber++)
            node.addToChildren(child)
        }
        return node
    }

    private Collection<XMLTemplateNode> createXMLTemplateNodes(JSONArray xmlTags) {
        xmlTags.collect {
            createXMLTemplateNode(it)
        }
    }

    private Set<ReportTemplate> getNestedTemplates(XMLTemplateNode node) {
        Set<ReportTemplate> templates = new HashSet<>()
        if (node.type == XMLNodeType.TAG_PROPERTIES && node.template) {
            templates.add(node.template)
        }
        if (node.children) {
            node.children.each {
                templates.addAll(getNestedTemplates(it))
            }
        }
        return templates
    }

    //todo: combine with version in QueryController - morett
    private void addTags(ReportTemplate template) {
        template.tags?.clear()
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

                template.addToTags(tag)
            }
        }
    }

    private void bindFixedTemlate(ReportTemplate template, def params) {
        // Reset fixed template
        if (userService.currentUser.isDev()) {
            if (template.fixedTemplate && template.fixedTemplate.data && !params.fixedTemplateName) {
                template.fixedTemplate.data = null
                template.useFixedTemplate = false
            } else if (params.fixedTemplateFile && params.fixedTemplateFile instanceof MultipartFile) {
                // Update fixed template
                MultipartFile fixedTemplateFile = (MultipartFile) params.fixedTemplateFile
                if (fixedTemplateFile.size > 0) {
                    byte[] data = fixedTemplateFile.bytes
                    if (!template.fixedTemplate) {
                        FileResource mainReport = new FileResource(
                                name: "${template.name}.jrxml",
                                label: template.name,
                                fileType: FileResource.TYPE_JRXML)
                        RepoFileResource fixedTemplate = new RepoFileResource()
                        fixedTemplate.copyFromClient(mainReport)
                        fixedTemplate.save()
                        template.fixedTemplate = fixedTemplate
                    }
                    template.fixedTemplate.name = fixedTemplateFile.originalFilename
                    template.fixedTemplate.data = data
                }
            }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.template'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    def granularityForTemplate(Long templateId) {
        def result = [:]
        if (templateId) {
            DataTabulationTemplate template = DataTabulationTemplate.get(templateId)
            if (template) {
                result.granularity = template.isGranularity()
            }
        }
        render result as JSON
    }

    def reassessDateForTemplate(Long templateId) {
        boolean result = false
        if (templateId) {
            ReportTemplate template = ReportTemplate.get(templateId)
            if (template) {
                if (template?.instanceOf(TemplateSet)) {
                    template = TemplateSet.get(templateId)
                    template?.nestedTemplates?.each {
                        result = (it.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE  && !it?.templtReassessDate)
                    }
                }
                else {
                    result = template.showTempltReassessDate()
                }
            }
        }
        render result
    }

    def customSQLValuesForTemplate(Long templateId) {
        def result = []
        if (templateId) {
            NonCaseSQLTemplate nonCaseSQLTemplate = NonCaseSQLTemplate.get(templateId)
            CustomSQLTemplate customSQLTemplate = CustomSQLTemplate.get(templateId)
            List<String> nonCaseKeys = nonCaseSQLTemplate?.nonCaseSql?.findAll(CUSTOM_SQL_VALUE_REGEX_CONSTANT)
            List<String> customSqlKeys = (customSQLTemplate?.customSQLTemplateSelectFrom + " " + customSQLTemplate?.customSQLTemplateWhere)?.findAll(CUSTOM_SQL_VALUE_REGEX_CONSTANT)

            (nonCaseKeys ?: customSqlKeys)?.unique()?.each { key ->
                (nonCaseSQLTemplate ?: customSQLTemplate)?.customSQLValues?.each {
                    result += [key: it.key == key ? it.key : null, value: it.key == key ? it.value : null]
                }
            }
        }
        render result as JSON
    }

    def poiInputsForTemplate() {
        Set<String> result = []
        if (params.templateId) {
            result = ReportTemplate.get(params.templateId)?.getPOIInputsKeys()
        }
        render([data: result ?: []] as JSON)
    }

    Response userReportFieldsOptsBySource(Integer sourceId, String selectionType) {
        ReportFieldSelectionTypeEnum selectionTypeEnum = ReportFieldSelectionTypeEnum.valueOf(selectionType)
        Locale selectedLocale = session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'] as Locale ?: new Locale('en')
        render([data: reportFieldsToMap(reportFieldService.getReportingFields(userService.currentUser, selectionTypeEnum, sourceId), selectedLocale)] as JSON)
    }

    Response userDefaultReportFieldsOpts(String selectionType) {
        //UI caching based on username and lastModified.
        ReportFieldSelectionTypeEnum selectionTypeEnum = ReportFieldSelectionTypeEnum.valueOf(selectionType)
        Locale selectedLocale = session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'] as Locale ?: new Locale('en')
        header('Cache-Control', "public, max-age=${(Math.max(0, (new Date() + 30).time - new Date().time) / 1000L).toLong()}")
        header('Pragma', "cache")
        render([data: reportFieldsToMap(reportFieldService.getReportingFields(userService.currentUser, selectionTypeEnum), selectedLocale)] as JSON)
    }

    private createRFInfoList(String fieldsInfo) {
        ReportFieldInfoList rfList = null
        Set<ReportField> blindedList=User.getBlindedFieldsForUser(userService.currentUser)
        if (fieldsInfo) {
            rfList = new ReportFieldInfoList()
            List infoList = MiscUtil.parseJsonText(fieldsInfo)
            infoList.eachWithIndex { it, i ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(getBindingMap(it))
                if (i == 0) reportFieldInfo.hideSubtotal = false
                if (blindedList.find { blinded -> blinded.id == reportFieldInfo.reportField.id }) {
                    if (!reportFieldInfo.blindedValue && !reportFieldInfo.redactedValue) reportFieldInfo.blindedValue = true
                }
                rfList.addToReportFieldInfoList(reportFieldInfo)
            }
            rfList.save(failOnError: true)
        }
        return rfList
    }

    private setColumnNamesListFromSelectClause(def template, String sqlString, boolean usePvrDB) {
        String selectClause = sqlString.split("where")[0] + "where (0 = 1)"
        sqlService.validateColumnName(template, selectClause, usePvrDB)
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

    private boolean hasPermissionToView(TemplateTypeEnum type) {
        if (userService.isAnyGranted("ROLE_TEMPLATE_ADVANCED")) return true
        if (userService.isAnyGranted("ROLE_ICSR_PROFILE_VIEWER")) return true
        if (((type == TemplateTypeEnum.CASE_LINE) && userService.isAnyGranted("ROLE_TEMPLATE_VIEW")) ||
                ((type == TemplateTypeEnum.DATA_TAB) && userService.isAnyGranted("ROLE_TEMPLATE_VIEW")) ||
                ((type == TemplateTypeEnum.TEMPLATE_SET) && userService.isAnyGranted("ROLE_TEMPLATE_SET_VIEW"))) return true
        return false
    }

    private boolean hasPermissionToEdit(TemplateTypeEnum type) {
        if (userService.isAnyGranted("ROLE_TEMPLATE_ADVANCED")) return true
        if (((type == TemplateTypeEnum.CASE_LINE) && userService.isAnyGranted("ROLE_TEMPLATE_CRUD")) ||
                ((type == TemplateTypeEnum.DATA_TAB) && userService.isAnyGranted("ROLE_TEMPLATE_CRUD")) ||
                ((type == TemplateTypeEnum.TEMPLATE_SET) && userService.isAnyGranted("ROLE_TEMPLATE_SET_CRUD"))) return true
        return false
    }

    private ReportTemplate getReportTemplate(Long id){
        return ReportTemplate.read(id)
    }

    private List reportFieldsToMap(List<Map<String, List>> data, Locale locale) {
        Map sourceColumnMasterMap = [:]
        List<SourceColumnMaster> list = SourceColumnMaster.list()
        list.each { SourceColumnMaster it ->
            sourceColumnMasterMap.put(it.reportItem + "_" + it.lang, [argusName: it.reportFieldTableColumn, isClob: (it.columnType == 'C')])
        }
        data.collect { group ->
            [group   : (message(code: "app.reportFieldGroup.${group.text[0]}")),
             children: group.children.collect { field ->
                 def actualField = field
                 if (field instanceof CustomReportField) {
                     actualField = field.reportField
                 }
                 Map sourceColumnMaster = sourceColumnMasterMap.get(actualField.sourceColumnId + "_" + locale) as Map ?: sourceColumnMasterMap.get(actualField.sourceColumnId + "_*") as Map
                 [argusName        : sourceColumnMaster?.argusName,
                  reportFieldName  : field.name,
                  clobType         : sourceColumnMaster?.isClob,
                  description      : field.reportFieldDescription,
                  legend           : message(code: ('app.reportField.' + field.name + '.label.legend'), default: ''),
                  customExpression : field.hasProperty("defaultExpression") ? field.defaultExpression : "",
                  customFieldId    : field.hasProperty("defaultExpression") ? field.id : "",
                  fromBlindedList  : field.isBlinded,
                  fromProtectedList: field.isProtected,
                  fieldLabelJ      : message(code: ('app.reportField.' + field.name), default: '', locale: Locale.JAPANESE),
                  fieldLabelE      : message(code: ('app.reportField.' + field.name), default: '', locale: Locale.ENGLISH),
                  value            : field.id,
                  dateType         : actualField.dataType?.equals(Date) ? "1" : "0",
                  displayText      : field.reportFieldName ?: field.name]
             }.sort { it.displayText }]
        }
    }

}
