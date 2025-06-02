$(function () {
    var isForPeriodicReportLocal = (typeof isForPeriodicReport == "undefined" ? true : isForPeriodicReport)
    var tasks = $("#tasks").val();
    if (tasks) {
        var data = JSON.parse(tasks);
        for (var i = 0; i < data.length; i++) {
            add(data[i].description, data[i].priority, data[i].assignedToId, data[i].dueDateShift, data[i].createDateShift, data[i].baseDate);
        }
    }
    if (aggregateReportViewTaskMode) {
        $(".taskTemplateField").prop("disabled", "disabled");
        $("#taskTable .glyphicon ").hide();
        $("#taskTable .select2-arrow ").hide();
        $(".taskTemplateField").css("border", "0");
        $(".taskTemplateField").css("background", "#ffffff")
    }
    $(document).on("click", '.addTaskTable', function () {
        add()
    });
    if (pageType != "show") {
        $(document).on("click", '.removeTaskTable', function () {
            $(this).parents('tr').detach();
        });
    }
    $(document).on("change", 'select[name=aiBeforeAfter]', function () {
        $createInput = $(this).parent().parent().find("input[name=aiCreateDateShift]");
        if ($(this).val() === "BEFORE") {
            $createInput.prop('readonly', false);
        } else {
            $createInput.val("");
            $createInput.prop('readonly', true);
        }
    });
    $('select[name=aiBeforeAfter]').trigger("change");

    $(document).on("click", '.task-template-add', function () {
        var id = $(this).attr("data-id");
        showLoader();
        $.ajax({
            url: getTaskForTemplateUrl + "?id=" + id,
            type: 'get',
            dataType: 'json'
        })
            .done(function (response) {
                var data = response.data;
                for (var i = 0; i < data.length; i++) {
                    add(data[i].description, data[i].priority, data[i].assignedToId, data[i].dueDateShift, data[i].createDateShift, data[i].baseDate);
                }
                $('select[name=aiBeforeAfter]').trigger("change");
                $("#taskTemplateList").modal("hide");
                hideLoader();
            })
            .fail(function (err) {
                    hideLoader();
                    alert((err.responseJSON.message ? err.responseJSON.message : "") +
                        (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : ""));
                }
            );
    });
    $(document).on("click", '.task-template-show', function () {
        $.ajax({
            url: listTaskTemplateUrl,
            type: 'get',
            dataType: 'json'
        })
            .fail(function (err) {
                alert((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : ""));
            })
            .done(function (data) {
                var templateContent = data.data;
                var html = "";
                for (var i = 0; i < templateContent.length; i++)
                    html += "<div><a class='task-template-add' data-id='" + templateContent[i].id + "' href=\"javascript:void(0)\">" + encodeToHTML(templateContent[i].name) + "</a></div><br>";
                $("#taskTemplateListContent").html(html);
                $("#taskTemplateList").modal("show");
            });
    });

    function add(description, priority, assign, due, createDateShift, baseDate) {
        var $clone = $("#taskTable").find('tr.hide').clone(true).removeClass('hide table-line');
        $("#taskTable").append($clone);
        if (description) $clone.find("textarea").val(description);
        if (priority) $clone.find("select[name=aiPriority]").val(priority);
        if (assign) $clone.find("select[name=aiAssignedTo]").val(assign);
        if (baseDate && (isForPeriodicReportLocal || (baseDate != 'DUE_DATE')))
            $clone.find("select[name=baseDate]").val(baseDate);
        else
            $clone.find("select[name=baseDate]").val("CREATION_DATE");
        if (!Number.isNaN(due) && due !== undefined) {
            var dueVal = parseInt(due);
            if (dueVal <= 0) {
                $clone.find("select[name=sign]").val("-");
                dueVal = -dueVal;
            }
            $clone.find("input[name=aiDueDateShift]").val(dueVal);

        } else {
            $clone.find("input[name=aiDueDateShift]").val(1);
        }
        if (createDateShift) {
            var createVal = parseInt(createDateShift);
            $clone.find("input[name=aiCreateDateShift]").val(createVal);
            $clone.find("select[name=aiBeforeAfter]").val("BEFORE");
        } else {
            $clone.find("select[name=aiBeforeAfter]").val("AFTER");
        }

        $clone.find("select[name=aiAssignedTo]").select2().on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });

    }
});
