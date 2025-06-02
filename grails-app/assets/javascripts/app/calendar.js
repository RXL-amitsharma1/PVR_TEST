var EVENTS_TYPE_CONSTANTS = {
    EXECUTED_ADHOC_REPORT: "EXECUTED_ADHOC_REPORT",
    SCHEDULED_ADHOC_REPORT: "SCHEDULED_ADHOC_REPORT",
    EXECUTED_PERIODIC_REPORT: "EXECUTED_PERIODIC_REPORT",
    SCHEDULED_PERIODIC_REPORT: "SCHEDULED_PERIODIC_REPORT",
    EXECUTED_ICSR_REPORT: "EXECUTED_ICSR_REPORT",
    SCHEDULED_ICSR_REPORT: "SCHEDULED_ICSR_REPORT",
    REPORT_REQUEST: "REPORT_REQUEST",
    ACTION_ITEM: "ACTION_ITEM",
    SCHEDULED_CASE_SERIES: "SCHEDULED_CASE_SERIES",
    EXECUTED_CASE_SERIES: "EXECUTED_CASE_SERIES"
};

$(function () {

    var calendarId = 'calendar';

    var init_calendar = function (calendarId) {

        $('div[id^=' + calendarId + ']').fullCalendar({
            customButtons: {
                reload: {
                    icon: 'reload',
                    click: function () {
                        $(this).closest('div[id^=' + calendarId + ']').fullCalendar('refetchEvents');
                    }
                }
            },
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'reload month,basicWeek,basicDay'
            },
            aspectRatio: 2,
            firstDay: 1,
            eventLimit: true,   // allow "more" link when there are too many events

            dayClick: function (date, jsEvent, view) {
                date = moment(date).format(DEFAULT_DATE_DISPLAY_FORMAT);
                $("#dueDateHidden").val(date);
                $("#createEventModal #modalTitle").text($.i18n._("calendar.addEvents") + " ( " + date + " )");
                $('#createEventModal').modal();
                $('#createReportRequest').attr('href', "" + createReportRequestUrl + "?dueDate=" + date);
            },

            eventClick: function (calEvent, jsEvent, view) {
                if (calEvent.url) {
                    window.open(calEvent.url, "_blank");
                    return false;
                }

                //To check if event is Action Item.
                if (calEvent.url == undefined) {
                    var actionItemId = calEvent.id;
                    bindActionItemCRUD(actionItemId);
                }
                // change the border style
                $(this).css({
                    "border-color": "#ba3939",
                    "border-width": "1px",
                    "border-style": "dashed"
                });

            },

            eventMouseover: function (calEvent, jsEvent, view) {

                // change the border style
                $(this).css({
                    "border-color": "#ba3939",
                    "border-width": "1px",
                    "border-style": "dashed"
                });
                if (view.name !== 'agendaDay') {
                    $(jsEvent.target).attr('title', calEvent.title);
                }

            },

            eventMouseout: function (calEvent, jsEvent, view) {

                // change the border style
                $(this).css({
                    "border-color": "#cccccc",
                    "border-width": "1px",
                    "border-style": "solid"
                });

            },

            //Calendar api automatically appends the start and end date of the rendered event.
            //When user clicks on the next or previous or today then these events are re-fetched.
            events: function (start, end, timezone, callback) {
                jQuery.ajax({
                    url: eventsUrl,
                    type: 'POST',
                    dataType: 'json',
                    data: {
                        start: start.format(DEFAULT_DATE_DISPLAY_FORMAT),
                        end: end.format(DEFAULT_DATE_DISPLAY_FORMAT)
                    }
                })
                    .done(function (result) {
                        var events = [];
                        if (!!result) {
                            $.map(result, function (r) {
                                var startDate = (r.eventType == EVENTS_TYPE_CONSTANTS.ACTION_ITEM ? moment.utc(r.startDate) : moment.utc(r.startDate).tz(userTimeZone));
                                var endDate = r.endDate ? moment.utc(r.endDate).tz(userTimeZone) : undefined;
                                events.push({
                                    id: r.id,
                                    title: r.title,
                                    start: startDate,
                                    end: endDate,
                                    url: getEventUrl(r.eventType, r.id),
                                    allDay: r.allDay,
                                    color: r.color,
                                    textColor: r.textColor
                                });
                            });
                        }
                        callback(events);
                    });
            },

            loading: function (isLoading, view) {
                if (isLoading) {
                    $('.alert-info').removeClass('hide');
                } else {
                    $('.alert-info').addClass('hide');
                }
            }
            ,
            locale: userLocale

        });
    };

    //Initiate the calendar.
    init_calendar(calendarId);

    $('#actionItemModal').find('.datepicker').datepicker({
        allowPastDates: true,
        momentConfig: {
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    //Bind the click event on the create action item button.
    $("#createActionItem").on('click', function () {
        actionItem.actionItemModal.init_action_item_modal(true, PR_Calendar);
    });
    hideCalendarModal();
});

function bindActionItemCRUD(actionItemId) {
    $('#actionItemModal').find('#deleteActionItem').removeClass('hide');
    actionItem.actionItemModal.view_action_item(actionItemId);

    //Click event bind to the delete button.
    $('.action-item-delete').on('click', function () {
        actionItem.actionItemModal.delete_action_item(actionItemId, true, PR_Calendar);
    });

    //Click event bind to the edit button.
    $('.edit-action-item').on('click', function () {
        actionItem.actionItemModal.edit_action_item(hasAccessOnActionItem, actionItemId, true, null, null);
    });

    //Click event bind to the update button.
    $('.update-action-item').on('click', function () {
        actionItem.actionItemModal.update_action_item(true, $('#actionItemModal'), PR_Calendar);
    });

}

function hideCalendarModal() {
    $('.prModalHide').on('click', function () {
        $('#createEventModal').modal('hide');
    });
}

function getEventUrl(eventType, id) {
    if (eventType == EVENTS_TYPE_CONSTANTS.REPORT_REQUEST) {
        return reportRequestShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.EXECUTED_PERIODIC_REPORT) {
        return executedReportShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.EXECUTED_ADHOC_REPORT) {
        return executedReportShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.SCHEDULED_ADHOC_REPORT) {
        return adhocReportShowURL + "/" + id;
    }

    if (eventType == EVENTS_TYPE_CONSTANTS.EXECUTED_ICSR_REPORT) {
        return executedReportShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.SCHEDULED_ICSR_REPORT) {
        return icsrReportShowURL + "/" + id;
    }

    if (eventType == EVENTS_TYPE_CONSTANTS.SCHEDULED_PERIODIC_REPORT) {
        return periodicReportShowURL + "/" + id;
    }

    if (eventType == EVENTS_TYPE_CONSTANTS.SCHEDULED_CASE_SERIES) {
        return caseSeriesShowURL + "/" + id;
    }

    if (eventType == EVENTS_TYPE_CONSTANTS.EXECUTED_CASE_SERIES) {
        return executedCaseSeriesShowURL + "/" + id;
    }
    return undefined;
}