
<div class="modal fade" id="${errorModalId ?: 'errorModal'}"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="errorModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel"><g:message code="app.widget.errors"/></h4>
            </div>

            <div class="modal-body">
                <div class="description" style="font-weight:bold;">${messageBody ?: ''}</div>

                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
            </div>
        </div>
    </div>
</div>