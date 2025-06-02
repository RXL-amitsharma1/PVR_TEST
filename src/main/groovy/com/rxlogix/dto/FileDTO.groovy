package com.rxlogix.dto

import org.apache.commons.io.FilenameUtils

class FileDTO {
    String name
    byte[] data
    String contentType

    String getExtension() {
        return FilenameUtils.getExtension(name)
    }
}
