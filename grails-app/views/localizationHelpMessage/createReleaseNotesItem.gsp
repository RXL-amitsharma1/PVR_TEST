<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.label.releaseNotes.create.title"/> <g:message code="app.label.releaseNotesItem.item"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.localizationHelp.createReleaseNote")} ${message(code: "app.label.releaseNotesItem.item")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="saveReleaseNotesItem" class="form-horizontal">

            <g:render template="includes/formReleaseNoteItem" model=" [instance: instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="saveReleaseNotesItem"
                                        value="${message(code: 'default.button.save.label')}"/>
                        <a class="btn pv-btn-grey"
                           href="${createLink(controller: 'localizationHelpMessage', action: 'viewReleaseNotes')}?id=${instance.releaseNotes.id}"><g:message
                                code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>