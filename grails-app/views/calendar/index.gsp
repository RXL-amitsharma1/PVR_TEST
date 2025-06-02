<%@ page import="com.rxlogix.CalendarEventDTO; com.rxlogix.util.DateUtil; com.rxlogix.CalendarService" contentType="text/html;charset=UTF-8" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.reports.calendar.title"/></title>

    <asset:javascript src="vendorUi/fullcalendar/fullcalendar.min.js"/>
    <asset:javascript src="vendorUi/fullcalendar/fullcalendar-lang-all.js"/>

    <asset:stylesheet src="vendorUi/fullcalendar/fullcalendar.min.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar/fullcalendar.print.css" media="print"/>

    <script>
        var eventsUrl = "${createLink(controller: "calendar", action: "events")}";
        var createReportRequestUrl = "${createLink(controller: 'reportRequest', action: 'create')}";
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var deleteActionItemUrl = "${createLink(controller: 'actionItem', action: 'delete')}";
        var reportRequestShowURL = "${createLink(controller: 'reportRequest', action: 'show')}";
        var executedReportShowURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var adhocReportShowURL = "${createLink(controller: 'configuration', action: 'view')}";
        var periodicReportShowURL = "${createLink(controller: 'periodicReport', action: 'view')}";
        var caseSeriesShowURL = "${createLink(controller: 'caseSeries', action: 'show')}";
        var executedCaseSeriesShowURL = "${createLink(controller: 'executedCaseSeries', action: 'show')}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var icsrReportShowURL = "${createLink(controller: "icsrReport", action: "view")}";
</script>

    <asset:javascript src="app/calendar.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>

</head>

<body>
<div class="container">
    <div class="alert alert-danger hide">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <strong><g:message code="app.label.icsr.error"/> !</strong> <span id="errorNotification"></span>
    </div>

    <div class="alert alert-success hide">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <strong><g:message code="app.label.success"/> !</strong> <span id="successNotification"></span>
    </div>
    <rx:container title="${message(code: "calendar.label")}">
        <div class="body">
            <div id="report-request-conainter" class="list pv-caselist">
                <div class="row">
                    <div class="alert alert-info hide calendar">
                        <g:message code="calendar.events.load.label" />
                    </div>

                    <div class="calendar" id="calendar">
                        <g:render template="/calendar/includes/calendarLegend"/>
                    </div>

                </div>
            </div>
        </div>
        <g:render template="/actionItem/includes/actionItemModal"/>
    </rx:container>
</div>
<g:render template="includes/createEventModal"/>
<g:render template="/includes/widgets/deleteRecord"/>
</body>
