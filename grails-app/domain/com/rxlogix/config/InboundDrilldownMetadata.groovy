package com.rxlogix.config

class InboundDrilldownMetadata extends DrilldownMetadata implements Serializable {

    Long caseId
    Long caseVersion
    Long senderId
    Long tenantId

    static hasMany = [attachments: PvcAttachment]

    static constraints = {

    }

    static mapping = {
        table('IN_DRILLDOWN_METADATA')
        id column: 'ID', generator: "sequence", params: [sequence: "IN_DRILLDOWN_METADATA_ID"]
        caseId column: 'CASE_ID'
        caseVersion column: 'VERSION_NUM'
        senderId column: 'SENDER_ID'
        tenantId column: 'TENANT_ID'
        actionItems joinTable: [name: "IN_DDWN_MDATA_ACTN_ITEM", column: "ACTION_ITEM_ID", key: "CLL_ROW_ID"]
        comments joinTable: [name: "IN_DDWN_MDATA_CMNTS", column: "COMMENT_ID", key: "CLL_ROW_ID"]
        issues joinTable: [name: "IN_DDWN_MDATA_ISSUES", column: "ISSUE_ID", key: "CLL_ROW_ID"]
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
            eq('caseVersion', Long.valueOf(params.get('masterVersionNum')))
            eq('tenantId', Long.valueOf(params.get('tenantId')))
            eq('senderId', Long.valueOf(params.get('senderId')))
        }

    }

    String toString() {
        "[InboundDrilldownMetadata = " +
                " caseId->${caseId}" +
                " caseVersion->${caseVersion}" +
                " tenantId->${tenantId}" +
                " senderId->${senderId} ]"
    }

}
