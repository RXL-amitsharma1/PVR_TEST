package reports

import com.rxlogix.dto.AjaxResponseDTO
import grails.plugin.springsecurity.annotation.Secured

import javax.sql.DataSource

//https://localhost:8443/reports/health/ping/alihoih398yewkj3298hfeiub
@Secured('permitAll')
class HealthController {

    static scope = "singleton"
    static allowedMethods = [index: ["GET", "POST"]]

    def index() {
        if (!grailsApplication.config.getProperty('endpoints.enabled', Boolean, false)) {
            log.warn("Actuator endpoints are not enabled. so can't give full health details.")
            forward(action: 'ping')
            return
        }
        forward(uri: '/manage/health')
    }

    def ping() {
        def resp = new AjaxResponseDTO<>()
        resp.message = "App Server is UP"
        resp.data = 'OK'
        render resp.toAjaxResponse()
    }
}
