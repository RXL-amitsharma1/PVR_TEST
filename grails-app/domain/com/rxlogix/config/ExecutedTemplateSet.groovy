package com.rxlogix.config

import com.rxlogix.enums.ReportFormatEnum

class ExecutedTemplateSet extends TemplateSet{
    static auditable = false
    static mapping = {
        table name: "EX_TEMPLT_SET"
    }

    static constraints = {
        originalTemplateId(nullable:false, min:1L)
    }


    boolean isNotExportable(ReportFormatEnum format) {
        return super.isNotExportable(format) || format == ReportFormatEnum.HTML
    }

    @Override
    public String toString() {
        super.toString()
    }
}
