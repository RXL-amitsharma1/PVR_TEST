<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'etlSchedule.label')}"/>
    <title><g:message code="app.etlStatus.title"/></title>
    <g:javascript>
        var etlStatusUrl = "${createLink(controller: 'etlSchedule', action: 'getEtlStatus')}";
        var affEtlStatusUrl = "${createLink(controller: 'etlSchedule', action: 'getAffiliateEtlStatus')}";
        var preMartEtlStatusUrl = "${createLink(controller: 'etlSchedule', action: 'getPreMartEtlStatus')}";
        var pauseEtlUrl = "${createLink(controller: 'etlSchedule', action: 'pauseEtl')}";
        var refreshInterval = 300000;
    </g:javascript>
    <asset:javascript src="app/etlStatus.js"/>

</head>
<body>
    <div class="content">
        <div class="container">
            <div>
                <rx:container title="${message(code: "app.etlStatus.label")}" customButtons="${g.render(template: "includes/customHeaderButtons",model: [etlScheduleInstance:etlScheduleInstance, etlStatus: etlStatus, affEtlStatus: affEtlStatus])}">
                    <g:render template="/includes/layout/flashErrorsDivs" bean="${etlScheduleInstance}" var="theInstance"/>
                    <div class="alert alert-success alert-dismissible pauseETLSuccess m-t-4" role="alert" hidden="hidden">
                        <button type="button" class="close" id="pauseETLSuccess">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <div id="message"></div>
                    </div>
                    <div class="alert alert-danger alert-dismissible pauseETLError m-t-4" role="alert" hidden="hidden">
                        <button type="button" class="close" id="pauseETLError">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <div id="errormessage"></div>
                    </div>
                    <g:render template="show" model="[etlScheduleInstance: etlScheduleInstance,
                                                      etlStatus: etlStatus,
                                                      lastRunDateTime: lastRunDateTime,
                                                      isPreMartStatusApplicable: isPreMartStatusApplicable,
                                                      preMartEtlStatus: preMartEtlStatus,
                                                      preMartLastRunDateTime: preMartLastRunDateTime,
                                                      isAffEtlStatusApplicable : isAffEtlStatusApplicable,
                                                      affEtlStatus : affEtlStatus,
                                                      affEtlLastRunDateTime : affEtlLastRunDateTime]"/>

                </rx:container>
            </div>
        </div>
    </div>
</body>
</html>


