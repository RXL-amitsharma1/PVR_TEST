<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'cognosReport.label')}"/>
    <title><g:message code="app.CognosReportLibrary.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/cognos/cognos.js"/>
</head>

<body>
<div class="content">
    <div class="container ">
        <div>
<rx:container title="${message(code: "app.viewCognosReports.menu")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${cognosReportInstance}" var="theInstance"/>

    <sec:ifAnyGranted roles="ROLE_COGNOS_CRUD">
        <div class="row">
            <div class="col-md-12">
                <div class="navScaffold m-b-10">
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["cognosReport", "create"]}' id="createButton">
                        <span class="glyphicon glyphicon-plus icon-white"></span>
                        <g:message code="default.new.label" args="[entityName]"/>
                    </button>
                </div>
            </div>
        </div>
    </sec:ifAnyGranted>

    <div class="pv-caselist">
        <table class="table table-striped pv-list-table dataTable no-footer">
            <thead>
            <tr>
                <g:sortableColumn property="name" title="${message(code: message(code: "cognosReport.name.label"))}"/>
                <g:sortableColumn property="description"
                                  title="${message(code: message(code: "cognosReport.description.label"))}"/>
            </tr>
            </thead>
            <tbody>
            <g:if test="${cognosReportInstanceTotal > 0}">
                <g:each in="${cognosReportInstanceList}" status="i" var="cognosReportInstance">
                    <tr>
                        <td><g:link action="show"
                                    id="${cognosReportInstance.id}">${cognosReportInstance.name}</g:link></td>
                        <td><div class="comment forceLineWrap">${cognosReportInstance.description}</div></td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="2">None</td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>
    <g:render template="/includes/widgets/pagination" bean="${cognosReportInstanceTotal}" var="theInstanceTotal"/>
</rx:container>
        </div>
    </div>
</div>


</body>
</html>
