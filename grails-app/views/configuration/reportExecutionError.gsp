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
        <g:if test="${isInbound}">
            <div class="row">
                <div class="col-lg-12">
                    <div class="row">
                        <div class="col-lg-4">
                            <label><g:message code="app.label.sender.name" /></label>
                            <div>
                                ${exStatus?.senderName}
                            </div>
                        </div>
                        <div class="col-lg-4">
                            <label><g:message code="app.label.status" /></label>
                            <div>
                                ${exStatus?.status}
                            </div>
                        </div>
                    </div>
                    <sec:ifAnyGranted roles="ROLE_DEV">
                        <div class="row">
                            <div class="col-lg-12">
                                <label><g:message code="app.label.stackTrace"/></label>
                                <div>
                                    <div>
                                        <g:textArea name="text" class="error" value="${exStatus?.errorDetails}" readonly="true"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </sec:ifAnyGranted>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="row">
                <div class="col-lg-12">
                    <div class="row">
                        <div class="col-lg-4">
                            <label><g:message code="app.label.reportName" /></label>
                            <div>
                                ${exStatus?.reportName}
                            </div>

                        </div>
                        <div class="col-lg-4">
                            <label><g:message code="app.label.sectionTitle"/></label>
                            <div>
                                ${exStatus?.sectionName}
                            </div>

                        </div>
                        <div class="col-lg-4">
                            <label><g:message code="app.label.reportVersion" /></label>
                            <div>
                                ${exStatus?.reportVersion}
                            </div>

                        </div>
                    </div>

                    <div class="row">
                        <div class="col-lg-4">
                            <label><g:message code="app.label.runDate" /></label>
                            <div>
                                ${exStatus?.nextRunDate}
                            </div>

                        </div>
                        <div class="col-lg-4">
                            <label><g:message code="app.label.frequency"/></label>
                            <div>
                                ${(exStatus?.frequency).value()}
                            </div>

                        </div>

                    </div>

                    <sec:ifAnyGranted roles="ROLE_DEV">
                        <div class="row">
                            <div class="col-lg-12">
                                <label><g:message code="app.label.stackTrace"/></label>
                                <div>
                                    <div>
                                        <span>${exStatus?.message}</span>
                                    </div>
                                    <div>
                                        <g:textArea name="text" class="error" value="${exStatus?.stackTrace}" readonly="true"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </sec:ifAnyGranted>
                </div>
            </div>
        </g:else>
    </rx:container>
</div>

</body>
</html>