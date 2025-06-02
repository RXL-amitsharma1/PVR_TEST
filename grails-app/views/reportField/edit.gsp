<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.reportField.appName")}"/>
    <title><g:message code="default.edit.title" args="[entityName]"/></title>
    <asset:javascript src="app/reportField/report-field.js"/>
</head>

<body>
<div class="content ">
    <div class="container ">
            <rx:container title="${message(code: "default.edit.label", args: [entityName])}" bean="${reportFieldInstance}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFieldInstance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="reportField" action="update" method="post">
            <g:hiddenField name="id" value="${reportFieldInstance.id}"/>

            <g:render template="includes/form-edit"
                      model="[reportFieldInstance: reportFieldInstance, fields: fields, selectedLocale: selectedLocale, fieldGroup: fieldGroup, isCentralField:isCentralField]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["reportField", "index"]}'
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