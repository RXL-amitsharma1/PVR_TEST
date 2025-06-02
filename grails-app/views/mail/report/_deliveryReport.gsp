<%@ page import="com.rxlogix.config.ExecutedReportConfiguration; com.rxlogix.config.ExecutedPeriodicReportConfiguration; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.config.ExecutedCaseSeries; com.rxlogix.config.Capa8D; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper" %>
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
        }

        /* Set the padding on the td rather than the div for Outlook compatibility */
        .body-wrap .container {
            padding: 5px;
        }

        /* This should also be a block element, so that it will fill 100% of the .container */
        .content {
            display: block;
            margin: 0 auto;
            padding: 1px;
        }

        /* Let's make sure tables in the content area are 100% wide */
        .content table {
            width: 100%;
        }

        </style>
    </head>

    <body>

    <!-- body -->
    <table class="body-wrap" bgcolor="#FFFFFF">
        <tr>
            <td></td>
            <td class="container" bgcolor="#FFFFFF">

                <!-- content -->
                <div class="content">
                    <g:if test="${emailBodyCustom}">
                        <g:applyCodec encodeAs="none">
                            ${emailBodyCustom}
                        </g:applyCodec>
                        <g:if test="${emailBodyError}">
                            <div>
                                <g:applyCodec encodeAs="none">
                                    ${emailBodyError}
                                </g:applyCodec>
                            </div>
                        </g:if>
                    </g:if>
                    <g:else>
                        <p><g:message code="app.label.hello.all"/></p>

                        <p>
                            <span>
                                <g:message code="app.label.emailConfiguration.email.delivery"/>
                            </span>
                        </p>
                        <br/>

                        <div style="width:50%">
                            <span><b><g:message code="app.label.type"/> :</b></span>
                            <span>
                                <g:if test="${executedConfiguration instanceof ExecutedConfiguration}">
                                    <g:message code="app.configurationType.ADHOC_REPORT"/>
                                </g:if>
                                <g:elseif
                                        test="${executedConfiguration instanceof ExecutedPeriodicReportConfiguration}">
                                    <g:message code="app.configurationType.PERIODIC_REPORT"/>
                                </g:elseif>
                                <g:elseif test="${executedConfiguration instanceof Capa8D}">
                                    <g:message code="app.configurationType.ISSUE_NUMBER"/>
                                </g:elseif>
                                <g:else>
                                    <g:message code="app.caseSeries.label"/>
                                </g:else>
                            </span>
                        </div>

                        <div style="width:50%">
                            <span><b><g:message code="app.label.name"/> :</b></span>
                            <g:if test="${executedConfiguration instanceof ExecutedCaseSeries}">
                                <span>${executedConfiguration?.seriesName}</span>
                            </g:if>
                            <g:elseif test="${executedConfiguration instanceof Capa8D}">
                                <span>${executedConfiguration?.issueNumber}</span>
                            </g:elseif>
                            <g:else>
                                <span>${executedConfiguration?.reportName}</span>
                            </g:else>
                        </div>

                        <g:if test="${executedConfiguration instanceof ExecutedReportConfiguration}">
                            <div style="width:50%">
                                <span><b><g:message code="app.label.executionStatus"/> :</b></span>
                                <span>
                                    <g:message
                                            code="${executedConfiguration?.status?.getI18nValueForAggregateReportStatus()}"/>
                                </span>
                            </div>

                            <div style="width:50%">
                                <span><b><g:message code="app.label.workflow.status"/> :</b></span>
                                <span>
                                    ${executedConfiguration?.workflowState?.name}
                                </span>
                            </div>
                        </g:if>
                        <div style="width:50%">
                            <span><b><g:message code="app.label.report.numOfExecutions"/> :</b></span>
                            <g:if test="${executedConfiguration instanceof ExecutedCaseSeries}">
                                <span>${executedConfiguration?.numExecutions}</span>
                            </g:if>
                            <g:elseif test="${executedConfiguration instanceof Capa8D}">
                                <span></span>
                            </g:elseif>
                            <g:else>
                                <span>${executedConfiguration?.numOfExecutions}</span>
                            </g:else>
                        </div>

                        <div style="width:50%">
                            <span><b><g:message code="app.label.description"/> :</b></span>
                            <span>
                                ${executedConfiguration?.description}
                            </span>
                        </div>
                        <g:if test="${!(executedConfiguration instanceof Capa8D)}">
                            <div style="width:50%">
                                <span><b><g:message code="app.label.productSelection"/>:</b></span>
                                <span>
                                    ${ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.PRODUCT)}
                                </span>
                            </div>

                            <div style="width:50%">
                                <span><b><g:message code="app.label.studySelection"/>:</b></span>
                                <span>
                                    ${ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.STUDY)}
                                </span>
                            </div>
                        </g:if>
                        <g:if test="${executedConfiguration instanceof ExecutedConfiguration || executedConfiguration instanceof ExecutedCaseSeries}">
                            <div style="width:50%">
                                <span><b><g:message code="app.label.eventSelection"/>:</b></span>
                                <span>
                                    ${ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.EVENT)}
                                </span>
                            </div>
                        </g:if>

                        <div style="width: 50%">
                            <span><b><g:message code="app.label.reportLink"/>:</b></span>
                            <span><a href="${url}"><g:message code="app.label.report.link"/></a></span>
                        </div>
                        <g:if test="${(executedConfiguration instanceof ExecutedPeriodicReportConfiguration) && executedConfiguration.isPublisherReport}">
                            <div style="width: 50%">
                                <span><b>Publisher Link:</b></span>
                                <span><a
                                        href='${grailsApplication.config.grails.appBaseURL}/pvp/sections?id=${executedConfiguration.id}'>Refer Publisher</a>
                                </span>
                            </div>
                        </g:if>

                        <br/>

                        <!-- Link -->
                        <div>
                            <g:applyCodec encodeAs="none">
                                ${emailBodyMessage}
                            </g:applyCodec>
                        </div>
                        <br/>
                        <g:if test="${emailBodyError}">
                            <div>
                                <g:applyCodec encodeAs="none">
                                    ${emailBodyError}
                                </g:applyCodec>
                            </div>
                        </g:if>
                        <p><g:message code="app.label.thanks"/>,</p>

                        <p><g:message code="app.label.pv.reports"/></p>
                    </g:else>

                </div>
                <!-- /content -->
            </td>
            <td></td>
        </tr>

    </table>
    <!-- /body -->

    <!-- Footer -->
    <g:if test="${grailsApplication.config.email.footer.text}">
        <em style="color: #0c7cd5; margin-left: 8px; font-size: 12px;">Generated by RxLogix PV Reports.</em>
    </g:if>
    </body>
    </html>
</g:withOutTenant>