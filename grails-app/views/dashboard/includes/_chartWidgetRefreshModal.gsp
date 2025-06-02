<div id="chartConfigurationModal" class="modal fade" role="dialog" style="height: 500px; overflow: hidden;">
    <div class="modal-dialog modal-lg">
        <input type="hidden" id="selectedWidget">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h5 class="modal-title modalHeader"><g:message code="app.label.reportConfiguration"/></h5>
            </div>

            <div class="modal-body"  id="chartConfigurationModalContent" style="max-height: 350px">

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary submitRunRefreshModal" data-dismiss="modal"><g:message code="default.button.saveAndRun.label"/></button>
                <button type="button" class="btn btn-primary submitSaveRefreshModal" data-dismiss="modal"><g:message code="default.button.save.label"/></button>
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
            </div>
        </div>

    </div>
</div>