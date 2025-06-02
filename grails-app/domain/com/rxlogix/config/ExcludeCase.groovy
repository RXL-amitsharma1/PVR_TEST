package com.rxlogix.config

class ExcludeCase {

    String caseNumber
    SourceProfile sourceProfile

    static belongsTo = [bmQuerySection: BmQuerySection]

    static mapping = {
        table name: "BQMQ_EXCLUDE_CASE"
        tablePerHierarchy false
        caseNumber column: "CASE_NUMBER"
        sourceProfile column: "SRC_PROFILE_ID"
        bmQuerySection column: "BQMQ_SECTION_ID"
        version false
    }
    static constraints = {
        caseNumber nullable: false
        sourceProfile nullable: false
        bmQuerySection nullable: false
    }

    @Override
    public String toString() {
        return "${sourceProfile.sourceName} - ${caseNumber}"
    }
}
