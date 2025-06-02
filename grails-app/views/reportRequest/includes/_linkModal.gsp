<div class="modal fade" id="reportRequestLinkModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="reportRequestLinkModal"
     aria-hidden="true" style="z-index: 9999;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close closeLinkSelect" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" ><g:message code="app.label.reportRequest.linking"/></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="deleteDlgErrorDiv" style="display: none">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <i class="fa fa-check"></i> <g:message code="app.error.fill.all.required"/>
                </div>

                <input type="hidden" id="RRLinkFrom" name="from.id" value="${reportRequestId}" >

                <label style="margin-top: 25px"><g:message code="app.label.report.request"/>:</label>
                <div id = "RRLinkToDiv"><select name="to.id" id="RRLinkTo" class="form-control"></select></div>

                <label style="margin-top: 25px"><g:message code="app.label.reportRequestLinkType.link.type"/>:</label>
                <div id = "RRLinkTypeDiv"><g:select name="linkType" optionKey="id" optionValue="name"
                          from="${linkType}" class="form-control select2-box" /></div>

                <label style="margin-top: 25px"><g:message code="app.label.description"/>:</label>
                <g:textArea id="RRLinkDescription" name="RRLinkDescription" value="" rows="15" cols="40" maxlength="4000" class="form-control" />

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey closeLinkSelect" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button type="button" id="saveLinkButton" class="btn btn-primary">
                    ${message(code: 'default.button.save.label', default: 'Save')}
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->