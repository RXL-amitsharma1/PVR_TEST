<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.config.ResponsibleParty; com.rxlogix.enums.ReasonOfDelayAppEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PvqTypeEnum; groovy.json.JsonOutput" %>
<html>
<head>
    <title><g:message code="app.quality.actionPlan.title"/></title>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/actionItem/actionItemList.js"/>
    <asset:javascript src="app/actionPlan.js"/>
    <asset:stylesheet src="rowGroup.dataTables.min.css"/>
    <meta name="layout" content="main"/>
    <style>
    #actionPlatTable_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
    }
    #actionPlatTable_wrapper .dt-layout-row.dt-layout-table {
        overflow-x: auto;
        width: 100%;
    }
    .customDateRange .datepicker .open > .dropdown-menu {
        z-index: 1002 !important;
        right: -10px;
    }
    </style>

</head>

<body>
<div class="content ">
    <div class="container ">
        <rx:container title="${message(code: "app.actionPlan.actionPlan")}" options="${false}">
            <g:render template="/quality/includes/actionPlan" model="[module: 'quality']"/>
        </rx:container>
        <g:render template="/actionItem/includes/actionItemModal" model="${[categories: ViewHelper.actionItemCategoryForActionPlan()]}"/>
    </div>
</div>
<g:render template="/quality/includes/actionPlanCaseListModal"/>
<g:render template="/includes/widgets/confirmation"/>
<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.pvc.export.warn'), warningModalId:'exportWarning', warningButtonId:'exportWarningOkButton', queryType: '']"/>
</body>
</html>


