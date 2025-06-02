package com.rxlogix.config

import com.rxlogix.UserService
import com.rxlogix.UtilService
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import com.rxlogix.util.ViewHelper
import groovy.sql.Sql

@CollectionSnapshotAudit
class WorkflowJustification {
    static UtilService utilService
    static UserService userService
    static auditable = true

    WorkflowState fromState
    WorkflowState toState
    User routedBy
    private String description
    ExecutedReportConfiguration executedReportConfiguration
    QualityCaseData qualityCaseData
    QualitySubmission qualitySubmission
    QualitySampling qualitySampling
    DrilldownCLLMetadata drilldownCLLMetadata
    InboundDrilldownMetadata inboundMetadata
    ReportRequest reportRequest
    PublisherConfigurationSection publisherSection
    PublisherReport publisherReport
    PublisherReport publisherReportQc
    WorkflowRule workflowRule
    User assignedToUser
    UserGroup assignedToUserGroup

    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    public void setDescription(String d){
        description = d?.replaceAll("\r","")
    }
    public String getDescription(){
        return description
    }

    static constraints = {
        description type: "text", nullable: true, validator: { val, obj ->
            if (obj.executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration && !val) {
                return 'invalid'
            }
        }
        executedReportConfiguration nullable: true
        qualityCaseData nullable: true
        qualitySubmission nullable: true
        qualitySampling nullable: true
        drilldownCLLMetadata nullable: true
        inboundMetadata nullable: true
        reportRequest nullable: true
        workflowRule(nullable: true, validator: {val, obj ->
        if(obj && obj.fromState != obj.toState && !(obj.workflowRule) ){
            return false
        }else{
            return true
        }
        })
        assignedToUser nullable: true
        assignedToUserGroup nullable: true

        publisherSection nullable: true
        publisherReport nullable: true
        publisherReportQc nullable: true
    }

    static mapping = {
        table('WORKFLOW_JUSTIFICATION')
        executedReportConfiguration column: "EX_REPORT", cascade: "none"
        qualityCaseData column: "QUALITY_CASE_DATA", cascade: "none"
        qualitySubmission column: "QUALITY_SUBMISSION", cascade: "none"
        qualitySampling column: "QUALITY_SAMPLING", cascade: "none"
        drilldownCLLMetadata column: "DRILLDOWN_METADATA", cascade: "none"
        inboundMetadata column: "IN_DRILLDOWN_METADATA", cascade: "none"
        reportRequest column: "REPORT_REQUEST", cascade: "none"
        assignedToUser column: "ASSIGNED_TO_USER_ID"
        assignedToUserGroup column: "ASSIGNED_TO_USERGROUP_ID"
        publisherSection column: "PUBLISHER_SECTION", cascade: "none"
        publisherReport column: "PUBLISHER_REPORT_ID", cascade: "none"
        publisherReportQc column: "PUBLISHER_REPORT_QC_ID", cascade: "none"
    }

    Map toWorkflowJustificationMap() {
        User currentUser = userService.currentUser
        [
                fromState: fromState.name,
                toState: toState.name,
                justification: description ?: "",
                routedBy: routedBy.fullName,
                assignedToUser: assignedToUser,
                assignedToUserGroup: assignedToUserGroup,
                dateCreated: DateUtil.getLongDateStringForLocaleAndTimeZone(dateCreated,currentUser.preference.locale,currentUser.preference.timeZone,false),
        ]
    }

