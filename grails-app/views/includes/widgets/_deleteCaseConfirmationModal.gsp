<div class="modal fade" id="deleteCaseModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="deleteCaseModalLabel"
     aria-hidden="true" style="z-index: 9999;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteCaseModalLabel"></h4>
            </div>

            <div class="modal-body">
                <g:renderClosableInlineAlert id="deleteCaseDlgErrorDiv" icon="warning" type="danger" forceLineWrap="true"
                                             message="${g.message(code: 'app.label.justification.cannotbeblank')}" />

                <div id="nameToDelete"></div>
                <p></p>
                <div class="row mb-5">
                    <div class="col-md-6">
                        <label><g:message code="app.caseList.caseNumber"/> :</label>
                        <span class="caseNumberValue"></span>
                    </div>
                    <div class="col-md-6">
                        <label><g:message code="app.label.reportSubmission.cases.versionNumber"/> :</label>
                        <span class="versionNumberValue"></span>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <label><g:message code="icsr.case.tracking.profile"/> :</label>
                        <span class="profileNameValue"></span>
                    </div>
                    <div class="col-md-6">
                        <label><g:message code="app.label.followUpType"/> :</label>
                        <span class="followUpNumberValue"></span>
                    </div>
                </div>
                <div class="description" style="font-weight:bold;"></div>
                <div class="extramessage"></div>

                <g:render template="/includes/justification/standardJustification" model="[justificationLabel: message(code: 'app.label.justification'), justificationId: 'deleteCaseJustification', justificationJaId: 'deleteCaseJustificationJa', maxlength: 2000]"/>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button id="deleteCaseButton" class="btn btn-danger">
                    <span class="glyphicon glyphicon-trash icon-white"></span>
                    ${message(code: 'default.button.deleteRecord.label', default: 'Delete Record')}
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

