<%@ page import="com.rxlogix.CalendarEventDTO" %>
<div id="legend" class="fc-view-container">
    <p><strong style="font-size: 12px;"><g:message code="calendar.legend.label"/></strong></p>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.EXECUTED_ADHOC_REPORT_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.adhoc.executed.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.SCHEDULED_ADHOC_REPORT_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.adhoc.scheduled.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.EXECUTED_PERIODIC_REPORT_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.periodic.executed.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.SCHEDULED_PERIODIC_REPORT_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.periodic.scheduled.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.EXECUTED_ICSR_REPORT_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.icsr.executed.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.SCHEDULED_ICSR_REPORT_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.icsr.scheduled.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.ACTION_ITEM.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.actionItem.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.REPORT_REQUEST.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.reportRequest.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.EXECUTED_CASE_SERIES_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.caseSeries.executed.label"/></span>

    <div class="legendColorBox"
         style="background-color: ${CalendarEventDTO.ColorCodeEnum.SCHEDULED_CASE_SERIES_COLOR.code}">&nbsp;</div><span
        class="legendLabel text-muted"><g:message code="calendar.caseSeries.scheduled.label"/></span>
</div>
