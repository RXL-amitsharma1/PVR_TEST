<%@ page import="grails.util.Holders; com.rxlogix.config.UnitConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.Constants; com.rxlogix.util.DateUtil; com.rxlogix.enums.ReportSubmissionStatusEnum" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<asset:javascript src="app/scheduler.js"/>
<asset:javascript src="app/icsrCaseTracking/reason.js"/>
<asset:stylesheet src="icsrCaseTrackingSubmissionForm.css"/>
<script>
    $(function () {
        $('select.select2-box').select2();
        $(document).off('change', '.icsrCaseState');
        $(document).on('change', '.icsrCaseState', function () {
            var status = $('select[name=icsrCaseState]').val();
            if(status.toUpperCase() == "SUBMITTED") {
                $(".reportSubDateDiv").css("display", "block");
                $(".submissionDateDiv").css("display", "block");
                $(".submissionTimeDiv").css("display", "block");
                $(".timeZoneDiv").css("display", "block");
            }else {
                $(".reportSubDateDiv").css("display", "none");
                $(".submissionDateDiv").css("display", "none");
                $(".submissionTimeDiv").css("display", "none");
                $(".timeZoneDiv").css("display", "none");
            }

            if (status.toUpperCase() === 'SUBMISSION_NOT_REQUIRED' || status.toUpperCase() === 'SUBMISSION_NOT_REQUIRED_FINAL') {
                $(document).trigger('loadStandardJustifications', ['ICSRSubmissionNotRequired']);
            } else if (status.toUpperCase() === 'SUBMITTED') {
                $(document).trigger('loadStandardJustifications', ['ICSRSubmit']);
            } else {
                $(document).trigger('hideStandardJustifications');
            }
        });

        $('.icsrCaseState').trigger("change");
    });
</script>
<style>
.sub-sate-width > .row{width:auto!important;}
</style>

<!-- Modal header -->
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <span class="modalHeader"><g:message code="app.label.submit"/></span>
</div>

<!-- Modal body -->
<div class="modal-body" style="min-height: 500px; padding: 20px 10px">
    <div class="alert alert-danger hide">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <span id="bulkSubmitErrorMessage"></span>
    </div>
    <g:renderClosableInlineAlert id="icsrTrackingBulkSubmFormValidationAlert" icon="warning" type="danger" forceLineWrap="true"
                                 message="${g.message(code: 'app.label.comment.is.required')}" />

    <form name="reportSubmissionForm" action="#">
        <g:hiddenField name="icsrCaseIds" value="${id}"/>
        %{--<g:hiddenField name="profileName" value="${profileName}"/>--}%
        %{--<g:hiddenField name="currentState" value="${currentState}"/>--}%
        <div>
            <div class="row">
                <div class="col-xs-4 form-group">
                    <label><g:message code="report.submission.status"/></label>

                    <g:select name="icsrCaseState"
                              from="${ViewHelper.getCaseSubmissionStatusEnumI18n()}" optionKey="name"
                              optionValue="display" class="form-control select2-box icsrCaseState"/>
                </div>
                <div class="col-xs-4 form-group">
                    <label><g:message code="icsr.case.tracking.recipient"/></label>
                    <g:textField name="reportingDestinations" class="form-control" value="${recipient}" readonly="readonly"/>
                </div>
            </div>

            <div id="needApprovalDiv"
                 class="${Holders.config.getProperty('icsr.case.submission.user.revalidate', Boolean) ? '' : 'hidden'}">
                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label>
                            <g:message code="app.label.workflow.rule.needApproval"/>
                        </label>
                        <div>
                            <g:message code="app.label.workflow.rule.needApproval.submission"/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label>
                            <g:message code="user.username.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <input autocomplete="autocomplete_off_xfr4!k1" name="${new Date().getTime()}"
                               disabled="true" id="login-input" value="${currentUser.fullName}"
                               class="form-control login-input">
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label>
                            <g:message code="user.password.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <input autocomplete="autocomplete_off_xfr4!k" name="${new Date().getTime()}"
                               id="bulk-submission-password-input" class="form-control bulk-submission-password-input" type="text">
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label>
                            <g:message code="report.submission.approvalDate"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <g:set var="timeZoneLocal" value="${com.rxlogix.enums.TimeZoneEnum.values().find{it.timezoneId == userService.getUser().preference.timeZone}}"/>
                        <input type="text" id="approvalDate" name="approvalDate"  disabled="true" class="form-control"
                               value="${com.rxlogix.util.RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(userService.getUser(), "dd-MMM-yyyy hh:mm a", timeZoneLocal.timezoneId)} (${timeZoneLocal.timezoneId})">

                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-8 form-group" style="margin-top: 10px">
                    <div class="row">
                        <div class="col-xs-4 submissionDateDiv">
                            <label><g:message code="report.submission.submissionDate"/></label>
                        </div>
                        <div class="col-xs-3 submissionTimeDiv" style="margin-left: 5px;">
                            <label><g:message code="report.submission.submissionTime"/></label>
                        </div>
                        <div class="col-xs-4 timeZoneDiv" style="margin-left: 10px;">
                            <label><g:message code="report.submission.timeZone"/></label>
                        </div>
                    </div>
                    <div class="row form-group reportSubDateDiv" style="margin-left: 10px;">
                        <div class="fuelux sub-sate-width">
                            <g:render template="/configuration/dateWithTimeAndTimezoneTemplate"/>
                            <g:hiddenField name="scheduleDateJSON" id="scheduleDateJSON"/>
                            <g:hiddenField name="schedulerTime"
                                           value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(currentUser, timeZone.getTimezoneId())}"/>
                            <input type="hidden" id="timezoneFromServer" name="timezone"
                                   value="${DateUtil.getTimezone(timeZone)}"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 input-group">
                    <label><g:message code="app.label.document"/></label>
                    <div>
                        <input type="text" class="form-control" id="file_name" readonly="" style="width: 90%;">
                        <label class="input-group-btn">
                            <span class="btn btn-primary">
                                <g:message code="app.label.attach"/>
                                <input type="file" id="bulk_submission_file_input" name="file" multiple=""
                                    accept="application/vnd.openxmlformats-officedocument.wordprocessingml.document, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/pdf, image/png, image/jpeg, text/plain, application/vnd.ms-outlook"
                                    style="display: none;">
                            </span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="row">
                <g:render template="/includes/justification/standardJustification" model="[justificationLabel: message(code: 'report.submission.comment'), justificationId: 'bulkCaseSubmissionComments', justificationJaId: 'bulkCaseSubmissionCommentsJa', maxlength: 2000]"/>
            </div>

            %{--<div class="row">
                <div class="col-xs-6 form-group">
                    <label><g:message code="app.label.action.item.due.date"/></label>

                    <div class="fuelux">
                        <div>
                            <div class="datepicker toolbarInline" id="dueDateDiv">
                                <div class="input-group">
                                    <g:textField class="form-control fuelux" name="dueDate"
                                                 value="${renderShortFormattedDate(date: dueDate)}"/>
                                    <g:render class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>--}%
        </div>
    </form>
</div>

<!-- Modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-primary submit-draft"><g:message code="app.label.submit"/></button>
    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
            code="default.button.cancel.label"/></button>
</div>
