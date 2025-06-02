package com.rxlogix.commandObjects

import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import grails.validation.Validateable

class ReportFieldCO implements Validateable{
    Long id
    String name
    String description
    ReportFieldGroup fieldGroup
    boolean querySelectable
    boolean templateCLLSelectable
    boolean templateDTRowSelectable
    boolean templateDTColumnSelectable
    boolean override
    boolean isCreatedByUser

    static constraints = {
        id(nullable: true)
        importFrom(ReportField)
        description(nullable: false, blank: false)
    }
}
