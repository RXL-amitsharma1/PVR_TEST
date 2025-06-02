<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.help.title" /></title>
    <asset:javascript src="app/help/help.js"/>
</head>

<body>
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <rx:container title="${message(code: "app.label.help")}">



        <div>
            <g:message code="app.label.help" />
        </div>
    </rx:container>
</body>