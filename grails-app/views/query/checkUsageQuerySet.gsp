<%@ page import="com.rxlogix.util.DateUtil" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.CheckUsageQuerySet.title"/></title>
    <asset:javascript src="app/query/usage.js"/>
</head>

<body>
<rx:container title="Usages of query: ${query}" options="${true}">

    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="pv-caselist">
        <table id="rxCheckQueryUsage" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.querySetName"/></th>
            <th><g:message code="app.label.description"/></th>
            <th><g:message code="app.label.dateCreated"/></th>
            <th><g:message code="app.label.tag"/></th>
        </tr>
        </thead>
        <g:each in="${usages}" var="usage">
            <g:each in="${usage}" var="querySet">
                <tr>
                    <td><a href="${createLink(controller: 'query', action: 'view', params: [id: querySet.id])}">
                        ${querySet.name}</a></td>
                    <td>${querySet.description}</td>
                    <td class="forceLineWrapDate"><g:renderLongFormattedDate date="${querySet.dateCreated}" timeZone="${g.getCurrentUserTimezone()}"/></td>
                    <td>
                        <g:each in="${querySet.tags}" var="tag">
                            <div>${tag.name}</div>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </g:each>
    </table>
    </div>
</rx:container>
</body>
