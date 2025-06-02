<%@ page import="grails.gorm.multitenancy.Tenants; com.rxlogix.enums.AppTypeEnum; com.rxlogix.util.DateUtil;com.rxlogix.util.ViewHelper" %>
<g:withOutTenant>
    <!doctype html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style>

        /* -------------------------------------
            GLOBAL
        ------------------------------------- */


        * {
            font-family: Calibri, sans-serif;
            font-size: 100%;
            line-height: 1.6em;
            margin: 0;
            padding: 0;
        }

        img {
            max-width: 600px;
            width: auto;
        }

        body {
            -webkit-font-smoothing: antialiased;
            height: 100%;
            -webkit-text-size-adjust: none;
            width: 100% !important;
            background-image: url('cid:pvreportsMailBackground')
        }

        /* -------------------------------------
            ELEMENTS
        ------------------------------------- */

        .last {
            margin-bottom: 0;
        }

        .first {
            margin-top: 0;
        }

        .padding {
            padding: 10px 0;
        }

        span {
            font-family: Calibri, sans-serif;
            font-size: 14px;
        }

        /* -------------------------------------
            BODY
        ------------------------------------- */
        table.body-wrap {
            padding: 20px;
            width: 100%;
        }

        table.body-wrap .container {
            border: 1px solid #f0f0f0;
        }

        /* -------------------------------------
            TYPOGRAPHY
        ------------------------------------- */
        h1,
        h2,
        h3 {
            color: #111111;
            font-family: Calibri, sans-serif;
            font-weight: 200;
            line-height: 1.2em;
            margin: 40px 0 10px;
        }

        h1 {
            font-size: 36px;
        }

        h2 {
            font-size: 28px;
        }

        h3 {
            font-size: 22px;
        }

        p,
        ul,
        ol {
            font-size: 14px;
            font-weight: normal;
            margin-bottom: 10px;
        }

        ul li,
        ol li {
            margin-left: 5px;
            list-style-position: inside;
        }

        /* ---------------------------------------------------
            RESPONSIVENESS
        ------------------------------------------------------ */
        /* Set a max-width, and make it display as block so it will automatically stretch to that width, but will also shrink down on a phone or something */
        .container {
            clear: both !important;
            display: block !important;
            Margin: 0 auto !important;
            max-width: 600px !important;
        }

        /* Set the padding on the td rather than the div for Outlook compatibility */
        .body-wrap .container {
            padding: 20px;
        }

        /* This should also be a block element, so that it will fill 100% of the .container */
        .content {
            display: block;
            margin: 0 auto;
            max-width: 600px;
        }

        /* Let's make sure tables in the content area are 100% wide */
        .content table {
            width: 100%;
        }

        </style>
    </head>

    <body>

    <div class="first"><img src="cid:pvreportslogo"/></div>

    <!-- body -->
    <table class="body-wrap" bgcolor="#f6f6f6">
        <tr>
            <td></td>
            <td class="container" bgcolor="#FFFFFF">

                <!-- content -->
                <div class="content">

                    <p><span style="color: red"><g:message code="pvc.report.email.body.text"/></span></p>

                    <p><g:message code="app.label.hi"/></p>

                    <g:if test="${userCaseInfo}">
                        <p><span><g:message code="app.notification.reasonOfDelay.user.subject"/></span></p>
                        <div class="row" style="width:100%">

                            <span><b><g:message code="app.label.action.item.associated.caseNumber"/> :</b></span>
                            <span>
                                <g:each in="${userCaseInfo}" var="caseInfo" status="i">
                                    <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${caseInfo.reportResultId}?cllRecordId=${caseInfo.drilldownRecordId}">
                                        ${caseInfo.caseNum}
                                    </a>
                                    <g:if test="${i < userCaseInfo.size() - 1}">,</g:if>
                                </g:each>
                            </span>
                        </div>

                        <div style="margin-top: 10px;">
                            <span><b><g:message code="app.email.associated.cases.link"/> :</b></span>
                            <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${reportResultId}?linkFilter=${linkFilter}&hasLargeIds=${hasLargeIds}">
                                <g:message code="app.pvc.email.pvc.case.link"/>
                            </a>
                        </div>


                    </g:if>


                    <g:if test="${groupedCases}">
                        <p><span><g:message code="app.notification.reasonOfDelay.group.subject"/></span></p>


                        <!-- Group cases by user group name -->
                        <g:set var="groupedByUserGroup" value="${groupedCases.groupBy { it.userGroupName }}"/>

                        <g:each in="${groupedByUserGroup}" var="groupEntry">
                            <span><b><g:message code="app.notification.associatedGroup"/>:</b> ${groupEntry.key}</span>
                            <br>
                            <span><b><g:message code="app.label.action.item.associated.caseNumber"/> :</b></span>
                            <span>
                            <g:each in="${groupEntry.value}" var="caseInfo" status="i">
                                <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${caseInfo.reportResultId}?cllRecordId=${caseInfo.drilldownRecordId}">
                                    ${caseInfo.caseNum}</a>
                                </a><g:if test="${i < groupEntry.value.size() - 1}">,</g:if>
                            </g:each>
                            </span>

                        </g:each>

                        <div style="margin-top: 10px;">
                            <span><b><g:message code="app.email.associated.cases.link"/> :</b></span>
                            <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${reportResultId}?linkFilter=${linkFilter}&hasLargeIds=${hasLargeIds}">
                                <g:message code="app.pvc.email.pvc.case.link"/>
                            </a>
                        </div>

                    </g:if>
                    <br>

                    <p><strong><em><g:message code="app.email.body.note"/>:</em></strong></p>
                    <ul>
                        <li><em><g:message code="app.email.pvc.pvq.note"/></em></li>
                        <li><em><g:message code="app.email.pvc.pvq.note.label" args="[timeframe]"/></em></li>
                    </ul>
                    <br>

                    <p><span><g:message code="app.label.thanks"/>,</span></p>

                    <p><span><g:message code="app.label.pv.reports"/></span></p>

                </div>

                <!-- /content -->
            </td>
            <td></td>
        </tr>
    </table>
    <!-- /body -->

    <!-- Footer -->
    <p class="left">PV Reports &copy; ${(new Date())[Calendar.YEAR]} RxLogix Corporation. All rights reserved.</p>

    </body>
    </html>
</g:withOutTenant>