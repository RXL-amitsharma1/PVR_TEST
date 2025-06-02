package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.config.Capa8D
import com.rxlogix.config.Capa8DAttachment
import com.rxlogix.enums.ReasonOfDelayAppEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile

@Secured('permitAll')
class IssueRestController extends RestfulController implements SanitizePaginationAttributes {
    def userService
    def CRUDService
    def qualityService

    IssueRestController() {
        super(Capa8D)
    }

    def createCapaAttachment(){
        List attachments =[]
        Boolean flag = false
        Capa8D capa = Capa8D.findByIssueNumberAndOwnerType(params.issueNumber, ReasonOfDelayAppEnum.PVQ)
        if(capa){
            capa.attachments.each {
                if (it.isDeleted == false) {
                    attachments.add(it)
                }
            }
            if (params.file && (params.file.getClass().name).equals("java.util.LinkedList")) {
                flash.error = message(code: 'quality.multi.select.attachment')
                return
            }
            attachments.each {
                if ((it.filename.split("\\.")[0]) == params.filename_attach || (params.filename_attach?.size() == 0 && (it.filename == params.file?.originalFilename))) {
                    flag = true
                }
            }
            if (flag) {
                render(status: 500, text: "The file name already exists")
                return
            }
            params.put('oldAttachments', attachments.sort().join(","))
            bindFile(capa, request.getFiles('file'), params.filename_attach.toString())
            capa.attachmentChecked = true
            (Capa8D) CRUDService.update(capa)
        }else {
            capa = params.id ? Capa8D.get(params.long("id")) : new Capa8D(lastStatusChanged: new Date())
            capa.ownerType = ReasonOfDelayAppEnum.PVQ
            bindData(capa, params)
            bindFile(capa, request.getFiles('file'), params.filename_attach.toString())
            capa.attachmentChecked = true
            CRUDService.save(capa)
        }
        render "success"
    }

    def createCapaAttachmentforROD(){
        List attachments =[]
        Boolean flag = false
        Capa8D capa = Capa8D.findByIssueNumberAndOwnerType(params.issueNumber, ReasonOfDelayAppEnum.PVC)
        if(capa){
            capa.attachments.each {
                if (it.isDeleted == false) {
                    attachments.add(it)
                }
            }
            if (params.file && (params.file.getClass().name).equals("java.util.LinkedList")) {
                flash.error = message(code: 'quality.multi.select.attachment')
                return
            }
            attachments.each {
                if ((it.filename.split("\\.")[0]) == params.filename_attach || (params.filename_attach?.size() == 0 && (it.filename == params.file?.originalFilename))) {
                    flag = true
                }
            }
            if (flag) {
                render(status: 500, text: "The file name already exists")
                return
            }
            params.put('oldAttachments', attachments.sort().join(","))
            bindFile(capa, request.getFiles('file'), params.filename_attach.toString())
            capa.attachmentChecked = true
            (Capa8D) CRUDService.update(capa)
        }else {
            capa = params.id ? Capa8D.get(params.long("id")) : new Capa8D(lastStatusChanged: new Date())
            capa.ownerType = ReasonOfDelayAppEnum.PVC
            bindData(capa, params)
            bindFile(capa, request.getFiles('file'), params.filename_attach.toString())
            capa.attachmentChecked = true
            CRUDService.save(capa)
        }
        render "success"
    }

