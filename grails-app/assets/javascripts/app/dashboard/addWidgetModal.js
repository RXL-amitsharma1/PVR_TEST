$(function () {
    $('#addWidgetModal').on('show.bs.modal', function(event) {
        if (!$.fn.DataTable.isDataTable('#rxTableConfiguration')) {
            var table = $('#rxTableConfiguration').DataTable({
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
                // //"sPaginationType": "bootstrap",
                "customProcessing": true, //handled using processing.dt event
                "serverSide": true,
                "ajax": {
                    "url": CONFIGURATION.listUrl,
                    "dataSrc": "data",
                    "data": function (d) {
                        d.searchString = d.search.value;
                        if (d.order.length > 0) {
                            d.direction = d.order[0].dir;
                            //Column header mData value extracting
                            d.sort = d.columns[d.order[0].column].data;
                        }
                    }
                },
                "aaSorting": [],
                "order": [[6, "desc"]],
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aLengthMenu": [[5, 10, 15], [5, 10, 15]],
                "pagination": true,
                "pagingType": "full_numbers",
                "fnHeaderCallback": function() {
                    $("#addWidgetModal").find(".dataTables_length").css("margin-top", "10px");
                },

                drawCallback: function (settings) {
                    pageDictionary($('#rxTableConfiguration_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                    getReloader($('#rxTableConfiguration_info'), $("#rxTableConfiguration"));
                    },
                "aoColumns": [
                    //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
                    {
                        "data": "configurationType"
                    },
                    {
                        "mData": "reportName",
                        mRender: function (data, type, row) {
                            return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                        }
                    },
                    {
                        "mData": "sections",
                        "bSortable": false,
                        mRender: function (data, type, row) {
                            var result=[];
                            for(var i in data){
                                result.push("<a href='#' class='selectChartSection' data-id='"+row.id+"' data-number='"+data[i].sectionNumber+"'>"+
                                    data[i].sectionName.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')+"</a>")
                            }
                            return result.join("<br>")
                        }
                    },
                    {
                        "mData": "description",
                        mRender: function (data, type, row) {
                            var text = (data == null) ? '' : data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                            return '<div class="comment">'
                                + text +
                                '</div>';
                        }
                    },
                    {
                        "mData": "numOfExecutions",
                        "sClass": "dataTableColumnCenter"
                    },
                    {"mData": "createdBy"},
                    {
                        "mData": "dateCreated",
                        "aTargets": ["dateCreated"],
                        "sClass": "dataTableColumnCenter forceLineWrapDate",
                        "mRender": function (data, type, full) {
                            return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                        }
                    }
                ]
            }).on("draw.dt", function () {
                eventBindingClk();
            }).on('xhr.dt', function (e, settings, json, xhr) {
                checkIfSessionTimeOutThenReload(e, json)
            })
        }
        else {
            $('#rxTableConfiguration').DataTable().order([6, 'desc']).draw();
        }
    });
    $(document).on("click", ".selectChartSection", function (e) {
        e.preventDefault();
        $('#addWidgetModal').modal('hide');
        window.location = CONFIGURATION.addWidgetUrl + "?" +
            $.param({
                id: $("#dashboardId").val(),
                widgetType: 'CHART',
                chartId: $(this).attr("data-id"),
                sectionNumber: $(this).attr("data-number")
            })

    });
    //actionButton('#rxTableConfiguration');
    //loadTableOption('#rxTableConfiguration');
    $('#addDataAnalysisModal').on('show.bs.modal', function (event) {
        if (!$.fn.DataTable.isDataTable('#dataAnalysisTable')) {
            var table = $('#dataAnalysisTable').DataTable({
                layout: {
                    topStart: null,
                    topEnd: "search",
                    bottomStart: [
                        "pageLength",
                        "info",
                        {
                            paging: {
                                type: "full_numbers",
                            },
                        },
                    ],
                    bottomEnd: null,
                },
                language: { search: ''},
                // //"sPaginationType": "bootstrap",
                "customProcessing": true, //handled using processing.dt event
                "serverSide": false,
                "ajax": {
                    "url": spotfireFilesListUrl,
                    "dataSrc": ""
                },
                "aaSorting": [],
                "order": [[1, "desc"]],
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aLengthMenu": [[5, 10, 15], [5, 10, 15]],
                "pagination": true,

                drawCallback: function (settings) {
                    var api = this.api();
                    pageDictionary($('#dataAnalysisTable_wrapper')[0], settings.aLengthMenu[0][0], api.rows().count());
                    getReloader($('#dataAnalysisTable_info'), $("#dataAnalysisTable"));
                },
                "aoColumns": [
                    {
                        "mData": "fileName",
                        mRender: function (data, type, row) {
                            return "<a href='javascript:void(0)' class='selectDataFile' data-file='" + data + "' >" +
                                data + "</a>";
                        }
                    },
                    {
                        "mData": "dateCreated",
                        "aTargets": ["dateCreated"],
                        "sClass": "dataTableColumnCenter forceLineWrapDate",
                        "mRender": function (data, type, full) {
                            return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                        }
                    }
                ]
            }).on('xhr.dt', function (e, settings, json, xhr) {
                checkIfSessionTimeOutThenReload(e, json)
            })
        }
    });
    $(document).on("click", ".selectDataFile", function () {
        $('#addWidgetModal').modal('hide');
        window.location = CONFIGURATION.addWidgetUrl + "?" +
            $.param({
                id: $("#dashboardId").val(),
                widgetType: 'SPOTFIRE',
                file: $(this).attr("data-file")
            })
    });

    let eventBindingClk = (function () {
        $("[data-evt-clk]").on('click', function (e) {
            e.preventDefault();
            const eventData = JSON.parse($(this).attr("data-evt-clk"));
            const methodName = eventData.method;
            const params = eventData.params;
            if (methodName == "disableEventBinding") {
                var eventElement = $(this);
                disableEventBinding(eventElement, params);
            }
        });
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
