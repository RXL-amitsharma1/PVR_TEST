<%@ page import="com.rxlogix.util.DateUtil;com.rxlogix.util.ViewHelper" %>
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

                            <g:message code="app.notification.actionItem.duesoon"/>

                        </span>
                    </p>
                    <br/>
                    <g:if test="${actionItemNotification.overdue}">
                        <p>
                            <span>
                                <g:message code="app.notification.actionItem.overdue"/>
                            </span>
                        </p>
                        <g:render template="/mail/actionItem/description"
                                  model="[actionItems: actionItemNotification.overdue, url: url, userTimeZone: userTimeZone]"/>
                    </g:if>
                    <br/>
                    <g:if test="${actionItemNotification.today}">
                        <p>
                            <span>
                                <g:message code="app.notification.actionItem.dueToday"/>
                            </span>
                        </p>
                        <g:render template="/mail/actionItem/description"
                                  model="[actionItems: actionItemNotification.today, url: url, userTimeZone: userTimeZone]"/>
                    </g:if>
                    <br/>
                    <g:if test="${actionItemNotification.oneDay}">
                        <p>
                            <span>
                                <g:message code="app.notification.actionItem.due1"/>
                            </span>
                        </p>
                        <g:render template="/mail/actionItem/description"
                                  model="[actionItems: actionItemNotification.oneDay, url: url, userTimeZone: userTimeZone]"/>
                    </g:if>
                    <br/>
                    <g:if test="${actionItemNotification.threeDay}">
                        <p>
                            <span>
                                <g:message code="app.notification.actionItem.due3"/>
                            </span>
                        </p>
                        <g:render template="/mail/actionItem/description"
                                  model="[actionItems: actionItemNotification.threeDay, url: url, userTimeZone: userTimeZone]"/>
                    </g:if>
                    <br/>
                    <g:if test="${actionItemNotification.fiveDay}">
                        <p>
                            <span>
                                <g:message code="app.notification.actionItem.due5"/>
                            </span>
                        </p>
                        <g:render template="/mail/actionItem/description"
                                  model="[actionItems: actionItemNotification.fiveDay, url: url, userTimeZone: userTimeZone]"/>
                    </g:if>
                    <br/>
                    <br/>


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