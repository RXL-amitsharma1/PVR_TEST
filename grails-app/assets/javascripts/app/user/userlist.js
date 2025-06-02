var selectedGroupId;
$(function () {

    var table = $('#rxUserSearchResultsTable').DataTable({
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        language: { search: ''},
        // //"sPaginationType": "bootstrap",
        "bFilter": false,
        "stateSave": true,
        "stateDuration": -1,
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": USERSEARCH.listUrl,
            "dataSrc": "data",
            "data": function (d) {
                var params = $('#userSearchForm').serializeArray()
                for (var i = 0; i < params.length; i++) {
                    d[params[i].name] = params[i].value;
                }
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    //Column header mData value extracting
                    d.sort = d.columns[d.order[0].column].data;
                }
            }
        },
        rowId: "id",
        "aaSorting": [],
        "order": [[0, "asc"]],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            showTotalPage(settings.json.recordsFiltered);
            pageDictionary($('#rxUserSearchResultsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        },
        "aoColumns": [
            //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
            {
                "mData": "username",
                "mRender": $.fn.dataTable.render.text()
            },
            {
                "mData": "fullName",
                "mRender": $.fn.dataTable.render.text()
            },
            {
                "mData": "email",
                "mRender": $.fn.dataTable.render.text()
            },
            {
                "mData": "enabled",
                mRender: function (data, type, row) {
                    return $.i18n._((data == true ? 'yes' : 'no'));
                }
            },
            {
                "mData": "lastLogin",
                "bSortable": true,
                "aTargets": ["lastLogin"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    if (data)
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    else
                        return $.i18n._("app.user.neverLoggedIn.label")
                }
            },
            {
                "mData": "roles",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return data.split(",").join("<br>");
                }
            },
            {
                "mData": "userGroups",
                "bSortable": false,
                mRender: function (data, type, row) {
                    if (data.length > 0) {
                        return data.split(",").join("<br>");
                    } else {
                        return "";
                    }
                }
            },
            {
                "mData": null,
                "sClass":"dt-center",
                "bSortable": false,
                "aTargets": ["id"],
                "mRender": function (data, type, full) {
                    if (data.username == "application") {
                        return '';
                    }
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs" href="' + USERSEARCH.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + USERSEARCH.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                        </ul> \
                    </div>';
                    return actionButton;
                }
            }
        ]
    }).on('draw.dt', function () {
        setTimeout(function () {
            $('#rxTableFieldProfile tbody tr').each(function () {
                $(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
            });
        }, 100);
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });
    actionButton('#rxUserSearchResultsTable');
    loadTableOption('#rxUserSearchResultsTable');
    $('#userSearchButton').on('click', function () {
        table.ajax.reload();
    });
    $("#roles").select2({
        placeholder: $.i18n._('placeholder.selectRoles'),
        allowClear: true
    });
    $("#userGroups").select2({
        placeholder: $.i18n._('placeholder.selectUserGroups'),
        allowClear: true
    });
    $("#enabled").select2();
    $("#locale").select2();
    $("#timeZone").select2();

});