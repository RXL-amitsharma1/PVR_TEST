<%@ page import="com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper; groovy.json.JsonOutput" %>
<html>
<head>

    <title><g:message code="app.viewResult.title"/></title>
    <style>
    .dropdown-toggle {
        height: 22px;
    }
    </style>
    <asset:stylesheet href="application.css"/>
    <script>

        var LOAD_THEME_URL = "${createLink(controller: 'preference', action: 'loadTheme')}";
        var userLocale = "${session.getAttribute('org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE')?.language?:'en'}";
        var APP_PATH = '${request.contextPath}';
        var appContext = "${request.contextPath}";
        var APP_ASSETS_PATH = APP_PATH + '/assets/';
    </script>
    <asset:javascript src="application.js"/>
    <script>
        moment.locale(userLocale);
        userTimeZone = "${(session.getAttribute('user.preference.timeZone'))?.ID?:TimeZone.default.ID}";
        if (userLocale == JAPANESE_LOCALE) {
            DEFAULT_DATE_DISPLAY_FORMAT = "YYYY/MM/DD";
            DEFAULT_DATE_TIME_DISPLAY_FORMAT = "YYYY/MM/DD hh:mm A";
        }
        $.ajax({
            url: APP_ASSETS_PATH + 'i18n/' + userLocale + '.json',
            dataType: 'json',
            async: false
        })
            .done(function (data) {
                $.i18n.load(data);
            });

        var resizefunc = [];

    </script>

</head>

<body>
<asset:javascript src="vendorUi/gridstack/gridstack.min.js"/>
<asset:javascript src="vendorUi/gridstack/gridstack.jQueryUI.min.js"/>
<asset:javascript src="vendorUi/fullcalendar/fullcalendar.min.js"/>
<asset:javascript src="vendorUi/fullcalendar/fullcalendar-lang-all.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
<asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
<asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
<asset:javascript src="vendorUi/counterup/jquery.counterup.min.js"/>
<asset:javascript src="app/dashboard/dashboard.js"/>
<asset:javascript src="app/dashboard/addWidgetModal.js"/>
<asset:javascript src="app/actionItem/actionItemModal.js"/>
<asset:javascript src="app/calendar.js"/>
<asset:javascript src="app/workFlow.js"/>
<asset:javascript src="app/report/advancedViewerChart.js"/>
<asset:javascript src="app/report/advancedViewer.js"/>
<asset:javascript src="app/actionPlan.js"/>
<asset:javascript src="app/configuration/configurationCommon.js"/>
<asset:javascript src="app/configuration/deliveryOption.js"/>
<asset:javascript src="app/commonGeneratedReportsActions.js"/>
<asset:javascript src="app/periodicReport.js"/>
<asset:javascript src="UIConstants.js"/>
<asset:javascript src="common/change-theme.js"/>

<asset:javascript src="app/utils/pvr-common-util.js"/>
<asset:javascript src="app/utils/pvr-filter-util.js"/>

<asset:javascript src="datatables/extendedDataTable.js"/>
<asset:stylesheet src="datatables/extendedDataTable.css"/>

<asset:javascript src="datatables/dataTables.columnResize.js"/>
<asset:stylesheet src="datatables/dataTables.columnResize.css"/>

<asset:javascript src="datatables/dataTables.fixedHeader.js"/>
<asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>

<g:render template="viewCasesContent" model="${pageScope.variables}"/>
<asset:javascript src="common/jquery.app.js"/>
<g:render template="/includes/widgets/deleteCaseConfirmationModal"/>
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/icsrProfileConfiguration/submissionHistoryCase"/>
<g:render template="/icsrProfileConfiguration/errorDetails"/>
<g:render template="/icsrProfileConfiguration/caseHistoryDetails"/>
<g:render template="/icsrProfileConfiguration/transmitJustification"/>
<g:form controller="icsr" name="emailForm">
    <g:hiddenField name="exIcsrTemplateQueryId"/>
    <g:hiddenField name="caseNumber"/>
    <g:hiddenField name="versionNumber"/>
    <g:render template="/report/includes/emailToModal"
              model="['isIcsrViewTracking': true, forClass: IcsrProfileConfiguration]"/>
</g:form>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/includes/widgets/confirmation"/>
</body>
</html>