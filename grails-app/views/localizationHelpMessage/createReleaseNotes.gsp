<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.label.releaseNotes.create.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.localizationHelp.createReleaseNote")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="saveReleaseNotes" class="form-horizontal">

            <g:render template="includes/formReleaseNote"
                      model=" [instance: instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="saveReleaseNotes"
                                        value="${message(code: 'default.button.save.label')}"/>
                        <a class="btn pv-btn-grey"
                           href="${createLink(controller: 'localizationHelpMessage', action: 'releaseNotes')}"><g:message
                                code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>