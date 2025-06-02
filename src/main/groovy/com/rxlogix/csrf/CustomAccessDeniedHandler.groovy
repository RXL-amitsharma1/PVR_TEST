package com.rxlogix.csrf

import groovy.util.logging.Slf4j
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.access.AccessDeniedHandlerImpl

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
public class CustomAccessDeniedHandler extends AccessDeniedHandlerImpl {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, accessDeniedException)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.sendRedirect(request.getContextPath() + "/login/authfail");
    }
}
