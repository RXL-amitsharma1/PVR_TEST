<div class="modal fade" id="deleteWarningModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="deleteWarningModalLabel">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteWarningModalLabel">${title ?: 'Warning'}</h4>
            </div>

            <div class="modal-body">
                <div id="warningType">${queryType ?: g.message(code: 'are.you.sure.you.want.to.delete.this')}</div>

                <p></p>
                <div class="description" style="font-weight:bold;">${messageBody ?: ''}</div>

                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button id="deleteButton" class="btn btn-danger">
                    <span class="glyphicon glyphicon-trash icon-white"></span>
                    ${message(code: 'default.button.delete.label', default: 'Delete')}
                </button>
            </div>

        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->