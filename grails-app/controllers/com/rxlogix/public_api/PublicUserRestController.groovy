package com.rxlogix.public_api

import com.rxlogix.config.SourceProfile
import com.rxlogix.dto.BlindedUsersDTO
import com.rxlogix.customException.ParameterMissingException
import com.rxlogix.dto.FieldResponseDTO
import com.rxlogix.dto.PrivacyProfileResponseDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.localization.Localization
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.FieldProfileFields
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import com.rxlogix.Constants
import groovy.sql.Sql
import org.springframework.http.HttpStatus
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import com.rxlogix.RxCodec

import java.sql.SQLException

import javax.xml.ws.Response
import java.util.stream.Collectors
import com.rxlogix.enums.TimeZoneEnum

@Secured('permitAll')
class PublicUserRestController {

    SessionRepository sessionRepository
    def userService
    def seedDataService
    def customMessageService
    def utilService
    def oneDriveRestService
    def reportFieldService
    def dataSource_pva
    def hazelService
    def executorThreadInfoService

    static allowedMethods = [copyBulkUsers: ['POST'], copyBulkUserGroups: ['POST'], sendTeamsNotification: ['POST']]

    def copyBulkUsers(String userName, String jsonData) {
        log.info("Copying User Started by user ${userName} ...")
        log.info("jsonData is = " + jsonData)
        seedDataService.copyUsers(userName, MiscUtil.parseJsonText(jsonData) as Map, params)
        log.info("Copying User Finished...")
        render([result: "success"] as JSON)
    }

    def copyBulkUserGroups(String userName, String jsonData) {
        log.info("Copying User Group Started by user ${userName} ...")
        log.info("jsonData is = " + jsonData)
        seedDataService.copyUserGroups(userName, MiscUtil.parseJsonText(jsonData) as Map, params)
        log.info("Copying User Group Finished...")
        render([result: "success"] as JSON)
    }

