package com.rxlogix.security

import com.rxlogix.util.SecurityUtil
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.util.Holders
import org.springframework.security.core.Authentication
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.security.web.savedrequest.SavedRequest

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by gologuzov on 21.01.17.
 */
class PvrAuthenticationSuccessHandler extends AjaxAwareAuthenticationSuccessHandler {
    def csrfFilter

    @Override
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws ServletException, IOException {
        // The parameter forceDefaultRedirect is used for login from Jaspersoft Studio

        //To renew CSRF token once successfully authenticated
        if(Holders.config.getProperty('csrfProtection.enabled', Boolean)){
            CsrfToken generatedToken = csrfFilter.tokenRepository.generateToken(request);
            csrfFilter.tokenRepository.saveToken(generatedToken, request, response)
        }

        final SavedRequest cachedRequest = requestCache.getRequest(request, response);
        final String accept = request.getHeader("Accept");
        logger.trace("[PvrAuthenticationSuccessHandler] cachedRequest= ${cachedRequest} cachedRequest.getRedirectUrl()= ${cachedRequest?.getRedirectUrl()}")
        if (cachedRequest != null &&
                ("true".equalsIgnoreCase(request.getParameter("forceDefaultRedirect"))
                        || cachedRequest.getRedirectUrl().endsWith("central/index")
                        || (accept != null && accept.toLowerCase().contains("application/json")))) {
            // 1) forceDefaultRedirect=true means that client doesn't want to follow redirect to any stored URL
            // 2) cachedRequest.getRedirectUrl().endsWith("flow.html") -  cannot restore web flow state
            // 3) json is requested. No stored request should be used in this case. See also determineTargetUrl() below
            // redirection to default page in both cases
            requestCache.removeRequest(request, response)
        }

        String return_uri = request.session.getAttribute('return_uri')
        def spotfire_auth = request.session.getAttribute('spotfire_auth')

        if (return_uri && spotfire_auth) {
            def principle = authentication.getPrincipal()
            String username = principle.username
            String secret = Holders.config.getProperty('spotfire.token_secret')

            def new_url = ""
            if (return_uri.contains('?'))
                new_url = return_uri + "&auth_token=" + URLEncoder.encode(SecurityUtil.encrypt(secret, username), "UTF-8")
            else
                new_url = return_uri + "?auth_token=" + URLEncoder.encode(SecurityUtil.encrypt(secret, username), "UTF-8")

            response.sendRedirect(new_url)
        }

        super.onAuthenticationSuccess(request, response, authentication)
    }
}
