package interceptor

import grails.config.Config
import grails.converters.JSON
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus

@CompileStatic
class PublicRestInterceptor implements GrailsConfigurationAware {

    private static final String PUBLIC_API_TOKEN = "PVR_PUBLIC_TOKEN"

    String publicApiToken

    PublicRestInterceptor() {
        match(uri: '/public/api/**')
        match(controller: "executedCaseSeries", action: 'saveCaseSeriesForSpotfire')
    }

    void setConfiguration(Config cfg) {
        publicApiToken = cfg.getProperty('publicApi.token', String)
    }

    boolean before() {
        if (publicApiToken && publicApiToken != request.getHeader(PUBLIC_API_TOKEN)) {
            Map responseMap = [
                    message: "Unauthorized access!!",
                    status : HttpStatus.UNAUTHORIZED.value()
            ]
            render(contentType: "application/json", responseMap as JSON)
            return false
        }
        true
    }
}
