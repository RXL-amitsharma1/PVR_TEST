$(function() {

    $('#transferOwnershipModal').on('show.bs.modal', function () {
        var modal = $(this);
        if($("#transferType").val()==OWNERSHIP )
            modal.find('.newOwnerFullName').html($('#newOwner').select2("data")[0].text );
        else
            modal.find('.newOwnerFullName').html($('#sharedWith').select2("data")[0].text );
    });

    $(".owner-select").select2();

    $("#transferType").select2();

    $("#ownerForm").find("[name=transferTypeValue]").val(OWNERSHIP);

    $("#transferType").on("change", function () {
        if ($(this).val() == OWNERSHIP) {
            $(".ownerSelect").show();
            $(".ownerItems").show();
            $(".shareSelect").hide();
            $(".shareItems").hide();
            $("#newOwner").trigger("change")
        } else if ($(this).val() == SHAREWITH) {
            $(".shareSelect").show();
            $(".shareItems").show();
            $(".ownerSelect").hide();
            $(".ownerItems").hide();
            $("#sharedWith").trigger("change")
        } else {
            $(".shareSelect").show();
            $(".shareItems").hide();
            $(".ownerSelect").hide();
            $(".ownerItems").show();
            $("#sharedWith").trigger("change")
        }
        $("input[name=transferTypeValue]").val($(this).val());
    });

    $(".owner-select").on("change", function () {
        if ($('#newOwner').select2("data")[0].id) {
            //When a new owner is selected
            $("#transferOwnershipButton").prop("disabled", false);
            $("#transferOwnershipButton").removeClass("btn-default");
            $("#transferOwnershipButton").addClass("btn-primary");
        } else {
            //When --Select One-- or no owner is selected
            $("#transferOwnershipButton").prop("disabled", true);
            $("#transferOwnershipButton").removeClass("btn-primary");
            $("#transferOwnershipButton").addClass("btn-default");
        }
        $("input[name=newOwnerValue]").val($(this).val());
    });

    $("#sharedWith").on("change", function () {
        if ($(this).val()!="no") {
            //When a new owner is selected
            $("#transferOwnershipButton").prop("disabled", false);
            $("#transferOwnershipButton").removeClass("btn-default");
            $("#transferOwnershipButton").addClass("btn-primary");
        } else {
            //When --Select One-- or no owner is selected
            $("#transferOwnershipButton").prop("disabled", true);
            $("#transferOwnershipButton").removeClass("btn-primary");
            $("#transferOwnershipButton").addClass("btn-default");
        }
        $("input[name=sharedWithValue]").val($(this).val())
    });

    $('#newOwner').sort_select_box();

    $("#ownerChangeButton").on('click', function(){
        if($("#transferType").val()==SHAREWITH)
            $("#shareForm").submit();
        else
            $("#ownerForm").submit();
        $("#ownerChangeButton").prop('disabled', true);
    });



});

$(document).on("data-clk", function (event, elem) {
    const elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
    const methodName = elemClkData.method;
    const params = elemClkData.params;

    if (methodName == "toggleCheck") {
        toggleCheck(...params);
    }
});

function toggleCheck(prefix) {
    $(":checkbox[name^=" + prefix + "]").each(function () {
        $(this).prop('checked', !$(this).prop('checked'));
    });

}
