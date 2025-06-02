<%@ page import="com.rxlogix.enums.EtlStatusEnum" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="etlSchedule" action="index"
        title="${message(code: 'app.widget.button.etl.label')}" class="rxmain-container-header-label rx-widget-title"><g:message code="app.widget.button.etl.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content">
        <div class="etlSummary">
            <div class="row">
                <div class="col-sm-3" style="text-align: center;">
                        <span class="etl dashboardAlert" ><i class="fa fa-exclamation-circle" aria-hidden="true"></i></span>
                        <span class="etl dashboardWarning"><i class="fa fa-warning" aria-hidden="true"></i></span>
                        <span class="etl dashboardSuccess"><i class="fa fa-thumbs-o-up" aria-hidden="true"></i></span>
                </div>

                <div class="col-sm-9">
                    <i class="md md-check preStatusContent${index}"></i> <g:message code="pre.mart.etl.execution.status"/>: <span id="preStatusContent${index}"><g:message code="etl.execution.no.status"/></span></span><br>
                    <i class="md md-check"></i> <g:message code="pre.mart.etl.lastRun.dateTime"/>: <span id="preLastRunContent${index}"></span><br>
                    <div class="affiliateEtl hide">
                        <i class="md md-check affStatusContent${index}"></i> <g:message code="affiliate.etl.execution.status"/>: <span id="affStatusContent${index}"><g:message code="etl.execution.no.status"/></span></span><br>
                        <i class="md md-check"></i> <g:message code="affiliate.etl.lastRun.dateTime"/>: <span id="affLastRunContent${index}"></span><br>
                    </div>
                    <i class="md md-check statusContent${index}"></i> <g:message code="etl.execution.status"/>: <span id="statusContent${index}"><g:message code="etl.execution.no.status"/></span></span><br>
                    <i class="md md-check"></i> <g:message code="etl.lastRun.dateTime"/>: <span id="lastRunContent${index}"></span><br>
                    <i class="md md-check enabledContent${index}"></i> <g:message code="default.button.enable.label"/>: <span id="enabledContent${index}"></span><br>
                    <i class="md md-check"></i> <g:message code="app.label.scheduler"/>: <span id="repeatContent${index}"></span><br>
                    <br>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(function () {
        var loadData = function () {
            $.ajax({
                "url": etlUrl,
                "dataType": 'json'
            }).done(function (data) {

                var status;
                var preStatus;
                var preLastRun;
                var affStatus;
                if (data.result.preEtlAvailable == "false") {
                    preStatus = $.i18n._("app.label.not.applicable")
                    preLastRun = $.i18n._("app.label.not.applicable")
                } else {
                    preLastRun = data.result.preLastRun
                }
                getEtlStatus(data.result.preStatus, preStatus, ".preStatusContent${index}", "#preStatusContent${index}", "#preLastRunContent${index}", preLastRun);
                if (data.result.affEtlAvailable == "true") {
                    $(".affiliateEtl").removeClass("hide");
                    getEtlStatus(data.result.affStatus, affStatus, ".affStatusContent${index}", "#affStatusContent${index}", "#affLastRunContent${index}", data.result.affLastRun);
                }
                getEtlStatus(data.result.status, status, ".statusContent${index}", "#statusContent${index}", "#lastRunContent${index}", data.result.lastRun);
                const result = data.result;
                getIconBasedOnEtlStatus(result.preEtlAvailable, result.preStatus, result.affEtlAvailable, result.affStatus, result.status);
                $("#enabledContent${index}").html(data.result.enabled);
                var repeatContent = "";
                for (var i = 0; i < data.result.repeat.length; i++) {
                    repeatContent += data.result.repeat[i].label + ': ' + data.result.repeat[i].value + '; ';
                }
                $("#repeatContent${index}").html(repeatContent);
            });
        };
        $('#refresh-widget${index}').hide();
        loadData();
    });

    function getIconBasedOnEtlStatus(preEtlAvailable, preStatus, affEtlAvailable, affStatus, status) {
        $(".etl").hide();
        if(preEtlAvailable == "true" && affEtlAvailable == "true") {
            if(preStatus != "${EtlStatusEnum.FAILED}" && affStatus == "${EtlStatusEnum.SUCCESS}" && status == "${EtlStatusEnum.SUCCESS}") {
                $(".etl.dashboardSuccess").show();
            }else if(preStatus != "${EtlStatusEnum.FAILED}" && affStatus == "${EtlStatusEnum.RUNNING}" && status == "${EtlStatusEnum.RUNNING}") {
                $(".etl.dashboardWarning").show();
            }else if(preStatus == "${EtlStatusEnum.FAILED}" && affStatus == "${EtlStatusEnum.FAILED}" && status == "${EtlStatusEnum.FAILED}") {
                $(".etl.dashboardAlert").show();
            }
        }else if(preEtlAvailable == "true" && affEtlAvailable == "false") {
            if(preStatus == "${EtlStatusEnum.SUCCESS}" && status == "${EtlStatusEnum.SUCCESS}") {
                $(".etl.dashboardSuccess").show();
            }else if(preStatus == "${EtlStatusEnum.FAILED}" || status == "${EtlStatusEnum.FAILED}") {
                $(".etl.dashboardAlert").show();
            }else if(preStatus == "${EtlStatusEnum.RUNNING}" || status == "${EtlStatusEnum.RUNNING}") {
                $(".etl.dashboardWarning").show();
            }
        }else {
            if(status == "${EtlStatusEnum.SUCCESS}") {
                $(".etl.dashboardSuccess").show();
            }else if(status == "${EtlStatusEnum.FAILED}") {
                $(".etl.dashboardAlert").show();
            }else if(status == "${EtlStatusEnum.RUNNING}") {
                $(".etl.dashboardWarning").show();
            }
        }
    }

    function getEtlStatus(resultStatus, status, statusContentClass, statusContentId, lastRunContent, resultLastRun) {
        if (resultStatus == "${EtlStatusEnum.SUCCESS}") {
            status = "${g.message(code:EtlStatusEnum.SUCCESS.i18nKey)}";
        } else if (resultStatus == "${EtlStatusEnum.RUNNING}") {
            $(statusContentClass).removeClass("md-check");
            $(statusContentClass).removeClass("md");
            $(statusContentClass).addClass("fa");
            $(statusContentClass).addClass("fa-warning");
            $(statusContentClass).wrap("<span style='color: #d58512'></span>");
            status = "${g.message(code:EtlStatusEnum.RUNNING.i18nKey)}";
        } else {
            $(statusContentClass).removeClass("md-check");
            $(statusContentClass).removeClass("md");
            $(statusContentClass).addClass("fa");
            $(statusContentClass).addClass("fa-exclamation-circle");
            $(statusContentClass).wrap("<span style='color: red'></span>");
            if (resultStatus == "${EtlStatusEnum.FAILED}") status = "${g.message(code:EtlStatusEnum.FAILED.i18nKey)}";
            if (resultStatus == "${EtlStatusEnum.ETL_INITIATED}") status = "${g.message(code:EtlStatusEnum.ETL_INITIATED.i18nKey)}";
            if (resultStatus == "${EtlStatusEnum.ETL_PAUSED}") status = "${g.message(code:EtlStatusEnum.ETL_PAUSED.i18nKey)}";
            if (resultStatus == "${EtlStatusEnum.ETL_STOPPED}") status = "${g.message(code:EtlStatusEnum.ETL_STOPPED.i18nKey)}";
            if (resultStatus == "${EtlStatusEnum.ETL_NOT_STARTED}") status = "${g.message(code:EtlStatusEnum.ETL_NOT_STARTED.i18nKey)}";
        }
        if (status && status != "")
            $(statusContentId).html(status);
        $(lastRunContent).html(resultLastRun);
    }
</script>