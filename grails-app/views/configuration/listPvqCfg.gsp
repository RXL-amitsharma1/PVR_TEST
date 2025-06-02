<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.config.ReportConfiguration; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.configuration.autorca.title"/></title>

    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        $(function () {
            $("#pvqTableConfiguration").dataTable({
                "layout": {
                    topStart: null,
                    topEnd: {search: {placeholder: 'Search'}},
                    bottomStart: ['pageLength', 'info', {
                        paging: {
                            type: 'full_numbers'
                        }
                    }],
                    bottomEnd: null,
                },
                language: { search: ''},
                "search": {
                    "regex": false,
                    "smart": false
                },
                aoColumnDefs: [
                    {
                        bSortable: false,
                        aTargets: [6,7]
                    }
                ],
                "pagingType": "full_numbers"
            }).on('draw.dt', function () {
                eventBindingClk();
            });
            actionButton('#pvqTableConfiguration');

            let eventBindingClk = (function () {
                $("[data-evt-clk]").on('click', function (e) {
                    const eventData = JSON.parse($(this).attr("data-evt-clk"));
                    const methodName = eventData.method;
                    const params = eventData.params;
                    if (methodName == "disableEventBinding") {
                        var eventElement = $(this);
                        disableEventBinding(eventElement, params);
                    }
                });
            });

            function disableEventBinding(eventElement, params) {
                $(eventElement).on('click', function (e) {
                    if ($(this).attr("disabled") == "disabled") {
                        e.preventDefault();
                    }
                });
                $(eventElement).attr("disabled", "disabled");
                location.href = params[0];
            }
        });
    </g:javascript>
    <style>
    .dt-layout-row:last-child {
        margin-top:10px;
    }
    </style>

</head>

<body>
<div class="content">
    <div class="container ">
        <div class="pv-caselist basicDataTable">
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.configuration.pvrcfg.title.name")}" >
                <div class="pull-right" id="create" style="cursor: pointer; text-align: right; position: relative;">
                    <a href="${createLink(controller: 'configuration', action: 'create')}?pvqType=${defaultPvqType}" class="ic-sm pv-ic pv-ic-hover" style="color: #353d43;position: absolute;top: -37px; right: 5px;width: 25px;">
                        <i class="md-plus" id="createActionItem" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.pvq.configuration.create')}" style="color: #353d43;"></i>
                    </a>

                </div>

                <table id="pvqTableConfiguration" class="table table-striped pv-list-table dataTable no-footer">
                    <thead class="filter-head">
                    <tr>
                        <th><g:message code="app.label.reportName"/></th>
                        <th><g:message code="app.label.runTimes"/></th>
                        <th style="min-width: 60px;"><g:message code="qualityModule.ad.hoc.alert.button"/></th>
                        <th style="min-width: 60px;"><g:message code="app.label.state"/></th>
                        <th style="min-width: 70px;"><g:message code="next.run.date"/></th>
                        <th style="min-width: 75px;"><g:message code="app.configuration.autorca.lastRunStatus"/></th>
                        <th style="min-width: 55px;"><g:message code="app.label.view"/></th>
                        <th style="min-width: 70px;"><g:message code="app.label.action"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each var="o" in="${observations}">
                        <tr>
                            <td>${o.reportName}</td>
                            <td>${o.numOfExecutions}</td>
                            <td>${o.pvqType?.replaceAll(";","; ")}</td>
                            <td>${o.state}</td>
                            <td>${o.nextRunDate}</td>
                            <td>${o.laststate}</td>
                            <td><a href="${createLink(controller: 'report', action: 'index')}?reportName=${o.reportName}&forPvq=true"><g:message code="app.configuration.autorca.viewResults"/></a></td>
                         <td>
                           <div class="btn-group dropdown dataTableHideCellContent" align="center">
                             <a class="btn btn-success btn-xs" data-evt-clk='{"method": "disableEventBinding", "params": ["${createLink(controller: 'configuration', action: 'runOnce')}/${o.id}?isPriorityReport=false"]}'><g:message code="default.button.run.label"/></a>
                               <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                                 <span class="caret"></span>
                                 <span class="sr-only">Toggle Dropdown</span>
                             </button>
                             <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                                 <li role="presentation"><a role="menuitem" href="${createLink(controller: 'configuration', action: 'edit')}/${o.id}"><g:message code="default.button.edit.label"/></a></li>
                                 <li role="presentation"><a role="menuitem" href="${createLink(controller: 'configuration', action: 'copy')}/${o.id}"><g:message code="default.button.copy.label"/></a></li>
                                 <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="Configuration" data-instanceid="${o.id}" data-instancename="${o.reportName}"><g:message code="app.label.delete"/></a></li>
                                 <li role="presentation"><a role="menuitem" href="${createLink(controller: 'configuration', action: 'runOnce')}/${o.id}?isPriorityReport=true"><g:message code="app.pvq.prioritizeReport" default="Prioritize Report"/></a></li>
                             </ul>
                         </div>

                         </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <g:form controller="${controller}" method="delete">
                    <g:render template="/includes/widgets/deleteRecord"/>
                </g:form>
            </rx:container>
        </div>
    </div>
</div>
</body>
