$(function (e) {

    $("#sharedWith").select2();
    var dashboard_types_PVR = [{id: "PVR_PUBLIC", text: $.i18n._("public")}, {
        id: "PVR_USER",
        text: $.i18n._("personal")
    }];
    var dashboard_types_PVQ = [{id: "PVQ_PUBLIC", text: $.i18n._("public")}, {
        id: "PVQ_USER",
        text: $.i18n._("personal")
    }];
    var dashboard_types_PVC = [{id: "PVC_PUBLIC", text: $.i18n._("public")}, {
        id: "PVC_USER",
        text: $.i18n._("personal")
    }];


    $(document).on('change', '#dashboardType', function () {
        var val = $(this).val();
        if ((val == "PVR_PUBLIC") || (val == "PVQ_PUBLIC") || (val == "PVC_PUBLIC")) {
            $('#sharedWith').prop("disabled", false);
        } else {
            $('#sharedWith').val(null).trigger('change');
            $('#sharedWith').prop("disabled", true);
        }
    });

    function formatDashbardIcon(state) {
        return "<i class='" + state.text + "'> " + state.text + "</i>"
    }

    $("#dashboardIcon").select2({
        tags: true,
        templateSelection: formatDashbardIcon,
        templateResult: formatDashbardIcon,
        escapeMarkup: function (m) {
            return m;
        },
        dropdownParent: $("#dashboardModal")
    });

    $(".dashboard-edit").on('click', function (e) {
        $('.dictionaryErrorDiv').hide();
        var dashboardId = $(this).data('id');
        $.ajax({
            url: editDashboardUrl + "?id=" + dashboardId,
            dataType: 'json'
        })
            .done(function (result) {
                $("#label").val(result.dashboardLabel);
                $("#dashboardId").val(dashboardId);

                var dashboard_types;
                var dashboardTypeElement = $("#dashboardType");
                if (result.dashboardType.indexOf("PVR") > -1) {
                    dashboard_types = dashboard_types_PVR;
                } else if (result.dashboardType.indexOf("PVQ") > -1) {
                    dashboard_types = dashboard_types_PVQ;
                } else {
                    dashboard_types = dashboard_types_PVC;
                }
                dashboardTypeElement.select2({
                    data: dashboard_types,
                    dropdownParent: $("#dashboardModal")
                });
                dashboardTypeElement.val(result.dashboardType);
                dashboardTypeElement.trigger('change');

                if (result.isAdmin) {
                    dashboardTypeElement.prop("disabled", false);
                } else {
                    dashboardTypeElement.prop("disabled", true);
                }
                var dashboardIconElement = $("#dashboardIcon");
                if (result.dashboardIcon !== null) {
                    if (!dashboardIconElement.find(`option[value="${result.dashboardIcon}"]`).length) {
                        dashboardIconElement
                            .append(new Option(result.dashboardIcon, result.dashboardIcon, false, true)).trigger("change");
                    }
                    dashboardIconElement.val(result.dashboardIcon);
                    dashboardIconElement.trigger('change');
                }
                if ((result.dashboardType.indexOf("PVR") > -1) || (result.dashboardType.indexOf("PVQ") > -1) || (result.hasParent)) {
                    dashboardIconElement.prop("disabled", true);
                } else {
                    dashboardIconElement.prop("disabled", false);
                }

                var shareWithElement = $("#sharedWith");
                for (var i in result.userGroups) {
                    var userGroupObj = result.userGroups[i];
                    var optionValue = 'UserGroup_' + userGroupObj.id;
                    // Check if the option already exists
                    if (!shareWithElement.find(`option[value="${optionValue}"]`).length) {
                        var userGroupOption = new Option(userGroupObj.name, optionValue, false, userGroupObj.selected);
                        $(shareWithElement.find('optgroup')[0]).append(userGroupOption).trigger('change');
                    }
                    else{
                        shareWithElement.find(`option[value="${optionValue}"]`).prop('selected', userGroupObj.selected).trigger('change');
                    }
                }

                for (var j in result.users) {
                    var userObj = result.users[j];
                    var optionValue = 'User_' + userObj.id;
                    // Check if the option already exists
                    if (!shareWithElement.find(`option[value="${optionValue}"]`).length) {
                        var userOption = new Option(userObj.name, optionValue, false, userObj.selected);
                        $(shareWithElement.find('optgroup')[1]).append(userOption).trigger('change');
                    }
                    else{
                        shareWithElement.find(`option[value="${optionValue}"]`).prop('selected', userObj.selected).trigger('change');
                    }
                }
                if (result.dashboardType.indexOf("PUBLIC") > -1) {
                    $('#sharedWith').prop("disabled", false);
                } else {
                    $('#sharedWith').select2("val", "")
                    $('#sharedWith').prop("disabled", true);
                }
            });
    });

    $("#submitButton").on('click', function (e) {
        $('.dictionaryErrorDiv').hide();
        var update = true;
        if (update) {
            $('#errormessage').html("");
            var errorMessage = "";
            if ($("#label").val().length > 255) {
                errorMessage += $.i18n._('dashboardDictionary.label.maxSize.exceeded') + "<br>";
            }

            if ($("#label").val() == '') {
                errorMessage += $.i18n._('dashboardDictionary.label.nullable') + "<br>";
            }

            if ($("#dashboardType").val() == '') {
                errorMessage += $.i18n._('dashboardDictionary.type.nullable') + "<br>";
            }

            if (($("#dashboardType").val() == 'PVQ_PUBLIC') || ($("#dashboardType").val() == 'PVR_PUBLIC') || ($("#dashboardType").val() == 'PVC_PUBLIC')) {
                if ($("#sharedWith").val() == null)
                    errorMessage += $.i18n._('dashboardDictionary.sharedwith.nullable') + "<br>";
            }

            if($("#label").val().length!="" && $("#label").val().length<5){
                errorMessage += $.i18n._('Dashboard.label.minSize.notmet') + "<br>";
            }

            if (errorMessage !== "") {
                $('.dictionaryErrorDiv').show();
                $('#errormessage').html(errorMessage);
                update = false;
            }
        }
        if (update) {
            $.ajax({
                url: updateDashboardUrl + "?id=" + $("#dashboardId").val(),
                data: {
                    'label': $("#label").val(), 'icon': $("#dashboardIcon").val(),
                    'type': $("#dashboardType").val(), 'sharedWith': $("#sharedWith").val()
                },
                method: 'POST'
            })
                .done(function (result) {
                    window.location.reload();
                });
        }
    });

    $("#dictionaryErrorDiv").on('click', function () {
        $(".dictionaryErrorDiv").hide();
    });
});