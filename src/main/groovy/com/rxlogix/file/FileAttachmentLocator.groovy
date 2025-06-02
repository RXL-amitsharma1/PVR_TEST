package com.rxlogix.file

import com.rxlogix.Constants

class FileAttachmentLocator {
    ArgusFileAttachmentService argusFileAttachmentService
    PVCMFileAttachmentService pvcmFileAttachmentService

    def grailsApplication

    FileAttachmentService getServiceFor(String source) {
        //TODO need to implement logic for switch on Source Type
        if(source != null && source.equals(Constants.PVCM)) {
            return pvcmFileAttachmentService
        }
        return argusFileAttachmentService
    }
}
