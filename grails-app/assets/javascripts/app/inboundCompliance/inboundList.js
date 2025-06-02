DATE_RANGE_ENUM = {
    CUSTOM: 'CUSTOM',
    CUMULATIVE: 'CUMULATIVE',
    RELATIVE: 'RELATIVE'
};
X_OPERATOR_ENUMS = {
    LAST_X_DAYS: 'LAST_X_DAYS',
    LAST_X_WEEKS: 'LAST_X_WEEKS',
    LAST_X_MONTHS: 'LAST_X_MONTHS',
    LAST_X_YEARS: 'LAST_X_YEARS',
    NEXT_X_DAYS: 'NEXT_X_DAYS',
    NEXT_X_WEEKS: 'NEXT_X_WEEKS',
    NEXT_X_MONTHS: 'NEXT_X_MONTHS',
    NEXT_X_YEARS: 'NEXT_X_YEARS'
};
DATE_RANGE_TYPE = {
    SUBMISSION_DATE: $.i18n._('app.dataRangeType.submissionDate')
};

$(function () {

    if($("#configurationForm").length == 1) //create or edit page
        init();

    function init() {
        $(document).find("select[name='globalDateRangeInbound.dateRangeEnum']").on("change", function (e) {
            globalDateRangeChangedAction(document);
        }).select2().trigger('change');
    }
    // checkDateRangeType();


    if ($('#rxTableInboundCompliance').is(":visible")) {

        var table = $('#rxTableInboundCompliance').DataTable({
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
                "url": listUrl,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "inboundComplianceUniqueId",
            "aaSorting": [],
            "order": [[6, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#rxTableInboundCompliance_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "senderName",
                    mRender: function (data, type, row) {
                        return (data == null) ? data : ("<div class='three-row-dot-overflow'>" + encodeToHTML(data) + "</div>")
                    }
                },
                {
                    "mData": "description",
                    mRender: function (data, type, row) {
                        return (data == null) ? data :  ("<div class='three-row-dot-overflow'>" + encodeToHTML(data) + "</div>")
                    }
                },
                {
                    "mData": "tags",
                    "bSortable": false,
                    "aTargets": ["tags"],
                    "mRender": function (data, type, full) {
                        var tags = data ? encodeToHTML(data) : '';
                        return "<div class='three-row-dot-overflow'>" + tags + "</div>";
                    }
                },
                {
                    "mData": "qualityChecked",
                    "sClass": "dataTableColumnCenter",
                    "mRender": function (data, type, full) {
                        return data == true ? $.i18n._("yes") : "";
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
                    "sClass":"dt-center",
                    "bSortable": false,
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                                                <a class="btn btn-success btn-xs" href="' + viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                                                <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                                    <span class="caret"></span> \
                                                    <span class="sr-only">Toggle Dropdown</span> \
                                                </button> \
                                                <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                                    <li role="presentation"><a role="menuitem" href="' + editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                                                    \<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                                        data-target="#deleteModal" data-instancetype="' + $.i18n._('configuration') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["senderName"]) + '">' + $.i18n._('delete') + '</a></li> \
                                                    \<li role="presentation"><a role="menuitem" href="' + initializeUrl + '/' + data["id"] + '">' + $.i18n._('initialize') + '</a></li> \
                                                    \
                                                </ul> \
                                            </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#rxTableInboundCompliance tbody tr').each(function () {
                    /*$(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');*/
                });
            }, 100);
            updateTitleForThreeRowDotElements();
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#rxTableInboundCompliance');
        loadTableOption('#rxTableInboundCompliance');
    }

    //For Select2 library
    $(".select2-box").select2({
        placeholder: "-Select One-"
    });
});