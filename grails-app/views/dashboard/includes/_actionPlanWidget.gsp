<%@ page import="com.rxlogix.enums.ReasonOfDelayAppEnum; com.rxlogix.util.DateUtil; grails.converters.JSON" %>
<asset:javascript src="app/rxTitleOptions.js"/>
<div class="grid-stack-item-content rx-widget panel actionPlanWidgetContainer">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="${module}" action="actionPlan" title="${message(code: 'app.actionPlan.actionPlan')}"
                class="rxmain-container-header-label rx-widget-title"><g:message
                code="app.actionPlan.actionPlan"/></g:link>
        <i class="pull-right dropdown-toggle md md-list md-lg rxmain-dropdown-settings" id="dropdownMenu1"
           data-toggle="dropdown"></i>

        <div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu1">
            <div class="rxmain-container-dropdown">
                <table id="tableColumns" class="table table-condensed rxmain-dropdown-settings-table">
                    <thead>
                    <tr>
                        <th><g:message code='app.label.name'/></th>
                        <th><g:message code='app.label.show'/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content nicescroll pv-caselist actionPlanWidgetContent${index}" >
        <div >
            <span class="actionPlanWidgetTitle${index}"><span class="rrTitleContent">${message(code: "app.widget.reportRequest.no.title")}</span><span class="fa fa-edit rrTitleIcon"></span> </span>
        <span class="actionPlanWidgetSearchForm" style="display: none">
            <div><input  class="form-control" maxlength="255" width="100%" name="rrTitle" placeholder="${message(code: "placeholder.templateQuery.title")}"></div>
        </span>
        </div>
        <g:render template="/${module}/includes/actionPlan" model="[isEditable: isEditable, module: module]"/>

    </div>
</div>
<g:render template="/${module}/includes/actionPlanCaseListModal"/>
<g:render template="/includes/widgets/confirmation"/>
<input type="hidden" id="widgetSettings${index}" value="${widget.reportWidget.settings}"/>
<script>

    $(function () {

        function init() {
            var settingsString = $("#widgetSettings${index}").val();
            $(".actionPlanWidgetSearchForm").hide();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".actionPlanWidgetSearchForm");
                if (settings.title) {
                    $container.find('input[name=rrTitle]').val(settings.title);
                    $('.actionPlanWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
                }
                if (settings.primaryOnly) $("#primaryOnly").prop('checked', true);
                if (settings.groupBy) $("#groupBy").val(settings.groupBy);
                if (settings.topValues) $("#topValues").val(settings.topValues);
                $("#responsiblePartyFilter").val(settings.responsiblePartyFilter);
                $("#observationFilter").val(settings.observationFilter);
                $("#issueTypeFilter").val(settings.issueTypeFilter);
                $("#errorTypeFilter").val(settings.errorTypeFilter);
                $("#workflowFilter").val(settings.workflowFilter);
                $("#destinationFilter").val(settings.destinationFilter);
                $("#lateFilter").val(settings.lateFilter);
                if (settings.lastX) $("#lastX").val(settings.lastX);
                if (settings.dateRangeType) $("#dateRangeType").val(settings.dateRangeType);
                if (settings.periodsNumber) $("#periodsNumber").val(settings.periodsNumber);
                if (settings.dateRangeFrom) $("#dateRangeFrom").val(settings.dateRangeFrom);
                if (settings.dateRangeTo) $("#dateRangeTo").val(settings.dateRangeTo);
            }
        }

        init();

        function appendToLabel($label, val) {
            $label.html($label.text() + " (<span id='" + $label.attr("id") + "_val'>" + val + "</span>)");
        }

        $(".actionPlanWidgetTitle${index}").on('click', function () {
            $(".actionPlanWidgetSearchForm").show();
            $(".actionPlanWidgetTitle${index}").hide();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().show();
        });

        $(".actionPlanWidgetHideButton").on('click', function () {
            $(".actionPlanWidgetSearchForm").hide();
            $(".actionPlanWidgetTitle${index}").show();
        });

        $(".saveActionPlanWidget").on('click', function () {
            var errorMessage = validateInput($('.actionPlanWidgetContent${index}'));
            if (errorMessage) {
                $('#warningModal .description').text(errorMessage);
                $('#warningModal .modal-title').text("Errors");
                $("#warningModal").modal('show');
                return;
            }
            var $container = $(this).parent();
            var params = $('#criteriaForm').serializeArray();
            var settings = {title: $(".actionPlanWidgetSearchForm input").val()}
            for (var i = 0; i < params.length; i++) {
                if (settings[params[i].name])
                    settings[params[i].name] = settings[params[i].name] + ";" + params[i].value;
                else
                    settings[params[i].name] = params[i].value;
            }
            var settingsString = JSON.stringify(settings);
            $("#widgetSettings${index}").val(settingsString);
            $.ajax({
                url: "${createLink(controller: 'dashboard', action: 'updateWidgetSettings')}",
                type: 'post',
                data: {id:${widget.reportWidget.id}, data: settingsString},
                dataType: 'html'
            })
                .done(function (data) {
                    $container.parent().find(".successDiv").show();
                    setTimeout(function () {
                        $container.parent().find(".successDiv").hide();
                    }, 2000);
                    $('.actionPlanWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
                    createActionPlanTable($('#periodsNumber').val());
                })
                .fail(function (err) {
                        var mess = (err.responseJSON.message ? err.responseJSON.message : "") +
                            (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : "");
                        $container.parent().find(".errorContent").html(mess);
                        $container.parent().find(".errorDiv").show();
                    }
                );
        });

        $('#refresh-widget${index}').hide();
        $('.actionPlanWidgetContainer .rxmain-dropdown-settings').hide();
    });
</script>