<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.PublisherCommonParameter.edit.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.PublisherCommonParameter.edit")}" bean="${instance}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form  action="update" method="post">
            <g:hiddenField name="id" value="${instance.id}"/>

            <g:render template="includes/form" model="['instance': instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${instance?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["publisherCommonParameter", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>