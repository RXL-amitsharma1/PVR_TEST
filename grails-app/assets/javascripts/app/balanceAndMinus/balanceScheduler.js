var selectedSourceProfiles = [];
var sourceProfileName;
var bmQuerySectionChildCount = 0;
$(function () {
    createOnly = ($('#creatable').val() === 'true');
    $('.pickList_targetListContainer').addClass('col-md-6 width-45-right');
    $('.pickList_sourceListContainer').addClass('col-md-6 width-45');
    $('.pickList_controlsContainer').addClass('col-md-1');
    $('.buttonBar').addClass('m-t-10');

    var startTime = $('#startDateTime').val();
    var repeatInterval = $('#repeatInterval').val();
    var configSelectedTz = $("#configSelectedTimeZone").val();
    var prefTimezone = $("#timezoneFromServer").val();
    var name = '';
    var offset = '';
    if (configSelectedTz == '') {
        var timeZoneData = prefTimezone.split(",");
        name = timeZoneData[0].split(":")[1].trim();
        offset = timeZoneData[1].substring(8).trim();
    } else {
        var timeZoneData = configSelectedTz.split(",");
        name = timeZoneData[0].split(":")[1].trim();
        offset = timeZoneData[1].substring(8).trim();
    }

    if ($('#startDateTime').val() == '') {
        setToday();
    }

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

    $('#myScheduler').on('changed.fu.scheduler', function (event) {
        var newInfo = $('#myScheduler').scheduler('value');
        if (newInfo.recurrencePattern.includes(";COUNT=")) {
            newInfo.recurrencePattern = "FREQ=RUN_ONCE"
        }
        $('#startDateTime').val(newInfo.startDateTime);
        $('#repeatInterval').val(newInfo.recurrencePattern);
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);

    }).trigger("changed.fu.scheduler");

    $('#myDatePicker').on('changed.fu.datepicker dateClicked.fu.datepicker', function () {
        setDatePickerTime()
    });

    if ($('#isDisabled').val() != 'true') {
        $('#myScheduler').find('#myDatePicker').datepicker('setRestrictedDates', [{
            from: -Infinity,
            to: moment().tz(userTimeZone).subtract(1, 'days')
        }]);
    }

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
    }

    function setToday() {
        var scheduledDate = moment($("#schedulerTime").val()); // get the date time from server
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

        startTime = scheduledDate.format("YYYY-MM-DDTHH:mm:ss") + offset;
        repeatInterval = prefTimezone

    }

    function calculateNxtInterval(date, interval) {
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

    function calculateNumberOfIntervals(hour, min, ampm) {
        var count = (12 - hour) * 2;
        if (ampm == "AM") {
            count = count + 48
        }
        var listCount = min < 30 ? count : count - 1;
        return listCount
    }

    bmQuerySectionChildCount = $("#bmQuerySectionList").attr("data-counter");
    var init = function () {
        if (bmQuerySectionChildCount == 0) {
            addBmQuerySection(0);
        } else {
            $("#bmQuerySectionList").find(".executeFor").on("change", function (e) {
                executorForClassOnChange(this);
                startDatePickerOnChange(this);
                endDatePickerOnChange(this);
            });

            $("#bmQuerySectionList").find(".executionStartDate").on("change", function (e) {
                startDatePickerOnChange(this);
            }).trigger('change');

            $("#bmQuerySectionList").find(".executionEndDate").on("change", function (e) {
                endDatePickerOnChange(this);
            }).trigger('change');

            $("#bmQuerySectionList").find(".excludedCases").on("click", function (e) {
                flagCaseExcludeOnClick(this);
            });

            $("#bmQuerySectionList").find(".pickList_sourceListContainer").on("click", function (e) {
                pickListOnChange(this);
            });

            for (var i = 0; i < bmQuerySectionChildCount; i++) {
                var selectedSources = $("#bmQuerySectionList").find(".sourceProfile" + i).val();
                $("#bmQuerySectionList").find(".sourceProfile" + i).parents("#bmQuerySections" + i).addClass(selectedSources);
                if (!selectedSourceProfiles.includes(selectedSources)) {
                    selectedSourceProfiles.push(selectedSources);
                    $("#bmQuerySections" + i).find("select[id$=distinctTables]").html(fetchTableOptions(i));
                    $("#bmQuerySections" + i).find("select[id$=distinctTables]").pickList({
                        afterRefresh: caseInsensitiveSortWrapper(this) // don't use in-built sort option of pick-list, that is case sensitive.
                    });
                    $("#bmQuerySections" + i).find("select[id$=distinctTables]").parent().find(".pickList_list.pickList_targetList").css("height", "305px");
                    $("#bmQuerySections" + i).find("select[id$=distinctTables]").parent().find(".pickList_listLabel.pickList_sourceListLabel").text($.i18n._("picklist.available"));
                    $("#bmQuerySections" + i).find("select[id$=distinctTables]").parent().find(".pickList_listLabel.pickList_targetListLabel").text($.i18n._("picklist.selected"));
                    $("#bmQuerySections" + i).find("select[id$=distinctTables]").parent().find(".pickList_listLabel.pickList_sourceListLabel").html($("#bmQuerySections" + i).find("select[id$=distinctTables]").parent().find(".pickList_listLabel.pickList_sourceListLabel").html() +
                        '<br> <input class="fieldNameFilter" style="width:100%;" placeholder="' + $.i18n._("fieldprofile.search.label") + '" >');
                }
            }

        }
    }

    var addBmQuerySection = function (index) {
        var clone = $("#bmQuerySections_clone").clone();
        var htmlId = 'bmQuerySections[' + bmQuerySectionChildCount + '].';

        clone.find("input[id$=id]")
            .attr('id', htmlId + 'id')
            .attr('name', htmlId + 'id');

        clone.find("input[id$=dynamicFormEntryDeleted]")
            .attr('id', htmlId + 'dynamicFormEntryDeleted')
            .attr('name', htmlId + 'dynamicFormEntryDeleted');

        clone.find("input[id$=sourceProfile]")
            .attr('id', htmlId + 'sourceProfile')
            .attr('name', htmlId + 'sourceProfile').val(sourceProfileName);

        clone.find("span[id^=sourceProfileLabel]").attr('id', 'sourceProfileLabel' + bmQuerySectionChildCount);

        clone.find("select[id$=executeFor]")
            .attr('id', htmlId + 'executeFor')
            .attr('name', htmlId + 'executeFor');

        clone.find("div[id$=executeFor]").attr('id', htmlId + 'executeFor');

        clone.find("input[id$=executionStartDate]")
            .attr('id', htmlId + 'executionStartDate')
            .attr('name', htmlId + 'executionStartDate');

        clone.find("input[id$=executionEndDate]")
            .attr('id', htmlId + 'executionEndDate')
            .attr('name', htmlId + 'executionEndDate');

        clone.find("div[class^=bqmqDateCls-]")
            .attr('class', 'bqmqDateCls-' + bmQuerySectionChildCount);

        clone.find("input[id$=xValue]")
            .attr('id', htmlId + 'xValue')
            .attr('name', htmlId + 'xValue');

        clone.find("div[class^=lastXDaysCls-]")
            .attr('class', 'lastXDaysCls-' + bmQuerySectionChildCount);

        clone.find("textArea[id$=includeCases]")
            .attr('id', htmlId + 'includeCases')
            .attr('name', htmlId + 'includeCases');

        clone.find("div[class^=includeCasesCls-]")
            .attr('class', 'includeCasesCls-' + bmQuerySectionChildCount + ' toAddContainer');

        clone.find("input[id$=flagCaseExclude]").attr('id', htmlId + 'flagCaseExclude').attr('name', htmlId + 'flagCaseExclude');

        clone.find("a[class^=flagCaseExcludeCls-]")
            .attr('class', 'flagCaseExcludeCls-' + bmQuerySectionChildCount);

        clone.find("textArea[id$=excludeCases]")
            .attr('id', htmlId + 'excludeCases')
            .attr('name', htmlId + 'excludeCases')
            .attr('class', 'form-control caseNum-filter flagCaseExcludeCls-' + bmQuerySectionChildCount);

        clone.find("select[id$=distinctTables]")
            .attr('id', htmlId + 'distinctTables')
            .attr('name', htmlId + 'distinctTables').html(fetchTableOptions(index));


        //overriding method for pick-list
        pickListOnChange(clone.find("select[id$=distinctTables]"));

        clone.find("select[id$=executeFor]").on('change', function () {
            executorForClassOnChange(this);
            startDatePickerOnChange(this);
            endDatePickerOnChange(this);
        });


        clone.find("input[id$=flagCaseExclude]").on("click", function () {
            flagCaseExcludeOnClick(this);
        });

        clone.attr('id', 'bmQuerySections' + bmQuerySectionChildCount);
        if (index >= 0) {
            var srcProfile = $("#sourceProfile :selected")[index];
            if (srcProfile) {
                $("#bmQuerySectionList").append(clone);
                var selectedSources = $("#sourceProfile :selected")[index].value;
                selectedSourceProfiles.push(selectedSources);
                $("#sourceProfileLabel" + bmQuerySectionChildCount).html($("#sourceProfile :selected")[index].innerText);
                clone.find("input[id$=sourceProfile]").val($("#sourceProfile :selected")[index].value);
                $("#bmQuerySectionList").find("#bmQuerySections" + bmQuerySectionChildCount).attr('class', selectedSources);
            }
        }
        clone.show();
    };

    init();

    $(document).on('change', '#sourceProfile', function () {
        var currentSourceProfileSize = $("#sourceProfile :selected").length;
        if (currentSourceProfileSize == 0) {
            $("#bmQuerySectionList").empty();
            selectedSourceProfiles = [];
        } else {
            for (var i = 0; i < currentSourceProfileSize; i++) {
                var selectedSources = $("#sourceProfile :selected")[i].value;
                if (!selectedSourceProfiles.includes(selectedSources)) {
                    var bbQuerySectionsDiv = $("#bmQuerySectionList").find("div[id^=bmQuerySections]");
                    bmQuerySectionChildCount = bbQuerySectionsDiv.length;
                    sourceProfileName = $("#sourceProfile :selected")[i].innerText;
                    addBmQuerySection(i);
                    break;
                }
            }
        }

        var currentSourceProfiles = [];
        for (var i = 0; i < currentSourceProfileSize; i++) {
            var selectedSources = $("#sourceProfile :selected")[i].value;
            currentSourceProfiles.push(selectedSources);
        }
        var removedElement = arr_diff(selectedSourceProfiles, currentSourceProfiles);
        if (removedElement != '') {
            $("#bmQuerySectionList").find("." + removedElement).find("input[id$=dynamicFormEntryDeleted]").attr('value', 'true');
            $("#bmQuerySectionList").find("." + removedElement).hide();
            selectedSourceProfiles = arr_remove(selectedSourceProfiles, removedElement);
        }
    });

    if (!createOnly) {
        var currentSourceProfileSize = $("#sourceProfile :selected").length;
        for (var i = 0; i < currentSourceProfileSize; i++) {
            var selectedSources = $("#sourceProfile :selected")[i].value;
            if (!selectedSourceProfiles.includes(selectedSources)) {
                selectedSourceProfiles.push(selectedSources);
                $("#bmQuerySections" + i).find("select[id$=distinctTables]").html(fetchTableOptions(i));
                $("#bmQuerySections" + i).find("select[id$=distinctTables]").pickList({
                    afterRefresh: caseInsensitiveSortWrapper(this) // don't use in-built sort option of pick-list, that is case sensitive.
                });
            }
        }
    }

    $('.fieldNameFilter').on('keyup', function () {
        var f = $(this).val().toLowerCase();
        if (_.isEmpty(f)) {
            $(this).parents(".pickList").find(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
        } else {
            $(this).parents(".pickList").find(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
        }
        $(this).parents(".pickList_listContainer").find(".pickList_sourceList li").each(function () {
            var elem = $(this);
            if (elem.html().toLowerCase().indexOf(f) > -1)
                elem.show();
            else
                elem.hide();
        });
    });

    disable_enable();
});


function pickListOnChange(container) {
    $(container).pickList({
        afterRefresh: caseInsensitiveSortWrapper(this) // don't use in-built sort option of pick-list, that is case sensitive.
    });

    $(container).parent().find(".pickList_list.pickList_targetList").css("height", "305px");
    $(container).parent().find(".pickList_listLabel.pickList_sourceListLabel").text($.i18n._("picklist.available"));
    $(container).parent().find(".pickList_listLabel.pickList_targetListLabel").text($.i18n._("picklist.selected"));
    $(container).parent().find(".pickList_listLabel.pickList_sourceListLabel").html($(container).parent().find(".pickList_listLabel.pickList_sourceListLabel").html() +
        '<br> <input class="fieldNameFilter" style="width:100%;" placeholder="' + $.i18n._("fieldprofile.search.label") + '" >');

    $(container).parent().find('.fieldNameFilter').on('keyup', function () {
        var f = $(this).val().toLowerCase();
        if (_.isEmpty(f)) {
            $(this).parents(".pickList").find(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
        } else {
            $(this).parents(".pickList").find(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
        }
        $(container).parent().find(".pickList_sourceList li").each(function () {
            var elem = $(this);
            if (elem.html().toLowerCase().indexOf(f) > -1)
                elem.show();
            else
                elem.hide();
        });
    });
}

function executorForClassOnChange(container) {
    var elementId = (container.id);
    var index = parseInt(elementId.replace(/[^0-9\.]+/g, ""));

    $(document.getElementById('bmQuerySections[' + index + '].executionStartDate')).val('');
    $(document.getElementById('bmQuerySections[' + index + '].executionEndDate')).val('');
    $(document.getElementById('bmQuerySections[' + index + '].xValue')).val('');
    $(document.getElementById('bmQuerySections[' + index + '].includeCases')).val('');

    var bqmqDateCls = $(document.getElementsByClassName("bqmqDateCls-" + index));
    var lastXDaysCls = $(document.getElementsByClassName("lastXDaysCls-" + index));
    var includeCasesCls = $(document.getElementsByClassName("includeCasesCls-" + index));

    if ($(container).val() == "ETL_START_DATE") {
        lastXDaysCls.css("display", "none");
        includeCasesCls.css("display", "none");
        bqmqDateCls.css("display", "block");
    } else if ($(container).val() == "LAST_X_DAYS" || $(container).val() == "LAST_X_ETL") {
        bqmqDateCls.css("display", "none");
        includeCasesCls.css("display", "none");
        lastXDaysCls.css("display", "block");
    } else if ($(container).val() == "CASE_LIST") {
        bqmqDateCls.css("display", "none");
        lastXDaysCls.css("display", "none");
        includeCasesCls.css("display", "block");
    } else {
        bqmqDateCls.css("display", "none");
        lastXDaysCls.css("display", "none");
        includeCasesCls.css("display", "none");
    }
}

function flagCaseExcludeOnClick(container) {
    var elementId = (container.id);
    var index = parseInt(elementId.replace(/[^0-9\.]+/g, ""));
    var flagCaseExcludeCls = $(document.getElementsByClassName("flagCaseExcludeCls-" + index));
    if ($(container).prop('checked') == true) {
        flagCaseExcludeCls.css("display", "block");
    } else {
        flagCaseExcludeCls.css("display", "none");
    }
}

function startDatePickerOnChange(container) {
    var elementId = (container.id);
    var index = parseInt(elementId.replace(/[^0-9\.]+/g, ""));
    var asOfDate = null;

    var executionStartDateId = $(document.getElementById('bmQuerySections[' + index + '].executionStartDate'));
    if (executionStartDateId.val()) {
        asOfDate = executionStartDateId.val()
    }

    var today = new Date();
    var tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);

    executionStartDateId.parent().parent().parent().find('.executionStartDatePicker').datepicker({
        allowPastDates: true,
        date: asOfDate,
        restricted: [{from: tomorrow, to: Infinity}],
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    executionStartDateId.on('change', function () {
        executionStartDateId.val("");
    });
}

function endDatePickerOnChange(container) {

    var elementId = (container.id);
    var index = parseInt(elementId.replace(/[^0-9\.]+/g, ""));
    var asOfDate = null;

    var executionEndDateId = $(document.getElementById('bmQuerySections[' + index + '].executionEndDate'));

    var today = new Date();
    var tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);

    if (executionEndDateId.val()) {
        asOfDate = executionEndDateId.val();
    }

    executionEndDateId.parent().parent().parent().find('.executionEndDatePicker').datepicker({
        allowPastDates: true,
        date: asOfDate,
        restricted: [{from: tomorrow, to: Infinity}],
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    executionEndDateId.on('change', function () {
        executionEndDateId.val("");
    });
}

function fetchTableOptions(index) {
    var selectedSources = $("#sourceProfile :selected")[index];
    if (selectedSources != undefined) {
        sourceProfileName = $("#sourceProfile :selected")[index].innerText;
        var selectOptions = '';
        $.ajax({
            url: distTablesUrl,
            async: false,
            data: {
                sourceProfileName: sourceProfileName
            },
            dataType: 'json'
        })
            .done(function (data) {
                var selectedDistinctTables = $("#bmQuerySections" + index).find(".selectedDistinctTables").val();
                for (var i = 0; i < data.items.length; i++) {
                    if (selectedDistinctTables != undefined && selectedDistinctTables.includes(data.items[i].id)) {
                        selectOptions = selectOptions + "<option value='" + data.items[i].id + "' selected='selected'>" + data.items[i].text + "</option>";
                    } else {
                        selectOptions = selectOptions + "<option value='" + data.items[i].id + "'>" + data.items[i].text + "</option>";
                    }
                }
            });
        return selectOptions;
    }
    return "";
}

function caseInsensitiveSortWrapper(container) {
    var pickList_sourceList = $(container).parent().find('.pickList_sourceList');
    var pickList_targetList = $(container).parent().find('.pickList_targetList');
    caseInsensitiveSort(pickList_sourceList, 'label');
    caseInsensitiveSort(pickList_targetList, 'label');
}

function caseInsensitiveSort(list, sortItem) {
    var items = [];

    list.children().each(function () {
        items.push($(this));
    });

    items.sort(function (a, b) {
        var t1 = a.attr(sortItem).toLowerCase();
        var t2 = b.attr(sortItem).toLowerCase();
        return t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
    });

    list.empty();

    for (var i = 0; i < items.length; i++) {
        list.append(items[i]);
    }
}

function arr_remove(arr, value) {
    arr = _(arr).filter(function (item) {
        return item != value;
    });
    return arr;
}

function arr_diff(a1, a2) {
    var result = [];
    for (var i = 0; i < a1.length; i++) {
        if (a2.indexOf(a1[i]) === -1) {
            result.push(a1[i]);
        }
    }
    return result;
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

function disable_enable() {
    if ($('#isDisabled').val() == 'true') {
        $('#myScheduler').scheduler('disable');
        $(".pickList_addAll").attr("disabled", true);
        $(".pickList_add").attr("disabled", true);
        $(".pickList_remove").attr("disabled", true);
        $(".pickList_removeAll").attr("disabled", true);
    } else {
        $('#myScheduler').scheduler('enable');
    }
}
