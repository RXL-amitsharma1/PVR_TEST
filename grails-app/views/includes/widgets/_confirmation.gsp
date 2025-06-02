<div class="modal fade" id="confirmationModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="confirmationModalLabel"
     aria-hidden="true">
    <div class="vertical-alignment-helper">
        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">

                <!-- Modal header -->
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title"><span class="modalHeader"></span></h4>
                </div>

                <!-- Modal body -->
                <div class="modal-body">
                    <div class="confirmationMessage"></div>
                    <br>
                    <div class="description" style="font-weight:bold;"></div>
                </div>

                <!-- Modal footer -->
                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                        code="default.button.cancel.label"/></button>
                    <button type="button" class="btn btn-danger okButton" data-dismiss="modal">
                        <span class="glyphicon glyphicon-trash icon-white"></span>
                        ${message(code: 'default.button.deleteRecord.label')}
                    </button>
                </div>

            </div><!-- modal-content ends-->
        </div><!-- modal-dialog ends-->
    </div>

</div><!-- modal -->
