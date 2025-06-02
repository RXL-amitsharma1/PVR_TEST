package com.rxlogix.publisher

import com.rxlogix.RxCodec
import com.rxlogix.config.OneDriveUserSettings
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherExecutedTemplate
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.io.IOUtils
import org.grails.web.json.JSONElement
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.http.MediaType
import org.springframework.web.context.request.RequestContextHolder

import java.security.MessageDigest

class WopiController {

    def userService
    def oneDriveRestService
    def publisherService
    def CRUDService

    static def cachedUrls = [:]
    static Long lastUpdate

    String getActionUrl(String app, String action, String ext) {
        //Microsoft recommends refresh it ones a day
        if (!lastUpdate || (lastUpdate < System.currentTimeMillis() - 24 * 60 * 60 * 1000)) {
            updateUrls()
        }
        def urlNode = cachedUrls['net-zone'].app.find { it['@name'] == app }?.action?.find { (it['@name'] == action) && (ext == ext) }
        return urlNode['@urlsrc'].substring(0, urlNode['@urlsrc'].indexOf("<"))
    }

    static synchronized updateUrls() {
        String xml = new URL(Holders.grailsApplication.config.officeOnline.actionsUrl).text
        def parser = new XmlParser()
        cachedUrls = parser.parseText(xml)
    }


    def view() {
        if (grailsApplication.config.officeOnline.oneDriveProvider.enabled) {
            redirect(action: "edit", params: [reportId: params.reportId, id: params.id, type: params.type, fromOneDrive: params.fromOneDrive, view: true])
        } else {
            String apiUrl = URLEncoder.encode(grailsApplication.config.officeOnline.hostUrl + "/wopi/files/" + params.type + "_" + params.id, "UTF-8");
            String access_token = RxCodec.encode("u_" + userService.currentUser.id + "_a_view_" + params.type + "_" + params.id)
            String url = getActionUrl("Word", "view", "docx") + "embed=1&ui=en-US&rs=en-US&wopisrc=" + apiUrl + "&wdAccPdf=0"
            [actionUrl: url, access_token: URLEncoder.encode(access_token, "UTF-8")]
        }
    }

    def error404() {}

    def edit() {
        if (grailsApplication.config.officeOnline.oneDriveProvider.enabled) {
            def entity = params.type == "publisherExecutedTemplate" ? PublisherExecutedTemplate.get(params.long("id")) : PublisherReport.get(params.long("id"))
            def lockEntity = params.type == "publisherExecutedTemplate" ? entity.publisherConfigurationSection : entity
            String itemId = lockEntity.lockCode
            if (!params.boolean("fromOneDrive")) {
                itemId = oneDriveRestService.uploadOneDriveFile(entity.name, entity.data, publisherService.getFolderName(lockEntity)+(params.view?"/temp":"")).id
            }
            if (!itemId) {
                flash.error = ViewHelper.getMessage("app.publisher.notFound")
                redirect(action: "sections", controller: "pvp", params: [id: params.reportId])
                return
            }
            String url = oneDriveRestService.getWebUrl(itemId)
            if (!url) {
                flash.error = ViewHelper.getMessage("app.publisher.notFound")
                redirect(action: "sections", controller: "pvp", params: [id: params.reportId])
                return
            }
            if (params.view) {
                url = url.replace("action=default", "action=view")
            }
            render view: "wopiEdit", model: [url: url]

        } else {
            String apiUrl = URLEncoder.encode(grailsApplication.config.officeOnline.hostUrl + "/wopi/files/" + params.type + "_" + params.id, "UTF-8");
            String access_token = RxCodec.encode("u_" + userService.currentUser.id + "_a_edit_" + params.type + "_" + params.id)
            String url = getActionUrl("Word", "edit", "docx") + "ui=en-US&rs=en-US&wopisrc=" + apiUrl
            render view: "view", model: [actionUrl: url, access_token: URLEncoder.encode(access_token, "UTF-8")]
        }
    }

