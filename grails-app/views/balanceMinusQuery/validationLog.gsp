<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; com.rxlogix.config.SourceProfile; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.balanceMinusQuery.label')}"/>
    <title><g:message code="app.validation.log.label"/></title>

    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="/app/balanceAndMinus/balanceMinusQueryLogResult.js"/>
    <g:javascript>
        var listUrl= "${createLink(controller: 'balanceMinusQuery', action: 'getValidationLogList')}?sourceProfileId=${sourceProfileId}";
    </g:javascript>

</head>

<body>
<rx:container title="${message(code: "app.validation.log.label")}" options="${true}">
    <g:render template="/includes/layout/flashErrorsDivs" />
    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["balanceMinusQuery", "validationSummary", {"sourceProfileId" : ${sourceProfileId}}]}'>
        <g:message code="default.back.label" />
    </button>
    <div class="horizontalRuleFull"></div>
    <div class="pv-caselist">
        <table id="balanceQueryLogResult" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.validation.type.label"/></th>
                <th><g:message code="app.src.table.label"/></th>
                <th><g:message code="app.tgt.table.label"/></th>
                <th><g:message code="app.src.column.name.label"/></th>
                <th><g:message code="app.tgt.column.name.label"/></th>
                <th><g:message code="app.src.value.label"/></th>
                <th><g:message code="app.tgt.value.label"/></th>
                <th><g:message code="app.impacted.pk.label"/></th>
                <th><g:message code="app.case.id.label"/></th>
                <th><g:message code="app.caseNumber.label"/></th>
                <th class="dataTableColumnCenter forceLineWrapDate"><g:message code="app.last.update.time.label"/></th>
            </tr>
            </thead>
        </table>
    </div>

</rx:container>
</body>
</html>