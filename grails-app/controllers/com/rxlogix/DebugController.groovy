package com.rxlogix

import grails.converters.JSON
import net.bull.javamelody.internal.model.Collector
import net.bull.javamelody.internal.web.MonitoringController
import org.grails.plugins.console.Evaluation

class DebugController {

    static allowedMethods = [execute: 'POST']

    def consoleService

    def index() {
        render "Welcome to debugger only accessible via server"
    }

    def execute(String code, boolean autoImportDomains) {
        //Taken from console plugin code due to interceptor csrf console
        Evaluation eval = consoleService.eval(code, autoImportDomains, request)
        JSON.use('console') {
            render eval as JSON
        }
    }

    def monitoring() {
        def filterContext = request.servletContext
                .getAttribute('javamelody.filterContext')
        Collector collector = filterContext.getCollector()
        MonitoringController monitoringController = new MonitoringController(collector, null)

        monitoringController.doActionIfNeededAndReport(request, response,
                request.servletContext)
        return false
    }

}
