<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; com.rxlogix.config.SourceProfile; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.balanceMinusQuery.label')}"/>
    <title><g:message code="app.validation.summary.label"/></title>

    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/balanceAndMinus/balanceMinusQuerySummaryResult.js"/>
    <g:javascript>
        var listUrl= "${createLink(controller: 'balanceMinusQuery', action: 'getValidationSummaryList')}?sourceProfileId=${sourceProfileId}";
    </g:javascript>

</head>

<body>
<rx:container title="${message(code: "app.validation.summary.label")}" options="${true}">
    <g:render template="/includes/layout/flashErrorsDivs" />
    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["balanceMinusQuery", "index"]}'>
        <g:message code="default.back.label" />
    </button>
    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["balanceMinusQuery", "validationLog", {"sourceProfileId" : ${sourceProfileId}}]}'>
        <g:message code="app.validation.log.label" args="[entityName]" />
    </button>
    <div class="horizontalRuleFull"></div>
    <div class="pv-caselist">
        <table id="balanceQuerySummaryResult" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
                <tr>
                    <th><g:message code="app.validation.type.label"/></th>
                    <th><g:message code="app.src.table.label"/></th>
                    <th><g:message code="app.tgt.table.label"/></th>
                    <th><g:message code="app.src.column.name.label"/></th>
                    <th><g:message code="app.tgt.column.name.label"/></th>
                    <th><g:message code="app.src.count.label"/></th>
                    <th><g:message code="app.tgt.count.label"/></th>
                    <th><g:message code="app.elapsed.minutes.label"/></th>
                    <th><g:message code="app.label.status"/></th>
                </tr>
            </thead>
        </table>
    </div>
</rx:container>
</body>
</html>