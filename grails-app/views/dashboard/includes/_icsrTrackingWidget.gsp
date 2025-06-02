<%@ page import="com.rxlogix.enums.IcsrCaseStateEnum; com.rxlogix.util.FilterUtil; com.rxlogix.config.IcsrProfileConfiguration" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <span class="rxmain-container-header-label rx-widget-title" title="${message(code: "app.label.icsr.case.tracking")}">${message(code: "app.label.icsr.case.tracking")}</span>
        <a href="javascript:void(0);" id="exportUrl${index}"
           class="show-external-link rxmain-container-header-label pull-right exportUrl"
           data-content="" data-trigger="click" data-placement="bottom"><i class="md-lg md-link"></i></a>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <g:render template="/icsrProfileConfiguration/viewCasesContent" model="${pageScope.variables}"/>
</div>

<g:render template="/includes/widgets/deleteCaseConfirmationModal" />
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/icsrProfileConfiguration/submissionHistoryCase"/>
<g:render template="/icsrProfileConfiguration/errorDetails"/>
<g:render template="/icsrProfileConfiguration/caseHistoryDetails"/>
<g:render template="/icsrProfileConfiguration/transmitJustification"/>
<g:form controller="icsr" name="emailForm">
    <g:hiddenField name="exIcsrTemplateQueryId"/>
    <g:hiddenField name="caseNumber"/>
    <g:hiddenField name="versionNumber"/>
    <g:render template="/report/includes/emailToModal"
              model="['isIcsrViewTracking': true, forClass: IcsrProfileConfiguration]"/>
</g:form>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/includes/widgets/confirmation"/>