package com.rxlogix.commandObjects

import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import grails.validation.Validateable

class CustomReportFieldCO implements Validateable{

    Long id
    Long reportFieldId
    String fieldGroupId
    String customName
    String customDescription
    String defaultExpression

    boolean templateCLLSelectable
    boolean templateDTRowSelectable
    boolean templateDTColumnSelectable

    static constraints = {
        id(nullable: true)
        reportFieldId(nullable: false)
        fieldGroupId(nullable: false)
        customName(nullable: false)
        customDescription(nullable: true, maxSize: 2000)
        defaultExpression(nullable: false, maxSize: 32000)
    }

    ReportField getReportField() {
        return ReportField.get(reportFieldId)
    }

    ReportFieldGroup getFieldGroup() {
        return ReportFieldGroup.findByName(fieldGroupId)
    }
}
