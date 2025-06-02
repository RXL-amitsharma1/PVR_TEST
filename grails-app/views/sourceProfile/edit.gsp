<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="Workflow State"/>
    <title><g:message code="app.sourceProfile.edit.title"/></title>
</head>

<body>

<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.sourceProfile.label")}" options="${true}">

                <g:render template="/includes/layout/flashErrorsDivs" bean="${sourceProfileInstance}" var="theInstance"/>

                <g:form action="update" class="form-horizontal">
                    <g:render template="includes/form" model="['mode': 'show', sourceProfileInstance: sourceProfileInstance]"/>
                    <g:hiddenField name="id" value="${sourceProfileInstance?.id}"/>

                    <div class="buttonBar">
                        <div class="text-right">
                            <button name="edit" class="btn btn-primary">
                                <span><g:message code="default.button.update.label"/></span>
                            </button>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["sourceProfile", "index"]}'
                                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                        </div>
                    </div>
                </g:form>

            </rx:container>
        </div>
    </div>
</div>

</body>
</html>