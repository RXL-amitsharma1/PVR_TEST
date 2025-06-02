<!-- Modal -->

%{--
Input parameters:
    (optional) title         : Widget Title, default will be Warning
    (required) messageBody   : Message to be display should be pass into it
    (optional) queryType    : Type of query,default will be "Are you sure you want to proceed further?"

To include the gsp in the page, use:
<g:render template="/includes/widgets/warningTemplate" model="[title: 'Warning', messageBody: 'Test Message', queryType: 'Are you sure you want to proceed further?']"/>
--}%


<div class="modal fade" id="${warningModalId?:"warningModal"}"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#${warningModalId?:"warningModal"}"]}' aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">${title ?: 'Warning'}</h4>
            </div>

            <div class="modal-body">
                <div id="warningType">${queryType ?: g.message(code: 'app.label.warning.modal')}</div>

                <p></p>

                <div class="description" style="font-weight:bold;">${messageBody ?: ''}</div>

                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button id="${warningButtonId?:"warningButton"}" class="btn btn-primary">
                    <g:message code="default.warningModal.message.continue.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "modalHide", "params": ["#${warningModalId?:"warningModal"}"]}'><g:message
                        code="default.button.cancel.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<script type="text/javascript">
    $(function () {
        $('#${warningModalId?:"warningModal"}').on('show.bs.modal', function (event) {
            //Make sure cancel and continue buttons are enabled
            $(".btn").removeAttr("disabled", "disabled");
        });
    });
</script>