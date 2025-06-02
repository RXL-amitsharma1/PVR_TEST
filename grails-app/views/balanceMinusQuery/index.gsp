<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; com.rxlogix.config.SourceProfile; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.balanceMinusQuery.label')}"/>
    <title><g:message code="app.balanceMinusQueryStatus.title"/></title>

    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/balanceAndMinus/balanceMinusQueryResult.js"/>
    <g:javascript>
        var listUrl= "${createLink(controller: 'balanceMinusQuery', action: 'fetchBalanceMinusQueryList')}?sourceProfileId=${sourceProfileId}";
    </g:javascript>

</head>

<body>
<rx:container title="${message(code: "app.balanceMinusQuery.label")}" customButtons="${g.render(template: "/balanceMinusQuery/include/shareTemplate",model: [sourceProfileId:sourceProfileId])}">

    <g:render template="/includes/layout/flashErrorsDivs" />
    <div class="col-lg-4">
    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["balanceMinusQuery", "create"]}' id="balanceCheck">
        <g:message code="app.balanceMinusQueryStatus.run.label" args="[entityName]" />
    </button>
    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["balanceMinusQuery", "validationSummary", {"sourceProfileId" : ${sourceProfileId}}]}'>
        <g:message code="app.validation.summary.label" args="[entityName]" />
    </button>
    </div>
    <div class="col-lg-2">
    <g:select name="sourceProfile.id" id="sourceProfile"
              from="${sourceProfiles}"
              optionValue="sourceName" optionKey="id"
              value="${sourceProfileId ?: SourceProfile?.central?.id}"
              class="form-control"/>
    </div>
    <div class="horizontalRuleFull"></div>
    <div class="pv-caselist">
        <table id="balanceQueryResult" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
                <tr>
                    <th><g:message code="app.validation.key.label"/></th>
                    <th><g:message code="app.validation.value.label"/></th>
                </tr>
            </thead>
        </table>
    </div>

</rx:container>
</body>
</html>