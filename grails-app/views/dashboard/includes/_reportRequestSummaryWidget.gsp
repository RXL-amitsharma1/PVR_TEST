<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="reportRequest" action="index"
                title="${message(code: 'app.widget.button.reportRequest.label')}" class="rxmain-container-header-label rx-widget-title"><g:message code="app.widget.button.reportRequest.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content">
        <div class="dashboardSummaryContent reportRequestSummary">
            <div class="row">
                <div class="col-sm-3" style="text-align: center;">
                    <span class="reportRequest dashboardAlert" ><i class="fa fa-exclamation-circle" aria-hidden="true"></i></span>
                    <span class="reportRequest dashboardWarning"><i class="fa fa-warning" aria-hidden="true"></i></span>
                    <span class="reportRequest dashboardSuccess"><i class="fa fa-thumbs-o-up" aria-hidden="true"></i></span>
                </div>

                <div class="col-sm-9">
                    <span id="reportRequestOverDueContent${index}"></span><br>
                    <span id="reportRequestDueSoonContent${index}"></span><br>

                    <i class="md md-check"></i>   <g:message code="app.widget.inProgress"/>: <span id="reportRequestInProgressContent${index}"></span><br>
                    <i class="md md-check"></i>  <g:message code="app.widget.opened"/>: <span id="reportRequestOpenContent${index}"></span><br>
                    <br>
                </div>
            </div>
            <i class="md md-check"></i> <g:message code="app.widget.total"/>: <b><span id="reportRequestTotalContent${index}"></span></b>
            <i class="md md-check"></i> <g:message code="app.widget.recent"/>: <b><span id="reportRequestNewContent${index}"></span></b>
        </div>
    </div>
</div>
<script>
    $(function () {
        var loadData = function () {
            $.ajax({
                "url": reportRequestSummaryUrl,
                dataType: 'json'
            }).done(function (data) {
                $("#reportRequestTotalContent${index}").html(data.result.total ? data.result.total : 0);
                $("#reportRequestNewContent${index}").html(data.result.new ? data.result.new : 0);
                $("#reportRequestOpenContent${index}").html(data.result.open ? data.result.open : 0);
                $("#reportRequestInProgressContent${index}").html(data.result.inprogress ? data.result.inprogress : 0);

                $("#reportRequestOverDueContent${index}").html(data.result.overdue ? ('<span style="color: red"><i class="fa fa-exclamation-circle"></i> <g:message code="app.widget.overdue"/>: ' + data.result.overdue + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.widget.overdue"/>: 0');
                $("#reportRequestDueSoonContent${index}").html(data.result.dueSoon ? ('<span style="color: #d58512"><i class="fa fa-warning"></i>  <g:message code="app.widget.dueSoon"/>: ' + data.result.dueSoon + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.widget.dueSoon"/>: 0');


                if (data.result.overdue)
                    $(".reportRequest.dashboardAlert").show();
                else if (data.result.dueSoon)
                    $(".reportRequest.dashboardWarning").show();
                else
                    $(".reportRequest.dashboardSuccess").show();
            });
        };
        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>