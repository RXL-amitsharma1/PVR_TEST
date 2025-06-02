<%@ page import="com.rxlogix.user.UserRole" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.group.label')}"/>
    <title><g:message code="app.userGroupManagement.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        var USERGROUP = {
             listUrl: "${createLink(controller: 'userGroupRest', action: 'index')}",
             deleteUrl: "${createLink(controller: 'userGroup', action: 'delete')}",
             editUrl: "${createLink(controller: 'userGroup', action: 'edit')}",
             viewUrl: "${createLink(controller: 'userGroup', action: 'show')}"
        },
        querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?notBlank=true",
        queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}"
    </g:javascript>
    <asset:javascript src="app/userGroup/user-group.js"/>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${userGroupInstanceList}" var="theInstance"/>
<rx:container title="${message(code: "app.label.userGroupManagement")}" options="true">
    <div class="body">
        <div id="action-list-conainter" class="list">
        <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-right:15px;">
        <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["userGroup", "create"]}'>
            <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.usergroup.create')}" style="color: #353d43;"></i>
        </a>

    </div>
    <div class="pv-caselist">
        <table id="rxTableUserGroup" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th class="userGroupNameColumn"><g:message code="userGroup.name.label"/></th>
                <th class="userGroupDescriptionColumn"><g:message code="userGroup.description.label"/></th>
                <th><g:message code="userGroup.field.profile.label"/></th>
                <th><g:message code="userGroup.table.lastUpdatedDate.label"/></th>
                <th><g:message code="userGroup.table.createdDate.label"/></th>
                <th><g:message code="userGroup.table.createdBy.label"/></th>
                <th style="width: 80px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
    </div>
    </div>
</rx:container>
        </div>
    </div>
</div>
</body>
</html>
