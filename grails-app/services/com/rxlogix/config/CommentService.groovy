package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.commandObjects.CommentCommand
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.CommentTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.util.AuditLogConfigUtil
import grails.gorm.transactions.Transactional
import com.rxlogix.enums.PvqTypeEnum
import groovy.json.JsonSlurper
import org.apache.commons.text.StringEscapeUtils

import java.text.SimpleDateFormat

class CommentService {

    def CRUDService
    def reportExecutorService
    def qualityService
    def userService

    Set<Comment> fetchComments(CommentCommand commentCommand) {
        Set<Comment> comments = []
        switch (commentCommand.commentType) {
            case CommentTypeEnum.REPORT_RESULT:
                comments = ReportResult.read(commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.EXECUTED_CONFIGURATION:
                comments = ExecutedReportConfiguration.read(commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.CASE_QUALITY:
                comments = qualityService.initializeQualityObjById(PvqTypeEnum.CASE_QUALITY.toString(),commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.SUBMISSION_QUALITY:
                comments = qualityService.initializeQualityObjById(PvqTypeEnum.SUBMISSION_QUALITY.toString(),commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.SAMPLING:
                comments = qualityService.initializeQualityObjById(PvqTypeEnum.SAMPLING.toString(),commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.CASE_CORRECTIONS:
                comments = qualityService.initializeQualityObjById(PvqTypeEnum.CASE_CORRECTIONS.toString(),commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.DRILLDOWN_RECORD:
                def metadataRecord = getMetadataRecordForDrilldownRecord(DrilldownCLLData.get(commentCommand.ownerId))
                comments = metadataRecord?.comments
                break;
            case CommentTypeEnum.PUBLISHER_SECTION:
                comments = PublisherConfigurationSection.read(commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.PUBLISHER_FULL:
                comments = PublisherReport.read(commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.SCHEDULER:
                comments = SchedulerConfigParams.read(commentCommand.ownerId)?.comments
                break;
            case CommentTypeEnum.SCHEDULER_RR:
                comments = ReportRequest.read(commentCommand.ownerId)?.comments?.collect {
                    Comment c = new Comment(textData: it.reportComment,createdBy: it.createdBy,modifiedBy: it.modifiedBy)
                    c.dateCreated = it.dateCreated
                    c.lastUpdated = it.lastUpdated
                    c.discard()
                    c.id = it.id
                    return c
                }
                break;
        }
        return comments
    }

    @Transactional
    void save(Comment comment, CommentTypeEnum commentType, Long ownerId) {
        CRUDService.save(comment)
        switch (commentType) {
            case CommentTypeEnum.PUBLISHER_FULL:
                PublisherReport publisherReport = PublisherReport.get(ownerId)
                publisherReport.addToComments(comment)
                CRUDService.update(publisherReport)
                break;
            case CommentTypeEnum.PUBLISHER_SECTION:
                PublisherConfigurationSection publisherConfigurationSection = PublisherConfigurationSection.get(ownerId)
                publisherConfigurationSection.addToComments(comment)
                CRUDService.update(publisherConfigurationSection)
                break;
            case CommentTypeEnum.REPORT_RESULT:
                ReportResult reportResult = ReportResult.get(ownerId)
                String oldComments = reportResult.comments.join(", ")
                reportResult.addToComments(comment)
                CRUDService.update(reportResult)
                String newComments = reportResult.comments.join(", ")
                ExecutedReportConfiguration executedConfiguration = reportResult.executedTemplateQuery.executedConfiguration
                Boolean isInDraftMode = (executedConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
                commentAuditLog(executedConfiguration, newComments, commentType, oldComments)
                reportExecutorService.deleteReportsCachedFilesIfAny(executedConfiguration, isInDraftMode)
                break;
            case CommentTypeEnum.EXECUTED_CONFIGURATION:
                ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(ownerId)
                executedConfiguration.addToComments(comment)
                CRUDService.update(executedConfiguration)
                Boolean isInDraftMode = (executedConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
                reportExecutorService.deleteReportsCachedFilesIfAny(executedConfiguration, isInDraftMode)
                break;
            case CommentTypeEnum.CASE_QUALITY:
                QualityCaseData qualityCase = qualityService.initializeQualityObjById(PvqTypeEnum.CASE_QUALITY.toString(),ownerId)
                qualityCase.addToComments(comment)
                CRUDService.update(qualityCase)
                break;
            case CommentTypeEnum.SUBMISSION_QUALITY:
                QualitySubmission qualitySubmission = qualityService.initializeQualityObjById(PvqTypeEnum.SUBMISSION_QUALITY.toString(),ownerId)
                qualitySubmission.addToComments(comment)
                CRUDService.update(qualitySubmission)
                break;
            case CommentTypeEnum.SAMPLING:
                QualitySampling qualitySampling = qualityService.initializeQualityObjById(PvqTypeEnum.SAMPLING.toString(),ownerId)
                qualitySampling.addToComments(comment)
                CRUDService.update(qualitySampling)
                break;
            case CommentTypeEnum.CASE_CORRECTIONS:
                QualitySampling qualitySampling = qualityService.initializeQualityObjById(PvqTypeEnum.CASE_CORRECTIONS.toString(),ownerId)
                qualitySampling.addToComments(comment)
                CRUDService.update(qualitySampling)
                break;
            case CommentTypeEnum.DRILLDOWN_RECORD:
                DrilldownCLLData cllRecord = DrilldownCLLData.get(ownerId)
                def metadataRecord = getMetadataRecordForDrilldownRecord(cllRecord)
                String oldComments = metadataRecord.comments.join(", ")
                metadataRecord.comments.add(comment)
                CRUDService.update(metadataRecord)
                String newComments = metadataRecord.comments.join(", ")
                commentAuditLog(cllRecord, newComments, commentType, oldComments)
                break;
            case CommentTypeEnum.SCHEDULER:
                SchedulerConfigParams cfgParam = SchedulerConfigParams.get(ownerId)
                cfgParam.comments.add(comment)
                CRUDService.update(cfgParam)
                break;
            case CommentTypeEnum.SCHEDULER_RR:
                ReportRequest rr = ReportRequest.get(ownerId)
                ReportRequestComment rrc = new ReportRequestComment([reportComment: comment.textData, reportRequest: rr])
                CRUDService.save(rrc)
                break;
        }
    }

    @Transactional
    void delete(Comment comment, Long ownerId, CommentTypeEnum commentType) {
        switch (commentType) {
            case CommentTypeEnum.REPORT_RESULT:
                ReportResult reportResult = ReportResult.get(ownerId);
                String oldComments = reportResult.comments.join(", ")
                reportResult.removeFromComments(comment)
                CRUDService.save(reportResult)
                String newComments = reportResult.comments.join(", ")
                ExecutedReportConfiguration executedConfiguration = reportResult.executedTemplateQuery.executedConfiguration
                Boolean isInDraftMode = (executedConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
                commentAuditLog(executedConfiguration, newComments, commentType, oldComments, true)
                reportExecutorService.deleteReportsCachedFilesIfAny(executedConfiguration, isInDraftMode)
                break;
            case CommentTypeEnum.EXECUTED_CONFIGURATION:
                ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(ownerId);
                executedConfiguration.removeFromComments(comment)
                CRUDService.save(executedConfiguration)
                Boolean isInDraftMode = (executedConfiguration.class == ExecutedPeriodicReportConfiguration) ? (executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) : false
                reportExecutorService.deleteReportsCachedFilesIfAny(executedConfiguration, isInDraftMode)
                break;
            case CommentTypeEnum.CASE_QUALITY:
                QualityCaseData qualityCase = qualityService.initializeQualityObjById(PvqTypeEnum.CASE_QUALITY.toString(),ownerId)
                qualityCase.removeFromComments(comment)
                CRUDService.save(qualityCase)
                break;
            case CommentTypeEnum.SUBMISSION_QUALITY:
                QualitySubmission qualitySubmission = qualityService.initializeQualityObjById(PvqTypeEnum.SUBMISSION_QUALITY.toString(),ownerId)
                qualitySubmission.removeFromComments(comment)
                CRUDService.save(qualitySubmission)
                break;
            case CommentTypeEnum.SAMPLING:
                QualitySampling qualitySampling = qualityService.initializeQualityObjById(PvqTypeEnum.SAMPLING.toString(),ownerId)
                qualitySampling.removeFromComments(comment)
                CRUDService.save(qualitySampling)
                break;
            case CommentTypeEnum.CASE_CORRECTIONS:
                QualitySampling qualitySampling = qualityService.initializeQualityObjById(PvqTypeEnum.CASE_CORRECTIONS.toString(), ownerId)
                qualitySampling.removeFromComments(comment)
                CRUDService.save(qualitySampling)
                break;
            case CommentTypeEnum.DRILLDOWN_RECORD:
                DrilldownCLLData cllRecord = DrilldownCLLData.get(ownerId)
                def metadataRecord = getMetadataRecordForDrilldownRecord(cllRecord)
                String oldComments = metadataRecord.comments.join(", ")
                metadataRecord.removeFromComments(comment)
                CRUDService.save(metadataRecord)
                String newComments = metadataRecord.comments.join(", ")
                commentAuditLog(cllRecord, newComments, commentType, oldComments, true)
                break;
            case CommentTypeEnum.PUBLISHER_SECTION:
                PublisherConfigurationSection publisherConfigurationSection = PublisherConfigurationSection.get(ownerId)
                publisherConfigurationSection.removeFromComments(comment)
                CRUDService.save(publisherConfigurationSection)
                break;
            case CommentTypeEnum.PUBLISHER_FULL:
                PublisherReport publisherReport = PublisherReport.get(ownerId)
                publisherReport.removeFromComments(comment)
                CRUDService.save(publisherReport)
                break;
            case CommentTypeEnum.SCHEDULER:
                SchedulerConfigParams cfgParam = SchedulerConfigParams.get(ownerId)
                cfgParam.removeFromComments(comment)
                CRUDService.save(cfgParam)
                break;
            case CommentTypeEnum.SCHEDULER_RR:
                ReportRequestComment rrc=ReportRequestComment.get(ownerId)
                CRUDService.delete(rrc)
                break;
        }
    }
    @Transactional
    void deleteMultipleComment(Comment comment, Long ownerId, CommentTypeEnum commentType) {
        if (commentType in [CommentTypeEnum.CASE_QUALITY, CommentTypeEnum.SUBMISSION_QUALITY, CommentTypeEnum.SAMPLING]) {
            def obj
            if (commentType == CommentTypeEnum.CASE_QUALITY) {
                obj = QualityCaseData.get(ownerId)
            } else if (commentType == CommentTypeEnum.SUBMISSION_QUALITY) {
                obj = QualitySubmission.get(ownerId)
            } else {
                obj = QualitySampling.get(ownerId)
            }
            obj.removeFromComments(comment)
            CRUDService.save(obj)
        } else if (commentType == CommentTypeEnum.DRILLDOWN_RECORD) {
            DrilldownCLLData cllRecord = DrilldownCLLData.get(ownerId)
            def metadataRecord = getMetadataRecordForDrilldownRecord(cllRecord)
            String oldComments = metadataRecord.comments.join(", ")
            Comment lastComment = metadataRecord.getComments().find { it.textData == comment.textData }
            if (lastComment) {
                metadataRecord.removeFromComments(lastComment)
                CRUDService.save(metadataRecord)
                String newComments = metadataRecord.comments.join(", ")
                commentAuditLog(cllRecord, newComments, commentType, oldComments, true)
            }
        }
    }

    String getReportResultChartAnnotation(Long reportId){
        ReportResult reportResult = ReportResult.get(reportId)
        String annotationData = ""
        if(reportResult?.getComments() && !reportResult.getComments().isEmpty()){
            Comment lastComment = reportResult.getComments().sort{cmt1, cmt2 -> cmt2.lastUpdated <=> cmt1.lastUpdated}.first()

            String rawFullName = userService.getUserByUsername(lastComment.createdBy)?.fullName
            String userFullName = StringEscapeUtils.escapeHtml4(rawFullName)
            String commentText = StringEscapeUtils.escapeHtml4(lastComment.textData ?: "")
            annotationData = userFullName + " (${new SimpleDateFormat("dd-MMM-yyyy",Locale.ENGLISH).format(lastComment.lastUpdated)}): " + commentText
        }
        annotationData
    }

    String getLatestCommentForDrilldownRecord(def cllRecord){
        String latestComment = null
        if(cllRecord.comments && cllRecord.comments.size() > 0){
            latestComment = cllRecord.comments.last().textData
        }
        latestComment
    }

    def getMetadataRecordForDrilldownRecord(DrilldownCLLData cllRecord){
        Map metadataParams = [:]
        JsonSlurper jsonSlurper = new JsonSlurper()
        metadataParams.masterCaseId = Long.valueOf(jsonSlurper.parseText(cllRecord.cllRowData)['masterCaseId'])
        metadataParams.processedReportId = jsonSlurper.parseText(cllRecord.cllRowData)['vcsProcessedReportId']
        metadataParams.tenantId = Long.valueOf(jsonSlurper.parseText(cllRecord.cllRowData)['masterEnterpriseId'])
        metadataParams.senderId = jsonSlurper.parseText(cllRecord.cllRowData)['pvcIcSenderId']
        metadataParams.masterVersionNum = Long.valueOf(jsonSlurper.parseText(cllRecord.cllRowData)['masterVersionNum'])
        def metadataRecord
        if (metadataParams.senderId) {
            metadataRecord = InboundDrilldownMetadata.getMetadataRecord(metadataParams).get()
        }
        else
            metadataRecord = DrilldownCLLMetadata.getMetadataRecord(metadataParams).get()
        if(metadataRecord == null){
            if (metadataParams.senderId) {
                metadataRecord = new InboundDrilldownMetadata()
                metadataRecord.senderId = Long.valueOf(metadataParams.senderId)
                metadataRecord.caseVersion = metadataParams.masterVersionNum
            }
            else {
                metadataRecord = new DrilldownCLLMetadata()
                metadataRecord.processedReportId = metadataParams.processedReportId
            }
            metadataRecord.caseId = metadataParams.masterCaseId
            metadataRecord.tenantId = metadataParams.tenantId
            metadataRecord.workflowState = WorkflowState.defaultWorkState
            try{
                CRUDService.saveOrUpdate(metadataRecord)
                return metadataRecord
            }catch(Exception e){
                log.error("Error in saving metadata record", e)
            }
        }
        return metadataRecord
    }

    void commentAuditLog(def entity, String newComments, CommentTypeEnum commentType, String oldComments, boolean isDelete = false) {
        String extraValue = ""
        if(commentType == CommentTypeEnum.DRILLDOWN_RECORD) {
            if(isDelete) {
                extraValue = " - comment removed from case ${new JsonSlurper().parseText(entity.cllRowData)['masterCaseNum']}"
            } else {
                extraValue = " - comment added to case ${new JsonSlurper().parseText(entity.cllRowData)['masterCaseNum']}"
            }
            entity = ExecutedReportConfiguration.findById(entity.executedReportId)
        }
        if(commentType == CommentTypeEnum.REPORT_RESULT) {
            if(isDelete) {
                extraValue = " - removed comment from report output"
            } else {
                extraValue = " - added comment to report output"
            }
        }
        AuditLogConfigUtil.logChanges(entity, [comments:newComments], [comments:oldComments], Constants.AUDIT_LOG_UPDATE, extraValue)
    }
}
