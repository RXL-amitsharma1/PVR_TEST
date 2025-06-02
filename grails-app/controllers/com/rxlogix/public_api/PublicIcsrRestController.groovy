package com.rxlogix.public_api

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.commandObjects.BulkCaseSubmissionCO
import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.Email
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrTemplateQuery
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.IcsrCaseSubmission
import com.rxlogix.config.IcsrCaseTracking
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.TemplateQuery
import com.rxlogix.customException.CaseScheduleException
import com.rxlogix.customException.CaseSubmissionException
import com.rxlogix.customException.InvalidCaseInfoException
import com.rxlogix.dto.ApiResponseDTO
import com.rxlogix.dto.Select2Item
import com.rxlogix.dto.Select2ResponseDTO
import com.rxlogix.enums.DistributionChannelEnum
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.mapping.IcsrCaseMessageQueue
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import groovy.json.JsonSlurper
//import io.swagger.annotations.Api
//import io.swagger.annotations.ApiImplicitParam
//import io.swagger.annotations.ApiImplicitParams
//import io.swagger.annotations.ApiOperation
//import io.swagger.annotations.ApiResponse
//import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.apache.commons.codec.binary.Base64
import com.rxlogix.user.Preference
import java.text.SimpleDateFormat
import com.rxlogix.config.StandardJustification
import com.rxlogix.Constants
import grails.async.Promises
import java.sql.SQLException
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import com.rxlogix.mapping.IcsrCaseProcessingQueueHist

//@Api(value = "/public/api/icsr", description = "Icsr Api's")
@Secured('permitAll')
class PublicIcsrRestController implements SanitizePaginationAttributes{

    def icsrScheduleService
    def sqlGenerationService
    def axwayService
    def userService
    def icsrCaseTrackingService
    def reportExecutorService
    def CRUDService
    def executedIcsrConfigurationService
    def icsrProfileAckService
    def reportSubmissionService
    def emailService
    def ldapService
    def icsrReportService
    def executorThreadInfoService
    def dynamicReportService

    public final String TRN_SUCCESS = "TXN_SUCCESS"
    public final String TRN_FAIL = "TXN_FAIL"
    public final String VIEWER = "VIEWER"
    public final String EDITOR = "EDITOR"
    public final String ADMIN = "ADMIN"

    def profileList(Integer tenantId, String term, Integer page, Integer max) {
        Tenants.withId(tenantId) {
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (term) {
                term = term?.trim()
            }
            List data = IcsrProfileConfiguration.getAllActiveIcsrProfiles(term).list(offset: Math.max(page - 1, 0) * max, max: max).collect {
                [id: it.id, text: "${it.reportName}", isJapan: it.isJapanProfile, isDevice: it.deviceReportable]
            }
            int totalCount = IcsrProfileConfiguration.getAllActiveIcsrProfiles(term).count()
            render([data: data, totalCount: totalCount] as JSON)
        }
    }

    def templateQueriesFor() {
        Integer tenantId = params.int('tenantId')
        Long profileId = params.long('profileId')
        if (!profileId) {
            render([items      : [],
                    totalCount: 0] as JSON)
            return
        }
        Tenants.withId(tenantId) {
            IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
            Select2ResponseDTO responseDTO = new Select2ResponseDTO()
            responseDTO.data = profileConfiguration?.templateQueries?.unique{it.templateId}?.collect {
                new Select2Item([id: it.id, text: it.template.name])
            } ?: []
            responseDTO.totalCount = profileConfiguration?.templateQueries?.size() ?: 0
            render(responseDTO as JSON)
        }
    }

