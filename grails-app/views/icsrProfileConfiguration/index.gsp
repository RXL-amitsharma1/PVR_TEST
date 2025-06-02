<%@ page import="com.rxlogix.config.IcsrReportConfiguration" %>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.icsrProfileConf.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/configuration/icsrProfileConfiguration.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <g:javascript>
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz: IcsrReportConfiguration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var ICSRPROFILECONF = {
             listUrl: "${createLink(controller: 'icsrProfileConfigurationRest', action: 'list')}",
             editUrl: "${createLink(controller: 'icsrProfileConfiguration', action: 'edit')}",
             viewUrl: "${createLink(controller: 'icsrProfileConfiguration', action: 'view')}",
             copyUrl: "${createLink(controller: 'icsrProfileConfiguration', action: 'copy')}"

        }
    </g:javascript>
</head>

<body>
<div class="col-md-12">
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <rx:container title="${message(code: "app.icsrProfileConf.label.myInbox")}" options="true" filterButton="true"
                  customButtons="${g.render(template: "/icsrProfileConfiguration/customHeaderButtons")}">

        <div class="pv-caselist">
               <table id="rxTableIcsrrProfileConfiguration" class="table table-striped pv-list-table dataTable no-footer"
                      width="100%">
                   <thead>
                   <tr>
                       <th><g:message code="app.icsrProfileConf.label.icsrPartnerName"/></th>
                       <th><g:message code="app.label.description"/></th>
                       <th><g:message code="app.icsrProfileConf.label.senderOrg"/></th>
                       <th><g:message code="app.icsrProfileConf.label.senderType"/></th>
                       <th><g:message code="app.icsrProfileConf.label.recipientOrg"/></th>
                       <th><g:message code="app.icsrProfileConf.label.recipientType"/></th>
                       <th><g:message code="app.label.qualityChecked"/></th>
                       <th><g:message code="app.label.icsr.profile.conf.disabled"/></th>
                       <th><g:message code="app.label.dateCreated"/></th>
                       <th><g:message code="app.label.dateModified"/></th>
                       <th><g:message code="app.icsrProfileConf.label.profile.owner"/></th>
                       <th class="pv-col-sm"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
        </div>
    </rx:container>
</div>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>

