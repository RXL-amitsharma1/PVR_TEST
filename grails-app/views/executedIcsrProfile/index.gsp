<%@ page import="com.rxlogix.config.ExecutedIcsrProfileConfiguration" %>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.icsrProfileConf.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/icsrExecutedProfiles.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:javascript>
        var ICSRPROFILECONF = {
             profileListUrl: "${createLink(controller: 'icsrProfileConfigurationRest', action: 'profileList')}",
             profileViewUrl: "${createLink(controller: 'executedIcsrProfile', action: 'view')}",
             reportViewUrl: "${createLink(controller: "executedIcsrProfile", action: "showResult")}"
        }
    </g:javascript>
    <style>
    .dt-layout-row:first-child {
        margin-top: -38px;
        position: relative;
        .dt-layout-cell {
            float: right;
            padding-left: 0.5em;
        }
        .topControls > span.select2:last-child{
            min-width: 150px;
            width: auto;
        }
    }
    </style>
</head>
<body>
<div class="col-md-12">
    <g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.label.generated.profile")}" options="true">
    <div class="pv-caselist">
        <table id="icsrProfilesList" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.icsrProfileConf.label.icsrPartnerName"/></th>
                <th><g:message code="app.label.version"/></th>
                <th><g:message code="app.label.description"/></th>
                <th><g:message code="app.icsrProfileConf.label.senderOrg"/></th>
                <th><g:message code="app.icsrProfileConf.label.senderType"/></th>
                <th><g:message code="app.icsrProfileConf.label.recipientOrg"/></th>
                <th><g:message code="app.icsrProfileConf.label.recipientType"/></th>
                <th><g:message code="app.label.dateCreated"/></th>
                <th><g:message code="app.label.dateModified"/></th>
                <th><g:message code="app.icsrProfileConf.label.profile.owner"/></th>
                <th style="padding: 15px" ><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>
</rx:container>
</div>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
<g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>

    <g:hiddenField name="executedConfigId"/>

    <g:render template="/report/includes/sharedWithModal"/>

</g:form>
<g:render template="/actionItem/includes/actionItemModal" model="[]" />
</body>

