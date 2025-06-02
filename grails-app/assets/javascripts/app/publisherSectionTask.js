$(function () {

    var tasks = $("#tasks").val();
    if (tasks) {
        var data = JSON.parse(tasks);
        for (var i = 0; i < data.length; i++) {
            add(data[i].description, data[i].priority, data[i].assignedToId, data[i].dueDateShift, data[i].baseDate);
        }
    }
    $(document).on("click", '.addTaskTable', function () {
        add()
    });
    if(pageType != "show") {
        $(document).on("click", '.removeTaskTable', function () {
            $(this).parents('tr').detach();
        });
    }

    function add(description, priority, assign, due, baseDate) {
        var $clone = $("#taskTable").find('tr.hide').clone(true).removeClass('hide table-line');
        $("#taskTable").append($clone);
        if (description) $clone.find("textarea").val(description);
        if (priority) $clone.find("select[name=aiPriority]").val(priority);
        if (assign) $clone.find("select[name=aiAssignedTo]").val(assign);
        if (baseDate)
            $clone.find("select[name=baseDate]").val(baseDate);
        else
            $clone.find("select[name=baseDate]").val("CREATION_DATE");
        if (!Number.isNaN(due)) {
            var dueVal = parseInt(due);
            if (dueVal <= 0) {
                $clone.find("select[name=sign]").val("-");
                dueVal = -dueVal;
            }
            $clone.find("input[name=aiDueDateShift]").val(dueVal);

        } else {
            $clone.find("input[name=aiDueDateShift]").val("1");
        }

        $clone.find("select[name=aiAssignedTo]").select2();
    }
});
