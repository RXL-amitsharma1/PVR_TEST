<%@ page import="grails.util.Holders; com.rxlogix.config.UnitConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.Constants; com.rxlogix.util.DateUtil; com.rxlogix.enums.DistributionChannelEnum; com.rxlogix.enums.ReportSubmissionStatusEnum" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<asset:javascript src="app/scheduler.js"/>
<asset:javascript src="app/icsrCaseTracking/reason.js"/>
<asset:stylesheet src="icsrCaseTrackingSubmissionForm.css"/>
<asset:stylesheet src="icsrSubmissionHistoryCase.css"/>

<!-- Modal header -->
<div class="modal-header custom-modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <span class="modalHeader" style="font-size: 18px;"><g:message code="app.label.submission.details"/></span>
</div>

<!-- Modal body -->
<div class="modal-body custom-modal-body" style="min-height: 500px; padding: 20px 10px">
    <div class="commentAlert alert alert-danger hide">
        <a href="#" class="close" aria-label="close" data-evt-clk='{"method": "hideAlert", "params": []}'>&times;</a>
        <span id="submitErrorMessage"></span>
    </div>
    %{--<g:renderClosableInlineAlert id="icsrTrackingSubmFormValidationAlert" icon="warning" type="danger" forceLineWrap="true"
                                 message="${g.message(code: 'app.label.comment.is.required')}" /> --}%
    <form name="reportSubmissionForm" action="#">
        <g:hiddenField name="icsrCaseId" value="${id}"/>
        <g:hiddenField name="profileName" value="${profileName}"/>
        <g:hiddenField name="currentState" value="${currentState}"/>
        <g:hiddenField name="distributionChannel" value="${distributionChannel}"/>
        <div>
            <div class="row">
                <div class="col-xs-4 form-group">
                    <label><g:message code="report.submission.status"/></label>
                    <g:hiddenField name="icsrCaseState" id="icsrCaseState" value="${currentState}"/>
                    <g:if test="${currentState == 'SUBMITTED' && (distributionChannel?.toString() == DistributionChannelEnum.EMAIL?.toString() || distributionChannel?.toString() == DistributionChannelEnum.PAPER_MAIL?.toString())}">
                         <!-- Dropdown populated using a backend helper method -->
                         <g:select name="icsrCaseStateChanged"
                                   from="${ViewHelper.getGeneratedStateOptions()}"
                                   optionKey="name"
                                   optionValue="display"
                                   value="${message(code: "app.icsrCaseState.${currentState}")}"
                                   class="form-control select2-box icsrCaseStateChanged"/>
                     </g:if>
                     <g:elseif test="${currentState == 'SUBMISSION_NOT_REQUIRED' || currentState == 'SUBMISSION_NOT_REQUIRED_FINAL'}">
                         <g:select name="icsrCaseStateChanged"
                                   from="${ViewHelper.getPreviousStateOptions(id, profileName)}"
                                   optionKey="name"
                                   optionValue="display"
                                   value="${message(code: "app.icsrCaseState.${currentState}")}"
                                   class="form-control select2-box icsrCaseStateChanged"/>
                     </g:elseif>
                     <g:else>
                         <!-- Disabled field showing the current state -->
                         <input type="text" class="form-control" name="icsrCaseStateChanged" value="${message(code: "app.icsrCaseState.${currentState}")}" readonly style="pointer-events: none;" tabindex="-1"/>
                     </g:else>
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
                    <div class="col-xs-6 form-group">
                        <label>
                            <g:message code="user.username.label"/>
                            <span class="required-indicator">*</span>
                        </label>
                        <div>
                            <input autocomplete="autocomplete_off_xfr4!k1" name="${new Date().getTime()}"
                                   disabled="true" id="login-input" value="${currentUser.fullName}"
                                   class="form-control login-input">
                        </div>
                    </div>

                    <div class="col-xs-6 form-group">
                        <label>
                            <g:message code="user.password.label"/>
                            <span class="required-indicator">*</span>
                        </label>
                        <div>
                            <input autocomplete="autocomplete_off_xfr4!k" name="${new Date().getTime()}"
                                   id="password-input" class="form-control password-input" type="text">
                        </div>
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

            <div class="row form-group" style="margin-top: 9px;">
                <!-- Due Date -->
                <div class="col-xs-4">
                    <label><g:message code="app.label.action.item.due.date"/></label>
                    <div class="fuelux">
                        <div class="datepicker toolbarInline" id="dueDateDiv">
                            <div class="input-group">
                                <g:textField class="form-control fuelux" name="dueDate" id="dueDate"
                                             value="${renderShortFormattedDate(date: dueDate)}"/>
                                <g:render class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>



                <div class="col-xs-8">
                    <div class="row">
                        <div class="col-xs-4" style="margin-left: 5px;">
                            <label><g:message code="report.submission.submissionDate"/></label>
                        </div>
                        <div class="col-xs-3" style="margin-left: 5px;">
                            <label><g:message code="report.submission.submissionTime"/></label>
                        </div>
                        <div class="col-xs-4" style="margin-left: 6px;">
                            <label><g:message code="report.submission.timeZone"/></label>
                        </div>
                    </div>
                    <div class="row form-group reportSubDateDiv" style="margin-left: 10px;">
                        <div class="fuelux sub-sate-width">
                            <!-- Format submissionDate before passing to JS -->
                            <g:set var="formattedSubmissionDate" value="${submissionDate ? submissionDate.format('yyyy-MM-dd HH:mm:ss') : new Date().format('yyyy-MM-dd HH:mm:ss')}"/>
                            <input type="hidden" id="submissionDateNew" value="${formattedSubmissionDate}"/>
                            <g:render template="/configuration/dateWithTimeAndTimezoneTemplate" model="[submissionDate: submissionDate]"/>
                            <g:hiddenField name="scheduleDateJSON" id="scheduleDateJSON"/>
                            %{--<g:scheduleDateJSON value="${scheduleDateJSON ?: null}" name="scheduleDateJSON" id="scheduleDateJSON"/>--}%
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
                            <span class="btn btn-primary attachFile">
                                <g:message code="app.label.attach"/>
                                <input type="file" id="file_input" name="file" multiple=""
                                       accept="application/vnd.openxmlformats-officedocument.wordprocessingml.document, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/pdf, image/png, image/jpeg, text/plain, application/vnd.ms-outlook"
                                       style="display: none;">
                            </span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12  form-group" style="margin-top: 7px;">
                    <label><g:message code="report.submission.comment"/></label>
                    <span class="required-indicator">*</span>
                    <g:textArea name="comment" style="height: 80px;" id="caseSubmissionComments" maxlength="2000" class="form-control" value="${params?.comment}"/>
                    <div class="text-right" style="font-size: 14px; margin-top: 5px;">
                    <g:message code="app.label.max.characters" args="${['2000']}"/>
                    </div>
                </div>
                %{--<g:render template="/includes/justification/standardJustification" model="[justificationLabel: message(code: 'report.submission.comment'), justificationId: 'caseSubmissionComments', justificationJaId: 'caseSubmissionCommentsJa', maxlength: 2000]"/>--}%
            </div>
            <br>
            <!-- case history code-->
            <div><h2 class="modal-title" style="color: #000000;"><strong><g:message code="icsr.case.tracking..caseSubmissionHistory"/></strong></h2></div>
            <br>
            <div id="newSubmissionHistoryCase">
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
                    <table class="table table-striped" id="newSubmissionHistoryCaseTable" border="1px" width="100%">
                        <thead>
                            <tr>
                                <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.status"/></td>
                                <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.routed.date"/></td>
                                <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.routed.date.preferred.timezone"/></td>
                                <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.routedBy"/></td>
                                <td class="padding" style="font-weight: bold"><g:message code="app.label.icsr.case.history.comment"/></td>
                            </tr>
                        </thead>
                        <tbody id="newCaseSubmissionData">
                        </tbody>
                    </table>
                </div>
            </div>
            </div>
        </div>
    </form>
</div>

<!-- Modal footer -->
<div class="modal-footer custom-modal-footer">
    <button type="button" class="btn btn-primary new-submit-draft"><g:message code="default.button.confirm.label"/></button>
    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
            code="default.button.cancel.label"/></button>
</div>
