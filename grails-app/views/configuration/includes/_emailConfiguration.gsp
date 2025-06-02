<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.SensitivityLabelEnum; com.rxlogix.enums.PageSizeEnum; net.sf.dynamicreports.report.constant.PageOrientation; com.rxlogix.config.EmailTemplate" %>
<g:javascript>
var emptyEmailConfigImage="${assetPath(src: "/image/icons/email.png")}";
var editedEmailConfigImage="${assetPath(src: "/image/icons/email-secure.png")}";
var DEFAULT_EMAIL_OPTIONS={
    PAGE_ORIENTATION:'${PageOrientation.PORTRAIT}',
    PAPER_SIZE:'${PageSizeEnum.LETTER}',
    SENSITIVITY_LABEL:'${SensitivityLabelEnum.SENSITIVE}',
    SHOW_PAGE_NUMBERING:true,
    EXCLUDE_CRITERIA_SHEET:false,
    EXCLUDE_APPENDIX:false,
    EXCLUDE_COMMENTS:false,
    EXCLUDE_LEGEND:false,
    SHOW_COMPANY_LOGO:true

};
var listTemplateUrl="${createLink(controller: 'emailTemplate', action: 'axajList')}";
var saveTemplateUrl="${createLink(controller: 'emailTemplate', action: 'axajSave')}";
var deleteTemplateUrl="${createLink(controller: 'emailTemplate', action: 'axajDelete')}";
function toggleAdvancedEmailOptions(){
    $('#advancedEmailOptions').toggle();
    var caret=$('#advancedEmailOptionsLink').find('span');
    if(caret.hasClass("fa-caret-right")){
        caret.removeClass("fa-caret-right");
        caret.addClass("fa-caret-down");
    }else{
        caret.removeClass("fa-caret-down");
        caret.addClass("fa-caret-right");
    }
}

