<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="report" action="index"
                class="rxmain-container-header-label rx-widget-title" title="${message(code: 'app.widget.button.adhoc.label')}" ><g:message code="app.widget.button.adhoc.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content">
        <div class="dashboardSummaryContent adhocSummary">
            <div class="row">
                <div class="col-sm-3" style="text-align: center;">
                        <span class="adhoc dashboardAlert"><i class="fa fa-exclamation-circle" aria-hidden="true"></i></span>
                        <span class="adhoc dashboardSuccess"><i class="fa fa-thumbs-o-up" aria-hidden="true"></i></span>
                </div>

                <div class="col-sm-9">
                    <g:message code="auditLog.domainObject.WorkflowState"/>:<br>
                    <span id="adhocWorkFrowContent${index}"></span>
                    <br>
                </div>
            </div>

            <i class="md md-check"></i> <g:message code="app.widget.total"/>: <b><span id="adhocTotalContent${index}"></span></b>
            <i class="md md-check"></i> <g:message code="app.widget.recent"/>: <b><span id="adhocNewContent${index}"></span></b>
            <span id="adhocErrorContent${index}"></span>
        </div>
    </div>
</div>
<script>
    $(function () {
        var loadData = function () {
            $.ajax({
                "url": adhocReportsSummaryUrl,
                "dataType": 'json'
            }).done(function (data) {
                wfContent = $("#adhocWorkFrowContent${index}");
                var states = data.result.states;
                wfContentString = "";
                for (var i = 0; i < states.length; i++) {
                    wfContentString += "<i class='md md-check'></i> " + states[i][0] + ": " + states[i][1] + " <br>"

                }
                wfContent.html(wfContentString);
                $("#adhocTotalContent${index}").html(data.result.total ? data.result.total : 0);
                $("#adhocNewContent${index}").html(data.result.new ? data.result.new : 0);
                $("#adhocErrorContent${index}").html(data.result.error ? ('<span style="color: red"><i class="fa fa-exclamation-circle"></i> <g:message code="app.widget.errors"/>: ' + data.result.error + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.widget.errors"/>: 0')
                if (data.result.error)
                    $(".adhoc.dashboardAlert").show();
                else
                    $(".adhoc.dashboardSuccess").show();
            });
        };
        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>