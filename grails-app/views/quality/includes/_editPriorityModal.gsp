<div id="editPriorityModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><g:message code="app.pvq.modal.priority.title"/></h4>
            </div>

            <div class="modal-body">
                    <g:select name="priorityValue" from="[]"
                              noSelection="['' : 'Choose a priority value']"/>
                <input type="hidden" id="priorityCaseNumber">
                <input type="hidden" id="priorityCaseVersion">
                <input type="hidden" id="priorityErrorType">
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary updatePriority" data-dismiss="modal"><g:message code="default.button.update.label"/></button>
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
            </div>
        </div>

    </div>
</div>