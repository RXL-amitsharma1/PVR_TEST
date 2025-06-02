package interceptor

import com.rxlogix.CustomMessageService
import com.rxlogix.config.PeriodicReportConfiguration
import groovy.transform.CompileStatic

import static org.springframework.http.HttpStatus.NOT_FOUND

@CompileStatic
class PeriodicReportInterceptor {

    CustomMessageService customMessageService

    PeriodicReportInterceptor() {
        match(controller: "periodicReport", action: "createTemplate")
        match(controller: "periodicReport", action: "createQuery")
        match(controller: "periodicReport", action: "view")
    }

    boolean before() {
        Long id = params.id as Long
        PeriodicReportConfiguration periodicReportConfiguration = PeriodicReportConfiguration.get(id)
        if (!periodicReportConfiguration && request.method == 'GET') {
            flash.error = customMessageService.getMessage('default.not.saved.message')
            redirect(controller: 'periodicReport', action: "index", model: [status: NOT_FOUND])
            return false
        }
        else if(periodicReportConfiguration && periodicReportConfiguration.isDeleted){
            flash.warn = customMessageService.getMessage("app.configuration.isDeleted")
            redirect(action: "index")
            return false
        }
        true
    }
}