$(function() {
   $(document).on("click",".panel-heading", function(e){
          var $panelBody =  $(e.currentTarget).closest(".panel").find(".panel-collapse");
          if($panelBody) $panelBody.collapse('toggle');
    })
  var helpContent = $("#emailHelpTooltipContent").html().replaceAll("localizationHelpIcon glyphicon glyphicon-question-sign", "hidden")
  $("#emailBodyHelp").attr("data-content", helpContent);
  $("#emailBodyHelp").attr("data-toggle","popover" );// hack to avoid default initialisation of popover in jquery.core.js
  $("#emailBodyHelp").popover({html:true,template: '<div class="popover" style="max-width:550px; width:550px"><div class="arrow"></div><div class="popover-inner"><h3 class="popover-title"></h3><div class="popover-content"><p></p></div></div></div>' });
  if(location.href.indexOf("Series")>0)$(".noForCaseSeries").hide();
  $("[data-evt-clk]").on("click", function() {
    const eventData = JSON.parse($(this).attr("data-evt-clk"));
    const methodName = eventData.method;
    const params = eventData.params;
    if(methodName == "toggleAdvancedEmailOptions") {
        toggleAdvancedEmailOptions();
    }
  });
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
<div class="modal fade" id="emailConfiguration" style="overflow-y: auto" data-keyboard="false" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#emailConfiguration"]}' aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.Compose" default="Compose"/></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-xs-11">
                        <div class="alert alert-danger" style="display:none">
                            <a href="#" class="close"  id="closeError" aria-label="close">&times;</a>
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
                                  data-width="100%"
                                  class="form-control emailUsers" multiple="true"
                                  data-options-url="${createLink(controller: 'email', action: 'allEmailsForCC', params: [emails: emailConfiguration?.to])}"/>
                    </div>
                    <div class="col-xs-1 fa fa-pencil-square-o copyPasteEmailButton l-0" style="position: inherit;"></div>
                </div>

                <div class="row m-t-10">
                    <div class="col-xs-11" style="padding-right: 27px;">
                        <g:hiddenField name="ccValue"
                                       value="${emailConfiguration?.cc}"/>
                        <g:select placeholder="${g.message(code: 'app.label.emailConfiguration.cc', default: 'Cc')}"
                                  name="emailConfiguration.cc" id="emailConfiguration.cc"
                                  from="${[]}"
                                  data-value="${emailConfiguration?.cc}"
                                  data-width="100%"
                                  class="form-control emailUsers" multiple="true"
                                  data-options-url="${createLink(controller: 'email', action: 'allEmailsForCC', params: [emails: emailConfiguration?.cc])}"/>
                    </div>
                    <div class="col-xs-1 fa fa-pencil-square-o copyPasteEmailButton l-0" style="position: inherit;"></div>
                </div>
                <div class="row m-t-10">
                    <div class="col-xs-11">
                        <g:hiddenField name="subjectValue" id="subjectValue"
                                       value="${emailConfiguration?.subject}"/>
                        <g:textField name="emailConfiguration.subject" class="form-control add-margin-bottom" maxlength="2000"
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
                        <g:textArea name="emailConfiguration.body" class="form-control add-margin-bottom richEditor" value="${ emailConfiguration?.body}"
                                    placeholder="${g.message(code: 'app.label.emailConfiguration.message', default: 'Message')}"/>
                    </div>
                    <div class="col-xs-1 ">
                        <i id="emailBodyHelp" data-container="body" data-placement="left" data-content=''><span class="fa fa-question-circle" style="font-size: 25px; cursor: pointer; diisplay:none"></span></i>
                    </div>
                </div>

                <div class="checkbox checkbox-primary mh-auto m-t-10">
                    <g:checkBox name="emailConfiguration.noEmailOnNoData" id="noEmailOnNoData"  value="${emailConfiguration?.noEmailOnNoData}"/>
                    <label for="noEmailOnNoData">
                        <g:hiddenField name="noEmailOnNoDataValue" value="${emailConfiguration?.noEmailOnNoData}"/>
                        <g:message code="app.label.emailConfiguration.do.not.send.email"
                                   default="Do not send email when report returns no data"/>
                    </label>
                </div>
                <div id="advancedEmailOptionsLink" class="m-t-10"><a href="#" data-evt-clk='{"method": "toggleAdvancedEmailOptions", "params": []}'><span class="fa fa-lg fa-caret-right"></span> <g:message code="app.label.advancedOptions"/></a></div>
                <div id="advancedEmailOptions" style="display: none;padding-top: 25px" class="m-l-15">

                    <div class="row">
                        <div class="col-md-5 m-r-30">
                            <label class="dialogBox"><g:message code="page.orientation.pdf.and.word" /></label>
                            <div class="form-group">
                                <select name="emailConfiguration.pageOrientation"
                                        id="emailConfiguration.pageOrientation" class="form-control">
                                    <option ${(emailConfiguration?.pageOrientation == PageOrientation.PORTRAIT) ? "selected" : ""}
                                            value="${PageOrientation.PORTRAIT}"><g:message
                                            code="app.pageOrientation.PORTRAIT"/></option>
                                    <option ${(emailConfiguration?.pageOrientation == PageOrientation.LANDSCAPE) ? "selected" : ""}
                                            value="${PageOrientation.LANDSCAPE}"><g:message
                                            code="app.pageOrientation.LANDSCAPE"/></option>
                                </select>
                                <g:hiddenField name="pageOrientationValue" value="${emailConfiguration?.pageOrientation?:PageOrientation.PORTRAIT}"/>
                            </div>

                            <label class="dialogBox"><g:message code="paper.size.pdf.and.word" /></label>
                            <div class="form-group">
                                <g:select name="emailConfiguration.paperSize"
                                          from="${ViewHelper.getPageSizeEnum()}"
                                          optionKey="name"
                                          optionValue="display"
                                          value="${emailConfiguration?.paperSize ?: PageSizeEnum.LETTER }"
                                          class="form-control"/>
                                <g:hiddenField name="paperSizeValue" value="${emailConfiguration?.paperSize ?: PageSizeEnum.LETTER }"/>
                            </div>

                            <label class="dialogBox"><g:message code="sensitivity.label" /></label>
                            <div class="form-group">
                                <g:select name="emailConfiguration.sensitivityLabel"
                                          from="${ViewHelper.getSensitivityLabelEnum()}"
                                          optionKey="name"
                                          optionValue="display"
                                          value="${emailConfiguration?.sensitivityLabel ?: SensitivityLabelEnum.SENSITIVE }"
                                          class="form-control"/>
                                <g:hiddenField name="sensitivityLabelValue" value="${emailConfiguration?.sensitivityLabel ?: SensitivityLabelEnum.SENSITIVE }"/>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <label class="dialogBox"><g:message code="options" /></label>
                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="emailConfiguration.showPageNumbering" checked="${emailConfiguration? emailConfiguration.showPageNumbering : "true" }"/>
                                    <label for="emailConfiguration.showPageNumbering">
                                        <g:message code="show.page.numbering" />
                                    </label>
                                    <g:hiddenField name="showPageNumberingValue" value="${emailConfiguration? emailConfiguration.showPageNumbering : "true" }"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="emailConfiguration.showCompanyLogo" checked="${emailConfiguration? emailConfiguration.showCompanyLogo : "true" }"/>
                                    <label for="emailConfiguration.showCompanyLogo">
                                        <g:message code="show.company.logo" />
                                    </label>
                                    <g:hiddenField name="showCompanyLogoValue" value="${emailConfiguration?.showCompanyLogo ?: "true"  }"/>
                                </div>
                            </div>

                            <div class="form-group noForCaseSeries">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="emailConfiguration.excludeCriteriaSheet" checked="${emailConfiguration?.excludeCriteriaSheet ?: "false" }"/>
                                    <label for="emailConfiguration.excludeCriteriaSheet">
                                        <g:message code="exclude.criteria.sheet" />
                                    </label>
                                    <g:hiddenField name="excludeCriteriaSheetValue" value="${emailConfiguration?.excludeCriteriaSheet ?: "false"  }"/>
                                </div>
                            </div>

                            <div class="form-group noForCaseSeries">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="emailConfiguration.excludeAppendix" checked="${emailConfiguration?.excludeAppendix ?: "false" }"/>
                                    <label for="emailConfiguration.excludeAppendix">
                                        <g:message code="exclude.appendix" />
                                    </label>
                                    <g:hiddenField name="excludeAppendixValue" value="${emailConfiguration?.excludeAppendix ?: "false"  }"/>
                                </div>
                            </div>

                            <div class="form-group noForCaseSeries">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="emailConfiguration.excludeComments" checked="${emailConfiguration?.excludeComments ?: "false" }"/>
                                    <label for="emailConfiguration.excludeComments">
                                        <g:message code="exclude.comments" />
                                    </label>
                                    <g:hiddenField name="excludeCommentsValue" value="${emailConfiguration?.excludeComments ?: "false"  }"/>
                                </div>
                            </div>

                            <div class="form-group noForCaseSeries">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="emailConfiguration.excludeLegend" checked="${emailConfiguration?.excludeLegend ?: "false" }"/>
                                    <label for="emailConfiguration.excludeLegend">
                                        <g:message code="exclude.legend" />
                                    </label>
                                    <g:hiddenField name="excludeLegendValue" value="${emailConfiguration?.excludeLegend ?: "false"  }"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" id="cancelEmailConfiguration">
                    <g:message code="default.button.cancel.label"/>
                </button>
                <button type="button" class="btn btn-primary" id="saveEmailConfiguration">
                    <g:message code="default.button.save.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" id="resetEmailConfiguration">
                    <g:message code="default.button.reset.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="emailTemplateList"  data-keyboard="false" data-backdrop="static" tabindex="-1" role="dialog" STYLE="z-index: 1053">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#emailTemplateList"]}' aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.temlateList"/></h4>
            </div>
            <div class="modal-body">
                <g:hiddenField name="isUserSpecificTemplate"/>
                <g:textField name="emailSearch" class="form-control add-margin-bottom" placeholder="${g.message(code: 'default.button.search.label', default: 'Search')}"/>
                <div style="overflow: auto; height: 200px" id="templateListContent">

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "modalHide", "params": ["#emailTemplateList"]}'  aria-label="Close" >
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
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#emailTemplateSave"]}' aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.temlateList"/></h4>
            </div>
            <div class="modal-body">
                <div class="row form-group">
                        <label for="emailTemplateName"><g:message code="app.label.name" /><span class="required-indicator">*</span></label>
                        <input id="emailTemplateName" name="emailTemplateName" maxlength="${EmailTemplate.constrainedProperties.name.maxSize}" class="form-control"/>
                </div>

                <div class="row form-group">
                        <label for="emailTemplateDescription"><g:message code="app.label.description" /><span class="required-indicator">*</span></label>
                        <input id="emailTemplateDescription" name="emailTemplateDescription" maxlength="${EmailTemplate.constrainedProperties.description.maxSize}" class="form-control"/>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="saveEmailTemplate">
                    <g:message code="default.button.save.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "modalHide", "params": ["#emailTemplateSave"]}' aria-label="Close" >
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>

