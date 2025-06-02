<div class="modal fade" id="regenerateCaseModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="regenerateCaseModalLabel"
     style="z-index: 9999;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="regenerateCaseModalLabel"></h4>
            </div>

            <div class="modal-body">
                <div>
                    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="regenerateErrorMessageDiv" style="display: none">
                        <button type="button" class="close regenerateErrorMessageDivClose">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <i class="fa fa-check"></i> <g:message code="app.label.justification.cannotbeblank"/>
                    </div>
                    <p><g:message code="icsr.regenerate.case.warning"/></p>
                    <g:form autocomplete="off" name="justificationForm" id="justificationForm">
                        <div class="description-wrapper">
                            <label>
                                <g:message code="app.label.justification"/><span class="required-indicator">*</span>
                            </label>
                            <g:textArea rows="5" cols="3" name="comments" id="regenerateComments" maxlength="255"
                                        style="height: 110px;" value="" class="form-control "/>
                            <g:message code="icsr.commet.maxSize.exceeded"/>
                        </div>


                    </g:form>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="regenerateButton" class="btn btn-success confirm-paste">
                    <g:message code="default.button.confirm.label"/>
                </button>
                <button type="button" class="btn btn-default cancel" data-dismiss="modal">
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

