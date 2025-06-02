var caseObj = caseObj || {};

caseObj.caseList = (function () {

    var case_table;

    //The function for initializing the case tables.
    var init_case_list_table = function () {
        console.log("Initializing detailed case list table...");
        var columns = [{}, {}, {"data": "CASE_NUM"}, {"data": "VERSION_NUM"}, {}];
        var aocolumns = [
            {
                "className": 'details-control bold-font',
                "width": "50px",
                "orderable": false,
                "data": null,
                "defaultContent": '<span class=\'fa fa-angle-down\'></span>'
            },
            {
                "mData": "",
                "bSortable": false,
                "sWidth": "5%",
                "mRender": function (data, type, row) {
                    console.log("Rendering detailed tags for row:", row);
                    return showTags(row)
                }
            },
            {
                "mData": "CASE_NUM",
                "sWidth": "8%",
                "mRender": function (data, type, row) {
                    console.log(`Rendering detailed case number: ${data}, Version: ${row.VERSION_NUM}`);
                    return '<span class="caseNumberElement"><a href="' + caseObjConfig.caseDataLinkUrl + '?caseNumber=' + data + '&versionNumber=' + row.VERSION_NUM + '&cid=' + caseObjConfig.caseSeriesId + '" target="_blank">' + data + '</a></span>';
                }
            },
            {
                "mData": "VERSION_NUM",
                "sWidth": "8%",
                "visible": caseObjConfig.showVersionColumn,
                "mRender": function (data, type, row) {
                    console.log(`Rendering detailed version number: ${data}`);
                    return '<span class="versionNumberElement">' + data + '</span>';
                }
            },
            {
                "mData": "",
                "bSortable": false,
                "sClass": 'dataTableColumnCenter',
                "sWidth": "15%",
                "mRender": function (data, type, row) {
                    console.log("Rendering detailed tags in table for row:", row);
                    var tags = showTagsInTable(row);
                    return tags;
                }
            }];

        $.each(JSON.parse(caseObjConfig.additionalColumns), function (key, value) {
            console.log(`Processing detailed column: ${key}, Visible: ${value}`);
            if (value.visible) {
                columns.push({"data": key});
                if (value.type == 'Date') {
                    aocolumns.push({
                        "mData": key,
                        "aTargets": [key],
                        "sClass": "dataTableColumnCenter mw-100",
                        'visible': value.defaultHide ? false : true,
                        "mRender": function (data, type, full) {
                            console.log(`Rendering date column: Data: ${data}`);
                            if (data) {
                                return moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT);
                            } else {
                                return ""
                            }
                        }
                    });
                } else {
                    aocolumns.push({
                        "mData": key,
                        "sClass": "mw-100",
                        'visible': value.defaultHide ? false : true
                    });
                }
            }

        });

        //Disable secondary fields if they are not enabled click.
        var secondaryColumns = JSON.parse(caseObjConfig.secondaryColumns);
        if ($.isEmptyObject(secondaryColumns)) {
            aocolumns[0] = {
                "orderable": false,
                "data": null,
                "defaultContent": ''
            }
        }

        //Initialize the data table
        var state_key = caseObjConfig.userid;
        case_table = $("#caseList").DataTable({
            orderCellsTop: true,
            fixedHeader: true,
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
            "stateSave": true,
            stateSaveCallback: function (settings, data) {
                localStorage.setItem(('Datatable-Detailed-' + state_key), JSON.stringify(data));
            },
            stateLoadCallback: function () {
                var stateData = localStorage.getItem(('Datatable-Detailed-' + state_key));
                try {
                    if (stateData) {
                        var previousState = JSON.parse(stateData);
                        previousState.start = 0;
                        if (previousState.search) {
                            previousState.search.search = '';
                        }
                        return previousState
                    }
                } catch (e) {
                    console.error("Error while loading previous state : " + (stateData) + " error: " + e);
                }
                console.log("### Resetting Local Storage Values as not found or error ###");
                return JSON.parse('{}')
            },
            "stateDuration": -1, // (localStorage: 0 or greater, or sessionStorage: -1). made -1 as then state cleanup won't get called and would depend on localstorage only.
            //"sPaginationType": "bootstrap",
            "pagination": true,

            drawCallback: function (settings) {
                pageDictionary($('#caseList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                getReloader($('#caseList_info'), $("#caseList"));
                openCaseListTagModal();
                closeCaseListTagModal();
                $("#caseList").height($(window).height() - $("#caseList").offset().top - 90);
            },
            initComplete: function () {
                openCaseListTagModal();
                closeCaseListTagModal();
                bindExpansion($("#caseList"));
                $('#caseList thead tr').clone(true).appendTo('#caseList thead').addClass('column_filter_input').hide();
                $('#caseList thead tr:eq(1) th').each(function (i) {
                    var title = $(this).text();
                    var id = $(this).data('id');
                    if (id) {
                        $(this).html('<input type="text" class="form-control" style="width: 98%" placeholder="Search ' + title + '" data-id=' + id + ' />');
                        var timer = null;
                        $('input', this).on("keyup change", function (e) {
                            if (timer)
                                clearTimeout(timer);
                            timer = setTimeout(function () {
                                var searchJSON = {};
                                $("#caseList thead tr:eq(1) th input").each(function (i) {
                                    searchJSON[$(this).attr('data-id')] = $(this).val();
                                });
                                var searchString = JSON.stringify(searchJSON);
                                if (searchString != case_table.search()) {
                                    case_table.search(searchString).draw();
                                }
                            }, 800);
                        });
                    } else {
                        $(this).html('');
                    }
                });
            },
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": caseObjConfig.listUrl,
                "dataSrc": "data",
                "type": "post",
                "data": function (d) {
                    console.log("Sending detailed AJAX data request with:", d);
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "CASE_UNIQUE_ID",

            "aaSorting": [],
            //"aaSorting": [ [0,'asc'], [1,'asc'] ],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "order": [[2, "desc"]],
           // "columns": columns,
            "aoColumns": aocolumns
        });

        var state = case_table.state.loaded();
        if (state && state.search && state.search.search) {
            var searchValue = false;
            $.each(JSON.parse(state.search.search), function (key, value) {
                if (value) {
                    searchValue = true
                }
                $('#caseList thead tr:eq(1) th').find('input[data-id="' + key + '"]').val(value);
            });
            if (searchValue) {
                $('#caseList thead tr.column_filter_input').show();
            }
        }

        $('.column_filter_input').on('removeSearch', function () {
            var hasSearchData = false;
            $("#caseList thead tr:eq(1) th input").each(function (i) {
                if ($(this).val()) {
                    hasSearchData = true;
                }
                $(this).val('');
            });
            if (hasSearchData) {
                case_table.search("").draw();
            }
        });
        console.log("DataTable detailed case series initialized successfully!");
        return case_table;
    };

    function openCaseListTagModal() {
        $('.editTags').off().on('click', function () {
            var caseListTagModal = $("#caseListTagModal");
            var caseNumber = $(this).attr("data-case-number-id");
            var caseSeriesTags = $(this).attr("data-rowCaseSeriesTags").split(",");
            var globalTags = $(this).attr("data-rowGlobalTags").split(",");
            var caseNumberValue = $(this).attr("data-case-number");
            var oldCaseLevelTags = caseListTagModal.find('#caseLevelTags').val().join(",")
            var oldGlobalLevelTags = caseListTagModal.find('#globalTags').val().join(",")

            caseListTagModal.modal("show");
            var selectCaseLevelTags = bindMultipleSelect2WithUrl($("#caseLevelTags"), caseObjConfig.fetchAllTags, true, $.i18n._('placeholder.case.level.tags'), "A");
            var selectGlobalTags = bindMultipleSelect2WithUrl($("#globalTags"), caseObjConfig.fetchAllTags, true, $.i18n._('placeholder.global.level.tags'), "G");

            selectCaseLevelTags.parent().find(".select2-drop").on("click", "#addNewCaseLevelTag", function () {
                addNewTags($("#newCaseLevelTag"), "#caseLevelTags");
            });

            selectGlobalTags.parent().find(".select2-drop").on("click", "#addNewGlobalLevelTag", function () {
                addNewTags($("#newGlobalLevelTag"), "#globalTags");
            });

            caseListTagModal.find('#caseLevelTags').val(caseSeriesTags).trigger('change');
            caseListTagModal.find('#globalTags').val(globalTags).trigger('change');
            caseListTagModal.find(".addTags").off().on('click', function () {
                $.ajax({
                    url: caseObjConfig.updateTags,
                    data: {
                        caseLevelTags: caseListTagModal.find('#caseLevelTags').val().split(MULTIPLE_AJAX_SEPARATOR).join(","),
                        globalTags: caseListTagModal.find('#globalTags').val().split(MULTIPLE_AJAX_SEPARATOR).join(","),
                        caseNumber: caseNumber,
                        caseNumberValue: caseNumberValue,
                        oldCaseLevelTags: oldCaseLevelTags,
                        oldGlobalTags: oldGlobalLevelTags
                    },
                    contentType: 'application/json',
                    dataType: 'json'
                })
                    .done(function (result) {
                        caseListTagModal.modal('hide');
                        location.reload();
                    })
                    .fail(function (err) {
                        errorNotification(err.message);
                    });
            });
        });
    }


    function addNewTags(id, tagId) {
        var newTerm = id.val();
        newTerm = encodeToHTML(newTerm);
        if ($.trim(newTerm) != "") {
            $("<option>" + newTerm + "</option>").appendTo(tagId);
            var selectedItems = $(tagId).select2("val");
            selectedItems.push(newTerm);
            $(tagId).select2("val", selectedItems);
            $(tagId).select2("close");
        } else {
            id.val("");
            $(tagId).select2("close");
        }
    }

    var showTagsInTable = function (row) {
        var tagsElement = '<div class="tag-container">';
        if (row.ALERT_TAG_TEXT) {
            $.each(row.ALERT_TAG_TEXT, function (key, value) {
                tagsElement += '<span class="badge badge-info">' + value.split(MULTIPLE_AJAX_SEPARATOR) + '</span>'
            });
        }
        if (row.GLOBAL_TAG_TEXT) {
            $.each(row.GLOBAL_TAG_TEXT, function (key, value) {
                tagsElement += '<span class="badge badge-info">' + value.split(MULTIPLE_AJAX_SEPARATOR) + '&nbsp;' + '<i class="fa fa-globe"> ' + '</i>' + '</span>'
            });
        }
        tagsElement += '<div></div>';
        tagsElement += '<span class="editTags glyphicon glyphicon-edit" style="cursor: pointer; margin-left: 10px;" data-rowCaseSeriesTags="' + row.ALERT_TAG_TEXT + '" data-rowGlobalTags="' + row.GLOBAL_TAG_TEXT + '" data-case-number-id="' + row.CASE_ID + '" data-case-number="' + row.CASE_NUMBER + '">';
        tagsElement += '</span>';
        tagsElement += '</div>';
        return tagsElement
    };

    function closeCaseListTagModal() {
        var caseListTagModal = $("#caseListTagModal");
        caseListTagModal.find('.closeTagModal').on('click', function () {
            caseListTagModal.modal('hide');
            caseListTagModal.find("#caseLevelTags").val(null).trigger('change');
            caseListTagModal.find("#globalTags").val(null).trigger('change');
        });
    }

    function bindExpansion(table) {
        table.on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = case_table.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
                $(this).html("<span class='fa fa-angle-down'></span>");
            } else {
                // Open this row
                row.child(spanRowFormat(row.data())).show();
                tr.addClass('shown');
                $(this).html("<span class='fa fa-angle-up'></span>")
            }
        });
    }

    var reloadData = function (rowId, resetPagination) {
        if (resetPagination != true) {
            resetPagination = false
        }
        case_table.ajax.reload(function () {
            highlightRow(rowId, dataTable);
        }, resetPagination);
    };

    var highlightRow = function (rowId, dataTable) {
        if (rowId != undefined && rowId != "") {
            dataTable.row('#' + rowId).nodes()
                .to$()
                .addClass('flash-row');
        }
    };

    var showTags = function (row) {
        var iconHtml = "";
        if (row.NEW_CASE_FLAG) {
            iconHtml = iconHtml + '<span class="glyphicon glyphicon-tag" style="color: green"></span>';
        }
        if (row.ADDED_MANUAL_FLAG) {
            iconHtml = '<div class="annotationPopover" ><i class="showPopover"' +
                'data-content="' + row.justification + '" data-placement="right" ' +
                'title="' + $.i18n._('app.caseList.justification') + '">' +
                '<span class="glyphicon glyphicon-tag" style="color: purple"></span></i></div>';

        }
        if (row.UNLOCKED_TO_LOCKED_FLAG) {
            iconHtml = iconHtml + '<span class="glyphicon glyphicon-tag" style="color: darkblue"></span>';
        }
        if (row.HIGHER_VERSION_FLAG) {
            iconHtml = iconHtml + '<span class="glyphicon glyphicon-tag" style="color: orange"></span>';
        }
        return iconHtml
    };

    var spanRowFormat = function (d) {
        var result = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px; border-bottom: 1px solid #ccc;border-top: 1px solid #ccc; width: 100%">';
        $.each(JSON.parse(caseObjConfig.secondaryColumns), function (key, value) {
            if (value.label) {
                var val = d[key];
                if (value.type == 'Date' && val) {
                    val = moment(val).format(DEFAULT_DATE_DISPLAY_FORMAT);
                }
                result += '<tr>' +
                    '<td><label class="col-min-150"><b>' + value.label + ':</b></label> <span>' + (val ? (val) : '') + '</span></td>' +
                    '</tr>'
            }

        });
        result += '</table>';
        return result;
    };

    var resetTable = function () {
        if (confirm($.i18n._('datatable.reset.confirm'))) {
            case_table.state.clear();
            location.reload();
        }
    };

    return {
        init_case_list_table: init_case_list_table,
        resetTable: resetTable
    }

})();
