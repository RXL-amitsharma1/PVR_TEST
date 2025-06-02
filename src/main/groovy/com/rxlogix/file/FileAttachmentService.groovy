package com.rxlogix.file

import com.rxlogix.dto.FileDTO

interface FileAttachmentService {

    FileDTO getFile(String uniqueId)

}