    def reProcess() {
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String caseNumber = requestJSON.caseNumber
        Long version = requestJSON.version
        Locale requestLocale = new Locale(params.language?:"en")

        if (!caseNumber || !version) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        Tenants.withId(tenantId) {
            try {
                icsrScheduleService.addForReEvaluate(caseNumber, version)
                render([result: caseNumber, resultCode: HttpStatus.ACCEPTED.value(), resultMsg: ViewHelper.getMessage('icsr.reEvaluate.manual.case.success', [caseNumber + " v" + version] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            } catch (InvalidCaseInfoException ie) {
                log.error("Failed to add case for reprocess due to invalid data: $ie.message")
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.reEvaluate.manual.case.invalid', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            } catch (Exception ex) {
                log.error("Exception occurred in API while adding reprocessing case for $caseNumber : $version ", ex)
                render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.reEvaluate.manual.case.failed', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            }
        }
    }

    def manualScheduleCase() {

        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        Long profileId = requestJSON.profileId
        Long templateQueryId = requestJSON.templateQueryId
        String caseNumber = requestJSON.caseNumber
        Long version = requestJSON.version
        Integer dueInDays = requestJSON.dueInDays
        Boolean isExpedited = requestJSON.isExpedited
        String username = requestJSON.username
        String deviceId = requestJSON.deviceId
        Long authorizationTypeId = requestJSON.authorizationTypeId
        String approvalNumber = requestJSON.approvalNumber
        Locale requestLocale = new Locale(params.language?:"en")

        User user = User.findByUsername(username)
        Tenants.withId(tenantId) {
            log.info("Manual Schedule Case Request Recieved from PVCM for ${caseNumber} - ${version} for Profile ID ${profileId}")
            IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
            TemplateQuery templateQuery = profileConfiguration?.templateQueries?.find{it.id == templateQueryId}
            Long approvalId = approvalNumber ? approvalNumber.split(Constants.CASE_VERSION_SEPARATOR).first().toLong() : -1
            Long reportCategoryId = approvalNumber ? approvalNumber.split(Constants.CASE_VERSION_SEPARATOR).last().toLong() : -1
            if (!profileConfiguration || !caseNumber || !version || !templateQuery || dueInDays < 0) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.add.manual.case.invalid', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            if (deviceId == "-1" && profileConfiguration.deviceReportable) {
                render([result: null, resultCode: HttpStatus.NOT_ACCEPTABLE.value(), resultMsg: ViewHelper.getMessage('icsr.profile.manual.schedule.no.Device.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            final Date scheduleDate = new Date()
            try {
                icsrScheduleService.addCaseToSchedule(profileConfiguration, templateQuery, caseNumber, version, dueInDays, isExpedited, username, user?.fullName, deviceId, authorizationTypeId, approvalId, reportCategoryId, null, scheduleDate)
                render([result: caseNumber, resultCode: HttpStatus.CREATED.value(), resultMsg: ViewHelper.getMessage('icsr.add.manual.case.success', [caseNumber + " v" + version, profileConfiguration.reportName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                return
            } catch (CaseScheduleException cse) {
                log.error("Failed to add manual case as this is already processed for this profile")
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.add.manual.case.failure', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            } catch (InvalidCaseInfoException ie) {
                log.error("Failed to add manual case due to invalid data: $ie.message")
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.add.manual.case.invalid', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            } catch (Exception ex) {
                log.error("Exception occurred in API while adding manual case for $caseNumber : $version , $profileId , $templateQueryId", ex)
                render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.add.manual.case.failed', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            }
        }
    }

    def checkAvailableDevice(String caseNumber, Long version, Long profileId, Integer tenantId) {
        Locale requestLocale = new Locale(params.language?:"en")
        if (!caseNumber || !version || !profileId || !tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        log.info("Checking Available Devices for ${caseNumber} : ${version} and ${profileId}")

        Tenants.withId(tenantId) {
            IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.read(profileId)
            try {
                String caseId = sqlGenerationService.fetchCaseIdFromSource(caseNumber, version)
                if (caseId && icsrProfileConfiguration.deviceReportable) {
                    List caseProdList = sqlGenerationService.checkCaseProductList(caseId, version, tenantId)
                    // -1 denotes that case does not have any device
                    if (caseProdList.any { it.PROD_HASH_CODE == "-1" }) {
                        render([result: null, resultCode: HttpStatus.NOT_FOUND.value(), resultMsg: ViewHelper.getMessage('icsr.case.no.device', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                        return
                    } else {
                        render([result: caseNumber, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.device.found.for.case.profile', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                        return
                    }
                } else {
                    render([result: null, resultCode: HttpStatus.NOT_FOUND.value(), resultMsg: ViewHelper.getMessage('icsr.profile.not.device.reportable', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                    return
                }
            } catch (Exception e) {
                log.error("Failed to check available devices for case : " + e)
                render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            }
        }
    }

    def listDevices(String caseNumber, Long version, Integer tenantId, Long profileId) {
        Tenants.withId(tenantId) {
            try {
                String caseId = sqlGenerationService.fetchCaseIdFromSource(caseNumber, version)
                IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.get(profileId)
                if (caseId && profileId) {
                    Select2ResponseDTO responseDTO = new Select2ResponseDTO()
                    List caseProdList = []
                    if (icsrProfileConfiguration.deviceReportable) {
                        caseProdList = sqlGenerationService.checkCaseProductList(caseId, version, tenantId)
                    } else {
                        caseProdList = sqlGenerationService.checkProductList(caseId, version, tenantId)
                    }

                    responseDTO.data = caseProdList.findAll { it.PRODUCT_NAME }?.collect {
                        new Select2Item([id: it.PROD_HASH_CODE, text: it.PRODUCT_NAME])
                    } ?: []
                    responseDTO.totalCount = responseDTO.data?.size() ?: 0
                    render(responseDTO as JSON)
                    return
                } else {
                    sendResponse(HttpStatus.NOT_FOUND.value(), message(code: 'icsr.case.data.not.found').toString())
                    return
                }
            } catch (Exception e) {
                log.error("Unable to list devices for case : " + e)
                sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message(code: 'default.server.error.message').toString())
            }
        }
    }

    JSON listAuthorizationType() {
        Long profileId = params.long('profileId')
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)
        try {
            if (profileId) {
                IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.get(profileId)
                Select2ResponseDTO responseDTO = new Select2ResponseDTO()

                if (icsrProfileConfiguration.authorizationTypes) {
                    List authList = []
                    Integer langId = sqlGenerationService.getPVALanguageId(language ?: 'en')
                    List<AuthorizationType> authorizationTypeList = []
                    AuthorizationType.'pva'.withNewTransaction {
                        authorizationTypeList = AuthorizationType.'pva'.findAllByIdInListAndLangId(icsrProfileConfiguration.authorizationTypes, langId)
                    }
                    authorizationTypeList.collect { authList.add(id: it.id, text: it.name) }
                    responseDTO.data = authList
                    responseDTO.totalCount = responseDTO.data?.size() ?: 0
                    render(responseDTO as JSON)
                    return
                } else {
                    sendResponse(HttpStatus.NOT_FOUND.value(), ViewHelper.getMessage('icsr.case.data.not.found', null, null, requestLocale))
                    return
                }
            } else {
                sendResponse(HttpStatus.NOT_FOUND.value(), ViewHelper.getMessage('icsr.case.data.not.found', null, null, requestLocale))
                return
            }
        } catch (Exception e) {
            log.error("Unable to list authorization type for profile ${profileId} : " + e)
            sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ViewHelper.getMessage('default.server.error.message', null, null, requestLocale))
        }
    }

    JSON listApprovalNumber(String caseNumber, Long version, Integer tenantId, Long profileId, String deviceId, Long authorizationTypeId) {
        Tenants.withId(tenantId) {
            try {
                String caseId = sqlGenerationService.fetchCaseIdFromSource(caseNumber, version)
                IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.get(profileId)
                if (caseId && profileId && deviceId && authorizationTypeId) {
                    Select2ResponseDTO responseDTO = new Select2ResponseDTO()
                    List approvalList = sqlGenerationService.checkApprovalNumber(caseId as Long, version, deviceId as String, icsrProfileConfiguration.recipientOrganization.organizationCountry, authorizationTypeId as Long, icsrProfileConfiguration.isJapanProfile, tenantId, icsrProfileConfiguration.multipleReport)
                    if (approvalList.any { it.prodHashCode == "-1" }) {
                        sendResponse(HttpStatus.NOT_FOUND.value(), message(code: 'icsr.case.data.not.found').toString())
                        return
                    } else {
                        responseDTO.data = approvalList.collect {
                            new Select2Item([id: it.id, text: it.text])
                        } ?: []
                        responseDTO.totalCount = responseDTO.data?.size() ?: 0
                        render(responseDTO as JSON)
                        return
                    }
                } else {
                    sendResponse(HttpStatus.NOT_FOUND.value(), message(code: 'icsr.case.data.not.found').toString())
                    return
                }
            } catch (Exception e) {
                log.error("Unable to list Approval Number : " + e)
                sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message(code: 'default.server.error.message').toString())
            }
        }
    }

    def updateMdn(String fileName, String status){
        try{
            axwayService.setTransmitDateForFile(fileName, status)
            render([status: 200] as JSON)
        } catch (Exception e) {
            log.error("Error while making MDN Entry in Table : " + e)
            render([status: 400] as JSON)
        }
    }

    def updateAck(String fileName, String status){
        try{
            axwayService.setAckReceiveDateForFile(fileName, status)
            render([status: 200] as JSON)
        } catch (Exception e) {
            log.error("Error while making ACK Entry in Table : " + e)
            render([status: 400] as JSON)
        }
    }

    private def sendResponse(stat, msg, errors = null) {
        response.status = stat
        ApiResponseDTO responseDTO = new ApiResponseDTO(status: stat, message: msg, errors: errors)
        render(contentType: "application/json", responseDTO as JSON)
    }


    def listIcsrCaseTracking(String username, Integer tenantId) {
        def requestJSON = request.getJSON()
        Integer max = requestJSON.length
        Integer offset = requestJSON.start
        String state = requestJSON.state
        String sort = requestJSON.sort
        String order = requestJSON.direction
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)

        User user = User.findByUsername(username)

        if (!tenantId) {
            render([data: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }

        Tenants.withId(tenantId) {

            if (!max) {
                max = 50
            }
            if (!offset) {
                offset = 0
            }
            if (!sort || sort == "dateCreated") {
                sort = "generationDate"
            }
            if (!order) {
                order = "asc"
            }
            if (!state) {
                state = "GENERATED"
            }

            String caseNumber = requestJSON.caseNumber
            Long exIcsrTemplateQueryId = requestJSON.exIcsrTemplateQueryId
            Long exIcsrProfileId = requestJSON.exIcsrProfileId
            Long versionNumber = requestJSON.versionNumber

            String searchString = requestJSON.searchString
            String tableFilter = requestJSON.tableFilter

            params.put('searchString', searchString)
            params.put('tableFilter', tableFilter)

            sanitize(params)

            try {

                LibraryFilter filter = new LibraryFilter(params, user, IcsrCaseTracking.class)
                def searchDataJson = FilterUtil.convertToJsonFilter(params.searchData).findAll { it.value.val }
                List<Closure> searchData = FilterUtil.buildCriteriaForColumnFilter(searchDataJson, user)

                String icsrCaseStateEnum = null
                if (state != IcsrCaseStateEnum.ALL.name()) {
                    icsrCaseStateEnum = state
                }

                def data = []
                int recordsTotal = 0
                int recordsFilter = 0

                (data, recordsTotal, recordsFilter) = icsrCaseTrackingService.fetchIcsrData(filter, caseNumber, versionNumber, exIcsrProfileId, exIcsrTemplateQueryId, icsrCaseStateEnum, searchData, max, offset, sort, order, user, language)
                render([data: data, recordsFiltered: recordsFilter, recordsTotal: recordsTotal, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.list.fetch.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                return
            } catch (Exception e) {
                log.error("Unable to fetch ICSR Case list : " + e)
                e.printStackTrace()
                render([data: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            }
        }
    }

    def listCaseSubmissionHistoryDetails(String profileName, Long exIcsrTemplateQueryId, String caseNumber) {
        Long versionNumber = params.long('versionNumber')
        String language = params.language ?: 'en'
        Locale requestLocale = new Locale(language)
        profileName = URLDecoder.decode(profileName, "UTF-8")
        log.info("fetching case submission history details for profile - ${profileName}, case - ${caseNumber} and version - ${versionNumber}")
        if (!profileName || !exIcsrTemplateQueryId || !caseNumber || !versionNumber) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrTemplateQuery.read(exIcsrTemplateQueryId).usedConfiguration
            Long processedReportIdList = null
            List caseSubmissionList = []
            IcsrCaseSubmission.'pva'.withNewSession {
                processedReportIdList = IcsrCaseSubmission.fetchIcsrCaseSubmissionByCaseNoAndVersionNo(profileName, exIcsrTemplateQueryId, caseNumber, versionNumber).get()
                caseSubmissionList = IcsrCaseSubmission.findAllByProcessedReportId(processedReportIdList, [sort: 'e2bProcessId', order: 'asc'])
            }
            List<Map> data = caseSubmissionList.findAll { it }.collect { icsrCaseTrackingService.toCaseSubmissionHistoryMap(it, executedIcsrProfileConfiguration.preferredTimeZone, language) }
            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.submission.history.fetch.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to Open Case Submission History Modal : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }

    }

    def listCaseHistoryDetails(String caseNumber) {
        Long versionNumber = params.long('versionNumber')
        String language = params.language ?: 'en'
        Locale requestLocale = new Locale(language)
        if (!caseNumber || !versionNumber) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            List<IcsrCaseSubmission> caseHistoryList = []
            IcsrCaseSubmission.'pva'.withNewSession {
                caseHistoryList = IcsrCaseSubmission.findAllByCaseNumberAndVersionNumber(caseNumber, versionNumber, [sort: 'lastUpdateDate', order: 'asc'])
            }
            List<Map> data = caseHistoryList.collect { icsrCaseTrackingService.toCaseSubmissionHistoryMap(it, null, language) }
            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.fetch.succes', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to Open Case History Modal : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def downloadAckFile() {
        String filename = params.ackFileName
        String username = params.username
        Locale requestLocale = new Locale(params.language?:"en")
        User user = User.findByUsernameIlike(username)
        try {
            File file=new File(filename)
            if(!file.exists()){
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('app.report.file.not.found', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            byte[] fileBytes = file.bytes
            String encodedFile = Base64.encodeBase64String(fileBytes)
            String type = dynamicReportService.getContentType(file.name)
            response.contentType = type
            response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
            render([result: [file: encodedFile, fileName: file.name], resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage("app.label.success", null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
        } catch(Exception e) {
            log.error("Unknown Error occurred in downloadAckFile ", e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def downloadStatusFile(){
        Long e2bProcessId = params.long('e2bProcessId')
        Locale requestLocale = new Locale(params.language?:"en")
        IcsrCaseSubmission icsrCaseSubmission = null
        String filename = null
        byte[] data = null

        if (!e2bProcessId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try{
            IcsrCaseSubmission.'pva'.withNewSession {
                icsrCaseSubmission = IcsrCaseSubmission.findByE2bProcessId(e2bProcessId)
            }
            if (!icsrCaseSubmission) {
                render([result: null, resultCode: HttpStatus.NOT_FOUND.value(), resultMsg: ViewHelper.getMessage('app.label.submission.not.found', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            filename = icsrCaseSubmission.submissionFilename
            data = icsrCaseSubmission.submissionDocument
            if (!filename || !data) {
                render([result: null, resultCode: HttpStatus.NOT_FOUND.value(), resultMsg: ViewHelper.getMessage('app.label.file.not.found', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            String encodedFile = Base64.encodeBase64String(data)
            String contentType = dynamicReportService.getContentType(filename)
            response.contentType = contentType
            response.setHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
            render([result: [file: encodedFile, fileName: filename], resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage("app.label.success", null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
        } catch(Exception e) {
            log.error("Error occurred in downloadStatusFile for e2bProcessId=${e2bProcessId}", e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def listStatus() {
        Locale currLocale = new Locale(params.language?:"en")
        try {
            List<Map> data = ViewHelper.getIcsrCaseStateEnumI18n(currLocale).collect {
                [id: it.id, text: it.name]
            }
            data = data - null
            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.status.list.success', null, null, currLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to fetch Status list : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, currLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def listCase(Integer tenantId, String term, Integer page, Integer max) {
        Tenants.withId(tenantId) {
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (term) {
                term = term?.trim()
            }
            int offset = Math.max(page - 1, 0) * max
            try {
                Select2ResponseDTO responseDTO = new Select2ResponseDTO()
                responseDTO.data = sqlGenerationService.getManualCaseList(term, offset, max, false).collect {
                    new Select2Item([id: "${it.caseNumber}${Constants.CASE_VERSION_SEPARATOR}${it.version}", text: "${it.caseNumber}-${it.version}"])
                }
                responseDTO.totalCount = sqlGenerationService.getManualCaseList(term, offset, max, true).size()
                render(responseDTO as JSON)
                return
            } catch (Exception e) {
                log.error("Unable to list cases : " + e)
                sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message(code: 'default.server.error.message').toString())
            }
        }

    }

    def deleteCaseIcsr() {
        def requestJSON = request.getJSON()
        Long exIcsrTemplateQueryId = requestJSON.exIcsrTemplateQueryId
        String caseNumber = requestJSON.caseNumber
        Long versionNumber = requestJSON.versionNumber
        String justificationList = requestJSON.justification
        Long justificationId = requestJSON.justificationId
        String username = requestJSON.username
        Integer tenantId = requestJSON.tenantId
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)
        User user = User.findByUsername(username)
        if (!exIcsrTemplateQueryId || !caseNumber || !versionNumber || !tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        log.info("Deleting case with caseNumber = " + caseNumber)

        IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
            return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }

        if (!icsrCaseTracking) {
            log.error("No Record found with CaseNumber ${caseNumber} and versionNumber ${versionNumber}")
            render([result: null, resultCode: HttpStatus.NOT_FOUND.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.delete.case.exception', [caseNumber, versionNumber] as Object[], null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        String profileName = icsrCaseTracking.profileName
        String state = icsrCaseTracking.e2BStatus
        String prodHashCode = icsrCaseTracking.prodHashCode
        Long profileId = icsrCaseTracking.profileId
        Long processedRptId=icsrCaseTracking.processedReportId

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object justificationMap = jsonSlurper.parseText(justificationList)

        try {
            IcsrCaseTracking icsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, versionNumber)
            }
            Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
            String exTempltName=ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance?.exIcsrTemplateQueryId)?.executedTemplate?.name
            reportExecutorService.removeCaseFromTracking(profileName, caseNumber, versionNumber, state, tenantId, processedRptId, icsrCaseTrackingInstance?.exIcsrTemplateQueryId, icsrCaseTrackingInstance?.recipient, icsrCaseTrackingInstance?.dueDate, justificationId, justificationMap.en, justificationMap.ja)
            String justification = language == 'en' ? justificationMap.en : justificationMap.ja
            String extraJustification = language == 'en' ? (justificationMap.ja?.trim() ? justificationMap.ja : null) : (justificationMap.en?.trim() ? justificationMap.en : null)
            AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, [:], oldValues
                    , Constants.AUDIT_LOG_DELETE,ViewHelper.getMessage("auditLog.entityValue.icsr.delete", caseNumber, versionNumber, icsrCaseTrackingInstance?.profileName, exTempltName, icsrCaseTrackingInstance?.recipient, justification, extraJustification ?: ''), ("" + System.currentTimeMillis()), username, user?.fullName)
            render([result: caseNumber, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.delete.case.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Error Deleting case with CaseNumber ${caseNumber} and versionNumber ${versionNumber}" + e.getMessage())
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def nullifyReportIcsr() {
        def requestJSON = request.getJSON()
        Long icsrTempQueryId = requestJSON.exIcsrTemplateQueryId
        String caseNumber = requestJSON.caseNumber
        Long versionNumber = requestJSON.versionNumber
        Integer dueInDays = requestJSON.dueInDays
        String prodHashCode = requestJSON.prodHashCode
        String justification = requestJSON.justification
        String username = requestJSON.username
        Locale requestLocale = new Locale(params.language?:"en")

        if (!icsrTempQueryId || !caseNumber || !versionNumber || !dueInDays) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }


        if (!justification) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.justification.not.nullable', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        log.info("Request Received to Nullify Report for exTempQueryId ${icsrTempQueryId}, case number ${caseNumber} and versionNumber ${versionNumber}")

        justification = (justification?.length() > 2000) ? justification?.substring(0, 2000) : justification
        ExecutedIcsrTemplateQuery icsrTemplateQuery = ExecutedIcsrTemplateQuery.read(icsrTempQueryId)
        ExecutedIcsrProfileConfiguration icsrProfileConfiguration = icsrTemplateQuery?.usedConfiguration
        prodHashCode = prodHashCode ?: "-1"
        User user = User.findByUsernameIlike(username)

        try {
            ExecutedReportConfiguration executedIcsrProfileConfiguration = executedIcsrConfigurationService.createFromExecutedIcsrConfiguration(icsrProfileConfiguration, null)
            CRUDService.saveWithoutAuditLog(executedIcsrProfileConfiguration)
            ExecutedTemplateQuery executedTemplateQuery = executedIcsrProfileConfiguration.executedTemplateQueriesForProcessing.first()
            IcsrProfileConfiguration originalIcsrProfileConfiguration = IcsrProfileConfiguration.findByReportName(executedIcsrProfileConfiguration.reportName)
            IcsrCaseTracking icsrCaseTrackingRecord = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingRecord = icsrProfileAckService.getIcsrTrackingRecord(icsrTempQueryId, caseNumber, versionNumber)
            }
            icsrScheduleService.logIcsrCaseToScheduleTrackingForNullification(originalIcsrProfileConfiguration, executedTemplateQuery, caseNumber, versionNumber, dueInDays, justification, icsrTempQueryId, prodHashCode, icsrCaseTrackingRecord.authId, icsrCaseTrackingRecord.reportCategoryId, user)
            IcsrCaseTracking newIcsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
            }
            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
            newValues.put("justificationForNullification",justification)
            AuditLogConfigUtil.logChanges(newIcsrCaseTrackingInstance, newValues, [:]
                    , Constants.AUDIT_LOG_INSERT, ViewHelper.getMessage("auditLog.entityValue.icsr.scheduled.manual", caseNumber, versionNumber, newIcsrCaseTrackingInstance.profileName,ExecutedIcsrTemplateQuery.read(newIcsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, newIcsrCaseTrackingInstance.recipient), ("" + System.currentTimeMillis()), user?.username, user?.fullName)

            log.info("Successfully Added nullification Report for exTempQueryId ${executedTemplateQuery.id}, case number ${caseNumber} and versionNumber ${versionNumber}")
            render([result: caseNumber, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.nullify.report.success', [caseNumber, versionNumber, originalIcsrProfileConfiguration.reportName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Exception while adding case to nullfication. " + e.getMessage())
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def checkPreviousVersionTransmittedIcsr() {
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        Boolean bulk = requestJSON.bulk
        String caseDataJSON = requestJSON.caseData
        Locale requestLocale = new Locale(params.language?:"en")

        User user = User.findByUsername(username)
        if (!tenantId) {
            render([data: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        Boolean needToCheckPreviousVersion = grailsApplication.config.getProperty('pvr.icsr.enforce.transmission.in.version.sequence', Boolean)
        if (needToCheckPreviousVersion) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            Object caseData = jsonSlurper.parseText(caseDataJSON)
            if (caseData instanceof Map) {
                caseData = [caseData]
            }

            if (caseData.any {
                !it.templateId || !it.profileName || !it.recipient || !it.caseNumber || !it.versionNumber
            }) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }

            boolean canProceedToTransmit = true
            try {
                caseData.each {
                    Long templateId = it.templateId
                    String profileName = it.profileName
                    String recipient = it.recipient
                    String caseNumber = it.caseNumber
                    Integer versionNumber = it.versionNumber

                    canProceedToTransmit = icsrScheduleService.checkPreviousVersionIsTransmitted(caseNumber, versionNumber, profileName, recipient, templateId)
                    if (!canProceedToTransmit) {
                        render([result: null, resultCode: HttpStatus.CONFLICT.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.previous.version.transmitted.failed', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                        return
                    }
                }
                if (canProceedToTransmit) {
                    render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.previous.version.transmitted.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                    return
                }
            }
            catch (Exception e) {
                log.error("Error while checking previous version Transmitted for case", e)
                render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            }
        } else {
            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.previous.version.transmitted.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        }
    }

    def loadTransmitModal(String username, Integer tenantId) {
        Locale requestLocale = new Locale(params.language ?: "en")
        User user = User.findByUsername(username)
        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            String approvalDate = DateUtil.toDateStringWithTimeInAmPmFormatWithoutSec(user)
            Map data = [:]
            data.put('approvalDate', approvalDate + " (${user.preference?.timeZone})")
            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.load.transmission.form.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to Open Transmit Modal : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def transmitCaseIcsr() {
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        String password = requestJSON.password
        String approvalDate = requestJSON.approvalDate
        String comment = requestJSON.comments
        String caseDataJSON = requestJSON.caseData
        boolean bulk = requestJSON.bulk
        Locale requestLocale = new Locale(params.language?:"en")

        User user = User.findByUsername(username)
        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!password) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('app.label.workflow.rule.fillLogon', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            if (!ldapService.isLoginPasswordValid(username, password)) {
                render([result: null, resultCode: HttpStatus.UNAUTHORIZED.value(), resultMsg: ViewHelper.getMessage('app.label.workflow.rule.approvl.fail', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
        }
        comment = comment ?: ''
        String transmissionComment = null
        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
            transmissionComment = Constants.APPROVED_BY + user.fullName + "\n" + Constants.APPROVED_ON + approvalDate?.toString() + "\n\n" + comment?.toString()
        } else {
            transmissionComment = comment?.toString()
        }
        transmissionComment = transmissionComment.replaceAll("(?i)'","''")

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object caseData = jsonSlurper.parseText(caseDataJSON)
        if (caseData instanceof Map) {
            caseData = [caseData]
        }

        if (caseData.any {
            !it.exIcsrTemplateQueryId || !it.caseNumber || !it.versionNumber
        }) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        int successfulTransmitCount = 0
        int failedTransmitCount = 0

        caseData.each {
            Long exIcsrTemplateQueryId = it.exIcsrTemplateQueryId
            String caseNumber = it.caseNumber
            Integer versionNumber = it.versionNumber
            String profileName = ExecutedIcsrTemplateQuery.get(exIcsrTemplateQueryId)?.executedConfiguration?.reportName
            try {
                icsrReportService.transmitCase(exIcsrTemplateQueryId, caseNumber, versionNumber, transmissionComment, user.fullName, approvalDate?.toString(), transmissionComment, user?.username)
                if (!bulk) {
                    render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.case.transmit.success', [caseNumber, versionNumber, profileName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                    return
                } else {
                    successfulTransmitCount += 1
                }
            } catch (Exception e) {
                if (!bulk) {
                    log.error("Error while marking case ${caseNumber} as transmitted", e)
                    render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.case.transmit.failed', [caseNumber, versionNumber, profileName] as Object[], null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                    return
                } else {
                    failedTransmitCount += 1
                }
            }
        }
        render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.case.bulk.transmit.message', [successfulTransmitCount, failedTransmitCount] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
    }

    def listProfilesFilter(String username) {
        Locale requestLocale = new Locale(params.language?:"en")
        User user = User.findByUsername(username)
        try {
            def icsrProfilequery = IcsrProfileConfiguration.ownedByAndSharedWithUser(user, user.isAnyAdmin(), false)
            List<Map> data = icsrProfilequery.list().unique { it.reportName }.collect {
                [id: it.id, text: it.reportName]
            }
            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg:  ViewHelper.getMessage('icsr.public.api.profile.list.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to fetch Profile list : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def listAuthorizationFilter(String username) {
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)
        User user = User.findByUsername(username)
        try {
            List<Map> data = icsrScheduleService.getAuthType(language).collect {
                [id: it.name, text: it.name]
            }
            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.authorization.list.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to fetch Profile list : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def saveLocalCpIcsr() {
        def requestJSON = request.getJSON()
        Long exTempQueryId = requestJSON.exIcsrTemplateQueryId
        Long caseId = requestJSON.caseId
        Long versionNumber = requestJSON.versionNumber
        Integer flagLocalCp = requestJSON.flagLocalCp
        String caseNumber = requestJSON.caseNumber
        String prodHashCode = requestJSON.prodHashCode
        Long profileId = requestJSON.profileId
        String username = requestJSON.username
        Locale requestLocale = new Locale(params.language?:"en")

        if (!exTempQueryId || !caseId || !versionNumber || !flagLocalCp || !caseNumber || !profileId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.get(exTempQueryId)
        ExecutedIcsrProfileConfiguration executedConfiguration = executedTemplateQuery.executedConfiguration as ExecutedIcsrProfileConfiguration
        IcsrCaseTracking icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        if (!executedConfiguration.includeOpenCases) {
            if (!icsrCaseTracking?.flagCaseLocked) {
                log.info("Case Generation can not be processed because it is in Active state")
                render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.case.add.localcp.case.state.active', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
        }

        try {
            User user = User.findByUsernameIlike(username)
            Long tenantId = Tenants.currentId() as Long
            sqlGenerationService.localCpProc(caseId, versionNumber, executedConfiguration.id, executedConfiguration.reportName, tenantId, flagLocalCp, prodHashCode, profileId, icsrCaseTracking.processedReportId, user)
            log.info("Local CP Completed for caseId ${caseId}, Deleting Entry in IcsrCaseLocalCpData...")
            icsrScheduleService.deleteLocalCpProfileEntry(caseId, versionNumber, tenantId, profileConfiguration, true)
            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTracking)
            String code = flagLocalCp == 1 ? "auditLog.entityValue.icsr.triggered.local.cp" : "auditLog.entityValue.icsr.triggered.manual.generate"
            AuditLogConfigUtil.logChanges(icsrCaseTracking, newValues, [:]
                    , Constants.AUDIT_LOG_INSERT,ViewHelper.getMessage(code, caseNumber, versionNumber, icsrCaseTracking?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTracking?.recipient), ("" + System.currentTimeMillis()), username, user?.fullName)
            render([result: caseNumber, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage("icsr.case.add.localCp.success", [caseNumber + " v" + versionNumber, executedConfiguration.reportName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception ex) {
            log.error("Failed to add manual case due to invalid data", ex)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def loadIcsrSubmissionForm() {
        def requestJSON = request.getJSON()
        String username = requestJSON.username
        Integer tenantId = requestJSON.tenantId
        String recipient = requestJSON.recipient
        String action = requestJSON.action
        String preferredTimeZone = requestJSON.preferredTimeZone
        Boolean bulk = requestJSON.bulk
        String caseDataJSON = requestJSON.caseData
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)

        User user = User.findByUsername(username)
        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object caseData = jsonSlurper.parseText(caseDataJSON)
        if (caseData instanceof Map) {
            caseData = [caseData]
        }

        if (caseData.any {
            !it.exIcsrTemplateQueryId || !it.caseNumber || !it.versionNumber
        }) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        Date dueDate = null

        try {
            caseData.each {
                Long exIcsrTempQueryId = it.exIcsrTemplateQueryId
                String caseNumber = it.caseNumber
                Integer versionNumber = it.versionNumber
                IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
                    return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTempQueryId, caseNumber, versionNumber)
                }
                dueDate = icsrCaseTracking?.dueDate
            }

            String approvalDate = DateUtil.toDateStringWithTimeInAmPmFormatWithoutSec(user)
            String submissionDate = null
            String submissionTime = null

            TimeZoneEnum timeZone = TimeZoneEnum.values().find { it.timezoneId == preferredTimeZone }
            (submissionDate, submissionTime) = DateUtil.toDateAndTimeInAmPmFormat(timeZone.timezoneId)

            Map data = [:]

            String selectedStatus = null
            if (action == "submissionNotRequired") {
                selectedStatus = IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.key
            } else if (action == "submit") {
                selectedStatus = IcsrCaseStateEnum.SUBMITTED.key
            }
            data.put('status', selectedStatus)
            data.put('approvalDate', approvalDate + " (${user.preference?.timeZone})")
            data.put('recipient', recipient)
            data.put('submissionDate', submissionDate)
            data.put('time', submissionTime)
            data.put('timeZone', timeZone.name())
            data.put('dueDate', bulk ? "" : dueDate)
            data.put('timeData', DateUtil.genenrateTimeDropDownList(submissionTime))
            data.put('timeZoneData', icsrCaseTrackingService.getTimezone())
            data.put('statusData', ViewHelper.getSubmissionDropDown(action,requestLocale))

            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.load.submission.form.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to Open Submit Modal : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def loadIcsrStatusForm() {
        def requestJSON = request.getJSON()
        String username = requestJSON.username
        Integer tenantId = requestJSON.tenantId
        String recipient = requestJSON.recipient
        String action = requestJSON.action
        String preferredTimeZone = requestJSON.preferredTimeZone
        String caseDataJSON = requestJSON.caseData
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)

        User user = User.findByUsername(username)
        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object caseData = jsonSlurper.parseText(caseDataJSON)
        if (caseData instanceof Map) {
            caseData = [caseData]
        }

        if (caseData.any {
            !it.exIcsrTemplateQueryId || !it.caseNumber || !it.versionNumber
        }) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        Date dueDate = null
        Date icsrSubmissionDate = null
        ExecutedIcsrTemplateQuery executedTemplateQuery = null
        String icsrCaseId = null
        String profileName = null

        try {
            caseData.each {
                Long exIcsrTempQueryId = it.exIcsrTemplateQueryId
                String caseNumber = it.caseNumber
                Integer versionNumber = it.versionNumber

                IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
                    return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTempQueryId, caseNumber, versionNumber)
                }
                dueDate = icsrCaseTracking?.dueDate
                icsrSubmissionDate = icsrCaseTracking?.submissionDate
                icsrCaseId = icsrCaseTracking?.uniqueIdentifier()
                profileName = icsrCaseTracking?.profileName
                executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTempQueryId)
            }
            String distributionChannel = executedTemplateQuery.distributionChannelName

            String approvalDate = DateUtil.toDateStringWithTimeInAmPmFormatWithoutSec(user)
            String submissionTime = null
            String submissionDate = null
            TimeZoneEnum timeZone = null

            Map data = [:]
            //pretending that that action is coming direct from row.e2bstatus eg: action = "SUBMITTED"
            String selectedStatus = IcsrCaseStateEnum."${action}".key

            boolean distributionFlag = false

            if(action == "SUBMITTED" && (distributionChannel == DistributionChannelEnum.EMAIL?.toString() || distributionChannel == DistributionChannelEnum.PAPER_MAIL?.toString())) distributionFlag = true
            if(distributionFlag == true) {
                timeZone = TimeZoneEnum.values().find { it.timezoneId == preferredTimeZone }
                (submissionDate, submissionTime) = DateUtil.toDateAndTimeInAmPmFormat(timeZone.timezoneId, icsrSubmissionDate)
            }
            if(distributionFlag == true) {
                timeZone = TimeZoneEnum.values().find { it.timezoneId == preferredTimeZone }
                (submissionDate, submissionTime) = DateUtil.toDateAndTimeInAmPmFormat(timeZone.timezoneId, icsrSubmissionDate)
            }
            data.put('status', selectedStatus)
            data.put('approvalDate', approvalDate + " (${user.preference?.timeZone})")
            data.put('recipient', recipient)
            data.put('submissionDate', submissionDate)
            data.put('time', submissionTime)
            data.put('timeZone', timeZone?.name())
            data.put('dueDate', dueDate)
            data.put('timeData', submissionTime?DateUtil.genenrateTimeDropDownList(submissionTime):null)
            data.put('timeZoneData', icsrCaseTrackingService.getTimezone())
            data.put('statusData', ViewHelper.getIcsrStateDropDown(action, requestLocale, icsrCaseId, profileName))
            data.put('distributionFlag', distributionFlag)

            render([result: data, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.load.submission.form.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Unable to Open Submit Modal : " + e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def submitCaseIcsr(BulkCaseSubmissionCO bulkCaseSubmissionCO) {
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)
        User user = User.findByUsername(bulkCaseSubmissionCO.username)
        if (!bulkCaseSubmissionCO.tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!bulkCaseSubmissionCO.password) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('app.label.workflow.rule.fillLogon', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            if (!ldapService.isLoginPasswordValid(bulkCaseSubmissionCO.username, bulkCaseSubmissionCO.password)) {
                render([result: null, resultCode: HttpStatus.UNAUTHORIZED.value(), resultMsg: ViewHelper.getMessage('app.label.workflow.rule.approvl.fail', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
        }
        JsonSlurper jsonSlurper = new JsonSlurper()
        Object commentMap = jsonSlurper.parseText(bulkCaseSubmissionCO.comment)
        String commentEn = commentMap.en
        String commentJa = commentMap.ja
        String submissionComment = null
        String submissionCommentJa = null

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            submissionComment = Constants.APPROVED_BY + user.fullName + "\n" + Constants.APPROVED_ON + bulkCaseSubmissionCO.approvalDate?.toString() + "\n\n" + (commentEn ? commentEn.toString() : "")
            submissionCommentJa = Constants.APPROVED_BY + user.fullName + "\n" + Constants.APPROVED_ON + bulkCaseSubmissionCO.approvalDate?.toString() + "\n\n" + (commentJa ? commentJa.toString() : "")
        } else {
            submissionComment = (commentEn?.toString()?.trim()) ? commentEn.toString() : null
            submissionCommentJa = (commentJa?.toString()?.trim()) ? commentJa.toString() : null
        }

        Object caseData = jsonSlurper.parseText(bulkCaseSubmissionCO.caseData)
        if (caseData instanceof Map) {
            caseData = [caseData]
        }

        if (!bulkCaseSubmissionCO.approvalDate || !bulkCaseSubmissionCO.submissionDate || !bulkCaseSubmissionCO.time || !bulkCaseSubmissionCO.timeZone || !bulkCaseSubmissionCO.recipient) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (caseData.any {
            !it.exIcsrTemplateQueryId || !it.caseNumber || !it.versionNumber
        }) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        String dateAndTime = bulkCaseSubmissionCO.submissionDate + " " +bulkCaseSubmissionCO.time
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_FORMAT_AM_PM)
        Date localDate = formatter.parse(dateAndTime)
        TimeZoneEnum timeZoneEnum = TimeZoneEnum.values().find {
            it.name() == bulkCaseSubmissionCO.timeZone
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
        Date caseSubmissionDateUTC = Date.parse(DateUtil.DATE_FORMAT_AM_PM_WITH_OFFSET, (dateAndTime +' '+ timeZoneEnum.getGmtOffset()))

        Integer successfulSubmissionCount = 0
        Integer failedSubmissionCount = 0
        SimpleDateFormat inputFormat = new SimpleDateFormat(
                language.equals("en") ? DateUtil.DATEPICKER_FORMAT : DateUtil.DATEPICKER_JFORMAT
        )

        caseData.each {
            Long exIcsrTemplateQueryId = it.exIcsrTemplateQueryId
            String caseNumber = it.caseNumber
            String versionNumber = it.versionNumber.toString()
            String profileId
            String queryId
            try {
                IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
                    return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, Long.parseLong(versionNumber))
                }
                if(bulkCaseSubmissionCO.status == 'SUBMISSION_NOT_REQUIRED_FINAL') {
                    if (icsrCaseTracking.flagCaseLocked && icsrCaseTracking.e2BStatus == IcsrCaseStateEnum.SCHEDULED.toString() && (icsrCaseTracking.flagAutoGenerate || icsrCaseTracking.flagLocalCp in [1, 2])) {
                        failedSubmissionCount += 1
                        return
                    }
                }
                Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTracking)
                if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                    oldValues.put("approvedBy", null)
                    oldValues.put("approvedOn", null)
                }
                oldValues.put("submissionComments", null)
                oldValues.put("submissionCommentsJ", null)
                oldValues.put("documentName", null)

                CaseSubmissionCO caseSubmissionCO = new CaseSubmissionCO()
                caseSubmissionCO.icsrCaseState = IcsrCaseStateEnum.valueOf(bulkCaseSubmissionCO.status)
                caseSubmissionCO.reportingDestinations = bulkCaseSubmissionCO.recipient
                caseSubmissionCO.icsrCaseId = icsrCaseTracking.uniqueIdentifier()
                caseSubmissionCO.profileName = icsrCaseTracking.profileName
                if (caseSubmissionCO.validate()) {
                    (profileId, queryId, caseNumber, versionNumber) = caseSubmissionCO?.icsrCaseId?.split("\\*\\*")
                    caseSubmissionCO.profileId = Long.parseLong(profileId)
                    caseSubmissionCO.queryId = queryId
                    caseSubmissionCO.caseNumber = caseNumber
                    caseSubmissionCO.versionNumber = Long.parseLong(versionNumber)
                    caseSubmissionCO.comment = submissionComment?.replaceAll("(?i)'", "''")
                    caseSubmissionCO.commentJ = submissionCommentJa?.replaceAll("(?i)'","''")
                    caseSubmissionCO.justificationId = bulkCaseSubmissionCO.justificationId ?: null
                    caseSubmissionCO.submissionDocument = bulkCaseSubmissionCO.file?.getBytes()
                    caseSubmissionCO.submissionFilename = bulkCaseSubmissionCO.file?.filename
                    caseSubmissionCO.processedReportId = icsrCaseTracking.processedReportId
                    caseSubmissionCO.submissionDate = caseSubmissionDateUTC
                    caseSubmissionCO.localSubmissionDate = localDate
                    caseSubmissionCO.timeZoneId = timeZoneEnum.timezoneId
                    caseSubmissionCO.updatedBy = user.username
                    if(bulkCaseSubmissionCO.dueDate != null) {
                        Date parsedDate = inputFormat.parse(bulkCaseSubmissionCO.dueDate)
                        String formattedDateString = outputFormat.format(parsedDate)
                        caseSubmissionCO.dueDate = outputFormat.parse(formattedDateString)
                    } else {
                        caseSubmissionCO.dueDate = icsrCaseTracking?.dueDate
                    }
                    if(icsrProfileAckService.validateSubmissionDate(localDate, icsrCaseTracking.generationDate, caseSubmissionCO.timeZoneId, bulkCaseSubmissionCO.status)) {
                        failedSubmissionCount += 1
                        return
                    }
                    reportSubmissionService.submitIcsrCase(bulkCaseSubmissionCO.tenantId, caseSubmissionCO)
                    IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, Long.parseLong(versionNumber))
                    Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                    if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                        newValues.put("approvedBy", user.fullName)
                        newValues.put("approvedOn", bulkCaseSubmissionCO.approvalDate?.toString())
                    }
                    newValues.put("submissionComments", commentEn?.toString())
                    newValues.put("submissionCommentsJ", commentJa?.toString())
                    newValues.put("documentName", bulkCaseSubmissionCO.file?.filename?.toString())
                    AuditLogConfigUtil.logChanges(icsrCaseTracking, newValues, oldValues
                            , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", caseNumber, versionNumber, icsrCaseTracking.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTracking.recipient), ("" + System.currentTimeMillis()), user?.username, user?.fullName)

                    if (!bulkCaseSubmissionCO.bulk) {
                        if(IcsrCaseStateEnum.valueOf(bulkCaseSubmissionCO.status) in [IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL]) {
                            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('app.reportSubmission.submission.not.req.successful', [caseNumber, versionNumber, icsrCaseTracking.profileName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                        } else {
                            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('app.reportSubmission.submitted.successful', [caseNumber, versionNumber, icsrCaseTracking.profileName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                        }
                        return
                    } else {
                        successfulSubmissionCount += 1
                    }
                } else {
                    log.error("Validation Failed : Unable to submit case ${caseNumber} - ${versionNumber}, "+caseSubmissionCO.errors.allErrors?.toString())
                    if (!bulkCaseSubmissionCO.bulk) {
                        render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.system.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                        return
                    } else {
                        failedSubmissionCount += 1
                    }
                }
            }
            catch (CaseSubmissionException cse) {
                log.error("Case Submission Exception : Unable to submit case ${caseNumber} - ${versionNumber}, ${cse.message}")
                if (!bulkCaseSubmissionCO.bulk) {
                    render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.case.submit.not.transmitted.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                    return
                } else {
                    failedSubmissionCount += 1
                }
            } catch (Exception e) {
                log.error("Exception : Unable to submit case ${caseNumber} - ${versionNumber}, ${e.message}")
                if (!bulkCaseSubmissionCO.bulk) {
                    render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.system.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                    return
                } else {
                    failedSubmissionCount += 1
                }
            }
        }
        render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('app.bulkReportSubmission.submitted.successful', [successfulSubmissionCount, failedSubmissionCount] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
    }

    def updateIcsrCaseStatus(BulkCaseSubmissionCO bulkCaseSubmissionCO) {
        String language = params.language ?: "en"
        Locale requestLocale = new Locale(language)
        User user = User.findByUsername(bulkCaseSubmissionCO.username)
        if (!bulkCaseSubmissionCO.tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!bulkCaseSubmissionCO.password) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('app.label.workflow.rule.fillLogon', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            if (!ldapService.isLoginPasswordValid(bulkCaseSubmissionCO.username, bulkCaseSubmissionCO.password)) {
                render([result: null, resultCode: HttpStatus.UNAUTHORIZED.value(), resultMsg: ViewHelper.getMessage('app.label.workflow.rule.approvl.fail', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
        }

        String submissionComment = null
        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
            submissionComment = Constants.APPROVED_BY + user.fullName + "\n" + Constants.APPROVED_ON + bulkCaseSubmissionCO.approvalDate?.toString() + "\n\n" + bulkCaseSubmissionCO.comment?.toString()
        } else {
            submissionComment = bulkCaseSubmissionCO.comment?.toString()
        }

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object caseData = jsonSlurper.parseText(bulkCaseSubmissionCO.caseData)
        if (caseData instanceof Map) {
            caseData = [caseData]
        }

        if (!bulkCaseSubmissionCO.approvalDate || !bulkCaseSubmissionCO.recipient || !bulkCaseSubmissionCO.dueDate || !bulkCaseSubmissionCO.comment || !bulkCaseSubmissionCO.status || !bulkCaseSubmissionCO.currentStatus) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (caseData.any {
            !it.exIcsrTemplateQueryId || !it.caseNumber || !it.versionNumber
        }) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        SimpleDateFormat inputFormat = new SimpleDateFormat(
                language.equals("en") ? DateUtil.DATEPICKER_FORMAT : DateUtil.DATEPICKER_JFORMAT
        )
        Date localDate = null
        TimeZoneEnum timeZoneEnum = null
        Date caseSubmissionDateUTC = null
        if(bulkCaseSubmissionCO.submissionDate != 'null' && bulkCaseSubmissionCO.time !='null' && bulkCaseSubmissionCO.timeZone !='null'){
            String dateAndTime = bulkCaseSubmissionCO.submissionDate + " " +bulkCaseSubmissionCO.time
            SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_FORMAT_AM_PM)
            localDate = formatter.parse(dateAndTime)
            timeZoneEnum = TimeZoneEnum.values().find {
                it.name() == bulkCaseSubmissionCO.timeZone
            }
            caseSubmissionDateUTC = Date.parse(DateUtil.DATE_FORMAT_AM_PM_WITH_OFFSET, (dateAndTime +' '+ timeZoneEnum.getGmtOffset()))
        }

        Long exIcsrTemplateQueryId = null
        String caseNumber = ''
        String versionNumber = ''
        caseData.each {
            exIcsrTemplateQueryId = it.exIcsrTemplateQueryId
            caseNumber = it.caseNumber
            versionNumber = it.versionNumber.toString()
        }
        String profileId
        String queryId
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
            IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
                return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, Long.parseLong(versionNumber))
            }
            String formattedOldDueDate = outputFormat.format(icsrCaseTracking?.dueDate)
            Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTracking)
            if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                oldValues.put("approvedBy", null)
                oldValues.put("approvedOn", null)
            }
            oldValues.put("submissionComments", null)
            oldValues.put("documentName", null)

            CaseSubmissionCO caseSubmissionCO = new CaseSubmissionCO()
            caseSubmissionCO.icsrCaseState = IcsrCaseStateEnum.valueOf(bulkCaseSubmissionCO.status)
            caseSubmissionCO.currentState = bulkCaseSubmissionCO.currentStatus
            caseSubmissionCO.reportingDestinations = bulkCaseSubmissionCO.recipient
            caseSubmissionCO.icsrCaseId = icsrCaseTracking.uniqueIdentifier()
            caseSubmissionCO.profileName = icsrCaseTracking.profileName
            (profileId, queryId, caseNumber, versionNumber) = caseSubmissionCO?.icsrCaseId?.split("\\*\\*")
            caseSubmissionCO.profileId = Long.parseLong(profileId)
            caseSubmissionCO.queryId = queryId
            caseSubmissionCO.caseNumber = caseNumber
            caseSubmissionCO.versionNumber = Long.parseLong(versionNumber)
            caseSubmissionCO.comment = submissionComment.replaceAll("(?i)'", "''")
            caseSubmissionCO.submissionDocument = bulkCaseSubmissionCO.file?.getBytes()
            caseSubmissionCO.submissionFilename = bulkCaseSubmissionCO.file?.filename
            caseSubmissionCO.processedReportId = icsrCaseTracking.processedReportId
            caseSubmissionCO.submissionDate = caseSubmissionDateUTC
            caseSubmissionCO.localSubmissionDate = localDate
            Date parsedDate = inputFormat.parse(bulkCaseSubmissionCO.dueDate)
            String formattedDateString = outputFormat.format(parsedDate)
            caseSubmissionCO.dueDate = outputFormat.parse(formattedDateString)
            if(bulkCaseSubmissionCO.timeZone == 'null')
                caseSubmissionCO.timeZoneId = null
            else
                caseSubmissionCO.timeZoneId = timeZoneEnum.timezoneId
            caseSubmissionCO.updatedBy = user.fullName?:user.username
            if(icsrProfileAckService.validateSubmissionDate(localDate, icsrCaseTracking.generationDate, caseSubmissionCO.timeZoneId)) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('submission.date.later.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            sqlGenerationService.updatingIcsrStatusAndDate(caseSubmissionCO, icsrCaseTracking)

            IcsrCaseTracking newIcsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, Long.parseLong(versionNumber))
            }
            String formattedUpdatedDueDate = outputFormat.format(newIcsrCaseTrackingInstance?.dueDate)
            if(formattedUpdatedDueDate != formattedOldDueDate) {
                icsrScheduleService.calculateDueDateForManual(newIcsrCaseTrackingInstance.caseId,newIcsrCaseTrackingInstance.versionNumber, newIcsrCaseTrackingInstance.tenantId, new Date(), Constants.DUE_DATE_TYPE_MANUAL)
            }

            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
            if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
                newValues.put("approvedBy", user.fullName)
                newValues.put("approvedOn", bulkCaseSubmissionCO.approvalDate?.toString())
            }
            newValues.put("submissionComments", bulkCaseSubmissionCO.comment?.toString())
            newValues.put("documentName", bulkCaseSubmissionCO.file?.filename?.toString())
            AuditLogConfigUtil.logChanges(icsrCaseTracking, newValues, oldValues
                    , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", caseNumber, versionNumber, icsrCaseTracking.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTracking.recipient), ("" + System.currentTimeMillis()), user?.username, user?.fullName)

            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.report.unsubmitting.status', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Exception : Failed to un-submit the Icsr Report ${caseNumber} - ${versionNumber}, ${e.message}")
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage("icsr.report.unsubmitting.status.error", null, e.message, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
    }

    def allEmails(Integer tenantId) {
        Locale requestLocale = new Locale(params.language?:"en")
        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        try {
            render([result: userService.getAllEmails(tenantId), resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.profile.list.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
        } catch (Exception e) {
            log.error("Failed to fetch all Emails", e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def addAllEmail() {
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        def emails = requestJSON.emails

        User user = User.findByUsername(username)
        Locale requestLocale = new Locale(params.language?:"en")

        if (!tenantId || !emails) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        List successfulEmails = []

        try {
            emails.each {
                Email email = new Email(email: it.email, description: it.description, createdBy: username, modifiedBy: username)
                email.tenantId = tenantId as Long
                CRUDService.save(email)
                successfulEmails.add(it.email)
            }
            render([result: successfulEmails, resultCode: HttpStatus.CREATED.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.email.add.successful', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Failed to save email "+e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
    }


    def emailIcsr() {
        def requestJSON = request.getJSON()
        String username = requestJSON.username
        Integer tenantId = requestJSON.tenantId
        Long exIcsrTemplateQueryId = requestJSON.exIcsrTemplateQueryId
        String caseNumber = requestJSON.caseNumber
        Integer versionNumber = requestJSON.versionNumber
        boolean pdf = requestJSON.pdf
        boolean xml = requestJSON.xml
        boolean e2b = requestJSON.e2b
        String[] emails = requestJSON.emails

        User user = User.findByUsername(username)
        Locale requestLocale = new Locale(params.language?:"en")

        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
        if (!exIcsrTemplateQueryId || !caseNumber || !versionNumber || !emails) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
            List<ReportFormatEnum> formats = []
            if (pdf) {
                formats.add(ReportFormatEnum.PDF)
            }
            if (xml) {
                formats.add(ReportFormatEnum.XML)
            }
            if (e2b) {
                formats.add(ReportFormatEnum.R3XML)
            }
            List<String> emailList = emails

            println emailList

            if (!(executedTemplateQuery && formats && emailList)) {
                log.info("Fail to send email due to invalid data")
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.case.email.invalid.data.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            emailService.emailIcsrCaseTo(executedTemplateQuery, caseNumber, versionNumber, emailList, formats)
            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.case.email.sent.success', [caseNumber, versionNumber] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
        } catch (Exception ex) {
            log.error("Failed while sending email for ${caseNumber} and ${versionNumber}", ex)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.case.email.sent.failed', [caseNumber, versionNumber] as Object[], null, requestLocale), resultStatus: TRN_FAIL] as JSON)
        }
    }

    def downloadICSR(String username, Integer tenantId, Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber) {
        User user = User.findByUsername(username)
        Locale requestLocale = new Locale(params.language?:"en")

        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (!exIcsrTemplateQueryId || !caseNumber || !versionNumber) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
            ExecutedReportConfiguration executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
            if (!executedReportConfiguration || !executedTemplateQuery) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.invalid.data.for.download', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }

            String outputFormat
            File reportFile
            IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
            Date fileDate = newIcsrCaseTrackingInstance?.generationDate
            Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
            if (executedTemplateQuery.usedTemplate?.isCiomsITemplate()) { //creates CIOMS Report
                outputFormat = ReportFormatEnum.PDF.displayName
                reportFile = dynamicReportService.createCIOMSReport(caseNumber, versionNumber.intValue(), executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery)
            } else if (executedTemplateQuery.usedTemplate?.isMedWatchTemplate()) { //Creates Medwatch report
                outputFormat = ReportFormatEnum.PDF.displayName
                reportFile = dynamicReportService.createMedWatchReport(caseNumber, versionNumber.intValue(), executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery)
            } else {  //Creates R3XML report
                outputFormat = ReportFormatEnum.R3XML.displayName
                def xmlParameterMap = ["executedTemplateQuery": executedTemplateQuery, "outputFormat": outputFormat, "caseNumber": caseNumber, "versionNumber": versionNumber]
                reportFile = dynamicReportService.createR3XMLReport(executedTemplateQuery, false, xmlParameterMap, fileDate, isJapanProfile)
                outputFormat = ReportFormatEnum.XML.displayName
            }
            String currentSenderIdentifier = executedReportConfiguration?.getSenderIdentifier()
            Map paramsMap = ['caseNumber': caseNumber, 'exIcsrTemplateQueryId': exIcsrTemplateQueryId, 'versionNumber': versionNumber]
            String reportFileName = dynamicReportService.getReportName(currentSenderIdentifier, false, paramsMap, fileDate, isJapanProfile)
            byte[] fileBytes = reportFile.bytes
            String encodedFile = Base64.encodeBase64String(fileBytes)

            response.contentType = "application/octet-stream"
            response.setHeader("Content-Disposition", "attachment; filename=\"${reportFile.name}\"")

            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_DATE_TIME_FORMAT)
            AuditLogConfigUtil.logChanges(executedReportConfiguration, [outputFormat: outputFormat, fileName: reportFile.name ?: reportFileName, exportedDate: sdf.format(new Date())],
                    [:], Constants.AUDIT_LOG_EXPORT, Constants.SPACE_STRING + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(ReportFormatEnum.valueOf(outputFormat)).displayName), ("" + System.currentTimeMillis()), username, user?.fullName)
            render([result: [file: encodedFile, fileName: reportFileName + '.' +outputFormat], resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.download.successful', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception ex) {
            log.error("Exception occurred while downloading report", ex)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.download.fail', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
    }

    def listConfig(String username, Integer tenantId) {
        User user = User.findByUsername(username)
        Locale requestLocale = new Locale(params.language?:"en")

        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        boolean revalidate = grailsApplication.config.icsr.case.submission.user.revalidate ?: false
        String role = VIEWER
        if (user.isICSRAdmin()) {
            role = ADMIN
        } else if (user.hasICSRActionRoles()) {
            role = EDITOR
        }
        Map configJSON = [config: [reValidate: revalidate, role: role]]
        render([result: configJSON, resultCode: HttpStatus.OK.value(), resultMsg: TRN_SUCCESS, resultStatus: TRN_SUCCESS] as JSON)
    }

    def regenerateCaseIcsr() {
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        String comment = requestJSON.regenerateComment
        String caseDataJSON = requestJSON.caseData
        boolean bulk = requestJSON.bulk
        Locale requestLocale = new Locale(params.language?:"en")

        User user = User.findByUsername(username)
        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }


        String regenerateComment = comment?.toString()
        regenerateComment = regenerateComment.replaceAll("(?i)'", "''")

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object caseData = jsonSlurper.parseText(caseDataJSON)
        if (caseData instanceof Map) {
            caseData = [caseData]
        }

        if (caseData.any {
            !it.exIcsrTemplateQueryId || !it.caseNumber || !it.versionNumber
        }) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.case.history.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        int successfulRegenerateCount = 0
        int failedRegenerateCount = 0

        Tenants.withId(tenantId) {
            caseData.each {
                Long exIcsrTemplateQueryId = it.exIcsrTemplateQueryId
                String caseNumber = it.caseNumber
                Integer versionNumber = it.versionNumber

                IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, versionNumber)
                Long profileId = newIcsrCaseTrackingInstance.profileId
                Integer dueInDays = newIcsrCaseTrackingInstance.dueInDays
                Boolean isExpedited = newIcsrCaseTrackingInstance.isExpedited
                String prodHashCode = newIcsrCaseTrackingInstance.prodHashCode
                Long authorizationTypeId = newIcsrCaseTrackingInstance.authorizationTypeId
                Long authId = newIcsrCaseTrackingInstance.authId
                Long reportCategoryId = newIcsrCaseTrackingInstance.reportCategoryId
                Long originalSectionId = newIcsrCaseTrackingInstance.originalSectionId
                Long processedReportId = newIcsrCaseTrackingInstance.processedReportId

                IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
                TemplateQuery templateQuery = TemplateQuery.findById(originalSectionId)
                if (!templateQuery) {
                    failedRegenerateCount += 1
                    log.error("Failed to regenerate case with case number ${caseNumber} - ${versionNumber} for Profile ID ${profileId} as report created before the upgrade can not be Regenerated.")
                    return
                }
                final Date scheduleDate = new Date()
                boolean flagRegenerate = true
                try {
                    log.info("Regenerate Case Request Recieved from PVCM for ${caseNumber} - ${versionNumber} for Profile ID ${profileId}")
                    icsrScheduleService.addCaseToSchedule(profileConfiguration, templateQuery, caseNumber, versionNumber, dueInDays, isExpedited, username, user?.fullName, prodHashCode, authorizationTypeId, authId, reportCategoryId, newIcsrCaseTrackingInstance.followupInfo == "Nullification" ? 3 : null, scheduleDate, flagRegenerate, regenerateComment, processedReportId)
                    if (!bulk) {
                        render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.add.regenerate.case.success', [caseNumber + " v" + versionNumber, profileConfiguration.reportName] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
                        return
                    } else {
                        successfulRegenerateCount += 1
                    }
                } catch (Exception e) {
                    if (!bulk) {
                        log.error("Error while regenerating case ${caseNumber}", e)
                        render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('default.server.error.message', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                        return
                    } else {
                        failedRegenerateCount += 1
                    }
                }
            }

            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.case.bulk.regenerate.message', [successfulRegenerateCount, failedRegenerateCount] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
        }
    }

    def getUserPreferences() {
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        Locale requestLocale = new Locale(params.language?:"en")

        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            User user = User.findByUsername(username)
            Preference preference = user.preference
            Map json = JSON.parse(preference.userPreferences ? preference.userPreferences : "{}")

            if (!json.icsrCaseTrackingTableStateKey) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.user.preference.key.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
            String userPreferenceKey =  json.icsrCaseTrackingTableStateKey.toString()
            def icsrCaseTrackingTableStateKey = new JsonSlurper().parseText(userPreferenceKey)
            render([result: [icsrCaseTrackingTableStateKey], resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.user.fetch.preference.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Error occurred while fetching user preferences", e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.user.fetch.preference.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
    }

    def updateUserPreferences(){
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        String userPreferenceJSON = requestJSON.icsrCaseTrackingTableStateKey
        User user = User.findByUsername(username)
        Locale requestLocale = new Locale(params.language?:"en")

        if (!tenantId) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.tenantId.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (!userPreferenceJSON) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.user.Preference.required', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            Preference preference = user.preference
            if (!preference.userPreferences) preference.userPreferences = "{}"
            Map json = JSON.parse(preference.userPreferences)
            if(requestJSON.icsrCaseTrackingTableStateKey) {
                String icsrCaseTrackingTableStateValue = requestJSON.icsrCaseTrackingTableStateKey.toString()
                Map prefMap = JSON.parse(icsrCaseTrackingTableStateValue)
                //exclude session data from preferences (need to keep it only in session storage)
                prefMap.remove('length')
                prefMap.remove('search')
                prefMap.remove('start')
                json.icsrCaseTrackingTableStateKey = (prefMap as JSON).toString()
            }
            preference.userPreferences = json.toString();
            CRUDService.update(preference)
            render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.user.preference.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Error occurred while updating user preferences", e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.user.preference.error', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }
    }

    def listStandardJustification(String targetAction) {
        Locale requestLocale = new Locale(params.language?:"en")
        if (!targetAction) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.public.api.action.missing', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        try {
            String actionName = Constants.icsrSubmissionStatus[targetAction] ?: null
            List<StandardJustification> justifications = []
            StandardJustification.'pva'.withNewSession {
                justifications = StandardJustification.findAllByActionNameAndIsActiveAndIsDisplay(actionName, true, true)
            }

            Long pvaLangId = sqlGenerationService.getPVALanguageId(Locale.ENGLISH.toString())
            Long pvaJaLangId = sqlGenerationService.getPVALanguageId(Locale.JAPANESE.toString())

            List<StandardJustification> justificationList = justifications.findAll {it.langId == pvaLangId}
            List<StandardJustification> jaJustificationList = justifications.findAll {it.langId == pvaJaLangId}

            def result = [
                    ja: jaJustificationList.collect { jaItem -> [ id  : jaItem.codeId, text: jaItem.description ] },
                    en: justificationList.collect { item -> [ id  : item.codeId, text: item.description ] }
            ]
            render([result: result, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.standard.justification.success', null, null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
            return
        } catch (Exception e) {
            log.error("Error occurred while fetching Standard Justification list", e)
            render([result: null, resultCode: HttpStatus.INTERNAL_SERVER_ERROR.value(), resultMsg: ViewHelper.getMessage('icsr.standard.justification.failure', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

    }

    def previewCaseDataScheduled(){
        def requestJSON = request.getJSON()
        Integer tenantId = requestJSON.tenantId
        String username = requestJSON.username
        Long exIcsrTemplateQueryId = requestJSON.exIcsrTemplateQueryId
        String caseNumber = requestJSON.caseNumber
        Integer versionNumber = requestJSON.versionNumber
        String status = requestJSON.status
        Long processedReportId = requestJSON.processedReportId
        Locale requestLocale = new Locale(params.language?:"en")

        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecordByProcessedReportId(processedReportId, caseNumber, versionNumber)
        }

        exIcsrTemplateQueryId = icsrCaseTracking.exIcsrTemplateQueryId
        if (!icsrCaseTrackingService.validateCaseDataForPreview(exIcsrTemplateQueryId, caseNumber, versionNumber)) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.generate.manual.generating.or.generated', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        def icsrCaseProcessingQueue = null
        IcsrCaseProcessingQueue.'pva'.withNewSession {
            icsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }

        if(icsrCaseProcessingQueue==null) {
            IcsrCaseProcessingQueueHist.'pva'.withNewSession {
                icsrCaseProcessingQueue = IcsrCaseProcessingQueueHist.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
            }
        }

        if (!icsrCaseProcessingQueue) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.generate.manual.invalid', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if(icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.QUALIFIED && icsrCaseProcessingQueue.isLocked) {
            ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedTemplateQuery.get(exIcsrTemplateQueryId).executedConfiguration
            if (executedIcsrProfileConfiguration.autoGenerate) {
                render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.generate.manual.qualified.for.generation', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
                return
            }
        }

        if (!executorThreadInfoService.availableSlotsForCasesGeneration()) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.generate.manual.no.slot', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        if (icsrCaseProcessingQueue.id in executorThreadInfoService.totalCurrentlyGeneratingCases) {
            render([result: null, resultCode: HttpStatus.BAD_REQUEST.value(), resultMsg: ViewHelper.getMessage('icsr.generate.manual.generating.or.generated', null, null, requestLocale), resultStatus: TRN_FAIL] as JSON)
            return
        }

        Promises.task {
            Tenants.withId(icsrCaseProcessingQueue.tenantId) {
                User.withNewSession {
                    try {
                        icsrScheduleService.generateCaseDataManual(icsrCaseProcessingQueue, status, icsrCaseTracking)
                    } catch (SQLException dbe) {
                        log.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${dbe.message}")
                        try {
                            icsrScheduleService.updateErrorForCase(icsrCaseProcessingQueue, new Exception(dbe.getMessage(), dbe))
                        } catch (ex) {
                            log.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                        }
                    } catch (ExecutionStatusException e) {
                        log.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${e.message}")
                        try {
                            icsrScheduleService.updateErrorForCase(icsrCaseProcessingQueue, e)
                        } catch (ex) {
                            log.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                        }
                    } catch (e) {
                        log.error("Fatal error while icsr data generation")
                    }
                }
            }
        }
        render([result: null, resultCode: HttpStatus.OK.value(), resultMsg: ViewHelper.getMessage('icsr.generate.manual.request.success', [icsrCaseProcessingQueue.caseNumber + " v" + icsrCaseProcessingQueue.versionNumber] as Object[], null, requestLocale), resultStatus: TRN_SUCCESS] as JSON)
        return
    }
}
