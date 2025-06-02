<html>
<head>
    <meta name="layout" content="main">
       <title><g:message code="app.PublisherCommonParameter.create.title"/></title>
</head>

<body>
<div class="content">
    <div class="container ">
        <div class="row page-space m-t-5">
            <rx:container title="${message(code: "app.label.PublisherCommonParameter.createOne")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="save" class="form-horizontal">

            <g:render template="includes/form"
                      model="[instance: instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.save.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["publisherCommonParameter", "index"]}'
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