<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.gantt.appName")}"/>
    <title><g:message code="app.gantt.show.title"/></title>
    <asset:javascript src="app/publisher/ganttTemplate.js"/>
</head>

<body>

<rx:container title="${message(code: 'app.label.gantt.appName')}">
    <input type="hidden" id="mode" value="readonly">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${gantt}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <g:render template="includes/form" model="['gantt': gantt]"/>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["gantt", "edit", {"id": ${gantt.id}}]}' id="${gantt.id}">
                        <g:message code="default.button.edit.label"/>
                    </button>
                    <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="gantt"
                            data-instanceid="${gantt.id}"
                            data-instancename="${gantt.name}"
                            class="btn btn-default"><g:message code="default.button.delete.label"/></button>
                </div>
            </div> 
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${gantt}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>