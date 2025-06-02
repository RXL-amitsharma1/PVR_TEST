package com.rxlogix

import com.rxlogix.config.Email
import com.rxlogix.config.IcsrOrganizationType
import com.rxlogix.config.UnitConfiguration
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.enums.UnitTypeEnum
import com.rxlogix.mapping.OrganizationCountry
import com.rxlogix.mapping.PreferredLanguage
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.mapping.AllowedAttachment
import grails.gorm.transactions.ReadOnly
import com.rxlogix.util.ViewHelper

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["ROLE_SYSTEM_CONFIGURATION"])
class UnitConfigurationController {

    def CRUDService
    def organizationTypeService
    UnitConfigurationService unitConfigurationService
    def importService
    def utilService
    UserService userService
    def sqlGenerationService

    static allowedMethods = [update:['PUT','POST']]

    def index() {
    }

    @ReadOnly('pva')
    def create() {
        String userLocale = userService.currentUser?.preference?.locale
        List<UnitConfiguration> recipientWithList = UnitConfiguration.findAllByOrganizationTypeAndUnitRetired(IcsrOrganizationType.findByName(Constants.ICSR_UNIT_CONF_REGULATORY_AUTHORITY), false);
        List<String> organizationCountryList = []
        OrganizationCountry.'pva'.withNewSession {
            organizationCountryList = OrganizationCountry.'pva'.findAllByLangDesc(userLocale).collect { it.name }
        }
        render view: "create", model: [icsrOrganizationTypeList: getAllIcsrOrganizationType(), recipientWithList : recipientWithList, organizationCountry : organizationCountryList]
    }
    