<div id="emailHelpTooltipContent" style="display: none">
    <g:message code="app.emailconfiguration.help.label"/>
    <div class="form-pv"><div class="inner-table">
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
                            <li>[sectionOutputN] - <g:message code="app.label.sectionOutput"/></li>
                            <li>[sectionTableN] - <g:message code="app.label.tableOutput"/></li>
                            <li>[sectionChartN] - <g:message code="app.label.chartOutput"/></li>
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
                            <li>[sectionOutputN] - <g:message code="app.label.sectionOutput"/></li>
                            <li>[sectionTableN] - <g:message code="app.label.tableOutput"/></li>
                            <li>[sectionChartN] - <g:message code="app.label.chartOutput"/></li>
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
                            <li>[includeAllStudyDrugsCases] - <g:message code="reportCriteria.include.all.study.drugs.cases"/></li>
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
                            <g:message code="app.label.etlScheduler"/>
                        </a>
                    </h4>
                </div>
                <div id="collapseFive" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingFive">
                    <div class="panel-body">
                        <ul>
                            <li>[startTime] - <g:message code="scheduler.startTime"/></li>
                            <li>[etlStatus] - <g:message code="app.label.status"/></li>
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
        </div> </div> </div>
</div>

<g:render template="/includes/widgets/errorTemplate" model="[errorModalId:'emailConfigurationErrorModal']"/>

<asset:javascript src="vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="app/emailTemplateEditor.js"/>
<g:render template="/includes/widgets/confirmation"/>
<asset:javascript src="app/configuration/emailConfiguration.js"/>
