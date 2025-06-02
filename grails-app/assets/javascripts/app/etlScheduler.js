$(function () {
    var startTime = $('#startDateTime').val();
    var repeatInterval = $('#repeatInterval').val();

    var prefTimezone = $("#timezoneFromServer").val();
    var timeZoneData = prefTimezone.split(",");
    var name = timeZoneData[0].split(":")[1].trim();
    var offset = timeZoneData[1].substring(8).trim();

    $('#myScheduler').scheduler({
        startDateOptions: {
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            },
            date: moment().tz(userTimeZone)
        },
        endDateOptions: {
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            },
            date: moment().tz(userTimeZone)
        }
    });

    //Hide the Repeat none option in the scheduler.
    //The scheduling can be overridden by the Disabling the job.
    //Will remove below code of specifying data in future after upgrading Scheduler.js

    $("#myScheduler").find(".repeat-container").find(".dropdown-menu").find("[data-value=none]").hide();

    $('.timezone-container').hide();
    $('.repeat-end').hide();

    $('#myScheduler').scheduler('value', {
        startDateTime: startTime,
        recurrencePattern: repeatInterval,
        timeZone: {
            name: name,
            offset: offset
        }
    });

    for (var i = 0; i < 24; i++) {
        var min = 0;
        for (var j = 0; j < 2; j++) {
            $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
            min = 30
        }
    }

    disable_enable();

    $('#myScheduler').on('changed.fu.scheduler', function () {
        var newInfo = $('#myScheduler').scheduler('value');
        var newStartDateTime = newInfo.startDateTime;
        $('#startDateTime').val(newStartDateTime);
        $('#repeatInterval').val(newInfo.recurrencePattern);

    }).trigger("changed.fu.scheduler");


    $('#myDatePicker').on('click', function () {
        var newInfo = $('#myScheduler').scheduler('value');
        $('#startDateTime').val(newInfo.startDateTime);
        $('#repeatInterval').val(newInfo.recurrencePattern);
        $('timezoneFromServer').val(newInfo.timeZone.name);

    });
// Add restriction dates
    if ($('#isDisabled').val() != 'true') {
        $('#myScheduler').find('#myDatePicker').datepicker('setRestrictedDates', [{
            from: -Infinity,
            to: moment().tz(userTimeZone).subtract(1, 'days')
        }]);
        $('#myScheduler').find('.repeat-end').find('.end-on-date').datepicker('setRestrictedDates', [{
            from: -Infinity,
            to: moment().tz(userTimeZone).subtract(1, 'days')
        }]);
    }

    $('#sendEmailETLInterval').on('keyup', function () {
        var newVal = $('#sendEmailETLInterval').val();
        if(parseInt(newVal) > 999){
            newVal = newVal.substring(0, newVal.length-1);
        }
        $('#sendEmailETLInterval').val(newVal);
    });

    $("[data-evt-change]").on('change', function() {
        const eventData = JSON.parse($(this).attr("data-evt-change"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'validateEmailIsSelectedOrNot') {
            validateEmailIsSelectedOrNot();
        }
    });

});

function disable_enable() {

    if ($('#isDisabled').val() == 'true') {
        $('#myScheduler').scheduler('disable');
    } else {
        $('#myScheduler').scheduler('enable');
        $('#isDisabled').val('false');
    }
}

$(window).load(function() {
    validateEmailIsSelectedOrNot();
});

function validateEmailIsSelectedOrNot(){
    if ($('#emailToUsers').select2('val') == '') {
        $("#sendSuccessEmail").prop('checked', false);
        $("#sendSuccessEmail").attr('disabled', !$("#sendSuccessEmail").attr('disabled'));
        $("#pauseLongRunningETL").prop('checked', false);
        $("#pauseLongRunningETL").attr('disabled', !$("#pauseLongRunningETL").attr('disabled'));
        $("#sendEmailETLInterval").val("1");
        $("#sendEmailETLInterval").attr("disabled", !$("#sendEmailETLInterval").attr('disabled'));
    }else{
        $("#sendSuccessEmail").removeAttr('disabled');
        $("#pauseLongRunningETL").removeAttr('disabled');
        $("#sendEmailETLInterval").removeAttr("disabled");
    }
}

function calculateAllIntervals(hour, minute) {
    var amPm = hour >= 12 ? "PM" : "AM";
    hour = hour % 12;
    hour = hour ? hour : 12;
    minute = (minute > 9 ? minute : "0" + minute);
    hour = hour > 9 ? hour : "0" + hour;
    var now = hour + ':' + minute + ' ' + amPm;
    return now;
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}
