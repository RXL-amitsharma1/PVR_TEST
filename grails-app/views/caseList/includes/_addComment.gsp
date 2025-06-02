<div class="modal fade add-case-modal" id="addCaseNumberComment"  data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">

    <div class="vertical-alignment-helper">

        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">
                <div class="modal-header dropdown">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"><g:message code="app.label.comment.add"/></h4>
                </div>

                <div class="modal-body case-list-modal-body">
                    <div class="alert alert-danger hide">
                        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                        <strong><g:message code="app.add.case.error.label" /> !</strong> <span class="errorMessageSpan"></span>
                    </div>

                    <form name="updateCaseNumberCommentForm">
                        <div class="row">
                            <div class="col-lg-12">
                                <label for="comments"><g:message code="app.caseList.comments"/><span
                                        class="required-indicator">*</span></label>
                                <g:hiddenField name="executedCaseSeries" value="${caseSeriesId}"/>
                                <g:hiddenField name="caseNumberUniqueId" value=""/>
                                <g:hiddenField name="caseNumber" value=""/>
                                <g:hiddenField name="oldComment" value=""/>
                                %{--    maxlength limit is set according to the database limit given by database team     --}%
                                <g:textArea name="comments" class="form-control withCharCounter" maxlength="8000"/>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="modal-footer">
                    <div class="buttons creationButtons">
                        <input id="addcommentButton" type="button" class="btn btn-primary add-comment-to-case"
                               value="${message(code: "app.add.button.label")}">
                        <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                                code="app.button.close"/></button>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>