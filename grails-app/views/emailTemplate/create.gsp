<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.emailTemplate.appName")}"/>
    <title><g:message code="app.emailTemplateConfiguration.create.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.createemailTemplate")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${emailTemplate}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="save" class="form-horizontal">

            <g:render template="includes/form"
                      model="[emailTemplate: emailTemplate]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.save.label')}"/>
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