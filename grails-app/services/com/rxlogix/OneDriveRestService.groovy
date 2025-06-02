package com.rxlogix

import com.bhyoo.onedrive.client.Client
import com.bhyoo.onedrive.container.items.FolderItem
import com.bhyoo.onedrive.container.items.pointer.PathPointer
import com.bhyoo.onedrive.network.async.UploadFuture
import com.rxlogix.config.*
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders
import com.rxlogix.Constants

import java.nio.file.Paths

class OneDriveRestService {

    private static Client CLIENT_OBJECT = null
    private static String AUTH_CODE = null
    static String STATIC_ACK_FILE = "CASE_ACK_FILE.xml"
    final static Long MINUTE_50 = 1000 * 60 * 50

    def userService
    def grailsApplication
    def CRUDService
    def dynamicReportService


    void loadOneDriveClient() {
        String clientId = Holders.config.getProperty('oneDrive.clientId')
        String[] scope = ["Files.ReadWrite.All", "offline_access", "ChannelMessage.Send", "Group.ReadWrite.All"] as String[];
        String redirectURL = getCallBackUrl()
        String clientSecret = Holders.config.getProperty('oneDrive.secret')
        Client client = new Client(clientId, scope, redirectURL, URLEncoder.encode(clientSecret, Constants.UTF8), false);
        CLIENT_OBJECT = client
    }

    void setAuthCode(String authCode) {
        AUTH_CODE = authCode;
        CLIENT_OBJECT.authHelper.setExternalAuthCode(authCode);
        if (authCode) {
            CLIENT_OBJECT.login()
        }

    }

    String getAuthCodeUrl() {
        return CLIENT_OBJECT.authHelper.getAuthGenerateUrl();
    }

    boolean checkOnDriveLogin() {
        return !!getOneDriveClient()?.isLogin()
    }

    private Client getOneDriveClient() {
        if (CLIENT_OBJECT && AUTH_CODE && CLIENT_OBJECT.isLogin()) {
            return CLIENT_OBJECT
        }
        log.warn("Not able to get valid CLIENT session")
        return null
    }

    void upload(String folderPath, File f) {
        Client client = getOneDriveClient()
        if (!client) {
            return
        }
        log.debug("Uploading file on OneDrive on Path ${folderPath}")
        UploadFuture future = client.uploadFile(new PathPointer(folderPath), Paths.get(f.absolutePath));
        future.syncUninterruptibly()
        log.debug("Uploaded file on OneDrive on Path ${folderPath}")
    }

    void checkIfAckReceived(String incomingFolder) {
        Client client = getOneDriveClient()
        if (!incomingFolder || !client) {
            return
        }
        log.debug("Checking ACK Files")
        FolderItem folder = client.getFolder(new PathPointer("${incomingFolder}"))
        folder.fileChildren().each {
            if (it.name == STATIC_ACK_FILE) {
                log.debug("Mark case as acknowledged")
                it.delete()
            }
        }
    }
// --rest implementation ---------
    byte[] getCheckInFile(String id, User user) {
        OneDriveUserSettings oneDriveUserSettings = OneDriveUserSettings.findByUser(user)
        return getFile(grailsApplication.config.officeOnline.oneDriveProvider.siteId, id, oneDriveUserSettings).data
    }

    Map getFile(String siteId, String itemId, OneDriveUserSettings settings) {
        Map result = [error: false, errorMessage: "", contentType: "", name: "", data: []]
        try {

            String site = (siteId == "drive" ? "/me" : "/sites/${siteId}")
            String url = grailsApplication.config.oneDrive.api.url + site + "/drive/items/${itemId}"
            def response = doGet(url,  getAccessTokenForSettings(settings))

            if (response.code != 200) {
                return [error: true, code:response.code, exception: false, errorMessage: response.message]
            }
            def itemsJson = JSON.parse(response.message)

            result.data = new URL(itemsJson."@microsoft.graph.downloadUrl").bytes
            result.contentType = itemsJson.file.mimeType
            result.name = itemsJson.name
            return result

        } catch (Throwable t) {
            log.error("Error occurred when downloading file from oneDrive", t)
            return [error: true, exception: t]
        }
    }

