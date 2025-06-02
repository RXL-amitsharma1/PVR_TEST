<%@ page import="com.rxlogix.config.IcsrReportConfiguration; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.config.ExecutedCustomSQLTemplate; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.config.ExecutedCaseLineListingTemplate; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.config.ReportResult; com.rxlogix.config.ReportField; com.rxlogix.config.ReportTemplate; com.rxlogix.config.TemplateSet;" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.viewResult.title"/></title>
</head>

<body>
<rx:container title="${message(code: 'app.label.report')}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}" var="theInstance"/>
    <div class="row">
        <div class="col-md-11">
            <div class="btn-group">
        <g:link class="btn btn-default waves-effect waves-light" controller="report" action="criteria"
                id="${exIcsrProfileId}">
            <i class="md md-description icon-white"></i>
            <g:message code="app.label.reportCriteria"/>
        </g:link>
    </div>

    <div class="btn-group">
        <g:if test="${exIcsrProfileId}">
            <g:link class="btn btn-default waves-effect waves-light"
                    controller="report" action="downloadXml" target="_black"
                    params="[exIcsrTemplateQueryId: exIcsrTemplateQueryId, outputFormat: ReportFormatEnum.XML.name(), caseNumber: caseNumber, versionNumber: versionNumber, isInDraftMode: params.boolean('isInDraftMode') ?: false, reportLang: params.reportLang]"
                    data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.e2b.r2.xml')}">
                <asset:image src="r2-xml-icon.png" class="xml-icon" height="16" width="16"/>
            </g:link>
            <g:link class="btn btn-default waves-effect waves-light"
                    controller="report" action="downloadXml" target="_black"
                    params="[exIcsrTemplateQueryId: exIcsrTemplateQueryId, outputFormat: ReportFormatEnum.R3XML.name(), caseNumber: caseNumber, versionNumber: versionNumber, isInDraftMode: params.boolean('isInDraftMode') ?: false, reportLang: params.reportLang]"
                    data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.e2b.r3.xml')}">
                <asset:image src="xml-icon.png" class="xml-icon" height="16" width="16"/>
            </g:link>
            <g:link class="btn btn-default waves-effect waves-light"
                    controller="report" action="downloadPdf" target="_black"
                    params="[exIcsrTemplateQueryId: exIcsrTemplateQueryId, outputFormat: ReportFormatEnum.PDF.name(), caseNumber: caseNumber, versionNumber: versionNumber, isInDraftMode: params.boolean('isInDraftMode') ?: false, reportLang: params.reportLang]"
                    data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.pdf')}">
                <asset:image src="pdf-icon.png" class="pdf-icon" height="16" width="16"/>
            </g:link>
        </g:if>
        <g:if test = "${generatePmdaPaperReport}">
            <g:link class="btn btn-default waves-effect waves-light"
                                controller="report" action="downloadPdf" target="_black"
                                params="[exIcsrTemplateQueryId: exIcsrTemplateQueryId, outputFormat: ReportFormatEnum.PDF.name(), caseNumber: caseNumber, versionNumber: versionNumber, isInDraftMode: params.boolean('isInDraftMode') ?: false, reportLang: params.reportLang, generatePmdaPaperReport: true]"
                                data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.paper.report')}">
                <asset:image src="cioms-icon.png" class="xml-icon" height="16" width="16"/>
            </g:link>
        </g:if>
        <g:else>
            <g:link class="btn btn-default waves-effect waves-light"
                            controller="report" action="drillDown"
                            params="[caseNumber: caseNumber, versionNumber: versionNumber, isInDraftMode: params.boolean('isInDraftMode') ?: false]"
                            data-tooltip="tooltip" data-placement="bottom"
                            title="${isPvcm ? "Case Form" : message(code: 'save.as.cioms')}">
                <asset:image src="cioms-icon.png" class="xml-icon" height="16" width="16"/>
            </g:link>
        </g:else>
        </div>
        </div>
        <g:set var="userService" bean="userService"/>
        <g:if test="${userService.getCurrentUser().preference.locale.language != 'en'}">
            <g:set var="selectedLocale" value="${localeList.find { it.key == params.reportLang }}"/>
            <div class="col-md-1">
                <div>
                    <select id="reportLang" class="form-control" data-evt-change='{"method": "reportInSelectedLang", "params": []}'>
                        <g:each in="${localeList}" var="locale">
                            <option value="${locale.key}"
                                    data-url="${createLink(controller: 'icsr', action: 'showReport', params:[exIcsrTemplateQueryId: exIcsrTemplateQueryId, caseNumber: caseNumber, versionNumber: versionNumber, isInDraftMode: params.boolean('isInDraftMode') ?: false, reportLang: locale.key])}"
                                ${locale.key == reportLang ? 'selected' : ''}>
                                ${locale.value}
                            </option>
                        </g:each>
                    </select>
                </div>
            </div>
        </g:if>
    </div>

    <div id="reportDiv"></div>
    <g:render template="/configuration/includes/addSection"/>
</rx:container>
<script type="text/javascript">
    $(function () {
        var filename = encodeURIComponent('${filename}');
        var id = '${exIcsrProfileId}';
        if (filename != '') {
            $.ajax({
                url: "/reports/report/getReportAsHTMLString?isInDraftMode=${params.boolean('isInDraftMode')}",
                data: "filename=" + filename + "&id=" + id,
                dataType: "html"
            })
                .done(function (result) {
                    $("#reportDiv").empty().append(result);
                    $(".jrPage tr").css("height", "");
                    $(".column-title").attr("title", function () {
                        return $("#" + this.id + "Legend").val()
                    });
                });
            setGridHeight();
        }

        $("[data-evt-change]").on('change', function() {
            const eventData = JSON.parse($(this).attr("data-evt-change"));
            const methodName = eventData.method;
            const params = eventData.params;
            // Call the method from the eventHandlers object with the params
            if (methodName == 'reportInSelectedLang') {
                return reportInSelectedLang();
            }
        });
    });
    $(window).on('resize', function () {
        setGridHeight();
    });

    function setGridHeight() {
        var screenHeight = Math.round($(window).height());
        var reportContaienr = $("#reportDiv");
        var gridHeight = screenHeight - (reportContaienr.offset().top + 20); // 20 is margin-top value
        reportContaienr.css({"max-height": gridHeight + "px"});
        $("html").addClass("overflow-y-hidden");
        $("html body").addClass("overflow-y-hidden");
    }

    function reportInSelectedLang() {
        var selectElement = document.getElementById('reportLang');
        var selectedOption = selectElement.options[selectElement.selectedIndex];
        var url = selectedOption.getAttribute('data-url');
        if (url) {
            window.location.href = url;
        }
    }
</script>
</body>
</html>
