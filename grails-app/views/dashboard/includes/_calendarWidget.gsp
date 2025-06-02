<%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
            <g:link controller="calendar" action="index"
            title="${message(code: 'default.button.addCalendarWidget.label')}" class="rxmain-container-header-label rx-widget-title">${message(code: 'default.button.addCalendarWidget.label')}</g:link>
            <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div id="container${index}" class="row rx-widget-content nicescroll">
        <div class="widget-calendar" id="calendar${index}" data-show-reload-button="false">
            <g:render template="/calendar/includes/calendarLegend"/>
        </div>
    </div>

    <script>
        $(function () {
            $('#refresh-widget${index}').hide();
            $('#calendar${index}').fullCalendar('option', 'header', {
                left: 'prev,next today',
                center: 'title',
                right: 'month,basicWeek,basicDay'
            });
        })
    </script>
</div>
