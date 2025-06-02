package com.rxlogix.config

class DrilldownCLLMetadata extends DrilldownMetadata implements Serializable {

    Long caseId
    String processedReportId
    Long tenantId

    static hasMany = [attachments: PvcAttachment]

    static constraints = {

    }

    static mapping = {
        table('DRILLDOWN_METADATA')
        id column: 'ID', generator: "sequence", params: [sequence: "DRILLDOWN_METADATA_ID"]
        caseId column: 'CASE_ID'
        processedReportId column: 'PROCESSED_REPORT_ID'
        tenantId column: 'TENANT_ID'
        actionItems joinTable: [name: "DDWN_MDATA_ACTN_ITEM", column: "ACTION_ITEM_ID", key: "CLL_ROW_ID"]
        comments joinTable: [name: "DDWN_MDATA_CMNTS", column: "COMMENT_ID", key: "CLL_ROW_ID"]
        issues joinTable: [name: "DDWN_MDATA_ISSUES", column: "ISSUE_ID", key: "CLL_ROW_ID"]
        version false
    }

    static namedQueries = {
        getByActionItem { actionItemId ->
            maxResults(1)
            actionItems {
                eq 'id', actionItemId
            }
        }

        getMetadataRecord { Map params ->
            maxResults(1)
            eq('caseId', Long.valueOf(params.get('masterCaseId')))
            eq('processedReportId', params.get('processedReportId'))
            eq('tenantId', Long.valueOf(params.get('tenantId')))
        }

    }

    String toString() {
        "[DrilldownMetaData = " +
                " caseId->${caseId}" +
                " processedReportId->${processedReportId}" +
                " tenantId->${tenantId}"
    }

}
