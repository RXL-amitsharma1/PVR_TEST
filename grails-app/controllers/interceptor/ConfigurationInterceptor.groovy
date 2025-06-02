package interceptor

import com.rxlogix.CustomMessageService
import com.rxlogix.UserService
import com.rxlogix.config.BaseConfiguration
import com.rxlogix.config.Configuration
import com.rxlogix.user.User
import groovy.transform.CompileStatic

import static org.springframework.http.HttpStatus.NOT_FOUND

@CompileStatic
class ConfigurationInterceptor {

    UserService userService
    CustomMessageService customMessageService

    ConfigurationInterceptor(){
        match(controller: "configuration", action: "*")
                .except(action: "viewExecutedConfig")
                .except(action: "index")
                .except(action: "create")
                .except(action: "createTemplate")
                .except(action: "createQuery")
                .except(action: "save")
                .except(action: "run")
                .except(action: "delivered")
                .except(action: "runOnce")
                .except(action: "addSection")
                .except(action: "saveSection")
                .except(action: "saveOnDemandSection")
                .except(action: "importExcel")
                .except(action: "load")
                .except(action: "saveJSONConfigurations")
                .except(action: "favorite")
                .except(action: "validateValue")
                .except(action: "listTemplates")
                .except(action: "bulkUpdateConfig")
                .except(action: "importBulkExcel")
                .except(action: "exportToExcel")
                .except(action: "createFromTemplate")
                .except(action: "updateSectionAndRunAjax")
                .except(action: "listPvqCfg")
    }

    boolean before() {
        User currentUser = (User) userService.getUser()
        Long id = params.id as Long
        Configuration configurationInstance = Configuration.get(id)

        if (!configurationInstance) {
            flash.error = customMessageService.getMessage('default.not.found.message')
            redirect(controller: "configuration", action: "index", model: [status: NOT_FOUND])
            return false
        } else if (configurationInstance.isDeleted) {
            flash.warn = customMessageService.getMessage("app.configuration.isDeleted")
            redirect(action: "index")
            return false
        }


        if (configurationInstance.isViewableBy(currentUser))
            true
        else {
            flash.warn = customMessageService.getMessage("app.warn.noPermission")
            redirect(action: "index")
            false
        }

    }
}
