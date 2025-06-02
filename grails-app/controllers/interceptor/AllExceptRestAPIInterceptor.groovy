package interceptor

import com.rxlogix.user.CustomUserDetails
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import org.grails.datastore.mapping.multitenancy.web.SessionTenantResolver

class AllExceptRestAPIInterceptor {
    SpringSecurityService springSecurityService

    AllExceptRestAPIInterceptor() {
        matchAll().excludes(controller: "odata", action: "*")
                .excludes(controller: "serverInfoRest", action: "*")
                .excludes(controller: "repositoryRest", action: "*")
                .excludes(controller: "services", action: "*")
                .excludes(controller: "login", action: "*")
                .excludes(controller: "logout", action: "*")
                .excludes(controller: "health", action: "*")
                .excludes(controller: "errors", action: "*")
                .excludes(controller: "actionItemRestController", action: "*")
                .excludes(controller: "caseSeriesRest", action: "*")
                .excludes(controller: "configurationRest", action: "*")
                .excludes(controller: "executedCaseSeriesRest", action: "*")
                .excludes(controller: "executionStatusRest", action: "*")
                .excludes(controller: "fieldProfileRest", action: "*")
                .excludes(controller: "notificationRest", action: "*")
                .excludes(controller: "periodicReportConfigurationRest", action: "*")
                .excludes(controller: "queryRest", action: "*")
                .excludes(controller: "reportFieldRest", action: "*")
                .excludes(controller: "reportResultDataRest", action: "*")
                .excludes(controller: "reportResultRest", action: "*")
                .excludes(controller: "publicReportsBuilderRest", action: "*")
                .excludes(controller: "reportSubmissionRest", action: "*")
                .excludes(controller: "reportTemplateRest", action: "*")
                .excludes(controller: "userGroupRest", action: "*")
                .excludes(controller: "userRest", action: "*")
                .excludes(controller: "publicCaseSeries", action: "*")
                .excludes(controller: "publicConfiguration", action: "*")
                .excludes(controller: "publicQueryRest", action: "*")
                .excludes(controller: "publicTemplateRest", action: "*")
                .excludes(controller: "publicUserRestController", action: "*")
                .excludes(controller: "publicIcsrAttachment", action: "*")
                .excludes(controller: "publicUnitConfigurationController", action: "*")
                .excludes(controller: "publicReportController", action: "*")
                .excludes(controller: "publicIcsrRestController", action: "*")
    }

    boolean before() {
        /**
         * Lookup user timezone and locale for first time and set
         */

        if (!session.'user.preference.timeZone' || !session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' || (Holders.config.getProperty('pvreports.multiTenancy.enabled', Boolean) && !session[SessionTenantResolver.ATTRIBUTE])) {
            def currentUser = springSecurityService.currentUser
            if (currentUser instanceof CustomUserDetails) {
                currentUser = User.findByUsername(((CustomUserDetails) currentUser).getUsername())
            }
            Preference preference = currentUser?.preference
            if (!session['user.preference.timeZone'] && preference?.timeZone) {
                //@TODO translate the string .timeZone into format TimeZone understands
                session['user.preference.timeZone'] = TimeZone.getTimeZone(preference.timeZone)
            }

            if (!session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' && preference?.locale) {
                session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'] = preference.locale
            }

            //Todo need to add logic here once tenant domain integration happens.
            if (Holders.config.get('pvreports.multiTenancy.enabled') && currentUser && !session[SessionTenantResolver.ATTRIBUTE]) {
                session[SessionTenantResolver.ATTRIBUTE] = (currentUser.tenants.first().id as Integer)
            }

        }
        true
    }
}
