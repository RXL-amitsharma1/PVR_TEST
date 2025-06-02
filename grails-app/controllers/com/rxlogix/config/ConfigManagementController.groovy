package com.rxlogix.config

import com.rxlogix.NotificationService
import com.rxlogix.UserService
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.admin.AdminIntegrationApiService
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileUtil
import grails.async.Promise
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovyx.net.http.Method
import org.apache.http.HttpStatus
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat

import static grails.async.Promises.task

@Secured(["isAuthenticated()"])
class ConfigManagementController {


    ConfigManagementService configManagementService
    AdminIntegrationApiService adminIntegrationApiService
    UserService userService
    static final exportExcelName = "APP_CONFIG.xlsx"

    def index() {}

    /*
    Method to generate technical configuration export excel
    */
    def generateConfigurationFile() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {

            log.debug("Generate configuration file method called")
            String filepath = Holders.config.getProperty('pvadmin.api.export.file')
            XSSFWorkbook workbook = new XSSFWorkbook()
            configManagementService.exportCriteriaSheet(workbook)
            configManagementService.exportTechnicalConfiguration(workbook)
            String dirPath = configManagementService.USER_HOME_DIR + filepath
            FileUtil.checkAndCreateDir(dirPath)
            String fileName = configManagementService.USER_HOME_DIR + filepath + "${exportExcelName}"
            Map data = ["file": fileName]
            FileOutputStream outputStream = new FileOutputStream(fileName)
            workbook.write(outputStream);
            workbook.close();
            log.debug("Generated file exported to path: " + filepath)
            responseDTO.data = data
            log.debug("Generate configuration file method Ended")
        }
        catch (Exception exception) {
            log.error("An Error Occurred while generating configuration File", exception)
            responseDTO.status= false
        }
        render(responseDTO as JSON)
    }
    /*
    Method to download export file having name format as "<Environment>_yyyy-MMM-dd HH:mm GMT.xlsx"
     */
    def downloadFile() {
        String fileName
        File file
        if (params.isExport) {
            String appURL = Holders.config.getProperty('grails.serverURL')
            def url = new URL(appURL)
            String environmentName = url.host
            file = new File(params.filepath)
            def date = new Date()
            def sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm")
            fileName = "${environmentName}_" + sdf.format(date) + "GMT.xlsx"

        } else {
            file = new File(params.filepath as String)
            fileName = file.getName()
        }
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)

    }

    /*
    Method to compare the uploaded file
   */
    def compareConfigurations() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            MultipartFile configFileFirst = params.configFileFirst
            MultipartFile configFileSecond = params.configFileSecond
            def currentUser = userService.currentUser

            log.debug("Compare configuration file method called.")
            def url = Holders.config.getProperty('pvadmin.api.url')
            def path = Holders.config.getProperty('pvadmin.api.compare.uri')
            String filePath = Holders.config.getProperty('pvadmin.api.compare.file.path')
            String firstFileName = configFileFirst.getOriginalFilename()
            String secondFileName = configFileSecond.getOriginalFilename()
            String dirPath = configManagementService.USER_HOME_DIR + filePath
            FileUtil.checkAndCreateDir(dirPath)
            configFileFirst.transferTo(new File(configManagementService.USER_HOME_DIR + filePath + "/${firstFileName}"))
            configFileSecond.transferTo(new File(configManagementService.USER_HOME_DIR + filePath + "/${secondFileName}"))
            log.debug("files moved to location : " + configManagementService.USER_HOME_DIR + filePath)
            String compareGenerateTime = DateUtil.toDateStringWithTimeInAmPmFormat(currentUser) + DateUtil.getOffsetString(currentUser.preference.timeZone)
            Map queryMap = ["configFileFirst": firstFileName, "configFileSecond": secondFileName, "path": filePath, "sheetPrimaryKeys": Holders.config.getProperty('compare.sheet.primaryKeys'), "user": currentUser.fullName, "compareGenerateTime": compareGenerateTime]
            def response = adminIntegrationApiService.postData(url, path, queryMap, Method.POST)
            log.debug("API response from admin: " + response)
            if (response.status == HttpStatus.SC_OK) {
                responseDTO.data = ["file": configManagementService.USER_HOME_DIR + response.result?.generatedFilePath]
            } else {
                log.info("An Error occurred at PV admin")
                responseDTO.status = false
            }
        } catch (Exception exception) {
            log.error(exception)
            responseDTO.status = false
        }
        log.debug("Compare configuration file method Ended")
        render(responseDTO as JSON)
    }

    /*
    Method to import configuration file
    * */
    def importDataFromFile() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            MultipartFile configFile = params.configFile
            log.debug("Import configuration file method called.")
            String baseDirectoryPath = Holders.config.getProperty('pvadmin.api.import.read.directory')
            String dirPath= configManagementService.USER_HOME_DIR + baseDirectoryPath
            FileUtil.checkAndCreateDir(dirPath)
            File convFile = new File(configManagementService.USER_HOME_DIR + baseDirectoryPath + exportExcelName)
            configFile.transferTo(convFile)

            String url = Holders.config.getProperty('pvadmin.api.url')
            String path = Holders.config.getProperty('pvadmin.api.import.url')
            Map data = ["appName": "PVR", "callbackUrl": Holders.config.getProperty('grails.appBaseURL') + "/hazelcastNotification/publishHazelCastNotification"]
            def response = adminIntegrationApiService.postData(url, path, data, Method.POST)
            log.debug("Response from admin : " + response)
            if (response.status != HttpStatus.SC_OK) {
                log.info("An error Occurred in PV-Admin")
                responseDTO.status = false
            }
        } catch (Exception exception) {
            log.error("Error occurred when importing config : " + exception)
            responseDTO.status = false
        }
        log.debug("Config file import completed successfully.")
        render(responseDTO as JSON)
    }


}
