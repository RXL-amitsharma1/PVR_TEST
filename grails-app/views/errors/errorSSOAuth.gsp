<!doctype html>
<html>
<head>
    <meta name="layout" content="error"/>
    <title><g:message code="app.error.sso.title"/></title>
</head>

<body class="pv-bi">

<div class="page-header">
    <h1><g:message code="error.sso.title"/></h1>
</div>

<div style="margin-bottom: 20px">
    ${msg}
</div>

<g:link controller="dashboard" action="index">
    <button type="button" class="btn btn-primary"><g:message
            code="error.500.message.go.to.dashboard"/></button>
</g:link>
</body>
</html>