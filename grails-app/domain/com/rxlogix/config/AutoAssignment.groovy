package com.rxlogix.config

class AutoAssignment implements Serializable {


    String caseNumber
    Long caseId
    String processedReportId
    WorkflowRule workflowRule
    String moduleName
    String type
    Long versionNumber
    Long senderId

    Long tenantId

    static constraints = {
        caseNumber nullable: true
        caseId nullable: true
        processedReportId nullable: true
        workflowRule nullable: true
        versionNumber nullable: true
        type nullable: true
        senderId nullable: true
    }

    static mapping = {
        table('AUTO_ASSIGNMENT')
        caseNumber column: 'CASE_NUMBER'
        caseId column: 'CASE_ID'
        processedReportId column: 'PROCESSED_REPORT_ID'
        tenantId column: 'TENANT_ID'
        moduleName column: "MODULE_NAME"
        workflowRule column: "WORKFLOW_RULE_ID"
        type column: "TYPE"
        versionNumber column: 'VERSION_NUM'
        senderId column: 'SENDER_ID'
        version false
    }

    static namedQueries = {

        getAllRecordWithState { Map params ->
            maxResults(1)
            eq('caseId', Long.valueOf(params.get('masterCaseId')))
            eq('processedReportId', params.get('processedReportId'))
            eq('tenantId', Long.valueOf(params.get('tenantId')))
            ne('workflowState', null)
        }

        getAllInboundRecordWithState {
            maxResults(1)
            eq('caseId', Long.valueOf(params.get('masterCaseId')))
            eq('versionNumber', Long.valueOf(params.get('masterVersionNum')))
            eq('tenantId', Long.valueOf(params.get('tenantId')))
            eq('senderId', Long.valueOf(params.get('senderId')))
            ne('workflowState', null)
        }
    }

    String toString() {
        "[autoAssignmentData = " +
                " caseNumber->${caseNumber}" +
                " caseId->${caseId}" +
                " processedReportId->${processedReportId}" +
                " tenantId->${tenantId}" +
                " moduleName->${moduleName}" +
                " workflowRule->${workflowRule}"

    }
}

