<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.localization")}"/>
    <title><g:message code="default.edit.title" args="[entityName]"/></title>
    <asset:javascript src="app/localization.js"/>
</head>
<body>
<div class="content ">
    <div class="container ">
        <rx:container title="${message(code: "default.edit.label", args:[entityName])}">

            <g:render template="/includes/layout/flashErrorsDivs" bean="${locInstance}" var="theInstance"/>
            <div class="container-fluid">
                <g:form method="post" action="update" class="form-horizontal">
                    <g:hiddenField name="id" value="${locInstance.id}"/>

                    <g:render template="includes/form"
                              model="[locInstance: locInstance, editMode: true]"/>

                    <div class="row">
                        <div class="col-xs-12">
                            <div class="pull-right">
                                <g:actionSubmit class="btn btn-primary" action="update"
                                                value="${message(code: 'default.button.update.label')}"/>
                                <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["localization", "index"]}'
                                        id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                            </div>
                        </div>
                    </div>
                </g:form>
            </div>
        </rx:container>
    </div>
</div>
</body>
</html>