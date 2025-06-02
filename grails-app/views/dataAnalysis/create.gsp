<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title" /></title>

    <asset:javascript src="app/dataAnalysis/dataAnalysis.js"/>
    <asset:stylesheet href="spotfire_integration.css" />

    %{--todo:  Used only for code that toggle panel; move that code to a centralized/non-configuration specific file. - morett--}%
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <g:javascript>
        var getDetailsUrl = "${createLink(controller: 'dataAnalysis', action: 'getDetails')}";
    </g:javascript>
    <script type="application/javascript">
        $(function () {
            spotfire.init();
            spotfire.bindUiInputs();
        });
        var executedCaseSeriesListUrl = "${createLink(controller: 'executedCaseSeriesRest', action: 'getExecutedCaseSeriesList')}";
        var executedCaseSeriesItemUrl = "${createLink(controller: 'executedCaseSeriesRest', action: 'getExecutedCaseSeriesItem')}";
        var ajaxProductFamilySearchUrl = "${createLink(controller: 'dataAnalysis', action: 'getProductFamilyList')}"

</script>
</head>

<g:set var="spotfireService" bean="spotfireService"/>

<body>
<div class="content ">
    <div class="container ">
        <div>
<rx:container title="${message(code:"app.label.dataAnalysis", default: "Data Analysis")}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${spotfireCommand}" var="theInstance"/>
    <div id="spotfire-configuration">

        <g:form action="generate">
            <g:render template="form"/>
        </g:form>
    </div>
</rx:container>
        </div>
    </div>
</div>
</body>
