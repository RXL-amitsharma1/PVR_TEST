<%@ page import="com.rxlogix.enums.WidgetTypeEnum; com.rxlogix.ChartOptionsUtils; com.rxlogix.enums.ReportFormatEnum" %>
<div class="grid-stack-item"
     gs-id="${widget.reportWidget.id}"
     gs-x="${widget.reportWidget.x}" gs-y="${widget.reportWidget.y}"
     gs-w="${widget.reportWidget.width}" gs-h="${widget.reportWidget.height}"
     gs-auto-position="${widget.reportWidget.autoPosition}">
    <g:if test="${widget.type == WidgetTypeEnum.LAST_REPORTS}">
        <g:render template="includes/latestReportsWidget" model="${pageScope.variables}"/>
    </g:if>
    <g:elseif test="${widget.type == WidgetTypeEnum.CHART}">
        <g:render template="includes/chartWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.CONFIGURATIONS}">
        <g:render template="includes/configurationsWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ACTION_ITEMS}">
        <g:render template="includes/actionItemsWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.CALENDAR}">
        <g:render template="includes/calendarWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY}">
        <g:render template="includes/aggregateReportSummaryWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ADHOC_REPORTS_SUMMARY}">
        <g:render template="includes/adhocReportSummaryWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ACTION_ITEMS_SUMMARY}">
        <g:render template="includes/actionItemsSummaryWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.REPORT_REQUEST_SUMMARY}">
        <g:render template="includes/reportRequestSummaryWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ADVANCED_REPORT_REQUEST}">
        <g:render template="includes/advancedReportRequestWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ADVANCED_PUBLISHER}">
        <g:render template="includes/advancedPublisherWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.COMPLIANCE_PUBLISHER}">
        <g:render template="includes/compliancePublisherWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.SPOTFIRE}">
        <g:render template="includes/spotfireWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ETL}">
        <g:render template="includes/etlWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_ACTION_ITEMS}">
        <g:render template="includes/actionItemsWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_ACTION_ITEMS_SUMMARY}">
        <g:render template="includes/actionItemsSummaryWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.CASE_COUNT_BY_ERROR}">
        <g:render template="includes/caseCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.SUBMISSION_COUNT_BY_ERROR}">
        <g:render template="includes/submissionCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_CASE_COUNT}">
        <g:render template="includes/caseCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_PRODUCT_COUNT}">
        <g:render template="includes/productCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_ENTRYSITE_COUNT}">
        <g:render template="includes/entrySiteCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_ERROR_COUNT}">
        <g:render template="includes/top20ErrorsCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_LATEST_ISSUES}">
        <g:render template="includes/latestQualityIssuesWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.QUALITY_CASE_REPORT_TYPE}">
        <g:render template="includes/caseReportTypeCountWidget" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ACTION_PLAN_PVQ}">
        <g:render template="includes/actionPlanPvq" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ACTION_PLAN_PVC}">
        <g:render template="includes/actionPlanPvc" model="${pageScope.variables}"/>
    </g:elseif>
    <g:elseif test="${widget.type == WidgetTypeEnum.ICSR_TRACKING}">
        <g:render template="includes/icsrTrackingWidget" model="${pageScope.variables}"/>
    </g:elseif>

</div>