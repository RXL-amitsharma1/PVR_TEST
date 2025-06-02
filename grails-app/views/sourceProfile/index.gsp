<html>
<head>
    <asset:javascript src="app/sourceProfileList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.sourceProfile.title"/></title>
    <g:javascript>
        $(function() {
            //Initiate the datatable
            sourceProfile.sourceProfileList.init_source_profile_table("list");
    });
    </g:javascript>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.sourceProfile.label")}">

            <div class="pv-caselist">
                <table id="sourceProfileList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                    <tr>
                        <th></th>
                        <th><g:message default="Data Source #" code="app.label.sourceProfile.sourceId"/></th>
                        <th><g:message default="Source Name" code="app.label.sourceProfile.sourceName"/></th>
                        <th><g:message default="Source Abbreviation" code="app.label.sourceProfile.sourceAbbrev"/></th>
                        <th><g:message default="Central source" code="app.label.sourceProfile.isCentral"/></th>
                        <th><g:message default="Action" code="app.label.action"/></th>
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