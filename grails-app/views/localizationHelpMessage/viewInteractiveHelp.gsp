<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.interactiveHelp.view.title"/></title>

</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.interactiveHelp.view")}">

                <div class="body">
                    <div>

                        <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
                        <div class="row">
                            <div class="col-md-12">
                                <h2>${instance.title ?: ""}</h2>
                                <g:message code="app.label.systemNotification.published"/>:${instance.published ? message(code: "default.button.yes.label") : message(code: "default.button.no.label")}<br>
                                <g:message code="app.label.interactiveHelp.pages"/>:${instance.page}<br>

                                <textarea style="width: 100%; height: 500px">
                                ${raw(instance.description)}
                                </textarea>

                            </div>

                        </div>
                        <a class="btn btn-primary" href="${createLink(controller: 'localizationHelpMessage', action: 'editInteractiveHelp')}?id=${instance.id}"><g:message code="default.button.edit.label"/></a>
                        <a class="btn pv-btn-grey" href="${createLink(controller: 'localizationHelpMessage', action: 'interactiveHelp')}"><g:message code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </rx:container>

        </div>
    </div>
</div>

</body>
</html>