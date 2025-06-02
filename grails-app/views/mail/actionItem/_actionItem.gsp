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

                    <p><g:message code="app.label.hi"/></p>

                    <p>
                        <span>
                            <g:if test="${mode == 'create'}">
                                <g:message code="app.notification.actionItem.assigned"/>
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:message code="app.notification.actionItem.email.updated"/>
                            </g:elseif>
                            <g:elseif test="${mode == 'delete'}">
                                <g:message code="app.notification.actionItem.deleted"/>
                            </g:elseif>

                        </span>
                    </p>
                    <br/>

                    <div class="row" style="width:100%">
                        <div style="width:50%">
                            <span><b><g:message code="app.label.action.item.action.category"/> :</b></span>
                            <span>
                                <g:if test="${mode == 'create'}">
                                    ${ViewHelper.getMessage(actionItem?.actionCategory?.getI18nKey())}
                                </g:if>
                                <g:elseif test="${mode == 'update'}">
                                    <g:if test="${actionItem?.actionCategory?.id == oldActionItemRef?.actionCategory?.id}">
                                        ${ViewHelper.getMessage(actionItem?.actionCategory?.getI18nKey())}
                                    </g:if>
                                    <g:else>
                                        <s>${ViewHelper.getMessage(oldActionItemRef?.actionCategory?.getI18nKey())}</s> ${ViewHelper.getMessage(actionItem?.actionCategory?.getI18nKey())}
                                    </g:else>
                                </g:elseif>
                            </span>
                        </div>

                        <div style="width:50%">
                            <span><b><g:message code="app.label.action.item.priority"/> :</b></span>
                            <span>
                                <g:if test="${mode == 'create'}">
                                    ${ViewHelper.getMessage("app.priority." + actionItem?.priority)}
                                </g:if>
                                <g:elseif test="${mode == 'update'}">
                                    <g:if test="${actionItem?.priority == oldActionItemRef?.priority}">
                                        ${ViewHelper.getMessage("app.priority." + actionItem?.priority)}
                                    </g:if>
                                    <g:else>
                                        <s>${ViewHelper.getMessage("app.priority." + oldActionItemRef?.priority)}</s> ${ViewHelper.getMessage("app.priority." + actionItem?.priority)}
                                    </g:else>
                                </g:elseif>
                            </span>
                        </div>
                    </div>

                    <div class="row" style="width:100%">
                        <g:set var="actionItemDueDate"
                               value="${actionItem?.dueDate instanceof Date ? actionItem?.dueDate : DateUtil.StringToDate(actionItem?.dueDate, DateUtil.DATEPICKER_UTC_FORMAT)}"/>
                        <g:set var="actionItemCompletionDate"
                               value="${actionItem?.completionDate instanceof Date ? actionItem?.completionDate : DateUtil.StringToDate(actionItem?.completionDate, DateUtil.DATEPICKER_UTC_FORMAT)}"/>
                        <g:set var="oldActionItemRefDueDate"
                               value="${oldActionItemRef?.dueDate instanceof Date ? oldActionItemRef?.dueDate : DateUtil.StringToDate(oldActionItemRef?.dueDate, DateUtil.DATEPICKER_UTC_FORMAT)}"/>
                        <g:set var="oldActionItemRefCompletionDate"
                               value="${oldActionItemRef?.completionDate instanceof Date ? oldActionItemRef?.completionDate : DateUtil.StringToDate(oldActionItemRef?.completionDate, DateUtil.DATEPICKER_UTC_FORMAT)}"/>

                        <div style="width:50%">
                            <span><b><g:message code="app.label.action.item.due.date"/> :</b></span>
                            <span>
                                <g:if test="${mode == 'create'}">
                                    <g:renderShortFormattedDate date="${actionItemDueDate}"/>
                                </g:if>
                                <g:elseif test="${mode == 'update'}">
                                    <g:if test="${actionItem?.dueDate == oldActionItemRef?.dueDate}">
                                        <g:renderShortFormattedDate date="${actionItemDueDate}"/>
                                    </g:if>
                                    <g:else>
                                        <s><g:renderShortFormattedDate date="${oldActionItemRefDueDate}"/></s>
                                        <g:renderShortFormattedDate date="${actionItemDueDate}"/>
                                    </g:else>
                                </g:elseif>
                            </span>
                        </div>

                        <div style="width:50%">
                            <span><b><g:message code="app.label.action.item.completion.date"/> :</b></span>
                            <span>
                                <g:if test="${mode == 'create'}">
                                    <g:renderShortFormattedDate date="${actionItemCompletionDate}"/>
                                </g:if>
                                <g:elseif test="${mode == 'update'}">
                                    <g:if test="${actionItem?.completionDate == oldActionItemRef?.completionDate}">
                                        <g:renderShortFormattedDate date="${actionItemCompletionDate}"/>
                                    </g:if>
                                    <g:else>
                                        <s><g:renderShortFormattedDate date="${oldActionItemRefCompletionDate}"/></s>
                                        <g:renderShortFormattedDate date="${actionItemCompletionDate}"/>
                                    </g:else>
                                </g:elseif>
                            </span>
                        </div>
                    </div>

                    <div class="row" style="width:100%">
                        <div style="width:100%">
                            <span><b><g:message code="app.label.action.item.description"/> :</b></span>
                            <span>
                                <g:if test="${mode == 'create'}">
                                    ${actionItem?.description}
                                </g:if>
                                <g:elseif test="${mode == 'update'}">
                                    <g:if test="${actionItem?.description == oldActionItemRef?.description}">
                                        ${actionItem?.description}
                                    </g:if>
                                    <g:else>
                                        <s>${oldActionItemRef?.description}</s> ${actionItem?.description}
                                    </g:else>
                                </g:elseif>
                            </span>
                        </div>
                    </div>

                    <div class="row" style="width:100%">
                        <div style="width:100%">
                            <span><b><g:message code="app.label.action.item.status"/> :</b></span>
                            <span>
                                <g:if test="${mode == 'create'}">
                                    ${ViewHelper.getMessage(actionItem?.status?.getI18nKey())}
                                </g:if>
                                <g:elseif test="${mode == 'update'}">
                                    <g:if test="${actionItem?.status == oldActionItemRef?.status}">
                                        ${ViewHelper.getMessage(actionItem?.status?.getI18nKey())}
                                    </g:if>
                                    <g:else>
                                        <s>${ViewHelper.getMessage(oldActionItemRef?.status?.getI18nKey())}</s> ${ViewHelper.getMessage(actionItem?.status?.getI18nKey())}
                                    </g:else>
                                </g:elseif>
                            </span>
                        </div>
                    </div>

                    <rx:showReportRequest actionItemId="${actionItem.id}"/>
                <g:if test="${actionItem instanceof com.rxlogix.config.ActionItem}">
                <g:set var="executedReportConfiguration" value="${com.rxlogix.config.ExecutedReportConfiguration.getByActionItem(actionItem.id).get()}"/>
                <g:if test="${executedReportConfiguration}">
                    <div class="row" style="width:100%">
                        <div style="width:100%">
                            <span><b><g:message code="app.actionItem.associatedReport.label"/> :</b></span>
                            <span>
                                <a href="${grailsApplication.config.grails.appBaseURL}/report/showFirstSection/${executedReportConfiguration.id}" >${executedReportConfiguration.reportName}</a>
                            </span>
                        </div>
                    </div>
                </g:if>
                <g:if test="${actionItem.configuration}">
                    <div class="row" style="width:100%">
                        <div style="width:100%">
                            <span><b><g:message code="app.actionItem.associatedConfiguration.label"/> :</b></span>
                            <span>
                                <g:if test="${actionItem.actionCategory.key == 'PERIODIC_REPORT'}">
                                    <a href="${grailsApplication.config.grails.appBaseURL}/periodicReport/view/${actionItem.configuration.id}" >${actionItem.configuration.reportName}</a>
                                </g:if>
                                <g:else>
                                    <a href="${grailsApplication.config.grails.appBaseURL}/configuration/view/${actionItem.configuration.id}" >${actionItem.configuration.reportName}</a>
                                </g:else>
                            </span>
                        </div>
                    </div>
                </g:if>
                <g:if test="${actionItem.appType == AppTypeEnum.DRILLDOWN_RECORD}">
                    <g:set var="reasonOfDelayData" value="${actionItem.getReasonOfDelayData()}"/>
                    <g:if test="${reasonOfDelayData?.drilldownRecordCaseNum}">
                        <div class="row" style="width:100%">
                            <div style="width:100%">
                                <span><b><g:message code="app.actionItem.associatedDrilldown.label"/> :</b></span>
                                <span>
                                    <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${reasonOfDelayData.drilldownReportId}?cllRecordId=${reasonOfDelayData.drilldownRecordId}" >${reasonOfDelayData.drilldownReportName} - ${reasonOfDelayData.drilldownRecordCaseNum}</a>
                                </span>
                            </div>
                        </div>
                    </g:if>
                </g:if>
                <g:if test="${actionItem.appType == AppTypeEnum.IN_DRILLDOWN_RECORD}">
                    <g:set var="reasonOfDelayData" value="${actionItem.getReasonOfDelayData(true)}"/>
                    <g:if test="${reasonOfDelayData?.drilldownRecordCaseNum}">
                        <div class="row" style="width:100%">
                            <div style="width:100%">
                                <span><b><g:message code="app.actionItem.associatedDrilldown.label"/> :</b></span>
                                <span>
                                    <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${reasonOfDelayData.drilldownReportId}?cllRecordId=${reasonOfDelayData.drilldownRecordId}" >${reasonOfDelayData.drilldownReportName} - ${reasonOfDelayData.drilldownRecordCaseNum}</a>
                                </span>
                            </div>
                        </div>
                    </g:if>
                </g:if>
                <g:if test="${actionItem.appType == AppTypeEnum.QUALITY_MODULE}">
                    <g:set var="qualityService" bean="qualityService"/>
                    <g:set var="caseNoDetailMap" value="${qualityService.getCaseNoByActionItemId(actionItem.id)}"/>
                    <g:if test="${caseNoDetailMap?.id}">
                        <div class="row" style="width:100%">
                            <div style="width:100%">
                                <span><b><g:message code="app.label.action.item.associated.caseNumber"/> :</b></span>
                                <span>
                                    <a href='${grailsApplication.config.grails.appBaseURL}/quality/caseForm/?type=${caseNoDetailMap.dataType}&caseNumber=${caseNoDetailMap.masterCaseNum}&id=${caseNoDetailMap.id}&versionNumber=${caseNoDetailMap.masterVersionNum}'>${caseNoDetailMap.masterCaseNum}</a>
                                   </span>
                            </div>
                        </div>
                    </g:if>
                </g:if>
                <g:if test="${actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA}">
                    <g:set var="qualityService" bean="qualityService"/>
                    <g:set var="capa" value="${qualityService.getCapa(actionItem.id)}"/>
                    <g:if test="${capa}">
                        <div class="row" style="width:100%">
                            <div style="width:100%">
                                <span><b><g:message code="app.label.action.item.associated.caseNumber"/> :</b></span>
                                <span>
                                    <a href='${grailsApplication.config.grails.appBaseURL}/issue/view/${capa.associatedIssueId}'>${capa.associatedIssueNumber}</a>
                                </span>
                            </div>
                        </div>
                    </g:if>
                </g:if>
                </g:if>
                <div class="row" style="width:100%">
                    <div style="width:100%">
                        <span><b><g:message code="app.actionItem.associatedActionItem.label"/> :</b></span>
                        <span>
                            <a href="${url}"><g:message code="app.label.actionItem.link"/></a>
                        </span>
                    </div>
                </div>
                <br><br><br>
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