    void saveReports(ExecutedReportConfiguration configuration) {
        try {
            if (configuration?.executedDeliveryOption?.oneDriveFolderId && configuration?.executedDeliveryOption?.oneDriveUserSettings)
                configuration?.executedDeliveryOption?.oneDriveFormats?.each { ReportFormatEnum it ->

                    File reportFile
                    if (configuration instanceof ExecutedPeriodicReportConfiguration && configuration.status == ReportExecutionStatusEnum.GENERATED_CASES) {
                        reportFile = dynamicReportService.createCaseListReport(configuration.caseSeries, [outputFormat: it.name()])
                    } else if (configuration instanceof ExecutedCaseSeries) {
                        reportFile = dynamicReportService.createCaseListReport(configuration, [outputFormat: it.name(), showVersionColumn: ApplicationSettings.first().defaultUiSettings ? "false" : "true"])
                    } else {
                        reportFile = dynamicReportService.createMultiTemplateReport(configuration, [outputFormat: it.name()])
                    }
                    User user = configuration.executedDeliveryOption.oneDriveUserSettings.user
                    String fileName = configuration.reportName + "." + it.name()
                    Map result = uploadFile(configuration?.executedDeliveryOption?.oneDriveFolderId, configuration?.executedDeliveryOption?.oneDriveSiteId, fileName, reportFile.bytes, getAccessToken(user))
                    if (result.code != 201) {
                        log.error("Error occurred when uploading report to oneDrive. OneDrive response: " + result.message)
                    }
                }


        } catch (Throwable t) {
            log.error("Error occurred when uploading report to oneDrive", t)
        }
    }

    String getCallBackUrl() {
        return grailsApplication.config.oneDrive.redirectUrl?:(grailsApplication.config.grails.appBaseURL + "/oneDrive/callback")
    }

