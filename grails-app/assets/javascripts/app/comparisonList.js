$(function () {

    function createQueryTable(tableId, status) {
        $("#" + tableId).DataTable({
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
                // $(".dataTables_filter").children().html($.i18n._('searchByName') + ' <input type="search" class="form-control input-sm" placeholder="" aria-controls="' + tableId + '" maxlength="1000">')
            },
            "serverSide": true,
            "ajax": {
                "url": queueListURL,
                "dataSrc": "data",
                "data": function (d) {
                    d.status = status;
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,

            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,
            "customProcessing": true,
            "stateSave": true,
            "stateDuration": -1,
            "pagination": true,
            "aoColumns": [
                {
                    "mData": "entityName1",
                    mRender: function (data, type, row) {
                        return "<a href='" + viewReportURL + "?id=" + row["entityId1"] + "'>" + data + "</a>";
                    }
                },
                {
                    "mData": "entityName2",
                    mRender: function (data, type, row) {
                        return "<a href='" + viewConfigURL + "?id=" + row["entityId2"] + "'>" + data + "</a>";
                    }
                },
                {
                    "mData": "status",
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                },
                {
                    "mData": "dateCreated",
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "bSortable": false,
                    "mData": "message",
                    mRender: function (data, type, row) {
                        var text = (data == null) ? '' : encodeToHTML(data);
                        return '<div class="comment">'
                            + text +
                            '</div>';
                    }
                }
            ],
            drawCallback: function (settings) {
                pageDictionary($('#' + tableId + '_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                getReloader($('#' + tableId + '_wrapper .dataTables_info'), $("#" + tableId));
            },
        }).on('draw.dt', function () {
            addReadMoreButton('#' + tableId + ' .comment', 100)
        });
    }

    createQueryTable("queueList", "WAITING");

    var resultTable = $("#resultList").DataTable({
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
        //"sPaginationType": "bootstrap",
        initComplete: function () {
            //  $(".dataTables_filter").children().html($.i18n._('searchByName')+' <input type="search" class="form-control input-sm" placeholder="" aria-controls="queueList" maxlength="1000">')
        },
        "serverSide": true,
        "ajax": {
            "url": resultListURL,
            "dataSrc": "data",
            "data": function (d) {
                d.searchString = d.search.value;
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    d.sort = d.columns[d.order[0].column].data;
                }
            }
        },
        "aaSorting": [[3, "desc"]],
        "bLengthChange": true,

        "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
        "iDisplayLength": 10,
        "customProcessing": true,
        "stateSave": true,
        "stateDuration": -1,
        "pagination": true,
        "aoColumns": [

            {
                "mData": "entityName1",
                mRender: function (data, type, row) {
                    return "<a href='" + viewReportURL + "?id=" + row["entityId1"] + "'>" + data + "</a>";
                }
            },
            {
                "mData": "entityName2",
                mRender: function (data, type, row) {
                    return "<a href='" + viewReportURL + "?id=" + row["entityId2"] + "'>" + data + "</a>";
                }
            },
            {
                "mData": "result",
                "sClass": "forceLineWrapDate",
                mRender: function (data, type, row) {
                    if (row.supported) {
                        if (data) {
                            return '<span style="color: green">' + $.i18n._('comparison.equals') + '</span>' + (row.message ? ("(" + row.message + ")") : "");
                        }
                        return "<a style='color: red; text-decoration: underline' href='" + viewComporisonURL + "?id=" + row["id"] + "'>" + $.i18n._('comparison.notequals') + "</a>" + (row.message ? ("(" + row.message + ")") : "");
                    } else {
                        return '<span style="color: red">' + $.i18n._('comparison.notsupported') + '</span>' + (row.message ? ("(" + row.message + ")") : "");
                    }
                }
            },
            {
                "mData": "dateCreated",
                "sClass": "forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            }
        ],
        drawCallback: function (settings) {
            pageDictionary($('#resultList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            getReloader($('#resultList_wrapper .dataTables_info'), $("#resultList"));
        },
    }).on('draw.dt', function () {
        addReadMoreButton('.comment', 100);
    });
    bindSelect2WithUrl($("#caseSeriesId"), executedCaseSeriesListUrl, executedCaseSeriesItemUrl, true);
    var nextRunDate = null;

    if ($("#nextRunDate").val()) {
        nextRunDate = $("#nextRunDate").val();
    }

    $('#nextRunDateDatePicker,#nextRunDateDatePicker2').datepicker({
        allowPastDates: false,
        date: nextRunDate,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    var tomorrow = new Date();
    tomorrow = new Date(tomorrow.setDate(tomorrow.getDate() + 1));
    $(' #fromDate,#toDate').datepicker({
        allowPastDates: true,
        restricted: [
            {
                from: tomorrow,
                to: Infinity
            }],
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $(".showLoader").on("click", function () {
        showLoader();
    });

    $("form input").keydown(function(event){
        if (event.keyCode === 13 && event.shiftKey) {
            $(this).closest('form').submit();
            event.preventDefault();
        }
    });
});