    def pdf() {
        if (grailsApplication.config.officeOnline.oneDriveProvider.enabled) {
            def entity = params.type == "publisherExecutedTemplate" ? PublisherExecutedTemplate.get(params.long("id")) : PublisherReport.get(params.long("id"))
            def lockEntity = params.type == "publisherExecutedTemplate" ? entity.publisherConfigurationSection : entity
            String itemId = lockEntity.lockCode
            if (!params.boolean("fromOneDrive"))
                itemId = oneDriveRestService.uploadOneDriveFile(entity.name, entity.data, publisherService.getFolderName(lockEntity)+"/temp").id
            if (!itemId) {
                flash.error = ViewHelper.getMessage("app.publisher.notFound")
                redirect(action: "sections", controller: "pvp", params: [id: params.reportId])
                return
            }
            String siteId = grailsApplication.config.officeOnline.oneDriveProvider.siteId
            String secret = oneDriveRestService.getAccessToken()

            String pdfUrl = grailsApplication.config.oneDrive.api.url + (siteId == "drive" ? "/me" : "/sites/${siteId}") + "/drive/items/" + itemId + "/content?format=pdf"

            def connection = new URL(pdfUrl).openConnection()
            connection.with {
                doOutput = true
                requestMethod = 'GET'
                setRequestProperty("Accept", "application/json, text/plain, */*");
                setRequestProperty("Authorization", "bearer " + secret);
            }
            def code = connection.responseCode

            if (code != 200) {
                String error = connection.getErrorStream().text
                log.error("Error occurred when uploading report to convert file to pdf. OneDrive response: " + error)
                flash.error = ViewHelper.getMessage("app.publisher.unknownError") + error
                redirect(action: "sections", controller: "pvp", params: [id: params.reportId])
                return
            } else {
                response.contentType = 'application/pdf';
                response.setHeader("Content-Length", connection.contentLength.toString());
                response.outputStream << connection.getInputStream();
                GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
                webRequest.setRenderView(false)
            }

        } else {
            String apiUrl = URLEncoder.encode(grailsApplication.config.officeOnline.hostUrl + "/wopi/files/" + params.type + "_" + params.id, "UTF-8");
            String access_token = RxCodec.encode("u_" + userService.currentUser.id + "_a_view_" + params.type + "_" + params.id)
            String url = getActionUrl("Word", "view", "docx") + "embed=1&ui=en-US&rs=en-US&wopisrc=" + apiUrl + "&wdAccPdf=1"
            render view: "view", model: [actionUrl: url, access_token: URLEncoder.encode(access_token, "UTF-8")]
        }
    }

    def uploadEntityToOnedrive(Long id, String entity) {
        def result = []
        if (entity == "PublisherTemplate") {
            PublisherTemplate template = PublisherTemplate.get(id)
            result = oneDriveRestService.uploadOneDriveFile(template.name, template.template,  template.name)
        }
        render([lockCode: result.id, url: result.webUrl] as JSON)
    }

    def index() {}

