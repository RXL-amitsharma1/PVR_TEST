$(function () {
    if ($('#resultTableReports').is(":visible")) {

        var table = $('#resultTableReports').DataTable({
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
            //"sPaginationType": "bootstrap",
            "stateSave": true,
            "stateDuration": -1,
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": showReportUrl,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "resultTableReportsUniqueId",
            "aaSorting": [],
            "order": [[0, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#resultTableReports_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "senderName",
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {
                    "mData": "queryName",
                    "bSortable": false,
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {
                    "mData": "criteriaName",
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {
                    "mData": "caseNum",
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {
                    "mData": "versionNum",
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {
                    "mData": "senderReceiptDate",
                    "mRender": function (data, type, row) {
                        if (data == null) {
                            return data
                        }
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "mData": "safetyRecieptDate",
                    "mRender": function (data, type, row) {
                        if (data == null) {
                            return data
                        }
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "mData": "caseCreationDate",
                    "mRender": function (data, type, row) {
                        if (data == null) {
                            return data
                        }
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "mData": "daysToProcess",
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {
                    "mData": "status",
                    "mRender": function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#resultTableReports tbody tr').each(function () {
                    /*$(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');*/
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#resultTableReports');
        loadTableOption('#resultTableReports');
    }
});