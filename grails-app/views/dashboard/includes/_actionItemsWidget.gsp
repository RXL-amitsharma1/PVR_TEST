<%@ page import="com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
            <g:link controller="actionItem" action="index"
                    title="${message(code: 'default.button.addactionItemsWidget.label')}" class="rxmain-container-header-label rx-widget-title">${message(code: 'default.button.addactionItemsWidget.label')}
            </g:link>
            <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div id="container${index}" class="row rx-widget-content nicescroll pv-caselist">
        <table id="rxTableActionItems${index}" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
                <tr>
                    <th><g:message code="app.label.action.item.description"/></th>
                    <th><g:message code="app.label.action.item.due.date"/></th>
                    <th><g:message code="app.label.action.item.priority"/></th>
                    <th><g:message code="app.label.action.item.assigned.to"/></th>
                </tr>
            </thead>
        </table>
    </div>

    <script>
        var initAIEvtClk = function () {
            $("[data-evt-clk]").on('click', function(e) {
                e.preventDefault();
                const eventData = JSON.parse($(this).attr("data-evt-clk"));
                const methodName = eventData.method;
                const params = eventData.params;

                if(methodName == 'viewActionItem') {
                    viewActionItem(...params);
                }
            });
        }

        $(function () {
            var refreshData = function (dataSet) {
                var datatable = $('#rxTableActionItems${index}').dataTable().api();
                datatable.clear();
                datatable.rows.add(dataSet);
                datatable.draw();
            };
            var loadData = function (processData) {
                $.ajax({
                    "url": actionItemUrl,
                    "data": {
                        length: 5,
                        start: 0,
                        sort: "dueDate",
                        direction: "asc"
                    },
                    "dataType": 'json'
                }).done(function (dataSet) {
                    processData(dataSet.aaData);
                });
            };
            var initTable = function (dataSet) {
                var table = $('#rxTableActionItems${index}').DataTable({
                    fnInitComplete: function () {
                        $($("#rxTableActionItems${index}_wrapper")).find(">:first-child").css({ "display": "none"});
                    },
                    "customProcessing": true, //handled using processing.dt event
                    "serverSide": false,
                    "searching": false,
                    "paging": false,
                    "info": false,
                    "data": dataSet,
                    "beforeSend" : showDataTableLoader($('#rxTableActionItems${index}')),
                    "success" : hideDataTableLoader($('#rxTableActionItems${index}')),
                    "order": [[1, "desc"]],
                    "columns": [
                        {
                            "data": "description",
                            "render": function (data, type, row) {
                                var truncatedDes = "";
                                if (row.description.length > 30) {
                                    truncatedDes = row.description.substring(0, 29) + "...";
                                } else {
                                    truncatedDes = row.description
                                }

                                return '<a href="#" data-evt-clk=\'{\"method\": \"viewActionItem\", \"params\": [\"' + row.actionItemId + '\"]}\'>'
                                    + encodeToHTML(truncatedDes) + '</a>';
                            }
                        },
                        {
                            "data": "dueDate",
                            "className": "dataTableColumnCenter",
                            "render": function (data, type, full) {
                                return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                            }
                        },
                        {
                            "data": "priority",
                            "render": function (data, type, full) {
                                var prioritySpan = document.createElement("span");
                                prioritySpan.classList.add("label");
                                switch (data) {
                                    case "HIGH":
                                        prioritySpan.classList.add("label-danger");
                                        break;
                                    case "MEDIUM":
                                        prioritySpan.classList.add("label-warning");
                                        break;
                                    default:
                                        prioritySpan.classList.add("label-default");
                                        break;

                                }
                                prioritySpan.appendChild(document.createTextNode(data));
                                return prioritySpan.outerHTML;
                            }
                        },
                        {
                            "data": "assignedTo",
                            "mRender": $.fn.dataTable.render.text()
                        }
                    ]
                }).on('draw.dt', function () {
                    initAIEvtClk();
                });
            };

            $('#refresh-widget${index}').hide();

            loadData(initTable);
        });
        var viewActionItem = function (actionItemId) {

            $('.edit-action-item').on('click', function () {
                actionItem.actionItemModal.edit_action_item(hasAccessOnActionItem,actionItemId, true, null, null);
            });

            $('.update-action-item').on('click', function () {
                actionItem.actionItemModal.update_action_item(false, $('#actionItemModal'), null);
            });

            actionItem.actionItemModal.view_action_item(actionItemId);
        };
    </script>
</div>