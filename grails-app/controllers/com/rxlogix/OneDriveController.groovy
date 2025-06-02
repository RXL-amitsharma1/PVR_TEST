package com.rxlogix


import com.rxlogix.e2b.OneDriveService
import com.rxlogix.config.OneDriveUserSettings
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class OneDriveController {

    def icsrDriveService
    def oneDriveRestService
    def userService

    def index() {
        render "Welcome to One Drive. is it logged in? ${icsrDriveService.checkOnDriveLogin()}"
    }

    def callback(String code, String state) {
        if (state) {
            oneDriveRestService.createAccessToken(code)
        } else {
            icsrDriveService.setAuthCode(code)
            flash.message = "Successfully updated onedrive auth code"
            redirect(controller: 'controlPanel', action: 'index')
        }
    }

    def loadOneDriveClient() {
        icsrDriveService.loadOneDriveClient()
        flash.message = "Successfully loaded one drive client"
        redirect(controller: 'controlPanel', action: 'index')
    }

    def loginOnOnDrive() {
        redirect(url: icsrDriveService.getAuthCodeUrl())
    }

    def upload() {
        String access_token = oneDriveRestService.getAccessToken()
        if (!access_token) {
            render([status: 401, contentType: "application/json", encoding: "UTF-8", text: ""])
            return
        }
        byte[] data
        String url = params.url
        String cookies = "SESSION=" + request.getCookies()?.find { it.name == "SESSION" }?.value
        if (params.postParams) {
            url = url + (url.indexOf("?") > -1 ? "&" : "?") + "_csrf=" + params._csrf
        }
        Map res = oneDriveRestService.processDownloadUrl(url, cookies, params.postParams)
        if (res.code != 200) {
            log.error("Error: " + res.message)
        } else {
            data = res.data
        }

        if (!data) {
            render([status: 500, contentType: "application/json", encoding: "UTF-8", text: "No file content found for url: " + params.url])
            return
        }
        String fileName = params.name

        def response = oneDriveRestService.uploadFile(params.id, params.siteId, fileName, data, access_token)
        render([status: response.code, contentType: "application/json", encoding: "UTF-8", text: response.message])
    }

    def checkLogin() {
        if (oneDriveRestService.getAccessToken()) {
            render OneDriveUserSettings.findByUser(userService.currentUser).id
        } else {
            render "no"
        }

    }

    def newFolder() {
        String access_token = oneDriveRestService.getAccessToken()
        if (!access_token) {
            render([status: 401, contentType: "application/json", encoding: "UTF-8", text: ""])
            return
        }
        String site = (params.siteId == "drive" ? "/me" : "/sites/${params.siteId}")
        String url = grailsApplication.config.oneDrive.api.url + site + "/drive/root/children"
        if (params.id && params.id != "root")
            url = grailsApplication.config.oneDrive.api.url + site + "/drive/items/" + params.id + "/children"

        String queryString = """{ "name": "${params.folderName}", "folder": { }, "@microsoft.graph.conflictBehavior": "rename"}"""
        def response = oneDriveRestService.doPost(url, queryString, "application/json", access_token)
        if (response.code != 200) {
            render([status: response.code, contentType: "application/json", encoding: "UTF-8", text: response.message])
            return
        }
        render contentType: "application/json", encoding: "UTF-8", text: [:] as JSON
    }

    def listSites() {
        String access_token = oneDriveRestService.getAccessToken()
        if (!access_token) {
            render([status: 401, contentType: "application/json", encoding: "UTF-8", text: ""])
            return
        }
        String url = grailsApplication.config.oneDrive.api.url + "/sites?search="
        def response = oneDriveRestService.doGet(url, access_token)
        if (response.code != 200) {
            render([status: response.code, contentType: "application/json", encoding: "UTF-8", text: response.message])
            return
        }
        def itemsJson = JSON.parse(response.message)
        List items = []
        itemsJson.value.each { item ->
            items << [name: item.displayName, siteId: item.id]
        }
        render contentType: "application/json", encoding: "UTF-8", text: items as JSON
    }

    def folders() {
        String access_token = oneDriveRestService.getAccessToken()
        if (!access_token) {
            render([status: 401, contentType: "application/json", encoding: "UTF-8", text: ""])
            return
        }
        String site = (params.siteId == "drive" ? "/me" : "/sites/${params.siteId}")
        String url = grailsApplication.config.oneDrive.api.url + site + "/drive/root/children"
        if (params.id && params.id != "root")
            url = grailsApplication.config.oneDrive.api.url + site + "/drive/items/" + params.id + "/children"


        def response = oneDriveRestService.doGet(url, access_token)
        if (response.code != 200) {
            render([status: response.code, contentType: "application/json", encoding: "UTF-8", text: response.message])
            return
        }
        def itemsJson = JSON.parse(response.message)
        List items = []
        itemsJson.value.each { item ->
            if (item.folder) {
                items << [name: item.name, id: item.id, siteId: params.siteId, folder: true]
            } else {
                items << [name: item.name, id: item.id, siteId: params.siteId, folder: false]
            }

        }
        render contentType: "application/json", encoding: "UTF-8", text: items as JSON
    }
}
