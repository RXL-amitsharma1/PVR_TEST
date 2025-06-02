package com.rxlogix.config

import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.Strings
import grails.gorm.multitenancy.Tenants

class ReportResult {

    transient def userService

    ReportResultData data
    Date dateCreated
    Date lastUpdated
    User scheduledBy
    int sequenceNo = 0
    ReportExecutionStatusEnum executionStatus
    Date runDate = dateCreated
    String frequency
    // timing in ms for each SQL stage and cumulative
    Long totalTime = 0L
    Long versionTime = 0L
    Long filterVersionTime = 0L
    Long queryTime = 0L
    Long reportTime = 0L
    Long reAssessTime =0L

    // row counts for each SQL stage and cumulative
    Long versionRows = 0L
    Long versionRowsFilter = 0L
    Long queryRows = 0L
    Long reportRows = 0L

    String medDraVersion
    String clinicalCompoundNumber

    // case count
    Long caseCount = null

    // drilldown
    ExecutedTemplateQuery drillDownSource
    String measure
    String field
    String drillDownFilerColumns
    ReportResult parent
    ReportTemplate template

    String viewSettings

    private ExecutedTemplateQuery _executedTemplateQuery

    static hasMany = [comments: Comment]

    static transients = ['executedTemplateQuery', 'sourceProfile','_executedTemplateQuery']

    static mapping = {
        table name: "RPT_RESULT"

        data column: "RPT_RESULT_DATA_ID"
        scheduledBy column: "SCHEDULED_PVUSER_ID"
        sequenceNo column: "SEQUENCE"
        executionStatus column: "EX_STATUS"
        runDate column: "RUN_DATE"
        frequency column: "FREQUENCY"
        totalTime column: "TOTAL_TIME"
        versionTime column: "VERSION_TIME"
        filterVersionTime column: "FILTER_VERSION_TIME"
        reAssessTime column: "REASSESS_TIME"
        queryTime column: "QUERY_TIME"
        reportTime column: "REPORT_TIME"
        versionRows column: "VERSION_ROWS"
        versionRowsFilter column: "FILTERED_VERSION_ROWS"
        queryRows column: "QUERY_ROWS"
        reportRows column: "REPORT_ROWS"
        caseCount column: "CASE_COUNT"
        medDraVersion column: "MEDDRA_VERSION"
        clinicalCompoundNumber column: "CLINICAL_COMPOUND_NUMBER"
        drillDownSource column: "DRILL_DOWN_SOURCE_ID"
        measure column: "MEASURE"
        drillDownFilerColumns column: "FILTER_COLUMNS"
        field column: "FIELD_NAME"
        parent column: "PARENT_ID"
        template column: "TEMPLATE_ID"
        viewSettings column: "VIEW_SETTINGS"

    }

    static constraints = {
        data(nullable:true)
        runDate(nullable: true)
        frequency(nullable: true)
        comments cascade: 'all-delete-orphan'
        caseCount(nullable: true)
        medDraVersion(nullable: true)
        clinicalCompoundNumber(nullable: true)
        measure(nullable: true)
        drillDownSource(nullable: true)
        template(nullable: true)
        viewSettings(nullable: true)
        drillDownFilerColumns(nullable: true)
        field(nullable: true)
        parent(nullable: true)
    }

    def beforeInsert = {
        sequenceNo ++
    }

    ExecutedTemplateQuery getExecutedTemplateQuery() {
        if (!_executedTemplateQuery)
            _executedTemplateQuery = drillDownSource ?: ExecutedTemplateQuery.findAllByFinalReportResultOrDraftReportResult(this, this).first()
        return _executedTemplateQuery
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isDev() || ((executedTemplateQuery?.executedConfiguration?.tenantId == Tenants.currentId() as Long) && (currentUser?.isAdmin() || scheduledBy?.id == currentUser?.id || executedTemplateQuery?.executedConfiguration?.owner == currentUser
                || isVisible(currentUser) || (executedTemplateQuery?.executedConfiguration?.owner?.id in currentUser?.getUserTeamIds()))))
    }

    boolean isVisible(User currentUser){
        return executedTemplateQuery?.executedConfiguration?.executedDeliveryOption?.isSharedWith(currentUser)
    }

    String getName() {
        return (id != null) ? "$id" : null
    }

    Long resultDataSize() {
        if (!data?.id) {
            return 0
        }
        return ReportResultData.executeQuery("select valueBytesLength from ReportResultData where id =:id", [id: data.id]).first() ?: 0
    }

    String toString() {
        "[ReportResult = " +
                " data->${data}" +
                " dateCreated->${dateCreated}" +
                " lastUpdated->${lastUpdated}" +
                " scheduledBy->${scheduledBy}" +
                " sequenceNo->${sequenceNo}" +
                " runDate->${runDate}" +
                " frequency->${frequency}" +
                " totalTime->${totalTime} \n" +
                " executionStatus->${ Strings.trunc(executionStatus.toString(),555) } \n" +
                " executedTemplateQuery->${ Strings.trunc(executedTemplateQuery.toString(),555) }" +
                "]"
    }

    SourceProfile getSourceProfile(){
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.executedConfiguration
        return executedReportConfiguration.sourceProfile
    }
}
