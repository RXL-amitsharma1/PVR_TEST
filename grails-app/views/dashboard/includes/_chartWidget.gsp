<%@ page import="com.rxlogix.ChartOptionsUtils; com.rxlogix.util.ViewHelper;" %>
<input type="hidden" id="isPVCModule" value="${ViewHelper.isPvcModule(request)}">
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:if test="${widget.executedConfiguration != null}">
            <g:link controller="report" title="${widget.title}" action="criteria"
                    params="[id: widget.executedConfiguration.id]"
                    class="rxmain-container-header-label rx-widget-title titleLink">${widget.title}</g:link>
        </g:if>
        <g:else>
            <span class="rxmain-container-header-label rx-widget-title" title="${widget.title}">${widget.title}</span>
        </g:else>

        <div id="runningMessage" style="display: ${!widget.running ? 'none' : 'block'}"><g:message code="app.widget.running.message"/></div>
        <a href="#" id="saveWidgetView${index}" data-url="removeWidgetUrl" data-params='{"widgetId": "${widget.reportWidget.id}"}'
           class="show-hide-table rxmain-container-header-label pull-right"
           data-toggle="tooltip" data-placement="bottom" title="Show/Hide Table"><i class="md-lg md-view-list"></i></a>
        <a href="#" id="saveWidgetViewIndicator${index}" data-params='{"widgetId": "${widget.reportWidget.id}"}'
           class="show-as-indicator rxmain-container-header-label pull-right"
           data-toggle="tooltip" data-placement="bottom" title="Show as indicators/Show standard"><i class="md-lg md-box-shadow"></i>
        </a>
        <a href="#" id="exportUrl${index}"
           class="show-external-link rxmain-container-header-label pull-right exportUrl"
           data-content="" data-trigger="click" data-placement="bottom"><i class="md-lg md-link"></i></a>
        <a href="#" class='rxmain-container-header-label pull-right dropdown-toggle' data-toggle="dropdown" title="${message(code: 'app.label.exportTo')}"><i class='md-lg md-export'></i>
        </a>
        <ul class="dropdown-menu pull-right export-type-list" id="exportTypes${index}" style="overflow-y: hidden;">
        </ul>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div id="container${index}" data-index="${index}" class="row rx-widget-content chart-container nicescroll"
         data-widget-id="${widget.reportWidget.id}"></div>

    <g:javascript>
        $(function () {
            $("#exportUrl${index}").on("click", function(e){
                e.preventDefault();
                $("#exportUrlField").val($(this).attr("data-content"));
                $("#exportUrlModal").modal("show");
            });

            $("#saveWidgetView${index}").on('click', function (e) {
                e.preventDefault();
                 var $table =$("#tableDiv${index}");
                 $table.toggle();
                 var hideTable =  $table.is(':hidden')
                 var displayLength = 5;
                 $table.closest(".grid-stack-item").trigger("resize");
                 if (isEditable) {
                     $.ajax({
                        url: "${createLink(controller: 'dashboard', action: 'updateWidgetSettings')}",
                        type: 'post',
                        data: {id:${widget.reportWidget.id}, data: JSON.stringify({hideTable:hideTable, displayLength:displayLength, showIndicators:false})}
                        }).done(function (data) {
                           if($("#indicator${index}").length>0)$('#container${index}').trigger('reload');
                           console.log("saved")
                        })
                        .fail(function (err) {
                            var mess = (err.responseJSON.message ? err.responseJSON.message : "") +
                                (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : "");
                            $container.find(".errorContent").html(mess);
                            $container.find(".errorDiv").show();
                        });
                 }
            }); 
            $("#saveWidgetViewIndicator${index}").click(function (e) {
                e.preventDefault();
                var showIndicators = $("#indicator${index}").length>0
                 if (isEditable) {
                     $.ajax({
                        url: "${createLink(controller: 'dashboard', action: 'updateWidgetSettings')}",
                        type: 'post',
                        data: {id:${widget.reportWidget.id}, data: JSON.stringify({hideTable:false, displayLength:5, showIndicators:!showIndicators})},
                     }).done(function (data) {
                           console.log("saved")
                           $('#container${index}').trigger('reload');
                     }).fail(function (err) {
                            var mess = (err.responseJSON.message ? err.responseJSON.message : "") +
                                (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : "");
                            $container.find(".errorContent").html(mess);
                            $container.find(".errorDiv").show();
                    });
                 }
            });
            $("#refresh-widget${index}").on('click', function () {
                if (isEditable) {
                    $("#selectedWidget").val("container${index}");
                    submitRefreshModal(CHART_WIDGET_ACTIONS.REFRESH, $(this).attr("data-id"));
                }
            });
            $("#edit-widget${index}").on('click', function (e) {
                e.preventDefault();
                var params = $(this).data("params");
                $.ajax({
                   url: reportConfigurationUrl+"?widgetId=${widget.reportWidget.id}" ,
                   dataType: "html"
                   })
                   .done(function (result) {
                        $("#chartConfigurationModalContent").empty().append(result);
                        $("#chartConfigurationModal").modal("show");
                        $("#selectedWidget").val("container${index}");
                        $(".dateRangeEnumClass").trigger("change");
                   })
                   .fail(function(err) {
                      console.error(err);
                      alert($.i18n._("app.response.status.false") + err.message);
               });
            });
        });
    </g:javascript>
</div>