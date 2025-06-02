<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${rx.renderRRSettingsEntityName(type:params.type)}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
    <asset:javascript src="app/reportRequestTypeEdit.js"/>
</head>

<body>

<rx:container title="${message(code: 'default.show.label', args:[entityName])}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestTypeInstance}" var="theInstance"/>

    <div class="container-fluid">
        <g:render template="includes/form" model="['reportTemplateInstance': reportRequestTypeInstance]"/>
        <script>
            $(function () {
                $(".container-fluid input, .container-fluid select, .container-fluid textarea").attr("disabled", true);
                $(".formCascadeInputs").detach();
            });
        </script>
        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <a href="${createLink(controller:"reportRequestType", action:"edit")}?id=${reportRequestTypeInstance.id}&type=${params.type}"
                            class="btn btn-primary"><g:message
                            code="default.button.edit.label"/></a>
                    <g:link url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="reportRequestType"
                            data-instanceid="${reportRequestTypeInstance.id}"
                            data-instancename="${reportRequestTypeInstance.name}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></g:link>
                </div>
            </div>
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${reportRequestTypeInstance}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <input type="hidden" name="type" id="type" value="${params.type}">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>