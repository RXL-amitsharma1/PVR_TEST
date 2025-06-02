<div class="modal fade" id="actionCompletionModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#actionCompletionModal"]}' aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">Warning</h4>
            </div>
            <div class="modal-body">
                <div id="warningType"></div>
                <div class="extramessage"></div>
            </div>
            <div class="modal-footer">
                <button id="actionCompletionWarningButton" class="btn btn-primary">
                    <g:message code="default.button.yes.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "modalHide", "params": ["#actionCompletionModal"]}'><g:message
                        code="default.button.no.label"/></button>
            </div>
        </div>
    </div>
</div>