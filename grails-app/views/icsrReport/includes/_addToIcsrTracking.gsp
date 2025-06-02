<%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
<div class="modal fade" id="addToIcsrTracking" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <g:form action="transferCases" name="addToTrackingForm" id="${reportResult?.id}">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><g:message code="app.icsr.add.tracking"/></h4>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-4">
                            <g:message code="icsr.report.case.trasfer.casenumbers"/>
                            <input type="hidden" name="caseNumbersWithVersion" id="caseNumbersWithVersion"/></div>

                        <div class="col-md-8 caseNumbers">
                        </div>

                    </div>

                    <div class="row">
                        <div class="col-md-4"><g:message code="icsr.report.case.trasfer.profile"/></div>

                        <div class="col-md-8">
                            ${referenceProfileId ? executedConfigurationInstance.referenceProfileName : ''}
                            <g:hiddenField name="profile" value="${referenceProfileId}"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><g:message code="app.label.icsr.profile.conf.dueInDays"/></div>

                        <div class="col-md-8">
                            <g:textField type="number" min="1" data-evt-onkeyup='{"method": "checkDecimal", "params": []}'
                                         name='dueInDays' class="form-control dueInDays"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><g:message code="app.label.icsr.profile.conf.expedited"/></div>

                        <div class="col-md-8">
                            <g:checkBox name="isExpedited"/>
                        </div>
                    </div>

                </div>

                <div class="modal-footer">
                    <g:submitButton class="btn btn-primary"
                                    name="${message(code: 'default.button.submit.label')}" data-evt-clk='{"method": "showLoader", "params": []}'/>
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <g:message code="default.button.cancel.label"/>
                    </button>
                </div>
            </g:form>
        </div>
    </div>
</div>