package com.rxlogix.public_api

import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.caseSeries.integration.CaseSeriesListDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import org.springframework.security.access.annotation.Secured

@Secured('permitAll')
class PublicCaseSeriesController {

    def caseSeriesService

    def generateExecutedCaseSeries(ExecutedCaseSeriesDTO executedCaseSeriesDTO) {
        ResponseDTO responseDTO = new ResponseDTO()
        ExecutedCaseSeries executedCaseSeries
        try {
            executedCaseSeries = caseSeriesService.createExecutedCaseSeries(executedCaseSeriesDTO)
            if (executedCaseSeries) {
                responseDTO.setSuccessResponse(executedCaseSeries.id)
            }
        } catch (Exception ex) {
            responseDTO.setFailureResponse(ex)
        }

        render(responseDTO as JSON)
    }

    def fetchConfiguredCaseSeriesList(String username, Integer offset, Integer max, String term) {
        if (!max) {
            max = 30
        }
        if (term) {
            term = term.trim()
        }
        User user = User.findByUsernameIlike(username)
        render([result: user ? caseSeriesService.getAllConfiguredCaseSeries(user, max, offset, term) : null, totalCount: user ? caseSeriesService.countAllConfiguredCaseSeries(user, term) : 0] as JSON)
    }

    def fetchExecutedCaseSeriesByConfigID(Long id) {
        Map data = caseSeriesService.fetchExecutedCaseSeriesIdByConfigID(id)
        render((data ?: []) as JSON)
    }

    def fetchExecutedCaseSeriesList(String username, String searchString, Integer max, Integer offset) {
        if (!max) {
            max = 30
        }
        if (searchString) {
            searchString = searchString.trim()
        }
        User user = User.findByUsernameIlike(username)
        render([result: user ? caseSeriesService.getAllExeuctedCaseSeriesByUser(user, max, offset, searchString) : null, totalCount: user ? caseSeriesService.countAllExeuctedCaseSeriesByUser(user, searchString) : 0 ] as JSON)
    }

    def fetchExecutedCaseSeriesByExID(Long id) {
        Map data = caseSeriesService.fetchExecutedCaseSeriesIdByExID(id)
        render((data ?: []) as JSON)
    }

    def updateSharedWithCaseSeries(CaseSeriesListDTO caseSeriesListDTO) {
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            caseSeriesService.updateExecutedCaseSeriesList(caseSeriesListDTO)
            responseDTO.setSuccessResponse(null, ViewHelper.getMessage("app.case.series.updated.success"))
        } catch (Exception ex) {
            responseDTO.setFailureResponse(ex)
        }
        render(responseDTO as JSON)
    }
}
