$(function () {

    function initEvents() {
        $(document).on("click", ".publishingStageAdd, .pubSectStageAdd, .reportStageAdd", function () {
            var clone = $(".ganttRowTemplate").clone().show();
            clone.removeClass("ganttRowTemplate");
            clone.find("[name=itemName]").val("");
            if ($(this).hasClass("publishingStageAdd"))
                clone.find("[name=ganttTaskType]").val("PUB_FULL_FLOW_STEP");
            else if ($(this).hasClass("pubSectStageAdd"))
                clone.find("[name=ganttTaskType]").val("PUB_SEC_FLOW_STEP");
            else
                clone.find("[name=ganttTaskType]").val("REPORT_FLOW_STEP");
            clone.find(".itemConditionType").trigger("change");
            $(this).closest("table").find("tbody").append(clone);
            disableTypes(clone)
            clone.find("select").select2();
            clone.find(".itemConditionType").trigger("change");
        });

        $(document).on("click", ".ganttSectionRemove", function () {
            $(this).closest("tr").remove()
        });

        $(document).on("change", ".itemConditionType", function () {
            var row = $(this).closest("tr")
            var val = $(this).val();
            row.find(".advancedCondition,.publisherStateList,.reportStateList,.reportWorkflowList, .publisherSectionWorkflowList,.publisherSectionStateList,.publisherWorkflowList")
                .hide().attr("disabled", true);
            if (val === "PUBLISHER_SECTION_WORKFLOW") row.find(".publisherSectionWorkflowList").show().attr("disabled", false);
            if (val === "REPORT_WORKFLOW") row.find(".reportWorkflowList").attr("disabled", false).show();
            if (val === "REPORT_STATE") row.find(".reportStateList").attr("disabled", false).show();
            if (val === "PUBLISHER_SECTION_STATE") row.find(".publisherSectionStateList").attr("disabled", false).show();
            if (val === "PUBLISHER_FULL_STATE") row.find(".publisherStateList").attr("disabled", false).show();
            if (val === "PUBLISHER_FULL_WORKFLOW") row.find(".publisherWorkflowList").attr("disabled", false).show();
            if (val === "ADVANCED") row.find(".advancedCondition").attr("disabled", false).show();

        });

        $(document).on("click", ".ganttSectionUp,.ganttSectionDown", function () {
            var row = $(this).parents("tr:first");
            if ($(this).is(".ganttSectionUp")) {
                if (row.prev().is(':visible'))
                    row.insertBefore(row.prev());
            } else {
                row.insertAfter(row.next());
            }
        });

        $("#form").on('submit', function () {
            var valid = true;
            $(".ganttItemRow:not(.ganttRowTemplate)").each(function () {
                var $this = $(this);

                var field = $this.find("select.conditionField:not(:disabled), input.conditionField:not(:disabled)");
                var type = $this.find("select.itemConditionType").val();
                var conditionValue = field.val();
                if (!conditionValue && (type != "MANUAL")) {
                    var items = field.parent().children();
                    items.css("border-color", "red");
                    setTimeout(function () {
                        items.css("border-color", "#ccc");
                    }, 3000)
                    valid = false;
                } else
                    $(this).find("[name=ganttCondition]").val(conditionValue)

            })
            if (!valid) {
                $("#errorMessage").show();
            }
            return valid;
        })
    }

    function disableTypes(row) {
        var type = row.find("[name=ganttTaskType]").val();
        if (type === "PUB_SEC_FLOW_STEP") row.find('[value="REPORT_WORKFLOW"], [value="PUBLISHER_FULL_WORKFLOW"],[value="REPORT_STATE"],[value="PUBLISHER_FULL_STATE"]').remove();
        if (type === "REPORT_FLOW_STEP") row.find('[value="PUBLISHER_SECTION_WORKFLOW"], [value="PUBLISHER_FULL_WORKFLOW"], [value="PUBLISHER_SECTION_STATE"],[value="PUBLISHER_FULL_STATE"]').remove();
        if (type === "PUB_FULL_FLOW_STEP") row.find('[value="REPORT_WORKFLOW"],[value="REPORT_STATE"],[value="PUBLISHER_SECTION_WORKFLOW"], [value="PUBLISHER_SECTION_STATE"]').remove();
    }

    function init() {
        $(".ganttItemRow:not(.ganttRowTemplate)").each(function () {
            disableTypes($(this))
        });
        $(".multipleSelect2").select2();
        $(".itemConditionType").trigger("change");
    }

    if ($("#mode").val() === "readonly") {
        init();
        $("input, select").attr("disabled", true);
    } else {
        initEvents();
        init();
    }
});