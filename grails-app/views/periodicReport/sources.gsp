<%@ page import="com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.pv.publisher"/> - <g:message code="app.label.publisher.sources"/></title>
    <g:javascript>
                var testScriptUrl="${createLink(controller: 'publisherTemplate', action: 'testScript')}";
    </g:javascript>
    <asset:javascript src="app/publisher/PublisherTemplateConfiguration.js"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div class="row pv-caselist">
<rx:container title="${message(code: 'app.label.publisher.sources')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <g:render template="/periodicReport/includes/configurationAttchment" model="[attachments: attachments, executedConfigurationInstanceId: 0, publisherMode: true]"/>

</rx:container>
        </div>
    </div>
    <g:render template="/oneDrive/downloadModal" model="[select:true]"/>
</div>
</body>