<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'etlSchedule.label')}"/>
    <title><g:message code="app.etlSchedule.edit.title"/></title>
    <g:set var="userService" bean="userService"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/etlScheduler.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <script>
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
    </script>
</head>

<body>

<rx:container title="${message(code: "app.label.etlScheduler")}">

    <g:link controller="etlSchedule" action="index"><< <g:message code="app.label.etlStatus" /></g:link>

    <h4><g:message code="default.edit.label" args="[entityName]"/></h4>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${etlScheduleInstance}" var="theInstance"/>

    <g:form method="put" action="enable" class="form-horizontal" autocomplete="off">
        <g:hiddenField name="id" value="${etlScheduleInstance?.id}"/>
        <g:hiddenField name="version" value="${etlScheduleInstance?.version}"/>

        <g:render template="form" model="[etlScheduleInstance: etlScheduleInstance]"/>

            <button type="submit" class="btn btn-primary">${message(code: "etlSchedule.enable.label")}</button>
    </g:form>

</rx:container>

</body>
</html>


