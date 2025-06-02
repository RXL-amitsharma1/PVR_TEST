<%@ page import="com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.Constants" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.wopi.view.pageTitle" default="PV Publisher Office Online"/></title>
    <asset:stylesheet src="wopi.css"/>
    <asset:javascript src="/app/wopi.js"/>

</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" var="theInstance"/>

<g:applyCodec encodeAs="none">
<iframe allowfullscreen="true" title='Office Frame' id="office_frame" name="office_frame" width="100%" style="border: 0" src="${actionUrl}&access_token=${access_token}"></iframe>
</g:applyCodec>

</body>