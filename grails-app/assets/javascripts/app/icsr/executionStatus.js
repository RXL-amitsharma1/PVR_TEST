$(function () {
    var table = $('#rxTableReportsExecutionStatus').DataTable({
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
        initComplete: function () {
            getReloader($("#rxTableReportsExecutionStatus"));
        },
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": executionStatusUrl,
            "dataSrc": "data",
            "data": function (d) {
            }
        },
        searching: false,
        paging: false,
        "aaSorting": [],
        //we would need to change in checkStatus function as well while changing default order
        "order": [[4, "desc"]],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
            pageDictionary($('#rxTableReportsExecutionStatus_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        },
        "aoColumns": [
            {
                "mData": "reportName",
                "bSortable": false,
                "aTargets": ["reportName"],
                "mRender": function (data, type, row) {
                    var link = showConfigUrl + '/' + row.configId;
                    if (data) {
                        data = data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                    return '<a href=' + link + '>' + data + '</a>'
                }
            },
            {
                "mData": "caseNumber",
                "bSortable": false,
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "versionNumber",
                "bSortable": false,
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "executionStatus",
                "aTargets": ["executionStatus"],
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return '<div><i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="' + $.i18n._('Generating') + '"></i><span class="glyphicon glyphicon-stop" data-id="' + row.id + '" data-actionUrl="' + killExecutionUrl + '"></span></div>'
                }
            },
            {
                "mData": "runDate",
                "bSortable": false,
                "aTargets": ["runDate"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, row) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            }
        ]

    }).on('draw.dt', function () {
        setTimeout(function () {
            $('#rxTableReportsExecutionStatus tbody tr').each(function () {
                $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
            });
        }, 100)
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
    });

    $('#rxTableReportsExecutionStatus').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            placement: 'right',
            trigger: 'hover focus',
            viewport: '#rxTableReportsExecutionStatus',
            html: true
        });
    });

    $('#rxTableReportsExecutionStatus').on('click', '.glyphicon-stop', function () {
        if (confirm($.i18n._('cancel.execution'))) {
            $.ajax({
                url: $(this).attr('data-actionUrl'),
                data: {id: $(this).data('id')},
                dataType: 'json'
            })
                .done(function (result) {
                    if (result.success) {
                        setTimeout(function () {
                            $("#rxTableReportsExecutionStatus").DataTable().draw();
                        }, 1500)
                    }
                })
                .fail(function (err) {
                    var responseText = err.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    alert(responseTextObj.message);
                });
        }
    });

    loadTableOption('#rxTableReportsExecutionStatus');
    $('.outside').hide();

});

function getReloader(tableName) {
    if (tableName != undefined) {
        $('.reloaderBtn').on('click', function () {
            $('.reloaderBtn').addClass('glyphicon-refresh-animate');
            $(tableName).DataTable().draw();
        });
    }
}