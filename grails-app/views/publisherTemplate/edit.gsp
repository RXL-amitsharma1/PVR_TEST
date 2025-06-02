<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.PublisherTemplate.appName")}"/>
    <title><g:message code="app.PublisherTemplate.edit.title"/></title>
    <asset:javascript src="app/publisher/publisherTemplate.js"/>
    <g:javascript>
            var updatePublisherTemplateUrl="${createLink(controller: 'publisherTemplate', action: 'update')}";
            var listPublisherTemplatesUrl="${createLink(controller: 'publisherTemplate', action: 'index')}";
            var pubWordTemplateSizeLimit = ${grailsApplication.config.grails.controllers.upload.maxFileSize};
    </g:javascript>
</head>

<body>

<rx:container title="${message(code: "app.label.edit.PublisherTemplate")}" bean="${instance}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>

    <div class="container-fluid">
        <form id="updatePublisherForm" action="${createLink(action: 'update')}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}" method="post" enctype="multipart/form-data">
            <g:hiddenField name="id" value="${instance.id}"/>

            <g:render template="includes/form" model="['instance': instance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${instance?.version}"/>
                        <g:hiddenField name="lockCode" id="lockCode" value=""/>
                        <button class="btn btn-primary" type="button" id="updateButton">${message(code: 'default.button.update.label')}</button>
                        <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["publisherTemplate", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </form>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${instance}" var="theInstance"/>
</rx:container>
<g:render template="/oneDrive/downloadModal"/>
</body>
</html>