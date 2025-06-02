<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.email.appName")}"/>
    <title><g:message code="app.emailConfiguration.show.title"/></title>
</head>

<body>

<rx:container title="${message(code: 'app.label.viewemail')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${email}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-6">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.email.email"/></label>

                                <div>${email.email}</div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.email.description"/></label>

                                <div>${email.description}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["email", "edit", {"id": ${email.id}}]}' id="${email.id}">
                        <g:message code="default.button.edit.label"/>
                    </button>
                    <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="email"
                            data-instanceid="${email.id}"
                            data-instancename="${email.email}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
                </div>
            </div>
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${email}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>