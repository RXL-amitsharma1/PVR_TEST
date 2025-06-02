package com.rxlogix

import grails.plugin.springsecurity.annotation.Secured
import java.security.SecureRandom

class PvAdminController {
    def executorThreadInfoService

    @Secured(['permitAll'])
    def index() {
        try {
            SecureRandom secureRandom = new SecureRandom()
            byte[] bytes = new byte[16]
            secureRandom.nextBytes(bytes)
            String randomTokenForAdmin = Base64.getEncoder().encodeToString(bytes)
            String originalSessionToken = RxCodec.encode(session.id)
            log.debug("Generated random token: ${randomTokenForAdmin}, Original session token: ${originalSessionToken}")

            executorThreadInfoService.addTempTokenForAdmin(randomTokenForAdmin, originalSessionToken)

            String pvAdminUrl = grailsApplication.config.app.pvadmin.url

            redirect(url: "${pvAdminUrl}/login?token=${randomTokenForAdmin}&app=PVR")
        } catch (Exception e) {
            log.error("Error occurred: ${e.message}", e)
            flash.message = "An error occurred while processing your request."
            redirect(controller: "dashboard", action: "index")
        }
    }
}


