$(function () {


    function initTable(selector, sorting, columnts) {

        $(selector).DataTable({
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
            initComplete: function () {
                actionButton(selector);
            },
            "customProcessing": true,
            "serverSide": true,
            "ajax": {
                "url": listUrl,
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                },
            },
            "aaSorting": sorting,
            "bLengthChange": true,

            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,
            "stateSave": true,
            "stateDuration": -1,
            "pagination": true,
            "aoColumns": columnts
        });
    }

    if ($("#helpMessagesList").length > 0) {
        initTable("#helpMessagesList", [[2, "desc"]], [
            {
                "mData": "text"
            },
            {
                "mData": "code",
            },
            {
                "mData": "locale",
            },
            {
                "mData": null,
                "bSortable": false,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs viewHelpMessage" href="javascript:void(0)" data-id="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="Help Message" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.text) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]);
    } else if ($("#systemNotificationList").length > 0) {
        initTable("#systemNotificationList", [[2, "desc"]], [
            {
                "mData": "title"
            },
            {
                "mData": "published",
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, full) {
                    return data == true ? $.i18n._("yes") :  $.i18n._("no");
                }
            },
            {
                "mData": "dateCreated",
            },
            {
                "mData": null,
                "bSortable": false,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs " href="editSystemNotification?id=' + row.id + '" data-id="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="editSystemNotification?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="System Notification" data-action="deleteSystemNotification"  data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.title) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]);
    }else if ($("#interactiveHelpList").length > 0) {
        initTable("#interactiveHelpList", [[3, "desc"]], [
            {
                "mData": "title"
            }, {
                "mData": "page"
            },
            {
                "mData": "published",
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, full) {
                    return data == true ? $.i18n._("yes") :  $.i18n._("no");
                }
            },
            {
                "mData": "dateCreated",
            },
            {
                "mData": null,
                "bSortable": false,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs " href="viewInteractiveHelp?id=' + row.id + '" data-id="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="editInteractiveHelp?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="Interactive Help" data-action="deleteInteractiveHelp"  data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.title) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]);
    } else {
        initTable("#releaseNotesList", [[0, "desc"]], [
            {
                "mData": "releaseNumber"
            },
            {
                "mData": "title",
            },
            {
                "mData": null,
                "bSortable": false,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs " href="readReleaseNotes?fromList=true&id=' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="viewReleaseNotes?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="Release Note" data-action="deleteReleaseNotes" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.text) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]);
    }
    $(document).on("click", ".viewHelpMessage", function () {
        var id = $(this).attr("data-id");
        openLocalizationHelpModal(null, id);
    });
    $(document).on("click", ".updateFileReferences", function () {
        showLoader();
    });
    $('[name=file]').on('change', function (evt, numFiles, label) {
        $("#file_name").val($.map($('[name=file]')[0].files, function (val) {
            return val.name;
        }).join(";"));
    });
});