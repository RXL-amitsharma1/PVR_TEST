/**
 * Created by glennsilverman on 8/13/15.
 */

$(function () {

    $('#deleteModal').on('show.bs.modal', function (event) {
        $('#deleteDlgErrorDiv').hide();
        $('#deleteModal div.alert').hide();
        $('#deleteJustification').val('');
        var button = $(event.relatedTarget); // Button that triggered the modal

        // Extract info from data-* attributes
        var name = button.data('instancename');
        var controller = button.data('controller');
        var action = button.data('action');
        var domainId = button.data('instanceid');
        var extramsg = button.data('extramessage');
        var deleteForAllAllowed = button.data('deleteforallallowed');
        var ownerApp = button.data('appType');
        var instanceType = button.data('instancetype')?$.i18n._(button.data('instancetype')):"";

        var modal = $(this);

        //Make sure cancel and delete buttons are enabled
        $(".btn").removeAttr("disabled", "disabled");

        modal.find('#deleteForAll').prop("checked", false);
        if (deleteForAllAllowed === true) {
            $.ajax({
                url: checkDeleteForAllAllowedURL + "?id=" + domainId,
                dataType: 'text'
            })
                .done(function (data) {
                    if (data === "true")
                        modal.find('#deleteForAllAllowed').show();
                    else
                        modal.find('#deleteForAllAllowed').hide();
                });

        } else
            modal.find('#deleteForAllAllowed').hide();

        modal.find('#deleteModalLabel').text("");
        modal.find('#deleteModalLabel').text($.i18n._('modal.delete.title', instanceType));

        var nameToDeleteLabel = $.i18n._('deleteThis', instanceType);
        modal.find('#nameToDelete').text("");
        modal.find('#nameToDelete').text(nameToDeleteLabel);

        modal.find('.description').empty();
        modal.find('.description').text(name);

        modal.find('.extramessage').empty();
        if (extramsg) {
            modal.find('.extramessage').html(extramsg);
        }

        //create new action
        var ctx = window.location.pathname;

        var newAction = "/" + ctx.split("/")[1] + "/" + (controller ? controller : ctx.split("/")[2]) + "/" + (action ? action : "delete") + "/" + domainId + "?ownerApp=" + ownerApp;

        $("#deleteButton").closest("form").attr("action", newAction);
        $("#deleteButton").closest("form").on('submit', function (event) {
            if (!$('#deleteJustification').val().trim()) {
                $('#deleteDlgErrorDiv').show();
                $('#deleteModal div.alert').closest('#deleteDlgErrorDiv').show();
                return false;
            } else {
                $('#deleteDlgErrorDiv').hide();
                $('#deleteModal div.alert').hide();
                return true;
            }
        });

    });

    $("#closeJustification").on('click', function () {
        $("#deleteDlgErrorDiv").hide();
        $('#deleteModal div.alert').hide();
    });

});



