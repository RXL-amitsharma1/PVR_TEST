<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.customField.appName")}"/>
    <title><g:message code="default.create.title" args="[entityName]"/></title>
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="vendorUi/select2/select2-treeview.js"/>
    <asset:javascript src="app/customField.js"/>
</head>

<body>
<div class="content">
    <div class="container ">
            <rx:container title="${message(code: "default.create.label", args:[entityName])}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${customFieldInstance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="save" class="form-horizontal">

            <g:render template="includes/form"
                      model="[customFieldInstance: customFieldInstance,fields: fields, selectedLocale: selectedLocale, fieldGroup:fieldGroup]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.save.label')}"/>
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
</body>
</html>
