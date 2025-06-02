package com.rxlogix.csrf

import grails.util.Holders
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern

class CustomRequestMatcher implements RequestMatcher {

    private Pattern allowedMethods;

    private CustomRequestMatcher() {
        this.allowedMethods = Pattern.compile(/^(GET|HEAD|TRACE)$/);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String requestPath = request.getServletPath()
        return  !(
                    (allowedMethods.matcher(request.getMethod()).matches()) ||
                    (Holders.config.getProperty('csrfProtection.excludeURLPatterns', List, []).any{requestPath.contains(it)})
                )
    }
}
