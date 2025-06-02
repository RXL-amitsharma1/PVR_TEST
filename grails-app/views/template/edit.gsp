<%@ page import="com.rxlogix.enums.TemplateTypeEnum" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditTemplate.title" /></title>
    <g:javascript>
        var createCategoryUrl = "${createLink(controller: 'template', action: 'addCategory')}";
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var templateType = "${template.templateType}";
        var cllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL')}";
        var specificCllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: TemplateTypeEnum.CASE_LINE.name()])}";
        var customSQLTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: template.templateType.name()])}";
        var specificCllCSQLTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLLCSQL')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var hasConfigTemplateCreatorRole=false;
        var checkDrillDownUrl = "${createLink(controller: 'reportTemplateRest', action: 'checkDrillDown')}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var editPage = true;
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
    <asset:javascript src="app/template/editMeasures.js"/>
    <asset:javascript src="app/template/editColumnMeasure.js"/>
    <asset:javascript src="app/template/editTemplateSet.js"/>
    <asset:javascript src="app/template/editChartTemplate.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="template.css" />
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="select2/select2-treeview.js"/>
    <script>
        $(function () {
            var counter = $('#selectedCount');
            $("#selectedColumns").on('change', function() {
                counter.text($("#selectedColumns :selected").length);
            });
        });
    </script>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
    <rx:container title="${message(code: "app.label.editTemplate")}" bean="${template}">

        <g:render template="/includes/layout/flashErrorsDivs" bean="${template}" var="theInstance"/>

        <div class="container-fluid">
            <form id="templateForm" name="templateForm" action="${createLink(action: 'update')}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}" method="post" enctype="multipart/form-data">
                <g:hiddenField name="id" value="${template.id}" />

                <g:render template="form"
                          model="['reportTemplateInstance': template, 'edit': true, currentUser: currentUser, selectedLocale: selectedLocale,users: users, userGroups: userGroups, editable: editable, ciomsITemplate: template.ciomsITemplate || template.medWatchTemplate, sourceProfiles: sourceProfiles, actionType:'update']"/>

                <div class="row">
                    <div class="col-xs-12 mt-10">
                        <div class="pull-right">
                            <g:hiddenField name="edit" id="edit" value="${isAdmin}" />
                            <g:hiddenField name="templateId" id="templateId" value="${template.id}" />
                            <g:hiddenField name="version" id="version" value="${template?.version}" />
                            <g:submitButton class="btn btn-primary" name="${message(code:'default.button.update.label')}" value="${message(code:'default.button.update.label')}"/>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["template", "index"]}'
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