    def fetchUserDetail(String userName) {
        log.info("Fetching User Details for userName ${userName} ")
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            def validUser = User.findByUsernameIlike(userName)?.toUserMap()
            if (validUser) {
                log.debug("User with username: ${userName} found successfully")
                responseDTO.setSuccessResponse(validUser, "User with username: ${userName} found successfully")
            } else {
                log.warn("User with username: ${userName} not found")
                responseDTO.setMessage("User with username: ${userName} not found")
            }
        } catch (Exception ex) {
            log.error("Exception occured while fetching username: ${userName}")
            responseDTO.setFailureResponse("${customMessageService.getMessage('default.server.error.message')}, ${ex}")
        }
        render(responseDTO as JSON)
    }

    // Method for sending chatMessage in a ms teams channel. ref: https://learn.microsoft.com/en-us/graph/api/chatmessage-post?view=graph-rest-1.0&tabs=http
    def sendTeamsNotification() {
        def requestJson = request.JSON
        Long executionId = Long.valueOf(requestJson.executionId)
        String etlStartDateTime = requestJson.etlStartDateTime
        String etlFailedDateTime = requestJson.etlFailedDateTime
        String errorTableName = requestJson.errorTableName
        log.info("Start sending TeamsNotification for executionId : ${executionId}")
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            String authorizedToken = Holders.config.getProperty('rxlogix.onedrive.accessToken')
            String jobName = SourceProfile.getCentral()?.sourceName?.toLowerCase()?.equals(Constants.PVCM) ? "Data Sync Job" : "ETL"
            String pvaUrl = grailsApplication.config.dataSources.pva.url
            String serviceName = pvaUrl.substring(pvaUrl.lastIndexOf("/") + 1)
            String teamNotificationUrl = "${grailsApplication.config.rxlogix.ms.channel.message.url}/teams/${grailsApplication.config.rxlogix.ms.teamsId}/channels/${grailsApplication.config.rxlogix.ms.teams.channelId}/messages"
            String requestBody = """{
                "body": {
                        "contentType": "html",
                        "content": "<b>${jobName} Status -</b> FAILED (EXECUTION ID - ${executionId}) | ${utilService.getHostIdentifier().replace("_", ".")} | ${grailsApplication.config.dataSources.pva.username} | ${serviceName} <br> ETL started at ${etlStartDateTime} has been failed at ${etlFailedDateTime} while populating/Calculating data in ${errorTableName}. <br> Please contact system administrator."}
            }"""
            def responseForTeams = oneDriveRestService.doPost(teamNotificationUrl, requestBody, 'application/json', authorizedToken)
            if (responseForTeams.code != 200 && responseForTeams.code != 201) {
                log.warn("Failed to send teams notification for ETL execution id: ${executionId} due to : ${responseForTeams.message}")
                Map data = responseForTeams.message ? (MiscUtil.parseJsonText(responseForTeams.message) as Map) : [:]
                data.executionId = executionId
                responseDTO.setFailureResponse(data, "Failed to send Teams notification")
            } else {
                log.info("Teams notification sent successfully for ETL execution id: ${executionId}")
                Map data = responseForTeams.message ? (MiscUtil.parseJsonText(responseForTeams.message) as Map) : [:]
                data.executionId = executionId
                responseDTO.setSuccessResponse(data, "Teams notification sent successfully")
            }
        } catch (Exception ex) {
            log.error("Exception occured while sending teams Notification for ETL execution id: ${executionId}")
            Map data = ["executionId": executionId]
            responseDTO.setFailureResponse(data, "Failed to send Teams notification :: ${ex.toString()}")
        }
        render(responseDTO as JSON)
    }

    def fetchUser() {
        String token = request.getHeader("X-Auth-Token")
        log.info("temp token received from admin: " + token)
        def status
        Map result = [:]
        if (token) {
            String originalSessionToken = executorThreadInfoService.getTempTokenForAdmin(token)
            log.debug(" session token received from hazelcast: " + originalSessionToken)
            if (originalSessionToken) {
                String decodedSessionId = RxCodec.decode(originalSessionToken)
                Session activeSession = sessionRepository.findById(decodedSessionId)
                if (activeSession) {
                    User user = User.findByUsernameIlike(activeSession.getAttribute("javamelody.remoteUser"))
                    if (user) {
                        result.username = user.username
                        result.fullName = user.fullName
                        result.email = user.email
                        result.fullNameAndUsername = user.fullName + " (" + user.username + ")"
                        result.date = user.lastToLastLogin
                        result.timeZone = user.preference?.timeZone
                        result.formatName = 'user.lastLogin.date.format'
                        if (!user.lastToLastLogin) {
                            result.lastToLastLogin = "User Never logged in."
                        } else {
                            result.lastToLastLogin = (formatDate(result) + " (${message(code: 'app.timezone.TZ.GMT')} ${TimeZoneEnum.values().find { it.timezoneId == result.timeZone }?.gmtOffset})")
                        }
                        result.externalUser = true
                        result.globalUser = true
                        result.type = Holders.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean) ? "LDAP User" : ""
                        result.authType = null
                        result.role = user.getAuthorities()?.stream()?.map({ r -> r.getAuthority() })?.collect(Collectors.toList())
                        status = HttpStatus.OK
                        log.debug("User Details: " + result)
                    } else {
                        status = HttpStatus.NOT_FOUND
                        result.message = "No User found."
                    }
                } else {
                    status = HttpStatus.UNAUTHORIZED
                    result.message = "No Active session found."
                }
            } else {
                status = HttpStatus.UNAUTHORIZED
                result.message = "User do not have Access"
            }
        } else {
            status = HttpStatus.UNAUTHORIZED
            result.message = "User do not have Access"
        }
        log.info("fetch user details Completed. " + result.message)

        render(status: status, text: (result as JSON).toString(), contentType: 'application/json')
    }

    def getUserDefinedFields() {
        def jsonFieldMap = request.getJSON()
        Sql sql = new Sql(dataSource_pva)
        FieldResponseDTO userDefinedDTO = new FieldResponseDTO()
        try {
            jsonFieldMap.each { it ->
                log.info("Calling PV Admin Api to Update the Db fiels for unique_field_id-> {} , tenantId -> {} and langId -> {}", it.fieldId, it.tenantId, it.langId)
                if (it.fieldId && it.tenantId && it.langId) {
                    it.langId = (it.langId.toString().toLowerCase().trim().equals('en')) ? '*' : it.langId
                    if (it.fieldId.contains(Constants.USER_DEFINED_FIELD)) {
                        sql.call('{call pkg_pvr_app_util.P_CREATE_CSTM_CL_SYNONYM(?,?,?)}', [it.fieldId, it.langId, it.tenantId])
                    }
                    reportFieldService.getDataFromVWRPTFIELD(it.fieldId.toString(), it.tenantId, it.langId.toString())
                    String fieldVariable = reportFieldService.getFieldVariableForUniqueId(it.fieldId, it.langId.toString(), it.tenantId)
                    ConfigObject hazelcast = grailsApplication.config.hazelcast
                    Map allLangMap = reportFieldService.loadSingleValuesToCacheFile(fieldVariable)
                    if (fieldVariable) {
                        if (hazelcast.enabled) {
                            String cacheChannel = hazelcast.notification.cacheChannel
                            hazelService.publishToTopic(cacheChannel, (allLangMap as JSON).toString())
                        } else {
                            Localization.resetAll()
                            reportFieldService.clearAllCaches()
                            reportFieldService.addFileDatatoCache(allLangMap)
                        }
                    }
                } else {
                    throw new ParameterMissingException("Invalid Parameters -> unique field id , tenantId or lang id is null")
                }
                log.info("PVAdmin Api Call Ended!")
            }
            userDefinedDTO.setSuccessResponse(1, "SUCCESS", "SUCCESS", ["text": "Field Definition Data Refreshed Successfully"])
            response.status = HttpStatus.OK.value()
        }
        catch (ParameterMissingException ilexc) {
            log.error("Error Occurred due to", ilexc)
            userDefinedDTO.setFailureResponse(HttpStatus.BAD_REQUEST.value(), "FAILURE", "FAILURE", ["text": ilexc.message])
            response.status = HttpStatus.BAD_REQUEST.value()
        }
        catch (SQLException sqlException) {
            log.error("Error Ocurred During Saving Views Data in Mart", sqlException)
            userDefinedDTO.setFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "FAILURE", "FAILURE", ["text": sqlException.message])
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        }
        catch (Exception ex) {
            log.error("Error Ocurred During Calling PV-Admin Api", ex)
            userDefinedDTO.setFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "FAILURE", "Failed to Add data for Specific identifier", ["text": ex.message])
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        }
        finally {
            sql?.close()
        }
        render(userDefinedDTO as JSON)
    }

    def getRollBackFields() {
        def jsonFieldMap = request.getJSON()
        Sql sql = new Sql(dataSource_pva)
        FieldResponseDTO userDefinedDTO = new FieldResponseDTO()
        try {
            jsonFieldMap.each { it ->
                log.info("RollBack Occured For the Mentioned Fields->  unique_field_id-> {} , tenantId -> {} and langId -> {}", it.fieldId, it.tenantId, it.langId)
                if (it.fieldId && it.tenantId && it.langId) {
                    it.langId = (it.langId.toString().toLowerCase().trim().equals('en')) ? '*' : it.langId
                    if (it.fieldId.contains(Constants.USER_DEFINED_FIELD)) {
                        sql.call('{call pkg_pvr_app_util.P_CREATE_CSTM_CL_SYNONYM(?,?,?)}', [it.fieldId, it.langId, it.tenantId])
                    }
                    reportFieldService.getDataFromVWRPTFIELD(it.fieldId.toString(), Long.parseLong(it.tenantId), it.langId.toString())
                    String fieldVariable = reportFieldService.getFieldVariableForUniqueId(it.fieldId, it.langId.toString(), Long.parseLong(it.tenantId))
                    ConfigObject hazelcast = grailsApplication.config.hazelcast
                    Map allLangMap = reportFieldService.loadSingleValuesToCacheFile(fieldVariable)
                    if (fieldVariable) {
                        if (hazelcast.enabled) {
                            String cacheChannel = hazelcast.notification.cacheChannel
                            hazelService.publishToTopic(cacheChannel, (allLangMap as JSON).toString())
                        } else {
                            Localization.resetAll()
                            reportFieldService.clearAllCaches()
                            reportFieldService.addFileDatatoCache(allLangMap)
                        }
                    }
                } else {
                    throw new ParameterMissingException("Invalid Parameters -> unique field id , tenantId or lang id is null")
                }
                log.info("Roll Back Api Call Ended!")
            }
            userDefinedDTO.setSuccessResponse(1, "SUCCESS", "SUCCESS", ["text": "Field Definition RollBack Done Successfully"])
        }
        catch (ParameterMissingException ilexc) {
            log.error("Error Occurred due to", ilexc)
            userDefinedDTO.setFailureResponse(HttpStatus.BAD_REQUEST.value(), "FAILURE", "FAILURE", ["text": ilexc.message])
            response.status = HttpStatus.BAD_REQUEST.value()
        }
        catch (SQLException sqlException) {
            log.error("Error Occurred During Saving Views Data in Mart", sqlException)
            userDefinedDTO.setFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "FAILURE", "FAILURE", ["text": sqlException.message])
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        }
        catch (Exception ex) {
            log.error("Error Occurred During Calling PV-Admin Api", ex)
            userDefinedDTO.setFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "FAILURE", "Failed to Add data for Specific identifier", ["text": ex.message])
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        }
        finally {
            sql?.close()
        }
        render(userDefinedDTO as JSON)
    }

    /**
     * This method is an API being exposed to PV Admin for fetching list of blinded User Group names.
     * <b>Request Attributes :</b><br>
     * <ul>
     *   <li>Type : GET</li>
     * </ul>
     * <b>Response Attributes :</b><br>
     * <ul>
     *   <li>Header : <code>status</code> -> Status code indicating Success or Failure</li>
     *   <li>Body : A JSON containing the list of blinded user group names. It has the following attributes ->
     *     <ul>
     *       <li><code>resultCode</code> – Status code</li>
     *       <li><code>resultStatus</code> – "SUCCESS" or "FAILURE"</li>
     *       <li><code>resultMsg</code> – Additional detail or error message</li>
     *       <li><code>result</code> – Map containing result data</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return Response – The response is rendered as JSON directly to the HTTP response.
     */
    Response fetchBlindedUserGroups() {
        FieldResponseDTO responseDTO = new FieldResponseDTO()
        List<String> blindedUserGroupNames = UserGroup.findAllByIsBlinded(true).collect {it.name}
        response.status = HttpStatus.OK.value()
        responseDTO.setSuccessResponse(HttpStatus.OK.value(), Constants.SUCCESS, "Blinded User Group names fetch successfully", ["list": blindedUserGroupNames])
        render(responseDTO as JSON)
    }

    Response fetchBlindedUsers(String usernames) {
        log.info("Fetching blinded user details for users ${usernames}")
        ResponseDTO responseDTO = new ResponseDTO()
        List<String> nonExistingUsers = []
        Map responseMap = [:]
        String adminProfileName = Holders.config.getProperty("pvadmin.privacy.field.profile")
        try {
            if (usernames.isEmpty() || usernames == "") {
                log.info("No user found in PV-Signal User list")
                responseDTO.setSuccessResponse(null, "User request data is empty.")
            } else {
                List<String> usernamesList = usernames.split(',')
                List<BlindedUsersDTO> blindedUsersDTOList = []
                usernamesList.each {
                    User user = User.findByUsername(it)
                    if (!user) {
                        nonExistingUsers.add(it)
                        blindedUsersDTOList.add(new BlindedUsersDTO(it))
                    } else if (!user.enabled) {
                        blindedUsersDTOList.add(new BlindedUsersDTO(true, user, it))
                    } else {
                        BlindedUsersDTO blindedUsersDTO = new BlindedUsersDTO(false, user, it)
                        blindedUsersDTO.blindedFieldIds = userService.fetchUniqueFieldIdList(blindedUsersDTO.blindedFieldIds)
                        blindedUsersDTO.protectedFieldIds = userService.fetchUniqueFieldIdList(blindedUsersDTO.protectedFieldIds)
                        blindedUsersDTOList.add(blindedUsersDTO)
                    }
                }
                responseMap.put("users", blindedUsersDTOList)
                responseMap.put("adminProfileName", adminProfileName)
                List<String> adminProfileFields = FieldProfileFields.findAllByFieldProfileAndIsProtected(FieldProfile.findByNameAndIsDeleted(adminProfileName, false), true).collect {it.reportField.name}
                responseMap.put("adminProfileFields", adminProfileName ? userService.fetchUniqueFieldIdList(adminProfileFields) : [])
                responseDTO.setSuccessResponse(responseMap, "User data for blinded fields fetched successfully, Users ${nonExistingUsers} not found while fetching blinded data")
            }
        } catch (Exception ex) {
            log.error("Exception occurred while fetching blinded user details for PVS")
            responseDTO.setFailureResponse("${customMessageService.getMessage('default.server.error.message')}, ${ex}")
        }
        log.info("Executed fetchBlindedUsers for PVS, Users ${nonExistingUsers} not found while fetching blinded data")
        render(responseDTO as JSON)
    }

    /**
     * This method is an API being exposed to PV Admin for automatic Field Profile update.
     * <p>
     * This is used for syncing Privacy Profile data from PV Admin to PVR.
     * <p>
     * <b>Request Attributes :</b><br>
     * <ul>
     *   <li>Type : POST</li>
     *   <li>Header : <code>PVR_PUBLIC_TOKEN</code> – Authentication token</li>
     *   <li>Body : A JSON containing list of report fields that were updated in PV Admin. Each entity in the list will have 3 attributes ->
     *     <ul>
     *       <li><code>fieldId</code> – ID of the field</li>
     *       <li><code>tenantId</code> – Tenant identifier</li>
     *       <li><code>langId</code> – Language identifier</li>
     *     </ul>
     *   </li>
     * </ul>
     * <b>Response Attributes :</b><br>
     * <ul>
     *   <li>Header : <code>status</code> -> Status code indicating Success or Failure</li>
     *   <li>Body : A JSON containing the code and message for API Success or Failure. It has the following attributes ->
     *     <ul>
     *       <li><code>code</code> – Status code</li>
     *       <li><code>status</code> – "SUCCESS" or "FAILURE"</li>
     *       <li><code>message</code> – Additional detail or error message</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return Response – The response is rendered as JSON directly to the HTTP response.
     */
    Response updatePrivacyFieldProfile() {
        Map<String, List<Map>> fields = request.getJSON() as Map<String, List<Map>>
        PrivacyProfileResponseDTO responseDTO = new PrivacyProfileResponseDTO()
        String privacyProfileName = Holders.config.getProperty("pvadmin.privacy.field.profile")

        if (privacyProfileName) {
            FieldProfile fieldProfile = FieldProfile.findByNameAndIsDeleted(privacyProfileName, false)

            if (fieldProfile) {
                String missingKey = utilService.validateFieldJSON(fields.privacyFieldList)
                if (missingKey != null) {
                    log.error("Report Field JSON is Invalid, ${missingKey} is missing from the JSON")
                    response.status = HttpStatus.BAD_REQUEST.value()
                    responseDTO.setFailureResponse("Report Field JSON is Invalid")
                } else {
                    log.info("Updating privacy profile - ${privacyProfileName}")
                    responseDTO = utilService.updatePrivacyFieldProfile(fields, fieldProfile)
                    response.status = responseDTO.code
                }
            } else {
                log.error("Privacy Field Profile does not exist")
                response.status = HttpStatus.BAD_REQUEST.value()
                responseDTO.setFailureResponse("Privacy Field Profile does not exist")
            }
        } else {
            log.error("Value not set for config pvadmin.privacy.field.profile")
            response.status = HttpStatus.BAD_REQUEST.value()
            responseDTO.setFailureResponse("Value not set for config pvadmin.privacy.field.profile in PVR")
        }
        render(responseDTO as JSON)
    }
}
