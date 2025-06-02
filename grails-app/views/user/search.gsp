<!doctype html>
<html>
<head>
  <meta name="layout" content="main">
  <g:set var="entityName" value="${message(code: 'user.label')}" />
    <title><g:message code="app.userManagement.title"/></title>
  <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
  <asset:javascript src="app/dataTablesActionButtons.js"/>
  <style>
  .dt-layout-row:first-child {
    margin-top:0px;
  }
  #userSearchForm div.row {
    margin-left: 0;
    margin-right: 0;
  }
  </style>
  <g:javascript>
        var USERSEARCH = {
             listUrl: "${createLink(controller: 'userRest', action: 'index')}",
             editUrl: "${createLink(controller: 'user', action: 'edit')}",
             viewUrl: "${createLink(controller: 'user', action: 'show')}"
        }
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
  </g:javascript>
  <asset:javascript src="app/user/userlist.js"/>
</head>

<body>
<div class="content">
  <div class="container ">
    <div>
      <rx:container title="${message(code: "users.label")}">
<div id="list" role="main" class="pv-caselist">

  <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>

  %{--Search Form--}%
  <g:render template="searchForm" bean="${userInstance}" var="userInstance"/>

  <div class="horizontalRuleFull"></div>

  <h3><g:message code="app.label.searchResults"/></h3>

  %{--Search Results--}%
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
</rx:container>
    </div>
  </div>
</div>
</body>
</html>
