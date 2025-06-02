<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'cognosReport.label')}"/>
    <title><g:message code="app.cognosReport.create.title"/></title>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: "default.create.label", args:[entityName])} </h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="settings-content">

<rx:container title="${message(code: "app.label.cognosReport")}">



    <g:render template="/includes/layout/flashErrorsDivs" bean="${cognosReportInstance}" var="theInstance"/>

    <g:form method="post" action="save" class="form-horizontal">
        <g:render template="form" model="[cognosReportInstance: cognosReportInstance]"/>

        <div class="buttonBar">
            <button name="edit" class="btn btn-primary">
                <span class="glyphicon glyphicon-ok icon-white"></span>
                ${message(code: 'default.button.save.label')}
            </button>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["cognosReport", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </g:form>

</rx:container>
</div>
    </div>
</div>
</body>
</html>
