<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.SensitivityLabelEnum; com.rxlogix.enums.PageSizeEnum; net.sf.dynamicreports.report.constant.PageOrientation" %>
<g:javascript>
var emptyEmailConfigImage="${assetPath(src: "/image/icons/email.png")}";
var editedEmailConfigImage="${assetPath(src: "/image/icons/email-secure.png")}";
var listTemplateUrl="${createLink(controller: 'emailTemplate', action: 'axajList')}";
var saveTemplateUrl="${createLink(controller: 'emailTemplate', action: 'axajSave')}";
var deleteTemplateUrl="${createLink(controller: 'emailTemplate', action: 'axajDelete')}";

$(function() {
      $(document).on("click",".panel-heading", function(e){
          var $panelBody =  $(e.currentTarget).closest(".panel").find(".panel-collapse");
          if($panelBody) $panelBody.collapse('toggle');
    })
  var helpContent = $("#emailHelpTooltipContent").html().replaceAll("localizationHelpIcon glyphicon glyphicon-question-sign", "hidden")
  $("#emailBodyHelp").attr("data-content", helpContent)
  $("#emailBodyHelp").attr("data-toggle","popover" );// hack to avoid default initialisation of popover in jquery.core.js
  $("#emailBodyHelp").popover({html:true,template: '<div class="popover" style="max-width:550px; width:550px"><div class="arrow"></div><div class="popover-inner"><h3 class="popover-title"></h3><div class="popover-content"><p></p></div></div></div>' });
  if(location.href.indexOf("Series")>0)$(".noForCaseSeries").hide();
});

$('body').on('click', function (e) {
    if ($(e.target).data('toggle') !== 'popover'
        && $(e.target).parents('[data-toggle="popover"]').length === 0
        && $(e.target).parents('.popover.in').length === 0) {
        $('[data-toggle="popover"]').popover('hide');
    }
});
</g:javascript>
<g:if test="${emailConfiguration?.isDeleted}">
    <g:set var="emailConfiguration" value="${null}"/>
