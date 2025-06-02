<div role="tabpanel" class="tab-pane " id="spotfireTab">
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
        spotfire.openSpotfireReport("${params.fileName}");
    });
</script>
<div class="col-md-12">
    <rx:container title="${message(code: "app.label.dataAnalysis", default: "Data Analysis")} - ${g.generateSpotFireFileName(fileName: params.fileName)}">
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
</div>