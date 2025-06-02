<div class="modal fade" id="assignSimilarCases" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="assignSimilarLabel"
     aria-hidden="true">
    <div class="vertical-alignment-helper">
        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">

                <!-- Modal header -->
                <div class="modal-header">
                    <button type="button" class="close closeAssignToModal" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <span class="modalHeader"><g:message code="app.ROD.assignTo.modal.title"/></span>
                </div>

                <!-- Modal body -->
                <div class="modal-body">
                <div class="row">
                    <span class="bodyContent"></span>
                </div>

                <div class="radio radio-primary">
                    <input class="submitRadio" id="allSubmissions" type="radio" name="submit_val" checked="checked">
                    <label for="allSubmissions"><span style="padding-left: 5px"><g:message code="app.ROD.assignTo.all"/></span></label>
                </div>

                <div class="radio radio-primary">
                    <input class="submitRadio" id="currentSubmission" type="radio" name="submit_val">
                    <label for="currentSubmission"><span style="padding-left: 5px"><g:message code="app.ROD.assignTo.current"/></span></label>
                </div>

                <span class="assignToEvent" hidden="hidden"></span>

                </div>

                <!-- Modal footer -->
                <div class="modal-footer">
                    <button type="button" class="btn btn-success" id="submitAssignTo" data-dismiss="modal">
                        <g:message code="app.label.submit"/>
                    </button>
                    <button type="button" class="btn pv-btn-grey closeAssignToModal" data-dismiss="modal"><g:message
                            code="default.button.cancel.label"/></button>
                </div>

            </div><!-- modal-content ends-->
        </div><!-- modal-dialog ends-->
    </div>

</div><!-- modal -->
