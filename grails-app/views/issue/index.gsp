<%@ page import="com.rxlogix.config.Capa8D;" %>
<html>
<head>

    <asset:stylesheet src="quality.css"/>

    <title><g:message code="app.quality.title.quality.issue.list"/></title>

    <meta name="layout" content="main"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>

    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="app/quality/capa_list.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/quality/copyPasteValues.js"/>
    <script>
        var listCapaUrl="${createLink(controller: 'issue', action: 'ajaxList')}";
        var editUrl = "${createLink(controller: 'issue', action: 'edit')}";
        var viewUrl = "${createLink(controller: 'issue', action: 'view')}";
        var getSharedWith = "${createLink(controller: 'issue', action: 'getSharedWithUsers')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var addEmailConfiguration = "${createLink(controller: "issue",action: "addEmailConfiguration")}";
        var userListUrl = "${createLink(controller: 'userRest', action: 'listUsers')}";
        var editRole = false;
        <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
        editRole = true;
        </sec:ifAnyGranted>
    </script>
    <style>
        .input-group {width: 210px;}
        label {padding-bottom: 3px;}
        #config-filter-panel {margin-top: 65px;}
        #capa-list_wrapper > .dt-layout-row:first-child {
            margin-top : 0px;
        }
    </style>
</head>

<body>
<div class="container">
    <div class="row">
       <div class="col-md-12">
           <g:hiddenField name="ownerType" value="${type}"/>
<g:render template="/includes/layout/flashErrorsDivs" bean="${capaInstance}" var="theInstance"/>
<rx:container title="${message(code: message(code: "quality.issue.number.list.label"))}" filterButton="${true}"  customButtons="${g.render(template: "includes/capa_export")}">
    <div>
        <g:render template="includes/capa_list"  />
    </div>
</rx:container>

<g:form controller="issue" data-evt-sbt='{"method": "submitForm", "params": []}'>
    <g:hiddenField id="executedConfigId" name="id"/>

    <g:render template="includes/sharedWithModal"/>
    <g:render template="includes/emailToModal"/>
</g:form>

<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
<g:render template="/email/includes/copyPasteEmailModal" />
<form style="display: none" id="exportFormId" method="post" action="${createLink(controller: 'issue', action: 'exportCapa8d')}">
    <input name="data" id="data">
</form>
       </div></div>
</div>
</body>
</html>