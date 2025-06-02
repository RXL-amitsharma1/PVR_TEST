<%@ page import="com.rxlogix.config.Capa8D; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper" %>
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
                </g:if>
                <g:else>
                    <p><g:message code="app.label.hello.all"/></p>

                    <p>
                        <span>
                            <g:message code="app.label.emailConfiguration.emailBody.delivery"/>
                        </span>
                    </p>

                    <br/>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.type"/> :</b></span>
                        <span>
                            <g:if test="${capaInstance instanceof com.rxlogix.config.Capa8D}">
                                <g:message code="app.label.quality.issue"/>
                            </g:if>
                        </span>
                    </div>
                    <div style="width:50%">
                        <span><b><g:message code="app.label.name"/> :</b></span>
                        <g:if test="${capaInstance instanceof com.rxlogix.config.Capa8D}">
                            <span><g:message code="app.label.quality.issue"/> ${capaInstance?.issueNumber}</span>
                        </g:if>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="quality.capa.category.label"/> :</b></span>
                        <span>
                            ${capaInstance?.category}
                        </span>
                    </div>

                    <div style="width:50%">
                        <span><b><g:message code="app.label.description"/> :</b></span>
                        <span>
                            ${capaInstance?.description}
                        </span>
                    </div>

                    <div style="width: 50%">
                        <span><b><g:message code="app.label.issue.link" />:</b></span>
                        <span><a href="${url}"><g:message code="app.label.refer.link"/></a></span>
                    </div>

                    <br/>

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
    <em style="color: #0c7cd5; margin-left: 8px; font-size: 12px;">Generated by RxLogix PV Quality.</em>
</g:if>
</body>
</html>