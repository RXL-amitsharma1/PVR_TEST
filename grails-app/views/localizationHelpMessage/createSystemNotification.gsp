<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.label.systemNotification.create.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.systemNotification.create")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="saveSystemNotification" class="form-horizontal">

            <g:render template="includes/formSystemNotification"
                      model=" [instance: instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="saveSystemNotification"
                                        value="${message(code: 'default.button.save.label')}"/>
                        <a class="btn pv-btn-grey"
                           href="${createLink(controller: 'localizationHelpMessage', action: 'systemNotification')}"><g:message
                                code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>