<%@ page import="com.rxlogix.util.ViewHelper"%>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="actionItem" action="index"
                title="${message(code: 'app.widget.button.actionItem.label')}"  class="rxmain-container-header-label rx-widget-title"><g:message code="app.widget.button.actionItem.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content">
        <div class="dashboardSummaryContent actionItemSummary">
            <div class="row">
                <div class="col-sm-3" style="text-align: center;">
                        <span class="actionItem dashboardAlert" ><i class="fa fa-exclamation-circle" aria-hidden="true"></i></span>
                        <span class="actionItem dashboardWarning"><i class="fa fa-warning" aria-hidden="true"></i></span>
                        <span class="actionItem dashboardSuccess"><i class="fa fa-thumbs-o-up" aria-hidden="true"></i></span>
                </div>

                <div class="col-sm-9">
                    <span id="actionItemOverDueContent${index}"></span><br>
                    <span id="actionItemDueSoonContent${index}"></span><br>

                    <i class="md md-check"></i>   <g:message code="app.widget.inProgress"/>: <span id="actionItemInProgressContent${index}"></span><br>
                    <i class="md md-check"></i>  <g:message code="app.widget.opened"/>: <span id="actionItemOpenContent${index}"></span><br>
                    <br>
                </div>
            </div>
            <i class="md md-check"></i> <g:message code="app.widget.total"/>: <b><span id="actionItemTotalContent${index}"></span></b>
            <i class="md md-check"></i> <g:message code="app.widget.recent"/>: <b><span id="actionItemNewContent${index}"></span></b>
        </div>
    </div>
</div>
<script>
    $(function () {
        var loadData = function () {
            $.ajax({
                "url": actionItemSummaryUrl,
                "dataType": 'json'
            }).done(function (data) {
                $("#actionItemTotalContent${index}").html(data.result.total ? data.result.total : 0);
                $("#actionItemNewContent${index}").html(data.result.new ? data.result.new : 0);
                $("#actionItemOpenContent${index}").html(data.result.opened ? data.result.opened : 0);
                $("#actionItemInProgressContent${index}").html(data.result.inProgress ? data.result.inProgress : 0);

                $("#actionItemOverDueContent${index}").html(data.result.overDue ? ('<span style="color: red"><i class="fa fa-exclamation-circle"></i> <g:message code="app.widget.overdue"/>: ' + data.result.overDue + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.widget.overdue"/>: 0');
                $("#actionItemDueSoonContent${index}").html(data.result.dueSoon ? ('<span style="color: #d58512"><i class="fa fa-warning"></i>  <g:message code="app.widget.dueSoon"/>: ' + data.result.dueSoon + '</span>') : ' <i class="md md-check"></i>  <g:message code="app.widget.dueSoon"/>: 0');


                if (data.result.overDue)
                    $(".actionItem.dashboardAlert").show();
                else if (data.result.dueSoon)
                    $(".actionItem.dashboardWarning").show();
                else
                    $(".actionItem.dashboardSuccess").show();
            });
        };
        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>