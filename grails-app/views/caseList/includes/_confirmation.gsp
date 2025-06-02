<div class="modal fade" id="confirmationModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="confirmationModalLabel"
     aria-hidden="true">
    <div class="vertical-alignment-helper">

        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">

                <!-- Modal header -->
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <span class="modalHeader"></span>
                </div>

                <!-- Modal body -->
                <div class="modal-body">
                    <div class="alert alert-danger hide">
                        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                        <span class="errorMessageSpan"></span>
                    </div>
                    <div class="confirmationMessage"></div>
                    <br>

                    <div class="row">
                        <div class="col-md-12">
                            <label for="justification"><g:message code="app.label.justification"/><span
                                    class="required-indicator">*</span></label>
                            <g:textArea id="justification" placeholder="Enter the justification" class="form-control"
                                        maxlength="1000"
                                        name="justification"/>
                        </div>
                    </div>
                </div>

                <!-- Modal footer -->
                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-grey okButton"><g:message
                            code="default.button.ok.label"/></button>
                    <button type="button" class="btn pv-btn-grey closeButton" data-dismiss="modal"><g:message
                            code="default.button.cancel.label"/></button>
                </div>

            </div><!-- modal-content ends-->
        </div><!-- modal-dialog ends-->
    </div>

</div><!-- modal -->
