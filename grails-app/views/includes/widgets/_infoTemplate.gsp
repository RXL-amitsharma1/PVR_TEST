<div class="modal fade" id="${warningModalId?:"warningModal"}"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">${title ?:message(code: 'app.label.information') }</h4>
            </div>

            <div class="modal-body">


                <div class="description" style="font-weight:bold;">${messageBody ?: ''}</div>

                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                        code="default.button.ok.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->