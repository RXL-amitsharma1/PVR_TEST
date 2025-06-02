package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper

class CalendarEventDTO {

    Long id
    String title
    ColorCodeEnum colorCode
    boolean allDay = false
    ColorCodeEnum textColorCode
    EventType eventType
    Date startDate
    Date endDate

    Map toMap() {
        Map map = [id: id, title: title, color: colorCode.code, allDay: allDay, textColor: textColorCode.code, eventType: eventType.name(), startDate: startDate.format(DateUtil.DATEPICKER_UTC_FORMAT), endDate: endDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)]
        return map
    }


    static CalendarEventDTO getEvent(PeriodicReportConfiguration configuration, Date start) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : configuration.id,
                title : configuration.reportName,
                eventType : EventType.SCHEDULED_PERIODIC_REPORT,
                colorCode : ColorCodeEnum.SCHEDULED_PERIODIC_REPORT_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_LIGHT,
                startDate : start,
                endDate : start
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(IcsrReportConfiguration configuration, Date start) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : configuration.id,
                title : configuration.reportName,
                eventType : EventType.SCHEDULED_ICSR_REPORT,
                colorCode : ColorCodeEnum.SCHEDULED_ICSR_REPORT_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_LIGHT,
                startDate : start,
                endDate : start
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(Configuration configuration, Date start) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : configuration.id,
                title : configuration.reportName,
                eventType : EventType.SCHEDULED_ADHOC_REPORT,
                colorCode : ColorCodeEnum.SCHEDULED_ADHOC_REPORT_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_DARK,
                startDate : start,
                endDate : start
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(CaseSeries caseSeries, Date start) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : caseSeries.id,
                title : caseSeries.seriesName,
                eventType : EventType.SCHEDULED_CASE_SERIES,
                colorCode : ColorCodeEnum.SCHEDULED_CASE_SERIES_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_DARK,
                startDate : start,
                endDate : start
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(ExecutedIcsrReportConfiguration configuration) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : configuration.id,
                title : configuration.reportName,
                eventType : EventType.EXECUTED_ICSR_REPORT,
                colorCode : ColorCodeEnum.EXECUTED_ICSR_REPORT_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_WHITE,
                startDate : configuration.nextRunDate,
                endDate : configuration.nextRunDate
        )
        return eventDTO
    }


    static CalendarEventDTO getEvent(ExecutedPeriodicReportConfiguration configuration) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : configuration.id,
                title : configuration.reportName,
                eventType : EventType.EXECUTED_PERIODIC_REPORT,
                colorCode : ColorCodeEnum.EXECUTED_PERIODIC_REPORT_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_WHITE,
                startDate : configuration.nextRunDate,
                endDate : configuration.nextRunDate
        )
        return eventDTO
    }


    static CalendarEventDTO getEvent(ExecutedConfiguration configuration) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : configuration.id,
                title : configuration.reportName,
                eventType : EventType.EXECUTED_ADHOC_REPORT,
                colorCode : ColorCodeEnum.EXECUTED_ADHOC_REPORT_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_WHITE,
                startDate : configuration.nextRunDate,
                endDate : configuration.nextRunDate
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(ExecutedCaseSeries caseSeries) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : caseSeries.id,
                title : caseSeries.seriesName,
                eventType : EventType.EXECUTED_CASE_SERIES,
                colorCode : ColorCodeEnum.EXECUTED_CASE_SERIES_COLOR,
                allDay : false,
                textColorCode : ColorCodeEnum.TEXT_COLOR_WHITE,
                startDate : caseSeries.nextRunDate,
                endDate : caseSeries.nextRunDate
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(ActionItem actionItem) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : actionItem.id,
                title : actionItem.description + "(" + ViewHelper.getMessage(actionItem.status?.getI18nKey()) + ")",
                eventType : EventType.ACTION_ITEM,
                colorCode : ColorCodeEnum.ACTION_ITEM,
                allDay : true,
                textColorCode : ColorCodeEnum.TEXT_COLOR_DARK,
                startDate : actionItem.dueDate
        )
        return eventDTO
    }

    static CalendarEventDTO getEvent(ReportRequest reportRequest) {
        CalendarEventDTO eventDTO = new CalendarEventDTO(
                id : reportRequest.id,
                title : reportRequest.reportName,
                eventType : EventType.REPORT_REQUEST,
                colorCode : ColorCodeEnum.REPORT_REQUEST,
                allDay : true,
                textColorCode : ColorCodeEnum.TEXT_COLOR_DARK,
                startDate : reportRequest.dueDate
        )
        return eventDTO
    }


    static enum EventType {
        EXECUTED_ADHOC_REPORT,
        SCHEDULED_ADHOC_REPORT,
        EXECUTED_PERIODIC_REPORT,
        SCHEDULED_PERIODIC_REPORT,
        EXECUTED_ICSR_REPORT,
        SCHEDULED_ICSR_REPORT,
        REPORT_REQUEST,
        ACTION_ITEM,
        EXECUTED_CASE_SERIES,
        SCHEDULED_CASE_SERIES
    }

    static enum ColorCodeEnum {
        EXECUTED_ADHOC_REPORT_COLOR("#3b789e"),
        SCHEDULED_ADHOC_REPORT_COLOR("#acd3e1"),
        EXECUTED_PERIODIC_REPORT_COLOR("#609c78"),
        SCHEDULED_PERIODIC_REPORT_COLOR("#abd9b8"),
        EXECUTED_ICSR_REPORT_COLOR("#A569BD"),
        SCHEDULED_ICSR_REPORT_COLOR("#D2B4DE"),
        REPORT_REQUEST("#e1bdac"),
        ACTION_ITEM("#FF99F0"),
        EXECUTED_CASE_SERIES_COLOR("#9999F0"),
        SCHEDULED_CASE_SERIES_COLOR("#5555F0"),
        TEXT_COLOR_DARK("#000000"),
        TEXT_COLOR_LIGHT("#333333"),
        TEXT_COLOR_WHITE("#ffffff")

        String code

        ColorCodeEnum(String code) {
            this.code = code
        }
    }

}
