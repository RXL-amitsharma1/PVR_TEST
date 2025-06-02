<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper" %>
<html>
<head>
    <meta name="layout" content="main"/>
<title>
    <g:if test="${params.pvp}"><g:message code="app.PvpsubmissionHistory.title"/></g:if>
    <g:else><g:message code="app.submissionHistory.title"/></g:else>
</title>
    <script type="application/javascript">
        var reportSubmissionConfig = {
            viewExecutedReportUrl: "${createLink(controller: "periodicReport", action: "viewExecutedConfig")}",
            report_submission_list_url: "${createLink(controller: "reportSubmissionRest", action: "index",params: [icsr:params.icsr])}",
            attachment_download_url: "${createLink(controller: "reportSubmission", action: "downloadAttachment")}",
            viewSubmittedCaseSeries: "${createLink(controller: "reportSubmission", action: "viewCases")}",
            viewReportResultUrl: "${createLink(controller: "report", action: "showFirstSection")}",
            capaUrl: "${createLink(controller: "issue", action: "edit")}",
            createCapaUrl: "${createLink(controller: 'issue', action: 'create')}"
        }
        var isPublisherReport = sessionStorage.getItem("module")=="pvp";
        var isRodProcessingEnabled = ${Holders.config.submission.rod.processing};
    </script>
    <asset:javascript src="handlebar/handlebars-v4.0.11.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/reportSubmissionList.js"/>
    <asset:javascript src="app/reportSubmission.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div >
<rx:container title="${message(code: "app.label.reportSubmission.title")}" options="true" filterButton="true">
    <div class="topControls" style="float: right;display: -webkit-inline-box;">
        <div class="checkbox checkbox-primary pvpOnly" style=" text-align: center;">
            <g:checkBox id="allReportsFilter" name="allReportsFilter"/>
            <label for="allReportsFilter" style="font-weight: bold; padding-right: 5px;"><g:message code="app.label.allReportsFilter"/></label>
        </div>
        <div class="checkbox checkbox-primary pvpOnly" style=" text-align: center;display: none;">
            <g:checkBox id="allReportsFilter" name="allReportsFilter"/>
            <label for="allReportsFilter" style="font-weight: bold; padding-right: 5px;"><g:message code="app.label.allReportsFilter"/></label>
        </div>
        <g:select name="submissionStatusEnum" from="${ViewHelper.getReportSubmissionStatusEnumWoPendingI18n()}" optionKey="name" data-width="200px" style="  text-align: left; !important;float: right;margin-left: 5px !important;margin-right: 5px !important;" optionValue="display" noSelection="['':'']" class="form-control"/>
        <input id="caseSeriesSearchDiv" class="form-control" style="position: relative; width: 35%; max-width: 250px"  maxlength="255"  placeholder="${message(code:("app.caseNumber.label"))}">
    </div>

            <div class="pv-caselist">
                <table id="reportSubmissionsList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                    <thead>
                    <tr>
                        <th style="width: 60px"><g:message default="PR Type" code="app.label.reportSubmission.report.type"/></th>
                        <th style="width: 150px"><g:message default="Product" code="app.label.reportSubmission.report.product"/></th>
                        <th style="width: 200px"><g:message default="Report Name" code="app.label.reportSubmission.report.reportName"/></th>
                        <th style="width: 150px"><g:message default="Date Range" code="app.label.reportSubmission.report.dateRange"/></th>
                        <th style="width: 110px"><g:message default="Countries" code="app.label.reportSubmission.destinations"/></th>
                        <th style="width: 70px"><g:message default="Primary Agency (Y/N)" code="app.label.reportSubmission.primaryDestination"/></th>
                        <th style="width: 110px"><g:message default="Status" code="app.label.reportSubmission.state"/></th>
                        <th style="width: 150px"><g:message default="Submission Date" code="app.label.reportSubmission.submissionDate"/></th>
                        <th style="width: 150px"><g:message default="Submission Due Date" code="app.label.reportSubmission.dueDate"/></th>
                        <th style="width: 100px"><g:message default="License" code="app.label.reportSubmission.license"/></th>
                        <g:if test="${Holders.config.submission.rod.processing}">
                        <th style="width: 50px"><g:message default="Submitted on time (Y/N)" code="app.label.reportSubmission.onTime"/></th>
                        <th style="width: 50px"><g:message code="app.label.attachments"/></th>
                        </g:if>
                        <th style="width: 80px"><g:message default="View" code="app.label.view"/></th>
                    </tr>
                    </thead>
                </table>
            </div>

</rx:container>
        </div>
    </div>
</div>
<div class="modal fade in" id="lateModalDiv" role="dialog">
    <div class="modal-dialog">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">×</button>
                <h4 class="modal-title">Submitted Late</h4>
            </div>
            <g:form action="updateLate">
                <div class="modal-body">
                     <div class="row">

                        <div class="col-md-12">
                    <div class="form-group">
                        <label style="margin-top: 6px"><g:message code="app.label.view.case.late"/></label>
                        <g:select name="late" from="${Holders.config.submissions.late.name}"  class="form-control select2-box"/>
                    </div>
                    </div>
                     </div>
                    <div class="reasonResponsibleContainer">
                        <div id="selectTemplates" style="display: none">
                            <g:select name="responsible" from="${Holders.config.submission.responsibleparty}" value=""  class="form-control responsible" style="margin-bottom: 5px"/>
                            <g:select name="reason" from="${Holders.config.submission.reason}" value=""  class="form-control reason" style="margin-bottom: 5px"/>
                        </div>
                        <div class="row">
                            <div class="col-md-1"><label style="font-size: 17px;margin-left: 20px; cursor: pointer"><span title="Add Reason" class="fa fa-plus addReason"></span></label></div>
                            <div class="col-md-5"><label><g:message code="app.label.view.case.responsible.party"/></label></div>
                            <div class="col-md-6"><label><g:message code="app.label.view.case.reason"/></label></div>
                        </div>
                    </div>
                    <input type="hidden" name="id" id="id">

                    <div>
                        <small class="text-muted">
                            <span id="lastUpdated-label" class="property-label" style="margin-left: 50px;">
                                <g:message code="app.label.modifiedDate"/>:
                            </span>
                            <span class="property-value" aria-labelledby="lastUpdated-label" id="lastUpdated"></span>


                            <span id="modifiedBy-label" class="property-label" style="margin-left: 20px;">
                                <g:message code="app.label.modifiedBy"/>:
                            </span>
                            <span class="property-value" aria-labelledby="dateCreated-label" id="modifiedBy"></span>
                        </small>
                    </div>

                    <div id="capa"></div>

                </div>

                <div class="modal-footer">
                    <div style="margin-top: 10px; width: 100%; text-align: right;">

                        <input type="submit" class="btn btn-primary cancelButton updateLate" value="Update"/>
                        <button type="button" class="btn btn-default cancelButton" data-dismiss="modal"><g:message code="app.button.close"/></button>
                    </div>
                </div>
            </g:form>
        </div>
    </div>
</div>

</body>
</html>