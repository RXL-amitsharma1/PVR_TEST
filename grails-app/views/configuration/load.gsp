<%@ page import="grails.util.Holders" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.loadConfigurations.title"/></title>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
<rx:container title="${message(code: "app.label.loadConfigurations")}">

%{--todo:  This should be cleaned up. This "error" object is really the query being created. - morett  --}%
    <g:render template="/includes/layout/flashErrorsDivs" bean="${error}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="configuration" action="saveJSONConfigurations" method="post">

            <pre><g:message code="load.paste.your.json.configurations"/></pre>

            <div class="expandingArea">
                <pre><span></span><br></pre>
                <g:textArea name="JSONConfigurations" value="" maxlength="${Holders.config.rxlogix.pvreports.JSON.load.maxLength}"/>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="saveJSONConfigurations"
                                        value="${message(code: 'default.button.save.label')}"></g:actionSubmit>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["configuration", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>
        </div>
    </div>
</div>
</body>
