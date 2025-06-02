<%@ page import="grails.gorm.multitenancy.Tenants; com.rxlogix.util.DateUtil" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.CheckUsage.title"/></title>
    <asset:javascript src="app/query/usage.js"/>
</head>

<body>
<rx:container title="Usages of query: ${query}" options="${true}">
    <input type="hidden" id="sizeOfUsage" value="${usages.size()}" />
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="pv-caselist">
        <table id="rxCheckQueryUsage" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.reportName"/></th>
            <th><g:message code="app.label.reportDescription"/></th>
            <th><g:message code="app.label.dateCreated"/></th>
            <th><g:message code="app.label.owner"/></th>
            <th><g:message code="app.label.tag"/></th>
        </tr>
        </thead>
        <g:each in="${usages}" var="configuration">
            <tr>
                <td><a href="${configuration.url}">
                    <g:maskData tenantId="${configuration.tenantId}">${configuration.name}</g:maskData></a></td>
                <td><g:maskData tenantId="${configuration.tenantId}">${configuration.description}</g:maskData></td>
                <td class="forceLineWrapDate"><g:renderLongFormattedDate date="${configuration.dateCreated}" timeZone="${g.getCurrentUserTimezone()}"/></td>
                <td><g:maskData tenantId="${configuration.tenantId}">${configuration.fullName}</g:maskData></td>
                <td>
                    <g:maskData tenantId="${configuration.tenantId}">
                        <g:each in="${configuration.tags}" var="tag">
                            <div>${tag.name}</div>
                        </g:each>
                    </g:maskData>
                </td>
            </tr>
        </g:each>
    </table>
    </div>
</rx:container>
</body>