    def getFile() {
        String[] file = params.file.split("_")
        byte[] data
        String filename
        switch (file[0]) {
            case "publisherExecutedTemplate":
                PublisherExecutedTemplate publisherExecutedTemplate = PublisherExecutedTemplate.get(file[1] as long)
                data = publisherExecutedTemplate.data
                filename = publisherExecutedTemplate.name + ".docx"
                break;
            case "publisherReport":
                PublisherReport publisherReport = PublisherReport.get(file[1] as long)
                data = publisherReport.data
                filename = publisherReport.name + ".docx"
                break;
        }
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes("UTF-8"), "ISO-8859-1"));
        response.addHeader("Content-Length", String.valueOf(data.length));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        render(file: data, fileName: filename, contentType: "application/octet-stream")

    }

    def postFile() {
        //todo: support versions, out of data now!!!!!!!!!
        println("request.forwardURI:" + request.forwardURI)
        println("params.file:" + params.file)
        println("Authorization:" + request.getHeader("Authorization"))
        println("x-wopi-override:" + request.getHeader("x-wopi-override"))
        println("x-wopi-lock:" + request.getHeader("x-wopi-lock"))
        String[] file = params.file.split("_")
        //x-wopi-override:LOCK
        //x-wopi-lock:{"S":"3241c11e-7126-4b5a-a37a-4e4521b35516","F":4,"E":2,"C":"FF3","M":"DB3PEPF000007E9","P":"37A85A54-4292-4B02-8AB6-EE8A06DEDC10","W":"DB3PEPF000007E7","B":"EC6B3116-27D6-49D9-9560-6C0D94DE4DE0","D":"officeapps.live.com"}
        byte[] data = IOUtils.toByteArray(request.inputStream)
        if (data.length > 0) { //updating
            //todo: add user to CRUDService.update(
            switch (file[0]) {
                case "publisherExecutedTemplate":
                    PublisherExecutedTemplate publisherExecutedTemplate = PublisherExecutedTemplate.get(file[1] as long)
                    publisherExecutedTemplate.data = data
                    publisherExecutedTemplate.modifiedBy =
                            CRUDService.update(publisherExecutedTemplate)
                    break;
                case "publisherReport":
                    PublisherReport publisherReport = PublisherReport.get(file[1] as long)
                    publisherReport.data = data
                    CRUDService.update(publisherReport)
                    break;
            }
        } else {// lock/unlock
            switch (file[0]) {
                case "publisherExecutedTemplate":
                    PublisherExecutedTemplate publisherExecutedTemplate = PublisherExecutedTemplate.get(file[1] as long)
                    PublisherConfigurationSection publisherConfigurationSection = publisherExecutedTemplate.publisherConfigurationSection
                    if (request.getHeader("x-wopi-override") == "LOCK") {
                        String lockCode = JSON.parse(request.getHeader("x-wopi-lock")).S
                        if (publisherConfigurationSection.lockCode != lockCode) {
                            String auth = RxCodec.decode(request.getHeader("Authorization").substring(7))
                            Long userId = auth.split("_")[1] as Long
                            User user = User.get(userId)
                            publisherConfigurationSection.lockedBy = user
                            publisherConfigurationSection.lockCode = lockCode
                            publisherConfigurationSection.modifiedBy = user.fullName
                            publisherConfigurationSection.lastUpdated = new Date();
                            publisherConfigurationSection.save(flush: true)
                        }
                    }
                    if (request.getHeader("x-wopi-override") == "UNLOCK") {
                        String lockCode = JSON.parse(request.getHeader("x-wopi-lock")).S
                        if (publisherConfigurationSection.lockCode == lockCode) {
                            String auth = RxCodec.decode(request.getHeader("Authorization").substring(7))
                            Long userId = auth.split("_")[1] as Long
                            User user = User.get(userId)
                            publisherConfigurationSection.lockedBy = null
                            publisherConfigurationSection.lockCode = null
                            publisherConfigurationSection.modifiedBy = user.fullName
                            publisherConfigurationSection.lastUpdated = new Date();
                            publisherConfigurationSection.save(flush: true)
                            PublisherExecutedTemplate last = publisherConfigurationSection.getDraftPublisherExecutedTemplates()
                            last.modifiedBy = user.fullName
                            last.lastUpdated = new Date()
                            if (last.data) {
                                Map parameters = WordTemplateExecutor.fetchParameters(new ByteArrayInputStream(last.data))
                                publisherConfigurationSection.pendingComment = parameters.comment?.size() ?: 0
                                publisherConfigurationSection.pendingVariable = parameters.variable?.size() ?: 0
                                publisherConfigurationSection.pendingManual = parameters.manual?.size() ?: 0
                                publisherConfigurationSection.save(flush: true)
                            }
                            last.save(flush: true)
                        }
                    }
                    break;
                case "publisherReport":
                    PublisherReport publisherReport = PublisherReport.get(file[1] as long)
                    if (request.getHeader("x-wopi-override") == "LOCK") {
                        String lockCode = JSON.parse(request.getHeader("x-wopi-lock")).S
                        if (publisherReport.lockCode != lockCode) {
                            String auth = RxCodec.decode(request.getHeader("Authorization").substring(7))
                            Long userId = auth.split("_")[1] as Long
                            User user = User.get(userId)
                            publisherReport.lockedBy = user
                            publisherReport.lockCode = lockCode
                            publisherReport.modifiedBy = user.fullName
                            publisherReport.lastUpdated = new Date();
                            publisherReport.save(flush: true)
                        }
                    }
                    if (request.getHeader("x-wopi-override") == "UNLOCK") {
                        String lockCode = JSON.parse(request.getHeader("x-wopi-lock")).S
                        if (publisherReport.lockCode == lockCode) {
                            String auth = RxCodec.decode(request.getHeader("Authorization").substring(7))
                            Long userId = auth.split("_")[1] as Long
                            User user = User.get(userId)
                            publisherReport.lockedBy = null
                            publisherReport.lockCode = null
                            publisherReport.modifiedBy = user.fullName
                            publisherReport.lastUpdated = new Date();
                            publisherReport.save(flush: true)
                        }
                    }
                    break;
            }
        }

        render ""
    }


    def getFileInfo() {
        def result
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String[] file = params.file.split("_")
        println("request.forwardURI:" + request.forwardURI)
        println("params.file:" + params.file)
        println("Authorization:" + request.getHeader("Authorization"))
        String auth = RxCodec.decode(request.getHeader("Authorization").substring(7))

        //todo - remove
        Boolean allowEdit = auth.split("_")[3] == "edit"
        result = [OwnerId       : "pvpublisher",
                  //                   AllowExternalMarketplace:true,
                  UserCanWrite  : allowEdit,
                  SupportsUpdate: allowEdit,
                  SupportsLocks : true
        ]
        switch (file[0]) {
            case "publisherExecutedTemplate":
                PublisherExecutedTemplate publisherExecutedTemplate = PublisherExecutedTemplate.get(file[1] as long)
                result.BaseFileName = publisherExecutedTemplate.name + ".docx"
                result.Size = publisherExecutedTemplate.data.size()
                //  result.SHA256= new String(Base64.encodeBase64(digest.digest(publisherExecutedTemplate.data)))
                result.Version = "" + publisherExecutedTemplate.version
                break;
            case "publisherReport":
                PublisherReport publisherReport = PublisherReport.get(file[1] as long)
                result.BaseFileName = publisherReport.name + ".docx"
                result.Size = publisherReport.data.size()
                // result.SHA256= new String(Base64.encodeBase64(digest.digest(publisherReport.data)))
                result.Version = publisherReport.version
                break;
        }
        render([contentType: "application/json;charset=UTF-8", text: result as JSON])

    }
}
