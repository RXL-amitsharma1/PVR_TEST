package interceptor

import grails.converters.JSON
import grails.util.Holders
import org.grails.web.json.JSONElement


class CiomsExportInterceptor {

    CiomsExportInterceptor() {
        match(controller: "report", action: 'exportSingleCIOMS')
    }


    boolean before() {
        params.blinded = Holders.config.getProperty('ciomsI.blinded.flag', Boolean)
        params.privacy = Holders.config.getProperty('ciomsI.privacy.flag', Boolean)
        if (params.query) {
            // like this /report/exportSingleCIOMS?query = '{"casenumber":"16US00007747","version":2,"blinded":false,"privacy":true}'.bytes.encodeAsBase64().toString()
            try {
                JSONElement ciomsAdditional = JSON.parse(new String(params.query.decodeBase64()))
                params.blinded = ciomsAdditional.blinded
                params.privacy = ciomsAdditional.privacy
                params.caseNumber = ciomsAdditional.casenumber
                params.versionNumber = ciomsAdditional.version
            } catch (e) {
                log.warn("Failed to deserialize additional cioms due to ${e.message}")
            }
        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}