<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.customField.appName")}"/>
    <title><g:message code="default.edit.title" args="[entityName]"/></title>
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="select2/select2-treeview.js"/>
    <asset:javascript src="app/customField.js"/>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <rx:container title="${message(code: "default.edit.label", args:[entityName])}" bean="${customFieldInstance}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${customFieldInstance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="customField" action="update" method="post">
            <g:hiddenField name="id" value="${customFieldInstance.id}"/>

            <g:render template="includes/form" model="[customFieldInstance: customFieldInstance, fields: fields, selectedLocale: selectedLocale, fieldGroup: fieldGroup]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${customFieldInstance?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["customField", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>
        </div>
    </div>
</div>
</body>
</html>
