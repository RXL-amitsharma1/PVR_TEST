<html>
    <head>
        <meta name="layout" content="main">
        <g:set var="entityName" value="${message(code: "app.label.PublisherTemplate.appName")}"/>
        <title><g:message code="app.PublisherTemplate.create.title"/></title>
        <asset:javascript src="app/publisher/publisherTemplate.js"/>
        <g:javascript>
                var savePublisherTemplateUrl="${createLink(controller: 'publisherTemplate', action: 'save')}";
                var listPublisherTemplatesUrl="${createLink(controller: 'publisherTemplate', action: 'index')}";
                var pubWordTemplateSizeLimit = ${grailsApplication.config.grails.controllers.upload.maxFileSize};
        </g:javascript>
    </head>
<body>

<div class="content">
    <div class="container">
        <div class="row page-space m-t-5">
            <rx:container title="${message(code: "app.label.create.PublisherTemplate")}">
                <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
                <div class="container-fluid">
                    <form id="createPublisherForm" action="${createLink(action: 'save')}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}" method="post" enctype="multipart/form-data">
                        <g:render template="includes/form" model="[instance: instance]"/>
                        <div class="row">
                            <div class="col-xs-12">
                                <div class="pull-right">
                                    <button class="btn btn-primary" type="button" id="saveButton">${message(code: 'default.button.save.label')}</button>
                                    <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["publisherTemplate", "index"]}'
                                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
</html>