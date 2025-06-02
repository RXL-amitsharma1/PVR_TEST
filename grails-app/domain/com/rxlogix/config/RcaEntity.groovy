package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.util.Holders

abstract class RcaEntity implements Serializable{
    @AuditEntityIdentifier
    String textDesc
    static transients = ['deleteJustification']
    transient String deleteJustification
    static mapping = {
        textDesc column: "TEXT_DESC"
    }
    static constraints = {
        textDesc(nullable: false)
    }
    static mapWith = "none"

    String toString(){
        return textDesc
    }
}
