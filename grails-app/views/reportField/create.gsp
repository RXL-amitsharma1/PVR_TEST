<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.reportField.appName")}"/>
    <title><g:message code="default.create.title" args="[entityName]"/></title>
    <asset:javascript src="app/reportField/report-field.js"/>
</head>
      <body>
            <div class="content ">
                <div class="container ">
<rx:container title="${message(code: "default.create.label", args:[entityName])}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFieldInstance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="save" class="form-horizontal">

            <g:render template="includes/form"
                      model="[reportFieldInstance: reportFieldInstance,fields: fields, selectedLocale: selectedLocale, fieldGroup:fieldGroup]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.save.label')}"/>
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