<%@ page import="com.rxlogix.config.Comment" %>
<div class="modal fade add-case-modal" id="addCaseNumberComment"  data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">

    <div class="vertical-alignment-helper">

        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">
                <div class="modal-header dropdown">
                    <button type="button" class="close successMsg" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"><g:message code="app.label.comment.add"/></h4>
                </div>

                <div class="alert alert-danger alert-dismissible commentErrorDiv" role="alert" hidden="hidden">
                    <button type="button" class="close" id="commentErrorDiv">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <div id="errormessage"></div>
                </div>

                <div id="commentsList" style="padding: 20px;"></div>
                <div class="modal-body case-list-modal-body">
                    <div class="alert alert-danger hide">
                        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                        <strong><g:message code="app.add.case.error.label" /> !</strong> <span class="errorMessageSpan"></span>
                    </div>
                    <div class="add-comment-component">
                        <form name="updateCaseNumberCommentForm">
                            <div class="row">
                                <div class="col-lg-12">
                                    <label for="comments"><g:message code="app.caseList.comments"/><span
                                            class="required-indicator">*</span></label>
                                    <g:hiddenField name="executedCaseSeries" value="${caseSeriesId}"/>
                                    <g:hiddenField name="caseNumberUniqueId" value=""/>
                                    <g:textArea id="comments" name="comments" maxlength="${Comment.constrainedProperties.textData.maxSize}" class="form-control withCharCounter"/>
                                </div>
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
                            <button type="button" class="btn btn-default hideCommentForm"><g:message code="default.button.cancel.label"/></button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>