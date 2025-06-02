<%@ page import="com.rxlogix.localization.HelpLink" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.label.localizationHelp.helpLink.title"/></title>

</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.localizationHelp.list")}">
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation"><a href="${createLink(action: "index")}"><g:message code="app.label.localizationHelp.helpMessages"/></a>
                    </li>
                    <li role="presentation"><a href="${createLink(action: "releaseNotes")}"><g:message code="app.label.localizationHelp.releaseNotes"/></a>
                    </li>
                    <li role="presentation"><a href="${createLink(action: "interactiveHelp")}"><g:message code="app.label.interactiveHelp.interactiveHelp"/></a>
                    </li>
                    <li role="presentation"><a href="${createLink(action: "systemNotification")}"><g:message code="app.label.systemNotification.systemNotifications"/></a>
                    </li>
                    <li role="presentation" class="active"><a href="${createLink(action: "helpLink")}"><g:message code="app.label.localizationHelp.helpLink"/></a>
                    </li>
                </ul>
                <br>
                <div class="body">
                    <div id="action-list-conainter" class="list pv-caselist">

                        <g:render template="/includes/layout/flashErrorsDivs" bean="${helpMessage}" var="theInstance"/>
                        <div class="row">
                            <div class="col-md-6">
                                <form>

                                    <div class="input-group">
                                        <input type="text" class="form-control" name="link" value="${com.rxlogix.localization.HelpLink.getDefaultHelpLink()}">
                                        <label class="input-group-btn">
                                            <button type="submit" class="btn btn-primary" style="height: 24px">
                                                <g:message code="app.odataConfig.update"/>

                                            </button>
                                        </label>
                                    </div>

                                </form>
                            </div>
                        </div>
                    </div>

                </div>
            </rx:container>
            <g:form controller="${controller}" method="delete">
                <g:render template="/includes/widgets/deleteRecord"/>
            </g:form>
        </div>
    </div>
</div>

<g:render template="/oneDrive/downloadModal" model="[select: true]"/>

</body>
</html>