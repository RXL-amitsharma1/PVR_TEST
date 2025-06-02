<%@ page import="com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
            <span   title="${message(code: 'app.viewReports.menu')}" class="rxmain-container-header-label rx-widget-title">${message(code: 'app.viewReports.menu')}
            </span>

        <span class="show-hide-table rxmain-container-header-label pull-right" style="line-height: 10px;  height: 20px;  "><input placeholder="${message(code: 'default.button.search.label')}" id="rxTableConfigurations${index}_topfilter" style="width: 150px;margin-top: -2px;" class="form-control"></span>

        <div class="btn-group rxmain-container-header-label pull-right" style="margin-left: 10px">
            <span class="  md md-plus dropdown-toggle pv-cross " data-toggle="dropdown" aria-haspopup="true" aria-expanded="true" style="line-height: 10px;    margin-bottom: 5px;"></span>
            <ul class="dropdown-menu">
                <span style="text-transform: none; margin-left: 5px">
                <g:message code="app.configurationType.ADHOC_REPORT" />
                </span>
                <li>
                    <a class="createAdhocFromTemplate"  style="text-transform: none; cursor: pointer">
                        <g:message code="app.menu.createFromTemplate" />
                    </a>
                </li>
                <li>
                    <g:link controller="configuration" action="create" class=""  style="text-transform: none;">
                        <g:message code="app.newReport.menu" />
                    </g:link>
                </li>
                <span style="text-transform: none; margin-left: 5px">
                <g:message code="app.configurationType.PERIODIC_REPORT" />
                </span>
                 <li>
                    <a class="createAggregateFromTemplate"  style="text-transform: none; cursor: pointer">
                        <g:message code="app.menu.createFromTemplate" />
                    </a>
                </li>
                <li>
                    <g:link controller="periodicReport" action="create" class=""  style="text-transform: none;">
                        <g:message code="app.newReport.menu" />
                    </g:link>
                </li>
            </ul>
        </div>



            <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div id="container${index}" class="row rx-widget-content nicescroll pv-caselist ">
        <table id="rxTableConfigurations${index}" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th style="width: 30px; font-size: 16px"><span class="glyphicon glyphicon-star" ></span></th>
                <th style="min-width: 100px" ><g:message code="app.label.reportRequestType"/></th>
                <th style="min-width: 150px" class="reportNameColumn"><g:message code="app.label.reportName"/></th>
                <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
                <th style="width: 70px"><g:message code="app.label.runTimes"/></th>
                <th style="width: 150px;"><g:message code="app.label.dateModified"/></th>
                <th style="width: 80px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>

    <script>
        $(function () {
            $(document).on("keyup paste", "#rxTableConfigurations${index}_topfilter", function(){
                $('#rxTableConfigurations${index}_wrapper .dt-input').val($(this).val());
                $('#rxTableConfigurations${index}_wrapper .dt-input').trigger("keyup");
            });
            var refreshData = function (dataSet) {
                var datatable = $('#rxTableConfigurations${index}').dataTable().api();
                datatable.clear();
                datatable.draw();
            };
            var initTable = function () {
                var table = $('#rxTableConfigurations${index}').DataTable({
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
                    "customProcessing": true, //handled using processing.dt event
                    "serverSide": true,
                    "searching": true,
                    "paging": true,
                    "info": true,
                    "beforeSend": showDataTableLoader($('#rxTableConfigurations${index}')),
                    "success": hideDataTableLoader($('#rxTableConfigurations${index}')),
                    initComplete: function () {
                        $($("#rxTableConfigurations${index}_wrapper")).find(">:first-child").css({ "display": "none"});
                        $('#rxTableConfigurations${index}').on("click", ".favorite", function () {
                            changeFavoriteState($(this).data('exconfig-id'), $(this).hasClass("glyphicon-star-empty"), $(this));
                        });
                    },
                    "ajax": {
                        "url": CONFIGURATION.listUrl,
                        "dataSrc": "data",
                        "data": function (d) {
                            d.searchString = d.search.value;
                            d.sharedwith = $('#sharedWithFilterControl').val();
                            if (d.order.length > 0) {
                                d.direction = d.order[0].dir;
                                //Column header mData value extracting
                                d.sort = d.columns[d.order[0].column].data;
                            }
                        }
                    },
                    "bLengthChange": true,
                    "aLengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
                    "pagination": true,
                    "pagingType": "full_numbers",
                    "iDisplayLength": 10,
                    "columns": [
                        {
                            "data": "isFavorite",
                            "sClass": "dataTableColumnCenter",
                            "asSorting": ["asc"],
                            "render": renderFavoriteIcon
                        },
                        {
                            "mData": "configurationType"
                        },
                        {
                            "mData": "reportName",
                            mRender: function (data, type, row) {
                                var content = encodeToHTML(data);
                                return "<div class='three-row-dot-overflow' >" + content + "</div>";
                            }
                        },
                        {
                            "mData": "qualityChecked",
                            "sClass": "dataTableColumnCenter",
                            "mRender": function (data, type, full) {
                                return data == true ? $.i18n._("yes") : "";
                            }
                        },
                        {
                            "mData": "numOfExecutions",
                            "sClass": "dataTableColumnCenter"
                        },
                        {
                            "mData": "lastUpdated",
                            "aTargets": ["lastUpdated"],
                            "sClass": "dataTableColumnCenter forceLineWrapDate",
                            "mRender": function (data, type, full) {
                                return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                            }
                        }, {
                            "mData": null,
                            "bSortable": false,
                            "sClass": "dt-center",
                            "aTargets": ["id"],
                            "mRender": function (data, type, full) {
                                var cfg = full.isAdhocType?CONFIGURATION:AGG_CONFIGURATION;
                                var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs" data-evt-clk=\'{\"method\": \"disableEventBinding\", \"params\": [\"' + cfg.runUrl + '/' + data["id"] + '?isPriorityReport=false\"]}\'>' + $.i18n._('run') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + cfg.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + cfg.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + cfg.copyUrl + '/' + data["id"] + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                data-controller ="'+(full.isAdhocType?'configuration':'periodicReport')+'" data-action="delete" \
                                data-target="#deleteModal" data-instancetype="' + $.i18n._('cfg') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["reportName"]) + '">' + $.i18n._('delete') + '</a></li>';
                            if(isPriorityRoleEnable) {
                                actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="' + cfg.runUrl + '/' + data["id"] + '?isPriorityReport=true">' + $.i18n._('prioritize.report') + '</a></li>';
                            }
                                actionButton = actionButton + '</ul> \
                        </div>';
                                return actionButton;
                            }
                        }
                    ]
                }).on('draw.dt', function () {
                    updateTitleForThreeRowDotElements();
                    actionButton('#rxTableConfigurations${index}');

                }).on('xhr.dt', function (e, settings, json, xhr) {
                    checkIfSessionTimeOutThenReload(e, json)
                });
            };

            $('#refresh-widget${index}').hide();

            initTable();
        });

    </script>
</div>