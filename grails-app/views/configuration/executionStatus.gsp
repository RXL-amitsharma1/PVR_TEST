<%@ page import="com.rxlogix.enums.FrequencyEnum;com.rxlogix.enums.ExecutionStatusConfigTypeEnum;com.rxlogix.config.ExecutionStatus; com.rxlogix.util.FilterUtil; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ExecutionStatus.title"/></title>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/configuration/executionStatus.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>

    <asset:stylesheet src="executionStatus.css"/>
    <g:javascript>
        var inboundExStatus = "${createLink(controller: 'inboundCompliance', action: 'executionStatus')}";
        var reportsExStatus = "${createLink(controller: 'executionStatus', action: 'list')}";
        var toFavorite = "${createLink(controller: 'configuration', action: 'favorite')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:ExecutionStatus.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var frequencyType= JSON.parse("${FilterUtil.buildEnumOptions(FrequencyEnum)}")
        var executionStatusUrl = "${createLink(controller: 'configurationRest', action: 'executionStatus')}";
        var executionErrorUrl = "${createLink(controller: 'executionStatus', action: 'reportExecutionError')}";
        var ShowConfigUrl = "${createLink(controller: 'executionStatus', action: 'viewConfig')}";
        var viewResultURL = "${createLink(controller: 'executionStatus', action: 'viewResult')}";
        var ShowScheduledConfigUrl = "${createLink(controller: 'executionStatus', action: 'viewScheduledConfig')}";
        var killExecutionUrl = "${createLink(controller: 'configurationRest', action: 'killExecution')}";
        var unscheduleUrl = "${createLink(controller: 'configurationRest', action: 'unschedule')}";
        var removeFromBacklogUrl = "${createLink(controller: 'configurationRest', action: 'removeFromBacklog')}";
        var updatePriorityUrl = "${createLink(controller: 'configurationRest', action: 'updatePriority')}";
        var isPriorityRoleEnable = ${SpringSecurityUtils.ifAnyGranted("ROLE_RUN_PRIORITY_RPT")};
        var isICSRProfile = ${isICSRProfile}
    </g:javascript>
    <style>
        #exStatusConfigType, #reportExecutionStatusDropDown {
                select.form-control + .select2 {
                    text-align-last: center;
                    min-width: 150px;
                    width: auto;
                }
        }
        #reportExecutionStatusDropDown {
            .glyphicon {
                top: 3px;
                margin-right: 40px;
            }
        }
    </style>
</head>

<body>
    <div class="content ">
        <div class="container ">
            <div>
                <g:render template="/includes/layout/flashErrorsDivs"/>
                <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="WarningDiv" style="display: none">
                    <button type="button" class="close WarningDivclose">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <p></p>
                </div>
<rx:container title="${message(code: "app.label.ExecutionStatus")}" options="${true}" filterButton="true">
    <div class="body">
       <div id="report-request-conainter" class="list pv-caselist">
           <div class="topControls" style="display: none; float: right; text-align: right; margin-right: 0.5em">
                <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ExecutionStatus.name]"/>
                <g:render template="/includes/widgets/reportExecutionStatusDropDown" />
                <g:render template="/includes/widgets/executionStatusDropDown"/>
           </div>
           <div class="fuelux outside row">
               <div class="col-xs-6" id="resultDatepickerFrom">
                   <div class="row form-inline">
                       <div class="col-xs-3 datePickerMargin labelMargin ">
                           <label class="no-bold"><g:message code="app.dateFilter.from"/> </label>
                       </div>
                       <div class="col-xs-9 datePickerMargin">
                           <div class="input-group">
                               <input placeholder="${message(code: 'select.start.date')}" class="form-control input-sm" id="myResultDatepickerFrom" type="text"/>
                               <g:render template="/includes/widgets/datePickerTemplate"/>
                           </div>
                       </div>
                   </div>
               </div>
                <div class="col-xs-6" id="resultDatepickerTo">
                   <div class="row form-inline">
                       <div class="col-xs-2 datePickerMargin labelMargin ">
                           <label class="no-bold"><g:message code="app.dateFilter.to"/> </label>
                       </div>
                       <div class="col-xs-9 datePickerMargin">
                           <div class="input-group">
                               <input placeholder="${message(code:"select.end.date")}" class="form-control input-sm" id="myResultDatepickerTo" type="text"/>
                               <g:render template="/includes/widgets/datePickerTemplate"/>
                           </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="pv-caselist">
                <table id="rxTableReportsExecutionStatus" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.reportName"/></th>
                        <th><g:message code="app.label.reportType"/></th>
                        <th><g:message code="app.label.version"/></th>
                        <th><g:message code="app.label.executionStatus"/></th>
                        <th><g:message code="app.label.owner"/></th>
                        <th><g:message code="app.label.runDate"/></th>
                        <th><g:message code="app.label.runDuration"/></th>
                        <th><g:message code="app.label.frequency"/></th>
                        <th><g:message code="app.label.sharedWith"/></th>
                        <th><g:message code="app.label.deliveryMedia"/></th>
                    </tr>
                    </thead>
                </table>
            </div>

            <g:hiddenField name="isAdmin" id="isAdmin" value="${isAdmin}"/>
            </div>
    </div>
</rx:container>
            </div>
        </div>
    </div>
<g:render template="/periodicReport/includes/justification"/>
</body>