    def save() {
        String userLocale = userService.currentUser?.preference?.locale
        Map model = [:]
        if (request.method == 'GET') {
            notSaved()
            return
        }
        UnitConfiguration unitConfigurationInstance = new UnitConfiguration()
        
        bindData(unitConfigurationInstance, params)

        //unitConfigurationInstance.allowedAttachments.clear()
        bindAllowedAttachments(unitConfigurationInstance,params)
        params.put('oldAttachments', null)

        try {
            CRUDService.save(unitConfigurationInstance)
            bindEmail(unitConfigurationInstance)
        } catch (ValidationException ve) {
            unitConfigurationInstance.errors = ve.errors
            model.unitConfigurationInstance = unitConfigurationInstance
            List<UnitConfiguration> recipientWithList = UnitConfiguration.findAllByOrganizationTypeAndUnitRetired(IcsrOrganizationType.findByName(Constants.ICSR_UNIT_CONF_REGULATORY_AUTHORITY), false);
            List<String> organizationCountryList = []
            OrganizationCountry.'pva'.withNewSession {
                organizationCountryList = OrganizationCountry.'pva'.findAllByLangDesc(userLocale).collect { it.name }
            }
            render view: "create", model: [unitConfigurationInstance: unitConfigurationInstance, icsrOrganizationTypeList: getAllIcsrOrganizationType(), recipientWithList :recipientWithList, organizationCountry : organizationCountryList]
            return
        } catch (Exception ex) {
            log.error("Unexpected error in unitConfiguration -> save", ex)
            flash.error = message(code: "app.error.500")
            redirect(action: 'create')
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'app.unitConfiguration.label.myInbox'), ""])
        redirect (action: "index")
    }


    @Secured(["ROLE_ICSR_PROFILE_VIEWER", "ROLE_SYSTEM_CONFIGURATION"])
    def show(Long id) {
        UnitConfiguration unitConfigurationInstance = id ? UnitConfiguration.read(id) : null
        if (!unitConfigurationInstance) {
            notFound()
            return
        }
        String xsltName = unitConfigurationInstance?.xsltName
        String preferredLanguage = ViewHelper.getCorrectPreferredLanguage(unitConfigurationInstance?.preferredLanguage)
        TimeZoneEnum timeZone = TimeZoneEnum.values().find {
            it.timezoneId == unitConfigurationInstance.preferredTimeZone
        }
        String Hl7 = Constants.BLANK_STRING
        if (xsltName) {
            Hl7 = grailsApplication.config.pv.app.e2b.xslts.options.get(xsltName)?.'isHl7' ?: Constants.BLANK_STRING
            if (!Hl7) {
                flash.error = message(code: "app.icsr.unit.configuration.invalid.xslt")
            }
        }

        if (xsltName || unitConfigurationInstance?.unitType == UnitTypeEnum.SENDER) {
            render view: "show", model: [unitConfigurationInstance: unitConfigurationInstance, icsrOrganizationTypeList: getAllIcsrOrganizationType(), Hl7: Hl7, timeZone: timeZone, preferredLanguage: preferredLanguage]
        }
    }
    
    @ReadOnly('pva')
    def edit(Long id) {
        String userLocale = userService.currentUser?.preference?.locale
        UnitConfiguration unitConfigurationInstance = id ? UnitConfiguration.read(id) : null
        Integer langId = sqlGenerationService.getPVALanguageId(userLocale ?: 'en')
        if (!unitConfigurationInstance) {
            notFound()
            return
        }
        List<UnitConfiguration> recipientWithList = UnitConfiguration.findAllByOrganizationTypeAndUnitRetired(IcsrOrganizationType.findByName(Constants.ICSR_UNIT_CONF_REGULATORY_AUTHORITY), false) - UnitConfiguration.get(unitConfigurationInstance?.id);
        if(unitConfigurationInstance?.registeredWith?.id && !recipientWithList.find{it.id == unitConfigurationInstance?.registeredWith.id}){
            recipientWithList.add(unitConfigurationInstance?.registeredWith)
        }
        List<String> allowedAttachments = []
        AllowedAttachment.withNewSession {
            allowedAttachments = unitConfigurationInstance?.allowedAttachments?.collect { [id: it, name: AllowedAttachment.findByIdAndLangId(it, langId)?.name]  }
        }
        List<String> organizationCountryList = []
        OrganizationCountry.withNewSession {
            organizationCountryList = OrganizationCountry.'pva'.findAllByLangDesc(userLocale).collect { it.name }
        }
        String xsltName = unitConfigurationInstance?.xsltName
        if (xsltName) {
            if (!(grailsApplication.config.pv.app.e2b.xslts.options.get(xsltName))) {
                flash.error = message(code: "app.icsr.unit.configuration.invalid.xslt")
            }
        }

        if (xsltName || unitConfigurationInstance?.unitType == UnitTypeEnum.SENDER) {
            render view: "edit", model: [unitConfigurationInstance: unitConfigurationInstance, icsrOrganizationTypeList: getAllIcsrOrganizationType(), recipientWithList: recipientWithList, organizationCountry : organizationCountryList, allowedAttachments: (allowedAttachments as JSON).toString()]
        }
    }

    def update() {
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        def unitConfigurationId = params.long('id');
        def unitConfigurationInstance

        if (unitConfigurationId) {
            unitConfigurationInstance = UnitConfiguration.get(unitConfigurationId)
            if (unitConfigurationInstance) {
                bindData(unitConfigurationInstance, params)

                if (unitConfigurationInstance?.allowedAttachments) {
                    List attachments = []
                    AllowedAttachment.'pva'.withNewSession {
                        unitConfigurationInstance.allowedAttachments.collect {
                            AllowedAttachment attachment = AllowedAttachment.'pva'.findByIdAndLangId(it, langId)
                            if (attachment) {
                                attachments.add(attachment.name)
                            } else {
                                attachments.add("${it} (Not Found)")
                            }
                        }
                    }
                    params.put('oldAttachments', attachments.sort().join(","))

                    unitConfigurationInstance.allowedAttachments.clear()

                }
                bindAllowedAttachments(unitConfigurationInstance, params)

                try {
                    CRUDService.update(unitConfigurationInstance)
                    bindEmail(unitConfigurationInstance)
                } catch (ValidationException ve) {
                    unitConfigurationInstance.errors = ve.errors
                    render view: "edit", model: [unitConfigurationInstance: unitConfigurationInstance, icsrOrganizationTypeList: getAllIcsrOrganizationType()]
                    return
                } catch (Exception ex) {
                    log.error("Unexpected error in unitConfiguration -> update", ex)
                    flash.error = message(code: "app.error.500")
                    redirect(action: 'edit')
                    return
                }
                flash.message = message(code: 'default.updated.message', args: [message(code: 'app.unitConfiguration.label.myInbox'), ""])
                redirect(action: "index")
                return
            }
        }
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.unitConfiguration.label.myInbox'), params.id])
        redirect (action: "index")
        return
    }

    def getAllIcsrOrganizationType() {
        return organizationTypeService.getAllIcsrOrganizationType()
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

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.unitConfiguration.label.myInbox'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    List<UnitConfiguration> getAllUnitRegWithBasedOnParam(){
        List<UnitConfiguration> unitConfigurationList =  unitConfigurationService.getAllUnitRegWithBasedOnParam(params.id);
        render ([items: unitConfigurationList.collect{[id:it.id, text:it.unitName]}, total_count: unitConfigurationList.size()] as JSON)
    }

    @Secured(["ROLE_ICSR_PROFILE_VIEWER", "ROLE_SYSTEM_CONFIGURATION"])
    def showOrganizationDetails(String orgName) {
        UnitConfiguration unitConfigurationInstance = UnitConfiguration.findByUnitName(orgName)
        String preferredLanguage = ViewHelper.getCorrectPreferredLanguage(unitConfigurationInstance?.preferredLanguage)
        if (!unitConfigurationInstance) {
            notFound()
            return
        }
        render view: "show", model: [unitConfigurationInstance: unitConfigurationInstance, preferredLanguage: preferredLanguage, isExecuted: true]
    }

    def exportJson() {
        List unitConfigsJsons = []
        UnitConfiguration.getAllUnitConfigurationBySearchString(null, 'registeredWith', 'desc').list().each { UnitConfiguration configuration ->
            Map propertiesMap = MiscUtil.getObjectProperties(configuration)
            propertiesMap.remove('registeredWith')
            propertiesMap.remove('organizationType')
            if (configuration.registeredWith)
                propertiesMap.put("registeredWith", MiscUtil.getObjectProperties(configuration.registeredWith, ['unitName', 'unitRegisteredId', 'unitType', 'id']))
            if (configuration.organizationType)
                propertiesMap.put("organizationType", MiscUtil.getObjectProperties(configuration.organizationType))
            unitConfigsJsons.add(propertiesMap)
        }
        def contentType = "application/octet-stream"
        def filename = "UnitConfigurations.json"
        response.setHeader("Content-Disposition", "attachment;filename=${filename}")
        render(contentType: contentType, text: unitConfigsJsons as JSON)
    }

    def importJson() {
        try {
            MultipartFile file = request.getFile('importJSONFile')
            String jsonString = utilService.readFileToString(file);
            importService.importUnitConfigsJson(jsonString)
            flash.message = message(code: 'unitconfigurations.json.import.success')
        } catch (ValidationException ve) {
            log.warn("Validation Error during import UnitConfiguration json")
            flash.error = ve.errors.allErrors.collect{ message(error: it)}.join("\n\n")
        } catch (Exception ex) {
            log.error("Failed to upload field profile json.", ex)
            flash.error = message(code: 'unitconfigurations.json.import.failure')
        }
        redirect(action: 'index')
    }

     def bindEmail(UnitConfiguration configurationInstance) {
        if (configurationInstance.email && !emailExists(configurationInstance.email)) {
            Email email = new Email(email: configurationInstance.email, description: configurationInstance.unitName)
            email.tenantId = Tenants.currentId() as Long
            try {
                CRUDService.update(email)
            }
            catch (e){
                throw e
            }
        }
    }

    private boolean emailExists(String email) {
        return (Email.countByEmailAndIsDeleted(email, false) || User.countByEnabledAndEmail(true, email))
    }

    private void bindAllowedAttachments(UnitConfiguration configurationInstance, Map params) {
        if(params.attachmentControl  && !params.attachmentControl.equals("dummy")) {
            if(params.attachmentControl.contains("dummy@!")) {
                params.attachmentControl = params.attachmentControl.split("dummy@!")[1]
            }
            params.attachmentControl.each {
                configurationInstance.addToAllowedAttachments(Long.parseLong(it))
            }
        }
    }

    def checkXsltIsHl7(String xsltName) {
        String isHl7 = false
        isHl7 = grailsApplication.config.pv.app.e2b.xslts.options.get(xsltName).'isHl7'
        render(contentType: 'application/json') {
            success true
            data isHl7
        }
    }
}
