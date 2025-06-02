package com.rxlogix.public_api

import com.rxlogix.dto.FileDTO
import grails.plugin.springsecurity.annotation.Secured

/*
 Api for fetching Attachment file based on ID and Type. Right now, only argus only.
*/

@Secured('permitAll')
class PublicIcsrAttachmentController {

    def fileAttachmentLocator

    def grailsMimeUtility

    def fileDataOf(String type, String id) {
        FileDTO fileDTO = fileAttachmentLocator.getServiceFor(type).getFile(id)
        String contentType = grailsMimeUtility.getMimeTypeForExtension(fileDTO.extension)?.name ?: "application/octet-stream"
        response.setContentType(contentType)
        response.setHeader("Content-disposition", "filename=${fileDTO.name}")
        response.outputStream << fileDTO.data
    }
}
