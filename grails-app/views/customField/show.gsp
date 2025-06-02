<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.customField.appName")}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
</head>

<body>

<rx:container title="${message(code: 'default.show.label', args:[entityName])}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${customFieldInstance}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-6">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.name" /></label>

                                <div>${customFieldInstance.customName}</div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.description"/></label>

                                <div>${customFieldInstance.customDescription}</div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.customField.field"/></label>

                                <div><g:message code="app.reportField.${customFieldInstance.reportField.name}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.customField.fieldGroup"/></label>

                                <div><g:message code="app.reportFieldGroup.${customFieldInstance.fieldGroup.name}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.customField.templateCLLSelectable"/></label>

                                <div><g:formatBoolean boolean="${customFieldInstance?.templateCLLSelectable}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.customField.templateDTRowSelectable"/></label>

                                <div><g:formatBoolean boolean="${customFieldInstance?.templateDTRowSelectable}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.customField.templateDTColumnSelectable"/></label>

                                <div><g:formatBoolean boolean="${customFieldInstance?.templateDTColumnSelectable}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>


                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.template.customExpression"/></label>

                                <div>${customFieldInstance.defaultExpression}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <g:link controller="customField" action="edit" id="${customFieldInstance.id}"
                            class="btn btn-primary"><g:message
                            code="default.button.edit.label"/></g:link>
                    <g:link url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="customField"
                            data-instanceid="${customFieldInstance.id}"
                            data-instancename="${customFieldInstance.name}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></g:link>
                </div>
            </div> 
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${customFieldInstance}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>