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
var table;
$(function () {
    var tableFilter = {};
    var advancedFilter = false;
    var allTableParams = [];

    $.ajax({
        type: "GET",
        url: selectTemplatesUrl,
        dataType: 'json'
    })
        .done(function (result) {
            var options = "";
            _.each(result, function (data_item) {
                options += "<option value='" + data_item.id + "'>" + replaceBracketsAndQuotes(data_item.reportName) + "</option>\n";
            });
            $("#templates").html(options);
            $("#templates").select2().val(null).trigger('change');
            $("#templates").select2().on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });
        });

    table = $('#rxTableBulkSheduling').DataTable({
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
        //"sPaginationType": "bootstrap",
        "stateSave": true,
        "stateDuration": -1,
        "language": {
            "search": '',
            "searchPlaceholder": $.i18n._("fieldprofile.search.label")
        },
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": listUrl,
            "type": "POST",
            "dataSrc": "data",
            "data": function (d) {
                d.tableFilter = JSON.stringify(tableFilter);
                d.advancedFilter = advancedFilter;
                d.searchString = d.search.value;
                // d.sharedwith = $('#sharedWith').val();
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    //Column header mData value extracting
                    d.sort = d.columns[d.order[0].column].data;
                }

                allTableParams = d;
            }
        },
        "aaSorting": [],
        "order": [[5, "desc"]],
        columnDefs: [
            {width: "20", targets: 0},
            {width: "100", targets: 8}
        ],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            pageDictionary($('#rxTableBulkSheduling_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

        },
        "aoColumns": [
            {
                "sClass": "dataTableColumnCenter isTemplateCell",
                "mData": "isTemplate",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return (data ? formStatusCell("template") : (row.status ? formStatusCell("scheduled") : formStatusCell("unscheduled")))
                }
            }, {
                "sClass": "reportNameCell",
                "mData": "reportName",
                mRender: function (data, type, row) {
                    return formReportNameCell(row.id, data);
                }
            }, {
                "bSortable": false,
                "sClass": "configurationTemplateCell",
                "mData": "configurationTemplate"
            },
            {
                "sClass": "productCell",
                "mData": "productSelection",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formProductCell(row.id, row.productsJson, row.groupsJson, row.productSelection)
                }
            },
            {
                "sClass": "schedulerCell",
                "mData": "scheduleDateJSON",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formSchedulerCell(row.id, row.schedulerJSON, data)
                }
            },
            {
                "mData": "nextRunDate",
                "sClass": "forceLineWrapDate nextRunDateCell",
                mRender: function (data, type, row) {
                    return formNextRunDateCell(row.id, row.nextRunDate, row.status, data)
                }
            },
            {
                "mData": "DeliveryOption.sharedWith",
                "sClass": "sharedWithCell",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formSharedWithCell(row.id, row.sharedWithUsers, row.sharedWithGroup);
                }

            },
            {
                "mData": "DeliveryOption.emailToUsers",
                "sClass": "emailToCell",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formEmailToCell(row.id, row.emailToUsers);
                }
            },
            {
                "mData": null,
                "sClass": "dataTableColumnCenter col-min-100",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return formMenuCell(data["id"], data["status"], data["reportName"], data["isTemplate"]);
                }
            }
        ],
        initComplete: function () {
            loadTableOption('#rxTableBulkSheduling');
            removeOptionColumn();
            var topControls = $(".topControls");
            $('#rxTableBulkSheduling_wrapper').find('.dt-search').before(topControls);
            topControls.show();
        }
    }).on('draw.dt', function () {
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });

    var filter_data = [
        {
            label: $.i18n._("app.advancedFilter.template"),
            type: 'boolean',
            name: 'isTemplate'
        }, {
            label: $.i18n._("app.advancedFilter.reportName"),
            type: 'text',
            name: 'reportName'
        }, {
            label: $.i18n._("app.advancedFilter.configurationTemplate"),
            type: 'select2-id',
            name: 'configurationTemplate',
            ajax: {
                url: selectTemplatesUrl,
                data_handler: function (data) {
                    return pvr.filter_util.build_options(data, 'id', 'reportName', true);
                },
                error_handler: function (data) {
                    console.log(data);
                }
            }
        }, {
            label: $.i18n._("app.advancedFilter.product"),
            type: 'text',
            name: 'productSelection'
        }, {
            label: $.i18n._("app.advancedFilter.nextRunDateStart"),
            type: 'date-range',
            group: 'nextRunDate',
            group_order: 1
        },
        {
            label: $.i18n._("app.advancedFilter.nextRunDatedEnd"),
            type: 'date-range',
            group: 'nextRunDate',
            group_order: 2
        }, {
            label: $.i18n._("app.advancedFilter.status"),
            type: 'select2-manual',
            name: 'status',
            data: statuses
        }
    ];

    pvr.filter_util.construct_right_filter_panel({
        table_id: '#rxTableBulkSheduling',
        container_id: 'config-filter-panel',
        filter_defs: filter_data,
        column_count: 1,
        done_func: function (filter) {
            tableFilter = filter;
            advancedFilter = true;
            table.ajax.reload(function (data) {
            }, false).draw();
        }
    });

    $("select[data-name=isTemplate]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[data-name=configurationTemplate]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[data-name=status]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    function removeOptionColumn() {
        var tableColumns = $('#tableColumns');
        tableColumns.find("tbody tr:first").remove();
    }

    function formStatusCell(data) {
        return (data === "template" ? "<span class='fa fa-copy fa-lg' title='" + $.i18n._("app.advancedFilter.template") + "'></span>" :
            (data === "scheduled" ? "<span class='fa fa-play-circle fa-lg green' style='color:green' title='" + $.i18n._("Scheduled") + "'></span>" :
                    "<span class='fa fa-stop-circle fa-lg' title='" + $.i18n._("notscheduled") + "'></span>"
            ));
    }

    function formReportNameCell(id, text) {
        return "<span class='reportName' data-id='" + id + "'>" + encodeToHTML(text) + "</span>";
    }

    $(document).on("click", "td.reportNameCell", function (e) {
        var $this = $(this);
        var id = $this.find(".reportName").attr("data-id");
        var oldVal = $this.find(".reportName").text();

        var $textEditDiv = $("#reportNameEditDiv");
        showEditDiv($this, $textEditDiv, $textEditDiv.find('.newVal'));
        $textEditDiv.find('.newVal').val(oldVal).focus();
        $textEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = $textEditDiv.find('.newVal').val();
            if ((newVal !== oldVal) && newVal && newVal.trim().length > 0) {
                ajaxCall(editFieldUrl, {reportName: newVal, id: id},
                    function (result) {
                        $this.html(formReportNameCell(id, newVal));
                    },
                    function (err) {
                        $this.html(formReportNameCell(id, oldVal));
                    });
            } else
                $this.html(formReportNameCell(id, oldVal));
            $(".popupBox").hide();
        });
    });


    function formProductCell(id, productsValue, groupsValue, label) {
        return "<span  class='products' data-id='" + id + "' data-products-value='" + productsValue + "' data-groups-value='" + encodeToHTML(groupsValue) + "'>" + label + "</span>";
    }

    $(document).on("click", "td.productCell", function (e) {
        var $this = $(this);
        var id = $this.find(".products").attr("data-id");
        var oldProductsVal = $this.find(".products").attr("data-products-value");
        var oldGroupsVal = $this.find(".products").attr("data-groups-value");
        var oldLabel = $this.find(".products").text();
        $('#productSelection').val('');
        $('#productGroupSelection').val('');
        $("#showProductSelection").off();
        $("#productModal").modal("show");
        clearAllText(PRODUCT_DICTIONARY);
        clearDicGroupText(PRODUCT_DICTIONARY);
        $(".searchProducts").val("");
        $(".dicUlFormat ").html("");
        var productDictionaryObj = getDictionaryObject(PRODUCT_DICTIONARY);
        productDictionaryObj.resetDictionaryList();
        if (oldGroupsVal && oldGroupsVal !== 'null') {
            $('#productGroupSelection').val(oldGroupsVal);
            $.each(JSON.parse(oldGroupsVal), function (key, value) {
                productDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id});
            });
            productDictionaryObj.setProductGroupText(true);
        }

        if (oldProductsVal && oldProductsVal !== 'null') {
            $('#productSelection').val(oldProductsVal);
            var loadDataSource = 'pva';
            var loadValues = JSON.parse(oldProductsVal);
            productDictionaryObj.setValues(loadValues);
            productDictionaryObj.setValuesPva(loadValues);
            productDictionaryObj.setColumnViewText(loadValues, loadDataSource);
        }
        if ((oldProductsVal && oldProductsVal !== 'null') || (oldGroupsVal && oldGroupsVal !== 'null')) {
            showDictionaryValues(document.getElementById("showProductSelection"), productDictionaryObj.getValues(), PRODUCT_DICTIONARY, productDictionaryObj.getValuesDicGroup(), productDictionaryObj.getLevels(), true);
        }
        enableDisableProductGroupButtons();

        if ($('#productGroupSelection').data('hide-dictionary-group')) {
            $("#productModal").find('.dictionary-group').hide();
        } else {
            $("#productModal").find('.dictionary-group').show();
        }

        var observer = new MutationObserver(function (mutationsList, observer) {
            var mutation = mutationsList[0];
            if (mutation.type === "childList" || mutation.type === "subtree") {
                var newProductsVal = $("#productSelection").val();
                var newGroupsVal = $("#productGroupSelection").val();
                if ((newProductsVal !== oldProductsVal) || (newGroupsVal !== oldGroupsVal)) {
                    ajaxCall(editFieldUrl, {
                            productSelection: newProductsVal,
                            productGroupSelection: newGroupsVal,
                            id: id
                        },
                        function (result) {
                            var newLabel = $("#showProductSelection").text();
                            $this.html(formProductCell(id, newProductsVal, newGroupsVal, newLabel));
                        },
                        function (err) {
                            $this.html(formProductCell(id, oldProductsVal, oldGroupsVal, oldLabel));
                        });
                } else {
                    $this.html(formProductCell(id, oldProductsVal, oldGroupsVal, oldLabel));
                }

                observer.disconnect();
            }
        });

        observer.observe($("#showProductSelection")[0], {childList: true, subtree: true});
    });

    function formSchedulerCell(id, value, label) {
        return "<span  class='scheduler' data-id='" + id + "' data-value='" + value + "'>" + label + "</span>";
    }

    function formNextRunDateCell(id, value, status, label) {
        if (status) {
            return "<span  class='nextRunDateCell' data-id='" + id + "' data-value='" + value + "'>" + label + "</span>";
        } else {
            return "<span></span>";
        }
    }

    $(document).on("click", "td.schedulerCell", function (e) {
        var $this = $(this);

        var id = $this.find(".scheduler").attr("data-id");
        var oldVal = $this.find(".scheduler").attr("data-value");
        if (oldVal != 'null') {
            var oldLabel = $this.find(".scheduler").text();
            var $schedulerEditDiv = $("#schedulerEditDiv");
            showEditDiv($this, $schedulerEditDiv);
            $schedulerEditDiv.find('#myDatePicker').datepicker('setRestrictedDates', []);
            $schedulerEditDiv.find('.repeat-end').find('.end-on-date').datepicker('setRestrictedDates', []);
            var schedulerInfoJSON = parseServerJson(oldVal);
            if (schedulerInfoJSON.recurrencePattern.indexOf("WEEKLY") < 0)
                highlightCurrentDayForWeeklyFrequency(userTimeZone);
            $schedulerEditDiv.scheduler('value', schedulerInfoJSON);
            $('#configSelectedTimeZone').val(schedulerInfoJSON.timeZone.name);


            $schedulerEditDiv.find(".saveButton").one('click', function (e) {
                var newVal = $schedulerEditDiv.find('#scheduleDateJSON').val();
                if ((newVal !== oldVal) && newVal) {
                    ajaxCall(editFieldUrl, {scheduleDateJSON: newVal, id: id},
                        function (result) {
                            $this.html(formSchedulerCell(id, result.data.json, result.data.label));
                        },
                        function (err) {
                            $this.html(formSchedulerCell(id, oldVal, oldLabel));
                        });
                } else {
                    $this.html(formSchedulerCell(id, oldVal, oldLabel));
                }
                $schedulerEditDiv.hide()
            });
        }
    });

    function formSharedWithCell(id, userValue, groupValue) {
        var sharedName = '';
        var sharedId = '';
        var userCount = 0, groupCount = 0;
        while (userValue[userCount]) {
            if (!userValue[userCount])
                break;
            else {
                if (sharedName === '')
                    sharedName = userValue[userCount].fullName
                else
                    sharedName += ", " + userValue[userCount].fullName;
                if (sharedId === '')
                    sharedId = "User_" + userValue[userCount].id
                else
                    sharedId += ";" + "User_" + userValue[userCount].id

                userCount++;
            }
        }
        while (groupValue[groupCount]) {
            if (!groupValue[groupCount])
                break;
            else {
                if (sharedName === '')
                    sharedName = groupValue[groupCount].name
                else
                    sharedName += ", " + groupValue[groupCount].name;
                if (sharedId === '')
                    sharedId = "UserGroup_" + groupValue[groupCount].id
                else
                    sharedId += ";" + "UserGroup_" + groupValue[groupCount].id

                groupCount++;
            }
        }
        return "<span  class='sharedWithCol' data-id='" + id + "' data-value='" + sharedId + "' data-users='" + JSON.stringify(userValue) + "' data-groups='" + JSON.stringify(groupValue) + "'>" + sharedName + "</span>";
    }

    $(document).on("click", "td.sharedWithCell", function (e) {
        var $this = $(this);
        var id = $this.find(".sharedWithCol").attr("data-id");
        var oldVal = $this.find(".sharedWithCol").attr("data-value");
        var oldUsers = JSON.parse($this.find(".sharedWithCol").attr("data-users"));
        var oldGroups = JSON.parse($this.find(".sharedWithCol").attr("data-groups"));
        if (oldVal != 'null') {
            var $sharedWithEditDiv = $("#sharedWithEditDiv");
            showEditDiv($this, $sharedWithEditDiv, $sharedWithEditDiv.find('#sharedWithEdit'));
            $sharedWithEditDiv.find('#sharedWithEdit').val(oldVal).trigger('change');
            $('#sharedWithEdit').val(oldVal);
            $sharedWithEditDiv.find(".saveButton").one('click', function (e) {
                var newVal = $sharedWithEditDiv.find('#sharedWithEdit').val();
                if ((newVal !== oldVal) && newVal) {
                    ajaxCall(editFieldUrl, {sharedUsers: newVal, id: id},
                        function (result) {
                            $this.html(formSharedWithCell(id, result.data.sharedUsers, result.data.sharedGroups));
                        },
                        function (err) {
                            $this.html(formSharedWithCell(id, oldUsers, oldGroups));
                        });
                } else if (newVal == '') {
                    ajaxCall(editFieldUrl, {sharedUsers: 'currentUser', id: id},
                        function (result) {
                            $this.html(formSharedWithCell(id, result.data.sharedUsers, result.data.sharedGroups));
                        },
                        function (err) {
                            $this.html(formSharedWithCell(id, oldUsers, oldGroups));
                        });
                } else {
                    $this.html(formSharedWithCell(id, oldUsers, oldGroups));
                }
                $sharedWithEditDiv.hide()
            });
        }
    });

    function formEmailToCell(id, value) {
        var EmailIds = ''
        var EmailIdsList = []
        EmailIds = value.join(", ")
        return "<span  class='emailToCol' data-id='" + id + "' data-value='" + value + "'>" + EmailIds + "</span>";
    }

    $(document).on("click", "td.emailToCell", function (e) {
        var $this = $(this);
        var id = $this.find(".emailToCol").attr("data-id");
        var oldString = $this.find(".emailToCol").attr("data-value");
        var oldVal = oldString.split(',');

        if (oldString != '') {
            var $emailToEditDiv = $("#emailToEditDiv");
            showEditDiv($this, $emailToEditDiv);
            $emailToEditDiv.find('#newEmailUsers').val(oldVal).trigger('change');
            $('#newEmailUsers').val(oldVal);

            $emailToEditDiv.find(".saveButton").one('click', function (e) {
                var newVal = $emailToEditDiv.find('#newEmailUsers').val();
                var emailCount = 0;
                var newString = '';
                while (newVal) {
                    if (!newVal[emailCount])
                        break;
                    if (newString === '')
                        newString = newVal[emailCount];
                    else
                        newString += "," + newVal[emailCount];
                    emailCount++;
                }
                if ((newString !== oldString) && newString) {
                    ajaxCall(editFieldUrl, {emailToUsers: newString, id: id},
                        function (result) {
                            $this.html(formEmailToCell(id, result.data.emailToUsers));
                        },
                        function (err) {
                            $this.html(formEmailToCell(id, oldVal));
                        });
                } else if (newString == '') {
                    ajaxCall(editFieldUrl, {emailToUsers: 'currentUser', id: id},
                        function (result) {
                            $this.html(formEmailToCell(id, result.data.emailToUsers));
                        },
                        function (err) {
                            $this.html(formEmailToCell(id, oldVal));
                        });
                } else {
                    $this.html(formEmailToCell(id, oldVal));
                }
                $emailToEditDiv.hide();
            });
        }
    });

    function formMenuCell(id, isSheduled, reportName, isTemplate) {
        var actionButton = '<div class="btn-group dropdown" align="center">';
        if (isSheduled) {
            actionButton += '<a class="btn btn-success btn-xs unschedule"  href="javascript:void(0)" data-id="' + id + '">' + $.i18n._("app.executionStatus.unschedule.label") + '</a>';
        } else {
            actionButton += '<a class="btn btn-success btn-xs run" href="#"  data-id="' + id + '" ' + (isTemplate ? 'disabled="disabled"' : '') + '>' + $.i18n._('run') + '</a>';
        }
        actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + CONFIGURATION.viewUrl + '/' + id + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem"  href="' + CONFIGURATION.editUrl + '/' + id + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="javascript:void(0)" class="copy" data-id="' + id + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" class="delete"  data-id="' + id + '" data-instancename="' + replaceBracketsAndQuotes(reportName) + '" href="javascript:void(0)">' + $.i18n._('delete') + '</a></li> \
                        </ul> \
                    </div>';
        return actionButton;
    }

    $('#file_input').on('change', function (evt, numFiles, label) {
        $(".btn").attr('disabled', 'disabled');
        $("#file_name").val($('#file_input').get(0).files[0].name);
        if ($("#file_name").val() !== "") {
            $("#excelImportForm").trigger('submit');
        }
    });

    $(document).on("change", "#templates", function (e) {
        $("button.create").prop("disabled", !$("#templates").val());
    });
    $(document).on("click", "button.create", function (e) {
        var $this = $("#templates");
        $this.prop("disabled", false);
        var id = $this.val();
        createReport(id, $this);
    });

    $(document).on("click", "a.run", function (e) {
        var $this = $(this);
        var id = $this.attr("data-id");
        if (!$this.attr("disabled"))
            ajaxCall(CONFIGURATION.runUrl, {id: id},
                function (result) {
                    var $row = $this.closest("tr");
                    $this.closest("td").html(formMenuCell(id, true, $row.find(".reportName").text(), $row.find(".isTemplateCell").html().indexOf("copy") > -1));
                    $row.find(".nextRunDateCell").text(result.data.nextRunDate);
                    $row.find(".isTemplateCell").html(formStatusCell("scheduled"));
                }, function (err) {
                });
    });

    $(document).on("click", "a.copy", function (e) {
        var $this = $(this);
        $this.prop("disabled", false);
        var id = $this.attr("data-id");
        createReport(id, $this);
    });

    $(document).on("click", "a.delete", function (e) {
        var $this = $(this);
        var id = $this.attr("data-id");
        var name = $this.attr("data-instancename");
        var modal = $('#deleteModal');
        modal.off();
        modal.modal("show");

        var deleteAlert = $('#deleteModal #deleteDlgErrorDiv');
        deleteAlert.hide();
        $("#deleteJustification").val("");
        var deleteForAllAllowed = false;
        var instanceType = $.i18n._($.i18n._('configuration'));
        modal.find(".btn").removeAttr("disabled", "disabled");
        modal.find('#deleteForAllAllowed').hide();

        modal.find('#deleteModalLabel').text("");
        modal.find('#deleteModalLabel').text($.i18n._('modal.delete.title', instanceType));

        var nameToDeleteLabel = $.i18n._('deleteThis', instanceType);
        modal.find('#nameToDelete').text("");
        modal.find('#nameToDelete').text(nameToDeleteLabel);

        modal.find('.description').empty();
        modal.find('.description').text(name);
        modal.find("#deleteButton").off().on("click", function () {
            var confirmation = $("#deleteJustification").val();
            if (confirmation != "" && confirmation.trim().length > 0) {
                modal.modal("hide");
                ajaxCall(CONFIGURATION.deleteUrl, {id: id, confirmation: confirmation},
                    function (result) {
                        var $row = $this.closest("tr");
                        table.row($row).remove().draw();
                        successNotification(result.message, true)
                    }, function (err) {
                    });
            } else {
                deleteAlert.show();
            }
        });
    });

    $(document).on("click", "a.unschedule", function (e) {
        var $this = $(this);
        var id = $this.attr("data-id");
        ajaxCall(CONFIGURATION.unscheduleUrl, {id: id},
            function (result) {
                var $row = $this.closest("tr");
                $this.closest("td").html(formMenuCell(id, false, $row.find(".reportName").text(), $row.find(".isTemplateCell").html().indexOf("copy") > -1));
                $row.find(".nextRunDateCell").text('');
                $row.find(".isTemplateCell").html(formStatusCell("unscheduled"));
            }, function (err) {
            });
    });

    function ajaxCall(url, data, success, error) {
        showLoader();
        $.ajax({
            type: "POST",
            url: url,
            data: data,
            dataType: 'json'
        })
            .done(function (result) {
                success(result);
                hideLoader();
            })
            .fail(function (err) {
                errorNotification((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                error(err);
                hideLoader();
                window.scrollTo(0, 0);
            });
    }

    function createReport(id, element) {
        showLoader();
        $.ajax({
            type: "GET",
            url: CONFIGURATION.copyUrl,
            data: {id: id},
            dataType: 'json'
        })
            .done(function (result) {
                element.prop("disabled", false);
                table.row.add(result.data).draw();
                hideLoader();
            })
            .fail(function (err) {
                errorNotification((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                element.prop("disabled", false);
                hideLoader();
            });
    }

    $(document).on("click", ".popupBox .cancelButton", function () {
        $(".popupBox").hide();
        $(".saveButton").off();
    });

    $(document).on("click", "#exportBulkConfig", function () {
        var form = $("#excelExportForm");
        form.find("input").detach();
        for (var x in allTableParams) {
            form.append(
                "<input type='hidden' name='" +
                x +
                "' value='" +
                allTableParams[x] +
                "'>"
            );
        }
        form.submit();
    });
});
