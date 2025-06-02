<%@ page import="com.rxlogix.publisher.PublisherService" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/publisher/publisherCommonParameterList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.PublisherCommonParameter.list.title"/></title>
</head>
<body>
<div class="content">
    <div class="container ">
        <div class="row">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
            <rx:container title="${message(code:"app.label.PublisherCommonParameter.appName")}">
                <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-top: -37px; margin-right: 15px">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["publisherCommonParameter", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.report.request.create')}" style="color: #353d43;"></i>
                    </a>
                </div>

            <div class="pv-caselist basicDataTable">
                <table id="publisherCommonParameterList" class="table table-striped pv-list-table dataTable no-footer no-hyphens">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message code="app.label.name"/></th>
                            <th><g:message code="app.label.description"/></th>
                            <th><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/></th>
                            <th><g:message code="app.label.publisher.lastModifiedOn"/></th>
                            <th><g:message code="app.label.publisher.lastModifiedBy"/></th>
                            <th><g:message code="app.label.action"/></th>
                        </tr>
                    </thead>
                </table>

    </div><br>
    <i class="fa fa-info-circle"></i>
    <span ><g:message code="app.PublisherCommonParameter.builtInVariables1" />  ${PublisherService.BUILD_IN_PARAMS.join(", ")}. <g:message code="app.PublisherCommonParameter.builtInVariables2" /> </span>
</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
        </div>
    </div>
</div>
</body>
</html>