    def updateCapaAttachment(){
        List attachments =[]
        Boolean flag = false
        Capa8D capa = Capa8D.findByIssueNumber(params.issueNumber)
        capa.attachments.each{
            if(it.isDeleted == false){
                attachments.add(it)
            }
        }
        if(params.file && (params.file.getClass().name).equals("java.util.LinkedList")){
            flash.error = message(code: 'quality.multi.select.attachment')
            return
        }
        attachments.each {
            if((it.filename.split("\\.")[0]) == params.filename_attach || (params.filename_attach?.size()==0 && (it.filename == params.file?.originalFilename))) {
                flag = true
            }
        }
        if(flag){
            render(status: 500, text: "The file name already exists")
            return
        }
        params.put('oldAttachments',attachments.sort().join(","))
        bindFile(capa, request.getFiles('file'), params.filename_attach.toString())
        capa.attachmentChecked = true
        (Capa8D) CRUDService.update(capa)
        render "success"
    }
    void AddingFileToTempDirectory(String fileLocation , MultipartFile file , def params, String filename){
        String ext = file.originalFilename.substring(file.originalFilename.lastIndexOf(".") + 1)
        String fileName = filename ? filename+ '.' + ext : file.originalFilename
        String locationToFileTemp = fileLocation + File.separator + Constants.ATTACH_FOLDER + File.separator + params.counter
        File AttachmentFolder = new File(locationToFileTemp)
        if(!AttachmentFolder.exists()){
            AttachmentFolder.mkdirs()
        }
        File AttachmentFile = new File(locationToFileTemp + File.separator + fileName)
        if(!AttachmentFile.exists()){
            OutputStream os = new FileOutputStream(AttachmentFile)
            os.write(file.bytes)
            os.close()
        }
    }

    def attachmentParameters() {
        MultipartFile file = request.getFile('file')
        String ext = file.originalFilename.substring(file.originalFilename.lastIndexOf(".") + 1)
        String fileName=params.filename_attach
        if(fileName){
            int fileNameLength=fileName.length()+1+ext.length()
            int maxfileNameSize=Capa8DAttachment.constrainedProperties.filename.maxSize
            if(fileNameLength>maxfileNameSize){
                fileName=fileName.substring(0,fileName.length()-(fileNameLength-maxfileNameSize))
            }
        }
        String fileLocation = grailsApplication.config.tempDirectory
        AddingFileToTempDirectory(fileLocation ,file , params, fileName)
        Map responseMap = [
                filename: fileName ? fileName+ '.' + ext : file.originalFilename,
                createdby: userService.currentUser.fullName,
                datecreated: new Date(),
                counter:params.counter
        ]
        render(contentType: "application/json", responseMap as JSON)
    }

    def removeAttachments() {
        Capa8D capa
        if (params.capaId != null) {
            capa = Capa8D.get(params.capaId)
        } else {
            capa = Capa8D.findByIssueNumber(params.issueNumber)
        }

        def selectedIds = []
        if (params.boolean("selectAll")) {
            selectedIds = Capa8DAttachment.findAllByIssues(capa).collect {it.id}
        } else if (params.selectedIds) {
            selectedIds = params.selectedIds.split(",").toList()
        }
        removeAttachmentsByIds(capa, selectedIds)
        render "ok"
    }

    private void removeAttachmentsByIds(Capa8D capa, List attachmentIds) {
        capa.attachmentChecked = false
        Capa8DAttachment.findAllByIdInList(attachmentIds).each {
            if (it.isDeleted == false) {
                CRUDService.softDelete(it, it.filename, params.deletejustification)
            }
        }
        if (Capa8DAttachment.findByIsDeletedAndIssues(false,capa)) {
            capa.attachmentChecked = true
            CRUDService.update(capa)
        }
    }

    def deleteTempFiles(){
            File deleteFile = new File(grailsApplication.config.tempDirectory + File.separator + Constants.ATTACH_FOLDER + File.separator + params.counter)
            if (deleteFile.exists()) {
                qualityService.deleteAlltheDirectoriesForAttachment(deleteFile)
            }
        return "Ok"
    }

    private void bindFile(capa8d, files, filename) {
        files.each { MultipartFile file ->
            String ext = file.originalFilename.substring(file.originalFilename.lastIndexOf(".") + 1)
            if(file.size > 0){
                Capa8DAttachment attachment = new Capa8DAttachment()
                if(filename) {
                    attachment.filename = filename + '.' + ext
                }else {
                    attachment.filename = file.originalFilename
                }

                attachment.data = file.bytes
                attachment.createdBy = userService.currentUser.fullName
                attachment.modifiedBy = userService.currentUser.fullName
                attachment.ownerType = capa8d.ownerType
                capa8d.addToAttachments(attachment)
            }
        }
    }
}
