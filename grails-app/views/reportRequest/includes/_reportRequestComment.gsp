<div class="modal fade" id="reportCommentModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">
            <div class="modal-header dropdown">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.reportRequest.comment.label"/></h4>

            </div>
            <div class="modal-body action-item-modal-body">
                 <g:textArea id="reportComment" name="reportComment" class="form-control reportComment" rows="5" maxlength="4000"/>
                 <g:hiddenField id="index" name="index" />
                 <g:hiddenField id="commentId" name="commentId" />
                 <g:hiddenField id="edited" name="edited" />
                 <g:hiddenField id="newComment" name="newComment" />
                 <g:hiddenField id="dateCreated" name="dateCreated"/>
            </div>

            <div class="modal-footer">
               <div  class="buttons creationButtons">
                  <input id="creationScreenButton" type="button" class="btn btn-primary save-action-item creationButton" value="${message(code: "app.save.button.label")}">
                  <button id="closeCommentModal" type="button" class="btn pv-btn-grey"  data-dismiss="modal"><g:message code="app.button.close"/></button>
               </div>

            </div>
        </div>

    </div>
</div>