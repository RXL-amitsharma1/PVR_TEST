$(function () {

    $('#relativeDateRangeValueX').hide();
    // Disable the scheduler.
    $('#schedulerDiv input, #schedulerDiv button').attr("disabled", "disabled");
    $('#schedulerDiv fieldset').addClass("disabled");

    var value = $("#dateRangeValueRelative").val();
    if (value != undefined) {
        if (value.toLowerCase().indexOf('x') != -1) {
            $('#relativeDateRangeValueX').show();
        } else {
            $('#relativeDateRangeValueX').hide();
        }
    }
});