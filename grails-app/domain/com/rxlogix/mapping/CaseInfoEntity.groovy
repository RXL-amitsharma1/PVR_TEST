package com.rxlogix.mapping


abstract class CaseInfoEntity implements Serializable{

    Long caseId
    String caseNumber
    Long version


    static mapping = {
        caseNumber column: "CASE_NUM"
        caseId column: "CASE_ID"
        version column: "VERSION_NUM"
    }

    static constraints = {
        caseNumber nullable: true
    }
    static mapWith = "none"


    String toString(){
        return "$caseNumber-$caseId"
    }
}
