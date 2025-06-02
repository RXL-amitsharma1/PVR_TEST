<%@ page import="com.rxlogix.enums.FrequencyEnum" %>
<!doctype html>
<html>
<head>
    <title><g:message code="app.viewErrorDetails" /></title>
    <meta name="layout" content="main">
    <asset:stylesheet src="errors.css"/>
</head>

<body>
<div class="col-md-12">
    <rx:container title="${message(code: "app.ExecutionStatus.error")}">
        <div class="row">
            <div class="col-lg-12">
                <div class="row">
                    <div class="col-lg-4">
                        <label><g:message code="app.label.PublisherTemplate.FileError" /></label>
                    </div>
                </div>

                <sec:ifAnyGranted roles="ROLE_DEV">
                    <div class="row">
                        <div class="col-lg-12">
                            <label><g:message code="app.label.stackTrace"/></label>
                            <div>
                                <div>
                                    <g:textArea name="text" class="error" value="${error}" readonly="true"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </sec:ifAnyGranted>
            </div>
        </div>
    </rx:container>
</div>

</body>
</html>