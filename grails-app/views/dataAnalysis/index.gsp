<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title"/></title>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <script type="application/javascript">
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action:'list')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action:'view')}";
        var serverUrl = "${wp_url}";
        var tk = "${auth_token}";
        var libraryRoot = "${libraryRoot}";
        var cb_srv = "${callback_server}";
    </script>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.label.title.library.dataAnalysis", default: "Analysis File Library")}"  options="true">



    <div class="pv-caselist">
        <table id="rxTableSpoftfireFiles" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.spotfire.fileName"/></th>
                <th class="text-right"><g:message code="app.label.spotfire.file.generation.time"/></th>
                <th><g:message code="app.label.spotfire.dateCreated"/></th>
                <th><g:message code="app.label.spotfire.lastUpdated"/></th>
                <th><g:message code="app.label.spotfire.dateAccessed"/></th>
                <th style="width: 80px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>
</rx:container>
<asset:javascript src="app/dataAnalysis/dataAnalysisList.js"/>
<asset:javascript src="app/dataTablesActionButtons.js"/>
        </div>
    </div>
</div>
</body>
