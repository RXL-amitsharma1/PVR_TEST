package com.rxlogix.security;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutResponseValidator;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.HttpSessionLogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;


public final class RxSaml2LogoutResponseFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(getClass());

    private final RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;

    private final Saml2LogoutResponseValidator logoutResponseValidator;

    private final LogoutSuccessHandler logoutSuccessHandler;

    private Saml2LogoutRequestRepository logoutRequestRepository = new HttpSessionLogoutRequestRepository();

    private RequestMatcher logoutRequestMatcher = new AntPathRequestMatcher("/logout/saml2/slo");


    public RxSaml2LogoutResponseFilter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver,
                                       Saml2LogoutResponseValidator logoutResponseValidator, LogoutSuccessHandler logoutSuccessHandler) {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
        this.logoutResponseValidator = logoutResponseValidator;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!this.logoutRequestMatcher.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (request.getParameter(Saml2ParameterNames.SAML_RESPONSE) == null) {
            chain.doFilter(request, response);
            return;
        }

        Saml2LogoutRequest logoutRequest = this.logoutRequestRepository.removeLogoutRequest(request, response);
        if (logoutRequest == null) {
            this.logger.trace("Did not process logout response since could not find associated LogoutRequest");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to find associated LogoutRequest");
            return;
        }
        this.logoutSuccessHandler.onLogoutSuccess(request, response, null);
    }

    public void setLogoutRequestMatcher(RequestMatcher logoutRequestMatcher) {
        Assert.notNull(logoutRequestMatcher, "logoutRequestMatcher cannot be null");
        this.logoutRequestMatcher = logoutRequestMatcher;
    }

    public void setLogoutRequestRepository(Saml2LogoutRequestRepository logoutRequestRepository) {
        Assert.notNull(logoutRequestRepository, "logoutRequestRepository cannot be null");
        this.logoutRequestRepository = logoutRequestRepository;
    }


}

