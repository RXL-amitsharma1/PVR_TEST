<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.emailTemplate.appName")}"/>
    <title><g:message code="app.emailTemplateConfiguration.show.title"/></title>
</head>

<body>

<rx:container title="${message(code: 'app.label.viewemailTemplate')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${emailTemplate}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-12">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.name"/></label>

                                <div>${emailTemplate.name}</div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.emailConfiguration.subject"/></label>

                                <div>${emailTemplate.description}</div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.owner"/></label>

                                <div>${emailTemplate.owner.fullName}</div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.type"/></label>
                                <div><g:message code="${emailTemplate.type.getI18nKey()}"/></div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12" style="border: 1px solid #cccccc;">

                                ${applyCodec(encodeAs:'HTML',raw(emailTemplate.body))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <g:link controller="emailTemplate" action="edit" id="${emailTemplate.id}"
                            class="btn btn-primary"><g:message
                            code="default.button.edit.label"/></g:link>
                    <g:link url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="emailTemplate"
                            data-instanceid="${emailTemplate.id}"
                            data-instancename="${emailTemplate.name}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></g:link>
                </div>
            </div> 
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${emailTemplate}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>