    String getInstanceIdentifierForAuditLog() {
        String instanceIdentifier
        if(executedReportConfiguration instanceof ExecutedConfiguration)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","Adhoc Report ${executedReportConfiguration.reportName} (v${executedReportConfiguration.numOfExecutions})", fromState.name, toState.name)
        else if(executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","Aggregate Report ${executedReportConfiguration.reportName} (v${executedReportConfiguration.numOfExecutions})", fromState.name, toState.name)
        else if(executedReportConfiguration instanceof ExecutedIcsrReportConfiguration)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","ICSR Report ${executedReportConfiguration.reportName} (v${executedReportConfiguration.numOfExecutions})", fromState.name, toState.name)
        else if(reportRequest)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","Report Request ${reportRequest.reportName}", fromState.name, toState.name)
        else if(qualityCaseData)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","${qualityCaseData.caseNumber} (v${qualityCaseData.versionNumber})", fromState.name, "${toState.name} from Case Data Quality")
        else if(qualitySubmission)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","${qualitySubmission.caseNumber} (v${qualitySubmission.versionNumber})", fromState.name, "${toState.name} from Submission Quality")
        else if(qualitySampling)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification","${qualitySampling.caseNumber} (v${qualitySampling.versionNumber})", fromState.name, "${toState.name} from Case Sampling")
        else if(drilldownCLLMetadata?.processedReportId) {
            Map metadataParams = [:]
            metadataParams['masterCaseId'] = drilldownCLLMetadata.caseId
            metadataParams['masterEnterpriseId'] = drilldownCLLMetadata.tenantId
            metadataParams['vcsProcessedReportId'] = drilldownCLLMetadata.processedReportId
            DrilldownCLLData cllRecord = getDrilldownRecordForMetadataWorkflowJustification(metadataParams, false)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification",JSON.parse(cllRecord.cllRowData).masterCaseNum, fromState.name, "${toState.name} from Reason of Delay")
        }
        else if(Long.valueOf(inboundMetadata?.senderId)>-1) {
            Map metadataParams = [:]
            metadataParams['masterCaseId'] = inboundMetadata.caseId
            metadataParams['masterEnterpriseId'] = inboundMetadata.tenantId
            metadataParams['senderId'] = inboundMetadata.senderId
            metadataParams['masterVersionNum'] = inboundMetadata.caseVersion
            DrilldownCLLData cllRecord = getDrilldownRecordForMetadataWorkflowJustification(metadataParams, true)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification", JSON.parse(cllRecord.cllRowData).masterCaseNum, fromState.name, "${toState.name} from Inbound Compliance")
        }
        else if(publisherSection)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification", "Publisher Section ${publisherSection.name}", fromState.name, toState.name)
        else if(publisherReport)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification", "Publisher Report ${publisherReport.name}", fromState.name, toState.name)
        else if(publisherReportQc)
            instanceIdentifier = ViewHelper.getMessage("auditLog.entityValue.workflowJustification", "Publisher Report QC ${publisherReportQc.name}", fromState.name, toState.name)
        else
            instanceIdentifier = "${fromState.name} to ${toState.name}"
        return instanceIdentifier
    }

    static List<WorkflowJustification> getAllLatestJustifications(List<WorkflowState> states) {
        String sql = "from WorkflowJustification where id in " +
                "(select max(ex.id) from WorkflowJustification ex where (ex.executedReportConfiguration is not null or  ex.qualityCaseData is not null or ex.qualitySubmission  is not null or ex.qualitySampling  is not null) group by ex.executedReportConfiguration,  ex.qualityCaseData, ex.qualitySubmission,ex.qualitySampling) " +
                " and toState in (:states)"
        return states ? executeQuery(sql, [states: states]) as List<WorkflowJustification> : []

    }

    static List<WorkflowJustification> getLatestSectionJustifications(List<WorkflowState> states) {
        String sql = "from WorkflowJustification where id in " +
                "(select max(ex.id) from WorkflowJustification ex where ex.publisherSection is not null group by ex.publisherSection) " +
                " and toState in (:states)"
        return states ? executeQuery(sql, [states: states]) as List<WorkflowJustification> : []
    }

    static List<WorkflowJustification> getLatestPublisherJustifications(List<WorkflowState> states) {
        String sql = "from WorkflowJustification where id in " +
                "(select max(ex.id) from WorkflowJustification ex where ex.publisherReport is not null group by ex.publisherReport) " +
                " and toState in (:states)"
        return states ? executeQuery(sql, [states: states]) as List<WorkflowJustification> : []
    }

    public String toString() {
        return "${fromState} to ${toState}"
    }

    DrilldownCLLData getDrilldownRecordForMetadataWorkflowJustification(Map metadataMap, boolean isInbound){
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append(" SELECT ID FROM DRILLDOWN_DATA A WHERE A.CASE_ID = " + metadataMap.masterCaseId)
        if(isInbound) {
            stringBuilder.append(" AND A.SENDER_ID = " + metadataMap.senderId)
            stringBuilder.append(" AND A.CASE_VERSION = " + metadataMap.masterVersionNum)
        }else {
            stringBuilder.append(" AND A.PROCESSED_REPORT_ID = " + metadataMap.vcsProcessedReportId)
        }
        stringBuilder.append(" AND A.TENANT_ID = " + metadataMap.masterEnterpriseId)
        Sql pvrsql = null
        DrilldownCLLData cllRecord = null
        try {
            String queryString = stringBuilder.toString()
            if(queryString) {
                pvrsql = new Sql(utilService.getReportConnectionForPVR())
                Long dbrecord = pvrsql.firstRow(queryString).get('ID')
                cllRecord = DrilldownCLLData.get(dbrecord)
            }
        } catch (Exception e) {
            log.error("Exception in fetching CLL Records", e)
        } finally {
            pvrsql?.close()
        }

        return cllRecord
    }

}
