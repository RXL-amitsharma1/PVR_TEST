var totalFilteredRecord;
$(function () {
    if ($('#balanceQueryLogResult').is(":visible")) {

        var table = $('#balanceQueryLogResult').DataTable({
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
            "stateDuration": -1,
            "searching": true,
            "processing": true,
            "customProcessing": true,
            "serverSide": true,
            "ajax": {
                "url": listUrl,
                "dataSrc": function(res) {
                    totalFilteredRecord=res["recordsFiltered"];
                    return res["aaData"];
                },
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "pagination": true,
            drawCallback: function (settings) {
                pageDictionary($('#balanceQueryLogResult_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "VALIDATION_TYPE",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "SOURCE_TABLE",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "TARGET_TABLE",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "SRC_COLUMN_NAME",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "TGT_COLUMN_NAME",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "SOURCE_VALUE",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "TARGET_VALUE",
                    mRender: function (data, type, row) {
                        return data
                    }
                },{
                    "mData": "IMPACTED_PK",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "CASE_ID",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "CASE_NUMBER",
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "LAST_UPDATE_TIME",
                    mRender: function (data, type, row) {
                        return row.LAST_UPDATE_TIME ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";

                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#balanceQueryLogResult tbody tr').each(function () {
                    $(this).find('td:eq(8)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(9)').attr('nowrap', 'nowrap');
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#balanceQueryLogResult');
        loadTableOption('#balanceQueryLogResult');
    }
});