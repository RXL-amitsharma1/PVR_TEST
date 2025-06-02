package com.rxlogix

import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import org.springframework.web.multipart.MultipartFile

class ImportJsonService {

    def seedDataService
    def grailsApplication
    def PVCMIntegrationService
    UserService userService

    AjaxResponseDTO importJSON(MultipartFile file, String method) {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        InputStream fis = new BufferedInputStream(file.getInputStream())
        User adminUser = userService?.currentUser ?: seedDataService.getApplicationUserForSeeding()

        try {
            Tuple2<List<String>, List<String>> list = seedDataService."${method}"(fis, adminUser, true)
            String successMessage = ""
            String failureMessage = ""
            if (list.getV1()?.size() > 0) {
                successMessage = ViewHelper.getMessage("app.load.import.success", list.getV1() as String)
            }
            if (list.getV2()?.size() > 0) {
                failureMessage = ViewHelper.getMessage("app.load.import.fail", list.getV2() as String)
            }
            if (successMessage != "" && failureMessage != "") {
                responseDTO.setSuccessResponse([success: successMessage, failure: failureMessage])
            } else if (failureMessage != "" && successMessage == "") {
                responseDTO.setFailureResponse(failureMessage)
            } else {
                responseDTO.setSuccessResponse([success: successMessage])
                if (grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM) && method.equals("fetchSuperQuery")) {
                    PVCMIntegrationService.invokeRoutingConditionAPI()
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace()
            responseDTO.setFailureResponse(e, ViewHelper.getMessage("controlPanel.json.import.failure"))
        }
        finally {
            fis?.close()
        }
        return responseDTO
    }
}
