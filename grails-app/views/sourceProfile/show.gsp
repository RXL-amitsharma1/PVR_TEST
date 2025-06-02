<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.sourceProfile.show.title"/></title>
    <script type="text/javascript">
        $(function () {
            $('.sourceProfileField').attr("disabled", true);
        })
    </script>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
<rx:container title="${message(code: "app.sourceProfile.label")}">

    <g:render template="includes/form" model="['mode': 'show', sourceProfileInstance: sourceProfileInstance]"/>

    <div class="buttonBar">
        <div class="text-right">
            <g:link controller="sourceProfile" action="edit" id="${sourceProfileInstance.id}"
                    class="btn btn-primary"><g:message code="default.button.edit.label"/></g:link>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["sourceProfile", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </div>

</rx:container>
        </div>
    </div>
</div>
</body>
</html>