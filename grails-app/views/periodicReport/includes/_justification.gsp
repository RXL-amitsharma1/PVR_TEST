<div class="modal fade" id="reportJustificationModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close closeJustification" aria-hidden="true">&times;</button>
                <h4 class="modal-title" ><g:message code="app.label.justification"/></h4>
            </div>
            <div class="modal-body">
                <label for="reportJustification" class="forReport"><g:message code="app.report.justification"/></label>
                <label for="reportJustification" class="forUnschedule" style="display: none"><g:message code="app.unschedule.justification"/></label>
                <label for="reportJustification" class="forBulk" style="display: none"><g:message code="app.bulk.justification"/></label>
                <input id="reportJustification" name="auditLogJustification" class="form-control" maxlength="255"/>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary save"  type="button">
                    <g:message code="default.warningModal.message.continue.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey closeJustification"><g:message code="default.button.cancel.label"/></button>
            </div>
        </div>
    </div>
</div>
<script>
    $(function () {
       $(document).on("click", "#reportJustificationModal .closeJustification", function(){
           $('#reportJustification').val('');
           $('#reportJustificationModal').modal('hide');
       });
       $(document).on("click", "#reportJustificationModal .save", function(){
           $('#reportJustificationModal').modal('hide');
       });
    });
</script>