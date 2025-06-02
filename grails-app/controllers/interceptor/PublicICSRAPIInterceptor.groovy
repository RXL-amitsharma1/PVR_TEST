package interceptor

import grails.converters.JSON
import grails.util.Holders
import org.apache.http.HttpStatus
import com.rxlogix.user.User

/*
This API Interceptor will be used for all public APIs for ICSR
This will check mainly the Public token and User roles
 */
class PublicICSRAPIInterceptor {

    def customMessageService

    static PUBLIC_API_TOKEN = "PVR_PUBLIC_TOKEN"
    public final String TRN_FAIL = "TXN_FAIL"

    PublicICSRAPIInterceptor() {
        match(uri: "/public/api/icsr/*")
    }

    boolean before() {
        try {
            String publicApiToken = Holders.config.getProperty('rxlogix.pvreports.publicApi.token')
            String uri = request.forwardURI
            List<String> actionURIs = ["/icsr/manual/case", "/icsr/profiles", "/icsr/profile/templates", "/icsr/checkAvailableDevice", "/icsr/listDevices", "/icsr/listAuthorizationType", "/icsr/listApprovalNumber", "/icsr/case/reEvaluate", "/icsr/scheduleCase", "/icsr/checkPreviousVersionTransmitted", "/icsr/loadTransmitModal", "/icsr/transmitCase", "/icsr/loadSubmissionForm", "/icsr/submitCase", "/icsr/nullifyReport", "/icsr/deleteCase", "/icsr/saveLocalCp", "/icsr/generateReport", "/icsr/listEmail", "/icsr/addEmailAll", "/icsr/sendEmailForIcsr", "/icsr/regenerateCase", "/icsr/previewCaseData"]
            List<String> adminActionURIs = ["/icsr/loadStatusForm", "/icsr/updateCaseStatus"]
            List<String> listingURIs = ["/icsr/listIcsrCases", "/icsr/statusList", "/icsr/listProfiles", "/icsr/listAuthorizationTypeFilter", "/icsr/caseHistoryDetails", "/icsr/caseSubmissionHistoryDetails", "/icsr/downloadAckFile", "/icsr/downloadICSR", "/icsr/config", "/icsr/updateIcsrPreference", "/icsr/getIcsrPreference","/icsr/listJustification", "/icsr/downloadAttachFile"]
            Map responseMap = [:]
            if (publicApiToken && publicApiToken == request.getHeader(PUBLIC_API_TOKEN)) {
                if (uri.endsWith("/icsr/updateMdnAxway") || uri.endsWith("/icsr/updateAckAxway")) {
                    return true
                }
                String username = request.getJSON()?.username ?: params.username
                User user = User.findByUsernameIlike(username)
                if (adminActionURIs.any { uri.endsWith(it.trim()) } && user && user.isICSRAdmin()) {
                    return true
                } else if (actionURIs.any { uri.endsWith(it.trim()) } && user && user.hasICSRActionRoles()) {
                    return true
                } else if (listingURIs.any { uri.endsWith(it.trim()) } && user && user.hasICSRAccess()) {
                    return true
                } else {
                    responseMap = [
                            result       : null,
                            resultCode   : org.springframework.http.HttpStatus.UNAUTHORIZED.value(),
                            resultMsg    : customMessageService.getMessage('icsr.public.api.user.unauthorized') as String,
                            resultStatus : TRN_FAIL
                    ]
                }
            } else {
                responseMap = [
                        result       : null,
                        resultCode   : org.springframework.http.HttpStatus.UNAUTHORIZED.value(),
                        resultMsg    : customMessageService.getMessage('default.public.token.error.message') as String,
                        resultStatus : TRN_FAIL
                ]
            }
            response.status = org.springframework.http.HttpStatus.UNAUTHORIZED.value()
            render(contentType: "application/json", responseMap as JSON)
            return false
        } catch (Exception ex) {
            ex.printStackTrace()
            render([result: null, resultCode: HttpStatus.SC_INTERNAL_SERVER_ERROR, resultMsg: "Internal exception", resultStatus: TRN_FAIL] as JSON)
            response.status = HttpStatus.SC_INTERNAL_SERVER_ERROR
            return false
        }
    }

}
