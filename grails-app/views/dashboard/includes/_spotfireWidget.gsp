<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="etlSchedule" action="index"
        title="${message(code: 'app.label.dataAnalysis')}" class="rxmain-container-header-label rx-widget-title"><g:message code="app.label.dataAnalysis"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <g:javascript>
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action: 'list')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action: 'view')}";
        var ajaxProductFamilySearchUrl = "${createLink(controller: 'dataAnalysis', action: 'getProductFamilyList')}"
        var getDetailsUrl = "${createLink(controller: 'dataAnalysis', action: 'getDetails')}";
    </g:javascript>

    <asset:javascript src="app/dataAnalysis/dataAnalysis.js"/>
    <asset:stylesheet href="spotfire_integration.css"/>
    <script type="application/javascript">
        $(function () {
            spotfire.init();
            spotfire.keepLive();
            spotfire.openSpotfireReport(spotfire.config.libraryRoot + "/${widget.reportWidget.settings}");
        });
    </script>
    <div class="row rx-widget-content">
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

    </div>
</div>
<script>

</script>