<asset:stylesheet src="icsrSubmissionHistoryCase.css"/>
<div class="modal fade submissionHistoryCase"  data-backdrop="static" style="margin-left: 5px" id="submissionHistoryCase"
     tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document" style="overflow-y: initial; width: 67%">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" aria-label="Close" data-dismiss="modal">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="icsr.case.tracking..caseSubmissionHistory"/></h4>
            </div>

            <div class="modal-body" style="height: 400px; padding: 20px 10px">
                <div id="submissionHistoryErrorMessage" class="alert-danger"></div>

                <div class="row">
                    <div class="col-md-4">
                        <label><g:message code="icsr.case.tracking.case.number.label"/> : </label>
                        <span id="caseNumber"></span>
                    </div>
                    <div class="col-md-4">
                        <label><g:message code="app.label.version"/> : </label>
                        <span id="versionNumber"></span>
                    </div>
                    <div class="col-md-4">
                        <label><g:message code="app.label.followUpType"/> : </label>
                        <span id="followupNumber"></span>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-4">
                        <label><g:message code="icsr.case.tracking.recipient"/> : </label>
                        <span id="recipientName"></span>
                    </div>
                    <div class="col-md-4">
                        <label><g:message code="icsr.case.tracking.profile"/> :</label>
                        <span id="profileName" class="forceLineWrap"></span>
                    </div>
                    <div class="col-md-4">
                        <label><g:message code="app.label.localReportMsg"/> :</label>
                        <span id="localReportMessage"></span>
                    </div>
                </div>
                <div class="row" style="padding-top:15px;">
                    <div class="col-md-12">
                        <table class="table table-striped" id="submissionHistoryCaseTable" border="1px" width="100%">
                            <thead>
                                <tr>
                                    <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.status"/></td>
                                    <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.routed.date"/></td>
                                    <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.routed.date.preferred.timezone"/></td>
                                    <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.routedBy"/></td>
                                    <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.comment"/></td>
                                </tr>
                            </thead>
                            <tbody id="caseSubmissionData">
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey cancel" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>