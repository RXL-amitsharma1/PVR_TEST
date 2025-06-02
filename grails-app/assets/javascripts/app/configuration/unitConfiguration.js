$(function () {
    if ($('#rxTableUnitConfiguration').is(":visible")) {

        var table = $('#rxTableUnitConfiguration').DataTable({
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
                "url": UNITCONFIGURATION.listUrl,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "unitConfigurationUniqueId",
            "aaSorting": [],
            "order": [[6, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#rxTableUnitConfiguration_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "unitName",
                    mRender: function (data, type, row) {
                        return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                },
                {
                    "mData": "unitType",
                    mRender: function (data, type, row) {
                        return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                },
                {
                    "mData": "organizationType",
                    mRender: function (data, type, row) {
                        return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                },
                {
                    "mData": "unitRegisteredId",
                    mRender: function (data, type, row) {
                        return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                },
                {
                    "mData": "unitRetired",
                    mRender: function (data, type, row) {
                        return $.i18n._((data == true ? 'Yes' : 'No'));
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
                    "sClass": "dt-center",
                    "bSortable": false,
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                                                <a class="btn btn-success btn-xs" href="' + UNITCONFIGURATION.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                                                <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                                    <span class="caret"></span> \
                                                    <span class="sr-only">Toggle Dropdown</span> \
                                                </button> \
                                                <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                                    <li role="presentation"><a role="menuitem" href="' + UNITCONFIGURATION.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                                                    \
                                                </ul> \
                                            </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#rxTableUnitConfiguration tbody tr').each(function () {
                    /*$(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');*/
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#rxTableUnitConfiguration');
        loadTableOption('#rxTableUnitConfiguration');
    }

    showHideXsltFields($("#xsltName").val());

    $("#xsltName").on("change", function (e) {
        showHideXsltFields($(this).val());
    });
    
    if ($("#unitType").val() == "RECIPIENT" || $("#unitType").val() == "BOTH") {
        $(".xsltNameIndicator").removeClass("hide");
    }

    //For Select2 library
    $(".select2-box").select2({
    });
    $("#preferredTimeZone").select2();
    bindMultipleSelect2WithUrl($("#attachmentControl"), getAllowedAttachments, true, null, null, $("#attachmentControl").attr('data-value'));

    $(document).on('click', '.unitSave', function () {
        setTimeout(function () {
            $("button").attr("disabled", true);
        }, 5);
    });

    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;

        if (methodName == "hideShowContent") {
            // Call the method from the eventHandlers object with the params
            hideShowContent($(this));
        }
    });

    $("[data-evt-change]").on('change', function() {
        const eventData = JSON.parse($(this).attr("data-evt-change"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'onchangeUnitType') {
            onchangeUnitType($(this).val());
        }
    });
});

function onchangeUnitType(value) {
    $(".emptySelectedValue").val(null).trigger('change');
    $(".emptyValue").val('');

    if (value.indexOf('RECIPIENT') > -1 || value.indexOf("BOTH") > -1) {
        $(".attachment").show();
        $(".reportSubDateDiv").show();
        $(".unitAttachmentRegIdDiv").show();

    } else {
        $('.attachmentControl').val(null).trigger('change');
        $(".attachment").hide();
        $(".reportSubDateDiv").hide();
        $(".unitAttachmentRegIdDiv").hide();
    }

    if ($("#unitType").val() == "RECIPIENT" || $("#unitType").val() == "BOTH") {
        $(".xsltNameIndicator").removeClass("hide");
    } else {
        $(".xsltNameIndicator").addClass("hide");
    }
}

function showHideXsltFields(value) {
    if (value && value != undefined) {
        $.ajax({
            url: checkXsltIsHl7Url,
            type: 'post',
            data: {xsltName: value},
            dataType: 'json'
        })
            .done(function (response) {
                if (response.data == "false") {
                    showXMLFields();
                } else {
                    hideAndRemoveXMLFields();
                }
            })
            .fail(function (error) {
                hideAndRemoveXMLFields();
            });
    } else {
        hideAndRemoveXMLFields();
    }
}

function showXMLFields() {
    $(".xmlFields").show();
}

function hideAndRemoveXMLFields() {
    $("#xmlVersion").val("");
    $("#xmlEncoding").val("");
    $("#xmlDoctype").val("");
    $(".xmlFields").hide();
}

function hideShowContent(e) {
    var getContent = $(e).parent().parent().find('.rxmain-container-content');
    var display = true;
    if ($(getContent).hasClass('rxmain-container-hide')) {
        display = false;
    }

    var getIcon;
    if (display) {
        getIcon = $(e).parent().find('i');
        $(getIcon).removeClass('fa-caret-down').addClass('fa-caret-right').trigger("classAdded");
        $(getContent).removeClass('rxmain-container-show').addClass('rxmain-container-hide');
        if ($("#unitType").select2().find(":selected")[0] && ($("#unitType").select2().find(":selected")[0].value == "RECIPIENT" || $("#unitType").select2().find(":selected")[0].value == "BOTH")) {
            $(".xsltNameIndicator").removeClass("hide");
        }
    } else {
        getIcon = $(e).parent().find('i');
        $(getIcon).removeClass('fa-caret-right').addClass('fa-caret-down').trigger("classAdded");
        $(getContent).removeClass('rxmain-container-hide').addClass('rxmain-container-show');
    }
}
