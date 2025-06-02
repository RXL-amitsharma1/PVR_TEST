<g:javascript>
    var caseNumberWithVersionListUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "caseList")}";
    var configurationsListUrl = "${createLink(controller: "icsrProfileConfigurationRest", action: "profileListForManual")}";
    var templateQueryListUrl = "${createLink(controller: "icsrProfileConfigurationRest", action: "templateQueryForManual")}";
</g:javascript>
<asset:stylesheet src="icsrManualAddCase"/>
<div class="modal fade" id="addToScheduleManual" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <g:form action="manualScheduleCase" name="addToScheduleManualForm">
                <div class="modal-header">
                    <button type="button" class="close m-t-0" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><g:message code="icsr.profile.manual.schedule"/></h4>
                </div>

                <div class="modal-body" style="padding: 12px !important;">
                    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert"
                         id="addToManualCaseDlgErrorDiv" style="display: none">
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <i class="fa fa-check"></i> <g:message code="app.error.fill.all.required"/>
                    </div>

                    <div class="row">
                            <g:message code="icsr.profile.manual.schedule.casenumber"/>
                            <span class="required-indicator">*</span>
                    </div>
                    <div class="row m-b-5">
                        <div class="col-md-9" style="margin-left: -5px;">
                            <g:select name="caseNumberWithVersion"
                                      from="${[]}"
                                      class="form-control"/>
                        </div>
                        <div class="col-md-3"><g:actionSubmit class="btn btn-info" action="reProcess"
                                                              value="${message(code: 'icsr.button.reProcess.label')}"/></div>
                    </div>

                    <div class="row m-b-5">
                        <g:message code="icsr.profile.manual.schedule.profile"/>
                        <span class="required-indicator">*</span>

                        <div>
                            <g:select name="profileId" class="form-control" from="${[]}"/>
                        </div>
                    </div>

                    <div class="row m-b-5">
                        <g:message code="icsr.profile.manual.schedule.templateQuery"/>
                        <span class="required-indicator">*</span>

                        <div>
                            <g:select name="templateQueryId" class="form-control" from="${[]}"/>
                        </div>
                    </div>

                    <div class="row m-b-5" id="deviceIdDiv">
                        <g:message code="icsr.profile.manual.schedule.Device"/>
                        <span id="deviceNumberSpan" class="required-indicator" style="display: none">*</span>

                        <div>
                            <g:select name="deviceId" class="form-control" from="${[]}"></g:select>
                        </div>
                    </div>

                    <div class="row m-b-5">
                        <g:message code="icsr.profile.manual.authorization.type"/>
                        <span id="authorizationTypeSpan" class="required-indicator" style="display: none">*</span>

                        <div>
                            <g:select name="authorizationType" class="form-control" from="${[]}"></g:select>
                        </div>
                    </div>

                    <div class="row m-b-5">
                        <g:message code="icsr.profile.manual.approval.number"/>
                        <span id="approvalNumberSpan" class="required-indicator" style="display: none">*</span>
                        <div>
                            <g:select name="approvalNumber" class="form-control" from="${[]}"></g:select>
                        </div>
                    </div>

                    <div class="row m-b-5">
                        <g:message code="icsr.profile.manual.schedule.dueInDays"/>
                        <span class="required-indicator">*</span>

                        <div>
                            <g:textField type="number" min="1" data-evt-onkeyup='{"method": "checkDecimal", "params": []}'
                                         name='dueInDays' class="form-control dueInDays" maxlength="9"/>
                        </div>
                    </div>

                    <div class="row">
                        <div id="excludeNonValidCases" class="checkbox checkbox-primary">
                            <g:checkBox name="isExpedited"/>
                            <label for="isExpedited">
                                <g:message code="icsr.profile.manual.schedule.expedited"/>
                            </label>
                        </div>
                    </div>

                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-primary"
                            data-evt-clk='{"method": "validateValues", "params": []}'><g:message code="default.button.submit.label"/>
                    </button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <g:message code="default.button.cancel.label"/>
                    </button>
                </div>
            </g:form>
        </div>
    </div>
</div>