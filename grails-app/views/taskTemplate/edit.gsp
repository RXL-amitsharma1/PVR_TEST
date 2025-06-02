<%@ page import="com.rxlogix.enums.TaskTemplateTypeEnum" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <asset:javascript src="app/taskTemplate.js"/>
    <meta name="layout" content="main">
    <g:if test="${taskTemplateInstance.type == TaskTemplateTypeEnum.AGGREGATE_REPORTS}">
        <g:set var="entityName" value="Report Task Template"/>
    </g:if>
    <g:elseif test="${taskTemplateInstance.type == TaskTemplateTypeEnum.PUBLISHER_SECTION}">
        <g:set var="entityName" value="Publisher Section Template"/>
    </g:elseif>
    <g:else>
        <g:set var="entityName" value="Report Request Template" />
    </g:else>
    <title><g:message code="app.taskTemplate.edit.title"/></title>

    <style>
    .form-horizontal .form-group {
        margin-right: -5px;
        margin-left: -5px;
    }
    </style>



</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5 class="page-header-settings"><g:message code="default.edit.label" args="[entityName]"/></h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div  class="settings-content">
<rx:container title="${entityName}" >

    <g:render template="/includes/layout/flashErrorsDivs" bean="${taskTemplateInstance}" var="theInstance"/>

    <g:form method="post" action="update" class="form-horizontal">

        <g:if test="${taskTemplateInstance.type== TaskTemplateTypeEnum.AGGREGATE_REPORTS}">
            <g:render template="includes/formReportTask" model="['mode':'edit', taskTemplateInstance:taskTemplateInstance]" />
        </g:if>
        <g:elseif test="${taskTemplateInstance.type== TaskTemplateTypeEnum.PUBLISHER_SECTION}">
            <g:render template="includes/formPublisherSectionTask" model="['mode':'edit', taskTemplateInstance:taskTemplateInstance]" />
        </g:elseif>
        <g:else>
            <g:render template="includes/form" model="['mode':'edit', taskTemplateInstance:taskTemplateInstance]" />
        </g:else>
<div class="row">
    <div class="col-md-12">
        <div class="buttonBar">
            <div class="pull-right">
                <button name="edit" class="btn btn-primary">
                    ${message(code: 'default.button.update.label')}
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["taskTemplate", "index"]}'
                        id="cancelButton">${message(code: "default.button.cancel.label")}</button>
            </div>
        </div>
    </div>
</div>

    </g:form>

</rx:container>
        </div>
    </div>
</div>

</body>
</html>