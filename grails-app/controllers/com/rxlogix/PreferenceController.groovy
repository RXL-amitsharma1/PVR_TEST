package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.helper.LocaleHelper
import com.rxlogix.user.AIEmailPreference
import com.rxlogix.user.Preference
import com.rxlogix.user.PVCEmailPreference
import com.rxlogix.user.PVQEmailPreference
import com.rxlogix.user.ReportRequestEmailPreference
import com.rxlogix.user.User
import grails.gorm.transactions.ReadOnly
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import com.rxlogix.enums.ReportThemeEnum
import org.grails.datastore.mapping.multitenancy.web.SessionTenantResolver
import org.springframework.web.servlet.support.RequestContextUtils
import grails.gorm.transactions.Transactional

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Secured('isAuthenticated()')
class PreferenceController {

    static allowedMethods = [update: "POST"]


    def userService
    def springSecurityService
    def CRUDService

    def index() {
        User user = userService.getUser()
        Preference preference = user?.preference
        render(view: "index", model: [currentLocale: LocaleHelper.convertLocaleToMap(getSessionLocale()), theme: ReportThemeEnum.searchByName(preference?.theme)?.name,
                                      locales      : LocaleHelper.buildLocaleListAsPerUserLocale(preference?.locale.toString()), theUserTimezone: preference?.timeZone, actionItemEmail: preference?.actionItemEmail, reportRequestEmail: preference?.reportRequestEmail, pvcEmail: preference?.pvcEmail, pvqEmail: preference?.pvqEmail])
    }

