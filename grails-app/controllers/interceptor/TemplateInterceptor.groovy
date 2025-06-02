package interceptor

import com.rxlogix.config.ReportTemplate
import groovy.transform.CompileStatic
import com.rxlogix.CustomMessageService
import com.rxlogix.UserService
import com.rxlogix.user.User

import static org.springframework.http.HttpStatus.NOT_FOUND

@CompileStatic
class TemplateInterceptor {

    UserService userService

    TemplateInterceptor(){
        match(controller: "template", action: "*")
                .except(action: "index")
                .except(action: "viewExecutedTemplate")
                .except(action: "create")
                .except(action: "save")
                .except(action: "load")
                .except(action: "saveJSONTemplates")
                .except(action: "favorite")
                .except(action: "validateValue")
                .except(action: "userReportFieldsOptsBySource")
                .except(action: "userDefaultReportFieldsOpts")
                .except(action: "granularityForTemplate")
                .except(action: "reassessDateForTemplate")
                .except(action: "customSQLValuesForTemplate")
                .except(action: "setColumnNamesListFromSelectClause")


    }

    boolean before() {
        User currentUser = (User) userService.getUser()
        Long id = params.id as Long
        ReportTemplate template = ReportTemplate.get(id)

        if (!template) {
            redirect(action: "index", model: [status: NOT_FOUND], params: [messageKey: 'notFound'])
            return false
        } else if (template.isDeleted) {
            redirect(action: "index", params: [messageKey: 'isDeleted'])
            return false
        }

        if (!currentUser.isAdmin()) {
            if (template.isViewableBy(currentUser))
                return true
            else {
                redirect(action: "index", params: [messageKey: 'noPermission'])
                return false
            }
        }
        true
    }
}
