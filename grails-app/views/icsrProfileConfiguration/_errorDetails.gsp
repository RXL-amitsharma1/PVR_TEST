<style>
    .padding{padding:5px;}
</style>
<div class="modal fade errorDetails"  data-backdrop="static" style="margin-left: 5px" id="errorDetails"
     tabindex="-1" role="dialog">
    <div class="modal-dialog modal-md" role="document" style="overflow-y: initial">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" aria-label="Close" data-dismiss="modal">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.errorDetails.label"/></h4>
            </div>

            <div class="modal-body" style="height: 400px;padding: 20px 10px">
                <div class="row">
                    <div class="col-md-6">
                        <label><g:message code="app.caseNumber.label"/> : </label>
                        <span id="errorDetailCaseNumber"></span>
                    </div>
                    <div class="col-md-6">
                        <label><g:message code="app.label.case.series.version"/> : </label>
                        <span id="errorDetailVersionNumber"></span>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <label><g:message code="app.icsr.ack.filename.label"/> : </label>
                        <span id="errorAckFileName"></span>
                    </div>
                </div>
                <div class="row" style="padding-top:15px;">
                    <div class="col-md-12">
                        <table id="errorDetailsTable" border="1" bordercolor="#D3D3D3" width="100%">
                            <thead>
                                <tr>
                                    <th class="padding"><g:message code="app.error.label"/></th>
                                </tr>
                            </thead>
                            <tbody id="errorDetailsData">
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