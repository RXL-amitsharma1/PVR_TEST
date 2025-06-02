<%@ page import="com.rxlogix.util.DateUtil" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.CheckUsageTemplateSet.title"/></title>
    <asset:javascript src="app/template/usage.js"/>
</head>

<body>
<rx:container title="Usages of template: ${template}" options="${true}">

    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="pv-caselist">
        <table id="rxCheckTemplateUsage" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.templateSetName"/></th>
            <th><g:message code="app.label.description"/></th>
            <th><g:message code="app.label.dateCreated"/></th>
            <th><g:message code="app.label.tag"/></th>
        </tr>
        </thead>
        <g:each in="${usages}" var="usage">
            <g:each in="${usage}" var="templateSet">
                <tr>
                    <td><a href="${createLink(controller: 'template', action: 'view', params: [id: templateSet.id])}">
                        ${templateSet.name}</a></td>
                    <td>${templateSet.description}</td>
                    <td class="forceLineWrapDate"><g:renderLongFormattedDate date="${templateSet.dateCreated}" timeZone="${g.getCurrentUserTimezone()}"/></td>
                    <td>
                        <g:each in="${templateSet.tags}" var="tag">
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
