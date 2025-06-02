package com.rxlogix.config

class ExecutedDataTabulationTemplate extends DataTabulationTemplate {
    static auditable = false
    static mapping = {
        table name: "EX_DTAB_TEMPLT"
    }

    static constraints = {
        originalTemplateId(nullable:false, min:1L)
        transposeOutput(bindable: true)
        groupingList(bindable: true)
    }

    @Override
    public String toString() {
        super.toString()
    }
}
