<%@ page import="com.rxlogix.enums.TemplateTypeEnum" %>
<head>
    <meta name="layout" content="main"/>
    <title>
        <g:set var="templateType"
               value="${message(code: "app.templateType.${reportTemplateInstance?.templateType?.name()}")}"/>
        <g:message code="app.CreateTemplate.title" args="[templateType]"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var createCategoryUrl = "${createLink(controller: 'template', action: 'addCategory')}";
        var templateType = "${error?.templateType ? error?.templateType : ""}";
        var cllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL')}";
        var specificCllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: TemplateTypeEnum.CASE_LINE.name()])}";
        var specificCllCSQLTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLLCSQL')}";
        var customSQLTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: reportTemplateInstance?.templateType?.name()])}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var checkDrillDownUrl = "${createLink(controller: 'reportTemplateRest', action: 'checkDrillDown')}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
    </g:javascript>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/solid-gauge.js"/>
    <asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
    <asset:javascript src="vendorUi/easychart/ec.js"/>
    <asset:javascript src="vendorUi/easychart/ec_vapt.js"/>

    <asset:javascript src="vendorUi/underscore/underscore-observe.js"/>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/category.js"/>
    <asset:javascript src="app/template/editTemplate.js"/>
    <asset:javascript src="app/template/editColumns.js"/>
    <asset:javascript src="app/template/editColumnMeasure.js"/>
    <asset:javascript src="app/template/editMeasures.js"/>
    <asset:javascript src="app/template/editTemplateSet.js"/>
    <asset:javascript src="app/template/editChartTemplate.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="template.css" />
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="select2/select2-treeview.js"/>
</head>
<body>
<div class="content ">
    <div class="container ">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: "app.template.title",args:[templateType])}</h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div>
            %{--<rx:renderHeaderTitle code="app.template.title" args="[templateType]" />--}%
            <div class="mt-30">
                <rx:container title="${message(code: "app.label.createTemplate", args: [templateType])}">

                    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportTemplateInstance}" var="theInstance"/>

                    <div class="container-fluid">
                        <form id="templateForm" name="templateForm" action="${createLink(action: 'save')}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}" method="post" enctype="multipart/form-data">

                            <g:render template="form"
                                      model="['reportTemplateInstance': reportTemplateInstance, currentUser: currentUser, users: users, userGroups: userGroups, editable:editable, ciomsITemplate: reportTemplateInstance.ciomsITemplate || reportTemplateInstance.medWatchTemplate, sourceProfiles: sourceProfiles, actionType:'save']"/>

                            <div class="row">
                                <div class="col-xs-12 m-t-5">
                                    <div class="pull-right">
                                        <g:submitButton class="btn btn-primary" name="${message(code:'default.button.save.label')}" value="${message(code:'default.button.save.label')}"/>
                                        <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["template", "index"]}'
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
</div>
</body>
