<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.TaskLibrary.title" /></title>
    <asset:javascript src="app/task/task.js"/>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code:"app.label.tasks")}">



    <div>
        <g:message code="app.label.tasks" />
    </div>
</rx:container>
</body>
</html>
