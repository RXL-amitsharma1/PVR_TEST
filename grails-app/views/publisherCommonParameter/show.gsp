<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.PublisherCommonParameter.view.title"/></title>
</head>

<body>

<rx:container title="${message(code: 'app.label.PublisherCommonParameter.view')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${email}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-6">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.name"/></label>

                                <div>${instance.name}</div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.description"/></label>

                                <div>${instance.description}</div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/></label>
                                <div>${instance.value}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["publisherCommonParameter", "edit", {"id":  ${instance.id}}]}' id="${instance.id}">
                        <g:message code="default.button.edit.label"/>
                    </button>
                    <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="Common Parameter"
                            data-instanceid="${instance.id}"
                            data-instancename="${instance.name}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
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