</g:if>
<div class="modal fade" id="emailConfigurationDistributionChannel" style="overflow-y: auto" data-keyboard="false" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.Compose" default="Compose"/></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-xs-11">
                        <div class="alert alert-danger" style="display:none">
                            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                            <g:message code="app.error.fill.all.required" />
                        </div>
                    </div>
                    <div class="col-xs-1">
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-11" style="padding-right: 27px;">
                        <g:hiddenField name="toValue"
                                       value="${emailConfiguration?.to}"/>
                        <g:select placeholder="${g.message(code: 'app.label.emailConfiguration.to', default: 'To')}"
                                  name="emailConfiguration.to" id="emailConfiguration.to"
                                  from="${[]}"
                                  data-value="${emailConfiguration?.to}"
                                  class="form-control emailUsers" multiple="true"
                                  data-options-url="${createLink(controller: 'email', action: 'allEmailsForCC', params: [emails: emailConfiguration?.to])}"/><i
                            class="fa fa-pencil-square-o copyPasteEmailButton"></i>
                    </div>
                    <div class="col-xs-1">
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-11" style="padding-right: 27px;">
                        <g:hiddenField name="ccValue"
                                       value="${emailConfiguration?.cc}"/>
                        <g:select placeholder="${g.message(code: 'app.label.emailConfiguration.cc', default: 'Cc')}"
                                  name="emailConfiguration.cc" id="emailConfiguration.cc"
                                  from="${[]}"
                                  data-value="${emailConfiguration?.cc}"
                                  class="form-control emailUsers" multiple="true"
                                  data-options-url="${createLink(controller: 'email', action: 'allEmailsForCC', params: [emails: emailConfiguration?.cc])}"/><i
                            class="fa fa-pencil-square-o copyPasteEmailButton"></i>
                    </div>
                    <div class="col-xs-1">
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-11">
                        <g:hiddenField name="subjectValue" id="subjectValue"
                                       value="${emailConfiguration?.subject}"/>
                        <g:textField name="subject" id="subject" class="form-control add-margin-bottom"
                                     value="${emailConfiguration?.subject}"
                                     placeholder="${g.message(code: 'app.label.emailConfiguration.subject', default: 'Subject')}"/>
                    </div>
                    <div class="col-xs-1">
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-11">
                        <g:hiddenField name="bodyValue"
                                       value="${ emailConfiguration?.body}"/>
                        <g:textArea name="emailConfiguration.body" id="body" class="form-control add-margin-bottom richEditor" value="${ emailConfiguration?.body}"
                                    placeholder="${g.message(code: 'app.label.emailConfiguration.message', default: 'Message')}"/>
                    </div>
                    <div class="col-xs-1 ">
                        <i id="emailBodyHelp" data-container="body" data-placement="left" data-content=''>
                            <span class="fa fa-question-circle" style="font-size: 25px; cursor: pointer; diisplay:none"></span>
                        </i>
                    </div>
                </div>
                <g:hiddenField name="deliveryReceipt" value="false"/>
                               %{--<div class="checkbox checkbox-primary">
                    <g:checkBox name="deliveryReceiptCheckbox" id="deliveryReceiptCheckbox"  value="${emailConfiguration?.deliveryReceipt}"/>
                    <label for="deliveryReceipt">
                        <g:hiddenField name="deliveryReceipt" value="${emailConfiguration?.noEmailOnNoData}"/>
                        <g:message code="app.label.icsr.profile.conf.delivery.receipt"
                                   default="Delivery Receipt"/>
                    </label>
                </div>--}%
            </div>


            <div class="modal-footer">
                <g:if test="${editable != false}">
                    <button type="button" class="btn pv-btn-grey" id="cancelEmailConfiguration" >
                        <g:message code="default.button.cancel.label"/>
                    </button>
                    <button type="button" class="btn btn-primary" id="saveDistributionEmailConfiguration">
                        <g:message code="default.button.save.label"/>
                    </button>
                    <button type="button" class="btn pv-btn-grey" id="resetDistributionEmailConfiguration">
                        <g:message code="default.button.reset.label"/>
                    </button>
                </g:if>
                <g:else>
                    <button type="button" class="btn pv-btn-grey" id="cancelEmailConfiguration" >
                        <g:message code="default.button.close.label"/>
                    </button>
                </g:else>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="emailTemplateList"  data-keyboard="false" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.temlateList"/></h4>
            </div>
            <div class="modal-body">
                <div style="overflow: auto; height: 200px" id="templateListContent">

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal" aria-label="Close" >
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="emailTemplateSave"  data-keyboard="false" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.temlateList"/></h4>
            </div>
            <div class="modal-body">
                <div class="row form-group">
                    <label for="emailTemplateName"><g:message code="app.label.name" /><span class="required-indicator">*</span></label>
                    <input id="emailTemplateName" name="emailTemplateName"  class="form-control"/>
                </div>

                <div class="row form-group">
                    <label for="emailTemplateDescription"><g:message code="app.label.description" /><span class="required-indicator">*</span></label>
                    <input id="emailTemplateDescription" name="emailTemplateDescription"  class="form-control"/>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="saveEmailTemplate">
                    <g:message code="default.button.save.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal" aria-label="Close" >
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>

<g:render template="/includes/widgets/errorTemplate" model="[errorModalId:'emailConfigurationErrorModal']"/>

