$(function () {

   var reasonSize = 1;
   var currentSize = 1;

    //Show the removal icon if we have reasons.
    /*  if (reasonSize > 1) {
         $("#reasonsBody").find(".removeReason").show();
     }*/

    //Click event bind to the addReason
    $(".addReason").on('click', function() {

        $("#reasonsBody").find('tr:first').find("select[id$=responsibleParty]").select2("destroy");
        $("#reasonsBody").find('tr:first').find("select[id$=reason]").select2("destroy");

        var clone = $("#reasonsBody").find('tr:first').clone(true);
        var newIndex;
        if (reasonSize == 0) {
            newIndex = 1;
        } else {
            newIndex = reasonSize;
        }


        clone.attr("class", "reasonRow"+newIndex);

        clone.find("input[id$=deleted]")
            .attr('id', "lateReasons["+newIndex+"].deleted")
            .attr('name', "lateReasons["+newIndex+"].deleted")
            .attr('value', false);

        clone.find("select[id$=responsibleParty]")
            .attr('id', "lateReasons["+newIndex+"].responsibleParty")
            .attr('name', "lateReasons["+newIndex+"].responsibleParty")
            .val('').select2();

        clone.find("select[id$=reason]")
            .attr('id', "lateReasons["+newIndex+"].reason")
            .attr('name', "lateReasons["+newIndex+"].reason")
            .val('').select2();


        $("#reasonsBody").append(clone);
        $("#reasonsBody").find("tr:last").show();

        reasonSize++
        currentSize++

        if (reasonSize > 1) {
            $("#reasonsBody").find(".removeReason").show();
        }

        $("#reasonsBody").find('tr:first').find("select[id$=responsibleParty]").select2();
        $("#reasonsBody").find('tr:first').find("select[id$=reason]").select2();
    });

    //Click event bind to the remove reason icon.
    $(".removeReason").on('click', function() {

        //Values fetched
        var currentElement = $(this);
        var currentId = currentElement.attr("id");
        var reasonIndex = currentId.split("removeReason")[1];

        //Hide the row
        currentElement.closest('tr').hide();

        //Set the deleted flag true
        currentElement.closest('tr').find("input[id$=deleted]").val("true");


        //Index of reason size decreased.
        currentSize--;

        //If the removes reason was second last then we need to stop the user to delete
        //more reasons.
        if (currentSize == 1) {
            $("#reasonsBody").find(".removeReason").hide();
        }
    });
});