<%@ page import="com.rxlogix.user.User; com.rxlogix.user.UserRole" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'role.label')}"/>
    <title><g:message code="role.label"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        var USERSEARCH = {
             listUrl: "${createLink(controller: 'userRest', action: 'index')}",
             editUrl: "${createLink(controller: 'user', action: 'edit')}",
             viewUrl: "${createLink(controller: 'user', action: 'show')}"
        }
    </g:javascript>
    <asset:javascript src="app/user/userlist.js"/>
</head>

<body>
<div class="content">
    <div class="container">
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<rx:container title="${message(code: "app.label.roleManagement")}">
    <div class="row breadcrumbs">
           <div class="col-md-6">
    <g:link action="index"><< Roles List</g:link>
            </div>
    </div>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${roleInstance}" var="theInstance"/>

    <div class="row">
        <div class="col-md-6">

            <h3 class="sectionHeader">Role Details</h3>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="role.authority.label"/></label></div>

                <div class="col-md-${column2Width}"><g:message code="app.role.${roleInstance.authority}" default="${roleInstance.authority}"/></div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="role.description.label"/></label></div>

                <div class="col-md-${column2Width}">${roleInstance.description}</div>
            </div>

        </div>
    </div>

    <div class="margin20Top"></div>

    <div class="horizontalRuleFull"></div>

    %{--Search Results--}%
    <form id="userSearchForm">
        %{--to filter users for current role --}%
        <input type="hidden" name="roles" value="${roleInstance.id}">
    </form>
    <div class="pv-caselist">
        <table id="rxUserSearchResultsTable" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="user.username.label"/></th>
                <th><g:message code="user.fullName.label"/></th>
                <th><g:message code="user.email.label"/></th>
                <th><g:message code="user.enabled.label"/></th>
                <th><g:message code="user.lastLogin.label"/></th>
                <th><g:message code="roles.label"/></th>
                <th><g:message code="userGroup.label"/></th>
                <th style="width: 80px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>

    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${roleInstance}" var="theInstance"/>

</rx:container>
    </div>
</div>
</body>
</html>
