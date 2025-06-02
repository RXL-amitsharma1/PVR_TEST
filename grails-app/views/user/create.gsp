<%@ page import="grails.converters.JSON" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="app.user.create.title"/></title>
    <g:javascript>
        var LDAPSEARCH = {
             ajaxLdapSearchUrl: "${createLink(controller: 'user', action: 'ajaxLdapSearch')}"
        };
        var TenantSearch = {
            availableTenantsUrl: "${createLink(controller: 'user', action: 'availableTenants')}",
            selectedValues: "${(userInstance?.tenants?.collect { [id: it.id, name: it.displayName] } ?: []) as JSON}"
        };
    </g:javascript>
</head>
<body>
<div class="content">
    <div class="container">
        <div class="row">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h4 class="page-header-settings"><g:message code="default.create.label" args="[entityName]"/></h4>
                            </div>
                        </div>
                    </div>
                </div>
        </div>
        <div  class="settings-content pt-15">

            <rx:container title="${message(code: "app.label.userManagement")}">



    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>
     <div class="container">
      <div class=row">
               <div class="col-sm-12">
       <g:form method="post" action="save" class="form-horizontal">
        <g:render template="form" model="[userInstance: userInstance]"/>

        <div class="buttonBar text-right">
            <button name="edit" class="btn btn-primary">
                <span class="glyphicon glyphicon-ok icon-white"></span>
                ${message(code: 'default.button.save.label')}
            </button>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["user", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
       </g:form>
       </div>
      </div>
    </div>

</rx:container>
        </div>
    </div>
</div>
<asset:javascript src="app/user/user_edit.js" />
</body>
</html>
