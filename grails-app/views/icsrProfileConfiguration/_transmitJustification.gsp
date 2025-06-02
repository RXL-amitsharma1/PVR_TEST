<%@ page import="grails.util.Holders" %>
<div class="modal fade transmitJustification" data-backdrop="static" style="margin-left: 5px" id="transmitJustification"
     tabindex="-1" role="dialog">
    <div class="modal-dialog vertical-align-center" role="document">
        <div class="modal-content" style="width: 85%; margin-left:7%;">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.icsr.view.case.transmit"/></h4>
            </div>

            <div class="modal-body">
                <div>
                    <div class="alert alert-danger hide">
                        <button type="button" class="close transmitErrorMessageDivClose">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <span id="transmitErrorMessage"></span>
                    </div>

                    <g:form autocomplete="off" name="justificationForm" id="justificationForm">
                        <div id="needApprovalDiv"
                             class="${Holders.config.getProperty('icsr.case.submission.user.revalidate', Boolean) ? '' : 'hidden'}">

                            <label>
                                <g:message code="app.label.workflow.rule.needApproval"/>
                            </label>
                            <div>
                                <g:message code="app.label.workflow.rule.needApproval.transmission"/>
                            </div>
                            <br>

                            <div>
                                <label>
                                    <g:message code="user.username.label"/>
                                    <span class="required-indicator">*</span>
                                </label>
                            </div>

                            <div>
                                <g:set var="userService" bean="userService"/>
                                <input autocomplete="autocomplete_off_xfr4!k1" name="${new Date().getTime()}"
                                       disabled="true" id="login-input" value="${userService.currentUser.fullName}"
                                       class="form-control login-input">
                            </div>

                            <div>
                                <label>
                                    <g:message code="user.password.label"/>
                                    <span class="required-indicator">*</span>
                                </label>
                            </div>

                            <div id="password-input-div">
                                <input autocomplete="autocomplete_off_xfr4!k" name="${new Date().getTime()}"
                                       id="password-input" class="form-control password-input" type="text">
                            </div>

                            <div>
                                <label><g:message code="report.submission.approvalDate"/><span class="required-indicator">*</span></label>

                                <div class="fuelux">
                                    <g:set var="userService" bean="userService"/>
                                    <g:set var="timeZone" value="${com.rxlogix.enums.TimeZoneEnum.values().find{it.timezoneId == userService.getUser().preference.timeZone}}"/>
                                    <g:hiddenField name="scheduleDateJSON" id="scheduleDateJSON"/>
                                    <g:hiddenField name="schedulerTime"
                                                   value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(userService.getUser(), timeZone.getTimezoneId())}"/>
                                    <input type="text" id="approvalDate" name="approvalDate"  disabled="true" class="form-control"
                                           value="${com.rxlogix.util.RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(userService.getUser(), "dd-MMM-yyyy hh:mm a", timeZone.timezoneId)} (${timeZone.timezoneId})">
                                </div>
                            </div>
                        </div>

                        <div class="description-wrapper">
                            <label>
                                <g:message code="report.submission.comment"/>
                            </label>
                            <g:textArea rows="5" cols="3" name="comments" id="transmissionComments" maxlength="255"
                                        style="height: 110px;" value="" class="form-control "/>
                            <g:message code="icsr.commet.maxSize.exceeded"/>
                        </div>
                    </g:form>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="transmitButton" class="btn btn-success confirm-paste">
                    <g:message code="default.button.confirm.label"/>
                </button>
                <button type="button" class="btn btn-default cancel" data-dismiss="modal">
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>