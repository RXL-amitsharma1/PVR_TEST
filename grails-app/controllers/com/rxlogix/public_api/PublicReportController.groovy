package com.rxlogix.public_api

import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.reports.integration.ExecutedConfigurationSharedWithDTO
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.security.access.annotation.Secured

@Secured('permitAll')
class PublicReportController {

    def reportService

    def updateSharedWith(ExecutedConfigurationSharedWithDTO executedConfigurationSharedWithDTO) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(executedConfigurationSharedWithDTO.exConfigId)
        ResponseDTO responseDTO = new ResponseDTO()
        if (executedReportConfiguration) {
            try {
                reportService.updateExecutedConfiguration(executedReportConfiguration, executedConfigurationSharedWithDTO.sharedWithUsers, executedConfigurationSharedWithDTO.sharedWithGroups)
                responseDTO.setSuccessResponse(executedReportConfiguration.id)
            } catch (Exception e) {
                responseDTO.setFailureResponse(e)
            }
        } else {
            responseDTO.setMessage("Executed Report with id ${executedConfigurationSharedWithDTO.exConfigId} not found.")
        }
        render(responseDTO as JSON)
    }
}
