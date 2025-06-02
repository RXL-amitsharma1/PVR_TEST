package com.rxlogix.config

class StandardJustification implements Serializable {
    Long codeId
    Long langId
    String description
    String actionName
    boolean isDisplay
    boolean isActive

    static mapping = {
        datasource "pva"
        version false
        id composite: ['codeId', 'langId']
        table name: "VW_JUSTIFICATION_ACTION_MAPPING_PVR_DSP"
        codeId column: "JUSTIFICATION_ID"
        langId column: "LANGUAGE_CODE"
        description column: "JUSTIFICATION_VALUE"
        actionName column: "ACTION_NAME"
        isDisplay column: "DISPLAY"
        isActive column: "IS_ACTIVE"
    }

    String toString() {
        return description
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof StandardJustification)) return false

        StandardJustification that = (StandardJustification) o

        if (codeId != that.codeId) return false
        if (langId != that.langId) return false
        if (description != that.description) return false
        if (actionName != that.actionName) return false

        return true
    }
}
