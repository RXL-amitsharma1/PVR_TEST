<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.label.releaseNotes.edit.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.releaseNotes.edit")}" bean="${instance}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="localizationHelpMessage" action="updateReleaseNotes" method="post" >
            <g:hiddenField name="id" value="${instance.id}"/>
            <g:hiddenField name="version" id="version" value="${instance?.version}"/>

            <g:render template="includes/formReleaseNote" model=" [instance: instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">

                        <g:actionSubmit class="btn btn-primary" action="updateReleaseNotes"
                                        value="${message(code: 'default.button.update.label')}"/>
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