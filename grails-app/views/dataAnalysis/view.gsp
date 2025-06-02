<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title"/></title>
    <g:javascript>
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action: 'list')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action: 'view')}";
        var getDetailsUrl = "${createLink(controller: 'dataAnalysis', action: 'getDetails')}";
    </g:javascript>
    <asset:javascript src="app/dataAnalysis/dataAnalysis.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:stylesheet href="spotfire_integration.css"/>
    %{--todo:  Used only for code that toggle panel; move that code to a centralized/non-configuration specific file. - morett--}%
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <script type="application/javascript">
        $(function () {
            spotfire.init();
            spotfire.keepLive();
            spotfire.openSpotfireReport("${fileName}");
        });
        var ajaxProductFamilySearchUrl = "${createLink(controller: 'dataAnalysis', action: 'getProductFamilyList')}"
    </script>
</head>

<body>
<div class="col-md-12">
    <rx:container title="${message(code: "app.label.dataAnalysis", default: "Data Analysis")} - ${g.generateSpotFireFileName(fileName: fileName.encodeAsHTML())}">
        <div id="spotfire-configuration">
            <div id="spotfirePanel">
                %{--<g:message code="app.spotfire.openingfile.message"/>--}%
            </div>
            <g:javascript>
                var protocol = spotfire.config.protocol;
                var serverName = spotfire.config.server;
                var callbackServer = spotfire.config.callbackServer;
            </g:javascript>
        </div>

    </rx:container>
</div>

</body>
