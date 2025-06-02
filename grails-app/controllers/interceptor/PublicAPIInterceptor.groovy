package interceptor

import grails.converters.JSON
import grails.util.Holders
import org.apache.http.HttpStatus


class PublicAPIInterceptor {

    def customMessageService

    static PUBLIC_API_TOKEN = "PVR_PUBLIC_TOKEN"

    PublicAPIInterceptor() {
        match(uri: "/public/api/userRest/fetchUserDetail")
        match(uri: "/public/api/userRest/sendTeamsNotification")
        match(uri: "/public/api/createCaseForm")
        match(uri: "/public/api/getReportOutputStatus")
        match(uri: "/public/api/getReportOutput")
        match(uri: "/public/api/query/fetchQueriesByTag")
        match(uri: "/public/api/fetchRoutingConditions")
        match(uri: "/public/api/fetchResultForWorkflow")
        match(uri: "/public/api/reloadFieldDefinition")
        match(uri: "/public/api/rollbackFieldDefinition")
        match(uri: "/public/api/updatePrivacyFieldProfile")
    }

    boolean before() {
        try{
            String publicApiToken = Holders.config.getProperty('rxlogix.pvreports.publicApi.token')
            if(publicApiToken && publicApiToken==request.getHeader(PUBLIC_API_TOKEN)){
                return true
            }
            Map responseMap = [
                    message: customMessageService.getMessage('default.public.token.error.message') as String,
                    status : org.springframework.http.HttpStatus.UNAUTHORIZED.value()
            ]
            response.status = org.springframework.http.HttpStatus.UNAUTHORIZED.value()
            render(contentType: "application/json", responseMap as JSON)
            return false
        }  catch (Exception ex) {
            ex.printStackTrace()
            render([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, message: "Internal exception"] as JSON)
            response.status = HttpStatus.SC_INTERNAL_SERVER_ERROR
            return false
        }
    }

}
