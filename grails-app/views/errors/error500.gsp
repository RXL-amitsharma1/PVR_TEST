<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="error"/>
    <title><g:message code="error.500.title"/></title>
</head>

<body class="pv-bi">

<div class="page-header">
    <h1><g:message code="error.500.title"/></h1>
</div>

<div style="margin-bottom: 20px">
    <g:message code="error.500.message"/>
</div>

<g:link controller="dashboard" action="index">
    <button type="button" class="btn btn-primary"><g:message
            code="error.500.message.go.to.dashboard"/></button>
</g:link>

<g:if test="${grails.util.Holders.config.grails.show.error500.stackTraceButton}">
    <sec:ifAnyGranted roles="ROLE_DEV">
        <div class="stacktrace margin20Top">
            <g:renderException exception="${exception}"/>
        </div>

        <script>
            $(function () {
                $(".stacktrace").css("display", "block");
                $("h2, pre, .stacktrace h1, .error-details").hide();
                $(".stacktrace").before('<button type="button" class="btn btn-primary" id="toggleButton">View Details</button>');
                $("#toggleButton").on('click', function () {
                    $("h2, pre, .error-details").toggle();
                });
            });
        </script>
    </sec:ifAnyGranted>
</g:if>

</body>
</html>