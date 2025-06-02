<div class="modal fade" id="publisherWarningModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#publisherWarningModal"]}' aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">Warning</h4>
            </div>
            <div class="modal-body">
                <div id="warningType"></div>
                <div class="extramessage"></div>
            </div>
            <div class="modal-footer">
                <button id="publisherWarningButton" class="btn btn-primary">
                    <g:message code="default.warningModal.message.continue.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "modalHide", "params": ["#publisherWarningModal"]}'><g:message
                        code="default.button.cancel.label"/></button>
            </div>
        </div>
    </div>
</div>