<div id="emailHelpTooltipContent" style="display: none">
<g:message code="app.emailconfiguration.help.label"/>
<div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingOne">
            <h4 class="panel-title">
                <a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                    <g:message code="app.label.adhoc.report"/>
                </a>
            </h4>
        </div>
        <div id="collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="headingOne">
            <div class="panel-body">
                <ul>
                    <li>[reportName] - <g:message code="app.label.reportName"/></li>
                    <li>[version] - <g:message code="app.label.reportVersion"/></li>
                    <li>[description] - <g:message code="app.label.reportDescription"/></li>
                    <li>[productSelection] - <g:message code="app.label.productSelection"/></li>
                    <li>[studySelection] - <g:message code="app.label.studySelection"/></li>
                    <li>[eventSelection] - <g:message code="event.selections"/></li>
                    <li>[dateRangeType] - <g:message code="app.label.DateRangeType"/></li>
                    <li>[excludeNonValidCases] - <g:message code="reportCriteria.exclude.non.valid.cases"/></li>
                    <li>[excludeDeletedCases] - <g:message code="reportCriteria.exclude.deleted.cases"/></li>
                    <li>[includeMedicallyConfirmedCases] - <g:message code="reportCriteria.include.medically.confirm.cases"/></li>
                    <li>[excludeFollowUp] - <g:message code="reportCriteria.exclude.follow.up"/></li>
                    <li>[suspectProduct] - <g:message code="app.label.SuspectProduct"/></li>
                    <li>[limitPrimaryPath] - <g:message code="app.label.eventSelection.limit.primary.path"/></li>
                    <li>[includeLockedVersion] - <g:message code="app.label.includeLockedCasesOnly"/></li>
                    <li>[includeAllStudyDrugsCases] - <g:message code="app.label.includeAllStudyDrugsCases"/></li>
                    <li>[includeNonSignificantFollowUp] - <g:message code="reportCriteria.include.non.significant.followup.cases"/></li>
                    <li>[owner] - <g:message code="app.label.reportOwner"/></li>
                    <li>[lastRunDate] - <g:message code="app.label.runDateAndTime"/></li>
                    <li>[status] - <g:message code="app.label.executionStatus"/></li>
                    <li>[workflowState] - <g:message code="app.label.workflow.status"/></li>
                    <li>[sections] - <g:message code="app.label.reportSections"/></li>
                    <li>[referReport] - <g:message code="app.label.report.link"/></li>
                </ul>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingTwo">
            <h4 class="panel-title">
                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
                    <g:message code="app.periodicReports.generated.tittle"/>
                </a>
            </h4>
        </div>
        <div id="collapseTwo" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingTwo">
            <div class="panel-body">
                <ul>
                    <li>[reportName] - <g:message code="app.label.reportName"/></li>
                    <li>[version] - <g:message code="app.label.reportVersion"/></li>
                    <li>[description] - <g:message code="app.label.reportDescription"/></li>
                    <li>[periodicReportType] - <g:message code="app.label.periodicReportType"/></li>
                    <li>[productSelection] - <g:message code="app.label.productSelection"/></li>
                    <li>[studySelection] - <g:message code="app.label.studySelection"/></li>
                    <li>[executedGlobalQuery] - <g:message code="app.label.globalQueryName"/></li>
                    <li>[globalDateRange] - <g:message code="app.label.globalDateRangInformation"/></li>
                    <li>[dateRangeType] - <g:message code="app.label.DateRangeType"/></li>
                    <li>[excludeNonValidCases] - <g:message code="reportCriteria.exclude.non.valid.cases"/></li>
                    <li>[excludeDeletedCases] - <g:message code="reportCriteria.exclude.deleted.cases"/></li>
                    <li>[excludeFollowUp] - <g:message code="reportCriteria.exclude.follow.up"/></li>
                    <li>[suspectProduct] - <g:message code="app.label.SuspectProduct"/></li>
                    <li>[includePreviousMissingCases] - <g:message code="reportCriteria.include.previous.missing.cases"/></li>
                    <li>[includeOpenCasesInDraft] - <g:message code="app.label.includeOpenCasesInDraft"/></li>
                    <li>[includeAllStudyDrugsCases] - <g:message code="app.label.includeAllStudyDrugsCases"/></li>
                    <li>[includeNonSignificantFollowUp] - <g:message code="reportCriteria.include.non.significant.followup.cases"/></li>
                    <li>[owner] - <g:message code="app.label.reportOwner"/></li>
                    <li>[lastRunDate] - <g:message code="app.label.runDateAndTime"/></li>
                    <li>[status] - <g:message code="app.label.executionStatus"/></li>
                    <li>[workflowState] - <g:message code="app.label.workflow.status"/></li>
                    <li>[sections] - <g:message code="app.label.reportSections"/></li>
                    <li>[referReport] - <g:message code="app.label.report.link"/></li>
                </ul>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingThree">
            <h4 class="panel-title">
                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
                    <g:message code="app.icsrReports.generated.tittle"/>
                </a>
            </h4>
        </div>
        <div id="collapseThree" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingThree">
            <div class="panel-body">
                <ul>
                    <li>[reportName] - <g:message code="app.label.reportName"/></li>
                    <li>[recipientOrganizationName] - <g:message code="app.label.icsr.profile.conf.recipientOrganization"/></li>
                    <li>[senderOrganizationName] - <g:message code="app.label.icsr.profile.conf.senderOrganization"/></li>
                    <li>[owner] - <g:message code="app.label.scheduledBy"/></li>
                    <li>[icsrState] - <g:message code="app.label.icsr.profile.conf.case.state"/></li>
                    <li>[caseCountry] - <g:message code="app.label.icsr.profile.conf.case.country"/></li>
                    <li>[caseSource] - <g:message code="app.label.icsr.profile.conf.case.source"/></li>
                    <li>[caseReceiptDate] - <g:message code="app.label.icsr.profile.conf.case.receiptDate"/></li>
                    <li>[primarySuspectProduct] - <g:message code="app.label.icsr.profile.conf.case.primarySuspectProduct"/></li>
                    <li>[primaryProductTradeName] - <g:message code="app.label.icsr.profile.conf.case.primaryProductTradeName"/></li>
                    <li>[primaryEvent] - <g:message code="app.label.icsr.profile.conf.case.primaryEvent"/></li>
                    <li>[studyNumber] - <g:message code="app.label.icsr.profile.conf.case.studyNumber"/></li>
                    <li>[fupType] - <g:message code="app.label.icsr.profile.conf.case.fupType"/></li>
                    <li>[fupNo] - <g:message code="app.label.icsr.profile.conf.case.fupNo"/></li>
                    <li>[awareDate] - <g:message code="app.label.icsr.profile.conf.case.awareDate"/></li>
                    <li>[dueDate] - <g:message code="app.label.icsr.profile.conf.case.dueDate"/></li>
                    <li>[dueInDays] - <g:message code="app.label.icsr.profile.conf.case.dueInDays"/></li>
                    <li>[approvalNumber] - <g:message code="app.label.icsr.profile.conf.case.approvalNumber"/></li>
                    <li>[generationDate] - <g:message code="app.label.icsr.profile.conf.case.generationDate"/></li>
                    <li>[messageType] - <g:message code="app.label.icsr.profile.conf.case.messageType"/></li>
                    <li>[messageForm] - <g:message code="app.label.icsr.profile.conf.case.messageForm"/></li>
                    <li>[schedulingCriteria] - <g:message code="app.label.icsr.profile.conf.case.schedulingCriteria"/></li>
                    <li>[recipientType] - <g:message code="app.label.icsr.profile.conf.case.recepientType"/></li>
                </ul>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingFour">
            <h4 class="panel-title">
                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseFour" aria-expanded="false" aria-controls="collapseFour">
                    <g:message code="app.caseSeries.label"/>
                </a>
            </h4>
        </div>
        <div id="collapseFour" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingFour">
            <div class="panel-body">
                <ul>
                    <li>[seriesName] - <g:message code="app.label.reportName"/></li>
                    <li>[description] - <g:message code="app.label.reportDescription"/></li>
                    <li>[suspectProduct] - <g:message code="app.label.SuspectProduct"/></li>
                    <li>[productSelection] - <g:message code="app.productDictionary.label"/></li>
                    <li>[studySelection] - <g:message code="app.studyDictionary.label"/></li>
                    <li>[dateRangeType] - <g:message code="app.label.DateRangeType"/></li>
                    <li>[evaluateDateAs] - <g:message code="evaluate.on.label"/></li>
                    <li>[dateRange] - <g:message code="app.label.DateRange"/></li>
                    <li>[executedGlobalQuery] - <g:message code="app.label.queryName"/></li>
                    <li>[owner] - <g:message code="app.label.scheduledBy"/></li>
                    <li>[excludeFollowUp] - <g:message code="reportCriteria.exclude.follow.up"/></li>
                    <li>[includeLockedVersion] - <g:message code="reportCriteria.include.locked.versions.only"/></li>
                    <li>[includeAllStudyDrugsCases] - <g:message code="reportCriteria.include.comparators.cases"/></li>
                    <li>[excludeNonValidCases] - <g:message code="reportCriteria.exclude.non.valid.cases"/></li>
                    <li>[excludeDeletedCases] - <g:message code="reportCriteria.exclude.deleted.cases"/></li>
                    <li>[includeNonSignificantFollowUp] - <g:message code="reportCriteria.include.non.significant.followup.cases"/></li>
                    <li>[referReport] - <g:message code="app.label.report.link"/></li>
                </ul>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingFive">
            <h4 class="panel-title">
                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseFive" aria-expanded="false" aria-controls="collapseFive">
                    <g:message code="app.label.icsr.partner.profile.menuItem"/>
                </a>
            </h4>
        </div>
        <div id="collapseFive" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingFive">
            <div class="panel-body">
                <ul>
                    <li>[reportName] - <g:message code="app.label.reportName"/></li>
                    <li>[recipientOrganizationName] - <g:message code="app.label.icsr.profile.conf.recipientOrganization"/></li>
                    <li>[senderOrganizationName] - <g:message code="app.label.icsr.profile.conf.senderOrganization"/></li>
                    <li>[autoTransmit] - <g:message code="app.label.icsr.profile.conf.autoTransmit"/></li>
                    <li>[owner] - <g:message code="app.label.scheduledBy"/></li>
                    <li>[safetyReportId] - <g:message code="app.label.safetyReportId"/></li>
                    <li>[wwid] - <g:message code="app.label.wwid"/></li>
                    <li>[caseNumber] - <g:message code="app.label.icsr.profile.conf.case.number"/></li>
                    <li>[versionNumber] - <g:message code="app.label.icsr.profile.conf.version.number"/></li>
                    <li>[icsrState] - <g:message code="app.label.icsr.profile.conf.case.state"/></li>
                    <li>[caseCountry] - <g:message code="app.label.icsr.profile.conf.case.country"/></li>
                    <li>[caseSource] - <g:message code="app.label.icsr.profile.conf.case.source"/></li>
                    <li>[caseReceiptDate] - <g:message code="app.label.icsr.profile.conf.case.receiptDate"/></li>
                    <li>[primarySuspectProduct] - <g:message code="app.label.icsr.profile.conf.case.primarySuspectProduct"/></li>
                    <li>[primaryProductTradeName] - <g:message code="app.label.icsr.profile.conf.case.primaryProductTradeName"/></li>
                    <li>[primaryEvent] - <g:message code="app.label.icsr.profile.conf.case.primaryEvent"/></li>
                    <li>[studyNumber] - <g:message code="app.label.icsr.profile.conf.case.studyNumber"/></li>
                    <li>[fupType] - <g:message code="app.label.icsr.profile.conf.case.fupType"/></li>
                    <li>[fupNo] - <g:message code="app.label.icsr.profile.conf.case.fupNo"/></li>
                    <li>[awareDate] - <g:message code="app.label.icsr.profile.conf.case.awareDate"/></li>
                    <li>[dueDate] - <g:message code="app.label.icsr.profile.conf.case.dueDate"/></li>
                    <li>[dueInDays] - <g:message code="app.label.icsr.profile.conf.case.dueInDays"/></li>
                    <li>[approvalNumber] - <g:message code="app.label.icsr.profile.conf.case.approvalNumber"/></li>
                    <li>[approvalType] - <g:message code="app.label.icsr.profile.conf.case.approvalType"/></li>
                    <li>[generationDate] - <g:message code="app.label.icsr.profile.conf.case.generationDate"/></li>
                    <li>[messageType] - <g:message code="app.label.icsr.profile.conf.case.messageType"/></li>
                    <li>[messageForm] - <g:message code="app.label.icsr.profile.conf.case.messageForm"/></li>
                    <li>[schedulingCriteria] - <g:message code="app.label.icsr.profile.conf.case.schedulingCriteria"/></li>
                    <li>[recipientType] - <g:message code="app.label.icsr.profile.conf.case.recepientType"/></li>
                </ul>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingSix">
            <h4 class="panel-title">
                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseSix" aria-expanded="false" aria-controls="collapseSix">
                    <g:message code="example"/>
                </a>
            </h4>
        </div>
        <div id="collapseSix" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingSix">
            <div class="panel-body">
                Hi, this report includes cases for [productSelection] for the date range [globalDateRange] and was executed on [lastRunDate].
            </div>
        </div>
    </div>
</div>
</div>
<asset:javascript src="vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="app/emailTemplateEditor.js"/>
<g:render template="/includes/widgets/confirmation"/>
