package interceptor

import com.rxlogix.CustomMessageService
import com.rxlogix.UserService
import com.rxlogix.config.SuperQuery
import com.rxlogix.user.User
import groovy.transform.CompileStatic

import static org.springframework.http.HttpStatus.NOT_FOUND

@CompileStatic
class QueryInterceptor {

    UserService userService
    CustomMessageService customMessageService

    QueryInterceptor(){
        match(controller: "query", action: "*")
                .except(action: "index")
                .except(action: "create")
                .except(action: "load")
                .except(action: "saveJSONQueries")
                .except(action: "save")
                .except(action: "getFieldsValue")
                .except(action: "getAllKeywords")
                .except(action: "getStringOperators")
                .except(action: "getNumOperators")
                .except(action: "getDateOperators")
                .except(action: "getValuelessOperators")
                .except(action: "getAllFields")
                .except(action: "possibleValues")
                .except(action: "possiblePaginatedValues")
                .except(action: "extraValues")
                .except(action: "queryExpressionValuesForQuery")
                .except(action: "customSQLValuesForQuery")
                .except(action: "queryExpressionValuesForQuerySet")
                .except(action: "viewExecutedQuery")
                .except(action: "importExcel")
                .except(action: "favorite")
                .except(action: "ajaxReportFieldSearch")
                .except(action: "validateValue")
                .except(action: "userReportFieldsOptsBySource")
                .except(action: "userDefaultReportFieldsOpts")
                .except(action: "getEmbaseOperators")

    }

    boolean before() {
        User currentUser = (User) userService.getUser()
        Long id = params.id as Long
        SuperQuery query = SuperQuery.get(id)

        if (!query) {
            flash.message = customMessageService.getMessage('default.not.found.message')
            redirect(action: "index", model: [status: NOT_FOUND])
            return false
        } else if (query.isDeleted) {
            flash.warn = customMessageService.getMessage("app.query.warn.isDeleted")
            redirect(action: "index")
            return false
        }

        if (!currentUser.isAdmin()) {
            if (query.isViewableBy(currentUser))
                return true
            else {
                flash.warn = customMessageService.getMessage("app.warn.noPermission")
                redirect(action: "index")
                return false
            }
        }
        true
    }
}
