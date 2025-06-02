$(function () {

    var init = function () {
        init_table();
    };

    var init_table = function () {
        var table = $("#publisherTemplateParameterList").DataTable({

            // "sPaginationType": "bootstrap",
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
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "searchPlaceholder": $.i18n._("fieldprofile.search.label"),
            },

            initComplete: function () {
            },

            "ajax": {
                "url": listPublisherTemplateParameterUrl,
                "dataSrc": function (res) {
                    totalFilteredRecord = res["recordsFiltered"];
                    return res["aaData"];
                },
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "stateSave": true,
            "stateDuration": -1,
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            //"searching": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "pagination": true,
            "aaSorting": [[1, "asc"]],

            drawCallback: function (settings) {
                pageDictionary($('#publisherTemplateParameterList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "id",
                    "visible": false,
                },
                {
                    "mData": "name"
                },
                {
                    "mData": "hidden",
                    "mRender": function (data, type, full) {
                        return data ? $.i18n._('yes') : " "
                    }
                },
                {
                    "mData": "type"
                },
                {
                    "mData": "title"
                },
                {
                    "mData": "description",
                },
                {
                    "mData": "value",
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#publisherTemplateParameterList tbody tr').each(function () {
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        loadTableOption('#publisherTemplateParameterList');
    }
    init();
});