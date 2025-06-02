<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.config.IcsrReportConfiguration; com.rxlogix.config.IcsrProfileConfiguration; com.rxlogix.config.PeriodicReportConfiguration; grails.gorm.multitenancy.Tenants; com.rxlogix.util.DateUtil" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.CheckUsage.title"/></title>
    <asset:javascript src="app/template/usage.js"/>
</head>

<body>
<rx:container title="Usages of template: ${template}" options="${true}">
    <input type="hidden" id="sizeOfUsage" value="${usages.size()}" />
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="pv-caselist">
        <table id="rxCheckTemplateUsage" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.reportName"/></th>
            <th><g:message code="app.label.reportDescription"/></th>
            <th><g:message code="app.label.queryName"/></th>
            <th><g:message code="app.label.queryLevel"/></th>
            <th><g:message code="app.label.DateRange"/></th>
            <th><g:message code="app.label.dateCreated"/></th>
            <th><g:message code="app.label.scheduled"/></th>
            <th><g:message code="app.label.tag"/></th>
        </tr>
        </thead>
        <g:each in="${usages}" var="templateQuery">
            <tr>
                <g:set var="configClass" value="${templateQuery.report.class}"/>
                <td><a href="${createLink(controller: configClass == Configuration ? 'configuration' : configClass == PeriodicReportConfiguration ? 'periodicReport' : configClass == IcsrReportConfiguration ? 'icsrReport' : 'icsrProfileConfiguration', action: 'view', params: [id: templateQuery.report.id])}">
                    <g:maskData tenantId="${templateQuery.report.tenantId}">${templateQuery.report.reportName}</g:maskData></a></td>
                <td><g:maskData tenantId="${templateQuery.report.tenantId}">${templateQuery.report.description}</g:maskData></td>
                <td>${templateQuery.query?.name}</td>
                <td><g:message code="app.queryLevel.${templateQuery.queryLevel}"/></td>
                <td><g:message code="${templateQuery.dateRangeInformationForTemplateQuery.dateRangeEnum.i18nKey}"/></td>
                <td class="forceLineWrapDate"><g:renderLongFormattedDate date="${templateQuery.report.dateCreated}" timeZone="${g.getCurrentUserTimezone()}"/></td>
                <td><g:maskData tenantId="${templateQuery.report.tenantId}">${templateQuery.report.owner.fullName}</g:maskData></td>
                <td>
                    <g:maskData tenantId="${templateQuery.report.tenantId}">
                        <g:each in="${templateQuery.report.tags}" var="tag">
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
