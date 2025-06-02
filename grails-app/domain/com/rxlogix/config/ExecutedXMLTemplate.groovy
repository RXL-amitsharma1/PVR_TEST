package com.rxlogix.config

import com.rxlogix.enums.ReportFormatEnum

class ExecutedXMLTemplate extends XMLTemplate {
    static auditable = false
    static mapping = {
        table name: "EX_XML_TEMPLT"
    }

    static constraints = {
        originalTemplateId(nullable:false,  min:1L)
    }

    boolean isNotExportable(ReportFormatEnum format) {
        if(format in [ReportFormatEnum.XML,ReportFormatEnum.R3XML,ReportFormatEnum.PDF,ReportFormatEnum.HTML]){
            return false
        }
        return true
    }

    @Override
    public String toString() {
        super.toString()
    }
}
