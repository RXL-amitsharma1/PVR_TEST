package com.rxlogix.dto

import com.rxlogix.user.User

class IcsrTrackingBulkDownloadDTO {

    Long exIcsrTemplateQueryId
    String caseNumber
    Long versionNumber

    IcsrTrackingBulkDownloadDTO(String data){
        exIcsrTemplateQueryId = Long.parseLong(data.split("_")[0])
        caseNumber = data.split("_")[1].toString()
        versionNumber = Long.valueOf(data.split("_")[2])
    }
}