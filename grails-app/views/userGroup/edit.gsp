<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.group.label')}"/>
    <title><g:message code="app.userGroup.edit.title"/></title>
    <g:javascript>
        var USERGROUP = {
             ajaxProfileSearchUrl: "${createLink(controller: 'userGroup', action: 'ajaxProfileSearch')}",
             ajaxUserFetchUrl: "${createLink(controller: 'userGroup', action: 'ajaxFetchUsersByGroup', id: userGroupInstance?.id)}",
        },
        querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?notBlank=true",
        queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}",
        dashboardURL = "${createLink(controller: 'dashboard', action: 'index')}",
        dashboardDetailURL = "${createLink(controller: 'dashboard', action: 'getDashboardAjax')}";
        var managerIdsString = "${managers}";
    </g:javascript>
    <asset:javascript src="vendorUi/jquery/jquery.ui.widget.js"/>
    <asset:javascript src="vendorUi/jquery/jquery-picklist.js"/>
    <asset:stylesheet href="vendorUi/jquery-picklist.css"/>
    <asset:javascript src="app/userGroup/user-group.js"/>
%{--    <asset:stylesheet href="user-group.css"/>--}%
</head>`
<body>
<div class="content">
    <div class="container">
        <div class="col-md-12">
            <rx:container title="${message(code: "default.edit.label", args:[entityName])}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${userGroupInstance}" var="theInstance"/>
    <g:form method="put" action="update" params="['previousFieldId': userGroupInstance?.fieldProfile?.id]" class="form-horizontal">
        <g:hiddenField name="id" value="${userGroupInstance?.id}"/>
        <g:hiddenField name="version" value="${userGroupInstance?.version}"/>
        <g:hiddenField name="oldSourceProfileList" value="${userGroupInstance?.sourceProfiles}"/>
        <g:render template="form" model="[userGroupInstance: userGroupInstance,roleList:roleList,managers:managers,
                                          userGroupRoleList:userGroupRoleList, userGroupUserList:userGroupUserList, sourceProfileList: sourceProfileList]"/>
        <div class="buttonBar text-right">
            <button name="edit" class="btn btn-primary">
                <span class="glyphicon glyphicon-ok icon-white"></span>
                ${message(code: 'default.button.update.label')}
            </button>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["userGroup", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </g:form>
</rx:container>
        </div>
    </div>
</div>
<g:render template="addRemoveUser" model="[allowedUsers:userGroupUserList, allUserList: allUserList]"/>
</body>
</html>
