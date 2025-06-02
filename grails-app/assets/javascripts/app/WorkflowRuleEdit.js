$(function () {
  $('#configurationTypeEnum').on('change', function () {
        handleConfigurationType();
    });

    if ($("#actionToExecute").val() == 'create') {
        $(".pvcPvqRule").css("display", "none");
    }else {
        var configurationType = $('#configurationTypeEnum').val();
        if (configurationType == "QUALITY_CASE_DATA" || configurationType == "QUALITY_SUBMISSION" || configurationType.indexOf("QUALITY_SAMPLING") > -1 || configurationType == "PVC_REASON_OF_DELAY" || configurationType == "PVC_INBOUND") {
            $(".pvcPvqRule").css("display", "block");
        }else {
            $(".pvcPvqRule").css("display", "none");
        }
    }

    function handleConfigurationType() {
        var configurationType = $('#configurationTypeEnum').val();
        if (configurationType == 'PERIODIC_REPORT' || configurationType == "ICSR_REPORT" || configurationType == 'ADHOC_REPORT'||(configurationType == 'PUBLISHER_SECTION')||(configurationType == 'PUBLISHER_FULL')||(configurationType == 'PUBLISHER_FULL_QC')) {
            $('#defaultReportAction').attr("disabled", false);
        } else {
            $('#defaultReportAction').val('');
            $('#defaultReportAction').attr("disabled", true);
        }

        if (configurationType == "PERIODIC_REPORT" || configurationType == "ADHOC_REPORT" || configurationType == "QUALITY_CASE_DATA" || configurationType == "QUALITY_SUBMISSION" || configurationType.indexOf("QUALITY_SAMPLING") > -1 || configurationType == "PVC_REASON_OF_DELAY" || configurationType == "PVC_INBOUND") {
            $("#autoExecuteInDays").prop("disabled", false);
        } else {
            $("#autoExecuteInDays").prop("disabled", true);
            $("#autoExecuteInDays").val("");
            $("#autoExecuteExcludeWeekends").prop('checked', false);
            $("#autoExecuteExcludeWeekends").prop("disabled", true);
        }
        if (configurationType == "PERIODIC_REPORT" || configurationType == "ADHOC_REPORT") {
            $("#needApproval").prop("disabled", false);
        } else {
            $("#needApproval").prop('checked', false);
            $("#needApproval").prop("disabled", true);
        }

        if (configurationType == "QUALITY_CASE_DATA" || configurationType == "QUALITY_SUBMISSION" || configurationType.indexOf("QUALITY_SAMPLING") > -1 || configurationType == "PVC_REASON_OF_DELAY" || configurationType == "PVC_INBOUND") {
            if ($("#actionToExecute").val() == 'create') {
                $("#BASIC_RULE").prop('checked', true);
                $("#BASIC_RULE").trigger('click');
                $(".basicRuleOptionCheckbox").prop('checked', false);
                $("#assignToUserGroup").prop('checked', true);
                $('#assignedToUsersControl').val(null).trigger("change");
            }

            $(".pvcPvqRule").css("display", "block");
            setSelect2InputWidth($("#assignedToUsersControl"));
        }else{
            $("#dueInDays").val("");
            $("#excludeWeekends").prop('checked', false).prop("disabled", true);
            $(".pvcPvqRule").css("display", "none");
            $("#assignToUserGroup").prop('checked', false);
        }
    }

    handleConfigurationType();

    $("#autoExecuteInDays").on("change", function () {
        if ($("#autoExecuteInDays").val() != "") {
            $("#needApproval").prop('checked', false);
            $("#needApproval").prop("disabled", true);
        } else
            $("#needApproval").prop("disabled", false);
    });
    $("#autoExecuteInDays").trigger("change");

    $(".autoAssignmentRuleRadio").on('click', function() {
        var id = $(this).attr("id");
        $(".assignmentRule").val(id);
        if(id == 'BASIC_RULE'){
            $(".advancedAssignmentField").css("display", "none");
            $(".basicRuleOption").css('display', "block");
            $('.assignedToUsersControl').attr("disabled", false);
            $('.assignedToUsersControl input').addClass('add-cursor').css({'cursor' : 'text'});
        }else{
            $(".advancedAssignmentField").css("display", "block");
            $(".advancedAssignmentField").val($(".advancedAssignmentField option:first").val());
            $(".assignToUserGroupField").prop('checked', false);
            $(".autoAssignToUsersField").prop('checked', false);
            $(".basicRuleOption").css('display', "none");
            $('.assignedToUsersControl').val("").trigger('change');
            $('.assignedToUsersControl').attr("disabled", true);
            $('.assignedToUsersControl select').removeClass('add-cursor').css({'cursor':'not-allowed'});
        }
    });

    $("#autoExecuteInDays").on('change', function () {
        if ($(this).val()) {
            $("#autoExecuteExcludeWeekends").prop("disabled", false);
        } else {
            $("#autoExecuteExcludeWeekends").prop('checked', false).prop("disabled", true);
        }
    }).trigger("change");
    $("#dueInDays").on('change', function () {
        if ($(this).val()) {
            $("#excludeWeekends").prop("disabled", false);
        } else {
            $("#excludeWeekends").prop('checked', false).prop("disabled", true);
        }
    }).trigger("change");

    var assignmentRuleId = $('.assignmentRule').val();
    if(assignmentRuleId != "") {
        var elem = $("#" + assignmentRuleId);
        if (elem.length) {
            elem.attr('checked', true);
            if(assignmentRuleId == 'BASIC_RULE'){
                $(".advancedAssignmentField").css("display", "none");
                $(".basicRuleOption").css('display', "block");
            }else{
                $(".advancedAssignmentField").css("display", "block");
                $(".assignToUserGroupField").prop('checked', false);
                $(".autoAssignToUsersField").prop('checked', false);
                $(".basicRuleOption").css('display', "none");
                $('.assignedToUsersControl').attr("disabled", true);
                $('.assignedToUsersControl input').removeClass('add-cursor').css({'cursor':'not-allowed'});
            }
        } else {
            $("#BASIC_RULE").attr('checked', true);
            $(".assignmentRule").val("BASIC_RULE");
        }
    }


    if ($("#actionToExecute").val() == 'create') {
        $("#BASIC_RULE").attr('checked', true);
        $("#assignToUserGroup").attr('checked', true);
        $(".advancedAssignmentField").css("display", "none");
    }

    $(".basicRuleOptionCheckbox").on('click', function() {
        var id = $(this).attr("id");
        if(id == 'assignToUserGroup'){
            $("#autoAssignToUsers").prop('checked', false);
            $('#assignedToUsersControl optgroup[label="User"]').attr("display", "none");
        }else if(id == 'autoAssignToUsers'){
            $("#assignToUserGroup").prop('checked', false);
        }else{
            $("#assignToUserGroup").prop('checked', false);
            $("#autoAssignToUsers").prop('checked', false);
        }
    });

    $("#assignToUserGroup, #autoAssignToUsers").on("change", function () {
        if (!$("#assignToUserGroup").prop('checked') && !$("#autoAssignToUsers").prop('checked')) {
            $('.assignedToUsersControl').prop("disabled", true);
        } else {
            $('.assignedToUsersControl').prop("disabled", false);
        }
    });

});