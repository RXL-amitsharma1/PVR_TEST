<style>
    .padding{padding:5px;}
</style>
<div class="modal fade caseHistoryDetails"  data-backdrop="static" style="margin-left: 5px" id="caseHistoryDetails"
     tabindex="-1" role="dialog">
    <div class="modal-dialog modal-md" role="document" style="overflow-y: initial">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" aria-label="Close" data-dismiss="modal">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.caseHistory.label"/></h4>
            </div>

            <div class="modal-body" style="height: 400px; padding: 20px 10px">
                <div class="row">
                    <div class="col-md-6">
                        <label><g:message code="app.caseNumber.label"/> : </label>
                        <span id="caseDetailsCaseNumber"></span>
                    </div>
                    <div class="col-md-6">
                        <label><g:message code="app.label.case.series.version"/> : </label>
                        <span id="caseDetailsVersionNumber"></span>
                    </div>
                </div>
                <div class="row" style="padding-top:15px;">
                    <div class="col-md-12">
                        <table id="caseHistoryTable" border="1" bordercolor="#D3D3D3" width="100%">
                            <thead>
                                <tr>
                                    <th class="padding"><g:message code="icsr.case.tracking.recipient"/></th>
                                    <th class="padding"><g:message code="app.label.icsr.case.history.status"/></th>
                                    <th class="padding"><g:message code="app.label.icsr.case.history.date"/></th>
                                    <th class="padding"><g:message code="app.label.icsr.case.history.lastUpdatedBy"/></th>
                                    <th class="padding"><g:message code="app.label.icsr.case.history.lastUpdated"/></th>
                                </tr>
                            </thead>
                            <tbody id="caseHistoryData">
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