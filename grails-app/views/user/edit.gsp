<%@ page import="grails.converters.JSON" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="app.user.edit.title"/></title>
    <g:javascript>
        var LDAPSEARCH = {
             ajaxLdapSearchUrl: "${createLink(controller: 'user', action: 'ajaxLdapSearch')}"
        };
        var TenantSearch = {
            availableTenantsUrl: "${createLink(controller: 'user', action: 'availableTenants')}",
            selectedValues: "${(userInstance?.tenants?.collect { [id: it.id, name: it.name] } ?: []) as JSON}"
        };
    </g:javascript>
</head>

<body>
<div class="content">

    <div class="container">
<div>
    <div class="col-sm-12">
        <div class="page-title-box">
            <div class="fixed-page-head">
                <div class="page-head-lt">
                    <div class="col-md-12">
                        <h5 class="page-header-settings"><g:message code="default.edit.label" args="[entityName]"/></h5>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>
<div  class="settings-content">

<rx:container title="${message(code: "app.label.userManagement")}">



    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>

    <g:form method="put" action="update" class="form-horizontal">
        <g:hiddenField name="id" value="${userInstance?.id}"/>
        <g:hiddenField name="version" value="${userInstance?.version}"/>

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

</rx:container>

</div>
<asset:javascript src="app/user/user_edit.js" />

<script type="javascript">
    var tokenGenerateUrl = "";
</script>
    </div>
</div>
</body>
</html>
