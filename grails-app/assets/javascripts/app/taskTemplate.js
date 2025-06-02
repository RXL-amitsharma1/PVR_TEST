$(function() {

    //If mode is creation then we need to set the value of newObj as true.
    if ($("#mode").val() == 'create') {
        $("#tasksBody").find("input[id$=newObj]").attr('value', true);
    }

    //Size of tasks.
    var taskSize = $("#taskSize").val();

    //Show the removal icon if we have tasks.
    if (taskSize > 0) {
        $("#tasksBody").find(".removeTask").show();
    }

    //Click event bind to the add-task
    $(".add-task").on('click', function() {

        var clone = $("#tasksBody").find('tr:first').clone(true);
        var newIndex;
        if (taskSize == 0) {
            newIndex = 1;
        } else {
            newIndex = taskSize;
        }

        clone.attr("class", "taksRow"+newIndex);

        clone.find("input[id$=id]")
            .attr('id', "tasks["+newIndex+"].id")
            .attr('name', "tasks["+newIndex+"].id")
            .attr('value',null);
        clone.find("input[id$=deleted]")
            .attr('id', "tasks["+newIndex+"].deleted")
            .attr('name', "tasks["+newIndex+"].deleted")
            .attr('value', false);
        clone.find("input[id$=newObj]")
            .attr('id', "tasks["+newIndex+"].newObj")
            .attr('name', "tasks["+newIndex+"].newObj")
            .attr('value', true);

        clone.find("textarea[id$=taskName]")
            .attr('id', "tasks["+newIndex+"].taskName")
            .attr('name', "tasks["+newIndex+"].taskName")
            .val('');

        clone.find("input[id$=dueDate]")
            .attr('id', "tasks["+newIndex+"].dueDate")
            .attr('name', "tasks["+newIndex+"].dueDate")
            .val(0);
        clone.find("select[id$=baseDate]")
            .attr('id', "tasks[" + newIndex + "].baseDate")
            .attr('name', "tasks[" + newIndex + "].baseDate")

        clone.find("select[id$=sign]")
            .attr('id', "tasks["+newIndex+"].sign")
            .attr('name', "tasks["+newIndex+"].sign")
            .val('-');

        clone.find("select[id$=priority]")
            .attr('id', "task["+newIndex+"].priority")
            .attr('name', "tasks["+newIndex+"].priority")
            .val('');

        $("#tasksBody").append(clone);
        $("#tasksBody").find("tr:last").show();

        taskSize++

        if (taskSize > 1) {
            $("#tasksBody").find(".removeTask").show();
        }
    })

    //Click event bind to the remove task icon.
    $(".removeTask").on('click', function() {

        //Values fetched
        var currentElement = $(this);
        var currentId = currentElement.attr("id");
        var taskIndex = currentId.split("removeTask")[1];

        //Hide the row
        currentElement.closest('tr').hide();

        //Set the deleted flag true
        currentElement.closest('tr').find("input[id$=deleted]").val("true");

        //Index of task size decreased.
        taskSize--;

        //If the removes task was second last then we need to stop the user to delete
        //more tasks.
        if (taskSize == 1) {
            $("#tasksBody").find(".removeTask").hide();
        }
    });
    if($("#taskSize").val() == 1) {
        $("#tasksBody").find(".removeTask").hide();
    }

    $("[data-evt-onkeyup]").on('onkeyup', function() {
        const eventData = JSON.parse($(this).attr("data-evt-onkeyup"));
        const methodName = eventData.method;
        const params = eventData.params;

        if(methodName == 'checkDecimal') {
            var elem = $(this);
            checkDecimal(elem);
        }
    });

});

var checkDecimal=function(textbox) {
    var val=textbox.value;
   if(textbox.value.indexOf(".") !== -1){
       textbox.value= val.split(".").join("");
   }
   if(textbox.value.indexOf("-")!== -1){
       textbox.value=Math.abs(textbox.value);
   }
};