package com.rxlogix.api

import com.rxlogix.commandObjects.CommentCommand
import com.rxlogix.config.Comment
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.SchedulerConfigParams
import com.rxlogix.enums.CommentTypeEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.springframework.dao.DataIntegrityViolationException

@Secured('permitAll')
class CommentRestController {

    def springSecurityService
    def commentService
    def userService
    def CRUDService
    def calendarService

    def loadComments(CommentCommand commentCommand) {
        Set<Comment> comments = null
        if (params.ownerId?.contains("_")) {
            SchedulerConfigParams configParams = calendarService.getSchedulerConfigParams(params.ownerId)
            comments = configParams?.comments
        } else {
            comments = commentService.fetchComments(commentCommand)
        }
        render template: "comments", model: [comments: comments?.sort { it.dateCreated }, ownerId: commentCommand.ownerId, commentType: commentCommand.commentType, timeZone: userService.user.preference.timeZone]
    }

    def loadLatestComment(CommentCommand commentCommand) {
        Set<Comment> comments = null
        if (params.ownerId?.contains("_")) {
            SchedulerConfigParams configParams = calendarService.getSchedulerConfigParams(params.ownerId)
            comments = configParams?.comments
        } else {
            comments = commentService.fetchComments(commentCommand)
        }
        String latestCommentText = ' '
        if (comments) {
            latestCommentText = comments.sort { it.dateCreated }?.last()?.textData ?: ' '
        }
        render(latestCommentText)
    }

    def save(CommentCommand commentCommand) {
        if(!commentCommand.comment.textData){
            render([error: true, msg : message(code: "com.rxlogix.config.caseDataQuality.comment.isNull")] as JSON)
            return
        }

        String commentType = commentCommand.commentType.name()
        if (!(CommentTypeEnum.values().any { it.name() == commentType })) {
            commentType = CommentTypeEnum.SAMPLING.name()
        }

        if (params.commentType != CommentTypeEnum.SCHEDULER_RR.name()) {
            SchedulerConfigParams configParams = calendarService.getSchedulerConfigParams(params.ownerId, true)
            if (configParams) {
                commentCommand = new CommentCommand(comment: commentCommand.comment, commentType: CommentTypeEnum."${commentType}", ownerId: configParams.id, multipleIds: null)
            }
            if (!commentCommand.validate() || commentCommand.comment.textData.length()>8000) {
                log.warn(commentCommand.errors.allErrors?.toString())
                render([error: true, errors: commentCommand.comment.errors.allErrors.collect {
                    "comment." + it.field
                }, msg       : message(code: "com.rxlogix.config.caseDataQuality.comment.maxSize.exceeded")] as JSON)
                return
            }
        }
        try {
            if(commentCommand.multipleIds!=null){
                for(String ownerId:commentCommand.multipleIds.split(",")) {
                    commentService.save(commentCommand.comment, commentCommand.commentType,Long.parseLong(ownerId))
                }
            }
            else {
                commentService.save(commentCommand.comment, commentCommand.commentType, commentCommand.ownerId)
            }
        } catch (ValidationException ve) {
            log.error("Validation exception while saving comment")
            render([error: true, errors: commentCommand.comment.errors.allErrors.collect {
                "comment." + it.field
            }, msg       : message(code: "default.system.error.message")] as JSON)
            return
        }
        render([success: true] as JSON)
    }

    def delete(CommentCommand commentCommand) {

        boolean isCommentListEmpty = false
        try {
            if (params.commentType == CommentTypeEnum.SCHEDULER_RR.name()) {
                commentService.delete(null, params.comment.id as Long, commentCommand.commentType)
                isCommentListEmpty = commentService.fetchComments(commentCommand)?.isEmpty()
            } else {
                SchedulerConfigParams configParams = calendarService.getSchedulerConfigParams(params.ownerId)
                if (configParams) {
                    configParams.removeFromComments(commentCommand.comment)
                    CRUDService.save(configParams)
                } else if (commentCommand.multipleIds != null && commentCommand.multipleIds.length() > 0) {
                    for (String ownerId : commentCommand.multipleIds.split(",")) {
                        commentService.deleteMultipleComment(commentCommand.comment, Long.parseLong(ownerId), commentCommand.commentType)
                        isCommentListEmpty = commentService.fetchComments(commentCommand)?.isEmpty()
                    }

                } else {
                    commentService.delete(commentCommand.comment, commentCommand.ownerId, commentCommand.commentType)
                    isCommentListEmpty = commentService.fetchComments(commentCommand)?.isEmpty()
                }
            }
        } catch (DataIntegrityViolationException ve) {
            log.error("DataIntegrityViolationException exception while deleting comment")
            render([error: true, msg: message(code: "default.system.error.message")] as JSON)
            return
        }
        render([success: true, isCommentListEmpty: isCommentListEmpty] as JSON)
    }

    def getReportResultChartAnnotation(){
        render commentService.getReportResultChartAnnotation(params.long("reportId"))
    }
}
