var totalFilteredRecord;
$(function () {
    if ($('#balanceQueryResult').is(":visible")) {

        var table = $('#balanceQueryResult').DataTable({
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
            "searching": false,
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
            "aaSorting": [],
            drawCallback: function (settings) {
                pageDictionary($('#balanceQueryResult_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "VALIDATION_KEY",
                    "bSortable": false,
                    mRender: function (data, type, row) {
                        return data
                    }
                },
                {
                    "mData": "VALIDATION_VALUE",
                    "bSortable": false,
                    mRender: function (data, type, row) {
                        return data
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#balanceQueryResult tbody tr').each(function () {
                    /*$(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');*/
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#balanceQueryResult');
        loadTableOption('#balanceQueryResult');
    }

    $(document).on('change', '#sourceProfile', function () {
        var sourceProfileId = this.value
        var url = window.location.href.split("?")[0] + "?sourceProfileId=" +sourceProfileId;
        window.location.href = url;
    });

});