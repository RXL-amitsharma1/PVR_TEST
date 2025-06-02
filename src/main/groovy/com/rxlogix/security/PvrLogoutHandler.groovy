package com.rxlogix.security

import groovy.transform.CompileStatic
import org.springframework.security.core.Authentication
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.web.authentication.logout.LogoutHandler

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CompileStatic
class PvrLogoutHandler implements LogoutHandler {

    SessionRegistry sessionRegistry;

    @Override
    void logout(HttpServletRequest httpServletRequest,
                HttpServletResponse httpServletResponse,
                Authentication authentication) {
        //We need to remove from User session from SessionRegistry to make sure session removed off
        this.sessionRegistry.removeSessionInformation(httpServletRequest.getSession().getId())
    }
}
