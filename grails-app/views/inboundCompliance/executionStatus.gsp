<%@ page import="com.rxlogix.enums.ExecutionStatusConfigTypeEnum;com.rxlogix.config.ExecutionStatus; com.rxlogix.util.FilterUtil; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.ExecutionStatus.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
         var viewUrl = "${createLink(controller: 'inboundCompliance', action: 'view')}";
         var inboundStatusUrl = "${createLink(controller: 'inboundComplianceRest', action: 'executionStatus')}";
         var viewExecutedConfigUrl = "${createLink(controller: 'executedInbound', action: 'viewExecutedConfig')}";
         var executionErrorUrl = "${createLink(controller: 'executedInbound', action: 'executionError')}";
         var inboundExStatus = "${createLink(controller: 'inboundCompliance', action: 'executionStatus')}";
         var reportsExStatus = "${createLink(controller: 'executionStatus', action: 'list')}";
    </g:javascript>
    <asset:javascript src="/app/inboundCompliance/inboundExecutionStatus.js"/>
    <asset:stylesheet src="/executionStatus.css"/>
    <style>

    select.form-control + .select2 {
        width: 70%;
    }
    div.dt-container {
        div.dt-layout-cell.dt-end {
            width: 70%;
        }
    }
    .filterDiv {
        width: 44%;
    }
    #reportExecutionStatusDropDown {
        float: none !important;
        margin-left: 0px !important;
    }
</style>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.label.ExecutionStatus")}" options="${true}" filterButton="true">
                <div class="body">
                    <div id="report-request-conainter" class="list pv-caselist">
                        <div class="topControls">
                            <div class="filterDiv">
                                <g:render template="/includes/widgets/reportExecutionStatusDropDown"  model="[isInboundCompliance: true]" />
                                <g:render template="/includes/widgets/executionStatusDropDown"/>
                            </div>
                        </div>

%{--                        <div class="pv-caselist">--}%
                            <table id="rxTableInboundExecutionStatus" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                                <thead>
                                <tr>
                                    <th><g:message code="app.label.sender.name"/></th>
                                    <th><g:message code="app.label.version"/></th>
                                    <th><g:message code="app.label.dateModified"/></th>
                                    <th><g:message code="app.label.owner"/></th>
                                    <th><g:message code="app.label.runDate"/></th>
                                    <th><g:message code="app.label.runDuration"/></th>
                                    <th><g:message code="app.label.executionStatus"/></th>
                                </tr>
                                </thead>
                            </table>
%{--                        </div>--}%
                        <g:hiddenField name="isAdmin" id="isAdmin" value="${isAdmin}"/>
                    </div>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>

