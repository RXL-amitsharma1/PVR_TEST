var caseObj = caseObj || {};

caseObj.caseList = (function () {

    var case_table, open_case_table, removed_case_table, addCaseModalObj, addCaseNumberCommentsModalObj;

    //The function for initializing the case tables.
    var init_case_list_table = function (isTemporary) {
        console.log("Initializing case list table...");
        var columns = [{}, {}, {"data": "caseNumber"}, {"data": "versionNumber"}, {"data": "tags"}, {"data": "type"}, {"data": "productFamily"}, {"data": "eventPI"}, {"data": "seriousness"}, {"data": "listedness"}, {"data": "lockedDate"}];
        var aocolumns = [
            {
                "mData": "",
                "visible": caseObjConfig.showAddRemoveCaseControls,
                "bSortable": false,
                "sWidth": "6%",
                "mRender": function (data, type, row) {
                    console.log("Rendering checkbox for row:", row);
                    return '<input class="removeCaseChk" type="checkbox" style="cursor: pointer" />';
                }
            },
            {
                "mData": "",
                "bSortable": false,
                "sWidth": "8%",
                "mRender": function (data, type, row) {
                    console.log("Rendering tags for row:", row);
                    return showTags(row)
                }
            },
            {
                "mData": "caseNumber",
                "sWidth": "12%",
                "mRender": function (data, type, row) {
                    console.log("Rendering caseNumber for row:", row);
                    return '<span class="caseNumberElement"><a href="' + caseObjConfig.caseDataLinkUrl + '?caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&cid=' + caseObjConfig.caseSeriesId + '" target="_blank">' + row.caseNumber + '</a></span>';
                }
            },
            {
                "mData": "versionNumber",
                "sWidth": "12%",
                "visible": caseObjConfig.showVersionColumn,
                "mRender": function (data, type, row) {
                    console.log("Rendering versionNumber for row:", row);
                    return '<span class="versionNumberElement">' + row.versionNumber + '</span>';
                }
            },
            {
                "mData": "tags",
                "bSortable": false,
                "sClass": 'dataTableColumnCenter',
                "sWidth": "30%",
                "mRender": function (data, type, row) {
                    console.log("Rendering tags in table for row:", row);
                    var tags = showTagsInTable(row);
                    return tags;
                }
            },
            {
                "mData": "type",
                "sWidth": "12%"
            },
            {
                "mData": "productFamily",
                "sWidth": "12%",
            },
            {
                "mData": "eventPI",
                "sWidth": "12%"
            },
            {
                "mData": "seriousness",
                "sWidth": "12%"
            },
            {
                "mData": "listedness",
                "sWidth": "12%"
            },
            {
                "mData": "lockedDate",
                "sWidth": "12%",
                "aTargets": ["lockedDate"],
                "sClass": "dataTableColumnCenter mw-100",
                "mRender": function (data, type, full) {
                    console.log("Rendering lockedDate for row:", data);
                    if (data) {
                        return moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT);
                    } else {
                        return ""
                    }
                }
            }
        ];
        if (caseObjConfig.safetySource != 'pvcm') {
            console.log("Adding causality column for non-PVCM source.");
            columns.splice(10, 0, {"data": "causality"});
            aocolumns.splice(10, 0, {
                "mData": "causality",
                "sWidth": "12%",
                "visible": true
            });
        }
        if (caseObjConfig.dateRangeType == $.i18n._('app.label.eventReceiptDate')) {
            columns.push({"data": "eventSequenceNumber"});
            aocolumns.push({
                "mData": "eventSequenceNumber",
                "sWidth": "12%",
                "visible": false
            });
            columns.push({"data": "eventReceiptDate"});
            aocolumns.push({
                "mData": "eventReceiptDate",
                "sWidth": "12%",
                "visible": false,
                "sClass": "dataTableColumnCenter mw-100",
                "mRender": function (data, type, full) {
                    if (data) {
                        return moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT);
                    } else {
                        return ""
                    }
                }
            });
            columns.push({"data": "eventPreferredTerm"});
            aocolumns.push({
                "mData": "eventPreferredTerm",
                "sWidth": "12%",
                "visible": false
            });
            columns.push({"data": "eventSeriousness"});
            aocolumns.push({
                "mData": "eventSeriousness",
                "sWidth": "12%",
                "visible": false
            });
        }
        if (!isTemporary) {
            columns.push({"data": "comments"});
            aocolumns.push({
                "mData": "comments",
                "sWidth": "12%",
                "bSortable": false,
                "mRender": renderCommentsField
            });
        }
        //Initialize the data table
        case_table = $("#caseList").DataTable({
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
            "stateDuration": -1,
            //"sPaginationType": "bootstrap",
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                    pageDictionary($('#caseList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                    getReloader($('#caseList_info'), $("#caseList"));

                //Click event on the check box.
                $("#caseList .removeCaseChk").on('click', function () {
                    if ($("#caseList .removeCaseChk").is(":checked")) {
                        $('#caseList .removeCasesBtn').removeClass('hide');
                    } else {
                        $('#caseList .removeCasesBtn').addClass('hide');
                    }
                });
                openCaseListTagModal();
                closeCaseListTagModal();
                $("#caseList").height($(window).height() - $("#caseList").offset().top - 90);

            },
            initComplete: function () {

                //Click event on the button.
                $("#caseList .removeCasesBtn").off().on('click', function () {

                    var confirmationModal = $("#confirmationModal");
                    confirmationModal.modal("show");

                    //TODO: Need to change the static string to i18 compliance.
                    confirmationModal.find('.modalHeader').html($.i18n._('delete.confirm'));
                    confirmationModal.find('.confirmationMessage').html($.i18n._('justification.for.deletion'));


                    confirmationModal.find('.okButton').off().on('click', function () {

                        var checkedBoxeElements = $("#caseList").find(".removeCaseChk");

                        var casesData = [];

                        var justification = confirmationModal.find("#justification").val();

                        //Prepare the case numbers
                        checkedBoxeElements.each(function () {
                            if ($(this).is(":checked")) {
                                var caseNumber = $(this).closest('tr').find('.caseNumberElement').text();
                                var versionNumber = $(this).closest('tr').find('.versionNumberElement').text();
                                casesData.push({
                                    'caseNumber': caseNumber,
                                    'versionNumber': versionNumber,
                                    'justification': justification
                                });
                            }
                        });
                        $.ajax({
                            url: caseObjConfig.removeCaseUrl,
                            type: 'POST',
                            contentType: 'application/json; charset=utf-8',
                            dataType: 'json',
                            data: JSON.stringify(casesData)
                        })
                            .done(function (result) {
                                //For each check box element delete the row.
                                $("#caseList .removeCasesBtn").addClass('hide');
                                confirmationModal.modal('hide');
                                confirmationModal.find("#justification").val("");
                                reloadOpenCasesData("", true);
                                reloadData();
                                successNotification(result.message);
                            })
                            .fail(function (err) {
                                var responseText = err.responseText;
                                var responseTextObj = JSON.parse(responseText);
                                confirmationModal.find('.alert-danger').removeClass('hide');
                                confirmationModal.find('.errorMessageSpan').html(responseTextObj.message);
                                setTimeout(function () {
                                    confirmationModal.find('.alert-danger').addClass('hide');
                                }, 5000)
                            });
                    });

                    confirmationModal.find('.closeButton').off().on('click', function () {
                        confirmationModal.find("#justification").val("");
                    })

                });
                openCaseListTagModal();
                closeCaseListTagModal();
            },
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": caseObjConfig.listUrl,
                "dataSrc": "data",
                "data": function (d) {
                    console.log("Sending AJAX data request with:", d);
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "caseUniqueId",

            "aaSorting": [],
            //"aaSorting": [ [0,'asc'], [1,'asc'] ],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "order": [[2, "desc"]],
            //"columns": columns,
            "aoColumns": aocolumns
        });
        console.log("DataTable initialized successfully!");
        return case_table;
    };

    var init_open_case_list_table = function (isTemporary) {
        var columns = [{}, {}, {"data": "caseNumber"}, {"data": "versionNumber"}, {"data": "tags"}, {"data": "type"}, {"data": "productFamily"}, {"data": "eventPI"}, {"data": "seriousness"}, {"data": "listedness"}, {"data": "lockedDate"}];
        var aocolumns = [
            {
                "mData": "",
                "bVisible": false,
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return '-';
                }
            },
            {
                "mData": "",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return showTags(row)
                }
            },
            {
                "mData": "caseNumber",
                "mRender": function (data, type, row) {
                    return '<span class="caseNumberElement"><a href="' + caseObjConfig.caseDataLinkUrl + '?caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&cid=' + caseObjConfig.caseSeriesId + '" target="_blank">' + row.caseNumber + '</a></span>';
                }
            },
            {
                "mData": "versionNumber",
                "visible": caseObjConfig.showVersionColumn,
                "mRender": function (data, type, row) {
                    return '<span class="versionNumberElement">' + row.versionNumber + '</span>';
                }
            },
            {
                "mData": "tags",
                "bSortable": false,
                "sClass": 'dataTableColumnCenter',
                "sClass": 'dataTableColumnCenter',
                "mRender": function (data, type, row) {
                    var tags = showTagsInTable(row);
                    return tags;
                }
            },
            {
                "mData": "type"
            },
            {
                "mData": "productFamily",
            },
            {
                "mData": "eventPI"
            },
            {
                "mData": "seriousness"
            },
            {
                "mData": "listedness"
            },
            {
                "mData": "lockedDate",
                "aTargets": ["lockedDate"],
                "bVisible": false,
                "sClass": "dataTableColumnCenter mw-100",
                "mRender": function (data, type, full) {
                    return "-"
                }
            }
        ];
        if (caseObjConfig.safetySource != 'pvcm') {
            columns.splice(10, 0, {"data": "causality"});
            aocolumns.splice(10, 0, {
                "mData": "causality"
            });
        }
        if (caseObjConfig.dateRangeType == $.i18n._('app.label.eventReceiptDate')) {
            columns.push({"data": "eventSequenceNumber"});
            aocolumns.push({
                "mData": "eventSequenceNumber",
                "visible": false
            });
            columns.push({"data": "eventReceiptDate"});
            aocolumns.push({
                "mData": "eventReceiptDate",
                "visible": false,
                "sClass": "dataTableColumnCenter mw-100",
                "mRender": function (data, type, full) {
                    if (data) {
                        return moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT);
                    } else {
                        return ""
                    }
                }
            });
            columns.push({"data": "eventPreferredTerm"});
            aocolumns.push({
                "mData": "eventPreferredTerm",
                "visible": false
            });
            columns.push({"data": "eventSeriousness"});
            aocolumns.push({
                "mData": "eventSeriousness",
                "visible": false
            });
        }
        if (!isTemporary) {
            columns.push({"data": "comments"});
            aocolumns.push({
                "mData": "comments",
                "bSortable": false,
                "mRender": renderCommentsField
            });
        }
        //Initialize the data table
        open_case_table = $("#openCaseList").DataTable({
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
            "stateDuration": -1,
            //"sPaginationType": "bootstrap",
            drawCallback: function (settings) {
                if (settings.fnRecordsDisplay() <= 0) {
                    var vis = 0;
                    for (var i = 0; i < aocolumns.length; i++) {
                        if (aocolumns[i].bVisible != false) vis++;
                    }
                    $("#openCaseList tr td").attr('colspan', vis);
                }
                openCaseListTagModal();
                closeCaseListTagModal();
            },
            initComplete: function () {
                openCaseListTagModal();
                closeCaseListTagModal();
            },
            "customProcessing": true, //handled using processing.dt event
            "ajax": {
                "url": caseObjConfig.openListUrl,
                "dataSrc": ""
            },
            rowId: "caseUniqueId",
            "pagingType": "full_numbers",
            "aaSorting": [[2, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 1000, -1], [50, 100, 200, 1000, "All"]],
            // "columns": columns,
            "aoColumns": aocolumns
        });
        return open_case_table;
    };


    var init_removed_case_list_table = function (isTemporary) {
        var columns = [{}, {}, {"data": "caseNumber"}, {"data": "versionNumber"}, {"data": "tags"}, {"data": "type"}, {"data": "productFamily"}, {"data": "eventPI"}, {"data": "seriousness"}, {"data": "listedness"}, {"data": "lockedDate"}];
        var aocolumns = [
            {
                "mData": "",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return '<input class="addCaseChk" type="checkbox" style="cursor: pointer" />';
                }
            },
            {
                "mData": "",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return showTags(row)
                }
            },
            {
                "mData": "caseNumber",
                "mRender": function (data, type, row) {
                    return '<span class="caseNumberElement"><a href="' + caseObjConfig.caseDataLinkUrl + '?caseNumber=' + data + '&versionNumber=' + row.versionNumber + '&cid=' + caseObjConfig.caseSeriesId + '" target="_blank">' + data + '</a></span>';
                }
            },
            {
                "mData": "versionNumber",
                "visible": caseObjConfig.showVersionColumn,
                "mRender": function (data, type, row) {
                    return '<span class="versionNumberElement">' + data + '</span>';
                }
            },
            {
                "mData": "tags",
                "bSortable": false,
                "sClass": 'dataTableColumnCenter',
                "mRender": function (data, type, row) {
                    var tags = showTagsInTable(row);
                    return tags;
                }
            },
            {
                "mData": "type"
            },
            {
                "mData": "productFamily",
            },
            {
                "mData": "eventPI"
            },
            {
                "mData": "seriousness"
            },
            {
                "mData": "listedness"
            },
            {
                "mData": "lockedDate",
                "aTargets": ["lockedDate"],
                "sClass": "dataTableColumnCenter mw-100",
                "mRender": function (data, type, full) {
                    if (data) {
                        return moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT);
                    } else {
                        return ""
                    }

                }
            }
        ];
        if (caseObjConfig.safetySource != 'pvcm') {
            columns.splice(10, 0, {"data": "causality"});
            aocolumns.splice(10, 0, {
                "mData": "causality"
            });
        }
        if (caseObjConfig.dateRangeType == $.i18n._('app.label.eventReceiptDate')) {
            columns.push({"data": "eventSequenceNumber"});
            aocolumns.push({
                "mData": "eventSequenceNumber",
                "visible": false
            });
            columns.push({"data": "eventReceiptDate"});
            aocolumns.push({
                "mData": "eventReceiptDate",
                "visible": false,
                "sClass": "dataTableColumnCenter mw-100",
                "mRender": function (data, type, full) {
                    if (data) {
                        return moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT);
                    } else {
                        return ""
                    }
                }
            });
            columns.push({"data": "eventPreferredTerm"});
            aocolumns.push({
                "mData": "eventPreferredTerm",
                "visible": false
            });
            columns.push({"data": "eventSeriousness"});
            aocolumns.push({
                "mData": "eventSeriousness",
                "visible": false
            });
        }
        if (!isTemporary) {
            columns.push({"data": "comments"});
            aocolumns.push({
                "mData": "comments",
                "bSortable": false,
                "mRender": renderCommentsField
            });
        }
        //Initialize the data table
        removed_case_table = $("#removedCaseList").DataTable({
            "stateSave": true,
            "stateDuration": -1,
            //"sPaginationType": "bootstrap",

            drawCallback: function (settings) {
                if (settings.fnRecordsDisplay() <= 0) {
                    $("li#removedCasesListTab").hide();
                    $("#removedCaseList tr td").attr('colspan', aocolumns.length);
                    return
                } else {
                    $("li#removedCasesListTab").show();
                }
                //Click event on the check box.
                $("#removedCaseList .addCaseChk").on('click', function () {
                    if ($("#removedCaseList .addCaseChk").is(":checked")) {
                        $('#removedCaseList .addCasesBtn').removeClass('hide');
                    } else {
                        $('#removedCaseList .addCasesBtn').addClass('hide');
                    }
                });
                openCaseListTagModal();
                closeCaseListTagModal();
            },

            initComplete: function () {

                //Click event on the button.
                $("#removedCaseList .addCasesBtn").off().on('click', function () {
                    var confirmationModal = $("#confirmationModal");
                    confirmationModal.modal("show");
                    confirmationModal.find('.modalHeader').html($.i18n._('add.confirm'));
                    confirmationModal.find('.confirmationMessage').html($.i18n._('justification.for.add'));
                    confirmationModal.find('.okButton').off().on('click', function () {
                        var checkedBoxeElements = $("#removedCaseList").find(".addCaseChk");
                        var casesData = [];
                        var justification = confirmationModal.find("#justification").val();
                        //Prepare the case numbers
                        checkedBoxeElements.each(function () {
                            if ($(this).is(":checked")) {
                                var caseNumber = $(this).closest('tr').find('.caseNumberElement').text();
                                var versionNumber = $(this).closest('tr').find('.versionNumberElement').text();
                                casesData.push({
                                    'caseNumber': caseNumber,
                                    'versionNumber': versionNumber,
                                    'justification': justification
                                });
                            }
                        });
                        $.ajax({
                            url: caseObjConfig.moveCasesToListUrl,
                            type: 'POST',
                            contentType: 'application/json; charset=utf-8',
                            dataType: 'json',
                            data: JSON.stringify(casesData)
                        })
                            .done(function (result) {
                                //For each check box element move the row.
                                $("#removedCaseList .addCasesBtn").addClass('hide');
                                confirmationModal.modal('hide');
                                confirmationModal.find("#justification").val("");
                                reloadAllTables("", true);
                                successNotification(result.message);
                            })
                            .fail(function (err) {
                                var responseText = err.responseText;
                                var responseTextObj = JSON.parse(responseText);
                                confirmationModal.find('.alert-danger').removeClass('hide');
                                confirmationModal.find('.errorMessageSpan').html(responseTextObj.message);
                                setTimeout(function () {
                                    confirmationModal.find('.alert-danger').addClass('hide');
                                }, 5000)
                            });
                    });

                    confirmationModal.find('.closeButton').off().on('click', function () {
                        confirmationModal.find("#justification").val("");
                    })

                });
                openCaseListTagModal();
                closeCaseListTagModal();
            },
            "customProcessing": true, //handled using processing.dt event
            "ajax": {
                "url": caseObjConfig.removedListUrl,
                "dataSrc": ""
            },
            rowId: "caseUniqueId",

            "aaSorting": [[2, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 1000, -1], [50, 100, 200, 1000, "All"]],
            // "columns": columns,
            "aoColumns": aocolumns
        });
        bindMultipleSelect2WithUrl($("#caseLevelTags"), caseObjConfig.fetchAllTags, true, $.i18n._('placeholder.case.level.tags'), "A");
        bindMultipleSelect2WithUrl($("#globalTags"), caseObjConfig.fetchAllTags, true, $.i18n._('placeholder.global.level.tags'), "G");
        $(document).on('click', '#addNewCaseLevelTag', function () {
            addNewTags($("#newCaseLevelTag"), "#caseLevelTags");
        });

        $(document).on('click', '#addNewGlobalLevelTag', function () {
            addNewTags($("#newGlobalLevelTag"), "#globalTags");
        });

        return removed_case_table;
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
            intiTagSelect(caseListTagModal.find('#caseLevelTags'), caseSeriesTags);
            intiTagSelect(caseListTagModal.find('#globalTags'), globalTags);
            caseListTagModal.find(".addTags").off().on('click', function () {
                $.ajax({
                    url: caseObjConfig.updateTags,
                    data: {
                        caseLevelTags: caseListTagModal.find('#caseLevelTags').val().join(","),
                        globalTags: caseListTagModal.find('#globalTags').val().join(","),
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
                        reloadAllTables(caseNumber);
                        successNotification(result.message);
                    })
                    .fail(function (err) {
                        errorNotification(err.message);
                    });
            });
        });

        $("#caseListTagModal").on("shown.bs.modal", function () {
            setSelect2InputWidth($(this).find("#caseLevelTags"));
            setSelect2InputWidth($(this).find("#globalTags"));
        });
    }

    function addNewTags(id, tagId) {
        var newTerm = id.val();
        newTerm = encodeToHTML(newTerm);
        if ($.trim(newTerm) != "") {
            $(tagId).append(new Option(newTerm, newTerm, true, true)).trigger("change");
            var selectedItems = $(tagId).val();
            selectedItems.push(newTerm);
            $(tagId).val(selectedItems).trigger("change");
            $(tagId).select2("close");
        } else {
            id.val("").trigger("change");
            $(tagId).select2("close");
        }
    }

    function intiTagSelect(selector, values) {
        selector.find(`option`).detach();
        if (values && values.length > 0) {
            _.each(values, function (item) {
                if (item.trim() && !selector.find(`option[value="${item}"]`).length) {
                    selector.append(new Option(item, item, true, true))
                }
            });
            selector.val(values).trigger('change');
        } else {
            selector.val(null).trigger('change');
        }
    }

    var showTagsInTable = function (row) {
        var tagsElement = '<div class="tag-container">';
        if (row.caseSeriesTags) {
            $.each(row.caseSeriesTags, function (key, value) {
                tagsElement += '<span class="badge badge-info">' + value.split(MULTIPLE_AJAX_SEPARATOR) + '</span>'
            });
        }
        if (row.globalTags) {
            $.each(row.globalTags, function (key, value) {
                tagsElement += '<span class="badge badge-info">' + value.split(MULTIPLE_AJAX_SEPARATOR) + '&nbsp;' + '<i class="fa fa-globe"> ' + '</i>' + '</span>'
            });
        }
        tagsElement += '<div></div>'
        tagsElement += '<span class="editTags glyphicon glyphicon-edit" style="cursor: pointer; margin-left: 10px;" data-rowCaseSeriesTags="' + row.caseSeriesTags + '" data-rowGlobalTags="' + row.globalTags + '" data-case-number-id="' + row.caseId + '" data-case-number="' + row.caseNumber + '">';
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

    var init_add_case_modal = function () {
        addCaseModalObj = $('#addCaseModal');
        addCaseModalObj.find('input:text').val('');
        addCaseModalObj.find('#versionNumber').val('');
        addCaseModalObj.find('#justification').val("");
        addCaseModalObj.find('#importCasesExcel').prop('checked', false);
        addCaseModalObj.modal('show');

        addCaseModalObj.find('#importCasesExcel').on('click', function () {
            if (addCaseModalObj.find('#importCasesExcel').is(':checked')) {
                addCaseModalObj.find('#importCasesSection').removeAttr('hidden');
                addCaseModalObj.find('#versionNumber, #caseNumber').val('').attr('disabled', 'disabled').siblings('label').find('span').hide();
                $('.add-case-to-list').attr('disabled', 'disabled');
            } else {
                addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
                addCaseModalObj.find(':file').val('').parents('.input-group').find(':text').val('');
                addCaseModalObj.find('#versionNumber, #caseNumber').removeAttr('disabled').siblings('label').find('span').show();
                $('.add-case-to-list').removeAttr('disabled');
            }
        });

        addCaseModalObj.on('change', ':file', function () {
            var input = $(this);
            var numFiles = input.get(0).files ? input.get(0).files.length : 0;
            var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
            var validExts = new Array(".xlsx", ".xls");
            var fileExt = label.substring(label.lastIndexOf('.'));
            if (numFiles > 0) {
                if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                    $('#fileFormatError').show();
                    $('.add-case-to-list').attr('disabled', 'disabled');
                } else {
                    $('#fileFormatError').hide();
                    $('.add-case-to-list').removeAttr('disabled');
                }
            } else {
                $('#fileFormatError').hide();
            }
            input.trigger('fileselect', [numFiles, label]);
        });

        addCaseModalObj.find(':file').on('fileselect', function (event, numFiles, label) {
            var input = $(this).parents('.input-group').find(':text');
            var log = numFiles > 0 ? label : "";

            if (input.length) {
                input.val(log);
            }
        });
    };

    var init_add_case_comment_modal = function (caseNumberUniqueId, comments, caseNumber) {
        addCaseNumberCommentsModalObj = $('#addCaseNumberComment');
        addCaseNumberCommentsModalObj.find("form").find('#caseNumberUniqueId').val(caseNumberUniqueId);
        addCaseNumberCommentsModalObj.find("form").find('#caseNumber').val(caseNumber);
        addCaseNumberCommentsModalObj.find("form").find('textarea').val(comments);
        addCaseNumberCommentsModalObj.find("form").find('#oldComment').val(comments);
        addCaseNumberCommentsModalObj.modal('show');
        addCaseNumberCommentsModalObj.find('.errorMessageSpan').html("");
        addCaseNumberCommentsModalObj.find('.alert-danger').addClass('hide');
    };

    var add_comment_to_case_number = function () {
        $.ajax({
            url: caseObjConfig.addCaseNumberComment,
            type: 'POST',
            data: addCaseNumberCommentsModalObj.find("form").serialize(),
            dataType: 'json'
        })
            .done(function (data) {
                var caseNumberUniqueId = addCaseNumberCommentsModalObj.find("form").find('input:hidden').val();
                addCaseNumberCommentsModalObj.modal('hide');
                reloadAllTables(caseNumberUniqueId);
                successNotification(data.message);
            })
            .fail(function (err) {
                var responseText = err.responseText;
                var responseTextObj = JSON.parse(responseText);
                addCaseNumberCommentsModalObj.find('.errorMessageSpan').html(responseTextObj.message);
                addCaseNumberCommentsModalObj.find('.alert-danger').removeClass('hide');
            });
    };


    var add_case_to_list = function () {
        addCaseModalObj.find('#addCaseButton').attr('disabled', 'disabled');
        var formData = new FormData(addCaseModalObj.find('#addNewCase')[0]);
        if (addCaseModalObj.find(':file').val()) {
            formData.append("file", $('#file_input').get(0).files[0]);
        }
        $.ajax({
            url: caseObjConfig.addCaseUrl,
            type: 'POST',
            data: formData,
            mimeType: "multipart/form-data",
            contentType: false,
            cache: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (data) {
                addCaseModalObj.find('#addCaseButton').removeAttr('disabled');
                addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
                addCaseModalObj.find(':file').val('').parents('.input-group').find(':text').val('');
                addCaseModalObj.find('#versionNumber, #caseNumber').removeAttr('disabled').siblings('label').find('span').show();
                addCaseModalObj.modal('hide');
                reloadOpenCasesData("", true);
                reloadData("", true);
                successNotification(data.message);
            })
            .fail(function (err) {
                addCaseModalObj.find('#addCaseButton').removeAttr('disabled');
                var responseText = err.responseText;
                var responseTextObj = JSON.parse(responseText);
                addCaseModalObj.find('.alert-danger').removeClass('hide');
                addCaseModalObj.find('.errorMessageSpan').html(responseTextObj.message);
                setTimeout(function () {
                    addCaseModalObj.find('.alert-danger').addClass('hide');
                }, 5000)
            });
    };

    $(".caseJustifictaion").on('click', function () {
        $("#caseErrordiv").hide();
    });

    var show_remove_case_icons = function () {

        // Get the column API object
        var column = case_table.column(8);

        // Toggle the visibility
        column.visible(!column.visible());
    };

    var reloadData = function (rowId, resetPagination) {
        if (resetPagination != true) {
            resetPagination = false
        }
        var dataTable = $("#caseList").DataTable();
        dataTable.ajax.reload(function () {
            highlightRow(rowId, dataTable);
        }, resetPagination);
    };

    var reloadOpenCasesData = function (rowId, resetPagination) {
        if (resetPagination != true) {
            resetPagination = false
        }
        var dataTable = $("#openCaseList").DataTable();
        if (dataTable.ajax.url())
            dataTable.ajax.reload(function () {
                highlightRow(rowId, dataTable);
            }, resetPagination);
    };

    var reloadAllTables = function (rowId, resetPagination) {
        reloadData(rowId, resetPagination);
        reloadOpenCasesData(rowId, resetPagination);
        var dataTable = $("#removedCaseList").DataTable();
        //check that table was initialized
        if (dataTable.ajax.url())
            dataTable.ajax.reload(function () {
                highlightRow(rowId, dataTable);
            }, resetPagination);
    };

    var highlightRow = function (rowId, dataTable) {
        if (rowId != undefined && rowId != "") {
            $(dataTable
                .row("#" + rowId)
                .node())
                .addClass("flash-row");
        }
    };

    var showTags = function (row) {
        var iconHtml = "";
        if (row.isNewCase) {
            iconHtml = iconHtml + '<span class="glyphicon glyphicon-tag" style="color: green"></span>';
        }
        if (row.isManuallyAdded) {
            iconHtml = '<div class="annotationPopover" ><i class="showPopover"' +
                'data-content="' + row.justification + '" data-placement="right" ' +
                'title="' + $.i18n._('app.caseList.justification') + '">' +
                '<span class="glyphicon glyphicon-tag" style="color: purple"></span></i></div>';

        }
        if (row.isMovedFromOpen) {
            iconHtml = iconHtml + '<span class="glyphicon glyphicon-tag" style="color: darkblue"></span>';
        }
        if (row.higherVersionExists) {
            iconHtml = iconHtml + '<span class="glyphicon glyphicon-tag" style="color: orange"></span>';
        }
        return iconHtml
    };

    var generate_report_as_draft = function () {
        var urlToHit = caseObjConfig.generateDraftUrlWithId;
        $.ajax({
            url: urlToHit,
            dataType: 'json'
        })
            .done(function (result) {
                if (result.warning) {
                    warningNotification(result.message);
                    return
                }
                successNotification(result.message);
                setTimeout(
                    function () {
                        reloadData(exConfigId);
                    }, 1000);
            })
            .fail(function (err) {
                errorNotification("Server Error!");
            });
    };

    var getSortColumn = function () {
        var table = getCaseListTable();
        var columns = table.settings().init().aoColumns;
        var order = table.order();
        var columnIndex = order[0][0];
        return columns[columnIndex].data;
    };

    var getOrderDirection = function () {
        var table = getCaseListTable();

        var order = table.order();
        return order[0][1];
    };

    var currentActiveCaseListTab = function () {
        if ($("#openCaseList").is(":visible")) {
            return "openCaseList";
        } else if ($("#removedCaseList").is(":visible")) {
            return "removedCaseList";
        } else {
            return "caseList";
        }
    };

    var getCaseListTable = function () {
        var table;
        if ($.fn.dataTable.isDataTable('#caseList')) {
            table = $('#caseList').DataTable();

            return table;
        }
    };

    var renderCommentsField = function (data, type, row) {
        var icon = "fa-comment-o ";
        if (row.comments && row.comments.length > 0) {
            icon = "fa-commenting-o commentPopoverMessage showPopover ";
        } else {
            row.comments = ""
        }
        return '<div class="annotationPopover" ><i class=" fa ' + icon + ' addComment"' +
            'data-content="' + escapeHTML(encodeToHTML(row.comments)) + '" ' +
            'style="font-size:17px;padding-left: 30px" ' +
            'data-placement="left" ' +
            'data-case-number-id="' + row.caseUniqueId + '" ' +
            'data-case-number="' + row.caseNumber + '" ' +
            'title="' + $.i18n._('app.caseList.comment') + '"></i></div>';
    };

    return {
        show_remove_case_icons: show_remove_case_icons,
        add_case_to_list: add_case_to_list,
        add_comment_to_case_number: add_comment_to_case_number,
        init_case_list_table: init_case_list_table,
        init_open_case_list_table: init_open_case_list_table,
        init_removed_case_list_table: init_removed_case_list_table,
        init_add_case_modal: init_add_case_modal,
        init_add_case_comment_modal: init_add_case_comment_modal,
        generate_report_as_draft: generate_report_as_draft,
        getSortColumn: getSortColumn,
        getOrderDirection: getOrderDirection,
        currentActiveCaseListTab: currentActiveCaseListTab
    }

})();
