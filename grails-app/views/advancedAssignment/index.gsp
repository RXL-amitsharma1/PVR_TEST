<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <asset:javascript src="app/advancedAssignment.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.advanced.assignment.title"/></title>
    <style>
    div.dt-layout-cell.dt-end {
        margin-right:5px;
    }
    </style>
</head>
<body>
<div class="content">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <g:render template="/includes/layout/flashErrorsDivs" bean="${advancedAssignmentInstance}" var="theInstance"/>
            <rx:container title="${message(code: 'app.label.advanced.assignment.appName')}">

                <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-top: -37px; margin-right: 15px">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["advancedAssignment", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.advanced.assignment.create')}" style="color: #353d43;"></i>
                    </a>
                </div>
                <div class="basicDataTable pv-caselist">
                            <table id="advancedAssignmentList" class="table table-striped pv-list-table dataTable no-footer">
                                <thead>
                                <tr>
                                    <th></th>
                                    <th><g:message default="Name" code="app.label.name"/></th>
                                    <th><g:message default="Category" code="app.label.category"/></th>
                                    <th><g:message default="Owner" code="app.label.owner"/></th>
                                    <th><g:message default="Quality Checked" code="app.label.advanced.assignment.quality.checked"/></th>
                                    <th><g:message default="Description" code="app.label.description"/></th>
                                    <th><g:message default="Action" code="app.label.action"/></th>
                                </tr>
                                </thead>
                            </table>
                        </div>

            </rx:container>
            </div>
            <g:form controller="${controller}" method="delete">
                <g:render template="/includes/widgets/deleteRecord"/>
            </g:form>
        </div>
    </div>
</div>
</body>
</html>