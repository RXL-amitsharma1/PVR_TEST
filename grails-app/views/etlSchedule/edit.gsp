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
<div class="content ">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.etlScheduler")}">

    <g:link controller="etlSchedule" action="index"><< <g:message code="app.label.etlStatus" /></g:link>

    <h4><g:message code="default.edit.label" args="[entityName]"/></h4>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${etlScheduleInstance}" var="theInstance"/>

    <g:form method="put" action="update" class="form-horizontal" autocomplete="off">
        <g:hiddenField name="id" value="${etlScheduleInstance?.id}"/>
        <g:hiddenField name="version" value="${etlScheduleInstance?.version}"/>

        <g:render template="form" model="[etlScheduleInstance: etlScheduleInstance]"/>

        <div class="buttonBar m-t-10">

          <g:if test="${etlScheduleInstance.isDisabled}">
                <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["etlSchedule", "enable"]}' id="enableBtn">
                    ${message(code: "etlSchedule.enable.label")}
                </button>
            </g:if>
            <g:else>
                <button name="edit" class="btn btn-primary">
                    <span class="glyphicon glyphicon-ok icon-white"></span>
                    ${message(code: 'default.button.update.label')}
                </button>
                <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["etlSchedule", "disable"]}' id="disableButton">
                    <g:message code="etlSchedule.disable.label"/>
                </button>
            </g:else>
        </div>
    </g:form>

</rx:container>
        </div>
    </div>
</div>
</body>
</html>


