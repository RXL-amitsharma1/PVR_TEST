<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.reportFooter.appName")}"/>
    <title><g:message code="app.report.footer.create.title"/></title>
</head>

<body>
<div class="content">
    <div class="container ">
        <div>
            <rx:container title="${message(code: "app.label.createReportFooter")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFooterInstance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="save" class="form-horizontal">

            <g:render template="includes/form"
                      model="[reportFooterInstance: reportFooterInstance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.save.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["reportFooter", "index"]}'
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