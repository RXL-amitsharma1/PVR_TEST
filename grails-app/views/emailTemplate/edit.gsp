<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.emailTemplate.appName")}"/>
    <title><g:message code="app.emailTemplateConfiguration.edit.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.editemailTemplate")}" bean="${emailTemplate}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${emailTemplate}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="emailTemplate" action="update" method="post">
            <g:hiddenField name="id" value="${emailTemplate.id}"/>

            <g:render template="includes/form" model="['emailTemplate': emailTemplate]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${emailTemplate?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <a class="btn pv-btn-grey"
                           href="${createLink(controller: 'emailTemplate', action: 'index')}"><g:message
                                code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>