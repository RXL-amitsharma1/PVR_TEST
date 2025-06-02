<%@ page import="com.rxlogix.enums.CommentTypeEnum" %>
<div id="commentModal" class="modal fade"  data-backdrop="static" role="dialog" aria-hidden="true" style="display: none;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">×</button>
                <g:hiddenField name="${CommentTypeEnum.EXECUTED_CONFIGURATION.toString()}" value="${message([code: "${CommentTypeEnum.EXECUTED_CONFIGURATION}.comment.modal.tittle"])}"/>
                <g:hiddenField name="${CommentTypeEnum.REPORT_RESULT.toString()}" value="${message([code: "${CommentTypeEnum.REPORT_RESULT}.comment.modal.tittle"])}"/>
                <g:hiddenField name="${CommentTypeEnum.DRILLDOWN_RECORD.toString()}" value="${message([code: "${CommentTypeEnum.DRILLDOWN_RECORD}.comment.modal.title"])}"/>
                <g:hiddenField name="${CommentTypeEnum.SCHEDULER.toString()}" value="${message([code: "comment.textData.label"])}"/>
                <g:hiddenField name="${CommentTypeEnum.SCHEDULER_RR.toString()}" value="${message([code: "comment.textData.label"])}"/>
                <h5 class="modal-title" id="commentsTitle"></h5>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <span id="errorNotification"></span>
                </div>
                <div id="commentsList">
                    %{--Load Comments through ajax call--}%
                </div>

                <div class="add-comment-component">
                    <form name="annotationForm" action="#">
                        <g:hiddenField name="ownerId" value=""/>
                        <g:hiddenField name="commentType" value=""/>
                        <div class="form-group">
                            <label for="comment.textData"><g:message code="comment.textData.label"/></label>
                            <g:textArea name="comment.textData" class="form-control withCharCounter" rows="5" maxlength="4000"/>
                        </div>
                    </form>
                </div>
            </div>

            <div>
                <div class="modal-footer">
                    <div class="btn-add-comment" data-evt-clk='{"method": "showCommentForm", "params": []}'>
                        <a href="#" class="btn btn-primary"><span class="glyphicon glyphicon-comment"></span> <g:message code="comment.textData.label" />
                        </a>
                    </div>

                    <div class="add-comment-component">
                        <button type="button" class="btn btn-primary saveComment"><g:message code="default.button.add.label"/></button>
                        <button type="button" class="btn pv-btn-grey hideCommentForm"><g:message code="default.button.cancel.label"/></button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <g:hiddenField name="appContext" value="${request.getContextPath()}"/>
</div>
<g:render template="/includes/widgets/errorTemplate"/>
<asset:javascript src="app/comment.js"/>
