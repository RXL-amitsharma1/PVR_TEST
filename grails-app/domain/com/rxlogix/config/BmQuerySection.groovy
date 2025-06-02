package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['balanceMinusQuery'])
class BmQuerySection {
    static auditable = [ignore:['balanceMinusQuery']]

    SourceProfile sourceProfile
    String executeFor
    Date executionStartDate
    Date executionEndDate
    Integer xValue
    boolean flagCaseExclude = false
    List<DistinctTable> distinctTables
    List<IncludeCase> includeCases
    List<ExcludeCase> excludeCases

    int index
    boolean dynamicFormEntryDeleted

    static transients = ['dynamicFormEntryDeleted']

    static hasMany = [distinctTables: DistinctTable, includeCases: IncludeCase, excludeCases: ExcludeCase]
    static mappedBy = [distinctTables: 'bmQuerySection', includeCases: 'bmQuerySection', excludeCases: 'bmQuerySection']
    static belongsTo = [balanceMinusQuery: BalanceMinusQuery]

    static mapping = {
        table name: "BQMQ_SECTION"
        tablePerHierarchy false
        sourceProfile column: "SRC_PROFILE_ID"
        executeFor column: "EXECUTE_FOR"
        executionStartDate column: "EXECUTION_START_DATE"
        executionEndDate column: "EXECUTION_END_DATE"
        xValue column: "X_VALUE"
        flagCaseExclude column: "FLAG_CASE_EXCLUDE"
        distinctTables joinTable: [name: "BQMQ_DISTINCT_TABLE", column: "ID", key: "BQMQ_SECTION_ID"], indexColumn: [name: "BQMQ_DISTINCT_TABLE_IDX"]
        includeCases joinTable: [name: "BQMQ_INCLUDE_CASE", column: "ID", key: "BQMQ_SECTION_ID"], indexColumn: [name: "BQMQ_INCLUDE_CASE_IDX"]
        excludeCases joinTable: [name: "BQMQ_EXCLUDE_CASE", column: "ID", key: "BQMQ_SECTION_ID"], indexColumn: [name: "BQMQ_EXCLUDE_CASE_IDX"]
        balanceMinusQuery column: "BMQUERY_ID"
        index column: "INDX"
        version false
    }

    static constraints = {
        sourceProfile nullable: false
        executeFor nullable: false
        flagCaseExclude nullable: false
        executionStartDate (nullable: true, validator: { val, obj ->
            if(!val && obj.executeFor == 'ETL_START_DATE'){
                return "com.rxlogix.config.BmQuerySection.executionStartDate.nullable"
            }
            return true
        })
        executionEndDate  (nullable: true, validator: { val, obj ->
            if(!val && obj.executeFor == 'ETL_START_DATE'){
                return "com.rxlogix.config.BmQuerySection.executionEndDate.nullable"
            }
            return true
        })
        xValue(nullable: true, validator: { val, obj ->
            if(!val && (obj.executeFor == 'LAST_X_ETL' || obj.executeFor == 'LAST_X_DAYS')){
                return "com.rxlogix.config.BmQuerySection.xValue.nullable"
            }
            return true
        })
        distinctTables (cascade: 'all-delete-orphan')
        includeCases(cascade: 'all-delete-orphan', nullable: true, validator: { val, obj ->
            if(!val && obj.executeFor == 'CASE_LIST') {
                return "com.rxlogix.config.BmQuerySection.includeCases.nullable"
            }
            return true
        })
        excludeCases(cascade: 'all-delete-orphan', nullable: true, validator: { val, obj ->
            if(!val && obj.flagCaseExclude) {
                return "com.rxlogix.config.BmQuerySection.excludeCases.nullable"
            }
            return true
        })
        balanceMinusQuery nullable: false
        dynamicFormEntryDeleted bindable: true
    }

    @Override
    public String toString() {
        return "${sourceProfile.sourceName}"
    }
}
