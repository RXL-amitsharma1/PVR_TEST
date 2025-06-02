<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.label.localizationHelp.edit.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.localizationHelp.edit")}" bean="${helpMessage}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${helpMessage}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="localizationHelpMessage" action="update" method="post">
            <g:hiddenField name="id" value="${helpMessage.id}"/>
            <g:hiddenField name="version" id="version" value="${helpMessage?.version}"/>

            <g:render template="includes/form" model=" [localization: localization, helpMessage: helpMessage]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">

                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <a class="btn pv-btn-grey"
                           href="${createLink(controller: 'localizationHelpMessage', action: 'index')}"><g:message
                                code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>