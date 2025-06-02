$(function (e) {
    $("#dashboardType").select2();
    $("#sharedWith").select2();
    $(document).on('change', '#dashboardType', function () {
        var val = $(this).val();
        if ((val === "PVR_PUBLIC") || (val === "PVQ_PUBLIC")|| (val === "PVC_PUBLIC")) {
            $('#sharedWith').attr("disabled", false);
        } else {
            $('#sharedWith').attr("disabled", true);
        }
    });
    $("#dashboardType").trigger("change");
});