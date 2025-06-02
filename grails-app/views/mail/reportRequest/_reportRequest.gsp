<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DictionaryTypeEnum" %>
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
                                <g:message code="app.notification.reportRequest.created"/>
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:message code="app.notification.reportRequest.updated"/>
                            </g:elseif>
                            <g:elseif test="${mode == 'delete'}">
                                <g:message code="app.notification.reportRequest.deleted"/>
                                <g:set var="mode" value="create"/>
                            </g:elseif>

                        </span>
                    </p>
                    <br/>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.report.request.id"/> :</b></span>
                        <span>
                            ${reportRequest?.id}
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.report.request.name"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.reportName}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.reportName == oldReportRequestRef?.requestName}">
                                    ${reportRequest?.reportName}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.requestName}</s> ${reportRequest?.reportName}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.reportRequestType"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.reportRequestType?.name}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.reportRequestType?.name == oldReportRequestRef?.reportRequestType}">
                                    ${reportRequest?.reportRequestType?.name}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.reportRequestType}</s> ${reportRequest?.reportRequestType?.name}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.action.item.priority"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.priority?.name}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.priority == oldReportRequestRef?.priority}">
                                    ${reportRequest?.priority?.name}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.priority?.name}</s> ${reportRequest?.priority?.name}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.action.item.status"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.workflowState?.name}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.workflowState?.name == oldReportRequestRef?.status}">
                                    ${reportRequest?.workflowState?.name}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.status}</s> ${reportRequest?.workflowState?.name}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>


                    <div style="width:50%">
                        <span><b><g:message code="app.label.description"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.description}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.description == oldReportRequestRef?.description}">
                                    ${reportRequest?.description}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.description}</s> ${reportRequest?.description}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.action.item.due.date"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                <g:renderShortFormattedDate date="${reportRequest?.dueDate}"/>
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${DateUtil.dateRangeString(reportRequest?.dueDate, userTimeZone) == DateUtil.dateRangeString(oldReportRequestRef?.dueDate, userTimeZone)}">
                                    <g:renderShortFormattedDate date="${reportRequest?.dueDate}"/>
                                </g:if>
                                <g:else>
                                    <s><g:renderShortFormattedDate date="${oldReportRequestRef?.dueDate}"/></s>
                                    <g:renderShortFormattedDate date="${reportRequest?.dueDate}"/>
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.report.request.generated.for"/> :</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.requestorList}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.requestorList == oldReportRequestRef?.requesters}">
                                    ${reportRequest?.requestorList}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.requesters}</s> ${reportRequest?.requestorList}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.action.item.assigned.to"/>:</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${reportRequest?.assignedToName()}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.assignedToName() == oldReportRequestRef?.assignedTo}">
                                    ${reportRequest?.assignedToName()}
                                </g:if>
                                <g:else>
                                    <s>${oldReportRequestRef?.assignedTo}</s> ${reportRequest?.assignedToName()}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.productSelection"/>:</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${ViewHelper.getDictionaryValues(reportRequest?.productSelection ?: "", DictionaryTypeEnum.PRODUCT)}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.productSelection == oldReportRequestRef?.productSelection}">
                                    ${ViewHelper.getDictionaryValues(reportRequest?.productSelection ?: "", DictionaryTypeEnum.PRODUCT)}
                                </g:if>
                                <g:else>
                                    <s>${ViewHelper.getDictionaryValues(oldReportRequestRef?.productSelection ?: "", DictionaryTypeEnum.PRODUCT)}</s> ${ViewHelper.getDictionaryValues(reportRequest?.productSelection ?: "", DictionaryTypeEnum.PRODUCT)}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.studySelection"/>:</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${ViewHelper.getDictionaryValues(reportRequest?.studySelection ?: "", DictionaryTypeEnum.STUDY)}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.studySelection == oldReportRequestRef?.studySelection}">
                                    ${ViewHelper.getDictionaryValues(reportRequest?.studySelection ?: "", DictionaryTypeEnum.STUDY)}
                                </g:if>
                                <g:else>
                                    <s>${ViewHelper.getDictionaryValues(oldReportRequestRef?.studySelection ?: "", DictionaryTypeEnum.STUDY)}</s> ${ViewHelper.getDictionaryValues(reportRequest?.studySelection ?: "", DictionaryTypeEnum.STUDY)}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.eventSelection"/>:</b></span>
                        <span>
                            <g:if test="${mode == 'create'}">
                                ${ViewHelper.getDictionaryValues(reportRequest?.eventSelection ?: "", DictionaryTypeEnum.EVENT)}
                            </g:if>
                            <g:elseif test="${mode == 'update'}">
                                <g:if test="${reportRequest?.eventSelection == oldReportRequestRef?.eventSelection}">
                                    ${ViewHelper.getDictionaryValues(reportRequest?.eventSelection ?: "", DictionaryTypeEnum.EVENT)}
                                </g:if>
                                <g:else>
                                    <s>${ViewHelper.getDictionaryValues(oldReportRequestRef?.eventSelection ?: "", DictionaryTypeEnum.EVENT)}</s> ${ViewHelper.getDictionaryValues(reportRequest?.eventSelection ?: "", DictionaryTypeEnum.EVENT)}
                                </g:else>
                            </g:elseif>
                        </span>
                    </div>

                <div style="width:50%">
                    <span><b><g:message code="app.label.attachments"/>:</b></span>
                    <span>
                        <g:if test="${mode == 'create'}">
                            ${reportRequest?.attachmentsString}
                        </g:if>
                        <g:elseif test="${mode == 'update'}">
                            <g:if test="${reportRequest?.attachmentsString == oldReportRequestRef?.attachmentsString}">
                                ${reportRequest?.attachmentsString}
                            </g:if>
                            <g:else>
                                <s>${oldReportRequestRef?.attachmentsString}</s> ${reportRequest?.attachmentsString}
                            </g:else>
                        </g:elseif>
                    </span>
                </div>
                <div style="width:50%">
                <span><b><g:message code="app.label.reportRequest.linked"/>:</b></span>
                <span>
                    <g:if test="${mode == 'create'}">
                        ${reportRequest?.linksString}
                    </g:if>

                    <g:elseif test="${mode == 'update'}">
                        <g:if test="${reportRequest?.linksString == oldReportRequestRef?.linksString}">
                            ${oldReportRequestRef?.linksString}
                        </g:if>
                        <g:else>
                            <s>${oldReportRequestRef?.linksString}</s> ${reportRequest?.linksString}
                        </g:else>
                    </g:elseif>
                </span>
            </div>
                <br/>
                <br/>
                <br/>
                <!-- Link -->
                <div>
                <g:if test="${url}">
                    <a href="${url}"><g:message code="app.label.reportRequest.link"/></a>
                </g:if>
                </div>

                    <p><g:message code="app.label.thanks"/>,</p>

                    <p><g:message code="app.label.pv.reports"/></p>

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