/* Copyright 2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rxlogix

import com.rxlogix.user.sso.exception.SSOConfigurationException
import com.rxlogix.user.sso.exception.SSOUserDisabledException
import com.rxlogix.user.sso.exception.SSOUserLockedException
import com.rxlogix.user.sso.exception.SSOUserNotConfiguredException
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import org.springframework.security.web.csrf.InvalidCsrfTokenException
import org.springframework.security.web.csrf.MissingCsrfTokenException
import org.springframework.security.web.savedrequest.SavedRequest

import javax.servlet.http.HttpServletResponse

@Secured('permitAll')
class LoginController {

    static allowedMethods = [authAjax: 'POST', ajaxSuccess: 'POST']
    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index() {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {
            redirect action: 'auth', params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth() {

        def config = SpringSecurityUtils.securityConfig

        if (params.flashWarn) {
            flash.warn = params.flashWarn
        }

        if (springSecurityService.isLoggedIn() || Holders.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean)) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        String view = 'auth'
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        Integer sessionTimeOutInterval = Holders.config.getProperty('springsession.timeout.interval', Integer)

        render view: view, model: [postUrl: postUrl, sessionTimeOutInterval: sessionTimeOutInterval,
                                   usernameParameter: config.apf.usernameParameter, passwordParameter: config.apf.passwordParameter,
                                   rememberMeParameter: config.rememberMe.parameter]
    }

    /**
     * The redirect action for Ajax requests.
     */
    def authAjax() {
        response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Show denied page.
     */
    def denied() {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: 'full', params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full() {
        def config = SpringSecurityUtils.securityConfig
        if (Holders.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean)) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        Integer sessionTimeOutInterval = Holders.config.getProperty('springsession.timeout.interval', Integer)

        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication), postUrl: postUrl, sessionTimeOutInterval: sessionTimeOutInterval,
                        usernameParameter: config.apf.usernameParameter, passwordParameter: config.apf.passwordParameter]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail() {
        def exception = request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) ?: session[WebAttributes.AUTHENTICATION_EXCEPTION]
        if (Holders.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean)) {
            forward(action: 'ssoAuthFail')
            return
        }
        String msg = getFailMessage(exception, params.boolean('sessionInvalidated'))
        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
        } else {
            params.flashWarn = msg
            redirect action: 'auth', params: params
        }
    }

    private getFailMessage(exception, Boolean sessionInvalidated) {
        String msg = ''
        if (exception instanceof AccountExpiredException) {
            msg = message(code: "springSecurity.errors.login.expired")
        } else if (exception instanceof CredentialsExpiredException) {
            msg = message(code: "springSecurity.errors.login.passwordExpired")
        } else if (exception instanceof DisabledException) {
            msg = message(code: "springSecurity.errors.login.disabled")
        } else if (exception instanceof LockedException) {
            msg = message(code: "springSecurity.errors.login.locked")
        } else if (exception instanceof UsernameNotFoundException) {
            msg = message(code: "springSecurity.errors.login.fail")
        } else if (exception instanceof InvalidCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.invalid")
        } else if (exception instanceof MissingCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.missing")
        } else if (exception instanceof SessionAuthenticationException) {
            msg = message(code: "springSecurity.errors.login.multiple")
        } else if (sessionInvalidated) {
            msg = message(code: "springSecurity.errors.login.sessionInvalidated")
        } else {
            msg = message(code: "springSecurity.errors.login.fail")
            if (exception instanceof Throwable)
                log.warn("Login unknown exception", exception)
        }
        return msg
    }

    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess() {
        render([success: true, username: springSecurityService.authentication.name] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied() {
        render([error: 'access denied'] as JSON)
    }

    def ssoAuthFail() {
        def exception = request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) ?: session[WebAttributes.AUTHENTICATION_EXCEPTION]
        String msg = getSSOFailMessage(exception, params.boolean('sessionInvalidated'))
        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
            return
        }

        log.debug("Checking: ssoAuth Fail  -> SAML Exception(Error validating SAML message), Exception - ${exception?.message}")

        if (exception instanceof AuthenticationServiceException && exception?.message?.contains(Constants.SAML_ERROR_MSG)){
            log.debug("Redirecting to home page as session was expired due to ${msg}")
            redirect(uri: '/')
            return
        }
        render(view: '/errors/errorSSOAuth', model: [msg: msg])
    }

    private String getSSOFailMessage(exception, Boolean sessionInvalidated) {
        String msg = 'Error Occurred during SSO Authentication'
        if (exception instanceof AccountExpiredException) {
            msg = message(code: "springSecurity.errors.login.expired")
        } else if (exception instanceof CredentialsExpiredException) {
            msg = message(code: "springSecurity.errors.login.passwordExpired")
        } else if (exception instanceof InvalidCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.invalid")
        } else if (exception instanceof MissingCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.missing")
        } else if (exception instanceof SSOUserDisabledException || params.boolean('disabled')) {
            msg = message(code: 'springSecurity.errors.login.sso.disabled')
        } else if (exception instanceof SSOUserLockedException || params.boolean('locked')) {
            msg = message(code: 'springSecurity.errors.login.sso.locked')
        } else if (exception instanceof SSOUserNotConfiguredException || params.boolean('notfound')) {
            msg = message(code: 'springSecurity.errors.login.sso.notfound')
        } else if (exception instanceof SSOConfigurationException || params.boolean('configError')) {
            msg = message(code: 'springSecurity.errors.login.sso.config')
        } else if (exception instanceof SessionAuthenticationException) {
            msg = message(code: "springSecurity.errors.login.multiple")
        } else if (sessionInvalidated) {
            msg = message(code: "springSecurity.errors.login.sessionInvalidated")
        } else if (exception instanceof Throwable) {
            msg = exception.message
            log.warn("SSO Login unknown exception for the user", exception)
        }
        return msg
    }

    def securityAndPrivacyPolicy = {
        render (view: 'policy')
    }

    def setModule() {
        def req = session."SPRING_SECURITY_SAVED_REQUEST";
        String requestedUrl
        if (req) {
            requestedUrl = ((SavedRequest) req).redirectUrl
            if (requestedUrl.endsWith("reports") || requestedUrl.endsWith("reports/") ||
                    requestedUrl.endsWith("reports/dashboard") || requestedUrl.endsWith("reports/dashboard/") || requestedUrl.endsWith("reports/dashboard/index"))
                requestedUrl = null
        }
        if (params.module && !requestedUrl) {
            session.module = params.module
        }
        render "ok"
    }
}