    @Transactional
    def update() {
        User user = userService.getUser()
        if (!params.timeZone) {
            flash.warn = message(code: 'app.preference.timeZone.select')
            redirect(controller: 'preference')
            return
        }
        if (user == null) {
            notFound()
            return
        }

        Preference preferenceInstance = user.preference
        preferenceInstance.timeZone = params.timeZone
        preferenceInstance.locale = getLocaleFromParam(params.language)
        preferenceInstance.theme = params.theme

        AIEmailPreference actionItemEmailPreference = preferenceInstance?.actionItemEmail
        if (!actionItemEmailPreference) {
            actionItemEmailPreference = new AIEmailPreference(preference: preferenceInstance)
            CRUDService.instantSaveWithoutAuditLog(actionItemEmailPreference)
        }

        ReportRequestEmailPreference reportRequestEmailPreference = preferenceInstance?.reportRequestEmail
        if (!reportRequestEmailPreference) {
            reportRequestEmailPreference = new ReportRequestEmailPreference(preference: preferenceInstance)
            CRUDService.instantSaveWithoutAuditLog(reportRequestEmailPreference)
        }

        PVCEmailPreference pvcEmailPreference = preferenceInstance?.pvcEmail
        if (!pvcEmailPreference) {
            pvcEmailPreference = new PVCEmailPreference(preference: preferenceInstance)
            CRUDService.instantSaveWithoutAuditLog(pvcEmailPreference)
        }

        PVQEmailPreference pvqEmailPreference = preferenceInstance?.pvqEmail
        if (!pvqEmailPreference) {
            pvqEmailPreference = new PVQEmailPreference(preference: preferenceInstance)
            CRUDService.instantSaveWithoutAuditLog(pvqEmailPreference)
        }

        bindActionItemEmailPreference(actionItemEmailPreference)          //to bind params with actionItemEmail
        bindReportRequestEmailPreference(reportRequestEmailPreference)    //to bind params with reportRequestEmail
        bindPVCEmailPreference(pvcEmailPreference)        //to bind params with pvcEmail
        bindPVQEmailPreference(pvqEmailPreference)       //to bind params with pvqEmail

        try {
            user.markDirty()
            CRUDService.update(user)
            preferenceInstance = user.preference
            setSession(preferenceInstance)
        } catch (ValidationException ve) {
            render view: "index", model: [preferenceInstance: preferenceInstance]
            return
        }


        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.preference'), ""])
                redirect(view: "index")
            }
            '*' { respond preferenceInstance, [status: OK] }
        }


    }

    //Method to bind params with actionItemEmailInstance
    private bindActionItemEmailPreference(AIEmailPreference actionItemEmailInstance) {
        actionItemEmailInstance.creationEmails = params.containsKey('createAI')
        actionItemEmailInstance.updateEmails = params.containsKey('updateAI')
        actionItemEmailInstance.jobEmails = params.containsKey('AIJobEmails')
    }

    //Method to bind params with reportRequestEmailInstance
    private bindReportRequestEmailPreference(ReportRequestEmailPreference reportRequestEmailInstance) {
        reportRequestEmailInstance.creationEmails = params.containsKey('createReportRequest')
        reportRequestEmailInstance.updateEmails = params.containsKey('updateReportRequest')
        reportRequestEmailInstance.deleteEmails = params.containsKey('deleteReportRequest')
        reportRequestEmailInstance.workflowUpdate = params.containsKey('reportRequestWorkflow')
    }

    //Method to bind params with pvcEmailInstance
    private bindPVCEmailPreference(PVCEmailPreference pvcEmailInstance) {
        pvcEmailInstance.assignedToMe = params.containsKey('pvcAssignedToMe')
        pvcEmailInstance.assignedToMyGroup = params.containsKey('pvcAssignedToMyGroup')
        pvcEmailInstance.workflowStateChange = params.containsKey('pvcWorkflowStateChange')
    }

    //Method to bind params with pvqEmailInstance
    private bindPVQEmailPreference(PVQEmailPreference pvqEmailInstance) {
        pvqEmailInstance.assignedToMe = params.containsKey('pvqAssignedToMe')
        pvqEmailInstance.assignedToMyGroup = params.containsKey('pvqAssignedToMyGroup')
        pvqEmailInstance.workflowStateChange = params.containsKey('pvqWorkflowStateChange')
    }

    @ReadOnly
    def updateCurrentTenant(Long tenantId) {
        Tenant tenant = userService.currentUser.tenants.find { it.id == tenantId }
        if (tenant) {
            session[SessionTenantResolver.ATTRIBUTE] = (tenant.id as Integer)
            flash.message = message(code: 'tenant.current.changed.success', args: [tenant.name])
        } else {
            flash.error = message(code: 'tenant.current.changed.error')
        }
        if (!request.getHeader('referer') || request.getHeader('referer').contains('/updateCurrentTenant')) {
            redirect(controller: 'dashboard')
        } else {
            redirect(url: request.getHeader('referer'))
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'accessControlGroup.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private Locale getLocaleFromParam(def languagecode) {
        if (languagecode) {
            def (language, country) = languagecode.tokenize('_')
            if (country == null) {
                return new Locale(language)
            } else {
                return new Locale(language, country)
            }
        }
    }

    private def setSession(Preference preference) {
        session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'] = preference.locale
        session['user.preference.timeZone'] = TimeZone.getTimeZone(preference?.timeZone)
    }

    // Do we really need this??
    private Locale getSessionLocale() {
        return session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' ?: RequestContextUtils.getLocale(request)
    }

    //For integration with PV-UI plugin
    public loadTheme() {
        Preference preference = userService.getUser().preference
        if (!preference) {
            render([status: 401] as JSON)
            return
        }
        String theme = ReportThemeEnum.searchByName(preference?.theme)?.displayName
        render([status: 200, theme: theme] as JSON)

    }

    def updateRODSimilarCasesCheck() {
        Preference preference = userService.getUser().preference
        if (params?.isChecked)
            preference.checkSimilarCases = Boolean.parseBoolean(params?.isChecked)
        try {
            CRUDService.update(preference)
            return "Ok"
        } catch (Exception e) {
            log.error("Error occurred while updating ROD similar cases checkbox preference", e)
            return "Error"
        }
    }

    def updateUserPreferences() {
        if (!params?.key) throw new IllegalArgumentException("Key parameter is mandatory")
        if (!params?.value) throw new IllegalArgumentException("Value parameter is mandatory")
        Preference preference = userService.getUser().preference
        try {
            if (!preference.userPreferences) preference.userPreferences = "{}"
            Map json = JSON.parse(preference.userPreferences)
            json[params.key.toString()] = params.value.toString()
            preference.userPreferences = json.toString();
            CRUDService.update(preference)
            return "Ok"
        } catch (Exception e) {
            log.error("Error occurred while updating user preferences", e)
            return "Error"
        }
    }

    String getUserPreferences() {
        if (!params?.key) throw new IllegalArgumentException("Key parameter is mandatory")
        Preference preference = userService.getUser().preference

        try {
            Map json = JSON.parse(preference.userPreferences ? preference.userPreferences : "{}")
            if (!json[params.key]) {
                log.warn("User preferences not found by key: ${params.key.toString()}")
                render([success: true, data: "{}"] as JSON)
            }
            render([success: true, data: json[params.key.toString()]] as JSON)
        } catch (Exception e) {
            log.error("Error occurred while getting user preferences", e)
            render([success: false, message: message(code: 'default.server.error.message')] as JSON)
        }
    }

}
