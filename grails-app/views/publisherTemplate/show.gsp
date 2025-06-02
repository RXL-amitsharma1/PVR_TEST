<%@ page import="com.rxlogix.config.publisher.PublisherTemplateParameter" contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <script>
        var listPublisherTemplateParameterUrl="${createLink(controller: 'publisherTemplate', action: 'publisherTemplateParameterList')}?id=${instance.id}";
    </script>
    <asset:javascript src="app/publisher/publisherTemplateParameterList.js"/>
    <g:set var="entityName" value="${message(code: "app.label.PublisherTemplate.appName")}"/>
    <title><g:message code="app.PublisherTemplate.show.title"/></title>
</head>
<body>

    <rx:container title="${message(code: 'app.label.view.PublisherTemplate')}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-12">
                        <div class="row form-group">
                            <div class="col-lg-6">
                                <label><g:message code="app.label.name"/></label>

                                <div>${instance.name}</div>
                            </div>
                        </div>

                        <div class="row form-group">
                            <div class="col-lg-6">
                                <label><g:message code="app.label.description"/></label>

                                <div>${instance.description}</div>
                            </div>
                        </div>

                        <div class="row form-group">
                            <div class="col-lg-6">
                                <label><g:message code="app.label.qualityChecked"/></label>
                                <div>
                                    <g:formatBoolean boolean="${instance?.qualityChecked}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                                </div>
                            </div>
                        </div>

                        <div class="row form-group">
                            <div class="col-lg-6">
                                <label><g:message code="app.label.publisher.word.attachment" /></label>

                                <div><a href="${createLink(controller: 'publisherTemplate', action: 'downloadAttachment')}?id=${instance?.id}">${instance?.fileName}</a></div>
                            </div>
                        </div>
                       <div class="row form-group">

                           <div class="pv-caselist">
                               <table id="publisherTemplateParameterList" class="table table-striped pv-list-table dataTable no-footer">
                                   <thead>
                                   <tr>
                                       <th></th>
                                       <th><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.name"/></th>
                                       <th><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.hidden"/></th>
                                       <th><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.type"/></th>
                                       <th><g:message default="Date Created" code="app.label.PublisherTemplate.PublisherTemplateParameter.title"/></th>
                                       <th><g:message default="Created By" code="app.label.PublisherTemplate.PublisherTemplateParameter.description"/></th>
                                       <th><g:message default="Last Updated" code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/></th>
                                   </tr>
                                   </thead>
                               </table>
                           </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["publisherTemplate", "edit", {"id":  ${instance.id}}]}' id="${instance.id}">
                        <g:message code="default.button.edit.label"/>
                    </button>
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["publisherTemplate", "index"]}'>
                        <g:message code="default.button.cancel.label"/>
                    </button>
                    <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="Publisher Template"
                            data-instanceid="${instance.id}"
                            data-instancename="${instance.name}"
                            class="btn btn-default"><g:message code="default.button.delete.label"/></button>
                </div>
            </div>
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${instance}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>