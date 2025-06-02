package com.rxlogix.config

class DrilldownCLLData implements Serializable {
    Long executedReportId
    Long reportResultId
    String cllRowData
    Set<ActionItem> actionItems = []
    Set<Comment> comments = []
    Set<Capa8D> issues = []

    static hasMany = [actionItems: ActionItem, comments: Comment, issues: Capa8D]

    static constraints = {
    }

    static mapping = {
        table('DRILLDOWN_DATA')
        id column: 'ID', generator: "sequence", params: [sequence: "DRILLDOWN_DATA_ID"]
        executedReportId column: 'EX_REPORT_ID'
        reportResultId column: 'REPORT_RESULT_ID'
        cllRowData column: 'CLL_ROW_DATA'
        actionItems joinTable: [name: "DRILLDOWN_DATA_ACTION_ITEMS", column: "ACTION_ITEM_ID", key: "CLL_ROW_ID"]
        comments joinTable: [name: "DRILLDOWN_DATA_COMMENTS", column: "COMMENT_ID", key: "CLL_ROW_ID"]
        issues joinTable: [name: "DRILLDOWN_DATA_ISSUES", column: "ISSUE_ID", key: "CLL_ROW_ID"]
        version false
    }

    static namedQueries = {
        getByActionItem { actionItemId ->
            maxResults(1)
            actionItems {
                eq 'id', actionItemId
            }
        }

        getByMetadata { params ->
            maxResults(1)
            eq('cllRowData.masterCaseNum', params.masterCaseNum)
            eq('cllRowData.vcsProcessedReportId', params.vcsProcessedReportId)
            eq('cllRowData.masterEnterpriseId', params.masterEnterpriseId)
        }
    }

    String toString() {
        "[DrilldownData = " +
                " reportResultId->${reportResultId}" +
                " executedReportId->${executedReportId}" +
                " reportResultId->${reportResultId}"
    }
}
