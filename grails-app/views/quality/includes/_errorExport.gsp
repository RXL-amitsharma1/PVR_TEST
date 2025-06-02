<div class="modal fade" id="errorExportModal" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="errorExportModal"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">${title ?: 'Warning'}</h4>
            </div>

            <div class="modal-body">
                <div class="messageBody"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->