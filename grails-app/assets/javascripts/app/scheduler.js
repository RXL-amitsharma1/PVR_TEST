var scheduleDateJSONDefault;

$(function () {
    var initialSubmissionDate = $('#submissionDate').val();
    var initialSubmissionDateNew = $('#submissionDateNew').val();
    var formattedTime
    if(initialSubmissionDateNew == null)
        formattedTime = null
    else
        formattedTime = moment(initialSubmissionDateNew, "YYYY-MM-DD HH:mm:ss").format("hh:mm A");
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
    //Will remove below code of specifying data in future after upgrading Scheduler.js
    if ($('#scheduleDateJSON').val()) {
        var schedulerInfoJSON = parseServerJson(($("#scheduleDateJSON").val()));
        if (schedulerInfoJSON.recurrencePattern.indexOf("WEEKLY") < 0)
            highlightCurrentDayForWeeklyFrequency(userTimeZone);
        $('#myScheduler').scheduler('value', schedulerInfoJSON);
        $('#configSelectedTimeZone').val(schedulerInfoJSON.timeZone.name);
        for (var i = 0; i < 24; i++) {
            var min = 0;
            for (var j = 0; j < 2; j++) {
                $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
                min = 30
            }
        }
    } else {
        setToday();
        highlightCurrentDayForWeeklyFrequency(userTimeZone);
    }
    updateAvailableDays();
    $(document).on('click', ".yearly-year-month-select li", function (e, data) {
        updateAvailableDays();
    });

    function updateAvailableDays() {
        var val = $(".yearly-year-month-select li[data-selected=true]").attr("data-value");
        val = val ? parseInt(val) : 1
        if ((val == 1) || (val == 3) || (val == 5) || (val == 7) || (val == 8) || (val == 10) || (val == 12)) {
            $(".day31").show();
        } else {
            $(".day31").hide();
        }
        if (val == 2) {
            $(".day30").hide();
        } else {
            $(".day30").show();
        }
    }

    $('#myScheduler').on('changed.fu.scheduler', function (e, data) {
        var newInfo = $('#myScheduler').scheduler('value');

        $('#scheduleDateJSON').val(JSON.stringify(newInfo));
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);
    }).trigger('changed.fu.scheduler');

    $('#MyStartDate').on('change', function (e, data) {
        if(!$(this).val()){
            $('#myDatePicker').datepicker('setDate', null);
        }

        var newInfo = $('#myScheduler').scheduler('value');

        $('#scheduleDateJSON').val(JSON.stringify(newInfo));
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);

    });

    $('#myDatePicker').on('changed.fu.datepicker dateClicked.fu.datepicker', function () {
        setDatePickerTime()
    });

    function setDatePickerTime() {
        var selectedDate = $("#myDatePicker").datepicker('getDate');
        if (moment().tz(userTimeZone) < (moment(selectedDate))) {
            $("#myScheduler #timeSelect ul").empty();
            for (var i = 0; i < 24; i++) {
                var min = 0;
                for (var j = 0; j < 2; j++) {
                    $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
                    min = 30
                }
                $("#myStartTime").val(12 + ':' + 0 + +0 + ' ' + 'AM');
            }
        }
        var newInfo = $('#myScheduler').scheduler('value');
        $('#scheduleDateJSON').val(JSON.stringify(newInfo));
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);
    }

    var enable = false;
    if (document.getElementById("enable")) {
        enable = $("#enable").val();
        if (enable) {
            $('#schedule *').prop('disabled', true);
        }
    }
    $('#submissionDate').val(initialSubmissionDate);
    if(formattedTime)
        $('#myStartTime').val(formattedTime);
    scheduleDateJSONDefault = JSON.stringify($('#myScheduler').scheduler('value'));
    if ($('#submissionDate').is('[readonly]')) {
        var schedulerInfo = $('#myScheduler').scheduler('value');
        schedulerInfo.startDateTime = null;
        schedulerInfo.timeZone = null;
        var scheduleDateJSONDefault = JSON.stringify(schedulerInfo);
        $('#scheduleDateJSON').val(scheduleDateJSONDefault);
        $('#submissionDate').val('');
        $('#myStartTime').val('');
        $("#timezoneFromServer").val('');
    } else if($('#submissionDate').val()){
        var tzString = $("#timezoneFromServer").val();
        if (tzString) {
            var timeZoneData = tzString.split(",");
            var name = timeZoneData[0].split(":")[1].trim();
        }
        var submissionDate = $('#submissionDate').val();
        submissionDate = formatJapaneseDate(submissionDate)
        var startTime = $('#myStartTime').val();
        var formattedDateTime = moment.tz(submissionDate + " " + startTime, "DD-MMM-YYYY hh:mm A", name)
            .format("YYYY-MM-DDTHH:mmZ");
        var schedulerInfo = $('#myScheduler').scheduler('value');
        schedulerInfo.startDateTime = formattedDateTime;
        scheduleDateJSONDefault = JSON.stringify(schedulerInfo);
        $('#scheduleDateJSON').val(scheduleDateJSONDefault);
    }

    function
    calculateNxtInterval(date, interval) {
        var hour = date.hour() + 0;
        var minute = date.minute() + interval;
        if (minute >= 60) {
            hour = hour + Math.floor(minute / 60);
            minute = minute % 60
        }
        var amPm = hour >= 12 ? "PM" : "AM";
        hour = hour % 12;
        hour = hour ? hour : 12;
        minute = (minute > 9 ? minute : "0" + minute);
        hour = hour > 9 ? hour : "0" + hour;
        var now = hour + ':' + minute + ' ' + amPm;
        return now;
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

    function calculateNumberOfIntervals(hour, min, ampm) {
        var count = (12 - hour) * 2;
        if (ampm == "AM") {
            count = count + 48
        }
        var listCount = min < 30 ? count : count - 1;
        return listCount
    }

    function setToday() {
        var scheduledDate = moment($("#schedulerTime").val()) // get the date time from server

        var now = calculateNxtInterval(scheduledDate, 0);
        var hour = now.split(":")[0];
        var nowMin = now.split(":")[1].split(" ")[0];
        var nxtInterval = nowMin < 30 ? 30 - nowMin : 60 - nowMin;
        $("#time").text(now);
        var count = calculateNumberOfIntervals(hour, nowMin, now.split(":")[1].split(" ")[1]);
        for (var i = 0; i < count - 1; i++) {
            $("#myScheduler #timeSelect ul").append('<li><a href="#">' +
                calculateNxtInterval(scheduledDate, nxtInterval) + '</a></li>');
            nxtInterval += 30
        }
        // Its going to be always UTC based as server would run in UTC only and now we have custom Timezone support list only so could break if any other timezone server. TODO need to make full proof
        var prefTimezone = $("#timezoneFromServer").val();
        if (prefTimezone) {
            var timeZoneData = prefTimezone.split(",");
            if(timeZoneData){
                var name = timeZoneData[0].split(":")[1].trim();
                var offset = timeZoneData[1].substring(8).trim();
                $('#myScheduler').scheduler('value', {
                    startDateTime: scheduledDate.format("YYYY-MM-DDTHH:mm:ss")+offset,
                    timeZone: {
                        name: name,
                        offset: offset
                    }
                });
            }
        }

    }


});
function parseServerJson(scheduleInfo) {
    var scheduleInfoJson = JSON.parse(scheduleInfo);
    if (scheduleInfoJson.timeZone) {
        if (scheduleInfoJson.timeZone.text) {
            delete scheduleInfoJson.timeZone["text"];
        }
        if (scheduleInfoJson.timeZone.selected != undefined) {
            delete scheduleInfoJson.timeZone["selected"];
        }
    }
    return scheduleInfoJson
}
function highlightCurrentDayForWeeklyFrequency(timeZone) {

    var currentDayOfWeek = moment.tz(timeZone).day();
    switch (currentDayOfWeek) {
        case 0:
            $('#repeat-weekly-sun').checkbox('check').addClass("active");
            break;
        case 1:
            $('#repeat-weekly-mon').checkbox('check').addClass("active");
            break;
        case 2:
            $('#repeat-weekly-tue').checkbox('check').addClass("active");
            break;
        case 3:
            $('#repeat-weekly-wed').checkbox('check').addClass("active");
            break;
        case 4:
            $('#repeat-weekly-thu').checkbox('check').addClass("active");
            break;
        case 5:
            $('#repeat-weekly-fri').checkbox('check').addClass("active");
            break;
        case 6:
            $('#repeat-weekly-sat').checkbox('check').addClass("active");
    }
}


