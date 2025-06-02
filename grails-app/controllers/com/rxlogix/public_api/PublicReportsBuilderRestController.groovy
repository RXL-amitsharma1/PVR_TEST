package com.rxlogix.public_api

import com.rxlogix.Constants
import com.rxlogix.config.*
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.reports.integration.ExecutedConfigurationDTO
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/**
 * API exposed for report generation.
 */
@Secured(['permitAll'])
class PublicReportsBuilderRestController {

    static responseFormats = ['json', 'xml', 'text']

    final String spotfireReportName = "Spotfire Case Form Request"
    final String spotfireQueryName = "Spotfire Case And Version Query"

    def reportsBuilderService
    def templateService
    def dynamicReportService
    def CRUDService
    def executedConfigurationService
    def queryService

    /**
     * Method to import the configurations into pv reports.
     * @return
     */
    def importConfiguration(ExecutedConfigurationDTO executedConfigurationDTO) {
        log.info("Executing importConfiguration")
        ResponseDTO responseDTO = new ResponseDTO()
        String callbackURL = executedConfigurationDTO.callbackURL
        ExecutedConfiguration executedConfiguration

        try {
            session.signalUsername = executedConfigurationDTO.ownerName
            session.signalFullname = User.findByUsernameIlike(executedConfigurationDTO.ownerName)?.fullName ?: "PVS User"
            executedConfiguration = reportsBuilderService.createExecutedConfiguration(executedConfigurationDTO)
            log.info("ExecutedConfiguration created ID: ${executedConfiguration?.id}")
            if (executedConfiguration) {
                responseDTO.setSuccessResponse(executedConfiguration.id)
                reportsBuilderService.createExecutionStatus(executedConfiguration, ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION, callbackURL)
            }
        } catch (Exception ex) {
            log.error("Error during publicReportsBuilderRest -> importConfiguration", ex)
            responseDTO.setFailureResponse(ex)
        }

        render(responseDTO as JSON)
    }

