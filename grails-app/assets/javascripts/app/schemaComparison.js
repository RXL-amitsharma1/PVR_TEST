function createDataTableForDiffTable(tableElementId, dbTableName, fields) {
    var tableFilter = {};
    var advancedFilter = false;
    var allTableFilters
    $(function () {
        var columns = [];
        var filter_data = [];
        var header = []
        for (var i = 0; i < fields.length; i++) {
            columns.push({
                "bSortable": fields[i].isSortable,
                "mData": fields[i].name
            });
            filter_data.push({
                label: fields[i].label,
                type: 'text',
                name: fields[i].name
            });
            header.push("<th>" + fields[i].label + "</th>");
        }
        var tableElement = $("#" + tableElementId);
        tableElement.empty();
        tableElement.html("<thead><tr>" + header.join() + "</tr></thead>");

        var dataTable = $("#" + tableElementId).DataTable({
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
            initComplete: function () {
            },
            "serverSide": true,
            "ajax": {
                type: 'POST',
                "url": dataDiffListURL,
                "dataSrc": "data",
                "data": function (d) {
                    d.table = dbTableName;
                    d.advancedFilter = advancedFilter;
                    d.searchString = d.search.value;
                    d.tableFilter = JSON.stringify(tableFilter);
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                    allTableFilters = d;
                }
            },
            "aaSorting": [[0, "asc"]],
            "bLengthChange": true,
            "scrollX": true,
            "scrollCollapse": true,
            "aLengthMenu": [[25, 50, 100], [25, 50, 100]],
            "iDisplayLength": 25,
            "customProcessing": true,
            "stateSave": true,
            "stateDuration": -1,
            "pagination": true,

            "aoColumns": columns,
            drawCallback: function (settings) {
                pageDictionary($('#' + tableElementId + '_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                getReloader($('#' + tableElementId + '_wrapper .dataTables_info'), $("#" + tableElementId));
            },
        }).on('draw.dt', function () {
            addReadMoreButton('#' + tableElementId + ' .comment', 100)
        });


        pvr.filter_util.construct_right_filter_panel({
            table_id: '#' + tableElementId,
            container_id: (tableElementId + 'Cnt'),
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                dataTable.ajax.reload(function (data) {
                }, false).draw();
            }
        });
        loadTableOption("#" + tableElementId);
        $('#' + tableElementId).closest(".rxmain-container").find(".dropdown").append("<i style='cursor: pointer; color: #000000' class='fa fa-download exportButton pull-right pt-5 p-r-5'></i>");
        $(document).on("click", ".exportButton", function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (allTableFilters) {
                $("#table").val(allTableFilters.table);
                $("#advancedFilter").val(allTableFilters.advancedFilter);
                $("#searchString").val(allTableFilters.searchString);
                $("#tableFilter").val(allTableFilters.tableFilter);
                $("#direction").val(allTableFilters.direction);
                $("#sort").val(allTableFilters.sort);
            }
            $("#exportForm").trigger('submit');
        });

    });
}

function initDataComparison() {
    $(function () {
        var dataTable = $("#objectList").DataTable({
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
            initComplete: function () {
            },
            "serverSide": false,
            "aaSorting": [[0, "asc"]],
            "bLengthChange": true,

            "aLengthMenu": [[25, 50, 100], [25, 50, 100]],
            "iDisplayLength": 25,
            "customProcessing": true,
            "stateSave": true,
            "stateDuration": -1,
            "pagination": true,

        });

        $(".select2").select2();
    });
}