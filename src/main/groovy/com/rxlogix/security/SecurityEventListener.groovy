package com.rxlogix.security

import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.audit.AuditTrail
import grails.core.GrailsApplication
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ua_parser.Client
import ua_parser.Parser
import javax.servlet.http.HttpServletRequest

@Slf4j
class SecurityEventListener implements ApplicationListener<ApplicationEvent> {

    def customMessageService
    GrailsApplication grailsApplication

    @Override
    void onApplicationEvent(ApplicationEvent event) {

        if (!(event instanceof AbstractAuthenticationEvent)) {
            return
        }

        AuditTrail.withNewTransaction { status ->
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                final String userAgentHeader = request.getHeader("user-agent")
                List browserAndDeviceDetails = getBrowserAndDeviceDetails(userAgentHeader)
                String userTimezone = ""
                String userIpAddress = ""

                if (request.getHeader("X-Forwarded-For") != null) {
                    //for Environmens with load balancers
                    userIpAddress = request.getHeader("X-Forwarded-For")
                } else {
                    userIpAddress = request.getRemoteAddr()
                }
                if (event instanceof AbstractAuthenticationFailureEvent) {
                    //      boolean accountLocked = false
                    if (event.exception instanceof BadCredentialsException) {
                        User user = User.findByUsernameIlike(event.source.principal)
                        if (user) {
                            user.badPasswordAttempts++
                            if (hasExceededIncorrectLoginAttempts(user)) {
                                user.accountLocked = true
                            }
                            //Don't funnel this thru the CRUDService
                            if (user.accountLocked && user.isDirty('accountLocked')) {
//                                accountLocked = true
                                log.trace("${user.username} account has been locked by application")
                            }
                            user.save()
                            userTimezone = user.preference?.timeZone
                        }
                    }

                    WebAuthenticationDetails details = null
                    if (event.source?.details instanceof WebAuthenticationDetails) {
                        details = event.source.details
                    }
                    String username = event?.source?.principal instanceof String ?
                            event?.source?.principal : event?.source?.principal?.username

                    if (username) {
                        AuditTrail auditLog = new AuditTrail(category: AuditTrail.Category.LOGIN_FAILED.toString(),
                                applicationName: Holders.config.getProperty('grails.plugin.auditLog.applicationName'),
                                username: username, entityName: AuditTrail.Category.LOGIN_FAILED.displayName,
                                description: event?.exception?.message, userIpAddress: userIpAddress, moduleName: Constants.OTHER_STRING,
                                browser: browserAndDeviceDetails[0], device: browserAndDeviceDetails[1], timeZone: userTimezone)

                        //Don't funnel this thru the CRUDService
                        auditLog.save()
                    }
                    log.warn("Login failure for user: {} webdetails: [IP: {}, SessionId: {}]", event?.source?.principal?.toString(), details?.remoteAddress, details?.sessionId)

//                    if (accountLocked) {
//                        AuditTrail auditLogLocked = new AuditTrail(category: AuditTrail.Category.UPDATE.toString(),
//                                applicationName: Holders.config.grails.plugin.auditLog.applicationName,
//                                username: username,entityName: AuditTrail.Category.LOGIN_FAILED.displayName,
//                                description: "Account of ${username} has been locked",userIpAddress:userIpAddress,moduleName: Constants.OTHER_STRING,
//                                browser: browserAndDeviceDetails[0], device: browserAndDeviceDetails[1],timeZone: userTimezone)
//
//                        log.warn("Login failure for user: {} webdetails: [IP: {}, SessionId: {}]", username, details?.remoteAddress, details?.sessionId)
//
//                        //Don't funnel this thru the CRUDService
//                        auditLogLocked.save()
//                    }
                }

                if (event instanceof AuthenticationSuccessEvent) {
                    User user = User.findByUsernameIlike(event.source.principal.username)
                    user.badPasswordAttempts = 0
                    //We need to capture last login not current login that's why added this.
                    user.lastToLastLogin = user.lastLogin
                    user.lastLogin = new Date()
                    String fullname = user.fullName

                    user.save()
                    userTimezone = user.preference?.timeZone
                    WebAuthenticationDetails details = null
                    if (event.source?.details instanceof WebAuthenticationDetails) {
                        details = event.source.details
                    }

                    AuditTrail auditLog = new AuditTrail(category: AuditTrail.Category.LOGIN_SUCCESS.toString(),
                            username: event?.source?.principal?.username, fullname: fullname, applicationName: Holders.config.getProperty('grails.plugin.auditLog.applicationName'),
                            description: "Login Successful", userIpAddress: userIpAddress,
                            entityName: AuditTrail.Category.LOGIN_SUCCESS.displayName, moduleName: Constants.OTHER_STRING,
                            browser: browserAndDeviceDetails[0], device: browserAndDeviceDetails[1], timeZone: userTimezone)

                    log.trace("Login Success for user: {} webdetails: [IP: {}, SessionId: {}]", event?.source?.principal?.username, details?.remoteAddress, details?.sessionId)

                    auditLog.save()
                }
            } catch (e) {
                log.error("Serious error during security event ${event}: ", e)
                status.setRollbackOnly()
            }
        }

    }

    Boolean hasExceededIncorrectLoginAttempts(User user) {
        Integer incorrectLoginAttemptsAllowed = Holders.config.getProperty('bruteforce.incorrectLoginAttemptsAllowed', Integer)
        return incorrectLoginAttemptsAllowed ? (user.badPasswordAttempts >= incorrectLoginAttemptsAllowed) : false
    }

    private List getBrowserAndDeviceDetails(String userAgent) {
        String browserDetails = null
        String deviceDetails = null
        List details = []
        Parser uaParser = new Parser()
        Client client = uaParser.parse(userAgent);
        if (client != null) {
            browserDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor
            deviceDetails = client.os.family
            details.add(browserDetails)
            details.add(deviceDetails)
        }
        return details
    }
}
