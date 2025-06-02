$(function () {
    var table = $('#rxTableSpoftfireFiles').DataTable({
        //"sPaginationType": "bootstrap",
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
        language: {search: ''},
        "stateSave": true,
        "stateDuration": -1,
        "customProcessing": true, //handled using processing.dt event
        "serverSide": false,
        "pagination": true,
        "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
        "iDisplayLength": 10,

        drawCallback: function (settings) {
            var api = this.api();
            pageDictionary($('#rxTableSpoftfireFiles_wrapper')[0], settings.aLengthMenu[0][0], api.rows().count());
        },
        initComplete: function () {
            $('#rxTableSpoftfireFiles tbody tr').each(function () {
                $(this).find('td:eq(1)').attr('nowrap', 'nowrap');
                $(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
            });
        },
        "ajax": {
            "url": spotfireFilesListUrl,
            "dataSrc": ""
        },
        "aaSorting": [[3, "desc"]],
        "bLengthChange": true,
        "aoColumns": [
            {
                "mData": "fileName",
                "mRender": function (data, type, row) {
                    return (data.startsWith("B_") || data.startsWith("U_")) ? data.substring(2) : data;
                }
            },
            {
                "mData": "executionTime",
                "sClass": "dataTableColumnRight padding-right-20",
                "mRender": function (data, type, row) {
                    if (type === "display") {
                        return data && (data.length < 11) ? timeStringToUserTime(data, ':') : 'Not recorded';
                    } else {
                        return data && (data.length < 11) ? timeToSeconds(data) : 0;
                    }
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
            {
                "mData": "dateAccessed",
                "aTargets": ["dateAccessed"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    if (!data || data == undefined || data == "") {
                        return ""
                    }
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": null,
                "sClass": "dt-center",
                "bSortable": false,
                "aTargets": ["fileName"],
                "mRender": function (data, type, full) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> ' +
                        '<a class="btn btn-success btn-xs" target="_blank" ' +
                        'href="' + spotfireFileViewUrl + '?fileName=' + libraryRoot + "/" +
                        data["encodedFileName"] + '">' + $.i18n._('view') + '</a></div>';
                    return actionButton;
                }
            }
        ]
    });
    actionButton('#rxTableSpoftfireFiles');
    loadTableOption('#rxTableSpoftfireFiles');
});

function timeStringToUserTime(timeString, seperator) {
    var rawTime = timeString ? timeString.split(seperator) : [0, 0, 0];
    var hours = parseInt(rawTime[0]);
    var minutes = parseInt(rawTime[1]);
    var seconds = parseInt(rawTime[2]);
    var time = '';
    if (hours === 1) {
        time += hours + $.i18n._('hour');
    } else if (hours !== 0) {
        time += hours + $.i18n._('hours');
    }
    if (minutes === 1) {
        time += " " + minutes + $.i18n._('minute');
    } else if (minutes !== 0) {
        time += " " + minutes + $.i18n._('minutes');
    }
    if (seconds === 1) {
        time += " " + seconds + $.i18n._('second');
    } else if (seconds !== 0) {
        time += " " + seconds + $.i18n._('seconds');
    }
    return time;
}

function timeToSeconds(time) {
    time = time.split(/:/);
    return time[0] * 3600 + time[1] * 60 + time[2];
}