    def refreshOneDriveSession() {
        if (grailsApplication.config.oneDrive.enabled) {
            OneDriveUserSettings.findAllByLastRefreshLessThan(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 30))?.each {
                User u = it.user
                try {
                    if (u.enabled) getAccessToken(u)
                } catch (e) {
                    log.error("Unexpected exception occurred trying to refresh OneDrive session token for User : " + u.username, e)
                }
            }
            log.info "RefreshOneDriveSession job executed"
        }
    }

    String getAccessToken(User user = null) {
        User currentUser = user ?: userService.currentUser
        OneDriveUserSettings oneDriveUserSettings = OneDriveUserSettings.findByUser(currentUser)
        return getAccessTokenForSettings(oneDriveUserSettings)

    }
    String getAccessTokenForSettings(OneDriveUserSettings oneDriveUserSettings ) {

        if (!oneDriveUserSettings)
            return null

        if (oneDriveUserSettings.lastRefresh.getTime() + MINUTE_50 > System.currentTimeMillis())
            return oneDriveUserSettings.accessToken

        //oneDriveUserSettings.accessToken expired, we need to refresh it
        oneDriveUserSettings.discard()
        oneDriveUserSettings = OneDriveUserSettings.lock(oneDriveUserSettings.id)

        String clientSecret = URLEncoder.encode(grailsApplication.config.oneDrive.secret,Constants.UTF8)
        String attrs = "client_id=${grailsApplication.config.oneDrive.clientId}&redirect_uri=${getCallBackUrl()}&client_secret=${clientSecret}" +
                "&refresh_token=${oneDriveUserSettings.refreshToken}&grant_type=refresh_token&scope=offline_access%20openid"

        def result = doPost(grailsApplication.config.oneDrive.auth.token.url, attrs, "application/x-www-form-urlencoded")

        return updateOneDriveUserSettings(result.message.toString(), oneDriveUserSettings)
    }

    String updateOneDriveUserSettings(String responce, oneDriveUserSettings) {
        try {
            def responseJson = JSON.parse(responce)
            String access_token = responseJson.access_token
            String refreshToken = responseJson.refresh_token
            oneDriveUserSettings.refreshToken = refreshToken
            oneDriveUserSettings.accessToken = access_token
            oneDriveUserSettings.lastRefresh = new Date()
            CRUDService.saveOrUpdate(oneDriveUserSettings)
            return access_token
        } catch (e) {
            log.error("Unexpected exception in updateOneDriveUserSettings, response: " + responce, e)
        }
        return null
    }

    void createAccessToken(String code) {

        String clientSecret = URLEncoder.encode(grailsApplication.config.oneDrive.secret,Constants.UTF8)
        String attrs = "client_id=${grailsApplication.config.oneDrive.clientId}&redirect_uri=${getCallBackUrl()}&client_secret=${clientSecret}" +
                "&code=${code}&grant_type=authorization_code&scope=offline_access%20openid"

        def result = doPost(grailsApplication.config.oneDrive.auth.token.url, attrs, "application/x-www-form-urlencoded")

        User currenUser = userService.currentUser
        OneDriveUserSettings oneDriveUserSettings = OneDriveUserSettings.findByUser(currenUser)
        if (!oneDriveUserSettings) oneDriveUserSettings = new OneDriveUserSettings(user: currenUser)

        updateOneDriveUserSettings(result.message.toString(), oneDriveUserSettings)
    }

    Map uploadFile(folderId, siteId, fileName, data, access_token) {
        String site = (siteId == "drive" ? "/me" : "/sites/${siteId}")
        String url = grailsApplication.config.oneDrive.api.url + site + "/drive/items/${folderId}:/${URLEncoder.encode(fileName, "UTF-8")}:/createUploadSession?"
        String requestBody = """{"item": {"@name.conflictBehavior": "rename"}}"""
        def response = doPost(url, requestBody, 'application/json', access_token)

        if (response.code != 200) {
            return response
        }
        def uploadUrl = JSON.parse(response.message).uploadUrl
        return doPut(uploadUrl, data, access_token)
    }

    Map doPost(String baseUrl, String queryString, String contentType, String secret = null) {
        def result = [code: null, message: ""]
        def connection = new URL(baseUrl).openConnection()
        connection.with {
            doOutput = true
            requestMethod = 'POST'
            setRequestProperty("Content-Type", contentType);
            setRequestProperty("Accept", "*/*");
            if (secret)
                setRequestProperty("Authorization", "bearer " + secret);
            if (queryString) {
                outputStream.withWriter { writer ->
                    writer << queryString
                }
            } else {
                getOutputStream().close();
            }
            connect()
            result.code = responseCode
            if ((result.code != 200) && (result.code != 201)) {
                result.message = getErrorStream().text
            } else
                result.message = content.text
        }
        result
    }

    Map doPut(String baseUrl, byte[] data, String secret) {
        def result = [code: null, message: ""]
        int size = data.length
        def connection = new URL(baseUrl).openConnection()
        connection.with {
            doOutput = true
            requestMethod = 'PUT'
            setRequestProperty("Content-Length", "" + size);
            setRequestProperty("Content-Range", "bytes 0-${size - 1}/${size}");
            setRequestProperty("Authorization", "bearer " + secret);
            setRequestProperty("Accept", "*/*");
            outputStream.write(data)
            connect()
            result.code = responseCode
            if (!(result.code in [201, 200])) {
                result.message = getErrorStream().text
            } else
                result.message = content.text
        }
        result
    }

    Map doGet(String url, String secret = null) {
        def result = [code: null, message: ""]
        def connection = new URL(url).openConnection()
        connection.with {
            doOutput = true
            requestMethod = 'GET'
            setRequestProperty("Accept", "application/json, text/plain, */*");
            if (secret)
                setRequestProperty("Authorization", "bearer " + secret);
            result.code = responseCode
            if (result.code != 200) {
                result.message = getErrorStream().text
            } else
                result.message = content.text
        }
        result
    }

    Map doDelete(String url, String secret = null) {
        def result = [code: null, message: ""]
        def connection = new URL(url).openConnection()
        connection.with {
            doOutput = true
            requestMethod = 'DELETE'
            setRequestProperty("Accept", "application/json, text/plain, */*");
            if (secret)
                setRequestProperty("Authorization", "bearer " + secret);
            result.code = responseCode
            if (result.code != 204) {
                result.message = getErrorStream().text
            } else
                result.message = ""
        }
        result
    }

    Map processDownloadUrl(String baseUrl, String cookie, String postBody) {
        def result = [code: null, message: "", data: null]
        def connection = new URL(baseUrl).openConnection()
        connection.with {
            doOutput = true
            setRequestProperty("Accept", "*/*");
            if (cookie) {
                setRequestProperty("Cookie", cookie);
            }
            if (postBody) {
                requestMethod = 'POST'
                outputStream.withWriter { writer ->
                    writer << postBody
                }
            }
            connect()
            result.code = responseCode
            if (result.code != 200) {
                result.message = getErrorStream().text
            } else
                result.data = content.bytes
        }
        result
    }

    void updateAccessRights(Collection<User> users, String itemId) {
        String url = getApiUrl() + "/drive/items/${itemId}/permissions"
        def response = doGet(url, getAccessToken())
        def itemsJson = JSON.parse(response.message)
        List toDelete = []
        itemsJson.value?.each { permission ->
            if (permission.grantedToIdentitiesV2?.find { item -> item?.user?.email } ||
                    permission.grantedToV2?.user ||
                    permission.grantedToV2?.siteGroup?.loginName?.contains("Visitor") ||
                    permission.grantedToV2?.siteGroup?.loginName?.contains("Members")
            ) {
                toDelete << permission.id
            }
        }
        boolean result = true
        toDelete?.each {
            response = doDelete(url + "/" + it, getAccessToken())
            if (response.code != 204) {
                result = false
                log.error(response.message)
            }
        }
        share(users, itemId)
    }

    String getWebUrl(String itemId) {
        String url = getApiUrl() + "/drive/items/${itemId}"
        def response = doGet(url, getAccessToken())
        def itemsJson = JSON.parse(response.message)
        if (response.code != 200) return grailsApplication.config.grails.appBaseURL + "/wopi/error404?err=" + itemsJson.error?.message
        return itemsJson.webUrl
    }

    boolean removeItem(String itemId) {
        def result = true
        if (itemId) {
            def response = doDelete(getApiUrl() + "/drive/items/" + itemId, getAccessToken())
            if (response.code != 204) {
                result = false
                log.error(response.message)
            }
        }
        result
    }

    private getApiUrl() {
        String siteId = grailsApplication.config.officeOnline.oneDriveProvider.siteId
        return grailsApplication.config.oneDrive.api.url + (siteId == "drive" ? "/me" : "/sites/${siteId}")
    }

    void share(Collection<User> users, String itemId) {
        if (grailsApplication.config.officeOnline.oneDriveProvider.enabled) {

            String shareAccessUrl = getApiUrl() + "/drive/items/" + itemId + "/invite"
            //String shareAccessUrl = "https://graph.microsoft.com/v1.0/sites/rxlogix.sharepoint.com,cef0dbf6-47e2-423e-9926-0f19e621c61e,48f4cdd0-27dd-412e-a566-248c9cf8a8b8/drive/items/${result.id}/invite"
            String emails = users.findAll { it }.collect { '{"email": "' + it.email + '"}' }.join(",")
            String body = """{
                            "recipients": [${emails}],
                            "message": "${ViewHelper.getMessage("app.label.publisher.share.email")}",
                            "requireSignIn": true,
                            "sendInvitation": false,
                            "roles": ["write"]
                     } """

            def response = doPost(shareAccessUrl, body, 'application/json', getAccessToken())
            if (!(response.code in [201, 200])) {
                String error = "Error occurred when sharing access to file on oneDrive. OneDrive response: " + response.message
                log.error(error)
                throw new Exception(error)
            }
        }
    }

    Map pullChanges(String itemId) {
        if (grailsApplication.config.officeOnline.oneDriveProvider.enabled) {
            OneDriveUserSettings oneDriveUserSettings = OneDriveUserSettings.findByUser(userService.currentUser)
            Map result = getFile(grailsApplication.config.officeOnline.oneDriveProvider.siteId, itemId, oneDriveUserSettings)
            if (result.error) {
                if (result.code == 423) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.locked")]
                if (result.code == 404) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.notFound")]
                if (result.code == 410) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.notFound")]
                if (result.code == 403) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.noRignts")]
                if (result.code == 401) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.noRignts")]
                return [error: true, "message:": result.errorMessage]
            }
            return [error: false, data: result.data]
        }
    }

    Map pushChanges(String itemId, byte[] data) {
        if (grailsApplication.config.officeOnline.oneDriveProvider.enabled) {
            def result = updateOneDriveFile(data, itemId)
            if (result.code == 409) return [error: true, code: result.code, "message": "(409) " + ViewHelper.getMessage("app.publisher.locked")]
            if (result.code == 423) return [error: true, code: result.code, "message": "(423) " + ViewHelper.getMessage("app.publisher.locked")]
            if (result.code == 404) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.notFound")]
            if (result.code == 410) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.notFound")]
            if (result.code == 403) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.noRignts")]
            if (result.code == 401) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.noRignts")]
            if (result.code > 300) return [error: true, code: result.code, "message": ViewHelper.getMessage("app.publisher.unknownError") + " " + result.message]
            return [error: false]
        }
    }


    def uploadOneDriveFile(String name, byte[] data, String folder) {
        String siteId = grailsApplication.config.officeOnline.oneDriveProvider.siteId
        String folderId = grailsApplication.config.officeOnline.oneDriveProvider.folderId
        String access_token = getAccessToken()
        String site = (siteId == "drive" ? "/me" : "/sites/${siteId}")
        checkOrCreateFolder(folderId, site, access_token)
        String file = "/drive/root:/${folderId}/${(folder ? (URLEncoder.encode(folder, "UTF-8") + "/") : "")}${URLEncoder.encode(name + ".docx", "UTF-8")}:"
        String url = grailsApplication.config.oneDrive.api.url + site + file + "/createUploadSession?%40microsoft.graph.conflictBehavior=rename&@name.conflictBehavior=rename"
        String requestBody = """{"item": {"@microsoft.graph.conflictBehavior": "rename"}}"""
        def response = doPost(url, requestBody, 'application/json', access_token)

        if (response.code != 200) {
            return response
        }
        def uploadUrl = JSON.parse(response.message).uploadUrl
        def result = doPut(uploadUrl, data, access_token)
        if (!(result.code in [201, 200])) {
            throw new Exception("Error occurred when uploading file to oneDrive. OneDrive response: " + result.message)
        }
        JSON.parse(result.message)

    }

    Map updateOneDriveFile(byte[] data, String itemId) {
        String siteId = grailsApplication.config.officeOnline.oneDriveProvider.siteId
        String access_token = getAccessToken()
        String site = (siteId == "drive" ? "/me" : "/sites/${siteId}")
        String file = "/drive/items/" + itemId
        String url = grailsApplication.config.oneDrive.api.url + site + file + "/createUploadSession?%40microsoft.graph.conflictBehavior=replace&@name.conflictBehavior=replace"
        String requestBody = """{"item": {"@microsoft.graph.conflictBehavior": "replace"}}"""
        def response = doPost(url, requestBody, 'application/json', access_token)

        if (response.code != 200) {
            return response
        }
        def uploadUrl = JSON.parse(response.message).uploadUrl
        response = doPut(uploadUrl, data, access_token)
        if (response.code > 300) doDelete(uploadUrl, access_token)
        return response
    }

    private String checkOrCreateFolder(String folderId, String site, String access_token) {
        String url = grailsApplication.config.oneDrive.api.url + site + "/drive/root:/${folderId}"
        def resp = doGet(url, access_token)
        if (resp.code == 404) {
            url = grailsApplication.config.oneDrive.api.url + site + "/drive/root/children"
            String queryString = """{ "name": "${folderId}", "folder": { }, "@microsoft.graph.conflictBehavior": "rename"}"""
            resp = doPost(url, queryString, "application/json", access_token)
            if (resp.code != 201) {
                throw new Exception("OneDrive was unable to create pvp folder for user!")
            }
        }
        return JSON.parse(resp.message).id
    }

    def listFiles(String siteId, String folder) {
        String access_token = getAccessToken()
        if (access_token) {
            String site = (siteId == "drive" ? "/me" : "/sites/${siteId}")
            String url = grailsApplication.config.oneDrive.api.url + site + "/drive/items/" + folder + "/children"
            def response = doGet(url, access_token)
            if (response.code != 200) return [status: response.code, text: response.message]
            def itemsJson = JSON.parse(response.message)
            List items = []
            itemsJson.value.each { item ->

                if (!item.folder) {
                    String downloadUrl = item["@microsoft.graph.downloadUrl"]
                    if (downloadUrl && downloadUrl.contains("&"))
                        items << [name: item.name, id: item.id, url: downloadUrl.substring(0, downloadUrl.indexOf("&"))]
                }
            }
            return [status: 200, items: items]
        }
        return [status: 401, text: "Authorization to Sharepoint fault"]
    }


}
