<div id="capaOfferModal" class="modal fade" role="dialog" data-backdrop="static" data-keyboard="false">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title modalHeader"><g:message code="app.label.submission.modalHeader"/></h5>
            </div>

            <div class="modal-body">
                <g:message code="app.label.submission.modalContent"/>
                <div id="capaLinkList" style="text-align: left">
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
            </div>
        </div>

    </div>
</div>
<script>
    var capaCreateUrl = "${createLink(controller: 'issue', action: 'create')}";
</script>