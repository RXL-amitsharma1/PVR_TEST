<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="periodicReport" action="reports" title="${message(code: 'app.widget.button.aggregate.label')}"
                class="rxmain-container-header-label rx-widget-title"><g:message code="app.widget.button.aggregate.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content">
        <div class="dashboardSummaryContent aggregateSummary">
            <div class="row">
                <div class="col-sm-3" style="text-align: center;">
                        <span class="aggregate dashboardAlert" ><i class="fa fa-exclamation-circle" aria-hidden="true"></i></span>
                        <span class="aggregate dashboardWarning"><i class="fa fa-warning" aria-hidden="true"></i></span>
                        <span class="aggregate dashboardSuccess"><i class="fa fa-thumbs-o-up" aria-hidden="true"></i></span>
                </div>

                <div class="col-sm-5">
                    <span id="aggregateOverDueContent${index}"></span><br>
                    <span id="aggregateDueSoonContent${index}"></span><br>
                    <i class="md md-check"></i>  <g:message code="app.label.pendingSubmissionsCount"/>: <span id="aggregatePendingContent${index}"></span><br>
                    <i class="md md-check"></i>  <g:message code="app.label.submittedRecentlyCount"/>: <span id="aggregateSubmitedRecentlyContent${index}"></span><br>
                    <i class="md md-check"></i>  <g:message code="app.label.scheduledCount"/>: <span id="aggregateScheduledContent${index}"></span><br>


                </div>
                <div class="col-sm-4">
                    <g:message code="auditLog.domainObject.WorkflowState"/>:<br>
                    <span id="aggregateWorkFrowContent${index}"></span>
                </div>
            </div>
            <br>
            <i class="md md-check"></i> <g:message code="app.widget.total"/>: <b><span id="aggregateTotalContent${index}"></span></b>
            <i class="md md-check"></i> <g:message code="app.widget.recent"/>: <b><span id="aggregateNewContent${index}"></span></b>
            <span id="aggregateErrorContent${index}"></span>
        </div>
    </div>
</div>

<script>
    $(function () {
        var loadData = function () {
            $.ajax({
                "url": aggregateReportsSummaryUrl,
                "dataType": 'json'
            }).done(function (data) {
                wfContent = $("#aggregateWorkFrowContent${index}");
                var states = data.result.states;
                wfContentString = "";
                for (var i = 0; i < states.length; i++) {
                    wfContentString += "<i class='md md-check'></i> " + states[i][0] + ": " + states[i][1] + " <br>"

                }
                wfContent.html(wfContentString);
                $("#aggregateTotalContent${index}").html(data.result.total ? data.result.total : 0);
                $("#aggregateNewContent${index}").html(data.result.new ? data.result.new : 0);
                $("#aggregatePendingContent${index}").html(data.result.pending ? data.result.pending : 0);
                $("#aggregateSubmitedRecentlyContent${index}").html(data.result.submittedRecently ? data.result.submittedRecently : 0);
                $("#aggregateScheduledContent${index}").html(data.result.scheduled ? data.result.scheduled : 0);

                $("#aggregateOverDueContent${index}").html(data.result.overDue ? ('<span style="color: red"><i class="fa fa-exclamation-circle"></i> <g:message code="app.label.overdueCount"/>: ' + data.result.overDue + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.label.overdueCount"/>: 0')
                $("#aggregateDueSoonContent${index}").html(data.result.dueSoon ? ('<span style="color: #d58512"><i class="fa fa-warning"></i>  <g:message code="app.label.dueSoonCount"/>: ' + data.result.dueSoon + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.label.dueSoonCount"/>: 0')

                $("#aggregateErrorContent${index}").html(data.result.error ? ('<span style="color: red"><i class="fa fa-exclamation-circle"></i> <g:message code="app.widget.errors"/>: ' + data.result.error + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.widget.errors"/>: 0')

                if (data.result.error || data.result.overDue)
                    $(".aggregate.dashboardAlert").show();
                else if (data.result.dueSoon)
                    $(".aggregate.dashboardWarning").show();
                else
                    $(".aggregate.dashboardSuccess").show();
            });
        };
        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>