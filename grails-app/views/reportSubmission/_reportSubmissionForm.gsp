<%@ page import="com.rxlogix.enums.ReportFormatEnum; com.rxlogix.config.ReportSubmission; grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.Constants; com.rxlogix.util.DateUtil; com.rxlogix.enums.ReportSubmissionStatusEnum" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<asset:javascript src="app/scheduler.js"/>
<asset:javascript src="/app/emailModal.js"/>
<style>
.sub-sate-width > .row{width:auto!important;}
</style>
<!-- Modal header -->
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <span class="modalHeader">Submit ${executedPeriodicReportConfiguration.reportName}</span>
</div>

<!-- Modal body -->
<div class="modal-body">
    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="error-panel" style="display: none">
    </div>
    <div class="alert alert-danger hide">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <span><g:message code="app.error.choose.email.required"/></span>
    </div>
    <form name="reportSubmissionForm" action="#">
        <div>
            <div class="row">
                <div class="col-xs-4 form-group">
                    <label><g:message code="report.submission.status"/></label>

                    <g:hiddenField name="exPerConfId"
                                   value="${executedPeriodicReportConfiguration.id}"/>
                    <g:select name="reportSubmissionStatus" from="${ViewHelper.getReportSubmissionStatusEnumWoPendingI18n()}" optionKey="name" optionValue="display" class="form-control"/>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12  form-group">
                    <label><g:message code="report.submission.comment"/><span class="required-indicator">*</span></label>
                    <g:textArea name="comment" style="height: 100px;" maxlength="${ReportSubmission.constrainedProperties.comment.maxSize}" class="form-control" value="${params?.comment}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6  form-group destinations">
                    <label><g:message code="report.submission.reportingDestinations"/></label>
                    <g:hiddenField name="primaryReportingDestination"
                                   value="${primaryReportingDestination}"/>
                    <g:select name="reportingDestinations"
                              from="${[]}"
                              data-value="${reportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                              value="${reportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                              class="form-control" multiple="multiple"/>
                    <label><g:message code="app.label.document"/></label>

                    <g:if test="${executedPeriodicReportConfiguration.isPublisherReport}">
                        <select name="publisherDocument" id="publisherDocument" class="form-control">
                            <option value="" data-destination=""></option>
                            <g:each var="row" in="${executedPeriodicReportConfiguration.publisherReports.findAll { it.published }}">
                                <option value="${row.id}" data-destination="${row.destination}">${row.name + (row.destination ? (" (" + row.destination + ")") : "")}</option>
                            </g:each>
                        </select>
                    </g:if>
                    <g:else>
                    <div class="input-group" style="width: 300px;">
                        <input type="text" class="form-control" id="file_name" readonly>
                        <label class="input-group-btn">
                            <span class="btn btn-primary">
                                <g:message code="app.label.attach"/>
                                <input type="file" id="file_input" name="file" multiple  accept="application/vnd.openxmlformats-officedocument.wordprocessingml.document" style="display: none;">
                            </span>
                        </label>
                    </div>
                    </g:else>
                </div>
                <div class="col-xs-6  form-group">
                    <label><g:message code="report.submission.submissionDate"/></label>

                    <div class="fuelux sub-sate-width" style="margin-left: 5px;">
                                <g:render template="/configuration/dateWithTimezoneTemplate"/>
                                <g:hiddenField name="scheduleDateJSON" id="scheduleDateJSON"/>
                                <g:hiddenField name="schedulerTime"
                                               value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(currentUser)}"/>
                                <input type="hidden" id="timezoneFromServer" name="timezone"
                                       value="${DateUtil.getTimezone(currentUser)}"/>

                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-4 form-group">
                    <label><g:message code="app.label.action.item.due.date"/></label>

                    <div class="fuelux" >
                        <div>
                            <div class="datepicker toolbarInline" id="addDueDateDiv">
                                <div class="input-group">
                                    <g:textField class="form-control fuelux" name="dueDate"
                                                 value="${executedPeriodicReportConfiguration.dueDate ?
                                                         formatDate(date: executedPeriodicReportConfiguration.dueDate,format: DateUtil.getShortDateFormatForLocale(userService.user?.preference?.locale)) : ""}"/>
                                    <g:render class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12 form-group">
                    <label><g:message code="user.email.label" /></label> <g:message code="app.label.generatedIn" default="Generated Report in" />

                    <g:each in="${ReportFormatEnum.getEmailSubmitOptions()}">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <g:checkBox id="s_${it.key}" class="emailOption" name="attachmentFormats" value="${it}"/>
                            <label for="s_${it.key}">${message(code: it.i18nKey)} </label>
                        </div>
                    </g:each>
                    <div class="checkbox checkbox-primary checkbox-inline submittingDocument" style="margin-left: 30px !important;" >
                        <g:checkBox id="submittingDocument" class="emailOption" name="submittingDocument"/>
                        <label for="submittingDocument">
                            <g:message code="app.submitting.document" default="Submitting Document" />
                        </label>
                    </div>

                    <div class="row m-b-10">
                        <div class="col-xs-10" style="padding-right: 27px;">
                            <g:select id="emailUsersSubmit"
                                      name="emailToUsers"
                                      from="${[]}"
                                      data-value=""
                                      class="form-control emailUsers" multiple="true"
                                      data-options-url="${createLink(controller: 'email', action: 'allEmails')}"/><i
                                class="fa fa-pencil-square-o copyPasteEmailButton paste-icon" style="margin-right: -20px"></i>
                        </div>
                        <span class="showEmailConfiguration" style="display: none; cursor: pointer; margin-left: 25px" data-toggle="modal"
                              data-target="#emailConfiguration"><asset:image
                                src="icons/email.png" title="${message(code: 'default.button.addEmailConfiguration.label')}"/></span>
                    </div>



                    <div id="formatError" hidden="hidden">
                        <div class="row">
                            <div class="col-xs-12" style="color: #ff0000">
                                <g:message code="select.at.least.one.attachment.format" />!
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <g:if test="${Holders.config.submission.rod.processing}">
            <div class="row late" style="display: none">
                <div class="col-xs-3  form-group">
                    <label style="margin-top: 5px;"><g:message code="app.label.view.case.late"/></label>
                    <g:select name="late" from="${Holders.config.submissions.late.name}"  class="form-control select2-box"
                              />
                </div>

                <div class="col-md-9">
                    <div class="reasonResponsibleContainer">
                        <div id="selectTemplates" style="display: none">
                            <g:select name="responsible" from="${Holders.config.submission.responsibleparty}" value=""  class="form-control responsible" style="margin-bottom: 5px"/>
                            <g:select name="reason" from="${Holders.config.submission.reason}" value="" class="form-control reason" style="margin-bottom: 5px"/>
                        </div>
                        <div class="row">
                            <div class="col-md-1"><label style="font-size: 17px;margin-left: 20px; cursor: pointer"><span title="Add Reason" class="fa fa-plus addReason"></span></label></div>
                            <div class="col-md-5"><label><g:message code="app.label.view.case.responsible.party"/></label></div>
                            <div class="col-md-6"><label><g:message code="app.label.view.case.reason"/></label></div>
                        </div>
                    </div>
                </div>
            </div>
            </g:if>
            <g:if test="${submissions}">
                <div class="row">
                    <div class="col-xs-12  form-group">
                        <label><g:message code="report.submission.history"/></label>

                        <div>
                            <table width="100%" border="1">
                                <tr>
                                    <th class="text-center"><g:message
                                            code="app.reportSubmission.reportingDestination.label"/></th>
                                    <th class="text-center"><g:message code="app.reportSubmission.status.label"/></th>
                                    <th class="text-center"><g:message code="app.label.reportSubmission.submissionDate"/></th>
                                    <th class="text-center"><g:message code="app.label.reportSubmission.dueDate"/></th>
                                    <th class="text-center"><g:message code="app.report.submission.comment.label"/></th>
                                </tr>
                                <g:each in="${submissions}" var="reportSubmission">
                                    <tr>
                                        <td class="text-center"
                                            style="background-color: ${primaryReportingDestination == reportSubmission.reportingDestination ? '#a5dd5d' : 'none'}">${reportSubmission.reportingDestination}</td>
                                        <td class="text-center"><g:message
                                                message="${reportSubmission.reportSubmissionStatus}"/></td>
                                        <td class="text-center"><g:renderLongFormattedDate
                                                date="${reportSubmission.submissionDate}" timeZone="${g.getCurrentUserTimezone()}"/></td>
                                        <td class="text-center"><g:renderShortFormattedDate
                                                date="${reportSubmission.dueDate}"/></td>
                                        <td class="text-center">${reportSubmission.comment}</td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
                    </div>
                </div>
            </g:if>

        </div>
    </form>

</div>

<!-- Modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-primary submit-draft"><g:message code="app.label.submit"/></button>
    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
            code="default.button.cancel.label"/></button>
</div>

<script>
    var isRodProcessingEnabled = ${Holders.config.submission.rod.processing};
    if(isRodProcessingEnabled){
        addReason();
        $("#late").select2();
    }
    initSelect2ForEmailUsers("form[name=reportSubmissionForm] .emailUsers");
    $('#emailToModal').trigger('submissionModalOpen',["${executedPeriodicReportConfiguration.id}"]);
    $("#copyAndPasteEmailModal").css("z-index",1051);
    $("#emailConfiguration").css("z-index",1051);
    $(".copyPasteEmailButton").css("margin-top","-7px");
</script>