    /**
     * API to return the bytes of the generated report file.
     * @return : Byte[] of file
     * @input : executedConfigurationId and report format
     */
    def exportReportForSignal(Long executedConfigurationId, String format) {
        log.info("Calling exportReportForSignal for EC: ${executedConfigurationId} and format: $format")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigurationId)
            Map reportFormat = [outputFormat: format]
            File reportFile = dynamicReportService.createMultiTemplateReport(executedConfiguration, reportFormat)
            responseDTO.data = [bytes: reportFile.getBytes(), format: format]
        } catch (Exception e) {
            log.error("Error during publicReportsBuilderRest -> exportReportForSignal", e)
            responseDTO.status = false
        }
        render(responseDTO as JSON)
    }


    /**
     * API to soft delete executed Case Series corresponding to Alert in PVS and return String as the response .
     * @return :  String (success (successCount) out of (totalCountIds) )
     * @input :  String (Comma seperated Ids (caseSeries) and String (Comma seperated Ids(executedReportConfiguration))
     */
    def softDeleteConfiguration(String ids, String reportId){
        log.info("softDeleteConfiguration called from PVS")

        Map response = [:]

        // For Executed Case Series Deletion
        response['status'] = "ExecutedCaseSeries -> " + softDelete(ids, ExecutedCaseSeries)

        // For Executed Report Configuration Deletion
        response['status'] += " ExecutedReportConfiguration -> " + softDelete(reportId, ExecutedReportConfiguration)

        render(response as JSON)
    }

    String softDelete(String ids, def elemClass){

        if (ids.isEmpty()){
            log.warn("During publicReportsBuilderRest -> softDeleteConfiguration, Input string is Empty.")
            return "Empty String Passed"
        }

        List<Long> Ids = []

        try {
            Ids = ids.split(",").collect{ Long.valueOf(it.trim())}

        } catch (NumberFormatException e){
            log.warn("During publicReportsBuilderRest -> softDeleteConfiguration, Input string does not contain intended data.")
            return "Input string does not contain intended data."
        }

        int totalCount = Ids.size()

        int successCount = 0

        for (id in Ids){
            log.info("Soft Deleting ${elemClass} corresponding to ${id}.")
            def executedConfig = elemClass.get(id)

            if (!executedConfig){
                log.warn("During publicReportsBuilderRest -> softDeleteConfiguration, ${elemClass} with id - ${id} doesn't exist.")
                continue
            }

            try {
                CRUDService.softDelete(executedConfig, elemClass == ExecutedCaseSeries ? executedConfig.seriesName : executedConfig.reportName, "Alert corresponding to ${elemClass} with id - ${id} has been deleted.")
                successCount++
            }
            catch (Exception ex) {
                log.error("Error while deleting ${elemClass} with id - ${id}", ex)
            }
        }

        return "Success ${successCount} out of ${totalCount}."
    }

    /**
     * Returns report output status
     * @return : json with status
     * @input : executedReportId
     */
    def getReportOutputStatus(Long id) {
        log.info("Executing getReportOutputStatus")

        ExecutionStatus exstatus = ExecutionStatus.findByEntityId(id)

        if (!exstatus) {
            render([reportExecutionStatus: "REPORT_NOT_FOUND"] as JSON)
        } else {
            render([reportExecutionStatus: exstatus.executionStatus.name()] as JSON)
        }

    }
    /**
     * Returns report output for executedReportId in selected format
     * @return : binary file data
     * @input : executedReportId and format:PDF,XLSX,DOCX,PPTX,HTML
     */
    def getReportOutput(Long id, String format) {
        log.info("Executing getReportOutput")
        String outputFormat = format ? format.toUpperCase() : "PDF"
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(id)
        if (!executedConfiguration) {
            render([reportExecutionStatus: "REPORT_NOT_FOUND"] as JSON)
            return
        }
        if (executedConfiguration.status != ReportExecutionStatusEnum.COMPLETED) {
            render([message: "Report is not completed!", reportExecutionStatus: executedConfiguration.status.name()] as JSON)
        } else {
            Map reportFormat = [outputFormat: outputFormat]
            File reportFile = dynamicReportService.createMultiTemplateReport(executedConfiguration, reportFormat)
            render(file: reportFile.getBytes(), contentType: dynamicReportService.getContentType(outputFormat), fileName: dynamicReportService.getReportNameAsFileName(executedConfiguration) + "." + outputFormat)
        }
    }

    /**
     * Creates and executs report with given template for fiven case.
     * @return : json with Executed configuration id and status SUCCUSS for succuss call ane status:ERROR with error message is error occurs
     * @input : Case Number, Case Version, Tenant Id and template name to use in report
     */
    def createCaseForm(String cases, Long tenantId, String templateName) {
        log.info("Executing createCaseForm")
        Map response = [:]
        if (!cases.matches(/^[a-zA-Z0-9:;]*\u0024/)) {
            response = [status: "ERROR", errorMessage: "Illegal characters in request parameters! "]
            render(response as JSON)
            return
        }
        try {
            User owner = User.findByUsername("admin")
            ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(
                    reportName: spotfireReportName,
                    dateRangeType: DateRangeType.findAllByIsDeletedAndNameNotEqual(false, Constants.EVENT_RECEIPT_DATE, [sort: 'sortOrder', order: 'asc'])[0],
                    executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(executedAsOfVersionDate: new Date(), dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: BaseDateRangeInformation.MIN_DATE),
                    owner: owner,
                    excludeNonValidCases: false,excludeDeletedCases: false,
                    scheduleDateJSON: reportsBuilderService.getRunOnceScheduledDateJson(owner),
                    executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [], sharedWith: [owner]),
                    createdBy: owner.username,
                    modifiedBy: owner.username,
                    numOfExecutions: ExecutedConfiguration.countByReportNameAndOwner(spotfireReportName, owner) + 1,
                    sourceProfile: SourceProfile.sourceProfilesForUser(owner)[0],
                    nextRunDate: new Date(),
                    tenantId: tenantId
            )

            ReportTemplate template = ReportTemplate.findByNameAndOriginalTemplateIdAndIsDeleted(templateName, 0, false)
            ReportTemplate executedTemplate
            if (template instanceof ITemplateSet) {
                executedTemplate = executedConfigurationService.createReportTemplate(template)
            } else {
                Long templateid = ReportTemplate.getLatestExRptTempltByOrigTempltId(template.id).get()
                executedTemplate = ReportTemplate.get(templateid)
            }

            SuperQuery query = SuperQuery.findByNameAndOriginalQueryIdAndIsDeleted(spotfireQueryName, 0, false)
            SuperQuery executedQuery = query ? SuperQuery.get(SuperQuery.getLatestExQueryByOrigQueryId(query.id).get()) : createSpotfireQuery(owner)

            ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                    executedTemplate: executedTemplate,
                    title: template.name,
                    executedQuery: executedQuery,
                    createdBy: owner.username,
                    modifiedBy: owner.username,
                    executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(), dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: BaseDateRangeInformation.MIN_DATE))

            executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)

            ReportResult result = new ReportResult(executionStatus: ReportExecutionStatusEnum.SCHEDULED, scheduledBy: owner)
            executedTemplateQuery.setReportResult(result)

            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: executedQuery)
            String whereClause = cases.split(";").collect {
                String[] parts = it.split(":")
                return "('" + parts[0] + "'," + parts[1] + ")"
            }.join(",")
            ParameterValue caseParam = new CustomSQLValue(key: ":caseAndVersionList", value: whereClause)
            executedQVL.addToParameterValues(caseParam)
            executedTemplateQuery.addToExecutedQueryValueLists(executedQVL)

            executedConfiguration.workflowState = WorkflowState.defaultWorkState
            CRUDService.save(executedConfiguration)


            log.info("ExecutedConfiguration created ID: ${executedConfiguration?.id}")
            if (executedConfiguration) {
                response = [status: "SUCCESS", id: executedConfiguration.id]
                reportsBuilderService.createExecutionStatus(executedConfiguration, ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION)
            }
        } catch (Exception ex) {
            log.error("Error during publicReportsBuilderRest -> importConfiguration", ex)
            response = [status: "ERROR", errorMessage: ex.message]
        }
        render(response as JSON)
    }

    CustomSQLQuery createSpotfireQuery(User owner) {
        CustomSQLQuery customSQLQuery = new CustomSQLQuery(
                name: spotfireQueryName,
                owner: owner,
                createdBy: owner.username,
                modifiedBy: owner.username,
                hasBlanks: true,
                originalQueryId: 0,
                parameterSize: 2,
                queryType: QueryTypeEnum.CUSTOM_SQL,
                JSONQuery: null,
                customSQLQuery: "where (cm.case_num, cm.version_num) in (:caseAndVersionList)"
        )

        CustomSQLValue customParam = new CustomSQLValue(key: ":caseAndVersionList");
        customSQLQuery.addToCustomSQLValues(customParam)
        CRUDService.save(customSQLQuery)
        queryService.createExecutedQuery(customSQLQuery)

    }
}
