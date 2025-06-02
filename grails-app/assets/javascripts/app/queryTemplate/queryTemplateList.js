var checkedQueriesIds = [];
var checkedTemplatesIds = [];
var checkedDashboardsIds = [];
var checkedConfigurationsIds = [];
queryTemplateList = (function () {

    var query_template_table;

    var init_query_template_table = function (url, tableId, viewQueryTemplateUrl) {
        query_template_table = $(tableId).DataTable({
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
            "language": {
                "search": '',
                "searchPlaceholder": $.i18n._("fieldprofile.search.label")
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "stateSave": true,
            "stateDuration": -1,
            "aaSorting": [[2, "asc"]],
            "bLengthChange": true,

            "aLengthMenu": [[50, 100, 150, 200], [50, 100, 150, 200]],
            "iDisplayLength": 50,

            "aoColumns": [
                {
                    "mData": "id",
                    "visible": false,
                    "mRender": function (data, type, row) {
                        return '<span id="actionItemId">' + row.id + '</span>';
                    }
                },
                {
                    "mData": "select",
                    "bSortable": false,
                    "mRender": function (data, type, row) {
                        return '<input class="downloadJsonChk ' + tableId.substring(1) + '" type="checkbox" value=' + row.id + ' style="cursor: pointer"/>';

                    }
                },
                {
                    "mData": "type"
                },
                {
                    "mData": "name",
                    "mRender": function (data, type, row) {
                        return '<span><a href="' + viewQueryTemplateUrl + '?id=' + row.id + '" target="_blank">' + encodeToHTML(row.name) + '</a></span>';
                    }
                },
                {
                    "mData": "qualityChecked",
                    "mRender": function (data, type, full) {
                        return data == true ? $.i18n._("yes") : "";
                    }
                },
                {
                    "mData": "owner.fullName"
                },
                {
                    "mData": "dateCreated",
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "mData": "lastUpdated",
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                }
            ]
        });
        return query_template_table;
    };

    return {
        init_query_template_table: init_query_template_table
    }

})();

$(function () {
    initQueryTemplateTable();
    bindCheckboxEventsOnTableLoad("#queryListTable", checkedQueriesIds);
    bindCheckboxEventsOnTableLoad("#templateListTable", checkedTemplatesIds);
    bindCheckboxEventsOnTableLoad("#configurationListTable", checkedConfigurationsIds);
    bindCheckboxEventsOnTableLoad("#dashboardListTable", checkedDashboardsIds);
    $("#downloadQueryJSONbtn, #downloadTemplateJSONbtn, #downloadDashboardJSONbtn, #downloadConfigurationJSONbtn").on('click', function (e) {
        submitTemplateQueryJsonForm(e);
    });

    function initQueryTemplateTable() {
        queryTemplateList.init_query_template_table("queryList", "#queryListTable", viewQueryUrl);
        queryTemplateList.init_query_template_table("templateList", "#templateListTable", viewTemplateUrl);
        queryTemplateList.init_query_template_table("configurationList", "#configurationListTable", viewConfigurationUrl);
        queryTemplateList.init_query_template_table("dashboardList", "#dashboardListTable", viewDashboardUrl);
    }

    function bindCheckboxEventsOnTableLoad(id, checkedIds) {
        $(document).on("change", id + " .downloadJsonChk", function () {
            var value = $(this).val();
            var index = _.findIndex(checkedIds, function (item) {
                return item.id == value;
            })
            if ($(this).is(":checked")) {
                if (index < 0) {
                    checkedIds.push({
                        'id': value
                    });
                }
            } else {
                if (index > -1) checkedIds.splice(index, 1);
            }
            var dataTable = $(id).DataTable();
            var selectAllCheckbox = $(dataTable.table().container()).find(".selectAll");
            var totalEntries = dataTable.rows({search: 'applied'}).nodes().length;
            var allChecked = totalEntries === checkedIds.length;
            selectAllCheckbox.prop("checked", allChecked);
        });

        $("[value='" + id.substring(1) + "'].selectAll").on('change', function () {
            var table = $(id).DataTable();
            var filteredRowsNodes = table.rows({search: 'applied'}).nodes();
            var checkboxesToToggle = $(filteredRowsNodes).find(".downloadJsonChk");
            var selectAllChecked = $(this).is(":checked");
            checkboxesToToggle.each(function () {
                $(this).prop("checked", selectAllChecked);
                var value = $(this).val();
                var index = _.findIndex(checkedIds, function (item) {
                    return item.id == value;
                })
                if (selectAllChecked) {
                    if (index < 0) {
                        checkedIds.push({
                            'id': value
                        });
                    }
                } else {
                    if (index > -1) checkedIds.splice(index, 1);
                }
            });
        });
    }

    function submitTemplateQueryJsonForm(e) {
        var data;
        var url;
        var id = $(e.target).attr('id')
        if (id == "downloadTemplateJSONbtn") {
            data = JSON.stringify(checkedTemplatesIds);
            url = downloadTemplateJSONUrl;
        } else if (id == "downloadQueryJSONbtn") {
            data = JSON.stringify(checkedQueriesIds);
            url = downloadQueryJSONUrl;
        } else if (id == "downloadConfigurationJSONbtn") {
            data = JSON.stringify(checkedConfigurationsIds);
            url = downloadConfigurationJSONUrl;
        } else if (id == "downloadDashboardJSONbtn") {
            data = JSON.stringify(checkedDashboardsIds);
            url = downloadDashboardJSONUrl;
        }
        $.ajax({
            type: "POST",
            url: url,
            data: {selectedIds: data}
        })
            .done(function (resp) {
                window.location.href = renderFileUrl + "?name=" + resp
            });
    }
});
