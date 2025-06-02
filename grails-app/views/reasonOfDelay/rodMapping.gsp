<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<html>
<head>
    <asset:javascript src="app/reasonOfDelay/reasonOfDelay.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/reasonOfDelay/RCAMandatoryFields.js"/>
    <meta name="layout" content="main"/>
    <g:javascript>
        var getLateDataList = "${createLink(controller: 'reasonOfDelay', action: 'getLateMapping')}";
        var getRootCauseDataList = "${createLink(controller: 'reasonOfDelay', action: 'getRootCauseMapping')}";
        var getRespPartyDataList = "${createLink(controller: 'reasonOfDelay', action: 'getResponsiblePartyList')}";
        var getLateMapping = "${createLink(controller: 'reasonOfDelay', action: 'getRootCauseList')}";
        var getRootCauseMapping = "${createLink(controller: 'reasonOfDelay', action: 'getResponsiblePartyList')}";
        var getRootCauseSubCategoryList = "${createLink(controller: 'reasonOfDelay', action: 'getRootCauseSubCategoryList')}";
        var getRootCauseClassList = "${createLink(controller: 'reasonOfDelay', action: 'getRootCauseClassList')}";
        var saveLateUrl = "${createLink(controller: 'reasonOfDelay', action: 'saveLate')}";
        var hideWarningUrl= "${createLink(controller: 'reasonOfDelay', action: 'hideWarning')}";
        var saveRootCauseUrl = "${createLink(controller: 'reasonOfDelay', action: 'saveRootCause')}";
        var saveRespPartyUrl = "${createLink(controller: 'reasonOfDelay', action: 'saveResponsibleParty')}";
        var saveRootCauseSubUrl = "${createLink(controller: 'reasonOfDelay', action: 'saveRootCauseSub')}";
        var saveRootCauseClassUrl = "${createLink(controller: 'reasonOfDelay', action: 'saveRootCauseClass')}";
        var getLateTypeUrl = "${createLink(controller: 'reasonOfDelay', action: 'getRODLateTypeEnum')}"
        var addWorkflowStateUrl = "${createLink(controller: 'reasonOfDelay', action: 'addWorkflowState')}"
        var addEditableByUrl = "${createLink(controller: 'reasonOfDelay', action: 'addEditableBy')}"
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SYSTEM_CONFIGURATION")};
    </g:javascript>
    <title><g:message code="app.lateMapping.title"/></title>
    <style>
    .dt-layout-row:first-child {
        margin-top: 0px;
        padding-right:0px;
    }
    .dt-layout-row:last-child {
        margin-top:10px;
    }
    </style>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.rodMapping.appName")}">

            <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowStateInstance}" var="theInstance"/>

                <button type="button" class="btn btn-primary" id="createButton" data-toggle='modal' data-target='#rodModal'>
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" />
                </button>
                <div class="pull-right">
                                <div class="checkbox checkbox-primary">
                    <g:checkBox name="hiddenList" id = "hiddenList"/>
                    <label for="hiddenList">
                        <g:message code="app.RCA.display.list" />
                    </label>
                </div>
                </div>

            <div class="pv-caselist">
                <div class="case-quality-datatable-toolbar"></div>
                <table id="rodTable" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Name" code="app.label.lateMapping.name"/></th>
                            <th><g:message default="Type" code="label.late.type"/></th>
                            <th class="mappingColumn"><g:message default="Root Cause" code="app.label.lateMapping.root.cause"/></th>
                            <th ><g:message  code="app.pvc.RootCauseSubCategory"/></th>
                            <th ><g:message  code="app.pvc.RootCauseClass"/></th>
                            <th><g:message default="Owner App" code="label.owner.app"/></th>
                            <th style="width: 100px"><g:message default="Action" code="app.label.lateMapping.action"/></th>
                        </tr>
                    </thead>
                </table>
            </div>
                <g:render template="/reasonOfDelay/includes/rcaMandatory"/>
         </rx:container>
            </div>
<g:form controller="reasonOfDelay" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
        </div>
    </div>
</div>
<g:render template="/reasonOfDelay/includes/rodModal"/>
<g:render template="/includes/widgets/warningTemplate"/>
</body>
</html>