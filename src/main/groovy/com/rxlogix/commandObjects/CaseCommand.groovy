package com.rxlogix.commandObjects

import com.rxlogix.config.ExecutedCaseSeries
import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile

class CaseCommand implements Validateable{
    ExecutedCaseSeries executedCaseSeries
    String caseNumber
    String versionNumber
    String justification
    MultipartFile file
    static constraints = {
        caseNumber nullable:true,blank:true
        versionNumber nullable:true,blank:true
        executedCaseSeries nullable:false
        justification type:"text",nullable: false, blank:false
        file nullable: true, validator: { val, obj ->
            if (!val && !(obj.caseNumber)) {
            return "app.error.fill.all.required"
            }
        }
    }
}
