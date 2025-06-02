<%@ page import="com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.config.publisher.GanttItem; com.rxlogix.GanttService" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.submissionScheduler.pageTitle"/></title>
    <asset:javascript src="/gantt/jsgantt.js"/>

    <g:javascript>
        var ganttUrl= "${createLink(controller: 'gantt', action: 'ganttAjax')}?pvp=" + (sessionStorage.getItem("module")=="pvp");
        var changeDependenceUrl= "${createLink(controller: 'gantt', action: 'changeDependence')}";

        <g:applyCodec encodeAs="none">
            var TASK_TYPES = ${GanttItem.TaskType.list().encodeAsJSON()};
        </g:applyCodec>

    </g:javascript>
    <asset:stylesheet src="jsgantt.css"/>
    <asset:javascript src="/app/publisher/gantt.js"/>

</head>

<body>
<rx:container title="${message(code: "app.label.submissionScheduler.title")}" options="true" filterButton="true">
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <g:render template="includes/gantChart"/>
</rx:container>

</body>
</html>