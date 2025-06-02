<%@ page import="com.rxlogix.enums.IcsrReportSpecEnum; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.viewResult.title"/></title>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/report/icsrReport.js"/>
    <script type="text/javascript">
        var icsrReportConfig = {
            caseListUrl: "${createLink(controller: 'icsrReportConfigurationRest', action: 'resultCaseList', id: reportResult?.id)}",
            caseDataLinkR2Url: "${createLink(controller: 'icsr', action: 'showReport', params: ['exIcsrTemplateQueryId': reportResult.executedTemplateQuery?.id])}"
        };
        $(function () {
            icsrReport.icsrCaseList.init_icsr_case_list_table();
        });
    </script>
    <style type="text/css">
    table.dataTable tbody td.select-checkbox, table.dataTable tbody th.select-checkbox {
        position: relative;
    }
    table.dataTable tbody td.select-checkbox:before {
        top: 50% !important;
    }
    table.dataTable tbody td.select-checkbox:after {
        top: 50% !important;
    }
    table.dataTable tbody td.select-checkbox:before, table.dataTable tbody td.select-checkbox:after, table.dataTable tbody th.select-checkbox:before, table.dataTable tbody th.select-checkbox:after {
        display: block;
        position: absolute;
        top: 1.2em;
        left: 50%;
        width: 12px;
        height: 12px;
        box-sizing: border-box;
    }
    table.dataTable tbody td.select-checkbox:before, table.dataTable tbody th.select-checkbox:before {
        content: ' ';
        margin-top: -6px;
        margin-left: -6px;
        border: 1px solid black;
        border-radius: 3px;
    }
    table.dataTable tr.selected td.select-checkbox:after, table.dataTable tr.selected th.select-checkbox:after {
        content: '\2714';
        margin-top: -11px;
        margin-left: -4px;
        text-align: center;
        text-shadow: 1px 1px #B0BED9, -1px -1px #B0BED9, 1px -1px #B0BED9, -1px 1px #B0BED9;
    }

    #icsrCaseList_wrapper .dt-length, #icsrCaseList_wrapper .dt-info {
        float: left !important;
    }
    #icsrCaseList_wrapper .dt-search, #icsrCaseList_wrapper .dt-paging {
        float: right !important;
    }
    #icsrCaseList_wrapper:after {
        visibility: hidden;
        display: block;
        content: "";
        clear: both;
        height: 0;
    }
    #icsrCaseList_processing {
        position: absolute;
        top: 150px;
        left: 0;
        width: 100%;
        height: 150px;
        text-align: center;
        vertical-align: middle;
        line-height: 100px;
        font-size: 1.2em;
        background: linear-gradient(to right, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.9) 25%, rgba(255, 255, 255, 0.9) 75%, rgba(255, 255, 255, 0) 100%);
    }
    </style>
</head>

<body>
<rx:container title="${message(code: 'app.label.icsrReport')}:  ${executedConfigurationInstance.reportName.encodeAsHTML()}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}" var="theInstance"/>

    <div class="btn-group">
        <g:link class="btn btn-default waves-effect waves-light"
                controller="report" action="criteria" params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode]">
            <i class="md md-description icon-white"></i>
            <g:message code="app.label.reportCriteria"/>
        </g:link>
    </div>
    <div class="list add-margin-top">
        <table id="icsrCaseList" class="row-border hover dataTable no-footer" style="width: 100%">
            <thead>
            <tr>
                <th></th>
                <th style="min-width: 90px"><g:message code="app.caseList.caseNumber"/></th>
                <th><g:message code="app.caseList.versionNumber"/></th>
                <!--<th><g:message code="app.icsr.report.caseList.profileName"/></th>-->
                <th><g:message code="app.icsr.report.caseList.productName"/></th>
                <th><g:message code="app.icsr.report.caseList.eventPreferredTerm"/></th>
                <th><g:message code="app.icsr.report.caseList.susar"/></th>
                <th><g:message code="app.icsr.report.caseList.downgrade"/></th>
            </tr>
            </thead>
        </table>
    </div>
    <g:render template="includes/addToIcsrTracking" model="[reportResult:reportResult,executedConfigurationInstance:executedConfigurationInstance, referenceProfileId: referenceProfileId]"/>
</rx:container>
</body>
</html>
