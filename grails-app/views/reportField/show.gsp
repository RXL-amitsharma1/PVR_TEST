<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.CustomMessageService" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.reportField.appName")}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: 'default.show.label', args:[entityName])}">

                <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFieldInstance}" var="theInstance"/>

                <div class="container-fluid">
                    <div class="row">
                        <div class="col-xs-12">
                            <div class="row">
                                <div class="col-xs-6">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.customField.field"/></label>

                                            <div>${reportFieldInstance.name}</div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.description"/></label>
                                            <div>${reportFieldInstance.description}</div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.customField.fieldGroup"/></label>

                                            <div><g:message code="app.reportFieldGroup.${reportFieldInstance.fieldGroup.name}"/></div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.reportField.override"/></label>

                                            <div><g:formatBoolean boolean="${reportFieldInstance?.override}"
                                                                  true="${message(code: "default.button.yes.label")}"
                                                                  false="${message(code: "default.button.no.label")}"/></div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.reportField.querySelectable"/></label>

                                            <div><g:formatBoolean boolean="${reportFieldInstance?.querySelectable}"
                                                                  true="${message(code: "default.button.yes.label")}"
                                                                  false="${message(code: "default.button.no.label")}"/></div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.reportField.templateCLLSelectable"/></label>

                                            <div><g:formatBoolean boolean="${reportFieldInstance?.templateCLLSelectable}"
                                                                  true="${message(code: "default.button.yes.label")}"
                                                                  false="${message(code: "default.button.no.label")}"/></div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.reportField.templateDTRowSelectable"/></label>

                                            <div><g:formatBoolean boolean="${reportFieldInstance?.templateDTRowSelectable}"
                                                                  true="${message(code: "default.button.yes.label")}"
                                                                  false="${message(code: "default.button.no.label")}"/></div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.reportField.templateDTColumnSelectable"/></label>

                                            <div><g:formatBoolean boolean="${reportFieldInstance?.templateDTColumnSelectable}"
                                                                  true="${message(code: "default.button.yes.label")}"
                                                                  false="${message(code: "default.button.no.label")}"/></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <div class="pull-right">
                                <g:link controller="reportField" action="edit" id="${reportFieldInstance.id}"
                                        class="btn btn-primary"><g:message
                                        code="default.button.edit.label"/></g:link>
                            </div>
                        </div> 
                    </div>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
</html>