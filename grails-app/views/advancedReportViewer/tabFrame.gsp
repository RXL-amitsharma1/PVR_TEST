<%@ page import="com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper; groovy.json.JsonOutput" %>
<html>
<head>

    <title><g:message code="app.viewResult.title"/></title>
    <asset:stylesheet href="application.css"/>
    <script>

        var LOAD_THEME_URL = "${createLink(controller: 'preference', action: 'loadTheme')}";
        var userLocale = "${session.getAttribute('org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE')?.language?:'en'}";
        var APP_PATH = '${request.contextPath}';
        var appContext = "${request.contextPath}";
        var APP_ASSETS_PATH = APP_PATH + '/assets/';
    </script>
    <asset:javascript src="vendorUi/jquery/jquery-3.7.1.min.js"/>
    <asset:javascript src="vendorUi/jquery-ui/jquery-ui.min.js"/>
    <asset:javascript src="application.js"/>
    <asset:javascript src="vendorUi/fuelux/fuelux.min.js"/>
    <asset:stylesheet src="rowGroup.dataTables.min.css"/>
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

    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/map.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/world.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
    <asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
    <asset:javascript src="app/report/advancedViewerChart.js"/>
</head>

<body>
<asset:javascript src="UIConstants.js"/>
<asset:javascript src="common/change-theme.js"/>
<g:render template="tabContent" model="${pageScope.variables}"/>
<asset:javascript src="common/jquery.app.js"/>
</body>
</html>