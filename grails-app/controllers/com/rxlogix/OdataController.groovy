package com.rxlogix

import com.rxlogix.odata.OdataRequestProxy
import com.rxlogix.odata.OdataUtils
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.server.api.ODataHttpHandler
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

@Secured(["isAuthenticated()"])
class OdataController {

    def odataService

    def index() {
        OdataRequestProxy req = new OdataRequestProxy(request)
        OdataUtils.setDsName(req.getDsName())
        if (!odataService.checkIfDSExist(req.getDsName())) {
            response.status = HttpStatusCode.NOT_FOUND.statusCode
            render([error: "Source doesn't exist"] as JSON)
            return
        }
        ODataHttpHandler handler = odataService.getODataHttpHandler()
        handler.process(req, response)
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
    }
}
