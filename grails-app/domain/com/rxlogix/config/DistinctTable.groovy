package com.rxlogix.config

class DistinctTable {

    String entity
    SourceProfile sourceProfile


    static belongsTo = [bmQuerySection: BmQuerySection]

    static mapping = {
        table name: "BQMQ_DISTINCT_TABLE"
        tablePerHierarchy false
        entity column: "ENTITY"
        sourceProfile column: "SRC_PROFILE_ID"
        bmQuerySection column: "BQMQ_SECTION_ID"
        version false
    }

    static constraints = {
        entity nullable: false
        sourceProfile nullable: false
        bmQuerySection nullable: false
    }

    @Override
    public String toString() {
        return "${sourceProfile.sourceName} - ${entity}"
    }
}
