package interceptor

import grails.converters.JSON
import org.springframework.http.HttpStatus


class TenantInterceptor {

    def customMessageService

    TenantInterceptor() {
        match(uri: '/public/api/icsr/**').excludes(uri: '/public/api/icsr/updateMdnAxway').excludes(uri: '/public/api/icsr/updateAckAxway')
    }

    boolean before() {
        if (!params.tenantId) {
            response.status = HttpStatus.PRECONDITION_FAILED.value()
            Map responseMap = [
                    message: customMessageService.getMessage('icsr.tenantId.error'),
                    status : HttpStatus.PRECONDITION_FAILED.value(),
                    errors : null
            ]
            render(contentType: "application/json", responseMap as JSON)
            return false
        }
        return true
    }
}
