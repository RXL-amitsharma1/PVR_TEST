<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.reportField.appName")}"/>
    <title><g:message code="app.field.management.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        var REPORTFIELD = {
             listUrl: "${createLink(controller: 'reportFieldRest', action: 'index')}",
             editUrl: "${createLink(controller: 'reportField', action: 'edit')}",
             viewUrl: "${createLink(controller: 'reportField', action: 'show')}",
             deleteUrl: "${createLink(controller: 'reportField', action: 'delete')}",
             isCreatedByUserList: "${isCreatedByUserList}"
        }
    </g:javascript>
    <asset:javascript src="app/reportField/report-field.js"/>

</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFieldInstanceList}" var="theInstance"/>

            <rx:container title="${message(code: "app.label.field.management")}" options="true">
                <div class="body">
                    <div id="action-list-conainter" class="list">
                    <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-right:15px;">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["reportField", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.report.field.create')}" style="color: #353d43;"></i>
        </a>

    </div>
    <div class="pv-caselist">
    <table id="rxTableReportField" class="table table-striped pv-list-table dataTable no-footer" width="100%">
        <thead>
        <tr>
            <th class="reportFieldNameColumn"><g:message code="report.field.name.label"/></th>
            <th class="reportFieldGroupColumn"><g:message code="report.fieldGroup.label"/></th>
            <th style="width: 80px;"><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>
</div>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
                </div>
                </div>
</rx:container>
        </div>
    </div>
</div>
</body>
</html>
