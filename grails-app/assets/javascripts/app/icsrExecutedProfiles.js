$(function () {

    var table = $('#icsrProfilesList').DataTable({
        //"sPaginationType": "bootstrap",
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: $.i18n._("fieldprofile.search.label")}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        language: { search: ''},
        "stateSave": true,
        "stateDuration": -1,
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": ICSRPROFILECONF.profileListUrl,
            "type": "POST",
            "dataSrc": "data",
            "data": function (d) {
                d.searchString = d.search.value;
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    d.sort = d.columns[d.order[0].column].data;
                }
            }
        },
        rowId: "IcsrProfilesConfUniqueId",
        "aaSorting": [],
        "order": [[8, "desc"]],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,
        "columnDefs": [
            {"visible": false, "targets": [2]}
        ],
        drawCallback: function (settings) {
            pageDictionary($('#icsrProfilesList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        },
        "aoColumns": [
            {
                "mData": "reportName",
                mRender: function (data, type, row) {
                    return '<a href="' + ICSRPROFILECONF.reportViewUrl + '?id=' + row.id + '" >' + encodeToHTML(data)+ '</a>';
                }
            },
            {
                "data": "numOfExecutions"
            },
            {
                "mData": "description",
                "mRender": function (data, type, row) {
                    return '<span class="description">' + row.description ? encodeToHTML(row.description) : "" + '</span>';
                }
            },
            {
                "mData": "senderOrganizationName",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "senderTypeName",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "recipientOrganizationName",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "recipientTypeName",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "lastUpdated",
                "aTargets": ["lastUpdated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {"mData": "createdBy"},
            {
                "mData": null,
                "sClass": "dt-center",
                "bSortable": false,
                "aTargets": ["id"],
                "mRender": function (data, type, full) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                                                    <a class="btn btn-success btn-xs" href="' + ICSRPROFILECONF.profileViewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                                                     \
                                                </div>';
                    return actionButton;
                }
            }
        ]
    }).on('draw.dt', function () {
        setTimeout(function () {
            $('#icsrProfilesList tbody tr').each(function () {
                // $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
            });
        }, 100);
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });
    actionButton('#icsrProfilesList');
    loadTableOption('#icsrProfilesList');
});










